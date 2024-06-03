package it.agilelab.witboost.datafactory.bean;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureResourceManagerBean {

    @Bean
    public AzureResourceManager azureResourceManager() {
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        return AzureResourceManager.authenticate(credential, profile).withDefaultSubscription();
    }
}
