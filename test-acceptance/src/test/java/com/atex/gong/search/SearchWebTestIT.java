package com.atex.gong.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.atex.gong.utils.ContentTestUtil;
import com.google.inject.Inject;
import com.polopoly.testnext.base.ChangeList;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnj.TestNJRunner;

@RunWith(TestNJRunner.class)
@ImportTestContent(
    dir = "/com/atex/gong/search/",
    files = {"SearchWebTestIT.content" },
    waitUntilContentsAreIndexed = {"SearchWebTestIT.article1",
            "SearchWebTestIT.article2",
            "SearchWebTestIT.article3" })
public class SearchWebTestIT {

    @Inject
    private WebDriver webDriver;

    @Inject
    private ContentTestUtil contentUtil;

    @Inject
    private ChangeList changeList;
    private WebDriverWait wait;

    @Before
    public void init() {
        wait = new WebDriverWait(webDriver, 15);
        String siteId = contentUtil.getTestContentId(".site");
        changeList.waitFor("preview");

        webDriver.get(String.format("http://localhost:8080/cmlink/%s", siteId));
    }

    @Test
    public void testSearchBoxPresent() throws Exception {
        By searchSelector = By.cssSelector("input.search-query");
        wait.until(ExpectedConditions.visibilityOfElementLocated(searchSelector));
    }

    @Test
    public void testSearchNoResults() throws Exception {
        String query = "A_STRING_WHICH_WILL_NOT_MATCH_ANYTHING";
        search(query);

        By alertSelector = By.cssSelector(".search-results .alert-info");
        wait.until(ExpectedConditions.visibilityOfElementLocated(alertSelector));
        WebElement alert = webDriver.findElement(alertSelector);
        assertEquals("Your search for " + query + " did not match anything.", alert.getText());
    }

    @Test
    public void testSearchWithOneResult() throws Exception {
        String query = "test_article_1_lead";
        search(query);

        String resultSelector = ".search-results .hit";
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(resultSelector)));

        WebElement resultTitle = webDriver.findElement(By.cssSelector(resultSelector + " a"));
        assertEquals("TestArticle1", resultTitle.getText());

        WebElement resultLead = webDriver.findElement(By.cssSelector(resultSelector + " .hit_text"));
        assertEquals("test_article_1_lead, multiple_test_article_lead, test_pagination.", resultLead.getText());

        // Check that link is functional
        resultTitle.click();

        By articleSelector = By.cssSelector("section.article h1");
        wait.until(ExpectedConditions.visibilityOfElementLocated(articleSelector));

        WebElement articleTitle = webDriver.findElement(articleSelector);
        assertEquals("Article link looks incorrect", "TestArticle1", articleTitle.getText());
    }

    @Test
    public void testSearchWithMultipleResults() throws Exception {
        String query = "multiple_test_article_lead";
        search(query);

        assertResults("TestArticle1", "TestArticle2");
    }

    @Test
    public void testSearchWithFacets() throws Exception {
        String query = "multiple_test_article_lead";
        search(query);

        assertResults("TestArticle1", "TestArticle2");

        // Select facet

        WebElement article1 = getFacetValue("1 article1");
        assertNotNull("Facet not present", article1);
        article1.click();

        assertResults("TestArticle1");

        // Reset facet

        article1 = getFacetValue("article1");
        assertNotNull("Selected facet not present", article1);
        article1.click();

        assertResults("TestArticle1", "TestArticle2");
    }

    @Test
    public void testPagination() throws Exception {
        String query = "test_pagination";
        search(query);

        assertResults("TestArticle1", "TestArticle2");

        By pageSelector = By.cssSelector(".pagination li a");

        List<WebElement> resultPages = webDriver.findElements(pageSelector);
        assertEquals("Incorrect number of result pages", 3, resultPages.size());

        resultPages.get(2).click();
        assertResults("TestArticle3");

        resultPages = webDriver.findElements(pageSelector);
        assertEquals("Incorrect number of result pages", 3, resultPages.size());

        resultPages.get(0).click();
        assertResults("TestArticle1", "TestArticle2");

        resultPages = webDriver.findElements(pageSelector);
        resultPages.get(1).click();
        assertResults("TestArticle3");
    }

    private void assertResults(final String ... expected) {
        String resultSelector = ".search-results .hit";
        By titleSelector = By.cssSelector(resultSelector + " a");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(resultSelector)));

        List<WebElement> resultTitle = webDriver.findElements(titleSelector);
        assertEquals("Incorrect number of search results", expected.length, resultTitle.size());

        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Incorrect result title", expected[i], resultTitle.get(i).getText());
        }
    }

    private WebElement getFacetValue(final String valueName) {
        List<WebElement> facets = webDriver.findElements(By.cssSelector(".search-results .facets a"));
        WebElement article1 = null;
        for (WebElement facet : facets) {
            if (valueName.equals(facet.getText())) {
                article1 = facet;
                break;
            }
        }
        return article1;
    }

    private void search(final String query) {
        By searchSelector = By.cssSelector("input.search-query");
        wait.until(ExpectedConditions.visibilityOfElementLocated(searchSelector));
        WebElement search = webDriver.findElement(searchSelector);
        search.sendKeys(query);
        search.submit();
    }
}
