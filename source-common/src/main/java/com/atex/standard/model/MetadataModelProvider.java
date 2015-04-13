package com.atex.standard.model;

import com.polopoly.metadata.Metadata;
import com.polopoly.model.DescribesModelType;

@DescribesModelType
public interface MetadataModelProvider {
    Metadata getMetadata();
}
