package org.xbib.groovy.git.operation

import java.util.concurrent.Callable

import org.xbib.groovy.git.Branch
import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.internal.Operation
import org.xbib.groovy.git.service.ResolveService
import org.xbib.groovy.git.util.GitUtil
import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode
import org.eclipse.jgit.lib.Ref

/**
 * Changes a branch's start point and/or upstream branch. Returns the changed {@link Branch}.
 */
@Operation('change')
class BranchChangeOp implements Callable<Branch> {
    private final Repository repo

    /**
     * The name of the branch to change.
     */
    String name

    /**
     * The commit the branch should now start at.
     * @see {@link ResolveService#toRevisionString(Object)}
     */
    Object startPoint

    /**
     * The tracking mode to use.
     */
    Mode mode

    BranchChangeOp(Repository repo) {
        this.repo = repo
    }

    Branch call() {
        if (!GitUtil.resolveBranch(repo, name)) {
            throw new IllegalStateException("Branch does not exist: ${name}")
        }
        if (!startPoint) {
            throw new IllegalArgumentException('Must set new startPoint.')
        }
        CreateBranchCommand cmd = repo.jgit.branchCreate()
        cmd.name = name
        cmd.force = true
        if (startPoint) {
            String rev = new ResolveService(repo).toRevisionString(startPoint)
            cmd.startPoint = rev
        }
        if (mode) { cmd.upstreamMode = mode.jgit }

        Ref ref = cmd.call()
        return GitUtil.resolveBranch(repo, ref)
    }

    static enum Mode {
        TRACK(SetupUpstreamMode.TRACK),
        NO_TRACK(SetupUpstreamMode.NOTRACK)

        private final SetupUpstreamMode jgit

        Mode(SetupUpstreamMode jgit) {
            this.jgit = jgit
        }
    }
}
