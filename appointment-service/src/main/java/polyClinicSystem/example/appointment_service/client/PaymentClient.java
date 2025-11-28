package polyClinicSystem.example.appointment_service.client;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import polyClinicSystem.example.appointment_service.dto.request.CreatePaymentRequest;
import polyClinicSystem.example.appointment_service.dto.response.PaymentResponse;

import java.util.List;


@HttpExchange
public interface PaymentClient {

    @PostMapping("/api/payments/create-intent")
    PaymentResponse createPaymentIntent(@Valid @RequestBody CreatePaymentRequest request);

    @PostMapping("/api/payments/confirm/{paymentIntentId}")
    PaymentResponse confirmPayment(@PathVariable String paymentIntentId);

    @PostMapping("/api/payments/capture/{paymentIntentId}")
    void capturePayment(@PathVariable String paymentIntentId);

    @PostMapping("/api/payments/cancel-or-refund/{paymentIntentId}")
    void cancelOrRefundPayment(@PathVariable String paymentIntentId);

    @GetMapping("/api/payments/my-payments/{patientKeycloakId}")
    List<PaymentResponse> getMyPayments(@PathVariable String patientKeycloakId);

    @GetMapping("/api/payments/{id}")
    ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id);
}
