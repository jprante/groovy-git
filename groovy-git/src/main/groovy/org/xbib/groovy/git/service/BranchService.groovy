package org.xbib.groovy.git.service

import org.xbib.groovy.git.Branch
import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.internal.WithOperations
import org.xbib.groovy.git.operation.BranchAddOp
import org.xbib.groovy.git.operation.BranchChangeOp
import org.xbib.groovy.git.operation.BranchListOp
import org.xbib.groovy.git.operation.BranchRemoveOp
import org.xbib.groovy.git.operation.BranchStatusOp
import org.xbib.groovy.git.util.GitUtil
import org.eclipse.jgit.lib.Ref

/**
 * Provides support for performing branch-related operations on
 * a Git repository.
 *
 * <p>
 *   Details of each operation's properties and methods are available on the
 *   doc page for the class. The following operations are supported directly on
 *   this service instance.
 * </p>
 *
 * <ul>
 *   <li>{@link org.xbib.groovy.git.operation.BranchAddOp add}</li>
 *   <li>{@link org.xbib.groovy.git.operation.BranchChangeOp change}</li>
 *   <li>{@link org.xbib.groovy.git.operation.BranchListOp list}</li>
 *   <li>{@link org.xbib.groovy.git.operation.BranchRemoveOp remove}</li>
 *   <li>{@link org.xbib.groovy.git.operation.BranchStatusOp status}</li>
 * </ul>
 *
 */
@WithOperations(instanceOperations=[BranchListOp, BranchAddOp, BranchRemoveOp, BranchChangeOp, BranchStatusOp])
class BranchService {

    private final Repository repository

    BranchService(Repository repository) {
        this.repository = repository
    }

    /**
     * Gets the branch associated with the current HEAD.
     * @return the branch or {@code null} if the HEAD is detached
     */
    Branch getCurrent() {
        Ref ref = repository.jgit.repository.exactRef('HEAD')?.target
        return ref ? GitUtil.resolveBranch(repository, ref) : null
    }
}
