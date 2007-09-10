/**
 * 
 */
package gov.noaa.pmel.tmap.las.jdom;

import gov.noaa.pmel.tmap.las.ui.state.OptionBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

/**
 * @author Roland Schweitzer
 *
 */
public class LASUIRequest extends LASDocument {

    /**
     * Construct a new LASUIRequest object and set the root element.
     * TODO will this break other references to the empty constructor?
     */
    public LASUIRequest() {
        super();
        setRootElement(new Element("lasRequest"));
    }
    /**
     * Sets the variable to be used in this request.
     * @param dsID the ID of the dataset that contains the variable to be added
     * @param varID the ID of the variable to add
     */
    public void addVariable(String dsID, String varID) {
        Element args = getRootElement().getChild("args");
        if ( args == null ) {
            args = new Element("args");
            getRootElement().addContent(args);
        }
        Element link = new Element("link");
        link.setAttribute("match", "/lasdata/datasets/dataset[@ID='"+dsID+"']/variables/variable[@ID='"+varID+"']");
        args.addContent(link);
    }
    
    /**
     * Get a list of all the Variables in the request as XPaths.
     * @return ArrayList of XPath Strings for the variables in this request.
     */
    public ArrayList<String> getVariables() {
        ArrayList<String> variables = new ArrayList<String>();
        List vars = getRootElement().getChild("args").getChildren("link");
        for (Iterator varIt = vars.iterator(); varIt.hasNext();) {
            Element var = (Element) varIt.next();
            variables.add(var.getAttributeValue("match"));
        }
        return variables;
    }
    /**
     * Get a list of all the variables IDs in this request.
     * @return ArrayList<String> with the IDs
     */
    public ArrayList<String> getVariableIDs() {
    	ArrayList<String> ids = new ArrayList<String>();
    	ArrayList<String> vars = getVariables();
    	for (Iterator varIt = vars.iterator(); varIt.hasNext();) {
			String varXPath = (String) varIt.next();
			int isrt = varXPath.indexOf("/variables/");
	        ids.add(varXPath.substring(isrt+11, varXPath.length()));
		}
    	return ids;
    }
    /**
     * Sets the region element of the LAS UI Request object.
     * @param region A HashMap of HashMaps; the points map contains point elements, the range map element contains range elements.
     */
    public void setRegion(HashMap<String, HashMap<String,String[]>> region) {
        Element regionE = new Element("region");
        HashMap<String, String[]> points = region.get("points");
        HashMap<String, String[]> intervals = region.get("intervals");
        for (Iterator ptIt = points.keySet().iterator(); ptIt.hasNext();) {
            String type = (String) ptIt.next();
            String[] v = points.get(type);
            Element point = new Element("point");
            point.setAttribute("type", type);
            point.setAttribute("v",v[0]);
            regionE.addContent(point);
        }
        for (Iterator rgIt=intervals.keySet().iterator(); rgIt.hasNext();) {
            String type = (String) rgIt.next();
            String[] v = intervals.get(type);
            Element range = new Element("range");
            range.setAttribute("type", type);
            range.setAttribute("low", v[0]);
            range.setAttribute("high", v[1]);
            regionE.addContent(range);
        }
        Element args = getRootElement().getChild("args");
        if ( args == null ) {
            args = new Element("args");
            getRootElement().addContent(args);
        }
        args.addContent(regionE);
    }
    /**
     * Sets the operation for this request.
     * @param ID The operation ID to be used
     */
    public void setOperation(String ID) {
        Element link = new Element("link");
        String operation_xpath = "/lasdata/operations/operation[@ID='"+ID+"']";
        link.setAttribute("match", operation_xpath);
        getRootElement().addContent(link);
    }
    
    /**
     * Sets the options for this request.
     * @param options An ArrayList<OptionBean> containing the options to be set.
     */
    public void setOptions(String property_group, ArrayList<OptionBean> options) {
        Element properties = getRootElement().getChild("properties");
        Element group;
        if ( properties != null ) {
            group = properties.getChild(property_group);
            if ( group == null ) {
                group = new Element(property_group);
                properties.addContent(group);
            }
        } else {
            properties = new Element("properties");
            group = new Element(property_group);
            properties.addContent(group);
            getRootElement().addContent(properties);
        }
        for (Iterator opIt = options.iterator(); opIt.hasNext();) {
            OptionBean option = (OptionBean) opIt.next();
            String name = option.getWidget_name();
            String value = option.getValue();
            if ( value != null ) {
                Element n = new Element(name);
                n.setText(value);
                group.addContent(n);
            }
        }
    }
    public String getOperationXPath() {
        return this.getRootElement().getChild("link").getAttributeValue("match");
    }
    
    public String getSessionID() {
        String sessionID = this.getRootElement().getAttributeValue("SessionID");
        if (sessionID == null) {
            return "";
        } else {
            return sessionID;
        }
    }
    public boolean isOneToOne() {
        int data = 0;
        int region = 0;
        List args = this.getRootElement().getChildren("args");
        for (Iterator argIt = args.iterator(); argIt.hasNext();) {
            Element arg = (Element) argIt.next();
            if (arg.getName().equals("link")) {
                data++;
            }
            else if (arg.getName().equals("region")) {
                region++;
            }
        }
        return data == region;
    }

    public Element getProperties() {
       return this.getRootElement().getChild("properties");
    }
    public String getProperty(String group_name, String property_name) {
        String value = "";
        Element properties = this.getRootElement().getChild("properties");
        Element group = null;
        if ( properties != null ) {
           group = properties.getChild(group_name);
        } 
        if ( group != null) {
            value = group.getChildTextTrim(property_name);
        }
        if ( value != null ) {
           return value;
        } else {
            return "";
        }
    }
    public void setProperty(String group_name, String name, String value) {
            Element properties = this.getRootElement().getChild("properties");

            if(properties == null){
                properties = new Element("properties");
                this.getRootElement().addContent(properties);
            }

            Element group = null;
            if ( properties != null ) {
               group = properties.getChild(group_name);
            }
            if ( group == null ) {
                group = new Element(group_name);
                properties.addContent(group);
            }
            Element nameE = new Element(name);
            nameE.setText(value);
            group.addContent(nameE);
    }

    /**
     * @return
     */
    public boolean isAnalysisRequest() {
        List args = getRootElement().getChild("args").getChildren();
        for (Iterator argsIt = args.iterator(); argsIt.hasNext();) {
            Element arg = (Element) argsIt.next();
            Element analysis = arg.getChild("analysis");
            if ( analysis != null ) {
                return true;
            }
        }
        return false;
    }


    /**
     * Sets a range element of the LAS UI Request object.
     * @param type the axis to set range
     * @param lo   low end of the range
     * @param hi   high end of the range
     */
    public void setRange(String type, String lo, String hi){
        Element regionE =  getRootElement().getChild("args").getChild("region");
        if( regionE != null){
            List ranges = regionE.getChildren("range");

            for (Iterator rngIt=ranges.iterator(); rngIt.hasNext();){
                Element range = (Element) rngIt.next();
                if (range.getAttributeValue("type").equals(type)){
                    range.setAttribute("low", lo);
                    range.setAttribute("high",hi);
                }
                //regionE.addContent(range);
            }
        }
    }

    /**
     * Change a range to a point on an axis
     * @param type the axis to set range
     * @param val  value of the point
     */
    public void rangeToPoint(String type, String val){
        Element regionE =  getRootElement().getChild("args").getChild("region");

        //find the range and remove it
        if( regionE != null){
            List ranges = regionE.getChildren("range");
            Element theRange=null;

            for (Iterator rngIt=ranges.iterator(); rngIt.hasNext();){
                Element range = (Element) rngIt.next();
                if (range.getAttributeValue("type").equals(type)){
                    //range.setAttribute("low", lo);
                    //range.setAttribute("high",hi);
                    theRange = range;
                }
                //regionE.addContent(range);
            }
   
            if(theRange != null){
            regionE.removeContent(theRange);

            //add point for this type
            Element point = new Element("point");
            point.setAttribute("type", type);
            point.setAttribute("v",val);
            regionE.addContent(point);
            }
        }
    }

    /**
     * Get name of the dataset used by this request
     */
    public String getDatasetName() {
        String dataLink = this.getRootElement().getChild("args").getChild("link").getAttributeValue("match");
        int isrt = dataLink.indexOf("/lasdata/datasets/");
        int iend = dataLink.indexOf("/variables/");
        return dataLink.substring(isrt+18, iend);
    }
    /**
     * Get a list of all the Data Set IDs in this request.
     * @return ids an ArrayList<String> of the data sets in this request.
     */
    public ArrayList<String> getDatasetIDs() {
        ArrayList<String> ids = new ArrayList<String>();
    	ArrayList<String> vars = getVariables();
    	for (Iterator varIt = vars.iterator(); varIt.hasNext();) {
			String varXPath = (String) varIt.next();
			int isrt = varXPath.indexOf("/lasdata/datasets/");
	        int iend = varXPath.indexOf("/variables/");			
	        ids.add(varXPath.substring(isrt+18, iend));
		}
    	return ids;
    }
    /**
     * Get name of the variable used by this request
     */
    public String getVarName() {
        String dataLink = this.getRootElement().getChild("args").getChild("link").getAttributeValue("match");
        int isrt = dataLink.indexOf("/variables/");
        return dataLink.substring(isrt+11, dataLink.length());
    }

   /**
     * Changes the operation for this request.
     * @param ID the new operation (ID)
     */
    public void changeOperation(String ID) {
        Element opLink = this.getRootElement().getChild("link");
        if(opLink != null){
            String operation_xpath = "/lasdata/operations/operation[@ID='"+ID+"']";
            opLink.setAttribute("match", operation_xpath);
        }
    }

    /**
     * Changes the view for this request.
     * @param v the new view
     */
    public void changeView(String v) {
        Element view = this.getRootElement().getChild("properties").getChild("ferret").getChild("view");
        if(view != null){
            view.setText(v);
        }
    }

}
