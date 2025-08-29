CREATE UNIQUE INDEX IF NOT EXISTS uq_stops_code
  ON stops(code)
  WHERE code IS NOT NULL;
