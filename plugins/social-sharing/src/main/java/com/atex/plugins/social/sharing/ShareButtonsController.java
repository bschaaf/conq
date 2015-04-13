package com.atex.plugins.social.sharing;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.onecms.content.IdUtil;
import com.atex.plugins.social.sharing.google.GooglePlusRenderBean;
import com.atex.plugins.social.sharing.google.PlusOneAnnotation;
import com.atex.plugins.social.sharing.google.ShareAnnotation;
import com.atex.plugins.social.sharing.twitter.TwitterRenderBean;
import com.google.common.base.Strings;
import com.polopoly.cm.client.CMException;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

/**
 * Controller for the google plus output template.
 */
public class ShareButtonsController extends BaseSharingController {

    private static final Logger LOGGER = Logger.getLogger(ShareButtonsController.class.getName());

    @Override
    public void populateModelBeforeCacheKey(final RenderRequest request, final TopModel m,
                                            final ControllerContext context) {

        super.populateModelBeforeCacheKey(request, m, context);

        try {
            final SocialSharingConfigPolicy policy = getSocialSharingConfiguration(context);
            SocialSharingInfo socialSharingInfo = getSocialSharingInfoFetcher(context)
                    .fetch(IdUtil.fromPolicyContentId(context.getContentId()));

            m.getLocal().setAttribute("google-plus", getGooglePlusBean(policy));
            m.getLocal().setAttribute("twitter", getTwitterBean(policy, socialSharingInfo));


        } catch (CMException e) {
            LOGGER.log(Level.WARNING, "Failed to read Social Sharing configuration.", e);
        }
    }

    /**
     * Create the velocity model for google plus.
     *
     * @param policy
     * @return a not null value.
     */
    private GooglePlusRenderBean getGooglePlusBean(final SocialSharingConfigPolicy policy) {

        final String annotationId = policy.getGoogleAnnotation();
        String height = policy.getGoogleHeight();
        final String width = policy.getGoogleWidth();
        final String attributes = policy.getGoogleAttributes();
        final String typeId = policy.getGoogleType();

        final String widgetAnnotation;

        final boolean useShare = (typeId == null || typeId.equals(SocialSharingConfigPolicy.GPLUS_TYPE_SHARE));
        if (useShare) {
            final ShareAnnotation shareAnnotation = ShareAnnotation.fromId(annotationId);
            widgetAnnotation = shareAnnotation.getData();
            if (shareAnnotation == ShareAnnotation.BUBBLE_VERTICAL && !height.isEmpty()) {
                height = SocialSharingConfigPolicy.GPLUS_VERTICAL_BUBBLE_HEIGHT;
            }
        } else {
            final PlusOneAnnotation plusOneAnnotation = PlusOneAnnotation.fromId(annotationId);
            widgetAnnotation = plusOneAnnotation.getData();
        }

        return new GooglePlusRenderBean(widgetAnnotation, height, width,
                attributes, useShare);

    }

    /**
     * Create the velocity model for twitter.
     *
     * @param policy
     * @return a not null value.
     */
    private TwitterRenderBean getTwitterBean(final SocialSharingConfigPolicy policy,
                                             final SocialSharingInfo socialSharingInfo) {

        final String count = policy.getTwitterCount();
        final String language = policy.getTwitterLanguage();
        final String size = policy.getTwitterSize();
        final String related = policy.getTwitterRelated();
        final String attributes = policy.getTwitterAttributes();
        String text = policy.getTwitterText();

        if (Strings.isNullOrEmpty(text) && socialSharingInfo != null) {
            text = socialSharingInfo.getTitle();
        }

        return new TwitterRenderBean(
                text,
                count,
                language,
                size,
                related,
                attributes
        );

    }

    protected SocialSharingInfoFetcher getSocialSharingInfoFetcher(final ControllerContext context) {
        return new SocialSharingInfoFetcher(getCmClient(context));
    }
}
