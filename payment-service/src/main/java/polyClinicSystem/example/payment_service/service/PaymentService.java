package polyClinicSystem.example.payment_service.service;

import polyClinicSystem.example.payment_service.dto.Request.CreatePaymentRequest;
import polyClinicSystem.example.payment_service.dto.Response.PaymentResponse;
import polyClinicSystem.example.payment_service.model.Payment;

import java.util.*;

public interface PaymentService {

    void capturePayment(String paymentIntentId);

    void cancelOrRefundPayment(String paymentIntentId);

    PaymentResponse createPaymentIntent(CreatePaymentRequest request);

    PaymentResponse confirmPayment(String paymentIntentId);

    List<PaymentResponse> getMyPayments(String patientKeycloakId);

    PaymentResponse getPaymentById(Long id);

    PaymentResponse toResponse(Payment payment);
}
