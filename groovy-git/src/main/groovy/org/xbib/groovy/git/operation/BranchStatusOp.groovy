package org.xbib.groovy.git.operation

import java.util.concurrent.Callable

import org.xbib.groovy.git.Branch
import org.xbib.groovy.git.BranchStatus
import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.internal.Operation
import org.xbib.groovy.git.service.ResolveService
import org.eclipse.jgit.lib.BranchTrackingStatus

/**
 * Gets the tracking status of a branch. Returns a {@link BranchStatus}.
 *
 * <pre>
 * def status = git.branch.status(name: 'the-branch')
 * </pre>
 */
@Operation('status')
class BranchStatusOp implements Callable<BranchStatus> {
    private final Repository repo

    /**
     * The branch to get the status of.
     * @see {@link ResolveService#toBranch(Object)}
     */
    Object name

    BranchStatusOp(Repository repo) {
        this.repo = repo
    }

    BranchStatus call() {
        Branch realBranch = new ResolveService(repo).toBranch(name)
        if (realBranch.trackingBranch) {
            BranchTrackingStatus status = BranchTrackingStatus.of(repo.jgit.repository, realBranch.fullName)
            if (status) {
                return new BranchStatus(realBranch, status.aheadCount, status.behindCount)
            } else {
                throw new IllegalStateException("Could not retrieve status for ${name}")
            }
        } else {
            throw new IllegalStateException("${name} is not set to track another branch")
        }
    }
}
