package org.xbib.groovy.git.operation

import org.xbib.groovy.git.SimpleGitOpSpec

class RevertOpSpec extends SimpleGitOpSpec {

    List commits = []

    def setup() {
        5.times {
            repoFile("${it}.txt") << "1"
            git.add(patterns:['.'])
            commits << git.commit(message:'Test', all: true)
        }
    }

    def 'revert with no commits does nothing'() {
        when:
        git.revert()
        then:
        git.log().size() == 5
    }

    def 'revert with commits removes associated changes'() {
        when:
        git.revert(commits:[1, 3].collect { commits[it].id })
        then:
        git.log().size() == 7
        repoFile('.').listFiles().collect { it.name }.findAll { !it.startsWith('.') } as Set == [0, 2, 4].collect { "${it}.txt" } as Set
    }

    def 'revert with conflicts raises exception'() {
        given:
        repoFile("1.txt") << "Edited"
        git.add(patterns:['.'])
        commits << git.commit(message:'Modified', all: true)
        when:
        git.revert(commits:[1, 3].collect { commits[it].id })
        then:
        thrown(IllegalStateException)
        git.log().size() == 6
        git.status().conflicts.containsAll('1.txt')
    }
}
