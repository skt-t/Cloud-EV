package com.admin.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BindingRequest(
        @NotBlank(message = "El exchange es obligatorio") String exchange,
        @NotBlank(message = "La cola es obligatoria") String queue,
        @NotNull(message = "La routing key es obligatoria (puede ir vacía en fanout)") String routingKey) {
}
