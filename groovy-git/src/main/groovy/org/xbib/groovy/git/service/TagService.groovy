package org.xbib.groovy.git.service

import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.internal.WithOperations
import org.xbib.groovy.git.operation.TagAddOp
import org.xbib.groovy.git.operation.TagListOp
import org.xbib.groovy.git.operation.TagRemoveOp

/**
 * Provides support for performing tag-related operations on
 * a Git repository.
 *
 * <p>
 *   Details of each operation's properties and methods are available on the
 *   doc page for the class. The following operations are supported directly on
 *   this service instance.
 * </p>
 *
 * <ul>
 *   <li>{@link org.xbib.groovy.git.operation.TagAddOp add}</li>
 *   <li>{@link org.xbib.groovy.git.operation.TagListOp list}</li>
 *   <li>{@link org.xbib.groovy.git.operation.TagRemoveOp remove}</li>
 * </ul>
 *
 */
@WithOperations(instanceOperations=[TagListOp, TagAddOp, TagRemoveOp])
class TagService {
    private final Repository repository

    TagService(Repository repository) {
        this.repository = repository
    }
}
