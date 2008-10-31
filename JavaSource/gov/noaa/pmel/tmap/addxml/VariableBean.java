package gov.noaa.pmel.tmap.addxml;

import java.util.HashMap;
import java.util.Iterator;

import org.jdom.Element;

/**
 * <p>Title: addXML</p>
 *
 * <p>Description: Reads local or OPeNDAP netCDF files and generates LAS XML
 * configuration information.</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: NOAA/PMEL/TMAP</p>
 *
 * @author RHS
 * @version 1.0
 */
public class VariableBean extends LasBean {
  private String name;
  private String url;
  private String units;
  private GridBean grid;
  private HashMap<String, HashMap<String, String>> properties = new HashMap<String, HashMap<String, String>>();
  public VariableBean() {
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setUnits(String units) {
    this.units = units;
  }

  public void setGrid(GridBean grid) {

    this.grid = grid;
  }

  public String getName() {
    return name;
  }

  public String getUrl() {
    return url;
  }

  public String getUnits() {
    return units;
  }

  public GridBean getGrid() {

    return grid;
  }

  public void setProperty(String group, String name, String value) {
	  HashMap<String, String> groupMap = properties.get(group);
	  if ( groupMap == null ) {
		  groupMap = new HashMap<String, String>();
	  }
	  groupMap.put(name, value);
	  properties.put(group, groupMap);
  }
  
  public Element toXml() {
    Element variable = new Element(this.getElement());
    variable.setAttribute("name", name);
    variable.setAttribute("units", units);
    if ( url != null && url != "" ) {
      variable.setAttribute("url", url);
    }
    Element link = new Element("link");
    link.setAttribute("match", "/lasdata/grids/"+grid.getElement());
    variable.addContent(link);
    if ( properties.size() > 0 ) {
    	Element propertiesE = new Element("properties");
    	for (Iterator groupsIt = properties.keySet().iterator(); groupsIt.hasNext();) {
			String group = (String) groupsIt.next();
			Element groupE = new Element(group);
			HashMap<String, String> groupMap = properties.get(group);
			for (Iterator propIt = groupMap.keySet().iterator(); propIt.hasNext();) {
				String name = (String) propIt.next();
				String value = groupMap.get(name);
				Element nameE = new Element(name);
				nameE.setText(value);
				groupE.addContent(nameE);
			}
			propertiesE.addContent(groupE);
		}
    	variable.addContent(propertiesE);
    }
    return variable;
  }
  
  

}
