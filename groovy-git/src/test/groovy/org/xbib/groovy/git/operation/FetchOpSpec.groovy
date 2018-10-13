package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Git
import org.xbib.groovy.git.GitTestUtil
import org.xbib.groovy.git.MultiGitOpSpec
import org.eclipse.jgit.api.errors.GitAPIException

import spock.lang.Unroll

class FetchOpSpec extends MultiGitOpSpec {

    Git localGit

    Git remoteGit

    def setup() {
        // TODO: convert after branch and tag available
        remoteGit = init('remote')

        repoFile(remoteGit, '1.txt') << '1'
        remoteGit.commit(message: 'do', all: true)

        remoteGit.branch.add(name: 'my-branch')

        localGit = clone('local', remoteGit)

        repoFile(remoteGit, '1.txt') << '2'
        remoteGit.commit(message: 'do', all: true)

        remoteGit.tag.add(name: 'reachable-tag')
        remoteGit.branch.add(name: 'sub/mine1')

        remoteGit.checkout {
            branch = 'unreachable-branch'
            createBranch = true
        }

        repoFile(remoteGit, '1.txt') << '2.5'
        remoteGit.commit(message: 'do-unreachable', all: true)

        remoteGit.tag.add(name: 'unreachable-tag')

        remoteGit.checkout(branch: 'master')

        repoFile(remoteGit, '1.txt') << '3'
        remoteGit.commit(message: 'do', all: true)

        remoteGit.branch.add(name: 'sub/mine2')
        remoteGit.branch.remove(names: ['my-branch', 'unreachable-branch'], force: true)
    }

    def 'fetch from non-existent remote fails'() {
        when:
        localGit.fetch(remote: 'fake')
        then:
        thrown(GitAPIException)
    }

    def 'fetch without other settings, brings down correct commits'() {
        given:
        def remoteHead = remoteGit.log(maxCommits: 1).find()
        def localHead = { -> GitTestUtil.resolve(localGit, 'refs/remotes/origin/master') }
        assert localHead() != remoteHead
        when:
        localGit.fetch()
        then:
        localHead() == remoteHead
    }

    def 'fetch with prune true, removes refs deleted in the remote'() {
        given:
        assert GitTestUtil.remoteBranches(localGit) - GitTestUtil.branches(remoteGit, true)
        when:
        localGit.fetch(prune: true)
        then:
        GitTestUtil.remoteBranches(localGit) == GitTestUtil.branches(remoteGit, true)
    }

    @Unroll('fetch with tag mode #mode fetches #expectedTags')
    def 'fetch with different tag modes behave as expected'() {
        given:
        assert !GitTestUtil.tags(localGit)
        when:
        localGit.fetch(tagMode: mode)
        then:
        assert GitTestUtil.tags(localGit) == expectedTags
        where:
        mode         | expectedTags
        'none' | []
        'auto' | ['reachable-tag']
        'all'  | ['reachable-tag', 'unreachable-tag']
    }

    def 'fetch with refspecs fetches those branches'() {
        given:
        assert GitTestUtil.branches(localGit) == [
                'refs/heads/master',
                'refs/remotes/origin/master',
                'refs/remotes/origin/my-branch']
        when:
        localGit.fetch(refSpecs: ['+refs/heads/sub/*:refs/remotes/origin/banana/*'])
        then:
        GitTestUtil.branches(localGit) == [
                'refs/heads/master',
                'refs/remotes/origin/banana/mine1',
                'refs/remotes/origin/banana/mine2',
                'refs/remotes/origin/master',
                'refs/remotes/origin/my-branch']
    }
}
