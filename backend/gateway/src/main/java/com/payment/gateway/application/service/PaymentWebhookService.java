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

    public Mono<Payment> handleWebhookNotification(String eventId, String paymentId, PaymentStatus newStatus) {
        log.info("Procesando webhook - Event ID: {}, Payment ID: {}, Nuevo Estado: {}", eventId, paymentId, newStatus);

        // 1. Control de Idempotencia estricto por event_id
        return eventRepositoryPort.existsByEventId(eventId)
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("El evento ID {} ya fue procesado anteriormente. Notificación duplicada ignorada.", eventId);
                        // Retornamos el pago actual sin alterar nada ni volver a enviar eventos a RabbitMQ
                        return paymentRepositoryPort.findById(paymentId)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Pago no encontrado: " + paymentId)));
                    }

                    // 2. Si el evento es totalmente nuevo, ejecutamos la lógica de negocio
                    return paymentRepositoryPort.findById(paymentId)
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Pago no encontrado: " + paymentId)))
                            .flatMap(payment -> {
                                if (newStatus == PaymentStatus.APPROVED) {
                                    payment.approve();
                                } else if (newStatus == PaymentStatus.REJECTED) {
                                    payment.reject();
                                }
                                return paymentRepositoryPort.save(payment);
                            })
                            .flatMap(result -> 
                                // 3. Registrar el evento como procesado para bloquear futuros reintentos idénticos
                                eventRepositoryPort.saveProcessedEvent(eventId, result.getId(), result.getStatus().name())
                                    .thenReturn(result)
                            )
                            .flatMap(result -> 
                                // 4. Emitir evento a RabbitMQ de forma no bloqueante
                                Mono.fromRunnable(() -> paymentEventProducer.sendPaymentEvent(result.getId(), result.getStatus().name()))
                                    .subscribeOn(Schedulers.boundedElastic())
                                    .thenReturn(result)
                            );
                });
    }
}