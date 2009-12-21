package gov.noaa.pmel.tmap.las.client;

import java.util.List;
import java.util.Map;

import gov.noaa.pmel.tmap.las.client.util.Util;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class LASEntryPoint implements EntryPoint {
	RPCServiceAsync rpcService;
	String productServer;
	public void onModuleLoad() {
		rpcService = (RPCServiceAsync) GWT.create(RPCService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) rpcService;
		String rpcURL = "";
		String base_path = Util.getBaseURL();
		rpcURL = base_path + "rpc";
		endpoint.setServiceEntryPoint(rpcURL);
		productServer = base_path + "ProductServer.do";
	}
	public String getParameterString(String name) {
		Map<String, List<String>> parameters = Window.Location.getParameterMap();			
		List param = parameters.get(name);
		if ( param != null ) {
			return (String) param.get(0);
		}
		return null;
	}
}
