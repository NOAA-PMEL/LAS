package gov.noaa.pmel.tmap.las.client;

import com.google.gwt.user.client.ui.RootPanel;

import gov.noaa.pmel.tmap.las.client.laswidget.DateTimeWidget;

public class DateWidgetTest extends LASEntryPoint {
    DateTimeWidget dateWidget;
    DateTimeWidget dateWidget2;
	@Override
	public void onModuleLoad() {
		super.onModuleLoad();
		dateWidget = new DateTimeWidget();
		dateWidget.init("1991-03-14", "2010-11-10", "YMD", false);
		dateWidget.setRange(true);
		dateWidget2 = new DateTimeWidget();
		dateWidget2.init("1989-03-29", "1994-12-03", "YMD", false);
		dateWidget2.setRange(true);
		RootPanel.get("example1").add(dateWidget);
		RootPanel.get("example2").add(dateWidget2);
	}
}
