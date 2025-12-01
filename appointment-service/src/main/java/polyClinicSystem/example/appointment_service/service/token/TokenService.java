package polyClinicSystem.example.appointment_service.service.token;

import jakarta.servlet.http.HttpServletRequest;
import polyClinicSystem.example.appointment_service.dto.response.UserResponse;

public interface TokenService {
    String extractToken(HttpServletRequest request);
    String extractUserId(HttpServletRequest request);
    String extractUserRole(HttpServletRequest request);
    UserResponse getCurrentUser(HttpServletRequest request);
}
