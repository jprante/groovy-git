package org.xbib.groovy.git.operation

import java.util.concurrent.Callable

import org.xbib.groovy.git.Commit
import org.xbib.groovy.git.Person
import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.internal.Operation
import org.xbib.groovy.git.util.GitUtil
import org.eclipse.jgit.api.CommitCommand
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.revwalk.RevCommit

/**
 * Commits staged changes to the repository. Returns the new {@code Commit}.
 */
@Operation('commit')
class CommitOp implements Callable<Commit> {
    private final Repository repo

    /**
     * Commit message.
     */
    String message

    /**
     * Comment to put in the reflog.
     */
    String reflogComment

    /**
     * The person who committed the changes. Uses the git-config
     * setting, if {@code null}.
     */
    Person committer

    /**
     * The person who authored the changes. Uses the git-config
     * setting, if {@code null}.
     */
    Person author

    /**
     * Only include these paths when committing. {@code null} to
     * include all staged changes.
     */
    Set<String> paths = []

    /**
     * Commit changes to all previously tracked files, even if
     * they aren't staged, if {@code true}.
     */
    boolean all = false

    /**
     * {@code true} if the previous commit should be amended with
     * these changes.
     */
    boolean amend = false

    CommitOp(Repository repo) {
        this.repo = repo
    }

    Commit call() {
        CommitCommand cmd = repo.jgit.commit()
        cmd.message = message
        cmd.reflogComment = reflogComment
        if (committer) {
            cmd.committer = new PersonIdent(committer.name, committer.email)
        }
        if (author) {
            cmd.author = new PersonIdent(author.name, author.email)
        }
        paths.each {
            cmd.setOnly(it)
        }
        if (all) {
            cmd.all = all
        }
        cmd.amend = amend
        RevCommit commit = cmd.call()
        return GitUtil.convertCommit(repo, commit)
    }
}
