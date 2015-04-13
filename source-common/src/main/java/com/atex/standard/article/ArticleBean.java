package com.atex.standard.article;

import com.polopoly.cm.ContentId;

import java.util.List;

public class ArticleBean {
    private String title;
    private String lead;
    private String body;
    private List<ContentId> images;
    private long publishingTime;
    private boolean premiumContent;

    /**
     * Used for linking to this article.
     */
    private ContentId[] linkPath;

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getLead() {
        return lead;
    }

    public void setLead(final String lead) {
        this.lead = lead;
    }

    public String getBody() {
        return body;
    }

    public void setBody(final String body) {
        this.body = body;
    }

    public void setImages(final List<ContentId> images) {
        this.images = images;
    }

    public List<ContentId> getImages() {
        return images;
    }

    public long getPublishingTime() {
        return publishingTime;
    }

    public void setPublishingTime(final long publishingTime) {
        this.publishingTime = publishingTime;
    }

    public boolean isPremiumContent() {
        return premiumContent;
    }

    public void setPremiumContent(final boolean premiumContent) {
        this.premiumContent = premiumContent;
    }

    public ContentId[] getLinkPath() {
        return linkPath;
    }

    public void setLinkPath(final ContentId[] linkPath) {
        this.linkPath = linkPath;
    }
}
