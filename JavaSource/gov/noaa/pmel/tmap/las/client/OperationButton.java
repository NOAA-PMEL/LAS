package gov.noaa.pmel.tmap.las.client;

import com.google.gwt.user.client.ui.RadioButton;

public class OperationButton extends RadioButton {
    OperationSerializable operation;
	public OperationButton(String name, String label, boolean asHTML) {
		super(name, label, asHTML);
	}

	public OperationButton(String name, String label) {
		super(name, label);
	}

	public OperationButton(String name) {
		super(name);
	}
    public OperationSerializable getOperation() {
    	return operation;
    }
    public void setOperation(OperationSerializable operation) {
    	this.operation = operation;
    }
}
