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

        // 1. Validar idempotencia del Webhook: verificamos si este eventId ya fue procesado antes
        return eventRepositoryPort.existsByEventId(eventId)
                .flatMap(exists -> {
                    // Si el evento ya existe, es una notificación duplicada de la pasarela
                    if (exists) {
                        log.warn("El evento ID {} ya fue procesado anteriormente. Notificación duplicada ignorada.", eventId);
                        // Retornamos el pago actual sin volver a modificar su estado
                        return paymentRepositoryPort.findById(paymentId)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Pago no encontrado: " + paymentId)));
                    }

                    // 2. Si el evento es nuevo, buscamos el pago asociado en la base de datos
                    return paymentRepositoryPort.findById(paymentId)
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Pago no encontrado: " + paymentId)))
                            .flatMap(payment -> {
                                // 3. Actualizamos el estado del pago de acuerdo con la notificación recibida
                                if (newStatus == PaymentStatus.APPROVED) {
                                    payment.approve();
                                } else if (newStatus == PaymentStatus.REJECTED) {
                                    payment.reject();
                                } else {
                                    payment.updateStatus(newStatus);
                                }
                                // Guardamos los cambios del pago en la base de datos
                                return paymentRepositoryPort.save(payment);
                            })
                            .flatMap(result -> 
                                // 4. Registramos el eventId en la tabla de eventos procesados para bloquear futuros duplicados
                                eventRepositoryPort.saveProcessedEvent(eventId, result.getId(), eventType, result.getStatus().name())
                                    .thenReturn(result)
                            )
                            .flatMap(result -> 
                                // 5. Publicamos el evento de cambio de estado a RabbitMQ de forma no bloqueante
                                Mono.fromRunnable(() -> paymentEventProducer.sendPaymentEvent(result.getId(), result.getStatus().name()))
                                    .subscribeOn(Schedulers.boundedElastic())
                                    .thenReturn(result)
                            );
                });
    }
}