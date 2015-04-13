package com.atex.gong.paywall;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.onecms.content.ContentId;
import com.atex.plugins.users.AccessToken;
import com.atex.plugins.users.WebLoginEventListener;
import com.atex.plugins.users.WebLogoutEventListener;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.paywall.Capability;
import com.polopoly.paywall.Paywall;
import com.polopoly.paywall.PaywallProvider;
import com.polopoly.paywall.cookie.OnlineAccessCookie;
import com.polopoly.paywall.cookie.OnlineAccessDigestCookie;


public class OnlineAccessCookieHandler implements WebLoginEventListener, WebLogoutEventListener {

    private static final Logger LOGGER = Logger.getLogger(OnlineAccessCookieHandler.class.getName());

    @Override
    public void onLogin(final ContentId userId,
                        final AccessToken accessToken,
                        final HttpServletResponse httpServletResponse,
                        final CmClient cmClient) {
        try {
            PolicyCMServer cmServer = cmClient.getPolicyCMServer();
            Paywall paywall = new PaywallProvider(cmServer).getPaywall();
            if (paywall.isEnabled()) {
                Collection<com.polopoly.cm.ContentId> contentBundles =
                        SubscriptionUtil.getContentBundlesForUser(userId, cmClient,
                                paywall.getCapability(Capability.ONLINE_ACCESS_CAPABILITY_ID));
                setOnLineAccessCookies(accessToken, httpServletResponse, contentBundles, paywall.getSecretKey());
            }
        } catch (CMException exception) {
            LOGGER.log(Level.WARNING, "Failed to perform on login action.", exception);
        }
    }

    @Override
    public void onLogout(final HttpServletResponse response) {
        clearOnlineAccessCookies(response);
    }

    public static void setOnLineAccessCookies(final AccessToken accessToken,
                                              final HttpServletResponse httpServletResponse,
                                              final Collection<com.polopoly.cm.ContentId> contentBundles,
                                              final String secretKey) {
        if (!contentBundles.isEmpty()) {
            OnlineAccessCookie onlineAccessCookie =
                    new OnlineAccessCookie(OnlineAccessCookie.DEFAULT_COOKIE_NAME, contentBundles);
            OnlineAccessDigestCookie onlineAccessDigestCookie =
                    new OnlineAccessDigestCookie(
                            onlineAccessCookie, accessToken.getToken(), secretKey);
            httpServletResponse.addCookie(onlineAccessCookie);
            httpServletResponse.addCookie(onlineAccessDigestCookie);
        } else {
            clearOnlineAccessCookies(httpServletResponse);
        }
    }



    private static void clearOnlineAccessCookies(final HttpServletResponse httpServletResponse) {
        clearCookie(httpServletResponse, OnlineAccessCookie.DEFAULT_COOKIE_NAME);
        clearCookie(httpServletResponse, OnlineAccessDigestCookie.DEFAULT_COOKIE_NAME);
    }

    private static void clearCookie(final HttpServletResponse httpServletResponse,
                                    final String cookieName) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        httpServletResponse.addCookie(cookie);
    }
}
