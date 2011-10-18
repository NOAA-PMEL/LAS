package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class VariableConstraintLayout extends Composite {
	VerticalPanel mainPanel = new VerticalPanel();
	HorizontalPanel topRow = new HorizontalPanel();
	FlexTable layout = new FlexTable();
	HelpPanel help = new HelpPanel();
	VariableListBox constraintVariables = new VariableListBox();
	List<VariableConstraintWidget> widgets = new ArrayList<VariableConstraintWidget>();
	public VariableConstraintLayout(String title, boolean show_variables) {
		help.setPopupWidth("550px");
		help.setPopupHeight("550px");
		help.setHelpURL("../css/constraint_help.html");
		topRow.add(help);
		topRow.add(new HTML("<b>&nbsp;&nbsp;"+title+"&nbsp;&nbsp;</b>"));		
		if ( show_variables ) {
			topRow.add(constraintVariables);
		}
		mainPanel.add(topRow);
		mainPanel.add(layout);
		initWidget(mainPanel);
	}
	public void addWidget(VariableConstraintWidget widget) {
		widgets.add(widget);
		int row = widgets.size();
		int index = row - 1;
		widget.getApply().getElement().setId("apply-"+index);
		widget.getMaxTextBox().getElement().setId("max-"+index);
		widget.getMinTextBox().getElement().setId("min-"+index);
		layout.setWidget(row, 0, widget.getApply());
		layout.setWidget(row, 1, widget.getMinTextBox());
		layout.setWidget(row, 2, widget.getLabel());
		layout.setWidget(row, 3, widget.getMaxTextBox());
		if ( widget.isRemoveable() ) {
			layout.setWidget(row, 4, widget.getRemoveButton());
		}
	}
	public void setApply(int index, boolean value) {
		widgets.get(index).setApply(value);
	}
	public List<VariableConstraintWidget> getWidgets() {
		return widgets;
	}
	public void setWidgets(List<VariableConstraintWidget> list) {
		widgets = list;
	}
	public void removeWidget(VariableConstraintWidget vcw) {
		int index = -1;
		int i = 0;
		for (Iterator vcwIt = widgets.iterator(); vcwIt.hasNext();) {
			VariableConstraintWidget vc = (VariableConstraintWidget) vcwIt.next();
			if ( vc.getVariable().getID().equals(vcw.getVariable().getID())) index = i;
			i++;
		}
		if ( index >= 0 ) {
			layout.removeRow(index+1);
			widgets.remove(index);
		}
	}
	public void addItem(VariableSerializable var) {
		constraintVariables.addItem(var);
	}
	public void setHeader(String header) {
		constraintVariables.setHeader(header);
	}
	public void restore() {
		constraintVariables.restore();
	}
	public void removeItem(VariableSerializable variable) {
		constraintVariables.removeItem(variable);
	}
	public void addChangeHandler(ChangeHandler changeHandler) {
		constraintVariables.addChangeHandler(changeHandler);
	}
	public int getSelectedIndex() {
		return constraintVariables.getSelectedIndex();
	}
	public String getValue(int index) {
		return constraintVariables.getValue(index);
	}
}