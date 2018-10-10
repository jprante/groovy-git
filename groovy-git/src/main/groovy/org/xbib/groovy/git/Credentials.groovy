package org.xbib.groovy.git

import groovy.transform.Canonical

/**
 * Credentials to use for remote operations.
 */
@Canonical
class Credentials {

    final String username

    final String password

    Credentials() {
        this(null, null)
    }

    Credentials(String username, String password) {
        this.username = username
        this.password = password
    }

    String getUsername() {
        return username ?: ''
    }

    String getPassword() {
        return password ?: ''
    }

    boolean isPopulated() {
        return username != null
    }
}
