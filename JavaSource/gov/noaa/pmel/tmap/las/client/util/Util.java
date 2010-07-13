package gov.noaa.pmel.tmap.las.client.util;

import gov.noaa.pmel.tmap.las.client.RPCService;
import gov.noaa.pmel.tmap.las.client.RPCServiceAsync;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class Util {
	public static List<String> setOrthoAxes(String view, GridSerializable grid) {
		List<String> ortho = new ArrayList<String>();
		if ( !view.contains("t") && grid.hasT() ) {
			ortho.add("t");
		}
		if ( !view.contains("z") && grid.hasZ() ) {
			ortho.add("z");
		}
		if ( !view.contains("y") && grid.hasY() ) {
			ortho.add("y");
		}
		if ( !view.contains("x") && grid.hasX() ) {
			ortho.add("x");
		}
		return ortho;
	}
	public static HashMap<String, String> getTokenMap(String token) {
		String[] tokens = token.split(";");
		HashMap<String, String> tokenMap = new HashMap<String, String>();
		for( int i=0; i < tokens.length; i++ ) {
			String[] parts = tokens[i].split("=");
			String name = parts[0];
			String value = parts[1];
			if ( !value.contains("ferret_") ) {
			    tokenMap.put(name, value);
			}
		}
		return tokenMap;
	}
	public static HashMap<String, String> getOptionsMap(String token) {
		String[] tokens = token.split(";");
		HashMap<String, String> optionsMap = new HashMap<String, String>();
		for( int i=0; i < tokens.length; i++ ) {
			String[] parts = tokens[i].split("=");
			String name = parts[0];
			String value = parts[1];
			if ( name.contains("ferret_") ) {
			    optionsMap.put(name.substring(7, name.length()), value);
			}
		}
		return optionsMap;
	}
	public static RPCServiceAsync getRPCService() {
		RPCServiceAsync rpcService = (RPCServiceAsync) GWT.create(RPCService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) rpcService;
		String rpcURL = "";
		String base_path = URLUtil.getBaseURL();
		rpcURL = base_path + "rpc";
		endpoint.setServiceEntryPoint(rpcURL);
		return rpcService;
	}
	public static String getProductServer() {
		return URLUtil.getBaseURL() + "ProductServer.do";
	}
	public static String getParameterString(String name) {
		Map<String, List<String>> parameters = Window.Location.getParameterMap();			
		List param = parameters.get(name);
		if ( param != null ) {
			return (String) param.get(0);
		}
		return null;
	}
}
