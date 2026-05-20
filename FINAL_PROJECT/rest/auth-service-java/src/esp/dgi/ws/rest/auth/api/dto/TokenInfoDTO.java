package esp.dgi.ws.rest.auth.api.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TokenInfoDTO {
    private int userId;
    private String username;

    public TokenInfoDTO() {}

    public TokenInfoDTO(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
