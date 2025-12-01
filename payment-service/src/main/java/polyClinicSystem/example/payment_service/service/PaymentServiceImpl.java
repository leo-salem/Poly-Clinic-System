package polyClinicSystem.example.payment_service.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCaptureParams;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import polyClinicSystem.example.payment_service.client.UserClient;
import polyClinicSystem.example.payment_service.dto.Request.CreatePaymentRequest;
import polyClinicSystem.example.payment_service.dto.Response.PaymentResponse;
import polyClinicSystem.example.payment_service.dto.Response.UserResponse;
import polyClinicSystem.example.payment_service.exception.customExceptions.AccessDeniedException;
import polyClinicSystem.example.payment_service.exception.customExceptions.NotFoundException;
import polyClinicSystem.example.payment_service.exception.customExceptions.PaymentException;
import polyClinicSystem.example.payment_service.model.Payment;
import polyClinicSystem.example.payment_service.model.enums.Status;
import polyClinicSystem.example.payment_service.repository.PaymentRepository;
import polyClinicSystem.example.payment_service.service.token.TokenService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final TokenService tokenService;
    private final UserClient userClient;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    private UserResponse getCurrentUser(HttpServletRequest request) throws NotFoundException {
        String userId = tokenService.extractUserId(request);
        log.debug("Fetching current user with ID: {}", userId);

        try {
            return userClient.getUserByKeycloakId(userId);
        } catch (Exception e) {
            log.error("Failed to fetch user with ID: {}", userId, e);
            throw new NotFoundException("User not found with id: " + userId);
        }
    }

    private String extractUserRole(HttpServletRequest request) {
        String token = tokenService.extractToken(request);
        if (token == null) {
            throw new AccessDeniedException("Token not found");
        }
        UserResponse currentUser = getCurrentUser(request);
        return currentUser.getRole().name();

    }

    private void checkPatientAccess(HttpServletRequest request, String patientKeycloakId) {
        String currentUserId = tokenService.extractUserId(request);
        String role = extractUserRole(request);

        if (!"ADMIN".equals(role) && !currentUserId.equals(patientKeycloakId)) {
            throw new AccessDeniedException("Access denied to this payment");
        }
    }

    private void checkPaymentOwnership(HttpServletRequest request, Payment payment) {
        String currentUserId = tokenService.extractUserId(request);
        String role = extractUserRole(request);

        if (!"ADMIN".equals(role) && !currentUserId.equals(payment.getPatientKeycloakId())) {
            throw new AccessDeniedException("You can only access your own payments");
        }
    }

    @Override
    @Transactional
    public PaymentResponse createPaymentIntent(CreatePaymentRequest request, HttpServletRequest httpRequest) {
        log.debug("Creating payment intent: amount={}, currency={}", request.getAmount(), request.getCurrency());

        String currentUserId = tokenService.extractUserId(httpRequest);
        String role = extractUserRole(httpRequest);

        if (!"ADMIN".equals(role) && !currentUserId.equals(request.getPatientKeycloakId())) {
            throw new AccessDeniedException("You can only create payments for yourself");
        }

        try {
            // Convert amount to cents (Stripe requires smallest currency unit)
            Long amountInCents = request.getAmount().multiply(new BigDecimal("100")).longValue();

            // Create PaymentIntent with manual capture
            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(request.getCurrency())
                    .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL)
                    .setDescription(request.getDescription());

            if (request.getCustomerId() == null) {
                Customer customer = Customer.create(
                        CustomerCreateParams.builder()
                                .setDescription("Customer for " + request.getPatientKeycloakId())
                                .build()
                );
                request.setCustomerId(customer.getId());
            }

            // Add customer if provided
            if (request.getCustomerId() != null && !request.getCustomerId().trim().isEmpty()) {
                paramsBuilder.setCustomer(request.getCustomerId());
            }

            PaymentIntent paymentIntent = PaymentIntent.create(paramsBuilder.build());

            // Save payment record
            Payment payment = Payment.builder()
                    .appointmentId(request.getAppointmentId())
                    .patientKeycloakId(request.getPatientKeycloakId())
                    .paymentIntentId(paymentIntent.getId())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .status(Status.PENDING)
                    .description(request.getDescription())
                    .build();

            Payment saved = paymentRepository.save(payment);

            log.info("Payment intent created: id={}, paymentIntentId={}", saved.getId(), paymentIntent.getId());

            return PaymentResponse.builder()
                    .id(saved.getId())
                    .appointmentId(saved.getAppointmentId())
                    .patientKeycloakId(saved.getPatientKeycloakId())
                    .paymentIntentId(saved.getPaymentIntentId())
                    .clientSecret(paymentIntent.getClientSecret())
                    .amount(saved.getAmount())
                    .currency(saved.getCurrency())
                    .status(saved.getStatus())
                    .createdAt(saved.getCreatedAt())
                    .build();

        } catch (StripeException e) {
            log.error("Failed to create PaymentIntent", e);
            throw new PaymentException("Payment creation failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PaymentResponse confirmPayment(String paymentIntentId, HttpServletRequest httpRequest) {
        log.debug("Confirming payment: {}", paymentIntentId);

        Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new PaymentException("Payment not found"));

        checkPaymentOwnership(httpRequest, payment);

        payment.setStatus(Status.AUTHORIZED);
        Payment saved = paymentRepository.save(payment);

        log.info("Payment confirmed: id={}", saved.getId());

        return toResponse(saved);
    }

    @Override
    public List<PaymentResponse> getMyPayments(HttpServletRequest httpRequest) {
        String currentUserId = tokenService.extractUserId(httpRequest);
        return paymentRepository.findByPatientKeycloakIdOrderByCreatedAtDesc(currentUserId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponse getPaymentById(Long id, HttpServletRequest httpRequest) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentException("Payment not found"));

        checkPaymentOwnership(httpRequest, payment);

        return toResponse(payment);
    }

    @Override
    public List<PaymentResponse> getAllPayments(HttpServletRequest httpRequest) {

        String role =extractUserRole(httpRequest);
        if (!role.equals( "ADMIN")) {
            throw new AccessDeniedException("you are not admin to access all payments");
        }

        return paymentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .appointmentId(payment.getAppointmentId())
                .patientKeycloakId(payment.getPatientKeycloakId())
                .paymentIntentId(payment.getPaymentIntentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    @Override
    public void capturePayment(String paymentIntentId, HttpServletRequest httpRequest) {
        log.debug("Capturing payment: paymentIntentId={}", paymentIntentId);

        String role =extractUserRole(httpRequest);
        if (!role.equals( "ADMIN")) {
            throw new AccessDeniedException("you are not admin to capture ");
        }


        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            // Check if already captured
            if ("succeeded".equals(paymentIntent.getStatus())) {
                log.info("Payment already captured: {}", paymentIntentId);
                return;
            }

            // Capture the payment
            PaymentIntentCaptureParams params = PaymentIntentCaptureParams.builder().build();
            paymentIntent.capture(params);

            // Update payment status in database
            Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new PaymentException("Payment not found"));
            payment.setStatus(Status.COMPLETED);
            paymentRepository.save(payment);

            log.info("Payment captured successfully: {}", paymentIntentId);

        } catch (StripeException e) {
            log.error("Failed to capture payment: {}", paymentIntentId, e);
            throw new PaymentException("Failed to capture payment: " + e.getMessage());
        }
    }

    @Override
    public void cancelOrRefundPayment(String paymentIntentId, HttpServletRequest httpRequest) {
        log.debug("Cancelling/refunding payment: paymentIntentId={}", paymentIntentId);

        String role =extractUserRole(httpRequest);
        if (!role.equals( "ADMIN")) {
            throw new AccessDeniedException("you are not admin to cancel or refund");
        }


        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            String status = paymentIntent.getStatus();

            if ("requires_capture".equals(status)) {
                // Payment was authorized but not captured - we can cancel it
                paymentIntent.cancel();

                // Update payment status in database
                Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                        .orElseThrow(() -> new PaymentException("Payment not found"));
                payment.setStatus(Status.CANCELLED);
                paymentRepository.save(payment);

                log.info("Payment cancelled: {}", paymentIntentId);

            } else if ("succeeded".equals(status)) {
                // Payment was already captured - need to refund
                com.stripe.model.Refund refund = com.stripe.model.Refund.create(
                        com.stripe.param.RefundCreateParams.builder()
                                .setPaymentIntent(paymentIntentId)
                                .build()
                );

                // Update payment status in database
                Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                        .orElseThrow(() -> new PaymentException("Payment not found"));
                payment.setStatus(Status.REFUNDED);
                paymentRepository.save(payment);

                log.info("Payment refunded: {}, refundId={}", paymentIntentId, refund.getId());

            } else {
                log.warn("Payment in unexpected status for cancel/refund: status={}", status);
                throw new PaymentException("Payment in unexpected status: " + status);
            }

        } catch (StripeException e) {
            log.error("Failed to cancel/refund payment: {}", paymentIntentId, e);
            throw new PaymentException("Failed to cancel/refund payment: " + e.getMessage());
        }
    }
}