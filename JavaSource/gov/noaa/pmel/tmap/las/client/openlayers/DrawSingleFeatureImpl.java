package gov.noaa.pmel.tmap.las.client.openlayers;

import gov.noaa.pmel.tmap.las.client.openlayers.DrawSingleFeature.FeatureAddedListener;

import org.gwtopenmaps.openlayers.client.util.JSObject;

/**
 *
 * @author Erdem Gunay
 * @author Edwin Commandeur - Atlis EJS
 *
 */
class DrawSingleFeatureImpl {

	public static native JSObject create(JSObject layer, JSObject handler)/*-{
		return new $wnd.OpenLayers.Control.DrawSingleFeature(layer, handler);
	}-*/;

	public static native JSObject create(JSObject layer, JSObject handler, JSObject options)/*-{
		return new $wnd.OpenLayers.Control.DrawSingleFeature(layer, handler, options);
	}-*/;

	public static native JSObject createFeatureAddedCallback(FeatureAddedListener listener)/*-{
		var callback = function(obj){
			var vectorFeatureObj = @org.gwtopenmaps.openlayers.client.feature.VectorFeature::narrowToVectorFeature(Lorg/gwtopenmaps/openlayers/client/util/JSObject;)(obj);
			listener.@gov.noaa.pmel.tmap.las.client.openlayers.DrawSingleFeature.FeatureAddedListener::onFeatureAdded(Lorg/gwtopenmaps/openlayers/client/feature/VectorFeature;)(vectorFeatureObj);
		}
		return callback;
	}-*/;

}
