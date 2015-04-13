package com.atex.plugins.search.impl;

import com.atex.plugins.search.FacetFieldWrapper;
import com.atex.plugins.search.FacetRangeWrapper;
import com.atex.plugins.search.FacetValueWrapper;
import com.atex.plugins.search.FacetsProcessor;
import com.atex.plugins.search.SearchConfiguration;
import com.atex.plugins.search.data.DateFacet;
import com.google.common.collect.Lists;
import com.polopoly.cache.CacheKey;
import com.polopoly.cache.SynchronizedUpdateCache;
import com.polopoly.util.StringUtil;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.client.solrj.response.RangeFacet.Count;
import org.apache.solr.client.solrj.util.ClientUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of the {@link com.atex.plugins.search.FacetsProcessor}.
 */
public class FacetsProcessorImpl implements FacetsProcessor {

    private static final Logger LOGGER = Logger.getLogger(FacetsProcessorImpl.class.getName());

    public static final int CACHE_TIMEOUT = 30 * 60 * 1000;

    /**
     * this is used to parse the solr range values.
     */
    static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        /**
         * See {@link ThreadLocal#initialValue()}
         * @return a not null value.
         */
        @Override
        protected DateFormat initialValue() {
            final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            return df;
        }
    };

    private final SynchronizedUpdateCache updateCache;
    private final Locale locale;

    /**
     * Constructor.
     *
     * @param updateCache sync cache.
     * @param locale          the locale, it will be used to format the date facets values.
     */
    public FacetsProcessorImpl(final SynchronizedUpdateCache updateCache, final Locale locale) {
        this.updateCache = updateCache;
        this.locale = locale;
    }

    @Override
    public List<FacetFieldWrapper> process(final SearchConfiguration config,
                                           final List<QueryResponse> queryResponses,
                                           final List<String> filterQueriesList) {

        if (queryResponses == null || queryResponses.size() == 0) {
            return new ArrayList<>();
        }

        final QueryResponse response = queryResponses.get(0);
        if (response == null) {
            return new ArrayList<>();
        }

        // this map will contain all the filter queries.
        // the key will be the tag name, the value is the list
        // of tag values (at least one value).

        final Map<String, List<String>> filterQueries = filterQueryListToMap(filterQueriesList);

        final List<FacetFieldWrapper> facets = new ArrayList<>();

        // now process the facets list, we will use the filterQueries map
        // to understand which facets we should set as selected.
        addFacetFields(response, filterQueries, facets);

        // now process the facet ranges.
        addRangeFacets(config, response, filterQueries, facets);

        return facets;
    }

    private void addRangeFacets(final SearchConfiguration config,
                                final QueryResponse response,
                                final Map<String, List<String>> filterQueries,
                                final List<FacetFieldWrapper> facets) {
        final List<DateFacet> dateFacets = config.getDateFacets();
        final List<RangeFacet> facetRanges = response.getFacetRanges();
        if (facetRanges != null) {
            for (final RangeFacet rangeFacet : facetRanges) {
                final List<Count> counts = rangeFacet.getCounts();
                if (counts != null && counts.size() > 0) {
                    final FacetFieldWrapper facetFieldWrapper = new FacetFieldWrapper(rangeFacet.getName());
                    for (final Count facetCount : counts) {
                        final boolean selected = isSelected(rangeFacet.getName(), facetCount.getValue(), filterQueries);
                        final FacetRangeWrapper wrapper;
                        final DateFacet df = findDateFacet(dateFacets, rangeFacet.getName());
                        if (df != null && !df.getFormatValue().isEmpty()) {
                            final Date d = parseUTCDate(facetCount.getValue());
                            if (d != null) {
                                final DateFormat dateFormat = createDateFormat(df.getFormatValue());
                                wrapper = new FacetRangeWrapper(
                                        rangeFacet.getName(), dateFormat.format(d), facetCount, selected);
                            } else {
                                wrapper = new FacetRangeWrapper(rangeFacet.getName(), facetCount, selected);
                            }
                        } else {
                            wrapper = new FacetRangeWrapper(rangeFacet.getName(), facetCount, selected);
                        }
                        facetFieldWrapper.addFacetValue(wrapper);
                    }
                    facets.add(facetFieldWrapper);
                }
            }
        }
    }

    private void addFacetFields(final QueryResponse response,
                                final Map<String, List<String>> filterQueries,
                                final List<FacetFieldWrapper> facets) {
        final List<FacetField> facetFields = response.getFacetFields();
        if (facetFields != null) {
            for (final FacetField facetField : facetFields) {
                if (facetField.getValueCount() > 0) {
                    final FacetFieldWrapper facetFieldWrapper = new FacetFieldWrapper(facetField.getName());
                    for (FacetField.Count facetCount : facetField.getValues()) {
                        final boolean selected = isSelected(facetField.getName(), facetCount.getName(), filterQueries);
                        facetFieldWrapper.addFacetValue(
                                new FacetValueWrapper(facetField.getName(), facetCount, selected));
                    }
                    facets.add(facetFieldWrapper);
                }
            }
        }
    }

    private Map<String, List<String>> filterQueryListToMap(final List<String> filterQueryList) {
        final Map<String, List<String>> filterQueryMap = new HashMap<>();

        // this response's filter query
        if (filterQueryList != null) {
            for (final String fq : filterQueryList) {
                final int idx = fq.indexOf(':');
                if (idx > 0 && idx < fq.length() - 2) {
                    final String key = fq.substring(0, idx);
                    String value = fq.substring(idx + 1);

                    // check if the value is a range and extract the
                    // start parameter.

                    if (value.startsWith("[")) {
                        final int spaceIdx = value.indexOf(' ');
                        if (spaceIdx > 0) {
                            value = value.substring(1, spaceIdx);
                        }
                    }

                    List<String> values = filterQueryMap.get(key);
                    if (values == null) {
                        values = Lists.newArrayList();
                        filterQueryMap.put(key, values);
                    }
                    if (!values.contains(value)) {
                        values.add(value);
                    }
                }
            }
        }
        return filterQueryMap;
    }

    private DateFormat createDateFormat(final String format) {
        final CacheKey cacheKey = new CacheKey(getClass(), format);
        final Object object = updateCache.get(cacheKey);
        if (object instanceof DateFormat) {
            return (DateFormat) object;
        }
        final DateFormat dateFormat = new SimpleDateFormat(format, locale);
        updateCache.put(cacheKey, dateFormat, CACHE_TIMEOUT);
        return dateFormat;
    }

    private DateFacet findDateFacet(final List<DateFacet> dateFacets, final String name) {
        if (dateFacets != null) {
            for (final DateFacet df : dateFacets) {
                if (StringUtil.equals(df.getName(), name)) {
                    return df;
                }
            }
        }
        return null;
    }

    /**
     * Since in the filter queries we are sending the SOLR query filter as returned by
     * {@link org.apache.solr.client.solrj.response.FacetField.Count#getAsFilterQuery}
     * we may need to perform some escaping when comparing with the facet field.
     *
     * @param name  name of the field
     * @param value value of the field
     * @param fqs   map of the filter queries.
     * @return true if in the filter queries we find this name with that value.
     */
    final boolean isSelected(final String name, final String value, final Map<String, List<String>> fqs) {

        checkNotNull(name);
        checkNotNull(value);
        checkNotNull(fqs);

        List<String> fqValue = fqs.get(name);
        if (fqValue == null) {
            fqValue = fqs.get(ClientUtils.escapeQueryChars(name));
        }
        if (fqValue != null) {
            for (final String v : fqValue) {
                if (v.equals(value)) {
                    return true;
                } else if (v.equals(ClientUtils.escapeQueryChars(value))) {
                    return true;
                }
            }
        }
        return false;
    }

    final Date parseUTCDate(final String value) {
        try {
            return DATE_FORMAT.get().parse(value);
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE, "Cannot parse date " + value + ": " + e.getMessage(), e);
        }
        return null;
    }

}
