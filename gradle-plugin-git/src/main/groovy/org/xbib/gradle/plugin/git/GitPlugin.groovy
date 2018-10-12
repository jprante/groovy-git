package org.xbib.gradle.plugin.git

import org.xbib.groovy.git.Git
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin adding a {@code git} property to all projects that searches for a Git repo from the project's directory.
 */
class GitPlugin implements Plugin<Project> {
  @Override
  void apply(Project project) {
    try {
      Git git = Git.open(currentDir: project.rootDir)
      project.gradle.buildFinished {
        project.logger.info "closing git repo: ${git.repository.rootDir}"
        git.close()
      }
      project.allprojects { prj ->
        if (prj.ext.has('git')) {
          prj.logger.warn("project ${prj.path} already has a git property, overriding")
        }
        prj.ext.git = git
      }
    } catch (Exception e) {
      project.logger.debug("failed trying to find git repository for ${project.path}", e)
      project.ext.git = null
    }
  }
}
