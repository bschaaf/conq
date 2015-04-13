package com.atex.plugins.search;

import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;

import com.atex.plugins.search.data.DateFacet;
import com.atex.plugins.search.data.DateMathParser;
import com.atex.plugins.search.data.PaginationBean;
import com.atex.plugins.search.impl.FacetsProcessorImpl;
import com.atex.plugins.search.impl.SearchResultsVariantFilterImpl;
import com.google.common.base.Optional;
import com.polopoly.application.Application;
import com.polopoly.cache.LRUSynchronizedUpdateCache;
import com.polopoly.cache.SynchronizedUpdateCache;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CMRuntimeException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.management.ServiceNotAvailableException;
import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.RenderRequest;
import com.polopoly.search.solr.SearchClient;
import com.polopoly.search.solr.SearchResult;
import com.polopoly.search.solr.SearchResultPage;
import com.polopoly.search.solr.SolrSearchClient;
import com.polopoly.search.solr.querydecorators.WithDecorators;
import com.polopoly.search.solr.querydecorators.WithInputTemplate;
import com.polopoly.search.solr.schema.IndexFields;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.model.context.SiteScope;
import com.polopoly.siteengine.mvc.Renderer;
import com.polopoly.siteengine.scope.system.ServiceStatus;
import com.polopoly.util.StringUtil;

/**
 * This controller will provide the business logic that handles
 * the full text query and fetching of results.
 */
public class SearchResultsController extends SearchFormController {

    private static final Logger LOGGER = Logger.getLogger(SearchResultsController.class.getName());

    static final String PARAM_SORT_ORDER = "sortOrder";
    static final String SORT_NEWEST_FIRST = "date";
    static final String SORT_RELEVANCE = "rel";
    static final String FACET_QUERY_PARAM = "fq";
    static final String QUERY_PARAM = "q";
    static final String PAGE_PARAM = "page";
    static final String PARAM_PAGE_SIZE = "rps";

    public static final int PAGES_NAVIGATOR_RADIUS = 4;
    public static final String CONFIG_EXTERNALID = "plugins.com.atex.plugins.search.Config";

    @Override
    public void populateModelBeforeCacheKey(final RenderRequest request,
                                            final TopModel topModel,
                                            final ControllerContext context) {
        super.populateModelBeforeCacheKey(request, topModel, context);

        // Get query string
        final String queryString = getQueryParam(QUERY_PARAM, request);
        final List<String> facetQuery = getQueryParams(FACET_QUERY_PARAM, request);

        // Check if is search
        if (!StringUtil.isEmpty(queryString)) {
            // Default hit-count:
            ModelPathUtil.set(topModel.getLocal(), "hitCount", 0);

            performSearch(request, topModel, context, queryString, facetQuery);
        }
    }

    private ContentId getSiteContentId(final TopModel topModel) {
        return ((ContentId) topModel.getContext().getSite().getContent().getAttribute("contentId")).getContentId();
    }

    @Override
    public Renderer getRenderer(final RenderRequest request, final TopModel m, final Renderer defaultRenderer,
                                final ControllerContext context) {

        // If the index server isn't available, searching won't work, so let's
        // hide the search element (but not search results, where we can show an
        // error message instead). We could display an error message instead,
        // like the poll and comments elements do.

        boolean shouldDisplaySearch = !getQueryParam("q", request).isEmpty();

        if (shouldDisplaySearch && isSearchServiceAvailable(m)) {
            return super.getRenderer(request, m, defaultRenderer, context);
        } else {
            return DO_NOTHING_RENDERER;
        }
    }

    private boolean isSearchServiceAvailable(final TopModel m) {
        ServiceStatus searchStatus = m.getSystem().getServiceStatus(SolrSearchClient.DEFAULT_COMPOUND_NAME);
        return searchStatus.isServing();
    }

    private void performSearch(final RenderRequest request,
                               final TopModel topModel,
                               final ControllerContext context,
                               final String queryString,
                               final List<String> filterQueries) {

        final Application application = context.getApplication();
        if (application == null) {
            LOGGER.log(Level.INFO, "No application configured, search is not available.");
            return;
        }
        if (topModel.getContext().getSite() == null) {
            LOGGER.log(Level.INFO, "No site found, search is not available.");
            return;
        }
        final CmClient cmClient = getCmClient(context);
        if (cmClient == null) {
            throw new CMRuntimeException("Could not fetch cmClient");
        }

        final ContentId siteContentId = getSiteContentId(topModel);
        final SearchConfiguration config = getSearchConfiguration(cmClient.getPolicyCMServer());
        final Model model = topModel.getLocal();
        try {
            SolrQuery query = getSolrQuery(queryString, filterQueries, siteContentId, config, request, model);
            final int pageSize = getPageSize(request, config);
            final SearchClient searchClient =
                    (SearchClient) application.getApplicationComponent(SolrSearchClient.DEFAULT_COMPOUND_NAME);
            SearchResult result = searchClient.search(query, pageSize);

            // getPage() must be called before calling getApproximateNumberOfPages() in order for the latter to
            // work.
            result.getPage(0);
            final int pagesAvailable = result.getApproximateNumberOfPages();
            if (pagesAvailable > 0) {
                final int currentPage = getPageNumber(request, 1, pagesAvailable);

                final PaginationBean paginationBean = createPaginationBean(pageSize, pagesAvailable, currentPage);
                final SearchResultPage searchResultPage = result.getPage(currentPage - 1);
                final List<SearchResultView> searchResultViews = getSearchResultViews(cmClient, searchResultPage);
                final List<FacetFieldWrapper> facets = getFacets(
                        config, application, searchResultPage.getQueryResponses(), filterQueries, getLocale(topModel));

                // Paging vars
                ModelPathUtil.set(model, "pagination", paginationBean);
                ModelPathUtil.set(model, "hits", searchResultViews);
                ModelPathUtil.set(model, "hitCount", Integer.valueOf(searchResultViews.size()));
                if (facets.size() > 0) {
                    ModelPathUtil.set(model, "facets", facets);
                }
            }
        } catch (SolrServerException e) {
            LOGGER.log(Level.WARNING, "Search failed", e);
        } catch (ServiceNotAvailableException e) {
            LOGGER.log(Level.INFO, "Search service is currently not available.");
            LOGGER.log(Level.FINE, "Search failed", e);
        }
    }

    private PaginationBean createPaginationBean(final int pageSize,
                                                final int pagesAvailable,
                                                final int currentPage) {
        final PaginationBean paginationBean = new PaginationBean();
        paginationBean.setPageSize(pageSize);
        paginationBean.setPagesAvailable(pagesAvailable);
        paginationBean.setCurrentPage(currentPage);

        // Create stupid list to enable iteration in velocity
        paginationBean.generateNavigatorPagesIndex(PAGES_NAVIGATOR_RADIUS);

        return paginationBean;
    }

    private List<SearchResultView> getSearchResultViews(final CmClient cmClient,
                                                        final SearchResultPage searchResultPage) {
        final List<ContentId> contentIds = searchResultPage.getHits();
        final SearchResultsVariantFilter filter = createSearchResultsVariantFilter(cmClient);
        return filter.getSearchResultViewList(contentIds);
    }

    private int getPageSize(final RenderRequest request,
                            final SearchConfiguration config) {
        return Optional
                .fromNullable(getIntQueryParam(PARAM_PAGE_SIZE, request))
                .or(config.getResultsPageSize());
    }

    int getPageNumber(final RenderRequest request, final int pageMin, final int pageMax) {

        int page = 0;

        // Get page no from request
        final String pageNumber = getQueryParam(PAGE_PARAM, request);
        if (pageNumber != null) {
            try {
                page = Integer.parseInt(pageNumber);
            } catch (NumberFormatException e) {
                page = 0;
            }
            // Check if page no is within possible bounds
            if (page > pageMax) {
                page = pageMax;
            } else if (page < pageMin) {
                page = pageMin;
            }
        }

        return page;
    }

    private List<FacetFieldWrapper> getFacets(final SearchConfiguration config,
                                              final Application application,
                                              final List<QueryResponse> queryResponses,
                                              final List<String> filterQueries,
                                              final Locale locale) {
        final SynchronizedUpdateCache updateCache = (SynchronizedUpdateCache) application
                .getApplicationComponent(LRUSynchronizedUpdateCache.DEFAULT_COMPOUND_NAME);
        final FacetsProcessor facetsProcessor = new FacetsProcessorImpl(updateCache, locale);
        final List<FacetFieldWrapper> facets = facetsProcessor.process(config, queryResponses, filterQueries);
        return facets;
    }

    /**
     * By default the results will be sorted by newest first.
     *
     * @param queryString the query
     * @param filterQueries
     * @param pageContentId
     * @param config
     * @param request
     * @param model
     * @return a not null {@link org.apache.solr.client.solrj.SolrQuery} query object.
     */
    SolrQuery getSolrQuery(final String queryString,
                           final List<String> filterQueries,
                           final ContentId pageContentId,
                           final SearchConfiguration config,
                           final RenderRequest request,
                           final Model model) {

        SolrQuery query = new SolrQuery(escapeSpecialCharacters(toUpperCaseBooleanOperators(queryString)));

        if (filterQueries != null && filterQueries.size() > 0) {
          query.setFilterQueries(filterQueries.toArray(new String[filterQueries.size()]));
        }

        if (config != null) {
            final String queryType = config.getQueryType();
            if (!StringUtil.isEmpty(queryType)) {
                query.setParam("qt", queryType);
            }
            addFacets(config, query);
            query = decorateWithInputTemplateFilters(config, query);
        }

        query.addFilterQuery("page:" + pageContentId.getContentIdString());

        final String sortOrder = getQueryParam(PARAM_SORT_ORDER, request);
        if (StringUtils.isEmpty(sortOrder) || SORT_NEWEST_FIRST.equals(sortOrder)) {
            ModelPathUtil.set(model, PARAM_SORT_ORDER, SORT_NEWEST_FIRST);
            query.setSort(IndexFields.PUBLISHING_DATE.fieldName(), SolrQuery.ORDER.desc);
        } else {
            ModelPathUtil.set(model, PARAM_SORT_ORDER, SORT_RELEVANCE);
        }

        return query;
    }

    String escapeSpecialCharacters(final String query) {
        String[] split = query.split(" ");
        for (int i = 0; i < split.length; ++i) {
            split[i] = ClientUtils.escapeQueryChars(split[i]);
        }
        return StringUtils.join(split, " ");
    }

    private void addFacets(final SearchConfiguration config,
                           final SolrQuery query) {
        final List<String> fields = config.getFacetFieldsList();
        if (fields != null && fields.size() > 0) {
            //facet
            query.setFacet(true);
            query.setFacetLimit(5);
            query.setFacetMinCount(1);
            query.addFacetField(fields.toArray(new String[fields.size()]));

            final TimeZone timeZone = TimeZone.getDefault();
            final Locale locale = Locale.getDefault();
            final DateMathParser dateMathParser = new DateMathParser(timeZone, locale);

            final List<DateFacet> facets = config.getDateFacets();
            if (facets != null) {
                for (final DateFacet dateFacet : facets) {
                    final String start = dateFacet.getStart();
                    final String end = dateFacet.getEnd();
                    try {
                        query.addDateRangeFacet(
                                dateFacet.getName(),
                                dateMathParser.parseMath(start),
                                dateMathParser.parseMath(end),
                                dateFacet.getGap());
                    } catch (final ParseException e) {
                        LOGGER.log(Level.SEVERE, "Cannot parse \"" + start + "\" or \"" + end + "\" : "
                                        + e.getMessage(), e);
                    }
                }
            }
        }
    }

    private SolrQuery decorateWithInputTemplateFilters(final SearchConfiguration config,
                                                       final SolrQuery query) {
        final List<String> inputTemplatesFilter = config.getInputTemplatesForFilter();
        if (inputTemplatesFilter != null && inputTemplatesFilter.size() > 0) {
            final WithDecorators queryDecorator = new WithDecorators();
            queryDecorator.add(new WithInputTemplate(inputTemplatesFilter.toArray(
                    new String[inputTemplatesFilter.size()])));
            return queryDecorator.decorate(query);
        }
        return query;
    }

    private String toUpperCaseBooleanOperators(final String q) {
        if (q != null) {
            return replaceAll(replaceAll(q, "and"), "or");
        }
        return q;
    }

    private String replaceAll(final String q, final String value) {
        return Pattern.compile("\\s" + value.toLowerCase() + "\\s", Pattern.CASE_INSENSITIVE).matcher(q).replaceAll(""
                + " " + value.toUpperCase() + " ");
    }

    private Locale getLocale(final TopModel m) {
        Locale locale = null;
        SiteScope site = m.getContext().getSite();

        if (site != null && site.getBean() != null && site.getBean().getResources() != null) {
            locale = site.getBean().getResources().getLocale();
        }

        if (locale == null) {
            locale = Locale.getDefault();
        }

        return locale;
    }

    /**
     * Create the search variant filter.
     *
     * @param cmClient a not null cmClient.
     * @return a not null {@link com.atex.plugins.search.SearchResultsVariantFilter} filter.
     */
    SearchResultsVariantFilter createSearchResultsVariantFilter(final CmClient cmClient) {
        return new SearchResultsVariantFilterImpl(cmClient);
    }

    /**
     * Get the configuration from the plugin configuration policy.
     *
     * @param cmServer
     * @return the configuration or null.
     */
    SearchConfiguration getSearchConfiguration(final PolicyCMServer cmServer) {
        try {
            final ExternalContentId searchConfigId = new ExternalContentId(CONFIG_EXTERNALID);
            final SearchConfigurationPolicy policy = (SearchConfigurationPolicy) cmServer.getPolicy(searchConfigId);
            return policy;
        } catch (CMException e) {
            LOGGER.log(Level.SEVERE, "Cannot load configuration from " + CONFIG_EXTERNALID + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Create the facets processor.
     *
     * @return a not null {@link com.atex.plugins.search.FacetsProcessor}
     * @param updateCache
     * @param lc
     */
    FacetsProcessor createFacetsProcessor(final SynchronizedUpdateCache updateCache, final Locale lc) {
        return new FacetsProcessorImpl(updateCache, lc);
    }
}
