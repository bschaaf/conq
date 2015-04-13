package com.atex.gong.publishingqueues.decorators;

import com.polopoly.search.solr.QueryDecorator;
import org.apache.solr.client.solrj.SolrQuery;

import static org.apache.solr.client.solrj.SolrQuery.ORDER;


public class WithOrder implements QueryDecorator {

    private final String field;
    private final ORDER order;

    public WithOrder(final String field, final ORDER order) {
        this.field = field;
        this.order = order;
    }

    @Override
    public SolrQuery decorate(final SolrQuery solrQuery) {
        return solrQuery.setSort(field, order);
    }
}
