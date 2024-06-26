package it.agilelab.witboost.datafactory.service.validation;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

import io.vavr.control.Either;
import it.agilelab.witboost.datafactory.common.FailedOperation;
import it.agilelab.witboost.datafactory.common.Problem;
import it.agilelab.witboost.datafactory.model.*;
import it.agilelab.witboost.datafactory.service.ADFToolsWrapperService;
import it.agilelab.witboost.datafactory.service.AzureGitCloneCommandService;
import it.agilelab.witboost.datafactory.service.GitRepositoryService;
import it.agilelab.witboost.datafactory.util.FileUtils;
import jakarta.validation.Valid;
import java.io.File;
import java.util.Collections;
import org.eclipse.jgit.api.CloneCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;
import org.springframework.validation.annotation.Validated;

@org.springframework.stereotype.Component
@Validated
public class WorkloadValidation {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadValidation.class);

    private final GitRepositoryService gitRepositoryService;
    private final AzureGitCloneCommandService azureGitCloneCommandService;
    private final ADFToolsWrapperService adfToolsWrapperService;

    public WorkloadValidation(
            GitRepositoryService gitRepositoryService,
            AzureGitCloneCommandService azureGitCloneCommandService,
            ADFToolsWrapperService adfToolsWrapperService) {
        this.gitRepositoryService = gitRepositoryService;
        this.azureGitCloneCommandService = azureGitCloneCommandService;
        this.adfToolsWrapperService = adfToolsWrapperService;
    }

    public Either<FailedOperation, Void> validate(@Valid Component<? extends Specific> component) {
        logger.info("Checking component with ID {} is of type Workload", component.getId());
        if (component instanceof Workload<? extends Specific> workload) {
            logger.info("The received component is a Workload");
            if (workload.getSpecific() instanceof WorkloadSpecific specific) {
                var eitherTmpDir = FileUtils.createTempDirectory();
                if (eitherTmpDir.isLeft()) return left(eitherTmpDir.getLeft());
                var tmpDir = eitherTmpDir.get();
                try {
                    return Either.<FailedOperation, CloneCommand>right(
                                    azureGitCloneCommandService.build(specific.getGitRepo(), new File(tmpDir)))
                            .flatMap(cloneCommand -> gitRepositoryService
                                    .clone(cloneCommand)
                                    .flatMap(v -> adfToolsWrapperService
                                            .validate(tmpDir)
                                            .flatMap(vv -> right(null))));
                } finally {
                    FileSystemUtils.deleteRecursively(new File(tmpDir));
                }
            } else {
                String errorMessage = String.format(
                        "The specific section of the component %s is not of type WorkloadSpecific", component.getId());
                logger.error(errorMessage);
                return left(new FailedOperation(Collections.singletonList(new Problem(errorMessage))));
            }
        } else {
            String errorMessage = String.format("The component %s is not of type Workload", component.getId());
            logger.error(errorMessage);
            return left(new FailedOperation(Collections.singletonList(new Problem(errorMessage))));
        }
    }
}
