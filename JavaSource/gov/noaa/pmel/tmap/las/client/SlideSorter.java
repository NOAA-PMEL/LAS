package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.laswidget.AxisWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.DateTimeWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.Util;
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
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
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
	 * Grid for the Global ZT selector.
	 */
	FlexTable ztGrid;
	
	/*
	 * Label for compare axis selector
	 */
	Label varyAxis;
	
	/*
	 * Keep track of the new variable and change state to be able apply data set changes only after apply button is pressed.
	 */
	VariableSerializable nvar;
	boolean changeDataset = false;
	String tridown;
	String triright;
	
	/*
	 * Button for showing and hiding the panel headers.
	 */
	Image showHide;
	boolean panelHeaderHidden;
	
	/*
	 * Image size control.
	 */
	ListBox imageSize;
	Label imageSizeLabel = new Label("Image zoom: ");
	int pwidth = 0;
	
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
		
		
		slides = new Grid(2,2);
		header = new Grid(1, 10);
		
		ztGrid = new FlexTable();
		varyAxis = new Label("Select Axis to Vary in Panels");
		varyAxis.addStyleName("las-align-center");
		ztGrid.setWidget(0, 0, varyAxis);
		ztGrid.getFlexCellFormatter().setColSpan(0, 0, 2);
		ztGrid.addStyleName("LSS_middle");
		header.setWidget(0, 2, ztGrid);
		
		panelHeaderHidden = false;
		tridown = Util.getImageURL()+"tri-down.png";
		triright = Util.getImageURL()+"tri-right.png";
		showHide = new Image(tridown);
		header.setWidget(0, 0, showHide);
		showHide.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				if ( panelHeaderHidden ) {
					for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
			    		SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
			    		panel.show();
					}
					showHide.setUrl(tridown);
				} else {
					for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
			    		SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
			    		panel.hide();
					}
					showHide.setUrl(triright);
				}
				panelHeaderHidden = !panelHeaderHidden;
			}
			
		});
		showHide.setTitle("Show/Hide Panel Headers");
		differenceButton = new ToggleButton("Difference Mode");
		differenceButton.setTitle("Toggle Difference Mode");
		differenceButton.addClickListener(differencesClick);
		header.setWidget(0, 1, differenceButton);
		
		settingsButton = new SettingsButton("Gallery Settings", LatLng.newInstance(0.0, 0.0), 0, 256, 360, "Slide Sorter", op, rpcService);
		settingsButton.setTitle("Settings for all panels.");
		settingsButton.addDatasetTreeListener(datasetTreeListener);
		settingsButton.addOptionsOkClickListener(optionsOkListener);
		settingsButton.addOperationClickListener(operationsClickListener);
				
		autoContourButton = new ToggleButton("Auto Set Color Fill Levels for Gallery");
		autoContourButton.setTitle("Set consistent contour levels for all panels.");
		autoContourButton.addClickListener(autoContour);
		header.setWidget(0, 4, autoContourButton);
		autoContourTextBox = new TextBox();
		header.setWidget(0, 5, autoContourTextBox);
		
		imageSize = new ListBox();
		imageSize.addItem("Auto", "auto");
		imageSize.addItem("100%", "100");
		imageSize.addItem(" 90%", "90");
		imageSize.addItem(" 80%", "80");
		imageSize.addItem(" 70%", "70");
		imageSize.addItem(" 60%", "60");
		imageSize.addItem(" 50%", "50");
		imageSize.addItem(" 40%", "40");
		imageSize.addItem(" 30%", "30");
		
		imageSize.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				String value = imageSize.getValue(imageSize.getSelectedIndex());
				if ( !value.equals("auto") ) {
					for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
						SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
						panel.setImageSize(Integer.valueOf(value).intValue());
					}
				} else {
					if ( pwidth == 0 ) {
						pwidth = 400;
					}
					for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
						SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
						panel.setPanelWidth(pwidth);
					}
				}
			}		
		});
		header.setWidget(0, 6, imageSizeLabel);
		header.setWidget(0, 7, imageSize);
		
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
				nvar = (VariableSerializable) v;
				if ( nvar.getAttributes().get("grid_type").equals("regular") ) {
					changeDataset = true;
				} else {
					nvar = var;
					Window.alert("visGal cannot work with scattered data at this time.");
				}
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
						}
					}
				}
				initPanels();
				refresh(false);
			}
		}
		public void onFailure(Throwable caught) {
			Window.alert("Failed to initalizes SlideSorter."+caught.toString());
		}
	};
	public WindowResizeListener windowResizeListener = new WindowResizeListener() {
		public void onWindowResized(int width, int height) {
			pwidth = (width-rightPad)/2;
			if ( imageSize.getValue(imageSize.getSelectedIndex()).equals("auto") ) {
				for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
					SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
					panel.setPanelWidth(pwidth);
				}
			}
		}
	};
	private void initPanels() {

		ortho.clear();
		datePanel.clear();
		xyzPanel.clear();
		panels.clear();

		settingsButton.addApplyClickListener(settingsButtonApplyListener);
		header.setWidget(0, 3, settingsButton);


		int width = Window.getClientWidth();
		int pwidth = (width-rightPad)/2;
		SlideSorterPanel sp1 = new SlideSorterPanel("Panel 0", true, op, view, productServer, false, rpcService);
		sp1.addRevertListener(panelApplyButtonClick);
		sp1.addApplyListener(panelApplyButtonClick);
		slides.setWidget(0, 0, sp1);
		sp1.setPanelWidth(pwidth);
		sp1.addCompareAxisChangeListener(axisMenuChangeListener);
		sp1.addZChangeListner(axisMenuChangeListener);
		sp1.addTChangeListner(axisMenuChangeListener);
		panels.add(sp1);
		
		SlideSorterPanel sp2 = new SlideSorterPanel("Panel 1", false, op, view, productServer, false, rpcService);
		sp2.addRevertListener(panelApplyButtonClick);
		sp2.addApplyListener(panelApplyButtonClick);
		//sp2.addRegionChangeListener(regionChange);
		slides.setWidget(0, 1, sp2);
		sp2.setPanelWidth(pwidth);
		sp2.addCompareAxisChangeListener(axisMenuChangeListener);
		sp2.addZChangeListner(axisMenuChangeListener);
		sp2.addTChangeListner(axisMenuChangeListener);		
		panels.add(sp2);
		
		SlideSorterPanel sp3 = new SlideSorterPanel("Panel 2", false, op, view, productServer, false, rpcService);
		sp3.addRevertListener(panelApplyButtonClick);
		sp3.addApplyListener(panelApplyButtonClick);
		//sp2.addRegionChangeListener(regionChange);
		slides.setWidget(1, 0, sp3);
		sp3.setPanelWidth(pwidth);
		sp3.addCompareAxisChangeListener(axisMenuChangeListener);
		sp3.addZChangeListner(axisMenuChangeListener);
		sp3.addTChangeListner(axisMenuChangeListener);		
		panels.add(sp3);
		
		SlideSorterPanel sp4 = new SlideSorterPanel("Panel 3", false, op, view, productServer, false, rpcService);
		sp4.addRevertListener(panelApplyButtonClick);
		sp4.addApplyListener(panelApplyButtonClick);
		//sp2.addRegionChangeListener(regionChange);
		slides.setWidget(1, 1, sp4);
		sp4.setPanelWidth(pwidth);
		sp4.addCompareAxisChangeListener(axisMenuChangeListener);
		sp4.addZChangeListner(axisMenuChangeListener);
		sp4.addTChangeListner(axisMenuChangeListener);		
		panels.add(sp4);

		sp1.setVariable(var);
		sp1.init(false);
		sp2.setVariable(var);
        sp2.init(false);
        sp3.setVariable(var);
        sp3.init(false);
        sp4.setVariable(var);
        sp4.init(false);
        
		init();

	}
    public boolean init() {

    	settingsButton.setOperations(rpcService, var.getIntervals(), var.getDSID(), var.getID(), null);
		GridSerializable ds_grid = var.getGrid();
		double grid_west = Double.valueOf(ds_grid.getXAxis().getLo());
		double grid_east = Double.valueOf(ds_grid.getXAxis().getHi());

		double grid_south = Double.valueOf(ds_grid.getYAxis().getLo());
		double grid_north = Double.valueOf(ds_grid.getYAxis().getHi());

		double delta = Math.abs(Double.valueOf(ds_grid.getXAxis().getArangeSerializable().getStep()));

		LatLngBounds bounds = LatLngBounds.newInstance(LatLng.newInstance(grid_south, grid_west), LatLng.newInstance(grid_north, grid_east));
		settingsButton.getRefMap().initDataBounds(bounds, delta, true);
		
    	ortho.clear();
    	if ( datePanel != null ) {
    		datePanel.clear();
    		ztGrid.remove(datePanel);
    	}
    	if ( xyzPanel != null ) {
    		xyzPanel.clear();
    		ztGrid.remove(xyzPanel);
    	}
    	if ( var.getGrid().getZAxis() != null ) {
			ortho.add("z");
		}
		if ( var.getGrid().getTAxis() != null  ) {
			ortho.add("t");
		}
    	if ( ortho.size() == 0 ) {
			Window.alert("There are no axes orthogonal to the view on which the data can be compared.");
			return false;
		} else {
			// Build a widget for each orthogonal axis.  There should be a max of 2.
			
			int pos = 0;
			// Figure out which axis vary in each frame.  Take them in order of t, z, x, y...
			if ( ortho.contains("t") ) {
				compareAxis = "t";
			}  else if ( ortho.contains("z") ) {
				compareAxis = "z";
			}
			for (Iterator orthoIt = ortho.iterator(); orthoIt.hasNext();) {
				String type = (String) orthoIt.next();
				if ( type.equals("t") ) {
					// TODO  For now assuming t is a comparison axis and that is is active.
					TimeAxisSerializable axis = (TimeAxisSerializable) var.getGrid().getAxis(type);
					
					dateWidget = new DateTimeWidget(axis, false);
					dateWidget.addChangeListener(fixedAxisMenuChange);
					if ( compareAxis.equals("t") ) {
						dateWidget.setEnabled(false);
					} else {
						dateWidget.setEnabled(true);
						fixedAxis = "t";
					}
					dateButton = new RadioButton("compare", " ");
					dateButton.addClickListener(compareAxisChangeListener);
					if ( compareAxis.equals("t") ) {
						dateButton.setChecked(true);
					}
					datePanel.add(dateButton);
					datePanel.add(dateWidget);
					ztGrid.setWidget(1, pos, datePanel);
				} else {
					AxisSerializable axis = var.getGrid().getAxis(type);
							
					xyzButton = new RadioButton("compare", " ");
					xyzButton.addClickListener(compareAxisChangeListener);
					xyzWidget = new AxisWidget(axis);
					xyzWidget.addChangeListener(fixedAxisMenuChange);
					if ( compareAxis.equals(type) ) {
						xyzButton.setChecked(true);
						xyzWidget.setEnabled(false);
					} else {
						fixedAxis = type;
						xyzWidget.setEnabled(true);
					}
					xyzPanel.add(xyzButton);
					xyzPanel.add(xyzWidget);
					ztGrid.setWidget(1, pos, xyzPanel);
				}
				pos++;
			}
			if ( ortho.size() == 1 ) {
				fixedAxis = "none";
			}
			return true;
		}
    }
	public ClickListener compareAxisChangeListener = new ClickListener() {
		public void onClick(Widget sender) {
			if ( sender instanceof RadioButton) {
				String temp = compareAxis;
				compareAxis = fixedAxis;
				fixedAxis = temp;
				boolean fixed_axis_range = false;
				String fixedAxisLoValue = "";
				String fixedAxisHiValue = "";
				if ( compareAxis.equals("t") ) {
					dateWidget.setRange(false);
					dateWidget.setEnabled(false);
					xyzWidget.setEnabled(true);
					xyzWidget.setRange(view.contains("z"));
					fixedAxisLoValue = xyzWidget.getLo();
					fixedAxisHiValue = xyzWidget.getHi();
					fixed_axis_range = xyzWidget.isRange();
				}  else {
					dateWidget.setEnabled(true);
					dateWidget.setRange(view.contains("t"));
					xyzWidget.setRange(false);
					xyzWidget.setEnabled(false);
					fixedAxisLoValue = dateWidget.getFerretDateLo();
					fixedAxisHiValue = dateWidget.getFerretDateHi();
					fixed_axis_range = dateWidget.isRange();
				}
				// Set the value of the fixed axis in all the panels under slide sorter control.
				for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
		    		SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
		    		if ( !panel.isUsePanelSettings() ) {
		    			if ( fixed_axis_range ) {
		    				panel.setParentAxisRangeValues(fixedAxis, fixedAxisLoValue, fixedAxisHiValue);
		    			} else {
		    			    panel.setParentAxisValue(fixedAxis, fixedAxisLoValue);
		    			}
		    		}
				}
				refresh(true);
			}		
			differenceButton.setEnabled(!view.contains(compareAxis));
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
				if ( panel.getID().contains("Panel 0") ) {
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
                        // Always get values from the map.  They may get replaced with the panel settings.
						xlo = String.valueOf(settingsButton.getRefMap().getXlo());
						xhi = String.valueOf(settingsButton.getRefMap().getXhi());

						ylo = String.valueOf(settingsButton.getRefMap().getYlo());
						yhi = String.valueOf(settingsButton.getRefMap().getYhi());

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
						panel.computeDifference(settingsButton.getOptions(), switchAxis, comparePanel.getVariable(), settingsButton.getCurrentOperationView(), xlo, xhi, ylo, yhi, zlo, zhi, tlo, thi);
					}
				}
			}
		} else {
			// Get the current state of the options...
			Map<String, String> temp_state = new HashMap<String, String>(settingsButton.getOptions());
			if ( autoContourButton.isDown() ) {
				// If the auto button is down, it wins...
				autoScale();
			} else {
				// If it's not down, the current options value will be used.
				autoContourTextBox.setText("");
			}
			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
	    		SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
	    		panel.setFillLevels(autoContourTextBox.getText());
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
    		if ( changeDataset ) {
    			var = nvar;
    			changeDataset = false;
    			differenceButton.setEnabled(true);
    			differenceButton.setDown(false);
    			// Figure out the compare and fixed axis
    			init();
    			for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
        			SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
        			panel.setPanelColor("regularBackground");
        			panel.setOperation(op, view);
        			panel.setVariable(var);
        			// Send in the ortho axis to allow these to be build and displayed in the panel
        			
        			panel.init(false);
        			if ( fixedAxis.equals("t") ) {
						panel.setParentAxisValue("t", dateWidget.getFerretDateLo());
					} else if ( fixedAxis.equals("z") ) {
						panel.setParentAxisValue("z", xyzWidget.getLo());
					}
        		}
    		}
    		
    		// Check to see if the operation changed.  If so, change the tool.
    		String op_id = settingsButton.getCurrentOp().getID();
    		String op_view = settingsButton.getCurrentOperationView();
    		if ( !op_id.equals(op) && !op_view.equals(view) ) {
    			op = op_id;
    			view = op_view;
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
    				// reset the fixed axis, the lat/lon region and the operation to the slide sorter value.
    				if ( !panel.isUsePanelSettings() ) {
    					VariableSerializable v = panel.getVariable();
    					if ( !v.getID().equals(var.getID()) || !v.getDSID().equals(var.getDSID() ) ) {
    						panel.setVariable(var);
    						panel.init(false);
    					} 
    					if ( fixedAxis.equals("t") ) {
    						panel.setParentAxisValue("t", dateWidget.getFerretDateLo());
    					} else if ( fixedAxis.equals("z") ) {
    						panel.setParentAxisValue("z", xyzWidget.getLo());
    					}
        				panel.setLatLon(String.valueOf(settingsButton.getRefMap().getXlo()), String.valueOf(settingsButton.getRefMap().getXhi()), String.valueOf(settingsButton.getRefMap().getYlo()), String.valueOf(settingsButton.getRefMap().getYhi()));
    				    panel.setOperation(op, view);
    				}
    				if ( !panel.getCurrentOperationView().equals(settingsButton.getCurrentOperationView()) ) {
    					differenceButton.setDown(false);
    					differenceButton.setEnabled(false);
    				} else {
    					differenceButton.setEnabled(true);
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
    public ChangeListener axisMenuChangeListener = new ChangeListener() {
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
		    		    panel.setParentAxisValue("t", value);
		    		}
				}
			} else if ( fixedAxis.equals("z") ) {
	    		
	    		String lo_value = xyzWidget.getLo();
	    		String hi_value = xyzWidget.getHi();
	    		for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
		    		SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
		    		if ( !panel.isUsePanelSettings() ) {
		    			if ( xyzWidget.isRange() ) {
		    				panel.setParentAxisRangeValues("z", lo_value, hi_value);
		    			} else {
		    		        panel.setParentAxisValue("z", lo_value);
		    			}
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

        // Use the values from the "compare panel" to set the auto contour levels.
    	SlideSorterPanel panel = panels.get(0);

    	if ( panel.getMin() < globalMin ) {
    		globalMin = panel.getMin();
    	}
    	if ( panel.getMax() > globalMax ) {
    		globalMax = panel.getMax();
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
    public ClickListener operationsClickListener = new ClickListener() {
		public void onClick(Widget sender) {
			if ( sender instanceof OperationButton ) {
				OperationButton o = (OperationButton) sender;
			    view = settingsButton.getOperationsWidget().getCurrentView();
			    op = settingsButton.getCurrentOp().getID();
                // Turn off the difference button when the compare axis is a range.			    
                differenceButton.setEnabled(!view.contains(compareAxis));
				if ( view.length() !=  2 ) {
					autoContourTextBox.setText("");
					autoContourButton.setDown(false);
					autoContourButton.setEnabled(false);
				} else {
					autoContourButton.setEnabled(true);
				}
				// Set the current fixed menu to a range if necessary.
				if ( view.contains(fixedAxis) ) {
					if ( fixedAxis.equals("t") ) {
						dateWidget.setRange(true);
					} else if ( fixedAxis.equals("z") ) {
						xyzWidget.setRange(true);
					}
				} else {
					if ( fixedAxis.equals("t") ) {
						dateWidget.setRange(false);
					} else if ( fixedAxis.equals("z") ) {
						xyzWidget.setRange(false);
					}
				}
				
				// Set the orthogonal axes to a range in each panel.
				for (Iterator panelsIt = panels.iterator(); panelsIt.hasNext();) {
					SlideSorterPanel panel = (SlideSorterPanel) panelsIt.next();
					panel.setOperation(op, view);
					if ( view.contains("t") ) {
					   panel.setParentAxisRange("t", true);
					} else {
						panel.setParentAxisRange("t", false);
					}
					if ( view.contains("z") ) {
						panel.setParentAxisRange("z", true);
					} else {
						panel.setParentAxisRange("z", false);
					}
				}
				
			}
		}	
	};
}
