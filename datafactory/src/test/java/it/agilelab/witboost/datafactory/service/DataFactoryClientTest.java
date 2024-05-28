package it.agilelab.witboost.datafactory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.datafactory.DataFactoryManager;
import com.azure.resourcemanager.datafactory.models.Factories;
import com.azure.resourcemanager.datafactory.models.Factory;
import com.azure.resourcemanager.datafactory.models.FactoryRepoConfiguration;
import it.agilelab.witboost.datafactory.model.FactoryGitConfiguration;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class DataFactoryClientTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataFactoryManager manager;

    @InjectMocks
    private DataFactoryClientImpl dataFactoryClient;

    private final String resourceGroup = "a-resourceGroup";
    private final String region = "a-region";
    private final String name = "a-name";
    private final String factoryId = "a-factoryId";
    private final FactoryGitConfiguration config = new FactoryGitConfiguration("", "", "", "", "", "", "", false);
    private final ManagementException ex = new ManagementException("Error", null);

    private PagedIterable<Factory> emptyFactories() {
        return new PagedIterable<>(new PagedFlux<>(Mono::empty, continuationToken -> Mono.empty()));
    }

    private PagedIterable<Factory> singleFactories(Factory factory) {
        HttpHeaders headers = new HttpHeaders();
        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost");
        final Function<String, PagedResponse<Factory>> pagedResponseSupplier = continuationToken ->
                new PagedResponseBase<>(request, 200, headers, List.of(factory), continuationToken, null);
        return new PagedIterable<>(pageSize -> pagedResponseSupplier.apply(null));
    }

    @Test
    public void testCreateADFNotAlreadyExisting() {
        var mockedFactory = mock(Factory.class);
        when(manager.factories().listByResourceGroup(resourceGroup)).thenReturn(emptyFactories());
        when(manager.factories()
                        .define(name)
                        .withRegion(region)
                        .withExistingResourceGroup(resourceGroup)
                        .create())
                .thenReturn(mockedFactory);
        when(mockedFactory.id()).thenReturn(factoryId);

        var actualRes = dataFactoryClient.createADF(resourceGroup, region, name);

        assertTrue(actualRes.isRight());
        assertEquals(factoryId, actualRes.get());
    }

    @Test
    public void testCreateADFAlreadyExisting() {
        var mockedFactory = mock(Factory.class);
        when(manager.factories().listByResourceGroup(resourceGroup)).thenReturn(singleFactories(mockedFactory));
        when(mockedFactory.name()).thenReturn(name);
        when(mockedFactory.id()).thenReturn(factoryId);

        var actualRes = dataFactoryClient.createADF(resourceGroup, region, name);

        verify(
                        manager.factories()
                                .define(anyString())
                                .withRegion(anyString())
                                .withExistingResourceGroup(anyString()),
                        never())
                .create();
        assertTrue(actualRes.isRight());
        assertEquals(factoryId, actualRes.get());
    }

    @Test
    public void testCreateADFReturnError() {
        String expectedDesc =
                "An error occurred while creating the ADF 'a-name' on resource group a-resourceGroup. Please try again and if the error persists contact the platform team. Details: Error";
        when(manager.factories().listByResourceGroup(resourceGroup)).thenReturn(emptyFactories());
        when(manager.factories()
                        .define(name)
                        .withRegion(region)
                        .withExistingResourceGroup(resourceGroup)
                        .create())
                .thenThrow(ex);

        var actualRes = dataFactoryClient.createADF(resourceGroup, region, name);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(p.description(), expectedDesc);
            assertTrue(p.cause().isPresent());
            assertEquals(ex, p.cause().get());
        });
    }

    @Test
    public void testDeleteADFExisting() {
        var mockedFactory = mock(Factory.class);
        var mockedFactories = mock(Factories.class);
        when(manager.factories()).thenReturn(mockedFactories);
        when(mockedFactories.listByResourceGroup(resourceGroup)).thenReturn(singleFactories(mockedFactory));
        when(mockedFactory.id()).thenReturn(factoryId);
        when(mockedFactory.name()).thenReturn(name);
        doNothing().when(mockedFactories).deleteById(factoryId);

        var actualRes = dataFactoryClient.deleteADF(resourceGroup, name);

        assertTrue(actualRes.isRight());
    }

    @Test
    public void testDeleteADFNotExisting() {
        var mockedFactories = mock(Factories.class);
        when(manager.factories()).thenReturn(mockedFactories);
        when(mockedFactories.listByResourceGroup(resourceGroup)).thenReturn(emptyFactories());

        var actualRes = dataFactoryClient.deleteADF(resourceGroup, name);

        verify(mockedFactories, never()).deleteById(factoryId);
        assertTrue(actualRes.isRight());
    }

    @Test
    public void testDeleteADFReturnError() {
        var mockedFactory = mock(Factory.class);
        var mockedFactories = mock(Factories.class);
        when(manager.factories()).thenReturn(mockedFactories);
        when(mockedFactories.listByResourceGroup(resourceGroup)).thenReturn(singleFactories(mockedFactory));
        when(mockedFactory.id()).thenReturn(factoryId);
        when(mockedFactory.name()).thenReturn(name);
        String expectedDesc =
                "An error occurred while deleting the ADF 'a-name' on resource group a-resourceGroup. Please try again and if the error persists contact the platform team. Details: Error";
        doThrow(ex).when(mockedFactories).deleteById(factoryId);

        var actualRes = dataFactoryClient.deleteADF(resourceGroup, name);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(p.description(), expectedDesc);
            assertTrue(p.cause().isPresent());
            assertEquals(ex, p.cause().get());
        });
    }

    @Test
    public void testLinkGitRepositoryWhenADFNotExists() {
        var mockedFactories = mock(Factories.class);
        when(manager.factories()).thenReturn(mockedFactories);
        when(mockedFactories.listByResourceGroup(resourceGroup)).thenReturn(emptyFactories());
        String expectedDesc =
                "Cannot link the Git repository to ADF: unable to find ADF instance named 'a-name' on resource group a-resourceGroup";

        var actualRes = dataFactoryClient.linkGitRepository(resourceGroup, region, name, null);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isEmpty());
        });
    }

    @Test
    public void testLinkGitRepositoryNotAlreadyLinked() {
        var mockedFactory = mock(Factory.class);
        var mockedFactories = mock(Factories.class);
        when(manager.factories()).thenReturn(mockedFactories);
        when(mockedFactories.listByResourceGroup(resourceGroup)).thenReturn(singleFactories(mockedFactory));
        when(mockedFactory.repoConfiguration()).thenReturn(null);
        when(mockedFactory.name()).thenReturn(name);
        when(mockedFactory.id()).thenReturn(factoryId);
        when(mockedFactories.configureFactoryRepo(eq(region), any())).thenReturn(mockedFactory);

        var actualRes = dataFactoryClient.linkGitRepository(resourceGroup, region, name, config);

        assertTrue(actualRes.isRight());
    }

    @Test
    public void testLinkGitRepositoryAlreadyLinked() {
        var mockedFactory = mock(Factory.class);
        var mockedFactories = mock(Factories.class);
        var mockedFactoryRepoConfiguration = mock(FactoryRepoConfiguration.class);
        when(manager.factories()).thenReturn(mockedFactories);
        when(mockedFactories.listByResourceGroup(resourceGroup)).thenReturn(singleFactories(mockedFactory));
        when(mockedFactory.repoConfiguration()).thenReturn(mockedFactoryRepoConfiguration);
        when(mockedFactory.name()).thenReturn(name);

        var actualRes = dataFactoryClient.linkGitRepository(resourceGroup, region, name, config);

        verify(mockedFactories, never()).configureFactoryRepo(eq(region), any());
        assertTrue(actualRes.isRight());
    }

    @Test
    public void testLinkGitRepositoryNotAlreadyLinkedReturnError() {
        var mockedFactory = mock(Factory.class);
        var mockedFactories = mock(Factories.class);
        when(manager.factories()).thenReturn(mockedFactories);
        when(mockedFactories.listByResourceGroup(resourceGroup)).thenReturn(singleFactories(mockedFactory));
        when(mockedFactory.repoConfiguration()).thenReturn(null);
        when(mockedFactory.name()).thenReturn(name);
        when(mockedFactory.id()).thenReturn(factoryId);
        String expectedDesc =
                "An error occurred while linking the Git repository to ADF 'a-name' on resource group a-resourceGroup. Please try again and if the error persists contact the platform team. Details: Error";
        when(mockedFactories.configureFactoryRepo(eq(region), any())).thenThrow(ex);

        var actualRes = dataFactoryClient.linkGitRepository(resourceGroup, region, name, config);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(p.description(), expectedDesc);
            assertTrue(p.cause().isPresent());
            assertEquals(ex, p.cause().get());
        });
    }
}
