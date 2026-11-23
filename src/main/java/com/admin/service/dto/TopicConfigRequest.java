package com.admin.service.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.Map;

public record TopicConfigRequest(
        @NotEmpty(message = "Las configuraciones no pueden estar vacías")
        Map<String, String> configs
) {}
