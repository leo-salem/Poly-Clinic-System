package polyClinicSystem.example.payment_service.service;

import jakarta.servlet.http.HttpServletRequest;
import polyClinicSystem.example.payment_service.dto.Request.CreatePaymentRequest;
import polyClinicSystem.example.payment_service.dto.Response.PaymentResponse;
import polyClinicSystem.example.payment_service.model.Payment;

import java.util.*;

public interface PaymentService {

    PaymentResponse createPaymentIntent(CreatePaymentRequest request, HttpServletRequest httpRequest);
    PaymentResponse confirmPayment(String paymentIntentId, HttpServletRequest httpRequest);
    List<PaymentResponse> getMyPayments(HttpServletRequest httpRequest);
    PaymentResponse getPaymentById(Long id, HttpServletRequest httpRequest);
    void capturePayment(String paymentIntentId, HttpServletRequest httpRequest);
    void cancelOrRefundPayment(String paymentIntentId, HttpServletRequest httpRequest);


    List<PaymentResponse> getAllPayments(HttpServletRequest httpRequest);


    PaymentResponse toResponse(Payment payment);
}