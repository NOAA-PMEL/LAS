package gov.noaa.pmel.tmap.las.client.openlayers;

import org.gwtopenmaps.openlayers.client.util.JSObject;

class VerticalPathHandlerImpl {

	public static native JSObject create() /*-{
		return $wnd.OpenLayers.Handler.VerticalPath;
	}-*/;

}