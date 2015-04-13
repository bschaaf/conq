package com.atex.plugins.search;

import com.atex.plugins.search.data.DateFacet;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.polopoly.cm.client.CMException;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Unit test for {@link com.atex.plugins.search.SearchConfigurationPolicy}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchConfigurationPolicyTest extends AbstractTestCase {

    @Mock
    private SearchConfigurationPolicy policy;

    @Test
    public void test_getFacetFields_null() {
        Mockito.when(policy.getFacetFields()).thenCallRealMethod();

        Mockito
                .when(policy.getChildValue(
                        Mockito.eq(SearchConfigurationPolicy.FACETFIELDS),
                        Mockito.anyString()))
                .thenReturn(null);

        assertEquals("", policy.getFacetFields());
    }

    @Test
    public void test_getFacetFields_value() {
        Mockito.when(policy.getFacetFields()).thenCallRealMethod();

        final String value = RandomStringUtils.randomAlphabetic(10);

        Mockito
                .when(policy.getChildValue(
                        Mockito.eq(SearchConfigurationPolicy.FACETFIELDS),
                        Mockito.anyString()))
                .thenReturn(value);

        assertEquals(value, policy.getFacetFields());
    }

    @Test
    public void test_getFacetFieldsList_empty() {
        Mockito.when(policy.getFacetFieldsList()).thenCallRealMethod();

        Mockito.when(policy.getFacetFields()).thenReturn("");

        final List<String> facetFields = policy.getFacetFieldsList();
        assertNotNull(facetFields);
        assertEquals(0, facetFields.size());
    }

    @Test
    public void test_getFacetFieldsList_one() {
        Mockito.when(policy.getFacetFieldsList()).thenCallRealMethod();

        final String value = RandomStringUtils.randomAlphabetic(10);
        Mockito.when(policy.getFacetFields()).thenReturn(value);

        final List<String> facetFields = policy.getFacetFieldsList();
        assertNotNull(facetFields);
        assertEquals(1, facetFields.size());
        assertEquals(value, facetFields.get(0));
    }

    @Test
    public void test_getFacetFieldsList_more() {
        Mockito.when(policy.getFacetFieldsList()).thenCallRealMethod();

        final String value1 = RandomStringUtils.randomAlphabetic(10);
        final String value2 = RandomStringUtils.randomAlphabetic(10);
        final String value3 = RandomStringUtils.randomAlphabetic(10);

        Mockito.when(policy.getFacetFields()).thenReturn(Joiner.on(',').join(value1, value2, value3));

        final List<String> facetFields = policy.getFacetFieldsList();
        assertNotNull(facetFields);
        assertEquals(3, facetFields.size());
        assertEquals(value1, facetFields.get(0));
        assertEquals(value2, facetFields.get(1));
        assertEquals(value3, facetFields.get(2));
    }

    @Test
    public void test_getFacetGroups_not_null_for_no_data() {
        Mockito.when(policy.getDateFacets()).thenCallRealMethod();

        final List<DateFacet> facets = policy.getDateFacets();
        assertNotNull(facets);
        assertEquals(0, facets.size());
    }

    @Test
    public void test_get_one_date_facet() throws CMException {
        Mockito.when(policy.getDateFacets()).thenCallRealMethod();

        final String fieldName = RandomStringUtils.randomAlphabetic(10);
        final String start = RandomStringUtils.randomAlphabetic(10);
        final String end = RandomStringUtils.randomAlphabetic(10);
        final String gap = RandomStringUtils.randomAlphabetic(10);

        final Map<String, String> map = Maps.newHashMap();
        map.put(fieldName + SearchConfigurationPolicy.START_SUFFIX, start);
        map.put(fieldName + SearchConfigurationPolicy.END_SUFFIX, end);
        map.put(fieldName + SearchConfigurationPolicy.GAP_SUFFIX, gap);

        setup_policy_components(map);

        final List<DateFacet> facets = policy.getDateFacets();
        assertNotNull(facets);
        assertEquals(1, facets.size());

        final DateFacet f = facets.get(0);
        assertNotNull(f);
        assertEquals(fieldName, f.getName());
        assertEquals(start, f.getStart());
        assertEquals(end, f.getEnd());
        assertEquals(gap, f.getGap());
    }

    @Test
    public void test_get_two_date_facets() throws CMException {
        Mockito.when(policy.getDateFacets()).thenCallRealMethod();

        final String fieldName1 = "f1" + RandomStringUtils.randomAlphabetic(10);
        final String start1 = "f1" + RandomStringUtils.randomAlphabetic(10);
        final String end1 = "f1" + RandomStringUtils.randomAlphabetic(10);
        final String gap1 = "f1" + RandomStringUtils.randomAlphabetic(10);
        final String fieldName2 = "f2" + RandomStringUtils.randomAlphabetic(10);
        final String start2 = "f2" + RandomStringUtils.randomAlphabetic(10);
        final String end2 = "f2" + RandomStringUtils.randomAlphabetic(10);
        final String gap2 = "f2" + RandomStringUtils.randomAlphabetic(10);

        final Map<String, String> map = Maps.newHashMap();
        map.put(fieldName1 + SearchConfigurationPolicy.START_SUFFIX, start1);
        map.put(fieldName1 + SearchConfigurationPolicy.END_SUFFIX, end1);
        map.put(fieldName1 + SearchConfigurationPolicy.GAP_SUFFIX, gap1);
        map.put(fieldName2 + SearchConfigurationPolicy.START_SUFFIX, start2);
        map.put(fieldName2 + SearchConfigurationPolicy.END_SUFFIX, end2);
        map.put(fieldName2 + SearchConfigurationPolicy.GAP_SUFFIX, gap2);

        setup_policy_components(map);

        final List<DateFacet> facets = policy.getDateFacets();
        assertNotNull(facets);
        assertEquals(2, facets.size());

        final DateFacet f1 = facets.get(0);
        assertNotNull(f1);
        assertEquals(fieldName1, f1.getName());
        assertEquals(start1, f1.getStart());
        assertEquals(end1, f1.getEnd());
        assertEquals(gap1, f1.getGap());

        final DateFacet f2 = facets.get(1);
        assertNotNull(f2);
        assertEquals(fieldName2, f2.getName());
        assertEquals(start2, f2.getStart());
        assertEquals(end2, f2.getEnd());
        assertEquals(gap2, f2.getGap());
    }

    @Test
    public void test_get_results_size_from_empty() {

        Mockito.when(policy.getResultsPageSize()).thenCallRealMethod();

        Mockito
                .when(policy.getChildValue(
                        Mockito.eq(SearchConfigurationPolicy.RESULTSPAGESIZE),
                        Mockito.anyString()))
                .thenReturn(null)
                .thenReturn("")
                .thenReturn(RandomStringUtils.randomAlphabetic(5));

        assertEquals(SearchConfigurationPolicy.DEFAULT_RESULTS_PAGE_SIZE, policy.getResultsPageSize());
        assertEquals(SearchConfigurationPolicy.DEFAULT_RESULTS_PAGE_SIZE, policy.getResultsPageSize());
        assertEquals(SearchConfigurationPolicy.DEFAULT_RESULTS_PAGE_SIZE, policy.getResultsPageSize());
    }

    @Test
    public void test_get_results_size() {

        Mockito.when(policy.getResultsPageSize()).thenCallRealMethod();

        final int pageSize = rnd.nextInt(100) + SearchConfigurationPolicy.DEFAULT_RESULTS_PAGE_SIZE;
        Mockito
                .when(policy.getChildValue(
                        Mockito.eq(SearchConfigurationPolicy.RESULTSPAGESIZE),
                        Mockito.anyString()))
                .thenReturn(Integer.toString(pageSize));

        assertEquals(pageSize, policy.getResultsPageSize());
    }

    @Test
    public void test_query_type() {
        Mockito.when(policy.getQueryType()).thenCallRealMethod();

        final String queryType = RandomStringUtils.randomAlphanumeric(10);

        Mockito
                .when(policy.getChildValue(
                        Mockito.eq(SearchConfigurationPolicy.QUERYTYPE),
                        Mockito.anyString()))
                .thenReturn(null)
                .thenReturn("")
                .thenReturn(queryType);

        assertEquals("", policy.getQueryType());
        assertEquals("", policy.getQueryType());
        assertEquals(queryType, policy.getQueryType());
    }

    private void setup_policy_components(final Map<String, String> map) throws CMException {
        String[] keys = map.keySet().toArray(new String[map.keySet().size()]);
        Arrays.sort(keys);
        Mockito.when(policy.getComponentNames(
                Mockito.eq(SearchConfigurationPolicy.FACETDATES)))
                .thenReturn(keys);
        Mockito.when(policy.getComponent(
                Mockito.eq(SearchConfigurationPolicy.FACETDATES),
                Mockito.anyString()))
                .thenAnswer(new Answer<String>() {
                    @Override
                    public String answer(final InvocationOnMock invocationOnMock) throws Throwable {
                        return map.get(invocationOnMock.getArguments()[1]);
                    }
                });
    }

}
