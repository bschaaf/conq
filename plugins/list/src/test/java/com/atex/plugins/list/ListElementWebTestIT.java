package com.atex.plugins.list;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnext.web.WebTest;
import com.polopoly.testnj.TestNJRunner;


@RunWith(TestNJRunner.class)
@ImportTestContent(files = {"com.atex.plugins.list.ArticleMock.xml",
        "ListElementWebTestIT.content" })
public class ListElementWebTestIT {

    @Inject
    private WebTest webTest;

    private String listElementId;
    private String cappedListElementId;
    private String siteId;
    @Before
    public void init() {
        String className = this.getClass().getSimpleName();
        listElementId = className + ".element";
        cappedListElementId = className + ".cappedElement";
        siteId = className + ".site";
    }
    @Test
    public void testAllItems() {
        webTest.loadPage(String.format("/cmlink/%s/%s", siteId, listElementId));
        webTest.assertContains("My first article");
        webTest.assertContains("My second article");
        webTest.assertContains("My third article");
        webTest.assertContainsHtml("my-first-article-");
        webTest.assertContainsHtml("my-second-article-");
        webTest.assertContainsHtml("my-third-article-");
        testBasics();
    }

    @Test
    public void testCappedItems() {
        webTest.loadPage(String.format("/cmlink/%s/%s", siteId, cappedListElementId));
        webTest.assertContains("My first article");
        webTest.assertContains("My second article");
        webTest.assertDoesNotContain("My third article");
        webTest.assertContainsHtml("my-first-article-");
        webTest.assertContainsHtml("my-second-article-");
        webTest.assertDoesNotContainHtml("my-third-article-");
        webTest.assertContainsXPath("//section/div/ul[1]/li[1]/a/span[@class='time']");
        webTest.assertContainsXPath("//section/div/ul[1]/li[2]/a/span[@class='time']");
        webTest.assertContainsXPath("//section/div/ul[1][contains(@class,'lined')]");
        testBasics();
    }

    private void testBasics() {
        webTest.validatePageHtml();
        webTest.validateUnknownVelocityVariables();
    }
}
