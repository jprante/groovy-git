package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Git
import org.xbib.groovy.git.Remote
import org.xbib.groovy.git.MultiGitOpSpec

class RemoteListOpSpec extends MultiGitOpSpec {

    def 'will list all remotes'() {
        given:
        Git remoteGit = init('remote')

        repoFile(remoteGit, '1.txt') << '1'
        remoteGit.commit(message: 'do', all: true)

        Git localGrgit = clone('local', remoteGit)

        expect:
        localGrgit.remote.list() == [
                new Remote(
                        name: 'origin',
                        url: remoteGit.repository.rootDir.canonicalFile.toPath().toUri(),
                        fetchRefSpecs: ['+refs/heads/*:refs/remotes/origin/*'])
        ]
    }
}
