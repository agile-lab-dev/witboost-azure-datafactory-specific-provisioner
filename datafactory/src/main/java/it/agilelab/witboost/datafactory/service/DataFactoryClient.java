package it.agilelab.witboost.datafactory.service;

import io.vavr.control.Either;
import it.agilelab.witboost.datafactory.common.FailedOperation;
import it.agilelab.witboost.datafactory.model.FactoryGitConfiguration;

/***
 * Data Factory services
 */
public interface DataFactoryClient {

    /***
     * Create an Azure Data Factory instance. If the name is already existing, the corresponding ID is returned
     * @param resourceGroup resource group where to create the data factory
     * @param region region where to create the data factory
     * @param name name of data factory to create
     * @return the ID of the created data factory or the error encountered
     */
    Either<FailedOperation, String> createADF(String resourceGroup, String region, String name);

    /***
     * Delete an Azure Data Factory instance. If the instance doesn't exist, no error is returned
     * @param resourceGroup resource group of the data factory to delete
     * @param name name of the data factory to delete
     * @return nothing or the error encountered
     */
    Either<FailedOperation, Void> deleteADF(String resourceGroup, String name);

    /***
     * Link a GIT repository to an existing Data Factory instance. If the ADF instance doesn't exist, an error is returned. If there's already a linked repository, no operation is performed
     * @param resourceGroup resource group of the existing data factory
     * @param region region of the existing data factory
     * @param name name of the existing data factory
     * @param factoryGitConfiguration the git configuration
     * @return nothing or the error encountered
     */
    Either<FailedOperation, Void> linkGitRepository(
            String resourceGroup, String region, String name, FactoryGitConfiguration factoryGitConfiguration);
}
