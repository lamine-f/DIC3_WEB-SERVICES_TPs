package esp.dgi.ws.rest.auth.api.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "credentials")
@XmlAccessorType(XmlAccessType.FIELD)
public class CredentialsDTO {
    private String username;
    private String password;

    public CredentialsDTO() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
