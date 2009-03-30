package gov.noaa.pmel.tmap.las.client.map;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ListBox;
/*                        
 *             This are the default regions from LAS 
 *                        wLon, eLon,sLat, nLat
 *          <item values="-20, 60, -40, 40">Africa</item>
            <item values="40, 180, 0, 80">Asia</item>
            <item values="110, 180, -50, 0">Australia</item>
            <item values="-10, 40, 30, 75">Europe</item>
            <item values="-170, -50, 10, 75">North America </item>
            <item values="-90, -30, -60, 15">South America</item>
            <item values="20,120,-75,30">Indian Ocean</item>
            <item values="-80, 20, 0, 70">North Atlantic</item>
            <item values="-80, 20, -30, 30">Equatorial Atlantic</item>
            <item values="-70,25,-75,10">South Atlantic</item>
            <item values="110, -100, 0, 70">North Pacific</item>
            <item values="135,-75,-30,30">Equatorial Pacific</item>
            <item values="150, -70, -75, 0">South Pacific</item>

 */
import com.google.gwt.user.client.ui.Widget;
 
public class RegionWidget extends ListBox {
	ReferenceMap refMap;
    Map<String, LatLngBounds> regions = new HashMap<String, LatLngBounds>();
    public RegionWidget(ReferenceMap map) {
    	super();
    	refMap = map;
    	addItem("Select region", "none");
    	regions.put("africa", LatLngBounds.newInstance(LatLng.newInstance(-40, -20), LatLng.newInstance(40, 60)));
    	addItem("Africa", "africa");
    	regions.put("asia", LatLngBounds.newInstance(LatLng.newInstance(0, 40), LatLng.newInstance(80, 180)));
    	addItem("Asia", "asia");
    	regions.put("australia", LatLngBounds.newInstance(LatLng.newInstance(-50, 110), LatLng.newInstance(0, 180)));
    	addItem("Australia", "australia");
    	regions.put("europe", LatLngBounds.newInstance(LatLng.newInstance(30, -10), LatLng.newInstance(75, 40)));
    	addItem("Europe", "europe");
    	regions.put("north america", LatLngBounds.newInstance(LatLng.newInstance(10, -170), LatLng.newInstance(75, -50)));
    	addItem("North America", "north america");
    	regions.put("south america", LatLngBounds.newInstance(LatLng.newInstance(-60, -90), LatLng.newInstance(15, -30)));
    	addItem("South America", "south america");
    	regions.put("indian ocean", LatLngBounds.newInstance(LatLng.newInstance(-75, 20), LatLng.newInstance(30, 120)));
    	addItem("Indian Ocean", "indian ocean");
    	regions.put("north atlantic", LatLngBounds.newInstance(LatLng.newInstance(0, -80), LatLng.newInstance(70, 20)));
    	addItem("North Atlantic", "north atlantic");
    	regions.put("equatorial atlantic", LatLngBounds.newInstance(LatLng.newInstance(-30, -80), LatLng.newInstance(30, 20)));
    	addItem("Equatorial Atlantic", "equatorial atlantic");
    	regions.put("south atlantic", LatLngBounds.newInstance(LatLng.newInstance(-75, -70), LatLng.newInstance(10, 25)));
    	addItem("South Atlantic", "south atlantic");
    	regions.put("north pacific", LatLngBounds.newInstance(LatLng.newInstance(0, 110), LatLng.newInstance(70, -100)));
    	addItem("North Pacific", "north pacific");
    	regions.put("equatorial pacific", LatLngBounds.newInstance(LatLng.newInstance(-30, 135), LatLng.newInstance(30, -75)));
    	addItem("Equatorial Pacific", "equatorial pacific");
    	regions.put("south pacific", LatLngBounds.newInstance(LatLng.newInstance(-75, 150), LatLng.newInstance(0, -70)));
    	addItem("South Pacific", "south pacific");
    	addChangeListener(regionChange);
    }
    public void setRegion(int i, String name) {
    	LatLngBounds region = null;
    	if ( i >= 1 && getItemText(i).equals(name) ) {
    		region = regions.get(getValue(i));
    		setToRegion(region);
    	}	
    }
    public LatLngBounds getBounds() {
    	LatLngBounds region = null;
    	if ( getSelectedIndex() >= 1 ) {
    		region = regions.get(getValue(getSelectedIndex()));
    	}
    	if ( region == null ) {
    		setSelectedIndex(0);
    	}
    	return region;
    }
    public void setToRegion(LatLngBounds region) {
    	if ( region != null ) {
			int trys = 0;
			while ( trys < 3 ) {
				if ( refMap.getDataBounds().containsBounds(region) ) {
					
					if ( refMap.isModulo() ) {
						int zoom = refMap.getBoundsZoomLevel(region);
						LatLngBounds dBounds = refMap.getDataBounds();
						double nlat = dBounds.getNorthEast().getLatitude();
						double slat = dBounds.getSouthWest().getLatitude();
						double clon = region.getCenter().getLongitude();
						double wlon = clon - 180;
						double elon = clon + 179;
						LatLng sw = LatLng.newInstance(slat, wlon);
						LatLng ne = LatLng.newInstance(nlat, elon);
						LatLngBounds bounds = LatLngBounds.newInstance(sw, ne);
						refMap.setDataBounds(bounds, refMap.getDelta(), true);
						refMap.setCenter(region.getCenter());
						refMap.setZoom(zoom);
					}
					refMap.setSelectionBounds(region, true, true);
					
					break;
				} else {
					if ( refMap.isModulo() ){
						refMap.rotateEast();
					} else {
						break;
					}
					trys++;
				}		
			}
		}
    	setSelectedIndex(0);
    }
    ChangeListener regionChange = new ChangeListener() {
		public void onChange(Widget sender) {
			LatLngBounds region = getBounds();
			setToRegion(region);
		}	
	};
}
