package org.xbib.groovy.git.operation

import java.util.concurrent.Callable

import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.internal.Operation
import org.eclipse.jgit.api.AddCommand

/**
 * Adds files to the index.
 */
@Operation('add')
class AddOp implements Callable<Void> {
    private final Repository repo

    /**
     * Patterns of files to add to the index.
     */
    Set<String> patterns = []

    /**
     * {@code true} if changes to all currently tracked files should be added
     * to the index, {@code false} otherwise.
     */
    boolean update = false

    AddOp(Repository repo) {
        this.repo = repo
    }

    Void call() {
        AddCommand cmd = repo.jgit.add()
        patterns.each { cmd.addFilepattern(it) }
        cmd.update = update
        cmd.call()
        return null
    }
}
