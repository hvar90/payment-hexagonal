package com.payment.gateway.infrastructure.adapter.outbound.messaging;

import com.payment.gateway.infrastructure.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendPaymentEvent(String paymentId, String status) {
        String message = String.format("{\"paymentId\":\"%s\", \"status\":\"%s\"}", paymentId, status);
        log.info("Enviando evento de pago a RabbitMQ: {}", message);
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PAYMENT_EXCHANGE,
                RabbitMQConfig.PAYMENT_ROUTING_KEY,
                message
        );
    }
}