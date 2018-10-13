package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Git
import org.xbib.groovy.git.GitTestUtil
import org.xbib.groovy.git.MultiGitOpSpec
import org.eclipse.jgit.api.errors.GitAPIException

import spock.lang.Unroll

class BranchAddOpSpec extends MultiGitOpSpec {

    Git localGit

    Git remoteGit

    List commits = []

    def setup() {
        remoteGit = init('remote')

        repoFile(remoteGit, '1.txt') << '1'
        commits << remoteGit.commit(message: 'do', all: true)

        repoFile(remoteGit, '1.txt') << '2'
        commits << remoteGit.commit(message: 'do', all: true)

        remoteGit.branch.add(name: 'my-branch')

        localGit = clone('local', remoteGit)
    }

    def 'branch add with name creates branch pointing to current HEAD'() {
        when:
        localGit.branch.add(name: 'test-branch')
        then:
        localGit.branch.list() == [GitTestUtil.branch('refs/heads/master', 'refs/remotes/origin/master'), GitTestUtil.branch('refs/heads/test-branch')]
        localGit.resolve.toCommit('test-branch') == localGit.head()
    }

    def 'branch add with name and startPoint creates branch pointing to startPoint'() {
        when:
        localGit.branch.add(name: 'test-branch', startPoint: commits[0].id)
        then:
        localGit.branch.list() == [GitTestUtil.branch('refs/heads/master', 'refs/remotes/origin/master'), GitTestUtil.branch('refs/heads/test-branch')]
        localGit.resolve.toCommit('test-branch') == commits[0]
    }

    def 'branch add fails to overwrite existing branch'() {
        given:
        localGit.branch.add(name: 'test-branch', startPoint: commits[0].id)
        when:
        localGit.branch.add(name: 'test-branch')
        then:
        thrown(GitAPIException)
    }

    def 'branch add with mode set but no start point fails'() {
        when:
        localGit.branch.add(name: 'my-branch', mode: mode)
        then:
        thrown(IllegalStateException)
        where:
        mode << BranchAddOp.Mode.values()
    }

    @Unroll('branch add with #mode mode starting at #startPoint tracks #trackingBranch')
    def 'branch add with mode and start point behaves correctly'() {
        given:
        localGit.branch.add(name: 'test-branch', startPoint: commits[0].id)
        expect:
        localGit.branch.add(name: 'local-branch', startPoint: startPoint, mode: mode) == GitTestUtil.branch('refs/heads/local-branch', trackingBranch)
        localGit.resolve.toCommit('local-branch') == localGit.resolve.toCommit(startPoint)
        where:
        mode					  | startPoint		 | trackingBranch
        null					  | 'origin/my-branch' | 'refs/remotes/origin/my-branch'
        BranchAddOp.Mode.TRACK	| 'origin/my-branch' | 'refs/remotes/origin/my-branch'
        BranchAddOp.Mode.NO_TRACK | 'origin/my-branch' | null
        null					  | 'test-branch'	  | null
        BranchAddOp.Mode.TRACK	| 'test-branch'	  | 'refs/heads/test-branch'
        BranchAddOp.Mode.NO_TRACK | 'test-branch'	  | null
    }

    @Unroll('branch add with no name, #mode mode, and a start point fails')
    def 'branch add with no name fails'() {
        when:
        localGit.branch.add(startPoint: 'origin/my-branch', mode: mode)
        then:
        thrown(GitAPIException)
        where:
        mode << [null, BranchAddOp.Mode.TRACK, BranchAddOp.Mode.NO_TRACK]
    }
}
