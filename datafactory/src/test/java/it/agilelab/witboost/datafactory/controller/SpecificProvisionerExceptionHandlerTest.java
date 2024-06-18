package it.agilelab.witboost.datafactory.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import it.agilelab.witboost.datafactory.common.FailedOperation;
import it.agilelab.witboost.datafactory.common.Problem;
import it.agilelab.witboost.datafactory.common.SpecificProvisionerValidationException;
import it.agilelab.witboost.datafactory.openapi.model.RequestValidationError;
import it.agilelab.witboost.datafactory.openapi.model.SystemError;
import it.agilelab.witboost.datafactory.openapi.model.ValidationResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.hamcrest.Matchers;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(SpecificProvisionerExceptionHandler.class)
public class SpecificProvisionerExceptionHandlerTest {

    @InjectMocks
    SpecificProvisionerExceptionHandler specificProvisionerExceptionHandler;

    @Test
    void testHandleConflictSystemError() {
        RuntimeException runtimeException = new RuntimeException();
        String expectedError =
                "An unexpected error occurred while processing the request. Please try again later. If the issue still persists, contact the platform team for assistance! Details: ";

        SystemError error = specificProvisionerExceptionHandler.handleSystemError(runtimeException);

        assertTrue(error.getError().startsWith(expectedError));
    }

    @Test
    void testHandleConflictRequestValidationError() {
        String expectedError = "Validation error";
        SpecificProvisionerValidationException specificProvisionerValidationException =
                new SpecificProvisionerValidationException(
                        new FailedOperation(Collections.singletonList(new Problem(expectedError))));

        RequestValidationError requestValidationError =
                specificProvisionerExceptionHandler.handleValidationException(specificProvisionerValidationException);

        assertEquals(1, requestValidationError.getErrors().size());
        requestValidationError.getErrors().forEach(e -> assertEquals(expectedError, e));
    }

    @Test
    void testHandleConstraintValidationError() {
        Set<ConstraintViolation<?>> violations = Set.of(
                buildConstraintViolation("is not valid", "path.to.field"),
                buildConstraintViolation("must not be null", "other.field"));
        ConstraintViolationException error = new ConstraintViolationException(violations);

        ValidationResult validationResult = specificProvisionerExceptionHandler.handleValidationException(error);

        var expectedErrors = List.of("path.to.field is not valid", "other.field must not be null");

        assertFalse(validationResult.getValid());
        assertEquals(2, validationResult.getError().getErrors().size());
        assertThat(
                expectedErrors,
                Matchers.containsInAnyOrder(
                        validationResult.getError().getErrors().toArray()));
    }

    private ConstraintViolation<?> buildConstraintViolation(String interpolatedMessage, String path) {
        return ConstraintViolationImpl.forBeanValidation(
                "",
                null,
                null,
                interpolatedMessage,
                null,
                null,
                null,
                null,
                PathImpl.createPathFromString(path),
                null,
                null);
    }
}
