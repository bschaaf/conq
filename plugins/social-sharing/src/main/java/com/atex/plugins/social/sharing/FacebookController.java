package com.atex.plugins.social.sharing;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.client.CMException;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

/**
 * Controller for the facebook sharing.
 */
public class FacebookController extends BaseSharingController {

    private static final Logger LOGGER = Logger.getLogger(FacebookController.class.getName());

    @Override
    public void populateModelAfterCacheKey(final RenderRequest request,
                                           final TopModel topModel,
                                           final CacheInfo cacheInfo,
                                           final ControllerContext context) {
        try {
            final SocialSharingConfigPolicy policy = getSocialSharingConfiguration(context);
            final String facebookAppId = policy.getFacebookAppId();
            if (facebookAppId != null && !facebookAppId.isEmpty()) {
                topModel.getLocal().setAttribute("facebook-appid", facebookAppId);
            } else {
                LOGGER.log(Level.FINE, "No Facebook AppId configured.");
            }
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, "Failed to read Social Sharing configuration.", e);
        }
    }

}

