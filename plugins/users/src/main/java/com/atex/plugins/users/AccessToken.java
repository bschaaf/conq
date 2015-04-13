package com.atex.plugins.users;

/**
 * A token which can allow clients access as a specific user for a specific duration.
 * @see com.atex.plugins.users.AuthenticationManager
 * @see WebClientUtil
 */
public class AccessToken {
    private String token;

    public AccessToken(final String token) {
        this.token = token;
    }

    /**
     * @return the token as a string
     */
    public String getToken() {
        return token;
    }
}
