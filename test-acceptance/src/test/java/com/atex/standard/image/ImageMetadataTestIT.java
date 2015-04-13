package com.atex.standard.image;


import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.Subject;
import com.atex.standard.image.exif.MetadataTags;
import com.google.inject.Inject;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.testnj.TestNJRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(TestNJRunner.class)
public class ImageMetadataTestIT {

    @Inject
    private PolicyCMServer cmServer;

    @Inject
    private ContentManager contentManager;

    @Test
    public void shouldPopulateTitleFromImageFile() throws Exception {
        ImagePolicy imagePolicy = (ImagePolicy) cmServer.createContent(1, new ExternalContentId("standard.Image"));
        imagePolicy.importFile("image.jpg",
                getClass().getClassLoader().getResourceAsStream("com/atex/standard/image/image-with-metadata.jpg"));
        cmServer.commitContent(imagePolicy);

        VersionedContentId contentId = imagePolicy.getContentId();
        String name = cmServer.getPolicy(contentId.getContentId()).getContent().getName();
        assertEquals("Polopoly", name);
        ContentVersionId contentVersionId = IdUtil.contentVersionForPolicy(contentId, contentManager);
        ContentResult<ImageContentDataBean> contentResult = contentManager.get(contentVersionId,
                                                                               null,
                                                                               ImageContentDataBean.class,
                                                                               null,
                                                                               Subject.NOBODY_CALLER);
        MetadataTags tags = (MetadataTags) contentResult.getContent().getAspectData(MetadataTags.ASPECT_NAME);
        assertEquals("Wrong description", "Caption with åäö", tags.getDescription());
        assertEquals("Wrong byline", "Atex", tags.getByline());
        assertEquals("Wrong width", new Integer(800), tags.getImageWidth());
        assertEquals("Wrong height", new Integer(600), tags.getImageHeight());
    }
}
