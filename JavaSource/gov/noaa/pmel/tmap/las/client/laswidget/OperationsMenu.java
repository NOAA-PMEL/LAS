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
/**
 * A widget that shows the "non-plot" operations from an LAS (like Google Earth, animation, etc).
 * @author rhs
 *
 */
public class OperationsMenu extends Composite {
	/*
	 * The way this menu is used in the old UI is that all of the buttons appear all at once on the UI, but they are disabled.
	 * Then when the state changes, those buttons which apply to to the new state are enabled.
	 */
	HorizontalPanel buttonBar;
	OperationPushButton animationButton = new OperationPushButton("Animate");
	OperationPushButton compareButton = new OperationPushButton("Compare");
	OperationPushButton googleEarthButton = new OperationPushButton("Google Earth");
	OperationPushButton showValuesButton = new OperationPushButton("Show Values");
	OperationPushButton exportToDesktopButton = new OperationPushButton("Export to Desktop Application");
	OperationPushButton saveAsButton = new OperationPushButton("Save As...");
	boolean hasComparison = false;
	boolean hasAnimation = false;
	boolean hasGoogleEarth = false;
	ClickHandler clickHandler;
	public OperationsMenu() {
		buttonBar = new HorizontalPanel();
        turnOffButtons();
        animationButton.addStyleDependentName("SMALLER");
        compareButton.addStyleDependentName("SMALLER");
        googleEarthButton.addStyleDependentName("SMALLER");
        showValuesButton.addStyleDependentName("SMALLER");
        exportToDesktopButton.addStyleDependentName("SMALLER");
        saveAsButton.addStyleDependentName("SMALLER");
		buttonBar.add(animationButton);
		buttonBar.add(compareButton);
		buttonBar.add(googleEarthButton);
		buttonBar.add(showValuesButton);
		buttonBar.add(exportToDesktopButton);
		buttonBar.add(saveAsButton);
		initWidget(buttonBar);
	}
    private void turnOffButtons() {
		animationButton.setEnabled(false);
		compareButton.setEnabled(false);
		googleEarthButton.setEnabled(false);
		showValuesButton.setEnabled(false);
		exportToDesktopButton.setEnabled(false);
		saveAsButton.setEnabled(false);
    }
	public void setMenus(OperationSerializable[] ops, String view) {
		turnOffButtons();
		hasComparison = false;
		hasAnimation = false;
		hasGoogleEarth = false;
		for (int i = 0; i < ops.length; i++) {
			OperationSerializable op = ops[i];
			String category = op.getAttributes().get("category").toLowerCase();
			List<String> views = op.getViews();
			for (Iterator viewIt = views.iterator(); viewIt.hasNext();) {
				String op_view = (String) viewIt.next();
				if ( category.equals("comparison")) {
					if ( op.getName().toLowerCase().contains("compar") ) {
						if ( op_view.equals(view) ) {
							if ( (op.getAttributes().get("private") == null || !op.getAttributes().get("private").equalsIgnoreCase("true") ) ) {
								if ( !hasComparison ) {
									hasComparison = true;
									compareButton.setOperation(op);
									compareButton.addClickHandler(clickHandler);
									compareButton.setEnabled(true);
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
								animationButton.setOperation(op);
								animationButton.addClickHandler(clickHandler);
								animationButton.setEnabled(true);
							}
						}
					}
				} else if ( category.contains("globe") ) {
					if ( op_view.equals(view) ) {
						if ( (op.getAttributes().get("private") == null || !op.getAttributes().get("private").equalsIgnoreCase("true") ) 
								&& ( op.getAttributes().get("default") != null && op.getAttributes().get("default").equalsIgnoreCase("true") ) ) {
							if ( !hasGoogleEarth ) {
								hasGoogleEarth = true;
								googleEarthButton.setOperation(op);
								googleEarthButton.addClickHandler(clickHandler);
								googleEarthButton.setEnabled(true);
							}
						}
					}
				} else if ( category.contains("table") ) {
					if ( op_view.equals(view) ) {
						if ( (op.getAttributes().get("private") == null || !op.getAttributes().get("private").equalsIgnoreCase("true") ) ) {
							if ( op.getName().toLowerCase().contains("values") ) {
								showValuesButton.setOperation(op);
								showValuesButton.addClickHandler(clickHandler);
								showValuesButton.setEnabled(true);
							}
							if ( op.getName().toLowerCase().contains("download") ) {
								saveAsButton.setOperation(op);
								saveAsButton.addClickHandler(clickHandler);
								saveAsButton.setEnabled(true);
							}
						}
					}
				} else if ( category.equals("file") ) {
					if ( op_view.equals(view) ) {
						if ( (op.getAttributes().get("private") == null || !op.getAttributes().get("private").equalsIgnoreCase("true") ) ) {
							if ( op.getName().toLowerCase().contains("script") ) {
								exportToDesktopButton.setOperation(op);
								exportToDesktopButton.addClickHandler(clickHandler);
								exportToDesktopButton.setEnabled(true);
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
