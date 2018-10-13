package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Status
import org.xbib.groovy.git.SimpleGitOpSpec

class StatusOpSpec extends SimpleGitOpSpec {
    def setup() {
        4.times { repoFile("${it}.txt") << "1" }
        git.add(patterns: ['.'])
        git.commit(message: 'Test')
        git.checkout(branch: 'conflict', createBranch: true)
        repoFile('1.txt') << '2'
        git.add(patterns: ['.'])
        git.commit(message: 'conflicting change')
        git.checkout(branch: 'master')
        repoFile('1.txt') << '3'
        git.add(patterns: ['.'])
        git.commit(message: 'other change')
    }

    def 'with no changes all methods return empty list'() {
        expect:
        git.status() == new Status()
    }

    def 'new unstaged file detected'() {
        given:
        repoFile('5.txt') << '5'
        repoFile('6.txt') << '6'
        expect:
        git.status() == new Status(unstaged: [added: ['5.txt', '6.txt']])
    }

    def 'unstaged modified files detected'() {
        given:
        repoFile('2.txt') << '2'
        repoFile('3.txt') << '3'
        expect:
        git.status() == new Status(unstaged: [modified: ['2.txt', '3.txt']])
    }

    def 'unstaged deleted files detected'() {
        given:
        assert repoFile('1.txt').delete()
        assert repoFile('2.txt').delete()
        expect:
        git.status() == new Status(unstaged: [removed: ['1.txt', '2.txt']])
    }

    def 'staged new files detected'() {
        given:
        repoFile('5.txt') << '5'
        repoFile('6.txt') << '6'
        when:
        git.add(patterns: ['.'])
        then:
        git.status() == new Status(staged: [added: ['5.txt', '6.txt']])
    }

    def 'staged modified files detected'() {
        given:
        repoFile('1.txt') << '5'
        repoFile('2.txt') << '6'
        when:
        git.add(patterns: ['.'])
        then:
        git.status() == new Status(staged: [modified: ['1.txt', '2.txt']])
    }

    def 'staged removed files detected'() {
        given:
        assert repoFile('3.txt').delete()
        assert repoFile('0.txt').delete()
        when:
        git.add(patterns: ['.'], update: true)
        then:
        git.status() == new Status(staged: [removed: ['3.txt', '0.txt']])
    }

    def 'conflict files detected'() {
        when:
        git.merge(head: 'conflict')
        then:
        git.status() == new Status(conflicts: ['1.txt'])
        thrown(IllegalStateException)
    }
}
