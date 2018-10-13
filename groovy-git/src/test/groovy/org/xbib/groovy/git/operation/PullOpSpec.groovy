package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Commit
import org.xbib.groovy.git.Git
import org.xbib.groovy.git.Status
import org.xbib.groovy.git.MultiGitOpSpec

class PullOpSpec extends MultiGitOpSpec {

    Git localGit

    Git remoteGit

    Git otherRemoteGit

    Commit ancestorHead

    def setup() {
        remoteGit = init('remote')

        repoFile(remoteGit, '1.txt') << '1.1\n'
        remoteGit.add(patterns: ['.'])
        ancestorHead = remoteGit.commit(message: '1.1', all: true)

        remoteGit.branch.add(name: 'test-branch')

        localGit = clone('local', remoteGit)
        localGit.branch.add(name: 'test-branch', startPoint: 'origin/test-branch')

        otherRemoteGit = clone('remote2', remoteGit)
        repoFile(otherRemoteGit, '4.txt') << '4.1\n'
        otherRemoteGit.add(patterns: ['.'])
        otherRemoteGit.commit(message: '4.1', all: true)

        repoFile(remoteGit, '1.txt') << '1.2\n'
        remoteGit.commit(message: '1.2', all: true)
        repoFile(remoteGit, '1.txt') << '1.3\n'
        remoteGit.commit(message: '1.3', all: true)

        remoteGit.checkout(branch: 'test-branch')

        repoFile(remoteGit, '2.txt') << '2.1\n'
        remoteGit.add(patterns: ['.'])
        remoteGit.commit(message: '2.1', all: true)
        repoFile(remoteGit, '2.txt') << '2.2\n'
        remoteGit.commit(message: '2.2', all: true)
    }

    def 'pull to local repo with no changes fast-forwards current branch only'() {
        given:
        def localTestBranchHead = localGit.resolve.toCommit('test-branch')
        when:
        localGit.pull()
        then:
        localGit.head() == remoteGit.resolve.toCommit('master')
        localGit.resolve.toCommit('test-branch') == localTestBranchHead
    }

    def 'pull to local repo with clean changes merges branches from origin'() {
        given:
        repoFile(localGit, '3.txt') << '3.1\n'
        localGit.add(patterns: ['.'])
        localGit.commit(message: '3.1')
        def localHead = localGit.head()
        def remoteHead = remoteGit.resolve.toCommit('master')
        when:
        localGit.pull()
        then:
        // includes all commits from remote
        (remoteGit.log(includes: ['master']) - localGit.log()).size() == 0
        /*
         * Go back to one pass log command when bug is fixed:
         * https://bugs.eclipse.org/bugs/show_bug.cgi?id=439675
         */
        // localGrgit.log {
        //	 includes = [remoteHead.id]
        //	 excludes = ['HEAD']
        // }.size() == 0

        // has merge commit
        localGit.log {
            includes = ['HEAD']
            excludes = [localHead.id, remoteHead.id]
        }.size() == 1
    }

    def 'pull to local repo with conflicting changes fails'() {
        given:
        repoFile(localGit, '1.txt') << '1.4\n'
        localGit.commit(message: '1.4', all: true)
        def localHead = localGit.head()
        when:
        localGit.pull()
        then:
        localGit.status() == new Status(conflicts: ['1.txt'])
        localGit.head() == localHead
        thrown(IllegalStateException)
    }

    def 'pull to local repo with clean changes and rebase rebases changes on top of origin'() {
        given:
        repoFile(localGit, '3.txt') << '3.1\n'
        localGit.add(patterns: ['.'])
        localGit.commit(message: '3.1')
        def localHead = localGit.head()
        def remoteHead = remoteGit.resolve.toCommit('master')
        def localCommits = localGit.log {
            includes = [localHead.id]
            excludes = [ancestorHead.id]
        }
        when:
        localGit.pull(rebase: true)
        then:
        // includes all commits from remote
        localGit.log {
            includes = [remoteHead.id]
            excludes = ['HEAD']
        }.size() == 0

        // includes none of local commits
        localGit.log {
            includes = [localHead.id]
            excludes = ['HEAD']
        } == localCommits

        // has commit comments from local
        localGit.log {
            includes = ['HEAD']
            excludes = [remoteHead.id]
        }.collect {
            it.fullMessage
        } == localCommits.collect {
            it.fullMessage
        }

        // has state of all changes
        repoFile(localGit, '1.txt').text.normalize() == '1.1\n1.2\n1.3\n'
        repoFile(localGit, '3.txt').text.normalize() == '3.1\n'
    }

    def 'pull to local repo from other remote fast-forwards current branch'() {
        given:
        def otherRemoteUri = otherRemoteGit.repository.rootDir.toURI().toString()
        localGit.remote.add(name: 'other-remote', url: otherRemoteUri)
        when:
        localGit.pull(remote: 'other-remote')
        then:
        localGit.head() == otherRemoteGit.head()
    }

    def 'pull to local repo from specific remote branch merges changes'() {
        given:

        when:
        localGit.pull(branch: 'test-branch')
        then:
        (remoteGit.log(includes: ['test-branch']) - localGit.log()).size() == 0
    }
}
