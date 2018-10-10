package org.xbib.groovy.git.auth

import org.xbib.groovy.git.Credentials

import org.eclipse.jgit.api.TransportCommand
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

final class TransportOpUtil {

    private TransportOpUtil() {
    }

    /**
     * Configures the given transport command with the given credentials.
     * @param cmd the command to configure
     * @param credentials the hardcoded credentials to use, if not {@code null}
     */
    static void configure(TransportCommand cmd, Credentials credentials) {
        AuthConfig config = AuthConfig.fromSystem()
        cmd.setCredentialsProvider(determineCredentialsProvider(config, credentials))
    }

    private static CredentialsProvider determineCredentialsProvider(AuthConfig config, Credentials credentials) {
        Credentials systemCreds = config.hardcodedCreds
        if (credentials?.populated) {
            return new UsernamePasswordCredentialsProvider(credentials.username, credentials.password)
        } else if (systemCreds?.populated) {
            return new UsernamePasswordCredentialsProvider(systemCreds.username, systemCreds.password)
        } else {
            return null
        }
    }
}
