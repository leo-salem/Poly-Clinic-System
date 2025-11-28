package polyClinicSystem.example.appointment_service.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import polyClinicSystem.example.appointment_service.model.enums.Period;
import polyClinicSystem.example.appointment_service.model.enums.Reason;
import polyClinicSystem.example.appointment_service.model.enums.Status;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "appointments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_doctor_date_period",
                columnNames = {"doctor_keycloak_id", "appointment_date", "period"}
        ),
        indexes = {
                @Index(name = "idx_doctor_date_status", columnList = "doctor_keycloak_id, appointment_date, status"),
                @Index(name = "idx_patient_keycloak_id", columnList = "patient_keycloak_id"),
                @Index(name = "idx_reservation_token", columnList = "reservation_token"),
                @Index(name = "idx_status_created_at", columnList = "status, created_at")
        })
public class Appointment {
    /*
    * constrains prevent its table from having 2 raws with same constrain
      like in this case (there is no 2 appointments have date and period and doctor)
    * index optimize search or querying operation by just get the data it will search with
     */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doctor_keycloak_id", nullable = false)
    private String doctorKeycloakId;

    @Column(name = "nurse_keycloak_id")
    private String nurseKeycloakId;

    @Column(name = "patient_keycloak_id", nullable = false)
    private String patientKeycloakId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "reservation_token", unique = true)
    private String reservationToken;

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "payment_intent_id")
    private String paymentIntentId;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Version
    private Long version; //for optimistic locking with redis

    /** Optional notes or special instructions for the appointment */
    @Column(name = "notes", length = 1000)
    private String notes;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Reason reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Period period;

}
