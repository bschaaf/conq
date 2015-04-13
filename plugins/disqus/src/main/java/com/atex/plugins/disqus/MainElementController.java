package com.atex.plugins.disqus;

import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

import java.util.logging.Level;
import java.util.logging.Logger;


public class MainElementController extends RenderControllerBase {

    private static final Logger LOGGER = Logger.getLogger(MainElementController.class.getName());
    public static final String CONFIG_EXT_ID = "plugins.com.atex.plugins.disqus.Config";

    @Override
    public void populateModelAfterCacheKey(final RenderRequest request,
                                           final TopModel topModel,
                                           final CacheInfo cacheInfo,
                                           final ControllerContext context) {
        try {
            final ConfigPolicy config = getConfigPolicy(context);
            if (config.isEnabled()) {
                final String siteShortname = config.getSiteShortname();
                if (siteShortname != null && !siteShortname.isEmpty()) {
                    topModel.getLocal().setAttribute("disqusShortname", siteShortname);
                } else {
                    LOGGER.log(Level.WARNING, "No Disqus Shortname configured.");
                }
            }
        } catch (CMException exception) {
            LOGGER.log(Level.WARNING, "Failed to read Disqus configuration.", exception);
        }
    }

    private ConfigPolicy getConfigPolicy(final ControllerContext context) throws CMException {
        PolicyCMServer cmServer = getCmClient(context).getPolicyCMServer();
        return (ConfigPolicy) cmServer.getPolicy(new ExternalContentId(CONFIG_EXT_ID));
    }
}
