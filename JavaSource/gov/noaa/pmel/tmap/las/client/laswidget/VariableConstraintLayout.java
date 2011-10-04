package gov.noaa.pmel.tmap.las.client.laswidget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class VariableConstraintLayout extends Composite {
	FlexTable layout = new FlexTable();
	HorizontalPanel topRow = new HorizontalPanel();
	HelpPanel help = new HelpPanel();
	List<VariableConstraintWidget> widgets = new ArrayList<VariableConstraintWidget>();
	public VariableConstraintLayout(String title) {
		help.setPopupWidth("550px");
    	help.setPopupHeight("550px");
    	help.setHelpURL("../css/constraint_help.html");
		topRow.add(help);
    	topRow.add(new HTML("<b>&nbsp;&nbsp;"+title+"</b>"));
		layout.getFlexCellFormatter().setColSpan(0, 0, 5);
		layout.setWidget(0, 0, topRow);
		initWidget(layout);
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
}
