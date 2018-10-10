package org.xbib.groovy.git

import groovy.transform.Immutable

/**
 * A person.
 */
@Immutable
class Person {
    /**
     * Name of person.
     */
    String name

    /**
     * Email address of person.
     */
    String email
}
