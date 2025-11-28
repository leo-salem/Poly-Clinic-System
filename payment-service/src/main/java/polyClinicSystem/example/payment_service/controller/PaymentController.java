package polyClinicSystem.example.payment_service.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import polyClinicSystem.example.payment_service.dto.Request.CreatePaymentRequest;
import polyClinicSystem.example.payment_service.dto.Response.PaymentResponse;
import polyClinicSystem.example.payment_service.service.PaymentService;
import polyClinicSystem.example.payment_service.service.token.TokenService;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final TokenService tokenService;

    @PostMapping("/create-intent")
    public ResponseEntity<PaymentResponse> createPaymentIntent(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.createPaymentIntent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/confirm/{paymentIntentId}")
    public ResponseEntity<PaymentResponse> confirmPayment(@PathVariable String paymentIntentId) {
        PaymentResponse response = paymentService.confirmPayment(paymentIntentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/capture/{paymentIntentId}")
    public ResponseEntity<String> capturePayment(@PathVariable String paymentIntentId) {
        paymentService.capturePayment(paymentIntentId);
        return ResponseEntity.ok("Payment captured successfully.");
    }

    @PostMapping("/cancel-or-refund/{paymentIntentId}")
    public ResponseEntity<String> cancelOrRefund(@PathVariable String paymentIntentId) {
        paymentService.cancelOrRefundPayment(paymentIntentId);
        return ResponseEntity.ok("Payment cancelled or refunded successfully.");
    }


    @GetMapping("/my-payments/{patientKeycloakId}")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(@PathVariable String patientKeycloakId ) {
        List<PaymentResponse> payments = paymentService.getMyPayments(patientKeycloakId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(response);
    }
}