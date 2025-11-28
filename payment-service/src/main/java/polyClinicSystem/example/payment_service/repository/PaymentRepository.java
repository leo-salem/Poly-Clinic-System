package polyClinicSystem.example.payment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import polyClinicSystem.example.payment_service.model.Payment;

import java.util.*;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentIntentId(String paymentIntentId);
    List<Payment> findByPatientKeycloakIdOrderByCreatedAtDesc(String patientKeycloakId);
    Optional<Payment> findByAppointmentId(Long appointmentId);
}
