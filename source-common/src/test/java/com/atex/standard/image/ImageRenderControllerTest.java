package com.atex.standard.image;

import com.atex.onecms.content.Content;
import com.atex.onecms.content.ContentId;
import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.Status;
import com.atex.onecms.content.Subject;
import com.atex.onecms.content.aspects.Aspect;
import com.atex.onecms.image.AspectRatio;
import com.atex.onecms.image.ImageEditInfoAspectBean;
import com.atex.onecms.image.ImageFormat;
import com.atex.onecms.image.ImageFormats;
import com.atex.onecms.image.ImageFormatsPolicy;
import com.atex.onecms.image.ImageInfoAspectBean;
import com.atex.onecms.ws.image.ImageServiceUrlBuilder;
import static com.atex.standard.AbstractTest.getArg;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

@SuppressWarnings("methodname")
public class ImageRenderControllerTest {

    private static final double ASPECT_RATIO_ORIGINAL_IMAGE = 6.0D / 8.0D * 100F;
    private static final double ASPECT_RATIO_ROTATED_IMAGE = 8.0D / 6.0D * 100F;
    private static final double ASPECT_RATIO_IMAGE_FORMAT = 9.0d / 16.0d * 100f;
    private static final int NO_WRITES_TO_LOCAL_MODEL = 5;
    private static final int NO_WRITES_TO_LOCAL_MODEL_NO_ASPECT_RATIO = NO_WRITES_TO_LOCAL_MODEL - 1;
    private static final String IMAGE_TITLE = "My simple image for ImageRenderControllerTestIT";
    private static final String IMAGE_BYLINE = "Author is Gong the image maker";
    private static final String IMAGE_ALT = "Simple text describing the image";
    private static final String FILE_PATH = "image.jpg";
    private static final int IMAGE_WIDTH = 800;
    private static final int IMAGE_HEIGHT = 600;

    @Mock
    private ControllerContext context;
    @Mock
    private RenderRequest request;
    @Mock
    private TopModel topModel;
    @Mock
    private ModelWrite stack;
    @Mock
    private ModelWrite local;
    @Mock
    private ContentResult<ImageContentDataBean> contentResult;
    @Mock
    private CmClient cmClient;
    @Mock
    private ContentManager contentManager;
    private ImageRenderController imageRenderController;
    private ArgumentCaptor<String> nameCapture = ArgumentCaptor.forClass(String.class);
    private ArgumentCaptor<Object> valueCapture = ArgumentCaptor.forClass(Object.class);

    private ContentVersionId formatsId = new ContentVersionId("My-delegationId", "My-key", "My-version");
    @Mock
    private ContentResult<ImageFormats> formatResult;
    @Mock
    private Content<ImageFormats> imageFormatsContent;
    @Mock
    private ImageFormats imageFormatsContentData;
    private ImageFormat imageFormat;


    @Before
    public void before() throws CMException {
        MockitoAnnotations.initMocks(this);

        // **************************************************************
        // Setup for testing method populateModelBeforeCacheKey
        // **************************************************************
        com.polopoly.cm.ContentId imageContentId = new com.polopoly.cm.ContentId(1, 1111);
        ContentId contentId = IdUtil.fromPolicyContentId(imageContentId);
        ContentVersionId versionedImageId = new ContentVersionId(contentId, "VERSION");

        imageFormat = new ImageFormat("My image format", new AspectRatio(16, 9));

        when(contentManager.resolve(eq(contentId), any(Subject.class))).thenReturn(versionedImageId);
        when(contentManager.get(
                eq(versionedImageId),
                anyString(),
                eq(ImageContentDataBean.class),
                any(Map.class),
                any(Subject.class))).thenReturn(contentResult);
        when(cmClient.getContentManager()).thenReturn(contentManager);

        imageRenderController = spy(new ImageRenderController() {
            @Override
            protected CmClient getCmClient(final ControllerContext c) {
                return cmClient;
            }

            protected String getSecret(final CmClient client) {
                return "SECRET";
            }
        });
        when(topModel.getStack()).thenReturn(stack);
        when(stack.getAttribute(anyString())).thenReturn(null);
        when(context.getContentId()).thenReturn(imageContentId);
        when(topModel.getLocal()).thenReturn(local);

        // **************************************************************
        // Setup for testing methods used for getting aspect ratio.
        // **************************************************************
        when(contentManager.resolve(ImageFormatsPolicy.DEFAULT_IMAGE_FORMAT_CONFIG_ID, Subject.NOBODY_CALLER)).
                thenReturn(formatsId);
        when(contentManager.get(
                eq(formatsId),
                anyString(),
                eq(ImageFormats.class),
                any(Map.class),
                any(Subject.class))).thenReturn(formatResult);
        when(formatResult.getStatus()).thenReturn(Status.OK);
        when(formatResult.getContent()).thenReturn(imageFormatsContent);
        when(imageFormatsContent.getContentData()).thenReturn(imageFormatsContentData);
        when(imageFormatsContentData.getFormat(imageFormat.getName())).thenReturn(imageFormat);
    }

    /**
     * Test that attributes urlBuilder, inverseAspectRatio, title, byline and alt is set on local model.
     *
     */
    @Test
    public void populateModelBeforeCacheKey()  {
        rotate(0);
        imageRenderController.populateModelBeforeCacheKey(request, topModel, context);
        Mockito.verify(local, Mockito.atLeast(NO_WRITES_TO_LOCAL_MODEL))
                .setAttribute(nameCapture.capture(), valueCapture.capture());
        assertEquals(ASPECT_RATIO_ORIGINAL_IMAGE, getArg(nameCapture, valueCapture, "inverseAspectRatio"));
        assertEquals(IMAGE_TITLE, getArg(nameCapture, valueCapture, "title"));
        assertEquals(IMAGE_BYLINE, getArg(nameCapture, valueCapture, "byline"));
        assertEquals(IMAGE_ALT, getArg(nameCapture, valueCapture, "alt"));
        Object urlBuilder = getArg(nameCapture, valueCapture, "urlBuilder");
        assertTrue(urlBuilder instanceof ImageServiceUrlBuilder);
    }

    /**
     * Test that inverseAspectRatio is set correctly when image is rotated 90 degrees.
     * With and without imageformat.
     *
     */
    @Test
    public void populateModelBeforeCacheKeyWhenImageRotation90() {
        rotate(90);
        populateModelAndAssertInverseAspectRatio(ASPECT_RATIO_ROTATED_IMAGE);
        format(imageFormat);
        populateModelAndAssertInverseAspectRatio(ASPECT_RATIO_IMAGE_FORMAT);
    }

    /**
     * Test that inverseAspectRatio is set correctly when image is rotated 180 degrees.
     * With and without imageformat.
     *
     */
    @Test
    public void populateModelBeforeCacheKeyWhenImageRotation180() {
        rotate(180);
        populateModelAndAssertInverseAspectRatio(ASPECT_RATIO_ORIGINAL_IMAGE);
        format(imageFormat);
        populateModelAndAssertInverseAspectRatio(ASPECT_RATIO_IMAGE_FORMAT);
    }

    /**
     * Test that inverseAspectRatio is set correctly when image is rotated 270 degrees.
     * With and without imageformat.
     *
     */
    @Test
    public void populateModelBeforeCacheKeyWhenImageRotation270() {
        rotate(270);
        populateModelAndAssertInverseAspectRatio(ASPECT_RATIO_ROTATED_IMAGE);
        format(imageFormat);
        populateModelAndAssertInverseAspectRatio(ASPECT_RATIO_IMAGE_FORMAT);
    }

    @Test
    public void populateModelBeforeCacheKeyWhenUnknownImageFormat() {
        rotate(0);
        format(new ImageFormat("Non existing format", null));
        populateModelAndAssertInverseAspectRatio(ASPECT_RATIO_ORIGINAL_IMAGE);
    }

    @Test
    public void populateModelBeforeCacheKeyWhenImageFormatWidth0() {
        rotate(0);
        imageFormat.setAspectRatio(new AspectRatio(0, 1));
        format(imageFormat);
        populateModelAndAssertInverseAspectRatio(ASPECT_RATIO_ORIGINAL_IMAGE);
    }

    @Test
    public void populateModelBeforeCacheKeyWhenImageFormatHeight0() {
        rotate(0);
        imageFormat.setAspectRatio(new AspectRatio(1, 0));
        format(imageFormat);
        populateModelAndAssertInverseAspectRatio(ASPECT_RATIO_ORIGINAL_IMAGE);
    }

    @Test
    public void populateModelBeforeCacheKeyWhenImageFormatAspectRatioNull() {
        rotate(0);
        imageFormat.setAspectRatio(null);
        format(imageFormat);
        populateModelAndAssertInverseAspectRatio(ASPECT_RATIO_ORIGINAL_IMAGE);
    }

    @Test
    public void populateModelBeforeCacheKeyWhenImageFormatNull() {
        rotate(0);
        format(null);
        populateModelAndAssertInverseAspectRatio(ASPECT_RATIO_ORIGINAL_IMAGE);
    }

    private void populateModelAndAssertInverseAspectRatio(final Double expectedInverseAspectRatio) {
        imageRenderController.populateModelBeforeCacheKey(request, topModel, context);
        int numInvocations = expectedInverseAspectRatio == null
                             ? NO_WRITES_TO_LOCAL_MODEL_NO_ASPECT_RATIO : NO_WRITES_TO_LOCAL_MODEL;
        Mockito.verify(local, Mockito.atLeast(numInvocations))
               .setAttribute(nameCapture.capture(), valueCapture.capture());
        Object inverseAspectRatio = getArg(nameCapture, valueCapture, "inverseAspectRatio");
        assertEquals("Wrong inverseAspectRatio", expectedInverseAspectRatio, inverseAspectRatio);
    }

    private void rotate(final int degrees) {
        List<Aspect> aspects = new ArrayList<>();

        ImageContentDataBean imageContentDataBean = new ImageContentDataBean();
        imageContentDataBean.setByline(IMAGE_BYLINE);
        imageContentDataBean.setTitle(IMAGE_TITLE);
        imageContentDataBean.setDescription(IMAGE_ALT);

        ImageInfoAspectBean imageInfoAspectBean = new ImageInfoAspectBean(FILE_PATH, IMAGE_WIDTH, IMAGE_HEIGHT);
        aspects.add(new Aspect(ImageInfoAspectBean.ASPECT_NAME, imageInfoAspectBean));

        ImageEditInfoAspectBean imageEditInfoAspectBean = new ImageEditInfoAspectBean();
        imageEditInfoAspectBean.setRotation(degrees);
        aspects.add(new Aspect(ImageEditInfoAspectBean.ASPECT_NAME, imageEditInfoAspectBean));

        Content<ImageContentDataBean> imageContent =
                new Content<>(ImageContentDataBean.class.getName(),
                              new Aspect<>(ImageContentDataBean.class.getName(), imageContentDataBean),
                              null,
                              null,
                              aspects);
        when(contentResult.getContent()).thenReturn(imageContent);
    }

    private void format(final ImageFormat format) {
        String formatName = format == null ? null : format.getName();
        when(stack.getAttribute("imageFormat")).thenReturn(formatName);
    }

}
