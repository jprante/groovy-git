package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Git

import java.util.concurrent.Callable

import org.xbib.groovy.git.Credentials
import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.auth.TransportOpUtil
import org.xbib.groovy.git.internal.Operation
import org.xbib.groovy.git.util.CoercionUtil
import org.eclipse.jgit.api.CloneCommand

/**
 * Clones an existing repository. Returns a {@link org.xbib.groovy.git.Git} pointing
 * to the resulting repository.
 */
@Operation('clone')
class CloneOp implements Callable<Git> {
    /**
     * The directory to put the cloned repository.
     * @see {@link CoercionUtil#toFile(Object)}
     */
    Object dir

    /**
     * The URI to the repository to be cloned.
     */
    String uri

    /**
     * The name of the remote for the upstream repository. Defaults
     * to {@code origin}.
     */
    String remote = 'origin'

    /**
     * {@code true} if the resulting repository should be bare,
     * {@code false} (the default) otherwise.
     */
    boolean bare = false

    /**
     * {@code true} (the default) if a working tree should be checked out,
     * {@code false} otherwise
     */
    boolean checkout = true

    /**
     * The remote ref that should be checked out after the repository is
     * cloned. Defaults to {@code master}.
     */
    String refToCheckout

    /**
     * The username and credentials to use when checking out the
     * repository and for subsequent remote operations on the
     * repository. This is only needed if hardcoded credentials
     * should be used.
     */
    Credentials credentials

    Git call() {
        if (!checkout && refToCheckout) {
            throw new IllegalArgumentException('cannot specify a refToCheckout and set checkout to false')
        }
        CloneCommand cmd = org.eclipse.jgit.api.Git.cloneRepository()
        TransportOpUtil.configure(cmd, credentials)
        cmd.directory = CoercionUtil.toFile(dir)
        cmd.setURI(uri)
        cmd.remote = remote
        cmd.bare = bare
        cmd.noCheckout = !checkout
        if (refToCheckout) {
            cmd.branch = refToCheckout
        }
        org.eclipse.jgit.api.Git jgit = cmd.call()
        Repository repo = new Repository(CoercionUtil.toFile(dir), jgit, credentials)
        return new Git(repo)
    }
}

