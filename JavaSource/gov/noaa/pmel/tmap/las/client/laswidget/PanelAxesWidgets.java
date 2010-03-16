package gov.noaa.pmel.tmap.las.client.laswidget;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class PanelAxesWidgets extends Composite {
	HorizontalPanel layout = new HorizontalPanel();
	DateTimeWidget dateTimeWidget = new DateTimeWidget();
	AxisWidget zAxisWidget = new AxisWidget();
	public PanelAxesWidgets() {
		layout.add(dateTimeWidget);
		layout.add(zAxisWidget);
		initWidget(layout);
	}
	public DateTimeWidget getDateWidget() {
		return dateTimeWidget;
	}
	public AxisWidget getZWidget() {
		return zAxisWidget;
	}
	public void addAxis(String axis) {
		if ( axis.equals("t") ) {
			if ( dateTimeWidget != null ) {
				layout.add(dateTimeWidget);
			}
		} else if ( axis.equals("z") ) {
			if ( zAxisWidget != null ) {
				layout.add(zAxisWidget);
			}
		}
	}
	public void removeAxes() {
		if ( dateTimeWidget != null ) {
			layout.remove(dateTimeWidget);
		}
		if ( zAxisWidget != null ) {
			layout.remove(zAxisWidget);
		}		
	}
	public void addTChangeListener(ChangeListener listener) {
		dateTimeWidget.addChangeListener(listener);
	}
	public void addZChangeListener(ChangeListener listener) {
		zAxisWidget.addChangeListener(listener);
	}
	public void setRange(String type, boolean b) {
		if ( type.equals("z") ) {
			zAxisWidget.setRange(b);
		} else if ( type.equals("t") ) {
			dateTimeWidget.setRange(b);
		}
	}
}
