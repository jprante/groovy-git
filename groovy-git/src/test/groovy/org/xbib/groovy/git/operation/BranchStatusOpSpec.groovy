package org.xbib.groovy.git.operation

import org.xbib.groovy.git.BranchStatus
import org.xbib.groovy.git.Git
import org.xbib.groovy.git.GitTestUtil
import org.xbib.groovy.git.MultiGitOpSpec
import spock.lang.Unroll

class BranchStatusOpSpec extends MultiGitOpSpec {

    Git localGit

    Git remoteGit

    def setup() {
        remoteGit = init('remote')

        repoFile(remoteGit, '1.txt') << '1'
        remoteGit.commit(message: 'do', all: true)

        remoteGit.checkout(branch: 'up-to-date', createBranch: true)

        repoFile(remoteGit, '1.txt') << '2'
        remoteGit.commit(message: 'do', all: true)

        remoteGit.checkout(branch: 'master')
        remoteGit.checkout(branch: 'out-of-date', createBranch: true)

        localGit = clone('local', remoteGit)

        localGit.branch.add(name: 'up-to-date', startPoint: 'origin/up-to-date')
        localGit.branch.add(name: 'out-of-date', startPoint: 'origin/out-of-date')
        localGit.checkout(branch: 'out-of-date')

        repoFile(remoteGit, '1.txt') << '3'
        remoteGit.commit(message: 'do', all: true)

        repoFile(localGit, '1.txt') << '4'
        localGit.commit(message: 'do', all: true)
        repoFile(localGit, '1.txt') << '5'
        localGit.commit(message: 'do', all: true)

        localGit.branch.add(name: 'no-track')

        localGit.fetch()
    }

    def 'branch status on branch that is not tracking fails'() {
        when:
        localGit.branch.status(name: 'no-track')
        then:
        thrown(IllegalStateException)
    }

    @Unroll('branch status on #branch gives correct counts')
    def 'branch status on branch that is tracking gives correct counts'() {
        expect:
        localGit.branch.status(name: branch) == status
        where:
        branch		| status
        'up-to-date'  | new BranchStatus(branch: GitTestUtil.branch('refs/heads/up-to-date', 'refs/remotes/origin/up-to-date'), aheadCount: 0, behindCount: 0)
        'out-of-date' | new BranchStatus(branch: GitTestUtil.branch('refs/heads/out-of-date', 'refs/remotes/origin/out-of-date'), aheadCount: 2, behindCount: 1)
    }
}
