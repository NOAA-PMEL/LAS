package gov.noaa.pmel.tmap.catalogcleaner;

import gov.noaa.pmel.tmap.catalogcleaner.data.Catalog;
import gov.noaa.pmel.tmap.catalogcleaner.data.CatalogService;
import gov.noaa.pmel.tmap.catalogcleaner.data.DatasetTmg;
import gov.noaa.pmel.tmap.catalogcleaner.data.ServiceService;
import gov.noaa.pmel.tmap.catalogcleaner.data.TmgCreator;
import gov.noaa.pmel.tmap.catalogcleaner.data.TmgCreatorContact;
import gov.noaa.pmel.tmap.catalogcleaner.data.TmgCreatorName;
import gov.noaa.pmel.tmap.catalogcleaner.data.TmgDocumentation;
import gov.noaa.pmel.tmap.catalogcleaner.data.TmgMetadata;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogImpl;
import thredds.catalog.InvDataset;
import thredds.catalog.InvDatasetImpl;
import thredds.catalog.InvDocumentation;
import thredds.catalog.InvMetadata;
import thredds.catalog.InvService;
import thredds.catalog.ServiceType;
import thredds.catalog.ThreddsMetadata.Source;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

// TODO: replace with a real rules engine
public class Applier {
	
	private static InvCatalog rawCatalog;

	public static int applyCatalogRules(Element parent, InvCatalog crawCatalog) throws Exception{
		rawCatalog = crawCatalog;
		String xmlns = parent.getAttribute("xmlns");		// works
		String name = parent.getAttribute("name");			// works
		String base = parent.getAttribute("base");
		String version = parent.getAttribute("version");	// works
		String expires = parent.getAttribute("expires");
		String status = "new";
		
		// apply rules
		
		// end apply rules
		
		int catalogId = DataAccess.insertCatalog(name, expires, version, base, xmlns, status);
		//Catalog catalog = new Catalog(catalogId, xmlns, name, base, version, expires, status);	
		return catalogId;
	} 
	public static int applyCatalogServiceRules(int parentId, Element child) throws Exception{
		String name = child.getAttribute("name");	// works
		String base = child.getAttribute("base");	// works
		String servicetype = child.getAttribute("serviceType");	// works
		String suffix = child.getAttribute("suffix");
		String desc = child.getAttribute("desc");
		int childId = -1;
		// begin rules
		if(!servicetype.equals("COMPOUND")){	// todo: this would be a rule applied
			name = "ct2009";		// TODO: dynamically generate new name, determine other fields
			base = "";
			servicetype = "COMPOUND";
			suffix = "";
			desc = "";
			childId = DataAccess.insertService(suffix, name, base, desc, servicetype, "new");
			DataAccess.insertCatalogService(parentId, childId);
			addCompoundServices(childId);
		}
		else{
			childId = DataAccess.insertService(suffix, name, base, desc, servicetype, "new");
			DataAccess.insertCatalogService(parentId, childId);
			
		}
		// end rules

		//catalogService = new CatalogService(catalogId, serviceId, base, name, suffix, desc, "new", servicetype);
		return childId;
	}
	// this would be defined in a rule
	public static void addCompoundServices(int serviceId) throws Exception{
		String name="odap";
		String serviceType="OpenDAP";
		String base="http://dunkel.pmel.noaa.gov:8780/thredds/dodsC/";
		int childId = DataAccess.insertService("", name, base, "", serviceType, "new");
		DataAccess.insertServiceService(serviceId, childId);
		name="http";
		serviceType="HTTPServer";
		base="/thredds/fileServer/";	// TODO: dynamically determine which services are available on their server, use them
		childId = DataAccess.insertService("", name, base, "", serviceType, "new");
		DataAccess.insertServiceService(serviceId, childId);
		name="wms";
		serviceType="WMS";
		base="/thredds/wms/";	// TODO: dynamically determine which services are available on their server, use them
		childId = DataAccess.insertService("", name, base, "", serviceType, "new");
		DataAccess.insertServiceService(serviceId, childId);
	}
	public static int applyServiceServiceRules(int parentId, Element child) throws Exception{
		String name = child.getAttribute("name");	// works
		String base = child.getAttribute("base");	// works
		String servicetype = child.getAttribute("serviceType"); 	// works
		String suffix = child.getAttribute("suffix");
		String desc = child.getAttribute("desc");
		
		// apply rules

		int childId = DataAccess.insertService(suffix, name, base, desc, servicetype, "new");
		DataAccess.insertServiceService(parentId, childId);
		return childId;
	}
	public static int applyCatalogDatasetRules(int parentId, Element child) throws Exception{
		String dId = child.getAttribute("ID");						// works
		InvDataset d = rawCatalog.findDatasetByID(dId);
		String alias = child.getAttribute("alias");
		String harvest = child.getAttribute("harvest");
		String resourcecontrol = child.getAttribute("resourcecontrol");
		String urlpath = child.getAttribute("urlPath");				// works
		//String servicename = child.getAttribute("serviceName");
		String servicename = d.getServiceDefault().getName();
		String authority = child.getAttribute("authority");
		//String datatype = child.getAttribute("dataType");
		String datatype = d.getDataType().toString();
		String collectiontype = child.getAttribute("collectiontype");
//		if(!datasetT.getCollectionType().equals(""))
//			collectiontype = datasetT.getCollectionType().toString();
		String datasizeUnit = child.getAttribute("datasizeUnit");
////		if(!datasetT.getDataFormatType().equals(""))
//			datasizeUnit = datasetT.getDataFormatType().toString();
		String name = child.getAttribute("name");					// works
		String status = "new";
		
		// apply rules
		int childId = DataAccess.insertDataset(harvest, name, alias, authority, dId, servicename, urlpath, resourcecontrol, collectiontype, status, datatype, datasizeUnit);
		
		DataAccess.insertCatalogDataset(parentId, childId);
		//Dataset dataset = new Dataset(datasetId, alias, harvest, resourcecontrol, urlpath, servicename, dId, authority, name, datatype, status, collectiontype, datasizeUnit);
		return childId;
	}
	public static int applyDatasetTmgRules(int parentId) throws Exception{
		// nothing to check here, just insert new tmg for this dataset
		//int datasetId = dataset.getDatasetId();
		int childId = DataAccess.insertTmg();
		
		// apply rules
		
		DataAccess.insertDatasetTmg(parentId, childId);
		return childId;
	}
	public static int applyTmgMetadataRules(int parentId, Element child) throws Exception{
		String inherited = child.getAttribute("inherited");
		//String metadatatype = child.getAttribute("metadatatype");
		InvDataset d = rawCatalog.findDatasetByID("ct_flux"); // TODO: obviously, make this not hard-coded
		
		// apply rules
		int childId = DataAccess.insertMetadata(metadatatype, inherited);
		DataAccess.insertTmgMetadata(parentId, childId);
		return childId;
	}
	public static int applyTmgDocumentationRules(int parentId, Element child) throws Exception{
		String documentationenum = child.getAttribute("type");
		String value = child.getFirstChild().getTextContent();
		
		// apply rules
		int childId = DataAccess.insertTmgDocumentation(parentId, value, documentationenum);
		return childId;
	}
	public static int applyTmgCreatorRules(int parentId, Element child) throws Exception{
		// apply rules
		int tmgCreatorId = DataAccess.insertTmgCreator(parentId);
		return tmgCreatorId;
	}
	public static int applyTmgCreatorNameRules(int parentId, Element child) throws Exception{
		String value = child.getFirstChild().getTextContent();
		String vocabulary = child.getAttribute("vocabulary");
		int childId = DataAccess.insertTmgCreatorName(parentId, value, vocabulary);
		return childId;
		
	}
	public static int applyTmgCreatorContactRules(int parentId, Element child) throws Exception{
		String url = child.getAttribute("url");
		String email = child.getAttribute("email");
		int childId = DataAccess.insertTmgCreatorContact(parentId, email, url);
		return childId;
	}
	public static int applyMetadataTmgRules(int parentId) throws Exception {
		int childId = DataAccess.insertTmg();
		
		// apply rules
		
		DataAccess.insertMetadataTmg(parentId, childId);
		return childId;
	}
}
