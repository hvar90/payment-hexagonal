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
                .map(response -> {
                    if ("APPROVED".equalsIgnoreCase(response.getStatus())) {
                        payment.approve();
                        log.info("Pago ID: {} aprobado por la pasarela externa", payment.getId());
                    } else {
                        payment.reject();
                        log.warn("Pago ID: {} rechazado por la pasarela externa", payment.getId());
                    }
                    return payment;
                })
                // Aplica Circuit Breaker y Retry de forma reactiva y no bloqueante
                .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker("paymentService")))
                .transformDeferred(RetryOperator.of(retryRegistry.retry("paymentService")))
                .onErrorResume(error -> {
                    log.error("EJECUCIÓN DE FALLBACK: Falla con la pasarela externa para el pago ID {}: {}", payment.getId(), error.getMessage());
                    return Mono.error(new RuntimeException("La pasarela de pagos externa no se encuentra disponible temporalmente.", error));
                });
    }
}

class ExternalPaymentResponseDTO {
    private String transactionId;
    private String status;

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}