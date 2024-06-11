package it.agilelab.witboost.datafactory.service;

import it.agilelab.witboost.datafactory.config.AzureGitConfig;
import java.io.File;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AzureGitCloneCommandService {

    private final Logger logger = LoggerFactory.getLogger(AzureGitCloneCommandService.class);

    private final AzureGitConfig azureGitConfig;

    public AzureGitCloneCommandService(AzureGitConfig azureGitConfig) {
        this.azureGitConfig = azureGitConfig;
    }

    /***
     * Build a CloneCommand object
     * @param repoURI the repository to clone
     * @param path the directory where the repository is cloned
     * @return the CloneCommand object
     */
    public CloneCommand build(String repoURI, File path) {
        return Git.cloneRepository()
                .setURI(repoURI)
                .setDirectory(path)
                .setCredentialsProvider(
                        new UsernamePasswordCredentialsProvider(azureGitConfig.username(), azureGitConfig.password()));
    }
}
