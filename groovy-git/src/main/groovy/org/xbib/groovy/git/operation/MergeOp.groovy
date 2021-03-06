package org.xbib.groovy.git.operation

import java.util.concurrent.Callable
import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.internal.Operation
import org.xbib.groovy.git.service.ResolveService
import org.xbib.groovy.git.util.GitUtil
import org.eclipse.jgit.api.MergeCommand
import org.eclipse.jgit.api.MergeResult

/**
 * Merges changes from a single head. This is a simplified version of
 * merge. If any conflict occurs the merge will throw an exception. The
 * conflicting files can be identified with {@code grgit.status()}.
 *
 * <p>Merge another head into the current branch.</p>
 *
 * <pre>
 * grgit.merge(head: 'some-branch')
 * </pre>
 *
 * <p>Merge with another mode.</p>
 *
 * <pre>
 * grgit.merge(mode: MergeOp.Mode.ONLY_FF)
 * </pre>
 */
@Operation('merge')
class MergeOp implements Callable<Void> {
    private final Repository repo

    /**
     * The head to merge into the current HEAD.
     * @see {@link ResolveService#toRevisionString(Object)}
     */
    Object head

    /**
     * The message to use for the merge commit
     */
    String message

    /**
     * How to handle the merge.
     */
    Mode mode

    MergeOp(Repository repo) {
        this.repo = repo
    }

    void setMode(String mode) {
        this.mode = mode.toUpperCase().replace('-', '_')
    }

    Void call() {
        MergeCommand cmd = repo.jgit.merge()
        if (head) {
            /*
             * we want to preserve ref name in merge commit msg. if it's a ref, don't
             * resolve down to commit id
             */
            def ref = repo.jgit.repository.findRef(head)
            if (ref == null) {
                def revstr = new ResolveService(repo).toRevisionString(head)
                cmd.include(GitUtil.resolveObject(repo, revstr))
            } else {
                cmd.include(ref)
            }
        }
        if (message) {
            cmd.setMessage(message)
        }
        switch (mode) {
            case Mode.ONLY_FF:
                cmd.fastForward = MergeCommand.FastForwardMode.FF_ONLY
                break
            case Mode.CREATE_COMMIT:
                cmd.fastForward = MergeCommand.FastForwardMode.NO_FF
                break
            case Mode.SQUASH:
                cmd.squash = true
                break
            case Mode.NO_COMMIT:
                cmd.commit = false
                break
        }

        MergeResult result = cmd.call()
        if (!result.mergeStatus.successful) {
            throw new IllegalStateException("could not merge (conflicting files can be retrieved with a call to grgit.status()): ${result}")
        }
        return null
    }

    static enum Mode {
        /**
         * Fast-forwards if possible, creates a merge commit otherwise.
         * Behaves like --ff.
         */
        DEFAULT,

        /**
         * Only merges if a fast-forward is possible.
         * Behaves like --ff-only.
         */
        ONLY_FF,

        /**
         * Always creates a merge commit (even if a fast-forward is possible).
         * Behaves like --no-ff.
         */
        CREATE_COMMIT,
        /**
         * Squashes the merged changes into one set and leaves them uncommitted.
         * Behaves like --squash.
         */
        SQUASH,
        /**
         * Merges changes, but does not commit them. Behaves like --no-commit.
         */
        NO_COMMIT
    }
}
