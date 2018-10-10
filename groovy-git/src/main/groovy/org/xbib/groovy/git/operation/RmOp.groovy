package org.xbib.groovy.git.operation

import java.util.concurrent.Callable

import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.internal.Operation
import org.eclipse.jgit.api.RmCommand

/**
 * Remove files from the index and (optionally) delete them from the working tree.
 * Note that wildcards are not supported.
 */
@Operation('remove')
class RmOp implements Callable<Void> {
    private final Repository repo

    /**
     * The file patterns to remove.
     */
    Set<String> patterns = []

    /**
     * {@code true} if files should only be removed from the index,
     * {@code false} (the default) otherwise.
     */
    boolean cached = false

    RmOp(Repository repo) {
        this.repo = repo
    }

    Void call() {
        RmCommand cmd = repo.jgit.rm()
        patterns.each { cmd.addFilepattern(it) }
        cmd.cached = cached
        cmd.call()
        return null
    }
}
