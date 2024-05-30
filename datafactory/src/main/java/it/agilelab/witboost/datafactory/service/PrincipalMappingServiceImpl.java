package it.agilelab.witboost.datafactory.service;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

import io.vavr.control.Either;
import it.agilelab.witboost.datafactory.common.FailedOperation;
import it.agilelab.witboost.datafactory.common.Problem;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PrincipalMappingServiceImpl implements PrincipalMappingService {

    private final Logger logger = LoggerFactory.getLogger(PrincipalMappingServiceImpl.class);

    private final AzureGraphClient azureGraphClient;

    public PrincipalMappingServiceImpl(AzureGraphClient azureGraphClient) {
        this.azureGraphClient = azureGraphClient;
    }

    @Override
    public Map<String, Either<FailedOperation, String>> map(Set<String> subjects) {
        return subjects.stream()
                .map(s -> {
                    if (isWitboostUser(s)) {
                        var eitherMail = getMailFromWitboostIdentity(s);
                        return eitherMail.fold(
                                l -> new AbstractMap.SimpleEntry<String, Either<FailedOperation, String>>(s, left(l)),
                                mail -> new AbstractMap.SimpleEntry<>(s, mapUser(mail)));
                    } else if (isWitboostGroup(s)) {
                        String group = getGroup(s);
                        return new AbstractMap.SimpleEntry<>(s, mapGroup(group));
                    } else {
                        return new AbstractMap.SimpleEntry<>(s, mapUnkownIdentity(s));
                    }
                })
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    private Either<FailedOperation, String> getMailFromWitboostIdentity(String witboostIdentity) {
        String user = getUser(witboostIdentity);
        int underscoreIndex = user.lastIndexOf("_");
        if (underscoreIndex == -1) {
            String errorMessage =
                    String.format("The subject %s has not the expected format for a user", witboostIdentity);
            logger.error(errorMessage);
            return left(new FailedOperation(Collections.singletonList(new Problem(errorMessage))));
        } else {
            return right(user.substring(0, underscoreIndex) + "@" + user.substring(underscoreIndex + 1));
        }
    }

    private Either<FailedOperation, String> mapGroup(String group) {
        return azureGraphClient
                .getGroupId(group)
                .flatMap(g -> g.toEither(new FailedOperation(
                        Collections.singletonList(new Problem(String.format("The group %s was not found", group))))));
    }

    private Either<FailedOperation, String> mapUser(String mail) {
        return azureGraphClient
                .getUserId(mail)
                .flatMap(u -> u.toEither(new FailedOperation(
                        Collections.singletonList(new Problem(String.format("The user %s was not found", mail))))));
    }

    private Either<FailedOperation, String> mapUnkownIdentity(String s) {
        String errorMessage = String.format("The subject %s is neither a Witboost user nor a group", s);
        logger.error(errorMessage);
        return left(new FailedOperation(Collections.singletonList(new Problem(errorMessage))));
    }

    private String getUser(String witboostIdentity) {
        return witboostIdentity.substring(5);
    }

    private String getGroup(String witboostIdentity) {
        return witboostIdentity.substring(6);
    }

    private boolean isWitboostGroup(String witboostIdentity) {
        return witboostIdentity.startsWith("group:");
    }

    private boolean isWitboostUser(String witboostIdentity) {
        return witboostIdentity.startsWith("user:");
    }
}
