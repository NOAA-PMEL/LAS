package gov.noaa.pmel.tmap.las.ui;

import gov.noaa.pmel.tmap.las.product.server.LASAction;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

public class SaveEdits extends LASAction {

	/* (non-Javadoc)
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			        throws Exception {	

	    OutputStream output = response.getOutputStream();
	    BufferedWriter bsw = new BufferedWriter(new OutputStreamWriter(output));
	    
	    JsonStreamParser parser = new JsonStreamParser(request.getReader());
	    JsonObject message = (JsonObject) parser.next();
	    
	    String temp_file = message.get("temp_file").getAsString();
	    
	    bsw.write("temp_file: "+temp_file+"<br>");
	    
	    JsonElement com = message.get("comment");
	    String comment = "n/a";
	    if ( com != null ) {
	        comment =com.getAsString();
	    }
	    bsw.write("comment:" + comment+"<p></p>");
	    
	    JsonArray edits = (JsonArray) message.get("edits");
	    
	    for (int i = 0; i < edits.size(); i++) {
            Set<Entry<String, JsonElement>> row = ((JsonObject) edits.get(i)).entrySet();
            for (Iterator rowIt = row.iterator(); rowIt.hasNext();) {
                Entry<String, JsonElement> entry = (Entry<String, JsonElement>) rowIt.next();
                String name = entry.getKey();
                String value = entry.getValue().getAsString();
                bsw.write(name+": "+value);
                bsw.write(" ");
            }
            bsw.write("<br>");
        }
	    bsw.flush();
	    bsw.close();
	    
	    return null;
	}

}
