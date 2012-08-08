package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.TestMessages;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class DatasetButtonTest {

	@Test
	public void testClickDatasetButton() {
		WebDriver driver = new FirefoxDriver();
		String thisClassSimpleName = this.getClass().getSimpleName();
		driver.get(TestMessages.getString("Test" + ".baseURL") //$NON-NLS-1$
				+ ":" //$NON-NLS-1$
				+ TestMessages.getString("Test" + ".port") //$NON-NLS-1$
				+ "/baker/UI.vm"); //$NON-NLS-1$
		WebElement element = driver.findElement(By.id(TestMessages
				.getString(thisClassSimpleName + ".DatasetButtonElementID"))); //$NON-NLS-1$
		element.click();
		driver.close();
	}
}