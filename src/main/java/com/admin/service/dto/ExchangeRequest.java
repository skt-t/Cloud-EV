package com.admin.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ExchangeRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Pattern(regexp = "^[a-zA-Z0-9._-]{1,100}$",
                message = "Solo letras, números, punto, guion y guion bajo (máx. 100)")
        String name,
        @NotBlank(message = "El tipo es obligatorio")
        @Pattern(regexp = "^(topic|direct|fanout|headers)$",
                message = "El tipo debe ser topic, direct, fanout o headers")
        String type,
        boolean durable) {
}
