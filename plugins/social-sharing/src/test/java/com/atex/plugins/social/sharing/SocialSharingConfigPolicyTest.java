package com.atex.plugins.social.sharing;

import java.util.Date;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit test for {@link com.atex.plugins.social.sharing.SocialSharingConfigPolicy}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SocialSharingConfigPolicyTest {

    private final Random rnd = new Random(new Date().getTime());

    @Mock
    private SocialSharingConfigPolicy policy;

    @Test
    public void testGetFacebookAppId() {
        final String appId = RandomStringUtils.randomAlphanumeric(20);

        Mockito.when(policy.getFacebookAppId()).thenCallRealMethod();
        Mockito.when(policy.getChildValue(
                Mockito.eq(SocialSharingConfigPolicy.FACEBOOK_APPID_FIELD),
                Mockito.anyString()))
                .thenReturn(null)
                .thenReturn("")
                .thenReturn(appId);

        Assert.assertEquals("", policy.getFacebookAppId());
        Assert.assertEquals("", policy.getFacebookAppId());
        Assert.assertEquals(appId, policy.getFacebookAppId());
    }

    @Test
    public void testGetGooglePlusUrl() {

        final String url = RandomStringUtils.randomAlphanumeric(20);

        Mockito.when(policy.getGooglePlusUrl()).thenCallRealMethod();
        Mockito.when(policy.getChildValue(
                Mockito.eq(SocialSharingConfigPolicy.GPLUS_URL_FIELD),
                Mockito.anyString()))
               .thenReturn(null)
               .thenReturn("")
               .thenReturn(url);

        Assert.assertEquals("", policy.getGooglePlusUrl());
        Assert.assertEquals("", policy.getGooglePlusUrl());
        Assert.assertEquals(url, policy.getGooglePlusUrl());
    }

    @Test
    public void testGetGoogleType() {

        final String typeValue = "1";

        Mockito.when(policy.getGoogleType()).thenCallRealMethod();
        Mockito.when(policy.getChildValue(
                Mockito.eq(SocialSharingConfigPolicy.GPLUS_TYPE_FIELD),
                Mockito.anyString()))
               .thenReturn(null)
               .thenReturn("")
               .thenReturn(typeValue);

        Assert.assertEquals(SocialSharingConfigPolicy.GPLUS_TYPE_DEFAULT, policy.getGoogleType());
        Assert.assertEquals(SocialSharingConfigPolicy.GPLUS_TYPE_DEFAULT, policy.getGoogleType());
        Assert.assertEquals(typeValue, policy.getGoogleType());
    }

    @Test
    public void testGetGoogleAnnotation() {

        final String annotation = RandomStringUtils.randomAlphanumeric(20);

        Mockito.when(policy.getGoogleAnnotation()).thenCallRealMethod();
        Mockito.when(policy.getChildValue(
                Mockito.eq(SocialSharingConfigPolicy.GPLUS_ANNOTATION_FIELD),
                Mockito.anyString()))
               .thenReturn(null)
               .thenReturn("")
               .thenReturn(annotation);

        Assert.assertEquals(SocialSharingConfigPolicy.GPLUS_ANNOTATION_DEFAULT, policy.getGoogleAnnotation());
        Assert.assertEquals(SocialSharingConfigPolicy.GPLUS_ANNOTATION_DEFAULT, policy.getGoogleAnnotation());
        Assert.assertEquals(annotation, policy.getGoogleAnnotation());
    }

    @Test
    public void testGetGoogleHeight() throws Exception {

        final String height = Integer.toString(rnd.nextInt(100) + 120);

        Mockito.when(policy.getGoogleHeight()).thenCallRealMethod();
        Mockito.when(policy.getChildValue(
                Mockito.eq(SocialSharingConfigPolicy.GPLUS_HEIGHT_FIELD),
                Mockito.anyString()))
               .thenReturn(null)
               .thenReturn("")
               .thenReturn(height);

        Assert.assertEquals("20", policy.getGoogleHeight());
        Assert.assertEquals("20", policy.getGoogleHeight());
        Assert.assertEquals(height, policy.getGoogleHeight());
    }

    @Test
    public void testGetGoogleWidth() throws Exception {

        final int size = rnd.nextInt(100) + 120;

        Mockito.when(policy.getGoogleWidth()).thenCallRealMethod();
        Mockito.when(policy.getChildValue(
                Mockito.eq(SocialSharingConfigPolicy.GPLUS_WIDTH_FIELD),
                Mockito.anyString()))
               .thenReturn(null)
               .thenReturn("")
               .thenReturn(RandomStringUtils.randomAlphabetic(10))
               .thenReturn(Integer.toString(SocialSharingConfigPolicy.GPLUS_WIDTH_DEFAULT - 20))
               .thenReturn(Integer.toString(size));

        Assert.assertEquals("", policy.getGoogleWidth());
        Assert.assertEquals("", policy.getGoogleWidth());
        Assert.assertEquals(Integer.toString(SocialSharingConfigPolicy.GPLUS_WIDTH_DEFAULT), policy.getGoogleWidth());
        Assert.assertEquals(Integer.toString(SocialSharingConfigPolicy.GPLUS_WIDTH_DEFAULT), policy.getGoogleWidth());
        Assert.assertEquals(Integer.toString(size), policy.getGoogleWidth());
    }

    @Test
    public void testGetGoogleAttributes() throws Exception {

        final String attributes = RandomStringUtils.randomAlphanumeric(20);

        Mockito.when(policy.getGoogleAttributes()).thenCallRealMethod();
        Mockito.when(policy.getChildValue(
                Mockito.eq(SocialSharingConfigPolicy.GPLUS_ATTRIBUTES_FIELD),
                Mockito.anyString()))
               .thenReturn(null)
               .thenReturn("")
               .thenReturn(attributes);

        Assert.assertEquals("", policy.getGoogleAttributes());
        Assert.assertEquals("", policy.getGoogleAttributes());
        Assert.assertEquals(attributes, policy.getGoogleAttributes());
    }

    @Test
    public void testGetGoogleLanguage() throws Exception {

        final String language = RandomStringUtils.randomAlphanumeric(20);

        Mockito.when(policy.getGoogleLanguage()).thenCallRealMethod();
        Mockito.when(policy.getChildValue(
                Mockito.eq(SocialSharingConfigPolicy.GPLUS_LANG_FIELD),
                Mockito.anyString()))
               .thenReturn(null)
               .thenReturn("")
               .thenReturn(language);

        Assert.assertEquals("", policy.getGoogleLanguage());
        Assert.assertEquals("", policy.getGoogleLanguage());
        Assert.assertEquals(language, policy.getGoogleLanguage());
    }

    @Test
    public void testGetTwitterText() throws Exception {

        final String text = RandomStringUtils.randomAlphanumeric(20);

        Mockito.when(policy.getTwitterText()).thenCallRealMethod();
        Mockito.when(policy.getChildValue(
                Mockito.eq(SocialSharingConfigPolicy.TWITTER_TEXT_FIELD),
                Mockito.anyString()))
               .thenReturn(null)
               .thenReturn("")
               .thenReturn(text);

        Assert.assertEquals("", policy.getTwitterText());
        Assert.assertEquals("", policy.getTwitterText());
        Assert.assertEquals(text, policy.getTwitterText());
    }

    @Test
    public void testGetTwitterCount() throws Exception {

        final String text = RandomStringUtils.randomAlphanumeric(20);

        Mockito.when(policy.getTwitterCount()).thenCallRealMethod();
        Mockito.when(policy.getChildValue(
                Mockito.eq(SocialSharingConfigPolicy.TWITTER_COUNT_FIELD),
                Mockito.anyString()))
               .thenReturn(null)
               .thenReturn("")
               .thenReturn(text);

        Assert.assertEquals("none", policy.getTwitterCount());
        Assert.assertEquals("none", policy.getTwitterCount());
        Assert.assertEquals(text, policy.getTwitterCount());
    }

    @Test
    public void testGetTwitterLanguage() throws Exception {

        final String text = RandomStringUtils.randomAlphanumeric(20);

        Mockito.when(policy.getTwitterLanguage()).thenCallRealMethod();
        Mockito.when(policy.getChildValue(
                Mockito.eq(SocialSharingConfigPolicy.TWITTER_LANG_FIELD),
                Mockito.anyString()))
               .thenReturn(null)
               .thenReturn("")
               .thenReturn(text);

        Assert.assertEquals("", policy.getTwitterLanguage());
        Assert.assertEquals("", policy.getTwitterLanguage());
        Assert.assertEquals(text, policy.getTwitterLanguage());
    }

    @Test
    public void testGetTwitterSize() throws Exception {

        final String text = RandomStringUtils.randomAlphanumeric(20);

        Mockito.when(policy.getTwitterSize()).thenCallRealMethod();
        Mockito.when(policy.getChildValue(
                Mockito.eq(SocialSharingConfigPolicy.TWITTER_SIZE_FIELD),
                Mockito.anyString()))
               .thenReturn(null)
               .thenReturn("")
               .thenReturn(text);

        Assert.assertEquals("medium", policy.getTwitterSize());
        Assert.assertEquals("medium", policy.getTwitterSize());
        Assert.assertEquals(text, policy.getTwitterSize());
    }

    @Test
    public void testGetTwitterRelated() throws Exception {

        final String text = RandomStringUtils.randomAlphanumeric(20);

        Mockito.when(policy.getTwitterRelated()).thenCallRealMethod();
        Mockito.when(policy.getChildValue(
                Mockito.eq(SocialSharingConfigPolicy.TWITTER_RELATED_FIELD),
                Mockito.anyString()))
               .thenReturn(null)
               .thenReturn("")
               .thenReturn(text);

        Assert.assertEquals("", policy.getTwitterRelated());
        Assert.assertEquals("", policy.getTwitterRelated());
        Assert.assertEquals(text, policy.getTwitterRelated());
    }


    @Test
    public void testGetTwitterAttributes() throws Exception {

        final String text = RandomStringUtils.randomAlphanumeric(20);

        Mockito.when(policy.getTwitterAttributes()).thenCallRealMethod();
        Mockito.when(policy.getChildValue(
                Mockito.eq(SocialSharingConfigPolicy.TWITTER_ATTRIBUTES_FIELD),
                Mockito.anyString()))
               .thenReturn(null)
               .thenReturn("")
               .thenReturn(text);

        Assert.assertEquals("", policy.getTwitterAttributes());
        Assert.assertEquals("", policy.getTwitterAttributes());
        Assert.assertEquals(text, policy.getTwitterAttributes());
    }

}
