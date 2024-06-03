package it.agilelab.witboost.datafactory.service;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import io.vavr.control.Either;
import it.agilelab.witboost.datafactory.common.FailedOperation;
import it.agilelab.witboost.datafactory.common.Problem;
import it.agilelab.witboost.datafactory.config.PermissionServiceConfig;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);

    private final AzureResourceManager azureResourceManager;
    private final PermissionServiceConfig config;

    public PermissionServiceImpl(AzureResourceManager azureResourceManager, PermissionServiceConfig config) {
        this.azureResourceManager = azureResourceManager;
        this.config = config;
    }

    @Override
    public Either<FailedOperation, Void> assignOwnerPermissions(Set<String> objectIds, String scope) {
        try {
            objectIds.forEach(objectId -> {
                // contributor role assignment
                String contributorRoleAssignmentId = UUID.nameUUIDFromBytes(
                                (objectId + "contributor" + scope).getBytes())
                        .toString();
                azureResourceManager
                        .accessManagement()
                        .roleAssignments()
                        .define(contributorRoleAssignmentId)
                        .forObjectId(objectId)
                        .withBuiltInRole(BuiltInRole.CONTRIBUTOR)
                        .withScope(scope)
                        .create();

                // custom role assignment to allow testing linked services connections and previewing data on datasets
                String adfTestConnectionPreviewDataRoleDefinitionId = config.customRoleDefinitionId();
                String testConnectionPreviewDataRoleAssignmentId = UUID.nameUUIDFromBytes(
                                (objectId + adfTestConnectionPreviewDataRoleDefinitionId + scope).getBytes())
                        .toString();
                azureResourceManager
                        .accessManagement()
                        .roleAssignments()
                        .define(testConnectionPreviewDataRoleAssignmentId)
                        .forObjectId(objectId)
                        .withRoleDefinition(adfTestConnectionPreviewDataRoleDefinitionId)
                        .withScope(scope)
                        .create();
            });
            return right(null);
        } catch (ManagementException e) {
            String errorMessage = String.format(
                    "An error occurred while assigning owner permissions to %s on scope %s. Please try again and if the error persists contact the platform team. Details: %s",
                    objectIds, scope, e.getMessage());
            logger.error(errorMessage, e);
            return left(new FailedOperation(Collections.singletonList(new Problem(errorMessage, e))));
        }
    }

    @Override
    public Either<FailedOperation, Void> assignReaderPermissions(Set<String> objectIds, String scope) {
        try {
            objectIds.forEach(objectId -> {
                // reader role assignment
                String readerRoleAssignmentId = UUID.nameUUIDFromBytes((objectId + "reader" + scope).getBytes())
                        .toString();
                azureResourceManager
                        .accessManagement()
                        .roleAssignments()
                        .define(readerRoleAssignmentId)
                        .forObjectId(objectId)
                        .withBuiltInRole(BuiltInRole.READER)
                        .withScope(scope)
                        .create();
            });
            return right(null);
        } catch (ManagementException e) {
            String errorMessage = String.format(
                    "An error occurred while assigning reader permissions to %s on scope %s. Please try again and if the error persists contact the platform team. Details: %s",
                    objectIds, scope, e.getMessage());
            logger.error(errorMessage, e);
            return left(new FailedOperation(Collections.singletonList(new Problem(errorMessage, e))));
        }
    }
}
