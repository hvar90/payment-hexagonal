package com.payment.gateway.infrastructure.adapter.inbound.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.gateway.application.service.PaymentWebhookService;
import com.payment.gateway.domain.model.Payment;
import com.payment.gateway.domain.model.PaymentStatus;
import com.payment.gateway.infrastructure.security.WebhookSignatureValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.payment.gateway.infrastructure.adapter.inbound.rest.exception.MalformedWebhookPayloadException;
import com.payment.gateway.infrastructure.adapter.inbound.webhook.dto.WebhookRequestDTO;

import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final PaymentWebhookService paymentWebhookService;
    private final WebhookSignatureValidator signatureValidator;
    private final ObjectMapper objectMapper; 

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public Mono<Payment> handleWebhook(
            @RequestHeader("X-Signature") String signature,
            @RequestBody String rawPayload // Recibimos el cuerpo crudo para que la firma coincida exactamente
    ) {
        log.info("Recibida notificación Webhook con firma para validación de seguridad");

        // 2. Validar la autenticidad e integridad mediante la firma HMAC
        boolean isValid = signatureValidator.isValidSignature(rawPayload, signature);
        if (!isValid) {
            log.warn("¡Intento de webhook fraudulento o firma inválida detectada!");
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Firma de webhook inválida"));
        }

        // 3. Convertir el texto plano (JSON) a nuestro DTO usando el ObjectMapper inyectado
        WebhookRequestDTO request;
        try {
            request = objectMapper.readValue(rawPayload, WebhookRequestDTO.class);
        } catch (Exception e) {
            log.error("Error al deserializar el JSON del webhook: {}", e.getMessage());
            return Mono.error(new MalformedWebhookPayloadException("Error al parsear el JSON del webhook", e));
        }

        // 4. Continuar con la lógica de negocio de manera reactiva
        return paymentWebhookService.handleWebhookNotification(request.getPaymentId(), request.getStatus())
                .doOnSuccess(updated -> log.info("Webhook procesado correctamente para el pago ID: {}", updated.getId()))
                .doOnError(error -> log.error("Error procesando webhook: {}", error.getMessage()));
    }
}

