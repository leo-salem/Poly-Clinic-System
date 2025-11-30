package polyClinicSystem.example.appointment_service.service.CleanUpScheduler;

public interface CleanUpService {
    void cleanupExpiredReservations();
    void markCompletedAppointments();
    void deleteOldAppointments();
}
