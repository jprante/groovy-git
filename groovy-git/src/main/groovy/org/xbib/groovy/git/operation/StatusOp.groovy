package org.xbib.groovy.git.operation

import java.util.concurrent.Callable

import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.Status
import org.xbib.groovy.git.internal.Operation
import org.xbib.groovy.git.util.GitUtil
import org.eclipse.jgit.api.StatusCommand

/**
 * Gets the current status of the repository. Returns an {@link Status}.
 */
@Operation('status')
class StatusOp implements Callable<Status> {
    private final Repository repo

    StatusOp(Repository repo) {
        this.repo = repo
    }

    Status call() {
        StatusCommand cmd = repo.jgit.status()
        return GitUtil.convertStatus(cmd.call())
    }
}
