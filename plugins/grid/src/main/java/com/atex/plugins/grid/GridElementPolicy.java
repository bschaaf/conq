package com.atex.plugins.grid;

import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.Subject;
import com.atex.onecms.content.repository.StorageException;
import com.atex.plugins.baseline.policy.BaselinePolicy;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListProvider;
import com.polopoly.cm.collections.ContentListUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GridElementPolicy extends BaselinePolicy {

    private static final Logger LOG = Logger.getLogger(GridElementPolicy.class.getName());
    // Matching defaults in input template
    private static final String DEFAULT_NUMBER_OF_ROWS = "10";
    // Matching defaults in input template
    private static final String DEFAULT_NUMBER_OF_COLUMNS = "1";

    private ContentManager contentManager;

    public GridElementPolicy(final ContentManager contentManager) {
        this.contentManager = contentManager;
    }

    protected int getNumberOfConfiguredColumns() {
        return Integer.parseInt(getChildValue("numberOfColumns", DEFAULT_NUMBER_OF_COLUMNS));
    }

    protected int getNumberOfConfiguredRows() {
        return Integer.parseInt(getChildValue("numberOfRows", DEFAULT_NUMBER_OF_ROWS));
    }

    protected List<Teaserable> getFilteredList() {
        List<Teaserable> filteredList = new ArrayList<>();

        try {
            ContentList list = getUnfilteredList();
            int listSize = list.size();
            int index = 0;
            int itemsInGrid = fullGridSize();
            while (filteredList.size() < itemsInGrid && index < listSize) {
                ContentReference contentReference = list.getEntry(index);
                Teaserable teaserable = getTeaserable(contentReference.getReferredContentId());
                if (teaserable != null) {
                    filteredList.add(teaserable);
                }
                index++;
            }
        } catch (CMException e) {
            LOG.log(Level.SEVERE, "Unable to get content list publishingQueue", e);
        }

        return filteredList;
    }

    private ContentList getUnfilteredList() {
        ContentList list = ContentListUtil.EMPTY_CONTENT_LIST;
        try {
            ContentList queue = getContentList("publishingQueue");
            if (queue.size() > 0) {
                ContentId queueId = queue.getEntry(0).getReferredContentId();
                ContentListProvider queuePolicy = (ContentListProvider) getCMServer().getPolicy(queueId);
                list = queuePolicy.getContentList();
            }
        } catch (CMException e) {
            LOG.log(Level.SEVERE, "Unable to get content list publishingQueue", e);
        }
        return list;
    }

    private Teaserable getTeaserable(final ContentId teaserableId) {
        Teaserable teaserable = null;
        try {
            ContentVersionId versionId = contentManager.resolve(IdUtil.fromPolicyContentId(teaserableId),
                                                                Subject.NOBODY_CALLER);
            if (versionId == null) {
                LOG.log(Level.SEVERE, "Teaserable content not found: " + teaserableId.getContentIdString());
                return null;
            }
            ContentResult<Teaserable> dataResult = contentManager.get(
                    versionId,
                    "com.atex.plugins.grid.teaserable",
                    Teaserable.class,
                    Collections.<String, Object>emptyMap(),
                    Subject.NOBODY_CALLER);
            if (dataResult.getStatus().isOk()) {
                teaserable = dataResult.getContent().getContentData();
            }
        } catch (ClassCastException | StorageException e) {
            LOG.log(Level.SEVERE, "Unable to fetch teaserable content using id: "
                    + teaserableId.getContentIdString(), e);
        }
        return teaserable;
    }

    private int fullGridSize() {
        return getNumberOfConfiguredRows() * getNumberOfConfiguredColumns();
    }
}
