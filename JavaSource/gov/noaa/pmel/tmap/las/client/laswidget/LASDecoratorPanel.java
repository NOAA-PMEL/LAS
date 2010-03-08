package gov.noaa.pmel.tmap.las.client.laswidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;

public class LASDecoratorPanel extends Composite {

	DecoratorPanel myPanel = new DecoratorPanel();
	FlexTable myFlexTable = new FlexTable();
	ToggleButton showHide;
	Widget widget;

	public LASDecoratorPanel(String title, Widget widget, Boolean visible) {
        this.widget = widget;
        widget.setVisible(visible);
		Label widgetTitle = new Label(title);
		if (visible) {
			showHide = new ToggleButton(new Image(GWT.getModuleBaseURL()+"../images/minus_on.png"),new Image(GWT.getModuleBaseURL()+"../images/plus_on.png"),showHideHandler);
		} else {
			showHide = new ToggleButton(new Image(GWT.getModuleBaseURL()+"../images/plus_on.png"),new Image(GWT.getModuleBaseURL()+"../images/minus_on.png"),showHideHandler);
		}
		myFlexTable.setWidget(0, 0, showHide);
		myFlexTable.setWidget(0, 1, widgetTitle);
		FlexCellFormatter myFormatter = myFlexTable.getFlexCellFormatter();
        myFormatter.setColSpan(1, 0, 2);
        myFlexTable.setWidget(1, 0, widget);
        myPanel.add(myFlexTable);
        initWidget(myPanel);

		
	}
	
	ClickHandler showHideHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			if (widget.isVisible()) {
				widget.setVisible(false);
			} else {
				widget.setVisible(true);
			}
		}	
	};
}
