package com.payment.gateway.infrastructure.adapter.inbound.rest;

import com.payment.gateway.application.service.PaymentProcessService;
import com.payment.gateway.application.service.PaymentWebhookService;
import com.payment.gateway.domain.model.Payment;
import com.payment.gateway.domain.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentProcessService paymentProcessService;
    private final PaymentWebhookService paymentWebhookService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Payment> createPayment(@RequestBody PaymentRequestDTO request) {
        Payment payment = new Payment(
                request.getId(),
                request.getAmount(),
                request.getCurrency(),
                request.getDescription()
        );
        return paymentProcessService.processPayment(payment);
    }

    @PostMapping("/webhook")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Payment> handleWebhook(@RequestBody WebhookRequestDTO request) {
        return paymentWebhookService.handleWebhookNotification(request.getPaymentId(), request.getStatus());
    }
}

// DTOs auxiliares para el adaptador web
class PaymentRequestDTO {
    private String id;
    private java.math.BigDecimal amount;
    private String currency;
    private String description;
    // Getters y Setters
    public String getId() { return id; }
    public java.math.BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getDescription() { return description; }
}

class WebhookRequestDTO {
    private String paymentId;
    private PaymentStatus status;
    // Getters y Setters
    public String getPaymentId() { return paymentId; }
    public PaymentStatus getStatus() { return status; }
}