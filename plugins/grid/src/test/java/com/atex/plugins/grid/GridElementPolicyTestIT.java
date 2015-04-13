package com.atex.plugins.grid;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnj.TestNJRunner;

@RunWith(TestNJRunner.class)
@ImportTestContent(files = {
        "com.atex.plugins.grid.TeaserableMock.xml",
        "GridElementPolicyTestIT.dataApi.content",
        "GridElementPolicyTestIT.content",
        "com.atex.plugins.grid.PublishingQueue.xml" })
public class GridElementPolicyTestIT {

    private static final String GRID_ELEMENT_CONTENT_SUFFIX = ".gridelement";
    private static final String CLASS_NAME = GridElementPolicyTestIT.class.getSimpleName();

    private GridElementPolicy policy = null;

    @Inject
    private PolicyCMServer cmServer;

    @Before
    public void init() throws CMException {
        policy = (GridElementPolicy) getPolicy(GRID_ELEMENT_CONTENT_SUFFIX);
    }

    @Test
    public void testGetNumberOfColumns() {
        assertEquals(3, policy.getNumberOfConfiguredColumns());
    }

    @Test
    public void testGetNumberOfRows() {
        assertEquals(2, policy.getNumberOfConfiguredRows());
    }

    @Test
    public void testGetFilteredList() {
        List<Teaserable> list = policy.getFilteredList();
        // Filtered list will be max as large as rows * columns configuration
        assertEquals(6, list.size());
    }

    private Policy getPolicy(final String externalContentIdSuffix) throws CMException {
        return cmServer.getPolicy(getContentId(externalContentIdSuffix));
    }

    private ContentId getContentId(final String externalContentIdSuffix) throws CMException {
        String externalContentIdString = CLASS_NAME + externalContentIdSuffix;
        return cmServer.findContentIdByExternalId(new ExternalContentId(externalContentIdString));
    }
}
