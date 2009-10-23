package gov.noaa.pmel.tmap.las.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class LASEntryPoint implements EntryPoint {
	RPCServiceAsync rpcService;
	String productServer;
	public void onModuleLoad() {
		rpcService = (RPCServiceAsync) GWT.create(RPCService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) rpcService;
		String moduleRelativeURL = GWT.getModuleBaseURL();
		String moduleName = GWT.getModuleName();
		String rpcURL = "";
		int first_path_slash = moduleRelativeURL.indexOf("/", moduleRelativeURL.indexOf("://")+4);
		int second_path_slash = moduleRelativeURL.indexOf("/", first_path_slash+1);
		String base_path = moduleRelativeURL.substring(0, second_path_slash);
		rpcURL = base_path + "/rpc";
		endpoint.setServiceEntryPoint(rpcURL);
		productServer = base_path + "/ProductServer.do";
	}
}
