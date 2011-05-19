package gov.noaa.pmel.tmap.las.client.laswidget;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.CDATASection;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class LASAnnotationsPanel extends Composite {
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
	
	DisclosurePanel mainPanel = new DisclosurePanel("Plot Annotations");
	VerticalPanel layoutPanel = new VerticalPanel();
	
	public LASAnnotationsPanel() {
		mainPanel.add(layoutPanel);
		initWidget(mainPanel);
	}
	public void init(Map<String, String> lasAnnotations) {
		String title = lasAnnotations.get(LAB_TITLE);
		if ( title != null && !title.equals("") ) {
			mainPanel.getHeaderTextAccessor().setText(title);
		}
		layoutPanel.clear();
		String variable = lasAnnotations.get(LAB_VARIABLE);
		if ( variable != null && !variable.equals("") ) {
			layoutPanel.add(new Label(variable));
		}		
		String longitude = lasAnnotations.get(LAB_LONGITUDE);
		if ( longitude != null && !longitude.equals("") ) {
			layoutPanel.add(new Label(longitude));
		}
		String latitude = lasAnnotations.get(LAB_LATITUDE);
		if ( latitude != null && !latitude.equals("") ) {
			layoutPanel.add(new Label(latitude));
		}
		String depth = lasAnnotations.get(LAB_DEPTH);
		if ( depth != null && !depth.equals("") ) {
			layoutPanel.add(new Label(depth));
		}
		String time = lasAnnotations.get(LAB_TIME);
		if ( time != null && !time.equals("") ) {
			layoutPanel.add(new Label(time));
		}
		String calendar = lasAnnotations.get(LAB_CALENDAR);
		if ( calendar != null && !calendar.equals("") ) {
			layoutPanel.add(new Label(calendar));
		}
		String url = lasAnnotations.get(LAB_URL);
		if ( url != null && !url.equals("") ) {
			layoutPanel.add(new Label(url));
		}
		String note1 = lasAnnotations.get(LAB_NOTE_1);
		if ( note1 != null && !note1.equals("") ) {
			layoutPanel.add(new Label(note1));
		}
		String note2 = lasAnnotations.get(LAB_NOTE_2);
		if ( note2 != null && !note2.equals("") ) {
			layoutPanel.add(new Label(note2));
		}
		String note3 = lasAnnotations.get(LAB_NOTE_3);
		if ( note3 != null && !note3.equals("") ) {
			layoutPanel.add(new Label(note3));
		}
	}
	public void setError(String error) {
		layoutPanel.clear();
		layoutPanel.add(new Label(error));	
	}
	public void setTitle(String title) {
		mainPanel.getHeaderTextAccessor().setText(title);
	}
	
	public void setAnnotationsURL(String url) {
		RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET, url);
		
		try {
			sendRequest.sendRequest(null, annotationsCallback);
		} catch (RequestException e) {
			
		}
	}
	public void setAnnotations(String xml) {
		layoutPanel.clear();
		Map<String, FlexTable> tables = new HashMap<String, FlexTable>();
		Document responseXML = XMLParser.parse(xml);
		NodeList annotationGroups = responseXML.getElementsByTagName(ELEMENT_ANNOTATION_GROUP);
		
		for(int n=0; n<annotationGroups.getLength();n++) {
			FlexTable table;
			Element agE = (Element) annotationGroups.item(n);
			String type = agE.getAttribute(ATTRIBUTE_TYPE);
			int row;
			if ( tables.get(type) == null ) {
				 table = new FlexTable();
				 tables.put(type, table);
				 row = 0;
			} else {
				table = tables.get(type);
				row = table.getRowCount();
			}
			NodeList annotations = agE.getElementsByTagName(ELEMENT_ANNOTATION);
			for(int m=0; m < annotations.getLength(); m++ ) {
				Element annE = (Element) annotations.item(m);
				
				String label = null;
				String value = null;
				
				NodeList labels = annE.getElementsByTagName(ELEMENT_LABEL);
				Element labelE = null;
				if ( labels.getLength() > 0 ) {
					labelE = (Element) labels.item(0);
					NodeList cdataList = labelE.getChildNodes();
					for ( int q = 0; q < cdataList.getLength(); q++ ) {
						if ( cdataList.item(q).getNodeType() == Node.CDATA_SECTION_NODE ) {
							CDATASection cdata = (CDATASection) cdataList.item(q);
							label = cdata.getData();
						}
					}
				}
				
				NodeList values = annE.getElementsByTagName(ELEMENT_VALUE);
				Element valueE = null;
				if ( values.getLength() > 0 ) {
					valueE = (Element) values.item(0);
					NodeList cdataList = valueE.getChildNodes();
					for ( int q = 0; q < cdataList.getLength(); q++ ) {
						if ( cdataList.item(q).getNodeType() == Node.CDATA_SECTION_NODE ) {
							CDATASection cdata = (CDATASection) cdataList.item(q);
							value = cdata.getData();
						}
					}
				}
				
				if ( label != null && value != null ) {
					table.setWidget(row, 0, new Label(label));
					table.setWidget(row, 1, new Label(value));
					row++;
				} else if ( label == null && value != null ) {
					table.getFlexCellFormatter().setColSpan(row, 0, 2);
					table.setWidget(row, 0, new Label(value));
					row++;
				} else if ( label != null && value == null ) {
					table.getFlexCellFormatter().setColSpan(row, 0, 2);
					table.setWidget(row, 0, new Label(label));
					row++;
				}
				
			}
		}
		// Figure out what tables we have and put them in to the panel in an order that makes sense.
		if ( tables.get(TYPE_VARIABLE) != null ) {
			layoutPanel.add(tables.get(TYPE_VARIABLE));
		}
		if( tables.get(TYPE_DATASET) != null ) {
			layoutPanel.add(tables.get(TYPE_DATASET));
		}
		if ( tables.get(TYPE_ORTHOGONAL_AXES) != null ) {
			layoutPanel.add(tables.get(TYPE_ORTHOGONAL_AXES));
		}
		if ( tables.get(TYPE_CALENDAR) != null ) {
			layoutPanel.add(tables.get(TYPE_CALENDAR));
		}
		// Notes and LAS info should be the last two.
		if ( tables.get(TYPE_NOTES) != null ) {
			layoutPanel.add(tables.get(TYPE_NOTES));
		}
		if ( tables.get(TYPE_LAS) != null) {
			layoutPanel.add(tables.get(TYPE_LAS));
		}
	}
	RequestCallback annotationsCallback = new RequestCallback() {

		@Override
		public void onError(Request request, Throwable exception) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onResponseReceived(Request request, Response response) {
			
			String text = response.getText();
			setAnnotations(text);
			
		}
		
	};
}
