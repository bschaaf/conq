package com.atex.plugins.personalization;

import com.atex.onecms.content.IdUtil;
import com.polopoly.cm.ContentId;
import com.polopoly.metadata.Dimension;
import com.polopoly.metadata.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PersonalizationDomainObject {

    private final Personalization personalization;

    public PersonalizationDomainObject(final Personalization personalization) {
        this.personalization = personalization;
    }

    public List<ContentId> getRecentlyViewedLegacyIds(final int maxSize) {
        List<ContentId> viewedArticles = new ArrayList<>();
        for (Personalization.View view : personalization.getViews()) {
            if (viewedArticles.size() >= maxSize) {
                break;
            }
            if (IdUtil.isPolicyContentId(view.getArticleId())) {
                viewedArticles.add(IdUtil.toPolicyContentId(view.getArticleId()));
            }
        }
        return viewedArticles;
    }

    public Collection<Dimension> getEntitiesSortedByCount(final int maxSize) {
        // Store count for each entity.
        Map<Dimension, Integer> entitiesWithCount = new HashMap<>();
        for (Personalization.View view : personalization.getViews()) {
            for (Dimension dimension : getFlatDimensions(view)) {
                Integer count = entitiesWithCount.get(dimension);
                entitiesWithCount.put(dimension, count == null ? 1 : count + 1);
            }
        }

        // Sort the entities by count in descending order.
        List<Map.Entry<Dimension, Integer>> entitiesWithCountSorted =
            new ArrayList<>(entitiesWithCount.entrySet());

        Comparator<Map.Entry<Dimension, Integer>> comparator = new Comparator<Map.Entry<Dimension, Integer>>() {
            @Override
            public int compare(final Map.Entry<Dimension, Integer> o1, final Map.Entry<Dimension, Integer> o2) {
                return (o1.getValue() < o2.getValue()) ? 1 : (o1.getValue() > o2.getValue()) ? -1 : 0;
            }
        };
        Collections.sort(entitiesWithCountSorted, comparator);

        // Limit to top entities.
        entitiesWithCountSorted =
            entitiesWithCountSorted.subList(0, Math.min(entitiesWithCountSorted.size(), maxSize));

        // Group entities by dimension, preserving the order of dimensions based on their top entity.
        Map<String, Dimension> dimensionTree = new LinkedHashMap<>();
        for (Map.Entry<Dimension, Integer> entry : entitiesWithCountSorted) {
            Dimension dimension = entry.getKey();
            String id = dimension.getId();
            Dimension tree = dimensionTree.get(id);
            if (tree == null) {
                dimensionTree.put(id, dimension);
            } else {
                tree.addEntities(dimension.getEntities());
            }
        }

        return dimensionTree.values();
    }

    /**
     * "Flatten" dimensions to a list with one entry per entity.
     * I.e. [ { dimension1, [entity1, entity2] } ] becomes [ { dimension1, [entity1] }, { dimension1, [entity2] } ]
     */
    private List<Dimension> getFlatDimensions(final Personalization.View view) {
        List<Dimension> dimensions = new ArrayList<>();
        for (Dimension dimension : view.getDimensions()) {
            for (Entity entity : dimension.getEntities()) {
                dimensions.add(dimension.copyWithEntities(entity));
            }
        }

        return dimensions;
    }

    public Map<String, Integer> getTopLevelEntitiesAndCounts() {
        Map<String, Integer> tags = new HashMap<>();
        for (Personalization.View view : this.personalization.getViews()) {
            for (Dimension dimension : view.getDimensions()) {
                for (Entity entity : dimension.getEntities()) {
                    String name = entity.getName();
                    Integer count = tags.get(name);
                    tags.put(name, count == null ? 1 : count + 1);
                }
            }
        }
        return tags;
    }

}
