/**
 * This software module was contributed by Tasmanian Partnership for
 * Advanced Computing (TPAC) and Insight4 Pty. Ltd. to the Live
 * Access Server project at the US the National Oceanic and Atmospheric
 * Administration (NOAA)in as-is condition. The LAS software is
 * provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that neither NOAA nor TPAC and
 * Insight4 Pty. Ltd. assume liability for any errors contained in
 * the code.  Although this software is released without conditions
 * or restrictions in its use, it is expected that appropriate credit
 * be given to its authors, to TPAC and Insight4 Pty. Ltd. and to NOAA
 * should the software be included by the recipient as an element in
 * other product development.
 **/
package au.org.tpac.las.wrapper.lib.data;

import au.org.tpac.wms.lib.WMSParser;
import org.w3c.dom.Node;

import java.util.Vector;

/**
 * @author Pauline Mak <pauline@insight4.com>
 */
abstract public class LasRangeInfo extends WMSParser
{
    protected int cursor;
    protected Vector possibleValues;


    /**
     * Whether this range contains discrete values or
     * a range of values that can be defined by start/end/step
     * @return
     */
    public boolean hasMultipleValues()
    {
        return (possibleValues.size() > 0);
    }

    /**
     * If values are discrete, this will add an additional value
     * to a list of possible values in this range.
     * @param value
     */
    abstract public void addPossibleValue(String value);

     /**
     * Retrieves the start
     * @return start value  of this range
     */
    abstract public Object getStart();

     /**
     * Retrieves the end value
     * @return end value of this range
     */
    abstract public Object getEnd();

    /**
     * Reset cursor to the start of the range
     */
    abstract public void gotoStart();

    /**
     * Set cursor to the next value and return this value.
     * @return the next value that the cursor will point to at the end of this call.
     */
    abstract public Object getNext();
}
