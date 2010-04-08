package gov.noaa.pmel.tmap.las.client.laswidget;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;


public class ComparisonAxisSelector extends Composite {
	ListBox axes;
	
	public ComparisonAxisSelector(String width) {

		axes = new ListBox();
		
		
		DisclosurePanel disPanel = new DisclosurePanel("Select axis to vary in panels:");
		disPanel.add(axes);
		disPanel.setOpen(true);
		
		if ( width != null && !width.equals("") ) {
		    disPanel.setWidth(width);
		}
		initWidget(disPanel);
	}
	public void setAxes(List<String> ortho) {
		if ( axes == null ) {
			axes = new ListBox();
		}
		axes.clear();
		StringBuffer map_axes = new StringBuffer();
		for (Iterator orthoIt = ortho.iterator(); orthoIt.hasNext();) {
			String axis = (String) orthoIt.next();
			if ( !axis.equals("x") && !axis.equals("y") ) {
				axes.addItem(axis, axis);
			} else {
				map_axes.append(axis);
			}
		}
		if ( map_axes.length() == 1 ) {
			axes.addItem(map_axes.toString(), map_axes.toString());
		} else if ( map_axes.length() == 2 ) {
			axes.addItem("xy", "xy");
		}
	}
	public void setValue(String value) {
		for( int i = 0; i < axes.getItemCount(); i++) {
			if ( value.equals(axes.getValue(i) ) ) {
				axes.setSelectedIndex(i);
			}
		}
	}
	public String getValue() {
		return axes.getValue(axes.getSelectedIndex());
	}
	public void addAxesChangeHandler(ChangeHandler compareAxisChangeHandler) {
		axes.addChangeHandler(compareAxisChangeHandler);	
	}

}
