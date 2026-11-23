package com.admin.service.dto;

import jakarta.validation.constraints.Min;

public record PartitionsRequest(
        @Min(value = 1, message = "El conteo de particiones debe ser mayor a 0")
        int totalPartitions
) {}
