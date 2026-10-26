package com.payment.gateway.application.service;

import org.springframework.stereotype.Service;

import com.payment.gateway.domain.model.Payment;
import com.payment.gateway.domain.model.PaymentStatus;
import com.payment.gateway.domain.port.outbound.PaymentRepositoryPort;
import com.payment.gateway.infrastructure.adapter.outbound.messaging.PaymentEventProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentWebhookService {

    private final PaymentRepositoryPort paymentRepositoryPort;
    private final PaymentEventProducer paymentEventProducer;

    public Mono<Payment> handleWebhookNotification(String paymentId, PaymentStatus newStatus) {
        log.info("Recibiendo notificación webhook para el pago ID: {} con nuevo estado: {}", paymentId, newStatus);

        return paymentRepositoryPort.findById(paymentId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Pago no encontrado: " + paymentId)))
                .flatMap(payment -> {
                    // Control de Idempotencia: Si el pago ya se encuentra en el estado notificado, se omite duplicidad
                    if (payment.getStatus() == newStatus) {
                        log.warn("El pago ID {} ya registra el estado {}. Notificación duplicada ignorada.", paymentId, newStatus);
                        return Mono.just(payment);
                    }

                    // Actualizar estado según la regla de negocio (Esto puede lanzar InvalidPaymentStateException)
                    if (newStatus == PaymentStatus.APPROVED) {
                        payment.approve();
                    } else if (newStatus == PaymentStatus.REJECTED) {
                        payment.reject();
                    }

                    return paymentRepositoryPort.save(payment);
                })
                // Emitir evento a RabbitMQ de forma no bloqueante protegiendo el event loop
                .flatMap(result -> 
                    Mono.fromRunnable(() -> paymentEventProducer.sendPaymentEvent(result.getId(), result.getStatus().name()))
                        .subscribeOn(Schedulers.boundedElastic())
                        .thenReturn(result)
                );
    }
}