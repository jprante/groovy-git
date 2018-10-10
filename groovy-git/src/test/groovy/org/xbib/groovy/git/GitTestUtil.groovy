package org.xbib.groovy.git

import org.xbib.groovy.git.util.GitUtil
import org.eclipse.jgit.api.ListBranchCommand.ListMode
import org.eclipse.jgit.transport.RemoteConfig

final class GitTestUtil {

    private GitTestUtil() {
    }

    static File repoFile(Git grgit, String path, boolean makeDirs = true) {
        def file = new File(grgit.repository.rootDir, path)
        if (makeDirs) file.parentFile.mkdirs()
        return file
    }

    static File repoDir(Git grgit, String path) {
        def file = new File(grgit.repository.rootDir, path)
        file.mkdirs()
        return file
    }

    static Branch branch(String fullName, String trackingBranchFullName = null) {
        Branch trackingBranch = trackingBranchFullName ? branch(trackingBranchFullName) : null
        return new Branch(fullName, trackingBranch)
    }

    static List branches(Git grgit, boolean trim = false) {
        return grgit.repository.jgit.branchList().with {
            listMode = ListMode.ALL
            delegate.call()
        }.collect { trim ? it.name - 'refs/heads/' : it.name }
    }

    static List remoteBranches(Git grgit) {
        return grgit.repository.jgit.branchList().with {
            listMode = ListMode.REMOTE
            delegate.call()
        }.collect { it.name - 'refs/remotes/origin/' }
    }

    static List tags(Git grgit) {
        return grgit.repository.jgit.tagList().call().collect {
            it.name - 'refs/tags/'
        }
    }

    static List remotes(Git grgit) {
        def jgitConfig = grgit.repository.jgit.getRepository().config
        return RemoteConfig.getAllRemoteConfigs(jgitConfig).collect { it.name}
    }

    static Commit resolve(Git grgit, String revstr) {
        return GitUtil.resolveCommit(grgit.repository, revstr)
    }

    static void configure(Git grgit, Closure closure) {
        def config = grgit.repository.jgit.getRepository().config
        config.with(closure)
        config.save()
    }
}
