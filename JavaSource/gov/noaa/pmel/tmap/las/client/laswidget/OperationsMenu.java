package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;

public class OperationsMenu extends Composite {
	// In the configuration should have metadata that indicates that the operation goes in the button bar and that it is the default
	// operation of that type for that view.  This way we could construct these "on the fly" and keep them in a ArrayList and add them
	// all at once at the end.
	HorizontalPanel buttonBar;
	OperationPushButton animationButton;
	OperationPushButton compareButton;
	OperationPushButton googleEarthButton;
	OperationPushButton showValuesButton;
	OperationPushButton downloadButton;
	boolean hasComparison = false;
	boolean hasAnimation = false;
	boolean hasGoogleEarth = false;
	ClickHandler clickHandler;
	public OperationsMenu() {
		buttonBar = new HorizontalPanel();
		initWidget(buttonBar);
	}

	public void setMenus(OperationSerializable[] ops, String view) {
		hasComparison = false;
		hasAnimation = false;
		hasGoogleEarth = false;
		buttonBar.clear();
		for (int i = 0; i < ops.length; i++) {
			OperationSerializable op = ops[i];
			String category = op.getAttributes().get("category").toLowerCase();
			List<String> views = op.getViews();
			for (Iterator viewIt = views.iterator(); viewIt.hasNext();) {
				String op_view = (String) viewIt.next();
				if ( category.equals("visualization")) {
					if ( op.getName().toLowerCase().contains("compar") ) {
						if ( op_view.equals(view) ) {
							if ( (op.getAttributes().get("private") == null || !op.getAttributes().get("private").equalsIgnoreCase("true") ) 
									&& ( op.getAttributes().get("default") != null && op.getAttributes().get("default").equalsIgnoreCase("true") ) ) {
								if ( !hasComparison ) {
									hasComparison = true;
									compareButton = new OperationPushButton(op.getName());
									compareButton.setOperation(op);
									compareButton.addClickHandler(clickHandler);
									buttonBar.add(compareButton);
								}
							}
						}
					}
				} else if ( category.contains("animation") ) {
					if ( op_view.equals(view) ) {
						if ( (op.getAttributes().get("private") == null || !op.getAttributes().get("private").equalsIgnoreCase("true") ) 
								&& ( op.getAttributes().get("default") != null && op.getAttributes().get("default").equalsIgnoreCase("true") ) ) {	
							if ( !hasAnimation ) {
								hasAnimation = true;
								animationButton= new OperationPushButton(op.getName());
								animationButton.setOperation(op);
								animationButton.addClickHandler(clickHandler);
								buttonBar.add(animationButton);
							}
						}
					}
				} else if ( category.contains("globe") ) {
					if ( op_view.equals(view) ) {
						if ( (op.getAttributes().get("private") == null || !op.getAttributes().get("private").equalsIgnoreCase("true") ) 
								&& ( op.getAttributes().get("default") != null && op.getAttributes().get("default").equalsIgnoreCase("true") ) ) {
							if ( !hasGoogleEarth ) {
								hasGoogleEarth = true;
								googleEarthButton = new OperationPushButton(op.getName());
								googleEarthButton.setOperation(op);
								googleEarthButton.addClickHandler(clickHandler);
								buttonBar.add(googleEarthButton);
							}
						}
					}
				} else if ( category.contains("table") ) {
					if ( op_view.equals(view) ) {
						if ( (op.getAttributes().get("private") == null || !op.getAttributes().get("private").equalsIgnoreCase("true") ) 
					 	  && ( op.getAttributes().get("default") != null && op.getAttributes().get("default").equalsIgnoreCase("true") ) ) {
							if ( op.getName().toLowerCase().contains("values") ) {
								showValuesButton = new OperationPushButton(op.getName());
								showValuesButton.setOperation(op);
								showValuesButton.addClickHandler(clickHandler);
								buttonBar.add(showValuesButton);
							}
							if ( op.getName().toLowerCase().contains("download") ) {
								downloadButton = new OperationPushButton(op.getName());
								downloadButton.setOperation(op);
								downloadButton.addClickHandler(clickHandler);
								buttonBar.add(downloadButton);
							}
						}
					}
				}
			}
		}
	}
    public void addClickHandler(ClickHandler clickHandler) {
    	this.clickHandler = clickHandler;   	
    }
}
