package com.atex.plugins.search.impl;

import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.Subject;
import com.atex.onecms.content.repository.StorageException;
import com.atex.plugins.search.SearchResultView;
import com.atex.plugins.search.SearchResultsVariantFilter;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMRuntimeException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.PolicyCMServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Filter contentIds that do not support the {@link #SEARCHRESULTVIEW_VARIANT} variant.
 */
public class SearchResultsVariantFilterImpl implements SearchResultsVariantFilter {
    private static final Logger LOGGER = Logger.getLogger(SearchResultsVariantFilterImpl.class.getName());

    private final PolicyCMServer cmServer;
    private final ContentManager contentManager;

    public SearchResultsVariantFilterImpl(final CmClient cmClient) {
        if (cmClient == null) {
            throw new CMRuntimeException("Could not fetch cmClient");
        }

        cmServer = cmClient.getPolicyCMServer();
        contentManager = cmClient.getContentManager();
    }

    public SearchResultsVariantFilterImpl(final PolicyCMServer cmServer, final ContentManager contentManager) {
        this.cmServer = cmServer;
        this.contentManager = contentManager;
    }

    /**
     * Fetches the result content using content manager, content must have a wrapper for
     * "com.atex.plugins.search.searchresultview" variant.
     *
     * @return a {@link com.atex.plugins.search.SearchResultView} or null if no variant configured.
     */
    @Override
    public SearchResultView getSearchResultView(final ContentId contentId) {
        if (contentId != null) {
            try {
                ContentVersionId vid = getContentManager().resolve(IdUtil.fromPolicyContentId(contentId.getContentId()),
                                                                   Subject.NOBODY_CALLER);

                if (vid == null) {
                    LOGGER.log(Level.SEVERE, "version for id not found: " + contentId.getContentIdString());
                    return null;
                }

                ContentResult<SearchResultView> dataResult = getContentManager().get(
                        vid,
                        SEARCHRESULTVIEW_VARIANT,
                        SearchResultView.class,
                        Collections.<String, Object>emptyMap(),
                        Subject.NOBODY_CALLER);
                if (dataResult != null && dataResult.getContent() != null) {
                    return dataResult.getContent().getContentData();
                }

                LOGGER.log(Level.INFO, "id " + contentId.getContentIdString() + " does not support "
                    + "SearchResultView variant");
            } catch (ClassCastException | StorageException e) {
                LOGGER.log(Level.SEVERE, "Unable to fetch searchresultview variant content using id: "
                        + contentId.getContentId().getContentIdString(), e);
            }
        }
        return null;
    }

    /**
     * Return a list of beans using the content manager, only content that has a
     * "com.atex.plugins.search.searchresultview" variant will be returned.
     *
     * @param ids a list of content ids.
     * @return a not null list.
     */
    @Override
    public List<SearchResultView> getSearchResultViewList(final List<ContentId> ids) {
        final List<SearchResultView> filteredList = new ArrayList<>();
        for (final ContentId id : ids) {
            final SearchResultView result = getSearchResultView(id);
            if (result != null) {
                filteredList.add(result);
            }
        }
        return filteredList;
    }

    public PolicyCMServer getCMServer() {
        return cmServer;
    }

    public ContentManager getContentManager() {
        return contentManager;
    }
}
