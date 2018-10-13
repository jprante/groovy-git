package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Git
import org.xbib.groovy.git.GitTestUtil

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import spock.lang.Specification

class InitOpSpec extends Specification {

    @Rule TemporaryFolder tempDir = new TemporaryFolder()

    File repoDir

    def setup() {
        repoDir = tempDir.newFolder('repo')
    }

    def 'init with bare true does not have a working tree'() {
        when:
        def grgit = Git.init(dir: repoDir, bare: true)
        then:
        !GitTestUtil.repoFile(grgit, '.', false).listFiles().collect { it.name }.contains('.git')
    }

    def 'init with bare false has a working tree'() {
        when:
        def grgit = Git.init(dir: repoDir, bare: false)
        then:
        GitTestUtil.repoFile(grgit, '.', false).listFiles().collect { it.name } == ['.git']
    }

    def 'init repo can be deleted after being closed'() {
        given:
        def grgit = Git.init(dir: repoDir, bare: false)
        when:
        grgit.close()
        then:
        repoDir.deleteDir()
    }
}
