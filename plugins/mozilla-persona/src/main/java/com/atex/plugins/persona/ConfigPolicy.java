package com.atex.plugins.persona;

import java.util.logging.Level;

import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.model.DescribesModelType;

@DescribesModelType
public class ConfigPolicy extends ContentPolicy {
    private static final String PRIVACY_POLICY_FIELD = "privacyPolicy";
    private static final String TERMS_OF_SERVICE_FIELD = "termsOfService";
    private static final String SITE_NAME_FIELD = "siteName";
    private static final String SITE_LOGO_FIELD = "siteLogo";
    private static final String BG_COLOR_FIELD = "backgroundColor";

    public String getPrivacyPolicy() {
        String privacyPolicy = null;
        try {
            privacyPolicy = ((SingleValuePolicy) getChildPolicy(PRIVACY_POLICY_FIELD)).getValue();
        } catch (CMException e) {
            logger.log(Level.SEVERE, "Couldn't read child policy with name '" + PRIVACY_POLICY_FIELD + "'", e);
        }
        return privacyPolicy;
    }

    public String getTermsOfService() {
        String termsOfService = null;
        try {
            termsOfService = ((SingleValuePolicy) getChildPolicy(TERMS_OF_SERVICE_FIELD)).getValue();
        } catch (CMException e) {
            logger.log(Level.SEVERE, "Couldn't read child policy with name '" + TERMS_OF_SERVICE_FIELD + "'", e);
        }
        return termsOfService;
    }

    public String getSiteName() {
        String siteName = null;
        try {
            siteName = ((SingleValuePolicy) getChildPolicy(SITE_NAME_FIELD)).getValue();
        } catch (CMException e) {
            logger.log(Level.SEVERE, "Couldn't read child policy with name '" + SITE_NAME_FIELD + "'", e);
        }
        return siteName;
    }

    public String getSiteLogo() {
        String siteLogo = null;
        try {
            siteLogo = ((SingleValuePolicy) getChildPolicy(SITE_LOGO_FIELD)).getValue();
        } catch (CMException e) {
            logger.log(Level.SEVERE, "Couldn't read child policy with name '" + SITE_LOGO_FIELD + "'", e);
        }
        return siteLogo;
    }

    public String getBackgroundColor() {
        String bgColor = null;
        try {
            bgColor = ((SingleValuePolicy) getChildPolicy(BG_COLOR_FIELD)).getValue();
        } catch (CMException e) {
            logger.log(Level.SEVERE, "Couldn't read child policy with name '" + BG_COLOR_FIELD + "'", e);
        }
        return bgColor;
    }
}
