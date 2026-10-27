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
        log.info("Iniciando procesamiento para la referencia de comercio: {}", payment.getReference());

        // 1. Validar si ya existe un pago previo con la misma reference del comercio
        return paymentRepositoryPort.findByReference(payment.getReference())
                .flatMap(existingPayment -> {
                    log.warn("Intento de pago duplicado detectado para la referencia: {}. Retornando pago existente.", payment.getReference());
                    return Mono.just(existingPayment);
                })
                .switchIfEmpty(
                    // 2. Si no existe, se procede con el flujo normal de creación y cobro
                    paymentRepositoryPort.save(payment)
                        .flatMap(savedPayment -> externalPaymentGatewayPort.charge(savedPayment))
                        .flatMap(processedPayment -> paymentRepositoryPort.save(processedPayment))
                        .flatMap(result -> 
                            Mono.fromRunnable(() -> paymentEventProducer.sendPaymentEvent(result.getId(), result.getStatus().name()))
                                .subscribeOn(Schedulers.boundedElastic())
                                .thenReturn(result)
                        )
                );
    }
}