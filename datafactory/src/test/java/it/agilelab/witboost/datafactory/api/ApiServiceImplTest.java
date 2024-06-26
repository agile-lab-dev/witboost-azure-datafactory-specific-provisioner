package it.agilelab.witboost.datafactory.api;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import io.vavr.control.Either;
import it.agilelab.witboost.datafactory.common.FailedOperation;
import it.agilelab.witboost.datafactory.common.Problem;
import it.agilelab.witboost.datafactory.common.SpecificProvisionerValidationException;
import it.agilelab.witboost.datafactory.model.ADFInfo;
import it.agilelab.witboost.datafactory.model.ProvisionRequest;
import it.agilelab.witboost.datafactory.model.Specific;
import it.agilelab.witboost.datafactory.openapi.model.*;
import it.agilelab.witboost.datafactory.service.provision.ProvisionService;
import it.agilelab.witboost.datafactory.service.validation.ValidationService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ApiServiceImplTest {

    @Mock
    private ValidationService validationService;

    @Mock
    private ProvisionService provisionService;

    @InjectMocks
    private ApiServiceImpl apiService;

    @Test
    public void testValidateOk() {
        ProvisioningRequest provisioningRequest = new ProvisioningRequest();
        when(validationService.validate(provisioningRequest))
                .thenReturn(right(new ProvisionRequest<Specific>(null, null, false)));
        var expectedRes = new ValidationResult(true);

        var actualRes = apiService.validate(provisioningRequest);

        assertEquals(expectedRes, actualRes);
    }

    @Test
    public void testValidateError() {
        ProvisioningRequest provisioningRequest = new ProvisioningRequest();
        var failedOperation = new FailedOperation(Collections.singletonList(new Problem("error")));
        when(validationService.validate(provisioningRequest)).thenReturn(Either.left(failedOperation));
        var expectedRes = new ValidationResult(false).error(new ValidationError(List.of("error")));

        var actualRes = apiService.validate(provisioningRequest);

        assertEquals(expectedRes, actualRes);
    }

    @Test
    void testProvisionOK() {
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, "", false);
        when(provisionService.provision(provisioningRequest))
                .thenReturn(right(new ADFInfo("name", "instanceId", "url")));
        var privateInfo = Map.of("adfName", "name", "adfInstanceId", "instanceId", "adfUrl", "url");

        var actualRes = apiService.provision(provisioningRequest);

        assertEquals(ProvisioningStatus.StatusEnum.COMPLETED, actualRes.getStatus());
        assertEquals(privateInfo, actualRes.getInfo().getPrivateInfo());
    }

    @Test
    void testProvisionKO() {
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, "", false);
        var failedOperation = new FailedOperation(Collections.singletonList(new Problem("Error")));
        when(provisionService.provision(provisioningRequest)).thenReturn(left(failedOperation));

        var exception = Assertions.assertThrows(
                SpecificProvisionerValidationException.class, () -> apiService.provision(provisioningRequest));

        assertEquals(failedOperation, exception.getFailedOperation());
    }

    @Test
    void testUnprovisionOK() {
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, "", false);
        when(provisionService.unprovision(provisioningRequest)).thenReturn(right(null));

        var actualRes = apiService.unprovision(provisioningRequest);

        assertEquals(ProvisioningStatus.StatusEnum.COMPLETED, actualRes.getStatus());
    }

    @Test
    void testUnprovisionKO() {
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, "", false);
        var failedOperation = new FailedOperation(
                Collections.singletonList(new Problem("Implement the unprovision logic based on your requirements!")));
        when(provisionService.unprovision(provisioningRequest)).thenReturn(left(failedOperation));

        var exception = Assertions.assertThrows(
                SpecificProvisionerValidationException.class, () -> apiService.unprovision(provisioningRequest));

        assertEquals(failedOperation, exception.getFailedOperation());
    }
}
