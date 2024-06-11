package it.agilelab.witboost.datafactory.service;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

import io.vavr.control.Either;
import it.agilelab.witboost.datafactory.common.FailedOperation;
import it.agilelab.witboost.datafactory.common.Problem;
import java.util.Collections;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GitRepositoryService {

    private final Logger logger = LoggerFactory.getLogger(GitRepositoryService.class);

    /***
     * Clones a GIT repository
     * @param cloneCommand the command to call
     * @return nothing or the error encountered
     */
    public Either<FailedOperation, Void> clone(CloneCommand cloneCommand) {
        try (var ignored = cloneCommand.call()) {
            return right(null);
        } catch (GitAPIException ex) {
            String errorMessage = String.format(
                    "An error occurred while cloning the GIT repository. Please try again and if the error persists contact the platform team. Details: %s",
                    ex.getMessage());
            logger.error(errorMessage, ex);
            return left(new FailedOperation(Collections.singletonList(new Problem(errorMessage, ex))));
        }
    }
}
