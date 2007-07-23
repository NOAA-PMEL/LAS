package org.iges.anagram;

import java.io.*;
import java.util.*;
import javax.servlet.http.*;
import dods.dap.*;
import dods.dap.Server.*;

/** A tool for accessing and analyzing data.<p>
 *
 *  This class encapsulates all operations that are specific to  
 *  the particular data format and/or access mechanism being served
 *  by the Anagram server.
 *  
 *  Thus, implementing the Anagram framework simply means
 *  implementing this class, plus possibly TempDataHandle 
 *  (see doAnalysis() and doUpload()).
 *  
 */
public abstract class Tool 
    extends AbstractModule {

    /** Creates usable handles for data objects specified by tags in the 
     *  configuration file.<p>
     *  If possible, the import method should skip  
     *  data objects that are already loaded, and whose attributes have not changed. <p>
     *
     *  This method does not need to be threadsafe.
     *
     *	@param setting The tag tree specifying the data objects. This will be 
     *  the tree rooted at the <data> subtag of the <catalog> tag in the 
     *  configuration file.
     *  The combination of tag names and attributes to be used for specifying 
     *  data objects is up to the implementor of this method. <p>
     */
    public abstract List doImport(Setting setting);


    /** Performs an analysis task.<p>
     *  If analysis is not supported, this method
     *  should throw an exception. <p>
     *
     * This method must be threadsafe.<p>
     * @param name the name of the analysis expression
     * @param request the <code>HttpServletRequest</code> object for the 
     *       current request
     * @param privilege the privilege for the current request
     *
     * @return A handle to the results of the analysis. TempDataHandle
     *  is an abstract interface. Thus, this method is responsible for
     *  supplying an object which implements TempDataHandle properly.
     *
     * @throws ModuleException If the analysis fails for any reason
     */
    public abstract TempDataHandle doAnalysis(String name,
                                              HttpServletRequest request,
					      Privilege privilege) 
	throws ModuleException;

    /** Checks if the specified analysis is allowed for this request.
     * @param name the name of the analysis expression
     * @param privilege the privilege for the current request
     * @return true if this analysis is allowed, false if not allowed.
     * @throws ModuleException if the analysis is invalid or not allowed.
     */
    public abstract boolean allowAnalysis(String name,
					  Privilege privilege)
        throws ModuleException;
 

    /** Accepts an uploaded data object.<p>
     *  If uploads are not supported, this method
     *  should throw an exception. <p>
     *
     * This method must be threadsafe.<p>
     *
     * @param input The stream of data to be stored

     * @return A handle to the uploaded data object. TempDataHandle
     *  is an abstract interface. Thus, this method is responsible for
     *  supplying an object which implements TempDataHandle properly.
     *
     * @throws ModuleException if the upload fails for any reason
     */
    public abstract TempDataHandle doUpload(String name,
					    InputStream input,
					    long size,
					    Privilege privilege)
	throws ModuleException;

    /** Brings the data handle provided up to date with respect to the 
     *  data source. <p>
     * 
     * This method must be threadsafe.<p>
     *
     * @throws ModuleException If the data source has become unusable.
     * @return True if the data handle was modified. 
     */
    public abstract boolean doUpdate(DataHandle data) 
	throws ModuleException;

    /** Provides an object representation of the DODS Data Descriptor Structure for 
     *  the specified data object.
     *
     * It is guaranteed that the calling thread 
     * will already have a non-exclusive lock on the <code>data</code> parameter before
     * this method is called. Other than that, this method must guarantee its own
     * thread-safety. <p>
     * @see Handle#getSynch
     *
     * @param data The data object to be accessed
     * @param ce The DODS constraint to be applied to the DDS. Null indicates that
     *  the DDS should not be constrained.
     * @return an object representing the DDS 
     * @throws ModuleException if the request fails for any reason
     */
    public abstract ServerDDS getDDS(DataHandle data, 
                                     String ce,
                                     Privilege privilege,
                                     boolean useCache)
	throws ModuleException;
	

    /** Provides an object representation of the DODS Data Attribute Structure for 
     *  the specified data object.
     *
     * It is guaranteed that the calling thread 
     * will already have a non-exclusive lock on the <code>data</code> parameter before
     * this method is called. Other than that, this method must guarantee its own
     * thread-safety. <p>
     *
     * @see Handle#getSynch
     *
     * @param data The data object to be accessed
     * @return an object representing the DAS 
     * @throws ModuleException if the request fails for any reason
     */
    public abstract DAS getDAS(DataHandle data, 
                               Privilege privilege,
                               boolean useCache)
	throws ModuleException;

    /** Writes the DODS Data Descriptor Structure for 
     *  the specified data object to the specified stream.
     *
     * It is guaranteed that the calling thread 
     * will already have a non-exclusive lock on the <code>data</code> parameter before
     * this method is called. Other than that, this method must guarantee its own
     * thread-safety. <p>
     *
     * This method has a default implementation, which creates a DDS object
     * using getDDS(), and serializes it to the stream. For optimal performance
     * it is recommended to override this default implementation with a faster
     * approach, such as streaming the DDS text directly from a cached disk 
     * file.
     *
     * @see Handle#getSynch
     *
     * @param data The data object to be accessed
     * @param ce The DODS constraint to be applied to the DDS. Null indicates that
     *  the DDS should not be constrained.
     *  @param out A stream to which to write the DDS 
     * @throws ModuleException if the request fails for any reason
     */
    public void writeDDS(DataHandle data, 
                         String ce,
                         Privilege privilege,
                         OutputStream out, 
                         boolean useCache)
	throws ModuleException {
	
	ServerDDS dds = getDDS(data, ce, privilege, useCache);
	dds.print(out);

    }

    /** Writes the DODS Data Attribute Structure for 
     *  the specified data object to the specified stream.
     *
     * It is guaranteed that the calling thread 
     * will already have a non-exclusive lock on the <code>data</code> parameter before
     * this method is called. Other than that, this method must guarantee its own
     * thread-safety. <p>
     *
     * This method has a default implementation, which creates a DAS object
     * using getDAS(), and serializes it to the stream. For optimal performance
     * it is recommended to override this default implementation with a faster
     * approach, such as streaming the DAS text directly from a cached disk 
     * file.
     * @see Handle#getSynch
     *
     * @param data The data object to be accessed
     *  @param out A stream to which to write the DAS 
     * @throws ModuleException if the request fails for any reason
     */
    public void writeDAS(DataHandle data,
                         Privilege privilege,
                         OutputStream out,
                         boolean useCache)
	throws ModuleException {

	DAS das = getDAS(data, privilege, useCache);
	das.print(out);

    }

    /** Writes customized THREDDS metadata for the dataset, in the
     *  form of an XML fragment, to the specified stream.
     *
     * It is guaranteed that the calling thread will already have a
     * non-exclusive lock on the <code>data</code> parameter before
     * this method is called. Other than that, this method must
     * guarantee its own thread-safety. <p>
     *
     * @see Handle#getSynch
     *
     * @param data The data object to be accessed
     *  @param out A stream to which to write the DAS 
     * @throws ModuleException if the request fails for any reason
     */
    public abstract void writeTHREDDSTag(DataHandle data,
                                         Privilege privilege, 
                                         OutputStream out,
                                         boolean useCache)
	throws ModuleException;

    /** Writes a customized summary of the dataset, in the form of an HTML fragment,  
     *  to the specified stream.
     *
     * It is guaranteed that the calling thread 
     * will already have a non-exclusive lock on the <code>data</code> parameter before
     * this method is called. Other than that, this method must guarantee its own
     * thread-safety. <p>
     * @see Handle#getSynch
     *
     * @param data The data object to be accessed
     *  @param out A stream to which to write the DAS 
     * @throws ModuleException if the request fails for any reason
     */
    public abstract void writeWebInfo(DataHandle data,
                                      Privilege privilege,
                                      OutputStream out,
                                      boolean useCache)
	throws ModuleException;
    
    /** Writes a subset of the specified data object to the specified stream,
     *  in DODS binary format.<p>
     *  
     * It is guaranteed that the calling thread 
     *
     * will already have a non-exclusive lock on the <code>data</code> parameter before
     * this method is called. Other than that, this method must guarantee its own
     * thread-safety. <p>
     *
     * @see Handle#getSynch
     *
     * @param data The data object to be accessed
     *  @param ce The DODS constraint expression specifying the subset to be sent
     *  @param out The stream to which to write the subset
     * @throws ModuleException if the request fails for any reason
     */
    public abstract void writeBinaryData(DataHandle data, 
					 String ce, 
					 Privilege privilege,
					 OutputStream out,
                                         boolean useCache)
	throws ModuleException;

    /** Writes a data subset to a stream as a text table.<p>
     *
     * It is guaranteed that the calling thread 
     * will already have a non-exclusive lock on the <code>data</code> parameter before
     * this method is called. Other than that, this method must guarantee its own
     * thread-safety. <p>
     * @see Handle#getSynch
     *
     * @param data The data object to be accessed
     *  @param ce The DODS constraint expression specifying the subset to be sent
     *  @param out The stream to which to write the subset
     * @throws ModuleException if the request fails for any reason
     */
    public abstract void writeASCIIData(DataHandle data, 
					String ce, 
					Privilege privilege,
                                        HttpServletRequest request,
					OutputStream out,
                                        boolean useCache)
	throws ModuleException;

    /** This function is called when server is being shut down */
    public abstract void destroy();

}
