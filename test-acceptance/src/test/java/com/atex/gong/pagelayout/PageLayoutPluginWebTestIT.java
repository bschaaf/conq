package com.atex.gong.pagelayout;

import com.atex.gong.AbstractWebTestBase;
import com.atex.gong.utils.ContentTestUtil;
import com.google.inject.Inject;
import com.polopoly.cm.client.CMException;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnj.TestNJRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(TestNJRunner.class)
@ImportTestContent(
        dir = "/com/atex/gong/pagelayout/",
        files = {"PageLayoutPluginWebTestIT.content" })
public class PageLayoutPluginWebTestIT extends AbstractWebTestBase {

    @Inject
    private ContentTestUtil contentUtil;
    private String siteId;

    @Before
    public void init() throws CMException {
        siteId = contentUtil.getTestContentId(".site");
        webTest.loadPage(String.format("/cmlink/%s", siteId));
    }

    @Test
    public void testResourcesInHeader() {
        assertBefore("jquery-1.11.0.min.js", "bootstrap.min.js");
        webTest.assertContainsHtml("bootstrap.min.css");
        webTest.assertContainsHtml("bootstrap.min.js");
    }

    private void assertBefore(final String before, final String after) {
        String source = webTest.getPageAsXml();
        int indexOfBefore = source.indexOf(before);
        int indexOfAfter = source.indexOf(after);
        if (indexOfBefore < 0) {
            Assert.fail(String.format("Unable to find requested string (%s) in source", before));
        }
        if (indexOfAfter < 0) {
            Assert.fail(String.format("Unable to find requested string (%s) in source", after));
        }
        Assert.assertTrue(String.format("Expected %s to be before %s", before, after), indexOfBefore < indexOfAfter);
    }
}
