package it.agilelab.witboost.datafactory.service;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import it.agilelab.witboost.datafactory.common.FailedOperation;
import it.agilelab.witboost.datafactory.common.Problem;
import it.agilelab.witboost.datafactory.config.AzureGitConfig;
import it.agilelab.witboost.datafactory.config.MiscConfig;
import it.agilelab.witboost.datafactory.model.*;
import it.agilelab.witboost.datafactory.openapi.model.ProvisioningRequest;
import it.agilelab.witboost.datafactory.service.provision.ProvisionServiceImpl;
import it.agilelab.witboost.datafactory.service.validation.ValidationService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.eclipse.jgit.api.CloneCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProvisionServiceTest {

    @Mock
    private ValidationService validationService;

    @Mock
    private PrincipalMappingService principalMappingService;

    @Mock
    private AzureGitCloneCommandService azureGitCloneCommandService;

    @Mock
    private GitRepositoryService gitRepositoryService;

    @Mock
    private DataFactoryClient dataFactoryClient;

    @Mock
    private PermissionService permissionService;

    @Mock
    private ADFToolsWrapperService adfToolsWrapperService;

    @Mock
    private AzureGitConfig azureGitConfig;

    @Mock
    private MiscConfig miscConfig;

    @InjectMocks
    private ProvisionServiceImpl provisionService;

    @Mock
    private CloneCommand cloneCommand;

    private final ProvisioningRequest provisioningRequest = new ProvisioningRequest();

    private final WorkloadSpecific specific;
    private final Workload<WorkloadSpecific> workload;
    private final String expectedDesc = "Error";
    private final FailedOperation failedOperation = new FailedOperation(List.of(new Problem(expectedDesc)));

    public ProvisionServiceTest() {
        specific = new WorkloadSpecific();
        workload = new Workload<>();
        workload.setKind("workload");
        workload.setSpecific(specific);
    }

    @Test
    public void testProvisionDevOk() {
        DataProduct dp = getDP("development");
        var provisionRequest = new ProvisionRequest<>(dp, workload, false);
        String adfName = "mydomain-dpname-0-developmen-1e25";
        String adfInstanceId = "instanceId";
        String adfUrl = "https://adf.azure.com/en/home?factory=instanceId";
        String userId = UUID.randomUUID().toString();
        String groupId = UUID.randomUUID().toString();
        when(validationService.validate(provisioningRequest)).thenReturn(right(provisionRequest));
        when(miscConfig.developmentEnvironmentName()).thenReturn("development");
        when(azureGitCloneCommandService.build(eq(specific.getGitRepo()), any()))
                .thenReturn(cloneCommand);
        when(gitRepositoryService.clone(cloneCommand)).thenReturn(right(null));
        when(dataFactoryClient.createADF(specific.getResourceGroup(), specific.getRegion(), adfName))
                .thenReturn(right(adfInstanceId));
        when(dataFactoryClient.linkGitRepository(
                        eq(specific.getResourceGroup()), eq(specific.getRegion()), eq(adfName), any()))
                .thenReturn(right(null));
        when(principalMappingService.map(Set.of("user:name.surname_email.com", "group:group1")))
                .thenReturn(Map.of("user:name.surname_email.com", right(userId), "group:group1", right(groupId)));
        when(permissionService.assignOwnerPermissions(Set.of(userId, groupId), adfInstanceId))
                .thenReturn(right(null));
        when(adfToolsWrapperService.publish(
                        anyString(),
                        eq(specific.getResourceGroup()),
                        eq(adfName),
                        eq(specific.getRegion()),
                        eq(provisionRequest.dataProduct().getEnvironment()),
                        eq(true)))
                .thenReturn(right(null));
        var expectedAdfInfo = new ADFInfo(adfName, adfInstanceId, adfUrl);

        var actualRes = provisionService.provision(provisioningRequest);

        assertTrue(actualRes.isRight());
        assertEquals(expectedAdfInfo, actualRes.get());
    }

    @Test
    public void testProvisionNonDevOk() {
        DataProduct dp = getDP("prod");
        var provisionRequest = new ProvisionRequest<>(dp, workload, false);
        String adfName = "mydomain-dpname-0-prod-8a5e";
        String adfInstanceId = "instanceId";
        String adfUrl = "https://adf.azure.com/en/home?factory=instanceId";
        String userId = UUID.randomUUID().toString();
        String groupId = UUID.randomUUID().toString();
        when(validationService.validate(provisioningRequest)).thenReturn(right(provisionRequest));
        when(miscConfig.developmentEnvironmentName()).thenReturn("development");
        when(azureGitCloneCommandService.build(eq(specific.getGitRepo()), any()))
                .thenReturn(cloneCommand);
        when(gitRepositoryService.clone(cloneCommand)).thenReturn(right(null));
        when(dataFactoryClient.createADF(specific.getResourceGroup(), specific.getRegion(), adfName))
                .thenReturn(right(adfInstanceId));
        when(principalMappingService.map(Set.of("user:name.surname_email.com", "group:group1")))
                .thenReturn(Map.of("user:name.surname_email.com", right(userId), "group:group1", right(groupId)));
        when(permissionService.assignReaderPermissions(Set.of(userId, groupId), adfInstanceId))
                .thenReturn(right(null));
        when(adfToolsWrapperService.publish(
                        anyString(),
                        eq(specific.getResourceGroup()),
                        eq(adfName),
                        eq(specific.getRegion()),
                        eq(provisionRequest.dataProduct().getEnvironment()),
                        eq(false)))
                .thenReturn(right(null));
        var expectedAdfInfo = new ADFInfo(adfName, adfInstanceId, adfUrl);

        var actualRes = provisionService.provision(provisioningRequest);

        assertTrue(actualRes.isRight());
        assertEquals(expectedAdfInfo, actualRes.get());
    }

    @Test
    public void testUnprovisionOk() {
        DataProduct dp = getDP("development");
        var provisionRequest = new ProvisionRequest<>(dp, workload, false);
        String adfName = "mydomain-dpname-0-developmen-1e25";
        when(validationService.validate(provisioningRequest)).thenReturn(right(provisionRequest));
        when(dataFactoryClient.deleteADF(specific.getResourceGroup(), adfName)).thenReturn(right(null));

        var actualRes = provisionService.unprovision(provisioningRequest);

        assertTrue(actualRes.isRight());
    }

    @Test
    public void testProvisionValidationKo() {
        when(validationService.validate(provisioningRequest)).thenReturn(left(failedOperation));

        var actualRes = provisionService.provision(provisioningRequest);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isEmpty());
        });
    }

    @Test
    public void testUnprovisionValidationKo() {
        when(validationService.validate(provisioningRequest)).thenReturn(left(failedOperation));

        var actualRes = provisionService.unprovision(provisioningRequest);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isEmpty());
        });
    }

    @Test
    public void testUnprovisionDeleteKo() {
        DataProduct dp = getDP("development");
        var provisionRequest = new ProvisionRequest<>(dp, workload, false);
        String adfName = "mydomain-dpname-0-developmen-1e25";
        when(validationService.validate(provisioningRequest)).thenReturn(right(provisionRequest));
        when(dataFactoryClient.deleteADF(specific.getResourceGroup(), adfName)).thenReturn(left(failedOperation));

        var actualRes = provisionService.unprovision(provisioningRequest);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isEmpty());
        });
    }

    @Test
    public void testProvisionMapOwnerPartialFail() {
        DataProduct dp = getDP("development");
        var provisionRequest = new ProvisionRequest<>(dp, workload, false);
        String adfName = "mydomain-dpname-0-developmen-1e25";
        String adfInstanceId = "instanceId";
        String userId = UUID.randomUUID().toString();
        when(validationService.validate(provisioningRequest)).thenReturn(right(provisionRequest));
        when(miscConfig.developmentEnvironmentName()).thenReturn("development");
        when(azureGitCloneCommandService.build(eq(specific.getGitRepo()), any()))
                .thenReturn(cloneCommand);
        when(gitRepositoryService.clone(cloneCommand)).thenReturn(right(null));
        when(dataFactoryClient.createADF(specific.getResourceGroup(), specific.getRegion(), adfName))
                .thenReturn(right(adfInstanceId));
        when(dataFactoryClient.linkGitRepository(
                        eq(specific.getResourceGroup()), eq(specific.getRegion()), eq(adfName), any()))
                .thenReturn(right(null));
        when(principalMappingService.map(Set.of("user:name.surname_email.com", "group:group1")))
                .thenReturn(
                        Map.of("user:name.surname_email.com", right(userId), "group:group1", left(failedOperation)));

        var actualRes = provisionService.provision(provisioningRequest);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isEmpty());
        });
    }

    private DataProduct getDP(String env) {
        DataProduct dp = new DataProduct();
        dp.setName("dp name");
        dp.setVersion("0");
        dp.setDomain("mydomain");
        dp.setEnvironment(env);
        dp.setDataProductOwner("user:name.surname_email.com");
        dp.setDevGroup("group:group1");
        return dp;
    }
}
