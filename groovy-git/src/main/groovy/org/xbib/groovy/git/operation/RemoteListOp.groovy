package org.xbib.groovy.git.operation

import java.util.concurrent.Callable

import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.Remote
import org.xbib.groovy.git.internal.Operation
import org.xbib.groovy.git.util.GitUtil
import org.eclipse.jgit.transport.RemoteConfig

/**
 * Lists remotes in the repository. Returns a list of {@link Remote}.
 */
@Operation('list')
class RemoteListOp implements Callable<List<Remote>> {
    private final Repository repository

    RemoteListOp(Repository repo) {
        this.repository = repo
    }

    @Override
    List<Remote> call() {
        return RemoteConfig.getAllRemoteConfigs(repository.jgit.repository.config).collect { rc ->
            if (rc.getURIs().size() > 1 || rc.pushURIs.size() > 1) {
                throw new IllegalArgumentException("does not currently support multiple URLs in remote: [uris: ${rc.uris}, pushURIs:${rc.pushURIs}]")
            }
            GitUtil.convertRemote(rc)
        }
    }
}
