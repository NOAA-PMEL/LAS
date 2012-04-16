package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.laswidget.AxisWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.DateTimeWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.LASRequest;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.RegionSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.TimeAxisSerializable;
import gov.noaa.pmel.tmap.las.client.util.Util;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.xml.client.XMLParser;
import com.google.gwt.xml.client.impl.DOMParseException;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ChangeEvent;

/**
 * A dialog window that handles interactively saving the users data. The data
 * region has already been selected. Users can choose among various data formats
 * and time periods. This is a GWT entry point class that uses
 * <code>onModuleLoad()</code>.
 * 
 * @author weusijana
 */
// TODO: Write tests (at least integration tests) using gwt-test-utils without
// Gin and Guice
// TODO: Remove unneeded GWT.log calls
// TODO: Make MVP with Gin and Guice?
public class InteractiveDownloadData implements EntryPoint {
	// GUI objects
	private ListBox dataFormatComboBox;

	private DateTimeWidget dateWidget;

	private AxisWidget depthWidget;

	private RootPanel rootPanel;

	private Button saveButton;

	// Data objects from/to the server
	protected boolean dataHasT = false;

	protected boolean dataHasZ = false;

	private String dsID;

	protected GridSerializable grid = null;

	private LASRequest lasRequestDocument;

	private String lasRequestXMLString;

	private String tRangeHi;

	private String tRangeLo;

	private String tDisplayType;

	private String varID;

	private String zRangeHi;

	private String zRangeLo;

	AsyncCallback onGotGrid = new AsyncCallback() {
		@Override
		public void onFailure(Throwable caught) {
			Window.alert("Failed to get a Grid for the Interactive Download Data dialog. Caught error:"
					+ caught.toString());
			caught.printStackTrace();
		}

		public void onSuccess(Object result) {
			grid = (GridSerializable) result;
			if (grid == null) {
				onFailure(new Error("grid == null"));
			} else {
				gotGrid();
			}
		}
	};

	/**
	 * Dynamically generate the appropriate time selector and set it to the
	 * appropriate defaults. Then add it to the root panel.
	 * 
	 */
	private void genDateWidget() {
		HTML lblSelectTime = new HTML("<strong>Select Time:</strong>");
		rootPanel.add(lblSelectTime, 5, 172);

		TimeAxisSerializable tAxis = grid.getTAxis();
		tDisplayType = tAxis.getWidget_type();

		GWT.log("tDisplayType:" + tDisplayType);

		/*
		 * Range set to true means that there are two identical coordinated
		 * widgets (or set of widgets in the case of time) from which you can
		 * select a starting point and an ending point from that axis.
		 */
		boolean range = true;
		GWT.log("range:" + range);
		dateWidget = new DateTimeWidget(tAxis, range);
		try {
			String lo_date = tRangeLo;
			if ((lo_date == null) || (lo_date == "")) {
				GWT.log("tRangeLo was null or empty, setting lo_date to tAxis.getLo()");
				lo_date = tAxis.getLo();
			}
			GWT.log("lo_date:" + lo_date);

			String hi_date = tRangeHi;
			if ((hi_date == null) || (hi_date == "")) {
				GWT.log("tRangeHi was null or empty, setting hi_date to tAxis.getHi()");
				hi_date = tAxis.getHi();
			}
			GWT.log("hi_date:" + hi_date);

			// Set the date(s) to what the user all ready selected
			/*
			 * TODO: Replicate and perhaps fix: If lo_date was set to
			 * tAxis.getLo() or if hi_date was set to tAxis.getHi() sometimes
			 * the format can't be parsed by the dateWidget.
			 */
			dateWidget.setLo(lo_date);
			dateWidget.setHi(hi_date);
		} catch (IllegalArgumentException iae) {
			// Don't bother user with date parsing error
			iae.printStackTrace();
			GWT.log(iae.getLocalizedMessage(), iae);
		} catch (NullPointerException npe) {
			// Don't bother user with missing date information
			npe.printStackTrace();
			GWT.log(npe.getLocalizedMessage(), npe);
		}
		if (dateWidget != null)
			rootPanel.add(dateWidget, 100, 172);
	}

	/**
	 * Initialize and add depth widgets to the root panel.
	 */
	private void genDepthWidgets() {
		HTML lblSelectDepth = new HTML("<strong>Select Depth:</strong>");
		rootPanel.add(lblSelectDepth, 5, 266);
		lblSelectDepth.setSize("105px", "17px");

		depthWidget = new AxisWidget(grid.getZAxis(), true);
		// Set the depth(s) to what the user all ready selected
		depthWidget.setLo(zRangeLo);
		depthWidget.setHi(zRangeHi);
		rootPanel.add(depthWidget, 100, 266);
	}

	public void onModuleLoad() {
		init();

		// Get the grid and generate dependent widgets
		AsyncCallback gridCallback = onGotGrid;
		Util.getRPCService().getGrid(dsID, varID, gridCallback);
	}

	/**
	 * 
	 */
	void init() {
		// Get ALL the data download parameters from the URL
		lasRequestXMLString = Util.getParameterString("lasxmldoc");
		GWT.log("lasRequestXMLString:\n" + lasRequestXMLString);

		dsID = Util.getParameterString("dsID");
		GWT.log("dsID:" + dsID);
		varID = Util.getParameterString("varID");
		GWT.log("varID:" + varID);

		if (lasRequestXMLString != null && !lasRequestXMLString.equals("")) {
			StringBuffer backslashQuote = new StringBuffer(2);
			backslashQuote.append('\\');
			backslashQuote.append('"');
			StringBuffer quote = new StringBuffer(1);
			quote.append('"');
			lasRequestXMLString = lasRequestXMLString.replace(backslashQuote,
					quote);
			try {
				GWT.log("lasRequestXMLString after replaceing backslash-quotes with quotes:\n"
						+ lasRequestXMLString);
				lasRequestDocument = new LASRequest(lasRequestXMLString);
				GWT.log("lasRequestDocument:\n" + lasRequestDocument);

				dsID = lasRequestDocument.getDataset(0);
				varID = lasRequestDocument.getVariable(0);

				tRangeLo = lasRequestDocument.getRangeLo("t", 0);
				if (tRangeLo == null)
					tRangeLo = "";
				tRangeHi = lasRequestDocument.getRangeHi("t", 0);
				if (tRangeHi == null)
					tRangeHi = "";

				zRangeLo = lasRequestDocument.getRangeLo("z", 0);
				if (zRangeLo == null)
					zRangeLo = "";
				zRangeHi = lasRequestDocument.getRangeHi("z", 0);
				if (zRangeHi == null)
					zRangeHi = "";
			} catch (DOMParseException e) {
				GWT.log(e.getLocalizedMessage(), e);
				GWT.log(e.getContents());
				e.printStackTrace();
			}
		}

		rootPanel = RootPanel.get();
		rootPanel.setSize("400", "700");

		HTML lblDownloadData = new HTML();
		lblDownloadData
				.setHTML("<h2>Specify your data's requirements and then click \"Save\" to download.</h2>");
		lblDownloadData.setDirectionEstimator(true);
		lblDownloadData
				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		rootPanel.add(lblDownloadData, 0, 0);
		lblDownloadData.setSize("393px", "65px");

		HTML lblSelectedRegion = new HTML("<strong>Selected Region:</strong>");
		rootPanel.add(lblSelectedRegion, 5, 62);

		String initXLo = Window.Location.getParameter("initXLo");
		if (initXLo == null)
			initXLo = "";
		String initXHi = Window.Location.getParameter("initXHi");
		if (initXHi == null)
			initXHi = "";
		String initYLo = Window.Location.getParameter("initYLo");
		if (initYLo == null)
			initYLo = "";
		String initYHi = Window.Location.getParameter("initYHi");
		if (initYHi == null)
			initYHi = "";
		// Using two region labels, one for latitude and one for longitude
		String regionLongitudeText = "Longitude range: [" + initXLo + ", "
				+ initXHi + "]";
		String regionLatitudeText = "Latitude range: [" + initYLo + ", "
				+ initYHi + "]";

		Label selectedRegionLongitude = new Label(regionLongitudeText);
		rootPanel.add(selectedRegionLongitude, 130, 62);
		selectedRegionLongitude.setSize("200", "80");
		Label selectedRegionLatitude = new Label(regionLatitudeText);
		rootPanel.add(selectedRegionLatitude, 130, 89);
		selectedRegionLatitude.setSize("200", "80");

		HTML lblSelectAData = new HTML("<strong>Select a Data Format:</strong>");
		rootPanel.add(lblSelectAData, 5, 120);

		// TODO: Add a help pop-up regarding data formats
		dataFormatComboBox = new ListBox();
		dataFormatComboBox.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				updateUI();
			}
		});
		dataFormatComboBox.addItem("NetCDF");
		dataFormatComboBox.addItem("ASCII");
		dataFormatComboBox.addItem("CSV");
		dataFormatComboBox.addItem("arcGrid");
		// TODO: Experimental shape Files are not available in the LAS UI yet
		// dataFormatComboBox.addItem("Shape File");
		dataFormatComboBox.setSelectedIndex(0);
		dataFormatComboBox.setEnabled(false);
		rootPanel.add(dataFormatComboBox, 176, 120);

		saveButton = new Button();
		saveButton.setEnabled(false);
		rootPanel.add(saveButton, 176, 470);
		saveButton.setText("Save");

		saveButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				saveAction();
			}
		});
	}

	/**
	 * Opens a new Window with the LAS Request for the specified data
	 */
	private void saveAction() {
		// Create and send the LAS request to download data
		LASRequest newReq = updateLASRequest();
		sendLASRequest(newReq);
	}

	/**
	 * Updates the LASRequest object based on the users selections in the GUI
	 * and the original LAS Request XML.
	 * 
	 * @return The new LASRequest object
	 */
	private LASRequest updateLASRequest() {
		// Create the LAS request part of the url based on current selections
		if ((lasRequestXMLString == null) || (lasRequestXMLString == ""))
			lasRequestXMLString = "<?xml version=\"1.0\"?><lasRequest href=\"file:las.xml\"></lasRequest>";
		lasRequestDocument = new LASRequest(lasRequestXMLString);

		// set user selected date range
		String dataFormat = dataFormatComboBox.getValue(dataFormatComboBox
				.getSelectedIndex());
		if (dataHasT) {
			String ferretDateLo = dateWidget.getFerretDateLo();
			GWT.log("ferretDateLo:" + ferretDateLo);
			String ferretDateHi = dateWidget.getFerretDateHi();
			GWT.log("ferretDateHi:" + ferretDateHi);
			if (tDisplayType.equalsIgnoreCase("widget")) {
				if (dataFormat.equalsIgnoreCase("arcGrid")) {
					lasRequestDocument.setRange("t", ferretDateLo,
							ferretDateLo, 0);
				} else {
					lasRequestDocument.setRange("t", ferretDateLo,
							ferretDateHi, 0);
				}
			} else if (tDisplayType.equalsIgnoreCase("menu")) {
				if (dataFormat.equalsIgnoreCase("arcGrid")) {
					lasRequestDocument.setRange("t", ferretDateLo,
							ferretDateLo, 0);
				} else {
					lasRequestDocument.setRange("t", ferretDateLo,
							ferretDateHi, 0);
				}
			}
		}

		// set user selected depth range
		if (dataHasZ) {
			String zLo = depthWidget.getLo();
			GWT.log("zLo:" + zLo);
			if (dataFormat.equalsIgnoreCase("arcGrid")) {
				lasRequestDocument.setRange("z", zLo, zLo, 0);
			} else {
				String zHi = depthWidget.getHi();
				GWT.log("zHi:" + zHi);
				lasRequestDocument.setRange("z", zLo, zHi, 0);
			}
		}

		// set view
		if (dataHasT) {
			if (dataHasZ) {
				lasRequestDocument.setProperty("ferret", "view", "xyzt");
			} else {
				lasRequestDocument.setProperty("ferret", "view", "xyt");
			}
		} else {
			if (dataHasZ) {
				lasRequestDocument.setProperty("ferret", "view", "xyz");
			} else {
				lasRequestDocument.setProperty("ferret", "view", "xy");
			}
		}

		// arcGrid: only for xy map
		if (dataFormat.equalsIgnoreCase("arcGrid")
				|| dataFormat.equalsIgnoreCase("Shape File")) {
			lasRequestDocument.setProperty("ferret", "view", "xy");
		}

		if (dataFormat.equalsIgnoreCase("ASCII")) {
			// set output format for ascii, "tsv" for tab separated
			lasRequestDocument.setProperty("ferret", "data_format", "tsv");
		} else if (dataFormat.equalsIgnoreCase("CSV")) {
			lasRequestDocument.setProperty("ferret", "data_format", "csv");
		}

		// set operation
		if (dataFormat.equalsIgnoreCase("NetCDF")) {
			lasRequestDocument.setOperation("Data_Extract_netCDF", "V7");
		} else if (dataFormat.equalsIgnoreCase("ASCII")) {
			lasRequestDocument.setOperation("Data_Extract_File", "V7");
		} else if (dataFormat.equalsIgnoreCase("CSV")) {
			lasRequestDocument.setOperation("Data_Extract_File", "V7");
		} else if (dataFormat.equalsIgnoreCase("arcGrid")) {
			lasRequestDocument.setOperation("Data_Extract_ArcView", "V7");
		} else if (dataFormat.equalsIgnoreCase("Shape File")) {
			lasRequestDocument.setOperation("Shape_File", "V7");
		}

		return lasRequestDocument;
	}

	/**
	 * Use a LASRequest object to make a complete URL and open the URL in a new
	 * browser window.
	 * 
	 * @param lasRequestDoc
	 */
	private void sendLASRequest(LASRequest lasRequestDoc) {
		// Complete the URL and open it in a new window
		String lasRequestString = lasRequestDoc.toString();
		GWT.log("lasRequestString:");
		GWT.log(lasRequestString);
		String url = Util.getProductServer() + "?xml="
				+ URL.encode(lasRequestString);
		GWT.log("sendLASRequest is about to open url:");
		GWT.log(url);
		String windowName = "downloadWindow";
		String features = "height=450, width=650,toolbar=1,menubar=1,scrollbars=1,status=1";
		Window.open(url, windowName, features);
	}

	/**
	 * Update the UI such as when dataFormatComboBox changes.
	 */
	private void updateUI() {
		String dataFormat = dataFormatComboBox.getValue(dataFormatComboBox
				.getSelectedIndex());
		if (dataFormat == null)
			dataFormat = "";
		GWT.log("dataFormat:" + dataFormat);
		boolean range = !dataFormat.equalsIgnoreCase("arcGrid");
		// Hide or show the 2nd data and depth combo boxes.
		dateWidget.setRange(range);
		depthWidget.setRange(range);
	}

	/**
	 * Now that the relevant grid has been retrieved from the server, generate
	 * and make visible the relevant GUI components.
	 */
	void gotGrid() {
		GWT.log(grid.getID());
		// Then set dataHasT and dataHasZ
		dataHasT = grid.hasT();
		dataHasZ = grid.hasZ();

		// If dataHasT show the appropriate time selector
		if (dataHasT)
			genDateWidget();

		// If dataHasZ show the appropriate depth selector
		if (dataHasZ) {
			genDepthWidgets();
		}

		dataFormatComboBox.setEnabled(true);
		saveButton.setEnabled(true);
	}

}