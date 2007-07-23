/**
 * 
 */
package gov.noaa.pmel.tmap.las.util;

import org.jdom.Element;

/**
 * @author Roland Schweitzer
 *
 */
public class Variable extends Container implements VariableInterface {
    
    Element element;
    String dsid;
    
    public Variable(Element variable, String dsid) {
        super(variable);
        this.dsid = dsid;
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
}
