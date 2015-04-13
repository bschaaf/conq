package com.atex.gong.utils;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policymvc.PolicyModelDomain;
import com.polopoly.model.Model;
import com.polopoly.siteengine.dispatcher.SiteEngine;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Velocity tool for fetching models.
 */
public class ModelTool {

    private static final Logger LOG = Logger.getLogger(ModelTool.class.getName());

    /**
     * Returns a model for a given content id.
     *
     * @param contentId the content id
     * @return a model for the given content id.
     */
    public Model getModel(final ContentId contentId) throws CMException {

        PolicyModelDomain policyModelDomain =
                (PolicyModelDomain) SiteEngine.getApplication() /* new_wiring_approved */
                        .getModelDomain();

        return policyModelDomain.getModel(contentId);
    }

    /**
     * Returns a model for given content id string.
     *
     * @param contentIdString the content id string
     * @return a model for the given content id.
     * @throws CMException
     */
    public Model getModel(final String contentIdString) throws CMException {
        try {
            return getModel(ContentIdFactory.createContentId(contentIdString));
        } catch (IllegalArgumentException e) {
            LOG.log(Level.WARNING, "Couldn't get model for given content id string.", e);
        }
        return null;
    }
}
