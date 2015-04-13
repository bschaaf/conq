package com.atex.gong.wiring.statistics;

import com.polopoly.cm.ContentId;

/**
 * Used by variant "statistics.thin" to expose the data necessary for a statistics GUI.
 */
public class ThinBean {

    private String name;
    private ContentId parentId;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public ContentId getParentId() {
        return parentId;
    }

    public void setParentId(final ContentId securityParent) {
        this.parentId = securityParent;
    }
}
