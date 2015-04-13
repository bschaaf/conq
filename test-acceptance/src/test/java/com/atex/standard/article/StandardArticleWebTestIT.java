package com.atex.standard.article;

import static org.junit.Assert.fail;

import org.jsoup.Jsoup;
import org.junit.Before;
import org.junit.Test;

import com.atex.gong.AbstractWebTestBase;
import com.atex.gong.utils.ContentTestUtil;
import com.atex.plugins.baseline.policy.BaselinePolicy;
import com.google.inject.Inject;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.testnext.base.ImportTestContent;

/**
 * Tests visibility of fields, present in article, on site.
 */
@ImportTestContent(
        dir = "/com/atex/standard/article/")
public class StandardArticleWebTestIT extends AbstractWebTestBase {

    private static final String SITE_SUFFIX = ".site";

    private ContentPolicy toTest = null;

    @Inject
    private ContentTestUtil contentUtil;

    @Before
    public void init() {
        try {
            toTest = (ContentPolicy) contentUtil.getTestPolicy();
        } catch (CMException e) {
            fail("Unable to fetch test content, message: " + e.getMessage());
        }
        String idOfSite = contentUtil.getTestContentId(SITE_SUFFIX);
        String idOfTestContent = contentUtil.getTestContentId();
        webTest.loadPage(String.format("/cmlink/%s/%s", idOfSite, idOfTestContent));
    }

    @Test
    public void testTitlePresent() throws CMException {
        String title = toTest.getName();
        webTest.assertContains(title);
    }

    @Test
    public void testLeadPresent() throws CMException {
        String lead = toTest.getComponent("lead", "value");
        webTest.assertContains(lead);
    }

    @Test
    public void testBodyPresent() throws CMException {
        String body = toTest.getComponent("body", "value");
        body = Jsoup.parse(body).text();
        webTest.assertContains(body);
    }

    @Test
    public void testRelatedArticles() throws CMException {
        //Asserting no related article present for original article
        webTest.assertNotContainsXPath("Should not contain related header",
                                       "//h3[text()[contains(.,'Related')]]");

        String testSuffix = ".contentWithRelatedArticles";
        String idOfSite = contentUtil.getTestContentId(SITE_SUFFIX);
        String idOfTestContent = contentUtil.getTestContentId(testSuffix);
        ContentPolicy relatedArticlesArticle = (ContentPolicy) contentUtil.getTestPolicy(testSuffix);
        ContentPolicy otherArticle = (ContentPolicy) contentUtil.getTestPolicy(".contentWithRelatedTopics");
        webTest.loadPage(String.format("/cmlink/%s/%s", idOfSite, idOfTestContent));
        //Asserting we got the correct article
        webTest.assertContains(relatedArticlesArticle.getName());
        //Asserting related articles present
        webTest.assertContainsXPath("Should contain related header",
                                    "//h3[text()[contains(.,'Related')]]");
        webTest.assertContains(toTest.getName());
        webTest.assertContains(otherArticle.getName());
    }

    @Test
    public void testRelatedTopics() throws CMException {
        //Asserting no related topic present for original article
        webTest.assertNotContainsXPath("Should not contain related topic header",
                                       "//ul[contains(@class,'related-container')]"
                                       + "/li[text()[contains(.,'Related Topics')]]");

        String testSuffix = ".contentWithRelatedTopics";
        String idOfSite = contentUtil.getTestContentId(SITE_SUFFIX);
        String idOfTestContent = contentUtil.getTestContentId(testSuffix);
        ContentPolicy relatedArticlesArticle = (ContentPolicy) contentUtil.getTestPolicy(testSuffix);

        webTest.loadPage(String.format("/cmlink/%s/%s", idOfSite, idOfTestContent));
        //Asserting we got the correct article
        webTest.assertContains(relatedArticlesArticle.getName());
        //Asserting related articles present
        webTest.assertContainsXPath("Should contain related topic header",
                                    "//ul[contains(@class,'related-container')]"
                                    + "/li[text()[contains(.,'Related Topics')]]");
        //Asserting there's no link if no landing page
        webTest.assertContainsXPath("Should contain related topic 'politics'",
                                    "//ul[contains(@class,'related-container')]"
                                    + "/li/span[text()[contains(.,'politics')]]");

        testSuffix = ".contentWithRelatedTopicsAndLandingPage";
        idOfSite = contentUtil.getTestContentId(".siteWithLandingPage");
        idOfTestContent = contentUtil.getTestContentId(testSuffix);
        relatedArticlesArticle = (ContentPolicy) contentUtil.getTestPolicy(testSuffix);

        webTest.loadPage(String.format("/cmlink/%s/%s", idOfSite, idOfTestContent));
        //Asserting we got the correct article
        webTest.assertContains(relatedArticlesArticle.getName());
        //Asserting related articles present
        webTest.assertContainsXPath("Should contain related topic header",
                                    "//ul[contains(@class,'related-container')]"
                                    + "/li[text()[contains(.,'Related Topics')]]");
        //Asserting there IS a link if landing page present
        webTest.assertContainsXPath("Should contain related topic 'politics' and link to landing page",
                                    "//ul[contains(@class,'related-container')]"
                                    + "/li/a/span[text()[contains(.,'politics')]]");
    }

    @Test
    public void testContentLinkInBodyText() throws CMException {
        String testSuffix = ".contentWithContentLink";

        //Fetching ids for URL to load
        String idOfSite = contentUtil.getTestContentId(SITE_SUFFIX);
        String idOfTestContent = contentUtil.getTestContentId(testSuffix);

        //Fetching content for assertions
        ContentPolicy articleWithContentLink = (ContentPolicy) contentUtil.getTestPolicy(testSuffix);
        ContentPolicy testSite = (ContentPolicy) contentUtil.getTestPolicy(SITE_SUFFIX);

        String suffixOfContentLinked = ".contentWithRelatedArticles";
        BaselinePolicy linkedArticle = (BaselinePolicy) contentUtil.getTestPolicy(suffixOfContentLinked);

        // Asserting we got the correct article for this test
        webTest.loadPage(String.format("/cmlink/%s/%s", idOfSite, idOfTestContent));
        webTest.assertContains(articleWithContentLink.getName());

        // Asserting the link in the body text is correct
        String linkToAssert = String.format("<a href=\"/cmlink/%s/%s\">",
                                    testSite.getContentId().getContentId().getContentIdString(),
                                    "my-third-simple-article-with-related-articles-"
                                    + linkedArticle.getContentId().getContentId().getContentIdString());
        webTest.assertContainsHtml(linkToAssert);
    }
}
