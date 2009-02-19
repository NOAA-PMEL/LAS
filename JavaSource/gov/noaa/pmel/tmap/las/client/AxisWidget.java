package gov.noaa.pmel.tmap.las.client;

import java.util.Iterator;
import java.util.Map;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;

public class AxisWidget extends Composite {
	String type;
	String label;
    ListBox axis;
    NumberFormat format = NumberFormat.getFormat("###.##");
    public AxisWidget(AxisSerializable ax) {
    	if ( ax.getNames() != null && ax.getNames().length > 0) {
    		this.type = ax.getType();
        	axis = new ListBox();
        	axis.setName(type);
        	String[] names = ax.getNames();
        	String[] values = ax.getValues();
        	for (int i=0; i < names.length; i++) {
    			axis.addItem(names[i], values[i]);
    		}
        	axis.setSelectedIndex(0);
        	initWidget(axis);
    	} else {
    		this.type = ax.getType();
        	axis = new ListBox();
        	axis.setName(type);
        	ArangeSerializable arange = ax.getArangeSerializable();
        	int size = Integer.valueOf(arange.getSize());
        	double start = Double.valueOf(arange.getStart());
        	double step = Double.valueOf(arange.getStep());
        	for ( int i=0; i < size; i++ ) {
        		double value = start + i*step;
        		String v = format.format(value);
        		axis.addItem(v);
        	}
        	axis.setSelectedIndex(0);
        	initWidget(axis);
    	}
    }
    public AxisWidget(String type, double start, double step, int size) {
    	this.type = type;
    	axis = new ListBox();
    	axis.setName(type);
    	for ( int i=0; i < size; i++ ) {
    		double value = start + i*step;
    		String v = format.format(value);
    		axis.addItem(v);
    	}
    	axis.setSelectedIndex(0);
    	initWidget(axis);
    }
    
    public AxisWidget(String type, String[] names, String[] values) {
    	this.type = type;
    	axis = new ListBox();
    	axis.setName(type);
    	for (int i=0; i < names.length; i++) {
			axis.addItem(names[i], values[i]);
		}
    	axis.setSelectedIndex(0);
    	initWidget(axis);
    }
    public int getSelectedIndex() {
    	return axis.getSelectedIndex();
    }
    public String getValue(int i) {
    	return axis.getValue(i);
    }

	public void addChangeListener(ChangeListener listener) {
		axis.addChangeListener(listener);	
	}

	public void setEnabled(boolean b) {
		axis.setEnabled(b);
	}

	public String getSelectedValue() {
		return getValue(getSelectedIndex());
	}
}
