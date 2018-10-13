package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Commit
import org.xbib.groovy.git.CommitDiff
import org.xbib.groovy.git.SimpleGitOpSpec

class ShowOpSpec extends SimpleGitOpSpec {

    def 'can show diffs in commit that added new file'() {
        File fooFile = repoFile("dir1/foo.txt")
        fooFile << "foo!"
        git.add(patterns: ['.'])
        Commit commit = git.commit(message: "Initial commit")

        expect:
        git.show(commit: commit) == new CommitDiff(
                commit: commit,
                added: ['dir1/foo.txt']
        )
    }

    def 'can show diffs in commit that modified existing file'() {
        File fooFile = repoFile("bar.txt")
        fooFile << "bar!"
        git.add(patterns: ['.'])
        git.commit(message: "Initial commit")

        // Change existing file
        fooFile << "monkey!"
        git.add(patterns: ['.'])
        Commit changeCommit = git.commit(message: "Added monkey")

        expect:
        git.show(commit: changeCommit) == new CommitDiff(
                commit: changeCommit,
                modified: ['bar.txt']
        )
    }

    def 'can show diffs in commit that deleted existing file'() {
        File fooFile = repoFile("bar.txt")
        fooFile << "bar!"
        git.add(patterns: ['.'])
        git.commit(message: "Initial commit")

        // Delete existing file
        git.remove(patterns: ['bar.txt'])
        Commit removeCommit = git.commit(message: "Deleted file")

        expect:
        git.show(commit: removeCommit) == new CommitDiff(
                commit: removeCommit,
                removed: ['bar.txt']
        )
    }

    def 'can show diffs in commit with multiple changes'() {
        File animalFile = repoFile("animals.txt")
        animalFile << "giraffe!"
        git.add(patterns: ['.'])
        git.commit(message: "Initial commit")

        // Change existing file
        animalFile << "zebra!"

        // Add new file
        File fishFile = repoFile("salmon.txt")
        fishFile<< "salmon!"
        git.add(patterns: ['.'])
        Commit changeCommit = git.commit(message: "Add fish and update animals with zebra")

        expect:
        git.show(commit: changeCommit) == new CommitDiff(
                commit: changeCommit,
                modified: ['animals.txt'],
                added: ['salmon.txt']
        )
    }

    def 'can show diffs in commit with rename'() {
        given:
        repoFile('elephant.txt') << 'I have tusks.'
        git.add(patterns: ['.'])
        git.commit(message: 'Adding elephant.')

        repoFile('elephant.txt').renameTo(repoFile('mammoth.txt'))
        git.add(patterns: ['.'])
        git.remove(patterns: ['elephant.txt'])
        Commit renameCommit = git.commit(message: 'Renaming to mammoth.')

        expect:
        git.show(commit: renameCommit) == new CommitDiff(
                commit: renameCommit,
                renamed: ['mammoth.txt']
        )
    }

    def 'can show diffs based on rev string'() {
        File fooFile = repoFile("foo.txt")
        fooFile << "foo!"
        git.add(patterns: ['.'])
        Commit commit = git.commit(message: "Initial commit")

        expect:
        git.show(commit: commit.id) == new CommitDiff(
                commit: commit,
                added: ['foo.txt']
        )
    }
}
