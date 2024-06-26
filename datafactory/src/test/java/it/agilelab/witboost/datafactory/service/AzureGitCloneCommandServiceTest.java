package it.agilelab.witboost.datafactory.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import it.agilelab.witboost.datafactory.config.AzureGitConfig;
import java.io.File;
import org.junit.jupiter.api.Test;

public class AzureGitCloneCommandServiceTest {

    private final AzureGitConfig azureGitConfig = new AzureGitConfig("username", "password", "", "", "", "", "", true);

    private final AzureGitCloneCommandService service = new AzureGitCloneCommandService(azureGitConfig);

    @Test
    public void testBuild() {
        String repoURI = "https://my-repo";
        String folder = "/tmp/";

        var actualRes = service.build(repoURI, new File(folder));

        assertNotNull(actualRes);
    }
}
