package com.payment.gateway.infrastructure.adapter.inbound.rest;

import com.payment.gateway.application.service.PaymentProcessService;
import com.payment.gateway.application.service.PaymentWebhookService;
import com.payment.gateway.domain.model.Payment;
import com.payment.gateway.domain.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; 
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j 
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentProcessService paymentProcessService;
    private final PaymentWebhookService paymentWebhookService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Payment> createPayment(@RequestBody PaymentRequestDTO request) {
        log.info("Recibida solicitud HTTP POST /api/v1/payments para crear pago ID: {} con monto: {} {}", 
                request.getId(), request.getAmount(), request.getCurrency());

        Payment payment = new Payment(
                request.getId(),
                request.getAmount(),
                request.getCurrency(),
                request.getDescription()
        );
        
        return paymentProcessService.processPayment(payment)
                .doOnSuccess(saved -> log.info("Pago ID: {} procesado exitosamente vía REST", saved.getId()))
                .doOnError(error -> log.error("Error al procesar el pago ID: {}: {}", request.getId(), error.getMessage()));
    }

    @PostMapping("/webhook")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Payment> handleWebhook(@RequestBody WebhookRequestDTO request) {
        log.info("Recibida notificación Webhook POST /api/v1/payments/webhook para pago ID: {} con nuevo estado: {}", 
                request.getPaymentId(), request.getStatus());

        return paymentWebhookService.handleWebhookNotification(request.getPaymentId(), request.getStatus())
                .doOnSuccess(updated -> log.info("Webhook procesado correctamente para el pago ID: {} con estado actual: {}", 
                        updated.getId(), updated.getStatus()))
                .doOnError(error -> log.error("Error procesando webhook para el pago ID: {}: {}", 
                        request.getPaymentId(), error.getMessage()));
    }
}

// DTOs auxiliares para el adaptador web
class PaymentRequestDTO {
    private String id;
    private java.math.BigDecimal amount;
    private String currency;
    private String description;
    
    public String getId() { return id; }
    public java.math.BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getDescription() { return description; }
}

class WebhookRequestDTO {
    private String paymentId;
    private PaymentStatus status;
    
    public String getPaymentId() { return paymentId; }
    public PaymentStatus getStatus() { return status; }
}