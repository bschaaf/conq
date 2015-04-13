package com.atex.plugins.teaser;

import com.google.inject.Inject;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.testnext.web.WebTest;
import com.polopoly.testnj.TestNJRunner;
import org.junit.After;
import org.junit.runner.RunWith;

/**
 * Base class used for web tests that imports content. Sub classes must import
 * content of this class as well.
 */
@RunWith(TestNJRunner.class)
public abstract class WebTestBase extends TeaserVariantMocks {

    @Inject
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected WebTest webTest;

    @Inject
    private PolicyCMServer cmServer;

    @After
    public void validateAfter() {
        webTest.validateUnknownVelocityVariables();
        webTest.validatePageHtml();
    }

    protected Policy getPolicy(final String externalContentIdString) throws CMException {
        return cmServer.getPolicy(cmServer.findContentIdByExternalId(new ExternalContentId(externalContentIdString)));
    }

    protected Policy getPolicy(final ContentId contentId) throws CMException {
        return cmServer.getPolicy(contentId);
    }

}
