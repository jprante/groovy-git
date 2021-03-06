package org.xbib.groovy.git.operation

import org.xbib.groovy.git.SimpleGitOpSpec

class TagListOpSpec extends SimpleGitOpSpec {
    List commits = []
    List tags = []

    def setup() {
        repoFile('1.txt') << '1'
        commits << git.commit(message: 'do', all: true)
        tags << git.tag.add(name: 'tag1', message: 'My message')

        repoFile('1.txt') << '2'
        commits << git.commit(message: 'do', all: true)
        tags << git.tag.add(name: 'tag2', message: 'My other\nmessage')

        tags << git.tag.add(name: 'tag3', message: 'My next message.', pointsTo: 'tag1')
    }

    def 'tag list lists all tags'() {
        expect:
        git.tag.list() == tags
    }
}
