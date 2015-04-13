package com.atex.plugins.social.sharing;

import com.polopoly.cm.ContentId;

/**
 * A bean that represents information needed to write html tags related to sharing on social sites.
 */
public class SocialSharingInfo {

    public static final String VARIANT_NAME = "com.atex.plugins.social.sharing";
    private String title;
    private String description;
    private String openGraphType;
    private ContentId imageContentId;

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getOpenGraphType() {
        return openGraphType;
    }

    public void setOpenGraphType(final String openGraphType) {
        this.openGraphType = openGraphType;
    }

    /**
     * @return the contentId of the image that represents the content, or null if not applicable
     */
    public ContentId getImageContentId() {
        return imageContentId;
    }

    /**
     * @see #getImageContentId()
     * @param imageContentId The image content id.
     */
    public void setImageContentId(final ContentId imageContentId) {
        this.imageContentId = imageContentId;
    }
}
