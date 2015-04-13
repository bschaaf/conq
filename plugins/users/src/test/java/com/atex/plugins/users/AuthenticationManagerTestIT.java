package com.atex.plugins.users;

import com.atex.onecms.content.Content;
import com.atex.onecms.content.ContentId;
import com.google.inject.Inject;
import com.polopoly.cm.client.CmClient;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnj.TestNJRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(TestNJRunner.class)
@ImportTestContent
public class AuthenticationManagerTestIT {

    private static final int TWO_SECONDS = 2000;

    @Inject
    private CmClient cmClient;

    private AuthenticationManager authManager;
    private String method;

    @Before
    public void init() throws Exception {
        method = new AuthenticationMethodCreator(cmClient).create().getName();
        authManager = new AuthenticationManager(cmClient);
    }

    @Test
    public void testGetAuthMethod() throws Exception {
        Content<AuthenticationMethod> authMethod = authManager.getAuthenticationMethod(method);
        assertNotNull(authMethod);
        assertEquals(method, authMethod.getContentData().getName());
    }

    @Test
    public void testGetMissingUser() throws Exception {
        ContentId userId = authManager.getUserId(method, "testusernoexist");
        assertNull(userId);
    }

    @Test
    public void testCreateGetUser() throws Exception {
        String userLogin = "testuser-" + UUID.randomUUID().toString();
        ContentId userId = authManager.createUser(method, userLogin, new User());
        assertNotNull("Create user returned null", userId);
        ContentId getId = authManager.getUserId(method, userLogin);
        assertEquals("Get user did not return created user id", userId, getId);

        AccessToken token = authManager.getToken(userId);
        assertNotNull("Null token from user id", token);
        ContentId userIdFromToken = authManager.getUserId(token);
        assertEquals("Incorrect user id from token", userId, userIdFromToken);
    }

    @Test
    public void testExpiredToken() throws Exception {
        String userLogin = "testuser";
        ContentId userId = authManager.createUser(method, userLogin, new User());

        AccessToken token = authManager.getToken(userId, 1);
        ContentId userIdByToken = authManager.getUserId(token);
        assertEquals(userId, userIdByToken);

        Thread.sleep(TWO_SECONDS);

        ContentId expiredUserId = authManager.getUserId(token);
        assertNull("Expected null user id from expired token", expiredUserId);
    }

    @Test
    public void testInvalidTokenSignature() {
        String userLogin = "testuser";
        ContentId userId = authManager.createUser(method, userLogin, new User());
        AccessToken token = authManager.getToken(userId, 1);
        String tokenString = token.getToken();
        try {
            authManager.getUserId(new AccessToken(tokenString.substring(0, tokenString.length() - 1) + "*"));
            fail("Expected exception on invalid token signature.");
        } catch (RuntimeException exception) {
            assertEquals("Invalid token signature", exception.getMessage());
        }
    }

    @Test
    public void testInvalidToken() {
        String userLogin = "testuser";
        ContentId userId = authManager.createUser(method, userLogin, new User());
        AccessToken token = authManager.getToken(userId, 1);
        String tokenString = token.getToken();
        try {
            authManager.getUserId(new AccessToken(tokenString + "::foobar"));
            fail("Expected exception on invalid token.");
        } catch (RuntimeException exception) {
            assertEquals("Invalid token", exception.getMessage());
        }
    }
}
