package com.atex.plugins.search;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit test for {@link com.atex.plugins.search.FacetValueWrapper}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FacetValueWrapperTest extends AbstractTestCase {

    @Test
    public void test_constructor() {
        final String fieldName = RandomStringUtils.randomAlphabetic(10);
        final String facetValue = RandomStringUtils.randomAlphabetic(10);
        final String wrapName = RandomStringUtils.randomAlphabetic(10);
        final boolean selected = rnd.nextBoolean();

        final long count = rnd.nextInt();
        final FacetField field = new FacetField(fieldName);
        final FacetField.Count countField = new FacetField.Count(field, facetValue, count);


        final FacetValueWrapper value = new FacetValueWrapper(wrapName, countField, selected);

        assertEquals(wrapName, value.getName());
        assertEquals(facetValue, value.getValue());
        assertEquals(count, value.getCount());
        assertEquals(selected, value.isSelected());
        assertTrue(value.getFq().contains(fieldName));
        assertTrue(value.getFq().contains(facetValue));
    }
}
