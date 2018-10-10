package org.xbib.groovy.git.operation

import java.util.concurrent.Callable
import org.xbib.groovy.git.Commit
import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.internal.Operation
import org.xbib.groovy.git.service.ResolveService
import org.xbib.groovy.git.util.GitUtil
import org.eclipse.jgit.api.LogCommand

/**
 * Gets a log of commits in the repository. Returns a list of {@link Commit}s.
 * Since a Git history is not necessarilly a line, these commits may not be in
 * a strict order.
 */
@Operation('log')
class LogOp implements Callable<List<Commit>> {
    private final Repository repo

    /**
     * @see {@link ResolveService#toRevisionString(Object)}
     */
    List includes = []
    /**
     * @see {@link ResolveService#toRevisionString(Object)}
     */
    List excludes = []
    List paths = []
    int skipCommits = -1
    int maxCommits = -1

    LogOp(Repository repo) {
        this.repo = repo
    }

    void range(Object since, Object until) {
        excludes << since
        includes << until
    }

    List<Commit> call() {
        LogCommand cmd = repo.jgit.log()
        ResolveService resolve = new ResolveService(repo)
        def toObjectId = { rev ->
            String revstr = resolve.toRevisionString(rev)
            GitUtil.resolveRevObject(repo, revstr, true).id
        }

        includes.collect(toObjectId).each { object ->
            cmd.add(object)
        }
        excludes.collect(toObjectId).each { object ->
            cmd.not(object)
        }
        paths.each { path ->
            cmd.addPath(path as String)
        }
        cmd.skip = skipCommits
        cmd.maxCount = maxCommits
        return cmd.call().collect { GitUtil.convertCommit(repo, it) }.asImmutable()
    }
}
