package com.atex.plugins.social.sharing;

import com.atex.onecms.content.ContentId;
import com.atex.onecms.content.IdUtil;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CMRuntimeException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.model.request.ContentPath;
import com.polopoly.siteengine.mvc.RenderControllerBase;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A controller that prepares for writing OpenGraph meta tags based on the currently viewed content (or
 * department/site). The controller fetches information from the currently rendered content using the variant defined in
 * {@link SocialSharingInfo#VARIANT_NAME}.
 */
public class OpenGraphController extends RenderControllerBase {

    private static final Logger LOGGER = Logger.getLogger(OpenGraphController.class.getName());

    @Override
    public void populateModelAfterCacheKey(final RenderRequest request,
                                           final TopModel topModel,
                                           final CacheInfo cacheInfo,
                                           final ControllerContext context) {
        CmClient cmClient = getCmClient(context);
        if (cmClient == null) {
            throw new CMRuntimeException("Could not fetch cmClient");
        }
        ContentPath contentPath = topModel.getContext().getPage().getPathAfterPage();
        if (contentPath.size() == 0) {
            LOGGER.log(Level.FINE, "Path after page was empty, so using whole path: "
                    + topModel.getContext().getPage().getContentPath());
            contentPath = topModel.getContext().getPage().getContentPath();
        }
        com.atex.onecms.content.ContentId contentId = IdUtil.fromPolicyContentId(contentPath.getLast());
        if (contentId != null) {
            topModel.getLocal().setAttribute("canonicalContentId", contentPath.getLast());
            try {
                SocialSharingInfo info = new SocialSharingInfoFetcher(cmClient).fetch(contentId);
                if (info == null) {
                    info = new SocialSharingInfo();
                    info.setTitle(topModel.getContext().getPage().getBean().getName());
                    info.setDescription(topModel.getContext().getSite().getBean().getName());
                    info.setOpenGraphType("website");
                }
                setModelAttributes(topModel, cmClient, contentId, info);
            } catch (ClassCastException | CMException e) {
                throw new CMRuntimeException("Failed to fetch cm client: " + e.getMessage(), e);
            }
        }
    }

    private void setModelAttributes(final TopModel m,
                                    final CmClient cmClient,
                                    final ContentId contentId,
                                    final SocialSharingInfo info) throws CMException {
        m.getLocal().setAttribute("title", info.getTitle());
        m.getLocal().setAttribute("description", info.getDescription());
        m.getLocal().setAttribute("type", info.getOpenGraphType());
        m.getLocal().setAttribute("imageId", info.getImageContentId());
    }
}
