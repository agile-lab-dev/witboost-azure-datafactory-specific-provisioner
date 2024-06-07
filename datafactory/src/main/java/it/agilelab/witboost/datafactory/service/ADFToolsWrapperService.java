package it.agilelab.witboost.datafactory.service;

import io.vavr.control.Either;
import it.agilelab.witboost.datafactory.common.FailedOperation;

/***
 * azure.datafactory.tools PowerShell wrapper
 */
public interface ADFToolsWrapperService {

    /***
     * Call the Test-AdfCode PS cmdlet
     * @param repositoryPath the path where the GIT repository containing the ADF resources is cloned
     * @return nothing or the error encountered
     */
    Either<FailedOperation, Void> validate(String repositoryPath);

    /***
     * Call the Publish-AdfV2FromJson PS cmdlet
     * @param repositoryPath the path where the GIT repository containing the ADF resources is cloned
     * @param resourceGroup the resource group of the Data Factory
     * @param dataFactoryName the Data Factory name
     * @param location the location of the Data Factory
     * @param environment the deployment environment
     * @param isDevEnvironment true if the deployment environment is the development one
     * @return nothing or the error encountered
     */
    Either<FailedOperation, Void> publish(
            String repositoryPath,
            String resourceGroup,
            String dataFactoryName,
            String location,
            String environment,
            boolean isDevEnvironment);
}
