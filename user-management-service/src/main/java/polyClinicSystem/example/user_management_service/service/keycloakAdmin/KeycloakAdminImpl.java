package polyClinicSystem.example.user_management_service.service.keycloakAdmin;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestClient;
import polyClinicSystem.example.user_management_service.dto.request.create.RegisterStaffRequest;

import java.net.URI;
import java.util.*;
@Service
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
        //not handle this yet cause
        //for every client role (resource access)there is clientUid

        private final RestClient restClient = RestClient.create();

    @Override
    public String createUser(String token, RegisterStaffRequest userRequest) {
        Map<String, Object> userPayload = new HashMap<>();
        userPayload.put("username", userRequest.getUsername());
        userPayload.put("email", userRequest.getEmail());
        userPayload.put("enabled", true);
        userPayload.put("firstName", userRequest.getFirstName());
        userPayload.put("lastName", userRequest.getLastName());

        // Set password credentials
        Map<String, Object> credential = new HashMap<>();
        credential.put("type", "password");
        credential.put("value", userRequest.getPassword());
        credential.put("temporary", false);
        userPayload.put("credentials", List.of(credential));

        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users";

        ResponseEntity<Void> response = restClient.post()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(userPayload)
                .retrieve()
                .toEntity(Void.class);

        URI location = response.getHeaders().getLocation();
        if (location == null) {
            throw new RuntimeException("Keycloak did not return user location header");
        }

        // Extract userId from returned URI
        String path = location.getPath();
        return path.substring(path.lastIndexOf("/") + 1);
    }

    //  Get admin access token using password grant
        public String getAdminAccessToken() {
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

            return (String) response.get("access_token");
        }

        //  Get client role representation (for assigning roles)
        public Map getClientRoleRepresentation(String token, String roleName) {
            String url = keycloakServerUrl + "/admin/realms/" + realm
                    + "/clients/" + clientUid + "/roles/" + roleName;

            return restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(Map.class);
        }

        //  Assign client role to user (e.g., USER_WRITE)
        public void assignClientRoleToUser(String userId, String roleName) {
            String token = getAdminAccessToken();
            Map role = getClientRoleRepresentation(token, roleName);

            String url = keycloakServerUrl + "/admin/realms/" + realm
                    + "/users/" + userId + "/role-mappings/clients/" + clientUid;

            restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(List.of(role))
                    .retrieve()
                    .toBodilessEntity();
        }

        // Get realm role representation (ADMIN, DOCTOR, NURSE, PATIENT)
        public Map getRealmRoleRepresentation(String token, String roleName) {
            String url = keycloakServerUrl + "/admin/realms/" + realm + "/roles/" + roleName;

            return restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(Map.class);
        }

        // Assign realm role to user (ADMIN, DOCTOR, etc.)
        public void assignRealmRoleToUser(String userId, String roleName) {
            String token = getAdminAccessToken();
            Map role = getRealmRoleRepresentation(token, roleName);

            String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";

            restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(List.of(role))
                    .retrieve()
                    .toBodilessEntity();
        }

    public void deleteUser(String userId) {
        String token = getAdminAccessToken();

        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId;

        restClient.delete()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .toBodilessEntity();
    }

    public void updateUser(String userId, Map<String, Object> updatedFields) {
        String token = getAdminAccessToken();

        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId;

        restClient.put()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(updatedFields)
                .retrieve()
                .toBodilessEntity();
    }


    public void updateUserPassword(String userId, String newPassword) {
        String token = getAdminAccessToken();

        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId + "/reset-password";

        Map<String, Object> credential = new HashMap<>();
        credential.put("type", "password");
        credential.put("value", newPassword);
        credential.put("temporary", false);

        restClient.put()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(credential)
                .retrieve()
                .toBodilessEntity();
    }


}

