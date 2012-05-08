package gov.noaa.pmel.tmap.las.client;


import gov.noaa.pmel.tmap.las.client.laswidget.HelpMenuBar;

import com.google.gwt.event.shared.EventBus;
//import com.google.gwt.place.shared.PlaceController;

/**
 * ClientFactory helpful to use a factory or dependency injection framework like GIN to obtain 
 * references to objects needed throughout your application like the {@link EventBus},
 * {@link PlaceController} and views.
 */
public interface ClientFactory {

	EventBus getEventBus();

//	PlaceController getPlaceController();
	public InteractiveDownloadDataView getView();

	HelpMenuBar getHelpMenuBar();
}
