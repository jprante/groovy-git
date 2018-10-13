package org.xbib.groovy.git.operation

import org.xbib.groovy.git.SimpleGitOpSpec

class TagRemoveOpSpec extends SimpleGitOpSpec {

    def setup() {
        repoFile('1.txt') << '1'
        git.commit(message: 'do', all: true)
        git.tag.add(name: 'tag1')

        repoFile('1.txt') << '2'
        git.commit(message: 'do', all: true)
        git.tag.add(name: 'tag2', annotate: false)
    }

    def 'tag remove with empty list does nothing'() {
        expect:
        git.tag.remove() == []
        git.tag.list().collect { it.fullName } == ['refs/tags/tag1', 'refs/tags/tag2']
    }

    def 'tag remove with one tag removes tag'() {
        expect:
        git.tag.remove(names: ['tag2']) == ['refs/tags/tag2']
        git.tag.list().collect { it.fullName } == ['refs/tags/tag1']
    }

    def 'tag remove with multiple tags removes tags'() {
        expect:
        git.tag.remove(names: ['tag2', 'tag1']) as Set == ['refs/tags/tag2', 'refs/tags/tag1'] as Set
        git.tag.list() == []
    }

    def 'tag remove with invalid tags skips invalid and removes others'() {
        expect:
        git.tag.remove(names: ['tag2', 'blah4']) == ['refs/tags/tag2']
        git.tag.list().collect { it.fullName } == ['refs/tags/tag1']
    }
}
