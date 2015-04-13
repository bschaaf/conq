package com.atex.plugins.social.sharing.google;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.atex.plugins.social.sharing.ConfigurationTestUtil;
import com.google.inject.Inject;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.testnext.base.ChangeList;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnext.web.WebTest;
import com.polopoly.testnj.TestNJRunner;

@RunWith(TestNJRunner.class)
@ImportTestContent
public class GooglePlusWebTestIT {

    private static final String LANG_TEMPLATE = "window.___gcfg = {lang: '%s'};";

    @Inject
    private WebTest webTest;

    @Inject
    private ChangeList changeList;

    @Inject
    private PolicyCMServer cmServer;

    private ConfigurationTestUtil confUtil;
    private String siteId;
    private String articleId;

    public static final String ID_PREFIX = "GooglePlusWebTestIT";

    @Before
    public void init() throws Exception {
        // We need to set configuration through code in order to reset after testing is finished
        confUtil = new ConfigurationTestUtil(cmServer);
        confUtil.addConfiguration("gplus.annotation", "selected_0", "3");
        confUtil.addConfiguration("gplus.height", "selected_0", "24");
        confUtil.addConfiguration("gplus.width", "value", "240");
        confUtil.addConfiguration("gplus.lang", "value", "en-GB");
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
    public void testGooglePlusLanguageOnPage() {
        final String code = String.format(LANG_TEMPLATE, "en-GB");
        webTest.assertContainsHtml(code);
    }

    @Test
    public void testGooglePlusWidgetOnPage() {
        webTest.assertContainsHtml("class=\"g-plus\" data-action=\"share\"");
        webTest.assertContainsHtml("data-annotation=\"inline\"");
        webTest.assertContainsHtml("data-height=\"24\"");
        webTest.assertContainsHtml("data-width=\"240\"");
    }

    @After
    public void cleanUp() throws CMException, InterruptedException {
        // Reset configuration after test
        confUtil.resetConfiguration();
    }
}
