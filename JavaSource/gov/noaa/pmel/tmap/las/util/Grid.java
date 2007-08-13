/**
 * 
 */
package gov.noaa.pmel.tmap.las.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

/**
 * @author Roland Schweitzer
 *
 */
public class Grid extends Container implements Cloneable, GridInterface {
    
    public Grid(Element element) {
        super(element);
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.GridInterface#getAxes()
     */
    public ArrayList<Axis> getAxes() {
        List axesE = element.getChildren("axis");
        ArrayList<Axis> axes = new ArrayList<Axis>();
        for (Iterator axIt = axesE.iterator(); axIt.hasNext();) {
            Element axis = (Element) axIt.next();
            if ( !axis.getAttributeValue("type").equals("t")) {
               axes.add(new Axis(axis));
            }
        }
        return axes;
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.GridInterface#setAxes(java.util.ArrayList)
     */
    public void setAxes(ArrayList<Axis> axes) {
       

    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.GridInterface#hasX()
     */
    public boolean hasX() {
        List axes = element.getChildren("axis");
        for (Iterator axIt = axes.iterator(); axIt.hasNext();) {
            Element axis = (Element) axIt.next();
            if ( axis.getAttributeValue("type").equalsIgnoreCase("x") ) {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.GridInterface#hasY()
     */
    public boolean hasY() {
        List axes = element.getChildren("axis");
        for (Iterator axIt = axes.iterator(); axIt.hasNext();) {
            Element axis = (Element) axIt.next();
            if ( axis.getAttributeValue("type").equalsIgnoreCase("y") ) {
                return true;
            }
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.GridInterface#hasZ()
     */
    public boolean hasZ() {
        List axes = element.getChildren("axis");
        for (Iterator axIt = axes.iterator(); axIt.hasNext();) {
            Element axis = (Element) axIt.next();
            if ( axis.getAttributeValue("type").equalsIgnoreCase("z") ) {
                return true;
            }
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.GridInterface#hasT()
     */
    public boolean hasT() {
        List axes = element.getChildren("axis");
        for (Iterator axIt = axes.iterator(); axIt.hasNext();) {
            Element axis = (Element) axIt.next();
            if ( axis.getAttributeValue("type").equalsIgnoreCase("t") ) {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.GridInterface#getAxis(java.lang.String)
     */
    public Axis getAxis(String type) {
        List axes = element.getChildren("axis");
        for (Iterator axIt = axes.iterator(); axIt.hasNext();) {
            Element axis = (Element) axIt.next();
            if ( axis.getAttributeValue("type").equalsIgnoreCase(type) ) {
                return new Axis(axis);
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.GridInterface#getTime()
     */
    public TimeAxis getTime() {
        List axes = element.getChildren("axis");
        for (Iterator axIt = axes.iterator(); axIt.hasNext();) {
            Element axis = (Element) axIt.next();
            if ( axis.getAttributeValue("type").equalsIgnoreCase("t") ) {
                return new TimeAxis(axis);
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.GridInterface#setTime(gov.noaa.pmel.tmap.las.util.TimeAxis)
     */
    public void setTime(TimeAxis time) {
        Element timeE = time.getElement();
        Element remove = null;
        List axes = element.getChildren("axis");
        for (Iterator axIt = axes.iterator(); axIt.hasNext();) {
            Element axis = (Element) axIt.next();
            if ( axis.getAttributeValue("type").equalsIgnoreCase("t") ) {
                remove = axis;
            }
        }
        if ( remove != null ) {
           element.removeContent(remove);
        }
        element.addContent(timeE);
    }
    
    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.GridInterface#clone()
     */
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.GridInterface#hasAxis(java.lang.String)
     */
    public boolean hasAxis(String analysis_axis_type) {
        if ( analysis_axis_type.equalsIgnoreCase("x") ) {
            return hasX();
        } else if ( analysis_axis_type.equalsIgnoreCase("y") ) {
            return hasY();
        } else if ( analysis_axis_type.equalsIgnoreCase("z") ) {
            return hasZ();
        } else if ( analysis_axis_type.equalsIgnoreCase("t") ) {
            return hasT();
        } else {
          return false;
        }
    }

	public void ok() {
		Element status = new Element("status");
		Element error = new Element("error");
		status.setText("ok");
		error.setText("ok");
		element.addContent(status);
		element.addContent(error);
		
	}
}
