package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.Messages;

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
		driver.get(Messages.getString("Test" + ".baseURL") //$NON-NLS-1$
				+ ":" //$NON-NLS-1$
				+ Messages.getString("Test" + ".port") //$NON-NLS-1$
				+ "/baker/VizGal.vm?dsid=coads_climatology_cdf&vid=airt&opid=Plot_2D_XY_zoom&optionid=Options_2D_image_contour_xy_7&view=xy&xlo=21&xhi=379&ylo=-89&yhi=89&tlo=15-Jan&thi=15-Jan#panelHeaderHidden=false;differences=false;autoContour=false;xDSID=coads_climatology_cdf;varid=airt;imageSize=autotoken;tlo=15-Jan;thi=15-Jan;dsid=coads_climatology_cdf;varid=airt;xlo=21;xhi=379;ylo=-89;yhi=89;xlo=21;xhi=379;ylo=-89;yhi=89;tlo=15-Jan;thi=15-Jan;operation_id=Plot_2D_XY_zoom;view=xy"); //$NON-NLS-1$
		WebElement element = driver.findElement(By.id(Messages
				.getString(thisClassSimpleName + ".DatasetButtonElementID"))); //$NON-NLS-1$
		element.click();
		driver.close();
	}
}