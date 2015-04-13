package com.atex.standard.image;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.atex.gong.utils.ContentTestUtil;
import com.atex.gong.utils.SolrTestUtil;
import com.atex.gong.utils.SolrTestUtil.Searcher;
import com.google.inject.Inject;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnj.TestNJRunner;

/**
 * Tests indexed fields of an image both internal and public.
 */
@RunWith(TestNJRunner.class)
@ImportTestContent(
        dir = "/com/atex/standard/image/",
        waitUntilContentsAreIndexed = {"StandardImageIndexTestIT.content" })
public class StandardImageIndexTestIT {

    @Inject
    private SolrTestUtil searchUtil;
    @Inject
    private ContentTestUtil contentUtil;
    private ImageContentDataBean imageContentDataBean;
    private Searcher searcher;

    @Before
    public void init() throws CMException {
        ContentPolicy siteToTest = (ContentPolicy) contentUtil.getTestPolicy(".site");
        ImagePolicy contentToTest = (ImagePolicy) contentUtil.getTestPolicy();
        imageContentDataBean = (ImageContentDataBean) contentToTest.getContentData();
        searcher = searchUtil.makePreconfigureSearcher(contentUtil.getTestInputTemplateId(), siteToTest);
    }

    @Test
    public void testNamePresentInIndex() throws CMException {
        String textToTestFor = String.format("title:\"%s\"", imageContentDataBean.getTitle());
        searcher.assertHitsInInternalIndex(1, textToTestFor);
    }

    @Test
    public void testDescriptionPresentInIndex() throws CMException {
        String textToTestFor = String.format("text:\"%s\"", imageContentDataBean.getDescription());
        searcher.assertHitsInInternalIndex(1, textToTestFor);
    }

    @Test
    public void testBylinePresentInIndex() throws CMException {
        String textToTestFor = String.format("byline:\"%s\"", imageContentDataBean.getByline());
        searcher.assertHitsInInternalIndex(1, textToTestFor);
    }
}
