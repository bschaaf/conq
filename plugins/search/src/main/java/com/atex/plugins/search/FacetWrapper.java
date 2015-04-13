package com.atex.plugins.search;

/**
 * A facet wrapper, it can be a value wrapper or a range wrapper.
 */
public interface FacetWrapper {

    /**
     * Name of the facet.
     *
     * @return a not null string.
     */
    String getName();

    /**
     * Value of the facet.
     *
     * @return a not null string.
     */
    String getValue();

    /**
     * Count of the facet.
     *
     * @return usually &gt; 0.
     */
    long getCount();

    /**
     * Tell you if this facet should be selected in the UI.
     *
     * @return true if you should highlight as selected in the UI.
     */
    boolean isSelected();

    /**
     * The value to be used in the query string for filtering using this facet.
     *
     * @return a non null query string value.
     */
    String getFq();

}
