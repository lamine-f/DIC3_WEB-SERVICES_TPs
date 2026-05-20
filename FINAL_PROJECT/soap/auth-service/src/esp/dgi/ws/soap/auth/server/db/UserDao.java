package esp.dgi.ws.soap.auth.server.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDao {

    public static class User {
        public final int id;
        public final String username;
        public final String passwordHash;
        public User(int id, String username, String passwordHash) {
            this.id = id; this.username = username; this.passwordHash = passwordHash;
        }
    }

    public int create(String username, String passwordHash) throws SQLException {
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO users (username, password_hash) VALUES (?, ?) RETURNING id")) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public User findByUsername(String username) throws SQLException {
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT id, username, password_hash FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new User(rs.getInt(1), rs.getString(2), rs.getString(3));
            }
        }
    }

    public User findById(int id) throws SQLException {
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT id, username, password_hash FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new User(rs.getInt(1), rs.getString(2), rs.getString(3));
            }
        }
    }
}
