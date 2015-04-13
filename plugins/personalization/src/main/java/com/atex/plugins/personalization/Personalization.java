package com.atex.plugins.personalization;

import com.atex.onecms.content.ContentId;
import com.atex.onecms.content.aspects.annotations.AspectDefinition;
import com.polopoly.metadata.Dimension;

import java.util.ArrayList;
import java.util.List;

/**
 * Content data for the personalization engagement.
 * Contains a list of the user's most recently viewed content and their categorization
 * in the configured dimension.
 */
@AspectDefinition
public class Personalization {
    private List<View> views = new ArrayList<>();

    public List<View> getViews() {
        return views;
    }

    public void setViews(final List<View> views) {
        this.views = views;
    }

    /**
     * Registered data for a single article (or other categorized content) view.
     */
    public static class View {
        private ContentId articleId;
        private List<Dimension> dimensions = new ArrayList<>();

        public ContentId getArticleId() {
            return articleId;
        }

        public void setArticleId(final ContentId articleId) {
            this.articleId = articleId;
        }

        public List<Dimension> getDimensions() {
            return dimensions;
        }

        public void setDimensions(final List<Dimension> dimensions) {
            this.dimensions = dimensions;
        }
    }
}
