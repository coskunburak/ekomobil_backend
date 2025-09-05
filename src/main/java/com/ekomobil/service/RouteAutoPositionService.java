package com.ekomobil.service;

import com.ekomobil.api.rest.publicapi.RouteAutoPositionController;
import com.ekomobil.domain.entity.Route;
import com.ekomobil.domain.entity.RouteStop;
import com.ekomobil.domain.entity.Stop;
import com.ekomobil.error.NotFoundException;
import com.ekomobil.repo.RouteRepository;
import com.ekomobil.repo.RouteStopRepository;
import com.ekomobil.repo.StopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Rota polyline'ı boyunca durakların koordinatlarını otomatik yerleştirir.
 * - 1. tercih: Google Encoded Polyline (route.polyline)
 * - 2. tercih: Düz metin "lat,lon|lat,lon|..." veya noktalı virgül/whitespace ayracı
 * - 3. tercih: Mevcut non-zero duraklardan path inşa et
 *
 * Idempotent: overwrite=false && onlyZero=true ise yalnızca (0,0) olanları günceller.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteAutoPositionService {

    private static final double EPS = 1e-6;                // lat/lon eşik
    private static final double MERGE_TOL_METERS = 0.9;    // path'te 1m altı noktaları birleştir
    private static final DecimalFormat SIX = new DecimalFormat("0.000000");

    private final RouteRepository routeRepo;
    private final RouteStopRepository routeStopRepo;
    private final StopRepository stopRepo;

    /**
     * @param routeId        rota id
     * @param spacingMeters  >0 ise hedef aralık. Total uzunluk kısa ise eşit dağıtıma düşer.
     * @param overwrite      true ise mevcut lat/lon'ları da ez
     * @param onlyZero       true ise sadece (0,0) olanlara yaz (overwrite=false iken varsayılan)
     */
    @Transactional
    public RouteAutoPositionController.AutoPositionResult
    autoposition(long routeId, int spacingMeters, boolean overwrite, boolean onlyZero) {

        long t0 = System.currentTimeMillis();
        Route route = routeRepo.findById(routeId)
                .orElseThrow(() -> new NotFoundException("Rota bulunamadı: " + routeId));

        // Path üret (polyline -> ham text -> non-zero duraklar)
        List<double[]> path = safeBuildPath(route);
        if (path.size() < 2) {
            throw new IllegalStateException("Yol üretilemedi: en az 2 nokta gerekiyor (routeId=" + routeId + ")");
        }

        // Kümülatif mesafe hesapla
        double[] segLen = new double[path.size() - 1];
        double[] pref = new double[path.size()];
        double total = 0;
        for (int i = 0; i < segLen.length; i++) {
            segLen[i] = Geo.haversineMeters(path.get(i)[0], path.get(i)[1], path.get(i + 1)[0], path.get(i + 1)[1]);
            total += segLen[i];
            pref[i + 1] = total;
        }

        // Rota durakları
        List<RouteStop> ordered = routeStopRepo.findByRouteIdOrderByOrderNo(routeId);
        int n = ordered.size();
        if (n == 0) {
            return new RouteAutoPositionController.AutoPositionResult(routeId, 0, 0, 0);
        }

        // Hedef mesafeler: eşit dağıtım veya spacingMeters (kısa rotada eşit dağıtıma düşer)
        double[] targets = new double[n];
        if (n == 1) {
            targets[0] = total / 2.0;
        } else if (spacingMeters > 1 && total >= (n - 1) * 2.0 /* çok kısa değilse */) {
            double step = Math.min(spacingMeters, total / (n - 1.0)); // tüm rotaya yay
            for (int i = 0; i < n; i++) {
                double t = i * step;
                if (t > total) t = total;
                targets[i] = t;
            }
        } else {
            // default: eşit dağıt
            double step = total / (n - 1.0);
            for (int i = 0; i < n; i++) targets[i] = i * step;
        }

        // Yazma
        int updated = 0, skipped = 0;
        for (int i = 0; i < n; i++) {
            Stop s = ordered.get(i).getStop();

            boolean zeroish = (s.getLat() == null || s.getLon() == null
                    || (Math.abs(s.getLat()) < EPS && Math.abs(s.getLon()) < EPS));

            if (!overwrite) {
                if (onlyZero && !zeroish) { skipped++; continue; }
                if (!onlyZero && !zeroish) { skipped++; continue; }
            }

            double[] pos = interpolateAlong(path, pref, targets[i]);
            double lat = round6(pos[0]);
            double lon = round6(pos[1]);

            // aynı ise yazma
            if (!zeroish && s.getLat() != null && s.getLon() != null
                    && Math.abs(s.getLat() - lat) < EPS && Math.abs(s.getLon() - lon) < EPS) {
                skipped++; continue;
            }

            s.setLat(lat);
            s.setLon(lon);
            stopRepo.save(s);
            updated++;
        }

        long dt = System.currentTimeMillis() - t0;
        log.info("autoposition routeId={} stops={} updated={} skipped={} total={}m in {}ms",
                routeId, n, updated, skipped, Math.round(total), dt);

        return new RouteAutoPositionController.AutoPositionResult(routeId, n, updated, skipped);
    }

    /* ---------- PATH OLUŞTURMA (robust) ---------- */

    private List<double[]> safeBuildPath(Route route) {
        String pl = route.getPolyline();

        // a) Google Encoded Polyline
        if (pl != null && !pl.isBlank()) {
            try {
                List<double[]> dec = Polyline.decode(pl.trim());
                List<double[]> compact = dedupeClose(dec, MERGE_TOL_METERS);
                if (compact.size() >= 2) return compact;
            } catch (Exception ex) {
                log.warn("Polyline decode başarısız (encoded varsayıldı): {}", ex.getMessage());
            }

            // b) Ham metin "lat,lon|lat,lon|..." (| ; \n whitespace destekli)
            List<double[]> txt = tryParsePlainLatLonPath(pl);
            if (txt.size() >= 2) {
                return dedupeClose(txt, MERGE_TOL_METERS);
            }
        }

        // c) Non-zero duraklardan path
        List<RouteStop> ordered = routeStopRepo.findByRouteIdOrderByOrderNo(route.getId());
        List<double[]> fromStops = new ArrayList<>();
        for (RouteStop rs : ordered) {
            Stop s = rs.getStop();
            if (s.getLat() != null && s.getLon() != null
                    && Math.abs(s.getLat()) > EPS && Math.abs(s.getLon()) > EPS) {
                fromStops.add(new double[]{s.getLat(), s.getLon()});
            }
        }
        return dedupeClose(fromStops, MERGE_TOL_METERS);
    }

    private static List<double[]> tryParsePlainLatLonPath(String raw) {
        List<double[]> pts = new ArrayList<>();
        String s = raw.trim();
        if (s.isEmpty()) return pts;

        // normalize
        s = s.replace(';', '|')
                .replace('\n', '|')
                .replace('\r', '|')
                .replace('\t', ' ')
                .replace(" ", "");

        // also allow JSON-like: [lat,lon],[lat,lon]
        s = s.replace("[", "").replace("]", "");

        String[] pairs = s.split("\\|");
        if (pairs.length == 1 && pairs[0].contains(";")) {
            pairs = s.split(";");
        }
        for (String p : pairs) {
            if (p.isEmpty()) continue;
            String[] xy = p.split(",");
            if (xy.length != 2) continue;
            try {
                double lat = Double.parseDouble(xy[0]);
                double lon = Double.parseDouble(xy[1]);
                pts.add(new double[]{lat, lon});
            } catch (NumberFormatException ignore) { /* skip */ }
        }
        return pts;
    }

    private static List<double[]> dedupeClose(List<double[]> in, double tolMeters) {
        if (in == null || in.isEmpty()) return new ArrayList<>();
        List<double[]> out = new ArrayList<>();
        double[] prev = null;
        for (double[] p : in) {
            if (prev == null) {
                out.add(p);
                prev = p;
            } else {
                double d = Geo.haversineMeters(prev[0], prev[1], p[0], p[1]);
                if (d >= tolMeters) {
                    out.add(p);
                    prev = p;
                }
            }
        }
        return out;
    }

    /* ---------- GEOMETRİ ---------- */

    private static double[] interpolateAlong(List<double[]> path, double[] pref, double target) {
        int last = pref.length - 1;
        if (target <= 0) return path.get(0);
        if (target >= pref[last]) return path.get(path.size() - 1);

        int lo = 0, hi = last;
        while (lo < hi - 1) {
            int mid = (lo + hi) >>> 1;
            if (pref[mid] <= target) lo = mid; else hi = mid;
        }
        double segStart = pref[lo];
        double segLen = pref[lo + 1] - segStart;
        double alpha = segLen > 0 ? (target - segStart) / segLen : 0.0;

        double[] A = path.get(lo);
        double[] B = path.get(lo + 1);
        return new double[]{
                A[0] + (B[0] - A[0]) * alpha,
                A[1] + (B[1] - A[1]) * alpha
        };
    }

    private static double round6(double v) {
        // Locale bağımsız 6 ondalık
        synchronized (SIX) { return Double.parseDouble(SIX.format(v).replace(',', '.')); }
    }

    /* ---------- UTIL ---------- */

    static final class Geo {
        static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
            final double R = 6371000.0;
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lon2 - lon1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                    * Math.sin(dLon / 2) * Math.sin(dLon / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            return R * c;
        }
    }

    /**
     * Google Encoded Polyline decoder – taşmalara karşı sağlamlaştırıldı.
     * Malformed veride IllegalArgumentException fırlatır (catch edip fallback'e geçiyoruz).
     */
    static final class Polyline {
        static List<double[]> decode(String poly) {
            List<double[]> pts = new ArrayList<>();
            int index = 0, lat = 0, lng = 0, len = poly.length();
            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    if (index >= len) throw new IllegalArgumentException("Malformed polyline (lat)");
                    b = poly.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0; result = 0;
                do {
                    if (index >= len) throw new IllegalArgumentException("Malformed polyline (lon)");
                    b = poly.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                pts.add(new double[]{lat / 1e5, lng / 1e5});
            }
            return pts;
        }
    }
}
