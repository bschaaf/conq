/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.widget;

import com.atex.onecms.content.Content;
import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.Subject;
import com.atex.onecms.image.AspectRatio;
import com.atex.onecms.image.ResizeMode;
import com.atex.onecms.ws.image.ImageServiceConfigurationProvider;
import com.atex.onecms.ws.image.ImageServiceUrlBuilder;
import com.atex.plugins.imagegallery.ImageGalleryPolicy;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.search.widget.OSearchThumbBase;
import com.polopoly.cm.app.util.URLBuilder;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.CmClientBase;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.js.HasCssDependencies;

import java.io.IOException;
import java.util.Collection;

public class OSearchThumb extends OSearchThumbBase implements HasCssDependencies {

    private String cssFileUrl;

    @Override
    public void initSelf(final OrchidContext oc) throws OrchidException {
        super.initSelf(oc);
        cssFileUrl = URLBuilder.getFileUrl(OImageDropTargetWidget.PLUGIN_FILES_EXTERNAL_CONTENT_ID,
                                           OImageDropTargetWidget.CSS_FILE_PATH, oc);
    }

    private ImageGalleryPolicy getImageGalleryPolicy() {
        return (ImageGalleryPolicy) getPolicy();
    }

    @Override
    protected void renderThumb(final OrchidContext oc) throws IOException, OrchidException {
        Device device = oc.getDevice();

        CmClient cmClient = (CmClient) oc.getApplication()
                .getApplicationComponent(CmClientBase.DEFAULT_COMPOUND_NAME);
        ContentManager contentManager = cmClient.getContentManager();
        Collection<ContentId> images = getImageGalleryPolicy().getImages();
        device.println("<div class='image-container'>");
        int index = 0;
        for (ContentId imageContentId : images) {
            com.atex.onecms.content.ContentId contentId = IdUtil.fromPolicyContentId(imageContentId);

            ContentVersionId versionId = contentManager.resolve(contentId, Subject.NOBODY_CALLER);
            ContentResult<Object> result = contentManager
                    .get(versionId, null, Object.class, null, Subject.NOBODY_CALLER);
            Content<Object> content = result.getContent();

            try {
                String secret = new ImageServiceConfigurationProvider(cmClient.getPolicyCMServer())
                        .getImageServiceConfiguration().getSecret();
                ImageServiceUrlBuilder urlBuilder = new ImageServiceUrlBuilder(content, secret).width(80)
                        .aspectRatio(new AspectRatio(1, 1))
                        .mode(ResizeMode.FILL);
                device.println("<img src='" + urlBuilder.buildUrl() + "'>");
            } catch (CMException e) {
                throw new OrchidException(e);
            }
            if (++index > 3) {
                break;
            }
        }
        device.println("<div class='image-count'>" + images.size() + "</div>");
        device.println("</div>");

        try {
            device.println(String.format("<p>%s</p>", abbreviate(getImageGalleryPolicy().getName(), 35)));
        } catch (CMException e) {
            throw new OrchidException(e);
        }
    }

    private String abbreviate(final String str, final int maxWidth) {
        if (str.length() <= maxWidth) {
            return str;
        } else {
            return str.substring(0, maxWidth - 3) + "...";
        }
    }

    @Override
    protected int getWidth() {
        return 100;
    }

    @Override
    protected String getCSSClass() {
        return "custom-search-image-gallery";
    }

    @Override
    public String[] getCssDependencies() {
        return new String[] {
                cssFileUrl
        };
    }
}
