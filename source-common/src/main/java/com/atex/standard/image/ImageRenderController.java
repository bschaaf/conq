package com.atex.standard.image;

import com.atex.onecms.content.Content;
import com.atex.onecms.content.ContentId;
import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.Subject;
import com.atex.onecms.content.aspects.Aspect;
import com.atex.onecms.image.AspectRatio;
import com.atex.onecms.image.ImageEditInfoAspectBean;
import com.atex.onecms.image.ImageFormat;
import com.atex.onecms.image.ImageFormats;
import com.atex.onecms.image.ImageFormatsPolicy;
import com.atex.onecms.image.ImageInfoAspectBean;
import com.atex.onecms.ws.image.ImageServiceConfigurationProvider;
import com.atex.onecms.ws.image.ImageServiceUrlBuilder;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.common.util.FriendlyUrlConverter;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageRenderController extends RenderControllerBase {

    private static final Logger LOGGER = Logger.getLogger(ImageRenderController.class.getName());

    @Override
    public void populateModelBeforeCacheKey(final RenderRequest request,
                                            final TopModel topModel,
                                            final ControllerContext context) {
        super.populateModelBeforeCacheKey(request, topModel, context);

        String imageFormatName = (String) topModel.getStack().getAttribute("imageFormat");
        if (imageFormatName == null) {
            imageFormatName = "default";
        }

        CmClient cmClient = getCmClient(context);
        ContentManager contentManager = cmClient.getContentManager();
        ContentId contentId = IdUtil.fromPolicyContentId(context.getContentId());
        ContentVersionId versionId = contentManager.resolve(contentId, Subject.NOBODY_CALLER);
        ContentResult<ImageContentDataBean> result =
                contentManager.get(versionId, null, ImageContentDataBean.class, null, Subject.NOBODY_CALLER);
        Content<ImageContentDataBean> content = result.getContent();
        ImageInfoAspectBean imageInfo = (ImageInfoAspectBean) content.getAspectData(ImageInfoAspectBean.ASPECT_NAME);

        if (imageInfo != null && imageInfo.getFilePath() != null) {
            ImageContentDataBean contentData = content.getContentData();

            String secret = getSecret(cmClient);
            ImageServiceUrlBuilder urlBuilder = new ImageServiceUrlBuilder(content, secret).format(imageFormatName);
            topModel.getLocal().setAttribute("urlBuilder", urlBuilder);

            if (contentData != null && contentData.getTitle() != null) {
                urlBuilder.name(FriendlyUrlConverter.convert(contentData.getTitle()));
            }

            AspectRatio aspectRatio = getAspectRatio(getImageFormat(imageFormatName, contentManager), content);
            if (aspectRatio != null) {
                topModel.getLocal().setAttribute("inverseAspectRatio",
                        (double) aspectRatio.getHeight() / (double) aspectRatio.getWidth() * 100f);
            }

            if (contentData != null) {
                topModel.getLocal().setAttribute("title", contentData.getTitle());
                topModel.getLocal().setAttribute("byline", contentData.getByline());
                topModel.getLocal().setAttribute("alt", contentData.getDescription());
            }
        } else {
            LOGGER.log(Level.WARNING, "Content with id " + content.getId()
                    + " does not have a valid image info aspect.");
        }
    }

    /**
     * Get the secret key used to authenticate requests to the image service.
     * @param cmClient The cmClient.
     * @return The secret key.
     */
    protected String getSecret(final CmClient cmClient) {
        try {
            return new ImageServiceConfigurationProvider(
                    cmClient.getPolicyCMServer()).getImageServiceConfiguration().getSecret();
        } catch (CMException e) {
            throw new RuntimeException("Secret not found", e);
        }
    }

    /**
     * Reads the AspectRatio for an image. First it tries to get the AspectRatio from the ImageFormat. If ImageFormat is
     * not found it will try to read the AspectRatio from the ImageInfoAspectBean. In the latter case, rotation will be
     * taken into account. If AspectRatio is found but width or height <= 0, null is returned.
     *
     * @param imageFormat  The ImageFormat.
     * @param imageContent The image content.
     * @return The AspectRatio of the image or null if no AspectRatio is found.
     */
    protected AspectRatio getAspectRatio(final ImageFormat imageFormat,
                                      final Content<ImageContentDataBean> imageContent) {
        AspectRatio aspectRatio = getAspectRatio(imageFormat);
        if (aspectRatio != null) {
            return aspectRatio;
        }
        return getAspectRatio(imageContent);
    }

    /**
     * Reads the AspectRatio from the given ImageFormat. If no AspectRatio is found, null i returned. If AspectRatio is
     * found but width or height <= 0, null is returned.
     *
     * @param imageFormat The ImageFormat.
     * @return the AspectRatio or null if no AspectRatio is found.
     */
    private AspectRatio getAspectRatio(final ImageFormat imageFormat) {
        if (imageFormat != null && imageFormat.getAspectRatio() != null) {
            AspectRatio aspectRatio = imageFormat.getAspectRatio();
            if (aspectRatio.getWidth() > 0 && aspectRatio.getHeight() > 0) {
                return aspectRatio;
            }
        }
        return null;
    }

    /**
     * Reads the AspectRatio from the image's ImageInfoAspectBean. If no ImageInfoAspectBean is found, null is returned.
     * If AspectRatio is found but width or height <= 0, null is returned.
     *
     * @param imageContent The image content.
     * @return The AspectRatio of the image or null if no AspectRatio is found.
     */
    private AspectRatio getAspectRatio(final Content<ImageContentDataBean> imageContent) {
        Aspect<ImageInfoAspectBean> imageInfoAspect = imageContent.getAspect(ImageInfoAspectBean.ASPECT_NAME);
        if (imageInfoAspect != null) {
            ImageInfoAspectBean imageInfo = imageInfoAspect.getData();
            if (imageInfo != null) {
                int width = imageInfo.getWidth();
                int height = imageInfo.getHeight();
                final int rotation = getRotation(imageContent);
                if (rotation == 90 || rotation == 270) {
                    width = imageInfo.getHeight();
                    height = imageInfo.getWidth();
                }
                if (width > 0 && height > 0) {
                    return new AspectRatio(width, height);
                }
            }
        }
        return null;
    }

    /**
     * Fetches the ImageFormat with the given name. If the ImageFormat is not found, null is returned.
     *
     * @param imageFormatName The name of the ImageFormat.
     * @param contentManager  The ContentManager.
     * @return The ImageFormat or null if no ImageFormat is found.
     */
    protected ImageFormat getImageFormat(final String imageFormatName, final ContentManager contentManager) {
        ImageFormat imageFormat = null;
        ContentVersionId formatsId = contentManager.resolve(
                ImageFormatsPolicy.DEFAULT_IMAGE_FORMAT_CONFIG_ID, Subject.NOBODY_CALLER);
        ContentResult<ImageFormats> formatResult = contentManager.get(
                formatsId,
                null,
                ImageFormats.class,
                Collections.<String, Object>emptyMap(),
                Subject.NOBODY_CALLER);
        if (formatResult.getStatus().isOk()) {
            ImageFormats imageFormatsContentData = formatResult.getContent().getContentData();
            imageFormat = imageFormatsContentData.getFormat(imageFormatName);
        }
        return imageFormat;
    }

    /**
     * Reads the rotation from the ImageEditInfoAspect. If no ImageEditInfoAspect is found, 0 i returned.
     *
     * @param imageContent The image content.
     * @return The rotation of the image.
     */
    private int getRotation(final Content<ImageContentDataBean> imageContent) {
        int rotation = 0;
        Aspect<ImageEditInfoAspectBean> imageEditInfoAspect =
                imageContent.getAspect(ImageEditInfoAspectBean.ASPECT_NAME);
        if (imageEditInfoAspect != null) {
            ImageEditInfoAspectBean imageEditInfo = imageEditInfoAspect.getData();
            if (imageEditInfo != null) {
                rotation = imageEditInfo.getRotation();
            }
        }
        return rotation;
    }

}
