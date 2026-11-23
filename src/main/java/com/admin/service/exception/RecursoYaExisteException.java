package com.admin.service.exception;

/** Se lanza cuando se intenta crear un recurso (por ejemplo un tópico) que ya existe. */
public class RecursoYaExisteException extends RuntimeException {

    public RecursoYaExisteException(String message) {
        super(message);
    }
}
