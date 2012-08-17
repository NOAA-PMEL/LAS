package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
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
	OperationPushButton correlationButton = new OperationPushButton("Correlation");
	OperationPushButton googleEarthButton = new OperationPushButton("Google Earth");
	OperationPushButton showValuesButton = new OperationPushButton("Show Values");
	OperationPushButton exportToDesktopButton = new OperationPushButton("Export to Desktop Application");
	OperationPushButton saveAsButton = new OperationPushButton("Save As...");
	boolean hasComparison = false;
	boolean hasAnimation = false;
	boolean hasCorrelation = false;
	boolean hasGoogleEarth = false;
	ClickHandler clickHandler;
	public OperationsMenu() {
		buttonBar = new HorizontalPanel();
        turnOffButtons();
        animationButton.addStyleDependentName("SMALLER");
        compareButton.addStyleDependentName("SMALLER");
        correlationButton.addStyleDependentName("SMALLER");
        googleEarthButton.addStyleDependentName("SMALLER");
        showValuesButton.addStyleDependentName("SMALLER");
        exportToDesktopButton.addStyleDependentName("SMALLER");
        saveAsButton.addStyleDependentName("SMALLER");
        saveAsButton.ensureDebugId("saveAsButton");
		buttonBar.add(animationButton);
		buttonBar.add(correlationButton);
		buttonBar.add(googleEarthButton);
		buttonBar.add(showValuesButton);
		buttonBar.add(exportToDesktopButton);
		buttonBar.add(saveAsButton);
		initWidget(buttonBar);
		buttonBar.setSize("100%", "100%");
	}
    private void turnOffButtons() {
		animationButton.setEnabled(false);
		compareButton.setEnabled(false);
		googleEarthButton.setEnabled(false);
		showValuesButton.setEnabled(false);
		exportToDesktopButton.setEnabled(false);
		saveAsButton.setEnabled(false);
		correlationButton.setEnabled(false);
    }
	public void setMenus(OperationSerializable[] ops, String view) {
		turnOffButtons();
		hasComparison = false;
		hasAnimation = false;
		hasGoogleEarth = false;
		hasCorrelation = false;
		
	
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
								animationButton.setEnabled(true);
							}
						}
					}
				} else if ( category.contains("correlation") ) {
                    if ( op_view.equals(view) ) {
                        if ( (op.getAttributes().get("private") == null || !op.getAttributes().get("private").equalsIgnoreCase("true") ) 
                                && ( op.getAttributes().get("default") != null && op.getAttributes().get("default").equalsIgnoreCase("true") ) ) {  
                            if ( !hasCorrelation ) {
                                hasCorrelation = true;
                                correlationButton.setOperation(op);
                                correlationButton.setEnabled(true);
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
								googleEarthButton.setEnabled(true);
							}
						}
					}
				} else if ( category.contains("table") ) {
					if ( op_view.equals(view) ) {
						if ( (op.getAttributes().get("private") == null || !op.getAttributes().get("private").equalsIgnoreCase("true") ) ) {
							if ( op.getName().toLowerCase().contains("values") ) {
								showValuesButton.setOperation(op);
								showValuesButton.setEnabled(true);
							}
							if ( op.getName().toLowerCase().contains("download") ) {
								saveAsButton.setOperation(op);
								saveAsButton.setEnabled(true);
							}
						}
					}
				} else if ( category.equals("file") ) {
					if ( op_view.equals(view) ) {
						if ( (op.getAttributes().get("private") == null || !op.getAttributes().get("private").equalsIgnoreCase("true") ) ) {
							if ( op.getName().toLowerCase().contains("script") ) {
								exportToDesktopButton.setOperation(op);
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
    	compareButton.addClickHandler(clickHandler);
        animationButton.addClickHandler(clickHandler);
        correlationButton.addClickHandler(clickHandler);
        googleEarthButton.addClickHandler(clickHandler);
        showValuesButton.addClickHandler(clickHandler);
        saveAsButton.addClickHandler(clickHandler);
        exportToDesktopButton.addClickHandler(clickHandler);
    }
    public void setGoogleEarthButtonEnabled(boolean enable) {
        googleEarthButton.setEnabled(enable);
    }
    public void setCorrelationButtonEnabled(boolean b) {
        correlationButton.setEnabled(b);
    }
    public void enableByView(String view) {
        if ( animationButton.getOperation().getViews().contains(view) ) {
            animationButton.setEnabled(true);
        } else {
            animationButton.setEnabled(false);
        }
        if ( compareButton.getOperation().getViews().contains(view) ) {
            compareButton.setEnabled(true);
        } else {
            compareButton.setEnabled(false);
        }
        if ( googleEarthButton.getOperation().getViews().contains(view) ) {
            googleEarthButton.setEnabled(true);
        } else {
            googleEarthButton.setEnabled(false);
        }
        if ( showValuesButton.getOperation().getViews().contains(view) ) {
            showValuesButton.setEnabled(true);
        } else {
            showValuesButton.setEnabled(false);
        }
        if ( exportToDesktopButton.getOperation().getViews().contains(view) ) {
            exportToDesktopButton.setEnabled(true);

        } else {
            exportToDesktopButton.setEnabled(false);
        }
        if ( saveAsButton.getOperation().getViews().contains(view) ) {
            saveAsButton.setEnabled(true);
        } else {
            saveAsButton.setEnabled(false);
        }
        if ( correlationButton.getOperation().getViews().contains(view) ) {
            correlationButton.setEnabled(true);
        } else {
            correlationButton.setEnabled(false);
        }
        
    }
}
