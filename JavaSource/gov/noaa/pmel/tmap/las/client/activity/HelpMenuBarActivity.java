package gov.noaa.pmel.tmap.las.client.activity;

//import gov.noaa.pmel.tmap.las.client.laswidget.%placeName%;
import gov.noaa.pmel.tmap.las.client.ClientFactory;
import gov.noaa.pmel.tmap.las.client.laswidget.HelpMenuBar;
import gov.noaa.pmel.tmap.las.client.util.Util;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * Activities are started and stopped by an ActivityManager associated with a
 * container Widget.
 */
public class HelpMenuBarActivity extends AbstractActivity implements HelpMenuBar.Presenter {

    private ClientFactory clientFactory;
    private String name = "Help";

    public HelpMenuBarActivity() {
        clientFactory = GWT.create(ClientFactory.class);
    }

    public HelpMenuBarActivity(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        HelpMenuBar view = clientFactory.getHelpMenuBar();
        view.setName(name);
        view.setPresenter(this);
        // Make menu commands
        Command aboutCmd = new Command() {
            @Override
            public void execute() {
                Window.open(Util.getProductServer(), "_blank", "scrollbars=1");
            }
        };
        view.getAboutItem().setCommand(aboutCmd);

        Command tutorialsCmd = new Command() {
            @Override
            public void execute() {
                Window.open("http://ferret.pmel.noaa.gov/LAS/documentation/introduction/using-the-las-user-interface#videos", "_blank", "scrollbars=1");
            }
        };
        view.getVideoTutorialsItem().setCommand(tutorialsCmd);

        Command docsCmd = new Command() {
            @Override
            public void execute() {
                Window.open("http://ferret.pmel.noaa.gov/LAS/home/documentation/introduction/using-the-las-user-interface/", "_blank", "scrollbars=1");
            }
        };
        view.getOnlineDocsItem().setCommand(docsCmd);
        view.setSize("100%", "100%");

        containerWidget.setWidget(view.asWidget());
    }

}
