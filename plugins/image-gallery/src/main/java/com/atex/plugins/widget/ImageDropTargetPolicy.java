/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.widget;

import com.polopoly.cm.policy.PolicyImplBase;
import com.polopoly.cm.policy.PolicyUtil;

public class ImageDropTargetPolicy extends PolicyImplBase {

    private int maxFileSize;
    private int maxNofFile;

    @Override
    protected void initSelf() {
        maxFileSize = PolicyUtil.getParameterAsInt("maxFileSize", -1, this);
        maxNofFile = PolicyUtil.getParameterAsInt("maxNofFile", -1, this);
    }

    protected int getMaxFileSize() {
        return maxFileSize;
    }

    protected int getMaxNofFile() {
        return maxNofFile;
    }
}
