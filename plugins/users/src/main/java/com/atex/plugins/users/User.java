package com.atex.plugins.users;

import java.util.HashMap;
import java.util.Map;

import com.atex.onecms.content.ContentId;
import com.atex.onecms.content.aspects.annotations.AspectDefinition;

/**
 * A user.
 * @see com.atex.plugins.users.AuthenticationManager
 */
@AspectDefinition
public class User {
    public static final String ASPECT_NAME = User.class.getName();

    private String username;
    private Map<String, ContentId> engagements = new HashMap<>();
    private Map<String, String> authentications = new HashMap<>();

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     * @return map of engagement name to its content id
     */
    public Map<String, ContentId> getEngagements() {
        return engagements;
    }

    /**
     * @param engagements map of engagement name to its content id
     */
    public void setEngagements(final Map<String, ContentId> engagements) {
        this.engagements = engagements;
    }

    /**
     * Get the authentications for the user. The map should normally not be modified directly.
     * @return map of method name to {@link com.atex.plugins.users.UserAuthentication} external id
     */
    public Map<String, String> getAuthentications() {
        return authentications;
    }

    /**
     * Set authentications for the user. This should normally not be used directly.
     * @param authentications map of method name to {@link com.atex.plugins.users.UserAuthentication} external id
     */
    public void setAuthentications(final Map<String, String> authentications) {
        this.authentications = authentications;
    }
}
