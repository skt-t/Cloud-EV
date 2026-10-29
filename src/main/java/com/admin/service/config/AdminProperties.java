package com.admin.service.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbitmq")
public record AdminProperties(
        String managementUrl,
        String managementUser,
        String managementPassword,
        String dlx,
        List<String> protectedExchanges,
        List<String> protectedQueues) {
}
