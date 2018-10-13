package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Status
import org.xbib.groovy.git.SimpleGitOpSpec

class ResetOpSpec extends SimpleGitOpSpec {

    List commits = []

    def setup() {
        repoFile('1.bat') << '1'
        repoFile('something/2.txt') << '2'
        repoFile('test/3.bat') << '3'
        repoFile('test/4.txt') << '4'
        repoFile('test/other/5.txt') << '5'
        git.add(patterns:['.'])
        commits << git.commit(message: 'Test')
        repoFile('1.bat') << '2'
        repoFile('test/3.bat') << '4'
        git.add(patterns:['.'])
        commits << git.commit(message: 'Test')
        repoFile('1.bat') << '3'
        repoFile('something/2.txt') << '2'
        git.add(patterns:['.'])
        repoFile('test/other/5.txt') << '6'
        repoFile('test/4.txt') << '5'
    }

    def 'reset soft changes HEAD only'() {
        when:
        git.reset(mode:'soft', commit:commits[0].id)
        then:
        commits[0] == git.head()
        git.status() == new Status(
                staged: [modified: ['1.bat', 'test/3.bat', 'something/2.txt']],
                unstaged: [modified: ['test/4.txt', 'test/other/5.txt']]
        )
    }

    def 'reset mixed changes HEAD and index'() {
        when:
        git.reset(mode:'mixed', commit:commits[0].id)
        then:
        commits[0] == git.head()
        git.status() == new Status(
                unstaged: [modified: ['1.bat', 'test/3.bat', 'test/4.txt', 'something/2.txt', 'test/other/5.txt']])
    }

    def 'reset hard changes HEAD, index, and working tree'() {
        when:
        git.reset(mode:'hard', commit:commits[0].id)
        then:
        commits[0] == git.head()
        git.status().clean
    }

    def 'reset with paths changes index only'() {
        when:
        git.reset(paths:['something/2.txt'])
        then:
        commits[1] == git.head()
        git.status() == new Status(
                staged: [modified: ['1.bat']],
                unstaged: [modified: ['test/4.txt', 'something/2.txt', 'test/other/5.txt']]
        )
    }

    def 'reset with paths and mode set not supported'() {
        when:
        git.reset(mode:'hard', paths:['.'])
        then:
        thrown(IllegalStateException)
    }
}
