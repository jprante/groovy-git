package org.xbib.groovy.git.operation

import java.util.concurrent.Callable

import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.internal.Operation
import org.xbib.groovy.git.service.ResolveService
import org.eclipse.jgit.api.DeleteBranchCommand

/**
 * Removes one or more branches from the repository. Returns a list of
 * the fully qualified branch names that were removed.
 */
@Operation('remove')
class BranchRemoveOp implements Callable<List<String>> {
    private final Repository repo

    /**
     * List of all branche names to remove.
     * @see {@link ResolveService#toBranchName(Object)}
     */
    List names = []

    /**
     * If {@code false} (the default), only remove branches that
     * are merged into another branch. If {@code true} will delete
     * regardless.
     */
    boolean force = false

    BranchRemoveOp(Repository repo) {
        this.repo = repo
    }

    List<String> call() {
        DeleteBranchCommand cmd = repo.jgit.branchDelete()
        cmd.setBranchNames(names.collect { new ResolveService(repo).toBranchName(it) } as String[])
        cmd.force = force
        return cmd.call()
    }
}
