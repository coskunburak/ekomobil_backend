package com.ekomobil.service;

import com.ekomobil.domain.dto.SearchItemDto;
import com.ekomobil.repo.RouteRepository;
import com.ekomobil.repo.StopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;

    public List<SearchItemDto> search(String q, int limit) {
        final String query = q == null ? "" : q.trim();
        final int lim = Math.max(1, Math.min(limit, 50)); // 1..50
        final var page = PageRequest.of(0, lim);

        List<SearchItemDto> out = new ArrayList<>();

        routeRepository.search(query, page).forEach(r ->
                out.add(SearchItemDto.builder()
                        .type("route")
                        .id(r.getId())
                        .name(r.getName())
                        .code(r.getCode())
                        .build())
        );

        stopRepository.search(query, page).forEach(s ->
                out.add(SearchItemDto.builder()
                        .type("stop")
                        .id(s.getId())
                        .name(s.getName())
                        .build())
        );

        if (out.size() > lim) {
            return out.subList(0, lim);
        }
        return out;
    }
}
