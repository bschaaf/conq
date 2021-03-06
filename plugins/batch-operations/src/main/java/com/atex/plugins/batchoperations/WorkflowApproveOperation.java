package com.atex.plugins.batchoperations;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.orchid.widget.OBatchOperation.ContentOperation;
import com.polopoly.cm.app.orchid.widget.OBatchOperation.OperationFailedException;
import com.polopoly.cm.app.widget.OComplexPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CMServer;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.client.OperationFailureException;
import com.polopoly.cm.client.WorkflowAware;
import com.polopoly.cm.workflow.WorkflowAction;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.widget.OTextOutput;
import com.polopoly.util.LocaleUtil;

/**
 * Content operation that workflow approves content. Recognizes workflow types
 * "p.SimpleWorkflow" and "p.ComplexWorkflow". Content in Draft or NeedApprove
 * state will be approved by the {@link #execute(CmClient, ContentId)} method.
 */
public class WorkflowApproveOperation extends OComplexPolicyWidget implements ContentOperation {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(WorkflowApproveOperation.class.getName());
    private static final String APPROVED_STATE_NAME = "Approved";
    private static final String DRAFT_STATE_NAME = "Draft";
    private static final String NEED_APPROVE_STATE_NAME = "NeedApprove";
    private static final String APPROVE_ACTION_NAME = "approve";
    private static final String SET_ON_NEED_APPROVAL_ACTION_NAME = "settoneedapproval";

    private static final String OPERATION_NAME = "com.atex.plugins.batchoperations.WorkflowApproveOperation.name";
    private static final String NO_WORKFLOW_ACTION =
            "com.atex.plugins.batchoperations.WorkflowApproveOperation.noWorkflowAction";

    private OTextOutput helpText;

    @Override
    public void initSelf(final OrchidContext oc) throws OrchidException {
        helpText = new OTextOutput();
        addAndInitChild(oc, helpText);
        helpText.setText(LocaleUtil.format("com.atex.plugins.batchoperations.WorkflowApproveOperation.help",
                                            oc.getMessageBundle()) + "\n");
    }

    @Override
    public void localRender(final OrchidContext oc) throws IOException, OrchidException {
        String name = LocaleUtil.format(getName(), oc.getMessageBundle());
        oc.getDevice().println("<h2>" + name + "</h2>");
        helpText.render(oc);
    }

    @Override
    public void execute(final CmClient cmClient, final ContentId id) throws OperationFailedException {
        CMServer cmServer = cmClient.getCMServer();

        try {
            ContentId unversionedId = id.getOtherVersionId(VersionedContentId.UNVERSIONED_VERSION);

            if (cmServer.getLockInfo(unversionedId) != null) {
                throw new OperationFailureException("Content was locked");
            }

            ContentRead content = cmServer.getContent(id.getLatestCommittedVersionId());

            if (!(content instanceof WorkflowAware)) {
                // Not workflow aware --> nothing to do
                return;
            }

            WorkflowAware workflowAware = (WorkflowAware) content;
            VersionedContentId workflowId = workflowAware.getWorkflowId();

            if (workflowId == null) {
                // No workflow --> nothing to do
                return;
            }

            String currentStateName = workflowAware.getWorkflowState().getName();

            if (APPROVED_STATE_NAME.equals(currentStateName)) {
                // Already in approved state --> nothing to do
                LOG.log(Level.FINEST, "Encountered already approved content");
            }

            approve(workflowAware);
        } catch (CMException e) {
            throw new OperationFailedException(e.getMessage(), "FAILED", e);
        }
    }

    private void approve(final WorkflowAware workflowAware) throws CMException, OperationFailedException {
        String currentStateName = workflowAware.getWorkflowState().getName();

        while (!APPROVED_STATE_NAME.equals(currentStateName)) {
            // Note: getWorkflowActions() has built in permission check
            WorkflowAction actionToPerform = findNextWorkflowAction(currentStateName,
                    workflowAware.getWorkflowActions());

            if (actionToPerform == null) {
                throw new OperationFailedException("No permitted workflow action for caller to get from "
                        + currentStateName + " to " + APPROVED_STATE_NAME, NO_WORKFLOW_ACTION, null);
            }

            workflowAware.doWorkflowAction(actionToPerform);
            currentStateName = workflowAware.getWorkflowState().getName();
        }
    }

    /**
     * Tries to find an appropriate workflow action to get one step closer to
     * approved state. Modify this method if you want to support other workflow
     * types than p.SimpleWorkflow and p.ComplexWorkflow.
     */
    private WorkflowAction findNextWorkflowAction(final String currentStateName,
                                                    final WorkflowAction[] workflowActions) {
        WorkflowAction actionToPerform = null;

        for (int i = 0; actionToPerform == null && i < workflowActions.length; i++) {
            String actionName = workflowActions[i].getName();

            if ((APPROVE_ACTION_NAME.equals(actionName)
                    && (NEED_APPROVE_STATE_NAME.equals(currentStateName)
                            || DRAFT_STATE_NAME
                    .equals(currentStateName)))
                    || (SET_ON_NEED_APPROVAL_ACTION_NAME.equals(actionName) && DRAFT_STATE_NAME
                            .equals(currentStateName))) {

                actionToPerform = workflowActions[i];
            }
        }
        return actionToPerform;
    }

    @Override
    public String getName() {
        return OPERATION_NAME;
    }
}
