package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Credentials
import org.xbib.groovy.git.Commit
import org.xbib.groovy.git.Status
import org.xbib.groovy.git.SimpleGitOpSpec

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.errors.RepositoryNotFoundException
import spock.util.environment.RestoreSystemProperties

class OpenOpSpec extends SimpleGitOpSpec {
    private static final String FILE_PATH = 'the-dir/test.txt'
    Commit commit
    File subdir

    def setup() {
        repoFile(FILE_PATH) << '1.1'
        grgit.add(patterns: ['.'])
        commit = grgit.commit(message: 'first commit')
        subdir = repoDir('the-dir')
    }

    def 'open with dir fails if there is no repo in that dir'() {
        when:
        org.xbib.groovy.git.Git.open(dir: 'dir/with/no/repo')
        then:
        thrown(RepositoryNotFoundException)
    }

    def 'open with dir succeeds if repo is in that directory'() {
        when:
        org.xbib.groovy.git.Git opened = org.xbib.groovy.git.Git.open(dir: repoDir('.'))
        then:
        opened.head() == commit
    }

    @RestoreSystemProperties
    def 'open without dir fails if there is no repo in the current dir'() {
        given:
        File workingDir = tempDir.newFolder('no_repo')
        System.setProperty('user.dir', workingDir.absolutePath)
        when:
        org.xbib.groovy.git.Git.open()
        then:
        thrown(IllegalStateException)
    }

    @RestoreSystemProperties
    def 'open without dir succeeds if current directory is repo dir'() {
        given:
        File dir = repoDir('.')
        System.setProperty('user.dir', dir.absolutePath)
        when:
        org.xbib.groovy.git.Git opened = org.xbib.groovy.git.Git.open()
        repoFile(FILE_PATH) << '1.2'
        opened.add(patterns: [FILE_PATH])
        then:
        opened.head() == commit
        opened.status() == new Status(staged: [modified: [FILE_PATH]])
    }

    @RestoreSystemProperties
    def 'open without dir succeeds if current directory is subdir of a repo'() {
        given:
        System.setProperty('user.dir', subdir.absolutePath)
        when:
        org.xbib.groovy.git.Git opened = org.xbib.groovy.git.Git.open()
        repoFile(FILE_PATH) << '1.2'
        then:
        opened.head() == commit
        opened.status() == new Status(unstaged: [modified: [FILE_PATH]])
    }

    @RestoreSystemProperties
    def 'open without dir succeeds if .git in current dir has gitdir'() {
        given:
        File workDir = tempDir.newFolder()
        File gitDir = tempDir.newFolder()

        Git.cloneRepository()
                .setDirectory(workDir)
                .setGitDir(gitDir)
                .setURI(repoDir('.').toURI().toString())
                .call()

        new File(workDir, FILE_PATH) << '1.2'
        System.setProperty('user.dir', workDir.absolutePath)
        when:
        org.xbib.groovy.git.Git opened = org.xbib.groovy.git.Git.open()
        then:
        opened.head() == commit
        opened.status() == new Status(unstaged: [modified: [FILE_PATH]])
    }

    @RestoreSystemProperties
    def 'open without dir succeeds if .git in parent dir has gitdir'() {
        given:
        File workDir = tempDir.newFolder()
        File gitDir = tempDir.newFolder()

        Git.cloneRepository()
                .setDirectory(workDir)
                .setGitDir(gitDir)
                .setURI(repoDir('.').toURI().toString())
                .call()

        new File(workDir, FILE_PATH) << '1.2'
        System.setProperty('user.dir', new File(workDir, 'the-dir').absolutePath)
        when:
        org.xbib.groovy.git.Git opened = org.xbib.groovy.git.Git.open()
        then:
        opened.head() == commit
        opened.status() == new Status(unstaged: [modified: [FILE_PATH]])
    }

    def 'open with currentDir succeeds if current directory is subdir of a repo'() {
        when:
        org.xbib.groovy.git.Git opened = org.xbib.groovy.git.Git.open(currentDir: subdir)
        repoFile(FILE_PATH) << '1.2'
        then:
        opened.head() == commit
        opened.status() == new Status(unstaged: [modified: [FILE_PATH]])
    }

    def 'opened repo can be deleted after being closed'() {
        given:
        org.xbib.groovy.git.Git opened = org.xbib.groovy.git.Git.open(dir: repoDir('.').canonicalFile)
        when:
        opened.close()
        then:
        opened.repository.rootDir.deleteDir()
    }

    def 'credentials as param name should work'() {
        when:
        org.xbib.groovy.git.Git opened = org.xbib.groovy.git.Git.open(dir: repoDir('.'), credentials: new Credentials())
        then:
        opened.head() == commit
    }

    def 'creds as param name should work'() {
        when:
        org.xbib.groovy.git.Git opened = org.xbib.groovy.git.Git.open(dir: repoDir('.'), creds: new Credentials())
        then:
        opened.head() == commit
    }
}
