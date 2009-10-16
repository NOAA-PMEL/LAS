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
public class HorizontalPathHandler extends PointHandler {

	protected HorizontalPathHandler(JSObject element) {
		super(element);
	}

	public HorizontalPathHandler(){
		this(HorizontalPathHandlerImpl.create());
	}

}
