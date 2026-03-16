package com.mentoria.backend.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class StripeConfig {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${stripe.price-basic}")
    private String priceBasic;

    @Value("${stripe.price-premium}")
    private String pricePremium;

    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = secretKey;
    }
}