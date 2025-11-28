package polyClinicSystem.example.appointment_service.model.enums;

import lombok.Getter;

import java.time.LocalTime;

@Getter
public enum Period {
    /*
    enum is a special class that can define limited  instances
    which call constrains like(H08_09 or H10_11) in this enum or (PENDING , COMPLETED) in status
    but this instances can only be defined inside it's class and used outside it
    and as it's objects it can have fields and take it in constructor definitions
    */
    H08_09("08:00-09:00", LocalTime.of(8, 0), LocalTime.of(9, 0)),
    H09_10("09:00-10:00", LocalTime.of(9, 0), LocalTime.of(10, 0)),
    H10_11("10:00-11:00", LocalTime.of(10, 0), LocalTime.of(11, 0)),
    H11_12("11:00-12:00", LocalTime.of(11, 0), LocalTime.of(12, 0)),
    H12_13("12:00-13:00", LocalTime.of(12, 0), LocalTime.of(13, 0)),
    H13_14("13:00-14:00", LocalTime.of(13, 0), LocalTime.of(14, 0)),
    H14_15("14:00-15:00", LocalTime.of(14, 0), LocalTime.of(15, 0)),
    H15_16("15:00-16:00", LocalTime.of(15, 0), LocalTime.of(16, 0)),
    H16_17("16:00-17:00", LocalTime.of(16, 0), LocalTime.of(17, 0));

    private final String displayName;
    private final LocalTime startTime;
    private final LocalTime endTime;

    Period(String displayName, LocalTime startTime, LocalTime endTime) {
        this.displayName = displayName;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
