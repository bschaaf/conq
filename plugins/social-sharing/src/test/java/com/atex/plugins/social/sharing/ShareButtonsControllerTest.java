package com.atex.plugins.social.sharing;

import java.util.Date;
import java.util.Random;

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

import com.atex.onecms.content.ContentId;
import com.atex.plugins.social.sharing.google.GooglePlusRenderBean;
import com.atex.plugins.social.sharing.google.PlusOneAnnotation;
import com.atex.plugins.social.sharing.google.ShareAnnotation;
import com.atex.plugins.social.sharing.twitter.TwitterRenderBean;
import com.google.common.base.Strings;
import com.polopoly.cm.client.CMException;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

/**
 * Unit test for {@link com.atex.plugins.social.sharing.ShareButtonsController}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ShareButtonsControllerTest extends AbstractTest {

    private final Random rnd = new Random(new Date().getTime());

    @Spy
    private ShareButtonsController controller = new ShareButtonsController() {
        @Override
        protected SocialSharingInfoFetcher getSocialSharingInfoFetcher(final ControllerContext controllerContext) {
            return infoFetcher;
        }
    };

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

    @Mock
    private SocialSharingInfoFetcher infoFetcher;

    @Before
    public void init() throws CMException {

        Mockito.doReturn(policy).when(controller).getSocialSharingConfiguration(Mockito.any(ControllerContext.class));

        Mockito.when(topModel.getLocal()).thenReturn(model);
    }

    @Test
    public void testGoogleShareAttributes() {
        testGoogleAttributes(true, ShareAnnotation.NONE.getId());
        testGoogleAttributes(true, ShareAnnotation.BUBBLE_HORIZONTAL.getId());
        testGoogleAttributes(true, ShareAnnotation.BUBBLE_VERTICAL.getId());
        testGoogleAttributes(true, ShareAnnotation.INLINE.getId());
    }

    @Test
    public void testGooglePlusOneAttributes() {
        testGoogleAttributes(false, PlusOneAnnotation.NONE.getId());
        testGoogleAttributes(false, PlusOneAnnotation.BUBBLE_HORIZONTAL.getId());
        testGoogleAttributes(false, PlusOneAnnotation.BUBBLE_VERTICAL.getId());
        testGoogleAttributes(false, PlusOneAnnotation.INLINE.getId());
    }

    private void testGoogleAttributes(final boolean useShare, final String annotation) {

        final String attributes = RandomStringUtils.randomAlphabetic(30);
        final String width = Integer.toString(rnd.nextInt(100) + 120);
        String height = RandomStringUtils.randomNumeric(3);

        Mockito.when(policy.getGoogleAnnotation()).thenReturn(annotation);
        Mockito.when(policy.getGoogleHeight()).thenReturn(height);
        Mockito.when(policy.getGoogleWidth()).thenReturn(width);
        Mockito.when(policy.getGoogleAttributes()).thenReturn(attributes);
        Mockito.when(policy.getGoogleType()).thenReturn(
                (useShare ? SocialSharingConfigPolicy.GPLUS_TYPE_SHARE : SocialSharingConfigPolicy.GPLUS_TYPE_PLUSONE));

        final ArgumentCaptor<String> nameCapture = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Object> valueCapture = ArgumentCaptor.forClass(Object.class);

        controller.populateModelBeforeCacheKey(request, topModel, context);

        Mockito.verify(model, Mockito.atLeast(1))
               .setAttribute(nameCapture.capture(), valueCapture.capture());

        final Object modelAttribute = getArg(nameCapture, valueCapture, "google-plus");
        Assert.assertNotNull(modelAttribute);

        final GooglePlusRenderBean bean = (GooglePlusRenderBean) modelAttribute;

        final String widgetAnnotation;
        if (useShare) {
            widgetAnnotation = ShareAnnotation.fromId(annotation).getData();
            if (ShareAnnotation.BUBBLE_VERTICAL.getData().equals(widgetAnnotation)) {
                height = SocialSharingConfigPolicy.GPLUS_VERTICAL_BUBBLE_HEIGHT;
            }
        } else {
            widgetAnnotation = PlusOneAnnotation.fromId(annotation).getData();
        }

        Assert.assertEquals(Strings.emptyToNull(widgetAnnotation), bean.getAnnotation());
        Assert.assertEquals(height, bean.getHeight());
        Assert.assertEquals(width, bean.getWidth());
        Assert.assertEquals(attributes, bean.getAttributes());
        Assert.assertEquals(useShare, bean.isShareButton());
    }

    @Test
    public void testTwitterAttributes() {

        final String text = RandomStringUtils.randomAlphabetic(10);
        final String count = RandomStringUtils.randomAlphabetic(10);
        final String language = RandomStringUtils.randomAlphabetic(10);
        final String size = RandomStringUtils.randomAlphabetic(5);
        final String related = RandomStringUtils.randomAlphabetic(10);
        final String attributes = RandomStringUtils.randomAlphabetic(30);

        Mockito.when(policy.getTwitterText()).thenReturn(text);
        Mockito.when(policy.getTwitterCount()).thenReturn(count);
        Mockito.when(policy.getTwitterLanguage()).thenReturn(language);
        Mockito.when(policy.getTwitterSize()).thenReturn(size);
        Mockito.when(policy.getTwitterRelated()).thenReturn(related);
        Mockito.when(policy.getTwitterAttributes()).thenReturn(attributes);

        final ArgumentCaptor<String> nameCapture = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Object> valueCapture = ArgumentCaptor.forClass(Object.class);

        controller.populateModelBeforeCacheKey(request, topModel, context);

        Mockito.verify(model, Mockito.atLeast(1))
               .setAttribute(nameCapture.capture(), valueCapture.capture());

        final Object modelAttribute = getArg(nameCapture, valueCapture, "twitter");
        Assert.assertNotNull(modelAttribute);

        final TwitterRenderBean bean = (TwitterRenderBean) modelAttribute;

        Assert.assertEquals(text, bean.getText());
        Assert.assertEquals(count, bean.getCount());
        Assert.assertEquals(language, bean.getLanguage());
        Assert.assertEquals(size, bean.getSize());
        Assert.assertEquals(related, bean.getRelated());
        Assert.assertEquals(attributes, bean.getAttributes());
    }

    @Test
    public void testTwitterTextFromSocialSharingInfo() {

        final String text = RandomStringUtils.randomAlphabetic(10);

        SocialSharingInfo info = new SocialSharingInfo();
        info.setTitle(text);
        Mockito.when(infoFetcher.fetch(Mockito.any(ContentId.class))).thenReturn(info);

        final ArgumentCaptor<String> nameCapture = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Object> valueCapture = ArgumentCaptor.forClass(Object.class);

        controller.populateModelBeforeCacheKey(request, topModel, context);

        Mockito.verify(model, Mockito.atLeast(1))
               .setAttribute(nameCapture.capture(), valueCapture.capture());

        final Object modelAttribute = getArg(nameCapture, valueCapture, "twitter");
        Assert.assertNotNull(modelAttribute);

        final TwitterRenderBean bean = (TwitterRenderBean) modelAttribute;

        Assert.assertEquals(text, bean.getText());
    }


    @Test
    public void testException() throws CMException {

        Mockito.doThrow(new CMException("Test"))
            .when(controller).getSocialSharingConfiguration(Mockito.any(ControllerContext.class));

        controller.populateModelBeforeCacheKey(request, topModel, context);

        Mockito.verifyZeroInteractions(model);
    }

}
