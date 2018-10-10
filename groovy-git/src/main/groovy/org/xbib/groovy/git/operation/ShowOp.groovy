package org.xbib.groovy.git.operation

import java.util.concurrent.Callable

import org.xbib.groovy.git.CommitDiff
import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.internal.Operation
import org.xbib.groovy.git.service.ResolveService
import org.xbib.groovy.git.util.GitUtil
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.RenameDetector
import org.eclipse.jgit.diff.DiffEntry.ChangeType
import org.eclipse.jgit.treewalk.TreeWalk

/**
 * Show changes made in a commit.
 * Returns changes made in commit in the form of {@link CommitDiff}.
 */
@Operation('show')
class ShowOp implements Callable<CommitDiff> {
    private final Repository repo

    /**
     * The commit to show
     * @see {@link org.xbib.groovy.git.service.ResolveService#toRevisionString(Object)}
     */
    Object commit

    ShowOp(Repository repo) {
        this.repo = repo
    }

    CommitDiff call() {
        if (!commit) {
            throw new IllegalArgumentException('You must specify which commit to show')
        }
        def revString = new ResolveService(repo).toRevisionString(commit)
        def commitId = GitUtil.resolveRevObject(repo, revString)
        def parentId = GitUtil.resolveParents(repo, commitId).find()

        def commit = GitUtil.resolveCommit(repo, commitId)

        TreeWalk walk = new TreeWalk(repo.jgit.repository)
        walk.recursive = true

        if (parentId) {
            walk.addTree(parentId.tree)
            walk.addTree(commitId.tree)
            List initialEntries = DiffEntry.scan(walk)
            RenameDetector detector = new RenameDetector(repo.jgit.repository)
            detector.addAll(initialEntries)
            List entries = detector.compute()
            Map entriesByType = entries.groupBy { it.changeType }

            return new CommitDiff(
                    commit: commit,
                    added: entriesByType[ChangeType.ADD].collect { it.newPath },
                    copied: entriesByType[ChangeType.COPY].collect { it.newPath },
                    modified: entriesByType[ChangeType.MODIFY].collect { it.newPath },
                    removed: entriesByType[ChangeType.DELETE].collect { it.oldPath },
                    renamed: entriesByType[ChangeType.RENAME].collect { it.newPath }
            )
        } else {
            walk.addTree(commitId.tree)
            def added = []
            while (walk.next()) {
                added << walk.pathString
            }
            return new CommitDiff(
                    commit: commit,
                    added: added
            )
        }
    }
}
