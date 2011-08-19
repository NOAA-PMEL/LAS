package gov.noaa.pmel.tmap.catalogcleaner;

import gov.noaa.pmel.tmap.catalogcleaner.data.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import thredds.catalog.*;
import thredds.catalog.ThreddsMetadata.Source;

public class Crawler {
	private static Logger log = LoggerFactory.getLogger(Crawler.class);
	
	public static void main(String[] args) throws Exception{
		Crawler c = new Crawler();
		//p.run(args[0]);
	}
	
	public int crawlNew(String uri, InvCatalog rawCatalog) throws Exception{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(uri);
		
		Element parent = (Element) doc.getFirstChild();
		
		int parentId = Applier.applyCatalogRules(parent, rawCatalog);
		
		NodeList childServices = parent.getElementsByTagName("service");
		for(int i = 0; i<childServices.getLength(); i++){
			Element newchild = (Element) childServices.item(i);
			crawlNewCatalogService(parentId, newchild);
		}
		NodeList childDatasets = parent.getElementsByTagName("dataset");
		for(int i = 0; i<childDatasets.getLength(); i++){
			Element newchild = (Element) childDatasets.item(i);
			crawlNewCatalogDataset(parentId, newchild);
		}
		// TODO: catalog-metadata
		
		return parentId;
	}
	public void crawlNewCatalogService(int parentId, Element child) throws Exception{
		int childId = Applier.applyCatalogServiceRules(parentId, child);
		
		NodeList childServices = child.getElementsByTagName("service");
		for(int i = 0; i<childServices.getLength(); i++){
			Element newchild = (Element) childServices.item(i);
			crawlNewServiceService(childId, newchild);
		}
	}
	public void crawlNewServiceService(int parentId, Element child) throws Exception{
		int childId = Applier.applyCatalogServiceRules(parentId, child);
		NodeList childServices = child.getElementsByTagName("service");
		for(int i = 0; i<childServices.getLength(); i++){
			Element newchild = (Element) childServices.item(i);
			crawlNewServiceService(childId, newchild);
		}
	}
	public void crawlNewCatalogDataset(int parentId, Element child) throws Exception{
		int childId = Applier.applyCatalogDatasetRules(parentId, child);
		
		crawlNewDatasetTmg(childId, child);// exception to the norm
	}
	
	public void crawlNewDatasetTmg(int parentId, Element child) throws Exception{	// exception to the norm
		int childId = Applier.applyDatasetTmgRules(parentId);
		NodeList children = child.getChildNodes();
		for(int i = 0; i<children.getLength(); i++){
			Node n = children.item(i);
			if(n.getNodeType() == n.ELEMENT_NODE){
				Element newchild = (Element) children.item(i);
				if(newchild.getTagName().equals("metadata"))
					crawlNewTmgMetadata(childId, newchild);
				else if(newchild.getTagName().equals("documentation"))
					crawlNewTmgDocumentation(childId, newchild);
				else if(newchild.getTagName().equals("creator"))
					crawlNewTmgCreator(childId, newchild);
			}
		}
	}
	public void crawlNewMetadataTmg(int parentId, Element child) throws Exception{	// exception to the norm
		int childId = Applier.applyMetadataTmgRules(parentId);
		
		NodeList childServices = child.getElementsByTagName("metadata");
		for(int i = 0; i<childServices.getLength(); i++){
			Element newchild = (Element) childServices.item(i);
			crawlNewTmgMetadata(childId, newchild);
		}
		NodeList childDocumentations = child.getElementsByTagName("documentation");
		for(int i = 0; i<childDocumentations.getLength(); i++){
			Element newchild = (Element) childDocumentations.item(i);
			crawlNewTmgDocumentation(childId, newchild);
		}
		NodeList childCreators = child.getElementsByTagName("creator");
		for(int i = 0; i<childCreators.getLength(); i++){
			Element newchild = (Element) childCreators.item(i);
			crawlNewTmgCreator(childId, newchild);
		}
		
	}
	public void crawlNewTmgMetadata(int parentId, Element child) throws Exception{	// exception to the norm. Stupid tmg...
		int childId = Applier.applyTmgMetadataRules(parentId, child);
		
		crawlNewMetadataTmg(childId, child);// exception to the norm
		
		// other stuff here
		
	}
	public void crawlNewTmgDocumentation(int parentId, Element child) throws Exception{
		
		int childId = Applier.applyTmgDocumentationRules(parentId, child);
		
		// TODO: tmg_documentation_namespace
		// TODO: tmg_documentation_xlink
		
	}
	
	public void crawlNewTmgCreator(int parentId, Element child) throws Exception{
		int childId = Applier.applyTmgCreatorRules(parentId, child);	
		NodeList childName = child.getElementsByTagName("name");
		for(int i = 0; i<childName.getLength(); i++){
			Element newchild = (Element) childName.item(i);
			crawlNewTmgCreatorName(childId, newchild);
		}	
		NodeList childContact = child.getElementsByTagName("contact");
		for(int i = 0; i<childContact.getLength(); i++){
			Element newchild = (Element) childContact.item(i);
			crawlNewTmgCreatorContact(childId, newchild);
		}
	}
	public void crawlNewTmgCreatorName(int parentId, Element child) throws Exception{
		int childId = Applier.applyTmgCreatorNameRules(parentId, child);	
	}
	public void crawlNewTmgCreatorContact(int parentId, Element child) throws Exception{
		int childId = Applier.applyTmgCreatorContactRules(parentId, child);	
	}
}
