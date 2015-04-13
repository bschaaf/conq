package com.atex.gong.publishingqueues;


import com.atex.gong.publishingqueues.decorators.WithOrder;
import com.atex.plugins.baseline.collection.searchbased.decorators.DecoratorBasePolicy;
import com.polopoly.search.solr.QueryDecorator;

import static org.apache.solr.client.solrj.SolrQuery.ORDER;


public class DecoratorSort extends DecoratorBasePolicy {

    @Override
    public QueryDecorator getDecorator() {
        String field = getChildValue("field", "publishingDate");
        String selected = getChildValue("order", "asc");
        ORDER order = "asc".equals(selected) ? ORDER.asc : ORDER.desc;

        return new WithOrder(field, order);
    }
}
