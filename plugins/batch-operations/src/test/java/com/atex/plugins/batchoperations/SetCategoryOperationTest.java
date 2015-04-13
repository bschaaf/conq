package com.atex.plugins.batchoperations;

import com.atex.plugins.batchoperations.SetCategoryOperation.SetCategoryWithPreservedWorkflowState;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.LockInfo;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.orchid.widget.OBatchOperation.OperationFailedException;
import com.polopoly.cm.app.widget.OPolicyWidget;
import com.polopoly.cm.client.CMServer;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.metadata.Dimension;
import com.polopoly.metadata.Entity;
import com.polopoly.metadata.Metadata;
import com.polopoly.metadata.policy.MetadataAwarePolicy;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SetCategoryOperationTest {

    private SetCategoryOperation target;
    private ContentId contentId;
    @Mock private CmClient cmClient;
    @Mock private CMServer cmServer;
    @Mock private PolicyCMServer policyCmServer;
    @Mock private LockInfo lockInfo;
    @Mock private Content content;
    @Mock private OPolicyWidget categorySelect;
    @Mock private MetadataAwarePolicy sourcePolicy;
    @Mock private MetadataAwarePolicy targetPolicy;
    private Metadata selectedCategorization;
    private VersionedContentId defaultStageVersion;
    private VersionedContentId defaultStageVersionId;
    private VersionedContentId symbolicLatestCommittedVersion;
    private VersionedContentId newVersion;
    private VersionedContentId unversionedId;
    private Metadata initialCategorization;
    private Metadata newCategorization;
    @Mock private MetadataAwarePolicy categorizationProviderPolicy;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        target = new SetCategoryOperation();
        contentId = new ContentId(1, 111);
        Dimension originalAbcDimension = new Dimension("abc", "abc", false, new Entity("bla"));
        Dimension originalCdeDimension = new Dimension("cde", "cde", false, new Entity("ble"));
        Dimension newAbcDimension = new Dimension("abc", "abc", false, new Entity("gny"));
        initialCategorization = new Metadata(originalAbcDimension, originalCdeDimension);
        selectedCategorization = new Metadata(newAbcDimension);
        newCategorization = new Metadata(newAbcDimension, originalCdeDimension);

        // Hard to mock policyWidgetUtil.createMemoryBackedPolicyWidget so we do it in a less pretty way. This way, at
        // least categorySelect may be private in class SetCategoryOperation.
        setFieldCategorySelect(target);

        defaultStageVersion = new VersionedContentId(1, 111, VersionedContentId.LATEST_COMMITTED_VERSION);
        defaultStageVersionId = new VersionedContentId(1, 111, -1001);
        symbolicLatestCommittedVersion = new VersionedContentId(1, 111, VersionedContentId.LATEST_COMMITTED_VERSION);
        newVersion = new VersionedContentId(defaultStageVersionId, VersionedContentId.NEW_VERSION);
        unversionedId = new VersionedContentId(1, 111, VersionedContentId.UNVERSIONED_VERSION);

        when(cmClient.getCMServer()).thenReturn(cmServer);
        when(cmClient.getPolicyCMServer()).thenReturn(policyCmServer);
        when(policyCmServer.getPolicyFor(content)).thenReturn(targetPolicy);

        when(cmServer.translateSymbolicContentId(defaultStageVersionId)).
            thenReturn(defaultStageVersion);

        when(cmServer.translateSymbolicContentId(symbolicLatestCommittedVersion)).
        thenReturn(new VersionedContentId(1, 111, VersionedContentId.LATEST_COMMITTED_VERSION));


        when(cmServer.createLockedContents(new VersionedContentId[]{newVersion},
                                       new VersionedContentId[]{defaultStageVersion},
                                       null)).thenReturn(new Content[]{content});

        when(categorySelect.getPolicy()).thenReturn(sourcePolicy);
        when(sourcePolicy.getMetadata()).thenReturn(selectedCategorization);

        when(targetPolicy.getChildPolicy("categorization")).thenReturn(targetPolicy);
        when(targetPolicy.getMetadata()).thenReturn(initialCategorization);

        when(categorizationProviderPolicy.getMetadata()).thenReturn(initialCategorization);
    }

    private void setFieldCategorySelect(final SetCategoryOperation instance)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = instance.getClass().getDeclaredField("categorySelect");
        field.setAccessible(true);
        field.set(instance, categorySelect);
    }

    @Test
    public void testStealingLockedContentShouldFail() throws Exception {
        when(cmServer.getLockInfo(unversionedId)).thenReturn(lockInfo);

        try {
            //execute should give exception
            target.execute(cmClient, contentId);
            fail();
        } catch (OperationFailedException e) {
            assertTrue("Expected to get OperationFailedException", true);
        }
    }

    @Test
    public void testTakingUnlockedContentShouldSucceed() throws Exception {
        when(cmServer.getLockInfo(unversionedId)).thenReturn(null);
      //execute should give exception
        target.execute(cmClient, contentId);
    }

    @Test
    public void testShouldNotOverWriteUnselectedCategories() throws Exception {
        SetCategoryWithPreservedWorkflowState state =
            new SetCategoryOperation.SetCategoryWithPreservedWorkflowState(contentId, cmClient, categorySelect);
        state.contentOperation(content);
        verify(targetPolicy).setMetadata(newCategorization);
    }

    @Test
    public void testShouldUseCategorizationProviderPolicyToSetCategorization() throws Exception {
        when(policyCmServer.getPolicyFor(content)).thenReturn(categorizationProviderPolicy);
        SetCategoryWithPreservedWorkflowState state =
            new SetCategoryOperation.SetCategoryWithPreservedWorkflowState(contentId, cmClient, categorySelect);
        state.contentOperation(content);
        verify(categorizationProviderPolicy).setMetadata(newCategorization);
    }
}
