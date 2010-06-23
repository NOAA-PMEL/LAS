/**
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development. 
 */
package gov.noaa.pmel.tmap.las.client.serializable;


import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author rhs
 *
 */
public class VariableSerializable extends Serializable implements IsSerializable {
   
    String DSID;
    String DSName;
    GridSerializable grid;
    List<String> components;
	boolean vector;
	
	
	/**
	 * 
	 */
	public VariableSerializable() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * @return the dSID
	 */
	public String getDSID() {
		return DSID;
	}
	/**
	 * @param dsid the dSID to set
	 */
	public void setDSID(String dsid) {
		DSID = dsid;
	}
	/**
	 * @return the dSName
	 */
	public String getDSName() {
		return DSName;
	}
	/**
	 * @param name the dSName to set
	 */
	public void setDSName(String name) {
		DSName = name;
	}
	/**
	 * @return the grid
	 */
	public GridSerializable getGrid() {
		return grid;
	}
	/**
	 * @param grid the grid to set
	 */
	public void setGrid(GridSerializable grid) {
		this.grid = grid;
	}
	
	public String getIntervals() {
		return attributes.get("intervals");
	}
	
	public List<String> getComponents() {
		return components;
	}
	public void setComponents(List<String> components) {
		this.components = components;
	}
	public boolean isVector() {
		return vector;
	}
	public void setVector(boolean vector) {
		this.vector = vector;
	}
}
