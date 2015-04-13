package it.conquiste.cm.standard.article;

import it.conquiste.cm.utils.CategorizationHelper;

import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.app.policy.DateTimePolicy;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.app.policy.TimeStatePolicy;
import com.polopoly.cm.app.search.categorization.Categorization;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CMRuntimeException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListUtil;
import com.polopoly.metadata.Metadata;
import com.polopoly.metadata.legacy.LegacyCategorizationConverter;
import com.polopoly.unified.content.ArticleAware_v1;
import com.polopoly.unified.content.Article_v1;
import com.polopoly.unified.content.ImageReference_v1;
import com.polopoly.unified.content.RepresentationApplicationFailedException;
import com.polopoly.unified.content.RepresentationFactory;
import com.polopoly.unified.content.RepresentationNotCompatibleException;
import com.polopoly.unified.content.RepresentationNotFoundException;

/**
 * ArticleAwareAdapter.
 *
 * @author mnova
 */
public class ArticleAwareAdapter<T extends ArticlePolicy> implements ArticleAware_v1 {

    private static final String BYLINE = "byline";
    private static final String EDITED_DATE = "editedDate";
    private static final String PUBLISHED_DATE = "publishedDate";
    private static final String EDITORIAL_NOTES = "editorialNotes";
    private static final String CUSTOM_ATTRIBUTES = "customAttributes";

    private final T policy;

    public ArticleAwareAdapter(final T policy) {
        this.policy = policy;
    }

    @Override
    public void applyRepresentation(final Article_v1 article)
            throws RepresentationApplicationFailedException, RuntimeException {

        try {
            validateImages(article.getImages());

            policy.setName(article.getTitle());
            policy.setLead(article.getLead());

            policy.setTextBody(article.getBody());
            ((SingleValuePolicy) policy.getChildPolicy(BYLINE)).setValue(article.getByline());
            policy.setComponent(EDITORIAL_NOTES, "value", article.getEditorialNotes());

            DateTimePolicy modifiedDate = (DateTimePolicy) policy.getChildPolicy(EDITED_DATE);
            DateTimePolicy publishedDate = (DateTimePolicy) policy.getChildPolicy(PUBLISHED_DATE);

            // Ignoring article.getCreatedDate()

            if (article.getModifiedDate() != null) {
                modifiedDate.setTime(article.getModifiedDate());
            }

            if (article.getPublishedDate() != null) {
            	TimeStatePolicy timeStatePolicy = (TimeStatePolicy) policy.getChildPolicy("timeState");
            	timeStatePolicy.setOnTime(article.getPublishedDate());
                publishedDate.setTime(article.getPublishedDate());
            } else {
            	publishedDate.setTime(new Date());
            }

            ContentListUtil.clear(policy.getContentList("images"));
            for (ImageReference_v1 image : article.getImages()) {
                addImageToList(policy.getContentList("images"), image);
            }

            Categorization newCategorization = article.getCategorization();
            if (newCategorization != null) {
                policy.setMetadata(CategorizationHelper.updateMetadata(
                        CategorizationHelper.convert(newCategorization),
                        policy.getMetadata()));
            }

            setCustomAttributes(article.getCustomAttributes());
        } catch (CMException e) {
            throw new CMRuntimeException(e);
        }

    }

    @Override
    public Article_v1 getRepresentation(final Article_v1 article, final RepresentationFactory factory)
            throws RepresentationApplicationFailedException, RuntimeException {

        try {
            article.setTitle(policy.getName());
            article.setLead(policy.getLead());
            article.setBody(policy.getTextBody());
            article.setByline(policy.getChildValue(BYLINE, null));
            article.setCreatedDate(new Date(policy.getContentCreationTime()));
            article.setPublishedDate(new Date(policy.getPublishingDateTime()));
            article.setModifiedDate(getEditedDate());
            article.setEditorialNotes(getEditorialNotes());

            final Metadata metadata = policy.getMetadata();
            if (metadata != null) {
                article.setCategorization(new LegacyCategorizationConverter().getLegacyCategorization(metadata));
            }

            article.setCustomAttributes(getCustomAttributes());
            article.setImages(null);
            final ListIterator<ContentReference> iterator = policy.getContentList("images").getListIterator();
            while (iterator.hasNext()) {
                ImageReference_v1 imageref = factory.createEmptyRepresentation(ImageReference_v1.class);
                imageref.setId(iterator.next().getReferredContentId());
                article.getImages().add(imageref);
            }

            return article;
        } catch (CMException e) {
            throw new CMRuntimeException(e);
        }

    }

    private String getEditorialNotes() throws CMException {
        return policy.getComponent(EDITORIAL_NOTES, "value");
    }

    private Map<String, String> getCustomAttributes() throws CMException {
        String json = policy.getComponent(CUSTOM_ATTRIBUTES, "value");
        if (json == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, String> map = new Gson().fromJson(json, Map.class);
        return map;
    }

    private void setCustomAttributes(final Map<String, String> attributes) throws CMException {
        if (attributes == null) {
            policy.setComponent(CUSTOM_ATTRIBUTES, "value", null);
        } else {
            policy.setComponent(CUSTOM_ATTRIBUTES, "value", new Gson().toJson(attributes));
        }
    }

    private Date getEditedDate() throws CMException {
        return ((DateTimePolicy) policy.getChildPolicy(EDITED_DATE)).getDate();
    }

    private void validateImages(final List<ImageReference_v1> images) {
        for (ImageReference_v1 image : images) {
            try {
                image.getImage();
            } catch (RepresentationNotCompatibleException | RepresentationNotFoundException e) {
                throw new RepresentationApplicationFailedException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            }
        }
    }

    private void addImageToList(final ContentList contentList, final ImageReference_v1 imageref)
            throws CMException {

        contentList.add(contentList.size(), new ContentReference(imageref.getId(), null));
    }
}
