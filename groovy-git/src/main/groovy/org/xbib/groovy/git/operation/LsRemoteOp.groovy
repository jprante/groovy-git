package org.xbib.groovy.git.operation

import java.util.concurrent.Callable
import org.xbib.groovy.git.Ref
import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.auth.TransportOpUtil
import org.xbib.groovy.git.internal.Operation
import org.eclipse.jgit.api.LsRemoteCommand
import org.eclipse.jgit.lib.ObjectId

/**
 * List references in a remote repository.
 */
@Operation('lsremote')
class LsRemoteOp implements Callable<Map<Ref, String>> {
    private final Repository repo

    String remote = 'origin'

    boolean heads = false

    boolean tags = false

    LsRemoteOp(Repository repo) {
        this.repo = repo
    }

    Map<Ref, String> call() {
        LsRemoteCommand cmd = repo.jgit.lsRemote()
        TransportOpUtil.configure(cmd, repo.credentials)
        cmd.remote = remote
        cmd.heads = heads
        cmd.tags = tags
        return cmd.call().collectEntries { jgitRef ->
            Ref ref = new Ref(jgitRef.getName())
            [(ref): ObjectId.toString(jgitRef.getObjectId())]
        }.asImmutable()
    }
}
