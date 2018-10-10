package org.xbib.groovy.git.auth

import org.xbib.groovy.git.Credentials

class AuthConfig {

    static final String USERNAME_OPTION = 'org.xbib.groovy.git.auth.username'
    static final String PASSWORD_OPTION = 'org.xbib.groovy.git.auth.password'

    static final String USERNAME_ENV_VAR = 'GROOVY_GIT_USER'
    static final String PASSWORD_ENV_VAR = 'GROOVY_GIT_PASS'

    private final Map<String, String> props
    private final Map<String, String> env


    private AuthConfig(Map<String, String> props, Map<String, String> env) {
        this.props = props
        this.env = env

        GitSystemReader.install()
    }

    /**
     * Constructs and returns a {@link Credentials} instance reflecting the
     * settings in the system properties.
     * @return a credentials instance reflecting the settings in the system
     * properties, or, if the username isn't set, {@code null}
     */
    Credentials getHardcodedCreds() {
        String username = props[USERNAME_OPTION] ?: env[USERNAME_ENV_VAR]
        String password = props[PASSWORD_OPTION] ?: env[PASSWORD_ENV_VAR]
        return new Credentials(username, password)
    }

    /**
     * Factory method to construct an authentication configuration from the
     * given properties and environment.
     * @param properties the properties to use in this configuration
     * @param env the environment vars to use in this configuration
     * @return the constructed configuration
     * @throws IllegalArgumentException if force is set to an invalid option
     */
    static AuthConfig fromMap(Map props, Map env = [:]) {
        return new AuthConfig(props, env)
    }

    /**
     * Factory method to construct an authentication configuration from the
     * current system properties and environment variables.
     * @return the constructed configuration
     * @throws IllegalArgumentException if force is set to an invalid option
     */
    static AuthConfig fromSystem() {
        return fromMap(System.properties, System.env)
    }
}
