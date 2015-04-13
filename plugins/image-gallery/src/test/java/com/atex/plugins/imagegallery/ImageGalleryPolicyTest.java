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
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.siteengine.standard.image.ImageResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class ImageGalleryPolicyTest {

    private ImageGalleryPolicy target;

    @Mock
    private Content content;
    @Mock
    private InputTemplate inputTemplate;
    @Mock
    private Policy parent;
    @Mock
    private VersionedContentId parentVersionedContentId;
    @Mock
    private ContentId parentContentId;
    @Mock
    private PolicyCMServer cmServer;

    @Mock
    private ContentList contentList;

    @Mock
    private ContentReference reference;
    @Mock
    private ContentId referedContentId;

    @Mock
    private DummyImageResource imageResource;

    @Before
    public void before() throws CMException {
        MockitoAnnotations.initMocks(this);
        when(parent.getContentId()).thenReturn(parentVersionedContentId);
        when(parentVersionedContentId.getContentId()).thenReturn(parentContentId);
        when(reference.getReferredContentId()).thenReturn(referedContentId);
        when(contentList.getEntry(0)).thenReturn(reference);
        when(contentList.size()).thenReturn(1);
        when(content.getContentList(ImageGalleryPolicy.CONTENT_LIST_NAME)).thenReturn(contentList);
        target = new ImageGalleryPolicy() {
            @Override
            protected void initSelf() {
            }
        };
        target.init("PolicyName", new Content[] {content}, inputTemplate, parent, cmServer);
    }

    @Test
    public void shouldReturnListOfRepresentedContentIds() {
        Collection<ContentId> contentIds = target.getRepresentedContent();
        assertNotNull(contentIds);
        assertEquals(1, contentIds.size());
    }

    @Test
    public void shouldNotReturnNullCollectionWhenFailedToGetContentList() throws CMException {
        when(content.getContentList(ImageGalleryPolicy.CONTENT_LIST_NAME)).thenThrow(new CMException("Dummy message"));
        Collection<ContentId> result = target.getRepresentedContent();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnListOfImageResources() throws CMException {
        when(cmServer.getPolicy(referedContentId)).thenReturn(imageResource);
        Collection<ContentId> images = target.getImages();
        assertNotNull(images);
        assertEquals(1, images.size());
    }

    @Test
    public void shouldNotReturnNullFileFilter() {
        assertNotNull(target.getFileFilter());
    }

    private interface DummyImageResource extends Policy, ImageResource {

    }

}
