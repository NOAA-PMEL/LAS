package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.ClientFactory;
import gov.noaa.pmel.tmap.las.client.event.MapChangeEvent;
import gov.noaa.pmel.tmap.las.client.event.RemoveConstraintEvent;
import gov.noaa.pmel.tmap.las.client.event.WidgetSelectionChangeEvent;
import gov.noaa.pmel.tmap.las.client.serializable.ConstraintSerializable;
import gov.noaa.pmel.tmap.las.client.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class TrajectoryOuterSequenceConstraint extends Composite {
    
    VerticalPanel mainPanel = new VerticalPanel();
    
    VerticalPanel activeConstraints = new VerticalPanel();
    
    HorizontalPanel listPanel = new HorizontalPanel();
    
    ListBox outerSequenceVariable = new ListBox();
    ListBox outerSequenceVariableValue = new ListBox();
    
    
    ClientFactory clientFactory = GWT.create(ClientFactory.class);
    EventBus eventBus = clientFactory.getEventBus();
    
    String dsid = "";
    String varid = "";
    
    private static String CREATE = "Create a constraint on ...";
    
    
    // Keep constraints on x, y, z and t to limit the selections to the current region.
    String tlo;
    String thi;
    
    String zlo;
    String zhi;
    
    Double xlo;
    Double xhi;
    Double yhi;
    Double ylo;
    
    private boolean active = false;
    
    public TrajectoryOuterSequenceConstraint() {
        mainPanel.add(new Label("The choices in the second menu are restricted to those values which are available given the current X, Y, Z and T ranges."));
        mainPanel.add(activeConstraints);
        outerSequenceVariable.addItem(CREATE);
        outerSequenceVariableValue.addItem(Constants.PICK);
        outerSequenceVariableValue.addItem(Constants.APPEAR);
        listPanel.add(outerSequenceVariable);
        listPanel.add(outerSequenceVariableValue);
        outerSequenceVariableValue.setVisibleItemCount(30);
        outerSequenceVariableValue.addChangeHandler(outerSequenceVariableValueChangeHandler);
        outerSequenceVariable.addChangeHandler(outerSequenceChangeHandler);
        mainPanel.add(listPanel);
        eventBus.addHandler(WidgetSelectionChangeEvent.TYPE, new WidgetSelectionChangeEvent.Handler() {

            @Override
            public void onAxisSelectionChange(WidgetSelectionChangeEvent event) {
                Object source = event.getSource();
                if ( source instanceof DateTimeWidget ) {
                    DateTimeWidget changed = (DateTimeWidget) source;
                    tlo = changed.getISODateLo();
                    thi = changed.getISODateHi();
                } else if ( source instanceof AxisWidget ) {
                    AxisWidget z = (AxisWidget) source;
                    zlo = z.getLo();
                    zhi = z.getHi();
                }
                outerSequenceVariableValue.clear();
                int index = outerSequenceVariable.getSelectedIndex();
                if ( index > 0 ) {
                    outerSequenceVariableValue.addItem(Constants.LOADING);
                    String variableName = outerSequenceVariable.getItemText(index);
                    Util.getRPCService().getERDDAPOuterSequenceValues(dsid, varid, variableName, getXYZT(), outerSequenceValuesCallback);
                } else {
                    outerSequenceVariableValue.addItem(Constants.PICK);
                    outerSequenceVariableValue.addItem(Constants.APPEAR);
                }
            }
        });
        eventBus.addHandler(MapChangeEvent.TYPE, new MapChangeEvent.Handler() {

            @Override
            public void onMapSelectionChange(MapChangeEvent event) {
                xlo = event.getXlo();
                xhi = event.getXhi();
                ylo = event.getYlo();
                yhi = event.getYhi();
                outerSequenceVariableValue.clear();
                int index = outerSequenceVariable.getSelectedIndex();
                if ( index > 0 ) {
                    outerSequenceVariableValue.addItem(Constants.LOADING);
                    String variableName = outerSequenceVariable.getItemText(index);
                    Util.getRPCService().getERDDAPOuterSequenceValues(dsid, varid, variableName, getXYZT(), outerSequenceValuesCallback);
                } else {
                    outerSequenceVariableValue.addItem(Constants.PICK);
                    outerSequenceVariableValue.addItem(Constants.APPEAR);
                }
            }
            
        });
        eventBus.addHandler(RemoveConstraintEvent.TYPE, new RemoveConstraintEvent.Handler() {
            
            @Override
            public void onRemove(RemoveConstraintEvent event) {
                String name = event.getName();
                TrajectoryOuterSequenceConstraintPanel constraint = getTrajectoryOuterSequenceConstraintPanel(name);
                for (int i = 0; i < activeConstraints.getWidgetCount(); i++) {
                    TrajectoryOuterSequenceConstraintPanel c = (TrajectoryOuterSequenceConstraintPanel) activeConstraints.getWidget(i);
                    if ( c.getName().equals(name)) {
                        constraint = c;
                    }
                }
                if ( constraint != null ) {
                    activeConstraints.remove(constraint);
                    outerSequenceVariableValue.clear();
                    outerSequenceVariable.setSelectedIndex(0);
                }
            }

            
        });
        initWidget(mainPanel);
        
    }
    
    private TrajectoryOuterSequenceConstraintPanel getTrajectoryOuterSequenceConstraintPanel(String name) {
        TrajectoryOuterSequenceConstraintPanel constraint = null;
        for (int i = 0; i < activeConstraints.getWidgetCount(); i++) {
            TrajectoryOuterSequenceConstraintPanel c = (TrajectoryOuterSequenceConstraintPanel) activeConstraints.getWidget(i);
            if ( c.getName().equals(name)) {
                constraint = c;
            }
        }
        return constraint;
    }
    public void init(String dsid, String varid) {
        this.dsid = dsid;
        this.varid = varid;
        Util.getRPCService().getERDDAPOuterSequenceVariables(dsid, varid, outerSequenceVariablesCallback);
        
    }
    
    
    AsyncCallback<Map<String, String>> outerSequenceVariablesCallback = new AsyncCallback<Map<String, String>> () {

        @Override
        public void onFailure(Throwable caught) {
            Window.alert("Failed to get variables for constraints.");
        }

        @Override
        public void onSuccess(Map<String,String> result) {
            for (Iterator resultIt = result.keySet().iterator(); resultIt.hasNext();) {
                String value = (String) resultIt.next();
                String long_name = result.get(value);
                outerSequenceVariable.addItem(value, long_name);
            }
        }
        
    };
    AsyncCallback<Map<String, String>> outerSequenceValuesCallback = new AsyncCallback<Map<String, String>> () {

        @Override
        public void onFailure(Throwable caught) {
            Window.alert("Failed to get variable values for constraint.");
        }

        @Override
        public void onSuccess(Map<String, String> result) {
            outerSequenceVariableValue.clear();
            outerSequenceVariableValue.setVisibleItemCount(Math.min(result.keySet().size(), 30));
            for (Iterator resultIt = result.keySet().iterator(); resultIt.hasNext();) {
                String value = (String) resultIt.next();
                String long_name = result.get(value);
                outerSequenceVariableValue.addItem(value, long_name);
            }
            
        }
        
    };
    ChangeHandler outerSequenceVariableValueChangeHandler = new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent event) {
            String longname = outerSequenceVariable.getValue(outerSequenceVariable.getSelectedIndex());
            String value = outerSequenceVariable.getItemText(outerSequenceVariable.getSelectedIndex());

            int index = outerSequenceVariableValue.getSelectedIndex();
            if ( index >= 0 ) {

                String constraintValue = outerSequenceVariableValue.getValue(index);
                if ( constraintValue != null ) {
                    TrajectoryOuterSequenceConstraintPanel constraint = getTrajectoryOuterSequenceConstraintPanel(longname);
                    if ( constraint == null ) {
                        constraint = new TrajectoryOuterSequenceConstraintPanel(longname, value);
                        activeConstraints.add(constraint);
                        eventBus.fireEvent(new WidgetSelectionChangeEvent(false));
                    }
                    constraint.addConstraint(constraintValue);
                }
            }
        }
    };
    ChangeHandler outerSequenceChangeHandler = new ChangeHandler() {

        @Override
        public void onChange(ChangeEvent event) {
            removeEmptyConstraints();
            String variableName = outerSequenceVariable.getItemText(outerSequenceVariable.getSelectedIndex());
            String longname = outerSequenceVariable.getValue(outerSequenceVariable.getSelectedIndex());
            if ( variableName != null && !variableName.equals(CREATE) ) {

                TrajectoryOuterSequenceConstraintPanel p = getTrajectoryOuterSequenceConstraintPanel(longname);
                if ( p == null ) {
                    p = new TrajectoryOuterSequenceConstraintPanel(longname, variableName);
                    activeConstraints.add(p);
                }
                outerSequenceVariableValue.clear();
                outerSequenceVariableValue.addItem(Constants.LOADING);
                Util.getRPCService().getERDDAPOuterSequenceValues(dsid, varid, variableName, getXYZT(), outerSequenceValuesCallback);
            }
        }      
    };
    private Map<String, String> getXYZT() {
        Map<String, String> xyzt = new HashMap<String, String>();
        if ( xlo != null ) {
            xyzt.put("xlo", String.valueOf(xlo));
        }
        if ( xhi != null ) {
            xyzt.put("xhi", String.valueOf(xhi));
        }
        if ( ylo != null ) {
            xyzt.put("ylo", String.valueOf(ylo));
        }
        if ( yhi != null ) {
            xyzt.put("yhi", String.valueOf(yhi));
        }
        if ( zlo != null ) {
            xyzt.put("zlo", zlo);
        }
        if ( zhi != null ) {
            xyzt.put("zhi", zhi);
        }
        if ( tlo != null ) {
            xyzt.put("tlo", tlo);
        }
        if ( thi != null ) {
            xyzt.put("thi", thi);
        }
        return xyzt;
    }
    private void removeEmptyConstraints() {
        List<TrajectoryOuterSequenceConstraintPanel> remove = new ArrayList<TrajectoryOuterSequenceConstraintPanel>();
        for (int i = 0; i < activeConstraints.getWidgetCount(); i++) {
            TrajectoryOuterSequenceConstraintPanel c = (TrajectoryOuterSequenceConstraintPanel) activeConstraints.getWidget(i);
            if ( c.getConstraintCount() == 0 ) {
                remove.add(c);
            }
        }
        for (Iterator tPanelIt = remove.iterator(); tPanelIt.hasNext();) {
            TrajectoryOuterSequenceConstraintPanel trajectoryOuterSequenceConstraintPanel = (TrajectoryOuterSequenceConstraintPanel) tPanelIt.next();
            activeConstraints.remove(trajectoryOuterSequenceConstraintPanel);
        }
    }
    public List<ConstraintSerializable> getConstraints() {
        List<ConstraintSerializable> constraints = new ArrayList<ConstraintSerializable>();
        // The thing can exist, but not be active in the interface so it's state can be remembered and restored.
        // Only return what's in it if it's active.
        if ( isActive() ) {
            for (Iterator acIt = activeConstraints.iterator(); acIt.hasNext();) {
                TrajectoryOuterSequenceConstraintPanel toscp = (TrajectoryOuterSequenceConstraintPanel) acIt.next();
                if ( toscp.hasConstraints() ) {
                    ConstraintSerializable constraint = new ConstraintSerializable();
                    constraint.setId(toscp.getName());
                    constraint.setLhs(toscp.getValue());
                    constraint.setOp("like");
                    constraint.setRhs(toscp.getConstraintExpression());
                    constraints.add(constraint);
                }
            }
        }
        return constraints;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }

    public String getTlo() {
        return tlo;
    }

    public void setTlo(String tlo) {
        this.tlo = tlo;
    }

    public String getThi() {
        return thi;
    }

    public void setThi(String thi) {
        this.thi = thi;
    }

    public String getZlo() {
        return zlo;
    }

    public void setZlo(String zlo) {
        this.zlo = zlo;
    }

    public String getZhi() {
        return zhi;
    }

    public void setZhi(String zhi) {
        this.zhi = zhi;
    }

    public double getXlo() {
        return xlo;
    }

    public void setXlo(double xlo) {
        this.xlo = xlo;
    }

    public double getXhi() {
        return xhi;
    }

    public void setXhi(double xhi) {
        this.xhi = xhi;
    }

    public double getYhi() {
        return yhi;
    }

    public void setYhi(double yhi) {
        this.yhi = yhi;
    }

    public double getYlo() {
        return ylo;
    }

    public void setYlo(double ylo) {
        this.ylo = ylo;
    }
    
}
