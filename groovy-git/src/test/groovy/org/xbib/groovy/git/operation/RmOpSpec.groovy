package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Status
import org.xbib.groovy.git.SimpleGitOpSpec

class RmOpSpec extends SimpleGitOpSpec {

    def setup() {
        repoFile('1.bat') << '1'
        repoFile('something/2.txt') << '2'
        repoFile('test/3.bat') << '3'
        repoFile('test/4.txt') << '4'
        repoFile('test/other/5.txt') << '5'
        git.add(patterns:['.'])
        git.commit(message: 'Test')
    }

    def 'removing specific file only removes that file'() {
        given:
        def paths = ['1.bat'] as Set
        when:
        git.remove(patterns:['1.bat'])
        then:
        git.status() == new Status(staged: [removed: paths])
        paths.every { !repoFile(it).exists() }
    }

    def 'removing specific directory removes all files within it'() {
        given:
        def paths = ['test/3.bat', 'test/4.txt', 'test/other/5.txt'] as Set
        when:
        git.remove(patterns:['test'])
        then:
        git.status() == new Status(staged: [removed: paths])
        paths.every { !repoFile(it).exists() }
    }

    def 'removing file pattern does not work due to lack of JGit support'() {
        given:
        def paths = ['1.bat', 'something/2.txt', 'test/3.bat', 'test/4.txt', 'test/other/5.txt'] as Set
        when:
        git.remove(patterns:['**/*.txt'])
        then:
        git.status().clean
        /*
         * TODO: get it to work like this
         * status.removed == ['something/2.txt', 'test/4.txt', 'test/other/5.txt'] as Set
         */
        paths.every { repoFile(it).exists() }
    }

    def 'removing with cached true only removes files from index'() {
        given:
        def paths = ['something/2.txt'] as Set
        when:
        git.remove(patterns:['something'], cached:true)
        then:
        git.status() == new Status(staged: [removed: paths], unstaged: [added: paths])
        paths.every { repoFile(it).exists() }
    }
}
