package org.xbib.groovy.git.operation

import java.util.concurrent.Callable
import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.Tag
import org.xbib.groovy.git.internal.Operation
import org.xbib.groovy.git.util.GitUtil
import org.eclipse.jgit.api.ListTagCommand

/**
 * Lists tags in the repository. Returns a list of {@link Tag}.
 */
@Operation('list')
class TagListOp implements Callable<List<Tag>> {
    private final Repository repo

    TagListOp(Repository repo) {
        this.repo = repo
    }

    List<Tag> call() {
        ListTagCommand cmd = repo.jgit.tagList()

        return cmd.call().collect {
            GitUtil.resolveTag(repo, it)
        }
    }
}
