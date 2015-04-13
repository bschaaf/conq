package com.atex.standard.image.widget;

import com.atex.onecms.content.Content;
import com.atex.onecms.content.ContentId;
import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.Subject;
import com.atex.onecms.image.ImageInfoAspectBean;
import com.atex.onecms.ws.image.ImageServiceConfigurationProvider;
import com.atex.onecms.ws.image.ImageServiceUrlBuilder;
import com.atex.plugins.baseline.widget.OContentListEntryBasePolicyWidget;
import com.atex.plugins.imagegallery.MetadataWidgetRenderer;
import com.atex.standard.image.ImageContentDataBean;
import com.polopoly.cm.app.orchid.widget.OContentIdLink;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.CmClientBase;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.widget.OImage;
import com.polopoly.orchid.widget.OWidget;
import com.polopoly.util.StringUtil;

import java.io.IOException;

public class OImageTocEntryRenderer extends OContentListEntryBasePolicyWidget implements MetadataWidgetRenderer {

    private OWidget metadataWidget;
    private OContentIdLink imageLink;

    @Override
    public void initSelf(final OrchidContext oc) throws OrchidException {
        super.initSelf(oc);
        CmClient cmClient = (CmClient) oc.getApplication().getApplicationComponent(CmClientBase.DEFAULT_COMPOUND_NAME);
        ContentManager contentManager = cmClient.getContentManager();
        ContentId contentId = IdUtil.fromPolicyContentId(getPolicy().getContentId());
        ContentVersionId versionId = contentManager.resolve(contentId, Subject.NOBODY_CALLER);
        ContentResult<ImageContentDataBean> result = contentManager
                .get(versionId, null, ImageContentDataBean.class, null, Subject.NOBODY_CALLER);
        Content<ImageContentDataBean> content = result.getContent();
        ImageInfoAspectBean imageInfo = (ImageInfoAspectBean) content.getAspectData(ImageInfoAspectBean.ASPECT_NAME);

        if (imageInfo != null && !StringUtil.isEmpty(imageInfo.getFilePath())) {
            try {
                String secret = new ImageServiceConfigurationProvider(cmClient.getPolicyCMServer())
                    .getImageServiceConfiguration().getSecret();
                ImageServiceUrlBuilder urlBuilder = new ImageServiceUrlBuilder(content, secret).width(360).height(240);
                imageLink = new OContentIdLink();
                addAndInitChild(oc, imageLink);
                OImage image = new OImage();
                image.setSrc(urlBuilder.buildUrl());
                image.setStylesheetClass("image-thumbnail");
                imageLink.addAndInitChild(oc, image);
                imageLink.setContentId(getPolicy().getContentId());
            } catch (CMException e) {
                throw new OrchidException(e);
            }
        }

    }

    @Override
    protected void renderEntryHeader(final Device device, final OrchidContext oc) throws OrchidException, IOException {
        device.println("<div class='toolbox-right'>");
        renderToolbox(device, oc);
        device.println("</div>");
    }

    @Override
    protected void renderEntryBody(final Device device, final OrchidContext oc) throws OrchidException, IOException {
        device.println("<div class='clearfix'>");
        if (imageLink != null) {
            imageLink.render(oc);
        }
        contentLink.render(oc);
        if (metadataWidget != null) {
            metadataWidget.render(oc);
        }

        device.println("</div>");
    }

    @Override
    protected void renderEntryFooter(final Device device, final OrchidContext oc)
        throws OrchidException, IOException {
        // Just empty
    }

    @Override
    public void setMetadataWidget(final OWidget metadataWidget) {
        this.metadataWidget = metadataWidget;
    }
}
