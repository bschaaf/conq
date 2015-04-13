package com.atex.standard.article;

import com.google.inject.Inject;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.policy.DateTimePolicy;
import com.polopoly.cm.app.policy.SingleValued;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.testnj.TestNJRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(TestNJRunner.class)
public class StandardArticlePolicyTestIT {
    @Inject
    private PolicyCMServer policyCMServer;

    @Test
    public void publishedDateFallsBackToCreateTime() throws Exception {
        ArticlePolicy article = (ArticlePolicy) policyCMServer.createContent(1,
                new ExternalContentId("standard.Article"));
        article.commit();
        assertEquals(article.getContent().getContentCreationTime(), article.getPublishingDateTime());
    }

    @Test
    public void publishedDateIsSettable() throws Exception {
        ArticlePolicy article = (ArticlePolicy) policyCMServer.createContent(1,
                new ExternalContentId("standard.Article"));
        DateTimePolicy publishedDate = (DateTimePolicy) article.getChildPolicy("publishedDate");
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2014, 02, 12);
        publishedDate.setTime(cal.getTime());
        article.commit();
        assertEquals(cal.getTimeInMillis(), article.getPublishingDateTime());
    }

    @Test
    public void hasTextRepresentation() throws Exception {
        ArticlePolicy article = (ArticlePolicy) policyCMServer.createContent(1,
                new ExternalContentId("standard.Article"));
        long now = System.currentTimeMillis();
        String body = "Body: " + now;
        ((SingleValued) article.getChildPolicy("body")).setValue(body);
        String lead = "Lead: " + now + 1;
        ((SingleValued) article.getChildPolicy("lead")).setValue(lead);
        String title = "Title: " + now + 2;
        article.setName(title);

        String textRepresentation = article.getTextRepresentation();
        assertTrue("Body missing from text representation", textRepresentation.contains(body));
        assertTrue("Lead missing from text representation", textRepresentation.contains(lead));
        assertTrue("Title missing from text representation", textRepresentation.contains(title));
    }

    @Test
    public void worksWithNoReferredImage() throws Exception {
        ArticlePolicy article = (ArticlePolicy) policyCMServer.createContent(1,
                new ExternalContentId("standard.Article"));
        assertEquals(null, article.getReferredImageId());
    }

    @Test
    public void getsFirstImageAsReferredImage() throws Exception {
        Policy image = policyCMServer.createContent(1, new ExternalContentId("standard.Image"));
        policyCMServer.commitContents(new Policy[]{image});
        ArticlePolicy article = (ArticlePolicy) policyCMServer.createContent(1,
                new ExternalContentId("standard.Article"));
        article.setReferredImageId(image.getContentId());
        article.commit();
        ArticlePolicy committedArticle = (ArticlePolicy) policyCMServer.getPolicy(article.getContentId());
        assertEquals(image.getContentId().getContentId(), committedArticle.getReferredImageId());
    }

}
