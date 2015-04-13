package com.atex.gong.http;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;
import com.polopoly.testnj.TestNJRunner;
import com.polopoly.testnext.web.WebTest;

/**
 * Tests responses of web applications present in project.
 */
@RunWith(TestNJRunner.class)
public class SurfWebAppsIT {

    private static final int HTML_STATUS_CODE_OK = 200;

    @Inject
    private WebTest webTest;

    @Test
    public void surfDispatcher() throws Exception {
        testWebapp("/");
    }

    @Test
    public void surfGui() throws Exception {
        testWebapp("/polopoly");
    }

    @Test
    public void surfSolr() throws Exception {
        testWebapp("/solr");
    }

    @Test
    public void surfSolrIndexer() throws Exception {
        testWebapp("/solr-indexer");
    }

    @Test
    public void surfStatisticsServer() throws Exception {
        testWebapp("/statistics-server");
    }

    protected void testWebapp(final String contextPath) throws Exception {
        webTest.loadPage(contextPath);

        // Test status code
        webTest.assertStatusCode(HTML_STATUS_CODE_OK);

        // Test content length (not required)
        String contentLength = webTest.getResponse().getResponseHeaderValue("Content-Length");
        assertTrue("Content-Length was 0", (contentLength == null || Integer.parseInt(contentLength) != 0));

        // Test content type
        String contentType = webTest.getResponse().getResponseHeaderValue("Content-Type");
        assertTrue("Response did not contain Content-Type for path " + contextPath, contentType != null);
    }
}
