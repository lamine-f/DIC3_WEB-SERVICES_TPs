package esp.dgi.ws.rest.auth.server.db;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class SessionDao {

    private static final long TTL_HOURS = 8;
    private static final SecureRandom RNG = new SecureRandom();

    public static class Session {
        public final String token;
        public final int userId;
        public Session(String token, int userId) {
            this.token = token; this.userId = userId;
        }
    }

    public String createToken(int userId) throws SQLException {
        String token = generateToken();
        Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + TTL_HOURS * 3600_000L);
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO sessions (token, user_id, expires_at) VALUES (?, ?, ?)")) {
            ps.setString(1, token);
            ps.setInt(2, userId);
            ps.setTimestamp(3, expiresAt);
            ps.executeUpdate();
        }
        return token;
    }

    public Session findValid(String token) throws SQLException {
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT token, user_id FROM sessions WHERE token = ? AND expires_at > NOW()")) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Session(rs.getString(1), rs.getInt(2));
            }
        }
    }

    public boolean delete(String token) throws SQLException {
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM sessions WHERE token = ?")) {
            ps.setString(1, token);
            return ps.executeUpdate() > 0;
        }
    }

    private static String generateToken() {
        byte[] bytes = new byte[32];
        RNG.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(64);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
