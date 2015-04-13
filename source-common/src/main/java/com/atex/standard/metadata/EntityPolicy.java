package com.atex.standard.metadata;

import com.atex.plugins.baseline.policy.BaselinePolicy;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CMRuntimeException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.metadata.Entity;
import com.polopoly.metadata.EntityContainer;
import com.polopoly.metadata.Metadata;
import com.polopoly.metadata.MetadataEntity;
import com.polopoly.metadata.util.MetadataUtil;
import com.polopoly.model.DescribesModelType;

import static java.lang.String.format;

@DescribesModelType
public class EntityPolicy extends BaselinePolicy implements MetadataEntity {

    @Override
    public Metadata getEntityMetadata() {
        try {

            Entity entity = (Entity) this.getEntityWithChildren(0);

            ContentId securityParentId = this.getSecurityParentId();
            if (securityParentId != null) {
                Policy securityParent = this.getCMServer().getPolicy(securityParentId);
                if (securityParent instanceof MetadataEntity) {
                    Metadata entityMetadata = ((MetadataEntity) securityParent).getEntityMetadata();
                    MetadataUtil.appendAsLeaf(entityMetadata, entity);
                    return entityMetadata;
                } else {
                    throw new CMRuntimeException(format("Security Parent '%s (%s)' is not a metadata entity",
                            securityParent.getClass().getCanonicalName(), securityParentId.getContentIdString()));
                }
            } else {
                throw new CMRuntimeException("securityParent was null");
            }
        } catch (CMException cme) {
            throw new CMRuntimeException(cme);
        }
    }

    @Override
    public EntityContainer getEntityWithChildren(final int maxDepth) {
        try {
            String entityId = getContentId().getContentId().getContentIdString();
            if (getContent().getExternalId() != null) {
                entityId = getContent().getExternalId().getExternalId();
            }
            // no children so just return oneself
            return new Entity(entityId, getName());
        } catch (CMException e) {
            throw new CMRuntimeException(e);
        }
    }
}
