package com.atex.plugins.search;

import org.apache.solr.client.solrj.response.FacetField;

import com.polopoly.model.ModelTypeDescription;

/**
 * Wrap a facet value.
 */
public class FacetValueWrapper implements FacetWrapper, ModelTypeDescription {

    private final String name;
    private final String value;
    private final long count;
    private final boolean selected;
    private final String fq;

    /**
     * Constructor.
     *
     * @param name of the facet.
     * @param count facets count.
     * @param selected should be selected.
     */
    public FacetValueWrapper(final String name, final FacetField.Count count, final boolean selected) {
        this.name = name;
        this.selected = selected;
        this.value = count.getName();
        this.count = count.getCount();
        this.fq = count.getAsFilterQuery();
    }

    /**
     * Name of the facet.
     *
     * @return a not null string.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Value of the facet.
     *
     * @return a not null string.
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * Count of the facet.
     *
     * @return usually &gt; 0.
     */
    @Override
    public long getCount() {
        return count;
    }

    /**
     * Tell you if this facet should be selected in the UI.
     *
     * @return true if you should highlight as selected in the UI.
     */
    @Override
    public boolean isSelected() {
        return selected;
    }

    /**
     * The value to be used in the query string for filtering using this facet.
     *
     * @return a non null query string value.
     */
    @Override
    public String getFq() {
        return fq;
    }
}
