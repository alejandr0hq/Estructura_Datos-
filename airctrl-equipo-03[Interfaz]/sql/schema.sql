-- AIRCTRL: esquema de PostgreSQL (referencia).
-- La aplicacion lo crea sola al conectarse; este archivo sirve para revisarlo
-- en DataGrip/psql o para crearlo a mano. Tablas sin llaves foraneas a proposito:
-- los CSV originales traen referencias incompletas y se reportan como advertencias.

CREATE TABLE IF NOT EXISTS airctrl_meta (
    meta_key   VARCHAR(40) PRIMARY KEY,
    meta_value VARCHAR(200) NOT NULL
);

CREATE TABLE IF NOT EXISTS aircraft (
    seq         BIGSERIAL UNIQUE,
    aircraft_id VARCHAR(20) PRIMARY KEY,
    model       VARCHAR(80) NOT NULL,
    capacity    INTEGER NOT NULL CHECK (capacity > 0),
    status      VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS gates (
    seq            BIGSERIAL UNIQUE,
    gate_id        VARCHAR(20) PRIMARY KEY,
    terminal       VARCHAR(10) NOT NULL,
    status         VARCHAR(20) NOT NULL,
    current_flight VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS flights (
    seq         BIGSERIAL UNIQUE,
    flight_id   VARCHAR(20) PRIMARY KEY,
    airline     VARCHAR(60) NOT NULL,
    origin      VARCHAR(10) NOT NULL,
    destination VARCHAR(10) NOT NULL,
    aircraft_id VARCHAR(20) NOT NULL,
    gate        VARCHAR(20) NOT NULL DEFAULT '',
    status      VARCHAR(20) NOT NULL,
    priority    INTEGER NOT NULL CHECK (priority BETWEEN 1 AND 5)
);

CREATE TABLE IF NOT EXISTS baggage (
    seq            BIGSERIAL UNIQUE,
    bag_id         VARCHAR(20) PRIMARY KEY,
    flight_id      VARCHAR(20) NOT NULL,
    passenger_code VARCHAR(20) NOT NULL,
    status         VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS connections (
    id              BIGSERIAL PRIMARY KEY,
    source_location VARCHAR(30) NOT NULL,
    target_location VARCHAR(30) NOT NULL,
    distance_m      INTEGER
);

CREATE TABLE IF NOT EXISTS incidents (
    incident_id VARCHAR(20) PRIMARY KEY,
    type        VARCHAR(40) NOT NULL,
    description TEXT NOT NULL,
    priority    INTEGER NOT NULL CHECK (priority BETWEEN 1 AND 5),
    created_at  TIMESTAMP NOT NULL,
    resolved    BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS operation_log (
    seq          BIGSERIAL PRIMARY KEY,
    kind         VARCHAR(10) NOT NULL CHECK (kind IN ('FLIGHT', 'INCIDENT')),
    ref_id       VARCHAR(20) NOT NULL,
    priority     INTEGER NOT NULL,
    processed_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS import_warnings (
    id      BIGSERIAL PRIMARY KEY,
    message TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_flights_status ON flights (status);

CREATE INDEX IF NOT EXISTS idx_baggage_flight ON baggage (flight_id);

-- Consultas utiles
-- SELECT * FROM operation_log ORDER BY seq DESC;          -- historial atendido
-- SELECT status, COUNT(*) FROM flights GROUP BY status;   -- vuelos por estado
-- SELECT * FROM baggage WHERE status = 'MISSING';          -- equipaje extraviado
