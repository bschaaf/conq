package com.atex.plugins.imagegallery;

import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.policy.SingleValued;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.PolicyCMServer;

public class ImageGalleryConfigurationPolicy extends ContentPolicy {
    public static final ExternalContentId EXTERNAL_CONTENT_ID =
        new ExternalContentId("plugins.com.atex.plugins.image-gallery-plugin.Config");

    public static ImageGalleryConfigurationPolicy getConfigurationPolicy(final PolicyCMServer cmServer)
        throws CMException {
        return (ImageGalleryConfigurationPolicy) cmServer.getPolicy(EXTERNAL_CONTENT_ID);
    }

    public String getCreatorExternalIdString() throws CMException {
        return ((SingleValued) getChildPolicy("inputTemplate")).getValue();
    }
}
