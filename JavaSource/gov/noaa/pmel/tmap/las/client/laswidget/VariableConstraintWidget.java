package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class VariableConstraintWidget extends Composite {
	VerticalPanel bucket = new VerticalPanel();
	VariableSerializable xVariable;
	CheckBox xApply = new CheckBox("Apply");
	Label xLabel = new Label("variable");
	TextBox minTextBox = new TextBox();
	TextBox maxTextBox = new TextBox();
	boolean removeable = false;
	PushButton xRemove = new PushButton("Remove");
	boolean active = true;
	public VariableConstraintWidget(boolean removable) {
		this.removeable = removable;
		xRemove.addStyleDependentName("SMALLER");
		bucket.add(xApply);
		bucket.add(xLabel);
		bucket.add(minTextBox);
		bucket.add(maxTextBox);
		if ( removeable ) {
			bucket.add(xRemove);
		}
		initWidget(bucket);
	}
	public VariableConstraintWidget() {
		xRemove.addStyleDependentName("SMALLER");
		bucket.add(xApply);
		bucket.add(xLabel);
		bucket.add(minTextBox);
		bucket.add(maxTextBox);
		if ( removeable ) {
			bucket.add(xRemove);
		}
		initWidget(bucket);
	}
	public void setConstraint(String min, String max) {
		minTextBox.setValue(min);
		maxTextBox.setValue(max);
	}
	public void setVariable(VariableSerializable var) {
		this.xVariable = var;
		xLabel.setText("< "+xVariable.getName()+" <=");
		xRemove.getElement().setId("other-"+var.getID());
	}
    /**
     * Add a change handler to both the min and max text boxes of this constraint widget.
     * @param constraintChange
     */
	public void addChangeHandler(ChangeHandler constraintChange) {
		minTextBox.addChangeHandler(constraintChange);
		maxTextBox.addChangeHandler(constraintChange);
	}
	public void addApplyHandler(ClickHandler applyHandler) {
		xApply.addClickHandler(applyHandler);
	}
	public String getMin() {
		return minTextBox.getText();
	}
	public String getMax() {
		return maxTextBox.getText();
	}
	public void setMin(String min) {
		minTextBox.setText(min);
	}
	public void setMax(String max) {
		maxTextBox.setText(max);
	}
	public CheckBox getApply() {
		return xApply;
	}
	public Label getLabel() {
		return xLabel;
	}
	public TextBox getMinTextBox() {
		return minTextBox;
	}
	public TextBox getMaxTextBox() {
		return maxTextBox;
	}
	public PushButton getRemoveButton() {
		return xRemove;
	}
	public void setApply(boolean value) {
		xApply.setValue(value);
	}
	public VariableSerializable getVariable() {
		return xVariable;
	}
	public boolean isRemoveable() {
		return removeable;
	}
	public void addRemoveHandler(ClickHandler clickHandler) {
		xRemove.addClickHandler(clickHandler);	
	}
	public void setActive(boolean a) {
		this.active = a;
	}
	public boolean isActive() {
		return this.active;
	}	
	public void setVisible(boolean visible) {
		xApply.setVisible(visible);
		xLabel.setVisible(visible);
		minTextBox.setVisible(visible);
		maxTextBox.setVisible(visible);
	    xRemove.setVisible(visible);
	}
}
