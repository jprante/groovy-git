package org.xbib.groovy.git.operation

import org.xbib.groovy.git.BranchStatus
import org.xbib.groovy.git.Git
import org.xbib.groovy.git.GitTestUtil
import org.xbib.groovy.git.MultiGitOpSpec
import spock.lang.Unroll

class BranchStatusOpSpec extends MultiGitOpSpec {
    Git localGrgit
    Git remoteGrgit

    def setup() {
        remoteGrgit = init('remote')

        repoFile(remoteGrgit, '1.txt') << '1'
        remoteGrgit.commit(message: 'do', all: true)

        remoteGrgit.checkout(branch: 'up-to-date', createBranch: true)

        repoFile(remoteGrgit, '1.txt') << '2'
        remoteGrgit.commit(message: 'do', all: true)

        remoteGrgit.checkout(branch: 'master')
        remoteGrgit.checkout(branch: 'out-of-date', createBranch: true)

        localGrgit = clone('local', remoteGrgit)

        localGrgit.branch.add(name: 'up-to-date', startPoint: 'origin/up-to-date')
        localGrgit.branch.add(name: 'out-of-date', startPoint: 'origin/out-of-date')
        localGrgit.checkout(branch: 'out-of-date')

        repoFile(remoteGrgit, '1.txt') << '3'
        remoteGrgit.commit(message: 'do', all: true)

        repoFile(localGrgit, '1.txt') << '4'
        localGrgit.commit(message: 'do', all: true)
        repoFile(localGrgit, '1.txt') << '5'
        localGrgit.commit(message: 'do', all: true)

        localGrgit.branch.add(name: 'no-track')

        localGrgit.fetch()
    }

    def 'branch status on branch that is not tracking fails'() {
        when:
        localGrgit.branch.status(name: 'no-track')
        then:
        thrown(IllegalStateException)
    }

    @Unroll('branch status on #branch gives correct counts')
    def 'branch status on branch that is tracking gives correct counts'() {
        expect:
        localGrgit.branch.status(name: branch) == status
        where:
        branch		| status
        'up-to-date'  | new BranchStatus(branch: GitTestUtil.branch('refs/heads/up-to-date', 'refs/remotes/origin/up-to-date'), aheadCount: 0, behindCount: 0)
        'out-of-date' | new BranchStatus(branch: GitTestUtil.branch('refs/heads/out-of-date', 'refs/remotes/origin/out-of-date'), aheadCount: 2, behindCount: 1)
    }
}
