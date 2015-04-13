package com.atex.gong.utils;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Assert;

import com.google.inject.Inject;
import com.polopoly.cm.policy.Policy;
import com.polopoly.management.ServiceNotAvailableException;
import com.polopoly.search.solr.PostFilteredSolrSearchClient;
import com.polopoly.search.solr.SearchResult;
import com.polopoly.search.solr.SolrSearchClient;

import static org.junit.Assert.assertEquals;

/**
 * Injectable test utility used for solr integration test.
 */
public class SolrTestUtil {

    @Inject
    private SolrSearchClient solrClientPublic;

    @Inject
    private PostFilteredSolrSearchClient solrClientInternal;

    public SearchResult searchPublic(final String searchText, final String inputTemplate, final String page) {
        return getSearchResult(solrClientPublic, searchText, inputTemplate, page);
    }

    public SearchResult searchPublic(final String searchText, final String inputTemplate, final Policy page) {
        return searchPublic(searchText, inputTemplate, page.getContentId().getContentId().getContentIdString());
    }

    public SearchResult searchInternal(final String searchText, final String inputTemplate, final String page) {
        return getSearchResult(solrClientInternal, searchText, inputTemplate, page);
    }

    public SearchResult searchInternal(final String searchText, final String inputTemplate, final Policy page) {
        return searchInternal(searchText, inputTemplate, page.getContentId().getContentId().getContentIdString());
    }

    public long hitCountPublic(final String searchText, final String inputTemplate, final String page) {
        return count(searchPublic(searchText, inputTemplate, page));
    }

    public long hitCountInternal(final String searchText, final String inputTemplate, final String page) {
        return count(searchInternal(searchText, inputTemplate, page));
    }

    public long hitCountPublic(final String searchText, final String inputTemplate, final Policy page) {
        return count(searchPublic(searchText, inputTemplate, page));
    }

    public long hitCountInternal(final String searchText, final String inputTemplate, final Policy page) {
        return count(searchInternal(searchText, inputTemplate, page));
    }

    public Searcher makePreconfigureSearcher(final String inputTemplate, final String page) {
        return new Searcher(inputTemplate, page);
    }

    public Searcher makePreconfigureSearcher(final String inputTemplate, final Policy page) {
        return new Searcher(inputTemplate, page.getContentId().getContentId().getContentIdString());
    }

    public class Searcher {
        private String inputTemplate;
        private String page;

        /**
         * @param inputTemplate to filter searches on.
         * @param page          parent of test content to filter searches on.
         */
        Searcher(final String inputTemplate, final String page) {
            this.inputTemplate = inputTemplate;
            this.page = page;
        }

        /**
         * @param searchText to search for.
         * @return SearchResult object of <b>public</b> index search.
         */
        public SearchResult searchPublic(final String searchText) {
            return SolrTestUtil.this.searchPublic(searchText, inputTemplate, page);
        }

        /**
         * @param searchText to search for.
         * @return SearchResult object of <b>internal</b> index search.
         */
        public SearchResult searchInternal(final String searchText) {
            return SolrTestUtil.this.searchInternal(searchText, inputTemplate, page);
        }

        /**
         * @param searchText to search for.
         * @return number of results for provided text in <b>public</b> index, can be used for assertions.
         */
        public long hitCountPublic(final String searchText) {
            return count(searchPublic(searchText));
        }

        /**
         * @param searchText to search for.
         * @return number of results for provided text in <b>internal</b> index, can be used for assertions.
         */
        public long hitCountInternal(final String searchText) {
            return count(searchInternal(searchText));
        }

        /**
         * Searches for searchText and asserts hits equals expectedHits. Searches in all indexes.
         *
         * @param expectedHits expected number of hits.
         * @param searchText   to search for.
         */
        public void assertHitsInAllIndexes(final long expectedHits, final String searchText) {
            assertHitsInPublicIndex(expectedHits, searchText);
            assertHitsInInternalIndex(expectedHits, searchText);
        }

        /**
         * Searches for searchText in public index and asserts hits equals expectedHits. Searches in all indexes.
         *
         * @param expectedHits expected number of hits.
         * @param searchText   to search for.
         */
        public void assertHitsInPublicIndex(final long expectedHits, final String searchText) {
            long hits = hitCountPublic(searchText);
            assertEquals(String.format("Expected '%d' public search hit based on query '%s'. Got %d hits.",
                            expectedHits, searchText, hits),
                    expectedHits, hits
            );
        }

        /**
         * Searches for searchText in internal index and asserts hits equals expectedHits.
         *
         * @param expectedHits expected number of hits.
         * @param searchText   to search for.
         */
        public void assertHitsInInternalIndex(final long expectedHits, final String searchText) {
            long hits = hitCountInternal(searchText);
            assertEquals(String.format("Expected '%d' internal search hit based on query '%s'. Got %d hits.",
                            expectedHits, searchText, hits),
                    expectedHits, hits
            );
        }
    }

    private SearchResult getSearchResult(final SolrSearchClient searchClient,
                                               final String searchText,
                                               final String inputTemplate,
                                               final String page) {
        SolrQuery query = new SolrQuery(searchText);
        query.addFilterQuery("inputTemplate:" + inputTemplate);
        query.addFilterQuery("page:" + page);
        SearchResult result = searchClient.search(query, 1);
        try {
            result.getPage(0);
        } catch (SolrServerException e) {
            Assert.fail("Unable to search, message: " + e.getMessage());
        } catch (ServiceNotAvailableException e) {
            Assert.fail("Unable to search, message: " + e.getMessage());
        }
        return result;
    }

    private long count(final SearchResult result) {
        try {
            return result.getPage(0).getQueryResponses().get(0).getResults().getNumFound();
        } catch (SolrServerException e) {
            Assert.fail("Unable to search, message: " + e.getMessage());
        } catch (ServiceNotAvailableException e) {
            Assert.fail("Unable to search, message: " + e.getMessage());
        }
        return -1;
    }
}
