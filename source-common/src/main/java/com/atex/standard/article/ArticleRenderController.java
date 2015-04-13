package com.atex.standard.article;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.model.ModelWrite;
import com.polopoly.paywall.ContentBundle;
import com.polopoly.paywall.Paywall;
import com.polopoly.paywall.PaywallProvider;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

public class ArticleRenderController extends RenderControllerBase {

    private static final Logger LOGGER = Logger.getLogger(ArticleRenderController.class.getName());

    /**
     * Populates model based on preview, needs to be done before cache key.
     */
    @Override
    public void populateModelBeforeCacheKey(final RenderRequest request,
                                            final TopModel topModel,
                                            final ControllerContext context) {
        super.populateModelBeforeCacheKey(request, topModel, context);
        ModelWrite localModel = topModel.getLocal();
        ArticlePolicy article = (ArticlePolicy) ModelPathUtil.getBean(context.getContentModel());
        parseBody(request, topModel, context);

        localModel.setAttribute("premiumBundles", getReadableContentBundles(article));
        localModel.setAttribute("premiumBundleIds", getPremiumContentBundleIds(article));
        PolicyCMServer policyCMServer = getCmClient(context).getPolicyCMServer();
        localModel.setAttribute("meteredPaywallEnabled", isMeteredPaywallEnabled(policyCMServer));
    }

    private static void parseBody(final RenderRequest request,
                                  final TopModel topModel,
                                  final ControllerContext context) {
        Model contentModel = context.getContentModel();
        String body = (String) ModelPathUtil.get(contentModel, "body/value");
        if (body != null) {

            ContentId contentId = (ContentId) ModelPathUtil.get(contentModel, "contentId");
            boolean inPreviewMode = topModel.getRequest().getPreview().isInPreviewMode();

            String parsedBody = new BodyTranslator().translateBody(request,
                                                                   contentId,
                                                                   body,
                                                                   inPreviewMode);

            topModel.getLocal().setAttribute("parsedBody", parsedBody);
        }
    }


    private String getReadableContentBundles(final ArticlePolicy article) {
        String readableContentBundles = "";
        try {
            Set<String> contentBundleNames = new HashSet<String>();
            for (ContentBundle contentBundle : article.getPremiumSettings().getContentBundles()) {
                contentBundleNames.add(contentBundle.getName());
            }
            readableContentBundles = StringUtils.join(contentBundleNames.iterator(), " | ");
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, "Unable to read premium bundles from article", e);
        }
        return readableContentBundles;
    }

    private String getPremiumContentBundleIds(final ArticlePolicy article) {
        List<String> contentIds = new ArrayList<String>();
        try {
            for (ContentBundle bundle : article.getPremiumSettings().getContentBundles()) {
                contentIds.add(bundle.getContentId().getContentId().getContentIdString());
            }
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, "Unable to get premium bundles on article with id: "
                    + article.getContentId().getContentIdString());
        }
        return StringUtils.join(contentIds, ":");
    }

    private boolean isMeteredPaywallEnabled(final PolicyCMServer cmServer) {
        try {
            Paywall paywall = new PaywallProvider(cmServer).getPaywall();
            return (paywall.isEnabled() && paywall.isMeteredEnabled());
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, "Unable to fetch paywall settings", e);
        }
        return false;
    }
}
