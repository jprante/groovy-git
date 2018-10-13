package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Git
import org.xbib.groovy.git.GitTestUtil
import org.xbib.groovy.git.MultiGitOpSpec
import org.eclipse.jgit.api.errors.GitAPIException

class CloneOpSpec extends MultiGitOpSpec {
    File repoDir

    Git remoteGit

    String remoteUri

    def remoteBranchesFilter = { it =~ $/^refs/remotes/origin/$ }
    def localBranchesFilter = { it =~ $/^refs/heads/$ }
    def lastName = { it.split('/')[-1] }

    def setup() {
        // TODO: Convert branching and tagging to Grgit.
        repoDir = tempDir.newFolder('local')

        remoteGit = init('remote')
        remoteUri = remoteGit.repository.rootDir.toURI()

        repoFile(remoteGit, '1.txt') << '1'
        remoteGit.commit(message: 'do', all: true)

        remoteGit.branch.add(name: 'branch1')

        repoFile(remoteGit, '1.txt') << '2'
        remoteGit.commit(message: 'do', all: true)

        remoteGit.tag.add(name: 'tag1')

        repoFile(remoteGit, '1.txt') << '3'
        remoteGit.commit(message: 'do', all: true)

        remoteGit.branch.add(name: 'branch2')
    }

    def 'clone with non-existent uri fails'() {
        when:
        Git.clone(dir: repoDir, uri: 'file:///bad/uri')
        then:
        thrown(GitAPIException)
    }

    def 'clone with default settings clones as expected'() {
        when:
        def git = Git.clone(dir: repoDir, uri: remoteUri)
        then:
        git.head() == remoteGit.head()
        GitTestUtil.branches(git).findAll(remoteBranchesFilter).collect(lastName) == GitTestUtil.branches(remoteGit).collect(lastName)
        GitTestUtil.branches(git).findAll(localBranchesFilter).collect(lastName) == ['master']
        GitTestUtil.tags(git).collect(lastName) == ['tag1']
        GitTestUtil.remotes(git) == ['origin']
    }

    def 'clone with different remote does not use origin'() {
        when:
        def git = Git.clone(dir: repoDir, uri: remoteUri, remote: 'oranges')
        then:
        GitTestUtil.remotes(git) == ['oranges']
    }

    def 'clone with bare true does not have a working tree'() {
        when:
        def git = Git.clone(dir: repoDir, uri: remoteUri, bare: true)
        then:
        !repoFile(git, '.', false).listFiles().collect { it.name }.contains('.git')
    }

    def 'clone with checkout false does not check out a working tree'() {
        when:
        def git = Git.clone(dir: repoDir, uri: remoteUri, checkout: false)
        then:
        repoFile(git, '.', false).listFiles().collect { it.name } == ['.git']
    }

    def 'clone with checkout false and refToCheckout set fails'() {
        when:
        def git = Git.clone(dir: repoDir, uri: remoteUri, checkout: false, refToCheckout: 'branch2')
        then:
        thrown(IllegalArgumentException)
    }

    def 'clone with refToCheckout set to simple branch name works'() {
        when:
        def git = Git.clone(dir: repoDir, uri: remoteUri, refToCheckout: 'branch1')
        then:
        git.head() == remoteGit.resolve.toCommit('branch1')
        GitTestUtil.branches(git).findAll(remoteBranchesFilter).collect(lastName) == GitTestUtil.branches(remoteGit).collect(lastName)
        GitTestUtil.branches(git).findAll(localBranchesFilter).collect(lastName) == ['branch1']
        GitTestUtil.tags(git).collect(lastName) == ['tag1']
        GitTestUtil.remotes(git) == ['origin']
    }

    def 'clone with refToCheckout set to simple tag name works'() {
        when:
        def git = Git.clone(dir: repoDir, uri: remoteUri, refToCheckout: 'tag1')
        then:
        git.head() == remoteGit.resolve.toCommit('tag1')
        GitTestUtil.branches(git).findAll(remoteBranchesFilter).collect(lastName) == GitTestUtil.branches(remoteGit).collect(lastName)
        GitTestUtil.branches(git).findAll(localBranchesFilter).collect(lastName) == []
        GitTestUtil.tags(git).collect(lastName) == ['tag1']
        GitTestUtil.remotes(git) == ['origin']
    }

    def 'clone with refToCheckout set to full ref name works'() {
        when:
        def git = Git.clone(dir: repoDir, uri: remoteUri, refToCheckout: 'refs/heads/branch2')
        then:
        git.head() == remoteGit.resolve.toCommit('branch2')
        GitTestUtil.branches(git).findAll(remoteBranchesFilter).collect(lastName) == GitTestUtil.branches(remoteGit).collect(lastName)
        GitTestUtil.branches(git).findAll(localBranchesFilter).collect(lastName) == ['branch2']
        GitTestUtil.tags(git).collect(lastName) == ['tag1']
        GitTestUtil.remotes(git) == ['origin']
    }

    def 'cloned repo can be deleted'() {
        given:
        def git = Git.clone(dir: repoDir, uri: remoteUri, refToCheckout: 'refs/heads/branch2')
        when:
        git.close()
        then:
        repoDir.deleteDir()
    }
}
