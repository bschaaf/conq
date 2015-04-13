package com.atex.plugins.banner;

import com.atex.onecms.content.Content;
import com.atex.onecms.content.ContentId;
import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.Subject;
import com.polopoly.cm.ContentFileInfo;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.Policy;
import com.polopoly.common.util.FriendlyUrlConverter;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;
import com.polopoly.util.StringUtil;

import java.io.IOException;
import java.util.logging.Logger;

public class BannerController extends RenderControllerBase {

    private static final Logger LOGGER = Logger.getLogger(BannerController.class.getName());

    @Override
    public void populateModelBeforeCacheKey(final RenderRequest request, final TopModel m, final ControllerContext context) {
        super.populateModelBeforeCacheKey(request, m, context);

        CmClient cmClient = getCmClient(context);
        ContentManager contentManager = cmClient.getContentManager();
        ContentId contentId = IdUtil.fromPolicyContentId(context.getContentId());
        ContentVersionId versionId = contentManager.resolve(contentId, Subject.NOBODY_CALLER);
        ContentResult<BannerContentDataBean> result =
                contentManager.get(versionId, null, BannerContentDataBean.class, null, Subject.NOBODY_CALLER);
        Content<BannerContentDataBean> content = result.getContent();

        BannerContentDataBean contentData = content.getContentData();

        try {
            Policy policy = getCmClient(context).getPolicyCMServer().getPolicy(context.getContentId());
            ContentFileInfo fileInfo = policy.getContent().getFileInfo(contentData.getFilePath());
            m.getLocal().setAttribute("mimeType", fileInfo.getMimeType());
        } catch (CMException|IOException e) {
            throw new RuntimeException("Cannot get mime type", e);
        }
    }
}
