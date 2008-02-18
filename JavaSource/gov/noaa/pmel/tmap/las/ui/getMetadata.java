package gov.noaa.pmel.tmap.las.ui;

import gov.noaa.pmel.tmap.las.jdom.LASUIRequest;
import gov.noaa.pmel.tmap.las.product.server.LASAction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class getMetadata extends LASAction {

	/* (non-Javadoc)
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String dsid = request.getParameter("dsid");
		String catitem = request.getParameter("catitem");
		if ( catitem != null ) dsid = catitem;
		LASUIRequest ui_request = new LASUIRequest();
		ui_request.addVariable(dsid, "dummy");
		ui_request.setOperation("Metadata");
		return new ActionForward("/ProductServer.do?xml="+ui_request.toEncodedURLString());
	}

}
