package org.xbib.groovy.git.operation

import java.util.concurrent.Callable

import org.xbib.groovy.git.Branch
import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.internal.Operation
import org.xbib.groovy.git.service.ResolveService
import org.xbib.groovy.git.util.GitUtil
import org.eclipse.jgit.api.ListBranchCommand

/**
 * Lists branches in the repository. Returns a list of {@link Branch}.
 */
@Operation('list')
class BranchListOp implements Callable<List<Branch>> {
    private final Repository repo

    /**
     * Which branches to return.
     */
    Mode mode = Mode.LOCAL

    /**
     * Commit ref branches must contains
     */
    Object contains = null

    BranchListOp(Repository repo) {
        this.repo = repo
    }

    List<Branch> call() {
        ListBranchCommand cmd = repo.jgit.branchList()
        cmd.listMode = mode.jgit
        if (contains) {
            cmd.contains = new ResolveService(repo).toRevisionString(contains)
        }
        return cmd.call().collect {
            GitUtil.resolveBranch(repo, it.name)
        }
    }

    static enum Mode {
        ALL(ListBranchCommand.ListMode.ALL),
        REMOTE(ListBranchCommand.ListMode.REMOTE),
        LOCAL(null)

        private final ListBranchCommand.ListMode jgit

        private Mode(ListBranchCommand.ListMode jgit) {
            this.jgit = jgit
        }
    }
}
