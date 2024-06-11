package it.agilelab.witboost.datafactory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GitRepositoryServiceTest {

    @Mock
    private CloneCommand cloneCommand;

    private final GitRepositoryService gitRepositoryService = new GitRepositoryService();

    @Mock
    private Git git;

    @Test
    public void testCloneOk() throws GitAPIException {
        when(cloneCommand.call()).thenReturn(git);

        var actualRes = gitRepositoryService.clone(cloneCommand);

        assertTrue(actualRes.isRight());
    }

    @Test
    public void testCloneError() throws GitAPIException {
        var ex = new RefNotFoundException("Error");
        String expectedDesc =
                "An error occurred while cloning the GIT repository. Please try again and if the error persists contact the platform team. Details: Error";
        when(cloneCommand.call()).thenThrow(ex);

        var actualRes = gitRepositoryService.clone(cloneCommand);

        assertTrue(actualRes.isLeft());
        assertEquals(1, actualRes.getLeft().problems().size());
        actualRes.getLeft().problems().forEach(p -> {
            assertEquals(expectedDesc, p.description());
            assertTrue(p.cause().isPresent());
            assertEquals(ex, p.cause().get());
        });
    }
}
