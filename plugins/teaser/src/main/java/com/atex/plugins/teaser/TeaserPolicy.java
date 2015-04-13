package com.atex.plugins.teaser;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.Subject;
import com.atex.onecms.content.repository.StorageException;
import com.atex.plugins.baseline.policy.BaselinePolicy;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;

public class TeaserPolicy extends BaselinePolicy implements Teaserable, Teaser {

    public static final ExternalContentId TEASER_INPUT_TEMPLATE_ID =
            new ExternalContentId("com.atex.plugins.teaser.Teaser");
    private static final String TEASERABLE_LIST = "teaserables";
    private static final String NAME = "name";
    private static final String TEXT = "text";
    private static final String IMAGE_LIST = "images";
    private ContentManager contentManager;

    public TeaserPolicy(final ContentManager contentManager) {
        this.contentManager = contentManager;
    }

    @Override
    public String getName() {
        String name = getChildPolicyValue(NAME);
        name = (empty(name) ? getNameFromTeaserable() : name);
        return name;
    }

    @Override
    public String getText() {
        String text = getChildPolicyValue(TEXT);
        text = (empty(text) ? getTextFromTeaserable() : text);
        return text;
    }


    private String getNameFromTeaserable() {
        return getTeaserable().getName();
    }

    private String getTextFromTeaserable() {
        return getTeaserable().getText();
    }

    private String getChildPolicyValue(final String childPolicyName) {
        String value = "";
        try {
            value = ((SingleValuePolicy) getChildPolicy(childPolicyName)).getValue();
        } catch (CMException exception) {
            logger.log(Level.SEVERE, "Couldn't read child policy with name '" + childPolicyName + "'", exception);
        }
        value = (value != null ? value : "");
        return value;
    }

    /**
     * Fetches referred content to teaser using content manager, content must have mapper for
     * "com.atex.plugins.teaser.teaserable" variant.
     *
     * @return {@link #Teaser} representing model for variant "com.atex.plugins.teaser.teaserable" or empty
     * representation if none found.
     */
    private Teaserable getTeaserable() {
        ContentId teaserableId = getTeaserableContentId();
        Teaserable teaserable = new TeaserableBean();
        if (teaserableId != null) {
            try {
                ContentResult<Teaserable> dataResult = contentManager.get(
                        contentManager.resolve(IdUtil.fromPolicyContentId(teaserableId), Subject.NOBODY_CALLER),
                        "com.atex.plugins.teaser.teaserable",
                        Teaserable.class,
                        Collections.<String, Object>emptyMap(),
                        Subject.NOBODY_CALLER);
                if (dataResult.getStatus().isOk()) {
                    teaserable = dataResult.getContent().getContentData();
                }
            } catch (ClassCastException | StorageException e) {
                logger.log(Level.SEVERE, "Unable to fetch teaserable content using id: "
                        + teaserableId.getContentId().getContentIdString(), e);
            }
        }
        return teaserable;
    }

    public void setTeaserableId(final ContentId articleId) throws CMException {
        ContentId unversionedId = articleId.getContentId();
        ContentId currentArticleId = getTeaserableContentId();
        if (currentArticleId != null) {
            if (currentArticleId.equals(unversionedId)) {
                // Already done
                return;
            }
            getContentList(TEASERABLE_LIST).remove(0);
        }
        getContentList(TEASERABLE_LIST).add(0, new ContentReference(unversionedId, null));
    }

    @Override
    public ContentId getTeaserableContentId() {
        ContentId teaserableId = null;
        try {
            ContentList list = getContentList(TEASERABLE_LIST);
            if (list != null && list.size() > 0) {
                teaserableId = list.getEntry(0).getReferredContentId();
            }
        } catch (CMException exception) {
            logger.log(Level.SEVERE, "Couldn't get teaserable content id for teaser: " + getContentId(), exception);
        }
        return teaserableId;
    }

    /**
     * Fetches image to render from list or from referenced teaserable.
     *
     * @return ContentId of image or null if none found.
     */
    @Override
    public ContentId getImageContentId() {
        ContentId imageId = null;
        try {
            ContentList list = getContentList(IMAGE_LIST);
            imageId = (list != null && list.size() > 0) ? list.getEntry(0).getReferredContentId()
                    : getImageIdFromTeaserable();
        } catch (CMException exception) {
            logger.log(Level.SEVERE, "Couldn't get image id for teaser: " + getContentId(), exception);
        }
        return imageId;
    }

    private ContentId getImageIdFromTeaserable() {
        return getTeaserable().getImageContentId();
    }

    private boolean empty(final String value) {
        return (value == null || "".equals(value.trim()));
    }

    @Override
    public ContentId[] getLinkPath() {
        return getTeaserable().getLinkPath();
    }

    @Override
    public List<Attribute> getAttributes() {
        return getTeaserable().getAttributes();
    }
}
