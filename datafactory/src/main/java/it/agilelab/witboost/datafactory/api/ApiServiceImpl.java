package it.agilelab.witboost.datafactory.api;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.vavr.control.Either;
import it.agilelab.witboost.datafactory.common.FailedOperation;
import it.agilelab.witboost.datafactory.common.Problem;
import it.agilelab.witboost.datafactory.common.SpecificProvisionerValidationException;
import it.agilelab.witboost.datafactory.model.ProvisionRequest;
import it.agilelab.witboost.datafactory.model.Specific;
import it.agilelab.witboost.datafactory.openapi.model.*;
import it.agilelab.witboost.datafactory.service.provision.ProvisionService;
import it.agilelab.witboost.datafactory.service.validation.ValidationService;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ApiServiceImpl {

    private final ValidationService validationService;
    private final ProvisionService provisionService;

    public ApiServiceImpl(ValidationService validationService, ProvisionService provisionService) {
        this.validationService = validationService;
        this.provisionService = provisionService;
    }

    public ValidationResult validate(ProvisioningRequest provisioningRequest) {
        Either<FailedOperation, ProvisionRequest<? extends Specific>> validate =
                validationService.validate(provisioningRequest);
        return validate.fold(
                failedOperation -> new ValidationResult(false)
                        .error(new ValidationError(failedOperation.problems().stream()
                                .map(Problem::description)
                                .collect(Collectors.toList()))),
                provisionRequest -> new ValidationResult(true));
    }

    public ProvisioningStatus provision(ProvisioningRequest provisioningRequest) {
        return provisionService
                .provision(provisioningRequest)
                .fold(
                        failedOperation -> {
                            throw new SpecificProvisionerValidationException(failedOperation);
                        },
                        adfInfo -> {
                            var privateInfo = Map.of(
                                    "adfName", adfInfo.name(),
                                    "adfInstanceId", adfInfo.instanceId(),
                                    "adfUrl", adfInfo.url());
                            return new ProvisioningStatus(ProvisioningStatus.StatusEnum.COMPLETED, "")
                                    .info(new Info(JsonNodeFactory.instance.objectNode(), privateInfo));
                        });
    }

    public ProvisioningStatus unprovision(ProvisioningRequest provisioningRequest) {
        return provisionService
                .unprovision(provisioningRequest)
                .fold(
                        failedOperation -> {
                            throw new SpecificProvisionerValidationException(failedOperation);
                        },
                        v -> new ProvisioningStatus(ProvisioningStatus.StatusEnum.COMPLETED, ""));
    }
}
