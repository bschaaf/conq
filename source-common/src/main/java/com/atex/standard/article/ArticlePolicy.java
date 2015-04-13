package com.atex.standard.article;

import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentWrite;
import com.atex.onecms.content.LegacyContentAdapter;
import com.atex.onecms.content.aspects.Aspect;
import com.atex.onecms.content.metadata.MetadataInfo;
import com.atex.plugins.baseline.policy.BaselinePolicy;
import com.atex.plugins.teaser.TeaserPolicy;
import com.atex.standard.model.MetadataModelProvider;
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
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.policymvc.PolicyModelDomain;
import com.polopoly.metadata.Metadata;
import com.polopoly.metadata.MetadataAware;
import com.polopoly.metadata.util.MetadataUtil;
import com.polopoly.model.DescribesModelType;
import com.polopoly.paywall.PremiumContentAware;
import com.polopoly.paywall.PremiumContentSettings;
import com.polopoly.textmining.TextRepresentation;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

@DescribesModelType
public class ArticlePolicy extends BaselinePolicy implements PublishingDateTime,
                                                                LegacyContentAdapter<ArticleBean>,
                                                                MetadataAware,
                                                                MetadataModelProvider,
                                                                TextRepresentation,
                                                                PremiumContentAware,
                                                                ContentListInsertionHook {

    private static final String NAME = "headline";
    private static final String LEAD = "lead";
    private static final String BODY = "body";
    private static final String IMAGE_LIST = "images";
    private static final String PUBLISHED_DATE = "publishedDate";
    private static final String ALLOW_COMMENTS = "allowComments";

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
        int layoutElementMajor = cmServer.getMajorByName(DefaultMajorNames.LAYOUTELEMENT);
        TeaserPolicy teaser = (TeaserPolicy)
            cmServer.createContent(layoutElementMajor, contentToInsertInto, TeaserPolicy.TEASER_INPUT_TEMPLATE_ID);
        teaser.setTeaserableId(getContentId().getContentId());
        cmServer.commitContent(teaser);
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

    @Override
    public Metadata getMetadata() {
        return getMetadataAware().getMetadata();
    }

    @Override
    public void setMetadata(final Metadata metadata) {
        getMetadataAware().setMetadata(metadata);
    }
}
