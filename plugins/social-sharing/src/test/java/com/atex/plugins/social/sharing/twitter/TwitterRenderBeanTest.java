package com.atex.plugins.social.sharing.twitter;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit test for {@link com.atex.plugins.social.sharing.twitter.TwitterRenderBean}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TwitterRenderBeanTest {

    @Test
    public void testConfiguration() {

        final String text = RandomStringUtils.randomAlphabetic(10);
        final String count = RandomStringUtils.randomAlphabetic(10);
        final String language = RandomStringUtils.randomAlphabetic(10);
        final String size = RandomStringUtils.randomAlphabetic(5);
        final String related = RandomStringUtils.randomAlphabetic(10);
        final String attributes = RandomStringUtils.randomAlphabetic(30);

        final TwitterRenderBean conf = new TwitterRenderBean(
                text,
                count,
                language,
                size,
                related,
                attributes
        );

        Assert.assertEquals(text, conf.getText());
        Assert.assertEquals(count, conf.getCount());
        Assert.assertEquals(language, conf.getLanguage());
        Assert.assertEquals(size, conf.getSize());
        Assert.assertEquals(related, conf.getRelated());
        Assert.assertEquals(attributes, conf.getAttributes());

    }

    @Test
    public void testNullReturn() {

        final TwitterRenderBean conf = new TwitterRenderBean(
                "",
                "",
                "",
                "",
                "",
                ""
        );

        Assert.assertNull(conf.getText());
        Assert.assertNull(conf.getCount());
        Assert.assertNull(conf.getLanguage());
        Assert.assertNull(conf.getSize());
        Assert.assertNull(conf.getRelated());
        Assert.assertNull(conf.getAttributes());

    }

}
