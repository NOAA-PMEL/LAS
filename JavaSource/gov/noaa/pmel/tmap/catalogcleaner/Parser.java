package gov.noaa.pmel.tmap.catalogcleaner;

import gov.noaa.pmel.tmap.catalogcleaner.data.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import org.apache.xerces.parsers.SAXParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import thredds.catalog.*;
import thredds.catalog.ThreddsMetadata.Source;

public class Parser {
	private static Logger log = LoggerFactory.getLogger(Parser.class);
	
	public static void main(String[] args) throws Exception{
		Parser p = new Parser();
		p.run(args[0]);
	}

	public void run(String uri) throws Exception{
		//		XMLReader parser = new SAXParser();
		//		parser.parse(uri);
		InvCatalog rawT = (InvCatalog) (Utils.FACTORY.readXML(uri));
		StringBuilder buff = new StringBuilder();
		//if (!uri.startsWith("http://")) {
		//	System.out.println("Summary, " + uri+ ", will not clean relative catalogs.");
		//} else
		//if (!rawT.check(buff, false)) {
		//	log.error("Invalid catalog " + uri + "\n" + buff.toString(), 1);
		//	System.out.println("Summary, " + uri + ", invalid");
		//} else {
			log.info("Cleaning: " + uri, 0);
			//			CatalogCleaner cleaner = null;
			//			try {
			//				cleaner = new CatalogCleaner(catalog, aggregations, refs, skip, stop_string);
			//				CCUtils.AGGREGATE = aggregations;
			//				CCUtils.REFS = refs;
			//				CCUtils.STOPSTRING = stop_string;
			//				CCUtils.SKIP = skip;
			//				CCUtils.KEY = CCUtils.MD5Encode(raw.getUriString());

			//			} catch (UnsupportedEncodingException e) {
			//				e.printStackTrace();
			//			}

			
		//}
		DataAccess.init(this);
		// todo: remove from database if exists
		int catalogId = crawlNew(rawT);
		Writer ccwriter = new Writer();
		ccwriter.run(catalogId);
	}
	
	public int crawlNew(InvCatalog parent) throws Exception{
		int parentId = Applier.applyCatalogRules(parent);
		
		List<InvService> childServices = parent.getServices();
		for(Iterator<InvService> it = childServices.iterator(); it.hasNext();){
			InvService newchild = it.next();	
			crawlNewCatalogService(parentId, newchild);
		}
		List<InvDataset> childDatasets = parent.getDatasets();
		for(Iterator<InvDataset> it = childDatasets.iterator(); it.hasNext();){
			InvDataset newchild = it.next();
			crawlNewCatalogDataset(parentId, newchild);
		}
		// TODO: catalog-metadata
		
		return parentId;
	}
	public void crawlNewCatalogService(int parentId, InvService child) throws Exception{
		int childId = Applier.applyCatalogServiceRules(parentId, child);
		List<InvService> childServices = child.getServices();
		for(Iterator<InvService> it = childServices.iterator(); it.hasNext();){
			InvService newchild = it.next();
			crawlNewServiceService(childId, newchild);
		}
	}
	public void crawlNewServiceService(int parentId, InvService child) throws Exception{
		int childId = Applier.applyCatalogServiceRules(parentId, child);
		List<InvService> childServices = child.getServices();
		for(Iterator<InvService> it = childServices.iterator(); it.hasNext();){
			InvService newchild = it.next();
			crawlNewServiceService(childId, newchild);
		}
	}
	public void crawlNewCatalogDataset(int parentId, InvDataset child) throws Exception{
		int childId = Applier.applyCatalogDatasetRules(parentId, child);
		
		crawlNewDatasetTmg(childId, child);// exception to the norm
	}
	
	public void crawlNewDatasetTmg(int parentId, InvDataset child) throws Exception{	// exception to the norm
		int childId = Applier.applyDatasetTmgRules(parentId);
		
		List<InvMetadata> metadatas = child.getMetadata();
		for(Iterator<InvMetadata> it = metadatas.iterator(); it.hasNext();){
			InvMetadata newchild = it.next();	
			crawlNewTmgMetadata(childId, newchild);
		}
//		List<InvDocumentation> childDocumentations = child.getDocumentation();	// ick
//		for(Iterator<InvDocumentation> it = childDocumentations.iterator(); it.hasNext();){
//			InvDocumentation newchild = it.next();	
//			crawlNewTmgDocumentation(childId, newchild);
//		}
//		List<Source> childCreators = child.getCreators();
//		for(Iterator<Source> it = childCreators.iterator(); it.hasNext();){
//			Source newchild = it.next();	
//			crawlNewTmgCreator(childId, newchild);
//		}
	}
	public void crawlNewMetadataTmg(int parentId, InvMetadata child) throws Exception{	// exception to the norm
		int childId = Applier.applyDatasetTmgRules(parentId);
		
//		List<InvMetadata> metadatas = child.getParentDataset().getMetadata();
//		for(Iterator<InvMetadata> it = metadatas.iterator(); it.hasNext();){
//			InvMetadata newchild = it.next();	
//			crawlNewTmgMetadata(childId, newchild);
//		}
		List<InvDocumentation> childDocumentations = child.getParentDataset().getDocumentation();	// ick
		for(Iterator<InvDocumentation> it = childDocumentations.iterator(); it.hasNext();){
			InvDocumentation newchild = it.next();	
			crawlNewTmgDocumentation(childId, newchild);
		}
		List<Source> childCreators = child.getParentDataset().getCreators();
		for(Iterator<Source> it = childCreators.iterator(); it.hasNext();){
			Source newchild = it.next();	
			crawlNewTmgCreator(childId, newchild);
		}
	}
	public void crawlNewTmgMetadata(int parentId, InvMetadata child) throws Exception{	// exception to the norm. Stupid tmg...
		int childId = Applier.applyTmgMetadataRules(parentId, child);
		
		crawlNewMetadataTmg(childId, child);// exception to the norm
		
	}
	public void crawlNewTmgDocumentation(int parentId, InvDocumentation child) throws Exception{
		
		int childId = Applier.applyTmgDocumentationRules(parentId, child);
		
		// TODO: tmg_documentation_namespace
		// TODO: tmg_documentation_xlink
		
	}
	
	public void crawlNewTmgCreator(int parentId, Source child) throws Exception{
		int childId = Applier.applyTmgCreatorRules(parentId, child);	
	}

}
