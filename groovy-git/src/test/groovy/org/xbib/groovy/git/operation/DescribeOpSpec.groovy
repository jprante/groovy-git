package org.xbib.groovy.git.operation

import org.xbib.groovy.git.SimpleGitOpSpec

class DescribeOpSpec extends SimpleGitOpSpec {

    def setup() {
        git.commit(message:'initial commit')
        git.tag.add(name:'initial')
        git.commit(message:'another commit')
        git.tag.add(name:'another')
        git.commit(message:'other commit')
        git.tag.add(name:'other', annotate: false)
    }

    def 'with tag'() {
        given:
        git.reset(commit: 'HEAD~1', mode: 'hard')
        expect:
        git.describe() == 'another'
    }

    def 'with additional commit'(){
        given:
        repoFile('1.txt') << '1'
        git.add(patterns:['1.txt'])
        git.commit(message: 'another commit')
        expect:
        git.describe().startsWith('another-2-')
    }

    def 'from different commit'(){
        given:
        repoFile('1.txt') << '1'
        git.add(patterns:['1.txt'])
        git.commit(message:  'another commit')
        expect:
        git.describe(commit: 'HEAD~3') == 'initial'
    }

    def 'with long description'() {
        expect:
        git.describe(longDescr: true).startsWith('another-1-')
    }

    def 'with un-annotated tags'() {
        expect:
        git.describe(tags: true) == 'other'
    }

    def 'with match'() {
        expect:
        git.describe(match: ['initial*']).startsWith('initial-2-')
    }
}
