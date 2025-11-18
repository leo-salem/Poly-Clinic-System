package polyClinicSystem.example.prescription_service.service.token;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class TokenServiceImpl implements TokenService {

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
}
