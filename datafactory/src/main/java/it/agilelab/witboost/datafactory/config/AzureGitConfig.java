package it.agilelab.witboost.datafactory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "git.azure-dev-ops")
public record AzureGitConfig(String username, String password) {}
