package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Person
import org.xbib.groovy.git.Status
import org.xbib.groovy.git.GitTestUtil
import org.xbib.groovy.git.SimpleGitOpSpec

class CommitOpSpec extends SimpleGitOpSpec {

    def setup() {
        GitTestUtil.configure(git) {
            setString('user', null, 'name', 'Alfred Pennyworth')
            setString('user', null, 'email', 'alfred.pennyworth@wayneindustries.com')
        }

        repoFile('1.txt') << '1'
        repoFile('2.txt') << '1'
        repoFile('folderA/1.txt') << '1'
        repoFile('folderA/2.txt') << '1'
        repoFile('folderB/1.txt') << '1'
        repoFile('folderC/1.txt') << '1'
        git.add(patterns:['.'])
        git.commit(message: 'Test')
        repoFile('1.txt') << '2'
        repoFile('folderA/1.txt') << '2'
        repoFile('folderA/2.txt') << '2'
        repoFile('folderB/1.txt') << '2'
        repoFile('folderB/2.txt') << '2'
    }

    def 'commit with all false commits changes from index'() {
        given:
        git.add(patterns:['folderA'])
        when:
        git.commit(message:'Test2')
        then:
        git.log().size() == 2
        git.status() == new Status(
                unstaged: [added: ['folderB/2.txt'], modified: ['1.txt', 'folderB/1.txt']])
    }

    def 'commit with all true commits changes in previously tracked files'() {
        when:
        git.commit(message:'Test2', all: true)
        then:
        git.log().size() == 2
        git.status() == new Status(
                unstaged: [added: ['folderB/2.txt']])
    }

    def 'commit amend changes the previous commit'() {
        given:
        git.add(patterns:['folderA'])
        when:
        git.commit(message:'Test2', amend: true)
        then:
        git.log().size() == 1
        git.status() == new Status(
                unstaged: [added: ['folderB/2.txt'], modified: ['1.txt', 'folderB/1.txt']])
    }

    def 'commit with paths only includes the specified paths from the index'() {
        given:
        git.add(patterns:['.'])
        when:
        git.commit(message:'Test2', paths:['folderA'])
        then:
        git.log().size() == 2
        git.status() == new Status(
                staged: [added: ['folderB/2.txt'], modified: ['1.txt', 'folderB/1.txt']])
    }

    def 'commit without specific committer or author uses repo config'() {
        given:
        git.add(patterns:['folderA'])
        when:
        def commit = git.commit(message:'Test2')
        then:
        commit.committer == new Person('Alfred Pennyworth', 'alfred.pennyworth@wayneindustries.com')
        commit.author == new Person('Alfred Pennyworth', 'alfred.pennyworth@wayneindustries.com')
        git.log().size() == 2
        git.status() == new Status(
                unstaged: [added: ['folderB/2.txt'], modified: ['1.txt', 'folderB/1.txt']])
    }

    def 'commit with specific committer and author uses those'() {
        given:
        git.add(patterns:['folderA'])
        def bruce = new Person('Bruce Wayne', 'bruce.wayne@wayneindustries.com')
        def lucius = new Person('Lucius Fox', 'lucius.fox@wayneindustries.com')
        when:
        def commit = git.commit {
            message = 'Test2'
            committer = lucius
            author = bruce
        }
        then:
        commit.committer == lucius
        commit.author == bruce
        git.log().size() == 2
        git.status() == new Status(unstaged: [added: ['folderB/2.txt'], modified: ['1.txt', 'folderB/1.txt']])
    }
}
