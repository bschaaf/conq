package com.atex.plugins.search;

import com.atex.onecms.content.ContentManager;
import com.google.common.collect.Lists;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ContentManagerMappingReader {
    private static final Logger LOGGER = Logger.getLogger(ContentManagerMappingReader.class.getName());

    private PolicyCMServer cmServer;
    private ExternalContentId configId;

    public ContentManagerMappingReader(final PolicyCMServer cmServer, final ContentManager contentManager) {
        this.cmServer = cmServer;
        this.configId = new ExternalContentId(contentManager.getConfigId());
    }

    /**
     * Get the name of input templates which can reasonably be assumed to produce the given variant.
     * Only types with an inputTemplateMapping will be included.
     * For variants with a non-excluding default composer it is necessary to also explicitly map each source type
     * for this method to find them.
     * @param variantName The name of the variant. Must not be null.
     * @return A list of input template names.
     */
    public List<String> getInputTemplatesForVariant(final String variantName) {
        // This relies on the configuration being policy content and stored as implied by
        // support.polopoly.com/confluence/display/PolopolyMaster/How+to+make+content+available+to+the+Content+API
        List<String> types = Lists.newArrayList();
        try {
            Policy apiConf = cmServer.getPolicy(configId);
            String variantMapping = apiConf.getContent().getComponent("variantMapping", variantName);
            if (variantMapping == null) {
                LOGGER.warning("Variant not found in configuration: " + variantName);
                return types;
            }
            Policy searchVariantConf = cmServer.getPolicy(new ExternalContentId(variantMapping));
            String[] beanNames = searchVariantConf.getContent().getComponentNames("composer");

            for (String beanName : beanNames) {
                String it = apiConf.getContent().getComponent("inputTemplateMapping", beanName);
                if (it != null) {
                    types.add(it);
                }
            }
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, "Failed to read variant configuration", e);
        }

        return types;
    }
}
