package polyClinicSystem.example.user_management_service.exception.customExceptions;


public class KeycloakInvalidResponseException extends RuntimeException {
    public KeycloakInvalidResponseException(String message) {
        super(message);
    }
}
