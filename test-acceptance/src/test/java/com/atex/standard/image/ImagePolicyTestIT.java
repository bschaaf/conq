package com.atex.standard.image;

import com.atex.onecms.content.ContentFileInfo;
import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.ContentWrite;
import com.atex.onecms.content.FilesAspectBean;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.Subject;
import com.atex.onecms.content.SubjectUtil;
import com.atex.onecms.content.files.FileInfo;
import com.atex.onecms.content.files.FileServiceClient;
import com.atex.onecms.image.ImageEditInfoAspectBean;
import com.atex.onecms.image.ImageInfoAspectBean;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.policy.PrepareResult;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnj.TestNJRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(TestNJRunner.class)
@ImportTestContent(dir = "/com/atex/standard/image/",
        files = {"ImagePolicyTestIT.testImportWithFile.xml"})
public class ImagePolicyTestIT {
    public static final ExternalContentId INPUT_TEMPLATE_ID = new ExternalContentId("standard.Image");

    @Inject
    private PolicyCMServer cmServer;
    @Inject
    private ContentManager contentManager;
    @Inject
    private FileServiceClient fileServiceClient;

    @Test
    public void testImportWithFile() throws Exception {
        ExternalContentId contentId = new ExternalContentId(ContentId.UNDEFINED_MAJOR,
                ContentId.UNDEFINED_MINOR,
                VersionedContentId.LATEST_VERSION,
                ImagePolicyTestIT.class
                        .getSimpleName() + ".testImportWithFile");
        Policy policy = cmServer.getPolicy(contentId);

        ImagePolicy content = (ImagePolicy) cmServer.createContentVersion(policy.getContentId());
        ImageEditInfoAspectBean origEditInfo = new ImageEditInfoAspectBean();
        origEditInfo.setFlipHorizontal(true);
        content.setImageEditInfo(origEditInfo);
        content.commit();

        ContentVersionId version = IdUtil.contentVersionForPolicy(content.getContentId(), contentManager);
        Subject subject = SubjectUtil.fromCaller(cmServer.getCurrentCaller());
        ContentResult<ImageContentDataBean> res =
                contentManager.get(version,
                        null,
                        ImageContentDataBean.class,
                        null,
                        subject);

        assertTrue("Should get image from content manager", res.getStatus().isOk());
        ImageInfoAspectBean imageInfo =
                (ImageInfoAspectBean) res.getContent()
                        .getAspect(ImageInfoAspectBean.ASPECT_NAME)
                        .getData();
        assertEquals("Wrong image path", "images/image.png", imageInfo.getFilePath());

        FilesAspectBean files = (FilesAspectBean) res.getContent()
                .getAspect(FilesAspectBean.ASPECT_NAME)
                .getData();
        Map<String, ContentFileInfo> filesMap = files.getFiles();
        assertEquals(1, filesMap.size());
        ContentFileInfo fileInfo = filesMap.get("images/image.png");
        String fileUri = fileInfo.getFileUri();

        FileInfo fileInfoFromService = fileServiceClient.getFileService().getFileInfo(fileUri, subject);

        assertEquals("images/image.png", fileInfoFromService.getOriginalPath());

        assertEquals("Wrong width", 200, imageInfo.getWidth());
        assertEquals("Wrong height", 208, imageInfo.getHeight());

        ImageEditInfoAspectBean editInfo =
                (ImageEditInfoAspectBean) res.getContent().getAspect(ImageEditInfoAspectBean.ASPECT_NAME).getData();
        assertEquals("Image should be flipped horizontally", true, editInfo.isFlipHorizontal());
        assertEquals("Image should not be flipped vertically", false, editInfo.isFlipVertical());

        assertEquals("Title was not fetched from metadata",
                "Orbiting Carbon Observatory-2 (OCO-2) Launch",
                ((Content) cmServer.getPolicy(contentId)).getName());

        ImageContentDataBean contentData = res.getContent().getContentData();
        contentData.setTitle("Overridden title");
        ContentResult<ImageContentDataBean> update = contentManager.update(version.getContentId(),
                new ContentWrite<>(res.getContent())
                        .setContentData(contentData),
                subject);
        assertTrue("Failed to write updated title", update.getStatus().isOk());

        assertEquals("Title was fetched from metadata even though there is a perfectly good title in contentData",
                "Overridden title",
                ((Content) cmServer.getPolicy(contentId)).getName());
    }

    @Test
    public void testValidation() throws Exception {
        ImagePolicy content = (ImagePolicy) cmServer.createContent(1, INPUT_TEMPLATE_ID);
        PrepareResult result = content.prepare();
        assertTrue("Prepare result should be in error for empty content", result.isError());
        cmServer.abortContent(content);
    }
}
