package com.atex.plugins.social.sharing.google;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.atex.plugins.social.sharing.AbstractTest;
import com.atex.plugins.social.sharing.SocialSharingConfigPolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

/**
 * Unit test for {@link GooglePlusController}.
 */
@RunWith(MockitoJUnitRunner.class)
public class GooglePlusControllerTest extends AbstractTest {

    private final String lang = RandomStringUtils.randomAlphabetic(10);

    @Spy
    private GooglePlusController controller = new GooglePlusController();

    @Mock
    private SocialSharingConfigPolicy policy;

    @Mock
    private ModelWrite model;

    @Mock
    private TopModel topModel;

    @Mock
    private ControllerContext context;

    @Mock
    private RenderRequest request;

    @Before
    public void init() throws CMException {

        Mockito.doReturn(policy).when(controller).getSocialSharingConfiguration(Mockito.any(ControllerContext.class));

        Mockito.when(policy.getGoogleLanguage()).thenReturn(lang);

        Mockito.when(topModel.getLocal()).thenReturn(model);
    }

    @Test
    public void testAttributes() {

        final ArgumentCaptor<String> nameCapture = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Object> valueCapture = ArgumentCaptor.forClass(Object.class);

        controller.populateModelBeforeCacheKey(request, topModel, context);

        Mockito.verify(model, Mockito.atLeast(1))
               .setAttribute(nameCapture.capture(), valueCapture.capture());

        final Object confAttribute = getArg(nameCapture, valueCapture, "google-plus-lang");
        Assert.assertNotNull(confAttribute);

        final String conf = (String) confAttribute;

        Assert.assertEquals(lang, conf);
    }

    @Test
    public void testValidUrl() {

        final String url = RandomStringUtils.randomAlphabetic(10);
        Mockito.when(policy.getGooglePlusUrl()).thenReturn(url);

        final ArgumentCaptor<String> nameCapture = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Object> valueCapture = ArgumentCaptor.forClass(Object.class);

        controller.populateModelBeforeCacheKey(request, topModel, context);

        Mockito.verify(model, Mockito.atLeast(1))
               .setAttribute(nameCapture.capture(), valueCapture.capture());

        final Object confAttribute = getArg(nameCapture, valueCapture, "google-plus-url");
        Assert.assertNotNull(confAttribute);

        final String conf = (String) confAttribute;

        Assert.assertEquals(url, conf);
    }

    @Test
    public void testEmptyUrl() {

        Mockito.when(policy.getGooglePlusUrl()).thenReturn("");

        final ArgumentCaptor<String> nameCapture = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Object> valueCapture = ArgumentCaptor.forClass(Object.class);

        controller.populateModelBeforeCacheKey(request, topModel, context);

        Mockito.verify(model, Mockito.atLeast(1))
               .setAttribute(nameCapture.capture(), valueCapture.capture());

        missingArg(nameCapture, valueCapture, "google-plus-url");
    }

    @Test
    public void testException() throws CMException {

        Mockito.doThrow(new CMException("Test")).when(controller)
            .getSocialSharingConfiguration(Mockito.any(ControllerContext.class));

        controller.populateModelBeforeCacheKey(request, topModel, context);

        Mockito.verifyZeroInteractions(model);
    }

}
