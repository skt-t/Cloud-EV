package com.admin.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record TopicRequest(
        @NotBlank(message = "El nombre del tópico es obligatorio")
        String name,
        @Min(value = 1, message = "Debe tener al menos 1 partición")
        Integer partitions,
        @Min(value = 1, message = "Debe tener al menos 1 réplica")
        Short replicas,
        Map<String, String> configs
) {}
