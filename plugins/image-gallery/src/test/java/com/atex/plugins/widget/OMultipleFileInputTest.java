/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.widget;

import static junit.framework.Assert.*;

import java.io.IOException;
import java.util.List;

import com.polopoly.orchid.context.OrchidContext;
import org.apache.commons.fileupload.FileItem;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.event.OrchidEvent;
import com.polopoly.orchid.event.OrchidEventListener;
import com.polopoly.orchid.event.WidgetEventListener;
import com.polopoly.orchid.widget.OJavaScript;
import com.polopoly.orchid.widget.OWidget;

public class OMultipleFileInputTest {

    private OMultipleFileInput target;

    @Mock
    private FileItem fileItem1;
    @Mock
    private FileItem fileItem2;

    @Mock
    private OrchidContext oc;
    @Mock
    private Device device;
    @Mock
    private OJavaScript script;

    @Before
    public void before() throws OrchidException {
        MockitoAnnotations.initMocks(this);
        doReturn(device).when(oc).getDevice();
        target = spy(new OMultipleFileInput() {
            /**
             *
             */
            private static final long serialVersionUID = -2944392324517784376L;

            @Override
            public String getCompoundId() throws OrchidException {
                return "work_123";
            }
        });
        doNothing().when(target).addAndInitChild(eq(oc), any(OWidget.class));
    }

    @Test
    public void initSelfShouldAddMultipleJavascript() throws OrchidException {
        target.initSelf(oc);
        verify(target).addAndInitChild(eq(oc), any(OWidget.class));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldReturnUnmodifieableListWhenGetFileItems() {
        List<FileItem> files = target.getFileItems();
        files.add(fileItem1);
    }

    @Test
    public void shouldDisableMultipleUploadWhenSetMultipleToFalse() throws OrchidException, IOException {

        target.initSelf(oc);
        target.setMultiple(false);
        target.setMultiPartScript(script);
        target.localRender(oc);
        assertTrue(!target.isMultiple());
        verify(device, never()).print(" multiple='multiple' ");
    }

    @Test
    public void shouldEnableMultipleFileUploadWhenSetMultipleIsTrue() throws OrchidException, IOException {

        target.initSelf(oc);
        target.setMultiple(true);
        target.setMultiPartScript(script);
        target.localRender(oc);
        assertTrue(target.isMultiple());
        verify(device).print(" multiple='multiple' ");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void whenDecodeSelfShouldAddFileItemIntoBackedList() throws OrchidException {
        WidgetEventListener listener = mock(WidgetEventListener.class);
        List<FileItem> fileItems = (List<FileItem>) mock(List.class);
        target.initSelf(oc);
        target.setFileItems(fileItems);
        target.addNewFileListener(listener);
        when(fileItem1.isFormField()).thenReturn(false);
        when(fileItem1.getName()).thenReturn("image.jpg");
        when(fileItem2.isFormField()).thenReturn(false);
        when(fileItem2.getName()).thenReturn(null);
        when(device.getParameterValueObjects(anyString())).thenReturn(new Object[] {fileItem1, fileItem2});
        doReturn(true).when(target).isEnabled();
        doReturn(true).when(target).hasFlowEventListeners();
        target.decodeSelf(oc);
        verify(fileItems).add(any(FileItem.class));
        verify(oc).addFlowEvent(any(OrchidEvent.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void whenDecodeSelfShouldNotAddFileItemIntoBackedList() throws OrchidException {
        WidgetEventListener listener = mock(WidgetEventListener.class);
        List<FileItem> fileItems = (List<FileItem>) mock(List.class);
        target.initSelf(oc);
        target.setFileItems(fileItems);
        target.addNewFileListener(listener);
        when(fileItem1.isFormField()).thenReturn(false);
        when(fileItem1.getName()).thenReturn("image.jpg");
        when(fileItem2.isFormField()).thenReturn(false);
        when(fileItem2.getName()).thenReturn(null);
        when(device.getParameterValueObjects(anyString())).thenReturn(new Object[] {fileItem1, fileItem2});
        doReturn(false).when(target).isEnabled();
        doReturn(true).when(target).hasFlowEventListeners();
        target.decodeSelf(oc);
        verify(fileItems, times(0)).add(any(FileItem.class));
        verify(oc, times(0)).addFlowEvent(any(OrchidEvent.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void whenDecodeSelfShouldNotAddFileItemIntoBackedList2() throws OrchidException {
        WidgetEventListener listener = mock(WidgetEventListener.class);
        List<FileItem> fileItems = (List<FileItem>) mock(List.class);
        target.initSelf(oc);
        target.setFileItems(fileItems);
        target.addNewFileListener(listener);
        when(fileItem1.isFormField()).thenReturn(false);
        when(fileItem1.getName()).thenReturn("image.jpg");
        when(fileItem2.isFormField()).thenReturn(false);
        when(fileItem2.getName()).thenReturn(null);
        when(device.getParameterValueObjects(anyString())).thenReturn(new Object[] {fileItem1, fileItem2});
        doReturn(false).when(target).isEnabled();
        doReturn(false).when(target).hasFlowEventListeners();
        target.decodeSelf(oc);
        verify(fileItems, times(0)).add(any(FileItem.class));
        verify(oc, times(0)).addFlowEvent(any(OrchidEvent.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void whenDecodeSelfShouldNotAddFileItemIntoBackedList3() throws OrchidException {
        WidgetEventListener listener = mock(WidgetEventListener.class);
        List<FileItem> fileItems = (List<FileItem>) mock(List.class);
        target.initSelf(oc);
        target.setFileItems(fileItems);
        target.addNewFileListener(listener);
        when(fileItem1.isFormField()).thenReturn(false);
        when(fileItem1.getName()).thenReturn("image.jpg");
        when(fileItem2.isFormField()).thenReturn(false);
        when(fileItem2.getName()).thenReturn(null);
        when(device.getParameterValueObjects(anyString())).thenReturn(new Object[] {fileItem1, fileItem2});
        doReturn(true).when(target).isEnabled();
        doReturn(false).when(target).hasFlowEventListeners();
        target.decodeSelf(oc);
        verify(fileItems, times(0)).add(any(FileItem.class));
        verify(oc, times(0)).addFlowEvent(any(OrchidEvent.class));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void shouldReturnListOfEventListener() throws OrchidException {
        WidgetEventListener listener1 = mock(WidgetEventListener.class);
        OrchidEventListener listener2 = mock(OrchidEventListener.class);
        target.initSelf(oc);
        target.addNewFileListener(listener1);
        target.addNewFileListener(listener2);
        List result = target.getEventListeners(WidgetEventListener.class);
        assertNotNull(result);
        assertTrue(result.size() == 1);
    }
}
