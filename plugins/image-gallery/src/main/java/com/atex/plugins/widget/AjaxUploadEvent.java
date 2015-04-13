/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.widget;

import com.polopoly.orchid.ajax.event.StandardAjaxEvent;

public class AjaxUploadEvent extends StandardAjaxEvent {
    private static final long serialVersionUID = 1L;
    private String fileName;
    private String fileData;
    private int index;

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileData(final String fileData) {
        this.fileData = fileData;
    }

    public String getFileData() {
        return fileData;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }
}
