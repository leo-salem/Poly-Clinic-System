package polyClinicSystem.example.user_management_service.service.keycloakAdmin;

import polyClinicSystem.example.user_management_service.dto.request.create.RegisterStaffRequest;

import java.util.Map;

public interface KeycloakAdminService {
    String createUser(String token, RegisterStaffRequest userRequest);

    String getAdminAccessToken();

    Map<String, Object> getClientRoleRepresentation(String token, String roleName);

    void assignClientRoleToUser(String userId, String roleName);

    Map<String, Object> getRealmRoleRepresentation(String token, String roleName);

    void assignRealmRoleToUser(String userId, String roleName);

    void deleteUser(String userId);

    void updateUser(String userId, Map<String, Object> updatedFields);

    void updateUserPassword(String userId, String newPassword);
}
