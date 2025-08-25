-- V2__create_stops.sql
CREATE TABLE IF NOT EXISTS public.stops (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    lat DOUBLE PRECISION NOT NULL,
    lon DOUBLE PRECISION NOT NULL,
    description VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_stops_name ON public.stops (name);
CREATE INDEX IF NOT EXISTS idx_stops_lat_lon ON public.stops (lat, lon);

CREATE TABLE routes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIM
)