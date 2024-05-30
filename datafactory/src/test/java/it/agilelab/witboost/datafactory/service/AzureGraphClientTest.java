package it.agilelab.witboost.datafactory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.microsoft.graph.models.Group;
import com.microsoft.graph.models.User;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.kiota.ApiException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AzureGraphClientTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private GraphServiceClient graphServiceClient;

    @InjectMocks
    private AzureGraphClientImpl azureGraphClient;

    private final User azureUser = mock(User.class);
    private final Group azureGroup = mock(Group.class);

    @Test
    public void testFindUserByMailWithExistingUser() {
        String mail = "name.surname@email.com";
        String userId = UUID.randomUUID().toString();
        when(graphServiceClient.users().get(any()).getValue()).thenReturn(List.of(azureUser));
        when(azureUser.getId()).thenReturn(userId);

        var actualRes = azureGraphClient.getUserId(mail);

        assertTrue(actualRes.isRight());
        assertTrue(actualRes.get().isDefined());
        assertEquals(userId, actualRes.get().get());
    }

    @Test
    public void testFindUserByMailWithNotExistingUser() {
        String mail = "not.existing@email.com";
        when(graphServiceClient.users().get(any()).getValue()).thenReturn(Collections.emptyList());

        var actualRes = azureGraphClient.getUserId(mail);

        assertTrue(actualRes.isRight());
        assertTrue(actualRes.get().isEmpty());
    }

    @Test
    public void testFindUserByMailReturnError() {
        String mail = "name.surname@email.com";
        String error = "Unexpected error";
        var ex = new ApiException(error);
        when(graphServiceClient.users().get(any()).getValue()).thenThrow(ex);
        String expectedDesc =
                "An error occurred while retrieving the objectId of the user with mail name.surname@email.com. Please try again and if the error persists contact the platform team. Details: Unexpected error";

        var actualRes = azureGraphClient.getUserId(mail);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertTrue(p.description().startsWith(expectedDesc));
            assertTrue(p.cause().isPresent());
            assertEquals(ex, p.cause().get());
        });
    }

    @Test
    public void testFindGroupByNameWithExistingGroup() {
        String group = "group1";
        String groupId = UUID.randomUUID().toString();
        when(graphServiceClient.groups().get(any()).getValue()).thenReturn(List.of(azureGroup));
        when(azureGroup.getId()).thenReturn(groupId);

        var actualRes = azureGraphClient.getGroupId(group);

        assertTrue(actualRes.isRight());
        assertTrue(actualRes.get().isDefined());
        assertEquals(groupId, actualRes.get().get());
    }

    @Test
    public void testFindGroupByNameWithNotExistingGroup() {
        String group = "not-existing-group1";
        when(graphServiceClient.groups().get(any()).getValue()).thenReturn(Collections.emptyList());

        var actualRes = azureGraphClient.getGroupId(group);

        assertTrue(actualRes.isRight());
        assertTrue(actualRes.get().isEmpty());
    }

    @Test
    public void testFindGroupByNameReturnError() {
        String group = "group1";
        String error = "Unexpected error";
        var ex = new ApiException(error);
        when(graphServiceClient.groups().get(any()).getValue()).thenThrow(ex);
        String expectedDesc =
                "An error occurred while retrieving the objectId of the group group1. Please try again and if the error persists contact the platform team. Details: Unexpected error";

        var actualRes = azureGraphClient.getGroupId(group);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isPresent());
            assertEquals(ex, p.cause().get());
        });
    }
}
