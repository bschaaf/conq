package com.atex.gong.disqus;

import com.google.inject.Inject;
import com.polopoly.cm.client.CMException;
import com.polopoly.testnext.base.ChangeList;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnj.TestNJRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.assertEquals;

@RunWith(TestNJRunner.class)
@ImportTestContent(
        dir = "/com/atex/gong/disqus/",
        files = {"DisqusWebTestIT.content" })
public class DisqusWebTestIT {

    private static final String DOMAIN = "localhost:8080";
    private static final String HOST = "http://" + DOMAIN;
    private static final String ID_PREFIX = "DisqusWebTestIT";
    private static final String SITE_ID = ID_PREFIX + ".integrationtest.site";
    private static final String ARTICLE_ID = ID_PREFIX + ".integrationtest.article";
    private static final String DISQUS_DIV = "//div[@id = 'disqus_thread']";
    private static final String DISQUS_IFRAME = DISQUS_DIV + "//iframe[@id = 'dsq-2']";
    private static final String DISCUS_COMMUNITY_NAME = "//span[@class = 'community-name']";
    private static final String DISQUS_TEST_SITE_NAME = "Polopoly Post Test Site";

    @Inject
    private WebDriver webDriver;
    @Inject
    private ChangeList changelist;
    private Wait wait;

    @Before
    public void init() throws CMException {
        changelist.waitFor("preview");
        wait = new WebDriverWait(webDriver, 60);
        // Establish current domain in webdriver by surfing site
        webDriver.navigate().to(String.format("%s/cmlink/%s", HOST, SITE_ID));
    }

    @Test
    public void testDisqusCommentsPresent() throws CMException {
        webDriver.get(String.format("%s/cmlink/%s/%s", HOST, SITE_ID, ARTICLE_ID));
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

}
