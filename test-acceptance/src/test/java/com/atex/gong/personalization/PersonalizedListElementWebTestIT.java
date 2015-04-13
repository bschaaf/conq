package com.atex.gong.personalization;

import com.atex.gong.users.AuthenticationMethodCreator;
import com.atex.onecms.content.IdUtil;
import com.atex.plugins.users.AccessToken;
import com.atex.plugins.users.AuthenticationManager;
import com.atex.plugins.users.User;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnext.gui.agent.WaitAgent;
import com.polopoly.testnj.TestNJRunner;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.client.config.CookieSpecs;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(TestNJRunner.class)
@ImportTestContent(
        dir = "/com/atex/gong/personalization/",
        waitUntilContentsAreIndexed = {"PersonalizedListElementWebTestIT.no-category-article",
                "PersonalizedListElementWebTestIT.first-sport-category-article",
                "PersonalizedListElementWebTestIT.second-sport-category-article" })
public class PersonalizedListElementWebTestIT {

    private static final String ID_PREFIX = "PersonalizedListElementWebTestIT";
    private static final String NO_CATEGORY_ARTICLE_ID = ID_PREFIX + ".no-category-article";
    private static final String FIRST_SPORT_ARTICLE_ID = ID_PREFIX + ".first-sport-category-article";
    private static final String PERSONALIZATION_LIST_ELEMENT_ID = ID_PREFIX + ".personalized-list-element";
    private static final String PERSONALIZATION_LIST_ELEMENT_NAME = ID_PREFIX;
    private static final String SITE_ID = ID_PREFIX + ".site";
    private static final String DOMAIN = "localhost:8080";
    private static final String HOST = "http://" + DOMAIN;
    private static final String START_PAGE = HOST + "/cmlink/" + SITE_ID;
    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String ARTICLE_WITHOUT_CATEGORY_URL = START_PAGE + "/" + NO_CATEGORY_ARTICLE_ID;
    private static final String ARTICLE_WITH_CATEGORY_SPORT_URL = START_PAGE + "/" + FIRST_SPORT_ARTICLE_ID;

    private static final Logger LOGGER = Logger.getLogger(PersonalizedListElementWebTestIT.class.getName());

    @Inject
    private CmClient cmClient;

    @Inject
    private WebDriver webDriver;
    @Inject
    private WaitAgent waitor;

    private com.atex.onecms.content.ContentId userId;
    private AuthenticationManager authenticationManager;
    private String personalizationJavaScriptId;

    @Before
    public void init() throws CMException {
        final String authenticationMethodName = new AuthenticationMethodCreator(cmClient).create().getName();
        final String username = UUID.randomUUID().toString();
        authenticationManager = new AuthenticationManager(cmClient);
        userId = authenticationManager.createUser(authenticationMethodName, username, new User());
        final ContentId personalizationElementVersionedId = getVersionedContentId(PERSONALIZATION_LIST_ELEMENT_ID);
        personalizationJavaScriptId = "personalization-"
                + personalizationElementVersionedId.getContentId().getContentIdString().replace('.', '-');
        primeWebdriverWithLocalhostDomain();
    }

    /**
     * Test with anonymous (not logged in) user.
     */
    @Test
    public void testBehaviourWithAnonymousUser() {
        // Since personalization is not yet supported for anonymous
        // (not logged in) users, we just make a simple test.

        assertTrue(isLoggedOut());
        assertFalse(isPersonalizedListElementPresent());
    }

    /**
     * Test with a logged in user.
     */
    @Test
    public void testBehaviourWithLoggedInUser() throws Exception {
        assertFalse(isPersonalizedListElementPresent());
        logIn();

        ContentId articleOldId = cmClient.getCMServer().findContentIdByExternalId(
                new ExternalContentId(FIRST_SPORT_ARTICLE_ID));
        registerPageView(IdUtil.toIdString(IdUtil.fromPolicyContentId(articleOldId)));

        // Browse a categorized article.
        loadPage(ARTICLE_WITH_CATEGORY_SPORT_URL);
        assertTrue(isLoggedIn());
        assertTrue(isPersonalizedListElementPresent());

        // Assert correct number of articles in Personalization Element List.
        assertEquals(1, getPersonlizedListElementEntries().size());
        assertEquals("My second article with category sport", getPersonlizedListElementEntries().get(0).getText());
    }

    /**
     * Test with a logged in user on a non-categorized article.
     */
    @Test
    public void testViewingNonCategorizedArticleWithLoggedInUser() throws Exception {
        assertFalse(isPersonalizedListElementPresent());
        logIn();

        ContentId articleId = getVersionedContentId(NO_CATEGORY_ARTICLE_ID);
        registerPageView(IdUtil.toIdString(IdUtil.fromPolicyContentId(articleId)));

        // Browse a categorized article.
        loadPage(ARTICLE_WITHOUT_CATEGORY_URL);
        assertTrue(isLoggedIn());
        assertFalse(isPersonalizedListElementPresent());
    }

    /**
     * Fake log in.
     */
    private void logIn() {
        AccessToken accessToken = authenticationManager.getToken(userId);
        Cookie accessCookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, accessToken.getToken(), "/");
        webDriver.manage().addCookie(accessCookie);
    }

    private void primeWebdriverWithLocalhostDomain() {
        /* This is so cookies are associated with the correct domain */
        loadPage(START_PAGE);
    }

    private void loadPage(final String page) {
        LOGGER.log(Level.INFO, "Loading page: " + page);
        webDriver.get(page);
        waitForPersonalizedJavaScript();
    }

    private void waitForPersonalizedJavaScript() {
        LOGGER.log(Level.INFO,
                String.format("Waiting for javascript with id %s to execute.", personalizationJavaScriptId));
        final String isDone =
                "if (document.getElementById(\"" + personalizationJavaScriptId + "\").isDone) {"
                        + "return true;"
                        + "} else {"
                        + "return false;"
                        + "}";
        WebDriverWait wait = new WebDriverWait(webDriver, 30, 100);
        com.google.common.base.Predicate<WebDriver> predicate =
                new Predicate<WebDriver>() {
                    public boolean apply(final WebDriver driver) {
                        return (Boolean) ((JavascriptExecutor) webDriver).executeScript(isDone);
                    }
                };
        wait.until(predicate); // If timing out, org.openqa.selenium.TimeoutException is thrown.
    }

    private boolean isLoggedIn() {
        if (System.getProperty("testnext.gui.windowWidth") != null) {
            // We might be too small, log out button in menu
            try {
                int windowWidth = Integer.parseInt(System.getProperty("testnext.gui.windowWidth"));
                // Current menu breakpoint + some margin for windows
                if (windowWidth <= 760) {
                    WebElement navbarMenu = webDriver.findElement(By.cssSelector(".navbar-header .navbar-toggle"));
                    if (navbarMenu.isDisplayed()) {
                        navbarMenu.click();
                        waitor.waitForElement(By.id("persona-logout"));
                    }
                }
            } catch (NumberFormatException nfe) {
                LOGGER.log(Level.WARNING, "Unable to parse window width parameter, test might fail");
            }
        }
        return webDriver.findElement(By.id("persona-logout")).isDisplayed()
                && !webDriver.findElement(By.id("persona-login")).isDisplayed();
    }

    private boolean isLoggedOut() {
        return !isLoggedIn();
    }

    private void registerPageView(final String articleId) throws HttpException, IOException {
        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod("/personalization");
        post.getParams().setCookiePolicy(CookieSpecs.BROWSER_COMPATIBILITY);
        post.setRequestHeader("Cookie", "accessToken=" + authenticationManager.getToken(userId).getToken());
        post.setParameter("articleId", articleId);
        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setHost("localhost", 8080);
        int responseCode = httpClient.executeMethod(hostConfiguration, post);
        assertEquals(200, responseCode);
    }

    /**
     * Gets the list elements (the links in the list) from the Personalized List Element.
     */
    private List<WebElement> getPersonlizedListElementEntries() {
        List<WebElement> linksInPersonalizedListElement = new ArrayList<>();
        List<WebElement> personalizedListElements =
                webDriver.findElements(By.xpath("//div[@class='element-personalized-list']"));
        for (WebElement personalizedListElement : personalizedListElements) {
            if (!personalizedListElement.findElements(
                    By.xpath("./h2[text()='" + PERSONALIZATION_LIST_ELEMENT_NAME + "']")).isEmpty()) {
                // Found our personalized list element. Now get the links.
                linksInPersonalizedListElement = personalizedListElement.findElements(By.xpath("./ul/li/a"));
                break;
            }
        }
        return linksInPersonalizedListElement;
    }

    private boolean isPersonalizedListElementPresent() {
        List<WebElement> personalizedListElements = webDriver.findElements(
                By.xpath("//div[@class='element-personalized-list']"
                        + "/h2[text()='" + PERSONALIZATION_LIST_ELEMENT_NAME + "']")
        );
        if (personalizedListElements.isEmpty()) {
            return false;
        }
        if (personalizedListElements.size() == 1) {
            return true;
        }
        fail(String.format(
                "More than one Personalized List Element with name %s was found",
                PERSONALIZATION_LIST_ELEMENT_NAME));
        return false;
    }

    private ContentId getVersionedContentId(final String externalId) throws CMException {
        return cmClient.getCMServer().findContentIdByExternalId(
                new ExternalContentId(externalId));
    }

}
