package polyClinicSystem.example.appointment_service.service.ReservationLock;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationLockImpl implements ReservationLockService{
    private final RedisTemplate<String, String> redisTemplate;
    /**
     * Try to acquire a distributed lock for slot reservation
     *
     * @param lockKey The lock key (e.g., "appointment:lock:doctorId:date:period")
     *                it will be built in the AppointmentService
     * @param timeout How long to hold the lock
     * @return true if lock acquired, false otherwise
     */
    public boolean tryLock(String lockKey, Duration timeout) {
        try {
            String lockValue = Thread.currentThread().getName() + "-" + System.currentTimeMillis();

            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, lockValue, timeout.toMillis(), TimeUnit.MILLISECONDS);

            if (Boolean.TRUE.equals(success)) {
                log.debug("Lock acquired: {}", lockKey);
                return true;
            } else {
                log.debug("Lock already held: {}", lockKey);
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to acquire lock: {}", lockKey, e);
            // In case of Redis failure, allow the operation to proceed
            // The DB unique constraint will be the final safeguard
            return true;
        }
    }

    /**
     * Release the lock
     *
     * @param lockKey The lock key to release
     */
    public void unlock(String lockKey) {
        try {
            redisTemplate.delete(lockKey);
            log.debug("Lock released: {}", lockKey);
        } catch (Exception e) {
            log.error("Failed to release lock: {}", lockKey, e);
        }
    }

    /**
     * Check if a lock exists
     *
     * @param lockKey The lock key to check
     * @return true if lock exists
     */
    public boolean isLocked(String lockKey) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
        } catch (Exception e) {
            log.error("Failed to check lock: {}", lockKey, e);
            return false;
        }
    }
}
