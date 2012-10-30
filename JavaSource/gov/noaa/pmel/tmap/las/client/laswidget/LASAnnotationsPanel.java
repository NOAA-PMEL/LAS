package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.ClientFactory;
import gov.noaa.pmel.tmap.las.client.event.StringValueChangeEvent;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.CDATASection;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class LASAnnotationsPanel extends Composite {
	private ClientFactory clientFactory = GWT.create(ClientFactory.class);
	private EventBus eventBus = clientFactory.getEventBus();

	private static final String LAB_TITLE = "dataset_title_lab";
	private static final String LAB_URL = "dataset_url_lab";
	private static final String LAB_VARIABLE = "variable_title_lab";
	private static final String LAB_LONGITUDE = "longitude_lab";
	private static final String LAB_LATITUDE = "latitude_lab";
	private static final String LAB_DEPTH = "depth_lab";
	private static final String LAB_TIME = "time_lab";
	private static final String LAB_CALENDAR = "calendar_lab";
	private static final String LAB_NOTE_1 = "note_1_lab";
	private static final String LAB_NOTE_2 = "note_2_lab";
	private static final String LAB_NOTE_3 = "note_3_lab";

	private static final String ELEMENT_ANNOTATION_GROUP = "annotation_group";
	private static final String ELEMENT_ANNOTATION = "annotation";
	private static final String ELEMENT_LABEL = "label";
	private static final String ELEMENT_VALUE = "value";

	private static final String ATTRIBUTE_TYPE = "type";
	private static final String TYPE_VARIABLE = "variable";
	private static final String TYPE_DATASET = "dataset";
	private static final String TYPE_CALENDAR = "calendar";
	private static final String TYPE_ORTHOGONAL_AXES = "orthogonal_axes";
	private static final String TYPE_LAS = "las";
	private static final String TYPE_NOTES = "notes";
	private int popupLeft = -999;
	private int popupTop = -999;

	private static final String DEFAULT_TOOLTIP = "These are annotations of the associated plot.";

	DockPanel layoutPanel = new DockPanel();

	PushButton closeButton = new PushButton("X");

	public LASAnnotationsPanel() {
		super();
		initWidget(layoutPanel);
		// setWidth(CONSTANTS.DEFAULT_ANNOTATION_PANEL_WIDTH());
		layoutPanel.setTitle(DEFAULT_TOOLTIP);
		closeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				setOpenAndPublish(false);
			}
		});

		closeButton.setFocus(false);
		layoutPanel.add(closeButton, DockPanel.EAST);
		layoutPanel.setCellHeight(closeButton, "1em");
		layoutPanel.setCellWidth(closeButton, "1em");
		closeButton.setSize("1em", "1em");
		layoutPanel.setCellHorizontalAlignment(closeButton,
				HasHorizontalAlignment.ALIGN_RIGHT);
	}

	public void setAnnotationsHTMLURL(String url) {
		RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET, url);
		try {
			sendRequest.sendRequest(null, annotationsHTMLCallback);
		} catch (RequestException e) {

		}
	}

	public void setAnnotationsHTML(String html) {
		HTML annotations = new HTML(html);
		setup();
		add(annotations);
		// setWidth(CONSTANTS.DEFAULT_ANNOTATION_PANEL_WIDTH());
	}

	/**
	 * Clears the layoutPanel and adds the closeButton.
	 */
	void setup() {
		layoutPanel.clear();
		layoutPanel.add(closeButton, DockPanel.EAST);
	}

	/**
	 * Adds a {@link Widget} correctly to the correct sub-panel
	 * 
	 * @param child
	 */
	public void add(Widget child) {
		layoutPanel.add(child, DockPanel.CENTER);
	}

	public void setError(String html) {
		HTML annotations = new HTML(html);
		setup();
		add(annotations);
	}

	public void setAnnotations(String xml) {
		setup();
		Map<String, FlexTable> tables = new HashMap<String, FlexTable>();
		Document responseXML = XMLParser.parse(xml);
		NodeList annotationGroups = responseXML
				.getElementsByTagName(ELEMENT_ANNOTATION_GROUP);

		for (int n = 0; n < annotationGroups.getLength(); n++) {
			FlexTable table;
			Element agE = (Element) annotationGroups.item(n);
			String type = agE.getAttribute(ATTRIBUTE_TYPE);
			int row;
			if (tables.get(type) == null) {
				table = new FlexTable();
				tables.put(type, table);
				row = 0;
			} else {
				table = tables.get(type);
				row = table.getRowCount();
			}
			NodeList annotations = agE.getElementsByTagName(ELEMENT_ANNOTATION);
			for (int m = 0; m < annotations.getLength(); m++) {
				Element annE = (Element) annotations.item(m);

				String label = null;
				String value = null;

				NodeList labels = annE.getElementsByTagName(ELEMENT_LABEL);
				Element labelE = null;
				if (labels.getLength() > 0) {
					labelE = (Element) labels.item(0);
					NodeList cdataList = labelE.getChildNodes();
					for (int q = 0; q < cdataList.getLength(); q++) {
						if (cdataList.item(q).getNodeType() == Node.CDATA_SECTION_NODE) {
							CDATASection cdata = (CDATASection) cdataList
									.item(q);
							label = cdata.getData();
						}
					}
				}

				NodeList values = annE.getElementsByTagName(ELEMENT_VALUE);
				Element valueE = null;
				if (values.getLength() > 0) {
					valueE = (Element) values.item(0);
					NodeList cdataList = valueE.getChildNodes();
					for (int q = 0; q < cdataList.getLength(); q++) {
						if (cdataList.item(q).getNodeType() == Node.CDATA_SECTION_NODE) {
							CDATASection cdata = (CDATASection) cdataList
									.item(q);
							value = cdata.getData();
						}
					}
				}

				if (label != null && value != null) {
					table.setWidget(row, 0, new Label(label));
					table.setWidget(row, 1, new Label(value));
					row++;
				} else if (label == null && value != null) {
					table.getFlexCellFormatter().setColSpan(row, 0, 2);
					table.setWidget(row, 0, new Label(value));
					row++;
				} else if (label != null && value == null) {
					table.getFlexCellFormatter().setColSpan(row, 0, 2);
					table.setWidget(row, 0, new Label(label));
					row++;
				}

			}
		}
		// Figure out what tables we have and put them in to the panel in an
		// order that makes sense.
		if (tables.get(TYPE_VARIABLE) != null) {
			add(tables.get(TYPE_VARIABLE));
		}
		if (tables.get(TYPE_DATASET) != null) {
			add(tables.get(TYPE_DATASET));
		}
		if (tables.get(TYPE_ORTHOGONAL_AXES) != null) {
			add(tables.get(TYPE_ORTHOGONAL_AXES));
		}
		if (tables.get(TYPE_CALENDAR) != null) {
			add(tables.get(TYPE_CALENDAR));
		}
		// Notes and LAS info should be the last two.
		if (tables.get(TYPE_NOTES) != null) {
			add(tables.get(TYPE_NOTES));
		}
		if (tables.get(TYPE_LAS) != null) {
			add(tables.get(TYPE_LAS));
		}
		// setWidth(CONSTANTS.DEFAULT_ANNOTATION_PANEL_WIDTH());
	}

	public void setOpen(ToggleButton annotationsButton) {
		this.setVisible(true);
	}

	public void setPopupTop(int top) {
		popupTop = top;
	}

	public void setPopupLeft(int left) {
		popupLeft = left;
	}

	/**
	 * Sets the width of the main sub-panel ignoring the width of the
	 * closeButton.
	 * 
	 * @param width
	 */
	public void setPopupWidth(String width) {
		int closeButtonWidth = closeButton.getOffsetWidth();
		layoutPanel.setWidth(width);
		int widthInt = layoutPanel.getOffsetWidth();
		if (width.endsWith("px")) {
			try {
				widthInt = Integer.parseInt(width.substring(0,
						width.length() - 2));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		} else if (width.endsWith("em")) {
			widthInt = layoutPanel.getOffsetWidth();
		} else {
			try {
				widthInt = Integer.parseInt(width);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}
		setWidth((widthInt + closeButtonWidth) + "px");
	}

	/**
	 * 
	 */
	void setOpenAndPublish(boolean open) {
		setVisible(open);
		eventBus.fireEventFromSource(
				new StringValueChangeEvent(Boolean.toString(open)), this);
	}

	RequestCallback annotationsXMLCallback = new RequestCallback() {

		@Override
		public void onError(Request request, Throwable exception) {
			// TODO Auto-generated method stub
			GWT.log("onError(Request request, Throwable exception) called by annotationsXMLCallback");
			GWT.log("request");
			GWT.log(request.toString());
			GWT.log("exception");
			GWT.log(exception.toString());
			exception.printStackTrace();
		}

		@Override
		public void onResponseReceived(Request request, Response response) {

			String text = response.getText();
			setAnnotations(text);

		}

	};
	RequestCallback annotationsHTMLCallback = new RequestCallback() {

		@Override
		public void onError(Request request, Throwable exception) {
			// TODO Auto-generated method stub
			GWT.log("onError(Request request, Throwable exception) called by annotationsHTMLCallback");
			GWT.log("request");
			GWT.log(request.toString());
			GWT.log("exception");
			GWT.log(exception.toString());
			exception.printStackTrace();
		}

		@Override
		public void onResponseReceived(Request request, Response response) {

			String text = response.getText();
			setAnnotationsHTML(text);

		}

	};
}
