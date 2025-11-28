package polyClinicSystem.example.payment_service.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import polyClinicSystem.example.payment_service.dto.Response.UserResponse;

@HttpExchange
public interface UserClient {

    @GetExchange("/api/users/ById/{id}")
    UserResponse getUserById(@PathVariable Long id);
    @GetExchange("/api/users/ByKeycloak/{keycloakId}")
    UserResponse getUserByKeycloakId(@PathVariable String keycloakId);

}