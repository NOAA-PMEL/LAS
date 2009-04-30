package gov.noaa.pmel.tmap.las.client.laswidget;

import java.util.Map;

import gov.noaa.pmel.tmap.las.client.RPCServiceAsync;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;

public class OptionsButton extends Composite {
	Button optionsButton = new Button("Plot Options");
	DialogBox optionsDialog = new DialogBox(false);
	OptionsWidget options;
	Map<String, String> state;
    public OptionsButton (RPCServiceAsync optionsService, String opid) {
    	options = new OptionsWidget(optionsService, opid, okClick, cancelClick);
    	optionsButton.addClickListener(openClick);
    	optionsDialog.add(options);
    	optionsDialog.setText("Set Plot Options for all Plots.");
    	initWidget(optionsButton);
    	state = options.getState();
    }
    ClickListener okClick = new ClickListener() {
		public void onClick(Widget sender) {
			state = options.getState();
			optionsDialog.hide();
		}
    };
    ClickListener cancelClick = new ClickListener() {
		public void onClick(Widget sender) {
			options.restore(state);
			optionsDialog.hide();
		}
    };
    ClickListener openClick = new ClickListener() {
		public void onClick(Widget sender) {
			state = options.getState();
			optionsDialog.setPopupPosition(optionsButton.getAbsoluteLeft() - 300, optionsButton.getAbsoluteTop()-60);
			optionsDialog.show();
		}
    };
    public Map<String, String> getState() {
    	return state;
    }
	public void addOkClickListener(ClickListener optionsOkListener) {
		options.addOkClickListner(optionsOkListener);	
	}
	public void setState(Map<String, String> state) {
		options.restore(state);
		this.state = options.getState();
	}
}
