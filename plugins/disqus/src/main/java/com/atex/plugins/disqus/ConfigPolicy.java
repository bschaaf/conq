package com.atex.plugins.disqus;

import com.atex.plugins.baseline.policy.BaselinePolicy;
import com.polopoly.cm.app.policy.BooleanValuePolicy;
import com.polopoly.cm.client.CMException;

import java.util.logging.Level;

public class ConfigPolicy extends BaselinePolicy {

    private static final String DISCQUS_SITE_SHORTNAME = "siteShortname";
    private static final String DISQUS_ENABLED = "enabled";

    public String getSiteShortname() {
        return getChildValue(DISCQUS_SITE_SHORTNAME, "");
    }

    public boolean isEnabled() {
        boolean enabled = false;
        try {
            enabled = ((BooleanValuePolicy) getChildPolicy(DISQUS_ENABLED)).getBooleanValue();
        } catch (CMException exception) {
            logger.log(Level.SEVERE, "Couldn't read child policy with name '" + DISQUS_ENABLED + "'", exception);
        }
        return enabled;
    }
}
