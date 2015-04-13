package com.atex.gong.utils;

import com.polopoly.metadata.Entity;
import com.polopoly.metadata.util.MetadataUtil;

public class MetadataTool {
    /**
     * Returns the path needed to uniquely identify the leaf entity contained in <code>topLevelEntity</code>. Assumes
     * there is only one child entity at each level in the path.
     *
     * @param topLevelEntity the top entity in the path
     * @return a path to the leaf entity
     */
    public String getEntityPath(final Entity topLevelEntity) {
        return MetadataUtil.getEntityPath(topLevelEntity);
    }
}
