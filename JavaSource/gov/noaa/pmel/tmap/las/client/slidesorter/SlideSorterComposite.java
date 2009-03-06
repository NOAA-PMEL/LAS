package gov.noaa.pmel.tmap.las.client.slidesorter;

import gov.noaa.pmel.tmap.las.client.laswidget.AxisWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.DateTimeWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.LASDateWidget;
import gov.noaa.pmel.tmap.las.client.serializable.ArangeSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.AxisSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.TimeAxisSerializable;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
/**
 * 
 * This object holds the menus that control the specification of the orthogonal axis that belongs to the panel.  A SlideSorterOld
 * UI element might need two of these axes (one for dates and one for a xyz axis) to completely specify the choices for the orthogonal axes.
 * 
 * There are 3 different ways an orthogonal axis might be specified:
 * 1. As a time menu that is a list of name/value pairs.
 * 2. As a time axis that needs and LASDateWidget.
 * 3. As a X,Y or Z axis that is a list of name/value pairs.
 * 
 * Typically only one such selector is active, but in the general case of cross data set comparisons two might be needed.
 * @author rhs
 *
 */

public class SlideSorterComposite extends Composite {
	public DateTimeWidget dateWidget;
	public AxisWidget dateMenu;
	public AxisWidget xyzMenu;
	boolean hasDateWidget = false;
	boolean hasDateMenu = false;
	boolean hasXYZMenu = false;
	String renderString;
	String XYZLabel;
	String XYZType;
	public SlideSorterComposite (List<AxisSerializable> axes) {
		for (Iterator aIt = axes.iterator(); aIt.hasNext();) {
			AxisSerializable a = (AxisSerializable) aIt.next();

			if ( a instanceof TimeAxisSerializable ) {
				TimeAxisSerializable tAxis = (TimeAxisSerializable) a;
				if ( tAxis.getNames() != null && tAxis.getNames().length > 0 ) {
					String[] names = tAxis.getNames();
					String[] values = tAxis.getValues();
					dateMenu =  new AxisWidget("t", names, values);
					hasDateMenu = true;
				} else {	
					String hi = tAxis.getHi();
					String lo = tAxis.getLo();

					double interval = 0;
					if ( tAxis.isHourNeeded() ) {
						interval = tAxis.getMinuteInterval();
					}
					dateWidget = new DateTimeWidget(tAxis, false);
					renderString = tAxis.getRenderString();
					hasDateWidget = true;
				}
			} else {
				String type = a.getType();
				if ( a.getNames() != null ) {
					String[] names = a.getNames();
					String[] values = a.getValues();
					xyzMenu =  new AxisWidget(type, names, values);
				} else {	
					ArangeSerializable arange = a.getArangeSerializable();
					xyzMenu =  new AxisWidget(type, Double.valueOf(arange.getStart()), Double.valueOf(arange.getStep()), Integer.valueOf(arange.getSize()));
				}
				hasXYZMenu = true;
				String label = a.getLabel();
				if ( label != null && !label.equals("") ) {
					XYZLabel = label;
				} else {
					XYZLabel = type.toUpperCase();
				}
				XYZType = type;
			}
		}
	}
    public boolean hasDateWidget() {
    	return hasDateWidget;
    }
    public boolean hasDateMenu() {
    	return hasDateMenu;
    }
    public boolean hasXYZMenu() {
    	return hasXYZMenu;
    }
    public String getRenderString() {
    	return renderString;
    }
    public String getXYZLabel() {
    	return XYZLabel;
    }
    public String getXYZType() {
    	return XYZType;
    }
}
