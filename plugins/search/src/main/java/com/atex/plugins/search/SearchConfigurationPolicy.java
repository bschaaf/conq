package com.atex.plugins.search;

import com.atex.onecms.content.ContentManager;
import com.atex.plugins.baseline.policy.BaselinePolicy;
import com.atex.plugins.search.data.DateFacet;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.polopoly.cm.client.CMException;
import com.polopoly.model.DescribesModelType;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Policy that provides the plugin configuration.
 */
@DescribesModelType
public class SearchConfigurationPolicy extends BaselinePolicy implements SearchConfiguration {

    private static final Logger LOGGER = Logger.getLogger(SearchConfigurationPolicy.class.getName());

    static final String FACETFIELDS = "facetfields";
    static final String FACETDATES = "facetdates";
    static final String RESULTSPAGESIZE = "resultsPageSize";
    static final String QUERYTYPE = "queryType";

    public static final String START_SUFFIX = ".start";
    public static final String END_SUFFIX = ".end";
    public static final String GAP_SUFFIX = ".gap";
    public static final String FORMAT_SUFFIX = ".format";
    public static final int DEFAULT_RESULTS_PAGE_SIZE = 10;

    private ContentManager contentManager;
    private ContentManagerMappingReader mappingReader = null;

    /**
     * Constructor.
     *
     * @param contentManager the content manager, automatically provided by Polopoly.
     */
    public SearchConfigurationPolicy(final ContentManager contentManager) {
        this.contentManager = contentManager;
    }

    /**
     * Return the Facet Fields settings.
     *
     * @return a not null String.
     */
    public String getFacetFields() {
        return Strings.nullToEmpty(getChildValue(FACETFIELDS, ""));
    }

    /**
     * Return a list of facet fields.
     *
     * @return a not null list of facet fields.
     */
    @Override
    public List<String> getFacetFieldsList() {
        return Lists.newArrayList(Splitter
                .on(',')
                .omitEmptyStrings()
                .trimResults()
                .split(getFacetFields()));
    }

    @Override
    public List<DateFacet> getDateFacets() {
        final List<DateFacet> facets = Lists.newArrayList();
        try {
            final String[] names = getComponentNames(FACETDATES);
            if (names != null) {

                final List<String> fields = Lists.newArrayList();

                for (final String name : names) {
                    if (name.endsWith(START_SUFFIX)) {
                        fields.add(name.substring(0, name.length() - START_SUFFIX.length()));
                    }
                }

                for (final String fieldName : fields) {
                    final String start = Strings.nullToEmpty(getComponent(FACETDATES, fieldName + START_SUFFIX)).trim();
                    final String end = Strings.nullToEmpty(getComponent(FACETDATES, fieldName + END_SUFFIX)).trim();
                    final String gap = Strings.nullToEmpty(getComponent(FACETDATES, fieldName + GAP_SUFFIX)).trim();
                    final String format = Strings.nullToEmpty(getComponent(FACETDATES,
                            fieldName + FORMAT_SUFFIX)).trim();

                    facets.add(new DateFacet(fieldName, start, end, gap, format));
                }
            }
        } catch (CMException e) {
            LOGGER.log(Level.SEVERE, "Cannot parse field " + FACETDATES + ": " + e.getMessage(), e);
        }
        return facets;
    }

    @Override
    public List<String> getInputTemplatesForFilter() {
        if (mappingReader == null) {
            mappingReader = new ContentManagerMappingReader(getCMServer(), contentManager);
        }
        return mappingReader.getInputTemplatesForVariant(SearchResultsVariantFilter.SEARCHRESULTVIEW_VARIANT);
    }

    @Override
    public String getQueryType() {
        return Strings.nullToEmpty(getChildValue(QUERYTYPE, ""));
    }

    @Override
    public int getResultsPageSize() {

        final String value = getChildValue(RESULTSPAGESIZE, Integer.toString(DEFAULT_RESULTS_PAGE_SIZE));
        if (value == null || value.isEmpty()) {
            return DEFAULT_RESULTS_PAGE_SIZE;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Cannot parse " + value);
        }
        return DEFAULT_RESULTS_PAGE_SIZE;
    }

}
