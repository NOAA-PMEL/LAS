/**
 *
 */
package gov.noaa.pmel.tmap.las.client.openlayers;

import org.gwtopenmaps.openlayers.client.control.ControlOptions;

import gov.noaa.pmel.tmap.las.client.openlayers.DrawSingleFeature.FeatureAddedListener;

import org.gwtopenmaps.openlayers.client.handler.HandlerOptions;
import org.gwtopenmaps.openlayers.client.util.JSObject;

/**
 * @author Edwin Commandeur - Atlis EJS
 *
 */
public class DrawSingleFeatureOptions extends ControlOptions {

	public void onFeatureAdded(FeatureAddedListener listener){
		JSObject callback = DrawSingleFeatureImpl.createFeatureAddedCallback(listener);
		getJSObject().setProperty("featureAdded", callback);
	}

	public void setHandlerOptions(HandlerOptions handlerOptions){
		getJSObject().setProperty("handlerOptions", handlerOptions.getJSObject());
	}
}
