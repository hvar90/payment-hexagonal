package com.payment.gateway.infrastructure.adapter.inbound.rest;

import com.payment.gateway.application.service.PaymentProcessService;
import com.payment.gateway.domain.model.Payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; 

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.payment.gateway.infrastructure.adapter.inbound.rest.dto.PaymentRequestDTO;

import reactor.core.publisher.Mono;

@Slf4j 
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentProcessService paymentProcessService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Payment> createPayment(@RequestBody PaymentRequestDTO request) {
        log.info("Recibida solicitud HTTP POST /api/v1/payments para crear pago ID: {} con monto: {} {}", 
                request.getId(), request.getAmount(), request.getCurrency());

        Payment payment = new Payment(
                request.getId(),
                request.getReference(),
                request.getAmount(),
                request.getCurrency(),
                request.getDescription()
        );
        
        return paymentProcessService.processPayment(payment)
                .doOnSuccess(saved -> log.info("Pago ID: {} procesado exitosamente vía REST", saved.getId()))
                .doOnError(error -> log.error("Error al procesar el pago ID: {}: {}", request.getId(), error.getMessage()));
    }
}

