package it.agilelab.witboost.datafactory.service.validation;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import it.agilelab.witboost.datafactory.common.FailedOperation;
import it.agilelab.witboost.datafactory.common.Problem;
import it.agilelab.witboost.datafactory.model.OutputPort;
import it.agilelab.witboost.datafactory.model.Specific;
import it.agilelab.witboost.datafactory.model.Workload;
import it.agilelab.witboost.datafactory.model.WorkloadSpecific;
import it.agilelab.witboost.datafactory.service.ADFToolsWrapperService;
import it.agilelab.witboost.datafactory.service.AzureGitCloneCommandService;
import it.agilelab.witboost.datafactory.service.GitRepositoryService;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WorkloadValidationTest {

    @Mock
    private GitRepositoryService gitRepositoryService;

    @Mock
    private AzureGitCloneCommandService azureGitCloneCommandService;

    @Mock
    private ADFToolsWrapperService adfToolsWrapperService;

    @InjectMocks
    private WorkloadValidation workloadValidation;

    private final Workload<WorkloadSpecific> workload;

    public WorkloadValidationTest() {
        WorkloadSpecific workloadSpecific = new WorkloadSpecific();
        workload = new Workload<>();
        workload.setId("my_id_workload");
        workload.setName("workload name");
        workload.setDescription("workload desc");
        workload.setKind("workload");
        workload.setSpecific(workloadSpecific);
    }

    @Test
    public void testValidateOk() {
        when(gitRepositoryService.clone(any())).thenReturn(right(null));
        when(adfToolsWrapperService.validate(anyString())).thenReturn(right(null));

        var actualRes = workloadValidation.validate(workload);

        assertTrue(actualRes.isRight());
    }

    @Test
    public void testValidateCloneFailure() {
        String expectedDesc = "Error while cloning";
        var fail = new FailedOperation(Collections.singletonList(new Problem(expectedDesc)));
        when(gitRepositoryService.clone(any())).thenReturn(left(fail));

        var actualRes = workloadValidation.validate(workload);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isEmpty());
        });
    }

    @Test
    public void testValidateInvalidGitContent() {
        String expectedDesc = "Found an invalid json file name";
        var fail = new FailedOperation(Collections.singletonList(new Problem(expectedDesc)));
        when(gitRepositoryService.clone(any())).thenReturn(right(null));
        when(adfToolsWrapperService.validate(anyString())).thenReturn(left(fail));

        var actualRes = workloadValidation.validate(workload);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isEmpty());
        });
    }

    @Test
    public void testValidateWrongType() {
        OutputPort<Specific> outputPort = new OutputPort<>();
        outputPort.setId("my_id_op");
        String expectedDesc = "The component my_id_op is not of type Workload";

        var actualRes = workloadValidation.validate(outputPort);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isEmpty());
        });
    }

    @Test
    public void testValidateWrongSpecific() {
        Specific specific = new Specific();
        Workload<Specific> workload = new Workload<>();
        workload.setId("my_id_workload");
        workload.setName("workload name");
        workload.setDescription("workload desc");
        workload.setKind("workload");
        workload.setSpecific(specific);
        String expectedDesc = "The specific section of the component my_id_workload is not of type WorkloadSpecific";

        var actualRes = workloadValidation.validate(workload);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isEmpty());
        });
    }
}
