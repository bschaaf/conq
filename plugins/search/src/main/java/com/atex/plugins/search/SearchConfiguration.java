package com.atex.plugins.search;

import java.util.List;

import com.atex.plugins.search.data.DateFacet;

/**
 * Search Plugin Configuration.
 */
public interface SearchConfiguration {
    /**
     * Returns the external content IDs of input templates that search queries should be filtered on.
     *
     * @return a list of configured input template ExternalContentIds as String or null (or zero size) for no filtering.
     */
    List<String> getInputTemplatesForFilter();

    /**
     * Return a list of facet fields.
     *
     * @return a not null list of facet fields.
     */
    List<String> getFacetFieldsList();

    /**
     * Return a list of date facet fields.
     *
     * @return a not null list of date facet fields.
     */
    List<DateFacet> getDateFacets();

    /**
     * Return the query type to use when searching.
     *
     * @return can be a null or empty string, in that case the default query type will be used.
     */
    String getQueryType();

    /**
     * Return the number of results to be displayed per page.
     *
     * @return a positive int.
     */
    int getResultsPageSize();
}
