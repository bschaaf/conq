package com.atex.plugins.personalization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;

import com.atex.onecms.content.ContentId;
import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.Subject;
import com.atex.onecms.content.SubjectUtil;
import com.atex.plugins.baseline.policy.BaselinePolicyModelTypeDescription;
import com.atex.plugins.baseline.search.solr.SortByPublishingDate;
import com.atex.plugins.users.AccessToken;
import com.atex.plugins.users.AuthenticationManager;
import com.atex.plugins.users.User;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.polopoly.application.Application;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CMRuntimeException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.management.ServiceNotAvailableException;
import com.polopoly.metadata.Dimension;
import com.polopoly.metadata.Metadata;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.render.RenderResponse;
import com.polopoly.search.metadata.DimensionOperator;
import com.polopoly.search.metadata.EntityOperator;
import com.polopoly.search.metadata.MetadataQueryBuilder;
import com.polopoly.search.solr.SearchClient;
import com.polopoly.search.solr.SearchResult;
import com.polopoly.search.solr.SearchResultPage;
import com.polopoly.search.solr.querydecorators.UnderPage;
import com.polopoly.search.solr.querydecorators.WithDecorators;
import com.polopoly.search.solr.querydecorators.WithInputTemplate;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;
import com.polopoly.siteengine.structure.ParentPathResolver;
import com.polopoly.siteengine.tree.PolicyFetcher;
import com.polopoly.siteengine.tree.PolicyFetcherFactory;

public class MainElementAjaxController extends RenderControllerBase {

    private static final Logger LOGGER = Logger.getLogger(MainElementAjaxController.class.getName());
    private static final int MAX_NUMBER_OF_HITS = 100;
    private static final int MAX_CACHED_USERS = 10000;
    private static final long CACHE_TIMEOUT_MINUTES = 5;
    private static final long CACHE_TIMEOUT_SECONDS = CACHE_TIMEOUT_MINUTES * 60;
    private static final String MAIN_ELEMENT_EXTERNAL_ID = "com.atex.plugins.personalization.MainElement";

    private static final Cache<String, List<Result>> SEARCH_CACHE = CacheBuilder.newBuilder()
        .maximumSize(MAX_CACHED_USERS)
        .expireAfterWrite(CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES)
        .build();

    private PolicyFetcher associatedSitesFetcher = null;

    @Override
    public com.polopoly.siteengine.mvc.Renderer getRenderer(final RenderRequest request,
                                final TopModel m,
                                final com.polopoly.siteengine.mvc.Renderer defaultRenderer,
                                final ControllerContext context) {
        return new Renderer(defaultRenderer);
    }

    @Override
    public void populateModelAfterCacheKey(final RenderRequest request,
                                           final TopModel topModel,
                                           final CacheInfo cacheInfo,
                                           final ControllerContext context) {
        CmClient cmClient = getCmClient(context);
        if (cmClient == null) {
            throw new CMRuntimeException("Could not fetch cmClient");
        }

        ConfigPolicy config = getConfig(cmClient);
        if (!config.isEnabled()) {
            return;
        }

        associatedSitesFetcher =
            new PolicyFetcherFactory().createFetcherForAssociatedSites(cmClient.getPolicyCMServer());

        Policy mainElement;
        try {
            com.polopoly.cm.ContentId elementId = ContentIdFactory.createContentId(request.getParameter("elementId"));
            mainElement = cmClient.getPolicyCMServer().getPolicy(elementId);
            if (!MAIN_ELEMENT_EXTERNAL_ID.equals(mainElement.getInputTemplate().getName())) {
                LOGGER.log(Level.WARNING, "Failed to get MainElement's policy, incorrect type");
                return;
            }
            topModel.getLocal().setAttribute("name", mainElement.getContent().getName());
            topModel.getLocal().setAttribute("path", getParentIds(mainElement, cmClient.getPolicyCMServer()));
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, "Failed to get MainElement's policy", e);
            return;
        }

        String token = request.getCookieValue("accessToken");
        ContentId userId = new AuthenticationManager(cmClient).getUserId(new AccessToken(token));
        if (userId == null || !userId.equals(IdUtil.fromString(request.getParameter("userId")))) {
            LOGGER.log(Level.WARNING, "Token expired or user not authorized.");
            return;
        }

        Application application = context.getApplication();

        SearchClient solrClient = (SearchClient) application.getApplicationComponent("search_solrClientPublic");
        try {
            final QueryData queryData = getQueryData(cmClient, userId, config);
            List<Result> related = new ArrayList<>();
            if (!queryData.getMetadata().getDimensions().isEmpty()) {
                related = getSourceContentList(userId,
                                               cmClient,
                                               solrClient,
                                               config,
                                               mainElement,
                                               queryData);
            } else {
                cacheInfo.setCacheTime(0);
            }
            topModel.getLocal().setAttribute("related", related);
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, "Failed to get related articles for user " + userId, e);
        }
    }

    private List<Result> getSourceContentList(final ContentId userId,
                                              final CmClient cmClient,
                                              final SearchClient solrClient,
                                              final ConfigPolicy config,
                                              final Policy mainElement,
                                              final QueryData queryData)
        throws CMException {

        final int maxResults = config.getMaxResults();
        try {
            final com.polopoly.cm.ContentId[] associatedSites = getAssociatedSites(mainElement);
            String key = getCacheKey(userId, associatedSites);
            return SEARCH_CACHE.get(key, new Callable<List<Result>>() {
                @Override
                public List<Result> call() throws Exception {
                    List<Result> sourceContentList = new ArrayList<>();
                    final SolrQuery solrQuery = getSolrQuery(config, associatedSites, queryData);
                    SearchResultPage searchResult =
                        getSearchResult(solrClient, solrQuery, maxResults);

                    if (searchResult != null) {
                        for (com.polopoly.cm.ContentId id : searchResult.getHits()) {
                            Result result = new Result();
                            result.setContentId(id);
                            result.setName(cmClient.getPolicyCMServer().getPolicy(id).getContent().getName());
                            sourceContentList.add(result);
                        }
                    }

                    return sourceContentList;
                }
            });
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private com.polopoly.cm.ContentId[] getAssociatedSites(final Policy mainElement) throws CMException {
        Set<Policy> associatedSitePolicies = associatedSitesFetcher.fetch(mainElement);
        if (associatedSitePolicies != null && associatedSitePolicies.size() > 0) {
            return getContentIds(associatedSitePolicies);
        }
        return new com.polopoly.cm.ContentId[0];
    }

    private String getCacheKey(final ContentId userId,
                               final com.polopoly.cm.ContentId[] associatedSites)
        throws CMException {

        return IdUtil.toIdString(userId) + ':' + StringUtils.join(associatedSites, ':');
    }

    private SearchResultPage getSearchResult(final SearchClient solrClient,
                                             final SolrQuery query,
                                             final int desiredNumberOfHits)
        throws CMException {

        if (query == null) {
            throw new IllegalArgumentException();
        }
        if (desiredNumberOfHits > MAX_NUMBER_OF_HITS) {
            throw new IllegalArgumentException();
        }

        try {
            return search(solrClient, query, desiredNumberOfHits);
        } catch (ServiceNotAvailableException e) {
            LOGGER.info("Solr search service is unavailable, can not update queue");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unable to perform search", e);
        }

        return null;
    }

    private SearchResultPage search(final SearchClient solrClient, final SolrQuery query, final int desiredNumberOfHits)
        throws SolrServerException, ServiceNotAvailableException {
        if (query == null) {
            throw new IllegalArgumentException("Search criteria or query was null, no search performed!");
        }
        SearchResult searchResult = solrClient.search(query, desiredNumberOfHits);
        return searchResult.getPage(0);
    }

    private SolrQuery getSolrQuery(final ConfigPolicy config,
                                   final com.polopoly.cm.ContentId[] associatedSites,
                                   final QueryData queryData)
        throws CMException {

        String queryString = new MetadataQueryBuilder().buildMetadataQuery(queryData.getMetadata(),
                                                                           DimensionOperator.NONE,
                                                                           EntityOperator.OR);

        if (!queryData.getViewedArticles().isEmpty()) {
            StringBuilder articles = new StringBuilder();
            for (com.polopoly.cm.ContentId id : queryData.getViewedArticles()) {
                if (articles.length() > 0) {
                    articles.append(" OR ");
                }
                articles.append("contentId:").append(id.getContentId().getContentIdString());
            }
            queryString = "(" + queryString + ") AND NOT (" + articles + ")";
        }

        SolrQuery query = new SolrQuery(queryString.isEmpty() ? "*" : queryString);
        WithDecorators decorators = new WithDecorators();
        if (associatedSites != null && associatedSites.length > 0) {
            decorators.add(new UnderPage(associatedSites));
        }
        decorators.add(new WithInputTemplate(config.getTypes()));
        decorators.add(new SortByPublishingDate(SolrQuery.ORDER.desc));
        return decorators.decorate(query);
    }

    private QueryData getQueryData(final CmClient cmClient, final ContentId userId, final ConfigPolicy config) {

        Subject subject = SubjectUtil.fromCaller(cmClient.getCMServer().getCurrentCaller());
        final ContentManager contentManager = cmClient.getContentManager();
        ContentId persId = null;

        try {
            ContentVersionId userVid = contentManager.resolve(userId, subject);
            ContentResult<User> userRead = contentManager.get(userVid, null, User.class, null, subject);
            persId = userRead.getContent().getContentData().getEngagements().get("personalization");
        } catch (Exception e) {
            // This might happen if the user id from cookies isn't in this system
            LOGGER.log(Level.INFO, "Unable to fetch user data using id " + userId
                    + " will treat user as new, got exception: " + e.getClass().getCanonicalName());
        }

        if (persId == null) {
            return new QueryData(new Metadata(), new ArrayList<com.polopoly.cm.ContentId>());
        }

        ContentVersionId persVid = contentManager.resolve(persId, subject);
        ContentResult<Personalization> persRead =
                contentManager.get(persVid, null, Personalization.class, null, subject);
        Personalization pers = persRead.getContent().getContentData();
        PersonalizationDomainObject persDomainObj = new PersonalizationDomainObject(pers);
        Collection<Dimension> dimensions = persDomainObj.getEntitiesSortedByCount(config.getMaxEntities());
        return new QueryData(new Metadata(dimensions),
                             persDomainObj.getRecentlyViewedLegacyIds(config.getMaxExclude()));
    }

    private com.polopoly.cm.ContentId[] getContentIds(final Set<Policy> policies) {
        com.polopoly.cm.ContentId[] contentIds = new com.polopoly.cm.ContentId[policies.size()];
        int count = 0;
        for (Policy policy : policies) {
            contentIds[count++] = policy.getContentId().getContentId();
        }

        return contentIds;
    }

    private com.polopoly.cm.ContentId[] getParentIds(final Policy policy, final PolicyCMServer cmServer)
        throws CMException {
        com.polopoly.cm.ContentId[] result = null;
        if (policy instanceof BaselinePolicyModelTypeDescription) {
            result = ((BaselinePolicyModelTypeDescription) policy).getParentIds();
        }
        if (result == null) {
            try {
                result = new ParentPathResolver().getParentPath(policy.getContentId(), cmServer);
            } catch (CMException e) {
                LOGGER.log(Level.WARNING, "Could not get parent path for " + policy.getContentId()
                    + " returning self as only element in parent path.", e);
                result = new com.polopoly.cm.ContentId[] {policy.getContentId().getContentId() };
            }
        }
        return result;
    }

    private ConfigPolicy getConfig(final CmClient cmClient) {
        PolicyCMServer policyCMServer = cmClient.getPolicyCMServer();
        try {
            return (ConfigPolicy) policyCMServer.getPolicy(new ExternalContentId(ConfigPolicy.EXTERNAL_ID));
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, "Failed to read Personalization configuration.", e);
            throw new RuntimeException("Failed to read config.");
        }
    }

    public static class Result {
        private String name;
        private com.polopoly.cm.ContentId contentId;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public com.polopoly.cm.ContentId getContentId() {
            return contentId;
        }

        public void setContentId(final com.polopoly.cm.ContentId contentId) {
            this.contentId = contentId;
        }
    }

    private static final class QueryData {
        private final Metadata metadata;
        private final List<com.polopoly.cm.ContentId> viewedArticles;

        private QueryData(final Metadata metadata, final List<com.polopoly.cm.ContentId> viewedArticles) {
            this.metadata = metadata;
            this.viewedArticles = viewedArticles;
        }

        public Metadata getMetadata() {
            return metadata;
        }

        public List<com.polopoly.cm.ContentId> getViewedArticles() {
            return viewedArticles;
        }
    }

    private static class Renderer implements com.polopoly.siteengine.mvc.Renderer {
        private final com.polopoly.siteengine.mvc.Renderer defaultRenderer;

        public Renderer(final com.polopoly.siteengine.mvc.Renderer defaultRenderer) {
            this.defaultRenderer = defaultRenderer;
        }

        @Override
        public void render(final TopModel topModel,
                           final RenderRequest renderRequest,
                           final RenderResponse renderResponse,
                           final CacheInfo cacheInfo,
                           final ControllerContext controllerContext) {
            // Ensure that the response is private. The Cache-Control header will probably be overwritten,
            // but is set here just in case.
            if (((HttpServletResponse) renderResponse).isCommitted()) {
                LOGGER.warning("Response was committed before rendering.");
                return;
            }
            renderResponse.setHeader("Cache-Control", String.format("private, max-age=%s", CACHE_TIMEOUT_SECONDS));
            cacheInfo.setPrivate(true);
            defaultRenderer.render(topModel, renderRequest, renderResponse, cacheInfo, controllerContext);
        }
    }
}
