package org.xbib.groovy.git

import org.xbib.groovy.git.internal.WithOperations
import org.xbib.groovy.git.operation.AddOp
import org.xbib.groovy.git.operation.ApplyOp
import org.xbib.groovy.git.operation.CheckoutOp
import org.xbib.groovy.git.operation.CleanOp
import org.xbib.groovy.git.operation.CloneOp
import org.xbib.groovy.git.operation.CommitOp
import org.xbib.groovy.git.operation.DescribeOp
import org.xbib.groovy.git.operation.FetchOp
import org.xbib.groovy.git.operation.InitOp
import org.xbib.groovy.git.operation.LogOp
import org.xbib.groovy.git.operation.LsRemoteOp
import org.xbib.groovy.git.operation.MergeOp
import org.xbib.groovy.git.operation.OpenOp
import org.xbib.groovy.git.operation.PullOp
import org.xbib.groovy.git.operation.PushOp
import org.xbib.groovy.git.operation.ResetOp
import org.xbib.groovy.git.operation.RevertOp
import org.xbib.groovy.git.operation.RmOp
import org.xbib.groovy.git.operation.ShowOp
import org.xbib.groovy.git.operation.StatusOp
import org.xbib.groovy.git.service.BranchService
import org.xbib.groovy.git.service.RemoteService
import org.xbib.groovy.git.service.ResolveService
import org.xbib.groovy.git.service.TagService
import org.xbib.groovy.git.util.GitUtil

/**
 * Provides support for performing operations on and getting information about
 * a Git repository.
 *
 * <p>A Git instance can be obtained via 3 methods.</p>
 *
 * <ul>
 *   <li>
 *	 <p>{@link org.xbib.groovy.git.operation.OpenOp Open} an existing repository.</p>
 *	 <pre>def git = Git.open(dir: 'path/to/my/repo')</pre>
 *   </li>
 *   <li>
 *	 <p>{@link org.xbib.groovy.git.operation.InitOp Initialize} a new repository.</p>
 *	 <pre>def git = Git.init(dir: 'path/to/my/repo')</pre>
 *   </li>
 *   <li>
 *	 <p>{@link org.xbib.groovy.git.operation.CloneOp Clone} an existing repository.</p>
 *	 <pre>def git = Git.clone(dir: 'path/to/my/repo', uri: 'git@github.com:jprante/groovy-git.git')</pre>
 *   </li>
 * </ul>
 *
 * <p>
 *   Once obtained, operations can be called with two syntaxes.
 * </p>
 *
 * <ul>
 *   <li>
 *	 <p>Map syntax. Any public property on the {@code *Op} class can be provided as a Map entry.</p>
 *	 <pre>git.commit(message: 'Committing my code.', amend: true)</pre>
 *   </li>
 *   <li>
 *	 <p>Closure syntax. Any public property or method on the {@code *Op} class can be used.</p>
 *	 <pre>
 * git.log {
 *   range 'master', 'my-new-branch'
 *   maxCommits = 5
 * }
 *	 </pre>
 *   </li>
 * </ul>
 *
 * <p>
 *   Details of each operation's properties and methods are available on the
 *   doc page for the class. The following operations are supported directly on a
 *   Git instance.
 * </p>
 *
 * <ul>
 *   <li>{@link org.xbib.groovy.git.operation.AddOp add}</li>
 *   <li>{@link org.xbib.groovy.git.operation.ApplyOp apply}</li>
 *   <li>{@link org.xbib.groovy.git.operation.CheckoutOp checkout}</li>
 *   <li>{@link org.xbib.groovy.git.operation.CleanOp clean}</li>
 *   <li>{@link org.xbib.groovy.git.operation.CommitOp commit}</li>
 *   <li>{@link org.xbib.groovy.git.operation.DescribeOp describe}</li>
 *   <li>{@link org.xbib.groovy.git.operation.FetchOp fetch}</li>
 *   <li>{@link org.xbib.groovy.git.operation.LogOp log}</li>
 *   <li>{@link org.xbib.groovy.git.operation.LsRemoteOp lsremote}</li>
 *   <li>{@link org.xbib.groovy.git.operation.MergeOp merge}</li>
 *   <li>{@link org.xbib.groovy.git.operation.PullOp pull}</li>
 *   <li>{@link org.xbib.groovy.git.operation.PushOp push}</li>
 *   <li>{@link org.xbib.groovy.git.operation.RmOp remove}</li>
 *   <li>{@link org.xbib.groovy.git.operation.ResetOp reset}</li>
 *   <li>{@link org.xbib.groovy.git.operation.RevertOp revert}</li>
 *   <li>{@link org.xbib.groovy.git.operation.ShowOp show}</li>
 *   <li>{@link org.xbib.groovy.git.operation.StatusOp status}</li>
 * </ul>
 *
 * <p>
 *   And the following operations are supported statically on the Git class.
 * </p>
 *
 * <ul>
 *   <li>{@link org.xbib.groovy.git.operation.CloneOp clone}</li>
 *   <li>{@link org.xbib.groovy.git.operation.InitOp init}</li>
 *   <li>{@link org.xbib.groovy.git.operation.OpenOp open}</li>
 * </ul>
 *
 * <p>
 *   Further operations are available on the following services.
 * </p>
 *
 * <ul>
 *   <li>{@link org.xbib.groovy.git.service.BranchService branch}</li>
 *   <li>{@link org.xbib.groovy.git.service.RemoteService remote}</li>
 *   <li>{@link org.xbib.groovy.git.service.ResolveService resolve}</li>
 *   <li>{@link org.xbib.groovy.git.service.TagService tag}</li>
 * </ul>
 */
@WithOperations(staticOperations=[InitOp, CloneOp, OpenOp], instanceOperations=[CleanOp,
        StatusOp, AddOp, RmOp, ResetOp, ApplyOp, PullOp, PushOp, FetchOp, LsRemoteOp,
        CheckoutOp, LogOp, CommitOp, RevertOp, MergeOp, DescribeOp, ShowOp])
class Git implements AutoCloseable {
    /**
     * The repository opened by this object.
     */
    final Repository repository

    /**
     * Supports operations on branches.
     */
    final BranchService branch

    /**
     * Supports operations on remotes.
     */
    final RemoteService remote

    /**
     * Convenience methods for resolving various objects.
     */
    final ResolveService resolve

    /**
     * Supports operations on tags.
     */
    final TagService tag

    Git(Repository repository) {
        this.repository = repository
        this.branch = new BranchService(repository)
        this.remote = new RemoteService(repository)
        this.tag = new TagService(repository)
        this.resolve = new ResolveService(repository)
    }

    /**
     * Returns the commit located at the current HEAD of the repository.
     * @return the current HEAD commit
     */
    Commit head() {
        return resolve.toCommit('HEAD')
    }

    /**
     * Checks if {@code base} is an ancestor of {@code tip}.
     * @param base the version that might be an ancestor
     * @param tip the tip version
     */
    boolean isAncestorOf(Object base, Object tip) {
        Commit baseCommit = resolve.toCommit(base)
        Commit tipCommit = resolve.toCommit(tip)
        return GitUtil.isAncestorOf(repository, baseCommit, tipCommit)
    }

    /**
     * Release underlying resources used by this instance. After calling close
     * you should not use this instance anymore.
     */
    @Override
    void close() {
        repository.jgit.close()
    }
}
