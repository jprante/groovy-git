package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Status
import org.xbib.groovy.git.SimpleGitOpSpec

class AddOpSpec extends SimpleGitOpSpec {

    def 'adding specific file only adds that file'() {
        given:
        repoFile('1.txt') << '1'
        repoFile('2.txt') << '2'
        repoFile('test/3.txt') << '3'
        when:
        git.add(patterns:['1.txt'])
        then:
        git.status() == new Status(
                staged: [added: ['1.txt']],
                unstaged: [added: ['2.txt', 'test/3.txt']]
        )
    }

    def 'adding specific directory adds all files within it'() {
        given:
        repoFile('1.txt') << '1'
        repoFile('something/2.txt') << '2'
        repoFile('test/3.txt') << '3'
        repoFile('test/4.txt') << '4'
        repoFile('test/other/5.txt') << '5'
        when:
        git.add(patterns:['test'])
        then:
        git.status() == new Status(
                staged: [added: ['test/3.txt', 'test/4.txt', 'test/other/5.txt']],
                unstaged: [added: ['1.txt', 'something/2.txt']]
        )
    }

    def 'adding file pattern does not work due to lack of JGit support'() {
        given:
        repoFile('1.bat') << '1'
        repoFile('something/2.txt') << '2'
        repoFile('test/3.bat') << '3'
        repoFile('test/4.txt') << '4'
        repoFile('test/other/5.txt') << '5'
        when:
        git.add(patterns:['**/*.txt'])
        then:
        git.status() == new Status(
                unstaged: [added: ['1.bat', 'test/3.bat', 'something/2.txt', 'test/4.txt', 'test/other/5.txt']]
        )
        /*
         * TODO: get it to work like this
         * status.added == ['something/2.txt', 'test/4.txt', 'test/other/5.txt'] as Set
         * status.untracked == ['1.bat', 'test/3.bat'] as Set
         */
    }

    def 'adding with update true only adds/removes files already in the index'() {
        given:
        repoFile('1.bat') << '1'
        repoFile('something/2.txt') << '2'
        repoFile('test/3.bat') << '3'
        git.add(patterns:['.'])
        git.repository.jgit.commit().setMessage('Test').call()
        repoFile('1.bat') << '1'
        repoFile('something/2.txt') << '2'
        assert repoFile('test/3.bat').delete()
        repoFile('test/4.txt') << '4'
        repoFile('test/other/5.txt') << '5'
        when:
        git.add(patterns:['.'], update:true)
        then:
        git.status() == new Status(
                staged: [modified: ['1.bat', 'something/2.txt'], removed: ['test/3.bat']],
                unstaged: [added: ['test/4.txt', 'test/other/5.txt']]
        )
    }
}
