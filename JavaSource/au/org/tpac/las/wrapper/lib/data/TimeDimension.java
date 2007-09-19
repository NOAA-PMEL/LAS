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

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.DateTimeZone;

import java.util.Vector;
import java.util.HashMap;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * <p></p>
 * <p>Created by IntelliJ IDEA.
 * Date: 6/04/2006
 * Time: 15:33:51</p>
 * @author Pauline Mak (pauline@insight4.com, Insight4 Pty. Ltd.)
 */
public class TimeDimension extends LasRangeInfo
{
    public static HashMap resolutions;


    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private SimpleDateFormat dateFormatWord = new SimpleDateFormat("yyyy-MMM-dd");
    private SimpleDateFormat dateTimeFormatWord = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");

    private SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
    private Pattern wordMonths = Pattern.compile(".*[a-zA-Z]{3}");

    DateTimeFormatter longfmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(DateTimeZone.UTC);

    protected int size;
    protected Date start;
    protected int step;
    protected int unit;

    public TimeDimension()
    {
        makeTable();
    }

    public TimeDimension(Node node, String unitStr)
    {
        makeTable();
        unit = ((Integer)resolutions.get(unitStr)).intValue();
        parse(node);
    }

    public void setUnitString(String _unit)
    {
        String lowerUnit = _unit.toLowerCase();

        if(resolutions.containsKey(lowerUnit))
        {
            unit = ((Integer)resolutions.get(lowerUnit)).intValue();
        }
        else
        {
            //Why are we doing this?
            //Because some of the units are named: HOURS since 1901-01-15 00:00:00
            String[] splits = lowerUnit.split(" ");

            for(int i = 0; i < splits.length; i++)
            {
                if(resolutions.containsKey(splits[i]))
                {
                    unit = ((Integer)resolutions.get(splits[i])).intValue();
                }
            }
        }


    }

    public int getUnit()
    {
        return this.unit;
    }

    private void makeTable()
    {
        possibleValues = new Vector();
        resolutions = new HashMap();

        resolutions.put("hour", new Integer(Calendar.HOUR_OF_DAY));
        resolutions.put("hours", new Integer(Calendar.HOUR_OF_DAY));
        resolutions.put("minute", new Integer(Calendar.MINUTE));
        resolutions.put("second", new Integer(Calendar.SECOND));

        resolutions.put("day", new Integer(Calendar.DAY_OF_YEAR));
        resolutions.put("month", new Integer(Calendar.MONTH));
        resolutions.put("months", new Integer(Calendar.MONTH));
        resolutions.put("year", new Integer(Calendar.YEAR));

    }

    protected void setXMLElementSelf(Document doc)
    {
        //do nothing for now       
    }

    public void setSize(int _size)
    {
        size = _size;
    }


    public int getSize()
    {
        return size;
    }

    public Object getStart()
    {
        if(possibleValues.size() > 0)
        {
            return possibleValues.elementAt(0);
        }
        else
        {
            return (start);
        }
    }

    public Object getEnd()
    {
        if(possibleValues.size() > 0)
        {
            return possibleValues.get(possibleValues.size() - 1);
        }
        else
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(start);
            cal.add(unit, step * (size));
            return cal.getTime();
        }
    }

    public int getStep()
    {
        return step;
    }

    public void gotoStart()
    {
        cursor = 0;
    }


    public Object getNext()
    {
        if(possibleValues.size() > 0)
        {

            if(cursor >= possibleValues.size())
                return null;
            else
            {
                Object result = possibleValues.elementAt(cursor);
                cursor++;
                return result;
            }
        }
        else
        {
            if(cursor < size)
            {
                Calendar cal = Calendar.getInstance();
                cal.setTime(start);
                cal.add(unit, cursor * step);

                cursor++;
                return cal.getTime();
            }
        }
        return null;
    }


    public Date parseTime(String time)
    {
        Date result = null;


        Matcher matcher = wordMonths.matcher(time);

        try
        {
            if(matcher.find())
            {
                if(time.indexOf(":") >= 0)
                {
                    result = dateTimeFormatWord.parse(time);
                }
                else
                {
                    result = dateFormatWord.parse(time);
                }
            }
            else
            {
                if(time.indexOf(":") >= 0)
                    result = dateTimeFormat.parse(time);
                else
                    result = dateFormat.parse(time);
            }

        }
        catch(ParseException ex)
        {

        }

        return result;
    }

     public void addPossibleValue(String value)
    {
        Date date = parseTime(value);
        if(date == null)
        {
            Matcher matcher = wordMonths.matcher(value);
            if(matcher.find() && (value.length() == 3))
            {
                value = "15-" + value;
            }
            possibleValues.add(value);
        }
        else
        {
            possibleValues.add(date);
        }
    }

    protected void saveAttributeData(String attributeName, String attributeValue)
    {
        if(attributeName.equalsIgnoreCase("size"))
            size = Integer.parseInt(attributeValue);
        else if(attributeName.equalsIgnoreCase("start"))
            start = parseTime(attributeValue);
        else if(attributeName.equalsIgnoreCase("step"))
            step = Integer.parseInt(attributeValue);
    }

    protected void saveNodeData(String nodeName, String nodeValue)
    {

    }

    protected boolean processChildren(String nodeName, Node childNode)
    {
        return false;
    }


}

