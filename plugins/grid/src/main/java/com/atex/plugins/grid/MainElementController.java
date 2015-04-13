/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.grid;

import java.util.List;

import com.polopoly.model.ModelPathUtil;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

public class MainElementController extends RenderControllerBase {
    @Override
    public void populateModelAfterCacheKey(final RenderRequest request,
                                           final TopModel m,
                                           final CacheInfo cacheInfo,
                                           final ControllerContext context) {
        super.populateModelAfterCacheKey(request, m, cacheInfo, context);
        GridElementPolicy policy = (GridElementPolicy) ModelPathUtil.getBean(context.getContentModel());
        List<Teaserable> filteredList = policy.getFilteredList();
        int numberOfColumnsToRender = policy.getNumberOfConfiguredColumns();
        int numberOfRowsToRender = policy.getNumberOfConfiguredRows();
        numberOfRowsToRender = getNumberOfRows(numberOfRowsToRender, numberOfColumnsToRender, filteredList);
        int numberOfItemsToRender = getNumberOfItemsToRender(numberOfRowsToRender,
                numberOfColumnsToRender, filteredList);
        ModelWrite local = m.getLocal();
        local.setAttribute("numberOfItemsToRender", numberOfItemsToRender);
        local.setAttribute("numberOfColumns", numberOfColumnsToRender);
        local.setAttribute("numberOfRows", numberOfRowsToRender);
        local.setAttribute("filteredList", filteredList);
    }

    private int getNumberOfRows(final int numberOfConfiguredRows,
                                final int numberOfConfiguredColumns,
                                final List<Teaserable> filteredList) {
        return Math.min(numberOfConfiguredRows,
                (int) Math.ceil(((double) filteredList.size()) / numberOfConfiguredColumns));
    }

    private int getNumberOfItemsToRender(final int numberOfRowsToRender,
                                         final int numberOfColumnsToRender,
                                         final List<Teaserable> filteredList) {
        return Math.min(filteredList.size(), (numberOfRowsToRender * numberOfColumnsToRender));
    }

}
