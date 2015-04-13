package com.atex.standard.image;

import com.atex.gong.AbstractWebTestBase;
import com.atex.gong.utils.ContentTestUtil;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.Subject;
import com.atex.onecms.ws.image.ImageServiceUrlBuilder;
import com.google.inject.Inject;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.common.util.FriendlyUrlConverter;
import com.polopoly.testnext.base.ChangeList;
import com.polopoly.testnext.base.ImportTestContent;
import org.junit.Before;
import org.junit.Test;

/**
 * Test image visible on site.
 */
@ImportTestContent(dir = "/com/atex/standard/image/")
public class StandardImageWebTestIT extends AbstractWebTestBase {

    private ImageContentDataBean imageContentDataBean;

    @Inject
    private ContentTestUtil contentUtil;

    @Inject
    private ChangeList changeList;

    @Inject
    private CmClient cmClient;

    @Before
    public void init() throws CMException {
        ImagePolicy contentToTest = (ImagePolicy) contentUtil.getTestPolicy();
        imageContentDataBean = (ImageContentDataBean) contentToTest.getContentData();
        String siteId = contentUtil.getTestContentId(".site");
        String contentId = contentUtil.getTestContentId();
        changeList.waitFor("preview");
        webTest.loadPage(String.format("/cmlink/%s/%s", siteId, contentId));
    }

    @Test
    public void testImagePresent() {
        webTest.assertContainsXPath("//img[@src]");
        webTest.assertContainsXPath("//div[@data-src]");
    }

    @Test
    public void testAltTextPresent() {
        String xPath = String.format("//img[@alt='%s']", imageContentDataBean.getDescription());
        webTest.assertContainsXPath(xPath);
        xPath = String.format("//div[contains(@class, 'delayed-image-load') and @data-alt='%s']",
                imageContentDataBean.getDescription());
        webTest.assertContainsXPath(xPath);
    }

    @Test
    public void testFriendlyUrl() throws Exception {

        String servicePath = System.getProperty(
                ImageServiceUrlBuilder.SERVICE_PATH_PROPERTY,
                ImageServiceUrlBuilder.DEFAULT_SERVICE_PATH);

        VersionedContentId contentId = contentUtil.getTestPolicy().getContentId();
        ContentVersionId contentVersionId =
            cmClient.getContentManager().resolve(IdUtil.fromPolicyContentId(contentId), Subject.NOBODY_CALLER);

        String prefix = servicePath + "/" + IdUtil.toVersionedIdString(contentVersionId) + "/";
        String suffix = ".jpg";
        String value = prefix + FriendlyUrlConverter.convert(imageContentDataBean.getTitle()) + suffix;

        String xPath = String.format("//div[contains(@class, 'delayed-image-load') and contains(@data-src, '%s')]",
                value);

        webTest.assertContainsXPath(xPath);
    }
}
