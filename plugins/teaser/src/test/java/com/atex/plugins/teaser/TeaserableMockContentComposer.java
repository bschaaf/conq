package com.atex.plugins.teaser;

import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.mapping.ContentComposer;
import com.atex.onecms.content.mapping.Context;
import com.atex.onecms.content.mapping.Request;
import com.polopoly.cm.ContentId;
import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;

import java.util.ArrayList;
import java.util.List;

public class TeaserableMockContentComposer implements ContentComposer<Model, TeaserableBean, Object> {

    @Override
    public ContentResult<TeaserableBean> compose(final ContentResult<Model> input,
                                                 final String variant,
                                                 final Request request,
                                                 final Context<Object> context) {
        Model articleModel = input.getContent().getContentData();
        TeaserableBean teaserBean = new TeaserableBean();
        teaserBean.setName(ModelPathUtil.get(articleModel, "name", String.class));
        teaserBean.setText(ModelPathUtil.get(articleModel, "text/value", String.class));
        teaserBean.setImageContentId(ModelPathUtil.get(articleModel, "images/list[0]/contentId", ContentId.class));
        Teaserable.Attribute attribute = new Teaserable.Attribute("my-attribute-css-class", "My attribute value");
        List<Teaserable.Attribute> attributes = new ArrayList<>(1);
        attributes.add(attribute);
        teaserBean.setAttributes(attributes);
        teaserBean.setLinkPath(ModelPathUtil.get(articleModel, "parentIds", ContentId[].class));
        return new ContentResult<>(input, teaserBean);
    }

}
