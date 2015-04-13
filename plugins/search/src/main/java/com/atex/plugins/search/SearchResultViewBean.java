package com.atex.plugins.search;

/**
 * Used to store the model for a search result.
 */

import com.polopoly.cm.ContentId;

/**
 * POJO used to store a search model for a search model mapper.
 */
public class SearchResultViewBean implements SearchResultView {
    private ContentId contentId = null;
    private String name = "";
    private String text = "";
    private ContentId imageContentId = null;
    private ContentId[] linkPath = null;


    @Override
    public ContentId getContentId() {
        return contentId;
    }

    public void setContentId(final ContentId contentId) {
        this.contentId = contentId;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    @Override
    public ContentId getImageContentId() {
        return imageContentId;
    }

    public void setImageContentId(final ContentId imageContentId) {
        this.imageContentId = imageContentId;
    }

    @Override
    public ContentId[] getLinkPath() {
        return linkPath;
    }

    public void setLinkPath(final ContentId[] linkPath) {
        this.linkPath = linkPath;
    }

}
