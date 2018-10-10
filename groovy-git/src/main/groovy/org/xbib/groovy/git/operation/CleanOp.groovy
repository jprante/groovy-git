package org.xbib.groovy.git.operation

import java.util.concurrent.Callable

import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.internal.Operation
import org.eclipse.jgit.api.CleanCommand

/**
 * Remove untracked files from the working tree. Returns the list of
 * file paths deleted.
 */
@Operation('clean')
class CleanOp implements Callable<Set<String>> {
    private final Repository repo

    /**
     * The paths to clean. {@code null} if all paths should be included.
     */
    Set<String> paths

    /**
     * {@code true} if untracked directories should also be deleted,
     * {@code false} (the default) otherwise
     */
    boolean directories = false

    /**
     * {@code true} if the files should be returned, but not deleted,
     * {@code false} (the default) otherwise
     */
    boolean dryRun = false

    /**
     * {@code false} if files ignored by {@code .gitignore} should
     * also be deleted, {@code true} (the default) otherwise
     */
    boolean ignore = true

    CleanOp(Repository repo) {
        this.repo = repo
    }

    Set<String> call() {
        CleanCommand cmd = repo.jgit.clean()
        if (paths) {
            cmd.paths = paths
        }
        cmd.cleanDirectories = directories
        cmd.dryRun = dryRun
        cmd.ignore = ignore
        return cmd.call()
    }
}
