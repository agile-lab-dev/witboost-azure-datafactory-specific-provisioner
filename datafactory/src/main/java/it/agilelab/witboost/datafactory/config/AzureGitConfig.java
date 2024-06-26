package it.agilelab.witboost.datafactory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "git.azure-dev-ops")
public record AzureGitConfig(
        String username,
        String password,
        String accountName,
        String collaborationBranch,
        String rootFolder,
        String lastCommitId,
        String tenantId,
        boolean disablePublish) {}
