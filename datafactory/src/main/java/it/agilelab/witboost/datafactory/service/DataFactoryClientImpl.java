package it.agilelab.witboost.datafactory.service;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.datafactory.DataFactoryManager;
import com.azure.resourcemanager.datafactory.models.FactoryRepoUpdate;
import com.azure.resourcemanager.datafactory.models.FactoryVstsConfiguration;
import io.vavr.control.Either;
import it.agilelab.witboost.datafactory.common.FailedOperation;
import it.agilelab.witboost.datafactory.common.Problem;
import it.agilelab.witboost.datafactory.model.FactoryGitConfiguration;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DataFactoryClientImpl implements DataFactoryClient {

    private final Logger logger = LoggerFactory.getLogger(DataFactoryClientImpl.class);

    private final DataFactoryManager manager;

    public DataFactoryClientImpl(DataFactoryManager manager) {
        this.manager = manager;
    }

    @Override
    public Either<FailedOperation, String> createADF(String resourceGroup, String region, String name) {
        try {
            var factories = manager.factories().listByResourceGroup(resourceGroup);
            var optExistingFactory =
                    factories.stream().filter(f -> f.name().equals(name)).findFirst();
            var factory = optExistingFactory.orElseGet(() -> manager.factories()
                    .define(name)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup)
                    .create());
            return right(factory.id());
        } catch (ManagementException e) {
            String errorMessage = String.format(
                    "An error occurred while creating the ADF '%s' on resource group %s. Please try again and if the error persists contact the platform team. Details: %s",
                    name, resourceGroup, e.getMessage());
            logger.error(errorMessage, e);
            return left(new FailedOperation(Collections.singletonList(new Problem(errorMessage, e))));
        }
    }

    @Override
    public Either<FailedOperation, Void> deleteADF(String resourceGroup, String name) {
        try {
            var factories = manager.factories().listByResourceGroup(resourceGroup);
            var optExistingFactory =
                    factories.stream().filter(f -> f.name().equals(name)).findFirst();
            optExistingFactory.ifPresent(factory -> manager.factories().deleteById(factory.id()));
            return right(null);
        } catch (ManagementException e) {
            String errorMessage = String.format(
                    "An error occurred while deleting the ADF '%s' on resource group %s. Please try again and if the error persists contact the platform team. Details: %s",
                    name, resourceGroup, e.getMessage());
            logger.error(errorMessage, e);
            return left(new FailedOperation(Collections.singletonList(new Problem(errorMessage, e))));
        }
    }

    @Override
    public Either<FailedOperation, Void> linkGitRepository(
            String resourceGroup, String region, String name, FactoryGitConfiguration factoryGitConfiguration) {
        try {
            var factories = manager.factories().listByResourceGroup(resourceGroup);
            var optExistingFactory =
                    factories.stream().filter(f -> f.name().equals(name)).findFirst();
            if (optExistingFactory.isEmpty()) {
                String errorMessage = String.format(
                        "Cannot link the Git repository to ADF: unable to find ADF instance named '%s' on resource group %s",
                        name, resourceGroup);
                logger.error(errorMessage);
                return left(new FailedOperation(Collections.singletonList(new Problem(errorMessage))));
            }
            var factory = optExistingFactory.get();
            if (factory.repoConfiguration() == null) {
                manager.factories()
                        .configureFactoryRepo(
                                region,
                                new FactoryRepoUpdate()
                                        .withFactoryResourceId(factory.id())
                                        .withRepoConfiguration(new FactoryVstsConfiguration()
                                                .withAccountName(factoryGitConfiguration.accountName())
                                                .withProjectName(factoryGitConfiguration.projectName())
                                                .withRepositoryName(factoryGitConfiguration.repositoryName())
                                                .withCollaborationBranch(factoryGitConfiguration.collaborationBranch())
                                                .withRootFolder(factoryGitConfiguration.rootFolder())
                                                .withLastCommitId(factoryGitConfiguration.lastCommitId())
                                                .withTenantId(factoryGitConfiguration.tenantId())
                                                .withDisablePublish(factoryGitConfiguration.disablePublish())));
            }
            return right(null);
        } catch (ManagementException e) {
            String errorMessage = String.format(
                    "An error occurred while linking the Git repository to ADF '%s' on resource group %s. Please try again and if the error persists contact the platform team. Details: %s",
                    name, resourceGroup, e.getMessage());
            logger.error(errorMessage, e);
            return left(new FailedOperation(Collections.singletonList(new Problem(errorMessage, e))));
        }
    }
}
