package com.atex.gong.paywall;

import com.atex.onecms.content.ContentId;
import com.atex.plugins.users.AccessToken;
import com.atex.plugins.users.AuthenticationManager;
import com.atex.plugins.users.AuthenticationMethodCreator;
import com.atex.plugins.users.User;
import com.atex.plugins.users.UserUtil;
import com.google.inject.Inject;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CmClient;
import com.polopoly.paywall.Offering;
import com.polopoly.paywall.Paywall;
import com.polopoly.paywall.PaywallProvider;
import com.polopoly.paywall.cookie.OnlineAccessCookie;
import com.polopoly.paywall.cookie.OnlineAccessDigestCookie;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnj.TestNJRunner;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import java.util.UUID;

@RunWith(TestNJRunner.class)
@ImportTestContent(
        dir = "/com/atex/gong/paywall/",
        files = {
            "paywall.DefaultPaywallConfiguration.content",
            "paywall.MeteredModelEnabled.content" },
        once = false)
public class OnlineAccessCookieHandlerTestIT {

    private static final ExternalContentId OFFERING_ID =
            new ExternalContentId("paywall.DefaultPaywallConfiguration.offering");
    private static final ExternalContentId BUNDLE_ID =
            new ExternalContentId("paywall.DefaultPaywallConfiguration.bundle");

    @Inject
    private CmClient cmClient;

    private OnlineAccessCookieHandler handler = new OnlineAccessCookieHandler();
    private com.polopoly.cm.ContentId numericBundleId;
    private ContentId userId;
    private AccessToken accessToken;
    private Offering offering;

    @Before
    public void setUp() throws Exception {
        numericBundleId = cmClient.getCMServer().translateSymbolicContentId(BUNDLE_ID);

        String authenticationMethodName = new AuthenticationMethodCreator(cmClient).create().getName();

        String username = UUID.randomUUID().toString();
        AuthenticationManager authenticationManager = new AuthenticationManager(cmClient);
        userId = authenticationManager.createUser(authenticationMethodName, username, new User());
        accessToken = authenticationManager.getToken(userId);

        Paywall paywall = new PaywallProvider(cmClient.getPolicyCMServer()).getPaywall();

        offering = paywall.getOffering(OFFERING_ID);
        UserUtil userUtil = new UserUtil(userId, cmClient.getContentManager());
        SubscriptionUtil.buyOffering(offering, userUtil, cmClient);
    }

    @Test
    public void testCorrectCookiesAreSetOnLogin() throws Exception {
        CookieReceptor<OnlineAccessCookie> accessCookieResult = new CookieReceptor<>(OnlineAccessCookie.class);
        CookieReceptor<OnlineAccessDigestCookie> digestCookieResult =
                new CookieReceptor<>(OnlineAccessDigestCookie.class);
        HttpServletResponse response = mockServletResponseToCatchCookies(accessCookieResult, digestCookieResult);

        handler.onLogin(userId, accessToken, response, cmClient);

        Assert.assertNotNull("OnlineAccessCookie was never set!", accessCookieResult.getCookie());
        Assert.assertNotNull("OnlineAccessDigestCookie was never set!", digestCookieResult.getCookie());
        Assert.assertEquals("Not expected number of content bundles in cookie!",
                offering.getProduct().getContentBundles().size(),
                accessCookieResult.getCookie().getContentBundles().size());
        Assert.assertTrue("Expected bundle was not found in cookie!",
                accessCookieResult.getCookie().getContentBundles().contains(numericBundleId));
        Assert.assertTrue("Got empty digest value!", digestCookieResult.getCookie().getValue().length() > 0);
    }

    @Test
    public void testCorrectCookiesAreClearedOnLogout() throws Exception {
        CookieReceptor<Cookie> accessCookieResult = new CookieReceptor<>(OnlineAccessCookie.DEFAULT_COOKIE_NAME);
        CookieReceptor<Cookie> digestCookieResult = new CookieReceptor<>(OnlineAccessDigestCookie.DEFAULT_COOKIE_NAME);
        HttpServletResponse response = mockServletResponseToCatchCookies(accessCookieResult, digestCookieResult);

        handler.onLogout(response);
        Assert.assertNotNull("OnlineAccessCookie was never set!", accessCookieResult.getCookie());
        Assert.assertNotNull("OnlineAccessDigestCookie was never set!", digestCookieResult.getCookie());
        Assert.assertEquals("OnlineAccessCookie was never cleared!", "", accessCookieResult.getCookie().getValue());
        Assert.assertEquals("OnlineAccessDigestCookie was never cleared!",
                "", digestCookieResult.getCookie().getValue());
    }

    private HttpServletResponse mockServletResponseToCatchCookies(final CookieReceptor<?>... receptors) {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Mockito.doAnswer(new Answer<Object>() {
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                Cookie cookie = (Cookie) invocation.getArguments()[0];

                for (CookieReceptor<?> receptor : receptors) {
                    if (receptor.catchCookie(cookie)) {
                        return null;
                    }
                }

                return null;
            }
        }).when(response).addCookie(Mockito.any(Cookie.class));
        return response;
    }

    private class CookieReceptor<T extends Cookie> {
        private Class<T> clazz;
        private String name;
        private T cookieCatch;

        public CookieReceptor(final Class<T> clazz) {
            this.clazz = clazz;
        }

        public CookieReceptor(final String name) {
            this.name = name;
        }

        @SuppressWarnings("unchecked")
        public boolean catchCookie(final Cookie cookie) {
            if (clazz != null) {
                if (cookie.getClass().equals(clazz)) {
                    cookieCatch = (T) cookie;
                    return true;
                }
                return false;
            }

            if (cookie.getName().equals(name)) {
                cookieCatch = (T) cookie;
                return true;
            }

            return false;
        }

        public T getCookie() {
            return cookieCatch;
        }
    }
}
