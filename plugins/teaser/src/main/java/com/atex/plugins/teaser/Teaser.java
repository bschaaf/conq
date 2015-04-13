package com.atex.plugins.teaser;

import com.polopoly.cm.ContentId;
import com.polopoly.model.DescribesModelType;

@DescribesModelType
public interface Teaser {

    ContentId getTeaserableContentId();
}
