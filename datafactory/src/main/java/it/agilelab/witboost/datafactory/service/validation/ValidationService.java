package it.agilelab.witboost.datafactory.service.validation;

import io.vavr.control.Either;
import it.agilelab.witboost.datafactory.common.FailedOperation;
import it.agilelab.witboost.datafactory.model.ProvisionRequest;
import it.agilelab.witboost.datafactory.model.Specific;
import it.agilelab.witboost.datafactory.openapi.model.ProvisioningRequest;

public interface ValidationService {

    Either<FailedOperation, ProvisionRequest<? extends Specific>> validate(ProvisioningRequest provisioningRequest);
}
