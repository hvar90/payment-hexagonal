package com.payment.gateway.infrastructure.adapter.inbound.rest.exception;

import com.payment.gateway.domain.exception.InvalidPaymentStateException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja las excepciones de reglas de negocio relacionadas con el estado del pago.
     * Retorna un código HTTP 422 (Unprocessable Entity) o 400 según prefieras.
     */
    @ExceptionHandler(InvalidPaymentStateException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public Mono<ErrorResponseDTO> handleInvalidPaymentStateException(InvalidPaymentStateException ex) {
        log.warn("Violación de regla de negocio en el estado del pago: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                "INVALID_PAYMENT_STATE",
                ex.getMessage(),
                LocalDateTime.now()
        );
        
        return Mono.just(errorResponse);
    }

    /**
     * Manejo genérico para cualquier otra excepción no controlada en la API.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ErrorResponseDTO> handleGenericException(Exception ex) {
        log.error("Error interno no controlado en el servidor: {}", ex.getMessage(), ex);
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "Ocurrió un error inesperado al procesar la solicitud.",
                LocalDateTime.now()
        );
        
        return Mono.just(errorResponse);
    }
}

/**
 * DTO estándar para estructurar las respuestas de error de la API.
 */
@Getter
@AllArgsConstructor
class ErrorResponseDTO {
    private int status;
    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
}