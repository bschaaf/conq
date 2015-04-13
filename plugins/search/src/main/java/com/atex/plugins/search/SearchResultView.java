package com.atex.plugins.search;

import com.polopoly.cm.ContentId;
import com.polopoly.model.DescribesModelType;

/**
 * Describe a search result.
 */
@DescribesModelType
public interface SearchResultView {
    ContentId getContentId();

    String getName();

    String getText();

    /**
     * Returns the image content id, or null if no image is found.
     *
     * @return the image content id.
     */
    ContentId getImageContentId();

    /**
     * Fetches full path of teaserable content, content is responsible for adding itself to the
     * path.
     *
     * @return path of teaserable content, used for linking.
     */
    ContentId[] getLinkPath();
}
