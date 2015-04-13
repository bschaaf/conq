package com.atex.plugins.social.sharing;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.Subject;
import com.atex.onecms.content.SubjectUtil;
import com.polopoly.cm.client.CmClient;

public class SocialSharingInfoFetcher {
    private static final Logger LOGGER = Logger.getLogger(SocialSharingInfoFetcher.class.getName());

    private CmClient cmClient;

    public SocialSharingInfoFetcher(final CmClient cmClient) {
        this.cmClient = cmClient;
    }

    public SocialSharingInfo fetch(final com.atex.onecms.content.ContentId contentId) {
        ContentManager contentManager = cmClient.getContentManager();
        Subject subject = SubjectUtil.fromCaller(cmClient.getPolicyCMServer().getCurrentCaller());
        ContentVersionId versionedId = contentManager.resolve(contentId, subject);
        SocialSharingInfo info = null;
        if (versionedId != null) {
            ContentResult<SocialSharingInfo> result = contentManager.get(versionedId,
                                                                         SocialSharingInfo.VARIANT_NAME,
                                                                         SocialSharingInfo.class,
                                                                         Collections.<String, Object>emptyMap(),
                                                                         subject);
            if (result.getStatus().isOk()) {
                info = result.getContent().getContentData();
            } else {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE,
                               String.format(
                                    "Tried to fetch %s with variant %s but ContentManager result was %d.",
                                    IdUtil.toVersionedIdString(versionedId),
                                    SocialSharingInfo.VARIANT_NAME,
                                    result.getStatus().getDetailCode()));
                }
            }
        } else {
            LOGGER.log(Level.WARNING, "Could not resolve to versioned id: "
                    + IdUtil.toIdString(contentId));
        }
        return info;
    }
}
