package com.polopoly.siteengine.structure;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.model.ModelTypeDescription;
import com.polopoly.tools.publicapi.annotations.PublicApi;

/**
 * Interface specifying methods that should be exposed in page / site models.
 * This interface resides in the Legacy Site and Page Plugin (see the online
 * documentation).
 */
@PublicApi
public interface PageModelTypeDescription extends ModelTypeDescription {

    /**
     * Gets the name of this page.
     *
     * @return the name of the page
     */
    String getName();

    /**
     * Gets the subpages of this page.
     *
     * @return a list of subpages
     */
    ContentList getSubPages();

    /**
     * Gets the versioned content id of this page.
     *
     * @return the versioned content id of this page
     */
    public abstract VersionedContentId getContentId();

    /**
     * Returns whether this page should be indexed or not.
     *
     * @return true if this page should not be indexed
     */
    public boolean getDisallowIndexing();

    /**
     * Gets the parent ids of this page.
     *
     * @return the parent ids of this page
     */
    public ContentId[] getParentIds();
}