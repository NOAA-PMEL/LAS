package gov.noaa.pmel.tmap.las.client.laswidget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import gov.noaa.pmel.tmap.las.client.ClientFactory;
import gov.noaa.pmel.tmap.las.client.event.RemoveConstraintEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class TrajectoryOuterSequenceConstraintPanel extends Composite {
    ClientFactory clientFactory = GWT.create(ClientFactory.class);
    String name;
    String value;
    Set<String> activeConstraints = new TreeSet<String>();
    EventBus eventBus = clientFactory.getEventBus();
    VerticalPanel main = new VerticalPanel();
    HorizontalPanel titlePanel = new HorizontalPanel();
    PushButton close = new PushButton("x", new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
            eventBus.fireEvent(new RemoveConstraintEvent(name));        
        }

    });
    Label title = new Label();
    FlowPanel constraints = new FlowPanel();

    public TrajectoryOuterSequenceConstraintPanel(String name, String value) {
        this.name = name;       
        this.value = value;
        titlePanel.add(close);
        Label nameLabel = new Label(name);
        nameLabel.addStyleDependentName("PADDING");
        titlePanel.add(nameLabel);
        main.add(titlePanel);
        main.add(constraints);
        initWidget(main);
    }
    public void addConstraint(String con) {
        if ( !activeConstraints.contains(con) ) {
            activeConstraints.add(con);
            setConstraints();
        }
    }
    public void removeConstraint(String con) {
        activeConstraints.remove(con);
        setConstraints();
    }
    private void setConstraints() {
        constraints.clear();
        for (Iterator conIt = activeConstraints.iterator(); conIt.hasNext();) {
            String c = (String) conIt.next();
            Anchor remove = new Anchor("(x) "+c);
            remove.addStyleDependentName("PADDING");
            final String fname = c;
            remove.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    removeConstraint(fname);
                }

            });
            constraints.add(remove);
        }
    }
    public String getName() {
        return name;
    }
    public String getValue() {
        return value;
    }
    public int getConstraintCount() {
        if ( activeConstraints != null ) {
            return activeConstraints.size();
        } else {
            return 0;
        }
    }
    public String getConstraintExpression() {
        StringBuilder constraint = new StringBuilder("\"");
        for (Iterator acIt = activeConstraints.iterator(); acIt.hasNext();) {
            String c = (String) acIt.next();
            constraint.append(c);
            if ( acIt.hasNext() ) constraint.append("|");
        }
        constraint.append("\"");
        return constraint.toString();
    }
}
