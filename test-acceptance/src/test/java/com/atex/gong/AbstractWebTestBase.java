package com.atex.gong;

import com.google.inject.Inject;
import com.polopoly.testnext.web.WebTest;
import com.polopoly.testnj.TestNJRunner;
import org.junit.After;
import org.junit.runner.RunWith;

/**
 * Base class used for web tests that imports content. Sub classes must import
 * content of this class as well.
 */
@RunWith(TestNJRunner.class)
public abstract class AbstractWebTestBase {

    @Inject
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected WebTest webTest;

    @After
    public void validateUnknownVelocityVariables() {
        webTest.validateUnknownVelocityVariables();
    }

    @After
    public void validatePageHtml() {
        webTest.validatePageHtml();
    }
}
