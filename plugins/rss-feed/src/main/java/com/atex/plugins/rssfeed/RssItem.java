package com.atex.plugins.rssfeed;

import java.util.Date;

import com.atex.onecms.ws.image.ImageServiceUrlBuilder;
import com.polopoly.cm.ContentId;
import com.polopoly.siteengine.standard.feed.FeedItem;

/**
 * Simple data transport class, used to construct items in feeds.
 */
public class RssItem
    extends FeedItem
{
    private final String title;
    private final String description;
    private final String pubDate;
    private final String guid;
    private final ContentId[] parentIds;
    private final ImageServiceUrlBuilder imageUrlBuilder;

    public RssItem(String title, String description, String pubDate, String guid,
            ContentId[] parentIds, ImageServiceUrlBuilder imageUrlBuilder)
    {
        this.title = title;
        this.description = description;
        this.pubDate = pubDate;
        this.guid = guid;
        this.parentIds = parentIds;
        this.imageUrlBuilder = imageUrlBuilder;
    }

    public ContentId[] getParentIds()
    {
        return parentIds;
    }

    public ImageServiceUrlBuilder getImageUrlBuilder()
    {
        return  imageUrlBuilder;
    }

    public String getTitle()
    {
        return title;
    }

    public String getDescription()
    {
        return description;
    }

    public String getPubDate()
    {
        return pubDate;
    }

    @Override
    public String getFeedItemId() {
        return guid;
    }

    @Override
    public Date getFeedItemPublishedDate() {
        // Not needed
        return null;
    }
}
