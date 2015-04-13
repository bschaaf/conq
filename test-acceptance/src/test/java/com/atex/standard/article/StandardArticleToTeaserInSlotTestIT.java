package com.atex.standard.article;

import com.atex.gong.utils.ContentTestUtil;
import com.atex.plugins.teaser.TeaserPolicy;
import com.google.inject.Inject;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnext.gui.agent.ClipboardAgent;
import com.polopoly.testnext.gui.agent.ContentNavigatorAgent;
import com.polopoly.testnext.gui.agent.GuiBaseAgent;
import com.polopoly.testnext.gui.agent.ToolbarAgent;
import com.polopoly.testnj.TestNJRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(TestNJRunner.class)
@ImportTestContent(files = {"com/atex/standard/article/StandardArticleToTeaserInSlotTestIT.content" })
public class StandardArticleToTeaserInSlotTestIT {

    @Inject
    private GuiBaseAgent gui;

    @Inject
    private ContentNavigatorAgent navigator;

    @Inject
    private ClipboardAgent clipboard;

    @Inject
    private ToolbarAgent toolbar;

    @Inject
    private ContentTestUtil contentUtil;

    @Inject
    private WebDriver webDriver;

    private ContentPolicy testArticle = null;
    private ContentPolicy testSite = null;

    @Test
    public void testArticleBecomesTeaserWhenPastedInSlot() throws Exception {
        try {
            testArticle = (ContentPolicy) contentUtil.getTestPolicy();
            testSite = (ContentPolicy) contentUtil.getTestPolicy(".site");
        } catch (CMException e) {
            fail("Unable to fetch test content, message: " + e.getMessage());
        }

        gui.loginAsSysadmin();
        navigator.openContent(testArticle.getContentId().getContentIdString());
        clipboard.copyOpenedContent();
        navigator.openContent(testSite.getContentId().getContentIdString());
        toolbar.clickOnEdit();
        gui.click("//h2[contains(text(),'Main column')]/..//button[@title='Paste']");
        // Verify link to teaser present
        String teaserLink = String.format("//h2/a[contains(text(),'%s')]", testArticle.getName());
        WebElement teaserLinkElement = getElement(teaserLink);
        assertTrue("Expected teaser link present", teaserLinkElement.isDisplayed());

        // There should be only one element
        WebElement teaserContainer = getElement("//div[contains(@class,'p_listentry')]");
        String teaserId = teaserContainer.getAttribute("polopoly:contentid");
        toolbar.clickOnSaveAndView();
        assertTrue("Expected id of new element to be of Major LayoutElement", teaserId.startsWith("7"));

        Policy teaser = contentUtil.getPolicyForTesting(ContentIdFactory.createContentId(teaserId));
        assertTrue("Expected teaser policy when fetching id: " + teaserId, (teaser instanceof TeaserPolicy));

        TeaserPolicy teaserPolicy = (TeaserPolicy) teaser;
        assertTrue("Expected test article inside teaser",
                teaserPolicy.getTeaserableContentId().getContentId().equals(testArticle.getContentId().getContentId()));
    }

    private WebElement getElement(final String locator) {
        return webDriver.findElement(By.xpath(locator));
    }
}
