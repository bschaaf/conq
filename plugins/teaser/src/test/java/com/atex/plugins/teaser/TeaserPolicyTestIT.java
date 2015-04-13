package com.atex.plugins.teaser;

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

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(TestNJRunner.class)
@ImportTestContent
public class TeaserPolicyTestIT extends TeaserVariantMocks {

    private static final String TEASER_NAME = "My teaser name";
    private static final String TEASER_TEXT = "My teaser text";
    private static final String TEASERABLE_NAME = "My teaserable name";
    private static final String TEASERABLE_TEXT = "My teaserable text";
    private static final String TEASERABLE_ATTRIBUTE_CSS_CLASS = "my-attribute-css-class";
    private static final String TEASERABLE_ATTRIBUTE_VALUE = "My attribute value";
    private static final String EMPTY_TEASER_CONTENT_SUFFIX = ".empty.teaser";
    private static final String TEASER_CONTENT_SUFFIX = ".teaser";
    private static final String TEASER_WITH_TEASERABLE_CONTENT_SUFFIX = ".teaser.with.teaserable";
    private static final String TEASERABLE_CONTENT_SUFFIX = ".teaserable";
    private static final String IMAGE_FOR_TEASER_CONTENT_SUFFIX = ".image.for.teaser";
    private static final String IMAGE_FOR_TEASERABLE_CONTENT_SUFFIX = ".image.for.teaserable";
    private static final String CLASS_NAME = TeaserPolicyTestIT.class.getSimpleName();

    private TeaserPolicy emptyTeaser = null;
    private TeaserPolicy teaser = null;
    private TeaserPolicy teaserWithTeaserable = null;

    @Inject
    private PolicyCMServer cmServer;

    @Before
    public void init() throws CMException {
        emptyTeaser = (TeaserPolicy) getPolicy(EMPTY_TEASER_CONTENT_SUFFIX);
        teaser = (TeaserPolicy) getPolicy(TEASER_CONTENT_SUFFIX);
        teaserWithTeaserable = (TeaserPolicy) getPolicy(TEASER_WITH_TEASERABLE_CONTENT_SUFFIX);
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals("", emptyTeaser.getName());
        assertEquals(TEASER_NAME, teaser.getName());
        assertEquals(TEASERABLE_NAME, teaserWithTeaserable.getName());
    }

    @Test
    public void testGetText() throws Exception {
        assertEquals("", emptyTeaser.getText());
        assertEquals(TEASER_TEXT, teaser.getText());
        assertEquals(TEASERABLE_TEXT, teaserWithTeaserable.getText());
    }

    @Test
    public void testGetTeaserableContentId() throws Exception {
        assertNull(emptyTeaser.getTeaserableContentId());
        ContentId actualContentId = teaserWithTeaserable.getTeaserableContentId().getContentId();
        ContentId expectedContentId = cmServer.findContentIdByExternalId(new ExternalContentId(
                CLASS_NAME + TEASERABLE_CONTENT_SUFFIX));
        assertEquals(expectedContentId, actualContentId);
    }

    @Test
    public void testGetImageContentId() throws Exception {
        assertNull(emptyTeaser.getImageContentId());

        ContentId actualContentId = teaser.getImageContentId();
        ContentId expectedContentId = cmServer.findContentIdByExternalId(new ExternalContentId(
                CLASS_NAME + IMAGE_FOR_TEASER_CONTENT_SUFFIX));
        assertEquals(expectedContentId, actualContentId);

        actualContentId = teaserWithTeaserable.getImageContentId();
        expectedContentId = cmServer.findContentIdByExternalId(new ExternalContentId(
                CLASS_NAME + IMAGE_FOR_TEASERABLE_CONTENT_SUFFIX));
        assertEquals(expectedContentId, actualContentId);
    }

    @Test
    public void testGetLinkPath() throws Exception {
        ContentId[] expectedLinkPath = new ContentId[2];
        expectedLinkPath[0] = getContentId(".site");
        expectedLinkPath[1] = teaserWithTeaserable.getTeaserableContentId().getContentId();
        ContentId[] actualLinkPath = teaserWithTeaserable.getLinkPath();
        assertEquals("Expected and actual link path should be of same length",
                expectedLinkPath.length, expectedLinkPath.length);
        for (int i = 0; i < expectedLinkPath.length; i++) {
            assertEquals(expectedLinkPath[i], actualLinkPath[i]);
        }
    }

    @Test
    public void testGetAttributes() {
        List<Teaserable.Attribute> attributes = emptyTeaser.getAttributes();
        assertTrue(attributes == null || attributes.isEmpty());
        attributes = teaserWithTeaserable.getAttributes();
        assertEquals(1, attributes.size());
        Teaserable.Attribute attribute = attributes.get(0);
        assertEquals(TEASERABLE_ATTRIBUTE_CSS_CLASS, attribute.getCssClass());
        assertEquals(TEASERABLE_ATTRIBUTE_VALUE, attribute.getValue());
    }

    private Policy getPolicy(final String externalContentIdSuffix) throws CMException {
        return cmServer.getPolicy(getContentId(externalContentIdSuffix));
    }

    private ContentId getContentId(final String externalContentIdSuffix) throws CMException {
        String externalContentIdString = CLASS_NAME + externalContentIdSuffix;
        return cmServer.findContentIdByExternalId(new ExternalContentId(externalContentIdString));
    }
}
