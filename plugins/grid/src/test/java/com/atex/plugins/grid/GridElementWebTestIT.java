package com.atex.plugins.grid;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnext.web.WebTest;
import com.polopoly.testnj.TestNJRunner;

@RunWith(TestNJRunner.class)
@ImportTestContent(files = {
        "com.atex.plugins.grid.TeaserableMock.xml",
        "GridElementWebTestIT.content" })
public class GridElementWebTestIT {

    @Inject
    private WebTest webTest;

    @Before
    public void init() {
        String className = this.getClass().getSimpleName();
        String gridElementId = className + ".element";
        String siteId = className + ".site";
        webTest.loadPage(String.format("/cmlink/%s/%s", siteId, gridElementId));
    }

    @Test
    public void testTitlesPresent() {
        webTest.assertContains("Article 1 for grid element test");
        webTest.assertContains("Article 2 for grid element test");
        webTest.assertContains("Article 3 for grid element test");
        webTest.assertContains("Article 4 for grid element test");
    }

    @Test
    public void testTextsPresent() {
        webTest.assertContains("Text for article 1 for grid element test");
        webTest.assertContains("Text for article 2 for grid element test");
        webTest.assertContains("Text for article 3 for grid element test");
        webTest.assertContains("Text for article 4 for grid element test");
    }

    @Test
    public void testAttributesPresent() {
        webTest.assertContains("my-undefined-language-key");
        final String xPath = "//span[@class='my-attribute-css-class']";
        webTest.assertContainsXPath(xPath);
        // The question marks are present in the assert below because "my-undefined-language-key" is not a defined
        // language key.
        webTest.assertContainsByXPath(xPath, "???my-undefined-language-key???");
    }

    @After
    public void validateUnknownVelocityVariables() {
        webTest.validateUnknownVelocityVariables();
    }

    @After
    public void validatePageHtml() {
        webTest.validatePageHtml();
    }
}
