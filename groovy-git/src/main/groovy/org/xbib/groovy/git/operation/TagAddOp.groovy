package org.xbib.groovy.git.operation

import java.util.concurrent.Callable
import org.xbib.groovy.git.Person
import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.Tag
import org.xbib.groovy.git.internal.Operation
import org.xbib.groovy.git.service.ResolveService
import org.xbib.groovy.git.util.GitUtil
import org.eclipse.jgit.api.TagCommand
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.lib.Ref

/**
 * Adds a tag to the repository. Returns the newly created {@link Tag}.
 */
@Operation('add')
class TagAddOp implements Callable<Tag> {
    private final Repository repo

    /**
     * The name of the tag to create.
     */
    String name

    /**
     * The message to put on the tag.
     */
    String message

    /**
     * The person who created the tag.
     */
    Person tagger

    /**
     * {@code true} (the default) if an annotated tag should be
     * created, {@code false} otherwise.
     */
    boolean annotate = true

    /**
     * {@code true} to overwrite an existing tag, {@code false}
     * (the default) otherwise
     */
    boolean force = false

    /**
     * The commit the tag should point to.
     * @see {@link ResolveService#toRevisionString(Object)}
     */
    Object pointsTo

    TagAddOp(Repository repo) {
        this.repo = repo
    }

    Tag call() {
        TagCommand cmd = repo.jgit.tag()
        cmd.name = name
        cmd.message = message
        if (tagger) { cmd.tagger = new PersonIdent(tagger.name, tagger.email) }
        cmd.annotated = annotate
        cmd.forceUpdate = force
        if (pointsTo) {
            def revstr = new ResolveService(repo).toRevisionString(pointsTo)
            cmd.objectId = GitUtil.resolveRevObject(repo, revstr)
        }

        Ref ref = cmd.call()
        return GitUtil.resolveTag(repo, ref)
    }
}
