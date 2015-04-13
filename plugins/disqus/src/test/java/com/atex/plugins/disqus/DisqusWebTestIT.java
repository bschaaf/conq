package com.atex.plugins.disqus;

import com.google.inject.Inject;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.policy.BooleanValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.testnext.base.ChangeList;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnj.TestNJRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(TestNJRunner.class)
@ImportTestContent()
public class DisqusWebTestIT {

    private static final String CONFIG_EXT_ID = "plugins.com.atex.plugins.disqus.Config";
    private static final String DOMAIN = "localhost:8080";
    private static final String HOST = "http://" + DOMAIN;
    private static final String ID_PREFIX = "DisqusWebTestIT";
    private static final String SITE_ID = ID_PREFIX + ".site";
    private static final String ARTICLE_ID = ID_PREFIX + ".article";
    private static final String DISQUS_DIV = "//div[@id = 'disqus_thread']";
    private static final String DISQUS_IFRAME = DISQUS_DIV + "//iframe[@id = 'dsq-2']";
    private static final String DISCUS_COMMUNITY_NAME = "//span[@class = 'community-name']";
    private static final String DISQUS_TEST_SITE_NAME = "Polopoly Disqus Plugin Test Site";

    @Inject
    private WebDriver webDriver;
    @Inject
    private PolicyCMServer policyCMServer;
    @Inject
    private ChangeList changelist;
    private Wait wait;
    private ConfigPolicy configPolicy;

    @Before
    public void init() throws CMException {
        changelist.waitFor("preview");
        // Establish current domain in webdriver by surfing site
        webDriver.navigate().to(String.format("%s/cmlink/%s", HOST, SITE_ID));
        wait = new WebDriverWait(webDriver, 60);
        configPolicy = (ConfigPolicy) policyCMServer.getPolicy(new ExternalContentId(CONFIG_EXT_ID));
    }

    @Test
    public void testDisqusCommentsPresentThenTestDisableDisqus() throws CMException {
        webDriver.get(String.format("%s/cmlink/%s/%s", HOST, SITE_ID, ARTICLE_ID));
        verifyDisqusPresent();
        disableDisqus();
        webDriver.navigate().refresh();
        verifyNoDisqus();
    }

    @After
    public void enableDisqus() throws CMException {
        setDisableDisqus(false);
    }

    private void disableDisqus() throws CMException {
        setDisableDisqus(true);
    }

    private void setDisableDisqus(final boolean disable) throws CMException {
        configPolicy = (ConfigPolicy) policyCMServer.createContentVersion(configPolicy.getContentId().
                getLatestCommittedVersionId());
        ((BooleanValuePolicy) configPolicy.getChildPolicy("enabled")).setBooleanValue(!disable);
        policyCMServer.commitContent(configPolicy);
        changelist.waitFor("preview");
    }

    private void verifyDisqusPresent() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(DISQUS_DIV)));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(DISQUS_IFRAME)));
        final WebElement disqusIframeWebElement = webDriver.findElement(By.xpath(DISQUS_IFRAME));
        webDriver.switchTo().frame(disqusIframeWebElement);
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath(DISCUS_COMMUNITY_NAME)));
        final WebElement webElementCommunityName =
                webDriver.findElement(By.xpath(DISCUS_COMMUNITY_NAME));
        assertEquals(DISQUS_TEST_SITE_NAME, webElementCommunityName.getText());
    }

    private void verifyNoDisqus() {
        final String pageSource = webDriver.getPageSource();

        final String regexp = "<div id=\"test-render-disqus-ot\">\\s*BEFORE_RENDER_DISQUS_OT\\s*"
                + "AFTER_RENDER_DISQUS_OT\\s*</div>";
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(pageSource);
        assertTrue(matcher.find());

        pattern = Pattern.compile("disqus_thread");
        matcher = pattern.matcher(pageSource);
        assertFalse(matcher.find());
    }

}
