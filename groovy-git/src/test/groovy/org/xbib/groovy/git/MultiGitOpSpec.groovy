package org.xbib.groovy.git

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import spock.lang.Specification

class MultiGitOpSpec extends Specification {

    @Rule TemporaryFolder tempDir = new TemporaryFolder()

    Person person = new Person('Bruce Wayne', 'bruce.wayne@wayneindustries.com')

    protected Git init(String name) {
        File repoDir = tempDir.newFolder(name).canonicalFile
        org.eclipse.jgit.api.Git jgit = org.eclipse.jgit.api.Git.init().setDirectory(repoDir).call()

        // Don't want the user's git config to conflict with test expectations
        jgit.repo.FS.userHome = null

        jgit.repo.config.with {
            setString('user', null, 'name', person.name)
            setString('user', null, 'email', person.email)
            save()
        }
        Git.open(dir: repoDir)
    }

    protected Git clone(String name, Git remote) {
        File repoDir = tempDir.newFolder(name)
        return Git.clone {
            dir = repoDir
            uri = remote.repository.rootDir.toURI()
        }
    }

    protected File repoFile(Git git, String path, boolean makeDirs = true) {
        GitTestUtil.repoFile(git, path, makeDirs)
    }
}
