package gov.noaa.pmel.tmap.ferret.server.dodstype;

import dods.dap.Server.*;
import dods.dap.*;
import java.io.*;


/** Implements the server-side version of Float32 
 * All datatypes that appear in DDS'es for datasets must have
 * server-side implementations, even if its just a shell.<p>
 *
 * Modified from org.iges.grads.server.dap.GenericFloat32<p>
 *
 * @author Richard Roger
 */

public class GenericFloat32 
    extends SDFloat32 {
    
    /** Constructs a new <code>GenericFloat32</code>. */
    public GenericFloat32() { 
	super(); 
    }
    
    /**
     * Constructs a new <code>GenericFloat32</code> with name <code>n</code>.
     * @param n the name of the variable.
     */
    public GenericFloat32(String n) { 
	super(n); 
    }
    
        
    /** Dummy procedure
     */
    public boolean read(String datasetName, Object specialO)
	throws NoSuchVariableException, IOException, EOFException {
        setRead(true);
        return false;
    }
}


