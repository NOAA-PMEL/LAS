package gov.noaa.pmel.tmap.las.client.openlayers;

import org.gwtopenmaps.openlayers.client.handler.PointHandler;
import org.gwtopenmaps.openlayers.client.util.JSObject;

/**
 *
 *
 * @author Edwin Commandeur - Atlis EJS
 * @auther rhs - Roland.Schwetizer@noaa.gov
 *
 */
public class VerticalPathHandler extends PointHandler {

	protected VerticalPathHandler(JSObject element) {
		super(element);
	}

	public VerticalPathHandler(){
		this(VerticalPathHandlerImpl.create());
	}

}
