package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.map.MapButton;
import gov.noaa.pmel.tmap.las.client.slidesorter.SlideSorterPanel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootPanel;
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
	 * There will be at most two orthogonal axes.  For now assume one is time.
	 */
	DateTimeWidget dateWidget;
	AxisWidget xyzWidget;
	
	/*
	 * Switching between comparison axes is controlled by a radio button group.
	 */
	RadioButton dateButton;
	RadioButton xyzButton;
	
	/*
	 * Keep track of the labels for the radio buttons.
	 */
	String dateButtonSelected;
	String dateButtonNotSelected;
	
	String xyzButtonSelected;
	String xyzButtonNotSelected;
	/*
	 * The radio button and menu are held in a grid.
	 */
	Grid dateGrid;
	Grid xyzGrid;
	
	/*
	 * A header row with some widgets.
	 */
	Grid header;
	
	/*
	 * The slide sorter grid.
	 */
	Grid slides;
	
	/*
	 * The panels in this slide sorter.
	 */
	List<SlideSorterPanel> panels = new ArrayList<SlideSorterPanel>();
	
	/*
	 * A map behind a button for region selection.
	 */
	MapButton mapButton;
	
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
		
		dateGrid = new Grid(1, 2);
		xyzGrid = new Grid(1, 2);
		slides = new Grid(1,2);
		header = new Grid(1, 4);
		
		// Initialize the widgets to be used...
		if ( dsid != null && vid != null & op != null && view != null) {
			// If the proper information was sent to the widget, pull down the variable definition
			// and initialize the slide sorter with this Ajax call.
			rpcService.getCategories(dsid, initSlideSorter);
		}
		
		mapButton = new MapButton(LatLng.newInstance(0.0, 0.0), 1, 256, 360);
		mapButton.addClickListener(closeClick);
		header.setWidget(0, 3, mapButton);
		
		RootPanel.get("header").add(header);
		RootPanel.get("slides").add(slides);
	}
	AsyncCallback initSlideSorter = new AsyncCallback() {
		public void onSuccess(Object result) {
			CategorySerializable[] cats = (CategorySerializable[]) result;
			if ( cats != null && cats.length > 1 ) {
				Window.alert("Multiple categories found.");
			} else {
				if ( cats[0].isVariableChildren() ) {
					DatasetSerializable ds = cats[0].getDatasetSerializable();
					VariableSerializable[] vars = ds.getVariablesSerializable();
					List<AxisSerializable> axes = new ArrayList<AxisSerializable>();
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
							
							if ( ortho.size() == 0 ) {
								Window.alert("There are no axes orthogonal to the view on which the data can be compared.");
							} else if ( ortho.size() > 2 ) { 
								Window.alert("There are "+ortho.size()+" orthogonal axes.  The SlideSorterOld only allows 2.");
							} else {
								// Build a widget for each orthogonal axis.  There should be a max of 2.
								//TODO do a better job here...
								int pos = 0;
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
										TimeAxisSerializable axis = (TimeAxisSerializable) vars[i].getGrid().getAxis(type);
										axes.add(axis);
										dateWidget = new DateTimeWidget(axis, 0, 0, false);
										dateWidget.addChangeListener(fixedAxisMenuChange);
										if ( compareAxis.equals("t") ) {
										    dateWidget.setEnabled(false);
										} else {
											fixedAxis = type;
											dateWidget.setEnabled(true);
										}
										dateButtonNotSelected = axis.getLabel();
										if ( dateButtonNotSelected == null || dateButtonNotSelected.equals("") ) {
											dateButtonNotSelected = "Time ";
											dateButtonSelected = "Comparison Axis: " + dateButtonNotSelected;
										}
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
			                            dateGrid.setWidget(0, 0, dateButton);
			                            dateGrid.setWidget(0, 1, dateWidget);
			                            header.setWidget(0, pos, dateGrid);
									} else {
										AxisSerializable axis = vars[i].getGrid().getAxis(type);
										axes.add(axis);
										xyzButtonNotSelected = axis.getLabel();
										if ( xyzButtonNotSelected == null || xyzButtonNotSelected.equals("") ) {
											xyzButtonNotSelected = type.toUpperCase()+" ";
											xyzButtonSelected = "Comparison Axis: " + xyzButtonNotSelected;
										}
			                            xyzButton = new RadioButton("compare", xyzButtonSelected);
			                            xyzButton.addClickListener(compareAxisChangeListener);
										xyzWidget = new AxisWidget(axis);
										xyzWidget.setEnabled(true);
										xyzWidget.addChangeListener(fixedAxisMenuChange);
                                        if ( compareAxis.equals(type) ) {
											xyzButton.addStyleName("red");
											xyzButton.setChecked(true);
											xyzButton.setText(xyzButtonSelected);
										} else {
											fixedAxis = type;
											xyzButton.addStyleName("black");
											xyzButton.setText(xyzButtonNotSelected);
										}
                                        xyzGrid.setWidget(0, 0, xyzButton);
                                        xyzGrid.setWidget(0, 1, xyzWidget);
                                        header.setWidget(0, pos, xyzGrid);
									}
									pos++;
								}
								if ( ortho.size() == 1 ) {
									fixedAxis = "none";
								}
							}
							GridSerializable ds_grid = vars[i].getGrid();
							double grid_west = Double.valueOf(ds_grid.getXAxis().getLo());
							double grid_east = Double.valueOf(ds_grid.getXAxis().getHi());
							
							double grid_south = Double.valueOf(ds_grid.getYAxis().getLo());
							double grid_north = Double.valueOf(ds_grid.getYAxis().getHi());
							
							double delta = Math.abs(Double.valueOf(ds_grid.getXAxis().getArangeSerializable().getStep()));
							
							LatLngBounds bounds = LatLngBounds.newInstance(LatLng.newInstance(grid_south, grid_west), LatLng.newInstance(grid_north, grid_east));
							mapButton.getRefMap().initDataBounds(bounds, delta, true);
							
							SlideSorterPanel sp1 = new SlideSorterPanel(axes, vars[i].getDSName(), vars[i].getName(), vars[i].getDSID(), vars[i].getID(), op, compareAxis, view, productServer, rpcService);
							slides.setWidget(0, 0, sp1);
							panels.add(sp1);
							SlideSorterPanel sp2 = new SlideSorterPanel(axes, vars[i].getDSName(), vars[i].getName(), vars[i].getDSID(), vars[i].getID(), op, compareAxis, view, productServer, rpcService);
							slides.setWidget(0, 1, sp2);
							panels.add(sp2);
							update(false);
						}
					}
				}
			}
		}

		public void onFailure(Throwable caught) {
			Window.alert("Failed to initalizes SlideSorter."+caught.toString());
		}
	};
	
	public ClickListener compareAxisChangeListener = new ClickListener() {
		public void onClick(Widget sender) {
			if ( sender instanceof RadioButton) {
				String temp = compareAxis;
				compareAxis = fixedAxis;
				fixedAxis = temp;
				RadioButton b = (RadioButton) sender;
				String type = b.getText();
				String fixedAxisValue = "";
				if ( type.toLowerCase().contains("time") ) {
					dateWidget.setEnabled(false);
					dateButton.setStyleName("red");
					dateButton.setText(dateButtonSelected);
					xyzWidget.setEnabled(true);
					xyzButton.setStyleName("black");
					xyzButton.setText(xyzButtonNotSelected);
					fixedAxisValue = xyzWidget.getSelectedValue();
				}  else {
					dateWidget.setEnabled(true);
					dateButton.setStyleName("black");
					dateButton.setText(dateButtonNotSelected);
					xyzWidget.setEnabled(false);
					xyzButton.setStyleName("red");
					xyzButton.setText(xyzButtonSelected);
					fixedAxisValue = dateWidget.getFerretDateLo();
				}
				update(true);
			}			
		}			
    };
    ClickListener closeClick = new ClickListener() {
		public void onClick(Widget sender) {
			update(false);
		}
	};
	public ChangeListener fixedAxisMenuChange = new ChangeListener() {
		public void onChange(Widget sender) {
			if ( sender instanceof ListBox ) {
				ListBox l = (ListBox) sender;
				String value = l.getValue(l.getSelectedIndex());
				String axis = l.getName();
				update(false);
			}			
		}   	
    };
    public void update(boolean switchAxis) {
    	String view1lo = mapButton.getRefMap().getXlo();
    	String view1hi = mapButton.getRefMap().getXhi();
    	String view2lo = mapButton.getRefMap().getYlo();
    	String view2hi = mapButton.getRefMap().getYhi();
    	
    	String fixedAxisValue = "";
    	if ( fixedAxis.equals("t") ) {
    		fixedAxisValue = dateWidget.getFerretDateLo();
    	} else if ( !fixedAxis.equals("none")) {
    		fixedAxisValue = xyzWidget.getSelectedValue();
    	}
    		
    	
    	for (Iterator panelIt = panels.iterator(); panelIt.hasNext();) {
    		SlideSorterPanel panel = (SlideSorterPanel) panelIt.next();
    		if (switchAxis) {
    			panel.switchCompareAxis(compareAxis);
    		}
    		panel.refreshPlot(fixedAxis, fixedAxisValue, view1lo, view1hi, view2lo, view2hi, true);
    	}
    }
}
