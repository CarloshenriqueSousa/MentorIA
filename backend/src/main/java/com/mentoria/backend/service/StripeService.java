package com.mentoria.backend.service;

import com.mentoria.backend.config.StripeConfig;
import com.mentoria.backend.exception.BusinessException;
import com.mentoria.backend.exception.ResourceNotFoundException;
import com.mentoria.backend.model.Subscription;
import com.mentoria.backend.model.User;
import com.mentoria.backend.repository.SubscriptionRepository;
import com.mentoria.backend.repository.UserRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {

    private final StripeConfig stripeConfig;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    public String createCheckoutSession(UUID userId, String plan, String successUrl, String cancelUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        try {
            String customerId = getOrCreateCustomer(user);
            String priceId = plan.equals("PREMIUM")
                    ? stripeConfig.getPricePremium()
                    : stripeConfig.getPriceBasic();

            String resolvedSuccess = successUrl != null
                    ? successUrl : "http://localhost:3000/dashboard?upgrade=success";
            String resolvedCancel = cancelUrl != null
                    ? cancelUrl : "http://localhost:3000/pricing";

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setCustomer(customerId)
                    .setSuccessUrl(resolvedSuccess + "&session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(resolvedCancel)
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setPrice(priceId).setQuantity(1L).build())
                    .putMetadata("user_id", userId.toString())
                    .putMetadata("plan", plan)
                    .build();

            return Session.create(params).getUrl();

        } catch (StripeException e) {
            log.error("Stripe error: {}", e.getMessage());
            throw new BusinessException("Erro ao criar sessão de pagamento");
        }
    }

    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            throw new BusinessException("Assinatura do webhook inválida");
        }

        log.info("Stripe webhook: {}", event.getType());

        switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutCompleted(event);
            case "customer.subscription.deleted" -> handleSubscriptionDeleted(event);
            case "invoice.payment_failed" -> handlePaymentFailed(event);
            default -> log.debug("Evento não tratado: {}", event.getType());
        }
    }

    private void handleCheckoutCompleted(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElseThrow();
        String userId = session.getMetadata().get("user_id");
        String plan = session.getMetadata().get("plan");
        if (userId == null) return;

        UUID userUUID = UUID.fromString(userId);
        User user = userRepository.findById(userUUID).orElse(null);
        if (user == null) return;

        User.PlanType planType = plan.equals("PREMIUM") ? User.PlanType.PREMIUM : User.PlanType.BASIC;

        userRepository.updatePlanType(userUUID, planType);

        Subscription sub = subscriptionRepository.findByUser_Id(userUUID)
                .orElse(Subscription.builder().user(user).build());
        sub.setPlanType(planType);
        sub.setStripeSubscriptionId(session.getSubscription());
        sub.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(sub);

        log.info("Usuário {} fez upgrade para {}", userId, plan);
    }

    private void handleSubscriptionDeleted(Event event) {
        com.stripe.model.Subscription s =
                (com.stripe.model.Subscription) event.getDataObjectDeserializer().getObject().orElseThrow();

        subscriptionRepository.findByStripeSubscriptionId(s.getId()).ifPresent(sub -> {
            sub.setStatus(Subscription.SubscriptionStatus.CANCELLED);
            sub.setCancelledAt(LocalDateTime.now());
            sub.setPlanType(User.PlanType.FREE);
            subscriptionRepository.save(sub);

            userRepository.updatePlanType(sub.getUser().getId(), User.PlanType.FREE);
            log.info("Usuário {} revertido para FREE", sub.getUser().getId());
        });
    }

    private void handlePaymentFailed(Event event) {
        Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElseThrow();
        subscriptionRepository.findByStripeSubscriptionId(invoice.getSubscription()).ifPresent(sub -> {
            sub.setStatus(Subscription.SubscriptionStatus.PAST_DUE);
            subscriptionRepository.save(sub);
        });
    }

    private String getOrCreateCustomer(User user) throws StripeException {
        if (user.getStripeCustomerId() != null) return user.getStripeCustomerId();

        Customer customer = Customer.create(CustomerCreateParams.builder()
                .setEmail(user.getEmail())
                .setName(user.getName())
                .putMetadata("user_id", user.getId().toString())
                .build());

        user.setStripeCustomerId(customer.getId());
        userRepository.save(user);
        return customer.getId();
    }

    public Subscription getUserSubscription(UUID userId) {
        return subscriptionRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
                    return Subscription.builder()
                            .user(user)
                            .planType(User.PlanType.FREE)
                            .status(Subscription.SubscriptionStatus.ACTIVE)
                            .build();
                });
    }
}