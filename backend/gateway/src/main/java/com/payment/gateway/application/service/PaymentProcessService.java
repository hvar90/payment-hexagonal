package com.payment.gateway.application.service;

import com.payment.gateway.domain.model.Payment;
import com.payment.gateway.domain.port.inbound.ProcessPaymentUseCase;
import com.payment.gateway.domain.port.outbound.ExternalPaymentGatewayPort;
import com.payment.gateway.domain.port.outbound.PaymentRepositoryPort;
import com.payment.gateway.infrastructure.adapter.outbound.messaging.PaymentEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessService implements ProcessPaymentUseCase {

    private final ExternalPaymentGatewayPort externalPaymentGatewayPort;
    private final PaymentRepositoryPort paymentRepositoryPort;
    private final PaymentEventProducer paymentEventProducer;

    @Override
    public Mono<Payment> processPayment(Payment payment) {
        log.info("Iniciando procesamiento asíncrono para el pago ID: {}", payment.getId());

        // 1. Guardar inicialmente en estado PENDING
        return paymentRepositoryPort.save(payment)
                // 2. Invocar de forma no bloqueante a la pasarela externa
                .flatMap(savedPayment -> externalPaymentGatewayPort.charge(savedPayment))
                // 3. Actualizar el estado final resultante en la base de datos
                .flatMap(processedPayment -> paymentRepositoryPort.save(processedPayment))
                // 4. Emitir el evento a RabbitMQ de forma no bloqueante protegiendo el event loop
                .flatMap(result -> 
                    Mono.fromRunnable(() -> paymentEventProducer.sendPaymentEvent(result.getId(), result.getStatus().name()))
                        .subscribeOn(Schedulers.boundedElastic())
                        .thenReturn(result)
                )
                .doOnSuccess(result -> log.info("Pago procesado exitosamente con estado: {}", result.getStatus()))
                .doOnError(error -> log.error("Error crítico procesando el pago ID {}: {}", payment.getId(), error.getMessage()));
    }
}