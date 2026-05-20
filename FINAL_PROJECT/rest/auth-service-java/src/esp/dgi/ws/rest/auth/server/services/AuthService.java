package esp.dgi.ws.rest.auth.server.services;

import esp.dgi.ws.rest.auth.api.dto.TokenInfoDTO;
import esp.dgi.ws.rest.auth.server.db.SessionDao;
import esp.dgi.ws.rest.auth.server.db.UserDao;
import esp.dgi.ws.rest.auth.server.exceptions.AuthException;
import esp.dgi.ws.rest.auth.server.util.PasswordHasher;

import java.sql.SQLException;

public class AuthService {

    private final UserDao users = new UserDao();
    private final SessionDao sessions = new SessionDao();

    public String register(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.length() < 4) {
            throw new AuthException(400, "INVALID_INPUT", "username vide ou mot de passe trop court");
        }
        try {
            if (users.findByUsername(username) != null) {
                throw new AuthException(409, "USERNAME_TAKEN", "username deja utilise");
            }
            int id = users.create(username, PasswordHasher.hash(password));
            return sessions.createToken(id);
        } catch (SQLException e) {
            throw new AuthException(500, "DB_ERROR", e.getMessage());
        }
    }

    public String login(String username, String password) {
        try {
            UserDao.User u = users.findByUsername(username);
            if (u == null || !PasswordHasher.verify(password, u.passwordHash)) {
                throw new AuthException(401, "INVALID_CREDENTIALS", "identifiants invalides");
            }
            return sessions.createToken(u.id);
        } catch (SQLException e) {
            throw new AuthException(500, "DB_ERROR", e.getMessage());
        }
    }

    public boolean logout(String token) {
        try {
            return sessions.delete(token);
        } catch (SQLException e) {
            return false;
        }
    }

    public TokenInfoDTO validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new AuthException(401, "INVALID_TOKEN", "token absent");
        }
        try {
            SessionDao.Session s = sessions.findValid(token);
            if (s == null) {
                throw new AuthException(401, "INVALID_TOKEN", "token invalide ou expire");
            }
            UserDao.User u = users.findById(s.userId);
            if (u == null) {
                throw new AuthException(401, "USER_NOT_FOUND", "utilisateur introuvable");
            }
            return new TokenInfoDTO(u.id, u.username);
        } catch (SQLException e) {
            throw new AuthException(500, "DB_ERROR", e.getMessage());
        }
    }
}
