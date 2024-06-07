package it.agilelab.witboost.datafactory.service;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

import com.profesorfalken.jpowershell.PowerShell;
import io.vavr.control.Either;
import it.agilelab.witboost.datafactory.common.FailedOperation;
import it.agilelab.witboost.datafactory.common.Problem;
import java.io.*;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ADFToolsWrapperServiceImpl implements ADFToolsWrapperService {

    private final Logger logger = LoggerFactory.getLogger(ADFToolsWrapperServiceImpl.class);

    private final ApplicationContext applicationContext;

    @Value("classpath:validate.ps1")
    private Resource validateScript;

    @Value("classpath:publish.ps1")
    private Resource publishScript;

    public ADFToolsWrapperServiceImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Either<FailedOperation, Void> validate(String repositoryPath) {
        try (var ps = createPS();
                var reader = new BufferedReader(new FileReader(validateScript.getFile()))) {
            String params = String.format("-RootFolder \"%s\"", repositoryPath);
            var psResponse = ps.executeScript(reader, params);
            if (psResponse.isError()) {
                String errorMessage = String.format(
                        "An error occurred while executing the PS validation command. Please try again and if the error persists contact the platform team. Details: isTimeout: %s, isError: %s",
                        psResponse.isTimeout(), psResponse.isError());
                logger.error(errorMessage);
                logger.error(psResponse.getCommandOutput());
                return left(new FailedOperation(Collections.singletonList(new Problem(errorMessage))));
            }
            if (psResponse.getCommandOutput().contains("VALIDATION_KO")) {
                String errorMessage = String.format(
                        "The PS validation command returned one or more errors. Details: %s",
                        psResponse.getCommandOutput());
                logger.error(errorMessage);
                return left(new FailedOperation(Collections.singletonList(new Problem(errorMessage))));
            }
            logger.debug(psResponse.getCommandOutput());
            return right(null);
        } catch (IOException ex) {
            String errorMessage = String.format(
                    "An error occurred while validating the content of the GIT repository. Please try again and if the error persists contact the platform team. Details: %s",
                    ex.getMessage());
            logger.error(errorMessage, ex);
            return left(new FailedOperation(Collections.singletonList(new Problem(errorMessage, ex))));
        }
    }

    @Override
    public Either<FailedOperation, Void> publish(
            String repositoryPath,
            String resourceGroup,
            String dataFactoryName,
            String location,
            String environment,
            boolean isDevEnvironment) {
        try (var ps = createPS();
                var reader = new BufferedReader(new FileReader(publishScript.getFile()))) {
            StringBuilder sb = new StringBuilder(String.format("-RootFolder \"%s\"", repositoryPath));
            sb.append(String.format(" -ResourceGroupName \"%s\"", resourceGroup));
            sb.append(String.format(" -DataFactoryName \"%s\"", dataFactoryName));
            sb.append(String.format(" -Location \"%s\"", location));
            if (!isDevEnvironment) {
                sb.append(String.format(" -Stage \"%s\"", environment));
            }
            var psResponse = ps.executeScript(reader, sb.toString());
            if (psResponse.isError()) {
                String errorMessage = String.format(
                        "An error occurred while executing the PS publish command. Please try again and if the error persists contact the platform team. Details: isTimeout: %s, isError: %s",
                        psResponse.isTimeout(), psResponse.isError());
                logger.error(errorMessage);
                logger.error(psResponse.getCommandOutput());
                return left(new FailedOperation(Collections.singletonList(new Problem(errorMessage))));
            }
            if (psResponse.getCommandOutput().contains("PUBLISH_KO")) {
                String errorMessage = String.format(
                        "The PS publish command returned one or more errors. Details: %s",
                        psResponse.getCommandOutput());
                logger.error(errorMessage);
                return left(new FailedOperation(Collections.singletonList(new Problem(errorMessage))));
            }
            logger.debug(psResponse.getCommandOutput());
            return right(null);
        } catch (IOException ex) {
            String errorMessage = String.format(
                    "An error occurred while publishing resources on Data Factory. Please try again and if the error persists contact the platform team. Details: %s",
                    ex.getMessage());
            logger.error(errorMessage, ex);
            return left(new FailedOperation(Collections.singletonList(new Problem(errorMessage, ex))));
        }
    }

    private PowerShell createPS() {
        return applicationContext.getBean(PowerShell.class);
    }
}
