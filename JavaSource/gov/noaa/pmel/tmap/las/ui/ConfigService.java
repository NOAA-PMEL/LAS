package gov.noaa.pmel.tmap.las.ui;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

public class ConfigService extends Action {
	public static void sendError(HttpServletResponse response, String container, String format, String message) {
		PrintWriter respout;
		String xml = "<"+container+"><status>error</status><error>"+message+"</error></"+container+">";
		try {
			if ( format.equals("xml") ) {
				respout = response.getWriter();
			} else {
				respout = response.getWriter();
				JSONObject json_response = XML.toJSONObject(xml);
				json_response.write(respout);
			}
		} catch (IOException e) {
			// At this point we're toast and no error message will be generated.
			e.printStackTrace();
		} catch (JSONException e) {
			// ditto
			e.printStackTrace();
		}
	}

}
