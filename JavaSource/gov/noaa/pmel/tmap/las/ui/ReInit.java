package gov.noaa.pmel.tmap.las.ui;

import gov.noaa.pmel.tmap.las.product.server.LASConfigPlugIn;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class ReInit extends Action {
	private static Logger log = Logger.getLogger(ReInit.class.getName());
	/* (non-Javadoc)
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws Exception {
		LASConfigPlugIn plugin = new LASConfigPlugIn();
		log.debug("Running reinit action.");
		ServletContext context = (ServletContext) servlet.getServletContext();
		String reinit = request.getParameter("reinit");
		if ( reinit != null && reinit.equals("force") ) {
			context.setAttribute("lock", "true");
			plugin.reinit(context);
			log.debug("Reinit was forced and succeeded.");
			request.setAttribute("message", "has been reinitialized.");
		} else if (reinit != null && reinit.equals("wait") ) {
			// Check for sessions and wait...
		} else {
			// Check for sessions and quit if found...
			context.setAttribute(LASConfigPlugIn.LAS_LOCK_KEY, "true");
			boolean can_reinit = true;
			Enumeration attrs = context.getAttributeNames();
			while (attrs.hasMoreElements() ) {
				String attr = (String) attrs.nextElement();
				log.debug("Session attribute named: "+attr);
				if ( attr.contains("sessions_") ) {
					// There is a job runnning, don't change the config.
					can_reinit = false;
				}
			}
			if ( can_reinit ) {
				plugin.reinit(context);
				log.debug("Reinit succeeded.  Reinit was not forced.");
				request.setAttribute("message", "has been reinitialized.");
			} else {
				log.debug("Reinit was not forced.  It was not done because of pending requests.");
				request.setAttribute("message", "has NOT been reinitialized because there are pending requests.  Use reinit=force to force the initialization.  Using the forec option may cause pending jobs to fail.");
			}

		}
		context.removeAttribute(LASConfigPlugIn.LAS_LOCK_KEY);
		return mapping.findForward("reinit");
	}

}
