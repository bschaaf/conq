package com.atex.plugins.search;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

/**
 * Unit test for {@link com.atex.plugins.search.SearchFormController}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchFormControllerTest extends AbstractTestCase {
    private SearchFormController controller;

    @Mock
    private RenderRequest request;

    @Mock
    private ModelWrite model;

    @Before
    public void init() {
        controller = new SearchFormController();
    }

    @Test
    public void test_query_xss() {
        final TopModel m = Mockito.mock(TopModel.class);
        Mockito.when(m.getLocal()).thenReturn(model);

        final ArgumentCaptor<String> nameCapture = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Object> valueCapture = ArgumentCaptor.forClass(Object.class);

        final ControllerContext context = Mockito.mock(ControllerContext.class);

        final String text = RandomStringUtils.randomAlphabetic(10);
        final String html = "<b>" + text + "</b>";
        Mockito.when(request.getParameter(Mockito.same("q"))).thenReturn(html);

        controller.populateModelBeforeCacheKey(request, m, context);

        Mockito.verify(model, Mockito.atLeast(1))
                .setAttribute(nameCapture.capture(), valueCapture.capture());

        final Object query = getArg(nameCapture, valueCapture, "q");
        assertNotSame(html, query);
        assertEquals(text, query);
    }

    /**
     * Test that the xss cleanup does not translate the html code into entities.
     */
    @Test
    public void test_query_xss_no_entities() {

        final TopModel m = Mockito.mock(TopModel.class);
        Mockito.when(m.getLocal()).thenReturn(model);

        final ArgumentCaptor<String> nameCapture = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Object> valueCapture = ArgumentCaptor.forClass(Object.class);

        final ControllerContext context = Mockito.mock(ControllerContext.class);

        final String text = "\"" + RandomStringUtils.randomAlphabetic(10) + "\"";
        final String html = "<b>" + text + "</b>";
        Mockito.when(request.getParameter(Mockito.same("q"))).thenReturn(html);

        controller.populateModelBeforeCacheKey(request, m, context);

        Mockito.verify(model, Mockito.atLeast(1))
                .setAttribute(nameCapture.capture(), valueCapture.capture());

        final Object query = getArg(nameCapture, valueCapture, "q");
        assertNotSame(html, query);
        assertEquals(text, query);

    }

}
