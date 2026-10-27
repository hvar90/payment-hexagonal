package com.payment.gateway.application.service;

import org.springframework.stereotype.Service;

import com.payment.gateway.domain.model.Payment;
import com.payment.gateway.domain.model.PaymentStatus;
import com.payment.gateway.domain.port.outbound.EventRepositoryPort;
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
    private final EventRepositoryPort eventRepositoryPort;
    private final PaymentEventProducer paymentEventProducer;

    public Mono<Payment> handleWebhookNotification(String eventId, String paymentId, String eventType, PaymentStatus newStatus) {
        log.info("Procesando webhook - Event ID: {}, Payment ID: {}, Event Type: {}, Nuevo Estado: {}", eventId, paymentId, eventType, newStatus);

        return eventRepositoryPort.existsByEventId(eventId)
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("El evento ID {} ya fue procesado anteriormente. Notificación duplicada ignorada.", eventId);
                        return paymentRepositoryPort.findById(paymentId)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Pago no encontrado: " + paymentId)));
                    }

                    return paymentRepositoryPort.findById(paymentId)
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Pago no encontrado: " + paymentId)))
                            .flatMap(payment -> {
                                if (newStatus == PaymentStatus.APPROVED) {
                                    payment.approve();
                                } else if (newStatus == PaymentStatus.REJECTED) {
                                    payment.reject();
                                } else {
                                    payment.updateStatus(newStatus);
                                }
                                return paymentRepositoryPort.save(payment);
                            })
                            .flatMap(result -> 
                                eventRepositoryPort.saveProcessedEvent(eventId, result.getId(), eventType, result.getStatus().name())
                                    .thenReturn(result)
                            )
                            .flatMap(result -> 
                                Mono.fromRunnable(() -> paymentEventProducer.sendPaymentEvent(result.getId(), result.getStatus().name()))
                                    .subscribeOn(Schedulers.boundedElastic())
                                    .thenReturn(result)
                            );
                });
    }
}