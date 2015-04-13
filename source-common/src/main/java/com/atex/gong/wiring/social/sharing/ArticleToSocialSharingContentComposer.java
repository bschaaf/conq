package com.atex.gong.wiring.social.sharing;

import java.util.List;

import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.mapping.ContentComposer;
import com.atex.onecms.content.mapping.Context;
import com.atex.onecms.content.mapping.Request;
import com.atex.plugins.social.sharing.SocialSharingInfo;
import com.atex.standard.article.ArticleBean;
import com.polopoly.cm.ContentId;

public class ArticleToSocialSharingContentComposer implements ContentComposer<ArticleBean, SocialSharingInfo, Object> {

    @Override
    public ContentResult<SocialSharingInfo> compose(final ContentResult<ArticleBean> source,
                                                    final String variant,
                                                    final Request request,
                                                    final Context<Object> context) {
        SocialSharingInfo info = new SocialSharingInfo();
        ArticleBean article = source.getContent().getContentData();
        info.setTitle(article.getTitle());
        info.setDescription(article.getLead());
        List<ContentId> articleImages = article.getImages();
        if (articleImages.size() > 0) {
            info.setImageContentId(articleImages.get(0));
        }
        info.setOpenGraphType("article");
        return new ContentResult<>(source, info);
    }
}
