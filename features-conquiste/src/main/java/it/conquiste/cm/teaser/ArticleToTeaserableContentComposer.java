package it.conquiste.cm.teaser;

import it.conquiste.cm.standard.article.ArticleBean;

import java.util.ArrayList;
import java.util.List;

import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.mapping.ContentComposer;
import com.atex.onecms.content.mapping.Context;
import com.atex.onecms.content.mapping.Request;
import com.polopoly.cm.ContentId;

public class ArticleToTeaserableContentComposer implements ContentComposer<ArticleBean, TeaserableBean, Object> {

  private static final String PREMIUM_LABEL_CSS_CLASS = "premium-label-tag";
  private static final String PREMIUM_LABEL_VALUE = "paywall.premium.teaser.label";

  @Override
  public ContentResult<TeaserableBean> compose(final ContentResult<ArticleBean> source,
                                               final String variant,
                                               final Request request,
                                               final Context<Object> context) {
      ArticleBean article = source.getContent().getContentData();
      TeaserableBean teaserBean = new TeaserableBean();
      if (source.getContent().getId() != null) {
          teaserBean.setContentId(IdUtil.toPolicyContentId(source.getContent().getId().getContentId()));
      }
      teaserBean.setName(article.getTitle());
      teaserBean.setText(article.getLead());
      List<ContentId> articleImages = article.getImages();
      if (articleImages.size() > 0) {
          teaserBean.setImageContentId(articleImages.get(0));
      }
      teaserBean.setLinkPath(article.getLinkPath());
      if (article.isPremiumContent()) {
          List<Teaserable.Attribute> attributes = new ArrayList<>(1);
          attributes.add(new Teaserable.Attribute(PREMIUM_LABEL_CSS_CLASS, PREMIUM_LABEL_VALUE));
          teaserBean.setAttributes(attributes);
      }
      return new ContentResult<>(source, teaserBean);
  }

}
