package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Git
import org.xbib.groovy.git.Status
import org.xbib.groovy.git.MultiGitOpSpec
import spock.lang.Unroll

class MergeOpSpec extends MultiGitOpSpec {

    Git localGit

    Git remoteGit

    def setup() {
        remoteGit = init('remote')

        repoFile(remoteGit, '1.txt') << '1.1\n'
        remoteGit.add(patterns: ['.'])
        remoteGit.commit(message: '1.1', all: true)
        repoFile(remoteGit, '2.txt') << '2.1\n'
        remoteGit.add(patterns: ['.'])
        remoteGit.commit(message: '2.1', all: true)

        localGit = clone('local', remoteGit)

        remoteGit.checkout(branch: 'ff', createBranch: true)

        repoFile(remoteGit, '1.txt') << '1.2\n'
        remoteGit.commit(message: '1.2', all: true)
        repoFile(remoteGit, '1.txt') << '1.3\n'
        remoteGit.commit(message: '1.3', all: true)

        remoteGit.checkout(branch: 'clean', startPoint: 'master', createBranch: true)

        repoFile(remoteGit, '3.txt') << '3.1\n'
        remoteGit.add(patterns: ['.'])
        remoteGit.commit(message: '3.1', all: true)
        repoFile(remoteGit, '3.txt') << '3.2\n'
        remoteGit.commit(message: '3.2', all: true)

        remoteGit.checkout(branch: 'conflict', startPoint: 'master', createBranch: true)

        repoFile(remoteGit, '2.txt') << '2.2\n'
        remoteGit.commit(message: '2.2', all: true)
        repoFile(remoteGit, '2.txt') << '2.3\n'
        remoteGit.commit(message: '2.3', all: true)

        localGit.checkout(branch: 'merge-test', createBranch: true)

        repoFile(localGit, '2.txt') << '2.a\n'
        localGit.commit(message: '2.a', all: true)
        repoFile(localGit, '2.txt') << '2.b\n'
        localGit.commit(message: '2.b', all: true)

        localGit.fetch()
    }

    @Unroll('merging #head with #mode does a fast-forward merge')
    def 'fast-forward merge happens when expected'() {
        given:
        localGit.checkout(branch: 'master')
        when:
        localGit.merge(head: head, mode: mode)
        then:
        localGit.status().clean
        localGit.head() == remoteGit.resolve.toCommit(head - 'origin/')
        where:
        head		| mode
        'origin/ff' | MergeOp.Mode.DEFAULT
        'origin/ff' | MergeOp.Mode.ONLY_FF
        'origin/ff' | MergeOp.Mode.NO_COMMIT
    }

    @Unroll('merging #head with #mode creates a merge commit')
    def 'merge commits created when expected'() {
        given:
        def oldHead = localGit.head()
        def mergeHead = remoteGit.resolve.toCommit(head - 'origin/')
        when:
        localGit.merge(head: head, mode: mode)
        then:
        localGit.status().clean

        // has a merge commit
        localGit.log {
            includes = ['HEAD']
            excludes = [oldHead.id, mergeHead.id]
        }.size() == 1
        where:
        head		   | mode
        'origin/ff'	| MergeOp.Mode.CREATE_COMMIT
        'origin/clean' | MergeOp.Mode.DEFAULT
        'origin/clean' | MergeOp.Mode.CREATE_COMMIT
    }

    @Unroll('merging #head with #mode merges but leaves them uncommitted')
    def 'merge left uncommitted when expected'() {
        given:
        def oldHead = localGit.head()
        def mergeHead = remoteGit.resolve.toCommit(head - 'origin/')
        when:
        localGit.merge(head: head, mode: mode)
        then:
        localGit.status() == status
        localGit.head() == oldHead
        repoFile(localGit, '.git/MERGE_HEAD').text.trim() == mergeHead.id
        where:
        head		   | mode	  | status
        'origin/clean' | MergeOp.Mode.NO_COMMIT | new Status(staged: [added: ['3.txt']])
    }

    @Unroll('merging #head with #mode squashes changes but leaves them uncommitted')
    def 'squash merge happens when expected'() {
        given:
        def oldHead = localGit.head()
        when:
        localGit.merge(head: head, mode: mode)
        then:
        localGit.status() == status
        localGit.head() == oldHead
        !repoFile(localGit, '.git/MERGE_HEAD').exists()
        where:
        head		   | mode   | status
        'origin/ff'	| MergeOp.Mode.SQUASH | new Status(staged: [modified: ['1.txt']])
        'origin/clean' | MergeOp.Mode.SQUASH | new Status(staged: [added: ['3.txt']])
    }

    @Unroll('merging #head with #mode fails with correct status')
    def 'merge fails as expected'() {
        given:
        def oldHead = localGit.head()
        when:
        localGit.merge(head: head, mode: mode)
        then:
        localGit.head() == oldHead
        localGit.status() == status
        thrown(IllegalStateException)
        where:
        head			  | mode		  | status
        'origin/clean'	| MergeOp.Mode.ONLY_FF	   | new Status()
        'origin/conflict' | MergeOp.Mode.DEFAULT	   | new Status(conflicts: ['2.txt'])
        'origin/conflict' | MergeOp.Mode.ONLY_FF	   | new Status()
        'origin/conflict' | MergeOp.Mode.CREATE_COMMIT | new Status(conflicts: ['2.txt'])
        'origin/conflict' | MergeOp.Mode.SQUASH		| new Status(conflicts: ['2.txt'])
        'origin/conflict' | MergeOp.Mode.NO_COMMIT	 | new Status(conflicts: ['2.txt'])
    }

    def 'merge uses message if supplied'() {
        given:
        def oldHead = localGit.head()
        def mergeHead = remoteGit.resolve.toCommit('clean')
        when:
        localGit.merge(head: 'origin/clean', message: 'Custom message')
        then: 'all changes are committed'
        localGit.status().clean
        and: 'a merge commit was created'
        localGit.log {
            includes = ['HEAD']
            excludes = [oldHead.id, mergeHead.id]
        }.size() == 1
        and: 'the merge commits message is what was passed in'
        localGit.head().shortMessage == 'Custom message'
    }

    def 'merge of a branch includes this in default message'() {
        given:
        def oldHead = localGit.head()
        def mergeHead = remoteGit.resolve.toCommit('clean')
        when:
        localGit.merge(head: 'origin/clean')
        then: 'all changes are committed'
        localGit.status().clean
        and: 'a merge commit was created'
        localGit.log {
            includes = ['HEAD']
            excludes = [oldHead.id, mergeHead.id]
        }.size() == 1
        and: 'the merge commits message mentions branch name'
        localGit.head().shortMessage == 'Merge remote-tracking branch \'origin/clean\' into merge-test'
    }

    def 'merge of a commit includes this in default message'() {
        given:
        def oldHead = localGit.head()
        def mergeHead = remoteGit.resolve.toCommit('clean')
        when:
        localGit.merge(head: mergeHead.id)
        then: 'all changes are committed'
        localGit.status().clean
        and: 'a merge commit was created'
        localGit.log {
            includes = ['HEAD']
            excludes = [oldHead.id, mergeHead.id]
        }.size() == 1
        and: 'the merge commits message mentions commit hash'
        localGit.head().shortMessage == "Merge commit '${mergeHead.id}' into merge-test"
    }
}
