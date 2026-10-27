package com.payment.gateway.infrastructure.adapter.outbound.gateway;

import com.payment.gateway.domain.model.Payment;
import com.payment.gateway.domain.port.outbound.ExternalPaymentGatewayPort;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.payment.gateway.infrastructure.adapter.outbound.gateway.dto.ExternalPaymentResponseDTO;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalPaymentGatewayAdapter implements ExternalPaymentGatewayPort {

    private final WebClient paymentGatewayWebClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    @Override
    public Mono<Payment> charge(Payment payment) {
        log.info("Enviando pago ID: {} a la pasarela externa para procesamiento", payment.getId());

        return paymentGatewayWebClient.post()
                .uri("/charges")
                .bodyValue(payment)
                .retrieve()
                .bodyToMono(ExternalPaymentResponseDTO.class)
                .map(response -> updatePaymentStatusFromResponse(payment, response))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker("paymentService")))
                .transformDeferred(RetryOperator.of(retryRegistry.retry("paymentService")))
                .onErrorResume(error -> {
                    log.error("EJECUCIÓN DE FALLBACK: Falla con la pasarela externa para el pago ID {}: {}", payment.getId(), error.getMessage());
                    return Mono.error(new RuntimeException("La pasarela de pagos externa no se encuentra disponible temporalmente.", error));
                });
    }

    @Override
    public Mono<Payment> checkStatus(Payment payment) {
        log.info("Consultando estado actual en la pasarela externa para la referencia: {}", payment.getReference());

        return paymentGatewayWebClient.get()
                .uri("/charges/{reference}", payment.getReference())
                .retrieve()
                .bodyToMono(ExternalPaymentResponseDTO.class)
                .map(response -> updatePaymentStatusFromResponse(payment, response))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker("paymentService")))
                .transformDeferred(RetryOperator.of(retryRegistry.retry("paymentService")))
                .onErrorResume(error -> {
                    log.error("Error al consultar estado en pasarela para referencia {}: {}", payment.getReference(), error.getMessage());
                    return Mono.error(error);
                });
    }

    private Payment updatePaymentStatusFromResponse(Payment payment, ExternalPaymentResponseDTO response) {
        if ("APPROVED".equalsIgnoreCase(response.getStatus())) {
            payment.approve();
            log.info("Pago ID: {} aprobado por la pasarela externa", payment.getId());
        } else if ("REJECTED".equalsIgnoreCase(response.getStatus())) {
            payment.reject();
            log.warn("Pago ID: {} rechazado por la pasarela externa", payment.getId());
        }
        return payment;
    }
}
