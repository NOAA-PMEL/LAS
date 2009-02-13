package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.map.ReferenceMap;
import gov.noaa.pmel.tmap.las.client.slidesorter.SlideSorterPanel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class SlideSorter extends LASEntryPoint {
	/*
	 * These are the four pieces of information required
	 * to initialize a SlideSorter
	 */
	String dsid;
	String vid;
	String view;
	String op;
	
	// LAS Reference Map for xy selections.
	ReferenceMap refMap;
	
	// This keeps track of the axes orthogonal to the view.
	List<String> ortho;
	
	// This is the layout object for the slide sorter.
	Grid grid;
	
	// Object that has and knows how to initialize the orthogonal axes.
	SlideSorterComposite slideSorterComposite;
	
	FocusPanel fixedOrthoDatePanel;
	
	boolean firstClick = true;
	String firstValue;
	
	// Control the selection of the fixed orthogonal axis.
	RadioButton fixedOrthoDate;
	RadioButton fixedOrthoXYZ;
	// Group the radio button and axis together in a grid.
	Grid fixedOrthoDateGrid;
	Grid fixedOrthoXYZGrid;
	
	Grid header;
	
	// Keep track of which axis is selected in the header as the fixed axis
	String fixedAxis;
	
	// Keep track of the axis that is in the panels.
	String compareAxis;
	
	// Keep track of a list of active SlideSorterPanels.
	List<SlideSorterPanel> panels;
	
	// Button to pop up the reference map.
	Button mapButton;
	
	// Button to close the map panel.
	Button closeButton;
	
	// PopupPanel to hold the reference map.
	PopupPanel mapPanel;
	
	// Vertical Panel for the close button and map.
	VerticalPanel mapInteriorPanel;
	
	@Override
	public void onModuleLoad() {
		
		super.onModuleLoad();
		
		ortho = new ArrayList<String>();
		
		panels = new ArrayList<SlideSorterPanel>();
		
		// Initialize the widgets to be used...
		header = new Grid(1, 4);
		header.addStyleName("LSS_middle");
		
		Label ss = new Label("Comparison Axis:");
		ss.addStyleName("LLS_middle");
		
		header.setWidget(0, 0, ss);
		
		grid = new Grid(1, 2);
		
		mapButton = new Button ("Select Region");
		mapButton.addClickListener(mapButtonClick);
		
		mapPanel = new PopupPanel(false);
		mapInteriorPanel = new VerticalPanel();
		
		closeButton = new Button("Close and Plot");
		closeButton.addClickListener(closeClick);
		
		mapInteriorPanel.add(closeButton);
		
		refMap = new ReferenceMap(LatLng.newInstance(0.0, 0.0), 1, 256, 360);
		mapInteriorPanel.add(refMap);
		
		mapPanel.add(mapInteriorPanel);
		
		header.setWidget(0, 3, mapButton);
		
		fixedOrthoDatePanel = new FocusPanel();
		fixedOrthoDatePanel.addClickListener(datePanelClickListener);
		fixedOrthoDatePanel.addStyleName("LLS_middle");
			
		Map<String, List<String>> parameters = Window.Location.getParameterMap();
		
		dsid = parameters.get("dsid").get(0);
		vid = parameters.get("vid").get(0);
		//TODO If the operation is null, get the default operation (the map or plot; left nav) for this view.
		op = parameters.get("opid").get(0);
		view = parameters.get("view").get(0);
		if ( dsid != null && vid != null & op != null && view != null) {
			// If the proper information was sent to the widget, pull down the variable definition
			// and initialize the slide sorter with this Ajax call.
			rpcService.getCategories(dsid, initSlideSorter);
		}
		RootPanel.get("header").add(header);
		RootPanel.get("grid").add(grid);
		
	}
	ClickListener mapButtonClick = new ClickListener() {
		public void onClick(Widget sender) {
			mapPanel.setPopupPosition(mapButton.getAbsoluteLeft(), mapButton.getAbsoluteTop());
			mapPanel.show();			
		}		
	};
	ClickListener closeClick = new ClickListener() {
		public void onClick(Widget sender) {
			mapPanel.hide();	
			refreshPanels();
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
							/* Examine the variable axes and determine which are orthogonal to the view. */

							if ( vars[i].getGrid().getXAxis() != null && !view.contains("x") ) {
								ortho.add("x");
							}
							if ( vars[i].getGrid().getYAxis() != null && !view.contains("y") ) {
								ortho.add("y");
							}
							if ( vars[i].getGrid().getZAxis() != null && !view.contains("z") ) {
								ortho.add("z");
							}
							if ( vars[i].getGrid().getTAxis() != null && !view.contains("t") ) {
								ortho.add("t");
							}
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
							if ( ortho.size() == 0 ) {
								Window.alert("There are no axes orthogonal to the view on which the data can be compared.");
							} else if ( ortho.size() > 2 ) { 
								Window.alert("There are "+ortho.size()+" orthogonal axes.  The SlideSorter only allows 2.");
							} else {
								List <AxisSerializable> axes = new ArrayList<AxisSerializable> ();
								// Build a widget for each orthogonal axis.  There should be a max of 2.
								for (Iterator orthoIt = ortho.iterator(); orthoIt.hasNext();) {
									String type = (String) orthoIt.next();
									axes.add(vars[i].getGrid().getAxis(type));
								}
								slideSorterComposite = new SlideSorterComposite(axes);
								
								
								if (slideSorterComposite.hasDateWidget() ) {
									// Ortho axis might not include time, so build everything here if it is "t"
									fixedOrthoDateGrid = new Grid(1,2);
									fixedOrthoDate = new RadioButton("fixed_axis", "Time ");
									fixedOrthoDate.addClickListener(fixedAxisSelectClickListener);
									slideSorterComposite.dateWidget.renderToNode(fixedOrthoDatePanel.getElement(), slideSorterComposite.getRenderString(), null);
									if ( !compareAxis.equals("t") ) {
										fixedOrthoDate.setChecked(true);
										fixedAxis = "t";
									} else {
										fixedOrthoDatePanel.setVisible(false);
									}
									fixedOrthoDateGrid.setWidget(0, 0, fixedOrthoDate);
									fixedOrthoDateGrid.setWidget(0, 1, fixedOrthoDatePanel);
								} else if ( slideSorterComposite.hasDateMenu() ) {
									// Ortho axis might not include time, so build everything here if it is "t"
									fixedOrthoDate = new RadioButton("fixed_axis", "Time ");
									fixedOrthoDate.addClickListener(fixedAxisSelectClickListener);
									fixedOrthoDateGrid = new Grid(1, 2);
									fixedOrthoDateGrid.addStyleName("LSS_middle");
									if ( !compareAxis.equals("t") ) {
										fixedOrthoDate.setChecked(true);
										fixedAxis = "t";
									} else {
										slideSorterComposite.dateMenu.setVisible(false);
									}
									fixedOrthoDateGrid.setWidget(0, 0, fixedOrthoDate);
									fixedOrthoDateGrid.setWidget(0, 1, slideSorterComposite.xyzMenu);
								}
								if ( slideSorterComposite.hasXYZMenu() ) {
									fixedOrthoXYZGrid = new Grid(1, 2);
									fixedOrthoXYZGrid.addStyleName("LSS_middle");
									fixedOrthoXYZ = new RadioButton("fixed_axis", slideSorterComposite.getXYZLabel());
									fixedOrthoXYZ.addStyleName("LSS_middle");
									fixedOrthoXYZ.addClickListener(fixedAxisSelectClickListener);
																		
									if ( !compareAxis.equals(slideSorterComposite.getXYZType()) ) {
										fixedOrthoXYZ.setChecked(true);
										fixedAxis = slideSorterComposite.getXYZType();
									} else {
										slideSorterComposite.xyzMenu.setVisible(false);
									}

									slideSorterComposite.xyzMenu.addChangeListener(fixedAxisMenuChange);
									fixedOrthoXYZGrid.setWidget(0, 0, fixedOrthoXYZ);
									fixedOrthoXYZGrid.setWidget(0, 1, slideSorterComposite.xyzMenu);
								}
									

									header.setWidget(0, 1, fixedOrthoDateGrid);
									header.setWidget(0, 1, fixedOrthoDateGrid);
									header.setWidget(0, 2, fixedOrthoXYZGrid);
								
								SlideSorterPanel sp1 = new SlideSorterPanel(axes, vars[i].getDSID(), vars[i].getID(), op, compareAxis, view, productServer, rpcService);
								grid.setWidget(0, 0, sp1);
								panels.add(sp1);
								SlideSorterPanel sp2 = new SlideSorterPanel(axes, vars[i].getDSID(), vars[i].getID(), op, compareAxis, view, productServer, rpcService);
								grid.setWidget(0, 1, sp2);
								panels.add(sp2);
								GridSerializable grid = vars[i].getGrid();
								double grid_west = Double.valueOf(grid.getXAxis().getLo());
								double grid_east = Double.valueOf(grid.getXAxis().getHi());
								
								double grid_south = Double.valueOf(grid.getYAxis().getLo());
								double grid_north = Double.valueOf(grid.getYAxis().getHi());
								
								double delta = Math.abs(Double.valueOf(grid.getXAxis().getArangeSerializable().getStep()));
								
								LatLngBounds bounds = LatLngBounds.newInstance(LatLng.newInstance(grid_south, grid_west), LatLng.newInstance(grid_north, grid_east));
								
								refMap.initDataBounds(bounds, delta, true);
								
							}						
						}
					}
				    refreshPanels();
				}
			}
		}	
		public void onFailure(Throwable caught) {
			Window.alert("Variable not found.   "+caught.getMessage());
		}
	};
	public void refreshPanels() {
		String fixedAxisValue;
		if ( fixedAxis.equals("t") ) {
			if (slideSorterComposite.hasDateWidget ) {
				fixedAxisValue = slideSorterComposite.dateWidget.getDate1_Ferret();
			} else {
				fixedAxisValue = slideSorterComposite.dateMenu.getSelectedValue();
			}
		} else {
			fixedAxisValue = slideSorterComposite.xyzMenu.getSelectedValue();
		}
		String view1lo = refMap.getXlo();
		String view1hi = refMap.getXhi();
		String view2lo = refMap.getYlo();
		String view2hi = refMap.getYhi();
		for (int i=0; i < panels.size(); i++) {
			SlideSorterPanel panel = (SlideSorterPanel) panels.get(i);
			panel.refreshPlot(fixedAxis, fixedAxisValue, view1lo, view1hi, view2lo, view2hi, true);
		}
	}
	public ClickListener fixedAxisSelectClickListener = new ClickListener() {

		public void onClick(Widget sender) {
			if ( sender instanceof RadioButton) {
				String temp = compareAxis;
				compareAxis = fixedAxis;
				fixedAxis = temp;
				RadioButton b = (RadioButton) sender;
				String type = b.getText();
				String fixedAxisValue = "";
				if ( type.toLowerCase().contains("time") ) {
					if ( slideSorterComposite.hasDateWidget ) {
					    fixedOrthoDatePanel.setVisible(true);
					    fixedAxisValue = slideSorterComposite.dateWidget.getDate1_Ferret();
					} else {
					    slideSorterComposite.dateMenu.setVisible(true);
					    fixedAxisValue = slideSorterComposite.dateMenu.getSelectedValue();
					}
					if (slideSorterComposite.hasXYZMenu()) slideSorterComposite.xyzMenu.setVisible(false);
				}  else {
					if ( slideSorterComposite.hasDateWidget() ) {
					    fixedOrthoDatePanel.setVisible(false);
					} else {
					    slideSorterComposite.dateMenu.setVisible(false);
					}
					slideSorterComposite.xyzMenu.setVisible(true);
					fixedAxisValue = slideSorterComposite.xyzMenu.getSelectedValue();
				}
				String view1lo = refMap.getXlo();
				String view1hi = refMap.getXhi();
				String view2lo = refMap.getYlo();
				String view2hi = refMap.getYhi();
				for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
					SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
					panel.switchCompareAxis(compareAxis);
					panel.refreshPlot(fixedAxis, fixedAxisValue, view1lo, view1hi, view2lo, view2hi, true);
				}
			}			
		}		
	};
    public ChangeListener fixedAxisMenuChange = new ChangeListener() {

		public void onChange(Widget sender) {
			if ( sender instanceof ListBox ) {
				ListBox l = (ListBox) sender;
				String value = l.getValue(l.getSelectedIndex());
				String axis = l.getName();
				fixedAxis = axis;
				String view1lo = refMap.getXlo();
				String view1hi = refMap.getXhi();
				String view2lo = refMap.getYlo();
				String view2hi = refMap.getYhi();
				for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
					SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
					panel.refreshPlot(axis, value, view1lo, view1hi, view2lo, view2hi, true);
				}
			}			
		}   	
    };
    ClickListener datePanelClickListener = new ClickListener() {
		public void onClick(Widget sender) {
			if ( firstClick ) {
				firstClick = !firstClick;
				firstValue = slideSorterComposite.dateWidget.getDate1_Ferret();
			} else {
				firstClick = !firstClick;
				String currentValue = slideSorterComposite.dateWidget.getDate1_Ferret();
				// The value changed, refresh the plots.
				if ( !currentValue.equals(firstValue)) {
					String view1lo = refMap.getXlo();
					String view1hi = refMap.getXhi();
					String view2lo = refMap.getYlo();
					String view2hi = refMap.getYhi();
					for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
						SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
						panel.refreshPlot(fixedAxis, currentValue, view1lo, view1hi, view2lo, view2hi, true);
					}
				}
			}
			
		}
		
	};
}
