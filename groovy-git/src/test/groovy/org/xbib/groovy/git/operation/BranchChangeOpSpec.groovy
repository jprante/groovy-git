package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Git
import org.xbib.groovy.git.GitTestUtil
import org.xbib.groovy.git.MultiGitOpSpec
import spock.lang.Unroll

class BranchChangeOpSpec extends MultiGitOpSpec {

    Git localGit

    Git remoteGit

    List commits = []

    def setup() {
        remoteGit = init('remote')

        repoFile(remoteGit, '1.txt') << '1'
        commits << remoteGit.commit(message: 'do', all: true)

        repoFile(remoteGit, '1.txt') << '2'
        commits << remoteGit.commit(message: 'do', all: true)

        remoteGit.checkout(branch: 'my-branch', createBranch: true)

        repoFile(remoteGit, '1.txt') << '3'
        commits << remoteGit.commit(message: 'do', all: true)

        localGit = clone('local', remoteGit)
        localGit.branch.add(name: 'local-branch')

        localGit.branch.add(name: 'test-branch', startPoint: commits[0].id)
    }

    def 'branch change with non-existent branch fails'() {
        when:
        localGit.branch.change(name: 'fake-branch', startPoint: 'test-branch')
        then:
        thrown(IllegalStateException)
    }

    def 'branch change with no start point fails'() {
        when:
        localGit.branch.change(name: 'local-branch')
        then:
        thrown(IllegalArgumentException)
    }

    @Unroll('branch change with #mode mode starting at #startPoint tracks #trackingBranch')
    def 'branch change with mode and start point behaves correctly'() {
        expect:
        localGit.branch.change(name: 'local-branch', startPoint: startPoint, mode: mode) == GitTestUtil.branch('refs/heads/local-branch', trackingBranch)
        localGit.resolve.toCommit('local-branch') == localGit.resolve.toCommit(startPoint)
        where:
        mode						 | startPoint		 | trackingBranch
        null						 | 'origin/my-branch' | 'refs/remotes/origin/my-branch'
        BranchChangeOp.Mode.TRACK	| 'origin/my-branch' | 'refs/remotes/origin/my-branch'
        BranchChangeOp.Mode.NO_TRACK | 'origin/my-branch' | null
        null						 | 'test-branch'	  | null
        BranchChangeOp.Mode.TRACK	| 'test-branch'	  | 'refs/heads/test-branch'
        BranchChangeOp.Mode.NO_TRACK | 'test-branch'	  | null
    }
}
