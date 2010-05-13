package gov.noaa.pmel.tmap.las.client.laswidget;

import java.util.Map;

import gov.noaa.pmel.tmap.las.client.RPCServiceAsync;
import gov.noaa.pmel.tmap.las.client.serializable.OptionSerializable;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

public class OptionsButton extends Composite {
	PushButton optionsButton = new PushButton("Plot Options");
	DialogBox optionsDialog = new DialogBox(false);
	OptionsWidget options;
	Map<String, String> state;
	int offset;
    public OptionsButton (String opid, int offset) {
    	this.offset = offset;
    	options = new OptionsWidget(opid, okClick, cancelClick);
    	optionsButton.addClickListener(openClick);
    	optionsButton.setWidth("75px");
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
			optionsDialog.setPopupPosition(optionsButton.getAbsoluteLeft() - offset, optionsButton.getAbsoluteTop());
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
	public void setOptions(OptionSerializable[] opt) {
		options.setOptions(opt);
	}
	public void setOptions(String id) {
		options.setOptions(id);
	}
	public void setOptions(String id, Map<String, String> state) {
		options.setOptions(id, state);
	}
}
