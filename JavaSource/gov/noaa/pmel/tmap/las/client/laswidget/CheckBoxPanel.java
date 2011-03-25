/**
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development. 
 */
package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Used in the Time Series interfaces to control a group of check boxes.
 * @author rhs
 *
 */
public class CheckBoxPanel extends Composite {
	VerticalPanel layout;
	VerticalPanel box_layout;
	ScrollPanel scroll;
	ArrayList<CheckBox> boxes;
	Grid button_layout;
	Button plot;
	Button plot_options;
	boolean first = true;
	ClickListener listener;
	public CheckBoxPanel(ClickListener listener) {
		this.listener = listener;
		scroll = new ScrollPanel();
		scroll.setHeight("300px");
		scroll.setWidth("300px");
		layout = new VerticalPanel();
		box_layout = new VerticalPanel();
		boxes = new ArrayList<CheckBox>();
		button_layout = new Grid(1,2);
		plot_options = new Button("Plot Options");
		plot = new Button("Plot");
		button_layout.setWidget(0, 0, plot);
		button_layout.setWidget(0, 1, plot_options);
		plot.addClickListener(listener);
		plot_options.addClickListener(listener);
    	scroll.add(box_layout);
    	layout.add(scroll);
    	layout.add(button_layout);
    	hideButtons();
		initWidget(layout);
	}
	public boolean isFirst() {
		return first;
	}
	public void setFirst(boolean first) {
		this.first = first;
	}
	public void update(LatLng loc, HashMap<String, ArrayList<VariableSerializable>> dsMap) {
		box_layout.clear();
		boxes.clear();
		for (Iterator dsIt = dsMap.keySet().iterator(); dsIt.hasNext();) {
			String ds_name = (String) dsIt.next();
			ArrayList<VariableSerializable>vars = dsMap.get(ds_name);
			Label dataset = new Label(ds_name);
			Label location = new Label("Location: " + loc.toString());
			box_layout.add(dataset);
			box_layout.add(location);
			for (int i  = 0; i < vars.size(); i++) {
				VariableSerializable var = vars.get(i);
				CheckBox box = new CheckBox();
				box.setTitle(var.getID());
				box.setText(var.getName());
				box.addClickListener(listener);
				box.addClickListener(new ClickListener() {
					public void onClick(Widget box) {
						showButtons();
						setFirst(false);
					}
				});
				box_layout.add(box);
				boxes.add(box);
			}
		}
	}
	public boolean isChecked() {
		for (Iterator boxIt = boxes.iterator(); boxIt.hasNext();) {
			CheckBox box = (CheckBox) boxIt.next();
			if ( box.isChecked() ) {
				return true;
			}
		}
		return false;
	}
	public ArrayList<String> getSelected() {
		ArrayList<String> ids = new ArrayList<String>();
		for (Iterator boxIt = boxes.iterator(); boxIt.hasNext();) {
			CheckBox box = (CheckBox) boxIt.next();
			if ( box.isChecked() ) {
				ids.add(box.getTitle());
			}
		}
		return ids;
	}
	public void showButtons() {
		button_layout.setVisible(true);
	}
	public void hideButtons() {
		button_layout.setVisible(false);
	}
}
