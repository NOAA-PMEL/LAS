package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.laswidget.AxisWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.DatasetButton;
import gov.noaa.pmel.tmap.las.client.laswidget.DateTimeWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.OptionsButton;
import gov.noaa.pmel.tmap.las.client.map.MapButton;
import gov.noaa.pmel.tmap.las.client.map.SettingsButton;
import gov.noaa.pmel.tmap.las.client.serializable.AxisSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.serializable.DatasetSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.TimeAxisSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.slidesorter.SlideSorterPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.Widget;

/**
 * A UI widget with one or more panels containing an LAS product with widgets to interact with the specifications of the products.
 * @author rhs
 *
 */
public class SlideSorter extends LASEntryPoint {

	/*
	 * These are the four pieces of information required
	 * to initialize a SlideSorterOld
	 */
	String dsid;
	String vid;
	String view;
	String op;
	
	/*
	 * Keep track of the axes orthogonal to the view.
	 */
	List<String> ortho;
	
	/*
	 * Keep track of which axis is in the plot panels.
	 */
	String compareAxis;
	
	/*
	 * Keep track of which axis is selected in the header.
	 */
	String fixedAxis;
	
	/*
	 * A header row with some widgets.
	 */
	Grid header;
	
	/*
	 * The slide sorter grid.
	 */
	Grid slides;
	
	/*
	 * Button to make slide sorter compute differences
	 */
	ToggleButton differenceButton;
	
	/*
	 * The panels in this slide sorter.
	 */
	List<SlideSorterPanel> panels = new ArrayList<SlideSorterPanel>();
	
	/*
	 * Padding on the right side of the browser frame...
	 */
	int rightPad = 15;
	
	/*
	 * A DateTimeWidget to globally control the time.
	 */
	DateTimeWidget dateWidget;
	
	/*
	 * String to hold the labels for the Time radio button.
	 */
	String dateButtonSelected;
	String dateButtonNotSelected;
	
	/*
	 * RadioButton to select the dateWidget as the comparison axis.
	 */
	RadioButton dateButton;
	
	/*
	 * Panel to hold the date widget and the radio button.
	 */
	HorizontalPanel datePanel = new HorizontalPanel();
	
	/*
	 * An AxisWidget to globally control the z-axis.
	 */
	AxisWidget xyzWidget;
	
	/*
	 * A Radio Button to select the Z axis as the comparison axis.
	 */
	RadioButton xyzButton;
	
	/*
	 * String to hold the labels for Z radio button.
	 */
	String xyzButtonSelected;
	String xyzButtonNotSelected;
	
	/*
	 * Panel to hold the z widget and the radio button.
	 */
	HorizontalPanel xyzPanel = new HorizontalPanel();
	
	/*
	 * A settings panel for the entire Slide Sorter.
	 */
	SettingsButton settingsButton;
	
	/*
	 * The currently selected variable.
	 */
	VariableSerializable var;
	
	/*
	 * Global min and max for setting contour levels.
	 */
	double globalMin =  999999999.;
	double globalMax = -999999999.;
	
	/*
	 * Button to set the contour levels automatically.
	 */
	ToggleButton autoContourButton;
	TextBox autoContourTextBox;
	
	/*
	 * (non-Javadoc)
	 * @see gov.noaa.pmel.tmap.las.client.LASEntryPoint#onModuleLoad()
	 */
	@Override
	public void onModuleLoad() {
		super.onModuleLoad();
		
		ortho = new ArrayList<String>();
        Map<String, List<String>> parameters = Window.Location.getParameterMap();
		
		dsid = parameters.get("dsid").get(0);
		vid = parameters.get("vid").get(0);
		//TODO If the operation is null, get the default operation (the map or plot; left nav) for this view.
		op = parameters.get("opid").get(0);
		view = parameters.get("view").get(0);
		
		
		slides = new Grid(1,2);
		header = new Grid(1, 8);
		
		differenceButton = new ToggleButton("Difference Mode");
		differenceButton.addClickListener(differencesClick);
		header.setWidget(0, 0, differenceButton);
		
		settingsButton = new SettingsButton("Settings", LatLng.newInstance(0.0, 0.0), 0, 256, 360, "Slide Sorter", op, rpcService);
		
		settingsButton.addDatasetTreeListener(datasetTreeListener);
		settingsButton.addOptionsOkClickListener(optionsOkListener);
				
		autoContourButton = new ToggleButton("Auto Set Contour Levels");
		autoContourButton.addClickListener(autoContour);
		header.setWidget(0, 6, autoContourButton);
		autoContourTextBox = new TextBox();
		header.setWidget(0, 7, autoContourTextBox);
		
		// Initialize the widgets to be used...
		if ( dsid != null && vid != null & op != null && view != null) {
			// If the proper information was sent to the widget, pull down the variable definition
			// and initialize the slide sorter with this Ajax call.
			rpcService.getCategories(dsid, initSlideSorter);
		}
		
		RootPanel.get("header").add(header);
		RootPanel.get("slides").add(slides);
		Window.addWindowResizeListener(windowResizeListener);
	}
	TreeListener datasetTreeListener = new TreeListener() {

		public void onTreeItemSelected(TreeItem item) {
			Object v = item.getUserObject();
			if ( v instanceof VariableSerializable ) {
				VariableSerializable var = (VariableSerializable) v;
				initPanels(var);
			}
		}

		public void onTreeItemStateChanged(TreeItem item) {
			// TODO Auto-generated method stub
			
		}
		
	};
	AsyncCallback initSlideSorter = new AsyncCallback() {
		public void onSuccess(Object result) {
			CategorySerializable[] cats = (CategorySerializable[]) result;
			if ( cats != null && cats.length > 1 ) {
				Window.alert("Multiple categories found.");
			} else {
				if ( cats[0].isVariableChildren() ) {
					DatasetSerializable ds = cats[0].getDatasetSerializable();
					VariableSerializable[] vars = ds.getVariablesSerializable();

					for (int i=0; i < vars.length; i++ ) {
						if ( vars[i].getID().equals(vid) ) {
							var = vars[i];
							initPanels(vars[i]);
						}
					}
				}
			}
		}
		public void onFailure(Throwable caught) {
			Window.alert("Failed to initalizes SlideSorter."+caught.toString());
		}
	};
	public WindowResizeListener windowResizeListener = new WindowResizeListener() {
		public void onWindowResized(int width, int height) {
			int pwidth = (width-rightPad)/2;
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
	    		SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
	    		panel.setPanelWidth(pwidth);
			}
		}
	};
	private void initPanels(VariableSerializable var) {
		List<AxisSerializable> axes = new ArrayList<AxisSerializable>();
		ortho.clear();
		datePanel.clear();
		xyzPanel.clear();
		settingsButton.setOperations(rpcService, null, var.getDSID(), var.getID(), null);
		if ( view.equals("xy") ) {
			// If the plot view is XY set up the map for selecting the region in all panels.
			// TODO Still need this for other views but with parameters to set the map selector tool.
			GridSerializable ds_grid = var.getGrid();
			double grid_west = Double.valueOf(ds_grid.getXAxis().getLo());
			double grid_east = Double.valueOf(ds_grid.getXAxis().getHi());

			double grid_south = Double.valueOf(ds_grid.getYAxis().getLo());
			double grid_north = Double.valueOf(ds_grid.getYAxis().getHi());

			double delta = Math.abs(Double.valueOf(ds_grid.getXAxis().getArangeSerializable().getStep()));

			LatLngBounds bounds = LatLngBounds.newInstance(LatLng.newInstance(grid_south, grid_west), LatLng.newInstance(grid_north, grid_east));
			settingsButton.getRefMap().initDataBounds(bounds, delta, true);
			settingsButton.addApplyClickListener(settingsButtonApplyListener);
			header.setWidget(0, 3, settingsButton);
		}
		// Examine the variable axes and determine which are orthogonal to the view. 

		if ( var.getGrid().getXAxis() != null && !view.contains("x") ) {
			ortho.add("x");
		}
		if ( var.getGrid().getYAxis() != null && !view.contains("y") ) {
			ortho.add("y");
		}
		if ( var.getGrid().getZAxis() != null && !view.contains("z") ) {
			ortho.add("z");
		}
		if ( var.getGrid().getTAxis() != null && !view.contains("t") ) {
			ortho.add("t");
		}

		if ( ortho.size() == 0 ) {
			Window.alert("There are no axes orthogonal to the view on which the data can be compared.");
		} else if ( ortho.size() > 2 ) { 
			Window.alert("There are "+ortho.size()+" orthogonal axes.  The SlideSorterOld only allows 2.");
		} else {
			// Build a widget for each orthogonal axis.  There should be a max of 2.
			//TODO do a better job here...
			int pos = 1;
			// Figure out which axis vary in each frame.  Take them in order of t, z, x, y...
			if ( ortho.contains("t") ) {
				compareAxis = "t";
			}  else if ( ortho.contains("z") ) {
				compareAxis = "z";
			} else if ( ortho.contains("y") ) {
				compareAxis = "y";
			} else if ( ortho.contains("x") ) {
				compareAxis = "x";
			}
			for (Iterator orthoIt = ortho.iterator(); orthoIt.hasNext();) {
				String type = (String) orthoIt.next();
				if ( type.equals("t") ) {
					// TODO  For now assuming t is a comparison axis and that is is active.
					TimeAxisSerializable axis = (TimeAxisSerializable) var.getGrid().getAxis(type);
					axes.add(axis);
					dateWidget = new DateTimeWidget(axis, false);
					dateWidget.addChangeListener(fixedAxisMenuChange);
					if ( compareAxis.equals("t") ) {
						dateWidget.setEnabled(false);
					} else {
						dateWidget.setEnabled(true);
						fixedAxis = "t";
					}
					dateButtonNotSelected = " ";
					dateButtonSelected = "Comparison Axis--";
					dateButton = new RadioButton("compare", dateButtonNotSelected);
					dateButton.addClickListener(compareAxisChangeListener);
					if ( compareAxis.equals("t") ) {
						dateButton.addStyleName("red");
						dateButton.setChecked(true);
						dateButton.setText(dateButtonSelected);
					} else {
						dateButton.addStyleName("black");
						dateButton.setText(dateButtonNotSelected);
					}
					datePanel.add(dateButton);
					datePanel.add(dateWidget);
					header.setWidget(0, pos, datePanel);
				} else {
					AxisSerializable axis = var.getGrid().getAxis(type);
					axes.add(axis);
					xyzButtonNotSelected = " ";
					xyzButtonSelected = "Comparison Axis--";					
					xyzButton = new RadioButton("compare", xyzButtonSelected);
					xyzButton.addClickListener(compareAxisChangeListener);
					xyzWidget = new AxisWidget(axis);
					
					xyzWidget.addChangeListener(fixedAxisMenuChange);
					if ( compareAxis.equals(type) ) {
						xyzButton.addStyleName("red");
						xyzButton.setChecked(true);
						xyzWidget.setEnabled(false);
						xyzButton.setText(xyzButtonSelected);
					} else {
						fixedAxis = type;
						xyzWidget.setEnabled(true);
						xyzButton.addStyleName("black");
						xyzButton.setText(xyzButtonNotSelected);
					}
					xyzPanel.add(xyzButton);
					xyzPanel.add(xyzWidget);
					header.setWidget(0, pos, xyzPanel);
				}
				pos++;
			}
			if ( ortho.size() == 1 ) {
				fixedAxis = "none";
			}
			int width = Window.getClientWidth();
			int pwidth = (width-rightPad)/2;
			SlideSorterPanel sp1 = new SlideSorterPanel("Panel 1", var, ortho, op, compareAxis, fixedAxis, view, productServer, false, rpcService);
			sp1.addRevertListener(panelApplyButtonClick);
			sp1.addApplyListener(panelApplyButtonClick);
			slides.setWidget(0, 0, sp1);
			sp1.setPanelWidth(pwidth);
			sp1.addCompareAxisChangeListener(compareAxisMenuChangeListener);
			panels.add(sp1);
			SlideSorterPanel sp2 = new SlideSorterPanel("Panel 2", var, ortho, op, compareAxis, fixedAxis, view, productServer, false, rpcService);
			sp2.addRevertListener(panelApplyButtonClick);
			sp2.addApplyListener(panelApplyButtonClick);
			//sp2.addRegionChangeListener(regionChange);
			slides.setWidget(0, 1, sp2);
			sp2.setPanelWidth(pwidth);
			sp2.addCompareAxisChangeListener(compareAxisMenuChangeListener);
			panels.add(sp2);
			refresh(false);
		}
	}
	public ClickListener compareAxisChangeListener = new ClickListener() {
		public void onClick(Widget sender) {
			if ( sender instanceof RadioButton) {
				String temp = compareAxis;
				compareAxis = fixedAxis;
				fixedAxis = temp;
				
				String fixedAxisValue = "";
				if ( compareAxis.equals("t") ) {
					dateWidget.setEnabled(false);
					dateButton.setStyleName("red");
					dateButton.setText(dateButtonSelected);
					xyzWidget.setEnabled(true);
					xyzButton.setStyleName("black");
					xyzButton.setText(xyzButtonNotSelected);
					fixedAxisValue = xyzWidget.getLo();
				}  else {
					dateWidget.setEnabled(true);
					dateButton.setStyleName("black");
					dateButton.setText(dateButtonNotSelected);
					xyzWidget.setEnabled(false);
					xyzButton.setStyleName("red");
					xyzButton.setText(xyzButtonSelected);
					fixedAxisValue = dateWidget.getFerretDateLo();
				}
				// Set the value of the fixed axis in all the panels under slide sorter control.
				for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
		    		SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
		    		if ( !panel.isUsePanelSettings() ) {
		    			panel.setAxisValue(fixedAxis, fixedAxisValue);
		    		}
				}
				refresh(true);
			}			
		}			
    };
    private void refresh(boolean switchAxis) {
    	if ( differenceButton.isDown() ) {
    		if ( autoContourButton.isDown() ) {
    			autoContourButton.setDown(false);
    			autoContourTextBox.setText("");
    		}
			SlideSorterPanel comparePanel = null;
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
				SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
				if ( panel.getID().contains("Panel 1") ) {
					comparePanel = panel;
					panel.refreshPlot(settingsButton.getOptions(), switchAxis, true);	
				}
			}
			if ( comparePanel != null ) {
				for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
					SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
					if ( !panel.getID().equals(comparePanel.getID()) ) {
						String xlo = "";
						String xhi = "";
						String ylo = "";
						String yhi = "";
						String zlo = "";
						String zhi = "";
						String tlo = "";
						String thi = "";
						if ( view.contains("x") ) {
							xlo = String.valueOf(settingsButton.getRefMap().getXlo());
							xhi = String.valueOf(settingsButton.getRefMap().getXhi());
						}
						if ( view.contains("y") ) {
							ylo = String.valueOf(settingsButton.getRefMap().getYlo());
							yhi = String.valueOf(settingsButton.getRefMap().getYhi());
						}
						if ( fixedAxis.equals("t") ) {
							tlo = dateWidget.getFerretDateLo();
							thi = dateWidget.getFerretDateHi();
						} else {
							if ( comparePanel.getVariable().getGrid().getAxis("t") != null ) {
							    tlo = comparePanel.getTlo();
							    thi = comparePanel.getThi();
							}
						}
						if ( fixedAxis.equals("z") ) {
							zlo = xyzWidget.getLo();
							zhi = xyzWidget.getHi();
						} else {
							if ( comparePanel.getVariable().getGrid().getAxis("z") != null ) {
							    zlo = comparePanel.getZlo();
							    zhi = comparePanel.getZhi();
							}
						}
						panel.computeDifference(settingsButton.getOptions(), switchAxis, comparePanel.getVariable(), view, xlo, xhi, ylo, yhi, zlo, zhi, tlo, thi);
					}
				}
			}
		} else {
			// Get the current state of the options...
			Map<String, String> temp_state = new HashMap<String, String>(settingsButton.getOptions());
			if ( autoContourButton.isDown() ) {
				// If the auto button is down, it wins...
				autoScale();
				temp_state.put("fill_levels", autoContourTextBox.getText());
			} else {
				// If it's not down, the current options value will be used.
				autoContourTextBox.setText("");
			}
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
	    		SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
	    		panel.refreshPlot(temp_state, switchAxis, true);
			}
		}
    }
    /*
     * A region change in the panel is now only going to be applied to the panel.
     * 
     * Need some controls to prevent the global settings from overriding the panel setting after a panel it settings changed.
     * 
     * 
	ChangeListener regionChange = new ChangeListener() {

		public void onChange(Widget sender) {
		    RegionWidget r = (RegionWidget) sender;
			Widget parent = sender;
			String title="";
			while ( parent != null ) {
				parent = parent.getParent();
				if ( parent instanceof DialogBox ) {
					DialogBox d = (DialogBox) parent;
					title = d.getText();
				}
			}
			List<String> applyAll = new ArrayList<String>();
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
	    		SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
	    		if ( title.contains(panel.getID())) {
	    			applyAll = panel.getApplyAll();	
	    		}
			}
			if ( applyAll.contains("x") || applyAll.contains("y") ) {
				for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
		    		SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
		    		if (!title.contains(panel.getID())) {
		    			//TODO this assumes the regions are the same in each panel.  Bad assumption.
		    			panel.setRegion(r.getSelectedIndex(), r.getValue(r.getSelectedIndex()));
		    		}
				}
    		}	
		}		
	};
	*/
    ClickListener settingsButtonApplyListener = new ClickListener() {
    	public void onClick(Widget sender) {
    		// Check to see if the operation changed.  If so, change the tool.
    		String op_id = settingsButton.getCurrentOp().getID();
    		String op_view = settingsButton.getCurrentOperationView();
    		if ( !op_id.equals(op) && !op_view.equals(view) ) {
    			op = op_id;
    			view = settingsButton.getCurrentOperationView();
    		}
    		settingsButton.setToolType(view);
    		// Update the plot based on the new settings first by moving the map settings
    		// to all the panels that are under slide sorter control, the refresh (which handles the options).
    		for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
    			SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
    			if (!panel.isUsePanelSettings()) {
    				panel.setLatLon(String.valueOf(settingsButton.getRefMap().getXlo()), String.valueOf(settingsButton.getRefMap().getXhi()), String.valueOf(settingsButton.getRefMap().getYlo()), String.valueOf(settingsButton.getRefMap().getYhi()));
    			}
    		}
    		refresh(false);
    	}
    };
    ClickListener panelApplyButtonClick = new ClickListener() {
    	public void onClick(Widget sender) {
    		String title = sender.getTitle();
    		for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
    			SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
    			if (title.contains(panel.getID())) {
    				// See if the panel settings are not being used on this panel, if not
    				// reset the fixed axis and the lat/lon region to the slide sorter value.
    				if ( !panel.isUsePanelSettings() ) {
    					VariableSerializable v = panel.getVariable();
    					if ( !v.getID().equals(var.getID()) || !v.getDSID().equals(var.getDSID() ) ) {
    						panel.setVariable(var);
    						panel.init();
    					} 
    					if ( fixedAxis.equals("t") ) {
    						panel.setAxisValue("t", dateWidget.getFerretDateLo());
    					} else if ( fixedAxis.equals("z") ) {
    						panel.setAxisValue("z", xyzWidget.getLo());
    					}
        				panel.setLatLon(String.valueOf(settingsButton.getRefMap().getXlo()), String.valueOf(settingsButton.getRefMap().getXhi()), String.valueOf(settingsButton.getRefMap().getYlo()), String.valueOf(settingsButton.getRefMap().getYhi()));
    				}
    			}
    		}
    		refresh(false);
    	}
    };
    ClickListener differencesClick = new ClickListener() {
    	public void onClick(Widget sender) {
    		refresh(false);	
    	}
    };
    public ChangeListener compareAxisMenuChangeListener = new ChangeListener() {
		public void onChange(Widget sender) {
			refresh(false);
		}
    };
	public ChangeListener fixedAxisMenuChange = new ChangeListener() {
		public void onChange(Widget sender) {
			
			if ( fixedAxis.equals("t") ) {
				
				String value = dateWidget.getFerretDateLo();
				for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
		    		SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
		    		if ( !panel.isUsePanelSettings() ) {
		    		    panel.setAxisValue("t", value);
		    		}
				}
			} else if ( fixedAxis.equals("z") ) {
	    		
	    		String value = xyzWidget.getLo();
	    		for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
		    		SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
		    		if ( !panel.isUsePanelSettings() ) {
		    		    panel.setAxisValue("z", value);
		    		}
				}
	    	}
			refresh(false);
		}   	
    };
    ClickListener optionsOkListener = new ClickListener() {
		public void onClick(Widget sender) {
			refresh(false);
		}
    };
    ClickListener autoContour = new ClickListener() {
		public void onClick(Widget sender) {
			
			refresh(false);
		}	
    };
    private void autoScale() {

    	for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
    		SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
    		if ( !differenceButton.isDown() || (differenceButton.isDown() && !panel.getID().toLowerCase().contains("panel 1")))
    			if ( panel.getMin() < globalMin ) {
    				globalMin = panel.getMin();
    			}
    		if ( panel.getMax() > globalMax ) {
    			globalMax = panel.getMax();
    		}
    	}

    	// Algorithm from range.F subroutine in Ferret source code

    	double umin = globalMin;
    	double umax = globalMax;
    	int nints = 20;

    	double temp = (umax - umin) / nints;
    	if (temp <= 0.0000000001) {
    		temp = umax;
    	}

    	double nt = Math.floor(Math.log(temp) / Math.log(10.));
    	if (temp < 1.0) {
    		nt = nt - 1;
    	}
    	double pow = Math.pow(10,nt);
    	temp = temp / pow;

    	double dint = 10.0 * pow;
    	if (temp < Math.sqrt(2.0)) {
    		dint = pow;
    	} else {
    		if (temp < Math.sqrt(10.0)) {
    			dint = 2.0 * pow;
    		} else {
    			if (temp < Math.sqrt(50.0)) {
    				dint = 5.0 * pow;
    			}
    		}
    	}

    	double fm = umin / dint;
    	double m = Math.floor(fm);
    	if (m < 0) {
    		m = m - 1;
    	}
    	double uminr = Math.round(1000000 * dint * m) / 1000000;

    	fm = umax / dint;
    	m = Math.floor(fm);
    	if (m > 0) {
    		m = m + 1;
    	}
    	double umaxr = Math.round(1000000 * dint * m) / 1000000;

    	// END OF FERRET ALGORITHM

    	// Only use 4 significant digits

    	// Modify the optionTextField and submit the request
    	String fill_levels = "(" + uminr + "," + umaxr + "," + dint + ")";
    	autoContourTextBox.setText(fill_levels);
    }
}
