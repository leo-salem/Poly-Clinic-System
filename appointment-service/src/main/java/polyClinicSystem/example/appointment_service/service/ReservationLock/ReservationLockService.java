package polyClinicSystem.example.appointment_service.service.ReservationLock;

import java.time.Duration;

public interface ReservationLockService {
    boolean tryLock(String lockKey, Duration timeout);
    void unlock(String lockKey);
    boolean isLocked(String lockKey);
}
