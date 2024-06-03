package it.agilelab.witboost.datafactory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "permission")
public record PermissionServiceConfig(String customRoleDefinitionId) {}
