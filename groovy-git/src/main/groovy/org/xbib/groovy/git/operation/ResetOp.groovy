package org.xbib.groovy.git.operation

import java.util.concurrent.Callable

import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.internal.Operation
import org.xbib.groovy.git.service.ResolveService
import org.eclipse.jgit.api.ResetCommand

/**
 * Reset changes in the repository.
 */
@Operation('reset')
class ResetOp implements Callable<Void> {
    private final Repository repo

    /**
     * The paths to reset.
     */
    Set<String> paths = []

    /**
     * The commit to reset back to. Defaults to HEAD.
     * @see {@link ResolveService#toRevisionString(Object)}
     */
    Object commit

    /**
     * The mode to use when resetting.
     */
    Mode mode = Mode.MIXED

    ResetOp(Repository repo) {
        this.repo = repo
    }

    void setMode(String mode) {
        this.mode = mode.toUpperCase()
    }

    Void call() {
        if (!paths.empty && mode != Mode.MIXED) {
            throw new IllegalStateException('Cannot set mode when resetting paths.')
        }

        ResetCommand cmd = repo.jgit.reset()
        paths.each { cmd.addPath(it) }
        if (commit) {
            cmd.ref = new ResolveService(repo).toRevisionString(commit)
        }
        if (paths.empty) {
            cmd.mode = mode.jgit
        }

        cmd.call()
        return null
    }

    static enum Mode {
        /**
         * Reset the index and working tree.
         */
        HARD(ResetCommand.ResetType.HARD),
        /**
         * Reset the index, but not the working tree.
         */
        MIXED(ResetCommand.ResetType.MIXED),
        /**
         * Only reset the HEAD. Leave the index and working tree as-is.
         */
        SOFT(ResetCommand.ResetType.SOFT)

        private final ResetCommand.ResetType jgit

        private Mode(ResetCommand.ResetType jgit) {
            this.jgit = jgit
        }
    }
}
