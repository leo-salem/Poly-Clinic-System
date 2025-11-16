package polyClinicSystem.example.user_management_service.service.keycloakAdmin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
@Slf4j
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

    /**
     * Helper method to convert Map to form-urlencoded String
     */
    private String buildFormUrlencodedBody(Map<String, String> params) {
        StringBuilder body = new StringBuilder();
        params.forEach((key, value) -> {
            if (body.length() > 0) {
                body.append("&");
            }
            body.append(key).append("=").append(value);
        });
        return body.toString();
    }

    // Error Handler Helper

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
            // Build form body String properly cause APPLICATION_FORM_URLENCODED
            //doesn't accept map
            Map<String, String> params = new HashMap<>();
            params.put("client_id", clientId);
            params.put("username", adminUsername);
            params.put("password", adminPassword);
            params.put("grant_type", "password");

            String formBody = buildFormUrlencodedBody(params);
            String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            log.debug("Attempting to get admin token from: {}", url);

            Map<String, Object> response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formBody)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            (req, res) -> {
                                log.error("Authentication failed with status: {}", res.getStatusCode());
                                throwStatusError(res.getStatusCode(), "Authentication failed - check credentials");
                            })
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (req, res) -> {
                                log.error("Keycloak server error: {}", res.getStatusCode());
                                throwStatusError(res.getStatusCode(), "Keycloak server error");
                            })
                    .body(Map.class);

            if (response == null || response.get("access_token") == null) {
                throw new KeycloakInvalidResponseException("Keycloak did not return access token");
            }

            log.debug("Successfully obtained admin access token");
            return (String) response.get("access_token");

        } catch (RestClientException ex) {
            log.error("Failed to connect to Keycloak: {}", ex.getMessage(), ex);
            throw new KeycloakServerException("Cannot connect to Keycloak server: " + ex.getMessage());
        }
    }

    @Override
    public String createUser(String token, RegisterStaffRequest userRequest) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("username", userRequest.getUsername());
            payload.put("email", userRequest.getEmail());
            payload.put("enabled", true);
            payload.put("emailVerified", true); // Mark email as verified
            payload.put("firstName", userRequest.getFirstName());
            payload.put("lastName", userRequest.getLastName());

            // credentials
            Map<String, Object> credential = new HashMap<>();
            credential.put("type", "password");
            credential.put("value", userRequest.getPassword());
            credential.put("temporary", false);
            payload.put("credentials", List.of(credential));

            // custom attributes
            Map<String, List<String>> attributes = new HashMap<>();
            if (userRequest.getAge() != null) {
                attributes.put("age", List.of(String.valueOf(userRequest.getAge())));
            }
            if (userRequest.getGender() != null) {
                attributes.put("gender", List.of(userRequest.getGender().name()));
            }
            if (userRequest.getAddress() != null) {
                attributes.put("address", List.of(userRequest.getAddress()));
            }
            if (userRequest.getPhone() != null) {
                attributes.put("phone", List.of(userRequest.getPhone()));
            }
            if (userRequest.getSpecialization() != null) {
                attributes.put("specialization", List.of(userRequest.getSpecialization()));
            }
            if (userRequest.getExperience_years() != null) {
                attributes.put("experience_years", List.of(String.valueOf(userRequest.getExperience_years())));
            }
            if (userRequest.getBloodType() != null) {
                attributes.put("bloodType", List.of(userRequest.getBloodType().name()));
            }
            if (!attributes.isEmpty()) {
                payload.put("attributes", attributes);
            }

            String url = keycloakServerUrl + "/admin/realms/" + realm + "/users";

            log.debug("Creating user in Keycloak: {}", userRequest.getUsername());
            log.debug("Keycloak payload: {}", payload);

            ResponseEntity<Void> response = restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            (req, res) -> {
                                String errorMsg = "Cannot create user";
                                try {
                                    // Try to read error body
                                    String errorBody = new String(res.getBody().readAllBytes());
                                    log.error("Failed to create user. Status: {}, Body: {}", res.getStatusCode(), errorBody);
                                    if (res.getStatusCode().value() == 409) {
                                        errorMsg = "User with username '" + userRequest.getUsername() + "' or email '" + userRequest.getEmail() + "' already exists";
                                    } else {
                                        errorMsg = "Cannot create user - " + errorBody;
                                    }
                                } catch (Exception e) {
                                    log.error("Failed to create user. Status: {}", res.getStatusCode());
                                }
                                throwStatusError(res.getStatusCode(), errorMsg);
                            })
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (req, res) -> {
                                log.error("Keycloak server error while creating user: {}", res.getStatusCode());
                                throwStatusError(res.getStatusCode(),
                                        "Keycloak server error while creating user");
                            })
                    .toEntity(Void.class);

            URI location = response.getHeaders().getLocation();
            if (location == null) {
                throw new KeycloakInvalidResponseException("Keycloak did not return Location header for userId");
            }

            String userId = location.getPath().substring(location.getPath().lastIndexOf('/') + 1);
            log.info("User created successfully with ID: {}", userId);
            return userId;

        } catch (RestClientException ex) {
            log.error("Connection failure while creating user: {}", ex.getMessage(), ex);
            throw new KeycloakServerException("Connection failure while creating user: " + ex.getMessage());
        }
    }

    @Override
    public Map<String, Object> getClientRoleRepresentation(String token, String roleName) {
        String url = keycloakServerUrl + "/admin/realms/" + realm
                + "/clients/" + clientUid + "/roles/" + roleName;

        try {
            log.debug("Fetching client role: {}", roleName);

            return restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .onStatus(
                            HttpStatus.NOT_FOUND::equals,
                            (req, res) -> {
                                log.error("Client role not found: {}", roleName);
                                throwStatusError(res.getStatusCode(),
                                        "Client role not found: " + roleName);
                            })
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (req, res) -> {
                                log.error("Server error fetching client role: {}", res.getStatusCode());
                                throwStatusError(res.getStatusCode(),
                                        "Keycloak server error while fetching client role");
                            })
                    .body(Map.class);

        } catch (RestClientException ex) {
            log.error("Error fetching client role: {}", ex.getMessage(), ex);
            throw new KeycloakServerException("Error fetching client role: " + ex.getMessage());
        }
    }

    @Override
    public void assignClientRoleToUser(String userId, String roleName) {
        String token = getAdminAccessToken();
        Map<String, Object> role = getClientRoleRepresentation(token, roleName);

        String url = keycloakServerUrl + "/admin/realms/" + realm
                + "/users/" + userId + "/role-mappings/clients/" + clientUid;

        try {
            log.debug("Assigning client role {} to user {}", roleName, userId);

            restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(List.of(role))
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            (req, res) -> {
                                log.error("Cannot assign client role. Status: {}", res.getStatusCode());
                                throwStatusError(res.getStatusCode(),
                                        "Cannot assign client role");
                            })
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (req, res) -> {
                                log.error("Server error assigning client role: {}", res.getStatusCode());
                                throwStatusError(res.getStatusCode(),
                                        "Keycloak server error while assigning client role");
                            })
                    .toBodilessEntity();

            log.info("Client role {} assigned successfully to user {}", roleName, userId);

        } catch (RestClientException ex) {
            log.error("Connection failure while assigning client role: {}", ex.getMessage(), ex);
            throw new KeycloakServerException("Connection failure while assigning client role: " + ex.getMessage());
        }
    }

    @Override
    public Map<String, Object> getRealmRoleRepresentation(String token, String roleName) {
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/roles/" + roleName;

        try {
            log.debug("Fetching realm role: {}", roleName);

            return restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            (req, res) -> {
                                log.error("Realm role not found: {}", roleName);
                                throw new KeycloakNotFoundException("Realm role not found: " + roleName);
                            })
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (req, res) -> {
                                log.error("Server error fetching realm role: {}", res.getStatusCode());
                                throw new KeycloakServerException("Keycloak server error while fetching realm role");
                            })
                    .body(Map.class);

        } catch (RestClientException ex) {
            log.error("Error fetching realm role: {}", ex.getMessage(), ex);
            throw new KeycloakServerException("Error fetching realm role: " + ex.getMessage());
        }
    }

    @Override
    public void assignRealmRoleToUser(String userId, String roleName) {
        String token = getAdminAccessToken();
        Map<String, Object> role = getRealmRoleRepresentation(token, roleName);

        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";

        try {
            log.debug("Assigning realm role {} to user {}", roleName, userId);

            restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(List.of(role))
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            (req, res) -> {
                                log.error("Cannot assign realm role. Status: {}", res.getStatusCode());
                                throwStatusError(res.getStatusCode(),
                                        "Cannot assign realm role");
                            })
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (req, res) -> {
                                log.error("Server error assigning realm role: {}", res.getStatusCode());
                                throwStatusError(res.getStatusCode(),
                                        "Keycloak server error while assigning realm role");
                            })
                    .toBodilessEntity();

            log.info("Realm role {} assigned successfully to user {}", roleName, userId);

        } catch (RestClientException ex) {
            log.error("Connection failure while assigning realm role: {}", ex.getMessage(), ex);
            throw new KeycloakServerException("Connection failure while assigning realm role: " + ex.getMessage());
        }
    }

    @Override
    public void deleteUser(String userId) {
        String token = getAdminAccessToken();
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId;

        try {
            log.debug("Deleting user: {}", userId);

            restClient.delete()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .onStatus(
                            HttpStatus.NOT_FOUND::equals,
                            (req, res) -> {
                                log.error("User not found for deletion: {}", userId);
                                throwStatusError(res.getStatusCode(),
                                        "User not found");
                            })
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (req, res) -> {
                                log.error("Server error deleting user: {}", res.getStatusCode());
                                throwStatusError(res.getStatusCode(),
                                        "Keycloak server error while deleting user");
                            })
                    .toBodilessEntity();

            log.info("User deleted successfully: {}", userId);

        } catch (RestClientException ex) {
            log.error("Failure while deleting user: {}", ex.getMessage(), ex);
            throw new KeycloakServerException("Failure while deleting user: " + ex.getMessage());
        }
    }

    @Override
    public void updateUser(String userId, Map<String, Object> updatedFields) {
        String token = getAdminAccessToken();
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId;

        try {
            log.debug("Updating user: {} with fields: {}", userId, updatedFields);

            // First, get the current user data from Keycloak
            Map<String, Object> currentUser = restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(Map.class);

            if (currentUser == null) {
                throw new KeycloakNotFoundException("User not found in Keycloak");
            }

            // Merge the updates into the current user data
            currentUser.putAll(updatedFields);

            // Now send the complete user object back
            restClient.put()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(currentUser)
                    .retrieve()
                    .onStatus(
                            HttpStatus.NOT_FOUND::equals,
                            (req, res) -> {
                                log.error("User not found for update: {}", userId);
                                throwStatusError(res.getStatusCode(),
                                        "User not found");
                            })
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            (req, res) -> {
                                String errorMsg = "Invalid update data";
                                try {
                                    String errorBody = new String(res.getBody().readAllBytes());
                                    log.error("Invalid update data. Status: {}, Body: {}", res.getStatusCode(), errorBody);
                                    errorMsg = "Invalid update data: " + errorBody;
                                } catch (Exception e) {
                                    log.error("Invalid update data. Status: {}", res.getStatusCode());
                                }
                                throwStatusError(res.getStatusCode(), errorMsg);
                            })
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (req, res) -> {
                                log.error("Server error updating user: {}", res.getStatusCode());
                                throwStatusError(res.getStatusCode(),
                                        "Keycloak server error while updating user");
                            })
                    .toBodilessEntity();

            log.info("User updated successfully: {}", userId);

        } catch (RestClientException ex) {
            log.error("Error updating user: {}", ex.getMessage(), ex);
            throw new KeycloakServerException("Error updating user: " + ex.getMessage());
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
            log.debug("Updating password for user: {}", userId);

            restClient.put()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .onStatus(
                            HttpStatus.NOT_FOUND::equals,
                            (req, res) -> {
                                log.error("User not found for password reset: {}", userId);
                                throwStatusError(res.getStatusCode(),
                                        "User not found");
                            })
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            (req, res) -> {
                                log.error("Invalid password reset request. Status: {}", res.getStatusCode());
                                throwStatusError(res.getStatusCode(),
                                        "Invalid password reset request");
                            })
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (req, res) -> {
                                log.error("Server error resetting password: {}", res.getStatusCode());
                                throwStatusError(res.getStatusCode(),
                                        "Keycloak server error while resetting password");
                            })
                    .toBodilessEntity();

            log.info("Password updated successfully for user: {}", userId);

        } catch (RestClientException ex) {
            log.error("Keycloak server unreachable: {}", ex.getMessage(), ex);
            throw new KeycloakServerException("Keycloak server unreachable: " + ex.getMessage());
        }
    }
}