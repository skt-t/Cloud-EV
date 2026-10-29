package com.admin.service.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class DtoValidationTest {

    private final Validator validator = Validation.byDefaultProvider().configure().buildValidatorFactory().getValidator();

    @Test
    void colaValidaNoTieneErrores() {
        assertTrue(validator.validate(new QueueRequest("mi.cola", true, true)).isEmpty());
    }

    @Test
    void colaConNombreVacioOInvalidoFalla() {
        assertEquals(1, validator.validate(new QueueRequest("mala cola!", true, false)).size());
        assertTrue(validator.validate(new QueueRequest("", true, false)).size() >= 1);
    }

    @Test
    void exchangeConTipoInvalidoFalla() {
        assertEquals(1, validator.validate(new ExchangeRequest("ex.demo", "raro", true)).size());
    }

    @Test
    void exchangeValidoNoTieneErrores() {
        assertTrue(validator.validate(new ExchangeRequest("ex.demo", "topic", true)).isEmpty());
    }

    @Test
    void bindingSinRoutingKeyFalla() {
        assertEquals(1, validator.validate(new BindingRequest("ex", "cola", null)).size());
    }
}
