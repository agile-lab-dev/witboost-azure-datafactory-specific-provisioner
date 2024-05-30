package it.agilelab.witboost.datafactory.service;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.microsoft.kiota.ApiException;
import io.vavr.control.Option;
import it.agilelab.witboost.datafactory.common.FailedOperation;
import it.agilelab.witboost.datafactory.common.Problem;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PrincipalMappingServiceTest {

    @Mock
    private AzureGraphClient azureGraphClient;

    @InjectMocks
    private PrincipalMappingServiceImpl principalMappingService;

    private final String witboostUserIdentity = "user:name.surname_example.com";
    private final String mail = "name.surname@example.com";
    private final String witboostGroupIdentity = "group:name";
    private final String groupName = "name";
    private final Problem expectedProblem = new Problem("Error", new ApiException("Error"));

    @Test
    public void testMapExistingUser() {
        String userId = UUID.randomUUID().toString();
        when(azureGraphClient.getUserId(mail)).thenReturn(right(Option.of(userId)));

        var actualRes = principalMappingService.map(Collections.singleton(witboostUserIdentity));

        assertTrue(actualRes.containsKey(witboostUserIdentity));
        assertTrue(actualRes.get(witboostUserIdentity).isRight());
        assertEquals(userId, actualRes.get(witboostUserIdentity).get());
    }

    @Test
    public void testMapNotExistingUser() {
        when(azureGraphClient.getUserId(mail)).thenReturn(right(Option.none()));
        String expectedDesc = "The user name.surname@example.com was not found";

        var actualRes = principalMappingService.map(Collections.singleton(witboostUserIdentity));

        assertTrue(actualRes.containsKey(witboostUserIdentity));
        assertTrue(actualRes.get(witboostUserIdentity).isLeft());
        assertEquals(1, actualRes.get(witboostUserIdentity).getLeft().problems().size());
        actualRes.get(witboostUserIdentity).getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isEmpty());
        });
    }

    @Test
    public void testMapUserWithError() {
        when(azureGraphClient.getUserId(mail))
                .thenReturn(left(new FailedOperation(Collections.singletonList(expectedProblem))));

        var actualRes = principalMappingService.map(Collections.singleton(witboostUserIdentity));

        assertTrue(actualRes.containsKey(witboostUserIdentity));
        assertTrue(actualRes.get(witboostUserIdentity).isLeft());
        assertEquals(1, actualRes.get(witboostUserIdentity).getLeft().problems().size());
        actualRes.get(witboostUserIdentity).getLeft().problems().forEach(p -> assertEquals(expectedProblem, p));
    }

    @Test
    public void testMapExistingGroup() {
        String groupId = UUID.randomUUID().toString();
        when(azureGraphClient.getGroupId(groupName)).thenReturn(right(Option.of(groupId)));

        var actualRes = principalMappingService.map(Collections.singleton(witboostGroupIdentity));

        assertTrue(actualRes.containsKey(witboostGroupIdentity));
        assertTrue(actualRes.get(witboostGroupIdentity).isRight());
        assertEquals(groupId, actualRes.get(witboostGroupIdentity).get());
    }

    @Test
    public void testMapNotExistingGroup() {
        when(azureGraphClient.getGroupId(groupName)).thenReturn(right(Option.none()));
        String expectedDesc = "The group name was not found";

        var actualRes = principalMappingService.map(Collections.singleton(witboostGroupIdentity));

        assertTrue(actualRes.containsKey(witboostGroupIdentity));
        assertTrue(actualRes.get(witboostGroupIdentity).isLeft());
        assertEquals(
                1, actualRes.get(witboostGroupIdentity).getLeft().problems().size());
        actualRes.get(witboostGroupIdentity).getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isEmpty());
        });
    }

    @Test
    public void testMapGroupWithError() {
        when(azureGraphClient.getGroupId(groupName))
                .thenReturn(left(new FailedOperation(Collections.singletonList(expectedProblem))));

        var actualRes = principalMappingService.map(Collections.singleton(witboostGroupIdentity));

        assertTrue(actualRes.containsKey(witboostGroupIdentity));
        assertTrue(actualRes.get(witboostGroupIdentity).isLeft());
        assertEquals(
                1, actualRes.get(witboostGroupIdentity).getLeft().problems().size());
        actualRes.get(witboostGroupIdentity).getLeft().problems().forEach(p -> assertEquals(expectedProblem, p));
    }

    @Test
    public void testMapUnknownIdentity() {
        String unknownIdentity = "an_unknown_identity";
        Problem expectedUnknownProblem =
                new Problem("The subject an_unknown_identity is neither a Witboost user nor a group");

        var actualRes = principalMappingService.map(Collections.singleton(unknownIdentity));

        assertTrue(actualRes.containsKey(unknownIdentity));
        assertTrue(actualRes.get(unknownIdentity).isLeft());
        assertEquals(1, actualRes.get(unknownIdentity).getLeft().problems().size());
        actualRes.get(unknownIdentity).getLeft().problems().forEach(p -> assertEquals(expectedUnknownProblem, p));
    }

    @Test
    public void testMapExistingUserAndGroup() {
        String userId = UUID.randomUUID().toString();
        String groupId = UUID.randomUUID().toString();
        when(azureGraphClient.getUserId(mail)).thenReturn(right(Option.of(userId)));
        when(azureGraphClient.getGroupId(groupName)).thenReturn(right(Option.of(groupId)));

        var actualRes = principalMappingService.map(Set.of(witboostUserIdentity, witboostGroupIdentity));

        assertTrue(actualRes.containsKey(witboostUserIdentity));
        assertTrue(actualRes.get(witboostUserIdentity).isRight());
        assertEquals(userId, actualRes.get(witboostUserIdentity).get());
        assertTrue(actualRes.containsKey(witboostGroupIdentity));
        assertTrue(actualRes.get(witboostGroupIdentity).isRight());
        assertEquals(groupId, actualRes.get(witboostGroupIdentity).get());
    }

    @Test
    public void testMapUserWithWrongMailFormat() {
        String wrongUserIdentity = "user:no-underscore.example.com";
        Problem expectedWrongProblem =
                new Problem("The subject user:no-underscore.example.com has not the expected format for a user");

        var actualRes = principalMappingService.map(Collections.singleton(wrongUserIdentity));

        assertTrue(actualRes.containsKey(wrongUserIdentity));
        assertTrue(actualRes.get(wrongUserIdentity).isLeft());
        assertEquals(1, actualRes.get(wrongUserIdentity).getLeft().problems().size());
        actualRes.get(wrongUserIdentity).getLeft().problems().forEach(p -> assertEquals(expectedWrongProblem, p));
    }
}
