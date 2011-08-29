package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.ListBox;

public class VariableListBox extends ListBox {
    List<VariableSerializable> tVariables = new ArrayList<VariableSerializable>();
	public VariableListBox() {
	}

	public VariableListBox(boolean isMultipleSelect) {
		super(isMultipleSelect);
	}

	public VariableListBox(Element element) {
		super(element);
	}
    public void addItem(VariableSerializable v) {
    	tVariables.add(v);
    	addItem(v.getName(), v.getID());
    }
    public VariableSerializable getVariable(int i) {
    	return tVariables.get(i);
    }
}
