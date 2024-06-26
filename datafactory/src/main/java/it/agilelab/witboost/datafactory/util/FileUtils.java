package it.agilelab.witboost.datafactory.util;

import io.vavr.control.Either;
import io.vavr.control.Try;
import it.agilelab.witboost.datafactory.common.FailedOperation;
import it.agilelab.witboost.datafactory.common.Problem;
import java.nio.file.Files;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static Either<FailedOperation, String> createTempDirectory() {
        return Try.of(() -> Files.createTempDirectory("datafactory").toFile().getAbsolutePath())
                .toEither()
                .mapLeft(t -> {
                    String errorMessage = String.format(
                            "An error occurred while creating a temporary folder for the repository cloning. Please try again and if the error persists contact the platform team. Details: %s",
                            t.getMessage());
                    logger.error(errorMessage, t);
                    return new FailedOperation(Collections.singletonList(new Problem(errorMessage, t)));
                });
    }
}
