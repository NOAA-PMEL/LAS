package gov.noaa.pmel.tmap.las.client.map;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.ControlPosition;
import com.google.gwt.maps.client.control.Control.CustomControl;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class RotateControl extends CustomControl {
	
	Button left;
	Label label;
	Button right;
	Grid control;
	
	public RotateControl (ControlPosition position) {
		super(position);
		control = new Grid(1,3);
		left = new Button("<-");
		left.setTitle("Rotate west");
        label = new Label("Rotate 90\u00B0");
        right = new Button("->");
        right.setTitle("Rotate east");
        control.setWidget(0, 0, left);
        control.setWidget(0, 1, label);
        control.setWidget(0, 2, right);
	}
	@Override
	protected Widget initialize(MapWidget map) {		
        return control;
	}

	@Override
	public boolean isSelectable() {
		// TODO Auto-generated method stub
		return false;
	}
	public void addClickListener(ClickListener listener) {
    	left.addClickListener(listener);
    	right.addClickListener(listener);
    }
	public void setVisible(boolean visible) {
		control.setVisible(visible);
	}
}
