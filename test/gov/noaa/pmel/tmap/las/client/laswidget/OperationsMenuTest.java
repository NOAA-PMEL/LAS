/**
 * 
 */
package gov.noaa.pmel.tmap.las.client.laswidget;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import gov.noaa.pmel.tmap.las.TestMessages;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Predicate;

/**
 * @author weusijana
 * 
 */
public class OperationsMenuTest {

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

    private int tryOnWebElementCalled;
    private String thisClassSimpleName;
    private Exception tryClickingWebException;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        tryOnWebElementCalled = 0;
        thisClassSimpleName = this.getClass().getSimpleName();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    // /**
    // * Test method for {@link
    // gov.noaa.pmel.tmap.las.client.laswidget.OperationsMenu#OperationsMenu()}.
    // */
    // @Test
    // public void testOperationsMenu() {
    // fail("Not yet implemented"); // TODO
    // }

    @Test
    public void testClickSaveAsButton() {
        final WebDriver driver = new FirefoxDriver();
        driver.get(TestMessages.getString("Test" + ".baseURL") //$NON-NLS-1$
                + ":" //$NON-NLS-1$
                + TestMessages.getString("Test" + ".port") //$NON-NLS-1$
                + "/baker/UI.vm"); //$NON-NLS-1$
        /*
         * WebElement datasetButtonElement =
         * driver.findElement(By.id(TestMessages .getString(thisClassSimpleName
         * + ".DatasetButtonElementID"))); //$NON-NLS-1$ // TODO: try this click
         * multiple times datasetButtonElement.click(); WebElement
         * ocean_atlas_subset = driver.findElement(By
         * .id("ocean_atlas_subset")); // TODO: try this click multiple times
         * ocean_atlas_subset.click(); WebElement ocean_atlas_subset_radio =
         * driver .findElement(By .id(
         * "/lasdata/datasets/ocean_atlas_subset/variables/TEMP-ocean_atlas_subset_radio"
         * )); // TODO: try this click multiple times
         * ocean_atlas_subset_radio.click();
         */
        final By findSaveAsButtonElementByID = By.id(TestMessages
                .getString(thisClassSimpleName + ".SaveAsButtonElementID")); //$NON-NLS-1$
        /*
         * // Try finding saveAsButtonElement1 for 10 seconds
         * ExpectedCondition<Boolean> foundSaveAsButton = new
         * ExpectedCondition<Boolean>() { public Boolean apply(WebDriver d) {
         * WebElement saveAsButtonElement =
         * driver.findElement(findSaveAsButtonElementByID); return
         * (saveAsButtonElement != null); } }; new WebDriverWait(driver,
         * 30).until(foundSaveAsButton); final WebElement saveAsButtonElement1 =
         * driver.findElement(findSaveAsButtonElementByID);
         * Assert.assertNotNull(saveAsButtonElement1); // Wait for the button to
         * be enabled, timeout after 10 seconds WebDriverWait wait1 = new
         * WebDriverWait(driver, 10); ExpectedCondition<Boolean>
         * saveAsButtonIsEnabled = new ExpectedCondition<Boolean>() { public
         * Boolean apply(WebDriver d) { return saveAsButtonElement1.isEnabled();
         * } }; wait1.until(saveAsButtonIsEnabled);
         */
        // Wait 10 sec more before trying to click the Save As button
        final WebElement saveAsButtonElement = driver
                .findElement(findSaveAsButtonElementByID);
        Assert.assertNotNull(saveAsButtonElement);
        WebDriverWait wait10sec = new WebDriverWait(driver, 10);
        ExpectedCondition<Boolean> saveAsButtonIsEnabled = new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return saveAsButtonElement.isEnabled();
            }
        };
        wait10sec.until(saveAsButtonIsEnabled);
        saveAsButtonElement.click();

        String firstWindow = driver.getWindowHandle();

        // Wait 10 sec more before trying to click the ok button
        final By findOkElementByID = By.id(TestMessages
                .getString(thisClassSimpleName + ".okElementID")); //$NON-NLS-1$
        final WebElement okElement = driver
                .findElement(findOkElementByID);
        if ( okElement != null ) {
            ExpectedCondition<Boolean> okIsEnabled = new ExpectedCondition<Boolean>() {
                public Boolean apply(WebDriver d) {
                    return okElement.isEnabled();
                }
            };
            try {
                wait10sec.until(okIsEnabled);
                okElement.click();
            } catch ( TimeoutException te ) {
                // Ignore as the dialog might have been skipped
            }
        }

        ExpectedCondition<Boolean> thereIsMoreThanOneWindow = new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return driver.getWindowHandles().size() > 1;
            }
        };
        String click = "click";
        Object[] args = new Object[0];
        Class<?>[] paramTypes = new Class<?>[0];
        tryOnWebElement(driver, findSaveAsButtonElementByID,
                thereIsMoreThanOneWindow, click, paramTypes, args);
        for ( String handle : driver.getWindowHandles() ) {
            if ( !handle.equalsIgnoreCase(firstWindow) ) {
                driver.switchTo().window(handle);
                System.out.println("new window handle:" + handle);
            }
        }
        // TODO: if waiting longer for h2.getText()!=null doesn't work, try this
        // multiple times
        ExpectedCondition<Boolean> h2IsFound = new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return (driver.findElement(By.cssSelector("h2")) != null);
            }
        };
        wait10sec.until(h2IsFound);
        WebElement h2 = driver.findElement(By.cssSelector("h2"));
        Assert.assertNotNull(h2);
        String h2text = h2.getText();
        Assert.assertNotNull(h2text);
        System.out.println("h2text:" + h2text);
        if ( h2text.equalsIgnoreCase("Download Data") ) {
            closeWindows(driver, firstWindow);
            Assert.fail("The old data downloader was opened! h2text:" + h2text);
        }
        Assert.assertTrue(
                "The new interactive data downloader was found, but NOT displayed!",
                h2.isDisplayed());
        closeWindows(driver, firstWindow);
    }

    /**
     * @param driver
     * @param findElementBy
     * @param expectedCondition
     * @param args
     * @param paramTypes
     * @throws Exception
     */
    private synchronized void tryOnWebElement(WebDriver driver,
            By findElementBy, ExpectedCondition<Boolean> expectedCondition,
            String eleMethodName, Class<?>[] paramTypes, Object[] args) {
        tryOnWebElementCalled++;
        System.out.println("tryOnWebElementCalled:" + tryOnWebElementCalled);
        if ( tryOnWebElementCalled <= 10 ) {
            final WebElement webElement = driver.findElement(findElementBy);
            Assert.assertNotNull(webElement);

            // usually webElement.click();
            Method eleMethod = null;
            try {
                System.out.println("trying method:" + eleMethodName);
                eleMethod = webElement.getClass().getDeclaredMethod(
                        eleMethodName, paramTypes);
                eleMethod.invoke(webElement, args);
            } catch ( SecurityException e ) {
                System.err.println(e.getLocalizedMessage());
            } catch ( NoSuchMethodException e ) {
                System.err.println(e.getLocalizedMessage());
            } catch ( IllegalArgumentException e ) {
                System.err.println(e.getLocalizedMessage());
            } catch ( IllegalAccessException e ) {
                System.err.println(e.getLocalizedMessage());
            } catch ( InvocationTargetException e ) {
                System.err.println(e.getLocalizedMessage());
            }

            // Wait for expectedCondition
            WebDriverWait wait = (new WebDriverWait(driver, 30));
            try {
                wait.until(expectedCondition);
            } catch ( TimeoutException te ) {
                te.printStackTrace();
                tryClickingWebException = te;
                // Try all over again
                tryOnWebElement(driver, findElementBy, expectedCondition,
                        eleMethodName, paramTypes, args);
            } catch ( NoSuchElementException nsee ) {
                nsee.printStackTrace();
                tryClickingWebException = nsee;
                // Try all over again
                tryOnWebElement(driver, findElementBy, expectedCondition,
                        eleMethodName, paramTypes, args);
            }
            // If no exceptions are caught, reset counter and return
            tryOnWebElementCalled = 0;
            return;
        }
        // All tries failed, reset counter and fail using the latest exception
        tryOnWebElementCalled = 0;
        String message = tryClickingWebException.getLocalizedMessage();
        if ( (message == null) || (message == "") )
            message = tryClickingWebException.getMessage();
        Assert.fail(message);
    }

    /**
     * @param e
     */
    void failOnException(Exception e) {
        e.printStackTrace();
        System.err.println(e.getLocalizedMessage());
        Assert.fail(e.getClass().getName() + "\n" + e.getLocalizedMessage());
    }

    /**
     * @param driver
     * @param firstWindow
     */
    void closeWindows(final WebDriver driver, String firstWindow) {
        driver.close();
        driver.switchTo().window(firstWindow);
        driver.close();
    }

    // /**
    // * Test method for {@link
    // gov.noaa.pmel.tmap.las.client.laswidget.OperationsMenu#setMenus(gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable[],
    // java.lang.String)}.
    // */
    // @Test
    // public void testSetMenus() {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // /**
    // * Test method for {@link
    // gov.noaa.pmel.tmap.las.client.laswidget.OperationsMenu#addClickHandler(com.google.gwt.event.dom.client.ClickHandler)}.
    // */
    // @Test
    // public void testAddClickHandler() {
    // fail("Not yet implemented"); // TODO
    // }

}
