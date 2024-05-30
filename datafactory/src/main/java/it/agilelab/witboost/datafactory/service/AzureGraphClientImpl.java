package it.agilelab.witboost.datafactory.service;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

import com.microsoft.graph.serviceclient.GraphServiceClient;
import io.vavr.control.Either;
import io.vavr.control.Option;
import it.agilelab.witboost.datafactory.common.FailedOperation;
import it.agilelab.witboost.datafactory.common.Problem;
import java.util.Collections;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AzureGraphClientImpl implements AzureGraphClient {

    private final Logger logger = LoggerFactory.getLogger(AzureGraphClientImpl.class);

    private final GraphServiceClient graphServiceClient;

    public AzureGraphClientImpl(GraphServiceClient graphServiceClient) {
        this.graphServiceClient = graphServiceClient;
    }

    @Override
    public Either<FailedOperation, Option<String>> getUserId(String mail) {
        try {
            var res = Optional.ofNullable(graphServiceClient
                    .users()
                    .get(requestConfiguration ->
                            requestConfiguration.queryParameters.filter = String.format("mail eq '%s'", mail))
                    .getValue());
            var firstEntry = res.flatMap(l -> l.stream().findFirst());
            if (firstEntry.isEmpty()) {
                String errorMessage = String.format("User %s not found on the configured Azure tenant", mail);
                logger.error(errorMessage);
                return right(Option.none());
            } else {
                return right(Option.of(firstEntry.get().getId()));
            }
        } catch (Exception ex) {
            String errorMessage = String.format(
                    "An error occurred while retrieving the objectId of the user with mail %s. Please try again and if the error persists contact the platform team. Details: %s",
                    mail, ex.getMessage());
            logger.error(errorMessage, ex);
            return left(new FailedOperation(Collections.singletonList(new Problem(errorMessage, ex))));
        }
    }

    @Override
    public Either<FailedOperation, Option<String>> getGroupId(String group) {
        try {
            var res = Optional.ofNullable(graphServiceClient
                    .groups()
                    .get(requestConfiguration ->
                            requestConfiguration.queryParameters.filter = String.format("displayName eq '%s'", group))
                    .getValue());
            var firstEntry = res.flatMap(l -> l.stream().findFirst());
            if (firstEntry.isEmpty()) {
                String errorMessage = String.format("Group %s not found on the configured Azure tenant", group);
                logger.error(errorMessage);
                return right(Option.none());
            } else {
                return right(Option.of(firstEntry.get().getId()));
            }
        } catch (Exception ex) {
            String errorMessage = String.format(
                    "An error occurred while retrieving the objectId of the group %s. Please try again and if the error persists contact the platform team. Details: %s",
                    group, ex.getMessage());
            logger.error(errorMessage, ex);
            return left(new FailedOperation(Collections.singletonList(new Problem(errorMessage, ex))));
        }
    }
}
