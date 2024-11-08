package it.agilelab.witboost.datafactory.controller;

import it.agilelab.witboost.datafactory.common.Problem;
import it.agilelab.witboost.datafactory.common.SpecificProvisionerValidationException;
import it.agilelab.witboost.datafactory.openapi.model.*;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exception handler for the API layer.
 *
 * <p>The following methods wrap generic exceptions into 400 and 500 errors. Implement your own
 * exception handlers based on the business exception that the provisioner throws. No further
 * modifications need to be done outside this file to make it work, as Spring identifies at startup
 * the handlers with the @ExceptionHandler annotation
 */
@RestControllerAdvice
public class SpecificProvisionerExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(SpecificProvisionerExceptionHandler.class);

    @ExceptionHandler({SpecificProvisionerValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected RequestValidationError handleValidationException(SpecificProvisionerValidationException ex) {
        return new RequestValidationError(ex.getFailedOperation().problems().stream()
                .map(Problem::description)
                .collect(Collectors.toList()));
    }

    @ExceptionHandler({RuntimeException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected SystemError handleSystemError(RuntimeException ex) {
        String errorMessage = String.format(
                "An unexpected error occurred while processing the request. Please try again later. If the issue still persists, contact the platform team for assistance! Details: %s",
                ex.getMessage());
        logger.error(errorMessage, ex);
        return new SystemError(errorMessage);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.OK)
    protected ValidationResult handleValidationException(ConstraintViolationException ex) {
        logger.error("Validation error", ex);
        ValidationResult validationResult = new ValidationResult(false);
        var problems = ex.getConstraintViolations().stream()
                .map(constraintViolation -> String.format(
                        "%s %s", constraintViolation.getPropertyPath().toString(), constraintViolation.getMessage()))
                .toList();
        validationResult.setError(new ValidationError(problems));
        return validationResult;
    }
}
