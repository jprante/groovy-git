package org.xbib.groovy.git.operation

import org.xbib.groovy.git.SimpleGitOpSpec

class ApplyOpSpec extends SimpleGitOpSpec {

    def 'apply with no patch fails'() {
        when:
        git.apply()
        then:
        thrown(IllegalStateException)
    }

    def 'apply with patch succeeds'() {
        given:
        repoFile('1.txt') << 'something'
        repoFile('2.txt') << 'something else\n'
        git.add(patterns:['.'])
        git.commit(message: 'Test')
        def patch = tempDir.newFile()
        this.class.getResourceAsStream('/org/xbib/groovy/git/operation/sample.patch').withStream { stream ->
            patch << stream
        }
        when:
        git.apply(patch: patch)
        then:
        repoFile('1.txt').text == 'something'
        repoFile('2.txt').text == 'something else\nis being added\n'
        repoFile('3.txt').text == 'some new stuff\n'
    }
}
