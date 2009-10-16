package gov.noaa.pmel.tmap.las.client.openlayers;

import org.gwtopenmaps.openlayers.client.control.Control;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.handler.Handler;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.util.JSObject;

/**
 *
 *  Don't be suprised by the following:
 *  Upon activating the DrawFeature tool it creates an extra layer
 *  that is used by the handler (the name of this layer reflects this).
 *
 * (tested in OpenLayers 2.7)
 *
 * @author Erdem Gunay
 * @author Edwin Commandeur - Atlis EJS
 *
 */
public class DrawSingleFeature extends Control {

	public interface FeatureAddedListener {
		void onFeatureAdded(VectorFeature vectorFeature);
	}

	protected DrawSingleFeature(JSObject element) {
		super(element);
	}

	/**
	 *
	 * @param layer
	 * @param handler - a PointHandler, PathHandler or PolygonHandler
	 */
	public DrawSingleFeature(Vector layer, Handler handler) {
		this(DrawSingleFeatureImpl.create(layer.getJSObject(), handler.getJSObject()));
	}

	/**
	 *
	 * @param layer
	 * @param handler - a PointHandler, PathHandler or PolygonHandler
	 * @param options - see {@link DrawSingleFeatureOptions}
	 */
	public DrawSingleFeature(Vector layer, Handler handler, DrawSingleFeatureOptions options){
		this(DrawSingleFeatureImpl.create(layer.getJSObject(), handler.getJSObject(), options.getJSObject()));
	}

}
