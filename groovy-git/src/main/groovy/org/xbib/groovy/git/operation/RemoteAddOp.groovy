package org.xbib.groovy.git.operation

import java.util.concurrent.Callable
import org.xbib.groovy.git.Remote
import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.internal.Operation
import org.xbib.groovy.git.util.GitUtil
import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.transport.RemoteConfig
import org.eclipse.jgit.transport.URIish

/**
 * Adds a remote to the repository. Returns the newly created {@link Remote}.
 * If remote with given name already exists, this command will fail.
 */
@Operation('add')
class RemoteAddOp implements Callable<Remote> {

    private final Repository repository

    /**
     * Name of the remote.
     */
    String name

    /**
     * URL to fetch from.
     */
    String url

    /**
     * URL to push to.
     */
    String pushUrl

    /**
     * Specs to fetch from the remote.
     */
    List fetchRefSpecs = []

    /**
     * Specs to push to the remote.
     */
    List pushRefSpecs = []

    /**
     * Whether or not pushes will mirror the repository.
     */
    boolean mirror

    RemoteAddOp(Repository repo) {
        this.repository = repo
    }

    @Override
    Remote call() {
        Config config = repository.jgit.repository.config
        if (RemoteConfig.getAllRemoteConfigs(config).find { it.name == name }) {
            throw new IllegalStateException("remote $name already exists")
        }
        def toUri = {
            url -> new URIish(url)
        }
        def toRefSpec = {
            spec -> new RefSpec(spec)
        }
        RemoteConfig remote = new RemoteConfig(config, name)
        if (url) {
            remote.addURI(toUri(url))
        }
        if (pushUrl) {
            remote.addPushURI(toUri(pushUrl))
        }
        remote.fetchRefSpecs = (fetchRefSpecs ?: ["+refs/heads/*:refs/remotes/$name/*"]).collect(toRefSpec)
        remote.pushRefSpecs = pushRefSpecs.collect(toRefSpec)
        remote.mirror = mirror
        remote.update(config)
        config.save()
        return GitUtil.convertRemote(remote)
    }
}
