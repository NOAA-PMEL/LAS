package gov.noaa.pmel.tmap.las.client.activity;

//import gov.noaa.pmel.tmap.las.client.laswidget.%placeName%;
import gov.noaa.pmel.tmap.las.client.ClientFactory;
import gov.noaa.pmel.tmap.las.client.laswidget.VariableControls;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * Activities are started and stopped by an ActivityManager associated with a
 * container Widget.
 */
public class VariableControlsActivity extends AbstractActivity implements VariableControls.Presenter {

    private ClientFactory clientFactory;
    private String name;

    // Don't allow this Activity to be constructed without a name ID set
    @SuppressWarnings("unused")
    private VariableControlsActivity() {
        clientFactory = GWT.create(ClientFactory.class);
    }

    public VariableControlsActivity(ClientFactory clientFactory, String outputPanelID) {
        this.clientFactory = clientFactory;
        this.name = outputPanelID;
    }

    /**
     * @return initialized {@link VariableControls} view
     */
    public VariableControls init(String id) {
        VariableControls view = clientFactory.getVariableControls(id);
        view.setPresenter(this);
        return view;
    }

    @Override
    public String mayStop() {
        return "Please hold on. This activity is stopping.";
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        VariableControls view = init(name);
        containerWidget.setWidget(view.asWidget());
    }

    @Override
    public void update(VariableControls view) {
        // TODO: Update view, probably because the variable has changed
//        view.getVariableMetadataView().setDSID(view.getVariable().getDSID()); //TODO: Is this overwriting categories in bread crumbs?
    }
}
