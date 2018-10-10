package org.xbib.groovy.git.operation

import java.util.concurrent.Callable
import org.xbib.groovy.git.PushException
import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.auth.TransportOpUtil
import org.xbib.groovy.git.internal.Operation
import org.eclipse.jgit.api.PushCommand
import org.eclipse.jgit.transport.RemoteRefUpdate

/**
 * Push changes to a remote repository.
 */
@Operation('push')
class PushOp implements Callable<Void> {
    private final Repository repo

    /**
     * The remote to push to.
     */
    String remote

    /**
     * The refs or refspecs to use when pushing. If {@code null}
     * and {@code all} is {@code false} only push the current branch.
     */
    List refsOrSpecs = []

    /**
     * {@code true} to push all branches, {@code false} (the default)
     * to only push the current one.
     */
    boolean all = false

    /**
     * {@code true} to push tags, {@code false} (the default) otherwise.
     */
    boolean tags = false

    /**
     * {@code true} if branches should be pushed even if they aren't
     * a fast-forward, {@code false} (the default) if it should fail.
     */
    boolean force = false

    /**
     * {@code true} if result of this operation should be just estimation
     * of real operation result, no real push is performed.
     * {@code false} (the default) if real push to remote repo should be performed.
     *
     * @since 0.4.1
     */
    boolean dryRun = false

    PushOp(Repository repo) {
        this.repo = repo
    }

    Void call() {
        PushCommand cmd = repo.jgit.push()
        TransportOpUtil.configure(cmd, repo.credentials)
        if (remote) {
            cmd.remote = remote
        }
        refsOrSpecs.each {
            cmd.add(it)
        }
        if (all) {
            cmd.setPushAll()
        }
        if (tags) {
            cmd.setPushTags()
        }
        cmd.force = force
        cmd.dryRun = dryRun
        def failures = []
        cmd.call().each { result ->
            result.remoteUpdates.findAll { update ->
                !(update.status == RemoteRefUpdate.Status.OK || update.status == RemoteRefUpdate.Status.UP_TO_DATE)
            }.each { update ->
                String info = "${update.srcRef} to ${update.remoteName}"
                String message = update.message ? " (${update.message})" : ''
                failures << "${info}${message}"
            }
        }
        if (failures) {
            throw new PushException("Failed to push: ${failures.join(',')}")
        }
        return null
    }
}
