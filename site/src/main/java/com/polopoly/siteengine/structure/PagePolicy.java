package com.polopoly.siteengine.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.apache.lucene.document.Document;

import com.atex.onecms.app.siteengine.PageBean;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentWrite;
import com.atex.onecms.content.LegacyContentAdapter;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.policy.CheckboxPolicy;
import com.polopoly.cm.app.policy.SingleValued;
import com.polopoly.cm.app.policy.impl.SelectableFieldPolicy;
import com.polopoly.cm.app.search.categorization.Categorization;
import com.polopoly.cm.app.search.categorization.CategorizationProvider;
import com.polopoly.cm.app.util.PreviewURLBuilder;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.ContentListAware;
import com.polopoly.cm.client.Department;
import com.polopoly.cm.client.OutputTemplate;
import com.polopoly.cm.collections.ContentIdList;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListUtil;
import com.polopoly.cm.path.PathSegment;
import com.polopoly.cm.path.SimpleContentPathTranslator;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.policy.PolicyImplBase;
import com.polopoly.cm.policymvc.PolicyModelDomain;
import com.polopoly.cm.search.index.Candidate;
import com.polopoly.cm.search.index.IndexContext;
import com.polopoly.cm.search.index.builder.DocumentBuilder;
import com.polopoly.metadata.Metadata;
import com.polopoly.metadata.MetadataAware;
import com.polopoly.metadata.legacy.LegacyCategorizationConverterInternal;
import com.polopoly.metadata.util.CategorizationToMetadata;
import com.polopoly.metadata.util.MetadataUtil;
import com.polopoly.paywall.PremiumContentAware;
import com.polopoly.paywall.PremiumContentSettings;
import com.polopoly.siteengine.field.model.IntegerSelectable;
import com.polopoly.siteengine.search.ParentHierarchyIndexer;
import com.polopoly.siteengine.search.ParentHierarchyIndexerFactory;
import com.polopoly.siteengine.util.FriendlyUrlConverter;
import com.polopoly.tools.publicapi.annotations.PublicApi;

/**
 * The page policy. Base class for all departments in a SiteEngine based site.
 * This policy resides in the Legacy Site and Page Plugin (see the online
 * documentation).
 */
@PublicApi
public class PagePolicy
        extends ContentPolicy
        implements Page, Department, PageModelTypeDescription, SourcesProvider,
        PathSegment, PreviewURLBuilder, DocumentBuilder, MetadataAware, CategorizationProvider,
        PremiumContentAware, LegacyContentAdapter<PageBean> {

    ParentHierarchyIndexerFactory parentHierarchyIndexerFactory =
        new ParentHierarchyIndexerFactory();

    private volatile ContentId[] parentIds;
    private final Object[] parentMutex = new Object[0];

    private final ParentPathResolver parentPathResolver = new ParentPathResolver();
    private SimpleContentPathTranslator contentPathTranslator;

    @Override
    protected void initSelf()
    {
        super.initSelf();
        contentPathTranslator = new SimpleContentPathTranslator();
        contentPathTranslator.setPolicyCMServer(getCMServer());
    }

    public boolean getDisallowIndexing()
    {
        try {
            CheckboxPolicy disallowIndexingPolicy =
                (CheckboxPolicy) getChildPolicy("disallowIndexing");

            if (disallowIndexingPolicy != null && disallowIndexingPolicy.getValue() != null) {
                return disallowIndexingPolicy.getChecked();
            }
        } catch (CMException cme) {
            PolicyImplBase.logger.log(Level.WARNING, "Unable to get allowIndexing field", cme);
        }

        return false;
    }

    public String getPathSegmentString() throws CMException
    {
        SingleValued pathSegmentPolicy =
                (SingleValued) getChildPolicy("pathsegment");
        if (pathSegmentPolicy != null && pathSegmentPolicy.getValue() != null) {
            return FriendlyUrlConverter.convertPermissive(pathSegmentPolicy.getValue()).toLowerCase();
        } else if (getName() != null) {
            return FriendlyUrlConverter.convertPermissive(getName()).toLowerCase();
        }
        return null;
    }

    public OutputTemplate getLayoutOutputTemplate(String mode)
            throws CMException
    {
        Policy pageLayout = getSelectedPageLayout();
        return pageLayout == null ? null : pageLayout.getOutputTemplate(mode);
    }

    /**
     * Gets the site engine <code>OutputTemplate</code> for <code>Page</code>
     * if the <code>PageLayout</code> allows rendering in the specified mode.
     *
     * <p>
     * <code>Please note that it is the site engine <code>OutputTemplate</code>
     * for <code>Page</code> that is obtained, which is used for getting hold of
     * the controller. This means that the same output template is returned
     * regardless of mode. Note also that a <code>null</code> result means that
     * the page is not possible to render in the specified mode.
     * </p>
     *
     * @param mode the mode to render in.
     * @return the site engine <code>OutputTemplate</code> if
     *     <code>LayoutOutputTemplate</code> for mode exists, <code>null</code>
     *     otherwise.
     */
    public OutputTemplate getOutputTemplate(String mode) throws CMException
    {
        // Page and site always use mode www. Lets page layout decide
        // if mode is supported.
        if (getLayoutOutputTemplate(mode) != null) {
            return super.getOutputTemplate("www");
        }
        return null;
    }

    public ContentList getFeeds()
    {
        try {
            ContentListAware feeds = (ContentListAware) getChildPolicy("feeds");
            return feeds == null ? ContentListUtil.EMPTY_CONTENT_LIST : (ContentList) feeds
                    .getContentList();
        } catch (CMException e) {
            PolicyImplBase.logger.log(Level.WARNING, "Unable to get feeds", e);
            return null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.polopoly.siteengine.structure.PageModelTypeDescription#getName()
     */
    public String getName()
    {
        try {
            return super.getName();
        } catch (CMException e) {
            PolicyImplBase.logger.log(Level.WARNING, "Unable to get content name", e);
        }
        return null;
    }

    public ContentList getStyleSheets()
    {
        try {
            return (ContentList) ((ContentListAware) getChildPolicy("css"))
                    .getContentList();
        } catch (CMException e) {
            PolicyImplBase.logger.log(Level.WARNING, "Unable to get stylesheets", e);
            return null;
        }
    }

    public ContentList getSubPages()
    {
        try {
            return (ContentList) ((ContentListAware) getChildPolicy("pages"))
                    .getContentList();
        } catch (CMException e) {
            PolicyImplBase.logger.log(Level.WARNING, "Unable to get subpages", e);
            return null;
        }
    }

    @Override
    public ContentList getSources()
    {
        try {
            return (ContentList) ((ContentListAware) getChildPolicy("sources"))
                .getContentList();
        } catch (CMException e) {
            PolicyImplBase.logger.log(Level.WARNING, "Unable to get sources", e);
            return null;
        }
    }

    /**
     * @deprecated use {@link com.polopoly.cm.app.preview.PreviewController} instead.
     */
    public String getPreviewURL(String previewServletURL)
    {
        String path = "";
        try {
            ContentId[] parentPath = parentPathResolver.getParentPath(getContentId(),
                                                                      getCMServer());
            path = contentPathTranslator.createPath(parentPath);
        }
        catch (CMException e) {
            PolicyImplBase.logger.log(Level.WARNING, "Failed to get preview url", e);
        }
        return previewServletURL + path;

    }

    public boolean populateDocument(IndexContext indexContext,
            Document document, Candidate candidate)
    {
        ParentHierarchyIndexer parentHierarchyIndexer =
                parentHierarchyIndexerFactory.createParentHierarchyIndexer(getCMServerLocal(),
                                                                           document,
                                                                           false);

        try {
            parentHierarchyIndexer.indexParentHierarchy(this);
        } catch (CMException e) {
            PolicyImplBase.logger.log(Level.WARNING, "Unable to index parents", e);
        }

        return false;
    }

    PolicyCMServer getCMServerLocal()
    {
        return getCMServer();
    }

    private MetadataAware getMetadataAware()
    {
        return MetadataUtil.getMetadataAware(this);
    }

    @Override
    public Metadata getMetadata() {
        return getMetadataAware().getMetadata();
    }

    @Override
    public void setMetadata(Metadata metadata) {
        getMetadataAware().setMetadata(metadata);
    }

    @Override
    public Categorization getCategorization() throws CMException
    {
        return new LegacyCategorizationConverterInternal().getLegacyCategorization(getMetadata());
    }

    @Override
    public void setCategorization(Categorization categorization) throws CMException {
        setMetadata(CategorizationToMetadata.convert(categorization));
    }

    /**
     * Returns the content IDs of all the parents for this page.
     *
     * @return content IDs
     */
    public ContentId[] getParentIds() {
        if (parentIds == null) {
            synchronized(parentMutex) {
               if (parentIds == null) {
                   ArrayList<ContentId> result = new ArrayList<ContentId>();
                   try {
                       List<ContentId> parents = parentPathResolver
                           .getParentPathAsList(this, getCMServer());
                       for (ContentId cid : parents) {
                           if (!cid.equalsIgnoreVersion(getContentId())) {
                               result.add(cid);
                           }
                       }
                   }
                   catch (CMException e) {
                       PolicyImplBase.logger.log(Level.WARNING, "Unable to get parents", e);
                   }
                   parentIds = result.toArray(new ContentId[result.size()]);
               }
            }
        }
        return parentIds;
    }

    public long getSelectedCacheTime() throws CMException
    {
        return ((IntegerSelectable) getChildPolicy("cacheTime/cacheTime")).getSelectedValue();
    }

    public Policy getSelectedPageLayout() throws CMException {
        return ((SelectableFieldPolicy) getChildPolicy("pageLayout")).getSelectedSubField();
    }

    @Override
    public PremiumContentSettings getPremiumSettings() throws CMException {
        return (PremiumContentSettings) getChildPolicy("premiumContent");
    }

    /**
     * <strong>For internal use only.</strong>
     *
     * Converts and returns this policy as an internal bean representation.
     */
    @Override
    public ContentResult<PageBean> legacyToNew(final PolicyModelDomain policyModelDomain)
        throws CMException
    {
        PageBean pageBean = new PageBean();
        Content content = getContent();

        pageBean.setName(getName());
        pageBean.setFeeds(new ContentIdList(content, getFeeds().getContentListStorageGroup()));
        pageBean.setSubPages(new ContentIdList(content, getSubPages().getContentListStorageGroup()));
        pageBean.setStylesheets(new ContentIdList(content, getStyleSheets().getContentListStorageGroup()));

        return new ContentResult<>(pageBean, new HashMap<String, Object>());
    }

    /**
     * <strong>NOT IMPLEMENTED.</strong>
     */
    @Override
    public void newToLegacy(final ContentWrite<PageBean> dataResult)
        throws CMException
    {
        throw new UnsupportedOperationException("Write is not implemented");
    }
}
