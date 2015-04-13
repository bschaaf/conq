/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.imagegallery;

import com.atex.onecms.content.ContentManager;
import com.polopoly.application.Application;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CmClient;
import com.polopoly.model.Model;
import com.polopoly.model.ModelWrite;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.when;

public class MainElementControllerTest {

    private MainElementController target;

    @Mock
    private TopModel m;

    @Mock
    private ControllerContext context;

    @Mock
    private ImageGalleryPolicy policy;

    @Mock
    private Model model;

    @Mock
    private ModelWrite local;

    @Mock
    private ModelWrite stack;

    @Mock
    private Application application;

    @Mock
    private CmClient cmClient;

    @Mock
    private ContentManager cm;

    @Before
    public void before() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = new MainElementController();
        when(context.getContentModel()).thenReturn(model);
        when(model.getAttribute("_data")).thenReturn(policy);
        when(m.getLocal()).thenReturn(local);
        when(m.getStack()).thenReturn(stack);

        when(context.getApplication()).thenReturn(application);
        when(application.getPreferredApplicationComponent(CmClient.class)).thenReturn(cmClient);
        when(cmClient.getContentManager()).thenReturn(cm);
    }


    @Test
    public void noImageToRender() {
        Collection<ContentId> imageResources = Collections.emptyList();
        when(policy.getImages()).thenReturn(imageResources);
        target.populateModelBeforeCacheKey(null, m, context);
    }

}
