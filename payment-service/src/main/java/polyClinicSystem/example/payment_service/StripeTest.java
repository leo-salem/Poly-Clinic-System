package polyClinicSystem.example.payment_service;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
public class StripeTest {

    @Value("${stripe.api.key}")
    private String stripeKey;

    @PostConstruct
    public void testStripeConnection() {
        Stripe.apiKey = stripeKey;

        try {
            com.stripe.model.Account.retrieve();
            System.out.println("Stripe connection successful");
        } catch (Exception e) {
            System.out.println("Stripe connection failed: " + e.getMessage());
        }
    }
}