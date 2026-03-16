package com.mentoria.backend.controller;

import com.mentoria.backend.dto.request.CheckoutRequest;
import com.mentoria.backend.dto.response.ApiResponse;
import com.mentoria.backend.model.Subscription;
import com.mentoria.backend.service.StripeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final StripeService stripeService;
    private final AuthHelper authHelper;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<Map<String, String>>> createCheckout(
            @Valid @RequestBody CheckoutRequest request) {
        UUID userId = authHelper.getCurrentUserId();
        String url = stripeService.createCheckoutSession(
                userId, request.getPlan(),
                request.getSuccessUrl(), request.getCancelUrl()
        );
        return ResponseEntity.ok(ApiResponse.ok(Map.of("url", url)));
    }

    // Webhook autenticado via assinatura Stripe, não JWT
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        stripeService.handleWebhook(payload, sigHeader);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/subscription")
    public ResponseEntity<ApiResponse<Subscription>> getSubscription() {
        UUID userId = authHelper.getCurrentUserId();
        Subscription subscription = stripeService.getUserSubscription(userId);
        return ResponseEntity.ok(ApiResponse.ok(subscription));
    }
}