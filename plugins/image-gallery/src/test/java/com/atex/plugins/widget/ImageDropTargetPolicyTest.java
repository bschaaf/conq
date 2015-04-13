/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.widget;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;

import static org.junit.Assert.*;

public class ImageDropTargetPolicyTest {

    private ImageDropTargetPolicy target;

    @Mock
    private PolicyCMServer cmServer;
    @Mock
    private Content content;
    @Mock
    private InputTemplate inputTemplate;
    @Mock
    private Policy parent;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        target = spy(new ImageDropTargetPolicy());
        target.init("PolicyName", new Content[] {content}, inputTemplate, parent, cmServer);
    }

    @After
    public void tearDown() {
        target = null;
    }

    @Test
    public void testGetMaxFileSize() {
        int expected = -1;
        assertEquals(expected, target.getMaxFileSize());
    }

    @Test
    public void testGetMaxNofFile() {
        int expected = -1;
        assertEquals(expected, target.getMaxNofFile());
    }
}
