package it.agilelab.witboost.datafactory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import it.agilelab.witboost.datafactory.config.PermissionServiceConfig;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PermissionServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AzureResourceManager azureResourceManager;

    @Mock
    private PermissionServiceConfig config;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private final RoleAssignment mockedRoleAssignment = mock(RoleAssignment.class);
    private final Set<String> objectIds =
            Set.of(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    private final String scope = "scope";
    private final ManagementException ex = new ManagementException("Unexpected error", null);

    @Test
    public void testassignOwnerPermissionsOk() {
        var contributorsIds = objectIds.stream()
                .map(o -> UUID.nameUUIDFromBytes((o + "contributor" + scope).getBytes())
                        .toString())
                .collect(Collectors.toSet());
        when(azureResourceManager
                        .accessManagement()
                        .roleAssignments()
                        .define(argThat(contributorsIds::contains))
                        .forObjectId(argThat(objectIds::contains))
                        .withBuiltInRole(BuiltInRole.CONTRIBUTOR)
                        .withScope(scope)
                        .create())
                .thenReturn(mockedRoleAssignment);
        String customRoleDefinitionId = "customRoleDefinitionId";
        var customRoleIds = objectIds.stream()
                .map(o -> UUID.nameUUIDFromBytes((o + customRoleDefinitionId + scope).getBytes())
                        .toString())
                .collect(Collectors.toSet());
        when(config.customRoleDefinitionId()).thenReturn(customRoleDefinitionId);
        when(azureResourceManager
                        .accessManagement()
                        .roleAssignments()
                        .define(argThat(customRoleIds::contains))
                        .forObjectId(argThat(objectIds::contains))
                        .withRoleDefinition(customRoleDefinitionId)
                        .withScope(scope)
                        .create())
                .thenReturn(mockedRoleAssignment);

        var actualRes = permissionService.assignOwnerPermissions(objectIds, scope);

        assertTrue(actualRes.isRight());
    }

    @Test
    public void testassignOwnerPermissionsError() {
        when(azureResourceManager
                        .accessManagement()
                        .roleAssignments()
                        .define(anyString())
                        .forObjectId(argThat(objectIds::contains))
                        .withBuiltInRole(BuiltInRole.CONTRIBUTOR)
                        .withScope(scope)
                        .create())
                .thenThrow(ex);
        String expectedDesc = String.format(
                "An error occurred while assigning owner permissions to %s on scope %s. Please try again and if the error persists contact the platform team. Details: Unexpected error",
                objectIds, scope);

        var actualRes = permissionService.assignOwnerPermissions(objectIds, scope);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isPresent());
            assertEquals(ex, p.cause().get());
        });
    }

    @Test
    public void testassignReaderPermissionsOk() {
        when(azureResourceManager
                        .accessManagement()
                        .roleAssignments()
                        .define(anyString())
                        .forObjectId(argThat(objectIds::contains))
                        .withBuiltInRole(BuiltInRole.READER)
                        .withScope(scope)
                        .create())
                .thenReturn(mockedRoleAssignment);

        var actualRes = permissionService.assignReaderPermissions(objectIds, scope);

        assertTrue(actualRes.isRight());
    }

    @Test
    public void testassignReaderPermissionsError() {
        when(azureResourceManager
                        .accessManagement()
                        .roleAssignments()
                        .define(anyString())
                        .forObjectId(argThat(objectIds::contains))
                        .withBuiltInRole(BuiltInRole.READER)
                        .withScope(scope)
                        .create())
                .thenThrow(ex);
        String expectedDesc = String.format(
                "An error occurred while assigning reader permissions to %s on scope %s. Please try again and if the error persists contact the platform team. Details: Unexpected error",
                objectIds, scope);

        var actualRes = permissionService.assignReaderPermissions(objectIds, scope);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isPresent());
            assertEquals(ex, p.cause().get());
        });
    }
}
