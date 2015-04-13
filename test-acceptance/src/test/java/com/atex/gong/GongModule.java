package com.atex.gong;

import com.atex.gong.utils.ContentTestUtil;
import com.atex.gong.utils.SolrTestUtil;
import com.google.inject.AbstractModule;

public class GongModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ContentTestUtil.class);
        bind(SolrTestUtil.class);
    }
}
