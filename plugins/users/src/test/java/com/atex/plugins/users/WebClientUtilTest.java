package com.atex.plugins.users;

import com.atex.onecms.content.ContentId;
import com.polopoly.cm.client.CmClient;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class WebClientUtilTest implements WebLoginEventListener, WebLogoutEventListener {

    private static final String MY_ACCESS_TOKEN_VALUE = "policy:18.98::1234567890::0000000000000000";
    private static final int HTTP_STATUS_CREATED = 201;
    private static final int HTTP_STATUS_RESET_CONTENT = 205;

    @Test
    public void testSetToken() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ArgumentCaptor<Cookie> argument = ArgumentCaptor.forClass(Cookie.class);
        WebClientUtil.onLogin(null,
                new AccessToken(MY_ACCESS_TOKEN_VALUE),
                response,
                null);
        verify(response).addCookie(argument.capture());
        assertEquals(WebClientUtil.ACCESS_TOKEN_COOKIE_NAME, argument.getValue().getName());
        assertEquals(MY_ACCESS_TOKEN_VALUE, argument.getValue().getValue());
    }

    @Test
    public void testClearToken() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ArgumentCaptor<Cookie> argument = ArgumentCaptor.forClass(Cookie.class);
        WebClientUtil.onLogout(response);
        verify(response).addCookie(argument.capture());
        assertEquals(WebClientUtil.ACCESS_TOKEN_COOKIE_NAME, argument.getValue().getName());
        assertEquals("", argument.getValue().getValue());
    }

    @Test
    public void testDispatchOnLoginEvent() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ArgumentCaptor<Integer> argument = ArgumentCaptor.forClass(Integer.class);
        WebClientUtil.onLogin(null,
                new AccessToken(MY_ACCESS_TOKEN_VALUE),
                response,
                null);
        verify(response).setStatus(argument.capture());
        assertEquals(HTTP_STATUS_CREATED, argument.getValue().intValue());
    }

    @Test
    public void testDispatchOnLogoutEvent() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ArgumentCaptor<Integer> argument = ArgumentCaptor.forClass(Integer.class);
        WebClientUtil.onLogout(response);
        verify(response).setStatus(argument.capture());
        assertEquals(HTTP_STATUS_RESET_CONTENT, argument.getValue().intValue());
    }

    @Test
    public void testGetToken() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Cookie cookie = new Cookie(WebClientUtil.ACCESS_TOKEN_COOKIE_NAME, MY_ACCESS_TOKEN_VALUE);
        Cookie[] cookies = {cookie};
        Mockito.when(request.getCookies()).thenReturn(cookies);
        AccessToken accessToken = WebClientUtil.getToken(request);
        assertEquals(MY_ACCESS_TOKEN_VALUE, accessToken.getToken());
    }


    @Override
    public void onLogout(final HttpServletResponse response) {
        response.setStatus(HTTP_STATUS_RESET_CONTENT);
    }

    @Override
    public void onLogin(final ContentId user,
                        final AccessToken accessToken,
                        final HttpServletResponse response,
                        final CmClient cmClient) {
        response.setStatus(HTTP_STATUS_CREATED);
    }

}
