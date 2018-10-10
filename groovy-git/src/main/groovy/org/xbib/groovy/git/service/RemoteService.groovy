package org.xbib.groovy.git.service

import org.xbib.groovy.git.Repository
import org.xbib.groovy.git.internal.WithOperations
import org.xbib.groovy.git.operation.RemoteAddOp
import org.xbib.groovy.git.operation.RemoteListOp

/**
 * Provides support for remote-related operations on a Git repository.
 *
 * <p>
 *   Details of each operation's properties and methods are available on the
 *   doc page for the class. The following operations are supported directly on
 *   this service instance.
 * </p>
 *
 * <ul>
 *   <li>{@link org.xbib.groovy.git.operation.RemoteAddOp add}</li>
 *   <li>{@link org.xbib.groovy.git.operation.RemoteListOp list}</li>
 * </ul>
 */
@WithOperations(instanceOperations=[RemoteListOp, RemoteAddOp])
class RemoteService {
    private final Repository repository

    RemoteService(Repository repository) {
        this.repository = repository
    }
}
