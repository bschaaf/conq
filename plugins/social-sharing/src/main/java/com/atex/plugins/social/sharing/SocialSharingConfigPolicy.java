package com.atex.plugins.social.sharing;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.plugins.baseline.policy.BaselinePolicy;
import com.google.common.base.Strings;
import com.polopoly.model.DescribesModelType;

/**
 * Social sharing configuration.
 */
@DescribesModelType
public class SocialSharingConfigPolicy extends BaselinePolicy {

    private static final Logger LOGGER = Logger.getLogger(SocialSharingConfigPolicy.class.getName());

    static final String FACEBOOK_APPID_FIELD = "facebook.appid";

    static final String GPLUS_URL_FIELD = "gplus.url";
    static final String GPLUS_TYPE_FIELD = "gplus.type";
    static final String GPLUS_ANNOTATION_FIELD = "gplus.annotation";
    static final String GPLUS_HEIGHT_FIELD = "gplus.height";
    static final String GPLUS_WIDTH_FIELD = "gplus.width";
    static final String GPLUS_ATTRIBUTES_FIELD = "gplus.attributes";
    static final String GPLUS_LANG_FIELD = "gplus.lang";

    static final String TWITTER_TEXT_FIELD = "twitter.text";
    static final String TWITTER_COUNT_FIELD = "twitter.count";
    static final String TWITTER_LANG_FIELD = "twitter.lang";
    static final String TWITTER_SIZE_FIELD = "twitter.size";
    static final String TWITTER_RELATED_FIELD = "twitter.related";
    static final String TWITTER_ATTRIBUTES_FIELD = "twitter.attributes";

    static final int GPLUS_WIDTH_DEFAULT = 120;

    static final String GPLUS_TYPE_SHARE = "0";
    static final String GPLUS_TYPE_PLUSONE = "1";
    static final String GPLUS_TYPE_DEFAULT = GPLUS_TYPE_SHARE;

    static final String GPLUS_ANNOTATION_NONE = "0";
    static final String GPLUS_ANNOTATION_DEFAULT = GPLUS_ANNOTATION_NONE;

    static final String GPLUS_HEIGHT_DEFAULT = "20";
    static final String GPLUS_VERTICAL_BUBBLE_HEIGHT = "60";

    static final String TWITTER_COUNT_DEFAULT = "none";
    static final String TWITTER_SIZE_DEFAULT = "medium";

    /**
     * Return the configured facebook application id.
     *
     * @return a not null String.
     */
    public String getFacebookAppId() {

        return Strings.nullToEmpty(getChildValue(FACEBOOK_APPID_FIELD, ""));
    }

    /**
     * Return the google plus url.
     *
     * @return a not null String.
     */
    public String getGooglePlusUrl() {

        return Strings.nullToEmpty(getChildValue(GPLUS_URL_FIELD, ""));
    }

    /**
     * Return the google type.
     *
     * @return a not null String.
     */
    public String getGoogleType() {

        final String value = getChildValue(GPLUS_TYPE_FIELD, null);
        if (Strings.isNullOrEmpty(value)) {
            return GPLUS_TYPE_DEFAULT;
        }
        return value;
    }

    /**
     * Return the google annotation.
     *
     * @return a not null String.
     */
    public String getGoogleAnnotation() {

        final String value = getChildValue(GPLUS_ANNOTATION_FIELD, null);
        if (Strings.isNullOrEmpty(value)) {
            return GPLUS_ANNOTATION_DEFAULT;
        }
        return value;
    }

    /**
     * Return the google height.
     *
     * @return a not null String.
     */
    public String getGoogleHeight() {

        final String value = getChildValue(GPLUS_HEIGHT_FIELD, null);
        if (Strings.isNullOrEmpty(value)) {
            return GPLUS_HEIGHT_DEFAULT;
        }
        return value;
    }

    /**
     * Return the google width (it must be greater or equal to 120).
     *
     * @return a not null string.
     */
    public String getGoogleWidth() {

        final String value = Strings.nullToEmpty(getChildValue(GPLUS_WIDTH_FIELD, ""));
        if (!Strings.isNullOrEmpty(value)) {

            try {
                final int width = Integer.parseInt(value);
                if (width < GPLUS_WIDTH_DEFAULT) {
                    return Integer.toString(GPLUS_WIDTH_DEFAULT);
                }
                return value;
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "The width " + value + " cannot be converted to int");
            }

            return Integer.toString(GPLUS_WIDTH_DEFAULT);
        }

        return "";
    }

    /**
     * Return the additional google html attributes.
     *
     * @return a not null String.
     */
    public String getGoogleAttributes() {

        return Strings.nullToEmpty(getChildValue(GPLUS_ATTRIBUTES_FIELD, ""));
    }

    /**
     * Return the google language. The empty string means the default language.
     *
     * @return a not null String.
     */
    public String getGoogleLanguage() {

        return Strings.nullToEmpty(getChildValue(GPLUS_LANG_FIELD, ""));
    }

    /**
     * Return the default twitter text.
     *
     * @return a not null String.
     */
    public String getTwitterText() {

        return Strings.nullToEmpty(getChildValue(TWITTER_TEXT_FIELD, ""));

    }

    /**
     * Return the twitter count option.
     *
     * @return a not null String.
     */
    public String getTwitterCount() {

        final String value = getChildValue(TWITTER_COUNT_FIELD, null);
        if (Strings.isNullOrEmpty(value)) {
            return TWITTER_COUNT_DEFAULT;
        }
        return value;

    }

    /**
     * Return the twitter language.
     *
     * @return a not null String.
     */
    public String getTwitterLanguage() {

        return Strings.nullToEmpty(getChildValue(TWITTER_LANG_FIELD, ""));

    }

    /**
     * Return the twitter size.
     *
     * @return a not null String.
     */
    public String getTwitterSize() {

        final String value = getChildValue(TWITTER_SIZE_FIELD, null);
        if (Strings.isNullOrEmpty(value)) {
            return TWITTER_SIZE_DEFAULT;
        }
        return value;

    }

    /**
     * Return the twitter related option.
     *
     * @return a not null String.
     */
    public String getTwitterRelated() {

        return Strings.nullToEmpty(getChildValue(TWITTER_RELATED_FIELD, ""));

    }

    /**
     * Return the twitter additional html attributes.
     *
     * @return a not null String.
     */
    public String getTwitterAttributes() {

        return Strings.nullToEmpty(getChildValue(TWITTER_ATTRIBUTES_FIELD, ""));

    }

}
