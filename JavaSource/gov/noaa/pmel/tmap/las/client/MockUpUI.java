package gov.noaa.pmel.tmap.las.client;

import java.util.Iterator;

import gov.noaa.pmel.tmap.las.client.laswidget.Constants;
import gov.noaa.pmel.tmap.las.client.laswidget.OutputPanel;
import gov.noaa.pmel.tmap.las.client.laswidget.PopupTextPanel;
import gov.noaa.pmel.tmap.las.client.serializable.ConfigSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.util.Util;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootPanel;

public class MockUpUI extends VizGal {
	
	PushButton xmlButton = new PushButton("Change XML");
	PushButton imageButton = new PushButton("Change Image");
	PopupTextPanel xmlPanel = new PopupTextPanel("URL of the new annotations XML.");
	PopupTextPanel imagePanel = new PopupTextPanel("URL of the new image.");
	public void onModuleLoad() {
		
		//http://strider.weathertopconsulting.com:8282/baker/VizGal.vm?dsid=coads_climatology_cdf&vid=sst&opid=Plot_2D_XY_zoom&optionid=Options_2D_image_contour_xy_7&view=xy&xlo=21&xhi=379&ylo=-89&yhi=89&tlo=15-Jan&thi=15-Jan
		xDSID = "coads_climatology_cdf";
		xVarID = "sst";
		xOperationID = "Plot_2D_XY_zoom";
		xOptionID = "Options_2D_image_contour_xy_7";
		xView = "xy";
		
		
		super.onModuleLoad();
		
		xmlButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				
				xmlPanel.show(xmlButton.getAbsoluteLeft(), xmlButton.getAbsoluteTop());
				
			}
			
		});
		
		imageButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				
				imagePanel.show(imageButton.getAbsoluteLeft(), imageButton.getAbsoluteTop());
				
			}
			
		});
		xmlPanel.addCloseHandler(xmlCloseHandler);
		imagePanel.addCloseHandler(imageCloseHandler);
		buttonLayout.setWidget(0, 0, xmlButton);
		buttonLayout.setWidget(0, 1, imageButton);
		
	}
	ClickHandler imageCloseHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			
			for (Iterator iterator = xPanels.iterator(); iterator.hasNext();) {
				OutputPanel p = (OutputPanel) iterator.next();
				p.setImage(imagePanel.getText());
			}

		}
		
	};
	ClickHandler xmlCloseHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			
			for (Iterator panelsIt = xPanels.iterator(); panelsIt.hasNext();) {
				OutputPanel p = (OutputPanel) panelsIt.next();
				p.setAnnotationsURL(xmlPanel.getText());
			}
			
		}
	};
}
