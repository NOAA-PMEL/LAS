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

// TODO: replace with a real rules engine
public class Applier {

	public static int applyCatalogRules(InvCatalog rawCatalog) throws Exception{
		String xmlns = rawCatalog.getUriString();
		String name = rawCatalog.getName();
		String base = "";
		String version = rawCatalog.getVersion();
		String expires = "";
		if(rawCatalog.getExpires() != null)
			expires = rawCatalog.getExpires().toString();
		String status = "new";
		
		// apply rules
		
		// end apply rules
		
		int catalogId = DataAccess.insertCatalog(xmlns, name, base, version, expires, status);
		//Catalog catalog = new Catalog(catalogId, xmlns, name, base, version, expires, status);	
		return catalogId;
	} 
	public static int applyCatalogServiceRules(int parentId, InvService child) throws Exception{
		String name = child.getName();
		String base = child.getBase();
		String servicetype = child.getServiceType().toString();
		String suffix = child.getSuffix();
		String desc = child.getDescription();
		int childId = -1;
		// begin rules
		if(child.getServiceType() != ServiceType.COMPOUND){	// todo: this would be a rule applied
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
	public static int applyServiceServiceRules(int parentId, InvService child) throws Exception{
		String name = child.getName();
		String base = child.getBase();
		String servicetype = child.getServiceType().toString();
		String suffix = child.getSuffix();
		String desc = child.getDescription();
		
		// apply rules

		int childId = DataAccess.insertService(base, name, suffix, desc, "new", servicetype);
		DataAccess.insertServiceService(parentId, childId);
		return childId;
	}
	public static int applyCatalogDatasetRules(int parentId, InvDataset child) throws Exception{
		String alias = ""; 	//??
		String harvest = ""; //?
		String resourcecontrol = ""; //??
		String urlpath = child.getCatalogUrl(); // wrong. But urlPath is private and there appears to be no access method... even more incentive to just parse the xml directly...
		String servicename = child.getServiceDefault().getName(); //??
		String dId = child.getID();
		String authority = child.getAuthority();
		String datatype = child.getDataType().toString(); //?
		String collectiontype = "";
//		if(!datasetT.getCollectionType().equals(""))
//			collectiontype = datasetT.getCollectionType().toString();
		String datasizeUnit = "";
////		if(!datasetT.getDataFormatType().equals(""))
//			datasizeUnit = datasetT.getDataFormatType().toString();
		String name = child.getName();
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
	public static int applyTmgMetadataRules(int parentId, InvMetadata child) throws Exception{
		String inherited = child.isInherited() ? "true" : "false";
		String metadatatype = child.getMetadataType();
		
		// apply rules
		int childId = DataAccess.insertMetadata(metadatatype, inherited);
		DataAccess.insertTmgMetadata(parentId, childId);
		return childId;
	}
	public static int applyTmgDocumentationRules(int parentId, InvDocumentation child) throws Exception{
		String documentationenum = child.getType();
		String value = child.getInlineContent();
		
		// apply rules
		int childId = DataAccess.insertTmgDocumentation(value, documentationenum, parentId);
		return childId;
	}
	public static int applyTmgCreatorRules(int parentId, Source child) throws Exception{
		// apply rules
		int tmgCreatorId = DataAccess.insertTmgCreator(parentId);
		applyTmgCreatorNameRules(parentId, child);
		applyTmgCreatorContactRules(parentId, child);
		
		return tmgCreatorId;
	}
	public static int applyTmgCreatorNameRules(int parentId, Source child) throws Exception{
		String value = child.getName();
		String vocabulary = child.getNameVocab().toString();
		int childId = DataAccess.insertTmgCreatorName(vocabulary, value, parentId);
		return childId;
		
	}
	public static int applyTmgCreatorContactRules(int parentId, Source child) throws Exception{
		String url = child.getUrl();
		String email = child.getEmail();
		int childId = DataAccess.insertTmgCreatorContact(email, url, parentId);
		return childId;
	}
}
