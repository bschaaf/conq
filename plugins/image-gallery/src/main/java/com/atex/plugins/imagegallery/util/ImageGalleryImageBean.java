package com.atex.plugins.imagegallery.util;

public class ImageGalleryImageBean {

    public static final String VARIANT_NAME = "com.atex.plugins.gallery.image";
    private String byline;
    private String caption;

    public ImageGalleryImageBean() {
    }

    public ImageGalleryImageBean(final String byline, final String caption) {
        this.byline = byline;
        this.caption = caption;
    }

    public void setCaption(final String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public String getByline() {
        return byline;
    }

    public void setByline(final String byline) {
        this.byline = byline;
    }
}
