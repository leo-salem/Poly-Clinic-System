package polyClinicSystem.example.notification_service.model;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean isRead = false;

    private String message;

    @CreatedDate
    private LocalDateTime createdAt ;

    @Enumerated(EnumType.STRING)
    private NotificationType type;
}
