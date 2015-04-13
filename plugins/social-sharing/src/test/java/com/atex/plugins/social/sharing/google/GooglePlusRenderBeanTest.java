package com.atex.plugins.social.sharing.google;

import java.util.Date;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit test for {@link GooglePlusRenderBean}.
 */
@RunWith(MockitoJUnitRunner.class)
public class GooglePlusRenderBeanTest {

    private final Random rnd = new Random(new Date().getTime());

    @Test
    public void testConfiguration() {

        final String annotation = RandomStringUtils.randomAlphabetic(10);
        final String height = RandomStringUtils.randomNumeric(3);
        final String width = Integer.toString(rnd.nextInt(100) + 120);
        final String attributes = RandomStringUtils.randomNumeric(30);
        final boolean useShare = rnd.nextBoolean();

        final GooglePlusRenderBean conf =
                new GooglePlusRenderBean(annotation, height, width, attributes, useShare);

        Assert.assertEquals(annotation, conf.getAnnotation());
        Assert.assertEquals(height, conf.getHeight());
        Assert.assertEquals(width, conf.getWidth());
        Assert.assertEquals(attributes, conf.getAttributes());
        Assert.assertEquals(useShare, conf.isShareButton());

    }

    @Test
    public void testNullReturn() {

        final boolean useShare = rnd.nextBoolean();

        final GooglePlusRenderBean conf = new GooglePlusRenderBean("", "", "", "", useShare);

        Assert.assertNull(conf.getAnnotation());
        Assert.assertNull(conf.getHeight());
        Assert.assertNull(conf.getWidth());
        Assert.assertNull(conf.getAttributes());
        Assert.assertEquals(useShare, conf.isShareButton());

    }

}
