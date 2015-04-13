package com.atex.plugins.social.sharing.twitter;

import com.google.common.base.Strings;
import com.polopoly.model.DescribesModelType;

/**
 * Configuration for twitter button used in velocity.
 */
@DescribesModelType
public class TwitterRenderBean {

    private final String text;
    private final String count;
    private final String language;
    private final String size;
    private final String related;
    private final String attributes;

    public TwitterRenderBean(final String text, final String count, final String language,
                             final String size, final String related, final String attributes) {

        this.text = text;
        this.count = count;
        this.language = language;
        this.size = size;
        this.related = related;
        this.attributes = attributes;

    }

    /**
     * Get the default text.
     *
     * @return a null value if the string is empty (since it is used in velocity).
     */
    public String getText() {

        return Strings.emptyToNull(text);

    }

    /**
     * The count option.
     *
     * @return a null value if the string is empty (since it is used in velocity).
     */
    public String getCount() {

        return Strings.emptyToNull(count);

    }

    /**
     * The language.
     *
     * @return a null value if the string is empty (since it is used in velocity).
     */
    public String getLanguage() {

        return Strings.emptyToNull(language);

    }

    /**
     * Get the tweet button size.
     *
     * @return a null value if the string is empty (since it is used in velocity).
     */
    public String getSize() {

        return Strings.emptyToNull(size);

    }

    /**
     * Return the related account.
     *
     * @return a null value if the string is empty (since it is used in velocity).
     */
    public String getRelated() {

        return Strings.emptyToNull(related);

    }

    /**
     * Return additional html attributes.
     *
     * @return a null value if the string is empty (since it is used in velocity).
     */
    public String getAttributes() {

        return Strings.emptyToNull(attributes);

    }
}
