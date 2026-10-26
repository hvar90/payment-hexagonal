package com.payment.gateway.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient paymentGatewayWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://api.external-payment-gateway.com/v1") // URL simulada o real de la pasarela
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}