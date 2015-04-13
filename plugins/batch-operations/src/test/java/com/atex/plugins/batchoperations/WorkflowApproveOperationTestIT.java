package com.atex.plugins.batchoperations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.testnext.base.ChangeList;
import com.polopoly.testnext.base.LocalChangeList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import com.google.inject.Inject;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.Article;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.Creator;
import com.polopoly.cm.client.Workflow;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnext.gui.agent.ClipboardAgent;
import com.polopoly.testnext.gui.agent.ContentNavigatorAgent;
import com.polopoly.testnext.gui.agent.GuiBaseAgent;
import com.polopoly.testnext.gui.agent.WaitAgent;
import com.polopoly.testnj.TestNJRunner;
import com.polopoly.user.server.Acl;
import com.polopoly.user.server.AclEntry;
import com.polopoly.user.server.AclId;
import com.polopoly.user.server.UserId;
import com.polopoly.user.server.UserServer;

@RunWith(TestNJRunner.class)
@ImportTestContent
public class WorkflowApproveOperationTestIT {
    private static final Logger LOG = Logger.getLogger(WorkflowApproveOperationTestIT.class.getName());
    private static final String SITE_EXTERNAL_ID = "WorkflowApproveOperationTestIT.content";
    @Inject
    private PolicyCMServer cmServer;
    @Inject
    private CmClient client;
    @Inject
    private UserServer userServer;
    @Inject
    private GuiBaseAgent gui;
    @Inject
    private WaitAgent wait;
    @Inject
    private ClipboardAgent clipboard;
    @Inject
    private ContentNavigatorAgent navigator;
    @Inject
    private WebDriver webDriver;
    @Inject
    private ChangeList changeList;
    @Inject
    private LocalChangeList localChangeList;

    protected VersionedContentId createArticle(final VersionedContentId workflowId) throws CMException {
        Article content = (Article) Creator.createLockedContent(client.getCMServer(), 1);
        content.setName("Workflow controlled test content");
        content.setWorkflowId(workflowId);
        content.commit();
        return content.getContentId();
    }

    protected VersionedContentId createWorkflow() throws CMException {
        Workflow content = (Workflow) Creator.createLockedContent(client.getCMServer(), 11);
        VersionedContentId workflowTypeId = cmServer.findContentIdByExternalId(new ExternalContentId(
                "p.ComplexWorkflow"));
        content.setWorkflowTypeId(workflowTypeId);
        content.commit();
        return content.getContentId();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testApproveWithPermission() throws Exception {
        VersionedContentId workflowId = createWorkflow();
        VersionedContentId articleId = createArticle(workflowId);
        Workflow workflow = (Workflow) cmServer.getContent(workflowId);
        workflow.lockMetaData();
        AclId aclId = workflow.createAcl();
        Acl acl = userServer.findAcl(aclId);
        AclEntry entry = new AclEntry(new UserId("98"));
        entry.addPermission("settoneedapproval");
        entry.addPermission("approve");
        try {
            acl.addEntry(entry, cmServer.getCurrentCaller());
        } catch (Exception e) {
            // If unable to add entry log and move on
            LOG.log(Level.WARNING, "Unable to add permission in test", e);
        }
        VersionedContentId defaultStageVersion = cmServer.translateSymbolicContentId(articleId
                .getDefaultStageVersionId());
        assertEquals("Expected no default stage version", VersionedContentId.NO_EXISTING_VERSION,
                defaultStageVersion.getVersion());
        gui.loginAsSysadmin();

        // Wait for gui changelist
        changeList.waitFor("polopoly");
        navigator.openContent(articleId.getContentIdString());
        clipboard.copyOpenedContent();
        navigator.openContent(SITE_EXTERNAL_ID);
        gui.selectTab("Advanced");
        gui.selectTab("Batch operations");
        try {
            gui.selectTab("Approve articles");
        } catch (NoSuchElementException e) {
            // Tab most likely already open.. Verifying:
            assertTrue(isElementPresent("//div[@class='tabbedMenu']/ul/li/span[contains(text(),'Approve articles')]"));
        }
        gui.selectTab("Paste from clipboard");
        gui.click("//button[@title='Paste']");
        gui.click("//button[text() = 'Approve all']");
        wait.waitForElement(By.xpath("//div[@id='progressBlock']/h2[text() = 'Operation complete']"));

        assertFalse(isTextPresent("Failed to approve content"));

        localChangeList.waitFor();
        defaultStageVersion = cmServer.translateSymbolicContentId(articleId.getDefaultStageVersionId());
        assertEquals("Expected a default stage version",
                     articleId.getContentIdString(),
                     defaultStageVersion.getContentIdString());
    }

    @Test
    public void testApproveWithoutPermission() throws Exception {
        VersionedContentId workflowId = createWorkflow();
        VersionedContentId articleId = createArticle(workflowId);
        VersionedContentId defaultStageVersion = cmServer.translateSymbolicContentId(articleId
                .getDefaultStageVersionId());
        assertEquals("Exepected no default stage version", VersionedContentId.NO_EXISTING_VERSION,
                defaultStageVersion.getVersion());
        gui.loginAsSysadmin();

        // Wait for gui changelist
        changeList.waitFor("polopoly");
        navigator.openContent(articleId.getContentIdString());
        clipboard.copyOpenedContent();
        navigator.openContent(SITE_EXTERNAL_ID);
        gui.selectTab("Advanced");
        gui.selectTab("Batch operations");
        try {
            gui.selectTab("Approve articles");
        } catch (NoSuchElementException e) {
            // Tab most likely already open.. Verifying:
            assertTrue(isElementPresent("//div[@class='tabbedMenu']/ul/li/span[contains(text(),'Approve articles')]"));
        }
        gui.selectTab("Paste from clipboard");
        gui.click("//button[@title='Paste']");
        gui.click("//button[text() = 'Approve all']");
        wait.waitForElement(By.xpath("//div[@id='progressBlock']/h2[text() = 'Operation complete']"));
        wait.waitForText("Failed to approve content");

        localChangeList.waitFor();
        defaultStageVersion = cmServer.translateSymbolicContentId(articleId.getDefaultStageVersionId());
        assertEquals("Exepected no default stage version", VersionedContentId.NO_EXISTING_VERSION,
                defaultStageVersion.getVersion());
    }

    private boolean isTextPresent(final String locator) {
        return webDriver.findElement(By.cssSelector("body")).getText().contains(locator);
    }

    private boolean isElementPresent(final String locator) {
        return webDriver.findElement(By.xpath(locator)).isDisplayed();
    }
}
