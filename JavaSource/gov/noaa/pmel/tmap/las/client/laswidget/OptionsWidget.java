/**
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development. 
 */
package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.RPCServiceAsync;
import gov.noaa.pmel.tmap.las.client.serializable.OptionSerializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author rhs
 *
 */
public class OptionsWidget extends VerticalPanel {
	RPCServiceAsync optionsService;
	OptionSerializable[] options;
	Button ok = new Button("OK");
	Button cancel = new Button("Cancel");
	Grid layout_grid = new Grid(1,2);
	List<Widget> widgets = new ArrayList<Widget>();
	Map<String, String> callbackState = null;
	public OptionsWidget() {
		layout_grid.setWidget(0, 0, ok);
		layout_grid.setWidget(0, 1, cancel);
	}
	public OptionsWidget(RPCServiceAsync rpcService, String opID) {
		layout_grid.setWidget(0, 0, ok);
		layout_grid.setWidget(0, 1, cancel);
		this.optionsService = rpcService;
		optionsService.getOptions(opID, optionsCallback);	
	}
	public OptionsWidget(RPCServiceAsync rpcService, String optionID, ClickListener okListener, ClickListener cancelListener) {
		layout_grid.setWidget(0, 0, ok);
		layout_grid.setWidget(0, 1, cancel);
		ok.addClickListener(okListener);
		cancel.addClickListener(cancelListener);
		this.optionsService = rpcService;
		optionsService.getOptions(optionID, optionsCallback);	
	}
	public OptionsWidget(ClickListener listener) {
		ok.addClickListener(listener);
		cancel.addClickListener(listener);
		layout_grid.setWidget(0, 0, ok);
		layout_grid.setWidget(0, 1, cancel);
	}
	public void setOptions(RPCServiceAsync rpcService, String opID) {
		this.optionsService = rpcService;
		
	}
	public void setOptions(String id) {
		callbackState = null;
		if ( id != null && !id.equals("") ) {
		    optionsService.getOptions(id, optionsCallback);
		}
	}
	public void setOptions(String id, Map<String, String> options ) {
		callbackState = options;
		if ( id != null && !id.equals("") ) {
		    optionsService.getOptions(id, optionsCallback);
		}
	}
	AsyncCallback optionsCallback = new AsyncCallback() {
		public void onSuccess(Object result) {
			options = (OptionSerializable[]) result;
			clear();
			setOptions(options);
			if ( callbackState != null ) {
				restore(callbackState);
			}
		}
		public void onFailure(Throwable e) {
			Window.alert(e.toString());
		}
	};
	public void restore(Map<String, String> state) {
		for (Iterator widIt = widgets.iterator(); widIt.hasNext();) {
			Widget w = (Widget) widIt.next();
			if ( w instanceof TextBox ) {
				TextBox t = (TextBox) w;
				String value = state.get(t.getName());
				if ( value != null && !value.equals("") ) {
					t.setText(value);
				}
				if ( value != null && value.equals("reset") ) {
					t.setText("");
				}
			} else if ( w instanceof ListBox ) {
				ListBox l = (ListBox) w;
				String value = state.get(l.getName());
				if (value != null && !value.equals("") ) {
					int count = l.getItemCount();
					for (int i = 0; i < count; i++ ) {
						String item_value = l.getValue(i);
						if ( item_value.equals(value) ) {
							l.setSelectedIndex(i);
						}
					}
				}
			}
		}
	}
	public Map<String, String> getState() {
		Map<String, String> state = new HashMap<String, String>();
		for (Iterator widIt = widgets.iterator(); widIt.hasNext();) {
			Widget w = (Widget) widIt.next();
			if ( w instanceof TextBox ) {
				TextBox t = (TextBox) w;
				if ( t.getText() != null && !t.getText().equals("") ) {
					state.put(t.getName(), t.getText());
				}
			} else if ( w instanceof ListBox ) {
				ListBox l = (ListBox) w;
				state.put(l.getName(), l.getValue(l.getSelectedIndex()));
			}
		}
		return state;
	}
	public void addOkClickListner(ClickListener optionsOkListener) {
		ok.addClickListener(optionsOkListener);
	}
	public void setOptions(OptionSerializable[] op) {
		options = op;
		for (int i = 0; i < options.length; i++) {
			OptionSerializable opt = op[i];
			Button help = new Button("help");
			final String help_html = opt.getHelp();
			help.addClickListener(new ClickListener() {

				public void onClick(Widget help) {
					PopupPanel helpup = new PopupPanel(true);
					HTML html = new HTML();
					html.setHTML(help_html);
					helpup.add(html);
					helpup.center();
				}
				
			});
			Grid option_layout = new Grid(1,2);
			option_layout.setWidget(0, 0, help);
			
			if ( opt.getType().equals("textfield") ) {
				Grid box = new Grid(1,2);
				Label label = new Label(opt.getTitle());
				TextBox textbox = new TextBox();
				textbox.setName(opt.getName());
				box.setWidget(0, 0, label);
				box.setWidget(0, 1, textbox);	
				widgets.add(textbox);
				option_layout.setWidget(0, 1, box);
			} else {
				Label label = new Label(opt.getTitle());
				ListBox menu = new ListBox();
				Grid box = new Grid(1,2);
				Map<String, String> items = opt.getMenu();
				Vector keys = new Vector(items.keySet());
				keys.remove("default");
				keys.remove("Default");
				Collections.sort(keys);
				menu.addItem("Default", "default");
				for (Iterator itemIt = keys.iterator(); itemIt.hasNext();) {
					String name = (String) itemIt.next();
					String value = (String) items.get(name);
					menu.addItem(name, value);
				}
				menu.setName(opt.getName());
				box.setWidget(0, 0, label);
				box.setWidget(0, 1, menu);
				widgets.add(menu);
				option_layout.setWidget(0, 1, box);
			}
			add(option_layout);
		}
		add(layout_grid);	
	}
}
