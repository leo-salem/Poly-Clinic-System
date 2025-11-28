package polyClinicSystem.example.appointment_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic appointmentPaymentCreatedTopic() {
        return TopicBuilder.name("appointment-payment-created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic appointmentScheduledTopic() {
        return TopicBuilder.name("appointment-scheduled")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic appointmentRejectedTopic() {
        return TopicBuilder.name("appointment-rejected")
                .partitions(3)
                .replicas(1)
                .build();
    }
}