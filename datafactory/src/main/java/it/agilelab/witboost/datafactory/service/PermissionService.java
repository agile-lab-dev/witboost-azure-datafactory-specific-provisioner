package it.agilelab.witboost.datafactory.service;

import io.vavr.control.Either;
import it.agilelab.witboost.datafactory.common.FailedOperation;
import java.util.Set;

/***
 * RBAC services
 */
public interface PermissionService {

    /***
     * Assign owner permissions to the specified objectIds on the specified scope
     * @param objectIds the identities
     * @param scope the specific scope
     * @return nothing or the error encountered
     */
    Either<FailedOperation, Void> assignOwnerPermissions(Set<String> objectIds, String scope);

    /***
     * Assign reader permissions to the specified objectIds on the specified scope
     * @param objectIds the identities
     * @param scope the specific scope
     * @return nothing or the error encountered
     */
    Either<FailedOperation, Void> assignReaderPermissions(Set<String> objectIds, String scope);
}
