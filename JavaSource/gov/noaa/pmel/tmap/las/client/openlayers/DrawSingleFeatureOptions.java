/**
 * This software was developed by Roland Schweitzer of Weathertop Consulting, LLC 
 * (http://www.weathertopconsulting.com/) as part of work performed for
 * NOAA Contracts AB113R-04-RP-0068 and AB113R-09-CN-0182.  
 * 
 * The NOAA licensing terms are explained below.
 * 
 * 
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development. 
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
