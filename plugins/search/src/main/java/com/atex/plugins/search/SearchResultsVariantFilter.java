package com.atex.plugins.search;

import com.polopoly.cm.ContentId;

import java.util.List;

/**
 * Used to filter a contentId or a list of contentIds returning
 * only the ones that support the {@link #SEARCHRESULTVIEW_VARIANT} variant.
 */
public interface SearchResultsVariantFilter {
    /**
     * Search Result View variant.
     */
    String SEARCHRESULTVIEW_VARIANT = "com.atex.plugins.search.searchresultview";

    /**
     * Fetches the result content using content manager, content must have a wrapper for
     * "com.atex.plugins.search.searchresultview" variant.
     *
     * @param contentId A content id.
     * @return a {@link SearchResultView} or null if no variant configured.
     */
    SearchResultView getSearchResultView(final ContentId contentId);

    /**
     * Return a list of beans using the content manager, only content that a "com.atex.plugins.search.searchresultview"
     * variant will be returned.
     *
     * @param ids a list of content ids.
     * @return a not null list.
     */
    List<SearchResultView> getSearchResultViewList(final List<ContentId> ids);
}
