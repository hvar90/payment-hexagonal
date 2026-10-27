package com.payment.gateway.infrastructure.adapter.inbound.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.gateway.application.service.PaymentWebhookService;
import com.payment.gateway.domain.model.Payment;
import com.payment.gateway.infrastructure.adapter.inbound.webhook.dto.WebhookRequestDTO;
import com.payment.gateway.infrastructure.security.WebhookSignatureValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final PaymentWebhookService paymentWebhookService;
    private final WebhookSignatureValidator signatureValidator;
    private final ObjectMapper objectMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public Mono<Payment> handleWebhook(
            @RequestHeader("X-Signature") String signature,
            @RequestBody String rawPayload
    ) {
        log.info("Recibida notificación Webhook con firma para validación de seguridad");

        boolean isValid = signatureValidator.isValidSignature(rawPayload, signature);
        if (!isValid) {
            return Mono.error(new SecurityException("Firma de webhook inválida"));
        }

        return Mono.fromCallable(() -> objectMapper.readValue(rawPayload, WebhookRequestDTO.class))
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                .flatMap(request -> paymentWebhookService.handleWebhookNotification(
                        request.getEventId(),
                        request.getPaymentId(),
                        request.getEventType(), 
                        request.getStatus()
                ))
                .doOnSuccess(updated -> log.info("Webhook procesado correctamente para el pago ID: {}", updated.getId()))
                .doOnError(error -> log.error("Error procesando webhook: {}", error.getMessage()));
    }
}