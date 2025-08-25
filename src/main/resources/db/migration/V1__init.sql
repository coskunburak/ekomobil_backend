create table stop(
  id bigserial primary key,
  name text not null,
  lat double precision not null,
  lon double precision not null
);

create table vehicle_position(
  id bigserial primary key,
  bus_id bigint not null,
  ts timestamptz not null,
  lat double precision not null,
  lon double precision not null,
  speed double precision,
  heading double precision
);
create index vehicle_position_bus_ts_idx on vehicle_position(bus_id, ts desc);
