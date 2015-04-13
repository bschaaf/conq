package com.atex.plugins.users;

import com.atex.onecms.content.aspects.annotations.AspectDefinition;

/**
 * Declares an authentication method.
 * The method must have an external id with the format <code>atex.AuthenticationMethod:NAME</code>.
 */
@AspectDefinition
public class AuthenticationMethod {
    public static final String ASPECT_NAME = AuthenticationMethod.class.getName();

    private String name;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return a human-readable description of the method.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description a human-readable description of the method.
     */
    public void setDescription(final String description) {
        this.description = description;
    }
}
