package gov.noaa.pmel.tmap.las.client.map;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
/**
 * A widget that allows the user to set the map selection by choosing a named region.  These regions are derived from our application
 <pre>
                 These are the default regions from LAS 
                        wLon, eLon,sLat, nLat
                         "-20, 60, -40, 40"  Africa
                         "40, 180, 0, 80"    Asia
                         "110, 180, -50, 0"  Australia
                         "-10, 40, 30, 75"   Europe
                         "-170, -50, 10, 75" North America 
                         "-90, -30, -60, 15" South America
                         "20,120,-75,30"     Indian Ocean
                         "-80, 20, 0, 70"    North Atlantic
                         "-80, 20, -30, 30"  Equatorial Atlantic
                         "-70,25,-75,10"     South Atlantic
                         "110, -100, 0, 70"  North Pacific
                         "135,-75,-30,30"    Equatorial Pacific
                         "150, -70, -75, 0"  South Pacific
            </pre>
 *
 * @author rhs
 *
 */
public class RegionWidget extends ListBox {
    Map<String, double[]> regions = new HashMap<String, double[]>();
    public RegionWidget() {
    	super();
    	init( null, false);
    }
    public RegionWidget(String title, boolean global) {
    	super();
    	init(title, global);
    }
    public double[] getRegion(int i, String name) {
    	double[] region = null;
    	if ( i >= 1 && getItemText(i).equals(name) ) {
    		region = regions.get(getValue(i));
    	}	
    	return region;
    }
    /**
     * Start up the region widget
     * @param map the map to which this widget applies
     * @param title the title for the first entry in the list, can be null
     * @param global whether you want the "Global" region to appear in the list
     */
    private void init(String title, boolean global) {
    	
    	if (title == null ) {
    	    addItem("Select Region", "none");
    	} else {
    		addItem(title, "none");
    	}
    	if ( global ) {
    		regions.put("global", new double[]{-90., 90, -180., 180.0});
    		addItem("Global", "global");
    	}
    	regions.put("africa", new double[]{-40, 40, -20, 60});
    	addItem("Africa", "africa");
    	regions.put("asia", new double[]{0, 80, 40, 180});
    	addItem("Asia", "asia");
    	regions.put("australia", new double[]{-50, 0, 110, 180});
    	addItem("Australia", "australia");
    	regions.put("europe", new double[]{30, 75, -10, 40});
    	addItem("Europe", "europe");
    	regions.put("north america", new double[]{10, 75, -170, -50});
    	addItem("North America", "north america");
    	regions.put("south america", new double[]{-60, 15, -90, -30});
    	addItem("South America", "south america");
    	regions.put("indian ocean", new double[]{-75, 30, 20, 120});
    	addItem("Indian Ocean", "indian ocean");
    	regions.put("north atlantic", new double[] {0, 70, -80, 20});
    	addItem("North Atlantic", "north atlantic");
    	regions.put("equatorial atlantic", new double[] {-30, 30, -80, 20});
    	addItem("Equatorial Atlantic", "equatorial atlantic");
    	regions.put("south atlantic", new double[]{-75, 10, -70, 25});
    	addItem("South Atlantic", "south atlantic");
    	regions.put("north pacific", new double[]{0, 70, 110, 260});
    	addItem("North Pacific", "north pacific");
    	regions.put("equatorial pacific", new double[]{-30, 30, 135, 285});
    	addItem("Equatorial Pacific", "equatorial pacific");
    	regions.put("south pacific", new double[]{-75, 0, 150, 290});
    	addItem("South Pacific", "south pacific");
    }
    /**
     * Get the bounds of the currently selected named region
     * @return bounds the bounds of the currently selected region
     */
    public double[] getBounds() {
    	double[] region = regions.get(0);
    	if ( getSelectedIndex() >= 1 ) {
    		region = regions.get(getValue(getSelectedIndex()));
    	}
    	if ( region == null ) {
    		setSelectedIndex(0);
    	}
    	return region;
    }
////    /**
////     * Set the map to have the currently named region selected if possible
////     * @param region the region to select
////     */
////    public void setToRegion(LatLngBounds region) {
////    	if ( region != null ) {
////			int trys = 0;
////			while ( trys < 3 ) {
////				if ( refMap.getDataBounds().containsBounds(region) ) {
////					
////					if ( refMap.isModulo() ) {
////						int zoom = refMap.getBoundsZoomLevel(region);
////						LatLngBounds dBounds = refMap.getDataBounds();
////						double nlat = dBounds.getNorthEast().getLatitude();
////						double slat = dBounds.getSouthWest().getLatitude();
////						double clon = region.getCenter().getLongitude();
////						double wlon = clon - 180;
////						double elon = clon + 179;
////						LatLng sw = LatLng.newInstance(slat, wlon);
////						LatLng ne = LatLng.newInstance(nlat, elon);
////						LatLngBounds bounds = LatLngBounds.newInstance(sw, ne);
////						refMap.setDataBounds(bounds, refMap.getDelta(), true);
////						refMap.setCenter(region.getCenter());
////						refMap.setZoom(zoom);
////					}
////					refMap.setSelectionBounds(region, true, true);
////					
////					break;
////				} else {
////					if ( refMap.isModulo() ){
////						refMap.rotateEast();
////					} else {
////						break;
////					}
////					trys++;
////				}		
////			}
////		}
////    	setSelectedIndex(0);
////    }
//    /**
//     * Add a listener to region list
//     */
//    ChangeListener regionChange = new ChangeListener() {
//		public void onChange(Widget sender) {
//			LatLngBounds region = getBounds();
//			setToRegion(region);
//		}	
//	};
	/**
	 * Remove the default listener and add the listener
	 * @param listener the listener that will replace the default
	 */
	public void setChangeListener(ChangeListener listener) {
		addChangeListener(listener);
	}
}
