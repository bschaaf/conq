package com.atex.gong.paywall;

import com.atex.plugins.users.AccessToken;
import com.atex.plugins.users.WebClientUtil;
import com.atex.standard.article.ArticlePolicy;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.servlet.RequestPreparator;
import com.polopoly.cm.servlet.URLBuilder;
import com.polopoly.cm.servlet.dispatcher.DispatcherPreparator;
import com.polopoly.paywall.ContentBundle;
import com.polopoly.paywall.Paywall;
import com.polopoly.paywall.PaywallProvider;
import com.polopoly.paywall.PremiumContentAware;
import com.polopoly.paywall.PremiumContentSettings;
import com.polopoly.paywall.cookie.MeteredCookie;
import com.polopoly.paywall.cookie.OnlineAccessCookie;
import com.polopoly.paywall.cookie.OnlineAccessDigestCookie;
import org.apache.commons.lang.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Filter for controlling the Paywall. Provides or denies access to premium content.
 */
public class PaywallFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(PaywallFilter.class.getName());
    private static final ExternalContentId PAYWALL_CONTENT_ID = new ExternalContentId("com.atex.gong.paywall.paywall");
    private static final String PAYWALL_ELEMENT = "com.atex.gong.paywall.MainElement";

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(final ServletRequest request,
                         final ServletResponse response,
                         final FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = ((HttpServletRequest) request);
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        PolicyCMServer cmServer = DispatcherPreparator.getCMServer(httpRequest);
        try {
            Paywall paywall = new PaywallProvider(cmServer).getPaywall();
            if (paywall.isEnabled()) {
                Policy policy = getArticlePolicy(httpRequest, cmServer);
                if (policy != null && PAYWALL_ELEMENT.equals(policy.getInputTemplate().getName())) {
                    String requestedArticleIdStr = request.getParameter("aId");
                    if (requestedArticleIdStr != null && !"".equals(requestedArticleIdStr)) {
                        ContentId requestedArticleId = ContentIdFactory.createContentId(requestedArticleIdStr);
                        ArticlePolicy article = (ArticlePolicy) cmServer.getPolicy(requestedArticleId);
                        String articleUrl = buildArticleUrl(httpRequest, article);
                        if (paywall.isLocalAuthorizationEnabled()) {
                            if (hasOnlineAccess(httpRequest, article, paywall)) {
                                httpResponse.sendRedirect(articleUrl);
                                return;
                            }
                        } else {
                            httpResponse.setHeader("X-Premium-Redirect-Article-URL", articleUrl);
                            httpResponse.setHeader("X-Premium-Packages", getBundleIdsString(article));
                        }
                    }
                } else if (policy instanceof ArticlePolicy) {
                    ArticlePolicy article = (ArticlePolicy) policy;
                    PremiumContentSettings premiumSettings = article.getPremiumSettings();
                    if (premiumSettings.isPremiumContent()) {
                        String paywallUrl = buildPaywallUrl(httpRequest, article);
                        if (paywall.isLocalAuthorizationEnabled()) {
                            boolean hasOnlineAccess = hasOnlineAccess(httpRequest, article, paywall);
                            boolean hasMeteredAccess = false;
                            if (!hasOnlineAccess && paywall.isMeteredEnabled()) {
                                hasMeteredAccess = determineMeteredAccess(httpRequest, httpResponse, article, paywall);
                            }
                            boolean isWhitelistedUserAgent = paywall.hasWhitelistedUserAgent(httpRequest);
                            boolean isWhitelistedReferrer = paywall.hasWhitelistedReferrer(httpRequest);
                            if (!(hasOnlineAccess || hasMeteredAccess
                                    || isWhitelistedUserAgent || isWhitelistedReferrer)) {
                                httpResponse.sendRedirect(paywallUrl);
                                return;
                            }
                        } else {
                            httpResponse.setHeader("X-Premium-Packages", getBundleIdsString(article));
                            httpResponse.setHeader("X-Premium-Redirect-URL", paywallUrl);
                            if (paywall.isMeteredEnabled()) {
                                httpResponse.setHeader("X-Metered-Limit",
                                        String.valueOf(paywall.getMeteredNumberOfFreeClicks()));
                                httpResponse.setHeader("X-Metered-Period",
                                        String.valueOf(paywall.getMeteredPeriodInDays()));
                            }
                        }
                    }
                }
            }
        } catch (CMException e) {
            LOGGER.log(Level.FINE, "Error processing potential premium article", e);
        }
        chain.doFilter(request, response);
    }

    /**
     * Determine and update metered access to a content.
     * <p/>
     * Will examine the metered cookie to determine if there are clicks available, or if the article already has been
     * viewed (i.e. a click has been spent on it), and updates the article list and expiry time for the metered cookie.
     * See {@link com.polopoly.paywall.cookie.MeteredCookie#DEFAULT_COOKIE_NAME}.
     *
     * @param httpRequest  to get hold of the cookie
     * @param httpResponse to add the updated cookie to
     * @param article      article to remember, i.e. viewing same article multiple times will still only cost one
     *                     click.
     * @return <code>true</code> if metered access possible due to available clicks, <code>false</code> otherwise.
     */
    private boolean determineMeteredAccess(final HttpServletRequest httpRequest,
                                           final HttpServletResponse httpResponse,
                                           final ArticlePolicy article,
                                           final Paywall paywall) {
        Cookie plainCookie = getCookieByName(httpRequest, MeteredCookie.DEFAULT_COOKIE_NAME);
        String contentIdString = article.getContentId().getContentId().getContentIdString();
        MeteredCookie meteredCookie;
        if (plainCookie != null) {
            meteredCookie = new MeteredCookie(plainCookie,
                    paywall.getMeteredPeriodInDays(),
                    paywall.getMeteredNumberOfFreeClicks());
        } else {
            meteredCookie = new MeteredCookie(MeteredCookie.DEFAULT_COOKIE_NAME,
                    paywall.getMeteredPeriodInDays(),
                    paywall.getMeteredNumberOfFreeClicks());
        }
        boolean hasAccess = meteredCookie.hasAccess(contentIdString);
        if (hasAccess) {
            meteredCookie.grantClickAccess(contentIdString);
            httpResponse.addCookie(meteredCookie);
        }
        return hasAccess;
    }

    /**
     * Examines the cookie online access cookies to determine access.
     *
     * @param httpServletRequest where the cookies live
     * @param article     the premium content
     * @param paywall     the paywall
     * @return <code>true</code> if online access, <code>false</code> otherwise
     * @throws com.polopoly.cm.client.CMException if user could not be retrieved
     */
    private boolean hasOnlineAccess(final HttpServletRequest httpServletRequest,
                                    final PremiumContentAware article,
                                    final Paywall paywall) throws CMException {
        AccessToken accessToken = WebClientUtil.getToken(httpServletRequest);
        OnlineAccessCookie onlineAccessCookieFromRequest = getOnlineAccessCookie(httpServletRequest);
        OnlineAccessDigestCookie digestCookieFromRequest = getOnlineAccessDigestCookie(httpServletRequest);
        if (accessToken == null
                || onlineAccessCookieFromRequest == null
                || digestCookieFromRequest == null) {
            return false;
        }
        OnlineAccessDigestCookie expectedDigestCookie = new OnlineAccessDigestCookie(
                onlineAccessCookieFromRequest, accessToken.getToken(), paywall.getSecretKey());
        if (!expectedDigestCookie.getValue().equals(digestCookieFromRequest.getValue())) {
            return false;
        }
        Collection<ContentId> contentBundles = onlineAccessCookieFromRequest.getContentBundles();
        return isPremiumContentPartOfBundle(contentBundles, article, paywall);
    }

    private boolean isPremiumContentPartOfBundle(final Collection<ContentId> contentBundleIds,
                                                 final PremiumContentAware article,
                                                 final Paywall paywall) throws CMException {
        Collection<ContentBundle> contentBundles = new HashSet<ContentBundle>();
        for (ContentId bundleId : contentBundleIds) {
            contentBundles.add(paywall.getContentBundle(bundleId));
        }
        Collection<ContentBundle> premiumBundles = article.getPremiumSettings().getContentBundles();
        contentBundles.retainAll(premiumBundles);
        return !contentBundles.isEmpty();
    }

    private OnlineAccessDigestCookie getOnlineAccessDigestCookie(final HttpServletRequest httpRequest) {
        Cookie cookie = getCookieByName(httpRequest, OnlineAccessDigestCookie.DEFAULT_COOKIE_NAME);
        return cookie != null ? new OnlineAccessDigestCookie(cookie) : null;
    }

    private OnlineAccessCookie getOnlineAccessCookie(final HttpServletRequest httpRequest) {
        Cookie cookie = getCookieByName(httpRequest, OnlineAccessCookie.DEFAULT_COOKIE_NAME);
        return cookie != null ? new OnlineAccessCookie(cookie) : null;
    }

    private Cookie getCookieByName(final HttpServletRequest httpRequest, final String name) {
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    private String getBundleIdsString(final ArticlePolicy article) throws CMException {
        HashSet<String> bundleIds = new HashSet<String>();
        Collection<ContentBundle> premiumBundles = article.getPremiumSettings().getContentBundles();
        for (ContentBundle bundle : premiumBundles) {
            bundleIds.add(bundle.getContentId().getContentId().getContentIdString());
        }
        return StringUtils.join(bundleIds.iterator(), ":");
    }

    @SuppressWarnings("serial")
    private String buildPaywallUrl(final HttpServletRequest httpRequest,
                                   final ArticlePolicy article) throws CMException {
        final String articleId = article.getContentId().getContentId().getContentIdString();
        URLBuilder urlBuilder = RequestPreparator.getURLBuilder(httpRequest);
        ContentId[] articleParents = article.getParentIds();
        ContentId[] copyParents = Arrays.copyOf(articleParents, articleParents.length);
        copyParents[articleParents.length - 1] = PAYWALL_CONTENT_ID;
        return urlBuilder.createUrl(copyParents,
                new HashMap<String, String>() { { put("aId", articleId); } },
                httpRequest);
    }

    private String buildArticleUrl(final HttpServletRequest httpRequest,
                                   final ArticlePolicy article) throws CMException {
        URLBuilder urlBuilder = RequestPreparator.getURLBuilder(httpRequest);
        return urlBuilder.createUrl(article.getParentIds(), httpRequest);
    }

    private Policy getArticlePolicy(final HttpServletRequest httpRequest,
                                    final PolicyCMServer cmServer) throws CMException {
        Policy articlePolicy = null;
        String articleIdString = extractArticleIdString(httpRequest.getRequestURI());
        try {
            ContentId articleId = ContentIdFactory.createContentId(articleIdString);
            articlePolicy = cmServer.getPolicy(articleId);
        } catch (IllegalArgumentException e) {
            // Maybe it's an external id?
            articlePolicy = cmServer.getPolicy(new ExternalContentId(articleIdString));
        }
        return articlePolicy;
    }

    public static String extractArticleIdString(final String requestURI) {
        String articleIdString;
        int lastIndexOfSlash = requestURI.lastIndexOf("/");
        int lastIndexOfDash = requestURI.lastIndexOf("-");
        int index = Math.max(lastIndexOfDash, lastIndexOfSlash);
        if (index > -1) {
            articleIdString = requestURI.substring(index + 1);
        } else {
            articleIdString = requestURI;
        }
        return articleIdString;
    }

    @Override
    public void destroy() {
    }
}
