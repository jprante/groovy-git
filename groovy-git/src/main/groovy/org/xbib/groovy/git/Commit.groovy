package org.xbib.groovy.git

import groovy.transform.Immutable
import java.time.ZonedDateTime

/**
 * A commit.
 */
@Immutable(knownImmutableClasses=[ZonedDateTime])
class Commit {
    /**
     * The full hash of the commit.
     */
    String id

    /**
     * The abbreviated hash of the commit.
     */
    String abbreviatedId

    /**
     * Hashes of any parent commits.
     */
    List<String> parentIds

    /**
     * The author of the changes in the commit.
     */
    Person author

    /**
     * The committer of the changes in the commit.
     */
    Person committer

    /**
     * The time the commit was created with the time zone of the committer, if available.
     */
    ZonedDateTime dateTime

    /**
     * The full commit message.
     */
    String fullMessage

    /**
     * The shortened commit message.
     */
    String shortMessage

}
