package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.serializable.ArangeSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.AxisSerializable;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AxisWidget extends Composite {
	String type;
	Label lo_label = new Label();
	Label lo_label_range = new Label();
	Label hi_label_range = new Label();
    ListBox lo_axis = new ListBox();
    ListBox hi_axis = new ListBox();
    FlexTable lo_layout = new FlexTable();
    FlexTable hi_layout = new FlexTable();
    FlexTable layout = new FlexTable();
    NumberFormat format = NumberFormat.getFormat("###.##");
    boolean range;
    public AxisWidget(AxisSerializable ax) {
    	    init(ax);
        	initWidget(layout); 	
    }
    public void init(AxisSerializable ax, boolean range) {
    	this.range = range;
    	initialize(ax);
    }
    public void init(AxisSerializable ax) {
    	this.range = false;
    	initialize(ax);
    }
    private void initialize(AxisSerializable ax) {
    	lo_axis.clear();
    	hi_axis.clear();
    	lo_axis.addChangeHandler(loAxisChangeHandler);
    	hi_axis.addChangeHandler(hiAxisChangeHandler);
    	String units = ax.getUnits();
    	if ( ax.getLabel() != null && !ax.getLabel().equals("") ) {
    		if ( units != null && !units.equals("") ) {
    			lo_label_range.setText("Start "+ax.getLabel()+":");
    			hi_label_range.setText("End "+ax.getLabel()+":");
    			lo_label.setText(ax.getLabel()+":");
    		} else {
    			lo_label_range.setText("Start "+ax.getLabel()+"("+units+"):");
    			hi_label_range.setText("End "+ax.getLabel()+"("+units+"):");
    			lo_label.setText(ax.getLabel()+"("+units+"):");
    		}
    	} else {
    		if ( units != null && !units.equals("") ) {
    			lo_label_range.setText("Start Z ("+units+"): ");
    			hi_label_range.setText("End Z ("+units+"): ");
    			lo_label.setText("Z ("+units+"): ");
    		} else {
    			lo_label_range.setText("Start Z:");
    			hi_label_range.setText("End Z:");
    			lo_label.setText("Z:");
    		}
    	}
    	
    	if ( ax.getNames() != null && ax.getNames().length > 0) {
    		this.type = ax.getType();
        	lo_axis.setName(type);
        	String[] names = ax.getNames();
        	String[] values = ax.getValues();
        	for (int i=0; i < names.length; i++) {
    			lo_axis.addItem(names[i], values[i]);
    			hi_axis.addItem(names[i], values[i]);
    		}
        	lo_axis.setSelectedIndex(0);
        	hi_axis.setSelectedIndex(names.length - 1);
    	} else {
    		this.type = ax.getType();
        	lo_axis.setName(type);
        	ArangeSerializable arange = ax.getArangeSerializable();
        	int size = Integer.valueOf(arange.getSize());
        	double start = Double.valueOf(arange.getStart());
        	double step = Double.valueOf(arange.getStep());
        	for ( int i=0; i < size; i++ ) {
        		double value = start + i*step;
        		String v = format.format(value);
        		lo_axis.addItem(v);
        		hi_axis.addItem(v);
        	}
        	lo_axis.setSelectedIndex(0);
        	hi_axis.setSelectedIndex(hi_axis.getItemCount() - 1);
    	}
    	load_layout();
    }
    private void load_layout() {
    	lo_layout.clear();
    	hi_layout.clear();
    	layout.clear();
    	if ( range ) {
    		lo_layout.setWidget(0, 0, lo_label_range);
    	    lo_layout.setWidget(0, 1, lo_axis);
    	    hi_layout.setWidget(0, 0, hi_label_range);
    	    hi_layout.setWidget(0, 1, hi_axis);
    	    layout.setWidget(0, 0, lo_layout);
    	    layout.setWidget(1, 0, hi_layout);
    	} else {
    	    lo_layout.setWidget(0, 0, lo_label);
    	    lo_layout.setWidget(0, 1, lo_axis);
    	    layout.setWidget(0, 0, lo_layout);
    	}
    }
    public AxisWidget() {
		initWidget(layout);
	}
	public int getSelectedIndex() {
    	return lo_axis.getSelectedIndex();
    }
    public String getValue(int i) {
    	return lo_axis.getValue(i);
    }

	public void addChangeHandler(ChangeHandler handler) {
		lo_axis.addChangeHandler(handler);	
		hi_axis.addChangeHandler(handler);
	}

	public void setEnabled(boolean b) {
		lo_axis.setEnabled(b);
	}

	public String getLo() {
		return lo_axis.getValue(lo_axis.getSelectedIndex());
	}
	public String getHi() {
		if ( range ) {
			return hi_axis.getValue(hi_axis.getSelectedIndex());
		} else {
			return getLo();
		}	
	}
	
	public String getType() {
		return this.type;
	}
	public void setLo(String lo) {
		for(int i=0; i < lo_axis.getItemCount(); i++) {
			String value = lo_axis.getValue(i);
			if ( lo.equals(value) ) {
				lo_axis.setSelectedIndex(i);
			}
		}
	    checkOrderLo();	
	}
	public void setHi(String hi) {
		for(int i=0; i < hi_axis.getItemCount(); i++) {
			String value = hi_axis.getValue(i);
			if ( hi.equals(value) ) {
				hi_axis.setSelectedIndex(i);
			}
		}
		checkOrderHi();
	}
	public void setRange(boolean b) {
		range = b;
		load_layout();
	}
	public boolean isRange() {
		return range;
	}
	public void checkOrderLo() {
		if ( hi_axis.getSelectedIndex() < lo_axis.getSelectedIndex() ) {
			if ( lo_axis.getSelectedIndex() < lo_axis.getItemCount() - 2) {
				hi_axis.setSelectedIndex(lo_axis.getSelectedIndex() + 1);
			} else {
		    	hi_axis.setSelectedIndex(lo_axis.getSelectedIndex());
			}
		}		
	}
	public void checkOrderHi() {
		if ( hi_axis.getSelectedIndex() < lo_axis.getSelectedIndex() ) {
			if ( hi_axis.getSelectedIndex() > 1 ) {
				lo_axis.setSelectedIndex(hi_axis.getSelectedIndex() - 1);
			} else {
			    lo_axis.setSelectedIndex(hi_axis.getSelectedIndex());
			}
		}
	}
	public ChangeHandler loAxisChangeHandler = new ChangeHandler() {
		@Override
		public void onChange(ChangeEvent arg0) {
			checkOrderLo();	
		}
	};
	public ChangeHandler hiAxisChangeHandler = new ChangeHandler() {
		@Override
		public void onChange(ChangeEvent arg0) {
			checkOrderHi();
		}
	};
}
