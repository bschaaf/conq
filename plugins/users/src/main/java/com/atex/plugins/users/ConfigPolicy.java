package com.atex.plugins.users;

import java.util.logging.Level;

import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.model.DescribesModelType;

@DescribesModelType
public class ConfigPolicy extends ContentPolicy {
    private static final String TOKEN_SECRET_FIELD = "token.secret";

    public String getTokenSecret() {
        String tokenSecret = null;
        try {
            tokenSecret = ((SingleValuePolicy) getChildPolicy(TOKEN_SECRET_FIELD)).getValue();
        } catch (CMException e) {
            logger.log(Level.SEVERE, "Couldn't read child policy with name '" + TOKEN_SECRET_FIELD + "'", e);
        }
        return tokenSecret;
    }
}
