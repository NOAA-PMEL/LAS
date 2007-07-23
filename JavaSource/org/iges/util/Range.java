package org.iges.util;

import java.util.*;
import java.text.*;


/** This is actually a container for three similar classes, Range.Double, Range.Long, and Range.Date. 
 *  All represent an interval, either of real #s, integers, or time. The
 *  methods are similar in function, but take different primitive types as arguments
 *  Although there is no formal relationship between them, they are grouped together  
 *  to emphasize their similarity.
 *
 * Last modified: $Date: 2004/04/19 17:33:35 $ 
 * Revision for this file: $Revision: 1.2 $
 * Release name: $Name:  $
 * Original for this file: $Source: /home/ja9/tmap/FERRET_ROOT/fds/src/org/iges/util/Range.java,v $
 */
public class Range{

    /** Represents a range of floating-point numbers. */
    public static class Double {

	public double min;
	public double max;

	/** Creates a new Range.Double */
	public Double(double _min, double _max) {
	    min = _min;
	    max = _max;
	}

	/** Tests whether a given value is contained in this range */
	public boolean contains(Range.Double range) {
	    return (this.min <= range.min) && (this.max >= range.max);
	}
	
	/** Tests whether this range overlaps the one specified */
	public boolean overlaps(Range.Double range) {
	    return (range.min <= this.max) && (range.max >= this.min);
	}

	public boolean equals(Range.Double range) {
	    return (this.min == range.min) && (this.max == range.max);
	}
	
	/** Returns a string representation of this range */
	public String toString() {
	    return "(" + min + ", " + max + ")";
	}
    }
    
    /** Represents a range of integers. */
    public static class Long {

	public long min;
	public long max;

	/** Creates a new Range.Long */
	public Long(long _min, long _max) {
	    min = _min;
	    max = _max;
	}

	/** Tests whether a given value is contained in this range */
	public boolean contains(Range.Long range) {
	    return (this.min <= range.min) && (this.max >= range.max);
	}
	
	/** Tests whether this range overlaps the one specified */
	public boolean overlaps(Range.Long range) {
	    return (range.min <= this.max) && (range.max >= this.min);
	}

	public Range.Long union(Range.Long range) {
	    return new Range.Long(Math.min(this.min, range.min),
			     Math.max(this.max, range.max));
	}

	/** Returns the number of integers contained in this range */
	public long size() {
	    return max - min + 1;
	}
	
	/** Returns a string representation of this range */
	public String toString() {
	    return "(" + min + ", " + max + ")";
	}
    }

    /** Represents a range of dates. */
    public static class Date {

	
	public java.util.Date min;
	public java.util.Date max;
	public String minString;
	public String maxString;

	/** Creates a new Range.Date from two GrADS format date strings */
	public Date(String _minString, String _maxString) {
	    minString = _minString;
	    maxString = _maxString;
	}

	protected void parseDates() {
	    min = parseFerretFormat(minString);
	    max = parseFerretFormat(maxString);
	    
	    if (min == null) {
		throw new IllegalArgumentException
		    (minString + " is not a valid Ferret date");
	    }
	    if (max == null) {
		throw new IllegalArgumentException
		    (maxString + " is not a valid Ferret date");
	    }
	}	    


	/** Tests whether a given value is contained in this range */
	public boolean contains(Range.Date range) 
	    throws IllegalArgumentException {
	    parseDates();
	    return (min.getTime() <= range.min.getTime()) 
		&& (max.getTime() >= range.max.getTime());
	}
	
	/** Tests whether this range overlaps the one specified */
	public boolean overlaps(Range.Date range) 
	    throws IllegalArgumentException {
	    parseDates();
	    return (range.min.getTime() <= max.getTime()) 
		&& (range.max.getTime() >= min.getTime());
	}
	
	/** Returns a string representation of this range */
	public String toString() {
	    return "(" + min + ", " + max + ")";
	}
    }


    public static String printFerretDate(java.util.Date date) {
	SimpleDateFormat ferretFormat
	    = new SimpleDateFormat ("yyyy:M:d:H:m");
	return ferretFormat.format(date);
    }



    /** Parses a date from (hopefully) any of the various formats used
     * by Ferret into a Java Date object. */
    public static java.util.Date parseFerretFormat(String dateString) 
	throws IllegalArgumentException {

	dateString = dateString.toLowerCase() + " GMT";
	// Get a Date object from the dateString
	ParsePosition pos = new ParsePosition(0);

	// Check for alternate formats

	// These two are output by "set time"
	SimpleDateFormat minuteFormat
	    = new SimpleDateFormat ("yyyy:M:d:H:m z");
	java.util.Date parsedDate = minuteFormat.parse(dateString, pos);
	
	if (parsedDate == null) {
	    SimpleDateFormat hourFormat 
		= new SimpleDateFormat ("yyyy:M:d:H z");
	    parsedDate = hourFormat.parse(dateString, pos);
	}

	// These are legal formats in CTL files and "q time"
	if (parsedDate == null) {
	    SimpleDateFormat ctlHourFormat
		= new SimpleDateFormat ("H:m'z'ddMMMyyyy z"); 
	                              // single quotes for literal 'z'
	    parsedDate = ctlHourFormat.parse(dateString, pos);
	}

	if (parsedDate == null) {
	    SimpleDateFormat ctlHourFormat
		= new SimpleDateFormat ("H'z'ddMMMyyyy z"); 
	                              // single quotes for literal 'z'
	    parsedDate = ctlHourFormat.parse(dateString, pos);
	}

	if (parsedDate == null) {
	    SimpleDateFormat ctlFormat
		= new SimpleDateFormat ("ddMMMyyyy z");
	    parsedDate = ctlFormat.parse(dateString, pos);
	}

	if (parsedDate == null) {
	    SimpleDateFormat ctlFormat
		= new SimpleDateFormat ("MMMyyyy z");
	    parsedDate = ctlFormat.parse(dateString, pos);
	}

	if (parsedDate == null) {
	    throw new IllegalArgumentException("Can't parse date string: " + 
					       dateString);
	}

	return parsedDate;
    }
    
}
