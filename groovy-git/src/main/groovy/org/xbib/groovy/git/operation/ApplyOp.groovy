package org.xbib.groovy.git.operation

import java.util.concurrent.Callable

import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.internal.Operation
import org.xbib.groovy.git.util.CoercionUtil
import org.eclipse.jgit.api.ApplyCommand

/**
 * Apply a patch to the index.
 */
@Operation('apply')
class ApplyOp implements Callable<Void> {
    private final Repository repo

    /**
     * The patch file to apply to the index.
     * @see {@link CoercionUtil#toFile(Object)}
     */
    Object patch

    ApplyOp(Repository repo) {
        this.repo = repo
    }

    @Override
    Void call() {
        ApplyCommand cmd = repo.jgit.apply()
        if (!patch) {
            throw new IllegalStateException('Must set a patch file.')
        }
        CoercionUtil.toFile(patch).withInputStream { stream ->
            cmd.patch = stream
            cmd.call()
        }
        return
    }
}
