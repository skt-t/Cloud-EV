package com.trans.service.dto;

public record ContactoResponseDto(
    Long id,
    Long idUsuario,
    String nombre,
    String rut,
    String banco,
    String numeroCuenta
) {}