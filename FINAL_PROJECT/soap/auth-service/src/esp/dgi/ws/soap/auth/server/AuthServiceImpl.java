package esp.dgi.ws.soap.auth.server;

import esp.dgi.ws.soap.auth.api.AuthService;
import esp.dgi.ws.soap.auth.api.dto.TokenInfoDTO;
import esp.dgi.ws.soap.auth.api.faults.AuthFault;
import esp.dgi.ws.soap.auth.server.db.SessionDao;
import esp.dgi.ws.soap.auth.server.db.UserDao;
import esp.dgi.ws.soap.auth.server.util.PasswordHasher;
import jakarta.jws.WebService;

import java.sql.SQLException;

@WebService(
    endpointInterface = "esp.dgi.ws.soap.auth.api.AuthService",
    targetNamespace = "http://chatroom.dic3/auth",
    serviceName = "AuthService",
    portName = "AuthServicePort"
)
public class AuthServiceImpl implements AuthService {

    private final UserDao users = new UserDao();
    private final SessionDao sessions = new SessionDao();

    @Override
    public String register(String username, String password) throws AuthFault {
        if (username == null || username.isBlank() || password == null || password.length() < 4) {
            throw new AuthFault("INVALID_INPUT", "username vide ou mot de passe trop court");
        }
        try {
            if (users.findByUsername(username) != null) {
                throw new AuthFault("USERNAME_TAKEN", "username deja utilise");
            }
            int id = users.create(username, PasswordHasher.hash(password));
            return sessions.createToken(id);
        } catch (SQLException e) {
            throw new AuthFault("DB_ERROR", e.getMessage());
        }
    }

    @Override
    public String login(String username, String password) throws AuthFault {
        try {
            UserDao.User u = users.findByUsername(username);
            if (u == null || !PasswordHasher.verify(password, u.passwordHash)) {
                throw new AuthFault("INVALID_CREDENTIALS", "identifiants invalides");
            }
            return sessions.createToken(u.id);
        } catch (SQLException e) {
            throw new AuthFault("DB_ERROR", e.getMessage());
        }
    }

    @Override
    public boolean logout(String token) {
        try {
            return sessions.delete(token);
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public TokenInfoDTO validateToken(String token) throws AuthFault {
        try {
            SessionDao.Session s = sessions.findValid(token);
            if (s == null) throw new AuthFault("INVALID_TOKEN", "token invalide ou expire");
            UserDao.User u = users.findById(s.userId);
            if (u == null) throw new AuthFault("USER_NOT_FOUND", "utilisateur introuvable");
            return new TokenInfoDTO(u.id, u.username);
        } catch (SQLException e) {
            throw new AuthFault("DB_ERROR", e.getMessage());
        }
    }
}
