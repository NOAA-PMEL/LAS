package org.iges.util;

import java.util.*;

/** This is actually a container for two similar classes, Bounds.Grid and Bounds.World. 
 *  Both represent a 4-dimensional constraint (a dimension environment, in GrADS terms). The
 *  methods are similar in function, but take different primitive types as arguments
 *  Although there is no formal relationship between them, they are grouped together  
 *  to emphasize their similarity.
 *
 * Last modified: $Date: 2004/04/23 18:12:58 $ 
 * Revision for this file: $Revision: 1.5 $
 * Release name: $Name:  $
 * Original for this file: $Source: /home/ja9/tmap/FERRET_ROOT/fds/src/org/iges/util/Bounds.java,v $
 */
public class Bounds {

    /** Represents a 4-dimensional constraint in grid (relative) coordinates.
     *  This class follows the GrADS 1-based indexing convention.
     */
    public abstract static class Base {
	public abstract String toString();
    }

    public static class Grid 
         extends Base {
	/** Creates a Bounds.Grid object with the constraints given.
	 * No min < max validation is performed.
	 */
	public Grid(long xMin, long xMax,
		    long yMin, long yMax,
		    long zMin, long zMax,
		    long tMin, long tMax) {
	    x = new Range.Long(xMin, xMax);
	    y = new Range.Long(yMin, yMax);
	    z = new Range.Long(zMin, zMax);
	    t = new Range.Long(tMin, tMax);
	}

	public Grid(Range.Long x,
		    Range.Long y,
		    Range.Long z,
		    Range.Long t) {
	    this.x = x;
	    this.y = y;
	    this.z = z;
	    this.t = t;
	}


	/** Creates a Bounds.Grid object from a space-separated string.
	 *  The format of the string is "x1 x2 y1 y2 z1 z2 t1 t2". 
	 *  If the zeroBased parameter is true, the string will converted from 
	 *  0-based to 1-based values (i.e, 1 will be added to each value).
	 */
	public Grid(String list, boolean zeroBased) 
	    throws IllegalArgumentException {

	    StringTokenizer st = new StringTokenizer(list);
	    long value;
	    long[] bounds = new long[8];

	    int i = 0;
	    try {
		for (i = 0; i < 8; i++) {
		    value =  Long.valueOf(st.nextToken()).longValue();
		    bounds[i] = (zeroBased) ? value + 1 : value;
		}
	    } catch (NumberFormatException nfe) {
		throw new IllegalArgumentException("invalid number format in bounds expression (element " + (i+1) +")"+list);
	    } catch (NullPointerException npe) {
		throw new IllegalArgumentException("not enough elements in bounds expression");
	    } catch (NoSuchElementException nsee) {
		throw new IllegalArgumentException("not enough elements in bounds expression");
            }

	    x = new Range.Long(bounds[0], bounds[1]);
	    y = new Range.Long(bounds[2], bounds[3]);
	    z = new Range.Long(bounds[4], bounds[5]);
	    t = new Range.Long(bounds[6], bounds[7]);

	}

	public boolean equals(Bounds.Grid grid) {
	    return (this.x.equals(grid.x)
		    && this.y.equals(grid.y)
		    && this.y.equals(grid.z)
		    && this.y.equals(grid.t));
	}



	public Bounds.Grid union(Bounds.Grid grid) {
	    return new Bounds.Grid(this.x.union(grid.x),
				   this.y.union(grid.y),
				   this.z.union(grid.z),
				   this.t.union(grid.t));
	}

	/** Calculates the number of points in the grid (not size in bytes) */
	public long getSize() {
	    return (Math.abs(x.max - x.min) + 1) 
		* (Math.abs(y.max - y.min) + 1)
		* (Math.abs(z.max - z.min) + 1)
		* (Math.abs(t.max - t.min) + 1);
	}


	public Range.Long x;
	public Range.Long y;
	public Range.Long z;
	public Range.Long t;


	/** Returns a string representation of the grid bounds. This representation
	 * can be used to create a new Bounds.Grid object. */
	public String toString() {
	    return "/i="+x.min+":"+ x.max 
                + "/j="+y.min+":"+y.max 
                + "/k="+z.min+":"+z.max 
                + "/l="+t.min+":"+t.max;
	}

    }

    /** Represents a 4-dimensional constraint in world (absolute) coordinates.
     *  Latitude, longitude are in degrees. Elevation has no standard units.
     *  Time is stored as a Java Date object.
     */
    public static class World 
         extends Base {

	/** Creates a Bounds.World object with the constraints given.
	 * No min < max validation is performed.
	 */
	public World(Range.Double _lon,
		     Range.Double _lat,
		     Range.Double _lev,
		     Range.Date _time) {
	    lon = _lon;
	    lat = _lat;
	    lev = _lev;
	    time = _time;
	}

	/** Creates a Bounds.World object from a space-separated string.
	 *  The format of the string is "lon1 lon2 lat1 lat2 lev1 lev2 time1 time2". 
	 *  This constructor is used in parsing request URL's. 
	 */
	public World(String list)
	    throws IllegalArgumentException{

	    StringTokenizer st = new StringTokenizer(list);

	    double[] bounds = new double[6];
	    int i = 0;
	    try {
		for (i = 0; i < 6; i++) {
		    bounds[i] = Double.valueOf(st.nextToken()).doubleValue();
		}

	    lon = new Range.Double(bounds[0], bounds[1]);
	    lat = new Range.Double(bounds[2], bounds[3]);
	    lev = new Range.Double(bounds[4], bounds[5]);

	    String minDateString = st.nextToken();
	    String maxDateString = st.nextToken();
	    time = new Range.Date(minDateString, maxDateString);

	    } catch (NumberFormatException nfe) {
		throw new IllegalArgumentException("invalid number format in bounds expression (element " + (i+1) +")");
	    } catch (NoSuchElementException nsee) {
		throw new IllegalArgumentException("not enough elements in bounds expression");
	    }	    
	}

	public Range.Double lon;
	public Range.Double lat;
	public Range.Double lev;
	public Range.Date time;

	/** Returns a string representation of the world bounds. This representation
	 * can be used to create a new Bounds.World object. */
	public String toString() {
	    return 
		 "/x="+lon.min+":"+lon.max 
                +"/y="+lat.min+":"+lat.max 
                +"/z="+lev.min+":"+lev.max 
                +"/t="+time.minString+":"+time.maxString;
	}

    }


}
