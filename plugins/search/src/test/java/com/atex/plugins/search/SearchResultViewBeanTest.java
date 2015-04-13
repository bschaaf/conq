package com.atex.plugins.search;

import com.polopoly.cm.ContentId;
import junit.framework.TestCase;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;
import java.util.Random;

/**
 * Unit test for {@link com.atex.plugins.search.SearchResultViewBean}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchResultViewBeanTest extends TestCase {
    private Random rnd = new Random(new Date().getTime());

    @Test
    public void test_contentId() {
        final ContentId contentId = getRandomContentId(1);

        final SearchResultViewBean bean = new SearchResultViewBean();
        bean.setContentId(contentId);

        assertEquals(contentId, bean.getContentId());
    }

    @Test
    public void test_name() {
        final String text = RandomStringUtils.randomAlphabetic(20);

        final SearchResultViewBean bean = new SearchResultViewBean();
        bean.setName(text);

        assertEquals(text, bean.getName());
    }

    @Test
    public void test_text() {
        final String text = RandomStringUtils.randomAlphabetic(20);

        final SearchResultViewBean bean = new SearchResultViewBean();
        bean.setText(text);

        assertEquals(text, bean.getText());
    }

    private ContentId getRandomContentId(final int major) {
        final int minor = Math.abs(rnd.nextInt());
        return new ContentId(major, minor);
    }

}
