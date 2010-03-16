package gov.noaa.pmel.tmap.las.client.laswidget;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;


public class ComparisonAxisSelector extends Composite {
	ListBox axes;
	FlexTable layout = new FlexTable();
	public ComparisonAxisSelector(List<String> ortho) {
		FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

	    // Add a title to the form
	    layout.setHTML(0, 0, "Other Axes");
	    cellFormatter.setColSpan(0, 0, 2);
	    cellFormatter.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
	    //cellFormatter.setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);

		axes = new ListBox();
		for (Iterator orthoIt = ortho.iterator(); orthoIt.hasNext();) {
			String axis = (String) orthoIt.next();
			axes.addItem(axis, axis);
		}
		layout.setHTML(1, 0, "Select Axis to Vary In Panels");
		layout.setWidget(1, 1, axes);
		DecoratorPanel decPanel = new DecoratorPanel();
		decPanel.add(layout);
		decPanel.setWidth("275px");
		initWidget(decPanel);
	}
	public void setAxes(List<String> ortho) {
		if ( axes == null ) {
			axes = new ListBox();
		}
		axes.clear();
		for (Iterator orthoIt = ortho.iterator(); orthoIt.hasNext();) {
			String axis = (String) orthoIt.next();
			axes.addItem(axis, axis);
		}
	}
	public void setValue(String value) {
		for( int i = 0; i < axes.getItemCount(); i++) {
			if ( value.equals(axes.getValue(i) ) ) {
				axes.setSelectedIndex(i);
			}
		}
	}
	public void setFixedAxisWidget(Widget w) {
		FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();
		layout.setHTML(2, 0, "Fixed Axis Value:&nbsp;");
		layout.setWidget(3, 0, w);
		cellFormatter.setColSpan(2, 0, 2);
		cellFormatter.setColSpan(2, 1, 2);
	}
	public void addAxesChangeHandler(ChangeHandler compareAxisChangeHandler) {
		axes.addChangeHandler(compareAxisChangeHandler);	
	}

}
