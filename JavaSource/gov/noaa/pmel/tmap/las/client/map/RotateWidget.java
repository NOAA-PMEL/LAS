package gov.noaa.pmel.tmap.las.client.map;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.ControlPosition;
import com.google.gwt.maps.client.control.Control.CustomControl;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class RotateWidget extends Composite {
	
	Button left;
	Button right;
	HorizontalPanel control;
	
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
	public void addClickListener(ClickListener listener) {
    	left.addClickListener(listener);
    	right.addClickListener(listener);
    }
	public void setVisible(boolean visible) {
		control.setVisible(visible);
	}
}
