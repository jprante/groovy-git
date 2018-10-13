package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Git
import org.xbib.groovy.git.Ref
import org.xbib.groovy.git.MultiGitOpSpec
import org.eclipse.jgit.api.errors.GitAPIException

class LsRemoteOpSpec extends MultiGitOpSpec {

    Git localGit

    Git remoteGit

    List branches = []

    List tags = []

    def setup() {
        remoteGit = init('remote')

        branches << remoteGit.branch.current

        repoFile(remoteGit, '1.txt') << '1'
        remoteGit.commit(message: 'do', all: true)

        branches << remoteGit.branch.add(name: 'my-branch')

        localGit = clone('local', remoteGit)

        repoFile(remoteGit, '1.txt') << '2'
        remoteGit.commit(message: 'do', all: true)

        tags << remoteGit.tag.add(name: 'reachable-tag')
        branches << remoteGit.branch.add(name: 'sub/mine1')

        remoteGit.checkout {
            branch = 'unreachable-branch'
            createBranch = true
        }
        branches << remoteGit.branch.list().find { it.name == 'unreachable-branch' }

        repoFile(remoteGit, '1.txt') << '2.5'
        remoteGit.commit(message: 'do-unreachable', all: true)

        tags << remoteGit.tag.add(name: 'unreachable-tag')

        remoteGit.checkout(branch: 'master')

        repoFile(remoteGit, '1.txt') << '3'
        remoteGit.commit(message: 'do', all: true)

        branches << remoteGit.branch.add(name: 'sub/mine2')

        println remoteGit.branch.list()
        println remoteGit.tag.list()
    }

    def 'lsremote from non-existent remote fails'() {
        when:
        localGit.lsremote(remote: 'fake')
        then:
        thrown(GitAPIException)
    }

    def 'lsremote returns all refs'() {
        expect:
        localGit.lsremote() == format([new Ref('HEAD'), branches, tags].flatten())
    }

    def 'lsremote returns branches and tags'() {
        expect:
        localGit.lsremote(heads: true, tags: true) == format([branches, tags].flatten())
    }

    def 'lsremote returns only branches'() {
        expect:
        localGit.lsremote(heads: true) == format(branches)
    }

    def 'lsremote returns only tags'() {
        expect:
        localGit.lsremote(tags: true) == format(tags)
    }

    private Map format(things) {
        return things.collectEntries { refish ->
            Ref ref = new Ref(refish.fullName)
            [(ref): remoteGit.resolve.toObjectId(refish)]
        }
    }
}
