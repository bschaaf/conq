package com.atex.plugins.users;

import com.atex.onecms.content.ContentId;
import com.atex.onecms.content.aspects.annotations.AspectDefinition;

/**
 * Connects a user to its id in an AuthenticationMethod. This should normally not be modified directly.
 */
@AspectDefinition
public class UserAuthentication {
    public static final String ASPECT_NAME = UserAuthentication.class.getName();

    private String method;
    private String loginId;
    private ContentId userId;

    /**
     * Get the method name for this authentication.
     * @return the method name.
     */
    public String getMethod() {
        return method;
    }

    /**
     * Set the method name for this authentication.
     * @param method the method name.
     */
    public void setMethod(final String method) {
        this.method = method;
    }

    /**
     * Get the id of the user in the authentication method implementation.
     * @return the user id.
     */
    public String getLoginId() {
        return loginId;
    }

    /**
     * Set the id of the user in the authentication method implementation.
     * @param loginId the user id.
     */
    public void setLoginId(final String loginId) {
        this.loginId = loginId;
    }

    /**
     * Get the user's id (the content id of the user content).
     * @return the user content id.
     */
    public ContentId getUserId() {
        return userId;
    }

    /**
     * Set the user's id (the content id of the user content).
     * @param userId the user content id
     */
    public void setUserId(final ContentId userId) {
        this.userId = userId;
    }
}
