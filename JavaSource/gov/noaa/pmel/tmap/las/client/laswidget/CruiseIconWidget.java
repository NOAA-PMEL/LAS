package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.util.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Text;
import com.google.gwt.xml.client.XMLParser;


public class CruiseIconWidget extends Composite {
	HTML message = new HTML("message");
    List<String> ids = new ArrayList<String>();
    List<IconCheckBox> icons = new ArrayList<IconCheckBox>();
    ScrollPanel panel = new ScrollPanel();
    VerticalPanel layout = new VerticalPanel();
    VerticalPanel interior = new VerticalPanel();
    public CruiseIconWidget() {
    	layout.add(message);
    	message.setVisible(false);
    	panel.add(interior);
    	layout.add(panel);
    	initWidget(layout);
    }
    public void init(LASRequest request, String cruiseid) {
    	message.setVisible(false);
    	request.setOperation("Cruise_List", "v7");
    	List<Map<String, String>> c = request.getVariableConstraints();
    	for (Iterator cIt = c.iterator(); cIt.hasNext();) {
			Map<String, String> map = (Map<String, String>) cIt.next();
			String varid = map.get("varID");
			if ( varid.equals(cruiseid)) {
				
			} else {
				
			}
		}
		String url = Util.getProductServer()+"?xml="+URL.encode(request.toString());
    	RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET, url);
		try {
			sendRequest.sendRequest(null, iconListRequestCallback);
		} catch (RequestException e) {
			message.setVisible(true);
			message.setHTML(e.toString());
		}
    }
    public void init(String iconurl) {
    	RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET, iconurl);
    	try {
    		sendRequest.sendRequest(null, iconCallback);
    	} catch (RequestException e) {
    		message.setVisible(true);
    		message.setHTML(e.toString());
    	}
    }
    public String getIDs() {
    	StringBuilder ids = new StringBuilder();
    	for (Iterator iconsIt = icons.iterator(); iconsIt.hasNext();) {
			IconCheckBox icb = (IconCheckBox) iconsIt.next();
			if ( icb.isChecked() ) {
				String id = icb.getID();
				if ( id.contains("(") ) {
				    id = id.substring(0,id.indexOf(" ("));
				}
				ids.append(id);
				if (iconsIt.hasNext()) ids.append(",");
			}
		}
    	return ids.toString();
    }
    RequestCallback iconCallback = new RequestCallback() {

		@Override
		public void onError(Request request, Throwable e) {
			message.setVisible(true);
			message.setHTML(e.toString());
		}

		@Override
		public void onResponseReceived(Request request, Response response) {
			String doc = response.getText();
            load(doc);
		}
    	
    };
    RequestCallback iconListRequestCallback = new RequestCallback() {

		@Override
		public void onError(Request request, Throwable e) {
			message.setVisible(true);
			message.setHTML(e.toString());
		}

		@Override
		public void onResponseReceived(Request request, Response response) {
			String doc = response.getText();
			load(doc);
			
		}

    	
    };

    private void load(String doc) {
        icons.clear();
        interior.clear();
        ids.clear();
        doc = doc.replaceAll("\n", "").trim();
        Document responseXML = XMLParser.parse(doc);
        NodeList rows = responseXML.getElementsByTagName("currentRow");
        for(int n=0; n<rows.getLength();n++) {
            if ( rows.item(n) instanceof Element ) {
                Element result = (Element) rows.item(n);
                NodeList items = result.getElementsByTagName("columnValue");
                if ( items.item(1) instanceof Element ) {
                    Element idE = (Element) items.item(1);
                    if ( idE.getFirstChild() instanceof Text ) {
                        Text idT= (Text) idE.getFirstChild();
                        String id = idT.getData().toString().trim();
                        ids.add(id);
                    }
                }
            }
        }
        int i = 1;
        for (Iterator idsIt = ids.iterator(); idsIt.hasNext();) {
            String id = (String) idsIt.next();
            IconCheckBox icb = new IconCheckBox(i, id);
            icons.add(icb);
            interior.add(icb);
            i++;
        }
        
    }
}
