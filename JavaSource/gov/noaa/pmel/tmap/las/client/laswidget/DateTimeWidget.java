package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.serializable.TimeAxisSerializable;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DateTimeWidget extends Composite {
	DateTimeFormat longForm = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
	DateTimeFormat shortForm = DateTimeFormat.getFormat("yyyy-MM-dd");
	DateTimeFormat shortFerretForm = DateTimeFormat.getFormat("dd-MMM-yyyy");
	DateTimeFormat longFerretForm = DateTimeFormat.getFormat("dd-MMM-yyyy HH:mm:ss");
	Date lo;
	Date hi;

	ListBox lo_year = new ListBox();
	ListBox lo_month = new ListBox();
	ListBox lo_day = new ListBox();
	ListBox lo_hour = new ListBox();

	ListBox hi_year = new ListBox();
	ListBox hi_month = new ListBox();
	ListBox hi_day = new ListBox();
	ListBox hi_hour = new ListBox();

	FlowPanel lo_flow = new FlowPanel();
	FlowPanel hi_flow = new FlowPanel();

	VerticalPanel dateTimeWidget = new VerticalPanel();

	NumberFormat four_digits = NumberFormat.getFormat("####");
	NumberFormat two_digits = NumberFormat.getFormat("##");

	boolean hasYear = false;
	boolean hasMonth = false;
	boolean hasDay = false;
	boolean hasHour = false;

	boolean climatology;
	boolean range;
	
	boolean isMenu = false;

	private static final String[] MONTHS = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

	//public DateTimeWidget(String lo_date, String hi_date, int deltaMinutes, int minuteOffset, String render, boolean range, boolean climatology) {
	public DateTimeWidget(TimeAxisSerializable tAxis, boolean range) {
        init(tAxis, range);
		initWidget(dateTimeWidget);
	}
	public DateTimeWidget() {
		initWidget(dateTimeWidget);
	}
	public void init(TimeAxisSerializable tAxis, boolean range) {
		lo_year.clear();
		lo_month.clear();
		lo_day.clear();
		lo_hour.clear();
		hi_year.clear();
		hi_month.clear();
		hi_day.clear();
		hi_hour.clear();
		this.range = range;
		if (tAxis.getNames() != null && tAxis.getNames().length > 0 ) {
			isMenu = true;
			String[] names = tAxis.getNames();
			String[] values = tAxis.getValues();
			for(int i = 0; i < names.length; i++ ) {
				lo_day.addItem(names[i], values[i]);
				hi_day.addItem(names[i], values[i]);
				lo_flow.add(lo_day);
				if ( range ) {
					hi_flow.add(hi_day);
				}
			}

			hasYear = false;
			hasMonth = false;
			hasDay = true;
			hasHour = false;
		} else {
			String lo_date = tAxis.getLo();
			String hi_date = tAxis.getHi();
			String render = tAxis.getRenderString();
			this.climatology = tAxis.isClimatology();

			try {
				lo = longForm.parse(lo_date);
			} catch (IllegalArgumentException e) {
				try {
					lo = shortForm.parse(lo_date);
				} catch (IllegalArgumentException e1) {
					Window.alert("Date parsing failed for "+lo_date);
				}
			}

			try {
				hi = longForm.parse(hi_date);
			} catch (IllegalArgumentException e) {
				try {
					hi = shortForm.parse(hi_date);
				} catch (IllegalArgumentException e1) {
					Window.alert("Date parsing failed for "+hi_date);
				}
			}

			if ( range ) {
				years(lo, hi, lo_year);
				years(lo, hi, hi_year);
				lo_months(lo, lo_month);
				hi_months(hi, hi_month);
				lo_days(lo, lo_day);
				hi_days(hi, hi_day);
			} else {
				years(lo, hi, lo_year);
				lo_months(lo, lo_month);
				lo_days(lo, lo_day);
			}

			if ( render.contains("Y") || render.contains("y") ) {
				if ( range ) {
					lo_flow.add(lo_year);
					hi_flow.add(hi_year);
				} else {
					lo_flow.add(lo_year);
				}
				hasYear = true;
			}

			if ( render.contains("M") || render.contains("m") ) {
				if ( range ) {
					lo_flow.add(lo_month);
					hi_flow.add(hi_month);
				} else {
					lo_flow.add(lo_month);
				}
				hasMonth = true;
			}

			if ( render.contains("D") || render.contains("d") ) {
				if ( range ) {
					lo_flow.add(lo_day);
					hi_flow.add(hi_day);
				} else {
					lo_flow.add(lo_day);
				}
				hasDay = true;
			}


		}
		if ( range ) {
			dateTimeWidget.add(lo_flow);
			dateTimeWidget.add(hi_flow);
		} else {
			dateTimeWidget.add(lo_flow);
		}
	}
	private void lo_days(Date d, ListBox day) {
		int start = lo.getDay();
		int end = maxDays(d);
		for ( int i = start; i < end; i++) {
			day.addItem(two_digits.format(i), two_digits.format(i));
		}

	}
	private void hi_days(Date d, ListBox day) {
		int end = d.getDay();
		for ( int i = 1; i <= end; i++ ) {
			day.addItem(two_digits.format(i), two_digits.format(i));
		}
	}
	private int maxDays(Date d) {
		int m = d.getMonth();
		if ( m == 0 || m == 2 || m == 4 || m == 6 || m == 7 || m == 9 || m == 11) {
			return 31;
		} else if ( m == 1 ) {
			int year = d.getYear();
			int max = 28; 
			if ( (year%4) == 0 ) {
				max = 29;
				if ( (year%100) == 0 ) {
					max = 28;
					if ( (year%400) == 0 ) {
						max = 29;
					}
				}
			}
			return max;
		} else {
			return 30;
		}
	}
	private void lo_months(Date d, ListBox month) {
		int start = lo.getMonth();
		for (int m = start; m < 12; m++ ) {
			month.addItem(MONTHS[m], MONTHS[m]);
		}
	}
	private void hi_months(Date d, ListBox month) {
		int end = hi.getMonth();
		for ( int m = 0; m < end; m++ ) {
			month.addItem(MONTHS[m], MONTHS[m]);
		}
	}
	private void years(Date lo, Date hi, ListBox year) {

		int start = lo.getYear() + 1900;
		int end = hi.getYear() + 1900;
		for ( int y = start; y <= end; y++ ) {
			year.addItem(four_digits.format(y), four_digits.format(y));
		}
	}
	public void setEnabled(boolean b) {
		lo_year.setEnabled(b);
		lo_month.setEnabled(b);
		lo_day.setEnabled(b);
		lo_hour.setEnabled(b);

		hi_year.setEnabled(b);
		hi_month.setEnabled(b);
		hi_day.setEnabled(b);
		hi_hour.setEnabled(b);
	}

	public void setVisible(boolean b) {
		lo_year.setVisible(b);
		lo_month.setVisible(b);
		lo_day.setVisible(b);
		lo_hour.setVisible(b);

		hi_year.setVisible(b);
		hi_month.setVisible(b);
		hi_day.setVisible(b);
		hi_hour.setVisible(b);
	}
	public String getFerretDateLo() {
		StringBuffer date = new StringBuffer();
		if ( hasDay ) {
			date.append(lo_day.getValue(lo_day.getSelectedIndex()));
		} else {
			if ( hasMonth ) {
				date.append("15");
			}
		}
		if ( hasMonth ) {
			date.append("-"+lo_month.getValue(lo_month.getSelectedIndex()));
		}

		if ( climatology ) {
			date.append("-0001");   
		} else {
			if ( hasYear ) {
				date.append("-"+lo_year.getValue(lo_year.getSelectedIndex()));
			}
		}

		return date.toString();
	}

	public String getFerretDateHi() {
		StringBuffer date = new StringBuffer();
		if ( range ) {
			if ( hasDay ) {
				date.append(hi_day.getValue(hi_day.getSelectedIndex()));
			} else {
				if ( hasMonth ) {
					date.append("15");
				}
			}
			if ( hasMonth ) {
				date.append("-"+hi_month.getValue(hi_month.getSelectedIndex()));
			}
			if ( climatology ) {
				date.append("0001");              
			} else {
				if ( hasYear ) {
					date.append("-"+hi_year.getValue(hi_year.getSelectedIndex()));
				}
			}
            return date.toString();
		} else {
			return getFerretDateLo();
		}
	}
	public void addChangeListener(ChangeListener change) {
		lo_year.addChangeListener(change);
		lo_month.addChangeListener(change);
		lo_day.addChangeListener(change);
		lo_hour.addChangeListener(change);

		hi_year.addChangeListener(change);
		hi_month.addChangeListener(change);
		hi_day.addChangeListener(change);
		hi_hour.addChangeListener(change);
	}
	public void setLo(String tlo) {
		// TODO what about hours????
		if ( isMenu ) {
			for(int d = 0; d < lo_day.getItemCount(); d++) {
				String value = lo_day.getValue(d);
				if ( value.equals(tlo) ) {
					lo_day.setSelectedIndex(d);
				}
			}
		} else {
			Date lo;
			if ( tlo.length() == 11 ) {
				lo = shortFerretForm.parse(tlo);
			} else {
				lo = longFerretForm.parse(tlo);
			}
			if ( hasDay ) {
				String day = String.valueOf(lo.getDay());
				for(int d = 0; d < lo_day.getItemCount(); d++) {
					String value = lo_day.getValue(d);
					if ( value.equals(day) ) {
						lo_day.setSelectedIndex(d);
					}
				}
			} 
			if ( hasMonth ) {
				String month = MONTHS[lo.getMonth()];
				for ( int m = 0; m < lo_month.getItemCount(); m++ ) {
					String value = lo_month.getValue(m);
					if ( value.equals(month) ) {
						lo_month.setSelectedIndex(m);
					}
				}

			}
			if ( hasYear ) {
				String year = String.valueOf(lo.getYear() + 1900);
				for ( int y = 0; y < lo_year.getItemCount(); y++ ) {
					String value = lo_year.getValue(y);
					if ( value.equals(year) ) {
						lo_year.setSelectedIndex(y);
					}
				}
			}

		}
	}
}
