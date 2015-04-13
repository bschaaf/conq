package com.atex.standard.image;

import com.polopoly.model.DescribesModelType;

@DescribesModelType
public class ImageContentDataBean {

    private String title;
    private String description;
    private String byline;

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getByline() {
        return byline;
    }

    public void setByline(final String byline) {
        this.byline = byline;
    }
}
