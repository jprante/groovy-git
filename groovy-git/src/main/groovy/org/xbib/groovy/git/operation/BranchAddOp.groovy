package org.xbib.groovy.git.operation

import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode
import org.eclipse.jgit.lib.Ref
import org.xbib.groovy.git.Branch
import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.internal.Operation
import org.xbib.groovy.git.service.ResolveService
import org.xbib.groovy.git.util.GitUtil

import java.util.concurrent.Callable

/**
 * Adds a branch to the repository. Returns the newly created {@link Branch}.
 */
@Operation('add')
class BranchAddOp implements Callable<Branch> {
    private final Repository repo

    /**
     * The name of the branch to add.
     */
    String name

    /**
     * The commit the branch should start at. If this is a remote branch
     * it will be automatically tracked.
     * @see {@link ResolveService#toRevisionString(Object)}
     */
    Object startPoint

    /**
     * The tracking mode to use. If {@code null}, will use the default
     * behavior.
     */
    Mode mode

    BranchAddOp(Repository repo) {
        this.repo = repo
    }

    Branch call() {
        if (mode && !startPoint) {
            throw new IllegalStateException('Cannot set mode if no start point.')
        }
        CreateBranchCommand cmd = repo.jgit.branchCreate()
        cmd.name = name
        cmd.force = false
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
