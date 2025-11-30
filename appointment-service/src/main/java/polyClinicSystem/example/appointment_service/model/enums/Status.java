package polyClinicSystem.example.appointment_service.model.enums;

public enum Status {
    PENDING,
    // Temporary reservation, payment not yet confirmed
    EXPIRED,
    // Reservation TTL expired(if 15 minutes has been passed on pending status and not paid)
    PAID,
    // Payment confirmed, waiting for admin approval
    SCHEDULED,
    // Admin approved, appointment is final
    REJECTED,
    // Admin rejected
    COMPLETED,
    // Appointment completed (it's date has passed if it's happen or not)
    CANCELLED,
    // Cancelled by patient or system
}
