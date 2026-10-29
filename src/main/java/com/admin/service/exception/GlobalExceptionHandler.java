package com.admin.service.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(f -> campos.put(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("error", "DATOS_INVALIDOS", "campos", campos));
    }

    @ExceptionHandler(RecursoProtegidoException.class)
    public ResponseEntity<Map<String, Object>> protegido(RecursoProtegidoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "RECURSO_PROTEGIDO", "mensaje", e.getMessage()));
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> noEncontrado(RecursoNoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "NO_ENCONTRADO", "mensaje", e.getMessage()));
    }

    @ExceptionHandler(AmqpConnectException.class)
    public ResponseEntity<Map<String, Object>> sinRabbit(AmqpConnectException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "RABBITMQ_NO_DISPONIBLE"));
    }

    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<Map<String, Object>> amqp(AmqpException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "CONFLICTO_RABBITMQ", "mensaje", String.valueOf(e.getMessage())));
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, Object>> gestion(RestClientException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "API_GESTION_NO_DISPONIBLE"));
    }
}
