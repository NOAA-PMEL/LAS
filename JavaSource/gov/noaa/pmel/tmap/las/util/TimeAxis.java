/**
 * 
 */
package gov.noaa.pmel.tmap.las.util;

import org.jdom.Element;

/**
 * @author Roland Schweitzer
 *
 */
public class TimeAxis extends Axis implements TimeAxisInterface {
    
    
    public TimeAxis(Element element) {
        super(element);
    }
    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.TimeAxisInterface#isDayNeeded()
     */
    public boolean isDayNeeded() {
        return Boolean.valueOf(element.getAttributeValue("dayNeeded")).booleanValue();
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.TimeAxisInterface#setDayNeeded(boolean)
     */
    public void setDayNeeded(boolean dayNeeded) {
        element.setAttribute("dayNeeded", String.valueOf(dayNeeded));
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.TimeAxisInterface#getDisplay_type()
     */
    public String getDisplay_type() {
        return element.getAttributeValue("display_type");
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.TimeAxisInterface#setDisplay_type(java.lang.String)
     */
    public void setDisplay_type(String display_type) {
        element.setAttribute("display_type", display_type);
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.TimeAxisInterface#getHi()
     */
    public String getHi() {
        return element.getAttributeValue("hi");
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.TimeAxisInterface#setHi(java.lang.String)
     */
    public void setHi(String hi) {
        element.setAttribute("hi", hi);
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.TimeAxisInterface#isHourNeeded()
     */
    public boolean isHourNeeded() {
        return Boolean.valueOf(element.getAttributeValue("hourNeeded")).booleanValue();
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.TimeAxisInterface#setHourNeeded(boolean)
     */
    public void setHourNeeded(boolean hourNeeded) {
        element.setAttribute("hourNeeded", String.valueOf(hourNeeded));
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.TimeAxisInterface#getLo()
     */
    public String getLo() {
        return element.getAttributeValue("lo");
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.TimeAxisInterface#setLo(java.lang.String)
     */
    public void setLo(String lo) {
        element.setAttribute("lo", lo);
    }
    
    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.TimeAxisInterface#getMinuteInterval()
     */
    public double getMinuteInterval() {
        return Double.valueOf(element.getAttributeValue("minuteInterval")).doubleValue();
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.TimeAxisInterface#setMinuteInterval(double)
     */
    public void setMinuteInterval(double minuteInterval) {
        element.setAttribute("minuteInterval", String.valueOf(minuteInterval));
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.TimeAxisInterface#isMonthNeeded()
     */
    public boolean isMonthNeeded() {
        return Boolean.valueOf(element.getAttributeValue("monthNeeded")).booleanValue();
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.TimeAxisInterface#setMonthNeeded(boolean)
     */
    public void setMonthNeeded(boolean monthNeeded) {
        element.setAttribute("monthNeeded", String.valueOf(monthNeeded));
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.TimeAxisInterface#isYearNeeded()
     */
    public boolean isYearNeeded() {
        return Boolean.valueOf(element.getAttributeValue("yearNeeded")).booleanValue();
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.TimeAxisInterface#setYearNeeded(boolean)
     */
    public void setYearNeeded(boolean yearNeeded) {
        element.setAttribute("yearNeeded", String.valueOf(yearNeeded));
    }
    
    
}
