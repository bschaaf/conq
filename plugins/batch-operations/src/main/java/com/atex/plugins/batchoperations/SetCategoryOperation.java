package com.atex.plugins.batchoperations;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.orchid.widget.OBatchOperation.ContentOperation;
import com.polopoly.cm.app.orchid.widget.OBatchOperation.OperationFailedException;
import com.polopoly.cm.app.util.PolicyWidgetUtil;
import com.polopoly.cm.app.widget.OComplexPolicyWidget;
import com.polopoly.cm.app.widget.OPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.OperationFailureException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.workflow.WorkflowStatePreservingContentOperation;
import com.polopoly.cm.workflow.WorkflowStatePreservingContentOperation.LatestCommittedIsNotDefaultStageException;
import com.polopoly.cm.workflow.WorkflowStatePreservingContentOperation.NoLatestCommittedVersionException;
import com.polopoly.metadata.Dimension;
import com.polopoly.metadata.Metadata;
import com.polopoly.metadata.MetadataAware;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.widget.OTextOutput;
import com.polopoly.util.LocaleUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Content operation that replaces a content's categorization with the selected
 * categorization. Only replaces categorization in those dimensions where a
 * selection has been made, so can't be used to clear categorization. This is to
 * simplify the GUI so that we don't have to have checkboxes for each dimension
 * saying whether to clear or ignore them.
 */
public class SetCategoryOperation extends OComplexPolicyWidget implements ContentOperation {
    private static final long serialVersionUID = 1L;

    private static final String OPERATION_NAME = "com.atex.plugins.batchoperations.CategorizationBatchOperation";
    private static final String NO_LATEST_COMMITTED =
            "com.atex.plugins.batchoperations.SetCategoryOperation.noLatestCommitted";
    private static final String CONFLICT = "com.atex.plugins.batchoperations.SetCategoryOperation.conflict";
    private static final String OPERATION_FAILED = "com.atex.plugins.batchoperations.SetCategoryOperation.failure";
    private static final String NOT_CATEGORIZABLE =
            "com.atex.plugins.batchoperations.SetCategoryOperation.notCategorizable";
    private static final String CATEGORIZATION_WIDGET = "p.Metadata";

    private OPolicyWidget categorySelect;
    private OTextOutput helpText;

    @Override
    public void initSelf(final OrchidContext oc) throws OrchidException {
        helpText = new OTextOutput();
        addAndInitChild(oc, helpText);
        helpText.setText(LocaleUtil.format("com.atex.plugins.batchoperations.SetCategoryOperation.help",
                                            oc.getMessageBundle()));
        try {
            categorySelect = (OPolicyWidget) PolicyWidgetUtil.createMemoryBackedPolicyWidget(oc, getContentSession(),
                    getPolicy().getContentId(), CATEGORIZATION_WIDGET, true);
        } catch (CMException e) {
            throw new OrchidException(e);
        }
        addAndInitChildPolicyWidget(oc, categorySelect);
    }

    @Override
    public void execute(final CmClient cmClient, final ContentId id) throws OperationFailedException {
        ContentId unversionedId = id.getOtherVersionId(VersionedContentId.UNVERSIONED_VERSION);
        SetCategoryWithPreservedWorkflowState op = new SetCategoryWithPreservedWorkflowState(id, cmClient,
                categorySelect);

        try {
            if (cmClient.getCMServer().getLockInfo(unversionedId) != null) {
                throw new OperationFailureException("Content was locked");
            }
            op.execute();
        } catch (OperationFailedException e) {
            throw e;
        } catch (NoLatestCommittedVersionException e) {
            throw new OperationFailedException(e.getMessage(), NO_LATEST_COMMITTED, e);
        } catch (LatestCommittedIsNotDefaultStageException e) {
            throw new OperationFailedException(e.getMessage(), CONFLICT, e);
        } catch (Exception e) {
            throw new OperationFailedException("Could not perform operation.", OPERATION_FAILED, e);
        }
    }

    @Override
    public String getName() {
        return OPERATION_NAME;
    }

    @Override
    public void localRender(final OrchidContext oc) throws IOException, OrchidException {
        String name = LocaleUtil.format(getName(), oc.getMessageBundle());
        oc.getDevice().println("<h2>" + name + "</h2>");
        helpText.render(oc);
        categorySelect.render(oc);
    }

    public static class SetCategoryWithPreservedWorkflowState extends WorkflowStatePreservingContentOperation {

        private final PolicyCMServer policyCmServer;
        private final OPolicyWidget categorySelect;

        public SetCategoryWithPreservedWorkflowState(final ContentId contentId, final CmClient cmClient,
                final OPolicyWidget categorySelect) {
            super(contentId, cmClient.getCMServer());
            this.categorySelect = categorySelect;
            policyCmServer = cmClient.getPolicyCMServer();
        }

        @Override
        protected void contentOperation(final Content content) throws Exception {
            MetadataAware source = (MetadataAware) categorySelect.getPolicy();
            MetadataAware target = getMetadataAware(policyCmServer.getPolicyFor(content));

            Metadata newCategorization = filterOutDimensionsWithNoSelectedCategories(source.getMetadata());
            newCategorization = mergeInUnchangedCategorization(target, newCategorization);

            target.setMetadata(newCategorization);
        }

        private Metadata mergeInUnchangedCategorization(final MetadataAware target, final Metadata newCategorization)
                throws CMException {
            for (Dimension dim : target.getMetadata().getDimensions()) {
                if (newCategorization.getDimensionById(dim.getId()) == null) {
                    newCategorization.addDimension(dim);
                }
            }
            return newCategorization;
        }

        private Metadata filterOutDimensionsWithNoSelectedCategories(final Metadata cat) {
            List<Dimension> dimensions = new ArrayList<Dimension>();
            for (Dimension dim : cat.getDimensions()) {
                if (!dim.getEntities().isEmpty()) {
                    dimensions.add(dim);
                }
            }
            return new Metadata(dimensions);
        }

        public MetadataAware getMetadataAware(final Policy policy) throws CMException, OperationFailedException {
            if (policy instanceof MetadataAware) {
                return (MetadataAware) policy;
            }
            Policy metadata = policy.getChildPolicy("categorization");
            if (metadata instanceof MetadataAware) {
                return (MetadataAware) metadata;
            }
            throw new OperationFailedException("Can't set category. Skipping.", NOT_CATEGORIZABLE, null);
        }

        @Override
        protected boolean failIfLatestCommittedIsNotDefaultStage() {
            return true;
        }
    }
}
