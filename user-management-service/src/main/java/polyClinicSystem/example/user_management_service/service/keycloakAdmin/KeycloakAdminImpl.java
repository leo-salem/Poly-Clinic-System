package polyClinicSystem.example.user_management_service.service.keycloakAdmin;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import polyClinicSystem.example.user_management_service.dto.request.create.RegisterStaffRequest;
import polyClinicSystem.example.user_management_service.exception.customExceptions.KeycloakInvalidResponseException;
import polyClinicSystem.example.user_management_service.exception.customExceptions.KeycloakNotFoundException;
import polyClinicSystem.example.user_management_service.exception.customExceptions.KeycloakOperationException;
import polyClinicSystem.example.user_management_service.exception.customExceptions.KeycloakServerException;

import java.net.URI;
import java.util.*;

@Service
@RequiredArgsConstructor
public class KeycloakAdminImpl implements KeycloakAdminService {

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.admin.server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.admin.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.client-uid}")
    private String clientUid;

    private final RestClient restClient = RestClient.create();


    //  Error Handler Helper
    private void throwStatusError(HttpStatusCode status, String message) {
        HttpStatus http = HttpStatus.resolve(status.value());

        if (http == null) {
            throw new KeycloakServerException("Unknown Keycloak error: " + message);
        }

        switch (http) {
            case NOT_FOUND -> throw new KeycloakNotFoundException(message);
            case BAD_REQUEST, UNAUTHORIZED, FORBIDDEN ->
                    throw new KeycloakOperationException(message);
            default -> throw new KeycloakServerException("Keycloak server error: " + message);
        }
    }

    @Override
    public String getAdminAccessToken() {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("client_id", clientId);
            params.put("username", adminUsername);
            params.put("password", adminPassword);
            params.put("grant_type", "password");

            String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            Map response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params)
                    .retrieve()
                    .body(Map.class);

            if (response == null || response.get("access_token") == null) {
                throw new KeycloakInvalidResponseException("Keycloak did not return access token");
            }

            return (String) response.get("access_token");

        } catch (RestClientException ex) {
            throw new KeycloakServerException("Cannot connect to Keycloak server");
        }
    }

    @Override
    public String createUser(String token, RegisterStaffRequest userRequest) {

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("username", userRequest.getUsername());
            payload.put("email", userRequest.getEmail());
            payload.put("enabled", true);
            payload.put("firstName", userRequest.getFirstName());
            payload.put("lastName", userRequest.getLastName());

            Map<String, Object> credential = new HashMap<>();
            credential.put("type", "password");
            credential.put("value", userRequest.getPassword());
            credential.put("temporary", false);
            payload.put("credentials", List.of(credential));

            String url = keycloakServerUrl + "/admin/realms/" + realm + "/users";

            ResponseEntity<Void> response = restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            (req, res) -> throwStatusError(res.getStatusCode(),
                                    "Cannot create user — invalid data"))
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (req, res) -> throwStatusError(res.getStatusCode(),
                                    "Keycloak server error while creating user"))
                    .toEntity(Void.class);

            URI location = response.getHeaders().getLocation();
            if (location == null) {
                throw new KeycloakInvalidResponseException("Keycloak did not return Location header for userId");
            }

            return location.getPath().substring(location.getPath().lastIndexOf('/') + 1);

        } catch (RestClientException ex) {
            throw new KeycloakServerException("Connection failure while creating user");
        }
    }


    @Override
    public Map getClientRoleRepresentation(String token, String roleName) {

        String url = keycloakServerUrl + "/admin/realms/" + realm
                + "/clients/" + clientUid + "/roles/" + roleName;

        try {
            return restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .onStatus(
                            HttpStatus.NOT_FOUND::equals,
                            (req, res) -> throwStatusError(res.getStatusCode(),
                                    "Client role not found: " + roleName))
                    .body(Map.class);

        } catch (RestClientException ex) {
            throw new KeycloakServerException("Error fetching client role");
        }
    }

    @Override
    public void assignClientRoleToUser(String userId, String roleName) {

        String token = getAdminAccessToken();
        Map role = getClientRoleRepresentation(token, roleName);

        String url = keycloakServerUrl + "/admin/realms/" + realm
                + "/users/" + userId + "/role-mappings/clients/" + clientUid;

        try {
            restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(List.of(role))
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            (req, res) -> throwStatusError(res.getStatusCode(),
                                    "Cannot assign client role"))
                    .toBodilessEntity();

        } catch (RestClientException ex) {
            throw new KeycloakServerException("Connection failure while assigning client role");
        }
    }

    @Override
    public Map getRealmRoleRepresentation(String token, String roleName) {

        String url = keycloakServerUrl + "/admin/realms/" + realm + "/roles/" + roleName;

        try {
            return restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            (req, res) -> {
                                throw new KeycloakNotFoundException("Realm role not found: " + roleName);
                            }
                    )
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (req, res) -> {
                                throw new KeycloakServerException("Keycloak server error while fetching realm role");
                            }
                    )
                    .body(Map.class);

        } catch (RestClientException ex) {
            throw new KeycloakServerException("Error fetching realm role");
        }
    }


    @Override
    public void assignRealmRoleToUser(String userId, String roleName) {

        String token = getAdminAccessToken();
        Map role = getRealmRoleRepresentation(token, roleName);

        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";

        try {
            restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(List.of(role))
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            (req, res) -> throwStatusError(res.getStatusCode(),
                                    "Cannot assign realm role"))
                    .toBodilessEntity();

        } catch (RestClientException ex) {
            throw new KeycloakServerException("Connection failure while assigning realm role");
        }
    }


    @Override
    public void deleteUser(String userId) {

        String token = getAdminAccessToken();
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId;

        try {
            restClient.delete()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .onStatus(
                            HttpStatus.NOT_FOUND::equals,
                            (req, res) -> throwStatusError(res.getStatusCode(),
                                    "User not found"))
                    .toBodilessEntity();

        } catch (RestClientException ex) {
            throw new KeycloakServerException("Failure while deleting user");
        }
    }


    @Override
    public void updateUser(String userId, Map<String, Object> updatedFields) {

        String token = getAdminAccessToken();
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId;

        try {
            restClient.put()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(updatedFields)
                    .retrieve()
                    .onStatus(
                            HttpStatus.NOT_FOUND::equals,
                            (req, res) -> throwStatusError(res.getStatusCode(),
                                    "User not found"))
                    .toBodilessEntity();

        } catch (RestClientException ex) {
            throw new KeycloakServerException("Error updating user");
        }
    }


    @Override
    public void updateUserPassword(String userId, String newPassword) {

        String token = getAdminAccessToken();
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId + "/reset-password";

        Map<String, Object> payload = Map.of(
                "type", "password",
                "value", newPassword,
                "temporary", false
        );

        try {
            restClient.put()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .onStatus(
                            HttpStatus.NOT_FOUND::equals,
                            (req, res) -> throwStatusError(res.getStatusCode(),
                                    "User not found"))
                    .toBodilessEntity();

        } catch (RestClientException ex) {
            throw new KeycloakServerException("Keycloak server unreachable");
        }
    }
}
