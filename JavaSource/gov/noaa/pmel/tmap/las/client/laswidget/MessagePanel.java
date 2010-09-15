package gov.noaa.pmel.tmap.las.client.laswidget;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class MessagePanel extends Composite {
	PopupPanel messagePanel;
	Grid messageGrid;
	Button messageButton;
	HTML message;
	String messageText = "Empty message.";
	public MessagePanel() {
		messagePanel = new PopupPanel();
		messageGrid = new Grid(2, 1);
		messageButton = new Button("Close");
		messageButton.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				messagePanel.hide();
			}

		});
        messageButton.setWidth("60px");
		message = new HTML(messageText);
		messageGrid.setWidget(0, 0, message);
		messageGrid.setWidget(1, 0, messageButton);

		messagePanel.add(messageGrid);
	}
	public void show(int left, int top, String text) {
		messagePanel.setPopupPosition(left, top);
		messageText = text;
		message.setHTML(messageText);
		messagePanel.show();
	}
	public void hide() {
		messagePanel.hide();
	}
}
