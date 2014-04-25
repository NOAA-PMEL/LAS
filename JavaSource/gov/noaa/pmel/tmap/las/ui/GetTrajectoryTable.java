package gov.noaa.pmel.tmap.las.ui;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import gov.noaa.pmel.tmap.addxml.JDOMUtils;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.jdom.LASUIRequest;
import gov.noaa.pmel.tmap.las.product.server.LASAction;
import gov.noaa.pmel.tmap.las.product.server.LASConfigPlugIn;
import gov.noaa.pmel.tmap.las.util.Category;
import gov.noaa.pmel.tmap.las.util.Dataset;
import gov.noaa.pmel.tmap.las.util.NameValuePair;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.EntityRef;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.output.XMLOutputter;

public class GetTrajectoryTable extends LASAction {

	/* (non-Javadoc)
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			        throws Exception {	
	    LASProxy lasProxy = new LASProxy();
	    LASConfig lasConfig = (LASConfig)servlet.getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
	    String dsid = request.getParameter("dsid");
	    if ( dsid != null ) {
	        List<Category> c = lasConfig.getCategories(dsid);
	        Dataset dataset = c.get(0).getDataset();
	        Map<String, Map<String, String>> properties = dataset.getPropertiesAsMap();
	        if ( properties != null ) {
	            Map<String, String> tabledap = properties.get("tabledap_access");
	            if ( tabledap != null ) {
	                VariableSerializable[] vars = dataset.getVariablesSerializable();
	                if ( vars.length > 0 ) {
	                    String url = lasConfig.getDataAccessURL(dsid, vars[0].getID(), false);
	                    if ( url != null && !url.equals("") ) {
	                        if ( url.contains("#") ) url = url.substring(0, url.indexOf("#"));
	                        String id = tabledap.get("id");
	                        if ( id != null && !id.equals("") ) {
	                            if (!url.endsWith("/") ) url = url + "/";
	                            url = url + id + ".xhtml";
	                            String table = tabledap.get("table_variables");
	                            String document_base = tabledap.get("document_base");
	                            if ( !document_base.endsWith("/") ) document_base = document_base + "/";
	                            if ( table != null && !table.equals("") ) {
	                                url = url + "?" + table;
	                                response.setContentType("application/xhtml+xml");
	                                Document doc = new Document();
	                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
	                                lasProxy.executeGetMethodAndStreamResult(url+"&distinct()", stream);
	                                JDOMUtils.XML2JDOM(stream.toString(), doc);
	                                Element root = doc.getRootElement();
	                                Namespace ns = root.getNamespace();
	                                if ( root != null ) {
	                                    Element body = root.getChild("body", ns);
	                                    if ( body != null ) {
	                                        // Chrome complains about the &nbsp; in the body
	                                        List<Content> stuff = body.getContent();
	                                        for ( int c1 = 0; c1 < stuff.size(); c1++ ) {
	                                            Content s = stuff.get(c1);
	                                            if ( s instanceof EntityRef ) {
	                                                boolean out = body.removeContent(s);
	                                            }
	                                        }
	                                        Element tableE = body.getChild("table", ns);
	                                        List<Element> rows = tableE.getChildren("tr", ns);
	                                        Element header = rows.get(0);
	                                        Element th = new Element("th", ns);
	                                        th.setText("Documentation");
	                                        header.addContent(th);
	                                        Element blank = rows.get(1);
	                                        Element bh = new Element("th", ns);
	                                        blank.addContent(bh);
	                                        // This is a blank row of <th> elements we are skipping.
	                                        for (int i = 2; i < rows.size(); i++) {
	                                            Element row = rows.get(i);

	                                            List<Element> tds = row.getChildren("td", ns);
	                                            Element first = tds.get(0);

	                                            Element td = new Element("td", ns);
	                                            td.setAttribute("nowrap", "nowrap");
	                                            td.setAttribute("rowspan", "1");
	                                            td.setAttribute("colspan", "1");
	                                            if ( !first.getTextNormalize().equals("") ) {
	                                                
	                                                Element a = new Element("a", ns);
	                                                a.setAttribute("href", document_base+first.getTextNormalize());
	                                                a.setText("Documentation");

	                                                td.setContent(a);

	                                            }
	                                            row.addContent(td);
	                                        }
	                                        org.jdom.output.Format format = org.jdom.output.Format.getPrettyFormat();
	                                        format.setLineSeparator(System.getProperty("line.separator"));
	                                        XMLOutputter outputter =
	                                                new XMLOutputter(format);
	                                        outputter.output(doc, response.getOutputStream());
	                                        // http://dunkel.pmel.noaa.gov:8660/erddap/tabledap/dsg_files_badval_7f9a_0653_3fc1.xhtml?QC_flag,cruise_expocode,vessel_name,PIs&time>=2007-09-01&time<=2008-01-01&distinct()
	                                    }
	                                }
	                            }
	                        }
	                    }
	                }
	            }
	        }
	    }
	    return null;
	}
	public static String prepareURL(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		String dsid = request.getParameter("dsid");
		String catitem = request.getParameter("catitem");
		String opendap = request.getParameter("opendap");
		if ( catitem != null ) dsid = catitem;
		LASUIRequest ui_request = new LASUIRequest();
		ui_request.addVariable(dsid, "dummy");
		if ( opendap != null ) {
		   ui_request.setOperation("MetadataURLs");
		} else {
			ui_request.setOperation("Metadata");
		}
		return ui_request.toEncodedURLString();
	}

}
