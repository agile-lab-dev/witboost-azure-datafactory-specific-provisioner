package it.agilelab.witboost.datafactory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "misc")
public record MiscConfig(String developmentEnvironmentName) {}
