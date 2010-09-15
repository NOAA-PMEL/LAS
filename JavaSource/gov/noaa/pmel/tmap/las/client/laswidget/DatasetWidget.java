package gov.noaa.pmel.tmap.las.client.laswidget;


import gov.noaa.pmel.tmap.las.client.RPCServiceAsync;
import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.serializable.DatasetSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.util.Util;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class DatasetWidget extends Tree implements TreeListener {
    TreeItem currentlySelected = null;
    String openid;
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.TreeListener#onTreeItemSelected(com.google.gwt.user.client.ui.TreeItem)
	 */
	public void onTreeItemSelected(TreeItem item) {
		currentlySelected = item;
		Object u = item.getUserObject();
		if ( u instanceof VariableSerializable ) {
			VariableSerializable v = (VariableSerializable) u;
		}
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.TreeListener#onTreeItemStateChanged(com.google.gwt.user.client.ui.TreeItem)
	 */
	public void onTreeItemStateChanged(TreeItem item) {
		currentlySelected = item;
		if ( item.getChild(0).getText().equals("Loading...") ) {
			CategorySerializable cat = (CategorySerializable) item.getUserObject();
			Util.getRPCService().getCategories(cat.getID(), categoryCallback);
		}
	}
	/**
	 * Set up the tree and the associated RPC.
	 */
	public void init() {
		Util.getRPCService().getCategories(null, categoryCallback);
		addTreeListener(this);	
	}
	AsyncCallback categoryCallback = new AsyncCallback() {
		public void onSuccess(Object result) {
			CategorySerializable[] cats = (CategorySerializable[]) result;
			if ( cats != null && cats.length > 0 ) {
				if ( currentlySelected == null ) {
					for (int i = 0; i < cats.length; i++) {
						CategorySerializable cat = cats[i];
						if ( openid != null ) {
							String auth_url = cat.getAttributes().get("remote_las");
							if ( auth_url != null ) {
								auth_url = auth_url + "?openid="+openid;
								Frame authFrame = new Frame(auth_url);
								RootPanel.get(Constants.AUTH_FRAME_ID).add(authFrame);
							}
						}
						String name = cat.getName();
						TreeItem item = new TreeItem();
						item.addItem("Loading...");
						item.setText(name);
						item.setUserObject(cat);
						addItem(item);
					}
				} else {
					for (int i = 0; i < cats.length; i++) {
						CategorySerializable cat = cats[i];
						if ( cat.isCategoryChildren() ) {
							String name = cat.getName();
							TreeItem item;
							if ( i == 0 ) {
							    item = currentlySelected.getChild(0);
							} else {
								item = new TreeItem();
							}
							item.addItem("Loading...");
							item.setText(name);
							item.setUserObject(cat);
							if ( i > 0 ) {
								currentlySelected.addItem(item);
							}
						} else {
							// Must have variable children...
							TreeItem item = currentlySelected.getChild(0);
							if ( cat.hasMultipleDatasets() ) {
								DatasetSerializable[] dses = cat.getDatasetSerializableArray();
								DatasetSerializable ds = dses[0];
								VariableSerializable[] vars = ds.getVariablesSerializable();
								loadItem(item, vars);
								for (int j = 1; j < dses.length; j++) {
									ds = dses[j];
									vars = ds.getVariablesSerializable();
									loadItem(vars);
								}
							} else {
								DatasetSerializable ds = cat.getDatasetSerializable();
								VariableSerializable[] vars = ds.getVariablesSerializable();							
								loadItem(item, vars);
							}
						}
					}
				}
			}
		}
        
		public void onFailure(Throwable caught) {
			Window.alert("Server Request Failed: "+caught.getMessage());
		}
		
		private void loadItem(TreeItem item, VariableSerializable[] vars ) {			
			item.setText(vars[0].getName());
			item.setUserObject(vars[0]);
			for (int j = 1; j < vars.length; j++) {
				item = new TreeItem();
				item.setText(vars[j].getName());
				item.setUserObject(vars[j]);
				currentlySelected.addItem(item);
			}
		}
		private void loadItem(VariableSerializable[] vars) {
			for (int j = 0; j < vars.length; j++) {
				TreeItem item = new TreeItem();
				item.setText(vars[j].getName());
				item.setUserObject(vars[j]);
			    currentlySelected.addItem(item);
			}
		}
	};
	/* Work around for focus on tree in scroll panel scrolling to the top bug.  #369
	public void onBrowserEvent(Event event) {
		if (DOM.eventGetType(event) == Event.ONCLICK) return;
		super.onBrowserEvent(event);
	}
	*/
	public Object getCurrentlySelected() {
		return currentlySelected.getUserObject();
	}

	public void setOpenID(String openid) {
		this.openid = openid;
	}
}
