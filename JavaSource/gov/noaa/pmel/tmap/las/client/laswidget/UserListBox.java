package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.serializable.Serializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class UserListBox extends Composite {
	List<VariableSerializable> userObject = new ArrayList<VariableSerializable>();
	List<VariableSerializable> vectors = new ArrayList<VariableSerializable>();
    ListBox list;
    FlexTable panel = new FlexTable();
    PushButton add = new PushButton("+");
    PushButton remove = new PushButton("X");
	public UserListBox() {
		super();
		list = new ListBox();
		init();
	}
	public UserListBox(boolean isMultipleSelect) {
		super();
		list = new ListBox(isMultipleSelect);
		init();
	}
    public void init() {
    	panel.setWidget(0, 0, add);
    	panel.setWidget(0, 1, remove);
    	add.setVisible(false);
    	remove.setVisible(false);
    	add.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				// When this handler is activated, 
				// this button should disappear
				// and the the remove button should appear
				add.setVisible(false);
				remove.setVisible(false);
			}
			
		});
    	panel.setWidget(0, 2, list);
		initWidget(panel);
    }
	

	public void addUserObject(VariableSerializable var) {
		userObject.add(var);
	}
	
	public VariableSerializable getUserObject(int index) {
		if ( index >=0 && index < userObject.size() - 1 ) {
			return userObject.get(index);
		} else {
			return null;
		}
	}
	public void addChangeHandler(ChangeHandler handler) {
		list.addChangeHandler(handler);
	}
	public void addItem(String name, String value) {
		list.addItem(name, value);
		if ( list.getItemCount() == 2 ) {
			add.setVisible(true);
		}
	}
	public void clear() {
		userObject.clear();
		list.clear();
	}
	public int getSelectedIndex() {
		return list.getSelectedIndex();
	}
	public void setSelectedIndex(int index) {
		list.setSelectedIndex(index);
	}
	public void addAddButtonClickHandler(ClickHandler handler) {
		add.addClickHandler(handler);
	}
	public void addRemoveButtonClickHandler(ClickHandler handler) {
		remove.addClickHandler(handler);
	}
	public int getItemCount() {
		return userObject.size();
	}
	public String getName(int i) {
		return list.getItemText(i);
	}
	public String getValue(int i) {
		return list.getValue(i);
	}
	public void setAddButtonVisible(boolean v) {
		add.setVisible(v);
	}
	public void setRemoveButtonVisible(boolean v) {
		remove.setVisible(v);
	}
	public void removeVectors() {
		vectors.clear();
		List<Integer> r = new ArrayList<Integer>();
		for(int i = 0; i < list.getItemCount(); i++ ) {
			VariableSerializable v = userObject.get(i);
			if ( v.isVector() ) {
				vectors.add(v);
				r.add(i);
			}
		}
		for (Iterator rIt = r.iterator(); rIt.hasNext();) {
			Integer index = (Integer) rIt.next();
			list.removeItem(index.intValue());
		}
		for (int i = 0; i < vectors.size(); i++ ) {
			VariableSerializable v = vectors.get(i);
			userObject.remove(v);
		}
	}
	public void addVectors() {
		for (int i = 0; i < vectors.size(); i++ ) {
			VariableSerializable v = vectors.get(i);
			userObject.add(v);
			list.addItem(v.getName(), v.getID());
		}
	}
}
