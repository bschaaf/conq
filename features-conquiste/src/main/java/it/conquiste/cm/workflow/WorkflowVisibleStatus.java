package it.conquiste.cm.workflow;

import com.polopoly.cm.client.CMException;

public interface WorkflowVisibleStatus {
    public boolean isVisible() throws CMException;

    public boolean hasVisible();
}
