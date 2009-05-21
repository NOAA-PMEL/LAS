package gov.noaa.pmel.tmap.las.client.laswidget;

import com.google.gwt.user.client.ui.ListBox;

public class HourListBox extends ListBox {
	public void addItem(int hour, int min) {
		addItem(Util.format_two(hour)+":"+Util.format_two(min), Util.format_two(hour)+":"+Util.format_two(min));
	}
    public int getHour() {
    	String selected_time = getValue(getSelectedIndex());
    	return Integer.valueOf(selected_time.substring(0, selected_time.indexOf(":"))).intValue();
    }
    public int getMin() {
    	String selected_time = getValue(getSelectedIndex());
    	return Integer.valueOf(selected_time.substring(selected_time.indexOf(":")+1)).intValue();
    }
}
