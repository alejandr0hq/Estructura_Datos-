package airctrl.data;

import airctrl.model.Aircraft;
import airctrl.model.Baggage;
import airctrl.model.Connection;
import airctrl.model.Flight;
import airctrl.model.Gate;
import airctrl.model.Incident;
import airctrl.model.Operation;
import airctrl.service.AirCtrlException;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

/**
 * Repositorio PostgreSQL. Crea la base y el esquema si no existen, importa los CSV
 * la primera vez y guarda cada cambio que hace AirCtrlSystem.
 */
public final class PostgresRepository implements Persistence, AutoCloseable {
    private static final String[] SCHEMA = {
            """
            CREATE TABLE IF NOT EXISTS airctrl_meta (
                meta_key   VARCHAR(40) PRIMARY KEY,
                meta_value VARCHAR(200) NOT NULL
            )""",
            """
            CREATE TABLE IF NOT EXISTS aircraft (
                seq         BIGSERIAL UNIQUE,
                aircraft_id VARCHAR(20) PRIMARY KEY,
                model       VARCHAR(80) NOT NULL,
                capacity    INTEGER NOT NULL CHECK (capacity > 0),
                status      VARCHAR(20) NOT NULL
            )""",
            """
            CREATE TABLE IF NOT EXISTS gates (
                seq            BIGSERIAL UNIQUE,
                gate_id        VARCHAR(20) PRIMARY KEY,
                terminal       VARCHAR(10) NOT NULL,
                status         VARCHAR(20) NOT NULL,
                current_flight VARCHAR(20)
            )""",
            """
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
            )""",
            """
            CREATE TABLE IF NOT EXISTS baggage (
                seq            BIGSERIAL UNIQUE,
                bag_id         VARCHAR(20) PRIMARY KEY,
                flight_id      VARCHAR(20) NOT NULL,
                passenger_code VARCHAR(20) NOT NULL,
                status         VARCHAR(20) NOT NULL
            )""",
            """
            CREATE TABLE IF NOT EXISTS connections (
                id              BIGSERIAL PRIMARY KEY,
                source_location VARCHAR(30) NOT NULL,
                target_location VARCHAR(30) NOT NULL,
                distance_m      INTEGER
            )""",
            """
            CREATE TABLE IF NOT EXISTS incidents (
                incident_id VARCHAR(20) PRIMARY KEY,
                type        VARCHAR(40) NOT NULL,
                description TEXT NOT NULL,
                priority    INTEGER NOT NULL CHECK (priority BETWEEN 1 AND 5),
                created_at  TIMESTAMP NOT NULL,
                resolved    BOOLEAN NOT NULL DEFAULT FALSE
            )""",
            """
            CREATE TABLE IF NOT EXISTS operation_log (
                seq          BIGSERIAL PRIMARY KEY,
                kind         VARCHAR(10) NOT NULL CHECK (kind IN ('FLIGHT', 'INCIDENT')),
                ref_id       VARCHAR(20) NOT NULL,
                priority     INTEGER NOT NULL,
                processed_at TIMESTAMP NOT NULL
            )""",
            """
            CREATE TABLE IF NOT EXISTS import_warnings (
                id      BIGSERIAL PRIMARY KEY,
                message TEXT NOT NULL
            )""",
            "CREATE INDEX IF NOT EXISTS idx_flights_status ON flights (status)",
            "CREATE INDEX IF NOT EXISTS idx_baggage_flight ON baggage (flight_id)"
    };

    private final DbConfig config;
    private final java.sql.Connection connection;

    private PostgresRepository(DbConfig config, java.sql.Connection connection) {
        this.config = config;
        this.connection = connection;
    }

    /** Se conecta; si la base no existe la crea. Luego asegura el esquema. */
    public static PostgresRepository open(DbConfig config) throws SQLException {
        ensureDriver();
        java.sql.Connection connection;
        try {
            connection = DriverManager.getConnection(config.jdbcUrl(), config.getUser(), config.getPassword());
        } catch (SQLException exception) {
            if (!"3D000".equals(exception.getSQLState())) {
                throw exception;
            }
            createDatabase(config);
            connection = DriverManager.getConnection(config.jdbcUrl(), config.getUser(), config.getPassword());
        }
        PostgresRepository repository = new PostgresRepository(config, connection);
        repository.createSchema();
        return repository;
    }

    private static void ensureDriver() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("No se encontro el driver JDBC de PostgreSQL. "
                    + "Coloca postgresql.jar en la carpeta lib/ (los scripts run.sh y run.bat lo descargan).",
                    "08001", exception);
        }
    }

    private static void createDatabase(DbConfig config) throws SQLException {
        String name = config.getDatabase();
        if (!name.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new SQLException("Nombre de base de datos no valido: " + name);
        }
        try (java.sql.Connection admin = DriverManager.getConnection(
                config.jdbcUrl("postgres"), config.getUser(), config.getPassword());
             Statement statement = admin.createStatement()) {
            statement.executeUpdate("CREATE DATABASE " + name);
        }
    }

    private void createSchema() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (String sql : SCHEMA) {
                statement.execute(sql);
            }
        }
    }

    public boolean isSeeded() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM airctrl_meta WHERE meta_key = 'seeded'");
             ResultSet rs = statement.executeQuery()) {
            return rs.next();
        }
    }

    /** Importa los CSV si la base esta vacia. Devuelve true si importo. */
    public boolean seedIfEmpty(Path dataDirectory) throws SQLException, IOException {
        if (isSeeded()) {
            return false;
        }
        importCsv(dataDirectory);
        return true;
    }

    /** Borra todo y vuelve a importar los CSV originales. */
    public void resetFromCsv(Path dataDirectory) throws SQLException, IOException {
        importCsv(dataDirectory);
    }

    private void importCsv(Path dataDirectory) throws SQLException, IOException {
        DataStore raw = new CsvRepository(dataDirectory).loadRaw();
        inTransaction(() -> {
            try (Statement statement = connection.createStatement()) {
                statement.execute("TRUNCATE aircraft, gates, flights, baggage, connections, incidents, "
                        + "operation_log, import_warnings, airctrl_meta RESTART IDENTITY");
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO aircraft (aircraft_id, model, capacity, status) VALUES (?, ?, ?, ?)")) {
                for (Aircraft a : raw.getAircraft().values()) {
                    ps.setString(1, a.aircraftId());
                    ps.setString(2, a.model());
                    ps.setInt(3, a.capacity());
                    ps.setString(4, a.status());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO gates (gate_id, terminal, status, current_flight) VALUES (?, ?, ?, ?)")) {
                for (Gate g : raw.getGates().values()) {
                    ps.setString(1, g.getGateId());
                    ps.setString(2, g.getTerminal());
                    ps.setString(3, g.getStatus());
                    ps.setString(4, g.getCurrentFlight());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO flights (flight_id, airline, origin, destination, aircraft_id, gate, status, priority) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                for (Flight f : raw.getFlights()) {
                    bindFlight(ps, f);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO baggage (bag_id, flight_id, passenger_code, status) VALUES (?, ?, ?, ?)")) {
                for (Baggage b : raw.getBaggage().values()) {
                    ps.setString(1, b.bagId());
                    ps.setString(2, b.flightId());
                    ps.setString(3, b.passengerCode());
                    ps.setString(4, b.status());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO connections (source_location, target_location, distance_m) VALUES (?, ?, ?)")) {
                for (Connection c : raw.getConnections()) {
                    ps.setString(1, c.sourceLocation());
                    ps.setString(2, c.targetLocation());
                    if (c.distanceMeters() == null) {
                        ps.setNull(3, Types.INTEGER);
                    } else {
                        ps.setInt(3, c.distanceMeters());
                    }
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO import_warnings (message) VALUES (?)")) {
                for (String warning : raw.getWarnings()) {
                    ps.setString(1, warning);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO airctrl_meta (meta_key, meta_value) VALUES ('seeded', ?)")) {
                ps.setString(1, LocalDateTime.now().toString());
                ps.executeUpdate();
            }
        });
    }

    /** Lee toda la informacion y la deja en un DataStore listo para AirCtrlSystem. */
    public DataStore load() throws SQLException {
        DataStore store = new DataStore();
        try (Statement st = connection.createStatement()) {
            try (ResultSet rs = st.executeQuery(
                    "SELECT aircraft_id, model, capacity, status FROM aircraft ORDER BY seq")) {
                while (rs.next()) {
                    Aircraft a = new Aircraft(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4));
                    store.getAircraft().put(a.aircraftId(), a);
                }
            }
            try (ResultSet rs = st.executeQuery(
                    "SELECT gate_id, terminal, status, current_flight FROM gates ORDER BY seq")) {
                while (rs.next()) {
                    Gate g = new Gate(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
                    store.getGates().put(g.getGateId(), g);
                }
            }
            try (ResultSet rs = st.executeQuery(
                    "SELECT flight_id, airline, origin, destination, aircraft_id, gate, status, priority "
                            + "FROM flights ORDER BY seq")) {
                while (rs.next()) {
                    store.getFlights().addLast(new Flight(rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getInt(8)));
                }
            }
            try (ResultSet rs = st.executeQuery(
                    "SELECT bag_id, flight_id, passenger_code, status FROM baggage ORDER BY seq")) {
                while (rs.next()) {
                    Baggage b = new Baggage(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
                    store.getBaggage().put(b.bagId(), b);
                }
            }
            try (ResultSet rs = st.executeQuery(
                    "SELECT source_location, target_location, distance_m FROM connections ORDER BY id")) {
                while (rs.next()) {
                    int distance = rs.getInt(3);
                    Integer value = rs.wasNull() ? null : distance;
                    store.getConnections().add(new Connection(rs.getString(1), rs.getString(2), value));
                }
            }
            try (ResultSet rs = st.executeQuery(
                    "SELECT incident_id, type, description, priority, created_at, resolved "
                            + "FROM incidents ORDER BY created_at")) {
                while (rs.next()) {
                    Incident i = new Incident(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4),
                            toLocal(rs.getTimestamp(5)), rs.getBoolean(6));
                    store.getIncidents().put(i.getIncidentId(), i);
                }
            }
            try (ResultSet rs = st.executeQuery(
                    "SELECT kind, ref_id, priority, processed_at FROM operation_log ORDER BY seq")) {
                while (rs.next()) {
                    store.getProcessedLog().add(new ProcessedRecord(rs.getString(1), rs.getString(2),
                            rs.getInt(3), toLocal(rs.getTimestamp(4))));
                }
            }
            try (ResultSet rs = st.executeQuery("SELECT message FROM import_warnings ORDER BY id")) {
                while (rs.next()) {
                    store.getWarnings().add(rs.getString(1));
                }
            }
        }
        CsvRepository.validateReferences(store);
        return store;
    }

    // ---------------------------------------------------------------- Persistence

    @Override
    public void insertFlight(Flight flight, String occupiedGateId) {
        write(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO flights (flight_id, airline, origin, destination, aircraft_id, gate, status, priority) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                bindFlight(ps, flight);
                ps.executeUpdate();
            }
            if (occupiedGateId != null) {
                updateGateRow(occupiedGateId, "OCCUPIED", flight.getFlightId());
            }
        });
    }

    @Override
    public void insertAircraft(Aircraft aircraft) {
        write(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO aircraft (aircraft_id, model, capacity, status) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, aircraft.aircraftId());
                ps.setString(2, aircraft.model());
                ps.setInt(3, aircraft.capacity());
                ps.setString(4, aircraft.status());
                ps.executeUpdate();
            }
        });
    }

    @Override
    public void insertBaggage(Baggage baggage) {
        write(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO baggage (bag_id, flight_id, passenger_code, status) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, baggage.bagId());
                ps.setString(2, baggage.flightId());
                ps.setString(3, baggage.passengerCode());
                ps.setString(4, baggage.status());
                ps.executeUpdate();
            }
        });
    }

    @Override
    public void updateBaggageStatus(String bagId, String status) {
        write(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE baggage SET status = ? WHERE bag_id = ?")) {
                ps.setString(1, status);
                ps.setString(2, bagId);
                ps.executeUpdate();
            }
        });
    }

    @Override
    public void insertIncident(Incident incident) {
        write(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO incidents (incident_id, type, description, priority, created_at, resolved) "
                            + "VALUES (?, ?, ?, ?, ?, ?)")) {
                ps.setString(1, incident.getIncidentId());
                ps.setString(2, incident.getType());
                ps.setString(3, incident.getDescription());
                ps.setInt(4, incident.getPriority());
                ps.setTimestamp(5, Timestamp.valueOf(incident.getCreatedAt()));
                ps.setBoolean(6, incident.isResolved());
                ps.executeUpdate();
            }
        });
    }

    @Override
    public void updateGate(String gateId, String status, String currentFlight) {
        write(() -> updateGateRow(gateId, status, currentFlight));
    }

    @Override
    public void assignGate(String flightId, String newGateId, String releasedGateId) {
        write(() -> {
            if (releasedGateId != null) {
                updateGateRow(releasedGateId, "AVAILABLE", null);
            }
            updateGateRow(newGateId, "OCCUPIED", flightId);
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE flights SET gate = ? WHERE flight_id = ?")) {
                ps.setString(1, newGateId);
                ps.setString(2, flightId);
                ps.executeUpdate();
            }
        });
    }

    @Override
    public void recordProcessed(Operation operation, LocalDateTime processedAt) {
        write(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO operation_log (kind, ref_id, priority, processed_at) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, operation.isFlightOperation() ? "FLIGHT" : "INCIDENT");
                ps.setString(2, operation.getReferenceId());
                ps.setInt(3, operation.getPriority());
                ps.setTimestamp(4, Timestamp.valueOf(processedAt));
                ps.executeUpdate();
            }
            if (!operation.isFlightOperation()) {
                try (PreparedStatement ps = connection.prepareStatement(
                        "UPDATE incidents SET resolved = TRUE WHERE incident_id = ?")) {
                    ps.setString(1, operation.getIncident().getIncidentId());
                    ps.executeUpdate();
                }
            }
        });
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    public String describe() {
        return "PostgreSQL " + config.getDatabase() + " en " + config.getHost() + ":" + config.getPort();
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException ignored) {
            // Se esta cerrando la aplicacion.
        }
    }

    // ---------------------------------------------------------------- helpers

    private void updateGateRow(String gateId, String status, String currentFlight) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE gates SET status = ?, current_flight = ? WHERE gate_id = ?")) {
            ps.setString(1, status);
            ps.setString(2, currentFlight);
            ps.setString(3, gateId);
            ps.executeUpdate();
        }
    }

    private static void bindFlight(PreparedStatement ps, Flight f) throws SQLException {
        ps.setString(1, f.getFlightId());
        ps.setString(2, f.getAirline());
        ps.setString(3, f.getOrigin());
        ps.setString(4, f.getDestination());
        ps.setString(5, f.getAircraftId());
        ps.setString(6, f.getGate() == null ? "" : f.getGate());
        ps.setString(7, f.getStatus());
        ps.setInt(8, f.getPriority());
    }

    private static LocalDateTime toLocal(Timestamp timestamp) {
        return timestamp == null ? LocalDateTime.now() : timestamp.toLocalDateTime();
    }

    private void write(SqlWork work) {
        try {
            inTransaction(work);
        } catch (SQLException exception) {
            throw new AirCtrlException("No se pudo guardar en PostgreSQL: " + exception.getMessage(), exception);
        }
    }

    private void inTransaction(SqlWork work) throws SQLException {
        boolean previous = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            work.run();
            connection.commit();
        } catch (SQLException | RuntimeException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(previous);
        }
    }

    @FunctionalInterface
    private interface SqlWork {
        void run() throws SQLException;
    }
}
