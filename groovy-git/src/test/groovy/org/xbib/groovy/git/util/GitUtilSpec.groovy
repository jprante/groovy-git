package org.xbib.groovy.git.util

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

import org.xbib.groovy.git.Commit
import org.xbib.groovy.git.Git
import org.xbib.groovy.git.Person
import org.xbib.groovy.git.Repository
import org.eclipse.jgit.errors.RevisionSyntaxException
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.merge.MergeStrategy
import org.eclipse.jgit.revwalk.RevTag
import org.eclipse.jgit.revwalk.RevWalk
import org.junit.Rule
import org.junit.rules.TemporaryFolder

import spock.lang.Specification

class GitUtilSpec extends Specification {

    @Rule TemporaryFolder tempDir = new TemporaryFolder()

    Repository repo

    List commits = []

    Ref annotatedTag

    Ref unannotatedTag

    Ref taggedAnnotatedTag

    def 'resolveObject works for branch name'() {
        expect:
        GitUtil.resolveObject(repo, 'master') == commits[3]
    }

    def 'resolveObject works for full commit hash'() {
        expect:
        GitUtil.resolveObject(repo, ObjectId.toString(commits[0])) == commits[0]
    }

    def 'resolveObject works for abbreviated commit hash'() {
        expect:
        GitUtil.resolveObject(repo, ObjectId.toString(commits[0])[0..5]) == commits[0]
    }

    def 'resolveObject works for full ref name'() {
        expect:
        GitUtil.resolveObject(repo, 'refs/heads/master') == commits[3]
    }

    def 'resolveObject works for HEAD'() {
        expect:
        GitUtil.resolveObject(repo, 'HEAD') == commits[3]
    }

    def 'resolveObject works for parent commit'() {
        expect:
        GitUtil.resolveObject(repo, 'master^') == commits[1]
    }

    def 'resolveObject works for current commit'() {
        expect:
        GitUtil.resolveObject(repo, 'master^0') == commits[3]
    }

    def 'resolveObject works for n-th parent'() {
        expect:
        GitUtil.resolveObject(repo, 'master^2') == commits[2]
    }

    def 'resolveObject works for the n-th ancestor'() {
        expect:
        GitUtil.resolveObject(repo, 'master~2') == commits[0]
    }

    def 'resolveObject fails if revision cannot be found'() {
        expect:
        GitUtil.resolveObject(repo, 'unreal') == null
    }

    def 'resolveObject fails if revision syntax is wrong'() {
        when:
        GitUtil.resolveObject(repo, 'lkj!)#(*')
        then:
        thrown(RevisionSyntaxException)
    }

    def 'convertCommit works for valid commit'() {
        given:
        Person person = new Person(repo.jgit.repo.config.getString('user', null, 'name'), repo.jgit.repo.config.getString('user', null, 'email'))
        Instant instant = Instant.ofEpochSecond(commits[1].commitTime)
        ZoneId zone = ZoneId.ofOffset('GMT', ZoneId.systemDefault().getRules().getOffset(instant))
        ZonedDateTime commitTime = ZonedDateTime.ofInstant(instant, zone)
        Commit expectedCommit = new Commit(
                ObjectId.toString(commits[1]),
                ObjectId.toString(commits[1])[0..6],
                [ObjectId.toString(commits[0])],
                person,
                person,
                commitTime,
                'second commit',
                'second commit'
        )
        expect:
        def result = GitUtil.convertCommit(repo, commits[1])
        result == expectedCommit
        result.dateTime.toInstant() == commitTime.toInstant()
    }

    def 'resolveTag works for annotated tag ref'() {
        given:
        Person person = new Person(repo.jgit.repo.config.getString('user', null, 'name'), repo.jgit.repo.config.getString('user', null, 'email'))
        ZonedDateTime before = ZonedDateTime.now().minusSeconds(2)
        when:
        def tag = GitUtil.resolveTag(repo, annotatedTag)
        and:
        ZonedDateTime after = ZonedDateTime.now().plusSeconds(2)
        then:
        tag.commit == GitUtil.convertCommit(repo, commits[0])
        tag.tagger == person
        tag.fullName == 'refs/tags/v1.0.0'
        tag.fullMessage == 'first tag\ntesting'
        tag.shortMessage == 'first tag testing'
        tag.dateTime.isAfter(before)
        tag.dateTime.isBefore(after)
    }

    def 'resolveTag works for unannotated tag ref'() {
        given:
        Person person = new Person(repo.jgit.repo.config.getString('user', null, 'name'), repo.jgit.repo.config.getString('user', null, 'email'))
        ZonedDateTime before = ZonedDateTime.now().minusSeconds(2)
        when:
        def tag = GitUtil.resolveTag(repo, unannotatedTag)
        and:
        ZonedDateTime after = ZonedDateTime.now().plusSeconds(2)
        then:
        tag.commit == GitUtil.convertCommit(repo, commits[0])
        tag.tagger == null
        tag.fullName == 'refs/tags/v2.0.0'
        tag.fullMessage == null
        tag.shortMessage == null
        tag.dateTime == null
    }

    def 'resolveTag works for a tag pointing to a tag'() {
        given:
        Person person = new Person(repo.jgit.repo.config.getString('user', null, 'name'), repo.jgit.repo.config.getString('user', null, 'email'))
        ZonedDateTime before = ZonedDateTime.now().minusSeconds(2)
        when:
        def tag = GitUtil.resolveTag(repo, taggedAnnotatedTag)
        and:
        ZonedDateTime after = ZonedDateTime.now().plusSeconds(2)
        then:
        tag.commit == GitUtil.convertCommit(repo, commits[0])
        tag.tagger == person
        tag.fullName == 'refs/tags/v1.1.0'
        tag.fullMessage == 'testing'
        tag.shortMessage == 'testing'
        tag.dateTime.isAfter(before)
        tag.dateTime.isBefore(after)
    }

    def setup() {
        File repoDir = tempDir.newFolder('repo')
        org.eclipse.jgit.api.Git jgit = org.eclipse.jgit.api.Git.init().setDirectory(repoDir).call()
        jgit.repo.config.with {
            setString('user', null, 'name', 'Bruce Wayne')
            setString('user', null, 'email', 'bruce.wayne@wayneindustries.com')
            save()
        }
        File testFile = new File(repoDir, '1.txt')
        testFile << '1\n'
        jgit.add().addFilepattern(testFile.name).call()
        commits << jgit.commit().setMessage('first commit\ntesting').call()
        annotatedTag = jgit.tag().setName('v1.0.0').setMessage('first tag\ntesting').call()
        unannotatedTag = jgit.tag().setName('v2.0.0').setAnnotated(false).call()
        testFile << '2\n'
        jgit.add().addFilepattern(testFile.name).call()
        commits << jgit.commit().setMessage('second commit').call()
        jgit.checkout().setName(ObjectId.toString(commits[0])).call()
        testFile << '3\n'
        jgit.add().addFilepattern(testFile.name).call()
        commits << jgit.commit().setMessage('third commit').call()
        jgit.checkout().setName('master').call()
        commits << jgit.merge().include(commits[2]).setStrategy(MergeStrategy.OURS).call().newHead
        RevTag tagV1 = new RevWalk(jgit.repository).parseTag(annotatedTag.objectId)
        taggedAnnotatedTag = jgit.tag().setName('v1.1.0').setObjectId(tagV1).setMessage('testing').call()
        repo = Git.open(dir: repoDir).repository
    }
}
