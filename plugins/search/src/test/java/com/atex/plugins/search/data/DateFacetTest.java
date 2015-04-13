package com.atex.plugins.search.data;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.atex.plugins.search.AbstractTestCase;

/**
 * Unit test for {@link com.atex.plugins.search.data.DateFacet}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DateFacetTest extends AbstractTestCase {

    @Test
    public void test_constructor() {
        final String name = RandomStringUtils.randomAlphabetic(10);
        final String start = RandomStringUtils.randomAlphabetic(10);
        final String end = RandomStringUtils.randomAlphabetic(10);
        final String gap = RandomStringUtils.randomAlphabetic(10);
        final String formatValue = RandomStringUtils.randomAlphabetic(10);

        final DateFacet facet = new DateFacet(name, start, end, gap, formatValue);

        assertEquals(name, facet.getName());
        assertEquals(start, facet.getStart());
        assertEquals(end, facet.getEnd());
        assertEquals(gap, facet.getGap());
        assertEquals(formatValue, facet.getFormatValue());
    }
}
