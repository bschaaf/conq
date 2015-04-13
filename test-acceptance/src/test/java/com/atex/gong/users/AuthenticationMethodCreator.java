package com.atex.gong.users;


import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentWrite;
import com.atex.onecms.content.SetAliasOperation;
import com.atex.onecms.content.Subject;
import com.atex.plugins.users.AuthenticationMethod;
import com.polopoly.cm.client.CmClient;

import java.util.UUID;

public class AuthenticationMethodCreator {

    private final String authenticationMethodDescription = "Test Method";
    private final CmClient cmClient;

    public AuthenticationMethodCreator(final CmClient cmClient) {
        this.cmClient = cmClient;
    }

    public AuthenticationMethod create() {
        final String authenticationMethodName = "my-auth"
                + UUID.randomUUID().toString();
        final AuthenticationMethod authenticationMethod = new AuthenticationMethod();
        authenticationMethod.setName(authenticationMethodName);
        authenticationMethod.setDescription(authenticationMethodDescription);
        SetAliasOperation aliasOp = new SetAliasOperation(SetAliasOperation.EXTERNAL_ID,
                "atex.AuthenticationMethod:" + authenticationMethodName);
        final ContentManager contentManager = cmClient.getContentManager();
        contentManager.create(new ContentWrite<>(AuthenticationMethod.ASPECT_NAME, authenticationMethod, aliasOp),
                new Subject("98", null));
        return authenticationMethod;
    }

}
