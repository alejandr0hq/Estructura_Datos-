package airctrl.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Parametros de conexion a PostgreSQL. Se guardan en config/database.properties. */
public final class DbConfig {
    public static final Path DEFAULT_FILE = Path.of("config", "database.properties");

    private String host = "localhost";
    private int port = 5432;
    private String database = "airctrl";
    private String user = "postgres";
    private String password = "";
    private boolean rememberPassword;

    public static DbConfig load(Path file) {
        DbConfig config = new DbConfig();
        if (Files.exists(file)) {
            Properties properties = new Properties();
            try (InputStream in = Files.newInputStream(file)) {
                properties.load(in);
                config.host = properties.getProperty("host", config.host);
                config.port = Integer.parseInt(properties.getProperty("port", String.valueOf(config.port)));
                config.database = properties.getProperty("database", config.database);
                config.user = properties.getProperty("user", config.user);
                config.password = properties.getProperty("password", "");
                config.rememberPassword = !config.password.isEmpty();
            } catch (IOException | NumberFormatException ignored) {
                // Si el archivo esta danado se usan los valores por defecto.
            }
        }
        String envPassword = System.getenv("AIRCTRL_DB_PASSWORD");
        if (config.password.isEmpty() && envPassword != null) {
            config.password = envPassword;
        }
        return config;
    }

    public void save(Path file) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("host", host);
        properties.setProperty("port", String.valueOf(port));
        properties.setProperty("database", database);
        properties.setProperty("user", user);
        if (rememberPassword) {
            properties.setProperty("password", password);
        }
        Files.createDirectories(file.getParent());
        try (OutputStream out = Files.newOutputStream(file)) {
            properties.store(out, "AIRCTRL - conexion local a PostgreSQL");
        }
    }

    public String jdbcUrl() {
        return jdbcUrl(database);
    }

    public String jdbcUrl(String databaseName) {
        return "jdbc:postgresql://" + host + ":" + port + "/" + databaseName;
    }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host.trim(); }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database.trim(); }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user.trim(); }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isRememberPassword() { return rememberPassword; }
    public void setRememberPassword(boolean rememberPassword) { this.rememberPassword = rememberPassword; }
}
