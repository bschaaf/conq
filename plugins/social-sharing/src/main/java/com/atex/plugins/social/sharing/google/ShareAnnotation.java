package com.atex.plugins.social.sharing.google;

import com.google.common.base.Objects;

/**
 * Handle the annotation for the share button.
 */
public enum ShareAnnotation {

    NONE("0", "none"),

    BUBBLE_HORIZONTAL("1", "bubble"),

    BUBBLE_VERTICAL("2", "vertical-bubble"),

    INLINE("3", "inline");

    private String id;
    private String data;

    private ShareAnnotation(final String id, final String data) {
        this.id = id;
        this.data = data;
    }

    /**
     * Get the id used in the polopoly select.
     *
     * @return the id.
     */
    public String getId() {
        return id;
    }

    /**
     * Get the value to use as class.
     *
     * @return the value.
     */
    public String getData() {
        return data;
    }

    /**
     * Return the enum comparing the id.
     *
     * @param id a not null id, if the id is null the NONE value is returned.
     * @return the found enum or throw an {@link java.lang.IllegalAccessException} exception.
     */
    public static ShareAnnotation fromId(final String id) {

        if (id == null) {
            return ShareAnnotation.NONE;
        }

        for (final ShareAnnotation sh : values()) {
            if (Objects.equal(sh.getId(), id)) {
                return sh;
            }
        }

        throw new IllegalArgumentException("id " + id + " is not valid");
    }
}
