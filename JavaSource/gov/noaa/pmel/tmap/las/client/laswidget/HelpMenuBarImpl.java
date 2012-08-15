package gov.noaa.pmel.tmap.las.client.laswidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Widget;

/**
 * Implementation of {@link HelpMenuBar}.
 */
public class HelpMenuBarImpl extends Composite implements HelpMenuBar {

    interface Binder extends UiBinder<Widget, HelpMenuBarImpl> {
    }

    private static final Binder binder = GWT.create(Binder.class);
    @UiField
    MenuItem aboutItem;
    @UiField
    MenuItem helpMenu;
    @UiField
    MenuBar helpMenuBar;
    @UiField
    MenuBar helpSubMenuBar;
    private Presenter listener;

    @UiField
    MenuItem onlineDocsItem;

    @UiField
    MenuItem videoTutorialsItem;

    public HelpMenuBarImpl() {
        initWidget(binder.createAndBindUi(this));
        helpMenu.ensureDebugId("helpMenu");
    }

    /**
     * @return the aboutItem
     */
    @Override
    public MenuItem getAboutItem() {
        return aboutItem;
    }

    /**
     * @return the onlineDocsItem
     */
    @Override
    public MenuItem getOnlineDocsItem() {
        return onlineDocsItem;
    }

    /**
     * @return the videoTutorialsItem
     */
    @Override
    public MenuItem getVideoTutorialsItem() {
        return videoTutorialsItem;
    }

    @Override
    public void setName(String name) {
        // button.setHTML(name);
        helpMenu.setText(name);
    }

    @Override
    public void setPresenter(Presenter listener) {
        this.listener = listener;
    }

    /**
     * @see com.google.gwt.user.client.ui.UIObject#setSize(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void setSize(String width, String height) {
        super.setSize(width, height);
    }
}
