package it.conquiste.cm.standard.article;

import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentWrite;
import com.atex.onecms.content.LegacyContentAdapter;
import com.atex.onecms.content.aspects.Aspect;
import com.atex.onecms.content.metadata.MetadataInfo;
import com.atex.plugins.baseline.policy.BaselinePolicy;
import com.atex.plugins.rssfeed.Rssable;

import it.conquiste.cm.teaser.TeaserPolicy;
import it.conquiste.cm.standard.article.ArticleBean;

import com.atex.standard.model.MetadataModelProvider;
import com.google.gson.Gson;
import com.polopoly.application.Application;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.DefaultMajorNames;
import com.polopoly.cm.PublishingDateTime;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.inbox.AssociatedUsersPolicy;
import com.polopoly.cm.app.inbox.InboxFlags;
import com.polopoly.cm.app.policy.BooleanValuePolicy;
import com.polopoly.cm.app.policy.ContentListInsertionHook;
import com.polopoly.cm.app.policy.DateTimePolicy;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.app.policy.SingleValued;
import com.polopoly.cm.app.policy.TimeStatePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.policymvc.PolicyModelDomain;
import com.polopoly.metadata.Metadata;
import com.polopoly.metadata.MetadataAware;
import com.polopoly.metadata.util.MetadataUtil;
import com.polopoly.model.DescribesModelType;
import com.polopoly.paywall.PremiumContentAware;
import com.polopoly.paywall.PremiumContentSettings;
import com.polopoly.search.solr.SearchClient;
import com.polopoly.siteengine.structure.PagePolicy;
import com.polopoly.textmining.TextRepresentation;
import com.polopoly.unified.content.ArticleAware_v1;
import com.polopoly.unified.content.Article_v1;
import com.polopoly.unified.content.RepresentationApplicationFailedException;
import com.polopoly.unified.content.RepresentationFactory;

import it.conquiste.cm.utils.HermesUtil;
import it.conquiste.cm.workflow.WorkflowVisibleStatus;
import it.conquiste.cm.utils.PolicyHelper;
import it.conquiste.cm.utils.PolopolyUtil;
import it.conquiste.cm.util.slot.SlotUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;


@DescribesModelType
public class ArticlePolicy extends BaselinePolicy implements PublishingDateTime,
LegacyContentAdapter<ArticleBean>,
MetadataAware,
MetadataModelProvider,
TextRepresentation,
PremiumContentAware,
ContentListInsertionHook,
WorkflowVisibleStatus,
ArticleAware_v1,
Rssable {

	private static final String NAME = "headline";
	private static final String LEAD = "lead";
	private static final String OVERTITLE = "overtitle";
	private static final String BODY = "body";
	private static final String IMAGE_LIST = "images";
	private static final String PUBLISHED_DATE = "publishedDate";
	private static final String ALLOW_COMMENTS = "allowComments";
	private static final String HOME_PAGE_TEASER_POS_KEY = "position";
	private static final String PQ_KEY = "pq";
	private static final String PQ = "Rullo";
	private static final String ARTICLES_SLOT = "first";
	private static final String CUSTOM_ATTRIBUTES = "customAttributes";

	private final ArticleAwareAdapter articleAwareAdapter = new ArticleAwareAdapter(this);
	private final Application application;
	private final PolicyHelper policyHelper;

	public ArticlePolicy(final Application application, final CmClient cmClient) {   
		this.application = application;
		this.policyHelper = new PolicyHelper(this);
	}

	@Override
	public String getName() throws CMException {
		String name = null;
		try {
			name = ((SingleValuePolicy) getChildPolicy(NAME)).getValue();
		} catch (CMException e) {
			logger.log(Level.SEVERE, "Couldn't read child policy with name '" + NAME + "'", e);
		}
		return name == null ? super.getName() : name;
	}
	
	/**
	 * @return value of overtitle field for article, null if not present.
	 */
	public String getOvertitle() {
		String lead = null;
		try {
			lead = ((SingleValuePolicy) getChildPolicy(OVERTITLE)).getValue();
		} catch (CMException e) {
			logger.log(Level.SEVERE, "Couldn't read child policy with name '" + OVERTITLE + "'", e);
		}
		return lead;
	}

	/**
	 * @return value of lead field for article, null if not present.
	 */
	public String getLead() {
		String lead = null;
		try {
			lead = ((SingleValuePolicy) getChildPolicy(LEAD)).getValue();
		} catch (CMException e) {
			logger.log(Level.SEVERE, "Couldn't read child policy with name '" + LEAD + "'", e);
		}
		return lead;
	}

	public List<String> getAssociatedUsers()
			throws CMException {
		return ((AssociatedUsersPolicy) getChildPolicy("associatedUsers")).getAssociatedUsers();
	}

	public void setLead(final String lead) {
		try {
			((SingleValuePolicy) getChildPolicy(LEAD)).setValue(lead);
		} catch (CMException e) {
			logger.log(Level.SEVERE, "Couldn't read child policy with name '" + LEAD + "'", e);
		}
	}
	
	public void setOvertitle(final String overtitle) {
		try {
			((SingleValuePolicy) getChildPolicy(OVERTITLE)).setValue(overtitle);
		} catch (CMException e) {
			logger.log(Level.SEVERE, "Couldn't read child policy with name '" + OVERTITLE + "'", e);
		}
	}

	public ContentId getReferredImageId() {
		ContentId imageId = null;
		try {
			ContentList images = getContentList(IMAGE_LIST);
			if (images.size() > 0) {
				imageId = images.getEntry(0).getReferredContentId();
			}
		} catch (CMException e) {
			logger.log(Level.SEVERE, "Couldn't read child policy with name '" + IMAGE_LIST + "'", e);
		}
		return imageId;
	}

	public void setReferredImageId(final ContentId contentId) {
		try {
			ContentList images = getContentList(IMAGE_LIST);
			images.add(0, new ContentReference(contentId.getContentId()));
		} catch (CMException e) {
			logger.log(Level.SEVERE, "Couldn't read child policy with name '" + IMAGE_LIST + "'", e);
		}

	}

	@Override
	public long getPublishingDateTime() {
		long publishingDateTime = getContentCreationTime();
		try {
			DateTimePolicy policy = (DateTimePolicy) getChildPolicy(PUBLISHED_DATE);
			if (policy != null && policy.hasValueSet()) {
				publishingDateTime = policy.getTimeMillis();
			}
		} catch (CMException e) {
			logger.log(Level.SEVERE, "Couldn't read child policy with name '" + PUBLISHED_DATE + "'", e);
		}
		return publishingDateTime;
	}

	public boolean getAllowComments() {
		boolean showComments = false;
		try {
			showComments = ((BooleanValuePolicy) getChildPolicy(ALLOW_COMMENTS)).getBooleanValue();
		} catch (CMException e) {
			logger.log(Level.SEVERE, "Couldn't read child policy with name '" + ALLOW_COMMENTS + "'", e);
		}
		return showComments;
	}

	@Override
	public PremiumContentSettings getPremiumSettings() throws CMException {
		return (PremiumContentSettings) getChildPolicy("premiumContent");
	}

	public String getTextRepresentation() {
		StringBuilder representation = new StringBuilder();
		representation.append(getChildValue(NAME, "")).append("\n\n");
		representation.append(getChildValue(LEAD, "")).append("\n\n");
		representation.append(getChildValue(BODY, ""));
		return representation.toString();
	}

	@Override
	public ContentResult<ArticleBean> legacyToNew(final PolicyModelDomain modelDomain) throws CMException {
		ArticleBean bean = new ArticleBean();
		bean.setTitle(getName());
		bean.setLead(getLead());
		bean.setBody(((SingleValued) getChildPolicy(BODY)).getValue());
		bean.setImages(com.atex.standard.util.ContentListUtil.toList(getContentList(IMAGE_LIST)));
		bean.setPublishingTime(getPublishingDateTime());
		bean.setPremiumContent(getPremiumSettings().isPremiumContent());
		ContentId[] legacyParents = getParentIds();
		bean.setLinkPath(legacyParents);

		return new ContentResult<>(
				null,
				ArticleBean.class.getName(),
				new Aspect<>(ArticleBean.class.getName(), bean),
				null,
				null,
				Arrays.<Aspect>asList(new Aspect<>("atex.Metadata", getMetadataInfo())));
	}

	@Override
	public void newToLegacy(final ContentWrite<ArticleBean> dataWrite) throws CMException {
		ArticleBean bean = dataWrite.getContentData();
		setName(bean.getTitle());
		setLead(bean.getLead());
		((SingleValued) getChildPolicy(BODY)).setValue(bean.getBody());
		com.atex.standard.util.ContentListUtil.setContentIdList(getContentList(IMAGE_LIST), bean.getImages());
		MetadataInfo metadataInfo = dataWrite.getAspect("atex.Metadata", MetadataInfo.class);
		if (metadataInfo != null) {
			setMetadata(metadataInfo.getMetadata());
		}
	}

	@Override
	public void postCreateSelf() throws CMException {
		// All articles should be in the Inbox by default.
		// If integrating with e.g. a print system,
		// you might want to set this only on articles arriving from the print system.
		new InboxFlags().setShowInInbox(this, true);
	}

	@Override
	public ContentId onInsert(final VersionedContentId contentToInsertInto,
			final String contentListName,
			final int index) throws CMException {
		PolicyCMServer cmServer = getCMServer();
		
		TeaserPolicy teaser = SlotUtil.createTeaser(cmServer, contentToInsertInto, getContentId());
		return teaser.getContentId().getContentId();
	}

	private MetadataAware getMetadataAware() {
		return MetadataUtil.getMetadataAware(this);
	}

	private MetadataInfo getMetadataInfo() {
		MetadataInfo info = new MetadataInfo();

		info.setMetadata(getMetadata());
		info.setTaxonomyIds(MetadataUtil.getTaxonomyIds(this));

		return info;
	}

	public String getTextBody() {
		return policyHelper.getChildValue(BODY, null);
	}

	public void setTextBody(final String value) throws CMException {
		policyHelper.setChildValue(BODY, value);
	}

	@Override
	public Metadata getMetadata() {
		return getMetadataAware().getMetadata();
	}

	@Override
	public void setMetadata(final Metadata metadata) {
		getMetadataAware().setMetadata(metadata);
	}

	@Override
	public void applyRepresentation(Article_v1 article)
			throws RepresentationApplicationFailedException, RuntimeException {
		articleAwareAdapter.applyRepresentation(article);
		
		Map<String,String> customAttributes = article.getCustomAttributes();
		
		try {
			new HermesUtil().applyCustomFields(this, customAttributes, getCMServer(), (SearchClient) application.getApplicationComponent("search_solrClientInternal"));
		} catch (CMException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		
		if(customAttributes !=null && 
				customAttributes.containsKey(PQ_KEY) && customAttributes.containsKey(HOME_PAGE_TEASER_POS_KEY) )
			if (customAttributes.get(PQ_KEY).equals(PQ)){
				int position = 0;
				final String homePos = customAttributes.get(HOME_PAGE_TEASER_POS_KEY);
				if (!StringUtils.isEmpty(homePos)) {
					try {
						position = Integer.valueOf(homePos) - 1;
					}   catch (Exception e) {
						logger.warning("Could not read teaser position on page." + e);
					}
					try {
						publishTeaser(position);
					} catch (CMException e) {
						logger.warning("Could not publish teaser page." + e);
					}
				}
			}
	}

	@Override
	public Article_v1 getRepresentation(Article_v1 article,
			RepresentationFactory factory)
					throws RepresentationApplicationFailedException, RuntimeException {
		return articleAwareAdapter.getRepresentation(article, factory);
	}


	private Map<String, String> getCustomAttributes() throws CMException {
		String json = this.getComponent(CUSTOM_ATTRIBUTES, "value");
		if (json == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		Map<String, String> map = new Gson().fromJson(json, Map.class);
		return map;
	}

	protected void publishTeaser(int position) throws CMException {
		if (getSecurityParentId() != null) {
			Policy pagePolicy = PolopolyUtil.getSite(getCMServer(), getSecurityParentId());
			if (pagePolicy instanceof PagePolicy) {
				SlotUtil.addArticleTeaserToSlot(getCMServer(), ARTICLES_SLOT, position, pagePolicy.getContentId().getContentId(), getContentId());
			}
		}
	}
	
	public boolean isVisible() throws CMException {
        // first test if the target policy has an on-time
        TimeStatePolicy timeStatePolicy = (TimeStatePolicy) this.getChildPolicy("timeState");
        if (timeStatePolicy != null && !isVisible(timeStatePolicy)) {
            return false;
        }

        return true;
    }

    public boolean hasVisible() {
        try {
            //check wheather or not it has default stage version
            getCMServer().getPolicy(this.getContentId().getContentId().getDefaultStageVersionId());
            TimeStatePolicy timeStatePolicy = (TimeStatePolicy) this.getChildPolicy("timeState");
            if (timeStatePolicy != null && !isVisible(timeStatePolicy)) {
                return false;
            }
            return true;
        } catch (CMException e) {
            return false;
        }
    }

    public static boolean isVisible(final TimeStatePolicy timeStatePolicy) throws CMException {
        Date onTime = timeStatePolicy.getOnTime();
        Date offTime = timeStatePolicy.getOffTime();
        Date now = new Date();
        return (onTime == null || now.after(onTime)) && (offTime == null || now.before(offTime));
    }

    @Override
    public Date getItemPublishedDate() {
        long timeMillisec = getPublishingDateTime();
        return new Date(timeMillisec);
    }

    @Override
    public String getItemTitle() {
        try {
            return getName();
        } catch (CMException e) {
            logger.log(Level.SEVERE, "An error occurred in getItemTitle()", e);
        }
        return null;
    }

    @Override
    public String getItemDescription() {
        return getLead();
    }

    @Override
    public ContentId getItemContentId() {
        return getContentId().getContentId();
    }

    @Override
    public ContentId[] getItemParentIds() {
        try {
            return getParentIds();
        } catch (CMException e) {
            logger.log(Level.SEVERE, "An error occurred in getItemParentIds()", e);
        }
        return null;
    }
}
