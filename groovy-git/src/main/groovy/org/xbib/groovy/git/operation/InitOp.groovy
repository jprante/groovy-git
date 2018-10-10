package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Git
import java.util.concurrent.Callable
import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.internal.Operation
import org.xbib.groovy.git.util.CoercionUtil
import org.eclipse.jgit.api.InitCommand

/**
 * Initializes a new repository. Returns a {@link org.xbib.groovy.git.Git} pointing
 * to the resulting repository.
 */
@Operation('init')
class InitOp implements Callable<Git> {
    /**
     * {@code true} if the repository should not have a
     * working tree, {@code false} (the default) otherwise
     */
    boolean bare = false

    /**
     * The directory to initialize the repository in.
     * @see {@link CoercionUtil#toFile(Object)}
     */
    Object dir

    Git call() {
        InitCommand cmd = org.eclipse.jgit.api.Git.init()
        cmd.bare = bare
        cmd.directory = CoercionUtil.toFile(dir)
        org.eclipse.jgit.api.Git jgit = cmd.call()
        Repository repo = new Repository(CoercionUtil.toFile(dir), jgit, null)
        return new Git(repo)
    }
}
