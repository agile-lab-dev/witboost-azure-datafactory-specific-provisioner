package it.agilelab.witboost.datafactory.service.provision;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;
import static it.agilelab.witboost.datafactory.util.StringUtils.sanitize;

import io.vavr.control.Either;
import it.agilelab.witboost.datafactory.common.FailedOperation;
import it.agilelab.witboost.datafactory.common.Problem;
import it.agilelab.witboost.datafactory.config.AzureGitConfig;
import it.agilelab.witboost.datafactory.config.MiscConfig;
import it.agilelab.witboost.datafactory.model.*;
import it.agilelab.witboost.datafactory.openapi.model.ProvisioningRequest;
import it.agilelab.witboost.datafactory.service.*;
import it.agilelab.witboost.datafactory.service.validation.ValidationService;
import it.agilelab.witboost.datafactory.util.FileUtils;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

@Service
public class ProvisionServiceImpl implements ProvisionService {
    private static final Logger logger = LoggerFactory.getLogger(ProvisionServiceImpl.class);

    private final ValidationService validationService;
    private final PrincipalMappingService principalMappingService;
    private final AzureGitCloneCommandService azureGitCloneCommandService;
    private final GitRepositoryService gitRepositoryService;
    private final DataFactoryClient dataFactoryClient;
    private final PermissionService permissionService;
    private final ADFToolsWrapperService adfToolsWrapperService;
    private final AzureGitConfig azureGitConfig;
    private final MiscConfig miscConfig;

    public ProvisionServiceImpl(
            ValidationService validationService,
            PrincipalMappingService principalMappingService,
            AzureGitCloneCommandService azureGitCloneCommandService,
            GitRepositoryService gitRepositoryService,
            DataFactoryClient dataFactoryClient,
            PermissionService permissionService,
            ADFToolsWrapperService adfToolsWrapperService,
            AzureGitConfig azureGitConfig,
            MiscConfig miscConfig) {
        this.validationService = validationService;
        this.principalMappingService = principalMappingService;
        this.azureGitCloneCommandService = azureGitCloneCommandService;
        this.gitRepositoryService = gitRepositoryService;
        this.dataFactoryClient = dataFactoryClient;
        this.permissionService = permissionService;
        this.adfToolsWrapperService = adfToolsWrapperService;
        this.azureGitConfig = azureGitConfig;
        this.miscConfig = miscConfig;
    }

    @Override
    public Either<FailedOperation, ADFInfo> provision(ProvisioningRequest provisioningRequest) {
        var eitherValidation = validationService.validate(provisioningRequest);
        if (eitherValidation.isLeft()) return left(eitherValidation.getLeft());

        var provisionRequest = eitherValidation.get();
        // we can cast directly since checks were done on validation
        var specific = ((Workload<WorkloadSpecific>) provisionRequest.component()).getSpecific();

        var eitherTmpDir = FileUtils.createTempDirectory();
        if (eitherTmpDir.isLeft()) return left(eitherTmpDir.getLeft());
        var tmpDir = new File(eitherTmpDir.get());

        String devEnv = miscConfig.developmentEnvironmentName();
        String adfName = buildDataFactoryName(provisionRequest);
        Either<FailedOperation, String> eitherDeploy;
        try {
            if (devEnv.equalsIgnoreCase(provisionRequest.dataProduct().getEnvironment())) {
                eitherDeploy = deployOnDev(provisionRequest, specific, tmpDir, adfName);
            } else {
                eitherDeploy = deployOnOtherEnvs(provisionRequest, specific, tmpDir, adfName);
            }
        } finally {
            FileSystemUtils.deleteRecursively(tmpDir);
        }
        return eitherDeploy.map(id -> new ADFInfo(adfName, id, buildDataFactoryUrl(id)));
    }

    @Override
    public Either<FailedOperation, Void> unprovision(ProvisioningRequest provisioningRequest) {
        var eitherValidation = validationService.validate(provisioningRequest);
        if (eitherValidation.isLeft()) return left(eitherValidation.getLeft());

        var provisionRequest = eitherValidation.get();
        // we can cast directly since checks were done on validation
        var specific = ((Workload<WorkloadSpecific>) provisionRequest.component()).getSpecific();
        String adfName = buildDataFactoryName(provisionRequest);

        return dataFactoryClient.deleteADF(specific.getResourceGroup(), adfName);
    }

    private <T extends Specific> Either<FailedOperation, String> deployOnDev(
            ProvisionRequest<T> provisionRequest, WorkloadSpecific specific, File repoPath, String adfName) {
        FactoryGitConfiguration factoryGitConfiguration = new FactoryGitConfiguration(
                azureGitConfig.accountName(),
                specific.getProjectName(),
                specific.getRepositoryName(),
                azureGitConfig.collaborationBranch(),
                azureGitConfig.rootFolder(),
                azureGitConfig.lastCommitId(),
                azureGitConfig.tenantId(),
                azureGitConfig.disablePublish());

        return cloneGitRepository(specific, repoPath)
                .flatMap(v -> upsertDataFactoryInstance(specific, adfName).flatMap(instanceId -> dataFactoryClient
                        .linkGitRepository(
                                specific.getResourceGroup(), specific.getRegion(), adfName, factoryGitConfiguration)
                        .flatMap(vv -> mapOwners(provisionRequest).flatMap(owners -> permissionService
                                .assignOwnerPermissions(Set.copyOf(owners), instanceId)
                                .flatMap(vvv -> adfToolsWrapperService
                                        .publish(
                                                repoPath.getAbsolutePath(),
                                                specific.getResourceGroup(),
                                                adfName,
                                                specific.getRegion(),
                                                provisionRequest.dataProduct().getEnvironment(),
                                                true)
                                        .map(vvvv -> instanceId))))));
    }

    private <T extends Specific> Either<FailedOperation, String> deployOnOtherEnvs(
            ProvisionRequest<T> provisionRequest, WorkloadSpecific specific, File repoPath, String adfName) {
        return cloneGitRepository(specific, repoPath).flatMap(v -> upsertDataFactoryInstance(specific, adfName)
                .flatMap(instanceId -> mapOwners(provisionRequest).flatMap(owners -> permissionService
                        .assignReaderPermissions(Set.copyOf(owners), instanceId)
                        .flatMap(vvv -> adfToolsWrapperService
                                .publish(
                                        repoPath.getAbsolutePath(),
                                        specific.getResourceGroup(),
                                        adfName,
                                        specific.getRegion(),
                                        provisionRequest.dataProduct().getEnvironment(),
                                        false)
                                .map(vvvv -> instanceId)))));
    }

    private <T extends Specific> Either<FailedOperation, List<String>> mapOwners(ProvisionRequest<T> provisionRequest) {
        // FIXME workaround until related bug is fixed in witboost
        String devGroup = provisionRequest.dataProduct().getDevGroup().startsWith("group:")
                ? provisionRequest.dataProduct().getDevGroup()
                : "group:".concat(provisionRequest.dataProduct().getDevGroup());

        var owners = Set.of(provisionRequest.dataProduct().getDataProductOwner(), devGroup);

        var eitherPrincipals = principalMappingService.map(owners);

        var problems = eitherPrincipals.values().stream()
                .filter(Either::isLeft)
                .map(Either::getLeft)
                .map(FailedOperation::problems)
                .collect(ArrayList<Problem>::new, List::addAll, List::addAll);
        if (!problems.isEmpty()) return left(new FailedOperation(problems));

        return right(eitherPrincipals.values().stream().map(Either::get).toList());
    }

    private Either<FailedOperation, Void> cloneGitRepository(WorkloadSpecific specific, File path) {
        var cloneCommand = azureGitCloneCommandService.build(specific.getGitRepo(), path);
        return gitRepositoryService.clone(cloneCommand);
    }

    private Either<FailedOperation, String> upsertDataFactoryInstance(WorkloadSpecific specific, String adfName) {
        return dataFactoryClient.createADF(specific.getResourceGroup(), specific.getRegion(), adfName);
    }

    private String buildDataFactoryUrl(String adfInstanceId) {
        return String.format(
                "https://adf.azure.com/en/home?factory=%s", URLEncoder.encode(adfInstanceId, StandardCharsets.UTF_8));
    }

    private String buildDataFactoryName(ProvisionRequest<? extends Specific> provisionRequest) {
        /*
            Unique across Microsoft Azure. Names are case-insensitive.
            Each data factory is tied to exactly one Azure subscription.
            Object names must start with a letter or a number, and can contain only letters, numbers, and the dash (-) character.
            Every dash (-) character must be immediately preceded and followed by a letter or a number. Consecutive dashes are not permitted in container names.
            Name can be 3-63 characters long.
        */
        String domain = sanitize(provisionRequest.dataProduct().getDomain(), 10);
        String dpName = sanitize(provisionRequest.dataProduct().getName(), 32);
        String dpMajorVersion =
                sanitize(provisionRequest.dataProduct().getVersion().split("\\.")[0], 3);
        String env = sanitize(provisionRequest.dataProduct().getEnvironment(), 10);
        String hash = sanitize(DigestUtils.sha256Hex(domain + dpName + dpMajorVersion + env), 4);
        return String.format("%s-%s-%s-%s-%s", domain, dpName, dpMajorVersion, env, hash);
    }
}
