package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Git
import org.xbib.groovy.git.GitTestUtil
import org.xbib.groovy.git.MultiGitOpSpec
import spock.lang.Unroll

class BranchListOpSpec extends MultiGitOpSpec {

    Git localGit

    Git remoteGit

    def setup() {
        remoteGit = init('remote')

        repoFile(remoteGit, '1.txt') << '1'
        remoteGit.commit(message: 'do', all: true)

        remoteGit.branch.add(name: 'my-branch')

        repoFile(remoteGit, '2.txt') << '2'
        remoteGit.commit(message: 'another', all: true)
        remoteGit.tag.add(name: 'test-tag');

        localGit = clone('local', remoteGit)
    }

    @Unroll('list branch with #arguments lists #expected')
    def 'list branch without arguments only lists local'() {
        given:
        def expectedBranches = expected.collect { GitTestUtil.branch(*it) }
        def head = localGit.head()
        expect:
        localGit.branch.list(arguments) == expectedBranches
        where:
        arguments											  | expected
        [:]													| [['refs/heads/master', 'refs/remotes/origin/master']]
        [mode: BranchListOp.Mode.LOCAL]						| [['refs/heads/master', 'refs/remotes/origin/master']]
        [mode: BranchListOp.Mode.REMOTE]					   | [['refs/remotes/origin/master'], ['refs/remotes/origin/my-branch']]
        [mode: BranchListOp.Mode.ALL]						  | [['refs/heads/master', 'refs/remotes/origin/master'], ['refs/remotes/origin/master'], ['refs/remotes/origin/my-branch']]
        [mode: BranchListOp.Mode.REMOTE, contains: 'test-tag'] | [['refs/remotes/origin/master']]
    }

    def 'list branch receives Commit object as contains flag'() {
        given:
        def expectedBranches = [GitTestUtil.branch('refs/remotes/origin/master')]
        def head = localGit.head()
        def arguments = [mode: BranchListOp.Mode.REMOTE, contains: head]
        expect:
        localGit.branch.list(arguments) == expectedBranches
    }
}
