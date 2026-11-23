package com.admin.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record TopicRequest(
        @NotBlank(message = "El nombre del tópico es obligatorio")
        String name,
        @NotNull(message = "El número de particiones es obligatorio")
        @Min(value = 1, message = "Debe tener al menos 1 partición")
        Integer partitions,
        @NotNull(message = "El factor de replicación es obligatorio")
        @Min(value = 1, message = "Debe tener al menos 1 réplica")
        Short replicas,
        Map<String, String> configs
) {}
