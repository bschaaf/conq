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
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;

public class AjaxUploadEventTest {

    private AjaxUploadEvent target;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        target = new AjaxUploadEvent();
    }

    @After
    public void tearDown() {
        target = null;
    }

    @Test
    public void testFileName() {
        String fileName = "testFileName";
        target.setFileName(fileName);
        assertEquals(fileName, target.getFileName());
    }

    @Test
    public void testFileDate() {
        String fileData = "testFileData";
        target.setFileData(fileData);
        assertEquals(fileData, target.getFileData());
    }

    @Test
    public void testIndex() {
        int index = 0;
        target.setIndex(index);
        assertEquals(index, target.getIndex());
    }
}
