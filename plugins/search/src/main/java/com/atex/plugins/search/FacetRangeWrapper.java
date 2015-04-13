package com.atex.plugins.search;

import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.client.solrj.util.ClientUtils;

import com.polopoly.model.ModelTypeDescription;

/**
 * Wrap a date facet range.
 */
public class FacetRangeWrapper implements FacetWrapper, ModelTypeDescription {

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
    public FacetRangeWrapper(final String name, final RangeFacet.Count count, final boolean selected) {
        this.name = name;
        this.selected = selected;
        this.value = count.getValue();
        this.count = count.getCount();
        this.fq = getAsFilterQuery(count);
    }

    /**
     * Constructor.
     *
     * @param name of the facet.
     * @param value facets value.
     * @param count facets count.
     * @param selected should be selected.
     */
    public FacetRangeWrapper(final String name, final String value, final RangeFacet.Count count,
                             final boolean selected) {
        this.name = name;
        this.selected = selected;
        this.value = value;
        this.count = count.getCount();
        this.fq = getAsFilterQuery(count);
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

    private String getAsFilterQuery(final RangeFacet.Count rangeFacetCount) {

        final StringBuilder sb = new StringBuilder();

        final RangeFacet rf = rangeFacetCount.getRangeFacet();
        sb.append(ClientUtils.escapeQueryChars(rf.getName()));
        sb.append(":[");
        sb.append(ClientUtils.escapeQueryChars(rangeFacetCount.getValue()));
        sb.append(" TO ");
        sb.append(ClientUtils.escapeQueryChars(rangeFacetCount.getValue()));
        sb.append(ClientUtils.escapeQueryChars(rf.getGap().toString()));
        sb.append("]");
        return sb.toString();
    }

}
