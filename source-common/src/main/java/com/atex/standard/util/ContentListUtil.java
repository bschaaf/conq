package com.atex.standard.util;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class used for transforming ContentList <-> List<ContentId> for the Content API.
 */
public final class ContentListUtil {

    private ContentListUtil() { }

    public static void setContentIdList(final ContentList contentList,
                                        final List<ContentId> contentIdList) throws CMException {
        com.polopoly.cm.collections.ContentListUtil.clear(contentList);

        if (contentIdList != null) {
            for (ContentId id : contentIdList) {
                contentList.add(Integer.MAX_VALUE, new ContentReference(id));
            }
        }
    }

    public static List<ContentId> toList(final ContentList contentList) throws CMException {
        List<ContentId> idList = new ArrayList<>();
        for (int i = 0; i < contentList.size(); i++) {
            idList.add(contentList.getEntry(i).getReferredContentId().getContentId());
        }
        return idList;
    }
}
