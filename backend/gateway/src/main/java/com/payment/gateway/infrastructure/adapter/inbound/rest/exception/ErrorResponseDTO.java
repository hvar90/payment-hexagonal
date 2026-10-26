package com.payment.gateway.infrastructure.adapter.inbound.rest.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ErrorResponseDTO {
    private int status;
    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
}