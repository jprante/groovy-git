package org.xbib.groovy.git.operation

import org.xbib.groovy.git.SimpleGitOpSpec
import org.xbib.groovy.git.util.GitUtil

import org.eclipse.jgit.merge.MergeStrategy

class LogOpSpec extends SimpleGitOpSpec {
    List commits = []

    def intToCommit = { commits[it] }

    def setup() {
        File testFile1 = repoFile('1.txt')
        File testFile2 = repoFile('2.txt')

        testFile1 << '1'
        testFile2 << '2.1'
        git.add(patterns: ['.'])
        commits << git.commit(message: 'first commit\ntesting')

        testFile1 << '2'
        git.add(patterns: ['.'])
        commits << git.commit(message: 'second commit')
        git.tag.add(name: 'v1.0.0', message: 'annotated tag')

        git.checkout(branch: intToCommit(0).id)
        testFile1 << '3'
        git.add(patterns: ['.'])
        commits << git.commit(message: 'third commit')

        git.checkout(branch: 'master')
        def jgitId = GitUtil.resolveObject(git.repository, commits[2].id)
        def mergeCommit = git.repository.jgit.merge().include(jgitId).setStrategy(MergeStrategy.OURS).call().newHead
        commits << GitUtil.convertCommit(git.repository, mergeCommit)

        testFile1 << '4'
        git.add(patterns: ['.'])
        commits << git.commit(message: 'fifth commit')

        testFile2 << '2.2'
        git.add(patterns: ['.'])
        commits << git.commit(message: 'sixth commit')
    }

    def 'log with no arguments returns all commits'() {
        expect:
        git.log() == [5, 4, 3, 1, 2, 0].collect(intToCommit)
    }

    def 'log with max commits returns that number of commits'() {
        expect:
        git.log(maxCommits:2) == [5, 4].collect(intToCommit)
    }

    def 'log with skip commits does not return the first x commits'() {
        expect:
        git.log(skipCommits:2) == [3, 1, 2, 0].collect(intToCommit)
    }

    def 'log with range returns only the commits in that range'() {
        expect:
        git.log {
            range intToCommit(2).id, intToCommit(4).id
        } == [4, 3, 1].collect(intToCommit)
    }

    def 'log with path includes only commits with changes for that path'() {
        expect:
        git.log(paths:['2.txt']).collect { it.id } == [5, 0].collect(intToCommit).collect { it.id }
    }

    def 'log with annotated tag short name works'() {
        expect:
        git.log(includes: ['v1.0.0']) == [1, 0].collect(intToCommit)
    }
}
