package gov.noaa.pmel.tmap.las.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

import gov.noaa.pmel.tmap.las.client.laswidget.DateTimeWidget;

public class DateWidgetTest implements EntryPoint {
    DateTimeWidget dateWidget;
    DateTimeWidget dateWidget2;
	public void onModuleLoad() {
		dateWidget = new DateTimeWidget();
		dateWidget.init("1991-03-14 12:00", "2010-11-10 15:00", 3*60, "YMDT", false);
		dateWidget.setRange(true);
		dateWidget2 = new DateTimeWidget();
		dateWidget2.init("1989-03-29 00:00", "1994-12-03 18:00", 6*60, "YMDT", false);
		dateWidget2.setRange(true);
		RootPanel.get("example1").add(dateWidget);
		RootPanel.get("example2").add(dateWidget2);
	}
}
