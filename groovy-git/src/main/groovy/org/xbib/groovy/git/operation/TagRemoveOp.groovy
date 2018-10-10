package org.xbib.groovy.git.operation

import java.util.concurrent.Callable
import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.internal.Operation
import org.xbib.groovy.git.service.ResolveService
import org.eclipse.jgit.api.DeleteTagCommand

/**
 * Removes one or more tags from the repository. Returns a list of
 * the fully qualified tag names that were removed.
 */
@Operation('remove')
class TagRemoveOp implements Callable<List<String>> {
    private final Repository repo

    /**
     * Names of tags to remove.
     * @see {@link ResolveService#toTagName(Object)}
     */
    List names = []

    TagRemoveOp(Repository repo) {
        this.repo = repo
    }

    List<String> call() {
        DeleteTagCommand cmd = repo.jgit.tagDelete()
        cmd.tags = names.collect { new ResolveService(repo).toTagName(it) }

        return cmd.call()
    }
}
