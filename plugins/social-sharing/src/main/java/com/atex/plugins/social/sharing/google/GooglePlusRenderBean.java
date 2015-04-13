package com.atex.plugins.social.sharing.google;

import com.google.common.base.Strings;
import com.polopoly.model.DescribesModelType;

/**
 * Configuration for google plus used in velocity.
 */
@DescribesModelType
public class GooglePlusRenderBean {

    private final String annotation;
    private final String height;
    private final String width;
    private final String attributes;
    private final boolean shareButton;

    public GooglePlusRenderBean(final String annotation, final String height, final String width,
                                final String attributes, final boolean shareButton) {
        this.annotation = annotation;
        this.height = height;
        this.width = width;
        this.attributes = attributes;
        this.shareButton = shareButton;
    }

    /**
     * Return the annotation.
     *
     * @return a null value if the string is empty (since it is used in velocity).
     */
    public String getAnnotation() {
        return Strings.emptyToNull(annotation);
    }

    /**
     * Return the height.
     *
     * @return a null value if the string is empty (since it is used in velocity).
     */
    public String getHeight() {
        return Strings.emptyToNull(height);
    }

    /**
     * Return the width.
     *
     * @return a null value if the string is empty (since it is used in velocity).
     */
    public String getWidth() {
        return Strings.emptyToNull(width);
    }

    /**
     * Return the additional html attributes value.
     *
     * @return a null value if the string is empty (since it is used in velocity).
     */
    public String getAttributes() {
        return Strings.emptyToNull(attributes);
    }

    /**
     * Tell you if you should use the share button or the gplus button.
     *
     * @return a boolean value.
     */
    public boolean isShareButton() {
        return shareButton;
    }
}
