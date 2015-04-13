package com.atex.plugins.teaser;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.testnext.base.ImportTestContent;
import org.junit.Before;
import org.junit.Test;

/**
 * Test teaser visible on site.
 */
@ImportTestContent
public class TeaserWebTestIT extends WebTestBase {

    private static final String TEST_CONTENT_SUFFIX = ".content";
    private static final String SITE_SUFFIX = ".site";
    private String teaserWithNoTeaserableContentId;
    private String teaserWithTeaserableContentId;
    private String siteId;

    @Before
    public void init() throws CMException {
        String className = this.getClass().getSimpleName();
        teaserWithNoTeaserableContentId = className + TEST_CONTENT_SUFFIX;
        teaserWithTeaserableContentId = className + ".teaser.with.teaserables";
        siteId = className + SITE_SUFFIX;
    }

    @Test
    public void testNamePresent() throws CMException {
        webTest.loadPage(String.format("/cmlink/%s/%s", siteId, teaserWithNoTeaserableContentId));
        webTest.assertContainsHtml("My simple teaser name");
    }

    @Test
    public void testTextPresent() {
        webTest.loadPage(String.format("/cmlink/%s/%s", siteId, teaserWithNoTeaserableContentId));
        webTest.assertContainsHtml("My simple teaser text");
    }

    @Test
    public void testLinkPresent() throws CMException {
        ContentId contentId = ((TeaserPolicy) getPolicy(teaserWithTeaserableContentId))
                .getTeaserableContentId().getContentId();
        ContentPolicy toTest = (ContentPolicy) getPolicy(contentId);
        ContentPolicy siteToTest = (ContentPolicy) getPolicy(siteId);
        webTest.loadPage(String.format("/cmlink/%s/%s", siteId, teaserWithTeaserableContentId));
        webTest.assertContainsHtml(String.format("/cmlink/%s/%s",
                siteToTest.getContentId().getContentId().getContentIdString(),
                toTest.getContentId().getContentId().getContentIdString()));
    }

    @Test
    public void testImagePresent() {
        webTest.loadPage(String.format("/cmlink/%s/%s", siteId, teaserWithTeaserableContentId));
        webTest.assertContainsHtml("<img src=\"\" alt=\"\" title=\"My teaserable's image\"/>");
    }

    @Test
    public void testAttributesPresent() {
        webTest.loadPage(String.format("/cmlink/%s/%s", siteId, teaserWithTeaserableContentId));
        webTest.assertContains("my-undefined-language-key");
        final String xPath = "//span[@class='my-attribute-css-class']";
        webTest.assertContainsXPath(xPath);
        // The question marks are present in the assert below because "my-undefined-language-key" is not a defined
        // language key.
        webTest.assertContainsByXPath(xPath, "???my-undefined-language-key???");
    }
}
