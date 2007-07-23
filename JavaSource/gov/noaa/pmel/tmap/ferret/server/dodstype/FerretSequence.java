package gov.noaa.pmel.tmap.ferret.server.dodstype;

import dods.dap.Server.*;
import dods.dap.*;
import java.io.*;

import java.io.*;
import java.util.*;

import org.iges.anagram.AnagramException;

/** An implementation of the DODS Sequence data type. <p>
 *
 * @author Yonghua Wei
 */
public class FerretSequence
    extends SDSequence {

    
    /** Constructs a new GradsSequence. */
    public FerretSequence() { 
	super(); 
    }
    
    /**
     * Constructs a new GradsSequence with name n.
     * @param n the name of the variable.
     */
    public FerretSequence(String n) { 
	super(n); 
    }

    /** Dummy implementation */
    public boolean read(String datasetName, Object specialO) {
	return true;
    }
}
