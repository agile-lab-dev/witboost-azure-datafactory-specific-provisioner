package it.agilelab.witboost.datafactory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import com.azure.resourcemanager.AzureResourceManager;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellResponse;
import java.io.BufferedReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class ADFToolsWrapperServiceTest {

    @MockBean
    private GraphServiceClient graphServiceClient;

    @MockBean
    private AzureResourceManager azureResourceManager;

    @MockBean
    private PowerShell mockedPowerShell;

    @Autowired
    private ADFToolsWrapperServiceImpl aDFToolsWrapperService;

    @Test
    public void testValidateOk() {
        PowerShellResponse response = new PowerShellResponse(false, "VALIDATION_OK", false);
        when(mockedPowerShell.executeScript((BufferedReader) any(), eq("-RootFolder \"/tmp/folder\"")))
                .thenReturn(response);

        var actualRes = aDFToolsWrapperService.validate("/tmp/folder");

        assertTrue(actualRes.isRight());
    }

    @Test
    public void testValidateTimeout() {
        PowerShellResponse response = new PowerShellResponse(true, "", true);
        when(mockedPowerShell.executeScript((BufferedReader) any(), anyString()))
                .thenReturn(response);
        String expectedDesc =
                "An error occurred while executing the PS validation command. Please try again and if the error persists contact the platform team. Details: isTimeout: true, isError: true";

        var actualRes = aDFToolsWrapperService.validate("");

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isEmpty());
        });
    }

    @Test
    public void testValidateScriptError() {
        PowerShellResponse response = new PowerShellResponse(false, "VALIDATION_KO", false);
        when(mockedPowerShell.executeScript((BufferedReader) any(), anyString()))
                .thenReturn(response);
        String expectedDesc = "The PS validation command returned one or more errors. Details: VALIDATION_KO";

        var actualRes = aDFToolsWrapperService.validate("");

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isEmpty());
        });
    }

    @Test
    public void testPublishOk() {
        PowerShellResponse response = new PowerShellResponse(false, "PUBLISH_OK", false);
        when(mockedPowerShell.executeScript(
                        (BufferedReader) any(),
                        eq(
                                "-RootFolder \"/tmp/folder\" -ResourceGroupName \"rs1\" -DataFactoryName \"adf1\" -Location \"we\"")))
                .thenReturn(response);

        var actualRes = aDFToolsWrapperService.publish("/tmp/folder", "rs1", "adf1", "we", "dev", true);

        assertTrue(actualRes.isRight());
    }

    @Test
    public void testPublishNonDevOk() {
        PowerShellResponse response = new PowerShellResponse(false, "PUBLISH_OK", false);
        when(mockedPowerShell.executeScript(
                        (BufferedReader) any(),
                        eq(
                                "-RootFolder \"/tmp/folder\" -ResourceGroupName \"rs1\" -DataFactoryName \"adf1\" -Location \"we\" -Stage \"qa\"")))
                .thenReturn(response);

        var actualRes = aDFToolsWrapperService.publish("/tmp/folder", "rs1", "adf1", "we", "qa", false);

        assertTrue(actualRes.isRight());
    }

    @Test
    public void testPublishTimeout() {
        PowerShellResponse response = new PowerShellResponse(true, "", true);
        when(mockedPowerShell.executeScript((BufferedReader) any(), anyString()))
                .thenReturn(response);
        String expectedDesc =
                "An error occurred while executing the PS publish command. Please try again and if the error persists contact the platform team. Details: isTimeout: true, isError: true";

        var actualRes = aDFToolsWrapperService.publish("", "", "", "", "", true);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isEmpty());
        });
    }

    @Test
    public void testPublishScriptError() {
        PowerShellResponse response = new PowerShellResponse(false, "PUBLISH_KO", false);
        when(mockedPowerShell.executeScript((BufferedReader) any(), anyString()))
                .thenReturn(response);
        String expectedDesc = "The PS publish command returned one or more errors. Details: PUBLISH_KO";

        var actualRes = aDFToolsWrapperService.publish("", "", "", "", "", true);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isEmpty());
        });
    }
}
