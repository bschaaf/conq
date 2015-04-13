package com.atex.plugins.search;

import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.Subject;
import com.atex.plugins.search.data.DateFacet;
import com.atex.plugins.search.impl.SearchResultsVariantFilterImpl;
import com.google.common.collect.Lists;
import com.polopoly.application.Application;
import com.polopoly.application.ApplicationComponent;
import com.polopoly.application.ApplicationComponentControl;
import com.polopoly.application.ConnectionProperties;
import com.polopoly.application.ConnectionPropertiesConfigurationException;
import com.polopoly.application.IllegalApplicationStateException;
import com.polopoly.application.InitFailedException;
import com.polopoly.cache.NullSynchronizedUpdateCache;
import com.polopoly.cache.SynchronizedUpdateCache;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.management.ServiceNotAvailableException;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.search.solr.SearchClient;
import com.polopoly.search.solr.SearchResult;
import com.polopoly.search.solr.SearchResultPage;
import com.polopoly.search.solr.schema.IndexFields;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.model.context.ContextScope;
import com.polopoly.siteengine.model.context.PageScope;
import com.polopoly.siteengine.model.context.SiteScope;
import com.polopoly.siteengine.mvc.Renderer;
import com.polopoly.siteengine.scope.system.ServiceStatus;
import com.polopoly.siteengine.scope.system.SystemScope;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Unit test for {@link com.atex.plugins.search.SearchResultsController}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchResultsControllerTest extends AbstractTestCase {
    @Spy
    private SearchResultsController controller;

    @Mock
    private RenderRequest request;

    @Mock
    private ModelWrite model;

    @Mock
    private ModelWrite siteModel;

    @Mock
    private ContentManager contentManager;

    @Mock
    private PolicyCMServer cmServer;

    @Mock
    private SearchClient searchClient;

    @Mock
    private SearchConfiguration config;

    private SynchronizedUpdateCache updateCache = new NullSynchronizedUpdateCache();

    @Before
    public void init() {
        final List<String> facets = Lists.newArrayList();
        final List<DateFacet> dateFacets = Lists.newArrayList();

        Mockito.when(config.getFacetFieldsList()).thenReturn(facets);
        Mockito.when(config.getDateFacets()).thenReturn(dateFacets);
        Mockito.when(config.getResultsPageSize()).thenReturn(SearchConfigurationPolicy.DEFAULT_RESULTS_PAGE_SIZE);

        // use the doReturn syntax otherwise using the when syntax we will get a NullPointer
        // exception. See http://docs.mockito.googlecode.com/hg/latest/org/mockito/Mockito.html#13
        Mockito.doReturn(config).when(controller).getSearchConfiguration(Mockito.any(PolicyCMServer.class));
    }

    @Test
    public void test_escapeSpecialCharacters() throws Exception {
        String in = "Ab. + - && || ! ( ) { } [ ] ^ \" ~ * ? : \\";
        String result = controller.escapeSpecialCharacters(in);
        assertEquals("Escaped result incorrect",
                     "Ab. \\+ \\- \\&\\& \\|\\| \\! \\( \\) \\{ \\} \\[ \\] \\^ \\\" \\~ \\* \\? \\: \\\\",
                     result);
    }

    @Test
    public void test_filter_null() {
        final String text = RandomStringUtils.randomAlphabetic(20);
        final ContentId siteId = getRandomContentId(2);

        final SolrQuery q = controller.getSolrQuery(text, null, siteId, config, request, model);
        assertNotNull(q);

        final String query = q.toString();
        assertTrue(query.contains(text));
        assertTrue(query.contains(siteId.getContentIdString()));
        assertFalse(query.contains(IndexFields.INPUT_TEMPLATE.fieldName()));
        assertTrue(query.contains(IndexFields.PUBLISHING_DATE.fieldName()));
    }

    @Test
    public void test_no_filter() {
        final String text = RandomStringUtils.randomAlphabetic(20);
        final List<String> filter = Lists.newArrayList();
        Mockito.when(config.getInputTemplatesForFilter()).thenReturn(filter);

        final ContentId siteId = getRandomContentId(2);

        final SolrQuery q = controller.getSolrQuery(text, null, siteId, config, request, model);
        assertNotNull(q);

        final String query = q.toString();
        assertTrue(query.contains(text));
        assertTrue(query.contains(siteId.getContentIdString()));
        assertFalse(query.contains(IndexFields.INPUT_TEMPLATE.fieldName()));
        assertTrue(query.contains(IndexFields.PUBLISHING_DATE.fieldName()));
    }

    @Test
    public void test_filter_two_it() {
        final String text = RandomStringUtils.randomAlphabetic(20);

        // make sure the two strings are different.
        final String it1 = "it1." + RandomStringUtils.randomAlphabetic(10);
        final String it2 = "it2." + RandomStringUtils.randomAlphabetic(10);
        final List<String> it = new ArrayList<String>();
        it.add(it1);
        it.add(it2);

        Mockito.when(config.getInputTemplatesForFilter()).thenReturn(it);

        final ContentId siteId = getRandomContentId(2);

        final SolrQuery q = controller.getSolrQuery(text, null, siteId, config, request, model);
        assertNotNull(q);

        final String query = q.toString();
        assertTrue(query.contains(text));
        assertTrue(query.contains(siteId.getContentIdString()));
        assertTrue(query.contains(IndexFields.INPUT_TEMPLATE.fieldName()));
        assertTrue(query.contains(IndexFields.PUBLISHING_DATE.fieldName()));
        for (final String s : it) {
            assertTrue(query.contains(s));
        }
    }

    @Test
    public void test_sorting_relevance() {
        final String text = RandomStringUtils.randomAlphabetic(20);
        final ContentId siteId = getRandomContentId(2);

        Mockito
                .when(request.getParameter(Mockito.same(SearchResultsController.PARAM_SORT_ORDER)))
                .thenReturn(SearchResultsController.SORT_RELEVANCE);

        final SolrQuery q = controller.getSolrQuery(text, null, siteId, config, request, model);
        assertNotNull(q);

        final String query = q.toString();
        assertTrue(query.contains(text));
        assertTrue(query.contains(siteId.getContentIdString()));
        assertFalse(query.contains(IndexFields.INPUT_TEMPLATE.fieldName()));
        assertFalse(query.contains(IndexFields.PUBLISHING_DATE.fieldName()));
    }

    @Test
    public void test_no_configuration() {
        final String text = RandomStringUtils.randomAlphabetic(20);
        final ContentId siteId = getRandomContentId(2);

        final SolrQuery q = controller.getSolrQuery(text, null, siteId, null, request, model);
        assertNotNull(q);

        final String query = q.toString();
        assertTrue(query.contains(text));
        assertTrue(query.contains(siteId.getContentIdString()));
        assertFalse(query.contains(IndexFields.INPUT_TEMPLATE.fieldName()));
        assertTrue(query.contains(IndexFields.PUBLISHING_DATE.fieldName()));
    }

    @Test
    public void test_sorting_newest() {
        final String text = RandomStringUtils.randomAlphabetic(20);
        final ContentId siteId = getRandomContentId(2);

        Mockito.when(request.getParameter(Mockito.same(SearchResultsController.PARAM_SORT_ORDER)))
                .thenReturn(SearchResultsController.SORT_NEWEST_FIRST);

        final SolrQuery q = controller.getSolrQuery(text, null, siteId, config, request, model);
        assertNotNull(q);

        final String query = q.toString();
        assertTrue(query.contains(text));
        assertTrue(query.contains(siteId.getContentIdString()));
        assertFalse(query.contains(IndexFields.INPUT_TEMPLATE.fieldName()));
        assertTrue(query.contains(IndexFields.PUBLISHING_DATE.fieldName()));
    }

    @Test
    public void test_empty_renderer() {
        final TopModel m = Mockito.mock(TopModel.class);
        Mockito.when(m.getLocal()).thenReturn(model);

        final ControllerContext context = Mockito.mock(ControllerContext.class);

        controller.populateModelBeforeCacheKey(request, m, context);

        Mockito.verifyZeroInteractions(model);
    }

    @Test
    public void test_display_results() {
        final TopModel m = Mockito.mock(TopModel.class);
        Mockito.when(m.getLocal()).thenReturn(model);

        final ArgumentCaptor<String> nameCapture = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Object> valueCapture = ArgumentCaptor.forClass(Object.class);

        final ControllerContext context = Mockito.mock(ControllerContext.class);

        final String text = RandomStringUtils.randomAlphabetic(10);
        Mockito.when(request.getParameter(Mockito.same("q"))).thenReturn(text);

        controller.populateModelBeforeCacheKey(request, m, context);

        Mockito.verify(model, Mockito.atLeast(2))
                .setAttribute(nameCapture.capture(), valueCapture.capture());

        assertEquals(text, getArg(nameCapture, valueCapture, "q"));
        assertEquals(0, getArg(nameCapture, valueCapture, "hitCount"));
    }

    @Test
    public void test_query_xss() {
        final TopModel m = Mockito.mock(TopModel.class);
        Mockito.when(m.getLocal()).thenReturn(model);

        final ArgumentCaptor<String> nameCapture = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Object> valueCapture = ArgumentCaptor.forClass(Object.class);

        final ControllerContext context = Mockito.mock(ControllerContext.class);

        final String text = RandomStringUtils.randomAlphabetic(10);
        final String html = "<b>" + text + "</b>";
        Mockito.when(request.getParameter(Mockito.same("q"))).thenReturn(html);

        controller.populateModelBeforeCacheKey(request, m, context);

        Mockito.verify(model, Mockito.atLeast(1))
                .setAttribute(nameCapture.capture(), valueCapture.capture());

        final Object query = getArg(nameCapture, valueCapture, "q");
        assertNotSame(html, query);
        assertEquals(text, query);
    }

    @Test
    public void test_createSearchResultsVariantFilter() {
        final ContentManager contentManagerMock = Mockito.mock(ContentManager.class);
        final PolicyCMServer policyCMServerMock = Mockito.mock(PolicyCMServer.class);

        final CmClient cmClient = Mockito.mock(CmClient.class);
        Mockito.when(cmClient.getContentManager()).thenReturn(contentManagerMock);
        Mockito.when(cmClient.getPolicyCMServer()).thenReturn(policyCMServerMock);

        final SearchResultsVariantFilter filter = controller.createSearchResultsVariantFilter(cmClient);
        assertNotNull(filter);
    }

    @Test
    public void test_getRenderer_empty_when_no_query_parameter() {
        final ServiceStatus serviceStatus = Mockito.mock(ServiceStatus.class);
        Mockito.when(serviceStatus.isServing()).thenReturn(true);
        final TopModel m = setupTopModel(serviceStatus);

        final Renderer defaultRenderer = Mockito.mock(Renderer.class);
        final ControllerContext context = Mockito.mock(ControllerContext.class);
        final Renderer renderer = controller.getRenderer(request, m, defaultRenderer, context);
        assertNotNull(renderer);
        assertEquals(controller.DO_NOTHING_RENDERER, renderer);
    }

    @Test
    public void test_getRenderer_empty_when_search_not_available() {
        final String query = RandomStringUtils.randomAlphabetic(10);
        Mockito.when(request.getParameter(Mockito.same("q"))).thenReturn(query);

        final ServiceStatus serviceStatus = Mockito.mock(ServiceStatus.class);
        Mockito.when(serviceStatus.isServing()).thenReturn(false);
        final TopModel m = setupTopModel(serviceStatus);

        final Renderer defaultRenderer = Mockito.mock(Renderer.class);
        final ControllerContext context = Mockito.mock(ControllerContext.class);
        final Renderer renderer = controller.getRenderer(request, m, defaultRenderer, context);
        assertNotNull(renderer);
        assertEquals(controller.DO_NOTHING_RENDERER, renderer);
    }

    @Test
    public void test_getRenderer_not_null() {
        final String query = RandomStringUtils.randomAlphabetic(10);
        Mockito.when(request.getParameter(Mockito.same("q"))).thenReturn(query);

        final ServiceStatus serviceStatus = Mockito.mock(ServiceStatus.class);
        Mockito.when(serviceStatus.isServing()).thenReturn(true);
        final TopModel m = setupTopModel(serviceStatus);

        final Renderer defaultRenderer = Mockito.mock(Renderer.class);
        final ControllerContext context = Mockito.mock(ControllerContext.class);
        final Renderer renderer = controller.getRenderer(request, m, defaultRenderer, context);
        assertNotNull(renderer);
        assertEquals(defaultRenderer, renderer);
    }

    @Test
    public void test_performSearch_no_results() throws IllegalApplicationStateException {
        // setup site id

        final ContentId siteId = getRandomVersionedContentId(2);
        Mockito.when(siteModel.getAttribute(Mockito.eq("contentId"))).thenReturn(siteId);

        // setup query

        final String query = RandomStringUtils.randomAlphabetic(10);
        Mockito.when(request.getParameter(Mockito.same("q"))).thenReturn(query);

        final ServiceStatus serviceStatus = Mockito.mock(ServiceStatus.class);
        Mockito.when(serviceStatus.isServing()).thenReturn(true);
        final TopModel m = setupTopModel(serviceStatus);

        final ControllerContext context = setup_polopoly_context_mock();

        final SearchResult searchResult = Mockito.mock(SearchResult.class);
        Mockito.when(searchClient.search(Mockito.any(SolrQuery.class), Mockito.anyInt())).thenReturn(searchResult);
        Mockito.when(searchResult.getApproximateNumberOfPages()).thenReturn(0);

        final ArgumentCaptor<String> nameCapture = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Object> valueCapture = ArgumentCaptor.forClass(Object.class);

        controller.populateModelBeforeCacheKey(request, m, context);

        Mockito.verify(model, Mockito.atLeast(1))
                .setAttribute(nameCapture.capture(), valueCapture.capture());

        assertEquals(0, getArg(nameCapture, valueCapture, "hitCount"));
    }

    @Test
    public void test_performSearch_one_result_no_variant()
            throws IllegalApplicationStateException, ServiceNotAvailableException, SolrServerException, CMException {
        // setup site id

        final ContentId siteId = getRandomVersionedContentId(2);
        Mockito.when(siteModel.getAttribute(Mockito.eq("contentId"))).thenReturn(siteId);

        // setup query

        final String query = RandomStringUtils.randomAlphabetic(10);
        Mockito.when(request.getParameter(Mockito.same("q"))).thenReturn(query);

        final ServiceStatus serviceStatus = Mockito.mock(ServiceStatus.class);
        Mockito.when(serviceStatus.isServing()).thenReturn(true);
        final TopModel m = setupTopModel(serviceStatus);

        final ControllerContext context = setup_polopoly_context_mock();

        final SearchResult searchResult = Mockito.mock(SearchResult.class);
        Mockito.when(searchClient.search(Mockito.any(SolrQuery.class), Mockito.anyInt())).thenReturn(searchResult);
        Mockito.when(searchResult.getApproximateNumberOfPages()).thenReturn(1);

        final SearchResultPage resultPage = Mockito.mock(SearchResultPage.class);
        Mockito.when(searchResult.getPage(Mockito.anyInt())).thenReturn(resultPage);

        final VersionedContentId articleId = getRandomVersionedContentId(1);
        final List<ContentId> hits = new ArrayList<ContentId>();
        hits.add(articleId.getContentId());

        setupCmTranslateContentId(articleId);

        Mockito.when(resultPage.getHits()).thenReturn(hits);

        final ArgumentCaptor<String> nameCapture = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Object> valueCapture = ArgumentCaptor.forClass(Object.class);

        controller.populateModelBeforeCacheKey(request, m, context);

        Mockito.verify(model, Mockito.atLeast(2))
                .setAttribute(nameCapture.capture(), valueCapture.capture());

        assertEquals(0, getArg(nameCapture, valueCapture, "hitCount"));
    }

    @Test
    public void test_performSearch_one_result_one_variant()
            throws IllegalApplicationStateException, ServiceNotAvailableException, SolrServerException, CMException {
        // setup site id

        final ContentId siteId = getRandomVersionedContentId(2);
        Mockito.when(siteModel.getAttribute(Mockito.eq("contentId"))).thenReturn(siteId);

        // setup query

        final String query = RandomStringUtils.randomAlphabetic(10);
        Mockito.when(request.getParameter(Mockito.same("q"))).thenReturn(query);

        final ServiceStatus serviceStatus = Mockito.mock(ServiceStatus.class);
        Mockito.when(serviceStatus.isServing()).thenReturn(true);
        final TopModel m = setupTopModel(serviceStatus);

        final ControllerContext context = setup_polopoly_context_mock();

        final SearchResult searchResult = Mockito.mock(SearchResult.class);
        Mockito.when(searchClient.search(Mockito.any(SolrQuery.class), Mockito.anyInt())).thenReturn(searchResult);
        Mockito.when(searchResult.getApproximateNumberOfPages()).thenReturn(1);

        final SearchResultPage resultPage = Mockito.mock(SearchResultPage.class);
        Mockito.when(searchResult.getPage(Mockito.anyInt())).thenReturn(resultPage);

        final VersionedContentId articleId = getRandomVersionedContentId(1);
        final List<ContentId> hits = new ArrayList<ContentId>();
        hits.add(articleId.getContentId());

        setupCmTranslateContentId(articleId);

        Mockito.when(resultPage.getHits()).thenReturn(hits);

        final SearchResultView resultMock = Mockito.mock(SearchResultView.class);

        ContentVersionId vid = new ContentVersionId(IdUtil.fromPolicyContentId(articleId),
                                                    String.valueOf(articleId.getVersion()));
        Mockito.when(contentManager.resolve(Mockito.eq(IdUtil.fromPolicyContentId(articleId)),
                                            Mockito.any(Subject.class))).thenReturn(vid);

        Mockito.when(contentManager.get(
                Mockito.eq(vid),
                Mockito.eq(SearchResultsVariantFilterImpl.SEARCHRESULTVIEW_VARIANT),
                Mockito.eq(SearchResultView.class),
                Mockito.anyMap(),
                Mockito.any(Subject.class))).thenReturn(new ContentResult<SearchResultView>(resultMock));

        final ArgumentCaptor<String> nameCapture = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Object> valueCapture = ArgumentCaptor.forClass(Object.class);

        controller.populateModelBeforeCacheKey(request, m, context);

        Mockito.verify(model, Mockito.atLeast(3))
                .setAttribute(nameCapture.capture(), valueCapture.capture());

        assertEquals(1, getArg(nameCapture, valueCapture, "hitCount"));
        assertEquals(resultMock, ((List<SearchResultView>) getArg(nameCapture, valueCapture, "hits")).get(0));
    }

    @Test
    public void test_page_zero() {
        assertEquals(0, controller.getPageNumber(request, 0, 100));

        setup_request_param(SearchResultsController.PAGE_PARAM, "");

        assertEquals(0, controller.getPageNumber(request, 0, 100));

        setup_request_param(SearchResultsController.PAGE_PARAM, RandomStringUtils.randomAlphabetic(4));

        assertEquals(0, controller.getPageNumber(request, 0, 100));
    }

    @Test
    public void test_page_good_value() {
        for (int idx = 0; idx < 100; idx++) {
            final int page = rnd.nextInt(100) + 1;
            setup_request_param(SearchResultsController.PAGE_PARAM, Integer.toString(page));
            assertEquals(page, controller.getPageNumber(request, 0, 1000));
        }
    }

    @Test
    @SuppressWarnings("checkstyle:avoidnestedblocks")
    public void test_page_capped_values() {
        final int pageMin = 1;
        final int pageMax = 100;

        {
            final int page = pageMin - 1;
            setup_request_param(SearchResultsController.PAGE_PARAM, Integer.toString(page));
            assertEquals(pageMin, controller.getPageNumber(request, pageMin, pageMax));
        }
        {
            final int page = pageMax + 1;
            setup_request_param(SearchResultsController.PAGE_PARAM, Integer.toString(page));
            assertEquals(pageMax, controller.getPageNumber(request, pageMin, pageMax));
        }
    }

    @Test
    public void test_facets_processor() {
        assertNotNull(controller.createFacetsProcessor(updateCache, Locale.getDefault()));
    }

    private void setup_request_param(final String name, final String value) {
        Mockito
                .when(request.getParameter(Mockito.same(name)))
                .thenReturn(value);
    }

    private void setupCmTranslateContentId(final VersionedContentId contentId) throws CMException {
        setupCmTranslateContentId(contentId.getContentId().getDefaultStageVersionId(), contentId);
    }

    private void setupCmTranslateContentId(final VersionedContentId symbolicId,
                                           final VersionedContentId contentId) throws CMException {
        Mockito.when(cmServer.translateSymbolicContentId(Mockito.eq(symbolicId))).thenReturn(contentId);
    }

    private ControllerContext setup_polopoly_context_mock() throws IllegalApplicationStateException {
        final CmClient cmClient = Mockito.mock(CmClient.class);
        Mockito.when(cmClient.getContentManager()).thenReturn(contentManager);
        Mockito.when(cmClient.getPolicyCMServer()).thenReturn(cmServer);

        final Application app = Mockito.mock(Application.class);
        Mockito.when(app.getPreferredApplicationComponent(Mockito.eq(CmClient.class))).thenReturn(cmClient);
        Mockito.when(app.getApplicationComponent(
                Mockito.eq("search_solrClientPublic"))).thenReturn(createSearchClient());

        final ControllerContext context = Mockito.mock(ControllerContext.class);
        Mockito.when(context.getApplication()).thenReturn(app);


        return context;
    }

    private ApplicationComponent createSearchClient() {
        return new MockedSearchClient(Mockito.mock(ApplicationComponent.class));
    }

    private TopModel setupTopModel(final ServiceStatus serviceStatus) {
        final TopModel m = Mockito.mock(TopModel.class);
        final SystemScope systemScope = Mockito.mock(SystemScope.class);
        final SiteScope siteScope = Mockito.mock(SiteScope.class);
        final PageScope pageScope = Mockito.mock(PageScope.class);
        final ContextScope contextScope = Mockito.mock(ContextScope.class);
        Mockito.when(m.getLocal()).thenReturn(model);
        Mockito.when(m.getContext()).thenReturn(contextScope);
        Mockito.when(contextScope.getSite()).thenReturn(siteScope);
        Mockito.when(contextScope.getPage()).thenReturn(pageScope);
        Mockito.when(m.getSystem()).thenReturn(systemScope);
        Mockito.when(systemScope.getServiceStatus(Mockito.anyString())).thenReturn(serviceStatus);
        Mockito.when(siteScope.getContent()).thenReturn(siteModel);
        return m;
    }

    private final class MockedSearchClient implements ApplicationComponent, SearchClient {
        private ApplicationComponent appComp;

        private MockedSearchClient(final ApplicationComponent appComp) {
            this.appComp = appComp;
        }

        @Override
        public String getModuleName() {
            return appComp.getModuleName();
        }

        @Override
        public String getComponentName() {
            return appComp.getComponentName();
        }

        @Override
        public String getCompoundName() {
            return appComp.getCompoundName();
        }

        @Override
        public void readConnectionProperties(final ConnectionProperties connectionProperties)
                throws IllegalApplicationStateException, ConnectionPropertiesConfigurationException {
            appComp.readConnectionProperties(connectionProperties);
        }

        @Override
        public boolean init(final Application application) throws IllegalArgumentException, InitFailedException {
            return appComp.init(application);
        }

        @Override
        public boolean destroy() {
            return appComp.destroy();
        }

        @Override
        public ApplicationComponentControl getServiceControl() {
            return appComp.getServiceControl();
        }

        @Override
        public SearchResult search(final SolrQuery solrQuery, final int i) {
            return searchClient.search(solrQuery, i);
        }
    }

}
