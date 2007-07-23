package gov.noaa.pmel.tmap.ferret.server.dodstype;
import dods.dap.Server.*;
import dods.dap.*;
import java.io.*;


/** Implements the server-side version of String 
 * All datatypes that appear in DDS'es for Ferret datasets must have
 * server-side implementations, even if its just a shell.<p>
 * 
 * Modified from org.iges.grads.server.dap.GenericString <p>
 *
 * @author Yonghua Wei
 */
public class GenericString extends SDString {
    
    private static int rCount = 0;        
	
    /** Constructs a new <code>GenericString</code>. */
    public GenericString() { 
	super(); 
    }
    
    /**
     * Constructs a new <code>GenericString</code> with name <code>n</code>.
     * @param n the name of the variable.
     */
    public GenericString(String n) { 
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
