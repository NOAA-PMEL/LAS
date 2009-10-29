package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.laswidget.Util;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class LASEntryPoint implements EntryPoint {
	RPCServiceAsync rpcService;
	String productServer;
	public void onModuleLoad() {
		rpcService = (RPCServiceAsync) GWT.create(RPCService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) rpcService;
		String rpcURL = "";
		String base_path = Util.getBaseURL();
		rpcURL = base_path + "/rpc";
		endpoint.setServiceEntryPoint(rpcURL);
		productServer = base_path + "/ProductServer.do";
	}
}
