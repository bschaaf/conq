package com.atex.gong.persona;


import com.atex.gong.utils.ContentTestUtil;
import com.atex.onecms.content.ContentId;
import com.atex.plugins.users.AccessToken;
import com.atex.plugins.users.AuthenticationManager;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.polopoly.cm.client.CmClient;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnext.gui.agent.WaitAgent;
import com.polopoly.testnj.TestNJRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.annotation.Nullable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(TestNJRunner.class)
@ImportTestContent(
        dir = "/com/atex/gong/persona/",
        files = {"MozillaPersonaWebTestIT.content" })
public class MozillaPersonaWebTestIT {

    private static final Logger LOGGER = Logger.getLogger(MozillaPersonaWebTestIT.class.getName());
    public static final String EMAIL = "Team-Manana.STO@atex.com";
    public static final String PASSWORD = "fj4892jv82";
    public static final String ACCESS_COOKIE = "accessToken";
    public static final By LOGGED_OUT_LOCATOR = By.cssSelector(".mozilla-persona .loggedout");
    public static final By LOGGED_IN_LOCATOR = By.cssSelector(".mozilla-persona .loggedin");
    public static final By LOGIN_BUTTON_LOCATOR = By.cssSelector("#persona-login");
    public static final By LOGOUT_BUTTON_LOCATOR = By.cssSelector("#persona-logout");

    @Inject
    private CmClient cmClient;

    @Inject
    private WebDriver webDriver;
    @Inject
    private WaitAgent waitor;

    @Inject
    private ContentTestUtil contentUtil;

    private String siteId;
    private String articleId;
    private String mainWindow;

    @Before
    public void init() {
        siteId = contentUtil.getTestContentId(".site");
        articleId = contentUtil.getTestContentId(".article");
        webDriver.get(String.format("http://localhost:8080/cmlink/%s/%s", siteId, articleId));
        mainWindow = webDriver.getWindowHandle();
    }

    @SuppressWarnings({ "checkstyle:javancss", "checkstyle:methodlength" }) // When rewriting this test after merging
                                                                            // with facebook/google login, do NOT use
                                                                            // suppress warnings!
    @Test
    public void testLoginLogout() {
        assertTrue("Initially we expect elements with css class 'loggedout' to be visible",
                getLoggedInStatusButton(LOGGED_OUT_LOCATOR).isDisplayed());
        assertFalse("Initially we expect elements with css class 'loggedin' to be hidden",
                getLoggedInStatusButton(LOGGED_IN_LOCATOR).isDisplayed());

        assertEquals("There shall only be one window before we click on login button", 1,
                webDriver.getWindowHandles().size());

        // Click login button
        getLoggedInStatusButton(LOGIN_BUTTON_LOCATOR).click();

        WebDriverWait webDriverWait = new WebDriverWait(webDriver, 15);

        // Wait for Mozilla Persona login window to pop up
        webDriverWait.until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable final WebDriver input) {
                return input.getWindowHandles().size() > 1;
            }
        });

        // Switch to login window
        for (String handle : webDriver.getWindowHandles()) {
            if (!handle.equals(mainWindow)) {
                webDriver.switchTo().window(handle);
                break;
            }
        }

        // Wait for login window to become active window
        webDriverWait.until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable final WebDriver input) {
                return !input.getWindowHandle().equals(mainWindow);
            }
        });

        // Wait for email input field to be available
        By emailLocator = By.id("authentication_email");
        webDriverWait.until(ExpectedConditions.elementToBeClickable(emailLocator));

        // Fill in and submit email
        WebElement emailElement = webDriver.findElement(emailLocator);
        emailElement.sendKeys(EMAIL);
        emailElement.submit();

        // Wait for password input field to be available
        By passwordLocator = By.id("authentication_password");
        webDriverWait.until(ExpectedConditions.elementToBeClickable(passwordLocator));

        // Fill in and submit password
        WebElement passwordElement = webDriver.findElement(passwordLocator);
        passwordElement.sendKeys(PASSWORD);
        passwordElement.submit();

        // Mozilla Persona either gives you the possibility to choose session length
        // ('this session only' or 'one month') or, if previous choice is known by the server,
        // logs in the user and closes the login window. We have to check for both scenarios.
        final By thisSessionOnlyLocator = By.id("this_is_not_my_computer");
        webDriverWait.until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable final WebDriver input) {
                return (input.getWindowHandles().size() == 1)
                        || !input.findElements(thisSessionOnlyLocator).isEmpty();
            }
        });

        if (webDriver.getWindowHandles().size() > 1) {
            WebElement thisSessionOnlyElement = webDriver.findElement(thisSessionOnlyLocator);
            thisSessionOnlyElement.click();
            webDriverWait.until(new Predicate<WebDriver>() {
                @Override
                public boolean apply(@Nullable final WebDriver input) {
                    return input.getWindowHandles().size() == 1;
                }
            });
        }

        webDriver.switchTo().window(mainWindow);
        waitor.waitForPageToLoad();

        // Verify logged in with valid access token cookie
        webDriverWait.until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(final WebDriver input) {
                try {
                    WebElement logInButton = getLoggedInStatusButton(LOGGED_IN_LOCATOR);
                    return logInButton.isDisplayed();
                } catch (StaleElementReferenceException e) {
                    LOGGER.log(Level.INFO,
                            "Encountered reload while waiting for login button to appear, this is fine..");
                }
                return false;
            }
        });
        // Verify not logged out
        webDriverWait.until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(final WebDriver input) {
                try {
                    WebElement loggedOutButton = getLoggedInStatusButton(LOGGED_OUT_LOCATOR);
                    return !loggedOutButton.isDisplayed();
                } catch (StaleElementReferenceException e) {
                    LOGGER.log(Level.INFO, "Encountered reload while trying to find logout button, this is fine..");
                }
                return false;
            }
        });

        Cookie cookie = webDriver.manage().getCookieNamed(ACCESS_COOKIE);
        assertNotNull("Expected a cookie named '" + ACCESS_COOKIE + "'", cookie);

        AuthenticationManager authManager = new AuthenticationManager(cmClient);
        ContentId userId = authManager.getUserId(new AccessToken(cookie.getValue()));
        assertNotNull("Could not get user from token", userId);

        // Click logout button
        getLoggedInStatusButton(LOGOUT_BUTTON_LOCATOR).click();
        // Verify logged out
        webDriverWait.until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(final WebDriver input) {
                try {
                    WebElement loggedOutButton = getLoggedInStatusButton(LOGGED_OUT_LOCATOR);
                    return loggedOutButton.isDisplayed();
                } catch (StaleElementReferenceException e) {
                    LOGGER.log(Level.INFO, "Encountered reload while trying to find logout button, this is fine..");
                }
                return false;
            }
        });
        assertFalse("Didn't expect user to be logged in", webDriver.findElement(LOGGED_IN_LOCATOR).isDisplayed());
        assertNull("Expected '" + ACCESS_COOKIE + "' cookie to be cleared",
                webDriver.manage().getCookieNamed(ACCESS_COOKIE));
    }

    private WebElement getLoggedInStatusButton(final By buttonLocator) {
        openMenuIfWindowTooSmall(buttonLocator);
        return webDriver.findElement(buttonLocator);
    }

    private void openMenuIfWindowTooSmall(final By buttonLocator) {
        if (System.getProperty("testnext.gui.windowWidth") != null) {
            // We might be too small, log out button is located in menu
            try {
                int windowWidth = Integer.parseInt(System.getProperty("testnext.gui.windowWidth"));
                // Current menu breakpoint + some margin for windows
                if (windowWidth <= 760) {
                    openMenu(buttonLocator);
                }
            } catch (NumberFormatException nfe) {
                LOGGER.log(Level.WARNING, "Unable to parse window width parameter, test might fail");
            }
        }
    }

    private void openMenu(final By buttonLocator) {
        WebElement navbarMenu = webDriver.findElement(By.cssSelector(".navbar-header .navbar-toggle"));
        List<WebElement> collapseableMenu = webDriver.findElements(By.cssSelector(".navbar-collapse.collapse"));
        WebElement menu;
        //Should always be true when window size < 760 pixels
        if (collapseableMenu.size() > 0) {
            menu = collapseableMenu.get(0);
            if (navbarMenu.isDisplayed() && !menu.isDisplayed()) {
                navbarMenu.click();
                waitor.waitForElement(buttonLocator);
            }
        }
    }
}
