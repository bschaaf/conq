package com.atex.gong.personalization;

import com.atex.gong.utils.ContentTestUtil;
import com.atex.onecms.content.ContentId;
import com.atex.onecms.content.ContentWrite;
import com.atex.onecms.content.SetAliasOperation;
import com.atex.onecms.content.Subject;
import com.atex.plugins.users.AuthenticationManager;
import com.atex.plugins.users.AuthenticationMethod;
import com.atex.plugins.users.User;
import com.google.inject.Inject;
import com.polopoly.cm.client.CmClient;
import com.polopoly.testnext.base.ChangeList;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnj.TestNJRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(TestNJRunner.class)
@ImportTestContent(
        dir = "/com/atex/gong/personalization/",
        files = {"TargetingWebTestIT.content" })
public class TargetingWebTestIT {

    private static final Logger LOG = Logger.getLogger(TargetingWebTestIT.class.getName());
    public static final String ACCESS_COOKIE = "accessToken";
    public static final By LOGOUT_BUTTON_LOCATOR = By.cssSelector("#persona-logout");
    public static final String METHOD = "test-method-" + TargetingWebTestIT.class.getName();

    @Inject
    private CmClient cmClient;

    @Inject
    private WebDriver webDriver;

    @Inject
    private ContentTestUtil contentUtil;

    @Inject
    private ChangeList changeList;

    private AuthenticationManager authManager;
    private String siteId;
    private String articleId1;
    private String articleId2;
    private String articleId3;

    @Before
    public void init() {
        siteId = contentUtil.getTestContentId(".site");
        articleId1 = contentUtil.getTestContentId(".article1");
        articleId2 = contentUtil.getTestContentId(".article2");
        articleId3 = contentUtil.getTestContentId(".article3");
        get("");

        authManager = new AuthenticationManager(cmClient);

        if (authManager.getAuthenticationMethod(METHOD) == null) {
            AuthenticationMethod authMethod = new AuthenticationMethod();
            authMethod.setName(METHOD);
            authMethod.setDescription("Test method");
            SetAliasOperation aliasOp = new SetAliasOperation(SetAliasOperation.EXTERNAL_ID,
                                                              "atex.AuthenticationMethod:" + METHOD);

            cmClient.getContentManager().create(
                    new ContentWrite<>(AuthenticationMethod.ASPECT_NAME, authMethod, aliasOp),
                    new Subject("98", null));
        }

        ContentId id = authManager.createUser(METHOD, UUID.randomUUID().toString(), new User());
        webDriver.manage().addCookie(new Cookie(ACCESS_COOKIE, authManager.getToken(id).getToken(), "/"));

        changeList.waitFor("preview");
    }

    private void get(final String articleId) {
        String url = String.format("http://localhost:8080/cmlink/%s/%s", siteId, articleId);
        LOG.log(Level.INFO, "Fetching url: " + url);
        webDriver.get(url);
    }

    @Test
    public void testTags() throws Exception {
        WebDriverWait webDriverWait = new WebDriverWait(webDriver, 30);

        checkArticle(Arrays.asList("Japan", "Nuclear", "USA", "Article1"), articleId1, webDriverWait);
        checkArticle(Arrays.asList("Japan", "Nuclear", "USA", "Article1", "Article2"), articleId2, webDriverWait);
        checkArticle(Arrays.asList("Nuclear", "Article2", "Article3"), articleId3, webDriverWait);
    }

    private void checkArticle(final List<String> expected,
                              final String articleId,
                              final WebDriverWait webDriverWait) {
        get(articleId);
        webDriverWait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div.personalization-banner .description")));

        get("");
        assertTags(expected, webDriverWait);
    }

    private void assertTags(final List<String> expected, final WebDriverWait webDriverWait) {

        ExpectedCondition<WebElement> tagsPresent =
                approximateAmountOfTagsPresent(By.cssSelector("div.personalization-banner"), expected.size());
        // We get the first element that has at least the amount of expected tags
        WebElement webElement = webDriverWait.until(tagsPresent);

        List<WebElement> tags = webElement.findElements(By.cssSelector(".tag"));
        Set<String> actual = new HashSet<>();
        for (WebElement tag : tags) {
            actual.add(tag.getText());
        }

        assertEquals("Wrong number of tags", expected.size(), actual.size());

        for (String expectedTag : expected) {
            assertTrue("Missing tag " + expectedTag, actual.contains(expectedTag));
        }
    }

    public ExpectedCondition<WebElement> approximateAmountOfTagsPresent(final By by, final int expectedSize) {
        return new ExpectedCondition<WebElement>() {
            public WebElement apply(final WebDriver driver) {
                List<WebElement> elements = driver.findElements(by);
                // Two banner elements in page layout
                for (WebElement webElement : elements) {
                    List<WebElement> tags = webElement.findElements(By.cssSelector(".tag"));
                    // If one banner not visible to WebDriver it will only have 1 tag
                    if (tags.size() >= expectedSize) {
                        return webElement;
                    }
                }
                return null;
            }
        };
    }
}
