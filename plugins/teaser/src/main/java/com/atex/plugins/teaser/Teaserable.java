package com.atex.plugins.teaser;

import com.polopoly.cm.ContentId;
import com.polopoly.model.DescribesModelType;

import java.util.List;

@DescribesModelType
public interface Teaserable {

    ContentId getContentId();

    String getName();

    String getText();

    /**
     * Returns null if no image is found.
     *
     * @return the image content id.
     */
    ContentId getImageContentId();

    /**
     * Fetches full path of teaserable content, content is responsible for adding itself to the
     * path.
     *
     * @return path of teaserable content, used for linking.
     */
    ContentId[] getLinkPath();

    List<Attribute> getAttributes();

    public static class Attribute {

        private String cssClass = "";
        private String value = "";

        public Attribute() {
        }

        public Attribute(final String cssClass, final String value) {
            this.cssClass = cssClass;
            this.value = value;
        }

        public String getCssClass() {
            return cssClass;
        }

        public void setCssClass(final String cssClass) {
            this.cssClass = cssClass;
        }

        public String getValue() {
            return value;
        }

        public void setValue(final String value) {
            this.value = value;
        }
    }
}
