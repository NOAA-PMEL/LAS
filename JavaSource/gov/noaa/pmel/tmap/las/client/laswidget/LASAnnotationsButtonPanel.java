package gov.noaa.pmel.tmap.las.client.laswidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;



public class LASAnnotationsButtonPanel extends Composite {
	VerticalPanel layout = new VerticalPanel();
	LASAnnotationsPanel annotations = new LASAnnotationsPanel();
	ToggleButton button;
	
	public LASAnnotationsButtonPanel() {
		button = new ToggleButton(new Image(GWT.getModuleBaseURL()+"../images/i_off.png"), new Image(GWT.getModuleBaseURL()+"../images/i_on.png"));
		button.setTitle("Plot Annotations");
		button.setStylePrimaryName("OL_MAP-ToggleButton");
		button.addStyleDependentName("WIDTH");
		layout.add(button);
		layout.add(annotations);
		initWidget(layout);
	}
	
	public void addClickHandler(ClickHandler clickHandler) {
		button.addClickHandler(clickHandler);
	}
	public void setAnnotationsHTML(String html) {
		annotations.setAnnotationsHTML(html);
	}
	public void setError(String html) {
		annotations.setError(html);
	}
	public void setAnnotations(String xml) {
		annotations.setAnnotations(xml);
	}
	public void setAnnotationsHTMLURL(String url) {
		annotations.setAnnotationsHTMLURL(url);
	}
	public void setPopupWidth(String width) {
		annotations.setPopupWidth(width);
	}
	public void setOpen(boolean open) {
		annotations.setOpen(open);
	}
	public void setButtonDown(boolean down) {
		button.setDown(down);
	}
}
