/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.imagegallery;

import com.polopoly.cm.ContentId;
import com.polopoly.model.ModelTypeDescription;

import java.util.Collection;

/**
 * Model type description for galleries.
 *
 */
public interface ImageGalleryModelTypeDescription extends ModelTypeDescription {

    /**
     * Get collection of the images associated with this gallery.
     *
     * @return collection of the images in this gallery
     */
    Collection<ContentId> getImages();

    /**
     * The captions for all images, used for indexing.
     *
     * @return the captions for all images
     */
    String getAllCaptions();
}
