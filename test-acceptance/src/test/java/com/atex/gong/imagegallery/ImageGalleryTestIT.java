package com.atex.gong.imagegallery;


import com.atex.gong.utils.ContentTestUtil;
import com.google.inject.Inject;
import com.polopoly.testnext.base.ChangeList;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnext.gui.agent.ContentCreatorAgent;
import com.polopoly.testnext.gui.agent.ContentNavigatorAgent;
import com.polopoly.testnext.gui.agent.FrameAgent;
import com.polopoly.testnext.gui.agent.GuiBaseAgent;
import com.polopoly.testnext.gui.agent.InputAgent;
import com.polopoly.testnext.gui.agent.ToolbarAgent;
import com.polopoly.testnext.gui.agent.WaitAgent;
import com.polopoly.testnext.web.WebTest;
import com.polopoly.testnj.TestNJRunner;
import com.polopoly.util.FileUtil;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

@RunWith(TestNJRunner.class)
@ImportTestContent(
        dir = "/com/atex/gong/imagegallery/",
        files = {"ImageGalleryTestIT.content" })
public class ImageGalleryTestIT {

    @Inject
    private GuiBaseAgent gui;

    @Inject
    private ContentNavigatorAgent contentNav;

    @Inject
    private WebDriver webDriver;

    @Inject
    private ContentCreatorAgent creator;

    @Inject
    private InputAgent input;

    @Inject
    private FrameAgent frame;

    @Inject
    private ToolbarAgent toolbar;

    @Inject
    private ChangeList changeList;

    @Inject
    private WebTest webTest;

    @Inject
    private WaitAgent wait;

    @Inject
    private ContentTestUtil contentUtil;

    private String siteId;

    @Before
    public void before() {
        gui.loginIfNecessaryAsSysadmin();
        siteId = contentUtil.getTestContentId(".site");
    }

    @Test
    public void verifyImageGalleryPluginIsInstalled() throws Exception {
        String xpathExpName = "//input[contains(@value, 'com.atex.plugins.image-gallery.MainElement')]";
        String valueName = "com.atex.plugins.image-gallery.MainElement";

        contentNav.openContent(valueName);
        webDriver.findElement(By.xpath(xpathExpName));

        String valueOt = "com.atex.plugins.imagegallery.MainElement.ot";
        String xpathExpOt = "//a[contains(text(), '" + valueOt + "')]";

        webDriver.findElement(By.xpath(xpathExpOt));
    }

    public void uploadFiles() {
        webDriver.findElement(By.xpath("//button[@type='button' and contains(text(), 'Upload')]")).click();
        wait.waitForPageToLoad();
    }

    /**
     * This testcase make sure IPTC properties are well rendered into Polopoly-CMGUI
     * File imagegallery-image2.jpg have following attributes with IPTC metadata tags.
     * 1) Object name : Polopoly
     * 2) Caption-Abstract : OLYMPUS DIGITAL CAMERA
     * 3) By-line : Atex
     */
    @Test
    public void shouldPopulateTitleFromImageFile() throws Exception {
        String filePath = getFilePath("com/atex/gong/imagegallery/imagegallery-image2.jpg");
        String name = "image-gallery-plugin-test";

        contentNav.openContent(siteId);
        creator.createContent("Main column", "Image Gallery");
        input.typeInTextfield("Name", name);
        webDriver.findElement(By.cssSelector("input[type='file']")).sendKeys(filePath);
        uploadFiles();

        // Image uploading can take a while...
        webDriver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        WebElement element;
        try {
            element = webDriver.findElement(
                    By.xpath("//table[contains(@class, 'listentrycontainer')]//a[contains(text(), 'Polopoly')]"));

            assertEquals("Object name doesn't replace content name", "Polopoly", element.getText());
        } finally {
            webDriver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
        }

        frame.workFrame();
        toolbar.clickOnCancelAndClose();
    }

    /**
     * Copy file in jar to tmp dir.
     *
     * @param file
     *            path in jar
     * @return file path in file system
     * @throws java.io.FileNotFoundException
     *             when file not found
     */
    private String getFilePath(final String file) throws FileNotFoundException {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File tmpFile = new File(tmpDir, new File(file).getName());
        InputStream in = null;
        OutputStream os = null;
        try {
            in = getClass().getClassLoader().getResourceAsStream(file);
            os = new FileOutputStream(tmpFile);
            FileUtil.copyFile(in, os);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(os);
        }
        return tmpFile.getPath();
    }
}
