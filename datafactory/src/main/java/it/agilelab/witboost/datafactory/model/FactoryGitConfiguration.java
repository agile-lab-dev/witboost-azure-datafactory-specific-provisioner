package it.agilelab.witboost.datafactory.model;

public record FactoryGitConfiguration(
        String accountName,
        String projectName,
        String repositoryName,
        String collaborationBranch,
        String rootFolder,
        String lastCommitId,
        String tenantId,
        boolean disablePublish) {}
