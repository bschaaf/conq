package com.atex.plugins.users;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.onecms.content.Content;
import com.atex.onecms.content.ContentId;
import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.ContentWrite;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.SetAliasOperation;
import com.atex.onecms.content.Subject;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.PolicyCMServer;

/**
 * Utilities for creating and finding users with authentication methods
 * as well as exchanging user ids to and from access tokens.
 */
public class AuthenticationManager {

    private static final Logger LOG = Logger.getLogger(AuthenticationManager.class.getName());

    private static final int DEFAULT_EXPIRATION = 30 * 24 * 60 * 60; // 30 days
    private static final long MS_TO_SEC = 1000L;
    private static final int NUM_TOKEN_PARTS = 3;
    private static final Subject SYSTEM_SUBJECT = new Subject("98", null);
    private static final String CONFIG_EXT_ID = "plugins.com.atex.plugins.users.Config";

    private final CmClient cmClient;
    private final ContentManager contentManager;

    public AuthenticationManager(final CmClient cmClient) {
        this.cmClient = cmClient;
        this.contentManager = cmClient.getContentManager();
    }

    /**
     * Get the content which declares the named authentication method.
     * @param name name of authentication method (e.g. "polopoly", "mozilla-persona")
     * @return authentication method, or null if no such method exists
     */
    public Content<AuthenticationMethod> getAuthenticationMethod(final String name) {
        ContentVersionId id = contentManager.resolve(getMethodExternalId(name), SYSTEM_SUBJECT);
        if (id == null) {
            return null;
        }

        ContentResult<AuthenticationMethod> result = contentManager.get(id,
                                                                        null,
                                                                        AuthenticationMethod.class,
                                                                        Collections.<String, Object>emptyMap(),
                                                                        SYSTEM_SUBJECT);

        if (!result.getStatus().isOk()) {
            throw new RuntimeException(String.format("Failed to read content for authentication method '%s'.", name));
        }

        return result.getContent();
    }

    /**
     * Get the id of a user content from an authentication method and login id.
     * @param method name of authentication method (e.g. "polopoly", "mozilla-persona")
     * @param loginId user's id in authentication method (e.g. polopoly user id or persona email address)
     * @return id of user content or null if login is not connected to a user
     */
    public ContentId getUserId(final String method, final String loginId) {
        UserAuthentication auth = getUserAuth(method, loginId);
        if (auth == null) {
            return null;
        }

        return auth.getUserId();
    }

    /**
     * Create a user content and attach it to an authentication method and login id.
     * The method+login must not be associated with a user already.
     * @param method name of authentication method (e.g. "polopoly", "mozilla-persona")
     * @param loginId user's id in authentication method (e.g. polopoly user id or persona email address)
     * @param user data to initialize user with, note that authentications must be empty
     * @return id of created user content
     */
    public ContentId createUser(final String method, final String loginId, final User user) {
        UserAuthentication auth = getUserAuth(method, loginId);
        if (auth != null) {
            throw new RuntimeException("User exists");
        }
        auth = createUserWithAuth(method, loginId, user);

        return auth.getUserId();
    }

    /**
     * Validate token and return the user content id it represents.
     * @param token token, as returned by {@link #getToken(com.atex.onecms.content.ContentId)}.
     * @return user's content id, or null if the token has expired.
     */
    public ContentId getUserId(final AccessToken token) {
        if (token == null) {
            return null;
        }

        String[] message = token.getToken().split("::");
        if (message.length != NUM_TOKEN_PARTS) {
            throw new RuntimeException("Invalid token");
        }

        String signature = signature(message[0] + "::" + message[1]);
        if (!signature.equals(message[2])) {
            throw new RuntimeException("Invalid token signature");
        }

        if (Long.valueOf(message[1]) < getCurrentTime()) {
            return null;
        }

        return IdUtil.fromString(message[0]);
    }

    /**
     * Get a token for the user. See {@link #getUserId(AccessToken)}.
     * @param userId user's content id
     * @return an AccessToken
     */
    public AccessToken getToken(final ContentId userId) {
        return getToken(userId, DEFAULT_EXPIRATION);
    }

    protected AccessToken getToken(final ContentId userId, final int leaseTime) {
        long expirationTime = getCurrentTime() + leaseTime;
        String payload = IdUtil.toIdString(userId) + "::" + expirationTime;
        String token = payload + "::" + signature(payload);

        return new AccessToken(token);
    }

    private String getSignatureSecretKey() {
        PolicyCMServer policyCMServer = cmClient.getPolicyCMServer();
        try {
            ConfigPolicy policy = (ConfigPolicy) policyCMServer.getPolicy(new ExternalContentId(CONFIG_EXT_ID));
            String secret = policy.getTokenSecret();
            if (secret != null && !secret.isEmpty()) {
                return secret;
            } else {
                LOG.log(Level.SEVERE, "No secret key configured.");
            }
        } catch (CMException e) {
            LOG.log(Level.SEVERE, "Failed to read secret key configuration.", e);
        }
        throw new RuntimeException("Could not get secret key for token signature.");
    }

    private String signature(final String message) {
        try {
            byte[] keyBytes = getSignatureSecretKey().getBytes();
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKey secretKey = new SecretKeySpec(keyBytes, mac.getAlgorithm());

            mac.init(secretKey);
            mac.update(message.getBytes());

            byte[] signedPayload = mac.doFinal();

            return DatatypeConverter.printHexBinary(signedPayload);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Could not sign message", e);
        }
    }

    private UserAuthentication getUserAuth(final String method,
                                           final String loginId) {

        String externalId = getAuthExternalId(method, loginId);
        ContentVersionId authId = contentManager.resolve(externalId, SYSTEM_SUBJECT);

        if (authId != null) {
            ContentResult<UserAuthentication> authResult = contentManager.get(authId,
                                                                              null,
                                                                              UserAuthentication.class,
                                                                              Collections.<String, Object>emptyMap(),
                                                                              SYSTEM_SUBJECT);
            if (authResult.getStatus().isOk()) {
                return authResult.getContent().getContentData();
            } else {
                LOG.log(Level.WARNING,
                        String.format("Failed to retrieve user authentication for user %s with method %s: %s",
                        loginId, method, authResult.getStatus().getDetailCode()));
            }
        }

        return null;
    }

    private UserAuthentication createUserWithAuth(final String method,
                                                  final String alias,
                                                  final User user) {

        Content<AuthenticationMethod> methodContent = getAuthenticationMethod(method);
        if (methodContent == null) {
            throw new RuntimeException(String.format("Tried to login user with unknown method '%s'.", method));
        }

        String externalId = getAuthExternalId(method, alias);
        user.setAuthentications(Collections.singletonMap(method, externalId));

        ContentId userId = create(new ContentWrite<>(User.ASPECT_NAME, user));

        UserAuthentication auth = new UserAuthentication();
        auth.setMethod(method);
        auth.setLoginId(alias);
        auth.setUserId(userId);

        SetAliasOperation setAliasOperation = new SetAliasOperation(SetAliasOperation.EXTERNAL_ID, externalId);
        create(new ContentWrite<>(UserAuthentication.ASPECT_NAME, auth, setAliasOperation));
        return auth;
    }

    private <T> ContentId create(final ContentWrite<T> write) {
        ContentResult<T> result = contentManager.create(write, SYSTEM_SUBJECT);
        if (!result.getStatus().isOk()) {
            throw new RuntimeException("Failed to create " + write.getContentDataType());
        }

        return result.getContentId().getContentId();
    }

    private long getCurrentTime() {
        return System.currentTimeMillis() / MS_TO_SEC;
    }

    private String getAuthExternalId(final String method, final String key) {
        return String.format("atex.UserAuthentication:%s:%s", method, key);
    }

    private String getMethodExternalId(final String method) {
        return String.format("atex.AuthenticationMethod:%s", method);
    }
}
