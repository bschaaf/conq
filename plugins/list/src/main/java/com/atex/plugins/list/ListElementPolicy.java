package com.atex.plugins.list;

import java.util.logging.Level;

import com.atex.plugins.baseline.policy.BaselinePolicy;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListProvider;
import com.polopoly.cm.collections.ContentListUtil;
import com.polopoly.model.DescribesModelType;

@DescribesModelType
public class ListElementPolicy extends BaselinePolicy {

    // Matching defaults in input template
    private static final String DEFAULT_NUMBER_OF_ITEMS = "10";

    public int getNumberOfItems() {
        return Math.min(Integer.parseInt(getChildValue("numberOfItems", DEFAULT_NUMBER_OF_ITEMS)),
                (int) (double) getDefaultList().size());
    }

    public String getCssClassName() {
        return getChildValue("additionalStyle", "");
    }

    public boolean isShowTime() {
        return "showTime".equals(getChildValue("linkPrefix", ""));
    }

    public boolean isShowNumbers() {
        return "showNumbers".equals(getChildValue("linkPrefix", ""));
    }

    public ContentList getDefaultList() {
        ContentList list = ContentListUtil.EMPTY_CONTENT_LIST;
        try {
            ContentList queue = getContentList("publishingQueue");
            if (queue.size() > 0) {
                ContentId queueId = queue.getEntry(0).getReferredContentId();
                ContentListProvider queuePolicy = (ContentListProvider) getCMServer().getPolicy(queueId);
                list = queuePolicy.getContentList();
            }
        } catch (CMException e) {
            logger.log(Level.WARNING, "Unable to get content list publishingQueue", e);
        }
        return list;
    }
}
