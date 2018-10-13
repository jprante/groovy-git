package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Status
import org.xbib.groovy.git.SimpleGitOpSpec
import org.eclipse.jgit.api.errors.GitAPIException

class CheckoutOpSpec extends SimpleGitOpSpec {

    def setup() {
        repoFile('1.txt') << '1'
        git.add(patterns: ['1.txt'])
        git.commit(message: 'do')

        repoFile('1.txt') << '2'
        git.add(patterns: ['1.txt'])
        git.commit(message: 'do')

        git.branch.add(name: 'my-branch')

        repoFile('1.txt') << '3'
        git.add(patterns: ['1.txt'])
        git.commit(message: 'do')
    }

    def 'checkout with existing branch and createBranch false works'() {
        when:
        git.checkout(branch: 'my-branch')
        then:
        git.head() == git.resolve.toCommit('my-branch')
        git.branch.getCurrent().fullName == 'refs/heads/my-branch'
        git.log().size() == 2
        repoFile('1.txt').text == '12'
    }

    def 'checkout with existing branch, createBranch true fails'() {
        when:
        git.checkout(branch: 'my-branch', createBranch: true)
        then:
        thrown(GitAPIException)
    }

    def 'checkout with non-existent branch and createBranch false fails'() {
        when:
        git.checkout(branch: 'fake')
        then:
        thrown(GitAPIException)
    }

    def 'checkout with non-existent branch and createBranch true works'() {
        when:
        git.checkout(branch: 'new-branch', createBranch: true)
        then:
        git.branch.getCurrent().fullName == 'refs/heads/new-branch'
        git.head() == git.resolve.toCommit('master')
        git.log().size() == 3
        repoFile('1.txt').text == '123'
    }

    def 'checkout with non-existent branch, createBranch true, and startPoint works'() {
        when:
        git.checkout(branch: 'new-branch', createBranch: true, startPoint: 'my-branch')
        then:
        git.branch.getCurrent().fullName == 'refs/heads/new-branch'
        git.head() == git.resolve.toCommit('my-branch')
        git.log().size() == 2
        repoFile('1.txt').text == '12'
    }

    def 'checkout with no branch name and createBranch true fails'() {
        when:
        git.checkout(createBranch: true)
        then:
        thrown(IllegalArgumentException)
    }

    def 'checkout with existing branch and orphan true fails'() {
        when:
        git.checkout(branch: 'my-branch', orphan: true)
        then:
        thrown(GitAPIException)
    }

    def 'checkout with non-existent branch and orphan true works'() {
        when:
        git.checkout(branch: 'orphan-branch', orphan: true)
        then:
        git.branch.getCurrent().fullName == 'refs/heads/orphan-branch'
        git.status() == new Status(staged: [added: ['1.txt']])
        repoFile('1.txt').text == '123'
    }

    def 'checkout with non-existent branch, orphan true, and startPoint works'() {
        when:
        git.checkout(branch: 'orphan-branch', orphan: true, startPoint: 'my-branch')
        then:
        git.branch.getCurrent().fullName == 'refs/heads/orphan-branch'
        git.status() == new Status(staged: [added: ['1.txt']])
        repoFile('1.txt').text == '12'
    }

    def 'checkout with no branch name and orphan true fails'() {
        when:
        git.checkout(orphan: true)
        then:
        thrown(IllegalArgumentException)
    }
}
