package com.atex.plugins.rssfeed;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.onecms.content.Content;
import com.atex.onecms.content.ContentId;
import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.Subject;
import com.atex.onecms.image.ImageInfoAspectBean;
import com.atex.onecms.ws.image.ImageServiceConfigurationProvider;
import com.atex.onecms.ws.image.ImageServiceUrlBuilder;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

public abstract class RssImageController extends RenderControllerBase {

    private static final Logger LOG = Logger.getLogger(RssImageController.class.getName());
    private static final String imageFormatName = "default";
    protected void populateModelWithImage(final com.polopoly.cm.ContentId imageId, final TopModel topModel,
            final ControllerContext context,
            final CmClient cmClient) {

        ImageServiceUrlBuilder urlBuilder = getImageUrlBuilder(imageId, topModel, cmClient);
        if(urlBuilder != null) {
            topModel.getLocal().setAttribute("hasImage", true);
            topModel.getLocal().setAttribute("urlBuilder", urlBuilder);
        }
    }

    protected ImageServiceUrlBuilder getImageUrlBuilder(final com.polopoly.cm.ContentId imageId, final TopModel topModel,
                                                       final CmClient cmClient) {
        if (imageId == null) {
            return null;
        }
        ContentManager contentManager = cmClient.getContentManager();
        ContentId contentId = IdUtil.fromPolicyContentId(imageId);
        ContentVersionId versionId = contentManager.resolve(contentId, Subject.NOBODY_CALLER);
        ContentResult<?> result =
                contentManager.get(versionId, null, null, null, Subject.NOBODY_CALLER);
        Content<?> content = result.getContent();
        ImageInfoAspectBean imageInfo = (ImageInfoAspectBean) content.getAspectData(ImageInfoAspectBean.ASPECT_NAME);

        if (imageInfo != null && imageInfo.getFilePath() != null) {
            String secret = getSecret(cmClient);
            ImageServiceUrlBuilder urlBuilder = new ImageServiceUrlBuilder(content, secret).format(imageFormatName);
            return urlBuilder;
        } else {
            LOG.log(Level.WARNING, "Content with id " + content.getId()
                    + " does not have a valid image info aspect.");
        }
        return null;
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
}
