package gov.noaa.pmel.tmap.las.client;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import gov.noaa.pmel.tmap.las.Messages;
import gov.noaa.pmel.tmap.las.client.laswidget.AxisWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.DateTimeWidget;
import gov.noaa.pmel.tmap.las.client.rpc.RPCServiceAsync;
import gov.noaa.pmel.tmap.las.client.serializable.ArangeSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.AxisSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.TimeAxisSerializable;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.octo.gwt.test.GwtTestWithMockito;
import com.octo.gwt.test.utils.GwtReflectionUtils;
import com.octo.gwt.test.utils.events.Browser;

/**
 * Unit tests for InteractiveDownloadData
 * 
 * @author weusijana
 */
public class InteractiveDownloadDataTest extends GwtTestWithMockito {
	// @Mock tells gwt-test-utils to create a mock object using Mockito. Both
	// gwt-test-utils or Mockito annotation can be used.
	@Mock
	private RPCServiceAsync lasRPCServiceAsync;
	private InteractiveDownloadData underTest;

	/**
	 * Must refer to a valid module that sources this class.
	 */
	@Override
	public String getModuleName() {
		return "gov.noaa.pmel.tmap.las.InteractiveDownloadData"; //$NON-NLS-1$
	}

	/**
	 * Setup for tests
	 */
	@Before
	public void init() {
		underTest = new InteractiveDownloadData();
		Assert.assertNotNull(underTest);
	}

	@Test
	public void testOnModuleLoad() {
		underTest.init();
		String dsID = Messages.getString("InteractiveDownloadDataTest.dsID"); //$NON-NLS-1$
		String varID = Messages.getString("InteractiveDownloadDataTest.varID"); //$NON-NLS-1$
		GwtReflectionUtils.setPrivateFieldValue(underTest, "dsID", dsID); //$NON-NLS-1$
		GwtReflectionUtils.setPrivateFieldValue(underTest, "varID", varID); //$NON-NLS-1$
		// TODO: set lasRequestXMLString

		// Check that underTest is not ready to save data
		Button saveButton = GwtReflectionUtils.getPrivateFieldValue(underTest,
				"saveButton"); //$NON-NLS-1$
		Assert.assertNotNull(saveButton);
		Assert.assertFalse(saveButton.isEnabled());

		// Mock RPC calls using Mockito.
		GridSerializable grid = new GridSerializable();
		grid.setID(Messages.getString("InteractiveDownloadDataTest.gridID")); //$NON-NLS-1$
		// GwtTestWithMockito.doSuccessCallback(Object result) tells Mockito to
		// make the mocked object return the "result" object as a success
		// response.
		doSuccessCallback(grid).when(lasRPCServiceAsync).getGrid(eq(dsID),
				eq(varID), any(AsyncCallback.class));
		// Get the grid and generate dependent widgets
		AsyncCallback gridCallback = underTest.onGotGrid;
		lasRPCServiceAsync.getGrid(dsID, varID, gridCallback);

		// verify(Object mock) is a Mockito static method to check that a method
		// was actually called with expected parameters.
		verify(lasRPCServiceAsync).getGrid(eq(dsID), eq(varID),
				any(AsyncCallback.class));

		// Check that underTest got the grid, built its components, and is ready
		// to save data
		ListBox dataFormatComboBox = GwtReflectionUtils.getPrivateFieldValue(
				underTest, "dataFormatComboBox"); //$NON-NLS-1$
		Assert.assertNotNull(dataFormatComboBox);
		Assert.assertTrue(dataFormatComboBox.isEnabled());
		Assert.assertTrue(saveButton.isEnabled());
		String dataFormat = dataFormatComboBox.getValue(dataFormatComboBox
				.getSelectedIndex());
		Assert.assertTrue("dataFormat.equalsIgnoreCase(\"NetCDF\")", //$NON-NLS-1$
				dataFormat.equalsIgnoreCase("NetCDF")); //$NON-NLS-1$

		// Choose arcGrid format
		dataFormatComboBox.setSelectedIndex(3);
		dataFormat = dataFormatComboBox.getValue(dataFormatComboBox
				.getSelectedIndex());
		Assert.assertTrue("dataFormat.equalsIgnoreCase(\"arcGrid\")", //$NON-NLS-1$
				dataFormat.equalsIgnoreCase("arcGrid")); //$NON-NLS-1$
		// TODO: Check that the correct changes in the widget UI happened
		// (perhaps in interaction tests)

		// choose CSV format
		dataFormatComboBox.setSelectedIndex(2);
		dataFormat = dataFormatComboBox.getValue(dataFormatComboBox
				.getSelectedIndex());
		Assert.assertTrue("dataFormat.equalsIgnoreCase(\"CSV\")", //$NON-NLS-1$
				dataFormat.equalsIgnoreCase("CSV")); //$NON-NLS-1$
		// TODO: Check that the correct changes in the widget UI happened
		// (perhaps in interaction tests)

		// TODO: Assert that the proper download occurs when the button is
		// clicked (perhaps in interaction tests)
		// Browser.click(saveButton);
	}

	@Test
	public void testClicksaveButton() {
		WebDriver driver = new FirefoxDriver();
		String thisClassSimpleName = this.getClass().getSimpleName();
		driver.get(Messages.getString("Test" + ".baseURL") //$NON-NLS-1$ //$NON-NLS-2$
				+ ":" //$NON-NLS-1$
				+ Messages.getString("Test" + ".port") //$NON-NLS-1$ //$NON-NLS-2$
				+ Messages.getString("InteractiveDownloadDataTest.defaultInteractiveDownlodDataURL")); //$NON-NLS-1$
		WebElement element = driver.findElement(By.className(Messages
				.getString(thisClassSimpleName + ".saveButtonClassName"))); //$NON-NLS-1$
		element.click();
		driver.close();
	}
}
