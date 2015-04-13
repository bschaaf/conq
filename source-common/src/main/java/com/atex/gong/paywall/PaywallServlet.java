package com.atex.gong.paywall;

import com.atex.onecms.content.ContentId;
import com.atex.onecms.content.repository.ContentModifiedException;
import com.atex.plugins.baseline.policy.BaselinePolicy;
import com.atex.plugins.users.AccessToken;
import com.atex.plugins.users.AuthenticationManager;
import com.atex.plugins.users.UserUtil;
import com.atex.plugins.users.WebClientUtil;
import com.polopoly.application.IllegalApplicationStateException;
import com.polopoly.application.servlet.ApplicationServletUtil;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.servlet.RequestPreparator;
import com.polopoly.cm.servlet.URLBuilder;
import com.polopoly.paywall.Capability;
import com.polopoly.paywall.Offering;
import com.polopoly.paywall.Paywall;
import com.polopoly.paywall.PaywallProvider;
import com.polopoly.siteengine.structure.ParentPathResolver;

import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet used for buying subscriptions to premium content.
 */
@SuppressWarnings("serial")
public class PaywallServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(PaywallServlet.class.getName());
    private CmClient cmClient;
    private AuthenticationManager authenticationManager;
    private PolicyCMServer policyCMServer;

    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        try {
            this.cmClient = ApplicationServletUtil.getApplication(config.getServletContext())
                    .getPreferredApplicationComponent(CmClient.class);
            this.authenticationManager = new AuthenticationManager(cmClient);
            this.policyCMServer = cmClient.getPolicyCMServer();
        } catch (IllegalApplicationStateException exception) {
            throw new ServletException("Failed to get CmClient", exception);
        }
    }

    @Override
    protected void doGet(final HttpServletRequest httpServletRequest,
                         final HttpServletResponse httpServletResponse) throws ServletException, IOException {

        Paywall paywall;
        try {
            paywall = new PaywallProvider(policyCMServer).getPaywall();
        } catch (CMException e) {
            throw new ServletException("Failed to get Paywall", e);
        }

        String action = httpServletRequest.getParameter("action");
        String offeringId = httpServletRequest.getParameter("offeringId");
        AccessToken accessToken = WebClientUtil.getToken(httpServletRequest);
        ContentId userId = authenticationManager.getUserId(accessToken);
        if (userId == null) {
            LOGGER.log(Level.WARNING, String.format("Paywall servlet called with expired access token %s",
                                                    accessToken));
        } else if (offeringId == null || offeringId.isEmpty()) {
            LOGGER.log(Level.WARNING, "Paywall servlet called without offeringId");
        } else if ("success".equals(action)) {
            try {
                Offering offering = paywall.getOffering(ContentIdFactory.createContentId(offeringId));
                if (offering != null) {
                    UserUtil userUtil = new UserUtil(userId, cmClient.getContentManager());
                    SubscriptionUtil.buyOffering(offering, userUtil, cmClient);
                    Collection<com.polopoly.cm.ContentId> contentBundles =
                        SubscriptionUtil.getContentBundlesForUser(
                            userId, cmClient, paywall.getCapability(Capability.ONLINE_ACCESS_CAPABILITY_ID));
                    OnlineAccessCookieHandler.setOnLineAccessCookies(accessToken, httpServletResponse,
                                                                     contentBundles, paywall.getSecretKey());
                }
            } catch (ContentModifiedException | CMException exception) {
                LOGGER.log(Level.WARNING, "Error buying product", exception);
            }
        }
        doRedirect(httpServletRequest, httpServletResponse, cmClient.getPolicyCMServer());
    }

    private void doRedirect(final HttpServletRequest request,
                            final HttpServletResponse response,
                            final PolicyCMServer cmServer) throws IOException {
        String articleIdStr = request.getParameter("articleId");
        String referrer = request.getParameter("referrer");
        String backUrl = "/";
        if (articleIdStr != null) {
            try {
                URLBuilder urlBuilder = RequestPreparator.getURLBuilder(request);
                com.polopoly.cm.ContentId articleId = ContentIdFactory.createContentId(articleIdStr);
                Policy article = cmServer.getPolicy(articleId);
                backUrl = urlBuilder.createUrl(getParentIds(article, cmServer), request);
            } catch (CMException e) {
                LOGGER.log(Level.WARNING, "Unable to create redirect url. Will redirect to /.", e);
            }
        } else if (!StringUtils.isEmpty(referrer)) {
            backUrl = referrer;
        }
        response.sendRedirect(backUrl);
    }

    private com.polopoly.cm.ContentId[] getParentIds(final Policy policy, final PolicyCMServer cmServer)
        throws CMException {
        com.polopoly.cm.ContentId[] result = null;
        if (policy instanceof BaselinePolicy) {
            result = ((BaselinePolicy) policy).getParentIds();
        }
        if (result == null) {
            try {
                result = new ParentPathResolver().getParentPath(policy.getContentId(), cmServer);
            } catch (CMException e) {
                LOGGER.log(Level.WARNING, "Could not get parent path for " + policy.getContentId()
                    + " returning self as only element in parent path.", e);
                result = new com.polopoly.cm.ContentId[] {policy.getContentId().getContentId() };
            }
        }
        return result;
    }

}
