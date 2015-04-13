package com.atex.plugins.search.impl;

import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.Status;
import com.atex.onecms.content.Subject;
import com.atex.plugins.search.AbstractTestCase;
import com.atex.plugins.search.SearchResultView;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for {@link com.atex.plugins.search.impl.SearchResultsVariantFilterImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchResultsVariantFilterImplTest extends AbstractTestCase {
    @Mock
    private ContentManager contentManager;

    @Mock
    private PolicyCMServer cmServer;

    @Test(expected = RuntimeException.class)
    public void test_cmClient_null_constructor() {
        final SearchResultsVariantFilterImpl searchResultsFilter = new SearchResultsVariantFilterImpl(null);
    }

    @Test
    public void test_cmClient_constructor() {
        final CmClient cmClient = Mockito.mock(CmClient.class);
        Mockito.when(cmClient.getContentManager()).thenReturn(contentManager);
        Mockito.when(cmClient.getPolicyCMServer()).thenReturn(cmServer);

        final SearchResultsVariantFilterImpl searchResultsFilter = new SearchResultsVariantFilterImpl(cmClient);
        assertEquals(contentManager, searchResultsFilter.getContentManager());
        assertEquals(cmServer, searchResultsFilter.getCMServer());
    }

    @Test
    public void test_constructor() {
        final SearchResultsVariantFilterImpl searchResultsFilter = createFilter();
        assertEquals(contentManager, searchResultsFilter.getContentManager());
        assertEquals(cmServer, searchResultsFilter.getCMServer());
    }

    @Test
    public void test_getSearchResultView_null_id() {
        final SearchResultsVariantFilterImpl filter = createFilter();
        assertNull(filter.getSearchResultView(null));
    }

    @Test
    public void test_getSearchResultView_id_have_variant() {
        final VersionedContentId id = getRandomVersionedContentId(1);
        final SearchResultView resultMock = Mockito.mock(SearchResultView.class);
        final ContentResult<SearchResultView> dataResultMock = new ContentResult<SearchResultView>(resultMock);

        Mockito.when(contentManager.resolve(Mockito.eq(IdUtil.fromPolicyContentId(id.getContentId())),
                                            Mockito.any(Subject.class)))
            .thenReturn(new ContentVersionId(IdUtil.fromPolicyContentId(id.getContentId()),
                                             String.valueOf(id.getVersion())));

        Mockito.when(contentManager.get(
                Mockito.any(ContentVersionId.class),
                Mockito.eq(SearchResultsVariantFilterImpl.SEARCHRESULTVIEW_VARIANT),
                Mockito.eq(SearchResultView.class),
                Mockito.anyMap(),
                Mockito.any(Subject.class))).thenReturn(dataResultMock);

        final SearchResultsVariantFilterImpl filter = createFilter();
        final SearchResultView result = filter.getSearchResultView(id);
        assertEquals(resultMock, result);
    }

    @Test
    public void test_getSearchResultView_id_no_variant() {
        final VersionedContentId id = getRandomVersionedContentId(1);

        Mockito.when(contentManager.get(
                Mockito.any(ContentVersionId.class),
                Mockito.eq(SearchResultsVariantFilterImpl.SEARCHRESULTVIEW_VARIANT),
                Mockito.eq(SearchResultView.class),
                Mockito.anyMap(),
                Mockito.any(Subject.class))).thenReturn(null);

        final SearchResultsVariantFilterImpl filter = createFilter();
        final SearchResultView result = filter.getSearchResultView(id);
        assertNull(result);
    }

    @Test
    public void test_getSearchResultView_id_variant_without_content() {
        final VersionedContentId id = getRandomVersionedContentId(1);
        final ContentResult<SearchResultView> dataResultMock =
                new ContentResult<SearchResultView>(new ContentVersionId(IdUtil.fromPolicyContentId(id), "foo"),
                                     Status.NOT_FOUND_IN_VARIANT);

        ContentVersionId vid = new ContentVersionId(IdUtil.fromPolicyContentId(id), String.valueOf(id.getVersion()));
        Mockito.when(contentManager.resolve(Mockito.eq(IdUtil.fromPolicyContentId(id)),
                                            Mockito.any(Subject.class))).thenReturn(vid);

        Mockito.when(contentManager.get(
                Mockito.any(ContentVersionId.class),
                Mockito.eq(SearchResultsVariantFilterImpl.SEARCHRESULTVIEW_VARIANT),
                Mockito.eq(SearchResultView.class),
                Mockito.anyMap(),
                Mockito.any(Subject.class))).thenReturn(dataResultMock);

        final SearchResultsVariantFilterImpl filter = createFilter();
        final SearchResultView result = filter.getSearchResultView(id);
        assertNull(result);
    }

    @Test
    public void test_getSearchResultViewList() throws CMException {
        final List<VersionedContentId> vids = new ArrayList<VersionedContentId>();
        vids.add(getRandomVersionedContentId(1));
        vids.add(getRandomVersionedContentId(1));

        final List<ContentId> ids = new ArrayList<ContentId>();
        for (final VersionedContentId vid : vids) {
            ids.add(vid.getContentId());
        }
        Mockito.when(cmServer.translateSymbolicContentId(Mockito.any(VersionedContentId.class)))
                .thenReturn(vids.get(0))
                .thenReturn(vids.get(1));

        Mockito.when(contentManager.resolve(Mockito.eq(IdUtil.fromPolicyContentId(vids.get(0))),
                                            Mockito.any(Subject.class)))
            .thenReturn(new ContentVersionId(IdUtil.fromPolicyContentId(vids.get(0)),
                                             String.valueOf(vids.get(0).getVersion())));

        Mockito.when(contentManager.resolve(Mockito.eq(IdUtil.fromPolicyContentId(vids.get(1))),
                                            Mockito.any(Subject.class)))
            .thenReturn(new ContentVersionId(IdUtil.fromPolicyContentId(vids.get(1)),
                                             String.valueOf(vids.get(1).getVersion())));

        // we simulate that only one will return a good value.

        final SearchResultView resultMock = Mockito.mock(SearchResultView.class);
        final ContentResult<SearchResultView> dataResultMock = new ContentResult<SearchResultView>(resultMock);

        Mockito.when(contentManager.get(
                Mockito.eq(new ContentVersionId(IdUtil.fromPolicyContentId(vids.get(0)),
                                                String.valueOf(vids.get(0).getVersion()))),
                Mockito.eq(SearchResultsVariantFilterImpl.SEARCHRESULTVIEW_VARIANT),
                Mockito.eq(SearchResultView.class),
                Mockito.anyMap(),
                Mockito.any(Subject.class))).thenReturn(dataResultMock);

        final SearchResultsVariantFilterImpl filter = createFilter();
        final List<SearchResultView> results = filter.getSearchResultViewList(ids);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(resultMock, results.get(0));
    }

    private SearchResultsVariantFilterImpl createFilter() {
        return new SearchResultsVariantFilterImpl(cmServer, contentManager);
    }

}
