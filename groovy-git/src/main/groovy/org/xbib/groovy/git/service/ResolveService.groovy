package org.xbib.groovy.git.service

import org.xbib.groovy.git.Branch
import org.xbib.groovy.git.Commit
import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.Ref
import org.xbib.groovy.git.Tag
import org.xbib.groovy.git.util.GitUtil
import org.eclipse.jgit.lib.ObjectId

/**
 * Convenience methods to resolve various objects.
 */
class ResolveService {
    private final Repository repository

    ResolveService(Repository repository) {
        this.repository = repository
    }

    /**
     * Resolves an object ID from the given object. Can handle any of the following
     * types:
     *
     * <ul>
     *   <li>{@link Commit}</li>
     *   <li>{@link Tag}</li>
     *   <li>{@link Branch}</li>
     *   <li>{@link Ref}</li>
     * </ul>
     *
     * @param object the object to resolve
     * @return the corresponding object id
     */
    String toObjectId(Object object) {
        if (object == null) {
            return null
        } else if (object instanceof Commit) {
            return object.id
        } else if (object instanceof Branch || object instanceof Tag || object instanceof Ref) {
            return ObjectId.toString(repository.jgit.repository.exactRef(object.fullName).objectId)
        } else {
            throwIllegalArgument(object)
        }
    }

    /**
     * Resolves a commit from the given object. Can handle any of the following
     * types:
     *
     * <ul>
     *   <li>{@link Commit}</li>
     *   <li>{@link Tag}</li>
     *   <li>{@link Branch}</li>
     *   <li>{@link String}</li>
     *   <li>{@link GString}</li>
     * </ul>
     *
     * <p>
     * String arguments can be in the format of any
     * <a href="http://git-scm.com/docs/gitrevisions.html">Git revision string</a>.
     * </p>
     * @param object the object to resolve
     * @return the corresponding commit
     */
    Commit toCommit(Object object) {
        if (object == null) {
            return null
        } else if (object instanceof Commit) {
            return object
        } else if (object instanceof Tag) {
            return object.commit
        } else if (object instanceof Branch) {
            return GitUtil.resolveCommit(repository, object.fullName)
        } else if (object instanceof String || object instanceof GString) {
            return GitUtil.resolveCommit(repository, object)
        } else {
            throwIllegalArgument(object)
        }
    }

    /**
     * Resolves a branch from the given object. Can handle any of the following
     * types:
     * <ul>
     *   <li>{@link Branch}</li>
     *   <li>{@link String}</li>
     *   <li>{@link GString}</li>
     * </ul>
     * @param object the object to resolve
     * @return the corresponding commit
     */
    Branch toBranch(Object object) {
        if (object == null) {
            return null
        } else if (object instanceof Branch) {
            return object
        } else if (object instanceof String || object instanceof GString) {
            return GitUtil.resolveBranch(repository, object)
        } else {
            throwIllegalArgument(object)
        }
    }

    /**
     * Resolves a branch name from the given object. Can handle any of the following
     * types:
     * <ul>
     *   <li>{@link String}</li>
     *   <li>{@link GString}</li>
     *   <li>{@link Branch}</li>
     * </ul>
     * @param object the object to resolve
     * @return the corresponding branch name
     */
    String toBranchName(Object object) {
        if (object == null) {
            return object
        } else if (object instanceof String || object instanceof GString) {
            return object
        } else if (object instanceof Branch) {
            return object.fullName
        } else {
            throwIllegalArgument(object)
        }
    }

    /**
     * Resolves a tag from the given object. Can handle any of the following
     * types:
     * <ul>
     *   <li>{@link Tag}</li>
     *   <li>{@link String}</li>
     *   <li>{@link GString}</li>
     * </ul>
     * @param object the object to resolve
     * @return the corresponding commit
     */
    Tag toTag(Object object) {
        if (object == null) {
            return object
        } else if (object instanceof Tag) {
            return object
        } else if (object instanceof String || object instanceof GString) {
            GitUtil.resolveTag(repository, object)
        } else {
            throwIllegalArgument(object)
        }
    }

    /**
     * Resolves a tag name from the given object. Can handle any of the following
     * types:
     * <ul>
     *   <li>{@link String}</li>
     *   <li>{@link GString}</li>
     *   <li>{@link Tag}</li>
     * </ul>
     * @param object the object to resolve
     * @return the corresponding tag name
     */
    String toTagName(Object object) {
        if (object == null) {
            return object
        } else if (object instanceof String || object instanceof GString) {
            return object
        } else if (object instanceof Tag) {
            return object.fullName
        } else {
            throwIllegalArgument(object)
        }
    }

    /**
     * Resolves a revision string that corresponds to the given object. Can
     * handle any of the following types:
     * <ul>
     *   <li>{@link Commit}</li>
     *   <li>{@link Tag}</li>
     *   <li>{@link Branch}</li>
     *   <li>{@link String}</li>
     *   <li>{@link GString}</li>
     * </ul>
     * @param object the object to resolve
     * @return the corresponding commit
     */
    String toRevisionString(Object object) {
        if (object == null) {
            return object
        } else if (object instanceof Commit) {
            return object.id
        } else if (object instanceof Tag) {
            return object.fullName
        } else if (object instanceof Branch) {
            return object.fullName
        } else if (object instanceof String || object instanceof GString) {
            return object
        } else {
            throwIllegalArgument(object)
        }
    }

    private void throwIllegalArgument(Object object) {
        throw new IllegalArgumentException("Can't handle the following object (${object}) of class (${object.class})")
    }
}

