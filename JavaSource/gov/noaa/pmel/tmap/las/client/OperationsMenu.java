package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;

public class OperationsMenu extends MenuBar {
	MenuBar animationMenu = new MenuBar(true);
	MenuBar compareMenu = new MenuBar(true);
	MenuBar googleEarthMenu = new MenuBar(true);
	boolean hasComparison = false;
	boolean hasAnimation = false;
	boolean hasGoogleEarth = false;
	public OperationsMenu() {
		super();
		addStyleName("las-MenuBar");
		setAnimationEnabled(false);
		animationMenu.setVisible(false);
		compareMenu.setVisible(false);
		googleEarthMenu.setVisible(false);
		addItem("Animation", animationMenu);
		addItem("Compare", compareMenu);
		addItem("Google Earth", googleEarthMenu);

	}

	public OperationsMenu(boolean vertical, MenuBarImages images) {
		super(vertical, images);
		// TODO Auto-generated constructor stub
	}

	public OperationsMenu(boolean vertical) {
		super(vertical);
		// TODO Auto-generated constructor stub
	}

	public OperationsMenu(MenuBarImages images) {
		super(images);
		// TODO Auto-generated constructor stub
	}

	public void setMenus(OperationSerializable[] ops) {
		hasComparison = false;
		hasAnimation = false;
		hasGoogleEarth = false;
		for (int i = 0; i < ops.length; i++) {
			OperationSerializable op = ops[i];
			String category = op.getAttributes().get("category").toLowerCase();
			List<String> views = op.getViews();
			for (Iterator viewIt = views.iterator(); viewIt.hasNext();) {
				String view = (String) viewIt.next();
				if ( category.equals("visualization")) {
					if ( op.getName().toLowerCase().contains("compar") ) {

						if ( !hasComparison ) {
							compareMenu.clearItems();
							compareMenu.setVisible(true);
							hasComparison = true;
						}
						MenuItem item = new MenuItem(op.getName()+" in "+view, processMenuSelection);
						compareMenu.addItem(item);

					}
				} else if ( category.contains("animation") ) {
					if ( !hasAnimation ) {
						animationMenu.clearItems();
						animationMenu.setVisible(true);
						hasAnimation = true;
					}
					MenuItem item = new MenuItem(op.getName()+" in "+view, processMenuSelection);
					animationMenu.addItem(item);
				} else if ( category.contains("globe") ) {
					if ( !hasGoogleEarth ) {
						googleEarthMenu.clearItems();
						googleEarthMenu.setVisible(true);
						hasGoogleEarth = true;
					}
					MenuItem item = new MenuItem(op.getName()+" in "+view, processMenuSelection);
					googleEarthMenu.addItem(item);
				}

			}

		}
	}

	Command processMenuSelection = new Command() {
		public void execute() {

		}
	};
}
