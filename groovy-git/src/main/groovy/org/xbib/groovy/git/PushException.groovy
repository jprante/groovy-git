package org.xbib.groovy.git

import org.eclipse.jgit.api.errors.TransportException

class PushException extends TransportException {

    PushException(String message) {
        super(message)
    }

    PushException(String message, Throwable cause) {
        super(message, cause)
    }
}
