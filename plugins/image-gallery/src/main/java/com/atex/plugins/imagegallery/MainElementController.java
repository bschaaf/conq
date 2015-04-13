/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.imagegallery;

import static com.atex.onecms.content.IdUtil.fromPolicyContentId;

import com.atex.onecms.content.Content;
import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.Subject;
import com.atex.onecms.ws.image.ImageServiceUrlBuilder;
import com.atex.plugins.imagegallery.util.ImageGalleryImageBean;
import com.polopoly.application.IllegalApplicationStateException;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.Policy;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MainElementController extends RenderControllerBase {

    private static final String IMAGES_ATTRIBUTE = "images";

    @Override
    public void populateModelBeforeCacheKey(final RenderRequest request,
                                            final TopModel m,
                                            final ControllerContext context) {

        ImageGalleryPolicy policy = (ImageGalleryPolicy) ModelPathUtil.getBean(context.getContentModel());
        ModelWrite model = m.getLocal();
        Collection<ContentId> images = policy.getImages();
        ContentManager cm = getContentManager(context);

        ArrayList imageList = new ArrayList();
        int index = 0;
        for (ContentId image : images) {
            ImageHolder imageHolder = getImageHolder(cm, image);
            if (imageHolder != null) {
                imageList.add(imageHolder);

                try {
                    Policy imagesPolicy = policy.getChildPolicy("images");
                    Map<String, String> meta = new HashMap<>();
                    for (String name : imagesPolicy.getComponentNames()) {
                        String key = index + ":";
                        if (name.startsWith(key)) {
                            meta.put(name.substring(key.length()), imagesPolicy.getComponent(name));
                        }
                    }
                    imageHolder.setMeta(meta);
                } catch (CMException e) {
                    throw new RuntimeException(e);
                }
                index++;
            }
        }

        model.setAttribute(IMAGES_ATTRIBUTE, imageList);
    }

    private ImageHolder getImageHolder(final ContentManager cm,
                                       final ContentId image) {
        ImageHolder holder = null;
        ContentResult<ImageGalleryImageBean> variant = cm.get(cm.resolve(fromPolicyContentId(image),
                                                                         Subject.NOBODY_CALLER),
                                                              ImageGalleryImageBean.VARIANT_NAME,
                                                              null,
                                                              null,
                                                              Subject.NOBODY_CALLER);
        if (variant.getStatus().isOk()) {
            Content<ImageGalleryImageBean> content = variant.getContent();
            ImageServiceUrlBuilder urlBuilder = new ImageServiceUrlBuilder(content, "SECRET");
            holder = new ImageHolder(content.getId(),
                                     content.getContentData(),
                                     urlBuilder);
        }
        return holder;
    }

    private ContentManager getContentManager(final ControllerContext context) {
        ContentManager cm;
        try {
            CmClient cmClient = context.getApplication().getPreferredApplicationComponent(CmClient.class);
            cm = cmClient.getContentManager();
        } catch (IllegalApplicationStateException e) {
            throw new RuntimeException(e);
        }
        return cm;
    }

    public class ImageHolder {
        private final ContentVersionId imageId;
        private final ImageGalleryImageBean data;
        private final ImageServiceUrlBuilder urlBuilder;
        private Map<String, String> meta;

        public ImageHolder(final ContentVersionId imageId,
                           final ImageGalleryImageBean data,
                           final ImageServiceUrlBuilder urlBuilder) {
            this.imageId = imageId;
            this.data = data;
            this.urlBuilder = urlBuilder;
        }

        public ContentVersionId getImage() {
            return imageId;
        }

        public ImageGalleryImageBean getData() {
            return data;
        }

        public ImageServiceUrlBuilder getUrl() {
            return urlBuilder;
        }

        public Map<String, String> getMeta() {
            return meta;
        }

        public void setMeta(final Map<String, String> meta) {
            this.meta = meta;
        }
    }
}
