/**
 * 
 */
package gov.noaa.pmel.tmap.las.util;

import gov.noaa.pmel.tmap.las.client.VariableSerializable;

import org.jdom.Element;

/**
 * @author Roland Schweitzer
 *
 */
public class Variable extends Container implements VariableInterface {
    
    String dsid;
    String DSName;
    
    public Variable(Element variable, String dsid) {
        super(variable);
        this.dsid = dsid;
    }
    public Variable(Element variable, String dsid, String DSName) {
        super(variable);
        this.dsid = dsid;
        this.DSName = DSName;
    }
    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.VariableInterface#getXPath()
     */
    public String getXPath() {
        return "/lasdata/datasets/dataset@[ID='"+dsid+"']/variables/variable@[ID='"+getID()+"']";
    }
    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.VariableInterface#getDSID()
     */
    public String getDSID() {
        return dsid;
    }
    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.VariableInterface#setDSID(java.lang.String)
     */
    public void setDSID(String dsid) {
        this.dsid = dsid;
    }
    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.VariableInterface#getGridID()
     */
    public String getGridID() {
        return element.getChild("grid").getAttributeValue("IDREF");
    }
    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.VariableInterface#setGridID(java.lang.String)
     */
    public void setGridID(String gridID) {
        element.getChild("grid").setAttribute("IDREF",gridID);
    }
    
    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.VariableInterface#setURL(java.lang.String)
     */
    public void setURL(String var_url) {
        element.setAttribute("url", var_url);
    }
    
    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.VariableInterface#getUnits()
     */
    public String getUnits() {
    	String uni = element.getAttributeValue("units");
    	return uni;
    	//return element.getAttributeValue("units");
    }
    public VariableSerializable getVariableSerializable() {
    	VariableSerializable variableSerializable = new VariableSerializable();
    	variableSerializable.setName(getName());
    	variableSerializable.setID(getID());
    	variableSerializable.setDSID(getDSID());
    	variableSerializable.setDSName(getDSName());
    	variableSerializable.setAttributes(getAttributesAsMap());
    	variableSerializable.setProperties(getPropertiesAsMap());
    	return variableSerializable;
    }
	/**
	 * @return the dsid
	 */
	public String getDsid() {
		return dsid;
	}
	/**
	 * @return the dSName
	 */
	public String getDSName() {
		return DSName;
	}
	/**
	 * @param dsid the dsid to set
	 */
	public void setDsid(String dsid) {
		this.dsid = dsid;
	}
	/**
	 * @param name the dSName to set
	 */
	public void setDSName(String name) {
		DSName = name;
	}
}
