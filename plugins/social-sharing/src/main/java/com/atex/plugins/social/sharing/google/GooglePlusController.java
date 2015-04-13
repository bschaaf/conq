package com.atex.plugins.social.sharing.google;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.plugins.social.sharing.BaseSharingController;
import com.atex.plugins.social.sharing.SocialSharingConfigPolicy;
import com.google.common.base.Strings;
import com.polopoly.cm.client.CMException;
import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

/**
 * Controller for the google plus output template.
 */
public class GooglePlusController extends BaseSharingController {

    private static final Logger LOGGER = Logger.getLogger(GooglePlusController.class.getName());

    @Override
    public void populateModelBeforeCacheKey(final RenderRequest request, final TopModel m,
                                            final ControllerContext context) {

        super.populateModelBeforeCacheKey(request, m, context);

        try {
            final SocialSharingConfigPolicy policy = getSocialSharingConfiguration(context);

            final String lang = Strings.emptyToNull(policy.getGoogleLanguage());
            final String url = Strings.emptyToNull(policy.getGooglePlusUrl());

            final Model local = m.getLocal();
            ModelPathUtil.set(local, "google-plus-lang", lang);
            ModelPathUtil.set(local, "google-plus-url", url);
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, "Failed to read Social Sharing configuration.", e);
        }
    }

}
