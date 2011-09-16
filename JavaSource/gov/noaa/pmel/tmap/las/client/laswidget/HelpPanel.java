package gov.noaa.pmel.tmap.las.client.laswidget;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.CDATASection;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class HelpPanel extends Composite {
	
	private int popupLeft = -999;
	private int popupTop = -999;
	private String frameHeight = "525px";
	private String frameWidth = "525px";
	ToggleButton helpButton;
	PopupPanel mainPanel = new PopupPanel(false);
	VerticalPanel layoutPanel = new VerticalPanel();
	
	public HelpPanel() {
		mainPanel.add(layoutPanel);
		helpButton = new ToggleButton(new Image(GWT.getModuleBaseURL()+"../images/question_off.png"), 
				new Image(GWT.getModuleBaseURL()+"../images/question_on.png"), new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						if ( helpButton.isDown() ) {	
							setOpen(true);
						} else {
							setOpen(false);
						}						
					}
			
		});
		helpButton.setTitle("Help");
		helpButton.setStylePrimaryName("OL_MAP-ToggleButton");
		helpButton.addStyleDependentName("WIDTH");
		initWidget(helpButton);
	}
	
	public void setHelpHtml(String html) {
		HTML help = new HTML(html);
		layoutPanel.clear();
		layoutPanel.add(help);
	}
	public void setHelpURL(String url) {
		Frame help = new Frame(GWT.getModuleBaseURL()+url);
		help.setHeight(frameHeight);
		help.setWidth(frameWidth);
		layoutPanel.clear();
		layoutPanel.add(help);
	}
	public void setOpen(boolean open) {
		if ( open ) {
			if ( !helpButton.isDown() ) helpButton.setDown(true);
			if ( popupTop == -999 || popupLeft == -999 ) {
				mainPanel.setPopupPosition(helpButton.getAbsoluteLeft(), helpButton.getAbsoluteTop() + 32 );
			} else {
				mainPanel.setPopupPosition(popupLeft, popupTop);
			}
			mainPanel.show();
		} else {
			if ( helpButton.isDown() ) helpButton.setDown(false);
			mainPanel.hide();
		}
	}
	public void setPopupTop( int top ) {
		popupTop = top;
	}
	public void setPopupLeft( int left ) {
		popupLeft = left;
	}
	public void setPopupWidth(String width) {
		mainPanel.setWidth(width);
		frameWidth = width;
	}
	public void setPopupHeight(String height) {
		mainPanel.setHeight(height);
		frameHeight = height;
	}
}
