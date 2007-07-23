package gov.noaa.pmel.tmap.las.luis;
import java.util.*;
import java.io.*;
import org.apache.velocity.*;
import org.apache.velocity.context.Context;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.VelocityException;
import javax.servlet.ServletException;
import java.sql.SQLException;


public class LayoutManager {
    String mIncluded;
    String mTemplateName;

    public LayoutManager(TemplateContext tc, String name, Writer writer)
	throws Exception {
	mTemplateName = name;
	doMerge(tc, writer);
    }

    void doMerge(TemplateContext tc, Writer writer) throws Exception {
	Log.debug(this,"merging template:" + mTemplateName);
	tc.put("layout", this);
	String layoutManager = tc.getLayoutManager(mTemplateName);
	if (layoutManager == null){
	    Log.debug(this, "No layout manager found");
	    Velocity.mergeTemplate(mTemplateName, tc, writer);
	} else {
	    Log.debug(this, "Found layout manager:" + layoutManager);
	    StringWriter sw = new StringWriter();
	    Velocity.mergeTemplate(mTemplateName, tc, sw);
	    mIncluded = sw.toString();
	    Velocity.mergeTemplate(layoutManager, tc, writer);
	}
    }

    public String addMainTemplate() {
	Log.debug(this, "addMainTemplate called for " + mTemplateName);
	if (mIncluded != null){
	    return mIncluded;
	} else {
	    Log.debug(this, "Empty addTemplate call for "
				  + mTemplateName);
	    return "";
	}
    }

    public static void mergeTemplate(TemplateContext tc, String name,
					 Writer writer)
    throws ServletException
    {
      try {
	new LayoutManager(tc,name,writer);
      } catch (Exception e){
	throw new ServletException(e);
      }
    }
}
