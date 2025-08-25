CREATE TABLE route_stop (
  route_id BIGINT NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
  stop_id  BIGINT NOT NULL REFERENCES stops(id) ON DELETE CASCADE,
  order_index INT NOT NULL CHECK (order_index >= 0),
  dwell_seconds INT NOT NULL DEFAULT 0 CHECK (dwell_seconds >= 0),
  PRIMARY KEY (route_id, stop_id),
  UNIQUE (route_id, order_index)
);
