package com.atex.gong.paywall;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.paywall.ContentBundle;
import com.polopoly.paywall.Offering;
import com.polopoly.paywall.Paywall;
import com.polopoly.paywall.PaywallProvider;
import com.polopoly.paywall.Capability;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;
import com.atex.standard.article.ArticlePolicy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RenderController for the overlay displayed by the paywall when "hitting the wall", either by reaching max amount of
 * clicks of a metered paywall or by reaching a premium article not covered by a subscription of the viewer.
 */
public class PaywallRenderController extends RenderControllerBase {
    private static final Logger LOGGER = Logger.getLogger(PaywallRenderController.class.getName());

    @Override
    public void populateModelBeforeCacheKey(final RenderRequest request,
                                            final TopModel topModel,
                                            final ControllerContext context) {
        String requestedArticleIdStr = request.getParameter("aId");
        ContentId requestedArticleId = null;
        if (requestedArticleIdStr != null && !"".equals(requestedArticleIdStr)) {
            requestedArticleId = ContentIdFactory.createContentId(requestedArticleIdStr);
        }
        Collection<ContentBundle> validBundles = new ArrayList<ContentBundle>();
        Collection<Offering> validOfferings = new ArrayList<Offering>();
        String requestedArticleTitle = null;
        boolean paywallIsMetered = false;
        int paywallMeteredPeriod = 0;
        CmClient cmClient = getCmClient(context);
        PolicyCMServer cmServer = cmClient.getPolicyCMServer();
        try {
            Paywall paywall = new PaywallProvider(cmServer).getPaywall();
            paywallIsMetered = paywall.isMeteredEnabled();
            paywallMeteredPeriod = paywall.getMeteredPeriodInDays();
            if (requestedArticleId == null) {
                validBundles = paywall.getContentBundles();
                validOfferings = paywall.getOfferings();
            } else {
                ArticlePolicy articlePolicy = (ArticlePolicy) cmServer.getPolicy(requestedArticleId);
                requestedArticleTitle = articlePolicy.getName();
                validBundles = articlePolicy.getPremiumSettings().getContentBundles();
                validOfferings = getValidOfferings(paywall.getOfferings(), validBundles,
                        paywall.getCapability(Capability.ONLINE_ACCESS_CAPABILITY_ID));
            }
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, "Unable to fetch paywall data", e);
        }
        topModel.getLocal().setAttribute("requestedArticleTitle", requestedArticleTitle);
        topModel.getLocal().setAttribute("requestedArticleId", requestedArticleId);
        topModel.getLocal().setAttribute("validBundles", validBundles);
        topModel.getLocal().setAttribute("validOfferings", validOfferings);
        topModel.getLocal().setAttribute("paywallismetered", paywallIsMetered);
        topModel.getLocal().setAttribute("meteredPeriod", paywallMeteredPeriod);
        topModel.getLocal().setAttribute("articleId", requestedArticleIdStr);
    }

    private Collection<Offering> getValidOfferings(final Collection<Offering> offerings,
                                                   final Collection<ContentBundle> contentBundles,
                                                   final Capability capability) throws CMException {
        Collection<Offering> offeringsWithBundles = new ArrayList<Offering>();
        for (Offering offering : offerings) {
            Collection<Capability> capabilities = offering.getSubscriptionLevel().getCapabilities();
            if (capabilities.contains(capability)) {
                Collection<ContentBundle> contentBundlesInOffering = offering.getProduct().getContentBundles();
                for (ContentBundle contentBundleInOffering : contentBundlesInOffering) {
                    if (contentBundles.contains(contentBundleInOffering) && !offeringsWithBundles.contains(offering)) {
                        offeringsWithBundles.add(offering);
                    }
                }
            }
        }
        return offeringsWithBundles;
    }
}
