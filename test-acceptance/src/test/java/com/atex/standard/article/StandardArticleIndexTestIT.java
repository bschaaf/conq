package com.atex.standard.article;

import com.atex.gong.utils.ContentTestUtil;
import com.atex.gong.utils.SolrTestUtil;
import com.atex.gong.utils.SolrTestUtil.Searcher;
import com.google.inject.Inject;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.common.lang.TimeUtil;
import com.polopoly.management.ServiceNotAvailableException;
import com.polopoly.search.solr.schema.IndexFields;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnj.TestNJRunner;
import org.apache.solr.client.solrj.SolrServerException;
import org.jsoup.Jsoup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

/**
 * Tests indexed fields of an article both internal and public.
 */
@RunWith(TestNJRunner.class)
@ImportTestContent(
        dir = "/com/atex/standard/article/",
        waitUntilContentsAreIndexed = {"StandardArticleIndexTestIT.content" })
public class StandardArticleIndexTestIT {

    @Inject
    private SolrTestUtil searchUtil;

    @Inject
    private ContentTestUtil contentUtil;
    private ContentPolicy contentToTest;
    private Searcher searcher;

    @Before
    public void init() throws CMException {
        ContentPolicy siteToTest = (ContentPolicy) contentUtil.getTestPolicy(".site");
        contentToTest = (ContentPolicy) contentUtil.getTestPolicy();
        searcher = searchUtil.makePreconfigureSearcher(contentUtil.getTestInputTemplateId(), siteToTest);
    }

    @Test
    public void testTitlePresentInIndex() throws CMException, SolrServerException, ServiceNotAvailableException {
        String textToTestFor = String.format("title:\"%s\"", contentToTest.getName());
        searcher.assertHitsInAllIndexes(1, textToTestFor);
    }

    @Test
    public void testLeadPresentInIndex() throws CMException, SolrServerException, ServiceNotAvailableException {
        String textToTestFor = String.format("text:\"%s\"", contentToTest.getComponent("lead", "value"));
        searcher.assertHitsInAllIndexes(1, textToTestFor);
    }

    @Test
    public void testBodyPresentInIndex() throws CMException, SolrServerException, ServiceNotAvailableException {
        String textToTestFor = contentToTest.getComponent("body", "value");
        //Let's strip the html out
        textToTestFor = Jsoup.parse(textToTestFor).text();
        textToTestFor = String.format("text:(%s)", textToTestFor);
        searcher.assertHitsInAllIndexes(1, textToTestFor);
    }

    @Test
    public void testBylinePresentInIndex() throws CMException {
        String textToTestFor = String.format("byline:\"%s\"", contentToTest.getComponent("byline", "value"));
        searcher.assertHitsInInternalIndex(1, textToTestFor);

    }

    @Test
    public void testTimeStatePresentInIndex() throws CMException {
        testOnTimePresentInIndex();
        testOffTimePresentInIndex();
    }

    @Test
    public void testOnlineStatePresentInIndex() throws CMException {
        String id = contentToTest.getContentId().getContentId().getContentIdString();
        String onlineState = contentToTest.getComponent("p.Content.state", "onlineState");
        String textToTestFor =
                IndexFields.CONTENT_ID + ":" + id + " AND " + IndexFields.VISIBLE_ONLINE + ":" + onlineState;
        searcher.assertHitsInAllIndexes(1, textToTestFor);
    }

    private void testOnTimePresentInIndex() throws CMException {
        String onTimeInMillis = contentToTest.getComponent("polopoly.TimeState", "onTime");
        String formattedDate = formatSolrDate(onTimeInMillis);
        String textToTestFor = IndexFields.ON_TIME + ":[" + formattedDate + " TO " + formattedDate + "]";
        searcher.assertHitsInAllIndexes(1, textToTestFor);
    }

    private void testOffTimePresentInIndex() throws CMException {
        String offTimeInMillis = contentToTest.getComponent("polopoly.TimeState", "offTime");
        String formattedDate = formatSolrDate(offTimeInMillis);
        String textToTestFor = IndexFields.OFF_TIME + ":[" + formattedDate + " TO " + formattedDate + "]";
        searcher.assertHitsInAllIndexes(1, textToTestFor);
    }

    private String formatSolrDate(final String milliseconds) {
        return TimeUtil.ISO_8601_COMBINED_DATETIME_SECONDS_FORMATTER.format(
                new Date(Long.parseLong(milliseconds)));
    }
}
