package com.atex.plugins.search;

import com.polopoly.model.ModelTypeDescription;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Contains a list of facets wrapper.
 */
public class FacetFieldWrapper implements ModelTypeDescription {

    private final List<FacetWrapper> values;
    private final String name;

    /**
     * Constructor.
     *
     * @param name of the facet (it must not be null).
     */
    public FacetFieldWrapper(final String name) {
        checkNotNull(name);

        this.name = name;
        this.values = new ArrayList<>();
    }

    /**
     * Add a facet to the list.
     *
     * @param facetValue a not null facet.
     */
    public void addFacetValue(final FacetWrapper facetValue) {
        checkNotNull(facetValue);

        this.values.add(facetValue);
    }

    /**
     * Return the list of facet values.
     *
     * @return a not null list.
     */
    public List<FacetWrapper> getValues() {
        return values;
    }

    /**
     * The name of the facet.
     *
     * @return a not null String
     */
    public String getName() {
        return name;
    }

}
