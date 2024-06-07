package it.agilelab.witboost.datafactory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "powershell")
public record PowerShellBeanConfig(int waitPause, long maxWait, String tempFolder) {}
