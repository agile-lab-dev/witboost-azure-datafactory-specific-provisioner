package it.agilelab.witboost.datafactory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "graph")
public record GraphServiceClientConfig(String tenantId, String clientId, String clientSecret) {}
