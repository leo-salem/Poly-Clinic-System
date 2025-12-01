package polyClinicSystem.example.appointment_service.service.token;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import polyClinicSystem.example.appointment_service.client.UserClient;
import polyClinicSystem.example.appointment_service.dto.response.UserResponse;
import polyClinicSystem.example.appointment_service.exception.customExceptions.NotFoundException;

@RequiredArgsConstructor
@Service
public class TokenServiceImpl implements TokenService {

    private final UserClient userClient;

    @Override
    public String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7); // remove "Bearer "
    }

    @Override
    public String extractUserId(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            throw new RuntimeException("Token not found in request");
        }

        try {
            SignedJWT signed = SignedJWT.parse(token);
            JWTClaimsSet claims = signed.getJWTClaimsSet();
            return claims.getStringClaim("sub");
        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT Token", e);
        }
    }

    @Override
    public String extractUserRole(HttpServletRequest request) {
        UserResponse currentUser = getCurrentUser(request);
        return currentUser.getRole().name();
    }

    @Override
    public UserResponse getCurrentUser(HttpServletRequest request) throws NotFoundException {
        String userId = extractUserId(request);
        try {
            return userClient.getUserByKeycloakId(userId);
        } catch (Exception e) {
            throw new NotFoundException("User not found with id: " + userId);
        }
    }
}
