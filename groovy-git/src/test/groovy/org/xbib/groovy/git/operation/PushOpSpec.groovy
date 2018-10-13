package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Git
import org.xbib.groovy.git.PushException
import org.xbib.groovy.git.GitTestUtil
import org.xbib.groovy.git.MultiGitOpSpec
import org.eclipse.jgit.api.errors.GitAPIException

class PushOpSpec extends MultiGitOpSpec {

    Git localGit

    Git remoteGit

    def setup() {
        remoteGit = init('remote')

        repoFile(remoteGit, '1.txt') << '1'
        remoteGit.commit(message: 'do', all: true)

        remoteGit.branch.add(name: 'my-branch')

        remoteGit.checkout(branch: 'some-branch', createBranch: true)
        repoFile(remoteGit, '1.txt') << '1.5.1'
        remoteGit.commit(message: 'do', all: true)
        remoteGit.checkout(branch: 'master')

        localGit = clone('local', remoteGit)
        localGit.checkout(branch: 'my-branch', createBranch: true)

        repoFile(localGit, '1.txt') << '1.5'
        localGit.commit(message: 'do', all: true)

        localGit.tag.add(name: 'tag1')

        localGit.checkout(branch: 'master')

        repoFile(localGit, '1.txt') << '2'
        localGit.commit(message: 'do', all: true)

        localGit.tag.add(name: 'tag2')
    }

    def 'push to non-existent remote fails'() {
        when:
        localGit.push(remote: 'fake')
        then:
        thrown(GitAPIException)
    }

    def 'push without other settings pushes correct commits'() {
        when:
        localGit.push()
        then:
        GitTestUtil.resolve(localGit, 'refs/heads/master') == GitTestUtil.resolve(remoteGit, 'refs/heads/master')
        GitTestUtil.resolve(localGit, 'refs/heads/my-branch') != GitTestUtil.resolve(remoteGit, 'refs/heads/my-branch')
        !GitTestUtil.tags(remoteGit)
    }

    def 'push with all true pushes all branches'() {
        when:
        localGit.push(all: true)
        then:
        GitTestUtil.resolve(localGit, 'refs/heads/master') == GitTestUtil.resolve(remoteGit, 'refs/heads/master')
        GitTestUtil.resolve(localGit, 'refs/heads/my-branch') == GitTestUtil.resolve(remoteGit, 'refs/heads/my-branch')
        !GitTestUtil.tags(remoteGit)
    }

    def 'push with tags true pushes all tags'() {
        when:
        localGit.push(tags: true)
        then:
        GitTestUtil.resolve(localGit, 'refs/heads/master') != GitTestUtil.resolve(remoteGit, 'refs/heads/master')
        GitTestUtil.resolve(localGit, 'refs/heads/my-branch') != GitTestUtil.resolve(remoteGit, 'refs/heads/my-branch')
        GitTestUtil.tags(localGit) == GitTestUtil.tags(remoteGit)
    }

    def 'push with refs only pushes those refs'() {
        when:
        localGit.push(refsOrSpecs: ['my-branch'])
        then:
        GitTestUtil.resolve(localGit, 'refs/heads/master') != GitTestUtil.resolve(remoteGit, 'refs/heads/master')
        GitTestUtil.resolve(localGit, 'refs/heads/my-branch') == GitTestUtil.resolve(remoteGit, 'refs/heads/my-branch')
        !GitTestUtil.tags(remoteGit)
    }

    def 'push with refSpecs only pushes those refs'() {
        when:
        localGit.push(refsOrSpecs: ['+refs/heads/my-branch:refs/heads/other-branch'])
        then:
        GitTestUtil.resolve(localGit, 'refs/heads/master') != GitTestUtil.resolve(remoteGit, 'refs/heads/master')
        GitTestUtil.resolve(localGit, 'refs/heads/my-branch') != GitTestUtil.resolve(remoteGit, 'refs/heads/my-branch')
        GitTestUtil.resolve(localGit, 'refs/heads/my-branch') == GitTestUtil.resolve(remoteGit, 'refs/heads/other-branch')
        !GitTestUtil.tags(remoteGit)
    }

    def 'push with non-fastforward fails'() {
        when:
        localGit.push(refsOrSpecs: ['refs/heads/master:refs/heads/some-branch'])
        then:
        GitTestUtil.resolve(localGit, 'refs/heads/master') != GitTestUtil.resolve(remoteGit, 'refs/heads/some-branch')
        thrown(PushException)
    }

    def 'push in dryRun mode does not push commits'() {
        given:
        def remoteMasterHead = GitTestUtil.resolve(remoteGit, 'refs/heads/master')
        when:
        localGit.push(dryRun: true)
        then:
        GitTestUtil.resolve(localGit, 'refs/heads/master') != GitTestUtil.resolve(remoteGit, 'refs/heads/master')
        GitTestUtil.resolve(remoteGit, 'refs/heads/master') == remoteMasterHead
    }

    def 'push in dryRun mode does not push tags'() {
        given:
        def remoteMasterHead = GitTestUtil.resolve(remoteGit, 'refs/heads/master')
        when:
        localGit.push(dryRun: true, tags: true)
        then:
        !GitTestUtil.tags(remoteGit)
    }
}
