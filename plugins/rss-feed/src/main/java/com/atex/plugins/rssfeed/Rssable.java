package com.atex.plugins.rssfeed;

import com.polopoly.cm.ContentId;
import com.polopoly.siteengine.standard.feed.Feedable;

public interface Rssable extends Feedable {
    /**
     * @return description of the feed entry
     */
    public String getItemDescription();

    /**
     * @return contentId for this content
     */
    public ContentId getItemContentId();

    /**
     * @return the path for the content
     */
    public ContentId[] getItemParentIds();

    /**
     * @return image of the feed entry
     */
    public ContentId getReferredImageId();
}
