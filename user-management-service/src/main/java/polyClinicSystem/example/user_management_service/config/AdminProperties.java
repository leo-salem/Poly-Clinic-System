package polyClinicSystem.example.user_management_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.admin")
public class AdminProperties {
    private String keycloakId;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
    private String phone;
    private Integer age;
    private String gender;
    private String address;
    private String bloodType;
    private String role;
    private Boolean createDefault = true;
}