package org.xbib.groovy.git.operation

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoField

import org.xbib.groovy.git.Tag
import org.xbib.groovy.git.SimpleGitOpSpec
import org.eclipse.jgit.api.errors.GitAPIException

class TagAddOpSpec extends SimpleGitOpSpec {
    List commits = []

    def setup() {
        repoFile('1.txt') << '1'
        commits << git.commit(message: 'do', all: true)

        repoFile('1.txt') << '2'
        commits << git.commit(message: 'do', all: true)

        repoFile('1.txt') << '3'
        commits << git.commit(message: 'do', all: true)
    }

    def 'tag add creates annotated tag pointing to current HEAD'() {
        given:
        Instant instant = Instant.now().with(ChronoField.NANO_OF_SECOND, 0)
        ZoneId zone = ZoneId.ofOffset('GMT', ZoneId.systemDefault().getRules().getOffset(instant))
        ZonedDateTime tagTime = ZonedDateTime.ofInstant(instant, zone)
        when:
        git.tag.add(name: 'test-tag')
        then:
        git.tag.list() == [new Tag(
                commits[2],
                person,
                'refs/tags/test-tag',
                '',
                '',
                tagTime
        )]
        git.resolve.toCommit('test-tag') == git.head()
    }

    def 'tag add with annotate false creates unannotated tag pointing to current HEAD'() {
        when:
        git.tag.add(name: 'test-tag', annotate: false)
        then:
        git.tag.list() == [new Tag(
                commits[2],
                null,
                'refs/tags/test-tag',
                null,
                null,
                null
        )]
        git.resolve.toCommit('test-tag') == git.head()
    }

    def 'tag add with name and pointsTo creates tag pointing to pointsTo'() {
        given:
        Instant instant = Instant.now().with(ChronoField.NANO_OF_SECOND, 0)
        ZoneId zone = ZoneId.ofOffset('GMT', ZoneId.systemDefault().getRules().getOffset(instant))
        ZonedDateTime tagTime = ZonedDateTime.ofInstant(instant, zone)
        when:
        git.tag.add(name: 'test-tag', pointsTo: commits[0].id)
        then:
        git.tag.list() == [new Tag(
                commits[0],
                person,
                'refs/tags/test-tag',
                '',
                '',
                tagTime
        )]
        git.resolve.toCommit('test-tag') == commits[0]
    }

    def 'tag add without force fails to overwrite existing tag'() {
        given:
        git.tag.add(name: 'test-tag', pointsTo: commits[0].id)
        when:
        git.tag.add(name: 'test-tag')
        then:
        thrown(GitAPIException)
    }

    def 'tag add with force overwrites existing tag'() {
        given:
        git.tag.add(name: 'test-tag', pointsTo: commits[0].id)
        when:
        git.tag.add(name: 'test-tag', force: true)
        then:
        git.resolve.toCommit('test-tag') == git.head()
    }
}
