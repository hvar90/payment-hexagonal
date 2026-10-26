package com.payment.gateway.infrastructure.adapter.inbound.webhook;

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
@RequestMapping("/api/v1/payments/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final PaymentWebhookService paymentWebhookService;

    @PostMapping
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

// DTO auxiliar para las notificaciones de Webhook
class WebhookRequestDTO {
    private String paymentId;
    private PaymentStatus status;
    
    public String getPaymentId() { return paymentId; }
    public PaymentStatus getStatus() { return status; }
}