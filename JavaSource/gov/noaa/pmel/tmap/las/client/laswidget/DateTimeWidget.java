package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.serializable.TimeAxisSerializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;

public class DateTimeWidget extends Composite {
	
	private static final DateTimeFormat longForm = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
	private static final DateTimeFormat mediumForm = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm");
	private static final DateTimeFormat shortForm = DateTimeFormat.getFormat("yyyy-MM-dd");
	private static final DateTimeFormat shortFerretForm = DateTimeFormat.getFormat("dd-MMM-yyyy");
	private static final DateTimeFormat mediumFerretForm = DateTimeFormat.getFormat("dd-MMM-yyyy HH:mm"); 
	private static final DateTimeFormat longFerretForm = DateTimeFormat.getFormat("dd-MMM-yyyy HH:mm:ss"); 
	
	Date lo;
	Date hi;
    
	Label dt_label = new Label("Date/Time: ");
	Label d_label = new Label("Date: ");
	
	Label dt_label_lo_range = new Label("Start date/Time: ");
	Label d_label_lo_range = new Label("Start date: ");
	
	Label dt_label_hi_range = new Label("End date/Time: ");
	Label d_label_hi_range = new Label("End date: ");
	
	ListBox lo_year = new ListBox();
	ListBox lo_month = new ListBox();
	ListBox lo_day = new ListBox();
	HourListBox lo_hour = new HourListBox();

	ListBox hi_year = new ListBox();
	ListBox hi_month = new ListBox();
	ListBox hi_day = new ListBox();
	HourListBox hi_hour = new HourListBox();
    
	Grid dateTimeWidget = new Grid(2, 5);

	boolean hasYear = false;
	boolean hasMonth = false;
	boolean hasDay = false;
	boolean hasHour = false;

	boolean climatology;
	boolean range;
	
	boolean isMenu = false;
	
	String render;
	
	String lo_date;
	String hi_date;
	
	int delta;

	private static final List<String> MONTHS = new ArrayList<String>(Arrays.asList("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"));
	
	//public DateTimeWidget(String lo_date, String hi_date, int deltaMinutes, int minuteOffset, String render, boolean range, boolean climatology) {
	public DateTimeWidget(TimeAxisSerializable tAxis, boolean range) {
        init(tAxis, range);
        setListeners();
		initWidget(dateTimeWidget);
	}
	public DateTimeWidget() {
		setListeners();
		initWidget(dateTimeWidget);
	}
	public void init(TimeAxisSerializable tAxis, boolean range) {
		dateTimeWidget.clear();
		hasYear = false;
		hasMonth = false;
		hasDay = false;
		hasHour = false;
		isMenu = false;
		this.range = range;
		
		
		if (tAxis.getNames() != null && tAxis.getNames().length > 0 ) {
			
			isMenu = true;
			lo_day.clear();
			hi_day.clear();
			String[] names = tAxis.getNames();
			String[] values = tAxis.getValues();
			for(int i = 0; i < names.length; i++ ) {
				lo_day.addItem(names[i], values[i]);
				hi_day.addItem(names[i], values[i]);
			}
			hasYear = false;
			hasMonth = false;
			hasDay = true;
			hasHour = false;
			
			lo_date = values[0];
			hi_date = values[values.length - 1];
			loadWidget();
		} else {
			lo_date = tAxis.getLo();
			hi_date = tAxis.getHi();
			
            init(lo_date, hi_date, tAxis.getRenderString(), tAxis.isClimatology());
		}
			
	}
	public void setListeners() {
		lo_year.addChangeListener(loYearListener);
		lo_month.addChangeListener(loMonthListener);
		lo_day.addChangeListener(loDayListener);
		lo_hour.addChangeListener(loHourListener);
		
		hi_year.addChangeListener(hiYearListener);
		hi_month.addChangeListener(hiMonthListener);
		hi_day.addChangeListener(hiDayListener);
		hi_hour.addChangeListener(hiHourListener);
	}
	public void init(String lo_date, String hi_date, int delta, String render, boolean climo) {
		this.render = render;
		this.climatology = climo;
        this.delta = delta;
        lo = parseDate(lo_date);
        hi = parseDate(hi_date);
		years(lo, hi, lo_year);
		years(lo, hi, hi_year);
		months(lo_month, lo.getYear()+1900);
		months(hi_month, hi.getYear()+1900);
		days(lo_day, lo.getYear()+1900, lo.getMonth());
		days(hi_day, hi.getYear()+1900, hi.getMonth());
	    hours(lo_hour, lo.getHours(), lo.getMinutes(), 24, 0);
		hours(hi_hour, 0, 0, hi.getHours(), lo.getMinutes());
		hi_year.setSelectedIndex(hi_year.getItemCount() - 1);
		hi_month.setSelectedIndex(hi_month.getItemCount() - 1);
		hi_day.setSelectedIndex(hi_day.getItemCount() - 1);
		loadWidget();
	}
	/**
	 * 
	 * @param hours
	 * @param start_hour
	 * @param start_minute
	 * @param end_hour
	 * @param end_minute
	 * @param delta
	 */
	private void hours(HourListBox hours, int start_hour, int start_minute, int end_hour, int end_minute) {
		hours.clear();
		if ( delta < 0 ) {
            hours.addItem("00:00", "00:00");
		} else {
			int current = start_hour*60 + start_minute;
			int end = end_hour*60 + end_minute;
			while ( current < end ) {
				int hr = (int) Math.floor(current/60);
				int min = current - hr*60;
				hours.addItem(hr, min);
				current = current + delta;
			}
		}
	}
	public void init(String lo_date, String hi_date, String render, boolean climo) {
		init(lo_date, hi_date, -1, render, climo);
	}
	private void loadWidget() {
		dateTimeWidget.clear();
		if ( isMenu ) {
			dateTimeWidget.setWidget(0, 0, d_label_lo_range);
			dateTimeWidget.setWidget(0, 1, lo_day);
			dateTimeWidget.setWidget(1, 0, d_label_hi_range);
			dateTimeWidget.setWidget(1, 1, hi_day);
		} else {
			if ( render.toLowerCase().contains("t") ) {
				dateTimeWidget.setWidget(0, 0, dt_label_lo_range);
				dateTimeWidget.setWidget(1, 0, dt_label_hi_range);

			} else {
				dateTimeWidget.setWidget(0, 0, d_label_lo_range);
				dateTimeWidget.setWidget(1, 0, d_label_hi_range);
			}
			
			if ( render.toLowerCase().contains("y") ) {
				dateTimeWidget.setWidget(0, 1, lo_year);
				dateTimeWidget.setWidget(1, 1, hi_year);
				hasYear = true;
			}
			
			if ( render.toLowerCase().contains("m") ) {
				dateTimeWidget.setWidget(0, 2, lo_month);
				dateTimeWidget.setWidget(1, 2, hi_month);
				hasMonth = true;
			}
			
			if ( render.toLowerCase().contains("d") ) {
				dateTimeWidget.setWidget(0, 3, lo_day);
				dateTimeWidget.setWidget(1, 3, hi_day);
				hasDay = true;
			}
			
			if ( render.toLowerCase().contains("t") ) {
				dateTimeWidget.setWidget(0, 4, lo_hour);
				dateTimeWidget.setWidget(1, 4, hi_hour);
				hasHour = true;
			}
			
		}
		CellFormatter cellFormatter = dateTimeWidget.getCellFormatter();
		if ( range ) {
			for ( int i = 0; i < 5; i++ ) {
				cellFormatter.setVisible(1, i, true);
			}
		} else {
			for ( int i = 0; i < 5; i++ ) {
				cellFormatter.setVisible(1, i, false);
			}
		}
	}
	public void setRange(boolean b) {
		if ( b ) {
			// Want range, not currently range do something.
			if ( !range ) {
				range = b;
				CellFormatter cellFormatter = dateTimeWidget.getCellFormatter();
				for ( int i = 0; i < 5; i++ ) {
					cellFormatter.setVisible(1, i, true);
				}
			}
		} else {
			// Don't want range.  Currently range, do something.
			if ( range ) {
				range = b;
				CellFormatter cellFormatter = dateTimeWidget.getCellFormatter();
				for ( int i = 0; i < 5; i++ ) {
					cellFormatter.setVisible(1, i, false);
				}
			}
		}
	}
	public boolean isRange() {
		return range;
	}
	private void days(ListBox day, int year, int month) {
		day.clear();
		int lo_year = lo.getYear() + 1900;
		int hi_year = hi.getYear() + 1900;
		
		int lo_month = lo.getMonth();
		int hi_month = hi.getMonth();
		
		int start = 1;
		int end = maxDays(year, month);
		
		if ( lo_year == year && lo_month == month ) {
			start = lo.getDate();
			end = maxDays(year, month);
			
		} else if ( hi_year == year && hi_month == month ) {
			end = hi.getDate();
		}
		for ( int i = start; i <= end; i++) {
			day.addItem(Util.format_two(i), Util.format_two(i));
		}
	}
	
	private int maxDays(int year, int month) {
		if ( month == 0 || month == 2 || month == 4 || month == 6 || month == 7 || month == 9 || month == 11) {
			return 31;
		} else if ( month == 1 ) {
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
	private void months(ListBox month, int year) {
		month.clear();
		int lo_year = lo.getYear() + 1900;
		int hi_year = hi.getYear() + 1900;
		
		int start = 0;
		int end = 12;
		
		if  ( lo_year == year ) {
			start = lo.getMonth();
		} else if ( hi_year == year ) {
			end = hi.getMonth() + 1;
		}
		for (int m = start; m < end; m++ ) {
			month.addItem(MONTHS.get(m), MONTHS.get(m));
		}
	}
	
	private void years(Date lo, Date hi, ListBox year) {
        year.clear();
		int start = lo.getYear() + 1900;
		int end = hi.getYear() + 1900;
		for ( int y = start; y <= end; y++ ) {
			year.addItem(Util.format_four(y), Util.format_four(y));
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
		dateTimeWidget.setVisible(b);
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
        if ( hasHour ) {
        	date.append(" "+lo_hour.getValue(lo_hour.getSelectedIndex()));
        }
		return date.toString();
	}

	public String getFerretDateHi() {
		if ( range ) {
			StringBuffer date = new StringBuffer();
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
				date.append("-0001");   
			} else {
				if ( hasYear ) {
					date.append("-"+hi_year.getValue(hi_year.getSelectedIndex()));
				}
			}
            if ( hasHour ) {
            	date.append(" "+hi_hour.getValue(hi_hour.getSelectedIndex()));
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
		
		if ( isMenu ) {
			for(int d = 0; d < lo_day.getItemCount(); d++) {
				String value = lo_day.getValue(d);
				if ( value.equals(tlo) ) {
					lo_day.setSelectedIndex(d);
				}
			}
		} else {
			Date lo = parseFerretDate(tlo);
			
			if ( hasDay ) {
				String day = String.valueOf(lo.getDate());
				for(int d = 0; d < lo_day.getItemCount(); d++) {
					String value = lo_day.getValue(d);
					if ( value.equals(day) ) {
						lo_day.setSelectedIndex(d);
					}
				}
			} 
			if ( hasMonth ) {
				String month = MONTHS.get(lo.getMonth());
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
	public void setHi(String thi) {
		
		if ( isMenu ) {
			for(int d = 0; d < hi_day.getItemCount(); d++) {
				String value = hi_day.getValue(d);
				if ( value.equals(thi) ) {
					hi_day.setSelectedIndex(d);
				}
			}
		} else {
			Date hi = parseFerretDate(thi);
			
			if ( hasDay ) {
				String day = String.valueOf(hi.getDate());
				for(int d = 0; d < hi_day.getItemCount(); d++) {
					String value = hi_day.getValue(d);
					if ( value.equals(day) ) {
						hi_day.setSelectedIndex(d);
					}
				}
			} 
			if ( hasMonth ) {
				String month = MONTHS.get(hi.getMonth());
				for ( int m = 0; m < hi_month.getItemCount(); m++ ) {
					String value = hi_month.getValue(m);
					if ( value.equals(month) ) {
						hi_month.setSelectedIndex(m);
					}
				}

			}
			if ( hasYear ) {
				String year = String.valueOf(hi.getYear() + 1900);
				for ( int y = 0; y < hi_year.getItemCount(); y++ ) {
					String value = hi_year.getValue(y);
					if ( value.equals(year) ) {
						hi_year.setSelectedIndex(y);
					}
				}
			}

		}
	}

	private void loadAndSetMonthDayHour(ListBox month_list, ListBox day_list, HourListBox hour_list, int year, int month, int day, int hour, int min) {
		// Load the valid months for this year.
		months(month_list, year);


		if ( month < MONTHS.indexOf(month_list.getValue(0)) ) {
			// If the current month is before the first month in the list
			// set to the first month.
			month_list.setSelectedIndex(0);
		} else if ( month > MONTHS.indexOf(month_list.getValue(month_list.getItemCount() - 1))) {
			// If the current month is after the last month in the list
			// set to the last month.
			month_list.setSelectedIndex(month_list.getItemCount() - 1);
		} else {
			// Else set to that month
			for (int i = 0; i < month_list.getItemCount(); i++) {
				String v = month_list.getValue(i);
				if ( v.equals(MONTHS.get(month)) ) {
					month_list.setSelectedIndex(i);
				}
			}
		}
		
		loadAndSetDayHour(day_list, hour_list, year, MONTHS.indexOf(month_list.getValue(month_list.getSelectedIndex())), day, hour, min);	
	}
	
	public void loadAndSetDayHour(ListBox day_list, HourListBox hour_list, int year, int month, int day, int hour, int min) {
		// Load the valid days for this month (which as set above) and year.
		days(day_list, year, month);

		if ( day < Integer.valueOf(day_list.getValue(0)).intValue() ) {
			day_list.setSelectedIndex(0);
		} else if ( day > Integer.valueOf(day_list.getValue(day_list.getItemCount() - 1)).intValue() ) {
			day_list.setSelectedIndex(day_list.getItemCount() - 1);
		} else {

			for (int i = 0; i < day_list.getItemCount(); i++) {
				String v = day_list.getValue(i);
				if ( v.equals(Util.format_two(day)) ) {
					day_list.setSelectedIndex(i);
				}
			}
		}
		
		loadAndSetHour(hour_list, year, month, Integer.valueOf(day_list.getValue(day_list.getSelectedIndex())).intValue(), hour, min);
		
	}
	public void loadAndSetHour(HourListBox hour_list, int year, int month, int day, int hour, int min) {
		int start_year = lo.getYear() + 1900;
		int start_month = lo.getMonth();
		int start_day = lo.getDate();
		int start_hour = lo.getHours();
		int start_min = lo.getMinutes();
		
		int end_year = hi.getYear() + 1900;
		int end_month = hi.getMonth();
		int end_day = hi.getDate();
		int end_hour = hi.getHours();
		int end_min = hi.getMinutes();
		if ( start_year == year && start_month == month && start_day == day ) {
			hours(hour_list, start_hour, start_min, 24, 0);
		} else if ( end_year == year && end_month == month && end_day == day ) {
			hours(hour_list, 0, 0, end_hour, end_min);
		} else {
			hours(hour_list, 0, 0, 24, 0);
		}
	}
	private void checkRangeEndYear() {
		String current_lo = getFerretDateLo();
		String current_hi = getFerretDateHi();
		
		Date clo = parseFerretDate(current_lo);
		Date chi = parseFerretDate(current_hi);
		
		// Set the hi year to the lo year and check the month...
		if ( clo.after(chi) ) {
			int year = Integer.valueOf(lo_year.getValue(lo_year.getSelectedIndex()));
			hi_year.setSelectedIndex(lo_year.getSelectedIndex());
			loadAndSetMonthDayHour(hi_month, hi_day, hi_hour, year, MONTHS.indexOf(hi_month.getValue(hi_month.getSelectedIndex())), Integer.valueOf(hi_day.getValue(hi_day.getSelectedIndex())).intValue(), hi_hour.getHour(), hi_hour.getMin());
			checkRangeEndMonth();
		}
		
	}
	private void checkRangeEndMonth() {
		String current_lo = getFerretDateLo();
		String current_hi = getFerretDateHi();
		
		Date clo = parseFerretDate(current_lo);
		Date chi = parseFerretDate(current_hi);
		
		if ( clo.after(chi) ) {
			int ny = Integer.valueOf(hi_year.getValue(hi_year.getSelectedIndex()));
			int month = MONTHS.indexOf(lo_month.getValue(lo_month.getSelectedIndex()));
			int day = Integer.valueOf(hi_day.getValue(hi_day.getSelectedIndex()));
			hi_month.setSelectedIndex(lo_month.getSelectedIndex());
			int hour = hi_hour.getHour();
			int min = hi_hour.getMin();
			loadAndSetDayHour(hi_day, hi_hour, ny, month, day, hour, min);
			checkRangeEndDay();
		}
	}
	private void checkRangeEndDay() {
		String current_lo = getFerretDateLo();
		String current_hi = getFerretDateHi();
		
		Date clo = parseFerretDate(current_lo);
		Date chi = parseFerretDate(current_hi);
		
		if ( clo.after(chi) ) {
			hi_day.setSelectedIndex(lo_day.getSelectedIndex());
			checkRangeEndHour();
		}
	}
	private void checkRangeEndHour() {
		String current_lo = getFerretDateLo();
		String current_hi = getFerretDateHi();
		
		Date clo = parseFerretDate(current_lo);
		Date chi = parseFerretDate(current_hi);
		
		if ( clo.after(chi) ) {
			hi_hour.setSelectedIndex(lo_hour.getSelectedIndex());
		}
	}
	private void checkRangeStartYear() {
		String current_lo = getFerretDateLo();
		String current_hi = getFerretDateHi();
		
		Date clo = parseFerretDate(current_lo);
		Date chi = parseFerretDate(current_hi);
		
		// Set the hi year to the lo year and check the month...
		if ( clo.after(chi) ) {
			lo_year.setSelectedIndex(hi_year.getSelectedIndex());
			checkRangeStartMonth();
		}
		
	}
	private void checkRangeStartMonth() {
		String current_lo = getFerretDateLo();
		String current_hi = getFerretDateHi();
		
		Date clo = parseFerretDate(current_lo);
		Date chi = parseFerretDate(current_hi);

		if ( clo.after(chi) ) {
			lo_month.setSelectedIndex(hi_month.getSelectedIndex());
			checkRangeStartDay();
		}
	}
	private void checkRangeStartDay() {
		String current_lo = getFerretDateLo();
		String current_hi = getFerretDateHi();
		
		Date clo = parseFerretDate(current_lo);
		Date chi = parseFerretDate(current_hi);
		
		if ( clo.after(chi) ) {
			lo_day.setSelectedIndex(hi_day.getSelectedIndex());
			checkRangeStartHour();
		}
	}
	public void checkRangeStartHour() {
		String current_lo = getFerretDateLo();
		String current_hi = getFerretDateHi();
		
		Date clo = parseFerretDate(current_lo);
		Date chi = parseFerretDate(current_hi);
		
		if ( clo.after(chi) ) {
			lo_hour.setSelectedIndex(hi_hour.getSelectedIndex());
		}
	}
	public ChangeListener loYearListener = new ChangeListener() {
		public void onChange(Widget sender) {
			int year = Integer.valueOf(lo_year.getValue(lo_year.getSelectedIndex())).intValue();
			int month = MONTHS.indexOf(lo_month.getValue(lo_month.getSelectedIndex()));
			int day = Integer.valueOf(lo_day.getValue(lo_day.getSelectedIndex())).intValue();
			int hour = lo_hour.getHour();
			int min = lo_hour.getMin();
			loadAndSetMonthDayHour(lo_month, lo_day, lo_hour, year, month, day, hour, min);
			checkRangeEndYear();
		}
	};
	public ChangeListener loMonthListener = new ChangeListener() {
		public void onChange(Widget sender) {
			int year = Integer.valueOf(lo_year.getValue(lo_year.getSelectedIndex())).intValue();
			int month = MONTHS.indexOf(lo_month.getValue(lo_month.getSelectedIndex()));
			int day = Integer.valueOf(lo_day.getValue(lo_day.getSelectedIndex())).intValue();
			int hour = lo_hour.getHour();
			int min = lo_hour.getMin();
			loadAndSetDayHour(lo_day, lo_hour, year, month, day, hour, min);
			checkRangeEndMonth();
		}	
	};
	// The loDayListener and hiDayListener are the only ones that should fire when the Widget contains a menu.
	public ChangeListener loDayListener = new ChangeListener() {
		public void onChange(Widget sender) {
			if ( isMenu ) {
				int lo_i = lo_day.getSelectedIndex();
				int hi_i = hi_day.getSelectedIndex();
				if ( lo_i > hi_i ) {
					hi_day.setSelectedIndex(lo_i);
				}
			} else {
				int year = Integer.valueOf(lo_year.getValue(lo_year.getSelectedIndex())).intValue();
				int month = MONTHS.indexOf(lo_month.getValue(lo_month.getSelectedIndex()));
				int day = Integer.valueOf(lo_day.getValue(lo_day.getSelectedIndex())).intValue();
				int hour = lo_hour.getHour();
				int min = lo_hour.getMin();
				loadAndSetHour(lo_hour, year, month, day, hour, min);
				checkRangeEndDay();
			} 
		}
	};
	public ChangeListener loHourListener = new ChangeListener() {
		public void onChange(Widget sender) {
			checkRangeEndHour();
		}
	};
	public ChangeListener hiYearListener = new ChangeListener() {
		public void onChange(Widget sender) {
			int year = Integer.valueOf(hi_year.getValue(hi_year.getSelectedIndex())).intValue();
			int month = MONTHS.indexOf(hi_month.getValue(hi_month.getSelectedIndex()));
			int day = Integer.valueOf(hi_day.getValue(hi_day.getSelectedIndex())).intValue();
			int hour = hi_hour.getHour();
			int min = hi_hour.getMin();
			loadAndSetMonthDayHour(hi_month, hi_day, hi_hour, year, month, day, hour, min);
			checkRangeStartYear();
		}
	};
	public ChangeListener hiMonthListener = new ChangeListener() {
		public void onChange(Widget sender) {
			int year = Integer.valueOf(hi_year.getValue(hi_year.getSelectedIndex())).intValue();
			int month = MONTHS.indexOf(hi_month.getValue(hi_month.getSelectedIndex()));
			int day = Integer.valueOf(hi_day.getValue(hi_day.getSelectedIndex())).intValue();
			int hour = hi_hour.getHour();
			int	min = hi_hour.getMin();  
			loadAndSetDayHour(hi_day, hi_hour, year, month, day, hour, min);
			checkRangeStartMonth();
		}	
	};
	public ChangeListener hiDayListener = new ChangeListener() {
		public void onChange(Widget sender) {
			if ( isMenu ) {
				int lo_i = lo_day.getSelectedIndex();
				int hi_i = hi_day.getSelectedIndex();
				if ( lo_i > hi_i ) {
					lo_day.setSelectedIndex(hi_i);
				}
			} else {
			int year = Integer.valueOf(hi_year.getValue(hi_year.getSelectedIndex())).intValue();
			int month = MONTHS.indexOf(hi_month.getValue(hi_month.getSelectedIndex()));
			int day = Integer.valueOf(hi_day.getValue(hi_day.getSelectedIndex())).intValue();
			int hour = hi_hour.getHour();
			int	min = hi_hour.getMin();  
			loadAndSetHour(hi_hour, year, month, day, hour, min);
			checkRangeStartDay();
			}
		}
	};
	public ChangeListener hiHourListener = new ChangeListener() {
		public void onChange(Widget sender) {
			checkRangeStartHour();
		}
	};
	/**
	 * Helper method to parse ferret dates.
	 * @param date_string of the form 1998-Jan-15, 1983-Jan-30 12:32 or 1998-Apr-01 12:11:03
	 * @return Date for the parse date
	 */
	private static Date parseFerretDate(String date_string) {
		Date date;
		if ( date_string.length() == 11 ) {
			date = shortFerretForm.parse(date_string);
		} else if ( date_string.length() == 17 ) {
			date = mediumFerretForm.parse(date_string);
		} else {
			date = longFerretForm.parse(date_string);
		}
		return date;
	}
	/**
	 * Helper method to parse date strings
	 * @param date_string of the form 1998-11-05, 1998-12-31 11:02 or 1923-11-14 04:13:21
	 * @return
	 */
	private static Date parseDate(String date_string) {
		Date date;
		 try {
	        	date = longForm.parse(date_string);
	        } catch (IllegalArgumentException e) {
	        	try {
	        		 date = mediumForm.parse(date_string);
	        	} catch (IllegalArgumentException e1) {
	        		try{
	        			date = shortForm.parse(date_string);
	        		} catch (IllegalArgumentException e2) {
	        			date = null;
	        			Window.alert("Date parsing failed for "+date_string);
	        		}
	        	}
	        }
		return date;
	}
}
