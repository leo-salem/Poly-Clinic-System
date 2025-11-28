package polyClinicSystem.example.appointment_service.dto.response;
import lombok.*;
import polyClinicSystem.example.appointment_service.model.enums.Period;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableSlotResponse {
    private String doctorKeycloakId;
    private LocalDate date;
    private List<SlotInfo> availableSlots;
    private List<SlotInfo> bookedSlots;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SlotInfo {
        private Period period;
        private boolean available;
        private String reservationToken; // Only if patient has pending reservation
        private Instant expiresAt;
    }
}
