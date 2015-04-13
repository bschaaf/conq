package com.atex.plugins.search;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit test for {@link com.atex.plugins.search.FacetFieldWrapper}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FacetFieldWrapperTest extends AbstractTestCase {

    @Test
    public void test_constructor() {
        final String name = RandomStringUtils.randomAlphabetic(10);

        final FacetFieldWrapper value = new FacetFieldWrapper(name);

        assertEquals(name, value.getName());
        assertNotNull(value.getValues());
        assertEquals(0, value.getValues().size());
    }

    @Test
    public void test_facet_values() {
        final String name = RandomStringUtils.randomAlphabetic(10);
        final FacetValueWrapper wrap1 = getRandomFacetValue();
        final FacetValueWrapper wrap2 = getRandomFacetValue();

        final FacetFieldWrapper value = new FacetFieldWrapper(name);
        value.addFacetValue(wrap1);
        value.addFacetValue(wrap2);

        assertEquals(name, value.getName());
        assertNotNull(value.getValues());
        assertEquals(2, value.getValues().size());

        assertEquals(wrap1, value.getValues().get(0));
        assertEquals(wrap2, value.getValues().get(1));
    }

    private FacetValueWrapper getRandomFacetValue() {
        final String fieldName = RandomStringUtils.randomAlphabetic(10);
        final String facetValue = RandomStringUtils.randomAlphabetic(10);
        final String wrapName = RandomStringUtils.randomAlphabetic(10);
        final boolean selected = rnd.nextBoolean();

        final long count = rnd.nextInt();
        final FacetField field = new FacetField(fieldName);
        final FacetField.Count countField = new FacetField.Count(field, facetValue, count);

        return new FacetValueWrapper(wrapName, countField, selected);
    }

}
