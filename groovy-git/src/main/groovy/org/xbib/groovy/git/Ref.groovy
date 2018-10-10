package org.xbib.groovy.git

import groovy.transform.Immutable

import org.eclipse.jgit.lib.Repository

/**
 * A ref.
 */
@Immutable
class Ref {
    /**
     * The fully qualified name of this ref.
     */
    String fullName

    /**
     * The simple name of the ref.
     * @return the simple name
     */
    String getName() {
        return Repository.shortenRefName(fullName)
    }
}
