package gov.noaa.pmel.tmap.las.client.laswidget;


import gov.noaa.pmel.tmap.las.client.RPCServiceAsync;
import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.serializable.DatasetSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class DatasetWidget extends Tree implements TreeListener {
    TreeItem currentlySelected = null;
    RPCServiceAsync catService;
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
			catService.getCategories(cat.getID(), categoryCallback);
		}
	}
	/**
	 * Set up the tree and the associated RPC.
	 */
	public void init(RPCServiceAsync catService) {
		this.catService = catService;
		catService.getCategories(null, categoryCallback);
		addTreeListener(this);	
	}
	AsyncCallback categoryCallback = new AsyncCallback() {
		public void onSuccess(Object result) {
			CategorySerializable[] cats = (CategorySerializable[]) result;
			if ( cats != null && cats.length > 0 ) {
				if ( currentlySelected == null ) {
					for (int i = 0; i < cats.length; i++) {
						CategorySerializable cat = cats[i];
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
							// Must have variable children and there should be only 1, but we're not checking :-)
							DatasetSerializable ds = cat.getDatasetSerializable();
							VariableSerializable[] vars = ds.getVariablesSerializable();
							TreeItem item = currentlySelected.getChild(0);
							item.setText(vars[0].getName());
							item.setUserObject(vars[0]);
							for (int j = 1; j < vars.length; j++) {
								item = new TreeItem();
								item.setText(vars[j].getName());
								item.setUserObject(vars[j]);
								currentlySelected.addItem(item);
							}
						}
					}
				}
			}
		}
        
		public void onFailure(Throwable caught) {
			Window.alert("Messed up with "+caught.getMessage());
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
}
