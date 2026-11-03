package com.trans.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Respuesta de GET /api/contactos/{id} de back-contacto (mismos nombres de campo que su entidad Contacto). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ContactoResponseDto(
        Long idContacto,
        Long idUsuario,
        String nombreContacto,
        String gmail,
        String rut,
        Boolean cuentaValidada) {
}
