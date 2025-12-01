package polyClinicSystem.example.payment_service.controller;


import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<PaymentResponse> createPaymentIntent(
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest httpRequest) {

        PaymentResponse response = paymentService.createPaymentIntent(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/confirm/{paymentIntentId}")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @PathVariable String paymentIntentId,
            HttpServletRequest httpRequest) {

        PaymentResponse response = paymentService.confirmPayment(paymentIntentId, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/capture/{paymentIntentId}")
    public ResponseEntity<String> capturePayment(
            @PathVariable String paymentIntentId,
            HttpServletRequest httpRequest) {

        paymentService.capturePayment(paymentIntentId, httpRequest);
        return ResponseEntity.ok("Payment captured successfully.");
    }

    @PostMapping("/cancel-or-refund/{paymentIntentId}")
    public ResponseEntity<String> cancelOrRefund(
            @PathVariable String paymentIntentId,
            HttpServletRequest httpRequest) {

        paymentService.cancelOrRefundPayment(paymentIntentId, httpRequest);
        return ResponseEntity.ok("Payment cancelled or refunded successfully.");
    }

    @GetMapping("/my-payments")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(HttpServletRequest httpRequest) {
        List<PaymentResponse> payments = paymentService.getMyPayments(httpRequest);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        PaymentResponse response = paymentService.getPaymentById(id, httpRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<PaymentResponse>> getAllPayments(HttpServletRequest httpRequest) {
        List<PaymentResponse> payments = paymentService.getAllPayments(httpRequest);
        return ResponseEntity.ok(payments);
    }
}