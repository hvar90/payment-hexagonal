package com.payment.gateway.infrastructure.adapter.inbound.messaging;

import com.payment.gateway.infrastructure.config.RabbitMQConfig; 
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentEventConsumer {

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_QUEUE)
    public void consumePaymentEvent(String message) {
        log.info("Mensaje recibido de RabbitMQ para procesamiento asíncrono: {}", message);
        // Aquí puedes agregar lógica adicional de negocio (ej. notificaciones, auditoría, conciliación)
    }
}