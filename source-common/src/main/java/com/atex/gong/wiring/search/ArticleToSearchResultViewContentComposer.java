package com.atex.gong.wiring.search;

import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.mapping.ContentComposer;
import com.atex.onecms.content.mapping.Context;
import com.atex.onecms.content.mapping.Request;
import com.atex.plugins.search.SearchResultViewBean;
import com.atex.standard.article.ArticleBean;
import com.polopoly.cm.ContentId;

import java.util.List;

/**
 * {@link #ContentComposer} for search results view variant of standard.Article.
 */
public class ArticleToSearchResultViewContentComposer implements
        ContentComposer<ArticleBean, SearchResultViewBean, Object> {

    @Override
    public ContentResult<SearchResultViewBean> compose(final ContentResult<ArticleBean> source, final String variant,
                                                       final Request request, final Context<Object> context) {
        final ArticleBean article = source.getContent().getContentData();
        final SearchResultViewBean resultViewBean = new SearchResultViewBean();
        if (source.getContent().getId() != null) {
            resultViewBean.setContentId(IdUtil.toPolicyContentId(source.getContent().getId().getContentId()));
        }
        resultViewBean.setName(article.getTitle());
        resultViewBean.setText(article.getLead());
        resultViewBean.setLinkPath(article.getLinkPath());
        List<ContentId> images = article.getImages();
        resultViewBean.setImageContentId((images != null && images.size() > 0) ? images.get(0) : null);
        return new ContentResult<>(source, resultViewBean);
    }

}
