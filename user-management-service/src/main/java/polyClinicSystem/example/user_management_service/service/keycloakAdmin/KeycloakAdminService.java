package polyClinicSystem.example.user_management_service.service.keycloakAdmin;

import polyClinicSystem.example.user_management_service.dto.RegisterPatientRequest;

import java.util.Map;

public interface KeycloakAdminService {
    String createUser(String token, RegisterPatientRequest userRequest);
    String getAdminAccessToken();
    Map<String, Object> getClientRoleRepresentation(String token, String roleName);
    void assignClientRoleToUser(String userId, String roleName);
    Map<String, Object> getRealmRoleRepresentation(String token, String roleName);
    void assignRealmRoleToUser(String userId, String roleName);
}
