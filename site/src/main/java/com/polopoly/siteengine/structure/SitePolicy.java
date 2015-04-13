package com.polopoly.siteengine.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.onecms.app.siteengine.PageBean;
import com.atex.onecms.app.siteengine.SiteBean;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentWrite;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.policy.ContentTreeSelectPolicy;
import com.polopoly.cm.app.policy.SingleReference;
import com.polopoly.cm.app.policy.WebAliasEditorPolicy;
import com.polopoly.cm.app.policy.impl.DomainAliasPolicy;
import com.polopoly.cm.app.search.categorization.CategoryDimensionListProvider;
import com.polopoly.cm.app.search.categorization.dimension.CategoryDimension;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.collections.ContentIdList;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListRead;
import com.polopoly.cm.collections.impl.ContentListAdapter;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.DepartmentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyUtil;
import com.polopoly.cm.policymvc.PolicyModelDomain;
import com.polopoly.common.logging.LogUtil;
import com.polopoly.siteengine.field.properties.ComponentMapPolicy;
import com.polopoly.siteengine.localization.LocalizedStringsContentPolicy;
import com.polopoly.siteengine.resource.Resources;
import com.polopoly.siteengine.resource.ResourcesImpl;
import com.polopoly.siteengine.tree.PolicyFetcher;
import com.polopoly.siteengine.tree.PolicyFetcherFactory;
import com.polopoly.tools.publicapi.annotations.PublicApi;

/**
 * Policy that supports site aliases. This policy resides in the
 * Legacy Site and Page Plugin (see the online documentation).
 */
@PublicApi
public class SitePolicy
    extends PagePolicy
    implements Site, PageModelTypeDescription, CategoryDimensionListProvider
{

    private static final Logger LOG = LogUtil.getLog();
    private volatile Alias mainAlias;

    protected void initSelf()
    {
        super.initSelf();
    }

    public Alias getMainAlias() throws CMException
    {
        DomainAliasPolicy domainAliasPolicy = (DomainAliasPolicy) getChildPolicy("domainAlias");
        Alias alias = getMainAliasFromPolicy(domainAliasPolicy);

        return alias;
    }

    private Alias getMainAliasFromPolicy(DomainAliasPolicy domainAliasPolicy) throws CMException
    {
        Alias alias;

        if (mainAlias == null) {
            synchronized (this) {
                if (mainAlias == null) {
                    String mainAliasUrl = domainAliasPolicy.getMainAlias();

                    if (mainAliasUrl != null) {
                        mainAlias = new Alias(mainAliasUrl);
                    } else {
                        return null;
                    }
                }
            }
        }

        alias = mainAlias;
        return alias;
    }

    public Resources getResources()
    {
        try {
            ContentTreeSelectPolicy localizedStringsField = (ContentTreeSelectPolicy) getChildPolicy("localizedstrings");
            ContentId selectedId = localizedStringsField.getReference();

            // If there is a selected language, use it. Otherwise use the first
            // in localized strings list
            LocalizedStringsContentPolicy lang = null;
            if (selectedId != null) {
                lang = (LocalizedStringsContentPolicy) getCMServer().getPolicy(selectedId);
            } else {
                ContentPolicy languages = (ContentPolicy) getCMServer().getPolicy(
                        new ExternalContentId("p.siteengine.LocalizedStrings.d"));
                ContentListRead languageList = languages.getContentList();
                if (languageList.size() > 0) {
                    lang = (LocalizedStringsContentPolicy) getCMServer().getPolicy(
                            languageList.getEntry(0).getReferredContentId());
                }
            }

            if (lang != null) {
                Locale locale = null;
                // Create and add locale object from lc and possibly cc
                String languageCode = lang.getChildValue("lc");
                String countryConde = lang.getChildValue("cc");

                if (languageCode.length() > 0) {

                    if (countryConde.length() > 0) {
                        locale = new Locale(languageCode, countryConde);
                    } else {
                        locale = new Locale(languageCode);
                    }
                }

                // Create put resource strings object in global model
                ComponentMapPolicy cm = (ComponentMapPolicy) lang.getChildPolicy("localizedstrings");

                return new ResourcesImpl(locale, cm.getComponentMap());
            }
        } catch (CMException e) {
            logger.log(Level.WARNING, "Unable to get localized strings", e);
        }

        logger.log(Level.WARNING, "No localized resources found, returning empty resources.");

        return getEmptyResources();
    }

    private Resources getEmptyResources()
    {
        return new ResourcesImpl(Locale.ENGLISH, new HashMap());
    }

    /**
     * Returns a content list containing all dimensions defined for this site,
     * and all dimensions defined for any sites that are ancestors of this site.
     * The content list is read-only as it is a virtual list created from
     * content lists in multiple contents.
     *
     * @return a read only content list containing content IDs for
     * {@link CategoryDimension dimensions} and
     * {@link CategoryDimensionListProvider categorization departments}.
     */
    public ContentList getCategoryDimensions() throws CMException {
        Set<ContentId> dimensions = new LinkedHashSet<ContentId>();
        addReferredContentIdsToCollection(dimensions, getContentList("p.site.Categorization"));
        PolicyFetcher parentsFetcher = new PolicyFetcherFactory().createFetcherForAllParents(getCMServer());
        Set<Policy> parents;
        parents = getParents(parentsFetcher);
        for (Policy parent : parents) {
            if (parent instanceof CategoryDimensionListProvider &&
                !parent.getContentId().equalsIgnoreVersion(getContentId()))
            {
                ContentList categoryDimensions = ((CategoryDimensionListProvider) parent).getCategoryDimensions();
                addReferredContentIdsToCollection(dimensions, categoryDimensions);
            }
        }

        if (dimensions.isEmpty()) {
            DepartmentPolicy categorizationDepartment =
                (DepartmentPolicy) getCMServer().getPolicy(new ExternalContentId("p.StandardCategorization"));
            ContentList contentList = categorizationDepartment.getContentList();
            addReferredContentIdsToCollection(dimensions, contentList);
        }

        return new ContentListAdapter(new ArrayList(dimensions), "p.site.Categorization.virtual");
    }

    private void addReferredContentIdsToCollection(Collection dimensions,
        ContentList categoryDimensions) {
        ListIterator<ContentReference> listIterator = categoryDimensions.getListIterator();
        while (listIterator.hasNext()) {
            ContentId referredContentId = listIterator.next().getReferredContentId();
            if (referredContentId != null) {
                dimensions.add(referredContentId);
            }
        }
    }

    private Set<Policy> getParents(PolicyFetcher siteFetcher) {
        Set<Policy> sites;
        try {
            sites = siteFetcher.fetch(PolicyUtil.getTopPolicy(this));
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Couldn't get parents for " + getContentId() + ", using empty categorization", e);
            sites = new HashSet();
        }
        return sites;
    }

    @Override
    public Map getWebAliases() throws CMException
    {
        SingleReference aliasDefContent = (SingleReference)getChildPolicy("aliasdefcontent");
        ContentId referredContentId = aliasDefContent.getReference();
        if (referredContentId != null) {
            Policy referredPolicy = getCMServer().getPolicy(referredContentId);

            if(referredPolicy instanceof Site) {
                return ((Site)referredPolicy).getWebAliases();
            }
        }

        return ((WebAliasEditorPolicy) getChildPolicy("aliases")).getAllAliases();
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
        SiteBean siteBean = new SiteBean();
        Content content = getContent();

        siteBean.setName(getName());
        siteBean.setFeeds(new ContentIdList(content, getFeeds().getContentListStorageGroup()));
        siteBean.setSubPages(new ContentIdList(content, getSubPages().getContentListStorageGroup()));
        siteBean.setStylesheets(new ContentIdList(content, getStyleSheets().getContentListStorageGroup()));

        return new ContentResult<>((PageBean)siteBean, new HashMap<String, Object>());
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
