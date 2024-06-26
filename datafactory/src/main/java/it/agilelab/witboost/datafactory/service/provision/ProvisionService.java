package it.agilelab.witboost.datafactory.service.provision;

import io.vavr.control.Either;
import it.agilelab.witboost.datafactory.common.FailedOperation;
import it.agilelab.witboost.datafactory.model.ADFInfo;
import it.agilelab.witboost.datafactory.openapi.model.ProvisioningRequest;

/***
 * Provision services
 */
public interface ProvisionService {

    /**
     * Provision the component present in the request
     *
     * @param provisioningRequest the request
     * @return the outcome of the provision
     */
    Either<FailedOperation, ADFInfo> provision(ProvisioningRequest provisioningRequest);

    /**
     * Unprovision the component present in the request
     *
     * @param provisioningRequest the request
     * @return the outcome of the unprovision
     */
    Either<FailedOperation, Void> unprovision(ProvisioningRequest provisioningRequest);
}
