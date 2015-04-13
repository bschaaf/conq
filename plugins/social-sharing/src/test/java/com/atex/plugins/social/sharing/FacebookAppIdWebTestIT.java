package com.atex.plugins.social.sharing;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.testnext.base.ChangeList;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnext.web.WebTest;
import com.polopoly.testnj.TestNJRunner;

@RunWith(TestNJRunner.class)
@ImportTestContent
public class FacebookAppIdWebTestIT {

    @Inject
    private WebTest webTest;

    @Inject
    private ChangeList changeList;

    @Inject
    private PolicyCMServer cmServer;

    private ConfigurationTestUtil confUtil;
    private String siteId;
    private String articleId;

    public static final String ID_PREFIX = "FacebookAppIdWebTestIT";

    @Before
    public void init() throws Exception {
        // We need to set configuration through code in order to reset after testing is finished
        confUtil = new ConfigurationTestUtil(cmServer);
        confUtil.addConfiguration("facebook.appid", "value", "123456789");
        confUtil.setConfiguration();

        siteId = ID_PREFIX + ".site";
        articleId = ID_PREFIX + ".content";
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
    public void testFacebookAppIdOnPage() {
        webTest.assertContainsHtml("appId : '123456789'");
    }

    @After
    public void cleanUp() throws CMException, InterruptedException {
        // Reset configuration after test
        confUtil.resetConfiguration();
    }
}
