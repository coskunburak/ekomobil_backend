-- V2__create_stops.sql
CREATE TABLE IF NOT EXISTS public.stops (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    lat DOUBLE PRECISION NOT NULL,
    lon DOUBLE PRECISION NOT NULL,
    description VARCHAR(500)
);
