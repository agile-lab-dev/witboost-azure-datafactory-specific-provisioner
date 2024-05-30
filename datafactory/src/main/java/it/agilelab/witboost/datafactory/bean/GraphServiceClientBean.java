package it.agilelab.witboost.datafactory.bean;

import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import it.agilelab.witboost.datafactory.config.GraphServiceClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GraphServiceClientBean {

    @Bean
    public GraphServiceClient graphServiceClient(GraphServiceClientConfig config) {
        // The client credentials flow requires that you request the
        // /.default scope, and pre-configure your permissions on the
        // app registration in Azure. An administrator must grant consent
        // to those permissions beforehand.
        var scopes = new String[] {"https://graph.microsoft.com/.default"};

        var credential = new ClientSecretCredentialBuilder()
                .clientId(config.clientId())
                .tenantId(config.tenantId())
                .clientSecret(config.clientSecret())
                .build();
        return new GraphServiceClient(credential, scopes);
    }
}
