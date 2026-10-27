package com.payment.gateway.infrastructure.scheduler;


import com.payment.gateway.domain.model.Payment;
import com.payment.gateway.domain.model.PaymentStatus;
import com.payment.gateway.domain.port.outbound.ExternalPaymentGatewayPort;
import com.payment.gateway.domain.port.outbound.PaymentRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentReconciliationScheduler {

    private final PaymentRepositoryPort paymentRepositoryPort;
    private final ExternalPaymentGatewayPort externalPaymentGatewayPort;

    @Scheduled(fixedRate = 3600000)
    public void reconcilePendingPayments() {
        log.info("Iniciando proceso de conciliación automática para pagos huérfanos en estado PENDING...");

        LocalDateTime thresholdTime = LocalDateTime.now().minusMinutes(30);

        paymentRepositoryPort.findByStatusAndCreatedAtBefore(PaymentStatus.PENDING, thresholdTime)
                .flatMap(payment -> {
                    log.warn("Procesando pago huérfano ID: {} con referencia: {}", payment.getId(), payment.getReference());
                    
                    return externalPaymentGatewayPort.checkStatus(payment)
                            .flatMap(externalPayment -> {
                                if (externalPayment.getStatus() != PaymentStatus.PENDING) {
                                    payment.updateStatus(externalPayment.getStatus());
                                    log.info("Actualizando estado del pago huérfano {} a {}", payment.getId(), payment.getStatus());
                                    return paymentRepositoryPort.save(payment);
                                }
                                return Mono.empty();
                            })
                            .onErrorResume(error -> {
                                log.error("Error al consultar el estado en la pasarela para el pago ID {}: {}", payment.getId(), error.getMessage());
                                return Mono.empty();
                            });
                })
                .subscribe(
                        success -> log.debug("Pago conciliado y actualizado exitosamente."),
                        error -> log.error("Error crítico durante el proceso de conciliación: {}", error.getMessage()),
                        () -> log.info("Proceso de conciliación automática finalizado.")
                );
    }
}
