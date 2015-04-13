package com.atex.plugins.widget;

import com.atex.onecms.content.Content;
import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.Subject;
import com.atex.onecms.ws.image.ImageServiceConfigurationProvider;
import com.atex.onecms.ws.image.ImageServiceUrlBuilder;
import com.atex.plugins.baseline.widget.OContentListEntryBasePolicyWidget;
import com.atex.plugins.imagegallery.ImageGalleryPolicy;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.orchid.widget.OContentIdLink;
import com.polopoly.cm.app.util.URLBuilder;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.CmClientBase;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.js.HasCssDependencies;
import com.polopoly.orchid.widget.OWidgetBase;

import java.io.IOException;
import java.util.Collection;

public class OGalleryTocEntryRenderer extends OContentListEntryBasePolicyWidget implements HasCssDependencies {

    private String cssFileUrl;
    private OContentIdLink imagesLink;

    @Override
    public void initSelf(final OrchidContext oc) throws OrchidException {
        super.initSelf(oc);
        cssFileUrl = URLBuilder.getFileUrl(OImageDropTargetWidget.PLUGIN_FILES_EXTERNAL_CONTENT_ID,
                                           OImageDropTargetWidget.CSS_FILE_PATH,
                                           oc);
        imagesLink = new OContentIdLink();
        addAndInitChild(oc, imagesLink);
        imagesLink.setContentId(getPolicy().getContentId());
        imagesLink.addAndInitChild(oc, new ImageList());
    }

    private ImageGalleryPolicy getImageGalleryPolicy() {
        return (ImageGalleryPolicy) getPolicy();
    }

    @Override
    protected void renderEntryBody(final Device device, final OrchidContext oc) throws OrchidException, IOException {
        imagesLink.render(oc);
    }

    private void renderImageInline(final ContentId imageContentId,
                                   final ContentManager contentManager,
                                   final CmClient cmClient,
                                   final Device device)
        throws IOException, OrchidException {
        com.atex.onecms.content.ContentId contentId = IdUtil.fromPolicyContentId(imageContentId);

        ContentVersionId versionId = contentManager.resolve(contentId, Subject.NOBODY_CALLER);
        ContentResult<Object> result = contentManager
                                           .get(versionId, null, Object.class, null,
                                                Subject.NOBODY_CALLER);
        Content<Object> content = result.getContent();

        try {
            String secret = new ImageServiceConfigurationProvider(cmClient.getPolicyCMServer())
                                .getImageServiceConfiguration().getSecret();
            ImageServiceUrlBuilder urlBuilder = new ImageServiceUrlBuilder(content, secret).height(160);
            device.println("<img src='" + urlBuilder.buildUrl() + "'>");
        } catch (CMException e) {
            throw new OrchidException(e);
        }
    }

    @Override
    protected void renderEntryFooter(final Device device, final OrchidContext oc) throws OrchidException, IOException {
    }

    @Override
    public String[] getCssDependencies() {
        return new String[] {
                cssFileUrl
        };
    }

    private class ImageList extends OWidgetBase {
        @Override
        public void renderChildren(final OrchidContext oc)
            throws IOException, OrchidException {

            final CmClient cmClient = (CmClient) oc.getApplication()
                                                   .getApplicationComponent(CmClientBase.DEFAULT_COMPOUND_NAME);
            final ContentManager contentManager = cmClient.getContentManager();
            final Collection<ContentId> images = getImageGalleryPolicy().getImages();

            Device device = oc.getDevice();
            device.println("<div class='image-entry'>");
            int index = 0;
            for (ContentId imageContentId : images) {
                renderImageInline(imageContentId, contentManager, cmClient, device);
                if (++index > 3) {
                    break;
                }
            }
            if (images.size() > index) {
                device.println("<span>+" + (images.size() - index) + "<span>");
            }
            device.println("</div>");
        }
    }
}
