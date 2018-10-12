package org.xbib.gradle.plugin.git

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.xbib.groovy.git.Git
import spock.lang.Specification

class GitPluginTest extends Specification {
    @Rule TemporaryFolder tempDir = new TemporaryFolder()
    File projectDir
    File buildFile

    def setup() {
        projectDir = tempDir.newFolder('project')
        buildFile = projectFile('build.gradle')
    }

    def 'with no repo, plugin sets git to null'() {
        given:
        buildFile << '''\
plugins {
  id 'org.xbib.gradle.plugin.git'
}

task doStuff {
  doLast {
    assert git == null
  }
}
'''
        when:
        def result = build('doStuff')
        then:
        result.task(':doStuff').outcome == TaskOutcome.SUCCESS
    }

    def 'with repo, plugin opens the repo as git'() {
        given:
        Git git = Git.init(dir: projectDir)
        projectFile('1.txt') << '1'
        git.add(patterns: ['1.txt'])
        git.commit(message: 'yay')
        git.tag.add(name: '1.0.0')

        buildFile << '''\
plugins {
  id 'org.xbib.gradle.plugin.git'
}

task doStuff {
  doLast {
    println git.describe()
  }
}
'''
        when:
        def result = build('doStuff', '--quiet')
        then:
        result.task(':doStuff').outcome == TaskOutcome.SUCCESS
        result.output.normalize() == '1.0.0\n'
    }

    def 'with repo, plugin closes the repo after build is finished'() {
        given:
        Git git = Git.init(dir: projectDir)
        projectFile('1.txt') << '1'
        git.add(patterns: ['1.txt'])
        git.commit(message: 'yay')
        git.tag.add(name: '1.0.0')

        buildFile << '''\
plugins {
  id 'org.xbib.gradle.plugin.git'
}

task doStuff {
  doLast {
    println git.describe()
  }
}
'''
        when:
        def result = build('doStuff', '--info')
        then:
        result.task(':doStuff').outcome == TaskOutcome.SUCCESS
        result.output.contains('closing git repo')
    }

    private BuildResult build(String... args) {
        return GradleRunner.create()
                .withGradleVersion(GradleVersion.current().version)
                .withPluginClasspath()
                .withProjectDir(projectDir)
                .forwardOutput()
                .withArguments((args + '--stacktrace') as String[])
                .build()
    }

    private File projectFile(String path) {
        File file = new File(projectDir, path)
        file.parentFile.mkdirs()
        return file
    }
}
