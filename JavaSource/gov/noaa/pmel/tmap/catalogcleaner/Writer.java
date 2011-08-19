package gov.noaa.pmel.tmap.catalogcleaner;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import gov.noaa.pmel.tmap.catalogcleaner.data.*;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
 
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Writer {

	public Writer() throws TransformerException, ParserConfigurationException, IOException{
		
		
	}
	public void run(int catalogId) throws Exception{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		
		Catalog catalog = DataAccess.getCatalog(catalogId);
		writeCatalog(doc, catalog);
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
	    StreamResult result = new StreamResult(new File("testing_xml.xml").getPath());
 
		// Output to console for testing
	    // StreamResult result = new StreamResult(System.out);
 
		transformer.transform(source, result);
 
		System.out.println("File saved!");
		
	}
	
	public Document writeCatalog(Document doc, Catalog catalog) throws Exception{
		Element catalogElement = doc.createElement("catalog");
		if(!catalog.getXmlns().equals("")){
			Attr xmlns = doc.createAttribute("xmlns");
			xmlns.setValue(catalog.getXmlns());
			catalogElement.setAttributeNode(xmlns);
		}
		if(!catalog.getName().equals("")){
			Attr name = doc.createAttribute("name");
			name.setValue(catalog.getName());
			catalogElement.setAttributeNode(name);
		}
		if(!catalog.getVersion().equals("")){
			Attr version = doc.createAttribute("version");
			version.setValue(catalog.getVersion());
			catalogElement.setAttributeNode(version);
		}
		
		ArrayList<Service> services = DataAccess.getServiceBCatalog(catalog.getCatalogId());
		for(Iterator<Service> it = services.iterator(); it.hasNext();){
			Service service = it.next();
			appendService(doc, catalogElement, service);
		}
		ArrayList<Dataset> datasets = DataAccess.getDatasetBCatalog(catalog.getCatalogId());
		for(Iterator<Dataset> it = datasets.iterator(); it.hasNext();){
			Dataset dataset = it.next();
			appendDataset(doc, catalogElement, dataset);
		}

		doc.appendChild(catalogElement);
		return doc;
	}
	
	public Element appendService(Document doc, Element node, Service service) throws Exception{
		Element serviceElement = doc.createElement("service");

		if(!service.getName().equals("")){
			Attr name = doc.createAttribute("name");
			name.setValue(service.getName());
			serviceElement.setAttributeNode(name);
		}
		if(!service.getServicetype().equals("")){
			Attr servicetype = doc.createAttribute("serviceType");
			servicetype.setValue(service.getServicetype());
			serviceElement.setAttributeNode(servicetype);
		}
		if(!service.getBase().equals("")){
			Attr base = doc.createAttribute("base");
			base.setValue(service.getBase());
			serviceElement.setAttributeNode(base);
		}
		
		ArrayList<Service> services = DataAccess.getServiceBService(service.getServiceId());
		for(Iterator<Service> it = services.iterator(); it.hasNext();){
			Service child = it.next();
			appendService(doc, serviceElement, child);
		}
		node.appendChild(serviceElement);
		return node;
	}
	
	public Element appendDataset(Document doc, Element node, Dataset dataset) throws Exception{
		Element datasetElement = doc.createElement("dataset");
		
		if(!dataset.getName().equals("")){
			Attr name = doc.createAttribute("name");
			name.setValue(dataset.getName());
			datasetElement.setAttributeNode(name);
		}
		if(!dataset.getDId().equals("")){
			Attr dID = doc.createAttribute("ID");
			dID.setValue(dataset.getDId());
			datasetElement.setAttributeNode(dID);
		}
		if(!dataset.getUrlpath().equals("")){
			Attr urlPath = doc.createAttribute("urlPath");
			urlPath.setValue(dataset.getUrlpath());
			datasetElement.setAttributeNode(urlPath);
		}
		if(!dataset.getServicename().equals("")){
			Attr serviceName = doc.createAttribute("serviceName");
			serviceName.setValue(dataset.getServicename());
			datasetElement.setAttributeNode(serviceName);
		}
		if(!dataset.getDatatype().equals("")){
			Attr dataType = doc.createAttribute("dataType");
			dataType.setValue(dataset.getDatatype());
			datasetElement.setAttributeNode(dataType);
		}
		ArrayList<Tmg> tmgs = DataAccess.getTmgBDataset(dataset.getDatasetId());
		for(Iterator<Tmg> it = tmgs.iterator(); it.hasNext();){
			Tmg child = it.next();
			appendTmg(doc, datasetElement, child);
		}
		node.appendChild(datasetElement);
		return node;
	}
	
	public Element appendTmg(Document doc, Element node, Tmg tmg) throws Exception{
		int tmgId = tmg.getTmgId();
		// no node to actually append here, just collect tmg's children and append directly to node
		ArrayList<Metadata> metadatas = DataAccess.getMetadataBTmg(tmgId);
		for(Iterator<Metadata> it = metadatas.iterator(); it.hasNext();){
			Metadata child = it.next();
			appendMetadata(doc, node, child);
		}
		ArrayList<TmgDocumentation> documentations = DataAccess.getTmgDocumentationBTmg(tmgId);
		for(Iterator<TmgDocumentation> it = documentations.iterator(); it.hasNext();){
			TmgDocumentation child = it.next();
			appendTmgDocumentation(doc, node, child);
		}
		ArrayList<TmgCreator> creators = DataAccess.getTmgCreatorBTmg(tmgId);
		for(Iterator<TmgCreator> it = creators.iterator(); it.hasNext();){
			TmgCreator child = it.next();
			appendTmgCreator(doc, node, child);
		}
		// TODO: rest of tmg
		return node;
	}
	public Element appendMetadata(Document doc, Element node, Metadata metadata) throws Exception{
		Element metadataElement = doc.createElement("metadata");
		
		if(!metadata.getInherited().equals("")){
			Attr inherited = doc.createAttribute("inherited");
			inherited.setValue(metadata.getInherited());
			metadataElement.setAttributeNode(inherited);
		}
		ArrayList<Tmg> tmgs = DataAccess.getTmgBMetadata(metadata.getMetadataId());
		for(Iterator<Tmg> it = tmgs.iterator(); it.hasNext();){
			Tmg child = it.next();
			appendTmg(doc, metadataElement, child);
		}
		
		node.appendChild(metadataElement);
		return node;
	}
	public Element appendTmgDocumentation(Document doc, Element node, TmgDocumentation documentation){
		Element documentationElement = doc.createElement("documentation");

		if(!documentation.getDocumentationenum().equals("")){
			Attr type = doc.createAttribute("type");
			type.setValue(documentation.getDocumentationenum());
			documentationElement.setAttributeNode(type);
		}
		documentationElement.setTextContent(documentation.getValue());
		node.appendChild(documentationElement);
		return node;
	}
	public Element appendTmgCreator(Document doc, Element node, TmgCreator creator) throws Exception{
		Element creatorElement = doc.createElement("creator");
		// should only be one, but this is the way this is set up right now.
		ArrayList<TmgCreatorName> names = DataAccess.getTmgCreatorNameBTmgCreator(creator.getTmgCreatorId());
		for(Iterator<TmgCreatorName> it = names.iterator(); it.hasNext();){
			TmgCreatorName child = it.next();
			appendTmgCreatorName(doc, creatorElement, child);
		}
		ArrayList<TmgCreatorContact> contacts = DataAccess.getTmgCreatorContactBTmgCreator(creator.getTmgCreatorId());
		for(Iterator<TmgCreatorContact> it = contacts.iterator(); it.hasNext();){
			TmgCreatorContact child = it.next();
			appendTmgCreatorContact(doc, creatorElement, child);
		}
		node.appendChild(creatorElement);
		return node;
	}
	public Element appendTmgCreatorName(Document doc, Element node, TmgCreatorName name){
		Element nameElement = doc.createElement("name");
		if(!name.getVocabulary().equals("")){
			Attr vocabulary = doc.createAttribute("vocabulary");
			vocabulary.setValue(name.getVocabulary());
			nameElement.setAttributeNode(vocabulary);
		}
		nameElement.setTextContent(name.getValue());
		node.appendChild(nameElement);
		return node;
	}
	public Element appendTmgCreatorContact(Document doc, Element node, TmgCreatorContact contact){
		Element contactElement = doc.createElement("contact");
		if(!contact.getUrl().equals("")){
			Attr url = doc.createAttribute("url");
			url.setValue(contact.getUrl());
			contactElement.setAttributeNode(url);
		}
		if(!contact.getEmail().equals("")){
			Attr email = doc.createAttribute("email");
			email.setValue(contact.getEmail());
			contactElement.setAttributeNode(email);
		}
		node.appendChild(contactElement);
		return node;
	}
	
}
