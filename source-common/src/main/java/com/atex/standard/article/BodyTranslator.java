package com.atex.standard.article;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.servlet.HtmlPathUtil;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.path.ContentPathCreator;
import com.polopoly.cm.servlet.RequestPreparator;
import com.polopoly.cm.servlet.URLBuilder;
import com.polopoly.render.RenderRequest;
/*
 * Translator for Polopoly content links and images.
 */
public class BodyTranslator {

    private static final Logger LOG = Logger.getLogger(BodyTranslator.class.getName());

    public String translateBody(final RenderRequest request,
                                final ContentId contentId,
                                final String body,
                                final boolean inPreviewMode) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        ContentPathCreator pathCreator = RequestPreparator.getPathCreator(httpServletRequest);

        URLBuilder builder = RequestPreparator.getURLBuilder(httpServletRequest);

        // Use latest version when previewing
        VersionedContentId latestVersionContentId = null;

        if (inPreviewMode) {
            latestVersionContentId = contentId.getLatestVersionId();
        }

        try {
            // If we are in preview mode we keep Polopoly attributes on links
            if (inPreviewMode) {
                return HtmlPathUtil.pathify(body,
                                            pathCreator,
                                            builder,
                                            httpServletRequest,
                                            latestVersionContentId,
                                            true);
            } else {
                return HtmlPathUtil.pathify(body,
                                            pathCreator,
                                            builder,
                                            httpServletRequest,
                                            latestVersionContentId);
            }
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Could not parse body.", e);
        }
        return body;
    }
}
