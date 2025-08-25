-- V4__vehicle_position.sql
-- idempotent: tablo zaten varsa hata vermez
CREATE TABLE IF NOT EXISTS vehicle_position (
  id BIGSERIAL PRIMARY KEY,
  bus_id BIGINT NOT NULL REFERENCES buses(id) ON DELETE CASCADE,
  lat DOUBLE PRECISION NOT NULL CHECK (lat BETWEEN -90 AND 90),
  lon DOUBLE PRECISION NOT NULL CHECK (lon BETWEEN -180 AND 180),
  speed DOUBLE PRECISION NULL CHECK (speed >= 0),
  heading DOUBLE PRECISION NULL CHECK (heading >= 0 AND heading <= 360),
  ts TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_vehicle_position_bus_id ON vehicle_position(bus_id);
