package polyClinicSystem.example.appointment_service.service.Appointment;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import polyClinicSystem.example.appointment_service.client.PaymentClient;
import polyClinicSystem.example.appointment_service.dto.request.AdminApprovalRequest;
import polyClinicSystem.example.appointment_service.dto.request.ConfirmPaymentRequest;
import polyClinicSystem.example.appointment_service.dto.request.ReserveSlotRequest;
import polyClinicSystem.example.appointment_service.dto.response.AppointmentResponse;
import polyClinicSystem.example.appointment_service.dto.response.AvailableSlotResponse;
import polyClinicSystem.example.appointment_service.dto.response.ReservationResponse;
import polyClinicSystem.example.appointment_service.exception.customExceptions.BadRequestException;
import polyClinicSystem.example.appointment_service.exception.customExceptions.ConflictException;
import polyClinicSystem.example.appointment_service.exception.customExceptions.NotFoundException;
import polyClinicSystem.example.appointment_service.exception.customExceptions.PaymentException;
import polyClinicSystem.example.appointment_service.model.entity.Appointment;
import polyClinicSystem.example.appointment_service.model.enums.Period;
import polyClinicSystem.example.appointment_service.model.enums.Status;
import polyClinicSystem.example.appointment_service.repository.AppointmentRepository;
import polyClinicSystem.example.appointment_service.service.ReservationLock.ReservationLockService;
import polyClinicSystem.example.appointment_service.service.kafka.outboxService.OutboxService;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final OutboxService outboxService;
    private final PaymentClient paymentService;
    private final ReservationLockService reservationLockService;

    @Value("${appointment.reservation.ttl-minutes:10}")
    private int reservationTtlMinutes;

    @Override
    public AvailableSlotResponse getAvailableSlots(String doctorKeycloakId, LocalDate date, String requestingPatientId) {
        log.debug("Fetching available slots for doctor: {} on date: {}", doctorKeycloakId, date);

        // Get all booked periods for this doctor/date
        List<Status> bookedStatuses = Arrays.asList(
                Status.PENDING,
                Status.PAID,
                Status.SCHEDULED
        );

        List<Period> bookedPeriods = appointmentRepository.findBookedPeriods(
                doctorKeycloakId, date, bookedStatuses
        );

        // Get all possible periods
        List<Period> allPeriods = Arrays.asList(Period.values());

        // Calculate available periods
        List<AvailableSlotResponse.SlotInfo> availableSlots = new ArrayList<>();
        List<AvailableSlotResponse.SlotInfo> bookedSlots = new ArrayList<>();

        for (Period period : allPeriods) {
            boolean isBooked = bookedPeriods.contains(period);

            AvailableSlotResponse.SlotInfo slotInfo = AvailableSlotResponse.SlotInfo.builder()
                    .period(period)
                    .available(!isBooked)
                    .build();

            // If requesting patient has a pending reservation, include token
            if (requestingPatientId != null && isBooked) {
                Optional<Appointment> patientReservation = appointmentRepository
                        .findByDoctorKeycloakIdAndAppointmentDate(doctorKeycloakId, date)
                        .stream()
                        .filter(a -> a.getPatientKeycloakId().equals(requestingPatientId)
                                && a.getPeriod() == period
                                && (a.getStatus() == Status.PENDING || a.getStatus() == Status.PAID))
                        .findFirst();

                if (patientReservation.isPresent()) {
                    slotInfo.setReservationToken(patientReservation.get().getReservationToken());
                    slotInfo.setExpiresAt(patientReservation.get().getExpiresAt());
                }
            }

            if (isBooked) {
                bookedSlots.add(slotInfo);
            } else {
                availableSlots.add(slotInfo);
            }
        }

        log.info("Found {} available and {} booked slots for doctor {} on {}",
                availableSlots.size(), bookedSlots.size(), doctorKeycloakId, date);

        return AvailableSlotResponse.builder()
                .doctorKeycloakId(doctorKeycloakId)
                .date(date)
                .availableSlots(availableSlots)
                .bookedSlots(bookedSlots)
                .build();
    }

    @Override
    @Transactional
    public ReservationResponse reserveSlot(ReserveSlotRequest request) {
        log.debug("Attempting to reserve slot: doctor={}, date={}, period={}",
                request.getDoctorKeycloakId(), request.getAppointmentDate(), request.getPeriod());

        // Validation: date must be in the future
        if (request.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot reserve appointments in the past");
        }

        String lockKey = buildLockKey(request.getDoctorKeycloakId(), request.getAppointmentDate(), request.getPeriod());

        // Try to acquire distributed lock (optional, reduces DB conflicts)
        boolean lockAcquired = reservationLockService.tryLock(lockKey, Duration.ofSeconds(5));

        if (!lockAcquired) {
            log.warn("Could not acquire lock for slot reservation: {}", lockKey);
            throw new ConflictException("Slot is currently being reserved by another patient");
        }

        try {
            // Double-check slot availability
            boolean slotExists = appointmentRepository.existsByDoctorKeycloakIdAndAppointmentDateAndPeriodAndStatusIn(
                    request.getDoctorKeycloakId(),
                    request.getAppointmentDate(),
                    request.getPeriod(),
                    Arrays.asList(Status.PENDING, Status.PAID, Status.SCHEDULED)
            );

            if (slotExists) {
                throw new ConflictException("Slot is already reserved");
            }

            // Create reservation
            String reservationToken = UUID.randomUUID().toString();
            Instant expiresAt = Instant.now().plus(Duration.ofMinutes(reservationTtlMinutes));

            Appointment appointment = Appointment.builder()
                    .doctorKeycloakId(request.getDoctorKeycloakId())
                    .patientKeycloakId(request.getPatientKeycloakId())
                    .appointmentDate(request.getAppointmentDate())
                    .period(request.getPeriod())
                    .status(Status.PENDING)
                    .reservationToken(reservationToken)
                    .expiresAt(expiresAt)
                    .notes(request.getNotes())
                    .build();

            try {
                Appointment saved = appointmentRepository.save(appointment);

                log.info("Slot reserved successfully: appointmentId={}, token={}",
                        saved.getId(), reservationToken);

                return ReservationResponse.builder()
                        .reservationToken(reservationToken)
                        .expiresAt(expiresAt)
                        .appointmentId(saved.getId())
                        .message("Slot reserved successfully. Please complete payment within " + reservationTtlMinutes + " minutes.")
                        .build();

            } catch (DataIntegrityViolationException e) {
                log.error("Unique constraint violation while reserving slot", e);
                throw new ConflictException("Slot is already reserved");
            }

        } finally {
            reservationLockService.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    public void confirmPayment(ConfirmPaymentRequest request) {
        log.debug("Confirming payment for reservation: {}", request.getReservationToken());

        Appointment appointment = appointmentRepository.findByReservationToken(request.getReservationToken())
                .orElseThrow(() -> new NotFoundException("Reservation not found"));

        // Validate reservation status and expiry
        if (appointment.getStatus() != Status.PENDING) {
            throw new BadRequestException("Reservation is not in PENDING status");
        }

        if (appointment.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Reservation has expired");
        }

        // Check idempotency - if payment already confirmed, return success
        if (appointment.getPaymentIntentId() != null &&
                appointment.getPaymentIntentId().equals(request.getPaymentIntentId())) {
            log.info("Payment already confirmed for reservation: {}", request.getReservationToken());
            return;
        }

        // Update appointment with payment info
        appointment.setPaymentIntentId(request.getPaymentIntentId());
        appointment.setStatus(Status.PAID);
        appointmentRepository.save(appointment);

        // Publish event to outbox
        outboxService.publishPaymentConfirmedEvent(appointment);

        log.info("Payment confirmed successfully for appointment: {}", appointment.getId());
    }

    public String buildLockKey(String doctorId, LocalDate date, Period period) {
        return String.format("appointment:lock:%s:%s:%s", doctorId, date, period);
    }


    @Override
    @Transactional
    public AppointmentResponse adminApproval(AdminApprovalRequest request) {
        log.debug("Processing admin approval: token={}, decision={}",
                request.getReservationToken(), request.getDecision());

        Appointment appointment = appointmentRepository.findByReservationToken(request.getReservationToken())
                .orElseThrow(() -> new NotFoundException("Reservation not found"));

        // Validate appointment is in PAID status
        if (appointment.getStatus() != Status.PAID) {
            throw new BadRequestException("Appointment must be in PAID status for approval");
        }

        if (request.getDecision() == AdminApprovalRequest.ApprovalDecision.APPROVE) {
            return approveAppointment(appointment, request);
        } else {
            return rejectAppointment(appointment, request);
        }
    }

    public AppointmentResponse approveAppointment(Appointment appointment, AdminApprovalRequest request) {
        log.debug("Approving appointment: {}", appointment.getId());

        // Validate room availability
        Optional<Appointment> roomConflict = appointmentRepository.findByRoomIdAndAppointmentDateAndPeriodAndStatus(
                request.getRoomId(),
                appointment.getAppointmentDate(),
                appointment.getPeriod(),
                Status.SCHEDULED
        );

        if (roomConflict.isPresent()) {
            throw new ConflictException("Room is already booked for this time slot");
        }

        // Validate nurse availability
        Optional<Appointment> nurseConflict = appointmentRepository.findByNurseKeycloakIdAndAppointmentDateAndPeriodAndStatus(
                request.getNurseKeycloakId(),
                appointment.getAppointmentDate(),
                appointment.getPeriod(),
                Status.SCHEDULED
        );

        if (nurseConflict.isPresent()) {
            throw new ConflictException("Nurse is already assigned to another appointment at this time");
        }

        try {
            // Capture payment via Stripe
            paymentService.capturePayment(appointment.getPaymentIntentId());

            // Update appointment
            appointment.setRoomId(request.getRoomId());
            appointment.setNurseKeycloakId(request.getNurseKeycloakId());
            appointment.setStatus(Status.SCHEDULED);

            Appointment saved = appointmentRepository.save(appointment);

            // Publish scheduled event to outbox
            outboxService.publishAppointmentScheduledEvent(saved);

            log.info("Appointment approved and scheduled: {}", saved.getId());

            return toResponse(saved);

        } catch (Exception e) {
            log.error("Failed to approve appointment: {}", appointment.getId(), e);
            throw new PaymentException("Failed to capture payment: " + e.getMessage());
        }
    }

    public AppointmentResponse rejectAppointment(Appointment appointment, AdminApprovalRequest request) {
        log.debug("Rejecting appointment: {}", appointment.getId());

        try {
            // Cancel/Refund payment via Stripe
            paymentService.cancelOrRefundPayment(appointment.getPaymentIntentId());

            // Update appointment status
            appointment.setStatus(Status.REJECTED);
            appointment.setNotes(request.getRejectionReason());

            Appointment saved = appointmentRepository.save(appointment);

            // Publish rejected event to outbox
            outboxService.publishAppointmentRejectedEvent(saved, request.getRejectionReason());

            log.info("Appointment rejected: {}", saved.getId());

            return toResponse(saved);

        } catch (Exception e) {
            log.error("Failed to reject appointment: {}", appointment.getId(), e);
            throw new PaymentException("Failed to cancel payment: " + e.getMessage());
        }
    }

    @Override
    public AppointmentResponse getAppointmentById(Long id) {
        log.debug("Fetching appointment: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Appointment not found with id: " + id));

        return toResponse(appointment);
    }

    @Override
    public List<AppointmentResponse> getMyAppointments(String keycloakId, String role) {
        log.debug("Fetching appointments for user: {}, role: {}", keycloakId, role);

        List<Appointment> appointments;

        switch (role.toUpperCase()) {
            case "PATIENT":
                appointments = appointmentRepository.findByPatientKeycloakIdOrderByAppointmentDateDesc(keycloakId);
                break;
            case "DOCTOR":
                appointments = appointmentRepository.findByDoctorKeycloakIdOrderByAppointmentDateDesc(keycloakId);
                break;
            case "NURSE":
                appointments = appointmentRepository.findByNurseKeycloakIdOrderByAppointmentDateDesc(keycloakId);
                break;
            default:
                throw new BadRequestException("Invalid role: " + role);
        }

        return appointments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AppointmentResponse toResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .doctorKeycloakId(appointment.getDoctorKeycloakId())
                .nurseKeycloakId(appointment.getNurseKeycloakId())
                .patientKeycloakId(appointment.getPatientKeycloakId())
                .departmentId(appointment.getDepartmentId())
                .roomId(appointment.getRoomId())
                .status(appointment.getStatus())
                .appointmentDate(appointment.getAppointmentDate())
                .period(appointment.getPeriod())
                .reservationToken(appointment.getReservationToken())
                .paymentId(appointment.getPaymentId())
                .expiresAt(appointment.getExpiresAt())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .notes(appointment.getNotes())
                .build();
    }
}
