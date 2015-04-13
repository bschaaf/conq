package it.conquiste.cm.teaser;

import it.conquiste.cm.workflow.WorkflowVisibleStatus;

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
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentOperationFailedException;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.client.WorkflowAware;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.model.ModelTypeDescription;

public class TeaserPolicy extends BaselinePolicy implements Teaserable, Teaser, ModelTypeDescription {

	public static final ExternalContentId TEASER_INPUT_TEMPLATE_ID =
			new ExternalContentId("it.conquiste.teaser.Teaser");
	private static final String TEASERABLE_LIST = "teaserables";
	private static final String NAME = "name";
	private static final String TEXT = "text";
	private static final String IMAGE_LIST = "images";
	private ContentManager contentManager;

	public TeaserPolicy(final ContentManager contentManager) {
		this.contentManager = contentManager;
	}

	public String getOvertitle() {
		String overtitle = getChildPolicyValue("overtitle");
		overtitle = (empty(overtitle) ? getOvertitleFromTeaserable() : overtitle);
		return overtitle;
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

	private String getOvertitleFromTeaserable() {
		return getTeaserable().getOvertitle();
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
	 * "it.conquiste.cm.teaser.teaserable" variant.
	 *
	 * @return {@link #Teaser} representing model for variant "it.conquiste.cm.teaser.teaserable" or empty
	 * representation if none found.
	 */
	private Teaserable getTeaserable() {
		VersionedContentId teaserableId = getTeaserableContentId();
		Teaserable teaserable = new TeaserableBean();

		if (teaserableId != null) {
			try {

				ContentResult<Teaserable> dataResult = contentManager.get(
						contentManager.resolve(IdUtil.fromPolicyContentId(teaserableId), Subject.NOBODY_CALLER),
						"it.conquiste.teaser.teaserable",
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
	public void setTeaserableId(ContentId articleId) throws CMException {
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
	public VersionedContentId getTeaserableContentId() {
		VersionedContentId teaserableId = null;
		try {
			ContentList list = getContentList(TEASERABLE_LIST);
			if (list != null && list.size() > 0) {
				ContentId contentId = list.getEntry(0).getReferredContentId();
				teaserableId = new VersionedContentId(contentId.getMajor(), contentId.getMinor(),
						getCMServer().getContentInfo(contentId).getLatestCommittedVersion());
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


	@Override
	public List<ContentId> getListIdRelated() {
		return getTeaserable().getListIdRelated();
	}

	public ContentId getArticleId() throws CMException {
		ContentList list = getContentList(TEASERABLE_LIST);
		return (list != null && list.size() > 0) ? list.getEntry(0).getReferredContentId() : null;
	}

	/**
	 * Get referred article from this teaser.
	 *
	 * @return null if no article has yet been referred or the article object is
	 *         not available for some reason
	 */
	public ContentPolicy getArticle()
			throws CMException {
		ContentId articleId = getArticleId();
		if (articleId != null) {
			try {
				return (ContentPolicy) getCMServer().getPolicy(articleId.getContentId());
			} catch (ContentOperationFailedException articleNotFound) {
				logger.log(Level.FINE, "Unable to get article " + articleId
						+ " for teaser " + getContentId(), articleNotFound);
			}
		}
		return null;
	}

	public void setArticleId(ContentId articleId) throws CMException {
		ContentId unversionedId = articleId.getContentId();
		ContentId currentArticleId = getArticleId();

		if (currentArticleId != null) {

			if (currentArticleId.equals(unversionedId)) {
				// Already done
				return;
			}
			getContentList(TEASERABLE_LIST).remove(0);
		}
		getContentList(TEASERABLE_LIST).add(0, new ContentReference(unversionedId, null));
	}

	public void setOvertitle(String overtitle) throws CMException {
		((SingleValuePolicy)getChildPolicy("overtitle")).setValue(overtitle);
	}

	public void setText(String text) throws CMException {
		((SingleValuePolicy)getChildPolicy("text")).setValue(text);
	}

	public String getTeaserName() throws CMException {
		return super.getName();
	}

	/**
	 * @return true if this teaser links to a published content
	 * @throws CMException 
	 */
	public boolean isVisible() throws CMException  {
		ContentPolicy content =  getArticle();
		if (content instanceof WorkflowVisibleStatus){
			WorkflowVisibleStatus policy = (WorkflowVisibleStatus)content;
			if (policy != null) {
				return policy.isVisible();
			}
		}
		return true;
	}

	public boolean hasVisible() {
		try {
			getCMServer().getPolicy(this.getContentId().getContentId().getDefaultStageVersionId());
			ContentPolicy content =  getArticle();
			if (content instanceof WorkflowVisibleStatus){
				WorkflowVisibleStatus policy = (WorkflowVisibleStatus)content;
				if (policy != null) {
					return policy.hasVisible();
				}
			}
			return false;
		} catch (CMException e) {
			return false;
		}
	}
}
