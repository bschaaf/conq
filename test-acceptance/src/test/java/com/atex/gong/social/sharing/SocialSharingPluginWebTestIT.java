package com.atex.gong.social.sharing;

import com.atex.gong.AbstractWebTestBase;
import com.atex.gong.utils.ContentTestUtil;
import com.google.inject.Inject;
import com.polopoly.cm.policy.Policy;
import com.polopoly.testnext.base.ImportTestContent;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@ImportTestContent(
        dir = "/com/atex/gong/social-sharing/",
        files = {"SocialSharingPluginWebTestIT.image.xml", "SocialSharingPluginWebTestIT.content" })
public class SocialSharingPluginWebTestIT extends AbstractWebTestBase {

    private String siteId;
    private String articleId;
    private String imageId;

    @Inject
    private ContentTestUtil contentUtil;

    @Before
    public void init() throws Exception {
        siteId = contentUtil.getTestContentId(".site");
        articleId = contentUtil.getTestContentId(".article");
        imageId = contentUtil.getTestContentId(".image");

        webTest.loadPage(String.format("/cmlink/%s/%s", siteId, articleId));
    }

    @Test
    public void testOpenGraphHeaders() throws Exception {
        Policy image = contentUtil.getPolicyForTesting(imageId);

        assertMetaTag("og:title", "Social title");
        assertMetaTag("og:site_name", "Test site for social sharing web test");
        // This test is done without domain aliases. With proper aliases, this link would be absolute.
        assertMetaTag("og:url", getArticleCanonicalUrl());
        assertMetaTag("og:description", "Social lead");
        assertMetaTag("og:type", "article");
        assertMetaTagMatchesRegex("og:image",
                String.format("/image/policy:%s:[0-9]+/image.jpg\\?a=191%%3A100&w=1200.*", getContentId(image)));
    }


    @Test
    public void testFacebookShareButtonOnPage() throws Exception {
        webTest.assertXPathExpression("//div[@class='fb-share-button']/@data-href", getArticleCanonicalUrl());
    }

    @Test
    public void testGooglePlusShareButtonOnPage() throws Exception {
        webTest.assertXPathExpression("//div[@class='g-plus']/@data-href", getArticleCanonicalUrl());
    }

    @Test
    public void testTwitterShareButtonOnPage() throws Exception {
        webTest.assertXPathExpression("//a[@class='twitter-share-button']/@data-url", getArticleCanonicalUrl());
    }

    private String getArticleCanonicalUrl() throws Exception {
        Policy site = contentUtil.getPolicyForTesting(siteId);
        Policy article = contentUtil.getPolicyForTesting(articleId);
        return String.format("/cmlink/%s/%s", getContentId(site), "social-title-" + getContentId(article));
    }

    private String getContentId(final Policy site) {
        return site.getContentId().getContentId().getContentIdString();
    }

    private void assertMetaTag(final String property, final String expectedContent) {
        webTest.assertXPathExpression(
                String.format("/html/head/meta[@property='%s']/@content", property), expectedContent);
    }

    private void assertMetaTagMatchesRegex(final String property, final String expectedPattern) {
        assertMatchesRegex(String.format("/html/head/meta[@property='%s']/@content", property), expectedPattern);
    }

    private void assertMatchesRegex(final String xpathExpression, final String expectedRegex) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        InputSource inputSource = new InputSource(new StringReader(webTest.getPageAsXml()));
        try {
            String value = (String) xpath.evaluate(xpathExpression, inputSource, XPathConstants.STRING);
            assertTrue("For the XPath expression '" + xpathExpression
                    + "' Expected value to match regex: '" + expectedRegex
                    + "' but got '" + value + "'", value.matches(expectedRegex));
        } catch (XPathExpressionException e) {
            fail("Failed to evaluate xpath expression: " + xpathExpression);
        }
    }
}
