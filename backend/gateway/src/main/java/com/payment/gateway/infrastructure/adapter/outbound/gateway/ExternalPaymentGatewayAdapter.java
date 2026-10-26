package com.payment.gateway.infrastructure.adapter.outbound.gateway;

import com.payment.gateway.domain.model.Payment;
import com.payment.gateway.domain.port.outbound.ExternalPaymentGatewayPort;
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

    @Override
    public Mono<Payment> charge(Payment payment) {
        log.info("Enviando pago ID: {} a la pasarela externa para procesamiento", payment.getId());

        return paymentGatewayWebClient.post()
                .uri("/charges")
                .bodyValue(payment)
                .retrieve()
                .bodyToMono(ExternalPaymentResponseDTO.class)
                .map(response -> {
                    // Traducir la respuesta del proveedor externo a las reglas de negocio del dominio
                    if ("APPROVED".equalsIgnoreCase(response.getStatus())) {
                        payment.approve();
                        log.info("Pago ID: {} aprobado por la pasarela externa", payment.getId());
                    } else {
                        payment.reject();
                        log.warn("Pago ID: {} rechazado por la pasarela externa", payment.getId());
                    }
                    return payment;
                })
                .onErrorResume(error -> {
                    log.error("Error de comunicación con la pasarela externa para el pago ID {}: {}", payment.getId(), error.getMessage());
                    // En caso de caída del proveedor o timeout, propagamos un error controlado
                    return Mono.error(new RuntimeException("Falla en el servicio de la pasarela externa", error));
                });
    }
}

// DTO interno para mapear la respuesta de la pasarela de pagos
class ExternalPaymentResponseDTO {
    private String transactionId;
    private String status;

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}