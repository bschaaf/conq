package com.atex.plugins.users;

import com.atex.onecms.content.ContentId;
import com.polopoly.cm.client.CmClient;
import org.reflections.Reflections;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility methods for on login and on logout actions.
 */
public final class WebClientUtil {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final int MS_TO_SEC = 1000;
    private static final Logger LOGGER = Logger.getLogger(WebClientUtil.class.getName());

    private WebClientUtil() {
    }

    /**
     * Sets access token cookie with name {@value #ACCESS_TOKEN_COOKIE_NAME}. Dispatches on login events by calling
     * {@link com.atex.plugins.users.WebLoginEventListener#onLogin} in classes
     * implementing {@link WebLoginEventListener}.
     *
     * @param userId the content id of the currently logged in user.
     * @param response The HttpServletResponse.
     * @param accessToken    The AccessToken to be represented by a cookie.
     * @param cmClient a CmClient
     */
    public static void onLogin(final ContentId userId,
                               final AccessToken accessToken,
                               final HttpServletResponse response,
                               final CmClient cmClient) {
        dispatchOnLoginEvent(userId, accessToken, response, cmClient);
        setToken(response, accessToken);
    }

    /**
     * Set the accessToken cookie in the response with the given token.
     */
    private static void setToken(final HttpServletResponse response, final AccessToken token) {
        Long expiration = Long.valueOf(token.getToken().split("::")[1]);
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, token.getToken());
        cookie.setPath("/");
        cookie.setMaxAge((int) (expiration - (System.currentTimeMillis() / MS_TO_SEC)));
        response.addCookie(cookie);
    }

    private static void dispatchOnLoginEvent(final ContentId userId,
                                             final AccessToken accessToken,
                                             final HttpServletResponse response,
                                             final CmClient cmClient) {
        for (WebLoginEventListener listener : getAllListeners(WebLoginEventListener.class)) {
            listener.onLogin(userId, accessToken, response, cmClient);
        }
    }

    /**
     * Remove the accessToken cookie in the response.
     *
     * @param response The HttpServletResponse.
     */
    public static void onLogout(final HttpServletResponse response) {
        dispatchOnLogoutEvent(response);
        clearToken(response);
    }

    private static void dispatchOnLogoutEvent(final HttpServletResponse response) {
        for (WebLogoutEventListener listener : getAllListeners(WebLogoutEventListener.class)) {
            listener.onLogout(response);
        }
    }

    /**
     * Remove the accessToken cookie in the response.
     */
    private static void clearToken(final HttpServletResponse response) {
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /**
     * @param request the request containing the cookie.
     * @return the token from the accessToken cookie, or null if not found.
     */
    public static AccessToken getToken(final HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return new AccessToken(cookie.getValue());
            }
        }
        return null;
    }

    private static <T> Iterable<T> getAllListeners(final Class<T> iface) {
        // Note: For this to work, Reflections Maven Plugin must be used (groupId: org.reflections,
        // artifactId: reflections-maven).
        List<T> list = new ArrayList<>();
        Reflections reflections = Reflections.collect();
        if (reflections != null) {
            Set<Class<? extends T>> implementors = reflections.getSubTypesOf(iface);
            for (Class<? extends T> clazz : implementors) {
                try {
                    list.add(clazz.newInstance());
                } catch (InstantiationException | IllegalAccessException exception) {
                    LOGGER.log(Level.WARNING, "Failed to dispatch on event.", exception);
                }
            }
        }

        return list;
    }
}
