package com.atex.plugins.personalization;

import com.polopoly.cm.ContentReference;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.policy.NumberInputPolicy;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.app.search.categorization.TypeProvider;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.model.DescribesModelType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.logging.Level;

@DescribesModelType
public class ConfigPolicy extends ContentPolicy {
    public static final String EXTERNAL_ID = "plugins.com.atex.plugins.personalization.Config";

    private static final String MAX_HISTORY_FIELD = "maxHistory";
    private static final String MAX_ENTITIES_FIELD = "maxEntities";
    private static final String MAX_RESULTS_FIELD = "maxResults";
    private static final String MAX_EXCLUDE_FIELD = "maxExclude";
    private static final String ENABLED_FIELD = "enabled";
    private static final String TYPE_FIELD = "type";
    private static final int DEFAULT_MAX_HISTORY = 100;
    private static final int DEFAULT_MAX_ENTITIES = 5;
    private static final int DEFAULT_MAX_RESULTS = 10;
    private static final int DEFAULT_MAX_EXCLUDE = 20;

    public int getMaxHistory() {
        try {
            return ((NumberInputPolicy) getChildPolicy(MAX_HISTORY_FIELD)).getIntValue();
        } catch (CMException | NumberFormatException e) {
            logger.log(Level.SEVERE, "Couldn't read child policy with name '" + MAX_HISTORY_FIELD + "'", e);
            return DEFAULT_MAX_HISTORY;
        }
    }

    public int getMaxEntities() {
        try {
            return ((NumberInputPolicy) getChildPolicy(MAX_ENTITIES_FIELD)).getIntValue();
        } catch (CMException | NumberFormatException e) {
            logger.log(Level.SEVERE, "Couldn't read child policy with name '" + MAX_ENTITIES_FIELD + "'", e);
            return DEFAULT_MAX_ENTITIES;
        }
    }

    public int getMaxExclude() {
        try {
            return ((NumberInputPolicy) getChildPolicy(MAX_EXCLUDE_FIELD)).getIntValue();
        } catch (CMException | NumberFormatException e) {
            logger.log(Level.SEVERE, "Couldn't read child policy with name '" + MAX_EXCLUDE_FIELD + "'", e);
            return DEFAULT_MAX_EXCLUDE;
        }
    }

    public int getMaxResults() {
        try {
            return ((NumberInputPolicy) getChildPolicy(MAX_RESULTS_FIELD)).getIntValue();
        } catch (CMException | NumberFormatException e) {
            logger.log(Level.SEVERE, "Couldn't read child policy with name '" + MAX_RESULTS_FIELD + "'", e);
            return DEFAULT_MAX_RESULTS;
        }
    }

    public boolean isEnabled() {
        try {
            String value = ((SingleValuePolicy) getChildPolicy(ENABLED_FIELD)).getValue();
            return Boolean.valueOf(value);
        } catch (CMException | NumberFormatException e) {
            logger.log(Level.SEVERE, "Couldn't read child policy with name '" + ENABLED_FIELD + "'", e);
            return false;
        }
    }

    public String[] getTypes()
        throws CMException {
        Set<ExternalContentId> types;
        TypeProvider typeProvider = (TypeProvider) getChildPolicy(TYPE_FIELD);

        if (typeProvider != null) {
            types = typeProvider.getTypes();
        } else {
            types = Collections.emptySet();
        }

        ArrayList<String> typesList = new ArrayList<>();
        for (ExternalContentId eid : types) {
            typesList.add(eid.getExternalId());
        }
        return typesList.toArray(new String[types.size()]);
    }

    public List<String> getDimensionIds() {
        List<String> dimensionIds = new ArrayList<>();

        try {
            ListIterator<ContentReference> iterator = getContentList().getListIterator();
            while (iterator.hasNext()) {
                com.polopoly.cm.ContentId contentId = iterator.next().getReferredContentId();
                Policy policy = getCMServer().getPolicy(contentId);
                dimensionIds.add(policy.getContent().getExternalId().getExternalId());
            }
        } catch (CMException e) {
            logger.log(Level.WARNING, "Failed to read dimensions for personalization", e);
        }

        return dimensionIds;
    }
}
