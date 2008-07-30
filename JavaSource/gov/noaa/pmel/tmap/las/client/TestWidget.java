package gov.noaa.pmel.tmap.las.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class TestWidget implements EntryPoint {
    RPCServiceAsync rpcService;
    public void onModuleLoad() {
        rpcService = (RPCServiceAsync) GWT.create(RPCService.class);
        ServiceDefTarget endpoint = (ServiceDefTarget) rpcService;
        String moduleRelativeURL = GWT.getModuleBaseURL();
        String moduleName = GWT.getModuleName();
        moduleRelativeURL = moduleRelativeURL.substring(0,moduleRelativeURL.indexOf(moduleName)-1);
        moduleRelativeURL = moduleRelativeURL.substring(0,moduleRelativeURL.lastIndexOf("/")+1);
        String rpcURL = moduleRelativeURL + "rpc";
        endpoint.setServiceEntryPoint(rpcURL);
    }
}
