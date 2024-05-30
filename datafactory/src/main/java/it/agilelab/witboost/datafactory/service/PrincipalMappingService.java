package it.agilelab.witboost.datafactory.service;

import io.vavr.control.Either;
import it.agilelab.witboost.datafactory.common.FailedOperation;
import java.util.Map;
import java.util.Set;

/***
 * Principal mapping services
 */
public interface PrincipalMappingService {
    /**
     * Map subjects to Azure Identities
     *
     * @param subjects the set of subjects to map
     * @return return a FailedOperation or the mapped record for every subject to be mapped
     */
    Map<String, Either<FailedOperation, String>> map(Set<String> subjects);
}
