package it.agilelab.witboost.datafactory.service;

import io.vavr.control.Either;
import io.vavr.control.Option;
import it.agilelab.witboost.datafactory.common.FailedOperation;

/**
 * Azure mapping functions
 */
public interface AzureGraphClient {
    /**
     * Retrieve the corresponding Azure objectId for the given mail address
     * @param mail user mail address
     * @return either an error or the optional objectId
     */
    Either<FailedOperation, Option<String>> getUserId(String mail);

    /**
     * Retrieve the corresponding Azure objectId for the given group name
     * @param group group name
     * @return either an error or the optional objectId
     */
    Either<FailedOperation, Option<String>> getGroupId(String group);
}
