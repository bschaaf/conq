package com.atex.gong.wiring.gallery;

import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.aspects.Aspect;
import com.atex.onecms.content.mapping.ContentComposer;
import com.atex.onecms.content.mapping.Context;
import com.atex.onecms.content.mapping.Request;
import com.atex.onecms.image.ImageEditInfoAspectBean;
import com.atex.onecms.image.ImageInfoAspectBean;
import com.atex.plugins.imagegallery.util.ImageGalleryImageBean;
import com.atex.standard.image.ImageContentDataBean;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.Collection;

public class ImageToGalleryContentComposer
    implements ContentComposer<ImageContentDataBean, ImageGalleryImageBean, Object>
{
    @Override
    public ContentResult<ImageGalleryImageBean> compose(final ContentResult<ImageContentDataBean> orig,
                                                        final String s, final Request request,
                                                        final Context<Object> objectContext)
    {
        String caption = orig.getContent().getContentData().getDescription();
        if (caption == null) {
            caption = orig.getContent().getContentData().getTitle();
        }
        return new ContentResult<>(orig,
                                   new ImageGalleryImageBean(orig.getContent().getContentData().getByline(), caption),
                                   includeAspects(orig.getContent().getAspects(),
                                                  ImmutableSet.of(ImageInfoAspectBean.ASPECT_NAME,
                                                                  ImageEditInfoAspectBean.ASPECT_NAME)));
    }

    static Collection<Aspect> includeAspects(final Collection<Aspect> originalAspects,
                                             final ImmutableSet<String> aspectsToInclude)
    {
        ArrayList<Aspect> retained = new ArrayList<>();
        for (Aspect aspect : originalAspects) {
            if (aspectsToInclude.contains(aspect.getName())) {
                retained.add(aspect);
            }
        }

        return retained;
    }
}
