package gov.noaa.pmel.tmap.catalogcleaner;

import gov.noaa.pmel.tmap.catalogcleaner.data.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jdom.*;
import org.jdom.input.SAXBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Crawler {
	private static Logger log = LoggerFactory.getLogger(Crawler.class);
	
	public ArrayList<Attribute> getAttributes(List<?> l){
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		Iterator<?> i = l.iterator();
		while (i.hasNext()){
			atts.add((Attribute) i.next());
		}
		return atts;
	}
	public ArrayList<Element> getElements(List<?> l){
		ArrayList<Element> elems = new ArrayList<Element>();
		Iterator<?> i = l.iterator();
		while (i.hasNext()){
			elems.add((Element) i.next());
		}
		return elems;
	}
	public ArrayList<org.jdom.Text> getContent(List<?> l){
		ArrayList<org.jdom.Text> elems = new ArrayList<org.jdom.Text>();
		Iterator<?> i = l.iterator();
		while (i.hasNext()){
			elems.add((org.jdom.Text) i.next());
		}
		return elems;
	}
		public ArrayList<Namespace> getNamespaces(List<?> l){
		ArrayList<Namespace> elems = new ArrayList<Namespace>();
		Iterator<?> i = l.iterator();
		while (i.hasNext()){
			elems.add((Namespace) i.next());
		}
		return elems;
	}
	public Catalog crawlNewCatalog(String uri) throws Exception{
		Document doc = new Document();
		try {
			SAXBuilder parser = new SAXBuilder();
			 doc = parser.build(uri);
		} catch (Exception e) {
			log.error("Trouble reading source catalog: " + e.getMessage(), 0);
		}
		
		Element parent = doc.getRootElement();
		
		Datavalue name = new Datavalue();
		Datavalue expires = new Datavalue();
		Datavalue version = new Datavalue();
		Datavalue base = new Datavalue();
		Datavalue status = new Datavalue("new");
		
		Datavalue xmlns = new Datavalue(parent.getNamespaceURI());

		ArrayList<Attribute> values = getAttributes(parent.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
			else if(a.getName().equals("expires"))
				expires.NOTNULL(a.getValue());
			else if(a.getName().equals("version"))
				version.NOTNULL(a.getValue());
			else if(a.getName().equals("base"))
				base.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(parent.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
			else if(a.getName().equals("expires"))
				expires.NOTNULL(a.getValue());
			else if(a.getName().equals("version"))
				version.NOTNULL(a.getValue());
			else if(a.getName().equals("base"))
				base.NOTNULL(a.getValue());
		}
		
		int parentId = DataAccess.insertCatalog(new Datavalue(null), base, expires, name, version, xmlns, status);

		ArrayList<Element> all = getElements(parent.getChildren());
		for(int i = 0; i<all.size(); i++){
			Element newchild = all.get(i);
			if(newchild.getName().equals("service"))
				crawlNewCatalogService(parentId, newchild);
			else if(newchild.getName().equals("dataset"))
				crawlNewCatalogDataset(parentId, newchild);
			else if(newchild.getName().equals("property"))
				crawlNewCatalogProperty(parentId, newchild);
		}
		ArrayList<Namespace> xlinks = getNamespaces(parent.getAdditionalNamespaces());
		for(int i = 0; i<xlinks.size(); i++){
			Namespace newchild = xlinks.get(i);
			crawlNewCatalogXlink(parentId, newchild);
		}
		
		Catalog raw = DataAccess.getCatalog(parentId);
		return raw;
	}

	public void crawlNewCatalogXlink(int parentId, Namespace child) throws Exception{
		Datavalue value = new Datavalue(child.getURI());
		Datavalue xlink = new Datavalue(child.getPrefix());

		DataAccess.insertCatalogXlink(parentId, value, xlink);

	}
	public int crawlRawCatalog(Catalog raw) throws Exception{
		Catalog cleanCatalog = raw.clone();
		int cleanCatalogId = DataAccess.insertCatalog(cleanCatalog);
		cleanCatalog.setCatalogId(cleanCatalogId);

		cleanCatalog = Applier.applyCatalogRules(cleanCatalog);

		if(cleanCatalog == null){
			DataAccess.deleteCatalog(cleanCatalog);
			return -1;
		}
		raw.setCleanCatalogId(cleanCatalogId + "");
		DataAccess.updateCatalog(raw);
		
		int catalogId = raw.getCatalogId();
		
		ArrayList<Service> services = DataAccess.getServiceBCatalog(catalogId);
		for(int i = 0; i<services.size(); i++){
			crawlRawCatalogService(cleanCatalogId, services.get(i));
		}
		ArrayList<Dataset> datasets = DataAccess.getDatasetBCatalog(catalogId);
		for(int i = 0; i<datasets.size(); i++){
			crawlRawCatalogDataset(cleanCatalogId, datasets.get(i));
		}
		ArrayList<CatalogProperty> propertys = DataAccess.getCatalogPropertyBCatalog(catalogId);
		for(int i = 0; i<propertys.size(); i++){
			crawlRawCatalogProperty(cleanCatalogId, propertys.get(i));
		}
		ArrayList<CatalogXlink> xlinks = DataAccess.getCatalogXlinkBCatalog(catalogId);
		for(int i = 0; i<xlinks.size(); i++){
			crawlRawCatalogXlink(cleanCatalogId, xlinks.get(i));
		}	
		return cleanCatalogId;
	}
	
	public void crawlNewTmgCreatorContact(int parentId, Element child) throws Exception{
		Datavalue email = new Datavalue();
		Datavalue url = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("email"))
				email.NOTNULL(a.getValue());
			else if(a.getName().equals("url"))
				url.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("email"))
				email.NOTNULL(a.getValue());
			else if(a.getName().equals("url"))
				url.NOTNULL(a.getValue());
		}

		DataAccess.insertTmgCreatorContact(parentId, email, url);

	}
	public void crawlRawTmgCreatorContact(int cleanTmgCreatorId, TmgCreatorContact rawTmgCreatorContact) throws Exception{
		TmgCreatorContact cleanTmgCreatorContact = rawTmgCreatorContact.clone();
		cleanTmgCreatorContact.setTmgCreatorId(cleanTmgCreatorId);
		int cleanTmgCreatorContactId = DataAccess.insertTmgCreatorContact(cleanTmgCreatorContact);
		cleanTmgCreatorContact.setTmgCreatorContactId(cleanTmgCreatorContactId);

		cleanTmgCreatorContact = Applier.applyTmgCreatorContactRules(cleanTmgCreatorId, cleanTmgCreatorContact);

		if(cleanTmgCreatorContact == null){
			DataAccess.deleteTmgCreatorContact(cleanTmgCreatorContactId);
			return;
		}
		DataAccess.updateTmgCreatorContact(cleanTmgCreatorContact);
	}
	public void crawlNewTmgCreatorName(int parentId, Element child) throws Exception{
		Datavalue value = new Datavalue();
		Datavalue vocabulary = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("vocabulary"))
				vocabulary.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("vocabulary"))
				vocabulary.NOTNULL(a.getValue());
		}
		String val = "";
		ArrayList<org.jdom.Text> values3 = getContent(child.getContent());
		for(int i=0; i<values3.size(); i++){
			val += values3.get(i).getText();
		}
		value.NOTNULL(val);

		DataAccess.insertTmgCreatorName(parentId, value, vocabulary);

	}
	public void crawlRawTmgCreatorName(int cleanTmgCreatorId, TmgCreatorName rawTmgCreatorName) throws Exception{
		TmgCreatorName cleanTmgCreatorName = rawTmgCreatorName.clone();
		cleanTmgCreatorName.setTmgCreatorId(cleanTmgCreatorId);
		int cleanTmgCreatorNameId = DataAccess.insertTmgCreatorName(cleanTmgCreatorName);
		cleanTmgCreatorName.setTmgCreatorNameId(cleanTmgCreatorNameId);

		cleanTmgCreatorName = Applier.applyTmgCreatorNameRules(cleanTmgCreatorId, cleanTmgCreatorName);

		if(cleanTmgCreatorName == null){
			DataAccess.deleteTmgCreatorName(cleanTmgCreatorNameId);
			return;
		}
		DataAccess.updateTmgCreatorName(cleanTmgCreatorName);
	}
	public void crawlNewTmgTimecoverageDuration(int parentId, Element child) throws Exception{
		Datavalue duration = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("duration"))
				duration.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("duration"))
				duration.NOTNULL(a.getValue());
		}

		DataAccess.insertTmgTimecoverageDuration(parentId, duration);

	}
	public void crawlRawTmgTimecoverageDuration(int cleanTmgTimecoverageId, TmgTimecoverageDuration rawTmgTimecoverageDuration) throws Exception{
		TmgTimecoverageDuration cleanTmgTimecoverageDuration = rawTmgTimecoverageDuration.clone();
		cleanTmgTimecoverageDuration.setTmgTimecoverageId(cleanTmgTimecoverageId);
		int cleanTmgTimecoverageDurationId = DataAccess.insertTmgTimecoverageDuration(cleanTmgTimecoverageDuration);
		cleanTmgTimecoverageDuration.setTmgTimecoverageDurationId(cleanTmgTimecoverageDurationId);

		cleanTmgTimecoverageDuration = Applier.applyTmgTimecoverageDurationRules(cleanTmgTimecoverageId, cleanTmgTimecoverageDuration);

		if(cleanTmgTimecoverageDuration == null){
			DataAccess.deleteTmgTimecoverageDuration(cleanTmgTimecoverageDurationId);
			return;
		}
		DataAccess.updateTmgTimecoverageDuration(cleanTmgTimecoverageDuration);
	}
	public void crawlNewTmgTimecoverageEnd(int parentId, Element child) throws Exception{
		Datavalue format = new Datavalue();
		Datavalue value = new Datavalue();
		Datavalue dateenum = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("format"))
				format.NOTNULL(a.getValue());
			else if(a.getName().equals("dateenum"))
				dateenum.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("format"))
				format.NOTNULL(a.getValue());
			else if(a.getName().equals("dateenum"))
				dateenum.NOTNULL(a.getValue());
		}
		String val = "";
		ArrayList<org.jdom.Text> values3 = getContent(child.getContent());
		for(int i=0; i<values3.size(); i++){
			val += values3.get(i).getText();
		}
		value.NOTNULL(val);

		DataAccess.insertTmgTimecoverageEnd(parentId, format, value, dateenum);

	}
	public void crawlRawTmgTimecoverageEnd(int cleanTmgTimecoverageId, TmgTimecoverageEnd rawTmgTimecoverageEnd) throws Exception{
		TmgTimecoverageEnd cleanTmgTimecoverageEnd = rawTmgTimecoverageEnd.clone();
		cleanTmgTimecoverageEnd.setTmgTimecoverageId(cleanTmgTimecoverageId);
		int cleanTmgTimecoverageEndId = DataAccess.insertTmgTimecoverageEnd(cleanTmgTimecoverageEnd);
		cleanTmgTimecoverageEnd.setTmgTimecoverageEndId(cleanTmgTimecoverageEndId);

		cleanTmgTimecoverageEnd = Applier.applyTmgTimecoverageEndRules(cleanTmgTimecoverageId, cleanTmgTimecoverageEnd);

		if(cleanTmgTimecoverageEnd == null){
			DataAccess.deleteTmgTimecoverageEnd(cleanTmgTimecoverageEndId);
			return;
		}
		DataAccess.updateTmgTimecoverageEnd(cleanTmgTimecoverageEnd);
	}
	public void crawlNewTmgTimecoverageResolution(int parentId, Element child) throws Exception{
		Datavalue duration = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("duration"))
				duration.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("duration"))
				duration.NOTNULL(a.getValue());
		}

		DataAccess.insertTmgTimecoverageResolution(parentId, duration);

	}
	public void crawlRawTmgTimecoverageResolution(int cleanTmgTimecoverageId, TmgTimecoverageResolution rawTmgTimecoverageResolution) throws Exception{
		TmgTimecoverageResolution cleanTmgTimecoverageResolution = rawTmgTimecoverageResolution.clone();
		cleanTmgTimecoverageResolution.setTmgTimecoverageId(cleanTmgTimecoverageId);
		int cleanTmgTimecoverageResolutionId = DataAccess.insertTmgTimecoverageResolution(cleanTmgTimecoverageResolution);
		cleanTmgTimecoverageResolution.setTmgTimecoverageResolutionId(cleanTmgTimecoverageResolutionId);

		cleanTmgTimecoverageResolution = Applier.applyTmgTimecoverageResolutionRules(cleanTmgTimecoverageId, cleanTmgTimecoverageResolution);

		if(cleanTmgTimecoverageResolution == null){
			DataAccess.deleteTmgTimecoverageResolution(cleanTmgTimecoverageResolutionId);
			return;
		}
		DataAccess.updateTmgTimecoverageResolution(cleanTmgTimecoverageResolution);
	}
	public void crawlNewTmgTimecoverageStart(int parentId, Element child) throws Exception{
		Datavalue format = new Datavalue();
		Datavalue value = new Datavalue();
		Datavalue dateenum = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("format"))
				format.NOTNULL(a.getValue());
			else if(a.getName().equals("dateenum"))
				dateenum.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("format"))
				format.NOTNULL(a.getValue());
			else if(a.getName().equals("dateenum"))
				dateenum.NOTNULL(a.getValue());
		}
		String val = "";
		ArrayList<org.jdom.Text> values3 = getContent(child.getContent());
		for(int i=0; i<values3.size(); i++){
			val += values3.get(i).getText();
		}
		value.NOTNULL(val);

		DataAccess.insertTmgTimecoverageStart(parentId, format, value, dateenum);

	}
	public void crawlRawTmgTimecoverageStart(int cleanTmgTimecoverageId, TmgTimecoverageStart rawTmgTimecoverageStart) throws Exception{
		TmgTimecoverageStart cleanTmgTimecoverageStart = rawTmgTimecoverageStart.clone();
		cleanTmgTimecoverageStart.setTmgTimecoverageId(cleanTmgTimecoverageId);
		int cleanTmgTimecoverageStartId = DataAccess.insertTmgTimecoverageStart(cleanTmgTimecoverageStart);
		cleanTmgTimecoverageStart.setTmgTimecoverageStartId(cleanTmgTimecoverageStartId);

		cleanTmgTimecoverageStart = Applier.applyTmgTimecoverageStartRules(cleanTmgTimecoverageId, cleanTmgTimecoverageStart);

		if(cleanTmgTimecoverageStart == null){
			DataAccess.deleteTmgTimecoverageStart(cleanTmgTimecoverageStartId);
			return;
		}
		DataAccess.updateTmgTimecoverageStart(cleanTmgTimecoverageStart);
	}
	public void crawlNewMetadataNamespace(int parentId, Element child) throws Exception{
		Datavalue namespace = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("namespace"))
				namespace.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("namespace"))
				namespace.NOTNULL(a.getValue());
		}

		DataAccess.insertMetadataNamespace(parentId, namespace);

	}
	public void crawlRawMetadataNamespace(int cleanMetadataId, MetadataNamespace rawMetadataNamespace) throws Exception{
		MetadataNamespace cleanMetadataNamespace = rawMetadataNamespace.clone();
		cleanMetadataNamespace.setMetadataId(cleanMetadataId);
		int cleanMetadataNamespaceId = DataAccess.insertMetadataNamespace(cleanMetadataNamespace);
		cleanMetadataNamespace.setMetadataNamespaceId(cleanMetadataNamespaceId);

		cleanMetadataNamespace = Applier.applyMetadataNamespaceRules(cleanMetadataId, cleanMetadataNamespace);

		if(cleanMetadataNamespace == null){
			DataAccess.deleteMetadataNamespace(cleanMetadataNamespaceId);
			return;
		}
		DataAccess.updateMetadataNamespace(cleanMetadataNamespace);
	}
	public void crawlNewMetadataTmg(int parentId, Element child) throws Exception{


		int childId = DataAccess.insertTmg();

		DataAccess.insertMetadataTmg(parentId, childId);

		ArrayList<Element> all = getElements(child.getChildren());
		for(int i = 0; i<all.size(); i++){
			Element newchild = all.get(i);
			if(newchild.getName().equals("authority"))
				crawlNewTmgAuthority(childId, newchild);
			else if(newchild.getName().equals("contributor"))
				crawlNewTmgContributor(childId, newchild);
			else if(newchild.getName().equals("creator"))
				crawlNewTmgCreator(childId, newchild);
			else if(newchild.getName().equals("dataformat"))
				crawlNewTmgDataformat(childId, newchild);
			else if(newchild.getName().equals("datasize"))
				crawlNewTmgDatasize(childId, newchild);
			else if(newchild.getName().equals("datatype"))
				crawlNewTmgDatatype(childId, newchild);
			else if(newchild.getName().equals("date"))
				crawlNewTmgDate(childId, newchild);
			else if(newchild.getName().equals("documentation"))
				crawlNewTmgDocumentation(childId, newchild);
			else if(newchild.getName().equals("geospatialcoverage"))
				crawlNewTmgGeospatialcoverage(childId, newchild);
			else if(newchild.getName().equals("keyword"))
				crawlNewTmgKeyword(childId, newchild);
			else if(newchild.getName().equals("metadata"))
				crawlNewTmgMetadata(childId, newchild);
			else if(newchild.getName().equals("project"))
				crawlNewTmgProject(childId, newchild);
			else if(newchild.getName().equals("property"))
				crawlNewTmgProperty(childId, newchild);
			else if(newchild.getName().equals("publisher"))
				crawlNewTmgPublisher(childId, newchild);
			else if(newchild.getName().equals("servicename"))
				crawlNewTmgServicename(childId, newchild);
			else if(newchild.getName().equals("timecoverage"))
				crawlNewTmgTimecoverage(childId, newchild);
			else if(newchild.getName().equals("variables"))
				crawlNewTmgVariables(childId, newchild);
		}
	}
	public void crawlRawMetadataTmg(int cleanMetadataId, Tmg rawTmg) throws Exception{
		Tmg cleanTmg = rawTmg.clone();
		int cleanTmgId = DataAccess.insertTmg(cleanTmg);
		cleanTmg.setTmgId(cleanTmgId);

		cleanTmg = Applier.applyMetadataTmgRules(cleanMetadataId, cleanTmg);

		if(cleanTmg == null){
			DataAccess.deleteTmg(cleanTmgId);
			return;
		}
		DataAccess.updateTmg(cleanTmg);
		DataAccess.insertMetadataTmg(cleanMetadataId, cleanTmgId);
		int rawTmgId = rawTmg.getTmgId();

		ArrayList<TmgAuthority> authoritys = DataAccess.getTmgAuthorityBTmg(rawTmgId);
		for(int i = 0; i<authoritys.size(); i++){
			crawlRawTmgAuthority(cleanTmgId, authoritys.get(i));
		}
		ArrayList<TmgContributor> contributors = DataAccess.getTmgContributorBTmg(rawTmgId);
		for(int i = 0; i<contributors.size(); i++){
			crawlRawTmgContributor(cleanTmgId, contributors.get(i));
		}
		ArrayList<TmgCreator> creators = DataAccess.getTmgCreatorBTmg(rawTmgId);
		for(int i = 0; i<creators.size(); i++){
			crawlRawTmgCreator(cleanTmgId, creators.get(i));
		}
		ArrayList<TmgDataformat> dataformats = DataAccess.getTmgDataformatBTmg(rawTmgId);
		for(int i = 0; i<dataformats.size(); i++){
			crawlRawTmgDataformat(cleanTmgId, dataformats.get(i));
		}
		ArrayList<TmgDatasize> datasizes = DataAccess.getTmgDatasizeBTmg(rawTmgId);
		for(int i = 0; i<datasizes.size(); i++){
			crawlRawTmgDatasize(cleanTmgId, datasizes.get(i));
		}
		ArrayList<TmgDatatype> datatypes = DataAccess.getTmgDatatypeBTmg(rawTmgId);
		for(int i = 0; i<datatypes.size(); i++){
			crawlRawTmgDatatype(cleanTmgId, datatypes.get(i));
		}
		ArrayList<TmgDate> dates = DataAccess.getTmgDateBTmg(rawTmgId);
		for(int i = 0; i<dates.size(); i++){
			crawlRawTmgDate(cleanTmgId, dates.get(i));
		}
		ArrayList<TmgDocumentation> documentations = DataAccess.getTmgDocumentationBTmg(rawTmgId);
		for(int i = 0; i<documentations.size(); i++){
			crawlRawTmgDocumentation(cleanTmgId, documentations.get(i));
		}
		ArrayList<TmgGeospatialcoverage> geospatialcoverages = DataAccess.getTmgGeospatialcoverageBTmg(rawTmgId);
		for(int i = 0; i<geospatialcoverages.size(); i++){
			crawlRawTmgGeospatialcoverage(cleanTmgId, geospatialcoverages.get(i));
		}
		ArrayList<TmgKeyword> keywords = DataAccess.getTmgKeywordBTmg(rawTmgId);
		for(int i = 0; i<keywords.size(); i++){
			crawlRawTmgKeyword(cleanTmgId, keywords.get(i));
		}
		ArrayList<Metadata> metadatas = DataAccess.getMetadataBTmg(rawTmgId);
		for(int i = 0; i<metadatas.size(); i++){
			crawlRawTmgMetadata(cleanTmgId, metadatas.get(i));
		}
		ArrayList<TmgProject> projects = DataAccess.getTmgProjectBTmg(rawTmgId);
		for(int i = 0; i<projects.size(); i++){
			crawlRawTmgProject(cleanTmgId, projects.get(i));
		}
		ArrayList<TmgProperty> propertys = DataAccess.getTmgPropertyBTmg(rawTmgId);
		for(int i = 0; i<propertys.size(); i++){
			crawlRawTmgProperty(cleanTmgId, propertys.get(i));
		}
		ArrayList<TmgPublisher> publishers = DataAccess.getTmgPublisherBTmg(rawTmgId);
		for(int i = 0; i<publishers.size(); i++){
			crawlRawTmgPublisher(cleanTmgId, publishers.get(i));
		}
		ArrayList<TmgServicename> servicenames = DataAccess.getTmgServicenameBTmg(rawTmgId);
		for(int i = 0; i<servicenames.size(); i++){
			crawlRawTmgServicename(cleanTmgId, servicenames.get(i));
		}
		ArrayList<TmgTimecoverage> timecoverages = DataAccess.getTmgTimecoverageBTmg(rawTmgId);
		for(int i = 0; i<timecoverages.size(); i++){
			crawlRawTmgTimecoverage(cleanTmgId, timecoverages.get(i));
		}
		ArrayList<TmgVariables> variabless = DataAccess.getTmgVariablesBTmg(rawTmgId);
		for(int i = 0; i<variabless.size(); i++){
			crawlRawTmgVariables(cleanTmgId, variabless.get(i));
		}
	}
	public void crawlNewMetadataXlink(int parentId, Element child) throws Exception{
		Datavalue value = new Datavalue();
		Datavalue xlink = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("xlink"))
				xlink.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("xlink"))
				xlink.NOTNULL(a.getValue());
		}
		String val = "";
		ArrayList<org.jdom.Text> values3 = getContent(child.getContent());
		for(int i=0; i<values3.size(); i++){
			val += values3.get(i).getText();
		}
		value.NOTNULL(val);

		DataAccess.insertMetadataXlink(parentId, value, xlink);

	}
	public void crawlRawMetadataXlink(int cleanMetadataId, MetadataXlink rawMetadataXlink) throws Exception{
		MetadataXlink cleanMetadataXlink = rawMetadataXlink.clone();
		cleanMetadataXlink.setMetadataId(cleanMetadataId);
		int cleanMetadataXlinkId = DataAccess.insertMetadataXlink(cleanMetadataXlink);
		cleanMetadataXlink.setMetadataXlinkId(cleanMetadataXlinkId);

		cleanMetadataXlink = Applier.applyMetadataXlinkRules(cleanMetadataId, cleanMetadataXlink);

		if(cleanMetadataXlink == null){
			DataAccess.deleteMetadataXlink(cleanMetadataXlinkId);
			return;
		}
		DataAccess.updateMetadataXlink(cleanMetadataXlink);
	}
	public void crawlNewTmgVariablesVariable(int parentId, Element child) throws Exception{
		Datavalue name = new Datavalue();
		Datavalue units = new Datavalue();
		Datavalue vocabularyName = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
			else if(a.getName().equals("units"))
				units.NOTNULL(a.getValue());
			else if(a.getName().equals("vocabulary_name"))
				vocabularyName.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
			else if(a.getName().equals("units"))
				units.NOTNULL(a.getValue());
			else if(a.getName().equals("vocabulary_name"))
				vocabularyName.NOTNULL(a.getValue());
		}

		DataAccess.insertTmgVariablesVariable(parentId, name, units, vocabularyName);

	}
	public void crawlRawTmgVariablesVariable(int cleanTmgVariablesId, TmgVariablesVariable rawTmgVariablesVariable) throws Exception{
		TmgVariablesVariable cleanTmgVariablesVariable = rawTmgVariablesVariable.clone();
		cleanTmgVariablesVariable.setTmgVariablesId(cleanTmgVariablesId);
		int cleanTmgVariablesVariableId = DataAccess.insertTmgVariablesVariable(cleanTmgVariablesVariable);
		cleanTmgVariablesVariable.setTmgVariablesVariableId(cleanTmgVariablesVariableId);

		cleanTmgVariablesVariable = Applier.applyTmgVariablesVariableRules(cleanTmgVariablesId, cleanTmgVariablesVariable);

		if(cleanTmgVariablesVariable == null){
			DataAccess.deleteTmgVariablesVariable(cleanTmgVariablesVariableId);
			return;
		}
		DataAccess.updateTmgVariablesVariable(cleanTmgVariablesVariable);
	}
	public void crawlNewTmgVariablesVariablemap(int parentId, Element child) throws Exception{
		Datavalue value = new Datavalue();
		Datavalue xlink = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("xlink"))
				xlink.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("xlink"))
				xlink.NOTNULL(a.getValue());
		}
		String val = "";
		ArrayList<org.jdom.Text> values3 = getContent(child.getContent());
		for(int i=0; i<values3.size(); i++){
			val += values3.get(i).getText();
		}
		value.NOTNULL(val);

		DataAccess.insertTmgVariablesVariablemap(parentId, value, xlink);

	}
	public void crawlRawTmgVariablesVariablemap(int cleanTmgVariablesId, TmgVariablesVariablemap rawTmgVariablesVariablemap) throws Exception{
		TmgVariablesVariablemap cleanTmgVariablesVariablemap = rawTmgVariablesVariablemap.clone();
		cleanTmgVariablesVariablemap.setTmgVariablesId(cleanTmgVariablesId);
		int cleanTmgVariablesVariablemapId = DataAccess.insertTmgVariablesVariablemap(cleanTmgVariablesVariablemap);
		cleanTmgVariablesVariablemap.setTmgVariablesVariablemapId(cleanTmgVariablesVariablemapId);

		cleanTmgVariablesVariablemap = Applier.applyTmgVariablesVariablemapRules(cleanTmgVariablesId, cleanTmgVariablesVariablemap);

		if(cleanTmgVariablesVariablemap == null){
			DataAccess.deleteTmgVariablesVariablemap(cleanTmgVariablesVariablemapId);
			return;
		}
		DataAccess.updateTmgVariablesVariablemap(cleanTmgVariablesVariablemap);
	}
	public void crawlNewServiceDatasetroot(int parentId, Element child) throws Exception{
		Datavalue location = new Datavalue();
		Datavalue path = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("location"))
				location.NOTNULL(a.getValue());
			else if(a.getName().equals("path"))
				path.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("location"))
				location.NOTNULL(a.getValue());
			else if(a.getName().equals("path"))
				path.NOTNULL(a.getValue());
		}

		DataAccess.insertServiceDatasetroot(parentId, location, path);

	}
	public void crawlRawServiceDatasetroot(int cleanServiceId, ServiceDatasetroot rawServiceDatasetroot) throws Exception{
		ServiceDatasetroot cleanServiceDatasetroot = rawServiceDatasetroot.clone();
		cleanServiceDatasetroot.setServiceId(cleanServiceId);
		int cleanServiceDatasetrootId = DataAccess.insertServiceDatasetroot(cleanServiceDatasetroot);
		cleanServiceDatasetroot.setServiceDatasetrootId(cleanServiceDatasetrootId);

		cleanServiceDatasetroot = Applier.applyServiceDatasetrootRules(cleanServiceId, cleanServiceDatasetroot);

		if(cleanServiceDatasetroot == null){
			DataAccess.deleteServiceDatasetroot(cleanServiceDatasetrootId);
			return;
		}
		DataAccess.updateServiceDatasetroot(cleanServiceDatasetroot);
	}
	public void crawlNewServiceProperty(int parentId, Element child) throws Exception{
		Datavalue name = new Datavalue();
		Datavalue value = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
		}
		String val = "";
		ArrayList<org.jdom.Text> values3 = getContent(child.getContent());
		for(int i=0; i<values3.size(); i++){
			val += values3.get(i).getText();
		}
		value.NOTNULL(val);

		DataAccess.insertServiceProperty(parentId, name, value);

	}
	public void crawlRawServiceProperty(int cleanServiceId, ServiceProperty rawServiceProperty) throws Exception{
		ServiceProperty cleanServiceProperty = rawServiceProperty.clone();
		cleanServiceProperty.setServiceId(cleanServiceId);
		int cleanServicePropertyId = DataAccess.insertServiceProperty(cleanServiceProperty);
		cleanServiceProperty.setServicePropertyId(cleanServicePropertyId);

		cleanServiceProperty = Applier.applyServicePropertyRules(cleanServiceId, cleanServiceProperty);

		if(cleanServiceProperty == null){
			DataAccess.deleteServiceProperty(cleanServicePropertyId);
			return;
		}
		DataAccess.updateServiceProperty(cleanServiceProperty);
	}
	public void crawlNewServiceService(int parentId, Element child) throws Exception{
		Datavalue base = new Datavalue();
		Datavalue desc = new Datavalue();
		Datavalue name = new Datavalue();
		Datavalue suffix = new Datavalue();
		Datavalue serviceType = new Datavalue();
		Datavalue status = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("base"))
				base.NOTNULL(a.getValue());
			else if(a.getName().equals("desc"))
				desc.NOTNULL(a.getValue());
			else if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
			else if(a.getName().equals("suffix"))
				suffix.NOTNULL(a.getValue());
			else if(a.getName().equals("serviceType"))
				serviceType.NOTNULL(a.getValue());
			else if(a.getName().equals("status"))
				status.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("base"))
				base.NOTNULL(a.getValue());
			else if(a.getName().equals("desc"))
				desc.NOTNULL(a.getValue());
			else if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
			else if(a.getName().equals("suffix"))
				suffix.NOTNULL(a.getValue());
			else if(a.getName().equals("serviceType"))
				serviceType.NOTNULL(a.getValue());
			else if(a.getName().equals("status"))
				status.NOTNULL(a.getValue());
		}

		int childId = DataAccess.insertService(base, desc, name, suffix, serviceType, status);

		DataAccess.insertServiceService(parentId, childId);

		ArrayList<Element> all = getElements(child.getChildren());
		for(int i = 0; i<all.size(); i++){
			Element newchild = all.get(i);
			if(newchild.getName().equals("datasetroot"))
				crawlNewServiceDatasetroot(childId, newchild);
			else if(newchild.getName().equals("property"))
				crawlNewServiceProperty(childId, newchild);
			else if(newchild.getName().equals("service"))
				crawlNewServiceService(childId, newchild);
		}
	}
	public void crawlRawServiceService(int cleanparentId, Service rawService) throws Exception{
		Service cleanService = rawService.clone();
		int cleanServiceId = DataAccess.insertService(cleanService);
		cleanService.setServiceId(cleanServiceId);

		cleanService = Applier.applyServiceServiceRules(cleanparentId, cleanService);

		if(cleanService == null){
			DataAccess.deleteService(cleanServiceId);
			return;
		}
		DataAccess.updateService(cleanService);
		DataAccess.insertServiceService(cleanparentId, cleanServiceId);
		int rawServiceId = rawService.getServiceId();

		ArrayList<ServiceDatasetroot> datasetroots = DataAccess.getServiceDatasetrootBService(rawServiceId);
		for(int i = 0; i<datasetroots.size(); i++){
			crawlRawServiceDatasetroot(cleanServiceId, datasetroots.get(i));
		}
		ArrayList<ServiceProperty> propertys = DataAccess.getServicePropertyBService(rawServiceId);
		for(int i = 0; i<propertys.size(); i++){
			crawlRawServiceProperty(cleanServiceId, propertys.get(i));
		}
		ArrayList<Service> services = DataAccess.getServiceBService(rawServiceId);
		for(int i = 0; i<services.size(); i++){
			crawlRawServiceService(cleanServiceId, services.get(i));
		}
	}
	public void crawlNewDatasetAccessDatasize(int parentId, Element child) throws Exception{
		Datavalue value = new Datavalue();
		Datavalue units = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("units"))
				units.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("units"))
				units.NOTNULL(a.getValue());
		}
		String val = "";
		ArrayList<org.jdom.Text> values3 = getContent(child.getContent());
		for(int i=0; i<values3.size(); i++){
			val += values3.get(i).getText();
		}
		value.NOTNULL(val);

		DataAccess.insertDatasetAccessDatasize(parentId, value, units);

	}
	public void crawlRawDatasetAccessDatasize(int cleanDatasetAccessId, DatasetAccessDatasize rawDatasetAccessDatasize) throws Exception{
		DatasetAccessDatasize cleanDatasetAccessDatasize = rawDatasetAccessDatasize.clone();
		cleanDatasetAccessDatasize.setDatasetAccessId(cleanDatasetAccessId);
		int cleanDatasetAccessDatasizeId = DataAccess.insertDatasetAccessDatasize(cleanDatasetAccessDatasize);
		cleanDatasetAccessDatasize.setDatasetAccessDatasizeId(cleanDatasetAccessDatasizeId);

		cleanDatasetAccessDatasize = Applier.applyDatasetAccessDatasizeRules(cleanDatasetAccessId, cleanDatasetAccessDatasize);

		if(cleanDatasetAccessDatasize == null){
			DataAccess.deleteDatasetAccessDatasize(cleanDatasetAccessDatasizeId);
			return;
		}
		DataAccess.updateDatasetAccessDatasize(cleanDatasetAccessDatasize);
	}
	public void crawlNewCatalogDataset(int parentId, Element child) throws Exception{
		Datavalue alias = new Datavalue();
		Datavalue authority = new Datavalue();
		Datavalue dId = new Datavalue();
		Datavalue harvest = new Datavalue();
		Datavalue name = new Datavalue();
		Datavalue resourcecontrol = new Datavalue();
		Datavalue serviceName = new Datavalue();
		Datavalue urlPath = new Datavalue();
		Datavalue collectiontype = new Datavalue();
		Datavalue datasizeUnit = new Datavalue();
		Datavalue dataType = new Datavalue();
		Datavalue status = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("alias"))
				alias.NOTNULL(a.getValue());
			else if(a.getName().equals("authority"))
				authority.NOTNULL(a.getValue());
			else if(a.getName().equals("ID"))
				dId.NOTNULL(a.getValue());
			else if(a.getName().equals("harvest"))
				harvest.NOTNULL(a.getValue());
			else if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
			else if(a.getName().equals("resourcecontrol"))
				resourcecontrol.NOTNULL(a.getValue());
			else if(a.getName().equals("serviceName"))
				serviceName.NOTNULL(a.getValue());
			else if(a.getName().equals("urlPath"))
				urlPath.NOTNULL(a.getValue());
			else if(a.getName().equals("collectiontype"))
				collectiontype.NOTNULL(a.getValue());
			else if(a.getName().equals("datasize_unit"))
				datasizeUnit.NOTNULL(a.getValue());
			else if(a.getName().equals("dataType"))
				dataType.NOTNULL(a.getValue());
			else if(a.getName().equals("status"))
				status.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("alias"))
				alias.NOTNULL(a.getValue());
			else if(a.getName().equals("authority"))
				authority.NOTNULL(a.getValue());
			else if(a.getName().equals("ID"))
				dId.NOTNULL(a.getValue());
			else if(a.getName().equals("harvest"))
				harvest.NOTNULL(a.getValue());
			else if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
			else if(a.getName().equals("resourcecontrol"))
				resourcecontrol.NOTNULL(a.getValue());
			else if(a.getName().equals("serviceName"))
				serviceName.NOTNULL(a.getValue());
			else if(a.getName().equals("urlPath"))
				urlPath.NOTNULL(a.getValue());
			else if(a.getName().equals("collectiontype"))
				collectiontype.NOTNULL(a.getValue());
			else if(a.getName().equals("datasize_unit"))
				datasizeUnit.NOTNULL(a.getValue());
			else if(a.getName().equals("dataType"))
				dataType.NOTNULL(a.getValue());
			else if(a.getName().equals("status"))
				status.NOTNULL(a.getValue());
		}

		int childId = DataAccess.insertDataset(alias, authority, dId, harvest, name, resourcecontrol, serviceName, urlPath, collectiontype, datasizeUnit, dataType, status);

		DataAccess.insertCatalogDataset(parentId, childId);

		ArrayList<Element> all = getElements(child.getChildren());
		for(int i = 0; i<all.size(); i++){
			Element newchild = all.get(i);
			if(newchild.getName().equals("access"))
				crawlNewDatasetAccess(childId, newchild);
			else if(newchild.getName().equals("dataset"))
				crawlNewDatasetDataset(childId, newchild);
			else if(newchild.getName().equals("ncml"))
				crawlNewDatasetNcml(childId, newchild);
			else if(newchild.getName().equals("property"))
				crawlNewDatasetProperty(childId, newchild);
			else if(newchild.getName().equals("service"))
				crawlNewDatasetService(childId, newchild);
		}
		crawlNewDatasetTmg(childId, child);
	}
	public void crawlRawCatalogDataset(int cleanCatalogId, Dataset rawDataset) throws Exception{
		Dataset cleanDataset = rawDataset.clone();
		int cleanDatasetId = DataAccess.insertDataset(cleanDataset);
		cleanDataset.setDatasetId(cleanDatasetId);

		cleanDataset = Applier.applyCatalogDatasetRules(cleanCatalogId, cleanDataset);

		if(cleanDataset == null){
			DataAccess.deleteDataset(cleanDatasetId);
			return;
		}
		DataAccess.updateDataset(cleanDataset);
		DataAccess.insertCatalogDataset(cleanCatalogId, cleanDatasetId);
		int rawDatasetId = rawDataset.getDatasetId();

		ArrayList<DatasetAccess> accesss = DataAccess.getDatasetAccessBDataset(rawDatasetId);
		for(int i = 0; i<accesss.size(); i++){
			crawlRawDatasetAccess(cleanDatasetId, accesss.get(i));
		}
		ArrayList<Dataset> datasets = DataAccess.getDatasetBDataset(rawDatasetId);
		for(int i = 0; i<datasets.size(); i++){
			crawlRawDatasetDataset(cleanDatasetId, datasets.get(i));
		}
		ArrayList<DatasetNcml> ncmls = DataAccess.getDatasetNcmlBDataset(rawDatasetId);
		for(int i = 0; i<ncmls.size(); i++){
			crawlRawDatasetNcml(cleanDatasetId, ncmls.get(i));
		}
		ArrayList<DatasetProperty> propertys = DataAccess.getDatasetPropertyBDataset(rawDatasetId);
		for(int i = 0; i<propertys.size(); i++){
			crawlRawDatasetProperty(cleanDatasetId, propertys.get(i));
		}
		ArrayList<Service> services = DataAccess.getServiceBDataset(rawDatasetId);
		for(int i = 0; i<services.size(); i++){
			crawlRawDatasetService(cleanDatasetId, services.get(i));
		}
		ArrayList<Tmg> tmgs = DataAccess.getTmgBDataset(rawDatasetId);
		for(int i = 0; i<tmgs.size(); i++){
			crawlRawDatasetTmg(cleanDatasetId, tmgs.get(i));
		}
	}
	public void crawlNewCatalogProperty(int parentId, Element child) throws Exception{
		Datavalue name = new Datavalue();
		Datavalue value = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
		}
		String val = "";
		ArrayList<org.jdom.Text> values3 = getContent(child.getContent());
		for(int i=0; i<values3.size(); i++){
			val += values3.get(i).getText();
		}
		value.NOTNULL(val);

		DataAccess.insertCatalogProperty(parentId, name, value);

	}
	public void crawlRawCatalogProperty(int cleanCatalogId, CatalogProperty rawCatalogProperty) throws Exception{
		CatalogProperty cleanCatalogProperty = rawCatalogProperty.clone();
		cleanCatalogProperty.setCatalogId(cleanCatalogId);
		int cleanCatalogPropertyId = DataAccess.insertCatalogProperty(cleanCatalogProperty);
		cleanCatalogProperty.setCatalogPropertyId(cleanCatalogPropertyId);

		cleanCatalogProperty = Applier.applyCatalogPropertyRules(cleanCatalogId, cleanCatalogProperty);

		if(cleanCatalogProperty == null){
			DataAccess.deleteCatalogProperty(cleanCatalogPropertyId);
			return;
		}
		DataAccess.updateCatalogProperty(cleanCatalogProperty);
	}
	public void crawlNewCatalogService(int parentId, Element child) throws Exception{
		Datavalue base = new Datavalue();
		Datavalue desc = new Datavalue();
		Datavalue name = new Datavalue();
		Datavalue suffix = new Datavalue();
		Datavalue serviceType = new Datavalue();
		Datavalue status = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("base"))
				base.NOTNULL(a.getValue());
			else if(a.getName().equals("desc"))
				desc.NOTNULL(a.getValue());
			else if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
			else if(a.getName().equals("suffix"))
				suffix.NOTNULL(a.getValue());
			else if(a.getName().equals("serviceType"))
				serviceType.NOTNULL(a.getValue());
			else if(a.getName().equals("status"))
				status.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("base"))
				base.NOTNULL(a.getValue());
			else if(a.getName().equals("desc"))
				desc.NOTNULL(a.getValue());
			else if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
			else if(a.getName().equals("suffix"))
				suffix.NOTNULL(a.getValue());
			else if(a.getName().equals("serviceType"))
				serviceType.NOTNULL(a.getValue());
			else if(a.getName().equals("status"))
				status.NOTNULL(a.getValue());
		}

		int childId = DataAccess.insertService(base, desc, name, suffix, serviceType, status);

		DataAccess.insertCatalogService(parentId, childId);

		ArrayList<Element> all = getElements(child.getChildren());
		for(int i = 0; i<all.size(); i++){
			Element newchild = all.get(i);
			if(newchild.getName().equals("datasetroot"))
				crawlNewServiceDatasetroot(childId, newchild);
			else if(newchild.getName().equals("property"))
				crawlNewServiceProperty(childId, newchild);
			else if(newchild.getName().equals("service"))
				crawlNewServiceService(childId, newchild);
		}
	}
	public void crawlRawCatalogService(int cleanCatalogId, Service rawService) throws Exception{
		Service cleanService = rawService.clone();
		int cleanServiceId = DataAccess.insertService(cleanService);
		cleanService.setServiceId(cleanServiceId);

		cleanService = Applier.applyCatalogServiceRules(cleanCatalogId, cleanService);

		if(cleanService == null){
			DataAccess.deleteService(cleanServiceId);
			return;
		}
		DataAccess.updateService(cleanService);
		DataAccess.insertCatalogService(cleanCatalogId, cleanServiceId);
		int rawServiceId = rawService.getServiceId();

		ArrayList<ServiceDatasetroot> datasetroots = DataAccess.getServiceDatasetrootBService(rawServiceId);
		for(int i = 0; i<datasetroots.size(); i++){
			crawlRawServiceDatasetroot(cleanServiceId, datasetroots.get(i));
		}
		ArrayList<ServiceProperty> propertys = DataAccess.getServicePropertyBService(rawServiceId);
		for(int i = 0; i<propertys.size(); i++){
			crawlRawServiceProperty(cleanServiceId, propertys.get(i));
		}
		ArrayList<Service> services = DataAccess.getServiceBService(rawServiceId);
		for(int i = 0; i<services.size(); i++){
			crawlRawServiceService(cleanServiceId, services.get(i));
		}
	}
	public void crawlRawCatalogXlink(int cleanCatalogId, CatalogXlink rawCatalogXlink) throws Exception{
		CatalogXlink cleanCatalogXlink = rawCatalogXlink.clone();
		cleanCatalogXlink.setCatalogId(cleanCatalogId);
		int cleanCatalogXlinkId = DataAccess.insertCatalogXlink(cleanCatalogXlink);
		cleanCatalogXlink.setCatalogXlinkId(cleanCatalogXlinkId);

		cleanCatalogXlink = Applier.applyCatalogXlinkRules(cleanCatalogId, cleanCatalogXlink);

		if(cleanCatalogXlink == null){
			DataAccess.deleteCatalogXlink(cleanCatalogXlinkId);
			return;
		}
		DataAccess.updateCatalogXlink(cleanCatalogXlink);
	}
	public void crawlNewCatalogrefDocumentation(int parentId, Element child) throws Exception{
		Datavalue value = new Datavalue();
		Datavalue documentationenum = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("type"))
				documentationenum.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("type"))
				documentationenum.NOTNULL(a.getValue());
		}
		String val = "";
		ArrayList<org.jdom.Text> values3 = getContent(child.getContent());
		for(int i=0; i<values3.size(); i++){
			val += values3.get(i).getText();
		}
		value.NOTNULL(val);

		int childId = DataAccess.insertCatalogrefDocumentation(parentId, value, documentationenum);

		ArrayList<Element> all = getElements(child.getChildren());
		for(int i = 0; i<all.size(); i++){
			Element newchild = all.get(i);
			if(newchild.getName().equals("namespace"))
				crawlNewCatalogrefDocumentationNamespace(childId, newchild);
			else if(newchild.getName().equals("xlink"))
				crawlNewCatalogrefDocumentationXlink(childId, newchild);
		}
	}
	public void crawlRawCatalogrefDocumentation(int cleanCatalogrefId, CatalogrefDocumentation rawCatalogrefDocumentation) throws Exception{
		CatalogrefDocumentation cleanCatalogrefDocumentation = rawCatalogrefDocumentation.clone();
		cleanCatalogrefDocumentation.setCatalogrefId(cleanCatalogrefId);
		int cleanCatalogrefDocumentationId = DataAccess.insertCatalogrefDocumentation(cleanCatalogrefDocumentation);
		cleanCatalogrefDocumentation.setCatalogrefDocumentationId(cleanCatalogrefDocumentationId);

		cleanCatalogrefDocumentation = Applier.applyCatalogrefDocumentationRules(cleanCatalogrefId, cleanCatalogrefDocumentation);

		if(cleanCatalogrefDocumentation == null){
			DataAccess.deleteCatalogrefDocumentation(cleanCatalogrefDocumentationId);
			return;
		}
		DataAccess.updateCatalogrefDocumentation(cleanCatalogrefDocumentation);
		int rawCatalogrefDocumentationId = rawCatalogrefDocumentation.getCatalogrefDocumentationId();

		ArrayList<CatalogrefDocumentationNamespace> namespaces = DataAccess.getCatalogrefDocumentationNamespaceBCatalogrefDocumentation(rawCatalogrefDocumentationId);
		for(int i = 0; i<namespaces.size(); i++){
			crawlRawCatalogrefDocumentationNamespace(cleanCatalogrefDocumentationId, namespaces.get(i));
		}
		ArrayList<CatalogrefDocumentationXlink> xlinks = DataAccess.getCatalogrefDocumentationXlinkBCatalogrefDocumentation(rawCatalogrefDocumentationId);
		for(int i = 0; i<xlinks.size(); i++){
			crawlRawCatalogrefDocumentationXlink(cleanCatalogrefDocumentationId, xlinks.get(i));
		}
	}
	public void crawlNewCatalogrefXlink(int parentId, Element child) throws Exception{
		Datavalue value = new Datavalue();
		Datavalue xlink = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("xlink"))
				xlink.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("xlink"))
				xlink.NOTNULL(a.getValue());
		}
		String val = "";
		ArrayList<org.jdom.Text> values3 = getContent(child.getContent());
		for(int i=0; i<values3.size(); i++){
			val += values3.get(i).getText();
		}
		value.NOTNULL(val);

		DataAccess.insertCatalogrefXlink(parentId, value, xlink);

	}
	public void crawlRawCatalogrefXlink(int cleanCatalogrefId, CatalogrefXlink rawCatalogrefXlink) throws Exception{
		CatalogrefXlink cleanCatalogrefXlink = rawCatalogrefXlink.clone();
		cleanCatalogrefXlink.setCatalogrefId(cleanCatalogrefId);
		int cleanCatalogrefXlinkId = DataAccess.insertCatalogrefXlink(cleanCatalogrefXlink);
		cleanCatalogrefXlink.setCatalogrefXlinkId(cleanCatalogrefXlinkId);

		cleanCatalogrefXlink = Applier.applyCatalogrefXlinkRules(cleanCatalogrefId, cleanCatalogrefXlink);

		if(cleanCatalogrefXlink == null){
			DataAccess.deleteCatalogrefXlink(cleanCatalogrefXlinkId);
			return;
		}
		DataAccess.updateCatalogrefXlink(cleanCatalogrefXlink);
	}
	public void crawlNewCatalogrefDocumentationNamespace(int parentId, Element child) throws Exception{
		Datavalue namespace = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("namespace"))
				namespace.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("namespace"))
				namespace.NOTNULL(a.getValue());
		}

		DataAccess.insertCatalogrefDocumentationNamespace(parentId, namespace);

	}
	public void crawlRawCatalogrefDocumentationNamespace(int cleanCatalogrefDocumentationId, CatalogrefDocumentationNamespace rawCatalogrefDocumentationNamespace) throws Exception{
		CatalogrefDocumentationNamespace cleanCatalogrefDocumentationNamespace = rawCatalogrefDocumentationNamespace.clone();
		cleanCatalogrefDocumentationNamespace.setCatalogrefDocumentationId(cleanCatalogrefDocumentationId);
		int cleanCatalogrefDocumentationNamespaceId = DataAccess.insertCatalogrefDocumentationNamespace(cleanCatalogrefDocumentationNamespace);
		cleanCatalogrefDocumentationNamespace.setCatalogrefDocumentationNamespaceId(cleanCatalogrefDocumentationNamespaceId);

		cleanCatalogrefDocumentationNamespace = Applier.applyCatalogrefDocumentationNamespaceRules(cleanCatalogrefDocumentationId, cleanCatalogrefDocumentationNamespace);

		if(cleanCatalogrefDocumentationNamespace == null){
			DataAccess.deleteCatalogrefDocumentationNamespace(cleanCatalogrefDocumentationNamespaceId);
			return;
		}
		DataAccess.updateCatalogrefDocumentationNamespace(cleanCatalogrefDocumentationNamespace);
	}
	public void crawlNewCatalogrefDocumentationXlink(int parentId, Element child) throws Exception{
		Datavalue value = new Datavalue();
		Datavalue xlink = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("xlink"))
				xlink.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("xlink"))
				xlink.NOTNULL(a.getValue());
		}
		String val = "";
		ArrayList<org.jdom.Text> values3 = getContent(child.getContent());
		for(int i=0; i<values3.size(); i++){
			val += values3.get(i).getText();
		}
		value.NOTNULL(val);

		DataAccess.insertCatalogrefDocumentationXlink(parentId, value, xlink);

	}
	public void crawlRawCatalogrefDocumentationXlink(int cleanCatalogrefDocumentationId, CatalogrefDocumentationXlink rawCatalogrefDocumentationXlink) throws Exception{
		CatalogrefDocumentationXlink cleanCatalogrefDocumentationXlink = rawCatalogrefDocumentationXlink.clone();
		cleanCatalogrefDocumentationXlink.setCatalogrefDocumentationId(cleanCatalogrefDocumentationId);
		int cleanCatalogrefDocumentationXlinkId = DataAccess.insertCatalogrefDocumentationXlink(cleanCatalogrefDocumentationXlink);
		cleanCatalogrefDocumentationXlink.setCatalogrefDocumentationXlinkId(cleanCatalogrefDocumentationXlinkId);

		cleanCatalogrefDocumentationXlink = Applier.applyCatalogrefDocumentationXlinkRules(cleanCatalogrefDocumentationId, cleanCatalogrefDocumentationXlink);

		if(cleanCatalogrefDocumentationXlink == null){
			DataAccess.deleteCatalogrefDocumentationXlink(cleanCatalogrefDocumentationXlinkId);
			return;
		}
		DataAccess.updateCatalogrefDocumentationXlink(cleanCatalogrefDocumentationXlink);
	}
	public void crawlNewTmgPublisherContact(int parentId, Element child) throws Exception{
		Datavalue email = new Datavalue();
		Datavalue url = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("email"))
				email.NOTNULL(a.getValue());
			else if(a.getName().equals("url"))
				url.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("email"))
				email.NOTNULL(a.getValue());
			else if(a.getName().equals("url"))
				url.NOTNULL(a.getValue());
		}

		DataAccess.insertTmgPublisherContact(parentId, email, url);

	}
	public void crawlRawTmgPublisherContact(int cleanTmgPublisherId, TmgPublisherContact rawTmgPublisherContact) throws Exception{
		TmgPublisherContact cleanTmgPublisherContact = rawTmgPublisherContact.clone();
		cleanTmgPublisherContact.setTmgPublisherId(cleanTmgPublisherId);
		int cleanTmgPublisherContactId = DataAccess.insertTmgPublisherContact(cleanTmgPublisherContact);
		cleanTmgPublisherContact.setTmgPublisherContactId(cleanTmgPublisherContactId);

		cleanTmgPublisherContact = Applier.applyTmgPublisherContactRules(cleanTmgPublisherId, cleanTmgPublisherContact);

		if(cleanTmgPublisherContact == null){
			DataAccess.deleteTmgPublisherContact(cleanTmgPublisherContactId);
			return;
		}
		DataAccess.updateTmgPublisherContact(cleanTmgPublisherContact);
	}
	public void crawlNewTmgPublisherName(int parentId, Element child) throws Exception{
		Datavalue value = new Datavalue();
		Datavalue vocabulary = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("vocabulary"))
				vocabulary.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("vocabulary"))
				vocabulary.NOTNULL(a.getValue());
		}
		String val = "";
		ArrayList<org.jdom.Text> values3 = getContent(child.getContent());
		for(int i=0; i<values3.size(); i++){
			val += values3.get(i).getText();
		}
		value.NOTNULL(val);

		DataAccess.insertTmgPublisherName(parentId, value, vocabulary);

	}
	public void crawlRawTmgPublisherName(int cleanTmgPublisherId, TmgPublisherName rawTmgPublisherName) throws Exception{
		TmgPublisherName cleanTmgPublisherName = rawTmgPublisherName.clone();
		cleanTmgPublisherName.setTmgPublisherId(cleanTmgPublisherId);
		int cleanTmgPublisherNameId = DataAccess.insertTmgPublisherName(cleanTmgPublisherName);
		cleanTmgPublisherName.setTmgPublisherNameId(cleanTmgPublisherNameId);

		cleanTmgPublisherName = Applier.applyTmgPublisherNameRules(cleanTmgPublisherId, cleanTmgPublisherName);

		if(cleanTmgPublisherName == null){
			DataAccess.deleteTmgPublisherName(cleanTmgPublisherNameId);
			return;
		}
		DataAccess.updateTmgPublisherName(cleanTmgPublisherName);
	}
	public void crawlNewDatasetAccess(int parentId, Element child) throws Exception{
		Datavalue servicename = new Datavalue();
		Datavalue urlpath = new Datavalue();
		Datavalue dataformat = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("servicename"))
				servicename.NOTNULL(a.getValue());
			else if(a.getName().equals("urlpath"))
				urlpath.NOTNULL(a.getValue());
			else if(a.getName().equals("dataformat"))
				dataformat.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("servicename"))
				servicename.NOTNULL(a.getValue());
			else if(a.getName().equals("urlpath"))
				urlpath.NOTNULL(a.getValue());
			else if(a.getName().equals("dataformat"))
				dataformat.NOTNULL(a.getValue());
		}

		int childId = DataAccess.insertDatasetAccess(parentId, servicename, urlpath, dataformat);

		ArrayList<Element> all = getElements(child.getChildren());
		for(int i = 0; i<all.size(); i++){
			Element newchild = all.get(i);
			if(newchild.getName().equals("datasize"))
				crawlNewDatasetAccessDatasize(childId, newchild);
		}
	}
	public void crawlRawDatasetAccess(int cleanDatasetId, DatasetAccess rawDatasetAccess) throws Exception{
		DatasetAccess cleanDatasetAccess = rawDatasetAccess.clone();
		cleanDatasetAccess.setDatasetId(cleanDatasetId);
		int cleanDatasetAccessId = DataAccess.insertDatasetAccess(cleanDatasetAccess);
		cleanDatasetAccess.setDatasetAccessId(cleanDatasetAccessId);

		cleanDatasetAccess = Applier.applyDatasetAccessRules(cleanDatasetId, cleanDatasetAccess);

		if(cleanDatasetAccess == null){
			DataAccess.deleteDatasetAccess(cleanDatasetAccessId);
			return;
		}
		DataAccess.updateDatasetAccess(cleanDatasetAccess);
		int rawDatasetAccessId = rawDatasetAccess.getDatasetAccessId();

		ArrayList<DatasetAccessDatasize> datasizes = DataAccess.getDatasetAccessDatasizeBDatasetAccess(rawDatasetAccessId);
		for(int i = 0; i<datasizes.size(); i++){
			crawlRawDatasetAccessDatasize(cleanDatasetAccessId, datasizes.get(i));
		}
	}
	public void crawlNewDatasetDataset(int parentId, Element child) throws Exception{
		Datavalue alias = new Datavalue();
		Datavalue authority = new Datavalue();
		Datavalue dId = new Datavalue();
		Datavalue harvest = new Datavalue();
		Datavalue name = new Datavalue();
		Datavalue resourcecontrol = new Datavalue();
		Datavalue serviceName = new Datavalue();
		Datavalue urlPath = new Datavalue();
		Datavalue collectiontype = new Datavalue();
		Datavalue datasizeUnit = new Datavalue();
		Datavalue dataType = new Datavalue();
		Datavalue status = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("alias"))
				alias.NOTNULL(a.getValue());
			else if(a.getName().equals("authority"))
				authority.NOTNULL(a.getValue());
			else if(a.getName().equals("ID"))
				dId.NOTNULL(a.getValue());
			else if(a.getName().equals("harvest"))
				harvest.NOTNULL(a.getValue());
			else if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
			else if(a.getName().equals("resourcecontrol"))
				resourcecontrol.NOTNULL(a.getValue());
			else if(a.getName().equals("serviceName"))
				serviceName.NOTNULL(a.getValue());
			else if(a.getName().equals("urlPath"))
				urlPath.NOTNULL(a.getValue());
			else if(a.getName().equals("collectiontype"))
				collectiontype.NOTNULL(a.getValue());
			else if(a.getName().equals("datasize_unit"))
				datasizeUnit.NOTNULL(a.getValue());
			else if(a.getName().equals("dataType"))
				dataType.NOTNULL(a.getValue());
			else if(a.getName().equals("status"))
				status.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("alias"))
				alias.NOTNULL(a.getValue());
			else if(a.getName().equals("authority"))
				authority.NOTNULL(a.getValue());
			else if(a.getName().equals("ID"))
				dId.NOTNULL(a.getValue());
			else if(a.getName().equals("harvest"))
				harvest.NOTNULL(a.getValue());
			else if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
			else if(a.getName().equals("resourcecontrol"))
				resourcecontrol.NOTNULL(a.getValue());
			else if(a.getName().equals("serviceName"))
				serviceName.NOTNULL(a.getValue());
			else if(a.getName().equals("urlPath"))
				urlPath.NOTNULL(a.getValue());
			else if(a.getName().equals("collectiontype"))
				collectiontype.NOTNULL(a.getValue());
			else if(a.getName().equals("datasize_unit"))
				datasizeUnit.NOTNULL(a.getValue());
			else if(a.getName().equals("dataType"))
				dataType.NOTNULL(a.getValue());
			else if(a.getName().equals("status"))
				status.NOTNULL(a.getValue());
		}

		int childId = DataAccess.insertDataset(alias, authority, dId, harvest, name, resourcecontrol, serviceName, urlPath, collectiontype, datasizeUnit, dataType, status);

		DataAccess.insertDatasetDataset(parentId, childId);

		ArrayList<Element> all = getElements(child.getChildren());
		for(int i = 0; i<all.size(); i++){
			Element newchild = all.get(i);
			if(newchild.getName().equals("access"))
				crawlNewDatasetAccess(childId, newchild);
			else if(newchild.getName().equals("dataset"))
				crawlNewDatasetDataset(childId, newchild);
			else if(newchild.getName().equals("ncml"))
				crawlNewDatasetNcml(childId, newchild);
			else if(newchild.getName().equals("property"))
				crawlNewDatasetProperty(childId, newchild);
			else if(newchild.getName().equals("service"))
				crawlNewDatasetService(childId, newchild);
		}
		crawlNewDatasetTmg(childId, child);
	}
	public void crawlRawDatasetDataset(int cleanparentId, Dataset rawDataset) throws Exception{
		Dataset cleanDataset = rawDataset.clone();
		int cleanDatasetId = DataAccess.insertDataset(cleanDataset);
		cleanDataset.setDatasetId(cleanDatasetId);

		cleanDataset = Applier.applyDatasetDatasetRules(cleanparentId, cleanDataset);

		if(cleanDataset == null){
			DataAccess.deleteDataset(cleanDatasetId);
			return;
		}
		DataAccess.updateDataset(cleanDataset);
		DataAccess.insertDatasetDataset(cleanparentId, cleanDatasetId);
		int rawDatasetId = rawDataset.getDatasetId();

		ArrayList<DatasetAccess> accesss = DataAccess.getDatasetAccessBDataset(rawDatasetId);
		for(int i = 0; i<accesss.size(); i++){
			crawlRawDatasetAccess(cleanDatasetId, accesss.get(i));
		}
		ArrayList<Dataset> datasets = DataAccess.getDatasetBDataset(rawDatasetId);
		for(int i = 0; i<datasets.size(); i++){
			crawlRawDatasetDataset(cleanDatasetId, datasets.get(i));
		}
		ArrayList<DatasetNcml> ncmls = DataAccess.getDatasetNcmlBDataset(rawDatasetId);
		for(int i = 0; i<ncmls.size(); i++){
			crawlRawDatasetNcml(cleanDatasetId, ncmls.get(i));
		}
		ArrayList<DatasetProperty> propertys = DataAccess.getDatasetPropertyBDataset(rawDatasetId);
		for(int i = 0; i<propertys.size(); i++){
			crawlRawDatasetProperty(cleanDatasetId, propertys.get(i));
		}
		ArrayList<Service> services = DataAccess.getServiceBDataset(rawDatasetId);
		for(int i = 0; i<services.size(); i++){
			crawlRawDatasetService(cleanDatasetId, services.get(i));
		}
		ArrayList<Tmg> tmgs = DataAccess.getTmgBDataset(rawDatasetId);
		for(int i = 0; i<tmgs.size(); i++){
			crawlRawDatasetTmg(cleanDatasetId, tmgs.get(i));
		}
	}
	public void crawlNewDatasetNcml(int parentId, Element child) throws Exception{


		DataAccess.insertDatasetNcml(parentId);

	}
	public void crawlRawDatasetNcml(int cleanDatasetId, DatasetNcml rawDatasetNcml) throws Exception{
		DatasetNcml cleanDatasetNcml = rawDatasetNcml.clone();
		cleanDatasetNcml.setDatasetId(cleanDatasetId);
		int cleanDatasetNcmlId = DataAccess.insertDatasetNcml(cleanDatasetNcml);
		cleanDatasetNcml.setDatasetNcmlId(cleanDatasetNcmlId);

		cleanDatasetNcml = Applier.applyDatasetNcmlRules(cleanDatasetId, cleanDatasetNcml);

		if(cleanDatasetNcml == null){
			DataAccess.deleteDatasetNcml(cleanDatasetNcmlId);
			return;
		}
		DataAccess.updateDatasetNcml(cleanDatasetNcml);
	}
	public void crawlNewDatasetProperty(int parentId, Element child) throws Exception{
		Datavalue name = new Datavalue();
		Datavalue value = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
		}
		String val = "";
		ArrayList<org.jdom.Text> values3 = getContent(child.getContent());
		for(int i=0; i<values3.size(); i++){
			val += values3.get(i).getText();
		}
		value.NOTNULL(val);

		DataAccess.insertDatasetProperty(parentId, name, value);

	}
	public void crawlRawDatasetProperty(int cleanDatasetId, DatasetProperty rawDatasetProperty) throws Exception{
		DatasetProperty cleanDatasetProperty = rawDatasetProperty.clone();
		cleanDatasetProperty.setDatasetId(cleanDatasetId);
		int cleanDatasetPropertyId = DataAccess.insertDatasetProperty(cleanDatasetProperty);
		cleanDatasetProperty.setDatasetPropertyId(cleanDatasetPropertyId);

		cleanDatasetProperty = Applier.applyDatasetPropertyRules(cleanDatasetId, cleanDatasetProperty);

		if(cleanDatasetProperty == null){
			DataAccess.deleteDatasetProperty(cleanDatasetPropertyId);
			return;
		}
		DataAccess.updateDatasetProperty(cleanDatasetProperty);
	}
	public void crawlNewDatasetService(int parentId, Element child) throws Exception{
		Datavalue base = new Datavalue();
		Datavalue desc = new Datavalue();
		Datavalue name = new Datavalue();
		Datavalue suffix = new Datavalue();
		Datavalue serviceType = new Datavalue();
		Datavalue status = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("base"))
				base.NOTNULL(a.getValue());
			else if(a.getName().equals("desc"))
				desc.NOTNULL(a.getValue());
			else if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
			else if(a.getName().equals("suffix"))
				suffix.NOTNULL(a.getValue());
			else if(a.getName().equals("serviceType"))
				serviceType.NOTNULL(a.getValue());
			else if(a.getName().equals("status"))
				status.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("base"))
				base.NOTNULL(a.getValue());
			else if(a.getName().equals("desc"))
				desc.NOTNULL(a.getValue());
			else if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
			else if(a.getName().equals("suffix"))
				suffix.NOTNULL(a.getValue());
			else if(a.getName().equals("serviceType"))
				serviceType.NOTNULL(a.getValue());
			else if(a.getName().equals("status"))
				status.NOTNULL(a.getValue());
		}

		int childId = DataAccess.insertService(base, desc, name, suffix, serviceType, status);

		DataAccess.insertDatasetService(parentId, childId);

		ArrayList<Element> all = getElements(child.getChildren());
		for(int i = 0; i<all.size(); i++){
			Element newchild = all.get(i);
			if(newchild.getName().equals("datasetroot"))
				crawlNewServiceDatasetroot(childId, newchild);
			else if(newchild.getName().equals("property"))
				crawlNewServiceProperty(childId, newchild);
			else if(newchild.getName().equals("service"))
				crawlNewServiceService(childId, newchild);
		}
	}
	public void crawlRawDatasetService(int cleanDatasetId, Service rawService) throws Exception{
		Service cleanService = rawService.clone();
		int cleanServiceId = DataAccess.insertService(cleanService);
		cleanService.setServiceId(cleanServiceId);

		cleanService = Applier.applyDatasetServiceRules(cleanDatasetId, cleanService);

		if(cleanService == null){
			DataAccess.deleteService(cleanServiceId);
			return;
		}
		DataAccess.updateService(cleanService);
		DataAccess.insertDatasetService(cleanDatasetId, cleanServiceId);
		int rawServiceId = rawService.getServiceId();

		ArrayList<ServiceDatasetroot> datasetroots = DataAccess.getServiceDatasetrootBService(rawServiceId);
		for(int i = 0; i<datasetroots.size(); i++){
			crawlRawServiceDatasetroot(cleanServiceId, datasetroots.get(i));
		}
		ArrayList<ServiceProperty> propertys = DataAccess.getServicePropertyBService(rawServiceId);
		for(int i = 0; i<propertys.size(); i++){
			crawlRawServiceProperty(cleanServiceId, propertys.get(i));
		}
		ArrayList<Service> services = DataAccess.getServiceBService(rawServiceId);
		for(int i = 0; i<services.size(); i++){
			crawlRawServiceService(cleanServiceId, services.get(i));
		}
	}
	public void crawlNewDatasetTmg(int parentId, Element child) throws Exception{


		int childId = DataAccess.insertTmg();

		DataAccess.insertDatasetTmg(parentId, childId);

		ArrayList<Element> all = getElements(child.getChildren());
		for(int i = 0; i<all.size(); i++){
			Element newchild = all.get(i);
			if(newchild.getName().equals("authority"))
				crawlNewTmgAuthority(childId, newchild);
			else if(newchild.getName().equals("contributor"))
				crawlNewTmgContributor(childId, newchild);
			else if(newchild.getName().equals("creator"))
				crawlNewTmgCreator(childId, newchild);
			else if(newchild.getName().equals("dataformat"))
				crawlNewTmgDataformat(childId, newchild);
			else if(newchild.getName().equals("datasize"))
				crawlNewTmgDatasize(childId, newchild);
			else if(newchild.getName().equals("datatype"))
				crawlNewTmgDatatype(childId, newchild);
			else if(newchild.getName().equals("date"))
				crawlNewTmgDate(childId, newchild);
			else if(newchild.getName().equals("documentation"))
				crawlNewTmgDocumentation(childId, newchild);
			else if(newchild.getName().equals("geospatialcoverage"))
				crawlNewTmgGeospatialcoverage(childId, newchild);
			else if(newchild.getName().equals("keyword"))
				crawlNewTmgKeyword(childId, newchild);
			else if(newchild.getName().equals("metadata"))
				crawlNewTmgMetadata(childId, newchild);
			else if(newchild.getName().equals("project"))
				crawlNewTmgProject(childId, newchild);
			else if(newchild.getName().equals("property"))
				crawlNewTmgProperty(childId, newchild);
			else if(newchild.getName().equals("publisher"))
				crawlNewTmgPublisher(childId, newchild);
			else if(newchild.getName().equals("servicename"))
				crawlNewTmgServicename(childId, newchild);
			else if(newchild.getName().equals("timecoverage"))
				crawlNewTmgTimecoverage(childId, newchild);
			else if(newchild.getName().equals("variables"))
				crawlNewTmgVariables(childId, newchild);
		}
	}
	public void crawlRawDatasetTmg(int cleanDatasetId, Tmg rawTmg) throws Exception{
		Tmg cleanTmg = rawTmg.clone();
		int cleanTmgId = DataAccess.insertTmg(cleanTmg);
		cleanTmg.setTmgId(cleanTmgId);

		cleanTmg = Applier.applyDatasetTmgRules(cleanDatasetId, cleanTmg);

		if(cleanTmg == null){
			DataAccess.deleteTmg(cleanTmgId);
			return;
		}
		DataAccess.updateTmg(cleanTmg);
		DataAccess.insertDatasetTmg(cleanDatasetId, cleanTmgId);
		int rawTmgId = rawTmg.getTmgId();

		ArrayList<TmgAuthority> authoritys = DataAccess.getTmgAuthorityBTmg(rawTmgId);
		for(int i = 0; i<authoritys.size(); i++){
			crawlRawTmgAuthority(cleanTmgId, authoritys.get(i));
		}
		ArrayList<TmgContributor> contributors = DataAccess.getTmgContributorBTmg(rawTmgId);
		for(int i = 0; i<contributors.size(); i++){
			crawlRawTmgContributor(cleanTmgId, contributors.get(i));
		}
		ArrayList<TmgCreator> creators = DataAccess.getTmgCreatorBTmg(rawTmgId);
		for(int i = 0; i<creators.size(); i++){
			crawlRawTmgCreator(cleanTmgId, creators.get(i));
		}
		ArrayList<TmgDataformat> dataformats = DataAccess.getTmgDataformatBTmg(rawTmgId);
		for(int i = 0; i<dataformats.size(); i++){
			crawlRawTmgDataformat(cleanTmgId, dataformats.get(i));
		}
		ArrayList<TmgDatasize> datasizes = DataAccess.getTmgDatasizeBTmg(rawTmgId);
		for(int i = 0; i<datasizes.size(); i++){
			crawlRawTmgDatasize(cleanTmgId, datasizes.get(i));
		}
		ArrayList<TmgDatatype> datatypes = DataAccess.getTmgDatatypeBTmg(rawTmgId);
		for(int i = 0; i<datatypes.size(); i++){
			crawlRawTmgDatatype(cleanTmgId, datatypes.get(i));
		}
		ArrayList<TmgDate> dates = DataAccess.getTmgDateBTmg(rawTmgId);
		for(int i = 0; i<dates.size(); i++){
			crawlRawTmgDate(cleanTmgId, dates.get(i));
		}
		ArrayList<TmgDocumentation> documentations = DataAccess.getTmgDocumentationBTmg(rawTmgId);
		for(int i = 0; i<documentations.size(); i++){
			crawlRawTmgDocumentation(cleanTmgId, documentations.get(i));
		}
		ArrayList<TmgGeospatialcoverage> geospatialcoverages = DataAccess.getTmgGeospatialcoverageBTmg(rawTmgId);
		for(int i = 0; i<geospatialcoverages.size(); i++){
			crawlRawTmgGeospatialcoverage(cleanTmgId, geospatialcoverages.get(i));
		}
		ArrayList<TmgKeyword> keywords = DataAccess.getTmgKeywordBTmg(rawTmgId);
		for(int i = 0; i<keywords.size(); i++){
			crawlRawTmgKeyword(cleanTmgId, keywords.get(i));
		}
		ArrayList<Metadata> metadatas = DataAccess.getMetadataBTmg(rawTmgId);
		for(int i = 0; i<metadatas.size(); i++){
			crawlRawTmgMetadata(cleanTmgId, metadatas.get(i));
		}
		ArrayList<TmgProject> projects = DataAccess.getTmgProjectBTmg(rawTmgId);
		for(int i = 0; i<projects.size(); i++){
			crawlRawTmgProject(cleanTmgId, projects.get(i));
		}
		ArrayList<TmgProperty> propertys = DataAccess.getTmgPropertyBTmg(rawTmgId);
		for(int i = 0; i<propertys.size(); i++){
			crawlRawTmgProperty(cleanTmgId, propertys.get(i));
		}
		ArrayList<TmgPublisher> publishers = DataAccess.getTmgPublisherBTmg(rawTmgId);
		for(int i = 0; i<publishers.size(); i++){
			crawlRawTmgPublisher(cleanTmgId, publishers.get(i));
		}
		ArrayList<TmgServicename> servicenames = DataAccess.getTmgServicenameBTmg(rawTmgId);
		for(int i = 0; i<servicenames.size(); i++){
			crawlRawTmgServicename(cleanTmgId, servicenames.get(i));
		}
		ArrayList<TmgTimecoverage> timecoverages = DataAccess.getTmgTimecoverageBTmg(rawTmgId);
		for(int i = 0; i<timecoverages.size(); i++){
			crawlRawTmgTimecoverage(cleanTmgId, timecoverages.get(i));
		}
		ArrayList<TmgVariables> variabless = DataAccess.getTmgVariablesBTmg(rawTmgId);
		for(int i = 0; i<variabless.size(); i++){
			crawlRawTmgVariables(cleanTmgId, variabless.get(i));
		}
	}
	public void crawlNewTmgDocumentationNamespace(int parentId, Element child) throws Exception{
		Datavalue namespace = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("namespace"))
				namespace.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("namespace"))
				namespace.NOTNULL(a.getValue());
		}

		DataAccess.insertTmgDocumentationNamespace(parentId, namespace);

	}
	public void crawlRawTmgDocumentationNamespace(int cleanTmgDocumentationId, TmgDocumentationNamespace rawTmgDocumentationNamespace) throws Exception{
		TmgDocumentationNamespace cleanTmgDocumentationNamespace = rawTmgDocumentationNamespace.clone();
		cleanTmgDocumentationNamespace.setTmgDocumentationId(cleanTmgDocumentationId);
		int cleanTmgDocumentationNamespaceId = DataAccess.insertTmgDocumentationNamespace(cleanTmgDocumentationNamespace);
		cleanTmgDocumentationNamespace.setTmgDocumentationNamespaceId(cleanTmgDocumentationNamespaceId);

		cleanTmgDocumentationNamespace = Applier.applyTmgDocumentationNamespaceRules(cleanTmgDocumentationId, cleanTmgDocumentationNamespace);

		if(cleanTmgDocumentationNamespace == null){
			DataAccess.deleteTmgDocumentationNamespace(cleanTmgDocumentationNamespaceId);
			return;
		}
		DataAccess.updateTmgDocumentationNamespace(cleanTmgDocumentationNamespace);
	}
	public void crawlNewTmgDocumentationXlink(int parentId, Element child) throws Exception{
		Datavalue value = new Datavalue();
		Datavalue xlink = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("xlink"))
				xlink.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("xlink"))
				xlink.NOTNULL(a.getValue());
		}
		String val = "";
		ArrayList<org.jdom.Text> values3 = getContent(child.getContent());
		for(int i=0; i<values3.size(); i++){
			val += values3.get(i).getText();
		}
		value.NOTNULL(val);

		DataAccess.insertTmgDocumentationXlink(parentId, value, xlink);

	}
	public void crawlRawTmgDocumentationXlink(int cleanTmgDocumentationId, TmgDocumentationXlink rawTmgDocumentationXlink) throws Exception{
		TmgDocumentationXlink cleanTmgDocumentationXlink = rawTmgDocumentationXlink.clone();
		cleanTmgDocumentationXlink.setTmgDocumentationId(cleanTmgDocumentationId);
		int cleanTmgDocumentationXlinkId = DataAccess.insertTmgDocumentationXlink(cleanTmgDocumentationXlink);
		cleanTmgDocumentationXlink.setTmgDocumentationXlinkId(cleanTmgDocumentationXlinkId);

		cleanTmgDocumentationXlink = Applier.applyTmgDocumentationXlinkRules(cleanTmgDocumentationId, cleanTmgDocumentationXlink);

		if(cleanTmgDocumentationXlink == null){
			DataAccess.deleteTmgDocumentationXlink(cleanTmgDocumentationXlinkId);
			return;
		}
		DataAccess.updateTmgDocumentationXlink(cleanTmgDocumentationXlink);
	}
	public void crawlNewTmgAuthority(int parentId, Element child) throws Exception{
		Datavalue authority = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("authority"))
				authority.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("authority"))
				authority.NOTNULL(a.getValue());
		}

		DataAccess.insertTmgAuthority(parentId, authority);

	}
	public void crawlRawTmgAuthority(int cleanTmgId, TmgAuthority rawTmgAuthority) throws Exception{
		TmgAuthority cleanTmgAuthority = rawTmgAuthority.clone();
		cleanTmgAuthority.setTmgId(cleanTmgId);
		int cleanTmgAuthorityId = DataAccess.insertTmgAuthority(cleanTmgAuthority);
		cleanTmgAuthority.setTmgAuthorityId(cleanTmgAuthorityId);

		cleanTmgAuthority = Applier.applyTmgAuthorityRules(cleanTmgId, cleanTmgAuthority);

		if(cleanTmgAuthority == null){
			DataAccess.deleteTmgAuthority(cleanTmgAuthorityId);
			return;
		}
		DataAccess.updateTmgAuthority(cleanTmgAuthority);
	}
	public void crawlNewTmgContributor(int parentId, Element child) throws Exception{
		Datavalue name = new Datavalue();
		Datavalue role = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
			else if(a.getName().equals("role"))
				role.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
			else if(a.getName().equals("role"))
				role.NOTNULL(a.getValue());
		}

		DataAccess.insertTmgContributor(parentId, name, role);

	}
	public void crawlRawTmgContributor(int cleanTmgId, TmgContributor rawTmgContributor) throws Exception{
		TmgContributor cleanTmgContributor = rawTmgContributor.clone();
		cleanTmgContributor.setTmgId(cleanTmgId);
		int cleanTmgContributorId = DataAccess.insertTmgContributor(cleanTmgContributor);
		cleanTmgContributor.setTmgContributorId(cleanTmgContributorId);

		cleanTmgContributor = Applier.applyTmgContributorRules(cleanTmgId, cleanTmgContributor);

		if(cleanTmgContributor == null){
			DataAccess.deleteTmgContributor(cleanTmgContributorId);
			return;
		}
		DataAccess.updateTmgContributor(cleanTmgContributor);
	}
	public void crawlNewTmgCreator(int parentId, Element child) throws Exception{


		int childId = DataAccess.insertTmgCreator(parentId);

		ArrayList<Element> all = getElements(child.getChildren());
		for(int i = 0; i<all.size(); i++){
			Element newchild = all.get(i);
			if(newchild.getName().equals("contact"))
				crawlNewTmgCreatorContact(childId, newchild);
			else if(newchild.getName().equals("name"))
				crawlNewTmgCreatorName(childId, newchild);
		}
	}
	public void crawlRawTmgCreator(int cleanTmgId, TmgCreator rawTmgCreator) throws Exception{
		TmgCreator cleanTmgCreator = rawTmgCreator.clone();
		cleanTmgCreator.setTmgId(cleanTmgId);
		int cleanTmgCreatorId = DataAccess.insertTmgCreator(cleanTmgCreator);
		cleanTmgCreator.setTmgCreatorId(cleanTmgCreatorId);

		cleanTmgCreator = Applier.applyTmgCreatorRules(cleanTmgId, cleanTmgCreator);

		if(cleanTmgCreator == null){
			DataAccess.deleteTmgCreator(cleanTmgCreatorId);
			return;
		}
		DataAccess.updateTmgCreator(cleanTmgCreator);
		int rawTmgCreatorId = rawTmgCreator.getTmgCreatorId();

		ArrayList<TmgCreatorContact> contacts = DataAccess.getTmgCreatorContactBTmgCreator(rawTmgCreatorId);
		for(int i = 0; i<contacts.size(); i++){
			crawlRawTmgCreatorContact(cleanTmgCreatorId, contacts.get(i));
		}
		ArrayList<TmgCreatorName> names = DataAccess.getTmgCreatorNameBTmgCreator(rawTmgCreatorId);
		for(int i = 0; i<names.size(); i++){
			crawlRawTmgCreatorName(cleanTmgCreatorId, names.get(i));
		}
	}
	public void crawlNewTmgDataformat(int parentId, Element child) throws Exception{
		Datavalue dataformat = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("dataformat"))
				dataformat.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("dataformat"))
				dataformat.NOTNULL(a.getValue());
		}

		DataAccess.insertTmgDataformat(parentId, dataformat);

	}
	public void crawlRawTmgDataformat(int cleanTmgId, TmgDataformat rawTmgDataformat) throws Exception{
		TmgDataformat cleanTmgDataformat = rawTmgDataformat.clone();
		cleanTmgDataformat.setTmgId(cleanTmgId);
		int cleanTmgDataformatId = DataAccess.insertTmgDataformat(cleanTmgDataformat);
		cleanTmgDataformat.setTmgDataformatId(cleanTmgDataformatId);

		cleanTmgDataformat = Applier.applyTmgDataformatRules(cleanTmgId, cleanTmgDataformat);

		if(cleanTmgDataformat == null){
			DataAccess.deleteTmgDataformat(cleanTmgDataformatId);
			return;
		}
		DataAccess.updateTmgDataformat(cleanTmgDataformat);
	}
	public void crawlNewTmgDatasize(int parentId, Element child) throws Exception{
		Datavalue value = new Datavalue();
		Datavalue units = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("units"))
				units.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("units"))
				units.NOTNULL(a.getValue());
		}
		String val = "";
		ArrayList<org.jdom.Text> values3 = getContent(child.getContent());
		for(int i=0; i<values3.size(); i++){
			val += values3.get(i).getText();
		}
		value.NOTNULL(val);

		DataAccess.insertTmgDatasize(parentId, value, units);

	}
	public void crawlRawTmgDatasize(int cleanTmgId, TmgDatasize rawTmgDatasize) throws Exception{
		TmgDatasize cleanTmgDatasize = rawTmgDatasize.clone();
		cleanTmgDatasize.setTmgId(cleanTmgId);
		int cleanTmgDatasizeId = DataAccess.insertTmgDatasize(cleanTmgDatasize);
		cleanTmgDatasize.setTmgDatasizeId(cleanTmgDatasizeId);

		cleanTmgDatasize = Applier.applyTmgDatasizeRules(cleanTmgId, cleanTmgDatasize);

		if(cleanTmgDatasize == null){
			DataAccess.deleteTmgDatasize(cleanTmgDatasizeId);
			return;
		}
		DataAccess.updateTmgDatasize(cleanTmgDatasize);
	}
	public void crawlNewTmgDatatype(int parentId, Element child) throws Exception{
		Datavalue datatype = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("datatype"))
				datatype.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("datatype"))
				datatype.NOTNULL(a.getValue());
		}

		DataAccess.insertTmgDatatype(parentId, datatype);

	}
	public void crawlRawTmgDatatype(int cleanTmgId, TmgDatatype rawTmgDatatype) throws Exception{
		TmgDatatype cleanTmgDatatype = rawTmgDatatype.clone();
		cleanTmgDatatype.setTmgId(cleanTmgId);
		int cleanTmgDatatypeId = DataAccess.insertTmgDatatype(cleanTmgDatatype);
		cleanTmgDatatype.setTmgDatatypeId(cleanTmgDatatypeId);

		cleanTmgDatatype = Applier.applyTmgDatatypeRules(cleanTmgId, cleanTmgDatatype);

		if(cleanTmgDatatype == null){
			DataAccess.deleteTmgDatatype(cleanTmgDatatypeId);
			return;
		}
		DataAccess.updateTmgDatatype(cleanTmgDatatype);
	}
	public void crawlNewTmgDate(int parentId, Element child) throws Exception{
		Datavalue format = new Datavalue();
		Datavalue value = new Datavalue();
		Datavalue dateenum = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("format"))
				format.NOTNULL(a.getValue());
			else if(a.getName().equals("dateenum"))
				dateenum.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("format"))
				format.NOTNULL(a.getValue());
			else if(a.getName().equals("dateenum"))
				dateenum.NOTNULL(a.getValue());
		}
		String val = "";
		ArrayList<org.jdom.Text> values3 = getContent(child.getContent());
		for(int i=0; i<values3.size(); i++){
			val += values3.get(i).getText();
		}
		value.NOTNULL(val);

		DataAccess.insertTmgDate(parentId, format, value, dateenum);

	}
	public void crawlRawTmgDate(int cleanTmgId, TmgDate rawTmgDate) throws Exception{
		TmgDate cleanTmgDate = rawTmgDate.clone();
		cleanTmgDate.setTmgId(cleanTmgId);
		int cleanTmgDateId = DataAccess.insertTmgDate(cleanTmgDate);
		cleanTmgDate.setTmgDateId(cleanTmgDateId);

		cleanTmgDate = Applier.applyTmgDateRules(cleanTmgId, cleanTmgDate);

		if(cleanTmgDate == null){
			DataAccess.deleteTmgDate(cleanTmgDateId);
			return;
		}
		DataAccess.updateTmgDate(cleanTmgDate);
	}
	public void crawlNewTmgDocumentation(int parentId, Element child) throws Exception{
		Datavalue value = new Datavalue();
		Datavalue documentationenum = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("type"))
				documentationenum.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("type"))
				documentationenum.NOTNULL(a.getValue());
		}
		String val = "";
		ArrayList<org.jdom.Text> values3 = getContent(child.getContent());
		for(int i=0; i<values3.size(); i++){
			val += values3.get(i).getText();
		}
		value.NOTNULL(val);

		int childId = DataAccess.insertTmgDocumentation(parentId, value, documentationenum);

		ArrayList<Element> all = getElements(child.getChildren());
		for(int i = 0; i<all.size(); i++){
			Element newchild = all.get(i);
			if(newchild.getName().equals("namespace"))
				crawlNewTmgDocumentationNamespace(childId, newchild);
			else if(newchild.getName().equals("xlink"))
				crawlNewTmgDocumentationXlink(childId, newchild);
		}
	}
	public void crawlRawTmgDocumentation(int cleanTmgId, TmgDocumentation rawTmgDocumentation) throws Exception{
		TmgDocumentation cleanTmgDocumentation = rawTmgDocumentation.clone();
		cleanTmgDocumentation.setTmgId(cleanTmgId);
		int cleanTmgDocumentationId = DataAccess.insertTmgDocumentation(cleanTmgDocumentation);
		cleanTmgDocumentation.setTmgDocumentationId(cleanTmgDocumentationId);

		cleanTmgDocumentation = Applier.applyTmgDocumentationRules(cleanTmgId, cleanTmgDocumentation);

		if(cleanTmgDocumentation == null){
			DataAccess.deleteTmgDocumentation(cleanTmgDocumentationId);
			return;
		}
		DataAccess.updateTmgDocumentation(cleanTmgDocumentation);
		int rawTmgDocumentationId = rawTmgDocumentation.getTmgDocumentationId();

		ArrayList<TmgDocumentationNamespace> namespaces = DataAccess.getTmgDocumentationNamespaceBTmgDocumentation(rawTmgDocumentationId);
		for(int i = 0; i<namespaces.size(); i++){
			crawlRawTmgDocumentationNamespace(cleanTmgDocumentationId, namespaces.get(i));
		}
		ArrayList<TmgDocumentationXlink> xlinks = DataAccess.getTmgDocumentationXlinkBTmgDocumentation(rawTmgDocumentationId);
		for(int i = 0; i<xlinks.size(); i++){
			crawlRawTmgDocumentationXlink(cleanTmgDocumentationId, xlinks.get(i));
		}
	}
	public void crawlNewTmgGeospatialcoverage(int parentId, Element child) throws Exception{
		Datavalue upordown = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("upordown"))
				upordown.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("upordown"))
				upordown.NOTNULL(a.getValue());
		}

		int childId = DataAccess.insertTmgGeospatialcoverage(parentId, upordown);

		ArrayList<Element> all = getElements(child.getChildren());
		for(int i = 0; i<all.size(); i++){
			Element newchild = all.get(i);
			if(newchild.getName().equals("eastwest"))
				crawlNewTmgGeospatialcoverageEastwest(childId, newchild);
			else if(newchild.getName().equals("name"))
				crawlNewTmgGeospatialcoverageName(childId, newchild);
			else if(newchild.getName().equals("northsouth"))
				crawlNewTmgGeospatialcoverageNorthsouth(childId, newchild);
			else if(newchild.getName().equals("updown"))
				crawlNewTmgGeospatialcoverageUpdown(childId, newchild);
		}
	}
	public void crawlRawTmgGeospatialcoverage(int cleanTmgId, TmgGeospatialcoverage rawTmgGeospatialcoverage) throws Exception{
		TmgGeospatialcoverage cleanTmgGeospatialcoverage = rawTmgGeospatialcoverage.clone();
		cleanTmgGeospatialcoverage.setTmgId(cleanTmgId);
		int cleanTmgGeospatialcoverageId = DataAccess.insertTmgGeospatialcoverage(cleanTmgGeospatialcoverage);
		cleanTmgGeospatialcoverage.setTmgGeospatialcoverageId(cleanTmgGeospatialcoverageId);

		cleanTmgGeospatialcoverage = Applier.applyTmgGeospatialcoverageRules(cleanTmgId, cleanTmgGeospatialcoverage);

		if(cleanTmgGeospatialcoverage == null){
			DataAccess.deleteTmgGeospatialcoverage(cleanTmgGeospatialcoverageId);
			return;
		}
		DataAccess.updateTmgGeospatialcoverage(cleanTmgGeospatialcoverage);
		int rawTmgGeospatialcoverageId = rawTmgGeospatialcoverage.getTmgGeospatialcoverageId();

		ArrayList<TmgGeospatialcoverageEastwest> eastwests = DataAccess.getTmgGeospatialcoverageEastwestBTmgGeospatialcoverage(rawTmgGeospatialcoverageId);
		for(int i = 0; i<eastwests.size(); i++){
			crawlRawTmgGeospatialcoverageEastwest(cleanTmgGeospatialcoverageId, eastwests.get(i));
		}
		ArrayList<TmgGeospatialcoverageName> names = DataAccess.getTmgGeospatialcoverageNameBTmgGeospatialcoverage(rawTmgGeospatialcoverageId);
		for(int i = 0; i<names.size(); i++){
			crawlRawTmgGeospatialcoverageName(cleanTmgGeospatialcoverageId, names.get(i));
		}
		ArrayList<TmgGeospatialcoverageNorthsouth> northsouths = DataAccess.getTmgGeospatialcoverageNorthsouthBTmgGeospatialcoverage(rawTmgGeospatialcoverageId);
		for(int i = 0; i<northsouths.size(); i++){
			crawlRawTmgGeospatialcoverageNorthsouth(cleanTmgGeospatialcoverageId, northsouths.get(i));
		}
		ArrayList<TmgGeospatialcoverageUpdown> updowns = DataAccess.getTmgGeospatialcoverageUpdownBTmgGeospatialcoverage(rawTmgGeospatialcoverageId);
		for(int i = 0; i<updowns.size(); i++){
			crawlRawTmgGeospatialcoverageUpdown(cleanTmgGeospatialcoverageId, updowns.get(i));
		}
	}
	public void crawlNewTmgKeyword(int parentId, Element child) throws Exception{
		Datavalue value = new Datavalue();
		Datavalue vocabulary = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("vocabulary"))
				vocabulary.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("vocabulary"))
				vocabulary.NOTNULL(a.getValue());
		}
		String val = "";
		ArrayList<org.jdom.Text> values3 = getContent(child.getContent());
		for(int i=0; i<values3.size(); i++){
			val += values3.get(i).getText();
		}
		value.NOTNULL(val);

		DataAccess.insertTmgKeyword(parentId, value, vocabulary);

	}
	public void crawlRawTmgKeyword(int cleanTmgId, TmgKeyword rawTmgKeyword) throws Exception{
		TmgKeyword cleanTmgKeyword = rawTmgKeyword.clone();
		cleanTmgKeyword.setTmgId(cleanTmgId);
		int cleanTmgKeywordId = DataAccess.insertTmgKeyword(cleanTmgKeyword);
		cleanTmgKeyword.setTmgKeywordId(cleanTmgKeywordId);

		cleanTmgKeyword = Applier.applyTmgKeywordRules(cleanTmgId, cleanTmgKeyword);

		if(cleanTmgKeyword == null){
			DataAccess.deleteTmgKeyword(cleanTmgKeywordId);
			return;
		}
		DataAccess.updateTmgKeyword(cleanTmgKeyword);
	}
	public void crawlNewTmgMetadata(int parentId, Element child) throws Exception{
		Datavalue inherited = new Datavalue();
		Datavalue metadatatype = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("inherited"))
				inherited.NOTNULL(a.getValue());
			else if(a.getName().equals("metadatatype"))
				metadatatype.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("inherited"))
				inherited.NOTNULL(a.getValue());
			else if(a.getName().equals("metadatatype"))
				metadatatype.NOTNULL(a.getValue());
		}

		int childId = DataAccess.insertMetadata(inherited, metadatatype);

		DataAccess.insertTmgMetadata(parentId, childId);

		ArrayList<Element> all = getElements(child.getChildren());
		for(int i = 0; i<all.size(); i++){
			Element newchild = all.get(i);
			if(newchild.getName().equals("namespace"))
				crawlNewMetadataNamespace(childId, newchild);
			else if(newchild.getName().equals("xlink"))
				crawlNewMetadataXlink(childId, newchild);
		}
		crawlNewMetadataTmg(childId, child);
	}
	public void crawlRawTmgMetadata(int cleanTmgId, Metadata rawMetadata) throws Exception{
		Metadata cleanMetadata = rawMetadata.clone();
		int cleanMetadataId = DataAccess.insertMetadata(cleanMetadata);
		cleanMetadata.setMetadataId(cleanMetadataId);

		cleanMetadata = Applier.applyTmgMetadataRules(cleanTmgId, cleanMetadata);

		if(cleanMetadata == null){
			DataAccess.deleteMetadata(cleanMetadataId);
			return;
		}
		DataAccess.updateMetadata(cleanMetadata);
		DataAccess.insertTmgMetadata(cleanTmgId, cleanMetadataId);
		int rawMetadataId = rawMetadata.getMetadataId();

		ArrayList<MetadataNamespace> namespaces = DataAccess.getMetadataNamespaceBMetadata(rawMetadataId);
		for(int i = 0; i<namespaces.size(); i++){
			crawlRawMetadataNamespace(cleanMetadataId, namespaces.get(i));
		}
		ArrayList<Tmg> tmgs = DataAccess.getTmgBMetadata(rawMetadataId);
		for(int i = 0; i<tmgs.size(); i++){
			crawlRawMetadataTmg(cleanMetadataId, tmgs.get(i));
		}
		ArrayList<MetadataXlink> xlinks = DataAccess.getMetadataXlinkBMetadata(rawMetadataId);
		for(int i = 0; i<xlinks.size(); i++){
			crawlRawMetadataXlink(cleanMetadataId, xlinks.get(i));
		}
	}
	public void crawlNewTmgProject(int parentId, Element child) throws Exception{
		Datavalue value = new Datavalue();
		Datavalue vocabulary = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("vocabulary"))
				vocabulary.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("vocabulary"))
				vocabulary.NOTNULL(a.getValue());
		}
		String val = "";
		ArrayList<org.jdom.Text> values3 = getContent(child.getContent());
		for(int i=0; i<values3.size(); i++){
			val += values3.get(i).getText();
		}
		value.NOTNULL(val);

		DataAccess.insertTmgProject(parentId, value, vocabulary);

	}
	public void crawlRawTmgProject(int cleanTmgId, TmgProject rawTmgProject) throws Exception{
		TmgProject cleanTmgProject = rawTmgProject.clone();
		cleanTmgProject.setTmgId(cleanTmgId);
		int cleanTmgProjectId = DataAccess.insertTmgProject(cleanTmgProject);
		cleanTmgProject.setTmgProjectId(cleanTmgProjectId);

		cleanTmgProject = Applier.applyTmgProjectRules(cleanTmgId, cleanTmgProject);

		if(cleanTmgProject == null){
			DataAccess.deleteTmgProject(cleanTmgProjectId);
			return;
		}
		DataAccess.updateTmgProject(cleanTmgProject);
	}
	public void crawlNewTmgProperty(int parentId, Element child) throws Exception{
		Datavalue name = new Datavalue();
		Datavalue value = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("name"))
				name.NOTNULL(a.getValue());
		}
		String val = "";
		ArrayList<org.jdom.Text> values3 = getContent(child.getContent());
		for(int i=0; i<values3.size(); i++){
			val += values3.get(i).getText();
		}
		value.NOTNULL(val);

		DataAccess.insertTmgProperty(parentId, name, value);

	}
	public void crawlRawTmgProperty(int cleanTmgId, TmgProperty rawTmgProperty) throws Exception{
		TmgProperty cleanTmgProperty = rawTmgProperty.clone();
		cleanTmgProperty.setTmgId(cleanTmgId);
		int cleanTmgPropertyId = DataAccess.insertTmgProperty(cleanTmgProperty);
		cleanTmgProperty.setTmgPropertyId(cleanTmgPropertyId);

		cleanTmgProperty = Applier.applyTmgPropertyRules(cleanTmgId, cleanTmgProperty);

		if(cleanTmgProperty == null){
			DataAccess.deleteTmgProperty(cleanTmgPropertyId);
			return;
		}
		DataAccess.updateTmgProperty(cleanTmgProperty);
	}
	public void crawlNewTmgPublisher(int parentId, Element child) throws Exception{


		int childId = DataAccess.insertTmgPublisher(parentId);

		ArrayList<Element> all = getElements(child.getChildren());
		for(int i = 0; i<all.size(); i++){
			Element newchild = all.get(i);
			if(newchild.getName().equals("contact"))
				crawlNewTmgPublisherContact(childId, newchild);
			else if(newchild.getName().equals("name"))
				crawlNewTmgPublisherName(childId, newchild);
		}
	}
	public void crawlRawTmgPublisher(int cleanTmgId, TmgPublisher rawTmgPublisher) throws Exception{
		TmgPublisher cleanTmgPublisher = rawTmgPublisher.clone();
		cleanTmgPublisher.setTmgId(cleanTmgId);
		int cleanTmgPublisherId = DataAccess.insertTmgPublisher(cleanTmgPublisher);
		cleanTmgPublisher.setTmgPublisherId(cleanTmgPublisherId);

		cleanTmgPublisher = Applier.applyTmgPublisherRules(cleanTmgId, cleanTmgPublisher);

		if(cleanTmgPublisher == null){
			DataAccess.deleteTmgPublisher(cleanTmgPublisherId);
			return;
		}
		DataAccess.updateTmgPublisher(cleanTmgPublisher);
		int rawTmgPublisherId = rawTmgPublisher.getTmgPublisherId();

		ArrayList<TmgPublisherContact> contacts = DataAccess.getTmgPublisherContactBTmgPublisher(rawTmgPublisherId);
		for(int i = 0; i<contacts.size(); i++){
			crawlRawTmgPublisherContact(cleanTmgPublisherId, contacts.get(i));
		}
		ArrayList<TmgPublisherName> names = DataAccess.getTmgPublisherNameBTmgPublisher(rawTmgPublisherId);
		for(int i = 0; i<names.size(); i++){
			crawlRawTmgPublisherName(cleanTmgPublisherId, names.get(i));
		}
	}
	public void crawlNewTmgServicename(int parentId, Element child) throws Exception{
		Datavalue servicename = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("servicename"))
				servicename.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("servicename"))
				servicename.NOTNULL(a.getValue());
		}

		DataAccess.insertTmgServicename(parentId, servicename);

	}
	public void crawlRawTmgServicename(int cleanTmgId, TmgServicename rawTmgServicename) throws Exception{
		TmgServicename cleanTmgServicename = rawTmgServicename.clone();
		cleanTmgServicename.setTmgId(cleanTmgId);
		int cleanTmgServicenameId = DataAccess.insertTmgServicename(cleanTmgServicename);
		cleanTmgServicename.setTmgServicenameId(cleanTmgServicenameId);

		cleanTmgServicename = Applier.applyTmgServicenameRules(cleanTmgId, cleanTmgServicename);

		if(cleanTmgServicename == null){
			DataAccess.deleteTmgServicename(cleanTmgServicenameId);
			return;
		}
		DataAccess.updateTmgServicename(cleanTmgServicename);
	}
	public void crawlNewTmgTimecoverage(int parentId, Element child) throws Exception{
		Datavalue resolution = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("resolution"))
				resolution.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("resolution"))
				resolution.NOTNULL(a.getValue());
		}

		int childId = DataAccess.insertTmgTimecoverage(parentId, resolution);

		ArrayList<Element> all = getElements(child.getChildren());
		for(int i = 0; i<all.size(); i++){
			Element newchild = all.get(i);
			if(newchild.getName().equals("duration"))
				crawlNewTmgTimecoverageDuration(childId, newchild);
			else if(newchild.getName().equals("end"))
				crawlNewTmgTimecoverageEnd(childId, newchild);
			else if(newchild.getName().equals("resolution"))
				crawlNewTmgTimecoverageResolution(childId, newchild);
			else if(newchild.getName().equals("start"))
				crawlNewTmgTimecoverageStart(childId, newchild);
		}
	}
	public void crawlRawTmgTimecoverage(int cleanTmgId, TmgTimecoverage rawTmgTimecoverage) throws Exception{
		TmgTimecoverage cleanTmgTimecoverage = rawTmgTimecoverage.clone();
		cleanTmgTimecoverage.setTmgId(cleanTmgId);
		int cleanTmgTimecoverageId = DataAccess.insertTmgTimecoverage(cleanTmgTimecoverage);
		cleanTmgTimecoverage.setTmgTimecoverageId(cleanTmgTimecoverageId);

		cleanTmgTimecoverage = Applier.applyTmgTimecoverageRules(cleanTmgId, cleanTmgTimecoverage);

		if(cleanTmgTimecoverage == null){
			DataAccess.deleteTmgTimecoverage(cleanTmgTimecoverageId);
			return;
		}
		DataAccess.updateTmgTimecoverage(cleanTmgTimecoverage);
		int rawTmgTimecoverageId = rawTmgTimecoverage.getTmgTimecoverageId();

		ArrayList<TmgTimecoverageDuration> durations = DataAccess.getTmgTimecoverageDurationBTmgTimecoverage(rawTmgTimecoverageId);
		for(int i = 0; i<durations.size(); i++){
			crawlRawTmgTimecoverageDuration(cleanTmgTimecoverageId, durations.get(i));
		}
		ArrayList<TmgTimecoverageEnd> ends = DataAccess.getTmgTimecoverageEndBTmgTimecoverage(rawTmgTimecoverageId);
		for(int i = 0; i<ends.size(); i++){
			crawlRawTmgTimecoverageEnd(cleanTmgTimecoverageId, ends.get(i));
		}
		ArrayList<TmgTimecoverageResolution> resolutions = DataAccess.getTmgTimecoverageResolutionBTmgTimecoverage(rawTmgTimecoverageId);
		for(int i = 0; i<resolutions.size(); i++){
			crawlRawTmgTimecoverageResolution(cleanTmgTimecoverageId, resolutions.get(i));
		}
		ArrayList<TmgTimecoverageStart> starts = DataAccess.getTmgTimecoverageStartBTmgTimecoverage(rawTmgTimecoverageId);
		for(int i = 0; i<starts.size(); i++){
			crawlRawTmgTimecoverageStart(cleanTmgTimecoverageId, starts.get(i));
		}
	}
	public void crawlNewTmgVariables(int parentId, Element child) throws Exception{
		Datavalue vocabulary = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("vocabulary"))
				vocabulary.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("vocabulary"))
				vocabulary.NOTNULL(a.getValue());
		}

		int childId = DataAccess.insertTmgVariables(parentId, vocabulary);

		ArrayList<Element> all = getElements(child.getChildren());
		for(int i = 0; i<all.size(); i++){
			Element newchild = all.get(i);
			if(newchild.getName().equals("variable"))
				crawlNewTmgVariablesVariable(childId, newchild);
			else if(newchild.getName().equals("variablemap"))
				crawlNewTmgVariablesVariablemap(childId, newchild);
		}
	}
	public void crawlRawTmgVariables(int cleanTmgId, TmgVariables rawTmgVariables) throws Exception{
		TmgVariables cleanTmgVariables = rawTmgVariables.clone();
		cleanTmgVariables.setTmgId(cleanTmgId);
		int cleanTmgVariablesId = DataAccess.insertTmgVariables(cleanTmgVariables);
		cleanTmgVariables.setTmgVariablesId(cleanTmgVariablesId);

		cleanTmgVariables = Applier.applyTmgVariablesRules(cleanTmgId, cleanTmgVariables);

		if(cleanTmgVariables == null){
			DataAccess.deleteTmgVariables(cleanTmgVariablesId);
			return;
		}
		DataAccess.updateTmgVariables(cleanTmgVariables);
		int rawTmgVariablesId = rawTmgVariables.getTmgVariablesId();

		ArrayList<TmgVariablesVariable> variables = DataAccess.getTmgVariablesVariableBTmgVariables(rawTmgVariablesId);
		for(int i = 0; i<variables.size(); i++){
			crawlRawTmgVariablesVariable(cleanTmgVariablesId, variables.get(i));
		}
		ArrayList<TmgVariablesVariablemap> variablemaps = DataAccess.getTmgVariablesVariablemapBTmgVariables(rawTmgVariablesId);
		for(int i = 0; i<variablemaps.size(); i++){
			crawlRawTmgVariablesVariablemap(cleanTmgVariablesId, variablemaps.get(i));
		}
	}
	public void crawlNewTmgGeospatialcoverageEastwest(int parentId, Element child) throws Exception{
		Datavalue resolution = new Datavalue();
		Datavalue size = new Datavalue();
		Datavalue start = new Datavalue();
		Datavalue units = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("resolution"))
				resolution.NOTNULL(a.getValue());
			else if(a.getName().equals("size"))
				size.NOTNULL(a.getValue());
			else if(a.getName().equals("start"))
				start.NOTNULL(a.getValue());
			else if(a.getName().equals("units"))
				units.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("resolution"))
				resolution.NOTNULL(a.getValue());
			else if(a.getName().equals("size"))
				size.NOTNULL(a.getValue());
			else if(a.getName().equals("start"))
				start.NOTNULL(a.getValue());
			else if(a.getName().equals("units"))
				units.NOTNULL(a.getValue());
		}

		DataAccess.insertTmgGeospatialcoverageEastwest(parentId, resolution, size, start, units);

	}
	public void crawlRawTmgGeospatialcoverageEastwest(int cleanTmgGeospatialcoverageId, TmgGeospatialcoverageEastwest rawTmgGeospatialcoverageEastwest) throws Exception{
		TmgGeospatialcoverageEastwest cleanTmgGeospatialcoverageEastwest = rawTmgGeospatialcoverageEastwest.clone();
		cleanTmgGeospatialcoverageEastwest.setTmgGeospatialcoverageId(cleanTmgGeospatialcoverageId);
		int cleanTmgGeospatialcoverageEastwestId = DataAccess.insertTmgGeospatialcoverageEastwest(cleanTmgGeospatialcoverageEastwest);
		cleanTmgGeospatialcoverageEastwest.setTmgGeospatialcoverageEastwestId(cleanTmgGeospatialcoverageEastwestId);

		cleanTmgGeospatialcoverageEastwest = Applier.applyTmgGeospatialcoverageEastwestRules(cleanTmgGeospatialcoverageId, cleanTmgGeospatialcoverageEastwest);

		if(cleanTmgGeospatialcoverageEastwest == null){
			DataAccess.deleteTmgGeospatialcoverageEastwest(cleanTmgGeospatialcoverageEastwestId);
			return;
		}
		DataAccess.updateTmgGeospatialcoverageEastwest(cleanTmgGeospatialcoverageEastwest);
	}
	public void crawlNewTmgGeospatialcoverageName(int parentId, Element child) throws Exception{
		Datavalue value = new Datavalue();
		Datavalue vocabulary = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("vocabulary"))
				vocabulary.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("vocabulary"))
				vocabulary.NOTNULL(a.getValue());
		}
		String val = "";
		ArrayList<org.jdom.Text> values3 = getContent(child.getContent());
		for(int i=0; i<values3.size(); i++){
			val += values3.get(i).getText();
		}
		value.NOTNULL(val);

		DataAccess.insertTmgGeospatialcoverageName(parentId, value, vocabulary);

	}
	public void crawlRawTmgGeospatialcoverageName(int cleanTmgGeospatialcoverageId, TmgGeospatialcoverageName rawTmgGeospatialcoverageName) throws Exception{
		TmgGeospatialcoverageName cleanTmgGeospatialcoverageName = rawTmgGeospatialcoverageName.clone();
		cleanTmgGeospatialcoverageName.setTmgGeospatialcoverageId(cleanTmgGeospatialcoverageId);
		int cleanTmgGeospatialcoverageNameId = DataAccess.insertTmgGeospatialcoverageName(cleanTmgGeospatialcoverageName);
		cleanTmgGeospatialcoverageName.setTmgGeospatialcoverageNameId(cleanTmgGeospatialcoverageNameId);

		cleanTmgGeospatialcoverageName = Applier.applyTmgGeospatialcoverageNameRules(cleanTmgGeospatialcoverageId, cleanTmgGeospatialcoverageName);

		if(cleanTmgGeospatialcoverageName == null){
			DataAccess.deleteTmgGeospatialcoverageName(cleanTmgGeospatialcoverageNameId);
			return;
		}
		DataAccess.updateTmgGeospatialcoverageName(cleanTmgGeospatialcoverageName);
	}
	public void crawlNewTmgGeospatialcoverageNorthsouth(int parentId, Element child) throws Exception{
		Datavalue resolution = new Datavalue();
		Datavalue size = new Datavalue();
		Datavalue start = new Datavalue();
		Datavalue units = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("resolution"))
				resolution.NOTNULL(a.getValue());
			else if(a.getName().equals("size"))
				size.NOTNULL(a.getValue());
			else if(a.getName().equals("start"))
				start.NOTNULL(a.getValue());
			else if(a.getName().equals("units"))
				units.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("resolution"))
				resolution.NOTNULL(a.getValue());
			else if(a.getName().equals("size"))
				size.NOTNULL(a.getValue());
			else if(a.getName().equals("start"))
				start.NOTNULL(a.getValue());
			else if(a.getName().equals("units"))
				units.NOTNULL(a.getValue());
		}

		DataAccess.insertTmgGeospatialcoverageNorthsouth(parentId, resolution, size, start, units);

	}
	public void crawlRawTmgGeospatialcoverageNorthsouth(int cleanTmgGeospatialcoverageId, TmgGeospatialcoverageNorthsouth rawTmgGeospatialcoverageNorthsouth) throws Exception{
		TmgGeospatialcoverageNorthsouth cleanTmgGeospatialcoverageNorthsouth = rawTmgGeospatialcoverageNorthsouth.clone();
		cleanTmgGeospatialcoverageNorthsouth.setTmgGeospatialcoverageId(cleanTmgGeospatialcoverageId);
		int cleanTmgGeospatialcoverageNorthsouthId = DataAccess.insertTmgGeospatialcoverageNorthsouth(cleanTmgGeospatialcoverageNorthsouth);
		cleanTmgGeospatialcoverageNorthsouth.setTmgGeospatialcoverageNorthsouthId(cleanTmgGeospatialcoverageNorthsouthId);

		cleanTmgGeospatialcoverageNorthsouth = Applier.applyTmgGeospatialcoverageNorthsouthRules(cleanTmgGeospatialcoverageId, cleanTmgGeospatialcoverageNorthsouth);

		if(cleanTmgGeospatialcoverageNorthsouth == null){
			DataAccess.deleteTmgGeospatialcoverageNorthsouth(cleanTmgGeospatialcoverageNorthsouthId);
			return;
		}
		DataAccess.updateTmgGeospatialcoverageNorthsouth(cleanTmgGeospatialcoverageNorthsouth);
	}
	public void crawlNewTmgGeospatialcoverageUpdown(int parentId, Element child) throws Exception{
		Datavalue resolution = new Datavalue();
		Datavalue size = new Datavalue();
		Datavalue start = new Datavalue();
		Datavalue units = new Datavalue();

		ArrayList<Attribute> values = getAttributes(child.getAttributes());
		for(int i=0; i<values.size(); i++){
			Attribute a = values.get(i);
			if(a.getName().equals("resolution"))
				resolution.NOTNULL(a.getValue());
			else if(a.getName().equals("size"))
				size.NOTNULL(a.getValue());
			else if(a.getName().equals("start"))
				start.NOTNULL(a.getValue());
			else if(a.getName().equals("units"))
				units.NOTNULL(a.getValue());
		}
		ArrayList<Element> values2 = getElements(child.getChildren());
		for(int i=0; i<values2.size(); i++){
			Element a = values2.get(i);
			if(a.getName().equals("resolution"))
				resolution.NOTNULL(a.getValue());
			else if(a.getName().equals("size"))
				size.NOTNULL(a.getValue());
			else if(a.getName().equals("start"))
				start.NOTNULL(a.getValue());
			else if(a.getName().equals("units"))
				units.NOTNULL(a.getValue());
		}

		DataAccess.insertTmgGeospatialcoverageUpdown(parentId, resolution, size, start, units);

	}
	public void crawlRawTmgGeospatialcoverageUpdown(int cleanTmgGeospatialcoverageId, TmgGeospatialcoverageUpdown rawTmgGeospatialcoverageUpdown) throws Exception{
		TmgGeospatialcoverageUpdown cleanTmgGeospatialcoverageUpdown = rawTmgGeospatialcoverageUpdown.clone();
		cleanTmgGeospatialcoverageUpdown.setTmgGeospatialcoverageId(cleanTmgGeospatialcoverageId);
		int cleanTmgGeospatialcoverageUpdownId = DataAccess.insertTmgGeospatialcoverageUpdown(cleanTmgGeospatialcoverageUpdown);
		cleanTmgGeospatialcoverageUpdown.setTmgGeospatialcoverageUpdownId(cleanTmgGeospatialcoverageUpdownId);

		cleanTmgGeospatialcoverageUpdown = Applier.applyTmgGeospatialcoverageUpdownRules(cleanTmgGeospatialcoverageId, cleanTmgGeospatialcoverageUpdown);

		if(cleanTmgGeospatialcoverageUpdown == null){
			DataAccess.deleteTmgGeospatialcoverageUpdown(cleanTmgGeospatialcoverageUpdownId);
			return;
		}
		DataAccess.updateTmgGeospatialcoverageUpdown(cleanTmgGeospatialcoverageUpdown);
	}
}