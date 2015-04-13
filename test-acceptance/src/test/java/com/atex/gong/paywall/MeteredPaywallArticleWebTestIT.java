
package com.atex.gong.paywall;

import com.google.inject.Inject;
import com.polopoly.testnext.base.ChangeList;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnj.TestNJRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@RunWith(TestNJRunner.class)
@ImportTestContent(
        dir = "/com/atex/gong/paywall/",
        files = {
            "paywall.DefaultPaywallConfiguration.content",
            "paywall.MeteredModelEnabled.content",
            "paywall.MeteredPaywallArticleWebTestIT.content" },
        once = false)
public class MeteredPaywallArticleWebTestIT {

    private static final String DOMAIN = "localhost:8080";
    private static final String HOST = "http://" + DOMAIN;

    private static final String ARTICLE_HEADER_XPATH = "//article/header/h1";
    private static final String ARTICLE_LEAD_XPATH = "//article//p[@class='intro']";
    private static final String ARTICLE_BODY_PARAGRAPHS_XPATH = "//article//p[not(@class)]";
    private static final String ID_PREFIX             = "paywall.MeteredPaywallArticleWebTestIT";
    private static final String SITE_ID               = ID_PREFIX + ".site";
    private static final String FREE_ARTICLE_ID       = ID_PREFIX + ".freearticle";
    private static final String PREMIUM_ARTICLE_1_ID  = ID_PREFIX + ".premiumarticle1";
    private static final String PREMIUM_ARTICLE_2_ID  = ID_PREFIX + ".premiumarticle2";

    private static final String TITLE = "Title text";
    private static final String LEAD  = "Lead text";
    private static final String BODY  = "Body text";

    private static final String FREE_ARTICLE      = "Free business article. ";
    private static final String PREMIUM_ARTICLE   = "Premium sports article";
    private static final String PREMIUM_ARTICLE_1 = PREMIUM_ARTICLE + " 1. ";
    private static final String PREMIUM_ARTICLE_2 = PREMIUM_ARTICLE + " 2. ";

    private static final String FREE_ARTICLE_TITLE = FREE_ARTICLE + TITLE;
    private static final String FREE_ARTICLE_LEAD  = FREE_ARTICLE + LEAD;
    private static final String FREE_ARTICLE_BODY  = FREE_ARTICLE + BODY;

    private static final String PREMIUM_ARTICLE_1_TITLE  = PREMIUM_ARTICLE_1 + TITLE;
    private static final String PREMIUM_ARTICLE_1_LEAD   = PREMIUM_ARTICLE_1 + LEAD;
    private static final String PREMIUM_ARTICLE_1_BODY   = PREMIUM_ARTICLE_1 + BODY;

    private static final String PREMIUM_ARTICLE_2_TITLE  = PREMIUM_ARTICLE_2 + TITLE;
    private static final String PREMIUM_ARTICLE_2_LEAD   = PREMIUM_ARTICLE_2 + LEAD;
    private static final String PREMIUM_ARTICLE_2_BODY   = PREMIUM_ARTICLE_2 + BODY;

    private static final String PREMIUM_PACKAGE = "Premium package";
    private static final String SPORTS_BUNDLE_STR = "SPORTS BUNDLE";
    private static final String SUBSCRIPTION_REQUIRED = "Subscription required";

    private static final int FIVE_SECONDS = 5000;

    @Inject
    private ChangeList changelist;

    @Inject
    private WebDriver webDriver;

    @Before
    public void init() throws Exception {
        changelist.waitFor("preview");
        // Establish current domain in webdriver by surfing site
        webDriver.navigate().to(String.format("%s/cmlink/%s", HOST, SITE_ID));
    }

    @Test
    public void testFreeArticle() {
        webDriver.get(String.format("%s/cmlink/%s/%s", HOST, SITE_ID, FREE_ARTICLE_ID));
        assertContainsByXPath(ARTICLE_HEADER_XPATH, FREE_ARTICLE_TITLE);
        assertContainsByXPath(ARTICLE_LEAD_XPATH, FREE_ARTICLE_LEAD);
        assertContainsByXPath(ARTICLE_BODY_PARAGRAPHS_XPATH, FREE_ARTICLE_BODY);
        assertBodyDoesNotContain(PREMIUM_PACKAGE);
        assertBodyDoesNotContain(SUBSCRIPTION_REQUIRED);
    }

    private void assertContainsByXPath(final String xpath, final String text) {
        WebElement element = webDriver.findElement(By.xpath(xpath));
        assertTrue("Text at xpath " + xpath + " did not contain string '" + text + "': '" + element.getText() + "'",
                    element.getText().contains(text));
    }

    private void assertBodyContains(final String text) {
        WebElement element = webDriver.findElement(By.xpath("//body"));
        assertTrue("Visible page did not contain string '" + text + "': '" + element.getText() + "'",
                    element.getText().contains(text));
    }

    private void assertBodyDoesNotContain(final String text) {
        WebElement element = webDriver.findElement(By.xpath("//body"));
        assertFalse("Visible page unexpectedly contained string '" + text + "': '" + element.getText() + "'",
                    element.getText().contains(text));
    }

    @Test
    public void testPremiumArticle() {
        webDriver.get(String.format("%s/cmlink/%s/%s", HOST, SITE_ID, PREMIUM_ARTICLE_1_ID));
        assertContainsByXPath(ARTICLE_HEADER_XPATH, PREMIUM_ARTICLE_1_TITLE);
        assertContainsByXPath(ARTICLE_LEAD_XPATH, PREMIUM_ARTICLE_1_LEAD);
        assertContainsByXPath(ARTICLE_BODY_PARAGRAPHS_XPATH, PREMIUM_ARTICLE_1_BODY);
        assertBodyContains(PREMIUM_PACKAGE);
        assertBodyContains(SPORTS_BUNDLE_STR);
        assertBodyDoesNotContain(SUBSCRIPTION_REQUIRED);

        waitForBottomBar();
        // We start out with one complimentary article, so we should have 0 left now
        assertContainsByXPath("//div[@class='paywall hint']/h2/span", "0");
    }

    private void waitForBottomBar() {
        try {
            Thread.sleep(FIVE_SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPremiumArticleRevisited() {
        webDriver.get(String.format("%s/cmlink/%s/%s", HOST, SITE_ID, PREMIUM_ARTICLE_2_ID));

        assertContainsByXPath(ARTICLE_HEADER_XPATH, PREMIUM_ARTICLE_2_TITLE);
        assertContainsByXPath(ARTICLE_LEAD_XPATH, PREMIUM_ARTICLE_2_LEAD);
        assertContainsByXPath(ARTICLE_BODY_PARAGRAPHS_XPATH, PREMIUM_ARTICLE_2_BODY);
        assertBodyContains(PREMIUM_PACKAGE);
        assertBodyContains(SPORTS_BUNDLE_STR);
        assertBodyDoesNotContain(SUBSCRIPTION_REQUIRED);

        // Access same article a second time

        webDriver.get(String.format("%s/cmlink/%s/%s", HOST, SITE_ID, PREMIUM_ARTICLE_2_ID));
        assertContainsByXPath(ARTICLE_HEADER_XPATH, PREMIUM_ARTICLE_2_TITLE);
        assertContainsByXPath(ARTICLE_LEAD_XPATH, PREMIUM_ARTICLE_2_LEAD);
        assertContainsByXPath(ARTICLE_BODY_PARAGRAPHS_XPATH, PREMIUM_ARTICLE_2_BODY);
        assertBodyContains(PREMIUM_PACKAGE);
        assertBodyContains(SPORTS_BUNDLE_STR);
        assertBodyDoesNotContain(SUBSCRIPTION_REQUIRED);
    }

    @Test
    public void testPremiumArticleNotAccessable() {
        webDriver.get(String.format("%s/cmlink/%s/%s", HOST, SITE_ID, PREMIUM_ARTICLE_1_ID));

        assertContainsByXPath(ARTICLE_HEADER_XPATH, PREMIUM_ARTICLE_1_TITLE);
        assertContainsByXPath(ARTICLE_LEAD_XPATH, PREMIUM_ARTICLE_1_LEAD);
        assertContainsByXPath(ARTICLE_BODY_PARAGRAPHS_XPATH, PREMIUM_ARTICLE_1_BODY);
        assertBodyContains(PREMIUM_PACKAGE);
        assertBodyContains(SPORTS_BUNDLE_STR);
        assertBodyDoesNotContain(SUBSCRIPTION_REQUIRED);

        // Access another premium article. Should not be possible.
        webDriver.get(String.format("%s/cmlink/%s/%s", HOST, SITE_ID, PREMIUM_ARTICLE_2_ID));
        assertContainsByXPath(ARTICLE_HEADER_XPATH, PREMIUM_ARTICLE_2_TITLE);
        assertContainsByXPath(ARTICLE_LEAD_XPATH, PREMIUM_ARTICLE_2_LEAD);
        assertBodyDoesNotContain(PREMIUM_ARTICLE_2_BODY); // Weak test!
        assertBodyContains(SUBSCRIPTION_REQUIRED);
    }

}
