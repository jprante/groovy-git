package org.xbib.groovy.git.operation

import java.util.concurrent.Callable

import org.xbib.groovy.git.Commit
import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.internal.Operation
import org.xbib.groovy.git.service.ResolveService
import org.xbib.groovy.git.util.GitUtil
import org.eclipse.jgit.api.RevertCommand
import org.eclipse.jgit.revwalk.RevCommit

/**
 * Revert one or more commits. Returns the new HEAD {@link Commit}.
 */
@Operation('revert')
class RevertOp implements Callable<Commit> {
    private final Repository repo

    /**
     * List of commits to revert.
     * @see {@link ResolveService#toRevisionString(Object)}
     */
    List<Object> commits = []

    RevertOp(Repository repo) {
        this.repo = repo
    }

    Commit call() {
        RevertCommand cmd = repo.jgit.revert()
        commits.each {
            String revstr = new ResolveService(repo).toRevisionString(it)
            cmd.include(GitUtil.resolveObject(repo, revstr))
        }
        RevCommit commit = cmd.call()
        if (cmd.failingResult) {
            throw new IllegalStateException("Could not merge reverted commits (conflicting files can be retrieved with a call to grgit.status()): ${cmd.failingResult}")
        }
        return GitUtil.convertCommit(repo, commit)
    }
}
