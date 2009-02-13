package gov.noaa.pmel.tmap.las.client;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TimeAxisSerializable extends AxisSerializable implements IsSerializable {
	
	// Time specific stuff.
	String widget_type;
	double minuteInterval;
	boolean yearNeeded;
	boolean monthNeeded;
	boolean dayNeeded;
	boolean hourNeeded;
	boolean climatology;
	public boolean isClimatology() {
		return climatology;
	}
	public void setClimatology(boolean climatology) {
		this.climatology = climatology;
	}
	public String getWidget_type() {
		return widget_type;
	}
	public void setWidget_type(String widget_type) {
		this.widget_type = widget_type;
	}
	public double getMinuteInterval() {
		return minuteInterval;
	}
	public void setMinuteInterval(double minuteInterval) {
		this.minuteInterval = minuteInterval;
	}
	public boolean isYearNeeded() {
		return yearNeeded;
	}
	public void setYearNeeded(boolean yearNeeded) {
		this.yearNeeded = yearNeeded;
	}
	public boolean isMonthNeeded() {
		return monthNeeded;
	}
	public void setMonthNeeded(boolean monthNeeded) {
		this.monthNeeded = monthNeeded;
	}
	public boolean isDayNeeded() {
		return dayNeeded;
	}
	public void setDayNeeded(boolean dayNeeded) {
		this.dayNeeded = dayNeeded;
	}
	public boolean isHourNeeded() {
		return hourNeeded;
	}
	public void setHourNeeded(boolean hourNeeded) {
		this.hourNeeded = hourNeeded;
	}
	public String getRenderString() {	
		StringBuffer render = new StringBuffer();
		if ( !isClimatology() ) {
			// Leave off the year for climos...
			if ( isYearNeeded() ) {
				render.append("Y");
			}
		}
		if ( isMonthNeeded() ) {
			render.append("M");
		}
		if ( isDayNeeded() ) {
			render.append("D");
		}
		if ( isHourNeeded() ) {
			render.append("T");
		}
		return render.toString();
	}
	
}