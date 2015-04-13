package com.atex.plugins.imagegallery;


import com.google.inject.Inject;
import com.polopoly.testnext.base.ChangeList;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnext.gui.agent.ContentCreatorAgent;
import com.polopoly.testnext.gui.agent.ContentNavigatorAgent;
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
@ImportTestContent
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
    private ToolbarAgent toolbar;

    @Inject
    private ChangeList changeList;

    @Inject
    private WebTest webTest;

    @Inject
    private WaitAgent wait;

    public static final String TEST_SITE_ID = "ImageGalleryTestIT.site";


    @Before
    public void before() {
        gui.loginIfNecessaryAsSysadmin();
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

    @Test
    public void shouldAbleCreateImageGalleryElementViaZipFile() throws Exception {
        String filePath = getFilePath("images.zip");
        String name = "image-galerry-plugin-test";

        contentNav.openContent(TEST_SITE_ID);

        creator.createContent("Main column", "Image Gallery");
        input.typeInTextfield("Name", name);

        // upload file
        webDriver.findElement(By.cssSelector("input[type='file']")).sendKeys(filePath);

        uploadFiles();

        toolbar.clickOnSaveAndFastInsert();
        toolbar.clickOnSaveAndView();

        changeList.waitFor("preview");
        webTest.loadPage(String.format("/cmlink/%s", TEST_SITE_ID));
        webTest.assertContainsXPath("//div[contains(@class, 'element-gallery')]");
    }

    public void uploadFiles() {
        webDriver.findElement(By.xpath("//button[@type='button' and contains(text(), 'Upload')]")).click();
        wait.waitForPageToLoad();
    }

    @Test
    public void shouldAbleCreateImageGalleryElement() throws Exception {
        String filePath = getFilePath("imagegallery-image1.jpg");
        String name = "image-galerry-plugin-test";

        contentNav.openContent(TEST_SITE_ID);

        creator.createContent("Main column", "Image Gallery");
        input.typeInTextfield("Name", name);

        webDriver.findElement(By.cssSelector("input[type='file']")).sendKeys(filePath);

        uploadFiles();

        toolbar.clickOnSaveAndFastInsert();
        toolbar.clickOnSaveAndView();

        changeList.waitFor("preview");
        webTest.loadPage(String.format("/cmlink/%s", TEST_SITE_ID));
        webTest.assertContainsXPath("//div[contains(@class, 'element-gallery')]");
    }

    /**
     * This testcase make sure IPTC properties are well rendered into Polopoly-CMGUI
     * File imagegallery-image2.jpg have following attributes with IPTC metadata tags.
     * 1) Object name : Polopoly
     * 2) Caption-Abstract : OLYMPUS DIGITAL CAMERA
     * 3) By-line : Atex
     */
    @Test
    public void shouldPopulatePropertiesIPTC() throws Exception {
        String filePath = getFilePath("imagegallery-image2.jpg");
        String name = "image-galerry-plugin-test";

        contentNav.openContent(TEST_SITE_ID);
        creator.createContent("Main column", "Image Gallery");
        input.typeInTextfield("Name", name);
        webDriver.findElement(By.cssSelector("input[type='file']")).sendKeys(filePath);
        uploadFiles();

        // Image uploading can take a while...
        webDriver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        try {
            WebElement element = webDriver.findElement(
                    By.xpath("//table[contains(@class, 'listentrycontainer')]//a[contains(text(), 'Polopoly')]"));

            assertEquals("Object name doesn't replace content name", "Polopoly", element.getText());
        } finally {
            webDriver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
        }

        toolbar.clickOnSaveAndFastInsert();
        toolbar.clickOnSaveAndView();
    }

    /**
     * Copy file in jar to tmp dir.
     *
     * @param file
     *            path in jar
     * @return file path in file system
     * @throws FileNotFoundException
     *             when file not found
     */
    private String getFilePath(final String file) throws FileNotFoundException {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File tmpFile = new File(tmpDir, file);
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
