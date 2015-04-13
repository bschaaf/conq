package com.atex.gong.wiring.statistics;

import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.mapping.ContentComposer;
import com.atex.onecms.content.mapping.Context;
import com.atex.onecms.content.mapping.Request;
import com.atex.standard.article.ArticleBean;

/**
 * Composer used to map the standard ArticleBean to a ThinBean (used by variant "statistics.thin").
 */
public class ArticleThinBeanComposer implements ContentComposer<ArticleBean, ThinBean, Object> {

    @Override
    public ContentResult<ThinBean> compose(final ContentResult<ArticleBean> articleBeanContentResult,
                                           final String s,
                                           final Request request,
                                           final Context<Object> objectContext) {
        final ArticleBean article = articleBeanContentResult.getContent().getContentData();
        final ThinBean thinBean = new ThinBean();
        thinBean.setName(article.getTitle());
        thinBean.setParentId(article.getLinkPath()[0]);
        return new ContentResult<>(articleBeanContentResult, thinBean);
    }
}
