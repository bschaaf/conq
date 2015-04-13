package com.atex.gong.paywall;


import com.atex.gong.AbstractWebTestBase;
import com.google.inject.Inject;
import com.polopoly.testnext.base.ChangeList;
import com.polopoly.testnext.base.ImportTestContent;

import org.junit.Before;
import org.junit.Test;

@ImportTestContent(
        dir = "/com/atex/gong/paywall/",
        files = {"paywall.DefaultPaywallConfiguration.content", "paywall.TeaserPluginWebTestIT.content" },
        once = false)
public class TeaserPluginWebTestIT extends AbstractWebTestBase {

    private static final String PREMIUM_LABEL = "Premium";
    private static final String PREMIUM_LABEL_CLASS = "premium-label-tag";
    private static final String PREMIUM_LABEL_XPATH = "//span[@class='premium-label-tag']";
    private static final String LEAD = "My sports article lead text.";
    private static final String LEAD_INCLUDING_PREMIUM_LABEL = PREMIUM_LABEL + " " + LEAD;

    private String siteId = "paywall.TeaserPluginWebTestIT.site";
    private String teaserForPremiumArticle = "paywall.TeaserPluginWebTestIT.teaserForPremiumArticle";
    private String teaserForNonPremiumArticle = "paywall.TeaserPluginWebTestIT.teaserForNonPremiumArticle";

    @Inject
    private ChangeList changelist;

    @Before
    public void setUp() {
        changelist.waitFor("preview");
    }

    @Test
    public void testPremiumLabelPresent() {
        webTest.loadPage(String.format("/cmlink/%s/%s", siteId, teaserForPremiumArticle));
        webTest.assertContains(PREMIUM_LABEL);
        webTest.assertContains(LEAD_INCLUDING_PREMIUM_LABEL);
        webTest.assertContainsXPath(PREMIUM_LABEL_XPATH);
        webTest.assertContainsByXPath(PREMIUM_LABEL_XPATH, PREMIUM_LABEL);
    }

    @Test
    public void testPremiumLabelNotPresent() {
        webTest.loadPage(String.format("/cmlink/%s/%s", siteId, teaserForNonPremiumArticle));
        webTest.assertContains(LEAD);
        webTest.assertDoesNotContain(PREMIUM_LABEL);
        webTest.assertDoesNotContainHtml(PREMIUM_LABEL_CLASS);
    }
}
