package org.xbib.groovy.git

import org.eclipse.jgit.api.Git

/**
 * A repository.
 */
class Repository {
    /**
     * The directory the repository is contained in.
     */
    File rootDir

    /**
     * The JGit instance opened for this repository.
     */
    Git jgit

    /**
     * The credentials used when talking to remote repositories.
     */
    Credentials credentials

    Repository(File rootDir, Git jgit, Credentials credentials) {
        this.rootDir = rootDir
        this.jgit = jgit
        this.credentials = credentials
    }

    @Override
    String toString() {
        return "Repository(${rootDir.canonicalPath})"
    }
}
