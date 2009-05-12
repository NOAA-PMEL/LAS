package gov.noaa.pmel.tmap.las.client.map;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class RotateWidget extends Composite {
	
	Button left;
	Button right;
	HorizontalPanel control;
	/**
	 * A widget that centers the map at 0, 180 or at the center of the current valid data region.
	 */
	public RotateWidget () {
		control = new HorizontalPanel();
		control.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		left = new Button("<-");
		left.addStyleName("map-button");
		left.setTitle("Rotate west");
        //label = new Label("Rotate 90\u00B0");
        right = new Button("->");
        right.addStyleName("map-button");
        right.setTitle("Rotate east");
        control.add(left);
        //control.setWidget(0, 1, label);
        control.add(right);
        initWidget(control);
	}
	/**
	 * Adds an external click listener for applications that need to monitor changes
	 * @param listener the listener to add
	 */
	public void addClickListener(ClickListener listener) {
    	left.addClickListener(listener);
    	right.addClickListener(listener);
    }
	/**
	 * Sets whether the widgets are visible
	 */
	public void setVisible(boolean visible) {
		control.setVisible(visible);
	}
}
