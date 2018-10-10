package org.xbib.groovy.git.operation

import org.xbib.groovy.git.Git
import java.util.concurrent.Callable
import org.xbib.groovy.git.Credentials
import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.internal.Operation
import org.xbib.groovy.git.util.CoercionUtil
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

/**
 * Opens an existing repository. Returns a {@link org.xbib.groovy.git.Git} pointing
 * to the resulting repository.
 */
@Operation('open')
class OpenOp implements Callable<Git> {
    /**
     * Hardcoded credentials to use for remote operations.
     */
    Credentials credentials

    /**
     * The directory to open the repository from. Incompatible
     * with {@code currentDir}.
     * @see {@link CoercionUtil#toFile(Object)}
     */
    Object dir

    /**
     * The directory to begin searching from the repository
     * from. Incompatible with {@code dir}.
     * @see {@link CoercionUtil#toFile(Object)}
     */
    Object currentDir

    Git call() {
        if (dir && currentDir) {
            throw new IllegalArgumentException('Cannot use both dir and currentDir.')
        } else if (dir) {
            def dirFile = CoercionUtil.toFile(dir)
            def repo = new Repository(dirFile, org.eclipse.jgit.api.Git.open(dirFile), credentials)
            return new Git(repo)
        } else {
            FileRepositoryBuilder builder = new FileRepositoryBuilder()
            builder.readEnvironment()
            if (currentDir) {
                File currentDirFile = CoercionUtil.toFile(currentDir)
                builder.findGitDir(currentDirFile)
            } else {
                builder.findGitDir()
            }
            if(builder.getGitDir() == null){
                throw new IllegalStateException('No .git directory found!');
            }
            def jgitRepo = builder.build()
            org.eclipse.jgit.api.Git jgit = new org.eclipse.jgit.api.Git(jgitRepo)
            Repository repo = new Repository(jgitRepo.directory, jgit, credentials)
            return new Git(repo)
        }
    }
}
