package polyClinicSystem.example.appointment_service.model.event;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;


@Entity
@Table(name = "outbox_events",
        indexes = @Index(name = "idx_sent_created", columnList = "sent, created_at"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {
    // we put created_at in the index cause
    // each index should be unique and there can be more than one event not sent

    /*
    we use outbox design pattern with kafka in this service which is :
    when create event after create anything in DB like appointment it will not send the
    event, but it will save it in db as it's not sent and the cron job Background scheduler will check any
    events is not sent every five seconds and try to send it to kafka.
    this design is best practice cause there is multiple problems may happen like
    DB operation happened but
    Kafka down - Network - Timeout ->(there is no event, and it should be sent)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String aggregateType; // "appointment"

    @Column(nullable = false)
    private String aggregateId; // appointment.id or reservationToken

    @Column(nullable = false)
    private String eventType; // "appointment.payment.created", "appointment.scheduled"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload; // JSON

    @Column(nullable = false)
    private boolean sent = false;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant sentAt;

    @Column
    private int retryCount = 0;
}
