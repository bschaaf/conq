package com.atex.plugins.search;

import org.apache.solr.client.solrj.response.QueryResponse;

import java.util.List;

/**
 * Process a faceted SOLR response and get you a list of facets.
 */
public interface FacetsProcessor {

    /**
     * Process a list of SOLR query responses and provides a list
     * of facets fields suitable to be displayed in a search result
     * list.
     *
     * @param config the search configuration.
     * @param queryResponses the SOLR query responses.
     * @param filterQueriesList the list of filter queries.
     * @return a list of {@link com.atex.plugins.search.FacetFieldWrapper} or empty list
     *          if no query responses have been provided. Never returns null.
     */
    List<FacetFieldWrapper> process(final SearchConfiguration config,
                                    final List<QueryResponse> queryResponses,
                                    final List<String> filterQueriesList);
}
