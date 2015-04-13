package com.atex.plugins.poll;

import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.polopoly.cm.client.CMException;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnext.gui.agent.ContentCreatorAgent;
import com.polopoly.testnext.gui.agent.ContentNavigatorAgent;
import com.polopoly.testnext.gui.agent.GuiBaseAgent;
import com.polopoly.testnext.gui.agent.InputAgent;
import com.polopoly.testnext.gui.agent.ToolbarAgent;
import com.polopoly.testnj.TestNJRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@RunWith(TestNJRunner.class)
@ImportTestContent
public class PollWebTestIT {

    private String pollExternalId;
    private String siteExternalId;

    @Inject
    private WebDriver webDriver;

    @Inject
    private GuiBaseAgent gui;

    @Inject
    private ToolbarAgent toolbar;

    @Inject
    private InputAgent input;

    @Inject
    private ContentCreatorAgent creator;

    @Inject
    private ContentNavigatorAgent navigator;

    private WebDriverWait webDriverWait;

    @Before
    public void init() throws CMException {
        String className = this.getClass().getSimpleName();

        siteExternalId = className + ".site";
        webDriverWait = new WebDriverWait(webDriver, 5);
    }

    @Test
    public void test2Options() throws Exception {
        gui.loginAsSysadmin();
        navigator.openContent(siteExternalId);

        pollExternalId  = createPollElement(2);

        clickAndCheck("2", new String[]{"0", "100"});
    }

    @Test
    public void test3OptionsShowAndMoreVotes() throws Exception {
        gui.loginAsSysadmin();
        navigator.openContent(siteExternalId);

        pollExternalId  = createPollElement(3);

        clickAndCheck("2", new String[]{"0", "100", "0"});

        webDriver.get(String.format("http://localhost:8080/cmlink/%s/%s", siteExternalId, pollExternalId));


        //Already voted, but shows cached result
        checkResult(new String[]{"0", "0", "0"});
        Assert.assertEquals("\"Show the result\" should not be displayed", 0,
                            webDriver.findElements(By.linkText("Show the result")).size());

        webDriver.manage().deleteAllCookies();

        //show result
        webDriver.get(String.format("http://localhost:8080/cmlink/%s/%s", siteExternalId, pollExternalId));
        webDriver.findElement(By.linkText("Show the result")).click();

        //shows cached result
        checkResult(new String[]{"0", "0", "0"});
        webDriver.findElement(By.linkText("Back to poll")).click();

        clickAndCheck("3", new String[]{"0", "50", "50"});


        webDriver.manage().deleteAllCookies();

        clickAndCheck("2", new String[]{"0", "67", "33"});
    }


    private void clickAndCheck(final String voteOn, final String[] actual) {
        webDriver.get(String.format("http://localhost:8080/cmlink/%s/%s", siteExternalId, pollExternalId));
        webDriver.findElement(By.xpath("//h3[contains(.,'PollTestQuestion?')]"));
        webDriver.findElement(By.xpath("//label[contains(.,'PollTestAnswerOption" + voteOn + "')]")).click();
        waitForResult();
        webDriver.findElement(By.xpath("//div[@class='poll-container']//h3[contains(.,'Thank you for your vote!')]"));
        Assert.assertEquals("\"Back to poll\" should not be displayed", 0,
                            webDriver.findElements(By.linkText("Back to poll")).size());
        checkResult(actual);
    }

    private void checkResult(final String[] actual) {
        String result;

        for (int i = 0; actual.length > i; i++) {
            result = webDriver
                    .findElement(By.xpath("//span[contains(.,'PollTestAnswerOption"
                                                  + String.valueOf(i + 1)
                                                  +  "')]/following-sibling::progress"))
                    .getAttribute("value");
            Assert.assertEquals("Wrong progress bar value", actual[i], result);
        }
    }

    private String createPollElement(final int numberOfOptions) throws Exception {
        String contentId;
        creator.createContent("Main column", "Poll Element");
        contentId = navigator.getOpenedContentContentId();

        input.typeInTextfield("Question text", "PollTestQuestion?");

        String locator = "//h2[contains(text(), 'Label')]/..//input";

        input.typeInTextfield("Answer options", "\u0008");
        input.typeInTextfield("Answer options", String.valueOf(numberOfOptions));

        gui.click("//button[text() = 'Set count']");

        List<WebElement> elementList = new ArrayList<WebElement>();
        elementList = webDriver.findElements(By.xpath(locator));

        int i = 1;
        for (WebElement webElement : elementList) {
            webElement.sendKeys("PollTestAnswerOption" + i++);
        }

        toolbar.clickOnSaveAndFastInsert();
        toolbar.clickOnSaveAndClose();

        return contentId;
    }

    private void waitForResult() {
        webDriverWait.until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable final WebDriver driver) {
                return driver.findElement(By.cssSelector("ul.poll-result")).isDisplayed();
            }
        });
    }

    private void waitForForm() {
        webDriverWait.until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable final WebDriver driver) {
                return driver.findElement(By.xpath("//div[@class='poll']//form")).isDisplayed();
            }
        });
    }
}
