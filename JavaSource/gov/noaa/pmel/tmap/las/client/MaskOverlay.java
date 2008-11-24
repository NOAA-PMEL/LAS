package gov.noaa.pmel.tmap.las.client;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.maps.client.overlay.Polygon;

public class MaskOverlay extends Overlay {
    MapWidget mMap;
    MapPolygon mLeftPolygon;
    MapPolygon mTopPolygon;
    MapPolygon mRightPolygon;
    MapPolygon mBottomPolygon;
    LatLngBounds mMapBounds;
    LatLngBounds mDataBounds;
    String outlineColor;
    int outlineWeight;
    double outlineOpacity;
    String interiorColor;
    double interiorOpacity;
    ArrayList<Polygon> polyList;
    public MaskOverlay(LatLngBounds mapBounds, 
    	               LatLngBounds dataBounds, 
    	               String outlineColor, 
    	               int outlineWeight, 
    	               double outlineOpacity, 
    	               String interiorColor, 
    	               double interiorOpacity) {
    	
    	mMapBounds = mapBounds;
    	mDataBounds = dataBounds;
    	this.outlineColor = outlineColor;
    	this.outlineWeight = outlineWeight;
    	this.outlineOpacity = outlineOpacity;
    	this.interiorColor = interiorColor;
    	this.interiorOpacity = interiorOpacity;
    	
    	LatLng map_sw = mMapBounds.getSouthWest(); 	
    	LatLng map_ne = mMapBounds.getNorthEast();
    	
    	LatLng sw = mDataBounds.getSouthWest();
    	LatLng ne = mDataBounds.getNorthEast();
    	
    	polyList = new ArrayList<Polygon>();	
    	// Only mask if the west boundary of the map is west of the west boundary of the data.  Got it?
    	double maplongitude = map_sw.getLongitude();
    	double datalongitude = sw.getLongitude();
    	while ( maplongitude <= 0.0 ) {
    		maplongitude = maplongitude + 360.;
    	}
    	while (datalongitude <= 0.0 ) {
    		datalongitude = datalongitude + 360.;
    	}
    	if ( maplongitude <= datalongitude ) {
    	    mLeftPolygon = new MapPolygon(map_sw, LatLng.newInstance(map_ne.getLatitude(), sw.getLongitude()),  outlineColor, outlineWeight, outlineOpacity, interiorColor, interiorOpacity);
    	    for (Iterator polyIt = mLeftPolygon.getPolyList().iterator(); polyIt.hasNext();) {
    			Polygon poly = (Polygon) polyIt.next();
    			polyList.add(poly);
    		}
    	}
    	mTopPolygon = new MapPolygon(LatLng.newInstance(ne.getLatitude(), sw.getLongitude()), LatLng.newInstance(map_ne.getLatitude(), ne.getLongitude()), outlineColor, outlineWeight, outlineOpacity, interiorColor, interiorOpacity);
    	for (Iterator polyIt = mTopPolygon.getPolyList().iterator(); polyIt.hasNext();) {
			Polygon poly = (Polygon) polyIt.next();
			polyList.add(poly);
		}
    	// Only mask if the east boundary of the data is west of the east boundary of the map.  Got it?
    	maplongitude = map_ne.getLongitude();
    	datalongitude = ne.getLongitude();
    	while ( maplongitude <= 0.0 ) {
    		maplongitude = maplongitude + 360.;
    	}
    	while (datalongitude <= 0.0) {
    		datalongitude = datalongitude + 360.;
    	}
    	if ( datalongitude <= maplongitude ) {
    	    mRightPolygon = new MapPolygon(LatLng.newInstance(map_sw.getLatitude(), ne.getLongitude()), map_ne,  outlineColor, outlineWeight, outlineOpacity, interiorColor, interiorOpacity);
    	    for (Iterator polyIt = mRightPolygon.getPolyList().iterator(); polyIt.hasNext();) {
    			Polygon poly = (Polygon) polyIt.next();
    			polyList.add(poly);
    		}
    	}
    	mBottomPolygon = new MapPolygon(LatLng.newInstance(map_sw.getLatitude(), sw.getLongitude()), LatLng.newInstance(sw.getLatitude(), ne.getLongitude()), outlineColor, outlineWeight, outlineOpacity, interiorColor, interiorOpacity);
    	for (Iterator polyIt = mBottomPolygon.getPolyList().iterator(); polyIt.hasNext();) {
			Polygon poly = (Polygon) polyIt.next();
			polyList.add(poly);
		}
		
    }
    
	@Override
	protected Overlay copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void initialize(MapWidget map) {
		mMap = map;
		for (Iterator polyIt = polyList.iterator(); polyIt.hasNext();) {
			Polygon poly = (Polygon) polyIt.next();
			map.addOverlay(poly);
		}

	}

	@Override
	protected void redraw(boolean force) {
		

	}

	@Override
	protected void remove() {
		for (Iterator polyIt = polyList.iterator(); polyIt.hasNext();) {
			Polygon poly = (Polygon) polyIt.next();
			mMap.removeOverlay(poly);
		}

	}

}
