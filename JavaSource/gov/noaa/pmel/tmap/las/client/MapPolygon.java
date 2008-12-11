package gov.noaa.pmel.tmap.las.client;

import java.util.ArrayList;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Polygon;

public class MapPolygon {
	
	ArrayList<Polygon> polyList;
	LatLng sw;
    LatLng ne;
    String outlineColor;
    int outlineWeight;
    double outlineOpacity;
    String interiorColor;
    double interiorOpacity;
    
    public MapPolygon (LatLng sw, LatLng ne, String outlineColor, int outlineWeight, double outlineOpacity, String interiorColor, double interiorOpacity) {
    	this.sw = sw;
    	this.ne = ne;
    	this.outlineColor = outlineColor;
    	this.outlineWeight = outlineWeight;
    	this.outlineOpacity = outlineOpacity;
    	this.interiorColor = interiorColor;
    	this.interiorOpacity = interiorOpacity;
    	this.polyList = new ArrayList<Polygon>();
        /*
    	if ( sw.getLongitude() > ne.getLongitude() ) {

    		LatLng[] westBounds = new LatLng[4];
    		westBounds[0] = LatLng.newInstance(sw.getLatitude(), 180.);
    		westBounds[1] = LatLng.newInstance(sw.getLatitude(), sw.getLongitude());
    		westBounds[2] = LatLng.newInstance(ne.getLatitude(), sw.getLongitude());
    		westBounds[3] = LatLng.newInstance(ne.getLatitude(), 180.);
    		Polygon westPolygon = new Polygon(westBounds, outlineColor, outlineWeight, outlineOpacity, interiorColor, interiorOpacity);
    		polyList.add(westPolygon);

    		LatLng[] eastBounds = new LatLng[4];
    		eastBounds[0] = LatLng.newInstance(sw.getLatitude(), -180.);
    		eastBounds[1] = LatLng.newInstance(sw.getLatitude(), ne.getLongitude());
    		eastBounds[2] = LatLng.newInstance(ne.getLatitude(), ne.getLongitude());
    		eastBounds[3] = LatLng.newInstance(ne.getLatitude(), -180.);
    		Polygon eastPoly = new Polygon(eastBounds, outlineColor, outlineWeight, outlineOpacity, interiorColor, interiorOpacity);
    		polyList.add(eastPoly);

    	} else {
    		LatLng[] bounds = new LatLng[5];
			bounds[0] = LatLng.newInstance(sw.getLatitude(), sw.getLongitude());
			bounds[1] = LatLng.newInstance(ne.getLatitude(), sw.getLongitude());
			bounds[2] = LatLng.newInstance(ne.getLatitude(), ne.getLongitude());
			bounds[3] = LatLng.newInstance(sw.getLatitude(), ne.getLongitude());
			bounds[4] = LatLng.newInstance(sw.getLatitude(), sw.getLongitude());
			Polygon poly = new Polygon(bounds, outlineColor, outlineWeight, outlineOpacity, interiorColor, interiorOpacity);
    		polyList.add(poly);
    	}
    	*/
    	LatLngBounds bounds = LatLngBounds.newInstance(sw, ne);
    	LatLng center = bounds.getCenter();
    	LatLng[] boundingPolygon = new LatLng[9];
		boundingPolygon[0] = LatLng.newInstance(sw.getLatitude(), sw.getLongitude());
		boundingPolygon[1] = LatLng.newInstance(center.getLatitude(), sw.getLongitude());
		boundingPolygon[2] = LatLng.newInstance(ne.getLatitude(), sw.getLongitude());
		boundingPolygon[3] = LatLng.newInstance(ne.getLatitude(), center.getLongitude());
		boundingPolygon[4] = LatLng.newInstance(ne.getLatitude(), ne.getLongitude());
		boundingPolygon[5] = LatLng.newInstance(center.getLatitude(), ne.getLongitude());
		boundingPolygon[6] = LatLng.newInstance(sw.getLatitude(), ne.getLongitude());
		boundingPolygon[7] = LatLng.newInstance(sw.getLatitude(), center.getLongitude());
		boundingPolygon[8] = LatLng.newInstance(sw.getLatitude(), sw.getLongitude());
		Polygon poly = new Polygon(boundingPolygon, outlineColor, outlineWeight, outlineOpacity, interiorColor, interiorOpacity);
		polyList.add(poly);
    }

	/**
	 * @return the polyList
	 */
	public ArrayList<Polygon> getPolyList() {
		return polyList;
	}
    
}
