
package com.atex.gong.paywall;

import com.atex.plugins.users.AccessToken;
import com.atex.plugins.users.AuthenticationManager;
import com.atex.plugins.users.AuthenticationMethodCreator;
import com.atex.plugins.users.User;
import com.atex.plugins.users.WebClientUtil;
import com.google.inject.Inject;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CmClient;
import com.polopoly.paywall.cookie.MeteredCookie;
import com.polopoly.testnext.base.ChangeList;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnj.TestNJRunner;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(TestNJRunner.class)
@ImportTestContent(
        dir = "/com/atex/gong/paywall/",
        files = {
            "paywall.DefaultPaywallConfiguration.content",
            "paywall.MeteredModelEnabled.content",
            "paywall.BuyOfferingWebTestIT.content" },
            once = false)
public class BuyOfferingWebTestIT {

    private static final String ID_PREFIX             = "paywall.BuyOfferingWebTestIT";
    private static final String SITE_ID               = ID_PREFIX + ".site";
    private static final String SPORTS_ID             = ID_PREFIX + ".sportspage";
    private static final String PREMIUM_ARTICLE_1_ID  = ID_PREFIX + ".premiumarticle1";

    private static final ExternalContentId OFFERING_ID =
            new ExternalContentId("paywall.DefaultPaywallConfiguration.offering");

    private static final String BODY  = "Body text";

    private static final String PREMIUM_ARTICLE   = "Premium sports article";
    private static final String PREMIUM_ARTICLE_1 = PREMIUM_ARTICLE + " 1. ";

    private static final String PREMIUM_ARTICLE_1_BODY   = PREMIUM_ARTICLE_1 + BODY;

    private static final String DOMAIN = "localhost:8080";
    private static final String HOST = "http://" + DOMAIN;

    private static final String OFFERING_DROPDOWN_XPATH =
            "//div[@class='paywall_usersessionacquired']//select[@name='offeringId']";
    private static final String OFFERING_BUTTON_XPATH =
            "//div[@class='paywall_usersessionacquired']//input[@type='submit']";
    private static final String PAYMENT_OFFERING_CSS_SELECTOR = ".product-name";
    private static final String PAYMENT_CONFIRM_CSS_SELECTOR = "a.pay-now";

    private static final int METERED_PERIOD_IN_DAYS = 30;
    private static final int INTIAL_FREE_CLICKS = 1;

    @Inject
    private WebDriver webDriver;

    @Inject
    private CmClient cmClient;

    @Inject
    private ChangeList changelist;

    private com.atex.onecms.content.ContentId userId;
    private AuthenticationManager authenticationManager;

    @Before
    public void init() {
        changelist.waitFor("preview");
        final String authenticationMethodName = new AuthenticationMethodCreator(cmClient).create().getName();
        final String username = UUID.randomUUID().toString();
        authenticationManager = new AuthenticationManager(cmClient);
        userId = authenticationManager.createUser(authenticationMethodName, username, new User());
    }

    @Test
    public void testEntireWorkflow() throws Exception {
        try {
            // Establish current domain in webdriver by surfing site
            webDriver.navigate().to(String.format(HOST + "/cmlink/%s", SITE_ID));

            // Fake cookie with no complimentary clicks left
            MeteredCookie cookie =
                    new MeteredCookie(MeteredCookie.DEFAULT_COOKIE_NAME, METERED_PERIOD_IN_DAYS, INTIAL_FREE_CLICKS);
            cookie.grantClickAccess("1.1");
            Cookie webdriverCookie = new Cookie(cookie.getName(), cookie.getValue(), DOMAIN, "/", null);
            webDriver.manage().addCookie(webdriverCookie);

            // Log in
            AccessToken accessToken = authenticationManager.getToken(userId);
            Cookie accessCookie =
                    new Cookie(WebClientUtil.ACCESS_TOKEN_COOKIE_NAME, accessToken.getToken(), DOMAIN, "/", null);
            webDriver.manage().addCookie(accessCookie);

            // Check that article is paywalled and that we are presented with the option to buy it
            webDriver.get(String.format(HOST + "/cmlink/%s/%s/%s", SITE_ID, SPORTS_ID, PREMIUM_ARTICLE_1_ID));
            assertFalse(webDriver.getPageSource().contains(PREMIUM_ARTICLE_1_BODY));
            assertTrue(webDriver.findElement(By.xpath("//h1[text()='Subscription required!']")).isDisplayed());
            WebElement selectBox = webDriver.findElement(By.xpath(OFFERING_DROPDOWN_XPATH));
            assertTrue(selectBox.isDisplayed());
            WebElement button = webDriver.findElement(By.xpath(OFFERING_BUTTON_XPATH));
            assertTrue(button.isDisplayed());

            // Buy it
            button.click();

            // Check that payment page looks ok
            String offeringName = cmClient.getPolicyCMServer().getPolicy(OFFERING_ID).getContent().getName();
            assertEquals(offeringName,
                    webDriver.findElement(By.cssSelector(PAYMENT_OFFERING_CSS_SELECTOR)).getText().trim());
            WebElement confirmLink = webDriver.findElement(By.cssSelector(PAYMENT_CONFIRM_CSS_SELECTOR));
            assertTrue(confirmLink.isDisplayed());

            // Confirm payment
            confirmLink.click();

            // Can we see the whole article now?
            assertTrue(webDriver.getPageSource().contains(PREMIUM_ARTICLE_1_BODY));
        } catch (Throwable t) {
            System.out.println("Dumping source from " + webDriver.getCurrentUrl());
            System.out.println(webDriver.getPageSource());

            File file = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
            File destFile = new File("target/" + getClass().getCanonicalName() + ".png");
            System.out.println("Dumping screenshot as " + destFile.getAbsolutePath());
            FileUtils.copyFile(file, destFile);

            throw t;
        }
    }
}
