package esp.dgi.ws.rest.auth.server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DbConnection {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC driver missing", e);
        }
        String host = env("DB_HOST", "localhost");
        String port = env("DB_PORT", "5435");
        String name = env("DB_NAME", "chatroom_auth_rest");
        URL = "jdbc:postgresql://" + host + ":" + port + "/" + name;
        USER = env("DB_USER", "postgres");
        PASSWORD = env("DB_PASSWORD", "postgres");
    }

    private DbConnection() {}

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return v == null || v.isEmpty() ? def : v;
    }
}
