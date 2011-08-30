package gov.noaa.pmel.tmap.catalogcleaner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.noaa.pmel.tmap.catalogcleaner.data.*;

public class DataAccess {

	private static Properties configFile = null;
	private static Logger log = LoggerFactory.getLogger(DataAccess.class);
	private static Connection pgCache = null;


	public static void init(CatalogCleaner cleaner){
		configFile = new Properties();
		try{
			configFile.load(cleaner.getClass().getClassLoader().getResourceAsStream("myconfig.properties"));
			pgCache = DriverManager.getConnection(configFile.getProperty("PGURL"), configFile.getProperty("PGUSERNAME"), configFile.getProperty("PGPASSWORD"));
			//			boolean iic = pgCache.isClosed();
			//			System.out.println(iic);
			//			pgCache.close();
			//			iic = pgCache.isClosed();
			//			System.out.println(iic);
		}
		catch(Exception e){
			// do something
			System.out.println("Error: " + e.getMessage());
		}
	}
	public void printTest(){
		System.out.println(configFile.getProperty("PGURL"));
	}

	public static void main(String[] args){
		DataAccess da = new DataAccess();
		da.printTest();
		log.info("testing");
	}

	public static Collection<String> getCatalogItems(){
		// just a stub to get started
		ArrayList<String> list = new ArrayList<String>();
		return list;
	}
	public static String getOrder(int num){
		// just a stub to get started
		return "Some Order";
	}
	/**
	 * This won't be called when an out of memory exception is thrown.
	 */
	@Override
	protected void finalize() throws Exception {
		saveState();
	}

	/** Force the cache to save its state now.
	 *
	 * @TODO Determine if this is needed.
	 * @throws Exception
	 */
	public static void saveState() throws Exception {

		if (pgCache != null) {
			pgCache.close();
			pgCache = null;
		}

		System.out.println("Closing PG connection");
	}
	public static PreparedStatement setPreparedStatement(String functionName) throws Exception{

		PreparedStatement ps = null;
		//	try {
		String insert = "select " + functionName + "()";
	
		ps = pgCache.prepareStatement(insert);

		return ps;
	}
	public static PreparedStatement setPreparedStatement(String functionName, int[] theInts) throws Exception{

		if(theInts.length == 0)
			throw new Exception("Invalid int length");

		PreparedStatement ps = null;

		String insert = "select " + functionName + " (";
		int intSize = theInts.length;
		for(int i = 0; i<intSize-1; i++){
			insert += "?, ";
		}
		insert += "?)";
		ps = pgCache.prepareStatement(insert);

		for(int i = 0; i<intSize; i++){
			int theNum = theInts[i];				
			ps.setInt(i+1, theNum);
		}

		return ps;
	}
	public static PreparedStatement setPreparedStatement(String functionName, Datavalue[] theList) throws Exception{

		if(theList.length == 0)
			throw new Exception("Invalid vars length");

		PreparedStatement ps = null;
		int size = theList.length;
		//	try {
		String insert = "select " + functionName + " (";
		for(int i = 0; i<size-1; i++){
			insert += "?, ";
		}
		insert += "?)";
		ps = pgCache.prepareStatement(insert);
		for(int i = 0; i<size; i++){
			Datavalue name = theList[i];				
			if(name.isNull())
				ps.setNull(i+1, java.sql.Types.VARCHAR);
			else
				ps.setString(i+1, name.getValue());
		}

		return ps;
	}
	public static PreparedStatement setPreparedStatement(String functionName, int[] theInts, Datavalue[] theList) throws Exception{

		// just to make sure
		if(theInts.length == 0)
			throw new Exception("Invalid vars length");
		// just to make sure
		else if(theList.length == 0)
			throw new Exception("Invalid vars length");

		PreparedStatement ps = null;
		String insert = "select " + functionName + " (";
		int intSize = theInts.length;
		for(int i = 0; i<intSize; i++){
			insert += "?, ";
		}
		int strSize = theList.length;
		//	try {
		for(int i = 0; i<strSize - 1; i++){
			insert += "?, ";
		}
		insert += "?)";
		ps = pgCache.prepareStatement(insert);
		for(int i = 0; i<intSize; i++){
			int theNum = theInts[i];				
			ps.setInt(i+1, theNum);
		}
		for(int i = 0; i<strSize; i++){
			Datavalue name = theList[i];				
			if(name.isNull())
				ps.setNull(i+1 + intSize, java.sql.Types.VARCHAR);
			else
				ps.setString(i+1+ intSize, name.getValue());
		}

		return ps;
	}
	
	public static int runStatement(PreparedStatement ps) throws Exception{
		ResultSet rs = null;
		int id = -1;

		try{
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			id = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
	//			rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return id;
	}
	public static Hashtable<String, String> getObject(String tablename, int id) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Hashtable<String, String> h = new Hashtable<String, String>();
		try {
			ps = pgCache.prepareStatement("select * from " + tablename + " where " + tablename + "_id=?");
			ps.setInt(1, id);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			
			 // Get result set meta data
		    ResultSetMetaData rsmd = rs.getMetaData();
		    int numColumns = rsmd.getColumnCount();
			
			rs.next();
			
		    // Get the column names; column indices start from 1
		     for (int i=1; i<numColumns+1; i++) {
		        String columnName = rsmd.getColumnName(i);
		        String columnType = rsmd.getColumnTypeName(i);
		        if(columnType.indexOf("int") > -1)
		        	h.put(columnName, rs.getInt(i) + "");
		        else
		        	if(rs.getString(i) != null)
		        		h.put(columnName, rs.getString(i));

		    }
			
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//					rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return h;
	}
	public static ArrayList<Integer> getObjects(String tablename, String parenttable, int parentId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> startIds = new ArrayList<Integer>();
		String select = "select " + tablename + "_id from " + tablename + " where ";
		select+= parenttable + "_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, parentId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				startIds.add(rs.getInt(tablename + "_id"));
			}		
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//					rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return startIds;
	}
	public static ArrayList<Integer> getObjects(String tablename, String parenttable, String childtable, int parentId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> startIds = new ArrayList<Integer>();
		String select = "select " + childtable + "_id from " + tablename + " where ";
		select+= parenttable + "_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, parentId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				startIds.add(rs.getInt(childtable + "_id"));
			}		
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//					rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return startIds;
	}
	

	public static Catalog getCatalog(String uri) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		Catalog catalog = null;

		try {
			ps = pgCache.prepareStatement("select * from Catalog where xmlns=?");
			ps.setString(1, uri);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				int catalogId = rs.getInt("catalog_id");
				Datavalue xmlns = new Datavalue(rs.getString("xmlns"));
				Datavalue name = new Datavalue(rs.getString("name"));
				Datavalue base = new Datavalue(rs.getString("base"));
				Datavalue version = new Datavalue(rs.getString("version"));
				Datavalue expires = new Datavalue(rs.getString("expires"));
				Datavalue status = new Datavalue(rs.getString("status"));
				catalog = new Catalog(catalogId, xmlns, name, base, version, expires, new Datavalue(null), status);
			}
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} 
		catch(Exception e){
			if(e.getMessage().equals("null")){
				// is this how postgres deals with null? 
			}
			else{
				log.error("Error. {}", e);
				throw new Exception("Exception: " + e.getMessage());
			}
		}finally {
			try {
				ps.close();
//					rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalog;
	}
	


	/*begin get methods*/

	public static Catalog getCatalog(int catalogId) throws Exception{

		Catalog catalog = null;
		Hashtable<String, String> hash = getObject("catalog", catalogId);
		Datavalue cleanCatalogId = new Datavalue(hash.get("cleanCatalogId"));
		Datavalue base = new Datavalue(hash.get("base"));
		Datavalue expires = new Datavalue(hash.get("expires"));
		Datavalue name = new Datavalue(hash.get("name"));
		Datavalue version = new Datavalue(hash.get("version"));
		Datavalue xmlns = new Datavalue(hash.get("xmlns"));
		Datavalue status = new Datavalue(hash.get("status"));
		catalog = new Catalog(catalogId, cleanCatalogId, base, expires, name, version, xmlns, status);
		return catalog;
	}
	public static ArrayList<Dataset> getDatasetBCatalog(int catalogId) throws Exception{
		ArrayList<Dataset> datasets = new ArrayList<Dataset>();
		ArrayList<Integer> datasetIds = getObjects("catalog_dataset", "catalog", "dataset", catalogId);
		for(int i=0; i<datasetIds.size(); i++){
			datasets.add(getDataset(datasetIds.get(i)));
		}
		return datasets;
	}
	public static CatalogProperty getCatalogProperty(int catalogPropertyId) throws Exception{

		CatalogProperty catalogProperty = null;
		Hashtable<String, String> hash = getObject("catalog_property", catalogPropertyId);
		int catalogId = Integer.parseInt(hash.get("catalog_id"));
		Datavalue name = new Datavalue(hash.get("name"));
		Datavalue value = new Datavalue(hash.get("value"));
		catalogProperty = new CatalogProperty(catalogId, catalogPropertyId, name, value);
		return catalogProperty;
	}
	public static ArrayList<CatalogProperty> getCatalogPropertyBCatalog(int catalogId) throws Exception{
		ArrayList<CatalogProperty> propertys = new ArrayList<CatalogProperty>();
		ArrayList<Integer> propertyIds = getObjects("catalog_property", "catalog", catalogId);
		for(int i=0; i<propertyIds.size(); i++){
			propertys.add(getCatalogProperty(propertyIds.get(i)));
		}
		return propertys;
	}
	public static ArrayList<Service> getServiceBCatalog(int catalogId) throws Exception{
		ArrayList<Service> services = new ArrayList<Service>();
		ArrayList<Integer> serviceIds = getObjects("catalog_service", "catalog", "service", catalogId);
		for(int i=0; i<serviceIds.size(); i++){
			services.add(getService(serviceIds.get(i)));
		}
		return services;
	}
	public static CatalogXlink getCatalogXlink(int catalogXlinkId) throws Exception{

		CatalogXlink catalogXlink = null;
		Hashtable<String, String> hash = getObject("catalog_xlink", catalogXlinkId);
		int catalogId = Integer.parseInt(hash.get("catalog_id"));
		Datavalue value = new Datavalue(hash.get("value"));
		Datavalue xlink = new Datavalue(hash.get("xlink"));
		if (xlink.isNull())
			xlink = new Datavalue(hash.get("xlink_nonstandard"));
		catalogXlink = new CatalogXlink(catalogId, catalogXlinkId, value, xlink);
		return catalogXlink;
	}
	public static ArrayList<CatalogXlink> getCatalogXlinkBCatalog(int catalogId) throws Exception{
		ArrayList<CatalogXlink> xlinks = new ArrayList<CatalogXlink>();
		ArrayList<Integer> xlinkIds = getObjects("catalog_xlink", "catalog", catalogId);
		for(int i=0; i<xlinkIds.size(); i++){
			xlinks.add(getCatalogXlink(xlinkIds.get(i)));
		}
		return xlinks;
	}
	public static Catalogref getCatalogref(int catalogrefId) throws Exception{

		Catalogref catalogref = null;
		Hashtable<String, String> hash = getObject("catalogref", catalogrefId);
		catalogref = new Catalogref(catalogrefId);
		return catalogref;
	}
	public static CatalogrefDocumentation getCatalogrefDocumentation(int catalogrefDocumentationId) throws Exception{

		CatalogrefDocumentation catalogrefDocumentation = null;
		Hashtable<String, String> hash = getObject("catalogref_documentation", catalogrefDocumentationId);
		int catalogrefId = Integer.parseInt(hash.get("catalogref_id"));
		Datavalue value = new Datavalue(hash.get("value"));
		Datavalue documentationenum = new Datavalue(hash.get("documentationenum"));
		if (documentationenum.isNull())
			documentationenum = new Datavalue(hash.get("documentationenum_nonstandard"));
		catalogrefDocumentation = new CatalogrefDocumentation(catalogrefId, catalogrefDocumentationId, value, documentationenum);
		return catalogrefDocumentation;
	}
	public static ArrayList<CatalogrefDocumentation> getCatalogrefDocumentationBCatalogref(int catalogrefId) throws Exception{
		ArrayList<CatalogrefDocumentation> documentations = new ArrayList<CatalogrefDocumentation>();
		ArrayList<Integer> documentationIds = getObjects("catalogref_documentation", "catalogref", catalogrefId);
		for(int i=0; i<documentationIds.size(); i++){
			documentations.add(getCatalogrefDocumentation(documentationIds.get(i)));
		}
		return documentations;
	}
	public static CatalogrefDocumentationNamespace getCatalogrefDocumentationNamespace(int catalogrefDocumentationNamespaceId) throws Exception{

		CatalogrefDocumentationNamespace catalogrefDocumentationNamespace = null;
		Hashtable<String, String> hash = getObject("catalogref_documentation_namespace", catalogrefDocumentationNamespaceId);
		int catalogrefDocumentationId = Integer.parseInt(hash.get("catalogref_documentation_id"));
		Datavalue namespace = new Datavalue(hash.get("namespace"));
		catalogrefDocumentationNamespace = new CatalogrefDocumentationNamespace(catalogrefDocumentationId, catalogrefDocumentationNamespaceId, namespace);
		return catalogrefDocumentationNamespace;
	}
	public static ArrayList<CatalogrefDocumentationNamespace> getCatalogrefDocumentationNamespaceBCatalogrefDocumentation(int catalogrefDocumentationId) throws Exception{
		ArrayList<CatalogrefDocumentationNamespace> namespaces = new ArrayList<CatalogrefDocumentationNamespace>();
		ArrayList<Integer> namespaceIds = getObjects("catalogref_documentation_namespace", "catalogref_documentation", catalogrefDocumentationId);
		for(int i=0; i<namespaceIds.size(); i++){
			namespaces.add(getCatalogrefDocumentationNamespace(namespaceIds.get(i)));
		}
		return namespaces;
	}
	public static CatalogrefDocumentationXlink getCatalogrefDocumentationXlink(int catalogrefDocumentationXlinkId) throws Exception{

		CatalogrefDocumentationXlink catalogrefDocumentationXlink = null;
		Hashtable<String, String> hash = getObject("catalogref_documentation_xlink", catalogrefDocumentationXlinkId);
		int catalogrefDocumentationId = Integer.parseInt(hash.get("catalogref_documentation_id"));
		Datavalue value = new Datavalue(hash.get("value"));
		Datavalue xlink = new Datavalue(hash.get("xlink"));
		if (xlink.isNull())
			xlink = new Datavalue(hash.get("xlink_nonstandard"));
		catalogrefDocumentationXlink = new CatalogrefDocumentationXlink(catalogrefDocumentationId, catalogrefDocumentationXlinkId, value, xlink);
		return catalogrefDocumentationXlink;
	}
	public static ArrayList<CatalogrefDocumentationXlink> getCatalogrefDocumentationXlinkBCatalogrefDocumentation(int catalogrefDocumentationId) throws Exception{
		ArrayList<CatalogrefDocumentationXlink> xlinks = new ArrayList<CatalogrefDocumentationXlink>();
		ArrayList<Integer> xlinkIds = getObjects("catalogref_documentation_xlink", "catalogref_documentation", catalogrefDocumentationId);
		for(int i=0; i<xlinkIds.size(); i++){
			xlinks.add(getCatalogrefDocumentationXlink(xlinkIds.get(i)));
		}
		return xlinks;
	}
	public static CatalogrefXlink getCatalogrefXlink(int catalogrefXlinkId) throws Exception{

		CatalogrefXlink catalogrefXlink = null;
		Hashtable<String, String> hash = getObject("catalogref_xlink", catalogrefXlinkId);
		int catalogrefId = Integer.parseInt(hash.get("catalogref_id"));
		Datavalue value = new Datavalue(hash.get("value"));
		Datavalue xlink = new Datavalue(hash.get("xlink"));
		if (xlink.isNull())
			xlink = new Datavalue(hash.get("xlink_nonstandard"));
		catalogrefXlink = new CatalogrefXlink(catalogrefId, catalogrefXlinkId, value, xlink);
		return catalogrefXlink;
	}
	public static ArrayList<CatalogrefXlink> getCatalogrefXlinkBCatalogref(int catalogrefId) throws Exception{
		ArrayList<CatalogrefXlink> xlinks = new ArrayList<CatalogrefXlink>();
		ArrayList<Integer> xlinkIds = getObjects("catalogref_xlink", "catalogref", catalogrefId);
		for(int i=0; i<xlinkIds.size(); i++){
			xlinks.add(getCatalogrefXlink(xlinkIds.get(i)));
		}
		return xlinks;
	}
	public static Dataset getDataset(int datasetId) throws Exception{

		Dataset dataset = null;
		Hashtable<String, String> hash = getObject("dataset", datasetId);
		Datavalue alias = new Datavalue(hash.get("alias"));
		Datavalue authority = new Datavalue(hash.get("authority"));
		Datavalue dId = new Datavalue(hash.get("d_id"));
		Datavalue harvest = new Datavalue(hash.get("harvest"));
		Datavalue name = new Datavalue(hash.get("name"));
		Datavalue resourcecontrol = new Datavalue(hash.get("resourcecontrol"));
		Datavalue serviceName = new Datavalue(hash.get("serviceName"));
		Datavalue urlPath = new Datavalue(hash.get("urlPath"));
		Datavalue collectiontype = new Datavalue(hash.get("collectiontype"));
		if (collectiontype.isNull())
			collectiontype = new Datavalue(hash.get("collectiontype_nonstandard"));
		Datavalue datasizeUnit = new Datavalue(hash.get("datasize_unit"));
		if (datasizeUnit.isNull())
			datasizeUnit = new Datavalue(hash.get("datasize_unit_nonstandard"));
		Datavalue dataType = new Datavalue(hash.get("dataType"));
		if (dataType.isNull())
			dataType = new Datavalue(hash.get("dataType_nonstandard"));
		Datavalue status = new Datavalue(hash.get("status"));
		dataset = new Dataset(datasetId, alias, authority, dId, harvest, name, resourcecontrol, serviceName, urlPath, collectiontype, datasizeUnit, dataType, status);
		return dataset;
	}
	public static DatasetAccess getDatasetAccess(int datasetAccessId) throws Exception{

		DatasetAccess datasetAccess = null;
		Hashtable<String, String> hash = getObject("dataset_access", datasetAccessId);
		int datasetId = Integer.parseInt(hash.get("dataset_id"));
		Datavalue servicename = new Datavalue(hash.get("servicename"));
		Datavalue urlpath = new Datavalue(hash.get("urlpath"));
		Datavalue dataformat = new Datavalue(hash.get("dataformat"));
		if (dataformat.isNull())
			dataformat = new Datavalue(hash.get("dataformat_nonstandard"));
		datasetAccess = new DatasetAccess(datasetId, datasetAccessId, servicename, urlpath, dataformat);
		return datasetAccess;
	}
	public static ArrayList<DatasetAccess> getDatasetAccessBDataset(int datasetId) throws Exception{
		ArrayList<DatasetAccess> accesss = new ArrayList<DatasetAccess>();
		ArrayList<Integer> accessIds = getObjects("dataset_access", "dataset", datasetId);
		for(int i=0; i<accessIds.size(); i++){
			accesss.add(getDatasetAccess(accessIds.get(i)));
		}
		return accesss;
	}
	public static DatasetAccessDatasize getDatasetAccessDatasize(int datasetAccessDatasizeId) throws Exception{

		DatasetAccessDatasize datasetAccessDatasize = null;
		Hashtable<String, String> hash = getObject("dataset_access_datasize", datasetAccessDatasizeId);
		int datasetAccessId = Integer.parseInt(hash.get("dataset_access_id"));
		Datavalue value = new Datavalue(hash.get("value"));
		Datavalue units = new Datavalue(hash.get("units"));
		if (units.isNull())
			units = new Datavalue(hash.get("units_nonstandard"));
		datasetAccessDatasize = new DatasetAccessDatasize(datasetAccessId, datasetAccessDatasizeId, value, units);
		return datasetAccessDatasize;
	}
	public static ArrayList<DatasetAccessDatasize> getDatasetAccessDatasizeBDatasetAccess(int datasetAccessId) throws Exception{
		ArrayList<DatasetAccessDatasize> datasizes = new ArrayList<DatasetAccessDatasize>();
		ArrayList<Integer> datasizeIds = getObjects("dataset_access_datasize", "dataset_access", datasetAccessId);
		for(int i=0; i<datasizeIds.size(); i++){
			datasizes.add(getDatasetAccessDatasize(datasizeIds.get(i)));
		}
		return datasizes;
	}
	public static ArrayList<Catalogref> getCatalogrefBDataset(int datasetId) throws Exception{
		ArrayList<Catalogref> catalogrefs = new ArrayList<Catalogref>();
		ArrayList<Integer> catalogrefIds = getObjects("dataset_catalogref", "dataset", "catalogref", datasetId);
		for(int i=0; i<catalogrefIds.size(); i++){
			catalogrefs.add(getCatalogref(catalogrefIds.get(i)));
		}
		return catalogrefs;
	}
	public static ArrayList<Dataset> getDatasetBDataset(int parentId) throws Exception{
		ArrayList<Dataset> datasets = new ArrayList<Dataset>();
		ArrayList<Integer> childIds = getObjects("dataset_dataset", "parent", "child", parentId);
		for(int i=0; i<childIds.size(); i++){
			datasets.add(getDataset(childIds.get(i)));
		}
		return datasets;
	}
	public static DatasetNcml getDatasetNcml(int datasetNcmlId) throws Exception{

		DatasetNcml datasetNcml = null;
		Hashtable<String, String> hash = getObject("dataset_ncml", datasetNcmlId);
		int datasetId = Integer.parseInt(hash.get("dataset_id"));
		datasetNcml = new DatasetNcml(datasetId, datasetNcmlId);
		return datasetNcml;
	}
	public static ArrayList<DatasetNcml> getDatasetNcmlBDataset(int datasetId) throws Exception{
		ArrayList<DatasetNcml> ncmls = new ArrayList<DatasetNcml>();
		ArrayList<Integer> ncmlIds = getObjects("dataset_ncml", "dataset", datasetId);
		for(int i=0; i<ncmlIds.size(); i++){
			ncmls.add(getDatasetNcml(ncmlIds.get(i)));
		}
		return ncmls;
	}
	public static DatasetProperty getDatasetProperty(int datasetPropertyId) throws Exception{

		DatasetProperty datasetProperty = null;
		Hashtable<String, String> hash = getObject("dataset_property", datasetPropertyId);
		int datasetId = Integer.parseInt(hash.get("dataset_id"));
		Datavalue name = new Datavalue(hash.get("name"));
		Datavalue value = new Datavalue(hash.get("value"));
		datasetProperty = new DatasetProperty(datasetId, datasetPropertyId, name, value);
		return datasetProperty;
	}
	public static ArrayList<DatasetProperty> getDatasetPropertyBDataset(int datasetId) throws Exception{
		ArrayList<DatasetProperty> propertys = new ArrayList<DatasetProperty>();
		ArrayList<Integer> propertyIds = getObjects("dataset_property", "dataset", datasetId);
		for(int i=0; i<propertyIds.size(); i++){
			propertys.add(getDatasetProperty(propertyIds.get(i)));
		}
		return propertys;
	}
	public static ArrayList<Service> getServiceBDataset(int datasetId) throws Exception{
		ArrayList<Service> services = new ArrayList<Service>();
		ArrayList<Integer> serviceIds = getObjects("dataset_service", "dataset", "service", datasetId);
		for(int i=0; i<serviceIds.size(); i++){
			services.add(getService(serviceIds.get(i)));
		}
		return services;
	}
	public static ArrayList<Tmg> getTmgBDataset(int datasetId) throws Exception{
		ArrayList<Tmg> tmgs = new ArrayList<Tmg>();
		ArrayList<Integer> tmgIds = getObjects("dataset_tmg", "dataset", "tmg", datasetId);
		for(int i=0; i<tmgIds.size(); i++){
			tmgs.add(getTmg(tmgIds.get(i)));
		}
		return tmgs;
	}
	public static Metadata getMetadata(int metadataId) throws Exception{

		Metadata metadata = null;
		Hashtable<String, String> hash = getObject("metadata", metadataId);
		Datavalue inherited = new Datavalue(hash.get("inherited"));
		if (inherited.isNull())
			inherited = new Datavalue(hash.get("inherited_nonstandard"));
		Datavalue metadatatype = new Datavalue(hash.get("metadatatype"));
		if (metadatatype.isNull())
			metadatatype = new Datavalue(hash.get("metadatatype_nonstandard"));
		metadata = new Metadata(metadataId, inherited, metadatatype);
		return metadata;
	}
	public static MetadataNamespace getMetadataNamespace(int metadataNamespaceId) throws Exception{

		MetadataNamespace metadataNamespace = null;
		Hashtable<String, String> hash = getObject("metadata_namespace", metadataNamespaceId);
		int metadataId = Integer.parseInt(hash.get("metadata_id"));
		Datavalue namespace = new Datavalue(hash.get("namespace"));
		metadataNamespace = new MetadataNamespace(metadataId, metadataNamespaceId, namespace);
		return metadataNamespace;
	}
	public static ArrayList<MetadataNamespace> getMetadataNamespaceBMetadata(int metadataId) throws Exception{
		ArrayList<MetadataNamespace> namespaces = new ArrayList<MetadataNamespace>();
		ArrayList<Integer> namespaceIds = getObjects("metadata_namespace", "metadata", metadataId);
		for(int i=0; i<namespaceIds.size(); i++){
			namespaces.add(getMetadataNamespace(namespaceIds.get(i)));
		}
		return namespaces;
	}
	public static ArrayList<Tmg> getTmgBMetadata(int metadataId) throws Exception{
		ArrayList<Tmg> tmgs = new ArrayList<Tmg>();
		ArrayList<Integer> tmgIds = getObjects("metadata_tmg", "metadata", "tmg", metadataId);
		for(int i=0; i<tmgIds.size(); i++){
			tmgs.add(getTmg(tmgIds.get(i)));
		}
		return tmgs;
	}
	public static MetadataXlink getMetadataXlink(int metadataXlinkId) throws Exception{

		MetadataXlink metadataXlink = null;
		Hashtable<String, String> hash = getObject("metadata_xlink", metadataXlinkId);
		int metadataId = Integer.parseInt(hash.get("metadata_id"));
		Datavalue value = new Datavalue(hash.get("value"));
		Datavalue xlink = new Datavalue(hash.get("xlink"));
		if (xlink.isNull())
			xlink = new Datavalue(hash.get("xlink_nonstandard"));
		metadataXlink = new MetadataXlink(metadataId, metadataXlinkId, value, xlink);
		return metadataXlink;
	}
	public static ArrayList<MetadataXlink> getMetadataXlinkBMetadata(int metadataId) throws Exception{
		ArrayList<MetadataXlink> xlinks = new ArrayList<MetadataXlink>();
		ArrayList<Integer> xlinkIds = getObjects("metadata_xlink", "metadata", metadataId);
		for(int i=0; i<xlinkIds.size(); i++){
			xlinks.add(getMetadataXlink(xlinkIds.get(i)));
		}
		return xlinks;
	}
	public static Service getService(int serviceId) throws Exception{

		Service service = null;
		Hashtable<String, String> hash = getObject("service", serviceId);
		Datavalue base = new Datavalue(hash.get("base"));
		Datavalue desc = new Datavalue(hash.get("desc"));
		Datavalue name = new Datavalue(hash.get("name"));
		Datavalue suffix = new Datavalue(hash.get("suffix"));
		Datavalue serviceType = new Datavalue(hash.get("serviceType"));
		if (serviceType.isNull())
			serviceType = new Datavalue(hash.get("serviceType_nonstandard"));
		Datavalue status = new Datavalue(hash.get("status"));
		service = new Service(serviceId, base, desc, name, suffix, serviceType, status);
		return service;
	}
	public static ServiceDatasetroot getServiceDatasetroot(int serviceDatasetrootId) throws Exception{

		ServiceDatasetroot serviceDatasetroot = null;
		Hashtable<String, String> hash = getObject("service_datasetroot", serviceDatasetrootId);
		int serviceId = Integer.parseInt(hash.get("service_id"));
		Datavalue location = new Datavalue(hash.get("location"));
		Datavalue path = new Datavalue(hash.get("path"));
		serviceDatasetroot = new ServiceDatasetroot(serviceId, serviceDatasetrootId, location, path);
		return serviceDatasetroot;
	}
	public static ArrayList<ServiceDatasetroot> getServiceDatasetrootBService(int serviceId) throws Exception{
		ArrayList<ServiceDatasetroot> datasetroots = new ArrayList<ServiceDatasetroot>();
		ArrayList<Integer> datasetrootIds = getObjects("service_datasetroot", "service", serviceId);
		for(int i=0; i<datasetrootIds.size(); i++){
			datasetroots.add(getServiceDatasetroot(datasetrootIds.get(i)));
		}
		return datasetroots;
	}
	public static ServiceProperty getServiceProperty(int servicePropertyId) throws Exception{

		ServiceProperty serviceProperty = null;
		Hashtable<String, String> hash = getObject("service_property", servicePropertyId);
		int serviceId = Integer.parseInt(hash.get("service_id"));
		Datavalue name = new Datavalue(hash.get("name"));
		Datavalue value = new Datavalue(hash.get("value"));
		serviceProperty = new ServiceProperty(serviceId, servicePropertyId, name, value);
		return serviceProperty;
	}
	public static ArrayList<ServiceProperty> getServicePropertyBService(int serviceId) throws Exception{
		ArrayList<ServiceProperty> propertys = new ArrayList<ServiceProperty>();
		ArrayList<Integer> propertyIds = getObjects("service_property", "service", serviceId);
		for(int i=0; i<propertyIds.size(); i++){
			propertys.add(getServiceProperty(propertyIds.get(i)));
		}
		return propertys;
	}
	public static ArrayList<Service> getServiceBService(int parentId) throws Exception{
		ArrayList<Service> services = new ArrayList<Service>();
		ArrayList<Integer> childIds = getObjects("service_service", "parent", "child", parentId);
		for(int i=0; i<childIds.size(); i++){
			services.add(getService(childIds.get(i)));
		}
		return services;
	}
	public static Tmg getTmg(int tmgId) throws Exception{

		Tmg tmg = null;
		Hashtable<String, String> hash = getObject("tmg", tmgId);
		tmg = new Tmg(tmgId);
		return tmg;
	}
	public static TmgAuthority getTmgAuthority(int tmgAuthorityId) throws Exception{

		TmgAuthority tmgAuthority = null;
		Hashtable<String, String> hash = getObject("tmg_authority", tmgAuthorityId);
		int tmgId = Integer.parseInt(hash.get("tmg_id"));
		Datavalue authority = new Datavalue(hash.get("authority"));
		tmgAuthority = new TmgAuthority(tmgId, tmgAuthorityId, authority);
		return tmgAuthority;
	}
	public static ArrayList<TmgAuthority> getTmgAuthorityBTmg(int tmgId) throws Exception{
		ArrayList<TmgAuthority> authoritys = new ArrayList<TmgAuthority>();
		ArrayList<Integer> authorityIds = getObjects("tmg_authority", "tmg", tmgId);
		for(int i=0; i<authorityIds.size(); i++){
			authoritys.add(getTmgAuthority(authorityIds.get(i)));
		}
		return authoritys;
	}
	public static TmgContributor getTmgContributor(int tmgContributorId) throws Exception{

		TmgContributor tmgContributor = null;
		Hashtable<String, String> hash = getObject("tmg_contributor", tmgContributorId);
		int tmgId = Integer.parseInt(hash.get("tmg_id"));
		Datavalue name = new Datavalue(hash.get("name"));
		Datavalue role = new Datavalue(hash.get("role"));
		tmgContributor = new TmgContributor(tmgId, tmgContributorId, name, role);
		return tmgContributor;
	}
	public static ArrayList<TmgContributor> getTmgContributorBTmg(int tmgId) throws Exception{
		ArrayList<TmgContributor> contributors = new ArrayList<TmgContributor>();
		ArrayList<Integer> contributorIds = getObjects("tmg_contributor", "tmg", tmgId);
		for(int i=0; i<contributorIds.size(); i++){
			contributors.add(getTmgContributor(contributorIds.get(i)));
		}
		return contributors;
	}
	public static TmgCreator getTmgCreator(int tmgCreatorId) throws Exception{

		TmgCreator tmgCreator = null;
		Hashtable<String, String> hash = getObject("tmg_creator", tmgCreatorId);
		int tmgId = Integer.parseInt(hash.get("tmg_id"));
		tmgCreator = new TmgCreator(tmgId, tmgCreatorId);
		return tmgCreator;
	}
	public static ArrayList<TmgCreator> getTmgCreatorBTmg(int tmgId) throws Exception{
		ArrayList<TmgCreator> creators = new ArrayList<TmgCreator>();
		ArrayList<Integer> creatorIds = getObjects("tmg_creator", "tmg", tmgId);
		for(int i=0; i<creatorIds.size(); i++){
			creators.add(getTmgCreator(creatorIds.get(i)));
		}
		return creators;
	}
	public static TmgCreatorContact getTmgCreatorContact(int tmgCreatorContactId) throws Exception{

		TmgCreatorContact tmgCreatorContact = null;
		Hashtable<String, String> hash = getObject("tmg_creator_contact", tmgCreatorContactId);
		int tmgCreatorId = Integer.parseInt(hash.get("tmg_creator_id"));
		Datavalue email = new Datavalue(hash.get("email"));
		Datavalue url = new Datavalue(hash.get("url"));
		tmgCreatorContact = new TmgCreatorContact(tmgCreatorId, tmgCreatorContactId, email, url);
		return tmgCreatorContact;
	}
	public static ArrayList<TmgCreatorContact> getTmgCreatorContactBTmgCreator(int tmgCreatorId) throws Exception{
		ArrayList<TmgCreatorContact> contacts = new ArrayList<TmgCreatorContact>();
		ArrayList<Integer> contactIds = getObjects("tmg_creator_contact", "tmg_creator", tmgCreatorId);
		for(int i=0; i<contactIds.size(); i++){
			contacts.add(getTmgCreatorContact(contactIds.get(i)));
		}
		return contacts;
	}
	public static TmgCreatorName getTmgCreatorName(int tmgCreatorNameId) throws Exception{

		TmgCreatorName tmgCreatorName = null;
		Hashtable<String, String> hash = getObject("tmg_creator_name", tmgCreatorNameId);
		int tmgCreatorId = Integer.parseInt(hash.get("tmg_creator_id"));
		Datavalue value = new Datavalue(hash.get("value"));
		Datavalue vocabulary = new Datavalue(hash.get("vocabulary"));
		tmgCreatorName = new TmgCreatorName(tmgCreatorId, tmgCreatorNameId, value, vocabulary);
		return tmgCreatorName;
	}
	public static ArrayList<TmgCreatorName> getTmgCreatorNameBTmgCreator(int tmgCreatorId) throws Exception{
		ArrayList<TmgCreatorName> names = new ArrayList<TmgCreatorName>();
		ArrayList<Integer> nameIds = getObjects("tmg_creator_name", "tmg_creator", tmgCreatorId);
		for(int i=0; i<nameIds.size(); i++){
			names.add(getTmgCreatorName(nameIds.get(i)));
		}
		return names;
	}
	public static TmgDataformat getTmgDataformat(int tmgDataformatId) throws Exception{

		TmgDataformat tmgDataformat = null;
		Hashtable<String, String> hash = getObject("tmg_dataformat", tmgDataformatId);
		int tmgId = Integer.parseInt(hash.get("tmg_id"));
		Datavalue dataformat = new Datavalue(hash.get("dataformat"));
		if (dataformat.isNull())
			dataformat = new Datavalue(hash.get("dataformat_nonstandard"));
		tmgDataformat = new TmgDataformat(tmgId, tmgDataformatId, dataformat);
		return tmgDataformat;
	}
	public static ArrayList<TmgDataformat> getTmgDataformatBTmg(int tmgId) throws Exception{
		ArrayList<TmgDataformat> dataformats = new ArrayList<TmgDataformat>();
		ArrayList<Integer> dataformatIds = getObjects("tmg_dataformat", "tmg", tmgId);
		for(int i=0; i<dataformatIds.size(); i++){
			dataformats.add(getTmgDataformat(dataformatIds.get(i)));
		}
		return dataformats;
	}
	public static TmgDatasize getTmgDatasize(int tmgDatasizeId) throws Exception{

		TmgDatasize tmgDatasize = null;
		Hashtable<String, String> hash = getObject("tmg_datasize", tmgDatasizeId);
		int tmgId = Integer.parseInt(hash.get("tmg_id"));
		Datavalue value = new Datavalue(hash.get("value"));
		Datavalue units = new Datavalue(hash.get("units"));
		if (units.isNull())
			units = new Datavalue(hash.get("units_nonstandard"));
		tmgDatasize = new TmgDatasize(tmgId, tmgDatasizeId, value, units);
		return tmgDatasize;
	}
	public static ArrayList<TmgDatasize> getTmgDatasizeBTmg(int tmgId) throws Exception{
		ArrayList<TmgDatasize> datasizes = new ArrayList<TmgDatasize>();
		ArrayList<Integer> datasizeIds = getObjects("tmg_datasize", "tmg", tmgId);
		for(int i=0; i<datasizeIds.size(); i++){
			datasizes.add(getTmgDatasize(datasizeIds.get(i)));
		}
		return datasizes;
	}
	public static TmgDatatype getTmgDatatype(int tmgDatatypeId) throws Exception{

		TmgDatatype tmgDatatype = null;
		Hashtable<String, String> hash = getObject("tmg_datatype", tmgDatatypeId);
		int tmgId = Integer.parseInt(hash.get("tmg_id"));
		Datavalue datatype = new Datavalue(hash.get("datatype"));
		if (datatype.isNull())
			datatype = new Datavalue(hash.get("datatype_nonstandard"));
		tmgDatatype = new TmgDatatype(tmgId, tmgDatatypeId, datatype);
		return tmgDatatype;
	}
	public static ArrayList<TmgDatatype> getTmgDatatypeBTmg(int tmgId) throws Exception{
		ArrayList<TmgDatatype> datatypes = new ArrayList<TmgDatatype>();
		ArrayList<Integer> datatypeIds = getObjects("tmg_datatype", "tmg", tmgId);
		for(int i=0; i<datatypeIds.size(); i++){
			datatypes.add(getTmgDatatype(datatypeIds.get(i)));
		}
		return datatypes;
	}
	public static TmgDate getTmgDate(int tmgDateId) throws Exception{

		TmgDate tmgDate = null;
		Hashtable<String, String> hash = getObject("tmg_date", tmgDateId);
		int tmgId = Integer.parseInt(hash.get("tmg_id"));
		Datavalue format = new Datavalue(hash.get("format"));
		Datavalue value = new Datavalue(hash.get("value"));
		Datavalue dateenum = new Datavalue(hash.get("dateenum"));
		if (dateenum.isNull())
			dateenum = new Datavalue(hash.get("dateenum_nonstandard"));
		tmgDate = new TmgDate(tmgId, tmgDateId, format, value, dateenum);
		return tmgDate;
	}
	public static ArrayList<TmgDate> getTmgDateBTmg(int tmgId) throws Exception{
		ArrayList<TmgDate> dates = new ArrayList<TmgDate>();
		ArrayList<Integer> dateIds = getObjects("tmg_date", "tmg", tmgId);
		for(int i=0; i<dateIds.size(); i++){
			dates.add(getTmgDate(dateIds.get(i)));
		}
		return dates;
	}
	public static TmgDocumentation getTmgDocumentation(int tmgDocumentationId) throws Exception{

		TmgDocumentation tmgDocumentation = null;
		Hashtable<String, String> hash = getObject("tmg_documentation", tmgDocumentationId);
		int tmgId = Integer.parseInt(hash.get("tmg_id"));
		Datavalue value = new Datavalue(hash.get("value"));
		Datavalue documentationenum = new Datavalue(hash.get("documentationenum"));
		if (documentationenum.isNull())
			documentationenum = new Datavalue(hash.get("documentationenum_nonstandard"));
		tmgDocumentation = new TmgDocumentation(tmgId, tmgDocumentationId, value, documentationenum);
		return tmgDocumentation;
	}
	public static ArrayList<TmgDocumentation> getTmgDocumentationBTmg(int tmgId) throws Exception{
		ArrayList<TmgDocumentation> documentations = new ArrayList<TmgDocumentation>();
		ArrayList<Integer> documentationIds = getObjects("tmg_documentation", "tmg", tmgId);
		for(int i=0; i<documentationIds.size(); i++){
			documentations.add(getTmgDocumentation(documentationIds.get(i)));
		}
		return documentations;
	}
	public static TmgDocumentationNamespace getTmgDocumentationNamespace(int tmgDocumentationNamespaceId) throws Exception{

		TmgDocumentationNamespace tmgDocumentationNamespace = null;
		Hashtable<String, String> hash = getObject("tmg_documentation_namespace", tmgDocumentationNamespaceId);
		int tmgDocumentationId = Integer.parseInt(hash.get("tmg_documentation_id"));
		Datavalue namespace = new Datavalue(hash.get("namespace"));
		tmgDocumentationNamespace = new TmgDocumentationNamespace(tmgDocumentationId, tmgDocumentationNamespaceId, namespace);
		return tmgDocumentationNamespace;
	}
	public static ArrayList<TmgDocumentationNamespace> getTmgDocumentationNamespaceBTmgDocumentation(int tmgDocumentationId) throws Exception{
		ArrayList<TmgDocumentationNamespace> namespaces = new ArrayList<TmgDocumentationNamespace>();
		ArrayList<Integer> namespaceIds = getObjects("tmg_documentation_namespace", "tmg_documentation", tmgDocumentationId);
		for(int i=0; i<namespaceIds.size(); i++){
			namespaces.add(getTmgDocumentationNamespace(namespaceIds.get(i)));
		}
		return namespaces;
	}
	public static TmgDocumentationXlink getTmgDocumentationXlink(int tmgDocumentationXlinkId) throws Exception{

		TmgDocumentationXlink tmgDocumentationXlink = null;
		Hashtable<String, String> hash = getObject("tmg_documentation_xlink", tmgDocumentationXlinkId);
		int tmgDocumentationId = Integer.parseInt(hash.get("tmg_documentation_id"));
		Datavalue value = new Datavalue(hash.get("value"));
		Datavalue xlink = new Datavalue(hash.get("xlink"));
		if (xlink.isNull())
			xlink = new Datavalue(hash.get("xlink_nonstandard"));
		tmgDocumentationXlink = new TmgDocumentationXlink(tmgDocumentationId, tmgDocumentationXlinkId, value, xlink);
		return tmgDocumentationXlink;
	}
	public static ArrayList<TmgDocumentationXlink> getTmgDocumentationXlinkBTmgDocumentation(int tmgDocumentationId) throws Exception{
		ArrayList<TmgDocumentationXlink> xlinks = new ArrayList<TmgDocumentationXlink>();
		ArrayList<Integer> xlinkIds = getObjects("tmg_documentation_xlink", "tmg_documentation", tmgDocumentationId);
		for(int i=0; i<xlinkIds.size(); i++){
			xlinks.add(getTmgDocumentationXlink(xlinkIds.get(i)));
		}
		return xlinks;
	}
	public static TmgGeospatialcoverage getTmgGeospatialcoverage(int tmgGeospatialcoverageId) throws Exception{

		TmgGeospatialcoverage tmgGeospatialcoverage = null;
		Hashtable<String, String> hash = getObject("tmg_geospatialcoverage", tmgGeospatialcoverageId);
		int tmgId = Integer.parseInt(hash.get("tmg_id"));
		Datavalue upordown = new Datavalue(hash.get("upordown"));
		if (upordown.isNull())
			upordown = new Datavalue(hash.get("upordown_nonstandard"));
		tmgGeospatialcoverage = new TmgGeospatialcoverage(tmgId, tmgGeospatialcoverageId, upordown);
		return tmgGeospatialcoverage;
	}
	public static ArrayList<TmgGeospatialcoverage> getTmgGeospatialcoverageBTmg(int tmgId) throws Exception{
		ArrayList<TmgGeospatialcoverage> geospatialcoverages = new ArrayList<TmgGeospatialcoverage>();
		ArrayList<Integer> geospatialcoverageIds = getObjects("tmg_geospatialcoverage", "tmg", tmgId);
		for(int i=0; i<geospatialcoverageIds.size(); i++){
			geospatialcoverages.add(getTmgGeospatialcoverage(geospatialcoverageIds.get(i)));
		}
		return geospatialcoverages;
	}
	public static TmgGeospatialcoverageEastwest getTmgGeospatialcoverageEastwest(int tmgGeospatialcoverageEastwestId) throws Exception{

		TmgGeospatialcoverageEastwest tmgGeospatialcoverageEastwest = null;
		Hashtable<String, String> hash = getObject("tmg_geospatialcoverage_eastwest", tmgGeospatialcoverageEastwestId);
		int tmgGeospatialcoverageId = Integer.parseInt(hash.get("tmg_geospatialcoverage_id"));
		Datavalue resolution = new Datavalue(hash.get("resolution"));
		Datavalue size = new Datavalue(hash.get("size"));
		Datavalue start = new Datavalue(hash.get("start"));
		Datavalue units = new Datavalue(hash.get("units"));
		tmgGeospatialcoverageEastwest = new TmgGeospatialcoverageEastwest(tmgGeospatialcoverageId, tmgGeospatialcoverageEastwestId, resolution, size, start, units);
		return tmgGeospatialcoverageEastwest;
	}
	public static ArrayList<TmgGeospatialcoverageEastwest> getTmgGeospatialcoverageEastwestBTmgGeospatialcoverage(int tmgGeospatialcoverageId) throws Exception{
		ArrayList<TmgGeospatialcoverageEastwest> eastwests = new ArrayList<TmgGeospatialcoverageEastwest>();
		ArrayList<Integer> eastwestIds = getObjects("tmg_geospatialcoverage_eastwest", "tmg_geospatialcoverage", tmgGeospatialcoverageId);
		for(int i=0; i<eastwestIds.size(); i++){
			eastwests.add(getTmgGeospatialcoverageEastwest(eastwestIds.get(i)));
		}
		return eastwests;
	}
	public static TmgGeospatialcoverageName getTmgGeospatialcoverageName(int tmgGeospatialcoverageNameId) throws Exception{

		TmgGeospatialcoverageName tmgGeospatialcoverageName = null;
		Hashtable<String, String> hash = getObject("tmg_geospatialcoverage_name", tmgGeospatialcoverageNameId);
		int tmgGeospatialcoverageId = Integer.parseInt(hash.get("tmg_geospatialcoverage_id"));
		Datavalue value = new Datavalue(hash.get("value"));
		Datavalue vocabulary = new Datavalue(hash.get("vocabulary"));
		tmgGeospatialcoverageName = new TmgGeospatialcoverageName(tmgGeospatialcoverageId, tmgGeospatialcoverageNameId, value, vocabulary);
		return tmgGeospatialcoverageName;
	}
	public static ArrayList<TmgGeospatialcoverageName> getTmgGeospatialcoverageNameBTmgGeospatialcoverage(int tmgGeospatialcoverageId) throws Exception{
		ArrayList<TmgGeospatialcoverageName> names = new ArrayList<TmgGeospatialcoverageName>();
		ArrayList<Integer> nameIds = getObjects("tmg_geospatialcoverage_name", "tmg_geospatialcoverage", tmgGeospatialcoverageId);
		for(int i=0; i<nameIds.size(); i++){
			names.add(getTmgGeospatialcoverageName(nameIds.get(i)));
		}
		return names;
	}
	public static TmgGeospatialcoverageNorthsouth getTmgGeospatialcoverageNorthsouth(int tmgGeospatialcoverageNorthsouthId) throws Exception{

		TmgGeospatialcoverageNorthsouth tmgGeospatialcoverageNorthsouth = null;
		Hashtable<String, String> hash = getObject("tmg_geospatialcoverage_northsouth", tmgGeospatialcoverageNorthsouthId);
		int tmgGeospatialcoverageId = Integer.parseInt(hash.get("tmg_geospatialcoverage_id"));
		Datavalue resolution = new Datavalue(hash.get("resolution"));
		Datavalue size = new Datavalue(hash.get("size"));
		Datavalue start = new Datavalue(hash.get("start"));
		Datavalue units = new Datavalue(hash.get("units"));
		tmgGeospatialcoverageNorthsouth = new TmgGeospatialcoverageNorthsouth(tmgGeospatialcoverageId, tmgGeospatialcoverageNorthsouthId, resolution, size, start, units);
		return tmgGeospatialcoverageNorthsouth;
	}
	public static ArrayList<TmgGeospatialcoverageNorthsouth> getTmgGeospatialcoverageNorthsouthBTmgGeospatialcoverage(int tmgGeospatialcoverageId) throws Exception{
		ArrayList<TmgGeospatialcoverageNorthsouth> northsouths = new ArrayList<TmgGeospatialcoverageNorthsouth>();
		ArrayList<Integer> northsouthIds = getObjects("tmg_geospatialcoverage_northsouth", "tmg_geospatialcoverage", tmgGeospatialcoverageId);
		for(int i=0; i<northsouthIds.size(); i++){
			northsouths.add(getTmgGeospatialcoverageNorthsouth(northsouthIds.get(i)));
		}
		return northsouths;
	}
	public static TmgGeospatialcoverageUpdown getTmgGeospatialcoverageUpdown(int tmgGeospatialcoverageUpdownId) throws Exception{

		TmgGeospatialcoverageUpdown tmgGeospatialcoverageUpdown = null;
		Hashtable<String, String> hash = getObject("tmg_geospatialcoverage_updown", tmgGeospatialcoverageUpdownId);
		int tmgGeospatialcoverageId = Integer.parseInt(hash.get("tmg_geospatialcoverage_id"));
		Datavalue resolution = new Datavalue(hash.get("resolution"));
		Datavalue size = new Datavalue(hash.get("size"));
		Datavalue start = new Datavalue(hash.get("start"));
		Datavalue units = new Datavalue(hash.get("units"));
		tmgGeospatialcoverageUpdown = new TmgGeospatialcoverageUpdown(tmgGeospatialcoverageId, tmgGeospatialcoverageUpdownId, resolution, size, start, units);
		return tmgGeospatialcoverageUpdown;
	}
	public static ArrayList<TmgGeospatialcoverageUpdown> getTmgGeospatialcoverageUpdownBTmgGeospatialcoverage(int tmgGeospatialcoverageId) throws Exception{
		ArrayList<TmgGeospatialcoverageUpdown> updowns = new ArrayList<TmgGeospatialcoverageUpdown>();
		ArrayList<Integer> updownIds = getObjects("tmg_geospatialcoverage_updown", "tmg_geospatialcoverage", tmgGeospatialcoverageId);
		for(int i=0; i<updownIds.size(); i++){
			updowns.add(getTmgGeospatialcoverageUpdown(updownIds.get(i)));
		}
		return updowns;
	}
	public static TmgKeyword getTmgKeyword(int tmgKeywordId) throws Exception{

		TmgKeyword tmgKeyword = null;
		Hashtable<String, String> hash = getObject("tmg_keyword", tmgKeywordId);
		int tmgId = Integer.parseInt(hash.get("tmg_id"));
		Datavalue value = new Datavalue(hash.get("value"));
		Datavalue vocabulary = new Datavalue(hash.get("vocabulary"));
		tmgKeyword = new TmgKeyword(tmgId, tmgKeywordId, value, vocabulary);
		return tmgKeyword;
	}
	public static ArrayList<TmgKeyword> getTmgKeywordBTmg(int tmgId) throws Exception{
		ArrayList<TmgKeyword> keywords = new ArrayList<TmgKeyword>();
		ArrayList<Integer> keywordIds = getObjects("tmg_keyword", "tmg", tmgId);
		for(int i=0; i<keywordIds.size(); i++){
			keywords.add(getTmgKeyword(keywordIds.get(i)));
		}
		return keywords;
	}
	public static ArrayList<Metadata> getMetadataBTmg(int tmgId) throws Exception{
		ArrayList<Metadata> metadatas = new ArrayList<Metadata>();
		ArrayList<Integer> metadataIds = getObjects("tmg_metadata", "tmg", "metadata", tmgId);
		for(int i=0; i<metadataIds.size(); i++){
			metadatas.add(getMetadata(metadataIds.get(i)));
		}
		return metadatas;
	}
	public static TmgProject getTmgProject(int tmgProjectId) throws Exception{

		TmgProject tmgProject = null;
		Hashtable<String, String> hash = getObject("tmg_project", tmgProjectId);
		int tmgId = Integer.parseInt(hash.get("tmg_id"));
		Datavalue value = new Datavalue(hash.get("value"));
		Datavalue vocabulary = new Datavalue(hash.get("vocabulary"));
		tmgProject = new TmgProject(tmgId, tmgProjectId, value, vocabulary);
		return tmgProject;
	}
	public static ArrayList<TmgProject> getTmgProjectBTmg(int tmgId) throws Exception{
		ArrayList<TmgProject> projects = new ArrayList<TmgProject>();
		ArrayList<Integer> projectIds = getObjects("tmg_project", "tmg", tmgId);
		for(int i=0; i<projectIds.size(); i++){
			projects.add(getTmgProject(projectIds.get(i)));
		}
		return projects;
	}
	public static TmgProperty getTmgProperty(int tmgPropertyId) throws Exception{

		TmgProperty tmgProperty = null;
		Hashtable<String, String> hash = getObject("tmg_property", tmgPropertyId);
		int tmgId = Integer.parseInt(hash.get("tmg_id"));
		Datavalue name = new Datavalue(hash.get("name"));
		Datavalue value = new Datavalue(hash.get("value"));
		tmgProperty = new TmgProperty(tmgId, tmgPropertyId, name, value);
		return tmgProperty;
	}
	public static ArrayList<TmgProperty> getTmgPropertyBTmg(int tmgId) throws Exception{
		ArrayList<TmgProperty> propertys = new ArrayList<TmgProperty>();
		ArrayList<Integer> propertyIds = getObjects("tmg_property", "tmg", tmgId);
		for(int i=0; i<propertyIds.size(); i++){
			propertys.add(getTmgProperty(propertyIds.get(i)));
		}
		return propertys;
	}
	public static TmgPublisher getTmgPublisher(int tmgPublisherId) throws Exception{

		TmgPublisher tmgPublisher = null;
		Hashtable<String, String> hash = getObject("tmg_publisher", tmgPublisherId);
		int tmgId = Integer.parseInt(hash.get("tmg_id"));
		tmgPublisher = new TmgPublisher(tmgId, tmgPublisherId);
		return tmgPublisher;
	}
	public static ArrayList<TmgPublisher> getTmgPublisherBTmg(int tmgId) throws Exception{
		ArrayList<TmgPublisher> publishers = new ArrayList<TmgPublisher>();
		ArrayList<Integer> publisherIds = getObjects("tmg_publisher", "tmg", tmgId);
		for(int i=0; i<publisherIds.size(); i++){
			publishers.add(getTmgPublisher(publisherIds.get(i)));
		}
		return publishers;
	}
	public static TmgPublisherContact getTmgPublisherContact(int tmgPublisherContactId) throws Exception{

		TmgPublisherContact tmgPublisherContact = null;
		Hashtable<String, String> hash = getObject("tmg_publisher_contact", tmgPublisherContactId);
		int tmgPublisherId = Integer.parseInt(hash.get("tmg_publisher_id"));
		Datavalue email = new Datavalue(hash.get("email"));
		Datavalue url = new Datavalue(hash.get("url"));
		tmgPublisherContact = new TmgPublisherContact(tmgPublisherId, tmgPublisherContactId, email, url);
		return tmgPublisherContact;
	}
	public static ArrayList<TmgPublisherContact> getTmgPublisherContactBTmgPublisher(int tmgPublisherId) throws Exception{
		ArrayList<TmgPublisherContact> contacts = new ArrayList<TmgPublisherContact>();
		ArrayList<Integer> contactIds = getObjects("tmg_publisher_contact", "tmg_publisher", tmgPublisherId);
		for(int i=0; i<contactIds.size(); i++){
			contacts.add(getTmgPublisherContact(contactIds.get(i)));
		}
		return contacts;
	}
	public static TmgPublisherName getTmgPublisherName(int tmgPublisherNameId) throws Exception{

		TmgPublisherName tmgPublisherName = null;
		Hashtable<String, String> hash = getObject("tmg_publisher_name", tmgPublisherNameId);
		int tmgPublisherId = Integer.parseInt(hash.get("tmg_publisher_id"));
		Datavalue value = new Datavalue(hash.get("value"));
		Datavalue vocabulary = new Datavalue(hash.get("vocabulary"));
		tmgPublisherName = new TmgPublisherName(tmgPublisherId, tmgPublisherNameId, value, vocabulary);
		return tmgPublisherName;
	}
	public static ArrayList<TmgPublisherName> getTmgPublisherNameBTmgPublisher(int tmgPublisherId) throws Exception{
		ArrayList<TmgPublisherName> names = new ArrayList<TmgPublisherName>();
		ArrayList<Integer> nameIds = getObjects("tmg_publisher_name", "tmg_publisher", tmgPublisherId);
		for(int i=0; i<nameIds.size(); i++){
			names.add(getTmgPublisherName(nameIds.get(i)));
		}
		return names;
	}
	public static TmgServicename getTmgServicename(int tmgServicenameId) throws Exception{

		TmgServicename tmgServicename = null;
		Hashtable<String, String> hash = getObject("tmg_servicename", tmgServicenameId);
		int tmgId = Integer.parseInt(hash.get("tmg_id"));
		Datavalue servicename = new Datavalue(hash.get("servicename"));
		tmgServicename = new TmgServicename(tmgId, tmgServicenameId, servicename);
		return tmgServicename;
	}
	public static ArrayList<TmgServicename> getTmgServicenameBTmg(int tmgId) throws Exception{
		ArrayList<TmgServicename> servicenames = new ArrayList<TmgServicename>();
		ArrayList<Integer> servicenameIds = getObjects("tmg_servicename", "tmg", tmgId);
		for(int i=0; i<servicenameIds.size(); i++){
			servicenames.add(getTmgServicename(servicenameIds.get(i)));
		}
		return servicenames;
	}
	public static TmgTimecoverage getTmgTimecoverage(int tmgTimecoverageId) throws Exception{

		TmgTimecoverage tmgTimecoverage = null;
		Hashtable<String, String> hash = getObject("tmg_timecoverage", tmgTimecoverageId);
		int tmgId = Integer.parseInt(hash.get("tmg_id"));
		Datavalue resolution = new Datavalue(hash.get("resolution"));
		tmgTimecoverage = new TmgTimecoverage(tmgId, tmgTimecoverageId, resolution);
		return tmgTimecoverage;
	}
	public static ArrayList<TmgTimecoverage> getTmgTimecoverageBTmg(int tmgId) throws Exception{
		ArrayList<TmgTimecoverage> timecoverages = new ArrayList<TmgTimecoverage>();
		ArrayList<Integer> timecoverageIds = getObjects("tmg_timecoverage", "tmg", tmgId);
		for(int i=0; i<timecoverageIds.size(); i++){
			timecoverages.add(getTmgTimecoverage(timecoverageIds.get(i)));
		}
		return timecoverages;
	}
	public static TmgTimecoverageDuration getTmgTimecoverageDuration(int tmgTimecoverageDurationId) throws Exception{

		TmgTimecoverageDuration tmgTimecoverageDuration = null;
		Hashtable<String, String> hash = getObject("tmg_timecoverage_duration", tmgTimecoverageDurationId);
		int tmgTimecoverageId = Integer.parseInt(hash.get("tmg_timecoverage_id"));
		Datavalue duration = new Datavalue(hash.get("duration"));
		tmgTimecoverageDuration = new TmgTimecoverageDuration(tmgTimecoverageId, tmgTimecoverageDurationId, duration);
		return tmgTimecoverageDuration;
	}
	public static ArrayList<TmgTimecoverageDuration> getTmgTimecoverageDurationBTmgTimecoverage(int tmgTimecoverageId) throws Exception{
		ArrayList<TmgTimecoverageDuration> durations = new ArrayList<TmgTimecoverageDuration>();
		ArrayList<Integer> durationIds = getObjects("tmg_timecoverage_duration", "tmg_timecoverage", tmgTimecoverageId);
		for(int i=0; i<durationIds.size(); i++){
			durations.add(getTmgTimecoverageDuration(durationIds.get(i)));
		}
		return durations;
	}
	public static TmgTimecoverageEnd getTmgTimecoverageEnd(int tmgTimecoverageEndId) throws Exception{

		TmgTimecoverageEnd tmgTimecoverageEnd = null;
		Hashtable<String, String> hash = getObject("tmg_timecoverage_end", tmgTimecoverageEndId);
		int tmgTimecoverageId = Integer.parseInt(hash.get("tmg_timecoverage_id"));
		Datavalue format = new Datavalue(hash.get("format"));
		Datavalue value = new Datavalue(hash.get("value"));
		Datavalue dateenum = new Datavalue(hash.get("dateenum"));
		if (dateenum.isNull())
			dateenum = new Datavalue(hash.get("dateenum_nonstandard"));
		tmgTimecoverageEnd = new TmgTimecoverageEnd(tmgTimecoverageId, tmgTimecoverageEndId, format, value, dateenum);
		return tmgTimecoverageEnd;
	}
	public static ArrayList<TmgTimecoverageEnd> getTmgTimecoverageEndBTmgTimecoverage(int tmgTimecoverageId) throws Exception{
		ArrayList<TmgTimecoverageEnd> ends = new ArrayList<TmgTimecoverageEnd>();
		ArrayList<Integer> endIds = getObjects("tmg_timecoverage_end", "tmg_timecoverage", tmgTimecoverageId);
		for(int i=0; i<endIds.size(); i++){
			ends.add(getTmgTimecoverageEnd(endIds.get(i)));
		}
		return ends;
	}
	public static TmgTimecoverageResolution getTmgTimecoverageResolution(int tmgTimecoverageResolutionId) throws Exception{

		TmgTimecoverageResolution tmgTimecoverageResolution = null;
		Hashtable<String, String> hash = getObject("tmg_timecoverage_resolution", tmgTimecoverageResolutionId);
		int tmgTimecoverageId = Integer.parseInt(hash.get("tmg_timecoverage_id"));
		Datavalue duration = new Datavalue(hash.get("duration"));
		tmgTimecoverageResolution = new TmgTimecoverageResolution(tmgTimecoverageId, tmgTimecoverageResolutionId, duration);
		return tmgTimecoverageResolution;
	}
	public static ArrayList<TmgTimecoverageResolution> getTmgTimecoverageResolutionBTmgTimecoverage(int tmgTimecoverageId) throws Exception{
		ArrayList<TmgTimecoverageResolution> resolutions = new ArrayList<TmgTimecoverageResolution>();
		ArrayList<Integer> resolutionIds = getObjects("tmg_timecoverage_resolution", "tmg_timecoverage", tmgTimecoverageId);
		for(int i=0; i<resolutionIds.size(); i++){
			resolutions.add(getTmgTimecoverageResolution(resolutionIds.get(i)));
		}
		return resolutions;
	}
	public static TmgTimecoverageStart getTmgTimecoverageStart(int tmgTimecoverageStartId) throws Exception{

		TmgTimecoverageStart tmgTimecoverageStart = null;
		Hashtable<String, String> hash = getObject("tmg_timecoverage_start", tmgTimecoverageStartId);
		int tmgTimecoverageId = Integer.parseInt(hash.get("tmg_timecoverage_id"));
		Datavalue format = new Datavalue(hash.get("format"));
		Datavalue value = new Datavalue(hash.get("value"));
		Datavalue dateenum = new Datavalue(hash.get("dateenum"));
		if (dateenum.isNull())
			dateenum = new Datavalue(hash.get("dateenum_nonstandard"));
		tmgTimecoverageStart = new TmgTimecoverageStart(tmgTimecoverageId, tmgTimecoverageStartId, format, value, dateenum);
		return tmgTimecoverageStart;
	}
	public static ArrayList<TmgTimecoverageStart> getTmgTimecoverageStartBTmgTimecoverage(int tmgTimecoverageId) throws Exception{
		ArrayList<TmgTimecoverageStart> starts = new ArrayList<TmgTimecoverageStart>();
		ArrayList<Integer> startIds = getObjects("tmg_timecoverage_start", "tmg_timecoverage", tmgTimecoverageId);
		for(int i=0; i<startIds.size(); i++){
			starts.add(getTmgTimecoverageStart(startIds.get(i)));
		}
		return starts;
	}
	public static TmgVariables getTmgVariables(int tmgVariablesId) throws Exception{

		TmgVariables tmgVariables = null;
		Hashtable<String, String> hash = getObject("tmg_variables", tmgVariablesId);
		int tmgId = Integer.parseInt(hash.get("tmg_id"));
		Datavalue vocabulary = new Datavalue(hash.get("vocabulary"));
		if (vocabulary.isNull())
			vocabulary = new Datavalue(hash.get("vocabulary_nonstandard"));
		tmgVariables = new TmgVariables(tmgId, tmgVariablesId, vocabulary);
		return tmgVariables;
	}
	public static ArrayList<TmgVariables> getTmgVariablesBTmg(int tmgId) throws Exception{
		ArrayList<TmgVariables> variabless = new ArrayList<TmgVariables>();
		ArrayList<Integer> variablesIds = getObjects("tmg_variables", "tmg", tmgId);
		for(int i=0; i<variablesIds.size(); i++){
			variabless.add(getTmgVariables(variablesIds.get(i)));
		}
		return variabless;
	}
	public static TmgVariablesVariable getTmgVariablesVariable(int tmgVariablesVariableId) throws Exception{

		TmgVariablesVariable tmgVariablesVariable = null;
		Hashtable<String, String> hash = getObject("tmg_variables_variable", tmgVariablesVariableId);
		int tmgVariablesId = Integer.parseInt(hash.get("tmg_variables_id"));
		Datavalue name = new Datavalue(hash.get("name"));
		Datavalue units = new Datavalue(hash.get("units"));
		Datavalue vocabularyName = new Datavalue(hash.get("vocabulary_name"));
		tmgVariablesVariable = new TmgVariablesVariable(tmgVariablesId, tmgVariablesVariableId, name, units, vocabularyName);
		return tmgVariablesVariable;
	}
	public static ArrayList<TmgVariablesVariable> getTmgVariablesVariableBTmgVariables(int tmgVariablesId) throws Exception{
		ArrayList<TmgVariablesVariable> variables = new ArrayList<TmgVariablesVariable>();
		ArrayList<Integer> variableIds = getObjects("tmg_variables_variable", "tmg_variables", tmgVariablesId);
		for(int i=0; i<variableIds.size(); i++){
			variables.add(getTmgVariablesVariable(variableIds.get(i)));
		}
		return variables;
	}
	public static TmgVariablesVariablemap getTmgVariablesVariablemap(int tmgVariablesVariablemapId) throws Exception{

		TmgVariablesVariablemap tmgVariablesVariablemap = null;
		Hashtable<String, String> hash = getObject("tmg_variables_variablemap", tmgVariablesVariablemapId);
		int tmgVariablesId = Integer.parseInt(hash.get("tmg_variables_id"));
		Datavalue value = new Datavalue(hash.get("value"));
		Datavalue xlink = new Datavalue(hash.get("xlink"));
		if (xlink.isNull())
			xlink = new Datavalue(hash.get("xlink_nonstandard"));
		tmgVariablesVariablemap = new TmgVariablesVariablemap(tmgVariablesId, tmgVariablesVariablemapId, value, xlink);
		return tmgVariablesVariablemap;
	}
	public static ArrayList<TmgVariablesVariablemap> getTmgVariablesVariablemapBTmgVariables(int tmgVariablesId) throws Exception{
		ArrayList<TmgVariablesVariablemap> variablemaps = new ArrayList<TmgVariablesVariablemap>();
		ArrayList<Integer> variablemapIds = getObjects("tmg_variables_variablemap", "tmg_variables", tmgVariablesId);
		for(int i=0; i<variablemapIds.size(); i++){
			variablemaps.add(getTmgVariablesVariablemap(variablemapIds.get(i)));
		}
		return variablemaps;
	}


	/*begin insert methods*/

	public static int insertCatalog(Datavalue cleanCatalogId, Datavalue base, Datavalue expires, Datavalue name, Datavalue version, Datavalue xmlns, Datavalue status) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_catalog", new Datavalue[]{cleanCatalogId, base, expires, name, version, xmlns, status});
		return runStatement(ps);
	}
	public static int insertCatalog(Catalog catalog) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_catalog", new Datavalue[]{catalog.getCleanCatalogId(), catalog.getBase(), catalog.getExpires(), catalog.getName(), catalog.getVersion(), catalog.getXmlns(), catalog.getStatus()});
		return runStatement(ps);
	}
	public static int insertCatalogDataset(int catalogId, int datasetId) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_catalog_dataset", new int[]{catalogId, datasetId});
		return runStatement(ps);
	}
	public static int insertCatalogDataset(CatalogDataset catalogDataset) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_catalog_dataset", new int[]{catalogDataset.getParentId(), catalogDataset.getChildId()});
		return runStatement(ps);
	}
	public static int insertCatalogProperty(int catalogId, Datavalue name, Datavalue value) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_catalog_property", new int[]{catalogId}, new Datavalue[]{name, value});
		return runStatement(ps);
	}
	public static int insertCatalogProperty(CatalogProperty catalogProperty) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_catalog_property", new int[]{catalogProperty.getCatalogId()}, new Datavalue[]{catalogProperty.getName(), catalogProperty.getValue()});
		return runStatement(ps);
	}
	public static int insertCatalogService(int catalogId, int serviceId) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_catalog_service", new int[]{catalogId, serviceId});
		return runStatement(ps);
	}
	public static int insertCatalogService(CatalogService catalogService) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_catalog_service", new int[]{catalogService.getParentId(), catalogService.getChildId()});
		return runStatement(ps);
	}
	public static int insertCatalogXlink(int catalogId, Datavalue value, Datavalue xlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_catalog_xlink", new int[]{catalogId}, new Datavalue[]{value, xlink});
		return runStatement(ps);
	}
	public static int insertCatalogXlink(CatalogXlink catalogXlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_catalog_xlink", new int[]{catalogXlink.getCatalogId()}, new Datavalue[]{catalogXlink.getValue(), catalogXlink.getXlink()});
		return runStatement(ps);
	}
	public static int insertCatalogref() throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_catalogref");
		return runStatement(ps);
	}
	public static int insertCatalogref(Catalogref catalogref) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_catalogref");
		return runStatement(ps);
	}
	public static int insertCatalogrefDocumentation(int catalogrefId, Datavalue value, Datavalue documentationenum) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_catalogref_documentation", new int[]{catalogrefId}, new Datavalue[]{value, documentationenum});
		return runStatement(ps);
	}
	public static int insertCatalogrefDocumentation(CatalogrefDocumentation catalogrefDocumentation) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_catalogref_documentation", new int[]{catalogrefDocumentation.getCatalogrefId()}, new Datavalue[]{catalogrefDocumentation.getValue(), catalogrefDocumentation.getDocumentationenum()});
		return runStatement(ps);
	}
	public static int insertCatalogrefDocumentationNamespace(int catalogrefDocumentationId, Datavalue namespace) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_catalogref_documentation_namespace", new int[]{catalogrefDocumentationId}, new Datavalue[]{namespace});
		return runStatement(ps);
	}
	public static int insertCatalogrefDocumentationNamespace(CatalogrefDocumentationNamespace catalogrefDocumentationNamespace) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_catalogref_documentation_namespace", new int[]{catalogrefDocumentationNamespace.getCatalogrefDocumentationId()}, new Datavalue[]{catalogrefDocumentationNamespace.getNamespace()});
		return runStatement(ps);
	}
	public static int insertCatalogrefDocumentationXlink(int catalogrefDocumentationId, Datavalue value, Datavalue xlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_catalogref_documentation_xlink", new int[]{catalogrefDocumentationId}, new Datavalue[]{value, xlink});
		return runStatement(ps);
	}
	public static int insertCatalogrefDocumentationXlink(CatalogrefDocumentationXlink catalogrefDocumentationXlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_catalogref_documentation_xlink", new int[]{catalogrefDocumentationXlink.getCatalogrefDocumentationId()}, new Datavalue[]{catalogrefDocumentationXlink.getValue(), catalogrefDocumentationXlink.getXlink()});
		return runStatement(ps);
	}
	public static int insertCatalogrefXlink(int catalogrefId, Datavalue value, Datavalue xlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_catalogref_xlink", new int[]{catalogrefId}, new Datavalue[]{value, xlink});
		return runStatement(ps);
	}
	public static int insertCatalogrefXlink(CatalogrefXlink catalogrefXlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_catalogref_xlink", new int[]{catalogrefXlink.getCatalogrefId()}, new Datavalue[]{catalogrefXlink.getValue(), catalogrefXlink.getXlink()});
		return runStatement(ps);
	}
	public static int insertDataset(Datavalue alias, Datavalue authority, Datavalue dId, Datavalue harvest, Datavalue name, Datavalue resourcecontrol, Datavalue serviceName, Datavalue urlPath, Datavalue collectiontype, Datavalue datasizeUnit, Datavalue dataType, Datavalue status) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_dataset", new Datavalue[]{alias, authority, dId, harvest, name, resourcecontrol, serviceName, urlPath, collectiontype, datasizeUnit, dataType, status});
		return runStatement(ps);
	}
	public static int insertDataset(Dataset dataset) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_dataset", new Datavalue[]{dataset.getAlias(), dataset.getAuthority(), dataset.getDId(), dataset.getHarvest(), dataset.getName(), dataset.getResourcecontrol(), dataset.getServiceName(), dataset.getUrlPath(), dataset.getCollectiontype(), dataset.getDatasizeUnit(), dataset.getDataType(), dataset.getStatus()});
		return runStatement(ps);
	}
	public static int insertDatasetAccess(int datasetId, Datavalue servicename, Datavalue urlpath, Datavalue dataformat) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_dataset_access", new int[]{datasetId}, new Datavalue[]{servicename, urlpath, dataformat});
		return runStatement(ps);
	}
	public static int insertDatasetAccess(DatasetAccess datasetAccess) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_dataset_access", new int[]{datasetAccess.getDatasetId()}, new Datavalue[]{datasetAccess.getServicename(), datasetAccess.getUrlpath(), datasetAccess.getDataformat()});
		return runStatement(ps);
	}
	public static int insertDatasetAccessDatasize(int datasetAccessId, Datavalue value, Datavalue units) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_dataset_access_datasize", new int[]{datasetAccessId}, new Datavalue[]{value, units});
		return runStatement(ps);
	}
	public static int insertDatasetAccessDatasize(DatasetAccessDatasize datasetAccessDatasize) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_dataset_access_datasize", new int[]{datasetAccessDatasize.getDatasetAccessId()}, new Datavalue[]{datasetAccessDatasize.getValue(), datasetAccessDatasize.getUnits()});
		return runStatement(ps);
	}
	public static int insertDatasetCatalogref(int datasetId, int catalogrefId) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_dataset_catalogref", new int[]{datasetId, catalogrefId});
		return runStatement(ps);
	}
	public static int insertDatasetDataset(int parentId, int childId) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_dataset_dataset", new int[]{parentId, childId});
		return runStatement(ps);
	}
	public static int insertDatasetDataset(DatasetDataset datasetDataset) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_dataset_dataset", new int[]{datasetDataset.getParentId(), datasetDataset.getChildId()});
		return runStatement(ps);
	}
	public static int insertDatasetNcml(int datasetId) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_dataset_ncml", new int[]{datasetId});
		return runStatement(ps);
	}
	public static int insertDatasetNcml(DatasetNcml datasetNcml) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_dataset_ncml", new int[]{datasetNcml.getDatasetId()});
		return runStatement(ps);
	}
	public static int insertDatasetProperty(int datasetId, Datavalue name, Datavalue value) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_dataset_property", new int[]{datasetId}, new Datavalue[]{name, value});
		return runStatement(ps);
	}
	public static int insertDatasetProperty(DatasetProperty datasetProperty) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_dataset_property", new int[]{datasetProperty.getDatasetId()}, new Datavalue[]{datasetProperty.getName(), datasetProperty.getValue()});
		return runStatement(ps);
	}
	public static int insertDatasetService(int datasetId, int serviceId) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_dataset_service", new int[]{datasetId, serviceId});
		return runStatement(ps);
	}
	public static int insertDatasetService(DatasetService datasetService) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_dataset_service", new int[]{datasetService.getParentId(), datasetService.getChildId()});
		return runStatement(ps);
	}
	public static int insertDatasetTmg(int datasetId, int tmgId) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_dataset_tmg", new int[]{datasetId, tmgId});
		return runStatement(ps);
	}
	public static int insertDatasetTmg(DatasetTmg datasetTmg) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_dataset_tmg", new int[]{datasetTmg.getParentId(), datasetTmg.getChildId()});
		return runStatement(ps);
	}
	public static int insertMetadata(Datavalue inherited, Datavalue metadatatype) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_metadata", new Datavalue[]{inherited, metadatatype});
		return runStatement(ps);
	}
	public static int insertMetadata(Metadata metadata) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_metadata", new Datavalue[]{metadata.getInherited(), metadata.getMetadatatype()});
		return runStatement(ps);
	}
	public static int insertMetadataNamespace(int metadataId, Datavalue namespace) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_metadata_namespace", new int[]{metadataId}, new Datavalue[]{namespace});
		return runStatement(ps);
	}
	public static int insertMetadataNamespace(MetadataNamespace metadataNamespace) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_metadata_namespace", new int[]{metadataNamespace.getMetadataId()}, new Datavalue[]{metadataNamespace.getNamespace()});
		return runStatement(ps);
	}
	public static int insertMetadataTmg(int metadataId, int tmgId) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_metadata_tmg", new int[]{metadataId, tmgId});
		return runStatement(ps);
	}
	public static int insertMetadataTmg(MetadataTmg metadataTmg) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_metadata_tmg", new int[]{metadataTmg.getParentId(), metadataTmg.getChildId()});
		return runStatement(ps);
	}
	public static int insertMetadataXlink(int metadataId, Datavalue value, Datavalue xlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_metadata_xlink", new int[]{metadataId}, new Datavalue[]{value, xlink});
		return runStatement(ps);
	}
	public static int insertMetadataXlink(MetadataXlink metadataXlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_metadata_xlink", new int[]{metadataXlink.getMetadataId()}, new Datavalue[]{metadataXlink.getValue(), metadataXlink.getXlink()});
		return runStatement(ps);
	}
	public static int insertService(Datavalue base, Datavalue desc, Datavalue name, Datavalue suffix, Datavalue serviceType, Datavalue status) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_service", new Datavalue[]{base, desc, name, suffix, serviceType, status});
		return runStatement(ps);
	}
	public static int insertService(Service service) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_service", new Datavalue[]{service.getBase(), service.getDesc(), service.getName(), service.getSuffix(), service.getServiceType(), service.getStatus()});
		return runStatement(ps);
	}
	public static int insertServiceDatasetroot(int serviceId, Datavalue location, Datavalue path) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_service_datasetroot", new int[]{serviceId}, new Datavalue[]{location, path});
		return runStatement(ps);
	}
	public static int insertServiceDatasetroot(ServiceDatasetroot serviceDatasetroot) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_service_datasetroot", new int[]{serviceDatasetroot.getServiceId()}, new Datavalue[]{serviceDatasetroot.getLocation(), serviceDatasetroot.getPath()});
		return runStatement(ps);
	}
	public static int insertServiceProperty(int serviceId, Datavalue name, Datavalue value) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_service_property", new int[]{serviceId}, new Datavalue[]{name, value});
		return runStatement(ps);
	}
	public static int insertServiceProperty(ServiceProperty serviceProperty) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_service_property", new int[]{serviceProperty.getServiceId()}, new Datavalue[]{serviceProperty.getName(), serviceProperty.getValue()});
		return runStatement(ps);
	}
	public static int insertServiceService(int parentId, int childId) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_service_service", new int[]{parentId, childId});
		return runStatement(ps);
	}
	public static int insertServiceService(ServiceService serviceService) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_service_service", new int[]{serviceService.getParentId(), serviceService.getChildId()});
		return runStatement(ps);
	}
	public static int insertTmg() throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg");
		return runStatement(ps);
	}
	public static int insertTmg(Tmg tmg) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg");
		return runStatement(ps);
	}
	public static int insertTmgAuthority(int tmgId, Datavalue authority) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_authority", new int[]{tmgId}, new Datavalue[]{authority});
		return runStatement(ps);
	}
	public static int insertTmgAuthority(TmgAuthority tmgAuthority) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_authority", new int[]{tmgAuthority.getTmgId()}, new Datavalue[]{tmgAuthority.getAuthority()});
		return runStatement(ps);
	}
	public static int insertTmgContributor(int tmgId, Datavalue name, Datavalue role) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_contributor", new int[]{tmgId}, new Datavalue[]{name, role});
		return runStatement(ps);
	}
	public static int insertTmgContributor(TmgContributor tmgContributor) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_contributor", new int[]{tmgContributor.getTmgId()}, new Datavalue[]{tmgContributor.getName(), tmgContributor.getRole()});
		return runStatement(ps);
	}
	public static int insertTmgCreator(int tmgId) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_creator", new int[]{tmgId});
		return runStatement(ps);
	}
	public static int insertTmgCreator(TmgCreator tmgCreator) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_creator", new int[]{tmgCreator.getTmgId()});
		return runStatement(ps);
	}
	public static int insertTmgCreatorContact(int tmgCreatorId, Datavalue email, Datavalue url) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_creator_contact", new int[]{tmgCreatorId}, new Datavalue[]{email, url});
		return runStatement(ps);
	}
	public static int insertTmgCreatorContact(TmgCreatorContact tmgCreatorContact) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_creator_contact", new int[]{tmgCreatorContact.getTmgCreatorId()}, new Datavalue[]{tmgCreatorContact.getEmail(), tmgCreatorContact.getUrl()});
		return runStatement(ps);
	}
	public static int insertTmgCreatorName(int tmgCreatorId, Datavalue value, Datavalue vocabulary) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_creator_name", new int[]{tmgCreatorId}, new Datavalue[]{value, vocabulary});
		return runStatement(ps);
	}
	public static int insertTmgCreatorName(TmgCreatorName tmgCreatorName) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_creator_name", new int[]{tmgCreatorName.getTmgCreatorId()}, new Datavalue[]{tmgCreatorName.getValue(), tmgCreatorName.getVocabulary()});
		return runStatement(ps);
	}
	public static int insertTmgDataformat(int tmgId, Datavalue dataformat) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_dataformat", new int[]{tmgId}, new Datavalue[]{dataformat});
		return runStatement(ps);
	}
	public static int insertTmgDataformat(TmgDataformat tmgDataformat) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_dataformat", new int[]{tmgDataformat.getTmgId()}, new Datavalue[]{tmgDataformat.getDataformat()});
		return runStatement(ps);
	}
	public static int insertTmgDatasize(int tmgId, Datavalue value, Datavalue units) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_datasize", new int[]{tmgId}, new Datavalue[]{value, units});
		return runStatement(ps);
	}
	public static int insertTmgDatasize(TmgDatasize tmgDatasize) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_datasize", new int[]{tmgDatasize.getTmgId()}, new Datavalue[]{tmgDatasize.getValue(), tmgDatasize.getUnits()});
		return runStatement(ps);
	}
	public static int insertTmgDatatype(int tmgId, Datavalue datatype) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_datatype", new int[]{tmgId}, new Datavalue[]{datatype});
		return runStatement(ps);
	}
	public static int insertTmgDatatype(TmgDatatype tmgDatatype) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_datatype", new int[]{tmgDatatype.getTmgId()}, new Datavalue[]{tmgDatatype.getDatatype()});
		return runStatement(ps);
	}
	public static int insertTmgDate(int tmgId, Datavalue format, Datavalue value, Datavalue dateenum) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_date", new int[]{tmgId}, new Datavalue[]{format, value, dateenum});
		return runStatement(ps);
	}
	public static int insertTmgDate(TmgDate tmgDate) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_date", new int[]{tmgDate.getTmgId()}, new Datavalue[]{tmgDate.getFormat(), tmgDate.getValue(), tmgDate.getDateenum()});
		return runStatement(ps);
	}
	public static int insertTmgDocumentation(int tmgId, Datavalue value, Datavalue documentationenum) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_documentation", new int[]{tmgId}, new Datavalue[]{value, documentationenum});
		return runStatement(ps);
	}
	public static int insertTmgDocumentation(TmgDocumentation tmgDocumentation) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_documentation", new int[]{tmgDocumentation.getTmgId()}, new Datavalue[]{tmgDocumentation.getValue(), tmgDocumentation.getDocumentationenum()});
		return runStatement(ps);
	}
	public static int insertTmgDocumentationNamespace(int tmgDocumentationId, Datavalue namespace) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_documentation_namespace", new int[]{tmgDocumentationId}, new Datavalue[]{namespace});
		return runStatement(ps);
	}
	public static int insertTmgDocumentationNamespace(TmgDocumentationNamespace tmgDocumentationNamespace) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_documentation_namespace", new int[]{tmgDocumentationNamespace.getTmgDocumentationId()}, new Datavalue[]{tmgDocumentationNamespace.getNamespace()});
		return runStatement(ps);
	}
	public static int insertTmgDocumentationXlink(int tmgDocumentationId, Datavalue value, Datavalue xlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_documentation_xlink", new int[]{tmgDocumentationId}, new Datavalue[]{value, xlink});
		return runStatement(ps);
	}
	public static int insertTmgDocumentationXlink(TmgDocumentationXlink tmgDocumentationXlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_documentation_xlink", new int[]{tmgDocumentationXlink.getTmgDocumentationId()}, new Datavalue[]{tmgDocumentationXlink.getValue(), tmgDocumentationXlink.getXlink()});
		return runStatement(ps);
	}
	public static int insertTmgGeospatialcoverage(int tmgId, Datavalue upordown) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_geospatialcoverage", new int[]{tmgId}, new Datavalue[]{upordown});
		return runStatement(ps);
	}
	public static int insertTmgGeospatialcoverage(TmgGeospatialcoverage tmgGeospatialcoverage) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_geospatialcoverage", new int[]{tmgGeospatialcoverage.getTmgId()}, new Datavalue[]{tmgGeospatialcoverage.getUpordown()});
		return runStatement(ps);
	}
	public static int insertTmgGeospatialcoverageEastwest(int tmgGeospatialcoverageId, Datavalue resolution, Datavalue size, Datavalue start, Datavalue units) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_geospatialcoverage_eastwest", new int[]{tmgGeospatialcoverageId}, new Datavalue[]{resolution, size, start, units});
		return runStatement(ps);
	}
	public static int insertTmgGeospatialcoverageEastwest(TmgGeospatialcoverageEastwest tmgGeospatialcoverageEastwest) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_geospatialcoverage_eastwest", new int[]{tmgGeospatialcoverageEastwest.getTmgGeospatialcoverageId()}, new Datavalue[]{tmgGeospatialcoverageEastwest.getResolution(), tmgGeospatialcoverageEastwest.getSize(), tmgGeospatialcoverageEastwest.getStart(), tmgGeospatialcoverageEastwest.getUnits()});
		return runStatement(ps);
	}
	public static int insertTmgGeospatialcoverageName(int tmgGeospatialcoverageId, Datavalue value, Datavalue vocabulary) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_geospatialcoverage_name", new int[]{tmgGeospatialcoverageId}, new Datavalue[]{value, vocabulary});
		return runStatement(ps);
	}
	public static int insertTmgGeospatialcoverageName(TmgGeospatialcoverageName tmgGeospatialcoverageName) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_geospatialcoverage_name", new int[]{tmgGeospatialcoverageName.getTmgGeospatialcoverageId()}, new Datavalue[]{tmgGeospatialcoverageName.getValue(), tmgGeospatialcoverageName.getVocabulary()});
		return runStatement(ps);
	}
	public static int insertTmgGeospatialcoverageNorthsouth(int tmgGeospatialcoverageId, Datavalue resolution, Datavalue size, Datavalue start, Datavalue units) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_geospatialcoverage_northsouth", new int[]{tmgGeospatialcoverageId}, new Datavalue[]{resolution, size, start, units});
		return runStatement(ps);
	}
	public static int insertTmgGeospatialcoverageNorthsouth(TmgGeospatialcoverageNorthsouth tmgGeospatialcoverageNorthsouth) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_geospatialcoverage_northsouth", new int[]{tmgGeospatialcoverageNorthsouth.getTmgGeospatialcoverageId()}, new Datavalue[]{tmgGeospatialcoverageNorthsouth.getResolution(), tmgGeospatialcoverageNorthsouth.getSize(), tmgGeospatialcoverageNorthsouth.getStart(), tmgGeospatialcoverageNorthsouth.getUnits()});
		return runStatement(ps);
	}
	public static int insertTmgGeospatialcoverageUpdown(int tmgGeospatialcoverageId, Datavalue resolution, Datavalue size, Datavalue start, Datavalue units) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_geospatialcoverage_updown", new int[]{tmgGeospatialcoverageId}, new Datavalue[]{resolution, size, start, units});
		return runStatement(ps);
	}
	public static int insertTmgGeospatialcoverageUpdown(TmgGeospatialcoverageUpdown tmgGeospatialcoverageUpdown) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_geospatialcoverage_updown", new int[]{tmgGeospatialcoverageUpdown.getTmgGeospatialcoverageId()}, new Datavalue[]{tmgGeospatialcoverageUpdown.getResolution(), tmgGeospatialcoverageUpdown.getSize(), tmgGeospatialcoverageUpdown.getStart(), tmgGeospatialcoverageUpdown.getUnits()});
		return runStatement(ps);
	}
	public static int insertTmgKeyword(int tmgId, Datavalue value, Datavalue vocabulary) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_keyword", new int[]{tmgId}, new Datavalue[]{value, vocabulary});
		return runStatement(ps);
	}
	public static int insertTmgKeyword(TmgKeyword tmgKeyword) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_keyword", new int[]{tmgKeyword.getTmgId()}, new Datavalue[]{tmgKeyword.getValue(), tmgKeyword.getVocabulary()});
		return runStatement(ps);
	}
	public static int insertTmgMetadata(int tmgId, int metadataId) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_metadata", new int[]{tmgId, metadataId});
		return runStatement(ps);
	}
	public static int insertTmgMetadata(TmgMetadata tmgMetadata) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_metadata", new int[]{tmgMetadata.getParentId(), tmgMetadata.getChildId()});
		return runStatement(ps);
	}
	public static int insertTmgProject(int tmgId, Datavalue value, Datavalue vocabulary) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_project", new int[]{tmgId}, new Datavalue[]{value, vocabulary});
		return runStatement(ps);
	}
	public static int insertTmgProject(TmgProject tmgProject) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_project", new int[]{tmgProject.getTmgId()}, new Datavalue[]{tmgProject.getValue(), tmgProject.getVocabulary()});
		return runStatement(ps);
	}
	public static int insertTmgProperty(int tmgId, Datavalue name, Datavalue value) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_property", new int[]{tmgId}, new Datavalue[]{name, value});
		return runStatement(ps);
	}
	public static int insertTmgProperty(TmgProperty tmgProperty) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_property", new int[]{tmgProperty.getTmgId()}, new Datavalue[]{tmgProperty.getName(), tmgProperty.getValue()});
		return runStatement(ps);
	}
	public static int insertTmgPublisher(int tmgId) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_publisher", new int[]{tmgId});
		return runStatement(ps);
	}
	public static int insertTmgPublisher(TmgPublisher tmgPublisher) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_publisher", new int[]{tmgPublisher.getTmgId()});
		return runStatement(ps);
	}
	public static int insertTmgPublisherContact(int tmgPublisherId, Datavalue email, Datavalue url) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_publisher_contact", new int[]{tmgPublisherId}, new Datavalue[]{email, url});
		return runStatement(ps);
	}
	public static int insertTmgPublisherContact(TmgPublisherContact tmgPublisherContact) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_publisher_contact", new int[]{tmgPublisherContact.getTmgPublisherId()}, new Datavalue[]{tmgPublisherContact.getEmail(), tmgPublisherContact.getUrl()});
		return runStatement(ps);
	}
	public static int insertTmgPublisherName(int tmgPublisherId, Datavalue value, Datavalue vocabulary) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_publisher_name", new int[]{tmgPublisherId}, new Datavalue[]{value, vocabulary});
		return runStatement(ps);
	}
	public static int insertTmgPublisherName(TmgPublisherName tmgPublisherName) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_publisher_name", new int[]{tmgPublisherName.getTmgPublisherId()}, new Datavalue[]{tmgPublisherName.getValue(), tmgPublisherName.getVocabulary()});
		return runStatement(ps);
	}
	public static int insertTmgServicename(int tmgId, Datavalue servicename) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_servicename", new int[]{tmgId}, new Datavalue[]{servicename});
		return runStatement(ps);
	}
	public static int insertTmgServicename(TmgServicename tmgServicename) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_servicename", new int[]{tmgServicename.getTmgId()}, new Datavalue[]{tmgServicename.getServicename()});
		return runStatement(ps);
	}
	public static int insertTmgTimecoverage(int tmgId, Datavalue resolution) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_timecoverage", new int[]{tmgId}, new Datavalue[]{resolution});
		return runStatement(ps);
	}
	public static int insertTmgTimecoverage(TmgTimecoverage tmgTimecoverage) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_timecoverage", new int[]{tmgTimecoverage.getTmgId()}, new Datavalue[]{tmgTimecoverage.getResolution()});
		return runStatement(ps);
	}
	public static int insertTmgTimecoverageDuration(int tmgTimecoverageId, Datavalue duration) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_timecoverage_duration", new int[]{tmgTimecoverageId}, new Datavalue[]{duration});
		return runStatement(ps);
	}
	public static int insertTmgTimecoverageDuration(TmgTimecoverageDuration tmgTimecoverageDuration) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_timecoverage_duration", new int[]{tmgTimecoverageDuration.getTmgTimecoverageId()}, new Datavalue[]{tmgTimecoverageDuration.getDuration()});
		return runStatement(ps);
	}
	public static int insertTmgTimecoverageEnd(int tmgTimecoverageId, Datavalue format, Datavalue value, Datavalue dateenum) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_timecoverage_end", new int[]{tmgTimecoverageId}, new Datavalue[]{format, value, dateenum});
		return runStatement(ps);
	}
	public static int insertTmgTimecoverageEnd(TmgTimecoverageEnd tmgTimecoverageEnd) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_timecoverage_end", new int[]{tmgTimecoverageEnd.getTmgTimecoverageId()}, new Datavalue[]{tmgTimecoverageEnd.getFormat(), tmgTimecoverageEnd.getValue(), tmgTimecoverageEnd.getDateenum()});
		return runStatement(ps);
	}
	public static int insertTmgTimecoverageResolution(int tmgTimecoverageId, Datavalue duration) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_timecoverage_resolution", new int[]{tmgTimecoverageId}, new Datavalue[]{duration});
		return runStatement(ps);
	}
	public static int insertTmgTimecoverageResolution(TmgTimecoverageResolution tmgTimecoverageResolution) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_timecoverage_resolution", new int[]{tmgTimecoverageResolution.getTmgTimecoverageId()}, new Datavalue[]{tmgTimecoverageResolution.getDuration()});
		return runStatement(ps);
	}
	public static int insertTmgTimecoverageStart(int tmgTimecoverageId, Datavalue format, Datavalue value, Datavalue dateenum) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_timecoverage_start", new int[]{tmgTimecoverageId}, new Datavalue[]{format, value, dateenum});
		return runStatement(ps);
	}
	public static int insertTmgTimecoverageStart(TmgTimecoverageStart tmgTimecoverageStart) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_timecoverage_start", new int[]{tmgTimecoverageStart.getTmgTimecoverageId()}, new Datavalue[]{tmgTimecoverageStart.getFormat(), tmgTimecoverageStart.getValue(), tmgTimecoverageStart.getDateenum()});
		return runStatement(ps);
	}
	public static int insertTmgVariables(int tmgId, Datavalue vocabulary) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_variables", new int[]{tmgId}, new Datavalue[]{vocabulary});
		return runStatement(ps);
	}
	public static int insertTmgVariables(TmgVariables tmgVariables) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_variables", new int[]{tmgVariables.getTmgId()}, new Datavalue[]{tmgVariables.getVocabulary()});
		return runStatement(ps);
	}
	public static int insertTmgVariablesVariable(int tmgVariablesId, Datavalue name, Datavalue units, Datavalue vocabularyName) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_variables_variable", new int[]{tmgVariablesId}, new Datavalue[]{name, units, vocabularyName});
		return runStatement(ps);
	}
	public static int insertTmgVariablesVariable(TmgVariablesVariable tmgVariablesVariable) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_variables_variable", new int[]{tmgVariablesVariable.getTmgVariablesId()}, new Datavalue[]{tmgVariablesVariable.getName(), tmgVariablesVariable.getUnits(), tmgVariablesVariable.getVocabularyName()});
		return runStatement(ps);
	}
	public static int insertTmgVariablesVariablemap(int tmgVariablesId, Datavalue value, Datavalue xlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_variables_variablemap", new int[]{tmgVariablesId}, new Datavalue[]{value, xlink});
		return runStatement(ps);
	}
	public static int insertTmgVariablesVariablemap(TmgVariablesVariablemap tmgVariablesVariablemap) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_tmg_variables_variablemap", new int[]{tmgVariablesVariablemap.getTmgVariablesId()}, new Datavalue[]{tmgVariablesVariablemap.getValue(), tmgVariablesVariablemap.getXlink()});
		return runStatement(ps);
	}




	/*begin update methods*/

	public static int updateCatalog(int catalogId, Datavalue base, Datavalue expires, Datavalue name, Datavalue version, Datavalue xmlns, Datavalue status) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_catalog", new int[]{catalogId}, new Datavalue[]{base, expires, name, version, xmlns, status});
		return runStatement(ps);
	}
	public static int updateCatalog(Catalog catalog) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_catalog", new int[]{catalog.getCatalogId()}, new Datavalue[]{catalog.getCleanCatalogId(), catalog.getBase(), catalog.getExpires(), catalog.getName(), catalog.getVersion(), catalog.getXmlns(), catalog.getStatus()});
		return runStatement(ps);
	}
	public static int updateCatalogProperty(int catalogId, int catalogPropertyId, Datavalue name, Datavalue value) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_catalog_property", new int[]{catalogId, catalogPropertyId}, new Datavalue[]{name, value});
		return runStatement(ps);
	}
	public static int updateCatalogProperty(CatalogProperty catalogProperty) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_catalog_property", new int[]{catalogProperty.getCatalogId(), catalogProperty.getCatalogPropertyId()}, new Datavalue[]{catalogProperty.getName(), catalogProperty.getValue()});
		return runStatement(ps);
	}
	public static int updateCatalogXlink(int catalogId, int catalogXlinkId, Datavalue value, Datavalue xlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_catalog_xlink", new int[]{catalogId, catalogXlinkId}, new Datavalue[]{value, xlink});
		return runStatement(ps);
	}
	public static int updateCatalogXlink(CatalogXlink catalogXlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_catalog_xlink", new int[]{catalogXlink.getCatalogId(), catalogXlink.getCatalogXlinkId()}, new Datavalue[]{catalogXlink.getValue(), catalogXlink.getXlink()});
		return runStatement(ps);
	}
	public static int updateCatalogref(int catalogrefId) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_catalogref", new int[]{catalogrefId});
		return runStatement(ps);
	}
	public static int updateCatalogref(Catalogref catalogref) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_catalogref", new int[]{catalogref.getCatalogrefId()});
		return runStatement(ps);
	}
	public static int updateCatalogrefDocumentation(int catalogrefId, int catalogrefDocumentationId, Datavalue value, Datavalue documentationenum) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_catalogref_documentation", new int[]{catalogrefId, catalogrefDocumentationId}, new Datavalue[]{value, documentationenum});
		return runStatement(ps);
	}
	public static int updateCatalogrefDocumentation(CatalogrefDocumentation catalogrefDocumentation) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_catalogref_documentation", new int[]{catalogrefDocumentation.getCatalogrefId(), catalogrefDocumentation.getCatalogrefDocumentationId()}, new Datavalue[]{catalogrefDocumentation.getValue(), catalogrefDocumentation.getDocumentationenum()});
		return runStatement(ps);
	}
	public static int updateCatalogrefDocumentationNamespace(int catalogrefDocumentationId, int catalogrefDocumentationNamespaceId, Datavalue namespace) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_catalogref_documentation_namespace", new int[]{catalogrefDocumentationId, catalogrefDocumentationNamespaceId}, new Datavalue[]{namespace});
		return runStatement(ps);
	}
	public static int updateCatalogrefDocumentationNamespace(CatalogrefDocumentationNamespace catalogrefDocumentationNamespace) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_catalogref_documentation_namespace", new int[]{catalogrefDocumentationNamespace.getCatalogrefDocumentationId(), catalogrefDocumentationNamespace.getCatalogrefDocumentationNamespaceId()}, new Datavalue[]{catalogrefDocumentationNamespace.getNamespace()});
		return runStatement(ps);
	}
	public static int updateCatalogrefDocumentationXlink(int catalogrefDocumentationId, int catalogrefDocumentationXlinkId, Datavalue value, Datavalue xlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_catalogref_documentation_xlink", new int[]{catalogrefDocumentationId, catalogrefDocumentationXlinkId}, new Datavalue[]{value, xlink});
		return runStatement(ps);
	}
	public static int updateCatalogrefDocumentationXlink(CatalogrefDocumentationXlink catalogrefDocumentationXlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_catalogref_documentation_xlink", new int[]{catalogrefDocumentationXlink.getCatalogrefDocumentationId(), catalogrefDocumentationXlink.getCatalogrefDocumentationXlinkId()}, new Datavalue[]{catalogrefDocumentationXlink.getValue(), catalogrefDocumentationXlink.getXlink()});
		return runStatement(ps);
	}
	public static int updateCatalogrefXlink(int catalogrefId, int catalogrefXlinkId, Datavalue value, Datavalue xlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_catalogref_xlink", new int[]{catalogrefId, catalogrefXlinkId}, new Datavalue[]{value, xlink});
		return runStatement(ps);
	}
	public static int updateCatalogrefXlink(CatalogrefXlink catalogrefXlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_catalogref_xlink", new int[]{catalogrefXlink.getCatalogrefId(), catalogrefXlink.getCatalogrefXlinkId()}, new Datavalue[]{catalogrefXlink.getValue(), catalogrefXlink.getXlink()});
		return runStatement(ps);
	}
	public static int updateDataset(int datasetId, Datavalue alias, Datavalue authority, Datavalue dId, Datavalue harvest, Datavalue name, Datavalue resourcecontrol, Datavalue serviceName, Datavalue urlPath, Datavalue collectiontype, Datavalue datasizeUnit, Datavalue dataType, Datavalue status) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_dataset", new int[]{datasetId}, new Datavalue[]{alias, authority, dId, harvest, name, resourcecontrol, serviceName, urlPath, collectiontype, datasizeUnit, dataType, status});
		return runStatement(ps);
	}
	public static int updateDataset(Dataset dataset) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_dataset", new int[]{dataset.getDatasetId()}, new Datavalue[]{dataset.getAlias(), dataset.getAuthority(), dataset.getDId(), dataset.getHarvest(), dataset.getName(), dataset.getResourcecontrol(), dataset.getServiceName(), dataset.getUrlPath(), dataset.getCollectiontype(), dataset.getDatasizeUnit(), dataset.getDataType(), dataset.getStatus()});
		return runStatement(ps);
	}
	public static int updateDatasetAccess(int datasetId, int datasetAccessId, Datavalue servicename, Datavalue urlpath, Datavalue dataformat) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_dataset_access", new int[]{datasetId, datasetAccessId}, new Datavalue[]{servicename, urlpath, dataformat});
		return runStatement(ps);
	}
	public static int updateDatasetAccess(DatasetAccess datasetAccess) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_dataset_access", new int[]{datasetAccess.getDatasetId(), datasetAccess.getDatasetAccessId()}, new Datavalue[]{datasetAccess.getServicename(), datasetAccess.getUrlpath(), datasetAccess.getDataformat()});
		return runStatement(ps);
	}
	public static int updateDatasetAccessDatasize(int datasetAccessId, int datasetAccessDatasizeId, Datavalue value, Datavalue units) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_dataset_access_datasize", new int[]{datasetAccessId, datasetAccessDatasizeId}, new Datavalue[]{value, units});
		return runStatement(ps);
	}
	public static int updateDatasetAccessDatasize(DatasetAccessDatasize datasetAccessDatasize) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_dataset_access_datasize", new int[]{datasetAccessDatasize.getDatasetAccessId(), datasetAccessDatasize.getDatasetAccessDatasizeId()}, new Datavalue[]{datasetAccessDatasize.getValue(), datasetAccessDatasize.getUnits()});
		return runStatement(ps);
	}
	public static int updateDatasetNcml(int datasetId, int datasetNcmlId) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_dataset_ncml", new int[]{datasetId, datasetNcmlId});
		return runStatement(ps);
	}
	public static int updateDatasetNcml(DatasetNcml datasetNcml) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_dataset_ncml", new int[]{datasetNcml.getDatasetId(), datasetNcml.getDatasetNcmlId()});
		return runStatement(ps);
	}
	public static int updateDatasetProperty(int datasetId, int datasetPropertyId, Datavalue name, Datavalue value) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_dataset_property", new int[]{datasetId, datasetPropertyId}, new Datavalue[]{name, value});
		return runStatement(ps);
	}
	public static int updateDatasetProperty(DatasetProperty datasetProperty) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_dataset_property", new int[]{datasetProperty.getDatasetId(), datasetProperty.getDatasetPropertyId()}, new Datavalue[]{datasetProperty.getName(), datasetProperty.getValue()});
		return runStatement(ps);
	}
	public static int updateMetadata(int metadataId, Datavalue inherited, Datavalue metadatatype) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_metadata", new int[]{metadataId}, new Datavalue[]{inherited, metadatatype});
		return runStatement(ps);
	}
	public static int updateMetadata(Metadata metadata) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_metadata", new int[]{metadata.getMetadataId()}, new Datavalue[]{metadata.getInherited(), metadata.getMetadatatype()});
		return runStatement(ps);
	}
	public static int updateMetadataNamespace(int metadataId, int metadataNamespaceId, Datavalue namespace) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_metadata_namespace", new int[]{metadataId, metadataNamespaceId}, new Datavalue[]{namespace});
		return runStatement(ps);
	}
	public static int updateMetadataNamespace(MetadataNamespace metadataNamespace) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_metadata_namespace", new int[]{metadataNamespace.getMetadataId(), metadataNamespace.getMetadataNamespaceId()}, new Datavalue[]{metadataNamespace.getNamespace()});
		return runStatement(ps);
	}
	public static int updateMetadataXlink(int metadataId, int metadataXlinkId, Datavalue value, Datavalue xlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_metadata_xlink", new int[]{metadataId, metadataXlinkId}, new Datavalue[]{value, xlink});
		return runStatement(ps);
	}
	public static int updateMetadataXlink(MetadataXlink metadataXlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_metadata_xlink", new int[]{metadataXlink.getMetadataId(), metadataXlink.getMetadataXlinkId()}, new Datavalue[]{metadataXlink.getValue(), metadataXlink.getXlink()});
		return runStatement(ps);
	}
	public static int updateService(int serviceId, Datavalue base, Datavalue desc, Datavalue name, Datavalue suffix, Datavalue serviceType, Datavalue status) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_service", new int[]{serviceId}, new Datavalue[]{base, desc, name, suffix, serviceType, status});
		return runStatement(ps);
	}
	public static int updateService(Service service) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_service", new int[]{service.getServiceId()}, new Datavalue[]{service.getBase(), service.getDesc(), service.getName(), service.getSuffix(), service.getServiceType(), service.getStatus()});
		return runStatement(ps);
	}
	public static int updateServiceDatasetroot(int serviceId, int serviceDatasetrootId, Datavalue location, Datavalue path) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_service_datasetroot", new int[]{serviceId, serviceDatasetrootId}, new Datavalue[]{location, path});
		return runStatement(ps);
	}
	public static int updateServiceDatasetroot(ServiceDatasetroot serviceDatasetroot) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_service_datasetroot", new int[]{serviceDatasetroot.getServiceId(), serviceDatasetroot.getServiceDatasetrootId()}, new Datavalue[]{serviceDatasetroot.getLocation(), serviceDatasetroot.getPath()});
		return runStatement(ps);
	}
	public static int updateServiceProperty(int serviceId, int servicePropertyId, Datavalue name, Datavalue value) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_service_property", new int[]{serviceId, servicePropertyId}, new Datavalue[]{name, value});
		return runStatement(ps);
	}
	public static int updateServiceProperty(ServiceProperty serviceProperty) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_service_property", new int[]{serviceProperty.getServiceId(), serviceProperty.getServicePropertyId()}, new Datavalue[]{serviceProperty.getName(), serviceProperty.getValue()});
		return runStatement(ps);
	}
	public static int updateTmg(int tmgId) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg", new int[]{tmgId});
		return runStatement(ps);
	}
	public static int updateTmg(Tmg tmg) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg", new int[]{tmg.getTmgId()});
		return runStatement(ps);
	}
	public static int updateTmgAuthority(int tmgId, int tmgAuthorityId, Datavalue authority) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_authority", new int[]{tmgId, tmgAuthorityId}, new Datavalue[]{authority});
		return runStatement(ps);
	}
	public static int updateTmgAuthority(TmgAuthority tmgAuthority) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_authority", new int[]{tmgAuthority.getTmgId(), tmgAuthority.getTmgAuthorityId()}, new Datavalue[]{tmgAuthority.getAuthority()});
		return runStatement(ps);
	}
	public static int updateTmgContributor(int tmgId, int tmgContributorId, Datavalue name, Datavalue role) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_contributor", new int[]{tmgId, tmgContributorId}, new Datavalue[]{name, role});
		return runStatement(ps);
	}
	public static int updateTmgContributor(TmgContributor tmgContributor) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_contributor", new int[]{tmgContributor.getTmgId(), tmgContributor.getTmgContributorId()}, new Datavalue[]{tmgContributor.getName(), tmgContributor.getRole()});
		return runStatement(ps);
	}
	public static int updateTmgCreator(int tmgId, int tmgCreatorId) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_creator", new int[]{tmgId, tmgCreatorId});
		return runStatement(ps);
	}
	public static int updateTmgCreator(TmgCreator tmgCreator) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_creator", new int[]{tmgCreator.getTmgId(), tmgCreator.getTmgCreatorId()});
		return runStatement(ps);
	}
	public static int updateTmgCreatorContact(int tmgCreatorId, int tmgCreatorContactId, Datavalue email, Datavalue url) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_creator_contact", new int[]{tmgCreatorId, tmgCreatorContactId}, new Datavalue[]{email, url});
		return runStatement(ps);
	}
	public static int updateTmgCreatorContact(TmgCreatorContact tmgCreatorContact) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_creator_contact", new int[]{tmgCreatorContact.getTmgCreatorId(), tmgCreatorContact.getTmgCreatorContactId()}, new Datavalue[]{tmgCreatorContact.getEmail(), tmgCreatorContact.getUrl()});
		return runStatement(ps);
	}
	public static int updateTmgCreatorName(int tmgCreatorId, int tmgCreatorNameId, Datavalue value, Datavalue vocabulary) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_creator_name", new int[]{tmgCreatorId, tmgCreatorNameId}, new Datavalue[]{value, vocabulary});
		return runStatement(ps);
	}
	public static int updateTmgCreatorName(TmgCreatorName tmgCreatorName) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_creator_name", new int[]{tmgCreatorName.getTmgCreatorId(), tmgCreatorName.getTmgCreatorNameId()}, new Datavalue[]{tmgCreatorName.getValue(), tmgCreatorName.getVocabulary()});
		return runStatement(ps);
	}
	public static int updateTmgDataformat(int tmgId, int tmgDataformatId, Datavalue dataformat) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_dataformat", new int[]{tmgId, tmgDataformatId}, new Datavalue[]{dataformat});
		return runStatement(ps);
	}
	public static int updateTmgDataformat(TmgDataformat tmgDataformat) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_dataformat", new int[]{tmgDataformat.getTmgId(), tmgDataformat.getTmgDataformatId()}, new Datavalue[]{tmgDataformat.getDataformat()});
		return runStatement(ps);
	}
	public static int updateTmgDatasize(int tmgId, int tmgDatasizeId, Datavalue value, Datavalue units) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_datasize", new int[]{tmgId, tmgDatasizeId}, new Datavalue[]{value, units});
		return runStatement(ps);
	}
	public static int updateTmgDatasize(TmgDatasize tmgDatasize) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_datasize", new int[]{tmgDatasize.getTmgId(), tmgDatasize.getTmgDatasizeId()}, new Datavalue[]{tmgDatasize.getValue(), tmgDatasize.getUnits()});
		return runStatement(ps);
	}
	public static int updateTmgDatatype(int tmgId, int tmgDatatypeId, Datavalue datatype) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_datatype", new int[]{tmgId, tmgDatatypeId}, new Datavalue[]{datatype});
		return runStatement(ps);
	}
	public static int updateTmgDatatype(TmgDatatype tmgDatatype) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_datatype", new int[]{tmgDatatype.getTmgId(), tmgDatatype.getTmgDatatypeId()}, new Datavalue[]{tmgDatatype.getDatatype()});
		return runStatement(ps);
	}
	public static int updateTmgDate(int tmgId, int tmgDateId, Datavalue format, Datavalue value, Datavalue dateenum) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_date", new int[]{tmgId, tmgDateId}, new Datavalue[]{format, value, dateenum});
		return runStatement(ps);
	}
	public static int updateTmgDate(TmgDate tmgDate) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_date", new int[]{tmgDate.getTmgId(), tmgDate.getTmgDateId()}, new Datavalue[]{tmgDate.getFormat(), tmgDate.getValue(), tmgDate.getDateenum()});
		return runStatement(ps);
	}
	public static int updateTmgDocumentation(int tmgId, int tmgDocumentationId, Datavalue value, Datavalue documentationenum) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_documentation", new int[]{tmgId, tmgDocumentationId}, new Datavalue[]{value, documentationenum});
		return runStatement(ps);
	}
	public static int updateTmgDocumentation(TmgDocumentation tmgDocumentation) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_documentation", new int[]{tmgDocumentation.getTmgId(), tmgDocumentation.getTmgDocumentationId()}, new Datavalue[]{tmgDocumentation.getValue(), tmgDocumentation.getDocumentationenum()});
		return runStatement(ps);
	}
	public static int updateTmgDocumentationNamespace(int tmgDocumentationId, int tmgDocumentationNamespaceId, Datavalue namespace) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_documentation_namespace", new int[]{tmgDocumentationId, tmgDocumentationNamespaceId}, new Datavalue[]{namespace});
		return runStatement(ps);
	}
	public static int updateTmgDocumentationNamespace(TmgDocumentationNamespace tmgDocumentationNamespace) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_documentation_namespace", new int[]{tmgDocumentationNamespace.getTmgDocumentationId(), tmgDocumentationNamespace.getTmgDocumentationNamespaceId()}, new Datavalue[]{tmgDocumentationNamespace.getNamespace()});
		return runStatement(ps);
	}
	public static int updateTmgDocumentationXlink(int tmgDocumentationId, int tmgDocumentationXlinkId, Datavalue value, Datavalue xlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_documentation_xlink", new int[]{tmgDocumentationId, tmgDocumentationXlinkId}, new Datavalue[]{value, xlink});
		return runStatement(ps);
	}
	public static int updateTmgDocumentationXlink(TmgDocumentationXlink tmgDocumentationXlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_documentation_xlink", new int[]{tmgDocumentationXlink.getTmgDocumentationId(), tmgDocumentationXlink.getTmgDocumentationXlinkId()}, new Datavalue[]{tmgDocumentationXlink.getValue(), tmgDocumentationXlink.getXlink()});
		return runStatement(ps);
	}
	public static int updateTmgGeospatialcoverage(int tmgId, int tmgGeospatialcoverageId, Datavalue upordown) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_geospatialcoverage", new int[]{tmgId, tmgGeospatialcoverageId}, new Datavalue[]{upordown});
		return runStatement(ps);
	}
	public static int updateTmgGeospatialcoverage(TmgGeospatialcoverage tmgGeospatialcoverage) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_geospatialcoverage", new int[]{tmgGeospatialcoverage.getTmgId(), tmgGeospatialcoverage.getTmgGeospatialcoverageId()}, new Datavalue[]{tmgGeospatialcoverage.getUpordown()});
		return runStatement(ps);
	}
	public static int updateTmgGeospatialcoverageEastwest(int tmgGeospatialcoverageId, int tmgGeospatialcoverageEastwestId, Datavalue resolution, Datavalue size, Datavalue start, Datavalue units) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_geospatialcoverage_eastwest", new int[]{tmgGeospatialcoverageId, tmgGeospatialcoverageEastwestId}, new Datavalue[]{resolution, size, start, units});
		return runStatement(ps);
	}
	public static int updateTmgGeospatialcoverageEastwest(TmgGeospatialcoverageEastwest tmgGeospatialcoverageEastwest) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_geospatialcoverage_eastwest", new int[]{tmgGeospatialcoverageEastwest.getTmgGeospatialcoverageId(), tmgGeospatialcoverageEastwest.getTmgGeospatialcoverageEastwestId()}, new Datavalue[]{tmgGeospatialcoverageEastwest.getResolution(), tmgGeospatialcoverageEastwest.getSize(), tmgGeospatialcoverageEastwest.getStart(), tmgGeospatialcoverageEastwest.getUnits()});
		return runStatement(ps);
	}
	public static int updateTmgGeospatialcoverageName(int tmgGeospatialcoverageId, int tmgGeospatialcoverageNameId, Datavalue value, Datavalue vocabulary) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_geospatialcoverage_name", new int[]{tmgGeospatialcoverageId, tmgGeospatialcoverageNameId}, new Datavalue[]{value, vocabulary});
		return runStatement(ps);
	}
	public static int updateTmgGeospatialcoverageName(TmgGeospatialcoverageName tmgGeospatialcoverageName) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_geospatialcoverage_name", new int[]{tmgGeospatialcoverageName.getTmgGeospatialcoverageId(), tmgGeospatialcoverageName.getTmgGeospatialcoverageNameId()}, new Datavalue[]{tmgGeospatialcoverageName.getValue(), tmgGeospatialcoverageName.getVocabulary()});
		return runStatement(ps);
	}
	public static int updateTmgGeospatialcoverageNorthsouth(int tmgGeospatialcoverageId, int tmgGeospatialcoverageNorthsouthId, Datavalue resolution, Datavalue size, Datavalue start, Datavalue units) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_geospatialcoverage_northsouth", new int[]{tmgGeospatialcoverageId, tmgGeospatialcoverageNorthsouthId}, new Datavalue[]{resolution, size, start, units});
		return runStatement(ps);
	}
	public static int updateTmgGeospatialcoverageNorthsouth(TmgGeospatialcoverageNorthsouth tmgGeospatialcoverageNorthsouth) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_geospatialcoverage_northsouth", new int[]{tmgGeospatialcoverageNorthsouth.getTmgGeospatialcoverageId(), tmgGeospatialcoverageNorthsouth.getTmgGeospatialcoverageNorthsouthId()}, new Datavalue[]{tmgGeospatialcoverageNorthsouth.getResolution(), tmgGeospatialcoverageNorthsouth.getSize(), tmgGeospatialcoverageNorthsouth.getStart(), tmgGeospatialcoverageNorthsouth.getUnits()});
		return runStatement(ps);
	}
	public static int updateTmgGeospatialcoverageUpdown(int tmgGeospatialcoverageId, int tmgGeospatialcoverageUpdownId, Datavalue resolution, Datavalue size, Datavalue start, Datavalue units) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_geospatialcoverage_updown", new int[]{tmgGeospatialcoverageId, tmgGeospatialcoverageUpdownId}, new Datavalue[]{resolution, size, start, units});
		return runStatement(ps);
	}
	public static int updateTmgGeospatialcoverageUpdown(TmgGeospatialcoverageUpdown tmgGeospatialcoverageUpdown) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_geospatialcoverage_updown", new int[]{tmgGeospatialcoverageUpdown.getTmgGeospatialcoverageId(), tmgGeospatialcoverageUpdown.getTmgGeospatialcoverageUpdownId()}, new Datavalue[]{tmgGeospatialcoverageUpdown.getResolution(), tmgGeospatialcoverageUpdown.getSize(), tmgGeospatialcoverageUpdown.getStart(), tmgGeospatialcoverageUpdown.getUnits()});
		return runStatement(ps);
	}
	public static int updateTmgKeyword(int tmgId, int tmgKeywordId, Datavalue value, Datavalue vocabulary) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_keyword", new int[]{tmgId, tmgKeywordId}, new Datavalue[]{value, vocabulary});
		return runStatement(ps);
	}
	public static int updateTmgKeyword(TmgKeyword tmgKeyword) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_keyword", new int[]{tmgKeyword.getTmgId(), tmgKeyword.getTmgKeywordId()}, new Datavalue[]{tmgKeyword.getValue(), tmgKeyword.getVocabulary()});
		return runStatement(ps);
	}
	public static int updateTmgProject(int tmgId, int tmgProjectId, Datavalue value, Datavalue vocabulary) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_project", new int[]{tmgId, tmgProjectId}, new Datavalue[]{value, vocabulary});
		return runStatement(ps);
	}
	public static int updateTmgProject(TmgProject tmgProject) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_project", new int[]{tmgProject.getTmgId(), tmgProject.getTmgProjectId()}, new Datavalue[]{tmgProject.getValue(), tmgProject.getVocabulary()});
		return runStatement(ps);
	}
	public static int updateTmgProperty(int tmgId, int tmgPropertyId, Datavalue name, Datavalue value) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_property", new int[]{tmgId, tmgPropertyId}, new Datavalue[]{name, value});
		return runStatement(ps);
	}
	public static int updateTmgProperty(TmgProperty tmgProperty) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_property", new int[]{tmgProperty.getTmgId(), tmgProperty.getTmgPropertyId()}, new Datavalue[]{tmgProperty.getName(), tmgProperty.getValue()});
		return runStatement(ps);
	}
	public static int updateTmgPublisher(int tmgId, int tmgPublisherId) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_publisher", new int[]{tmgId, tmgPublisherId});
		return runStatement(ps);
	}
	public static int updateTmgPublisher(TmgPublisher tmgPublisher) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_publisher", new int[]{tmgPublisher.getTmgId(), tmgPublisher.getTmgPublisherId()});
		return runStatement(ps);
	}
	public static int updateTmgPublisherContact(int tmgPublisherId, int tmgPublisherContactId, Datavalue email, Datavalue url) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_publisher_contact", new int[]{tmgPublisherId, tmgPublisherContactId}, new Datavalue[]{email, url});
		return runStatement(ps);
	}
	public static int updateTmgPublisherContact(TmgPublisherContact tmgPublisherContact) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_publisher_contact", new int[]{tmgPublisherContact.getTmgPublisherId(), tmgPublisherContact.getTmgPublisherContactId()}, new Datavalue[]{tmgPublisherContact.getEmail(), tmgPublisherContact.getUrl()});
		return runStatement(ps);
	}
	public static int updateTmgPublisherName(int tmgPublisherId, int tmgPublisherNameId, Datavalue value, Datavalue vocabulary) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_publisher_name", new int[]{tmgPublisherId, tmgPublisherNameId}, new Datavalue[]{value, vocabulary});
		return runStatement(ps);
	}
	public static int updateTmgPublisherName(TmgPublisherName tmgPublisherName) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_publisher_name", new int[]{tmgPublisherName.getTmgPublisherId(), tmgPublisherName.getTmgPublisherNameId()}, new Datavalue[]{tmgPublisherName.getValue(), tmgPublisherName.getVocabulary()});
		return runStatement(ps);
	}
	public static int updateTmgServicename(int tmgId, int tmgServicenameId, Datavalue servicename) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_servicename", new int[]{tmgId, tmgServicenameId}, new Datavalue[]{servicename});
		return runStatement(ps);
	}
	public static int updateTmgServicename(TmgServicename tmgServicename) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_servicename", new int[]{tmgServicename.getTmgId(), tmgServicename.getTmgServicenameId()}, new Datavalue[]{tmgServicename.getServicename()});
		return runStatement(ps);
	}
	public static int updateTmgTimecoverage(int tmgId, int tmgTimecoverageId, Datavalue resolution) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_timecoverage", new int[]{tmgId, tmgTimecoverageId}, new Datavalue[]{resolution});
		return runStatement(ps);
	}
	public static int updateTmgTimecoverage(TmgTimecoverage tmgTimecoverage) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_timecoverage", new int[]{tmgTimecoverage.getTmgId(), tmgTimecoverage.getTmgTimecoverageId()}, new Datavalue[]{tmgTimecoverage.getResolution()});
		return runStatement(ps);
	}
	public static int updateTmgTimecoverageDuration(int tmgTimecoverageId, int tmgTimecoverageDurationId, Datavalue duration) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_timecoverage_duration", new int[]{tmgTimecoverageId, tmgTimecoverageDurationId}, new Datavalue[]{duration});
		return runStatement(ps);
	}
	public static int updateTmgTimecoverageDuration(TmgTimecoverageDuration tmgTimecoverageDuration) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_timecoverage_duration", new int[]{tmgTimecoverageDuration.getTmgTimecoverageId(), tmgTimecoverageDuration.getTmgTimecoverageDurationId()}, new Datavalue[]{tmgTimecoverageDuration.getDuration()});
		return runStatement(ps);
	}
	public static int updateTmgTimecoverageEnd(int tmgTimecoverageId, int tmgTimecoverageEndId, Datavalue format, Datavalue value, Datavalue dateenum) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_timecoverage_end", new int[]{tmgTimecoverageId, tmgTimecoverageEndId}, new Datavalue[]{format, value, dateenum});
		return runStatement(ps);
	}
	public static int updateTmgTimecoverageEnd(TmgTimecoverageEnd tmgTimecoverageEnd) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_timecoverage_end", new int[]{tmgTimecoverageEnd.getTmgTimecoverageId(), tmgTimecoverageEnd.getTmgTimecoverageEndId()}, new Datavalue[]{tmgTimecoverageEnd.getFormat(), tmgTimecoverageEnd.getValue(), tmgTimecoverageEnd.getDateenum()});
		return runStatement(ps);
	}
	public static int updateTmgTimecoverageResolution(int tmgTimecoverageId, int tmgTimecoverageResolutionId, Datavalue duration) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_timecoverage_resolution", new int[]{tmgTimecoverageId, tmgTimecoverageResolutionId}, new Datavalue[]{duration});
		return runStatement(ps);
	}
	public static int updateTmgTimecoverageResolution(TmgTimecoverageResolution tmgTimecoverageResolution) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_timecoverage_resolution", new int[]{tmgTimecoverageResolution.getTmgTimecoverageId(), tmgTimecoverageResolution.getTmgTimecoverageResolutionId()}, new Datavalue[]{tmgTimecoverageResolution.getDuration()});
		return runStatement(ps);
	}
	public static int updateTmgTimecoverageStart(int tmgTimecoverageId, int tmgTimecoverageStartId, Datavalue format, Datavalue value, Datavalue dateenum) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_timecoverage_start", new int[]{tmgTimecoverageId, tmgTimecoverageStartId}, new Datavalue[]{format, value, dateenum});
		return runStatement(ps);
	}
	public static int updateTmgTimecoverageStart(TmgTimecoverageStart tmgTimecoverageStart) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_timecoverage_start", new int[]{tmgTimecoverageStart.getTmgTimecoverageId(), tmgTimecoverageStart.getTmgTimecoverageStartId()}, new Datavalue[]{tmgTimecoverageStart.getFormat(), tmgTimecoverageStart.getValue(), tmgTimecoverageStart.getDateenum()});
		return runStatement(ps);
	}
	public static int updateTmgVariables(int tmgId, int tmgVariablesId, Datavalue vocabulary) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_variables", new int[]{tmgId, tmgVariablesId}, new Datavalue[]{vocabulary});
		return runStatement(ps);
	}
	public static int updateTmgVariables(TmgVariables tmgVariables) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_variables", new int[]{tmgVariables.getTmgId(), tmgVariables.getTmgVariablesId()}, new Datavalue[]{tmgVariables.getVocabulary()});
		return runStatement(ps);
	}
	public static int updateTmgVariablesVariable(int tmgVariablesId, int tmgVariablesVariableId, Datavalue name, Datavalue units, Datavalue vocabularyName) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_variables_variable", new int[]{tmgVariablesId, tmgVariablesVariableId}, new Datavalue[]{name, units, vocabularyName});
		return runStatement(ps);
	}
	public static int updateTmgVariablesVariable(TmgVariablesVariable tmgVariablesVariable) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_variables_variable", new int[]{tmgVariablesVariable.getTmgVariablesId(), tmgVariablesVariable.getTmgVariablesVariableId()}, new Datavalue[]{tmgVariablesVariable.getName(), tmgVariablesVariable.getUnits(), tmgVariablesVariable.getVocabularyName()});
		return runStatement(ps);
	}
	public static int updateTmgVariablesVariablemap(int tmgVariablesId, int tmgVariablesVariablemapId, Datavalue value, Datavalue xlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_variables_variablemap", new int[]{tmgVariablesId, tmgVariablesVariablemapId}, new Datavalue[]{value, xlink});
		return runStatement(ps);
	}
	public static int updateTmgVariablesVariablemap(TmgVariablesVariablemap tmgVariablesVariablemap) throws Exception{
		PreparedStatement ps = setPreparedStatement("update_tmg_variables_variablemap", new int[]{tmgVariablesVariablemap.getTmgVariablesId(), tmgVariablesVariablemap.getTmgVariablesVariablemapId()}, new Datavalue[]{tmgVariablesVariablemap.getValue(), tmgVariablesVariablemap.getXlink()});
		return runStatement(ps);
	}




	/*begin delete methods*/

	public static int deleteCatalog(int catalogId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_catalog", new int[]{catalogId});
		return runStatement(ps);
	}
	public static int deleteCatalog(Catalog catalog) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_catalog", new int[]{catalog.getCatalogId()});
		return runStatement(ps);
	}
	public static int deleteCatalogDataset(int parentId, int childId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_catalog_dataset", new int[]{parentId, childId});
		return runStatement(ps);
	}
	public static int deleteCatalogDataset(CatalogDataset catalogDataset) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_catalog_dataset", new int[]{catalogDataset.getParentId(), catalogDataset.getChildId()});
		return runStatement(ps);
	}
	public static int deleteCatalogProperty(int catalogPropertyId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_catalog_property", new int[]{catalogPropertyId});
		return runStatement(ps);
	}
	public static int deleteCatalogProperty(CatalogProperty catalogProperty) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_catalog_property", new int[]{catalogProperty.getCatalogPropertyId()});
		return runStatement(ps);
	}
	public static int deleteCatalogService(int parentId, int childId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_catalog_service", new int[]{parentId, childId});
		return runStatement(ps);
	}
	public static int deleteCatalogService(CatalogService catalogService) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_catalog_service", new int[]{catalogService.getParentId(), catalogService.getChildId()});
		return runStatement(ps);
	}
	public static int deleteCatalogXlink(int catalogXlinkId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_catalog_xlink", new int[]{catalogXlinkId});
		return runStatement(ps);
	}
	public static int deleteCatalogXlink(CatalogXlink catalogXlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_catalog_xlink", new int[]{catalogXlink.getCatalogXlinkId()});
		return runStatement(ps);
	}
	public static int deleteCatalogref(int catalogrefId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_catalogref", new int[]{catalogrefId});
		return runStatement(ps);
	}
	public static int deleteCatalogref(Catalogref catalogref) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_catalogref", new int[]{catalogref.getCatalogrefId()});
		return runStatement(ps);
	}
	public static int deleteCatalogrefDocumentation(int catalogrefDocumentationId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_catalogref_documentation", new int[]{catalogrefDocumentationId});
		return runStatement(ps);
	}
	public static int deleteCatalogrefDocumentation(CatalogrefDocumentation catalogrefDocumentation) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_catalogref_documentation", new int[]{catalogrefDocumentation.getCatalogrefDocumentationId()});
		return runStatement(ps);
	}
	public static int deleteCatalogrefDocumentationNamespace(int catalogrefDocumentationNamespaceId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_catalogref_documentation_namespace", new int[]{catalogrefDocumentationNamespaceId});
		return runStatement(ps);
	}
	public static int deleteCatalogrefDocumentationNamespace(CatalogrefDocumentationNamespace catalogrefDocumentationNamespace) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_catalogref_documentation_namespace", new int[]{catalogrefDocumentationNamespace.getCatalogrefDocumentationNamespaceId()});
		return runStatement(ps);
	}
	public static int deleteCatalogrefDocumentationXlink(int catalogrefDocumentationXlinkId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_catalogref_documentation_xlink", new int[]{catalogrefDocumentationXlinkId});
		return runStatement(ps);
	}
	public static int deleteCatalogrefDocumentationXlink(CatalogrefDocumentationXlink catalogrefDocumentationXlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_catalogref_documentation_xlink", new int[]{catalogrefDocumentationXlink.getCatalogrefDocumentationXlinkId()});
		return runStatement(ps);
	}
	public static int deleteCatalogrefXlink(int catalogrefXlinkId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_catalogref_xlink", new int[]{catalogrefXlinkId});
		return runStatement(ps);
	}
	public static int deleteCatalogrefXlink(CatalogrefXlink catalogrefXlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_catalogref_xlink", new int[]{catalogrefXlink.getCatalogrefXlinkId()});
		return runStatement(ps);
	}
	public static int deleteDataset(int datasetId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_dataset", new int[]{datasetId});
		return runStatement(ps);
	}
	public static int deleteDataset(Dataset dataset) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_dataset", new int[]{dataset.getDatasetId()});
		return runStatement(ps);
	}
	public static int deleteDatasetAccess(int datasetAccessId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_dataset_access", new int[]{datasetAccessId});
		return runStatement(ps);
	}
	public static int deleteDatasetAccess(DatasetAccess datasetAccess) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_dataset_access", new int[]{datasetAccess.getDatasetAccessId()});
		return runStatement(ps);
	}
	public static int deleteDatasetAccessDatasize(int datasetAccessDatasizeId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_dataset_access_datasize", new int[]{datasetAccessDatasizeId});
		return runStatement(ps);
	}
	public static int deleteDatasetAccessDatasize(DatasetAccessDatasize datasetAccessDatasize) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_dataset_access_datasize", new int[]{datasetAccessDatasize.getDatasetAccessDatasizeId()});
		return runStatement(ps);
	}
	public static int deleteDatasetDataset(int parentId, int childId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_dataset_dataset", new int[]{parentId, childId});
		return runStatement(ps);
	}
	public static int deleteDatasetDataset(DatasetDataset datasetDataset) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_dataset_dataset", new int[]{datasetDataset.getParentId(), datasetDataset.getChildId()});
		return runStatement(ps);
	}
	public static int deleteDatasetNcml(int datasetNcmlId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_dataset_ncml", new int[]{datasetNcmlId});
		return runStatement(ps);
	}
	public static int deleteDatasetNcml(DatasetNcml datasetNcml) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_dataset_ncml", new int[]{datasetNcml.getDatasetNcmlId()});
		return runStatement(ps);
	}
	public static int deleteDatasetProperty(int datasetPropertyId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_dataset_property", new int[]{datasetPropertyId});
		return runStatement(ps);
	}
	public static int deleteDatasetProperty(DatasetProperty datasetProperty) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_dataset_property", new int[]{datasetProperty.getDatasetPropertyId()});
		return runStatement(ps);
	}
	public static int deleteDatasetService(int parentId, int childId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_dataset_service", new int[]{parentId, childId});
		return runStatement(ps);
	}
	public static int deleteDatasetService(DatasetService datasetService) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_dataset_service", new int[]{datasetService.getParentId(), datasetService.getChildId()});
		return runStatement(ps);
	}
	public static int deleteDatasetTmg(int parentId, int childId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_dataset_tmg", new int[]{parentId, childId});
		return runStatement(ps);
	}
	public static int deleteDatasetTmg(DatasetTmg datasetTmg) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_dataset_tmg", new int[]{datasetTmg.getParentId(), datasetTmg.getChildId()});
		return runStatement(ps);
	}
	public static int deleteMetadata(int metadataId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_metadata", new int[]{metadataId});
		return runStatement(ps);
	}
	public static int deleteMetadata(Metadata metadata) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_metadata", new int[]{metadata.getMetadataId()});
		return runStatement(ps);
	}
	public static int deleteMetadataNamespace(int metadataNamespaceId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_metadata_namespace", new int[]{metadataNamespaceId});
		return runStatement(ps);
	}
	public static int deleteMetadataNamespace(MetadataNamespace metadataNamespace) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_metadata_namespace", new int[]{metadataNamespace.getMetadataNamespaceId()});
		return runStatement(ps);
	}
	public static int deleteMetadataTmg(int parentId, int childId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_metadata_tmg", new int[]{parentId, childId});
		return runStatement(ps);
	}
	public static int deleteMetadataTmg(MetadataTmg metadataTmg) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_metadata_tmg", new int[]{metadataTmg.getParentId(), metadataTmg.getChildId()});
		return runStatement(ps);
	}
	public static int deleteMetadataXlink(int metadataXlinkId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_metadata_xlink", new int[]{metadataXlinkId});
		return runStatement(ps);
	}
	public static int deleteMetadataXlink(MetadataXlink metadataXlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_metadata_xlink", new int[]{metadataXlink.getMetadataXlinkId()});
		return runStatement(ps);
	}
	public static int deleteService(int serviceId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_service", new int[]{serviceId});
		return runStatement(ps);
	}
	public static int deleteService(Service service) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_service", new int[]{service.getServiceId()});
		return runStatement(ps);
	}
	public static int deleteServiceDatasetroot(int serviceDatasetrootId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_service_datasetroot", new int[]{serviceDatasetrootId});
		return runStatement(ps);
	}
	public static int deleteServiceDatasetroot(ServiceDatasetroot serviceDatasetroot) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_service_datasetroot", new int[]{serviceDatasetroot.getServiceDatasetrootId()});
		return runStatement(ps);
	}
	public static int deleteServiceProperty(int servicePropertyId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_service_property", new int[]{servicePropertyId});
		return runStatement(ps);
	}
	public static int deleteServiceProperty(ServiceProperty serviceProperty) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_service_property", new int[]{serviceProperty.getServicePropertyId()});
		return runStatement(ps);
	}
	public static int deleteServiceService(int parentId, int childId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_service_service", new int[]{parentId, childId});
		return runStatement(ps);
	}
	public static int deleteServiceService(ServiceService serviceService) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_service_service", new int[]{serviceService.getParentId(), serviceService.getChildId()});
		return runStatement(ps);
	}
	public static int deleteTmg(int tmgId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg", new int[]{tmgId});
		return runStatement(ps);
	}
	public static int deleteTmg(Tmg tmg) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg", new int[]{tmg.getTmgId()});
		return runStatement(ps);
	}
	public static int deleteTmgAuthority(int tmgAuthorityId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_authority", new int[]{tmgAuthorityId});
		return runStatement(ps);
	}
	public static int deleteTmgAuthority(TmgAuthority tmgAuthority) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_authority", new int[]{tmgAuthority.getTmgAuthorityId()});
		return runStatement(ps);
	}
	public static int deleteTmgContributor(int tmgContributorId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_contributor", new int[]{tmgContributorId});
		return runStatement(ps);
	}
	public static int deleteTmgContributor(TmgContributor tmgContributor) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_contributor", new int[]{tmgContributor.getTmgContributorId()});
		return runStatement(ps);
	}
	public static int deleteTmgCreator(int tmgCreatorId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_creator", new int[]{tmgCreatorId});
		return runStatement(ps);
	}
	public static int deleteTmgCreator(TmgCreator tmgCreator) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_creator", new int[]{tmgCreator.getTmgCreatorId()});
		return runStatement(ps);
	}
	public static int deleteTmgCreatorContact(int tmgCreatorContactId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_creator_contact", new int[]{tmgCreatorContactId});
		return runStatement(ps);
	}
	public static int deleteTmgCreatorContact(TmgCreatorContact tmgCreatorContact) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_creator_contact", new int[]{tmgCreatorContact.getTmgCreatorContactId()});
		return runStatement(ps);
	}
	public static int deleteTmgCreatorName(int tmgCreatorNameId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_creator_name", new int[]{tmgCreatorNameId});
		return runStatement(ps);
	}
	public static int deleteTmgCreatorName(TmgCreatorName tmgCreatorName) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_creator_name", new int[]{tmgCreatorName.getTmgCreatorNameId()});
		return runStatement(ps);
	}
	public static int deleteTmgDataformat(int tmgDataformatId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_dataformat", new int[]{tmgDataformatId});
		return runStatement(ps);
	}
	public static int deleteTmgDataformat(TmgDataformat tmgDataformat) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_dataformat", new int[]{tmgDataformat.getTmgDataformatId()});
		return runStatement(ps);
	}
	public static int deleteTmgDatasize(int tmgDatasizeId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_datasize", new int[]{tmgDatasizeId});
		return runStatement(ps);
	}
	public static int deleteTmgDatasize(TmgDatasize tmgDatasize) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_datasize", new int[]{tmgDatasize.getTmgDatasizeId()});
		return runStatement(ps);
	}
	public static int deleteTmgDatatype(int tmgDatatypeId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_datatype", new int[]{tmgDatatypeId});
		return runStatement(ps);
	}
	public static int deleteTmgDatatype(TmgDatatype tmgDatatype) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_datatype", new int[]{tmgDatatype.getTmgDatatypeId()});
		return runStatement(ps);
	}
	public static int deleteTmgDate(int tmgDateId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_date", new int[]{tmgDateId});
		return runStatement(ps);
	}
	public static int deleteTmgDate(TmgDate tmgDate) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_date", new int[]{tmgDate.getTmgDateId()});
		return runStatement(ps);
	}
	public static int deleteTmgDocumentation(int tmgDocumentationId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_documentation", new int[]{tmgDocumentationId});
		return runStatement(ps);
	}
	public static int deleteTmgDocumentation(TmgDocumentation tmgDocumentation) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_documentation", new int[]{tmgDocumentation.getTmgDocumentationId()});
		return runStatement(ps);
	}
	public static int deleteTmgDocumentationNamespace(int tmgDocumentationNamespaceId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_documentation_namespace", new int[]{tmgDocumentationNamespaceId});
		return runStatement(ps);
	}
	public static int deleteTmgDocumentationNamespace(TmgDocumentationNamespace tmgDocumentationNamespace) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_documentation_namespace", new int[]{tmgDocumentationNamespace.getTmgDocumentationNamespaceId()});
		return runStatement(ps);
	}
	public static int deleteTmgDocumentationXlink(int tmgDocumentationXlinkId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_documentation_xlink", new int[]{tmgDocumentationXlinkId});
		return runStatement(ps);
	}
	public static int deleteTmgDocumentationXlink(TmgDocumentationXlink tmgDocumentationXlink) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_documentation_xlink", new int[]{tmgDocumentationXlink.getTmgDocumentationXlinkId()});
		return runStatement(ps);
	}
	public static int deleteTmgGeospatialcoverage(int tmgGeospatialcoverageId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_geospatialcoverage", new int[]{tmgGeospatialcoverageId});
		return runStatement(ps);
	}
	public static int deleteTmgGeospatialcoverage(TmgGeospatialcoverage tmgGeospatialcoverage) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_geospatialcoverage", new int[]{tmgGeospatialcoverage.getTmgGeospatialcoverageId()});
		return runStatement(ps);
	}
	public static int deleteTmgGeospatialcoverageEastwest(int tmgGeospatialcoverageEastwestId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_geospatialcoverage_eastwest", new int[]{tmgGeospatialcoverageEastwestId});
		return runStatement(ps);
	}
	public static int deleteTmgGeospatialcoverageEastwest(TmgGeospatialcoverageEastwest tmgGeospatialcoverageEastwest) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_geospatialcoverage_eastwest", new int[]{tmgGeospatialcoverageEastwest.getTmgGeospatialcoverageEastwestId()});
		return runStatement(ps);
	}
	public static int deleteTmgGeospatialcoverageName(int tmgGeospatialcoverageNameId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_geospatialcoverage_name", new int[]{tmgGeospatialcoverageNameId});
		return runStatement(ps);
	}
	public static int deleteTmgGeospatialcoverageName(TmgGeospatialcoverageName tmgGeospatialcoverageName) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_geospatialcoverage_name", new int[]{tmgGeospatialcoverageName.getTmgGeospatialcoverageNameId()});
		return runStatement(ps);
	}
	public static int deleteTmgGeospatialcoverageNorthsouth(int tmgGeospatialcoverageNorthsouthId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_geospatialcoverage_northsouth", new int[]{tmgGeospatialcoverageNorthsouthId});
		return runStatement(ps);
	}
	public static int deleteTmgGeospatialcoverageNorthsouth(TmgGeospatialcoverageNorthsouth tmgGeospatialcoverageNorthsouth) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_geospatialcoverage_northsouth", new int[]{tmgGeospatialcoverageNorthsouth.getTmgGeospatialcoverageNorthsouthId()});
		return runStatement(ps);
	}
	public static int deleteTmgGeospatialcoverageUpdown(int tmgGeospatialcoverageUpdownId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_geospatialcoverage_updown", new int[]{tmgGeospatialcoverageUpdownId});
		return runStatement(ps);
	}
	public static int deleteTmgGeospatialcoverageUpdown(TmgGeospatialcoverageUpdown tmgGeospatialcoverageUpdown) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_geospatialcoverage_updown", new int[]{tmgGeospatialcoverageUpdown.getTmgGeospatialcoverageUpdownId()});
		return runStatement(ps);
	}
	public static int deleteTmgKeyword(int tmgKeywordId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_keyword", new int[]{tmgKeywordId});
		return runStatement(ps);
	}
	public static int deleteTmgKeyword(TmgKeyword tmgKeyword) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_keyword", new int[]{tmgKeyword.getTmgKeywordId()});
		return runStatement(ps);
	}
	public static int deleteTmgMetadata(int parentId, int childId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_metadata", new int[]{parentId, childId});
		return runStatement(ps);
	}
	public static int deleteTmgMetadata(TmgMetadata tmgMetadata) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_metadata", new int[]{tmgMetadata.getParentId(), tmgMetadata.getChildId()});
		return runStatement(ps);
	}
	public static int deleteTmgProject(int tmgProjectId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_project", new int[]{tmgProjectId});
		return runStatement(ps);
	}
	public static int deleteTmgProject(TmgProject tmgProject) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_project", new int[]{tmgProject.getTmgProjectId()});
		return runStatement(ps);
	}
	public static int deleteTmgProperty(int tmgPropertyId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_property", new int[]{tmgPropertyId});
		return runStatement(ps);
	}
	public static int deleteTmgProperty(TmgProperty tmgProperty) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_property", new int[]{tmgProperty.getTmgPropertyId()});
		return runStatement(ps);
	}
	public static int deleteTmgPublisher(int tmgPublisherId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_publisher", new int[]{tmgPublisherId});
		return runStatement(ps);
	}
	public static int deleteTmgPublisher(TmgPublisher tmgPublisher) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_publisher", new int[]{tmgPublisher.getTmgPublisherId()});
		return runStatement(ps);
	}
	public static int deleteTmgPublisherContact(int tmgPublisherContactId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_publisher_contact", new int[]{tmgPublisherContactId});
		return runStatement(ps);
	}
	public static int deleteTmgPublisherContact(TmgPublisherContact tmgPublisherContact) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_publisher_contact", new int[]{tmgPublisherContact.getTmgPublisherContactId()});
		return runStatement(ps);
	}
	public static int deleteTmgPublisherName(int tmgPublisherNameId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_publisher_name", new int[]{tmgPublisherNameId});
		return runStatement(ps);
	}
	public static int deleteTmgPublisherName(TmgPublisherName tmgPublisherName) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_publisher_name", new int[]{tmgPublisherName.getTmgPublisherNameId()});
		return runStatement(ps);
	}
	public static int deleteTmgServicename(int tmgServicenameId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_servicename", new int[]{tmgServicenameId});
		return runStatement(ps);
	}
	public static int deleteTmgServicename(TmgServicename tmgServicename) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_servicename", new int[]{tmgServicename.getTmgServicenameId()});
		return runStatement(ps);
	}
	public static int deleteTmgTimecoverage(int tmgTimecoverageId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_timecoverage", new int[]{tmgTimecoverageId});
		return runStatement(ps);
	}
	public static int deleteTmgTimecoverage(TmgTimecoverage tmgTimecoverage) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_timecoverage", new int[]{tmgTimecoverage.getTmgTimecoverageId()});
		return runStatement(ps);
	}
	public static int deleteTmgTimecoverageDuration(int tmgTimecoverageDurationId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_timecoverage_duration", new int[]{tmgTimecoverageDurationId});
		return runStatement(ps);
	}
	public static int deleteTmgTimecoverageDuration(TmgTimecoverageDuration tmgTimecoverageDuration) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_timecoverage_duration", new int[]{tmgTimecoverageDuration.getTmgTimecoverageDurationId()});
		return runStatement(ps);
	}
	public static int deleteTmgTimecoverageEnd(int tmgTimecoverageEndId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_timecoverage_end", new int[]{tmgTimecoverageEndId});
		return runStatement(ps);
	}
	public static int deleteTmgTimecoverageEnd(TmgTimecoverageEnd tmgTimecoverageEnd) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_timecoverage_end", new int[]{tmgTimecoverageEnd.getTmgTimecoverageEndId()});
		return runStatement(ps);
	}
	public static int deleteTmgTimecoverageResolution(int tmgTimecoverageResolutionId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_timecoverage_resolution", new int[]{tmgTimecoverageResolutionId});
		return runStatement(ps);
	}
	public static int deleteTmgTimecoverageResolution(TmgTimecoverageResolution tmgTimecoverageResolution) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_timecoverage_resolution", new int[]{tmgTimecoverageResolution.getTmgTimecoverageResolutionId()});
		return runStatement(ps);
	}
	public static int deleteTmgTimecoverageStart(int tmgTimecoverageStartId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_timecoverage_start", new int[]{tmgTimecoverageStartId});
		return runStatement(ps);
	}
	public static int deleteTmgTimecoverageStart(TmgTimecoverageStart tmgTimecoverageStart) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_timecoverage_start", new int[]{tmgTimecoverageStart.getTmgTimecoverageStartId()});
		return runStatement(ps);
	}
	public static int deleteTmgVariables(int tmgVariablesId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_variables", new int[]{tmgVariablesId});
		return runStatement(ps);
	}
	public static int deleteTmgVariables(TmgVariables tmgVariables) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_variables", new int[]{tmgVariables.getTmgVariablesId()});
		return runStatement(ps);
	}
	public static int deleteTmgVariablesVariable(int tmgVariablesVariableId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_variables_variable", new int[]{tmgVariablesVariableId});
		return runStatement(ps);
	}
	public static int deleteTmgVariablesVariable(TmgVariablesVariable tmgVariablesVariable) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_variables_variable", new int[]{tmgVariablesVariable.getTmgVariablesVariableId()});
		return runStatement(ps);
	}
	public static int deleteTmgVariablesVariablemap(int tmgVariablesVariablemapId) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_variables_variablemap", new int[]{tmgVariablesVariablemapId});
		return runStatement(ps);
	}
	public static int deleteTmgVariablesVariablemap(TmgVariablesVariablemap tmgVariablesVariablemap) throws Exception{
		PreparedStatement ps = setPreparedStatement("delete_tmg_variables_variablemap", new int[]{tmgVariablesVariablemap.getTmgVariablesVariablemapId()});
		return runStatement(ps);
	}
}
