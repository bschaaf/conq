package com.atex.standard.article.widget;

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
import com.atex.standard.article.ArticlePolicy;
import com.atex.standard.image.ImageContentDataBean;
import com.atex.standard.util.WidgetUtil;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.search.widget.OSearchThumbBase;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.CmClientBase;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;

import java.io.IOException;

@SuppressWarnings("serial")
public class OSearchThumb extends OSearchThumbBase {

    private static final int DEFAULT_LEAD_WIDTH = 200;
    private static final int DEFAULT_IMAGE_WIDTH = 50;
    private static final int DEFAULT_WIDGET_WIDTH = 250;
    private ArticlePolicy article;
    private String name;
    private ImageServiceUrlBuilder urlBuilder;

    protected int getWidth() {
        return DEFAULT_WIDGET_WIDTH;
    }

    @Override
    public void initSelf(final OrchidContext oc) throws OrchidException {
        super.initSelf(oc);
        try {
            article = (ArticlePolicy) getPolicy();
            name = article.getName();
            if (name == null || "".equals(name)) {
                name = article.getContentId().getContentId().getContentIdString();
            }

            ContentId imageContentId = article.getReferredImageId();
            if (imageContentId != null) {
                CmClient cmClient = (CmClient) oc.getApplication()
                        .getApplicationComponent(CmClientBase.DEFAULT_COMPOUND_NAME);
                ContentManager contentManager = cmClient.getContentManager();
                com.atex.onecms.content.ContentId contentId = IdUtil.fromPolicyContentId(imageContentId);
                ContentVersionId versionId = contentManager.resolve(contentId, Subject.NOBODY_CALLER);
                ContentResult<ImageContentDataBean> result = contentManager
                        .get(versionId, null, ImageContentDataBean.class, null, Subject.NOBODY_CALLER);
                Content<ImageContentDataBean> content = result.getContent();

                String secret = new ImageServiceConfigurationProvider(cmClient.getPolicyCMServer())
                    .getImageServiceConfiguration().getSecret();
                this.urlBuilder = new ImageServiceUrlBuilder(content, secret).width(100)
                                                                             .aspectRatio(new AspectRatio(1, 1))
                                                                             .mode(ResizeMode.FILL);
            }
        } catch (CMException e) {
            throw new OrchidException(e);
        }
    }

    @Override
    protected void renderThumb(final OrchidContext oc) throws IOException, OrchidException {
        Device device = oc.getDevice();
        device.print("<h2>" + name + "</h2>");
        if (urlBuilder != null) {
            device.println("<img src='" + urlBuilder.buildUrl() + "' class='thumbnail' />");
        }
        if (article.getLead() != null) {
        device.println("<p class='lead'>"
                        + WidgetUtil.abbreviate(article.getLead(), DEFAULT_LEAD_WIDTH)
                        + "</p>");
        }
    }

    @Override
    protected String getCSSClass() {
        return "custom-search-article";
    }
}
