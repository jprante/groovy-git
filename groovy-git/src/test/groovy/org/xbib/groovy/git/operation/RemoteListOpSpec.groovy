package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Git
import org.xbib.groovy.git.Remote
import org.xbib.groovy.git.MultiGitOpSpec

class RemoteListOpSpec extends MultiGitOpSpec {
    def 'will list all remotes'() {
        given:
        Git remoteGrgit = init('remote')

        repoFile(remoteGrgit, '1.txt') << '1'
        remoteGrgit.commit(message: 'do', all: true)

        Git localGrgit = clone('local', remoteGrgit)

        expect:
        localGrgit.remote.list() == [
                new Remote(
                        name: 'origin',
                        url: remoteGrgit.repository.rootDir.canonicalFile.toPath().toUri(),
                        fetchRefSpecs: ['+refs/heads/*:refs/remotes/origin/*'])
        ]
    }
}
