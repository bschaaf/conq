package com.atex.plugins.disqus;

import com.polopoly.application.Application;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.ModelWrite;
import com.polopoly.pear.impl.ApplicationException;
import com.polopoly.pear.impl.InternalApplicationUtil;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import junit.framework.TestCase;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MainElementControllerTest extends TestCase {

    private static final String DISQUS_SHORTNAME_VALUE = "my-shortname-used-by-disqus";
    private static final boolean DISQUS_ENABLED_VALUE = true;
    private static final boolean DISQUS_DISABLED_VALUE = false;

    private MainElementController mainElementController;

    @Mock
    private Application application;
    @Mock
    private RenderRequest request;
    @Mock
    private TopModel topModel;
    @Mock
    private CacheInfo cacheInfo;
    @Mock
    private ControllerContext context;
    @Mock
    private ModelWrite model;
    @Mock
    private PolicyCMServer policyCMServer;
    @Mock
    private CmClient cmClient;
    @Mock
    private ConfigPolicy configPolicy;

    @Override
    protected void setUp() throws ApplicationException, CMException {
        MockitoAnnotations.initMocks(this);
        mainElementController = new MainElementController();
        when(context.getApplication()).thenReturn(application);
        when(InternalApplicationUtil.getApplicationComponent(context.getApplication(),
                CmClient.class)).thenReturn(cmClient);
        when(cmClient.getPolicyCMServer()).thenReturn(policyCMServer);
        when(policyCMServer.getPolicy(new ExternalContentId(MainElementController.CONFIG_EXT_ID))).
                thenReturn(configPolicy);
        when(topModel.getLocal()).thenReturn(model);
    }

    public void testDisqusShortnameInModel() throws ApplicationException, CMException {
        when(configPolicy.getSiteShortname()).thenReturn(DISQUS_SHORTNAME_VALUE);
        when(configPolicy.isEnabled()).thenReturn(DISQUS_DISABLED_VALUE);
        mainElementController.populateModelAfterCacheKey(request, topModel, cacheInfo, context);
        verify(model, never()).setAttribute("disqusShortname", DISQUS_SHORTNAME_VALUE);
    }

    public void testSiteShortnameIsNull() throws ApplicationException, CMException {
        when(configPolicy.getSiteShortname()).thenReturn(null);
        when(configPolicy.isEnabled()).thenReturn(DISQUS_ENABLED_VALUE);
        // Verify that method does not blow up.
        mainElementController.populateModelAfterCacheKey(request, topModel, cacheInfo, context);
    }

    public void testSiteShortnameIsEmptyString() throws ApplicationException, CMException {
        when(configPolicy.getSiteShortname()).thenReturn("");
        when(configPolicy.isEnabled()).thenReturn(DISQUS_ENABLED_VALUE);
        // Verify that method does not blow up.
        mainElementController.populateModelAfterCacheKey(request, topModel, cacheInfo, context);
    }
}
