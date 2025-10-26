package polyClinicSystem.example.payment_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import polyClinicSystem.example.payment_service.model.enums.Method;
import polyClinicSystem.example.payment_service.model.enums.Status;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    private LocalDateTime paymentDate;
    private double amount;
    private String currency;
    @Enumerated(EnumType.STRING)
    private Method method;

    @Enumerated(EnumType.STRING)
    private Status status;

}
