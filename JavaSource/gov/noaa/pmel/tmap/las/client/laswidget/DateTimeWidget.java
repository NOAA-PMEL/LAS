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
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class DateTimeWidget extends Composite {
	
	DateTimeFormat longForm = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
	DateTimeFormat shortForm = DateTimeFormat.getFormat("yyyy-MM-dd");
	DateTimeFormat shortFerretForm = DateTimeFormat.getFormat("dd-MMM-yyyy");
	DateTimeFormat longFerretForm = DateTimeFormat.getFormat("dd-MMM-yyyy HH:mm:ss");
	
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
	ListBox lo_hour = new ListBox();

	ListBox hi_year = new ListBox();
	ListBox hi_month = new ListBox();
	ListBox hi_day = new ListBox();
	ListBox hi_hour = new ListBox();

	HorizontalPanel lo_flow = new HorizontalPanel();
	HorizontalPanel hi_flow = new HorizontalPanel();

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
	
	String render;
	
	String lo_date;
	String hi_date;

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
		lo_flow.addStyleName("las-vertical-align-center");
		hi_flow.addStyleName("las-vertical-align-center");
		lo_flow.clear();
		hi_flow.clear();
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
		lo_hour.addChangeListener(loDayListener);
		
		hi_year.addChangeListener(hiYearListener);
		hi_month.addChangeListener(hiMonthListener);
		hi_day.addChangeListener(hiDayListener);
		hi_hour.addChangeListener(hiDayListener);
	}
	public void init(String lo_date, String hi_date, String render, boolean climo) {
		this.render = render;
		this.climatology = climo;

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
		} catch (IllegalArgumentException ex1) {
			try {
				hi = shortForm.parse(hi_date);
			} catch (IllegalArgumentException ex2) {
				Window.alert("Date parsing failed for "+hi_date);
			}
		}

		years(lo, hi, lo_year);
		years(lo, hi, hi_year);
		months(lo_month, lo.getYear()+1900);
		months(hi_month, hi.getYear()+1900);
		days(lo_day, lo.getYear()+1900, lo.getMonth());
		days(hi_day, hi.getYear()+1900, hi.getMonth());
		hi_year.setSelectedIndex(hi_year.getItemCount() - 1);
		hi_month.setSelectedIndex(hi_month.getItemCount() - 1);
		hi_day.setSelectedIndex(hi_day.getItemCount() - 1);
		loadWidget();
	}
	private void loadWidget() {
		lo_flow.clear();
		hi_flow.clear();
		dateTimeWidget.clear();
		if ( isMenu ) {
			if ( range ) {
				lo_flow.add(d_label_lo_range);
				lo_flow.add(lo_day);
				hi_flow.add(d_label_hi_range);
				hi_flow.add(hi_day);
			} else {
			    lo_flow.add(d_label);
			    lo_flow.add(lo_day);
			}
		} else {
			if ( render.contains("T") || render.contains("t") ) {
				if ( range ) {
					lo_flow.add(dt_label_lo_range);
					hi_flow.add(dt_label_hi_range);
				} else {
				    lo_flow.add(dt_label);
				}
			} else {
				if ( range ) {
					lo_flow.add(d_label_lo_range);
					hi_flow.add(d_label_hi_range);
				} else {
				    lo_flow.add(d_label);
				}
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
	public void setRange(boolean b) {
		if ( b ) {
			// Want range, not currently range do something.
			if ( !range ) {
				range = b;
				loadWidget();
			}
		} else {
			// Don't want range.  Currently range, do something.
			if ( range ) {
				range = b;
				loadWidget();
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
			day.addItem(format_two(i), format_two(i));
		}
	}
	private String format_two(int i) {
		if ( i < 10 ) {
			return "0"+i;
		} else {
			return String.valueOf(i);
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
		// TODO what about hours????
		if ( isMenu ) {
			for(int d = 0; d < hi_day.getItemCount(); d++) {
				String value = hi_day.getValue(d);
				if ( value.equals(thi) ) {
					hi_day.setSelectedIndex(d);
				}
			}
		} else {
			Date hi;
			if ( thi.length() == 11 ) {
				hi = shortFerretForm.parse(thi);
			} else {
				hi = longFerretForm.parse(thi);
			}
			if ( hasDay ) {
				String day = String.valueOf(hi.getDay());
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

	private void loadAndSetMonthDay(ListBox month_list, ListBox day_list, int year, int month, int day) {
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
		
		loadAndSetDay(day_list, year, MONTHS.indexOf(month_list.getValue(month_list.getSelectedIndex())), day);	
	}
	
	public void loadAndSetDay(ListBox day_list, int year, int month, int day) {
		// Load the valid days for this month (which as set above) and year.
		days(day_list, year, month);

		if ( day < Integer.valueOf(day_list.getValue(0)).intValue() ) {
			day_list.setSelectedIndex(0);
		} else if ( day > Integer.valueOf(day_list.getValue(day_list.getItemCount() - 1)).intValue() ) {
			day_list.setSelectedIndex(day_list.getItemCount() - 1);
		} else {

			for (int i = 0; i < day_list.getItemCount(); i++) {
				String v = day_list.getValue(i);
				if ( v.equals(format_two(day)) ) {
					day_list.setSelectedIndex(i);
				}
			}
		}
	}
	private void checkRangeEndYear() {
		String current_lo = getFerretDateLo();
		String current_hi = getFerretDateHi();
		
		Date clo; 
	    if ( current_lo.length() == 11 ) {
			clo = shortFerretForm.parse(current_lo);
	    } else {
	    	clo = longFerretForm.parse(current_lo);
	    }
		Date chi;
		if ( current_hi.length() == 11 ) {
		   chi = shortFerretForm.parse(current_hi);
		} else {
			chi = longFerretForm.parse(current_hi);
		}
		
		// Set the hi year to the lo year and check the month...
		if ( clo.after(chi) ) {
			int year = Integer.valueOf(lo_year.getValue(lo_year.getSelectedIndex()));
			hi_year.setSelectedIndex(lo_year.getSelectedIndex());
			loadAndSetMonthDay(hi_month, hi_day, year, MONTHS.indexOf(hi_month.getValue(hi_month.getSelectedIndex())), Integer.valueOf(hi_day.getValue(hi_day.getSelectedIndex())).intValue());
			checkRangeEndMonth();
		}
		
	}
	private void checkRangeEndMonth() {
		String current_lo = getFerretDateLo();
		String current_hi = getFerretDateHi();
		
		Date clo; 
	    if ( current_lo.length() == 11 ) {
			clo = shortFerretForm.parse(current_lo);
	    } else {
	    	clo = longFerretForm.parse(current_lo);
	    }
		Date chi;
		if ( current_hi.length() == 11 ) {
		   chi = shortFerretForm.parse(current_hi);
		} else {
			chi = longFerretForm.parse(current_hi);
		}
		if ( clo.after(chi) ) {
			int ny = Integer.valueOf(hi_year.getValue(hi_year.getSelectedIndex()));
			int month = MONTHS.indexOf(lo_month.getValue(lo_month.getSelectedIndex()));
			int day = Integer.valueOf(hi_day.getValue(hi_day.getSelectedIndex()));
			hi_month.setSelectedIndex(lo_month.getSelectedIndex());
			loadAndSetDay(hi_day, ny, month, day);
			checkRangeEndDay();
		}
	}
	private void checkRangeEndDay() {
		String current_lo = getFerretDateLo();
		String current_hi = getFerretDateHi();
		
		Date clo; 
	    if ( current_lo.length() == 11 ) {
			clo = shortFerretForm.parse(current_lo);
	    } else {
	    	clo = longFerretForm.parse(current_lo);
	    }
		Date chi;
		if ( current_hi.length() == 11 ) {
		   chi = shortFerretForm.parse(current_hi);
		} else {
			chi = longFerretForm.parse(current_hi);
		}
		if ( clo.after(chi) ) {
			hi_day.setSelectedIndex(lo_day.getSelectedIndex());
		}
	}
	private void checkRangeStartYear() {
		String current_lo = getFerretDateLo();
		String current_hi = getFerretDateHi();
		
		Date clo; 
	    if ( current_lo.length() == 11 ) {
			clo = shortFerretForm.parse(current_lo);
	    } else {
	    	clo = longFerretForm.parse(current_lo);
	    }
		Date chi;
		if ( current_hi.length() == 11 ) {
		   chi = shortFerretForm.parse(current_hi);
		} else {
			chi = longFerretForm.parse(current_hi);
		}
		
		// Set the hi year to the lo year and check the month...
		if ( clo.after(chi) ) {
			lo_year.setSelectedIndex(hi_year.getSelectedIndex());
			checkRangeStartMonth();
		}
		
	}
	private void checkRangeStartMonth() {
		String current_lo = getFerretDateLo();
		String current_hi = getFerretDateHi();
		
		Date clo; 
	    if ( current_lo.length() == 11 ) {
			clo = shortFerretForm.parse(current_lo);
	    } else {
	    	clo = longFerretForm.parse(current_lo);
	    }
		Date chi;
		if ( current_hi.length() == 11 ) {
		   chi = shortFerretForm.parse(current_hi);
		} else {
			chi = longFerretForm.parse(current_hi);
		}
		if ( clo.after(chi) ) {
			lo_month.setSelectedIndex(hi_month.getSelectedIndex());
			checkRangeStartDay();
		}
	}
	private void checkRangeStartDay() {
		String current_lo = getFerretDateLo();
		String current_hi = getFerretDateHi();
		
		Date clo; 
	    if ( current_lo.length() == 11 ) {
			clo = shortFerretForm.parse(current_lo);
	    } else {
	    	clo = longFerretForm.parse(current_lo);
	    }
		Date chi;
		if ( current_hi.length() == 11 ) {
		   chi = shortFerretForm.parse(current_hi);
		} else {
			chi = longFerretForm.parse(current_hi);
		}
		if ( clo.after(chi) ) {
			lo_day.setSelectedIndex(hi_day.getSelectedIndex());
		}
	}
	public ChangeListener loYearListener = new ChangeListener() {
		public void onChange(Widget sender) {
			int year = Integer.valueOf(lo_year.getValue(lo_year.getSelectedIndex())).intValue();
			int month = MONTHS.indexOf(lo_month.getValue(lo_month.getSelectedIndex()));
			int day = Integer.valueOf(lo_day.getValue(lo_day.getSelectedIndex())).intValue();
			loadAndSetMonthDay(lo_month, lo_day, year, month, day);
			checkRangeEndYear();
		}
	};
	public ChangeListener loMonthListener = new ChangeListener() {
		public void onChange(Widget sender) {
			int year = Integer.valueOf(lo_year.getValue(lo_year.getSelectedIndex())).intValue();
			int month = MONTHS.indexOf(lo_month.getValue(lo_month.getSelectedIndex()));
			int day = Integer.valueOf(lo_day.getValue(lo_day.getSelectedIndex())).intValue();
			loadAndSetDay(lo_day, year, month, day);
			checkRangeEndMonth();
		}	
	};
	public ChangeListener loDayListener = new ChangeListener() {
		public void onChange(Widget sender) {
			checkRangeEndDay();
		}
	};
	public ChangeListener hiYearListener = new ChangeListener() {
		public void onChange(Widget sender) {
			int year = Integer.valueOf(hi_year.getValue(hi_year.getSelectedIndex())).intValue();
			int month = MONTHS.indexOf(hi_month.getValue(hi_month.getSelectedIndex()));
			int day = Integer.valueOf(hi_day.getValue(hi_day.getSelectedIndex())).intValue();
			loadAndSetMonthDay(hi_month, hi_day, year, month, day);
			checkRangeStartYear();
		}
	};
	public ChangeListener hiMonthListener = new ChangeListener() {
		public void onChange(Widget sender) {
			int year = Integer.valueOf(hi_year.getValue(hi_year.getSelectedIndex())).intValue();
			int month = MONTHS.indexOf(hi_month.getValue(hi_month.getSelectedIndex()));
			int day = Integer.valueOf(hi_day.getValue(hi_day.getSelectedIndex())).intValue();
			loadAndSetDay(hi_day, year, month, day);
			checkRangeStartMonth();
		}	
	};
	public ChangeListener hiDayListener = new ChangeListener() {
		public void onChange(Widget sender) {
			checkRangeStartDay();
		}
	};
//	public ChangeListener loListener = new ChangeListener() {
//		public void onChange(Widget sender) {
//			/*
//			 *  When one of the widgets that makes up a date changes, two things must be true.
//			 *     1. All widgets must show all and only valid selections based on the new value.
//			 *     2. The end date has to be greater than or equal to the start date.
//			 */
//			String current_lo = getFerretDateLo();
//			String current_hi = getFerretDateHi();
//			
//			Date clo = shortFerretForm.parse(current_lo);
//			Date chi = shortFerretForm.parse(current_hi);
//			
//			if ( clo.after(chi) ) {
//				setHi(shortFerretForm.format(clo));
//			}
//		}
//	};	
//	public ChangeListener hiListener = new ChangeListener() {
//		public void onChange(Widget sender) {
//			/*
//			 *  When one of the widgets that makes up a date changes, two things must be true.
//			 *     1. All widgets must show all and only valid selections based on the new value.
//			 *     2. The end date has to be greater than or equal to the start date.
//			 */
//			
//			String current_lo = getFerretDateLo();
//			String current_hi = getFerretDateHi();
//			
//			Date clo; 
//		    if ( current_lo.length() == 11 ) {
//				clo = shortFerretForm.parse(current_lo);
//		    } else {
//		    	clo = longFerretForm.parse(current_lo);
//		    }
//			Date chi;
//			if ( current_hi.length() == 11 ) {
//			   chi = shortFerretForm.parse(current_hi);
//			} else {
//				chi = longFerretForm.parse(current_hi);
//			}
//			
//			if ( clo.after(chi) ) {
//				setLo(shortForm.format(chi));
//			}
//			
//		}
//	};	
}
