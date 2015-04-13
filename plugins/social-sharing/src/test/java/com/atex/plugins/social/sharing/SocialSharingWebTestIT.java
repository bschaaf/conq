package com.atex.plugins.social.sharing;

import com.google.inject.Inject;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.ContentOperationFailedException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.testnext.base.ChangeList;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnext.web.WebTest;
import com.polopoly.testnj.TestNJRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(TestNJRunner.class)
@ImportTestContent
public class SocialSharingWebTestIT {

    private static final int HTML_STATUS_CODE_OK = 200;

    @Inject
    private WebTest webTest;

    @Inject
    private ChangeList changeList;

    @Inject
    private PolicyCMServer cmServer;

    private String siteId;
    private String articleId;

    public static final String ID_PREFIX = "SocialSharingWebTestIT";

    @Before
    public void init() throws Exception {
        siteId = ID_PREFIX + ".site";
        articleId = ID_PREFIX + ".content";

        changeList.waitFor("preview");
        webTest.loadPage(String.format("/cmlink/%s/%s", siteId, articleId));
    }

    @Test
    public void testBasics() {
        webTest.validatePageHtml();
        webTest.validateUnknownVelocityVariables();
        webTest.assertStatusCode(HTML_STATUS_CODE_OK);
    }

    @Test
    public void testHeaders() throws Exception {
        // There is no og:image tag because it's too difficult to mock without a test project.

        assertMetaTag("og:title", "TITLE");
        assertMetaTag("og:site_name", "Test site for social sharing web test");
        // This test is done without domain aliases. With proper aliases, this link would be absolute.
        assertMetaTag("og:url", getArticleCanonicalUrl());
        assertMetaTag("og:description", "DESCRIPTION");
        assertMetaTag("og:type", "article");
    }

    @Test
    public void testFacebookShareButtonOnPage() throws Exception {
        webTest.assertXPathExpression("//div[@class='fb-share-button']/@data-href", getArticleCanonicalUrl());
    }

    private String getArticleCanonicalUrl() throws Exception {
        Policy site = getPolicyForTesting(siteId);
        Policy article = getPolicyForTesting(articleId);
        return String.format("/cmlink/%s/%s", getContentId(site), getContentId(article));
    }

    private String getContentId(final Policy site) {
        return site.getContentId().getContentId().getContentIdString();
    }

    private void assertMetaTag(final String property, final String expectedContent) {
        webTest.assertXPathExpression(
                String.format("/html/head/meta[@property='%s']/@content", property), expectedContent);
    }

    private Policy getPolicyForTesting(final String externalContentIdString) throws Exception {
        ExternalContentId externalContentId = new ExternalContentId(externalContentIdString);
        ContentId contentId = cmServer.findContentIdByExternalId(externalContentId);
        if (contentId == null) {
            throw new ContentOperationFailedException("Unable to find content using id "
                    + externalContentId.getExternalId());
        }
        return cmServer.getPolicy(contentId);
    }
}
