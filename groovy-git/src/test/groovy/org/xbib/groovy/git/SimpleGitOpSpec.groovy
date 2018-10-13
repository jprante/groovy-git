package org.xbib.groovy.git

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import spock.lang.Specification

class SimpleGitOpSpec extends Specification {

    @Rule TemporaryFolder tempDir = new TemporaryFolder()

    Git git

    Person person = new Person('Jörg Prante', 'joergprante@gmail.com')

    def setup() {
        File repoDir = tempDir.newFolder('repo')
        org.eclipse.jgit.api.Git jgit = org.eclipse.jgit.api.Git.init().setDirectory(repoDir).call()

        // Don't want the user's git config to conflict with test expectations
        jgit.repo.FS.userHome = null

        jgit.repo.config.with {
            setString('user', null, 'name', person.name)
            setString('user', null, 'email', person.email)
            save()
        }
        git = Git.open(dir: repoDir)
    }

    protected File repoFile(String path, boolean makeDirs = true) {
        return GitTestUtil.repoFile(git, path, makeDirs)
    }

    protected File repoDir(String path) {
        return GitTestUtil.repoDir(git, path)
    }
}
