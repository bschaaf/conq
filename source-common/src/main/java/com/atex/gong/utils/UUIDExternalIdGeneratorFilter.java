package com.atex.gong.utils;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.deepcopy.filter.DefaultExportFilter;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Filter for XML export.
 * <p/>
 * Usage: Replace <Site Content Id> in the string bellow with the contentId of your site, then paste
 * the string in your browser window.
 * <p/>
 * http://localhost:8080/polopoly/export?contentid=<Site-Content-Id>&externalIdPrefix=&includeContent=true&maxCopyTreeSize=1024000&filterClassName=com.atex.gong.utils.UUIDExternalIdGeneratorFilter&exportToJar=true
 * <p/>
 * The result is a file called export.jar.
 */
public class UUIDExternalIdGeneratorFilter extends DefaultExportFilter {

    private static final Logger LOGGER =
            Logger.getLogger(UUIDExternalIdGeneratorFilter.class.getName());

    public boolean typeIsCopyable(final PolicyCMServer cmServer, final ContentId referenceId) {
        if (!super.typeIsCopyable(cmServer, referenceId)) {
            return false;
        }
        // If the content doesn't have an externalId generate one base on the java implementation of
        // UUID
        try {
            ContentRead content = cmServer.getContent(referenceId);
            String templateExtID = cmServer.getContentInfo(content.getInputTemplateId()).getExternalId();
            //don't export blogs or banner elements
            if (templateExtID.contains("BannerElement") || templateExtID.contains("EditorialBlog")) {
                return false;
            }
            if (content.getExternalId() == null) {
                String externalId = "exportId-" + UUID.randomUUID().toString();
                Policy policy = cmServer.createContentVersion(referenceId.getLatestVersionId());
                policy.getContent().setExternalId(externalId);
                cmServer.commitContent(policy);
            }
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, "Error while checking/setting externalId", e);
        }
        return true;
    }

    public boolean isAlwaysKeepReference(final PolicyCMServer cmServer, final ContentId referenceId) {
        try {
            ContentRead content = cmServer.getContent(referenceId);
            String templateExtID = cmServer.getContentInfo(content.getInputTemplateId()).getExternalId();
            if (templateExtID.contains("BannerElement")
                    || templateExtID.contains("EditorialBlog")) {
                return false;
            }
            boolean isCategory = "p.TreeCategory".equals(templateExtID)
                    || "p.Site".equals(templateExtID) || "p.page".equals(templateExtID);
            if (isCategory) {
                return true;
            }
            return super.isAlwaysKeepReference(cmServer, referenceId);
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, "Error while checking externalId", e);
        }
        return true;
    }
}
