package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Branch
import org.xbib.groovy.git.GitTestUtil
import org.xbib.groovy.git.SimpleGitOpSpec
import org.eclipse.jgit.api.errors.GitAPIException

import spock.lang.Unroll

class BranchRemoveOpSpec extends SimpleGitOpSpec {

    def setup() {
        repoFile('1.txt') << '1'
        git.commit(message: 'do', all: true)

        git.branch.add(name: 'branch1')

        repoFile('1.txt') << '2'
        git.commit(message: 'do', all: true)

        git.branch.add(name: 'branch2')

        git.checkout(branch: 'branch3', createBranch: true)
        repoFile('1.txt') << '3'
        git.commit(message: 'do', all: true)

        git.checkout(branch: 'master')
    }

    def 'branch remove with empty list does nothing'() {
        expect:
        git.branch.remove() == []
        git.branch.list() == branches('branch1', 'branch2', 'branch3', 'master')
    }

    def 'branch remove with one branch removes branch'() {
        expect:
        git.branch.remove(names: ['branch2']) == ['refs/heads/branch2']
        git.branch.list() == branches('branch1', 'branch3', 'master')
    }

    def 'branch remove with multiple branches remvoes branches'() {
        expect:
        git.branch.remove(names: ['branch2', 'branch1']) == ['refs/heads/branch2', 'refs/heads/branch1']
        git.branch.list() == branches('branch3', 'master')
    }

    def 'branch remove with invalid branches skips invalid and removes others'() {
        expect:
        git.branch.remove(names: ['branch2', 'blah4']) == ['refs/heads/branch2']
        git.branch.list() == branches('branch1', 'branch3', 'master')
    }

    def 'branch remove with unmerged branch and force false fails'() {
        when:
        git.branch.remove(names: ['branch3'])
        then:
        thrown(GitAPIException)
    }

    def 'branch remove with unmerged branch and force true works'() {
        expect:
        git.branch.remove(names: ['branch3'], force: true) == ['refs/heads/branch3']
        git.branch.list() == branches('branch1', 'branch2', 'master')
    }

    @Unroll('branch remove with current branch and force #force fails')
    def 'branch remove with current branch fails, even with force'() {
        when:
        git.branch.remove(names: ['master'], force: force)
        then:
        thrown(GitAPIException)
        where:
        force << [true, false]
    }

    private List<Branch> branches(String... branches) {
        return branches.collect { GitTestUtil.branch("refs/heads/${it}") }
    }
}
