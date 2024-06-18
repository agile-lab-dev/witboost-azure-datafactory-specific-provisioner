package it.agilelab.witboost.datafactory.service.validation;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import it.agilelab.witboost.datafactory.common.FailedOperation;
import it.agilelab.witboost.datafactory.common.Problem;
import it.agilelab.witboost.datafactory.openapi.model.DescriptorKind;
import it.agilelab.witboost.datafactory.openapi.model.ProvisioningRequest;
import it.agilelab.witboost.datafactory.util.ResourceUtils;
import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ValidationServiceTest {

    @Mock
    private WorkloadValidation workloadValidation;

    @InjectMocks
    private ValidationServiceImpl service;

    @Test
    public void testValidateWorkloadOk() throws IOException {
        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_workload.yml");
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);
        when(workloadValidation.validate(any())).thenReturn(right(null));

        var actualRes = service.validate(provisioningRequest);

        assertTrue(actualRes.isRight());
    }

    @Test
    public void testValidateWorkloadKo() throws IOException {
        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_workload.yml");
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);
        String expectedDesc = "Error";
        var fail = new FailedOperation(Collections.singletonList(new Problem(expectedDesc)));
        when(workloadValidation.validate(any())).thenReturn(left(fail));

        var actualRes = service.validate(provisioningRequest);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isEmpty());
        });
    }

    @Test
    public void testValidateWrongDescriptorKind() throws IOException {
        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_outputport.yml");
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.DATAPRODUCT_DESCRIPTOR, ymlDescriptor, false);
        String expectedDesc =
                "The descriptorKind field is not valid. Expected: 'COMPONENT_DESCRIPTOR', Actual: 'DATAPRODUCT_DESCRIPTOR'";

        var actualResult = service.validate(provisioningRequest);

        assertTrue(actualResult.isLeft());
        assertEquals(1, actualResult.getLeft().problems().size());
        actualResult.getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isEmpty());
        });
    }

    @Test
    public void testValidateWrongDescriptorFormat() {
        String ymlDescriptor = "an_invalid_descriptor";
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);
        String expectedDesc = "Failed to deserialize the Yaml Descriptor. Details: ";

        var actualRes = service.validate(provisioningRequest);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertTrue(p.description().startsWith(expectedDesc));
            assertTrue(p.cause().isPresent());
        });
    }

    @Test
    public void testValidateMissingComponentIdToProvision() throws IOException {
        String ymlDescriptor =
                ResourceUtils.getContentFromResource("/pr_descriptor_storage_missing_componentIdToProvision.yml");
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);
        String expectedDesc = "Component with ID null not found in the Descriptor";

        var actualRes = service.validate(provisioningRequest);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isEmpty());
        });
    }

    @Test
    public void testValidateMissingComponentToProvision() throws IOException {
        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_storage_missing_component.yml");
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);
        String expectedDesc =
                "Component with ID urn:dmb:cmp:healthcare:vaccinations:0:storage not found in the Descriptor";

        var actualResult = service.validate(provisioningRequest);

        assertTrue(actualResult.isLeft());
        assertEquals(1, actualResult.getLeft().problems().size());
        actualResult.getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isEmpty());
        });
    }

    @Test
    public void testValidateMissingComponentKindToProvision() throws IOException {
        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_storage_missing_componentKind.yml");
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);
        String expectedDesc =
                "Component Kind not found for the component with ID urn:dmb:cmp:healthcare:vaccinations:0:storage";

        var actualRes = service.validate(provisioningRequest);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isEmpty());
        });
    }

    @Test
    public void testValidateWrongComponentKindToProvision() throws IOException {
        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_storage_wrong_componentKind.yml");
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);
        String expectedDesc =
                "The kind 'wrong' of the component to provision is not supported by this Specific Provisioner";

        var actualRes = service.validate(provisioningRequest);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isEmpty());
        });
    }
}
