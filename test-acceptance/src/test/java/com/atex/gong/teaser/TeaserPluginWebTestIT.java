package com.atex.gong.teaser;

import org.junit.Before;
import org.junit.Test;

import com.atex.gong.AbstractWebTestBase;
import com.atex.gong.utils.ContentTestUtil;
import com.google.inject.Inject;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.testnext.base.ImportTestContent;

@ImportTestContent(
        dir = "/com/atex/gong/teaser/",
        files = {"TeaserPluginWebTestIT.image.xml", "TeaserPluginWebTestIT.content" })
public class TeaserPluginWebTestIT extends AbstractWebTestBase {

    private static final String TEASER_NAME = "My article name";
    private static final String TEASER_TEXT = "My article lead";
    private static final String IMAGE_TITLE = "PHOTO BY My byline (My license URL)";
    private static final String IMAGE_ALT = "My image description";

    private String siteId;
    private String teaserableId;

    @Inject
    private ContentTestUtil contentUtil;

    @Before
    public void init() throws CMException {
        siteId = contentUtil.getTestContentId(".site");
        String teaserId = contentUtil.getTestContentId(".teaser");
        teaserableId = contentUtil.getTestContentId(".teaserable");
        webTest.loadPage(String.format("/cmlink/%s/%s", siteId, teaserId));
    }

    @Test
    public void testNamePresent() throws CMException {
        webTest.assertContains(TEASER_NAME);
    }

    @Test
    public void testTextPresent() {
        webTest.assertContains(TEASER_TEXT);
    }

    @Test
    public void testLinkPresent() throws CMException {
        ContentPolicy toTest = (ContentPolicy) contentUtil.getPolicyForTesting(teaserableId);
        Policy siteToTest = contentUtil.getPolicyForTesting(siteId);
        String toTestURL = toTest.getName().toLowerCase().replaceAll(" ", "-") + "-"
                + toTest.getContentId().getContentId().getContentIdString();
        webTest.assertContainsHtml(String.format("/cmlink/%s/%s",
                siteToTest.getContentId().getContentId().getContentIdString(),
                toTestURL));
    }

    @Test
    public void testImagePresent() {
        webTest.assertContainsXPath("//img[@src]");
    }

    @Test
    public void testImageAltTextPresent() {
        assertImageAttributeValue("alt", IMAGE_ALT);
    }

    private void assertImageAttributeValue(final String attribute, final String value) {
        String xPath = String.format("//img[@%s=\"%s\"]", attribute, value);
        webTest.assertContainsXPath(xPath);
    }

}
