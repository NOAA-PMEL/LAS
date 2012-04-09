package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.map.OLMapWidget;

import org.gwtopenmaps.openlayers.client.Icon;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Pixel;
import org.gwtopenmaps.openlayers.client.Size;
import org.gwtopenmaps.openlayers.client.control.SelectFeature;
import org.gwtopenmaps.openlayers.client.event.FeatureHighlightedListener;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.format.KML;
import org.gwtopenmaps.openlayers.client.layer.GML;
import org.gwtopenmaps.openlayers.client.layer.GMLOptions;
import org.gwtopenmaps.openlayers.client.layer.Markers;
import org.gwtopenmaps.openlayers.client.layer.MarkersOptions;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.layer.VectorOptions;
import org.gwtopenmaps.openlayers.client.popup.AnchoredBubble;
import org.gwtopenmaps.openlayers.client.popup.Popup;
import org.gwtopenmaps.openlayers.client.protocol.HTTPProtocol;
import org.gwtopenmaps.openlayers.client.protocol.HTTPProtocolOptions;
import org.gwtopenmaps.openlayers.client.strategy.FixedStrategy;
import org.gwtopenmaps.openlayers.client.strategy.FixedStrategyOptions;
import org.gwtopenmaps.openlayers.client.strategy.Strategy;
import org.gwtopenmaps.openlayers.client.util.Attributes;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class OpenLayersKML implements EntryPoint {
	OLMapWidget map;
	TextBox kmlURL = new TextBox();
	PushButton kmlButton = new PushButton("Load KML");
	HorizontalPanel header = new HorizontalPanel();
	VerticalPanel panel = new VerticalPanel();
	@Override
	public void onModuleLoad() {
		
		map = new OLMapWidget("512px", "1024px");
		map.setTool("xy");
        map.setDataExtent(-90, 90, -180, 180);
    	kmlURL.setValue("http://gazelle.weathertopconsulting.com:8282/baker/output/4D8EDF082029CB91B161DD9A12D198D7_kml.kml");
		kmlButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				String url = kmlURL.getValue();
				FixedStrategyOptions fOptions = new FixedStrategyOptions();
				FixedStrategy fStrategy = new FixedStrategy(fOptions);
				KML kml = new KML();
				kml.getJSObject().setProperty("extractStyles", true);
				kml.getJSObject().setProperty("extractAttributes", true);
				kml.getJSObject().setProperty("maxDepth", "2");
				HTTPProtocolOptions httpProtOptions = new HTTPProtocolOptions();
				httpProtOptions.setUrl(url);
				httpProtOptions.setFormat(kml);
				HTTPProtocol httpProt = new HTTPProtocol(httpProtOptions);
				VectorOptions options = new VectorOptions();
				options.setStrategies(new Strategy[] { fStrategy });
				options.setProtocol(httpProt);
				options.setIsBaseLayer(false);
				Vector v = new Vector("KML", options);
				SelectFeature selectFeature = new SelectFeature(v);
			
				selectFeature.addFeatureHighlightedListener(new FeatureHighlightedListener() {
					
					@Override
					public void onFeatureHighlighted(VectorFeature vectorFeature) {
						Attributes a = vectorFeature.getAttributes();
						// "<p>" + new KML().write(vectorFeature) + "</p>",
						String ds = a.getAttributeAsString("description");
						Popup pop = new AnchoredBubble("info",
	                            vectorFeature.getCenterLonLat(),
	                            new Size(250, 150),
	                            ds,
	                            new Icon("", new Size(0, 0), new Pixel(0, 0)),
	                            true);
						pop.setPanMapIfOutOfView(true);
						map.getMap().addPopup(pop);
					}
				});
				map.getMap().addLayer(v);
				map.getMap().addControl(selectFeature);
				selectFeature.activate();
			}
			
		});
		header.add(kmlURL);
		header.add(kmlButton);
		panel.add(header);
		panel.add(map);
		RootPanel.get("map").add(panel);
	}
}
