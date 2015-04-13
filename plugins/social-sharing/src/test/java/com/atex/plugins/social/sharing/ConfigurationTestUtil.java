package com.atex.plugins.social.sharing;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;

/**
 * Utility to set and reset configuration of social sharing.
 */
public class ConfigurationTestUtil {

    private static final ExternalContentId CONFIG_ID =
            new ExternalContentId("plugins.com.atex.plugins.social-sharing.Config");

    private static final Logger LOG = Logger.getLogger(ConfigurationTestUtil.class.getName());

    private PolicyCMServer cmServer;

    private SocialSharingConfigPolicy oldPolicy = null;
    private List<Config> newSettings = new ArrayList<Config>();

    /**
     * Constructor fetches policy to cache and takes injectable params required
     * for content operations.
     *
     * @param cmServer
     * @param changeList
     */
    public ConfigurationTestUtil(final PolicyCMServer cmServer) {
        this.cmServer = cmServer;
        try {
            oldPolicy = getSocialConfig();
        } catch (CMException e) {
            LOG.log(Level.SEVERE,
                    "Unable to get old config, will not be able to restore configuration after test, message: "
                            + e.getMessage());
        }
    }

    /**
     * Adds configuration component to internal list.
     * @param group
     * @param name
     * @param value
     */
    public void addConfiguration(final String group, final String name, final String value) {
        newSettings.add(new Config(group, name, value));
    }

    /**
     * Sets configuration added by addConfiguration method on new version of configuration policy.
     */
    public void setConfiguration() throws CMException, InterruptedException {
        SocialSharingConfigPolicy newPolicy =
                (SocialSharingConfigPolicy) cmServer.createContentVersion(oldPolicy.getContentId());
        for (Config config : newSettings) {
            newPolicy.setComponent(config.getGroup(), config.getName(), config.getValue());
        }
        newPolicy.commit();
        // Wait for changelist (make sure init method does this)
    }

    /**
     * Restores configuration from cached policy.
     */
    public void resetConfiguration() throws CMException, InterruptedException {
        SocialSharingConfigPolicy newPolicy =
                (SocialSharingConfigPolicy) cmServer.createContentVersion(getSocialConfig().getContentId());
        for (Config config : newSettings) {
            try {
                String oldValue = oldPolicy.getComponent(config.getGroup(), config.getName());
                LOG.fine(" ----- Setting values: " + config.getGroup() + ":" + config.getName() + ":" + oldValue);
                newPolicy.setComponent(config.getGroup(), config.getName(), oldValue);
            } catch (CMException e) {
                LOG.log(Level.SEVERE, "Test was unable to change back component: "
                        + config.getGroup() + ":"
                        + config.getName());
            }
        }
        newPolicy.commit();
        // Wait for changelist (not needed for reset)
    }

    private SocialSharingConfigPolicy getSocialConfig() throws CMException {
        ContentId configId = cmServer.findContentIdByExternalId(CONFIG_ID);
        SocialSharingConfigPolicy policy = (SocialSharingConfigPolicy) cmServer.getPolicy(configId);
        return policy;
    }

    /**
     * Represents a config component.
     */
    public class Config {
        private String group;
        private String name;
        private String value;
        public Config(final String group, final String name, final String value) {
            this.group = group;
            this.name = name;
            this.value = value;
        }
        public String getGroup() {
            return group;
        }
        public String getName() {
            return name;
        }
        public String getValue() {
            return value;
        }
    }
}
