package com.admin.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record QueueRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Pattern(regexp = "^[a-zA-Z0-9._-]{1,100}$",
                message = "Solo letras, números, punto, guion y guion bajo (máx. 100)")
        String name,
        boolean durable,
        boolean withDlq) {
}
