package gov.noaa.pmel.tmap.ferret.server.dodstype;

import dods.dap.Server.*;
import dods.dap.*;
import java.io.*;


/** Implements the server-side version of Int32 
 * All datatypes that appear in DDS'es for FDS datasets must have
 * server-side implementations, even if its just a shell.<p>
 *
 * Modified from org.iges.grads.server.dap.GenericInt32 <p>
 *
 * @author Yonghua Wei
 */
public class GenericInt32 extends SDInt32 {
    
    private static int rCount = 0;        
	
    /** Constructs a new <code>GenericInt32</code>. */
    public GenericInt32() { 
	super(); 
    }
    
    /**
     * Constructs a new <code>GenericInt32</code> with name <code>n</code>.
     * @param n the name of the variable.
     */
    public GenericInt32(String n) { 
	super(n); 
    }
    
    
    public static void resetCount(){
    	rCount = 0;
    }
    
    /** Dummy procedure
     */
    public boolean read(String datasetName, Object specialO)
	throws NoSuchVariableException, IOException, EOFException {
        setRead(true);
        return (false);
    }
}


