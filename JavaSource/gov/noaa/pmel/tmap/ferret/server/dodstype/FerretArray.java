package gov.noaa.pmel.tmap.ferret.server.dodstype;

import java.io.*;
import java.util.*;

import org.iges.anagram.AnagramException;

import dods.dap.Server.*;
import dods.dap.*;

/** An implementation of the DODS Array data type.<p>
 * 
 *  Only certain arrays are legal: <p>
 *  a 3 or 4 dimensional array that represents actual data,
 *  where the array dimensions are either [time][lev][lat][lon] or 
 *  [time][lat][lon]; or
 *  a 1 dimensional array that represents the grid definition of 
 *  lon, lat, lev or time. <p>
 *  All values are Float32, except time which is a Float64 using the
 *  COARDS time convention.<p>
 *
 * Last modified: $Date: 2005/02/03 02:39:33 $<p> 
 * Original for this file: $Source: /home/ja9/tmap/FERRET_ROOT/fds/src/gov/noaa/pmel/tmap/ferret/server/dodstype/FerretArray.java,v $<p>
 *
 * Modified from org.iges.grads.server.GradsArray <p>
 *
 *@author Richard Roger
 */
public class FerretArray 
    extends SDArray {

    /** Constructs a new FerretArray. */
    public FerretArray() { 
        super(); 
    }

    /**
    * Constructs a new FerretArray with name n.
    * @param n the name of the variable.
    */
    public FerretArray(String n) { 
        super(n); 
    }

    /** Just a dummy procedure. Data is actually read 
     *  by the serialize() method, so it can be 
     *  written directly to the output stream.
     */
    public boolean read(String datasetName, Object specialO)
	throws NoSuchVariableException, 
	       IOException, 
	       EOFException {

	// Flag read operation as complete.
	setRead(true);
	// False means no more data to read.
	return false;
    }
}
