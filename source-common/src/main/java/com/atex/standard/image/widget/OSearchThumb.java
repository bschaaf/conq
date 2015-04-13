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
import com.atex.standard.image.ImageContentDataBean;
import com.atex.standard.util.WidgetUtil;
import com.polopoly.cm.app.search.widget.OSearchThumbBase;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.CmClientBase;
import com.polopoly.html.CharConv;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.util.StringUtil;

import java.io.IOException;

@SuppressWarnings("serial")
public class OSearchThumb extends OSearchThumbBase {

    private static final int DEFAULT_TEXT_LENGTH = 20;
    private static final int DEFAULT_WIDGET_WIDTH = 115;
    private String name;
    private int width = 0;
    private int height = 0;
    private ImageServiceUrlBuilder urlBuilder;

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
        ImageContentDataBean contentData = content.getContentData();

        if (imageInfo != null && !StringUtil.isEmpty(imageInfo.getFilePath())) {
            String secret;
            try {
                secret = new ImageServiceConfigurationProvider(cmClient.getPolicyCMServer())
                    .getImageServiceConfiguration().getSecret();
            } catch (CMException e) {
                throw new OrchidException(e);
            }
            this.urlBuilder = new ImageServiceUrlBuilder(content, secret).width(200).height(160);
            this.width = imageInfo.getWidth();
            this.height = imageInfo.getHeight();
        }


        this.name = contentData.getTitle();
        if (StringUtil.isEmpty(this.name)) {
            try {
                this.name = getPolicy().getContent().getName();
            } catch (CMException e) {
                throw new OrchidException(e);
            }
        }
        if (StringUtil.isEmpty(this.name)) {
            this.name = getPolicy().getContentId().getContentId().getContentIdString();
        }
    }

    @Override
    protected int getWidth() {
        return DEFAULT_WIDGET_WIDTH;
    }

    @Override
    protected void renderThumb(final OrchidContext oc) throws IOException, OrchidException {
        Device device = oc.getDevice();

        device.print("<div title='" + (name != null ? CharConv.CC.toHTML(name) : "") + "'>");
        if (urlBuilder != null) {
            device.println(String.format("<img src='%s' class='thumbnail' />", urlBuilder.buildUrl()));
        }
        String dimension = "";
        if (width > 0 && height > 0) {
            dimension = width + " x " + height + " - ";
        }
        device.print("<p>" + dimension
                + (name != null ? WidgetUtil.abbreviate(name, DEFAULT_TEXT_LENGTH) : "") + "</p>");
        device.print("</div>");
    }

    @Override
    protected String getCSSClass() {
        return "custom-search-image";
    }
}
