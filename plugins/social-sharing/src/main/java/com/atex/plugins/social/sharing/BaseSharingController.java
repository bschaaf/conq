package com.atex.plugins.social.sharing;

import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.mvc.RenderControllerBase;

/**
 * Base controller for sharing output templates.
 */
public abstract class BaseSharingController extends RenderControllerBase {

    public static final String CONFIG_EXT_ID = "plugins.com.atex.plugins.social-sharing.Config";

    /**
     * Return the configuration policy.
     *
     * @param context the controller context.
     * @return a not null policy.
     * @throws CMException
     */
    public SocialSharingConfigPolicy getSocialSharingConfiguration(final ControllerContext context) throws
                                                                                                       CMException {

        PolicyCMServer policyCMServer = getCmClient(context).getPolicyCMServer();
        return (SocialSharingConfigPolicy) policyCMServer.getPolicy(new ExternalContentId(CONFIG_EXT_ID));

    }
}
