/**
 * 
 */
package gov.noaa.pmel.tmap.las.client.laswidget;

import static org.junit.Assert.*;
import gov.noaa.pmel.tmap.las.TestMessages;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author weusijana
 * 
 */
public class HelpMenuBarImplTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    private String thisClassSimpleName;

    /*
     * (non-Javadoc)
     * 
     * @see com.google.gwt.junit.client.GWTTestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        thisClassSimpleName = this.getClass().getSimpleName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.gwt.junit.client.GWTTestCase#tearDown()
     */
    @After
    public void tearDown() throws Exception {
        // TODO
    }

//    /**
//     * Test method for
//     * {@link gov.noaa.pmel.tmap.las.client.laswidget.HelpMenuBarImpl#getAboutItem()}
//     * .
//     */
//    @Test
//    public void testGetAboutItem() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for
//     * {@link gov.noaa.pmel.tmap.las.client.laswidget.HelpMenuBarImpl#getVideoTutorialsItem()}
//     * .
//     */
//    @Test
//    public void testGetVideoTutorialsItem() {
//        fail("Not yet implemented"); // TODO
//    }

    /**
     * Test method for
     * {@link gov.noaa.pmel.tmap.las.client.laswidget.HelpMenuBarImpl#getOnlineDocsItem()}
     * .
     */
    @Test
    public void testGetOnlineDocsItem() {
        final WebDriver driver = new FirefoxDriver();
        driver.get(TestMessages.getString("Test" + ".baseURL") //$NON-NLS-1$
                + ":" //$NON-NLS-1$
                + TestMessages.getString("Test" + ".port") //$NON-NLS-1$
                + "/baker/UI.vm"); //$NON-NLS-1$

        String firstWindow = driver.getWindowHandle();
        Assert.assertNotNull(firstWindow);

        // Wait up to 10 sec while trying to click the Help Menu button
        final By findHelpMenuButtonElementByID = By.id(TestMessages
                .getString(thisClassSimpleName + ".HelpMenuButtonElementID")); //$NON-NLS-1$
        final WebElement helpMenuButtonElement = driver
                .findElement(findHelpMenuButtonElementByID);
        Assert.assertNotNull(helpMenuButtonElement);
        WebDriverWait wait10sec = new WebDriverWait(driver, 10);
        ExpectedCondition<Boolean> helpMenuButtonIsEnabled = new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return helpMenuButtonElement.isEnabled();
            }
        };
        wait10sec.until(helpMenuButtonIsEnabled);
        helpMenuButtonElement.click();
        
        // Wait up to 10 sec while trying to click the getOnlineDocsItem
        final By findGetOnlineDocsItemElementByID = By.id(TestMessages
                .getString(thisClassSimpleName + ".GetOnlineDocsItemElementID")); //$NON-NLS-1$
        final WebElement getOnlineDocsItemElement = driver
                .findElement(findGetOnlineDocsItemElementByID);
        Assert.assertNotNull(getOnlineDocsItemElement);
        ExpectedCondition<Boolean> getOnlineDocsItemIsEnabled = new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return getOnlineDocsItemElement.isEnabled();
            }
        };
        wait10sec.until(getOnlineDocsItemIsEnabled);
        getOnlineDocsItemElement.click();
        
        // TODO: Verify that the new window opened to online docs
   
        driver.close();
    }

//    /**
//     * Test method for
//     * {@link gov.noaa.pmel.tmap.las.client.laswidget.HelpMenuBarImpl#HelpMenuBarImpl()}
//     * .
//     */
//    @Test
//    public void testHelpMenuBarImpl() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for
//     * {@link gov.noaa.pmel.tmap.las.client.laswidget.HelpMenuBarImpl#setName(java.lang.String)}
//     * .
//     */
//    @Test
//    public void testSetName() {
//        fail("Not yet implemented"); // TODO
//    }
//
//    /**
//     * Test method for
//     * {@link gov.noaa.pmel.tmap.las.client.laswidget.HelpMenuBarImpl#setPresenter(gov.noaa.pmel.tmap.las.client.laswidget.HelpMenuBar.Presenter)}
//     * .
//     */
//    @Test
//    public void testSetPresenter() {
//        fail("Not yet implemented"); // TODO
//    }

    public String getModuleName() {
        return "gov.noaa.pmel.tmap.las.client.UI";
    }

}
