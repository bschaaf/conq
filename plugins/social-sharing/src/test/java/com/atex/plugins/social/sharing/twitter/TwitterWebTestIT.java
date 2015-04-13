package com.atex.plugins.social.sharing.twitter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.atex.plugins.social.sharing.ConfigurationTestUtil;
import com.google.inject.Inject;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.testnext.base.ChangeList;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnext.web.WebTest;
import com.polopoly.testnj.TestNJRunner;

@RunWith(TestNJRunner.class)
@ImportTestContent
public class TwitterWebTestIT {

    private static final String XPATH = "//a[@class='twitter-share-button']";

    @Inject
    private WebTest webTest;

    @Inject
    private ChangeList changeList;

    @Inject
    private PolicyCMServer cmServer;

    private String siteId;
    private String articleId;
    private ContentId numericSiteId;
    private ContentId numericArticleId;
    private ConfigurationTestUtil confUtil;

    public static final String ID_PREFIX = "TwitterWebTestIT";

    @Before
    public void init() throws Exception {
        confUtil = new ConfigurationTestUtil(cmServer);

        // We need to set configuration through code in order to reset after testing is finished
        confUtil.addConfiguration("twitter.text", "value", "default text");
        confUtil.addConfiguration("twitter.count", "count", "1");
        confUtil.addConfiguration("twitter.count", "selected_0", "horizontal");
        confUtil.addConfiguration("twitter.lang", "value", "fr");
        confUtil.addConfiguration("twitter.related", "value", "atexglobal");
        confUtil.addConfiguration("twitter.size", "count", "1");
        confUtil.addConfiguration("twitter.size", "selected_0", "large");
        confUtil.addConfiguration("twitter.size", "value", "large");
        confUtil.addConfiguration("twitter.attributes", "value", "data-foo=\"bar\"");
        confUtil.setConfiguration();

        siteId = ID_PREFIX + ".site";
        numericSiteId = cmServer.findContentIdByExternalId(new ExternalContentId(siteId));
        articleId = ID_PREFIX + ".content";
        numericArticleId = cmServer.findContentIdByExternalId(new ExternalContentId(articleId));
        changeList.waitFor("preview");

        webTest.loadPage(String.format("/cmlink/%s/%s", siteId, articleId));
    }

    @Test
    public void testBasics() {
        webTest.validatePageHtml();
        webTest.validateUnknownVelocityVariables();
        webTest.assertStatusCode(200);
    }

    @Test
    public void testExpectedAttributes() {
        webTest.assertContainsByXPath(XPATH + "/@data-url",
                String.format("/cmlink/%s/%s",
                        numericSiteId.getContentId().getContentIdString(),
                        numericArticleId.getContentId().getContentIdString()));
        webTest.assertContainsByXPath(XPATH + "/@data-text", "default text");
        webTest.assertContainsByXPath(XPATH + "/@data-related", "atexglobal");
        webTest.assertContainsByXPath(XPATH + "/@data-count", "horizontal");
        webTest.assertContainsByXPath(XPATH + "/@data-lang", "fr");
        webTest.assertContainsByXPath(XPATH + "/@data-size", "large");
    }

    @After
    public void cleanUp() throws CMException, InterruptedException {
        // Reset configuration after test
        confUtil.resetConfiguration();
    }

}
