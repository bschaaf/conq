package com.atex.plugins.persona;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

public class SettingsController extends RenderControllerBase {

    private static final Logger LOGGER = Logger.getLogger(SettingsController.class.getName());
    public static final String CONFIG_EXT_ID = "plugins.com.atex.plugins.mozilla-persona.Config";

    @Override
    public void populateModelAfterCacheKey(final RenderRequest request,
                                           final TopModel topModel,
                                           final CacheInfo cacheInfo,
                                           final ControllerContext context) {
        PolicyCMServer policyCMServer = getCmClient(context).getPolicyCMServer();
        try {
            ConfigPolicy policy =
                    (ConfigPolicy) policyCMServer.getPolicy(new ExternalContentId(CONFIG_EXT_ID));

            String privacyPolicyExtId = policy.getPrivacyPolicy();
            if (privacyPolicyExtId != null && !privacyPolicyExtId.isEmpty()) {
                ContentId privacyPolicyId = policyCMServer.getPolicy(new ExternalContentId(privacyPolicyExtId))
                    .getContentId().getContentId();
                topModel.getLocal().setAttribute("privacyPolicy", privacyPolicyId);
            }

            String tosExtId = policy.getTermsOfService();
            if (tosExtId != null && !tosExtId.isEmpty()) {
                ContentId tosId = policyCMServer.getPolicy(new ExternalContentId(tosExtId))
                    .getContentId().getContentId();
                topModel.getLocal().setAttribute("termsOfService", tosId);
            }

            topModel.getLocal().setAttribute("siteName", policy.getSiteName());
            topModel.getLocal().setAttribute("siteLogo", policy.getSiteLogo());
            topModel.getLocal().setAttribute("backgroundColor", policy.getBackgroundColor());
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, "Failed to read Persona configuration.", e);
        }
    }

    @Override
    public Object getCacheKey(final RenderRequest request,
                              final TopModel topModel,
                              final Object defaultKey,
                              final ControllerContext context) {
        PolicyCMServer policyCMServer = getCmClient(context).getPolicyCMServer();
        try {
            ConfigPolicy policy =
                    (ConfigPolicy) policyCMServer.getPolicy(new ExternalContentId(CONFIG_EXT_ID));
            return policy.getContentId();
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, "Failed to read Persona configuration.", e);
            return defaultKey;
        }
    }
}

