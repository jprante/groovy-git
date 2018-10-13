package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Remote
import org.xbib.groovy.git.SimpleGitOpSpec

class RemoteAddOpSpec extends SimpleGitOpSpec {

    def 'remote with given name and push/fetch urls is added'() {
        given:
        Remote remote = new Remote(
                name: 'newRemote',
                url: 'http://fetch.url/',
                fetchRefSpecs: ['+refs/heads/*:refs/remotes/newRemote/*'])
        expect:
        remote == git.remote.add(name: 'newRemote', url: 'http://fetch.url/')
        [remote] == git.remote.list()
    }
}
