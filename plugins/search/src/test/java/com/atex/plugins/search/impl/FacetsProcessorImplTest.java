package com.atex.plugins.search.impl;

import com.atex.plugins.search.AbstractTestCase;
import com.atex.plugins.search.FacetFieldWrapper;
import com.atex.plugins.search.FacetWrapper;
import com.atex.plugins.search.FacetsProcessor;
import com.atex.plugins.search.SearchConfiguration;
import com.google.common.collect.Lists;
import com.polopoly.cache.NullSynchronizedUpdateCache;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Unit test for {@link com.atex.plugins.search.impl.FacetsProcessorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FacetsProcessorImplTest extends AbstractTestCase {

    private FacetsProcessor processor;

    @Mock
    private SearchConfiguration config;

    private final List<QueryResponse> queryResponses = Lists.newArrayList();
    private final List<String> filterQueries = Lists.newArrayList();
    private final List<String> requestFq = Lists.newArrayList();
    private final List<FacetField> facetFields = Lists.newArrayList();
    private final List<RangeFacet> rangeFacets = Lists.newArrayList();

    @Before
    public void init() {
        processor = new FacetsProcessorImpl(new NullSynchronizedUpdateCache(), Locale.getDefault());

        // setup a query response.
        final QueryResponse response = Mockito.mock(QueryResponse.class);
        queryResponses.add(response);

        final NamedList<Object> params = new SimpleOrderedMap<>();
        params.add("fq", filterQueries);

        final NamedList<Object> header = new SimpleOrderedMap<>();
        header.add("params", params);

        Mockito.when(response.getHeader()).thenReturn(header);
        Mockito.when(response.getFacetFields()).thenReturn(facetFields);
        Mockito.when(response.getFacetRanges()).thenReturn(rangeFacets);
    }

    /**
     * Make sure we do not crash on empty inputs.
     */
    @Test
    public void test_return_empty_list() {

        assertEquals(0, processor.process(config, null, filterQueries).size());

        final List<QueryResponse> response = Lists.newArrayList();

        assertEquals(0, processor.process(config, response, filterQueries).size());

        response.add(null);

        assertEquals(0, processor.process(config, response, filterQueries).size());
    }

    @Test
    public void test_no_facets() {

        final List<QueryResponse> myQueryResponses = Lists.newArrayList();
        final List<String> myFilterQueries = Lists.newArrayList();
        final List<FacetField> myFacetFields = Lists.newArrayList();

        // setup a query response.
        final QueryResponse response = Mockito.mock(QueryResponse.class);
        myQueryResponses.add(response);

        final NamedList<Object> params = new SimpleOrderedMap<>();
        params.add("fq", myFilterQueries);

        final NamedList<Object> header = new SimpleOrderedMap<>();
        header.add("params", params);

        Mockito.when(response.getHeader()).thenReturn(header);
        Mockito.when(response.getFacetFields()).thenReturn(null);

        final List<FacetFieldWrapper> facets = processor.process(config, myQueryResponses, myFilterQueries);

        assertNotNull(facets);
        assertEquals(0, facets.size());
    }

    @Test
    public void test_facets_count_zero() {

        final String tagName = RandomStringUtils.randomAlphabetic(5);
        final String tagValue = RandomStringUtils.randomAlphabetic(20);

        verify(tagName, tagValue, 0, false);
    }

    @Test
    public void test_one_facet() {

        final String tagName = RandomStringUtils.randomAlphabetic(5);
        final String tagValue = RandomStringUtils.randomAlphabetic(20);

        verify(tagName, tagValue, 1, false);
    }

    @Test
    public void test_one_facet_selected() {

        final String tagName = RandomStringUtils.randomAlphabetic(5);
        final String tagValue = RandomStringUtils.randomAlphabetic(20);

        filterQueries.add(tag(tagName, tagValue));
        requestFq.add(tag(tagName, tagValue));

        verify(tagName, tagValue, 1, true);
    }

    @Test
    public void test_one_facet_selected_after_cleanup() {

        final String tagName = RandomStringUtils.randomAlphabetic(5);
        final String tagValue = String.format("%s\\ %s",
                RandomStringUtils.randomAlphabetic(10),
                RandomStringUtils.randomAlphabetic(10));

        filterQueries.add(tag(tagName, tagValue));
        requestFq.add(tag(tagName, tagValue));

        verify(tagName, tagValue, 1, true);
    }

    @Test
    public void test_one_range_facet_selected() {

        final String tagName = "publishingDate";
        final String tagValue = "2014-04-28T10:00:00Z";
        final String tagGap = "+1HOUR/HOUR";
        final String rangeValue = String.format("[%s TO %s%s]", tagValue, tagValue, tagGap);

        filterQueries.add(tag(tagName, rangeValue));

        final RangeFacet facetField = Mockito.mock(RangeFacet.class);
        Mockito.when(facetField.getName()).thenReturn(tagName);
        Mockito.when(facetField.getGap()).thenReturn(tagGap);

        final RangeFacet.Count facetCount = Mockito.mock(RangeFacet.Count.class);
        Mockito.when(facetCount.getValue()).thenReturn(tagValue);
        Mockito.when(facetCount.getCount()).thenReturn(1);
        Mockito.when(facetCount.getRangeFacet()).thenReturn(facetField);

        final List<RangeFacet.Count> values = Lists.newArrayList();
        values.add(facetCount);

        Mockito.when(facetField.getCounts()).thenReturn(values);

        rangeFacets.add(facetField);

        final List<FacetFieldWrapper> facets = processor.process(config, queryResponses, filterQueries);

        assertNotNull(facets);
        assertEquals(1, facets.size());

        final FacetFieldWrapper field = facets.get(0);
        assertNotNull(field);
        assertEquals(tagName, field.getName());

        final List<FacetWrapper> fieldValues = field.getValues();
        assertNotNull(values);
        assertEquals(1, values.size());

        final FacetWrapper fieldValue = fieldValues.get(0);
        assertNotNull(fieldValue);
        assertEquals(tagName, fieldValue.getName());
        assertEquals(tagValue, fieldValue.getValue());
        assertEquals(1, fieldValue.getCount());
        assertEquals(true, fieldValue.isSelected());
    }

    @Test
    public void test_one_facet_wrong_fq() {

        final String tagName = RandomStringUtils.randomAlphabetic(5);
        final String tagValue = RandomStringUtils.randomAlphabetic(20);

        filterQueries.add(RandomStringUtils.randomAlphabetic(5));
        filterQueries.add(tag(tag(tagName, tagValue), RandomStringUtils.randomAlphabetic(5)));

        verify(tagName, tagValue, 1, false);
    }

    @Test
    @SuppressWarnings({ "checkstyle:javancss", "checkstyle:avoidnestedblocks" })
    public void test_two_facet_same_tag_selected() {

        final String tagName = RandomStringUtils.randomAlphabetic(5);
        final String tagValue1 = RandomStringUtils.randomAlphabetic(20);
        final String tagValue2 = RandomStringUtils.randomAlphabetic(20);

        filterQueries.add(tag(tagName, tagValue1));
        filterQueries.add(tag(tagName, tagValue2));

        final FacetField facetField1 = Mockito.mock(FacetField.class);
        Mockito.when(facetField1.getName()).thenReturn(tagName);
        Mockito.when(facetField1.getValueCount()).thenReturn(1);

        final FacetField.Count facetCount1 = Mockito.mock(FacetField.Count.class);
        Mockito.when(facetCount1.getName()).thenReturn(tagValue1);
        Mockito.when(facetCount1.getCount()).thenReturn(new Long(1));
        Mockito.when(facetCount1.getFacetField()).thenReturn(facetField1);

        final List<FacetField.Count> values1 = Lists.newArrayList();
        values1.add(facetCount1);

        Mockito.when(facetField1.getValues()).thenReturn(values1);

        facetFields.add(facetField1);

        final FacetField facetField2 = Mockito.mock(FacetField.class);
        Mockito.when(facetField2.getName()).thenReturn(tagName);
        Mockito.when(facetField2.getValueCount()).thenReturn(1);

        final FacetField.Count facetCount2 = Mockito.mock(FacetField.Count.class);
        Mockito.when(facetCount2.getName()).thenReturn(tagValue2);
        Mockito.when(facetCount2.getCount()).thenReturn(new Long(1));
        Mockito.when(facetCount2.getFacetField()).thenReturn(facetField2);

        final List<FacetField.Count> values2 = Lists.newArrayList();
        values2.add(facetCount2);

        Mockito.when(facetField2.getValues()).thenReturn(values2);

        facetFields.add(facetField2);

        final List<FacetFieldWrapper> facets = processor.process(config, queryResponses, filterQueries);

        assertNotNull(facets);
        assertEquals(2, facets.size());

        // field 1

        {
            final FacetFieldWrapper field = facets.get(0);
            assertNotNull(field);
            assertEquals(tagName, field.getName());

            final List<FacetWrapper> fieldValues = field.getValues();
            assertNotNull(fieldValues);
            assertEquals(1, fieldValues.size());

            final FacetWrapper fieldValue = fieldValues.get(0);
            assertNotNull(fieldValue);
            assertEquals(tagName, fieldValue.getName());
            assertEquals(tagValue1, fieldValue.getValue());
            assertEquals(1, fieldValue.getCount());
            assertEquals(true, fieldValue.isSelected());
        }

        // field 2

        {
            final FacetFieldWrapper field = facets.get(1);
            assertNotNull(field);
            assertEquals(tagName, field.getName());

            final List<FacetWrapper> fieldValues = field.getValues();
            assertNotNull(fieldValues);
            assertEquals(1, fieldValues.size());

            final FacetWrapper fieldValue = fieldValues.get(0);
            assertNotNull(fieldValue);
            assertEquals(tagName, fieldValue.getName());
            assertEquals(tagValue2, fieldValue.getValue());
            assertEquals(1, fieldValue.getCount());
            assertEquals(true, fieldValue.isSelected());
        }
    }

    @Test
    @SuppressWarnings({ "checkstyle:javancss", "checkstyle:avoidnestedblocks" })
    public void test_two_facet_different_tag_selected() {

        final String tagName1 = RandomStringUtils.randomAlphabetic(5);
        final String tagValue1 = RandomStringUtils.randomAlphabetic(20);
        final String tagName2 = RandomStringUtils.randomAlphabetic(5);
        final String tagValue2 = RandomStringUtils.randomAlphabetic(20);

        filterQueries.add(tag(tagName1, tagValue1));
        filterQueries.add(tag(tagName2, tagValue2));

        requestFq.add(tag(tagName1, tagValue1));

        final FacetField facetField1 = Mockito.mock(FacetField.class);
        Mockito.when(facetField1.getName()).thenReturn(tagName1);
        Mockito.when(facetField1.getValueCount()).thenReturn(1);

        final FacetField.Count facetCount1 = Mockito.mock(FacetField.Count.class);
        Mockito.when(facetCount1.getName()).thenReturn(tagValue1);
        Mockito.when(facetCount1.getCount()).thenReturn(new Long(1));
        Mockito.when(facetCount1.getFacetField()).thenReturn(facetField1);

        final List<FacetField.Count> values1 = Lists.newArrayList();
        values1.add(facetCount1);

        Mockito.when(facetField1.getValues()).thenReturn(values1);

        facetFields.add(facetField1);

        final FacetField facetField2 = Mockito.mock(FacetField.class);
        Mockito.when(facetField2.getName()).thenReturn(tagName2);
        Mockito.when(facetField2.getValueCount()).thenReturn(1);

        final FacetField.Count facetCount2 = Mockito.mock(FacetField.Count.class);
        Mockito.when(facetCount2.getName()).thenReturn(tagValue2);
        Mockito.when(facetCount2.getCount()).thenReturn(new Long(1));
        Mockito.when(facetCount2.getFacetField()).thenReturn(facetField2);

        final List<FacetField.Count> values2 = Lists.newArrayList();
        values2.add(facetCount2);

        Mockito.when(facetField2.getValues()).thenReturn(values2);

        facetFields.add(facetField2);

        final List<FacetFieldWrapper> facets = processor.process(config, queryResponses, requestFq);

        assertNotNull(facets);
        assertEquals(2, facets.size());

        // field 1

        {
            final FacetFieldWrapper field = facets.get(0);
            assertNotNull(field);
            assertEquals(tagName1, field.getName());

            final List<FacetWrapper> fieldValues = field.getValues();
            assertNotNull(fieldValues);
            assertEquals(1, fieldValues.size());

            final FacetWrapper fieldValue = fieldValues.get(0);
            assertNotNull(fieldValue);
            assertEquals(tagName1, fieldValue.getName());
            assertEquals(tagValue1, fieldValue.getValue());
            assertEquals(1, fieldValue.getCount());
            assertEquals(true, fieldValue.isSelected());
        }

        // field 2

        {
            final FacetFieldWrapper field = facets.get(1);
            assertNotNull(field);
            assertEquals(tagName2, field.getName());

            final List<FacetWrapper> fieldValues = field.getValues();
            assertNotNull(fieldValues);
            assertEquals(1, fieldValues.size());

            final FacetWrapper fieldValue = fieldValues.get(0);
            assertNotNull(fieldValue);
            assertEquals(tagName2, fieldValue.getName());
            assertEquals(tagValue2, fieldValue.getValue());
            assertEquals(1, fieldValue.getCount());
            assertEquals(false, fieldValue.isSelected());
        }
    }

    @Test
    public void test_date_parsing() throws ParseException {
        final String s = "2014-04-01T00:00:00Z";
        final Date d = FacetsProcessorImpl.DATE_FORMAT.get().parse(s);
        assertNotNull(d);
        final Calendar c = Calendar.getInstance();
        c.setTime(d);

        assertEquals(2014, c.get(Calendar.YEAR));
        assertEquals(3, c.get(Calendar.MONTH));
        assertEquals(1, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, c.get(Calendar.HOUR));
        assertEquals(0, c.get(Calendar.MINUTE));
        assertEquals(0, c.get(Calendar.SECOND));

        final String out = FacetsProcessorImpl.DATE_FORMAT.get().format(d);
        assertEquals(s, out);
    }

    private void verify(final String tagName, final String tagValue, final int count, final boolean selected) {

        final FacetField facetField = Mockito.mock(FacetField.class);
        Mockito.when(facetField.getName()).thenReturn(tagName);
        Mockito.when(facetField.getValueCount()).thenReturn(count);

        final FacetField.Count facetCount = Mockito.mock(FacetField.Count.class);
        Mockito.when(facetCount.getName()).thenReturn(tagValue);
        Mockito.when(facetCount.getCount()).thenReturn(new Long(count));
        Mockito.when(facetCount.getFacetField()).thenReturn(facetField);

        final List<FacetField.Count> values = Lists.newArrayList();
        values.add(facetCount);

        Mockito.when(facetField.getValues()).thenReturn(values);

        facetFields.add(facetField);

        final List<FacetFieldWrapper> facets = processor.process(config, queryResponses, requestFq);

        assertNotNull(facets);
        assertEquals(count, facets.size());

        if (count > 0) {
            final FacetFieldWrapper field = facets.get(0);
            assertNotNull(field);
            assertEquals(tagName, field.getName());

            final List<FacetWrapper> fieldValues = field.getValues();
            assertNotNull(values);
            assertEquals(count, values.size());

            final FacetWrapper fieldValue = fieldValues.get(0);
            assertNotNull(fieldValue);
            assertEquals(tagName, fieldValue.getName());
            assertEquals(tagValue, fieldValue.getValue());
            assertEquals(count, fieldValue.getCount());
            assertEquals(selected, fieldValue.isSelected());
        }
    }

    private String tag(final String name, final String value) {
        return name + ":" + value;
    }
}
