package gov.noaa.pmel.tmap.catalogcleaner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
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
	public static PreparedStatement setPreparedStatement(String functionName, String[] theList) throws Exception{

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
			String name = theList[i];				
			if(name == null || name.isEmpty())
				ps.setNull(i+1, java.sql.Types.VARCHAR);
			else
				ps.setString(i+1, name);
		}

		return ps;
	}
	public static PreparedStatement setPreparedStatement(String functionName, String[] theList, int[] theInts) throws Exception{

		// just to make sure
		if(theInts.length == 0)
			throw new Exception("Invalid vars length");
		// just to make sure
		else if(theList.length == 0)
			throw new Exception("Invalid vars length");

		PreparedStatement ps = null;
		int strSize = theList.length;
		//	try {
		String insert = "select " + functionName + " (";
		for(int i = 0; i<strSize; i++){
			insert += "?, ";
		}
		int intSize = theInts.length;
		for(int i = 0; i<intSize-1; i++){
			insert += "?, ";
		}
		insert += "?)";
		ps = pgCache.prepareStatement(insert);
		for(int i = 0; i<strSize; i++){
			String name = theList[i];				
			if(name == null || name.isEmpty())
				ps.setNull(i+1, java.sql.Types.VARCHAR);
			else
				ps.setString(i+1, name);
		}
		for(int i = 0; i<intSize; i++){
			int theNum = theInts[i];				
			ps.setInt(i+1, theNum);
		}

		return ps;
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
				String xmlns = rs.getString("xmlns");
				String name = rs.getString("name");
				String base = rs.getString("base");
				String version = rs.getString("version");
				String expires = rs.getString("expires");
				String status = rs.getString("status");
				catalog = new Catalog(catalogId, xmlns, name, base, version, expires, status);
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
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// begin generated functions
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



	/*begin get methods*/

	public static Catalog getCatalog(int catalogId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		Catalog catalog = null;

		try {
			ps = pgCache.prepareStatement("select * from catalog where catalog_id=?");
			ps.setInt(1, catalogId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String name = rs.getString("name");
				if (name == null)
					name = "";
				String expires = rs.getString("expires");
				if (expires == null)
					expires = "";
				String version = rs.getString("version");
				if (version == null)
					version = "";
				String base = rs.getString("base");
				if (base == null)
					base = "";
				String xmlns = rs.getString("xmlns");
				if (xmlns == null)
					xmlns = "";
				String status = rs.getString("status");
				if (status == null)
					status = "";
				catalog = new Catalog(catalogId, name, expires, version, base, xmlns, status);
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
		return catalog;
	}

	public static ArrayList<Dataset> getDatasetBCatalog(int catalogId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> datasetIds = new ArrayList<Integer>();
		ArrayList<Dataset> datasets = new ArrayList<Dataset>();
		String select = "select dataset_id from catalog_dataset where ";
		select+= "catalog_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, catalogId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				datasetIds.add(rs.getInt("dataset_id"));
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
		for(int i=0; i<datasetIds.size(); i++){
			datasets.add(getDataset(datasetIds.get(i)));
		}
		return datasets;
	}

	public static CatalogProperty getCatalogProperty(int catalogPropertyId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		CatalogProperty catalogProperty = null;

		try {
			ps = pgCache.prepareStatement("select * from catalog_property where catalog_property_id=?");
			ps.setInt(1, catalogPropertyId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String name = rs.getString("name");
				if (name == null)
					name = "";
				String value = rs.getString("value");
				if (value == null)
					value = "";
				int catalogId = rs.getInt("catalog_id");
				catalogProperty = new CatalogProperty(catalogPropertyId, name, value, catalogId);
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
		return catalogProperty;
	}

	public static ArrayList<CatalogProperty> getCatalogPropertyBCatalog(int catalogId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> propertyIds = new ArrayList<Integer>();
		ArrayList<CatalogProperty> propertys = new ArrayList<CatalogProperty>();
		String select = "select catalog_property_id from catalog_property where ";
		select+= "catalog_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, catalogId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				propertyIds.add(rs.getInt("property_id"));
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
		for(int i=0; i<propertyIds.size(); i++){
			propertys.add(getCatalogProperty(propertyIds.get(i)));
		}
		return propertys;
	}

	public static ArrayList<Service> getServiceBCatalog(int catalogId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> serviceIds = new ArrayList<Integer>();
		ArrayList<Service> services = new ArrayList<Service>();
		String select = "select service_id from catalog_service where ";
		select+= "catalog_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, catalogId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				serviceIds.add(rs.getInt("service_id"));
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
		for(int i=0; i<serviceIds.size(); i++){
			services.add(getService(serviceIds.get(i)));
		}
		return services;
	}

	public static CatalogXlink getCatalogXlink(int catalogXlinkId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		CatalogXlink catalogXlink = null;

		try {
			ps = pgCache.prepareStatement("select * from catalog_xlink where catalog_xlink_id=?");
			ps.setInt(1, catalogXlinkId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String value = rs.getString("value");
				if (value == null)
					value = "";
				String xlink = rs.getString("xlink");
				if (xlink == null)
					xlink = rs.getString("xlink_nonstandard");
				if (xlink == null)
					xlink = "";
				int catalogId = rs.getInt("catalog_id");
				catalogXlink = new CatalogXlink(catalogXlinkId, value, xlink, catalogId);
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
		return catalogXlink;
	}

	public static ArrayList<CatalogXlink> getCatalogXlinkBCatalog(int catalogId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> xlinkIds = new ArrayList<Integer>();
		ArrayList<CatalogXlink> xlinks = new ArrayList<CatalogXlink>();
		String select = "select catalog_xlink_id from catalog_xlink where ";
		select+= "catalog_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, catalogId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				xlinkIds.add(rs.getInt("xlink_id"));
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
		for(int i=0; i<xlinkIds.size(); i++){
			xlinks.add(getCatalogXlink(xlinkIds.get(i)));
		}
		return xlinks;
	}

	public static Catalogref getCatalogref(int catalogrefId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		Catalogref catalogref = null;

		try {
			ps = pgCache.prepareStatement("select * from catalogref where catalogref_id=?");
			ps.setInt(1, catalogrefId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				int childId = rs.getInt("child_id");
				int parentId = rs.getInt("parent_id");
				catalogref = new Catalogref(catalogrefId, childId, parentId);
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
		return catalogref;
	}

	public static CatalogrefDocumentation getCatalogrefDocumentation(int catalogrefDocumentationId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		CatalogrefDocumentation catalogrefDocumentation = null;

		try {
			ps = pgCache.prepareStatement("select * from catalogref_documentation where catalogref_documentation_id=?");
			ps.setInt(1, catalogrefDocumentationId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String value = rs.getString("value");
				if (value == null)
					value = "";
				String documentationenum = rs.getString("documentationenum");
				if (documentationenum == null)
					documentationenum = rs.getString("documentationenum_nonstandard");
				if (documentationenum == null)
					documentationenum = "";
				int catalogrefId = rs.getInt("catalogref_id");
				catalogrefDocumentation = new CatalogrefDocumentation(catalogrefDocumentationId, value, documentationenum, catalogrefId);
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
		return catalogrefDocumentation;
	}

	public static ArrayList<CatalogrefDocumentation> getCatalogrefDocumentationBCatalogref(int catalogrefId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> documentationIds = new ArrayList<Integer>();
		ArrayList<CatalogrefDocumentation> documentations = new ArrayList<CatalogrefDocumentation>();
		String select = "select catalogref_documentation_id from catalogref_documentation where ";
		select+= "catalogref_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, catalogrefId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				documentationIds.add(rs.getInt("documentation_id"));
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
		for(int i=0; i<documentationIds.size(); i++){
			documentations.add(getCatalogrefDocumentation(documentationIds.get(i)));
		}
		return documentations;
	}

	public static CatalogrefDocumentationNamespace getCatalogrefDocumentationNamespace(int catalogrefDocumentationNamespaceId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		CatalogrefDocumentationNamespace catalogrefDocumentationNamespace = null;

		try {
			ps = pgCache.prepareStatement("select * from catalogref_documentation_namespace where catalogref_documentation_namespace_id=?");
			ps.setInt(1, catalogrefDocumentationNamespaceId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String namespace = rs.getString("namespace");
				if (namespace == null)
					namespace = "";
				int catalogrefDocumentationId = rs.getInt("catalogref_documentation_id");
				catalogrefDocumentationNamespace = new CatalogrefDocumentationNamespace(catalogrefDocumentationNamespaceId, namespace, catalogrefDocumentationId);
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
		return catalogrefDocumentationNamespace;
	}

	public static ArrayList<CatalogrefDocumentationNamespace> getCatalogrefDocumentationNamespaceBCatalogrefDocumentation(int catalogrefDocumentationId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> namespaceIds = new ArrayList<Integer>();
		ArrayList<CatalogrefDocumentationNamespace> namespaces = new ArrayList<CatalogrefDocumentationNamespace>();
		String select = "select catalogref_documentation_namespace_id from catalogref_documentation_namespace where ";
		select+= "catalogref_documentation_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, catalogrefDocumentationId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				namespaceIds.add(rs.getInt("namespace_id"));
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
		for(int i=0; i<namespaceIds.size(); i++){
			namespaces.add(getCatalogrefDocumentationNamespace(namespaceIds.get(i)));
		}
		return namespaces;
	}

	public static CatalogrefDocumentationXlink getCatalogrefDocumentationXlink(int catalogrefDocumentationXlinkId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		CatalogrefDocumentationXlink catalogrefDocumentationXlink = null;

		try {
			ps = pgCache.prepareStatement("select * from catalogref_documentation_xlink where catalogref_documentation_xlink_id=?");
			ps.setInt(1, catalogrefDocumentationXlinkId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String value = rs.getString("value");
				if (value == null)
					value = "";
				String xlink = rs.getString("xlink");
				if (xlink == null)
					xlink = rs.getString("xlink_nonstandard");
				if (xlink == null)
					xlink = "";
				int catalogrefDocumentationId = rs.getInt("catalogref_documentation_id");
				catalogrefDocumentationXlink = new CatalogrefDocumentationXlink(catalogrefDocumentationXlinkId, value, xlink, catalogrefDocumentationId);
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
		return catalogrefDocumentationXlink;
	}

	public static ArrayList<CatalogrefDocumentationXlink> getCatalogrefDocumentationXlinkBCatalogrefDocumentation(int catalogrefDocumentationId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> xlinkIds = new ArrayList<Integer>();
		ArrayList<CatalogrefDocumentationXlink> xlinks = new ArrayList<CatalogrefDocumentationXlink>();
		String select = "select catalogref_documentation_xlink_id from catalogref_documentation_xlink where ";
		select+= "catalogref_documentation_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, catalogrefDocumentationId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				xlinkIds.add(rs.getInt("xlink_id"));
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
		for(int i=0; i<xlinkIds.size(); i++){
			xlinks.add(getCatalogrefDocumentationXlink(xlinkIds.get(i)));
		}
		return xlinks;
	}

	public static CatalogrefXlink getCatalogrefXlink(int catalogrefXlinkId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		CatalogrefXlink catalogrefXlink = null;

		try {
			ps = pgCache.prepareStatement("select * from catalogref_xlink where catalogref_xlink_id=?");
			ps.setInt(1, catalogrefXlinkId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String value = rs.getString("value");
				if (value == null)
					value = "";
				String xlink = rs.getString("xlink");
				if (xlink == null)
					xlink = rs.getString("xlink_nonstandard");
				if (xlink == null)
					xlink = "";
				int catalogrefId = rs.getInt("catalogref_id");
				catalogrefXlink = new CatalogrefXlink(catalogrefXlinkId, value, xlink, catalogrefId);
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
		return catalogrefXlink;
	}

	public static ArrayList<CatalogrefXlink> getCatalogrefXlinkBCatalogref(int catalogrefId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> xlinkIds = new ArrayList<Integer>();
		ArrayList<CatalogrefXlink> xlinks = new ArrayList<CatalogrefXlink>();
		String select = "select catalogref_xlink_id from catalogref_xlink where ";
		select+= "catalogref_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, catalogrefId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				xlinkIds.add(rs.getInt("xlink_id"));
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
		for(int i=0; i<xlinkIds.size(); i++){
			xlinks.add(getCatalogrefXlink(xlinkIds.get(i)));
		}
		return xlinks;
	}

	public static Dataset getDataset(int datasetId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		Dataset dataset = null;

		try {
			ps = pgCache.prepareStatement("select * from dataset where dataset_id=?");
			ps.setInt(1, datasetId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String harvest = rs.getString("harvest");
				if (harvest == null)
					harvest = "";
				String name = rs.getString("name");
				if (name == null)
					name = "";
				String alias = rs.getString("alias");
				if (alias == null)
					alias = "";
				String authority = rs.getString("authority");
				if (authority == null)
					authority = "";
				String dId = rs.getString("d_id");
				if (dId == null)
					dId = "";
				String servicename = rs.getString("servicename");
				if (servicename == null)
					servicename = "";
				String urlpath = rs.getString("urlpath");
				if (urlpath == null)
					urlpath = "";
				String resourcecontrol = rs.getString("resourcecontrol");
				if (resourcecontrol == null)
					resourcecontrol = "";
				String collectiontype = rs.getString("collectiontype");
				if (collectiontype == null)
					collectiontype = rs.getString("collectiontype_nonstandard");
				if (collectiontype == null)
					collectiontype = "";
				String status = rs.getString("status");
				if (status == null)
					status = "";
				String datatype = rs.getString("datatype");
				if (datatype == null)
					datatype = rs.getString("datatype_nonstandard");
				if (datatype == null)
					datatype = "";
				String datasizeUnit = rs.getString("datasize_unit");
				if (datasizeUnit == null)
					datasizeUnit = rs.getString("datasize_unit_nonstandard");
				if (datasizeUnit == null)
					datasizeUnit = "";
				dataset = new Dataset(datasetId, harvest, name, alias, authority, dId, servicename, urlpath, resourcecontrol, collectiontype, status, datatype, datasizeUnit);
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
		return dataset;
	}

	public static DatasetAccess getDatasetAccess(int datasetAccessId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		DatasetAccess datasetAccess = null;

		try {
			ps = pgCache.prepareStatement("select * from dataset_access where dataset_access_id=?");
			ps.setInt(1, datasetAccessId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String urlpath = rs.getString("urlpath");
				if (urlpath == null)
					urlpath = "";
				String servicename = rs.getString("servicename");
				if (servicename == null)
					servicename = "";
				String dataformat = rs.getString("dataformat");
				if (dataformat == null)
					dataformat = rs.getString("dataformat_nonstandard");
				if (dataformat == null)
					dataformat = "";
				int datasetId = rs.getInt("dataset_id");
				datasetAccess = new DatasetAccess(datasetAccessId, urlpath, servicename, dataformat, datasetId);
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
		return datasetAccess;
	}

	public static ArrayList<DatasetAccess> getDatasetAccessBDataset(int datasetId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> accessIds = new ArrayList<Integer>();
		ArrayList<DatasetAccess> accesss = new ArrayList<DatasetAccess>();
		String select = "select dataset_access_id from dataset_access where ";
		select+= "dataset_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, datasetId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				accessIds.add(rs.getInt("access_id"));
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
		for(int i=0; i<accessIds.size(); i++){
			accesss.add(getDatasetAccess(accessIds.get(i)));
		}
		return accesss;
	}

	public static DatasetAccessDatasize getDatasetAccessDatasize(int datasetAccessDatasizeId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		DatasetAccessDatasize datasetAccessDatasize = null;

		try {
			ps = pgCache.prepareStatement("select * from dataset_access_datasize where dataset_access_datasize_id=?");
			ps.setInt(1, datasetAccessDatasizeId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String value = rs.getString("value");
				if (value == null)
					value = "";
				String units = rs.getString("units");
				if (units == null)
					units = rs.getString("units_nonstandard");
				if (units == null)
					units = "";
				int datasetAccessId = rs.getInt("dataset_access_id");
				datasetAccessDatasize = new DatasetAccessDatasize(datasetAccessDatasizeId, value, units, datasetAccessId);
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
		return datasetAccessDatasize;
	}

	public static ArrayList<DatasetAccessDatasize> getDatasetAccessDatasizeBDatasetAccess(int datasetAccessId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> datasizeIds = new ArrayList<Integer>();
		ArrayList<DatasetAccessDatasize> datasizes = new ArrayList<DatasetAccessDatasize>();
		String select = "select dataset_access_datasize_id from dataset_access_datasize where ";
		select+= "dataset_access_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, datasetAccessId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				datasizeIds.add(rs.getInt("datasize_id"));
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
		for(int i=0; i<datasizeIds.size(); i++){
			datasizes.add(getDatasetAccessDatasize(datasizeIds.get(i)));
		}
		return datasizes;
	}

	public static ArrayList<Catalogref> getCatalogrefBDataset(int datasetId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> catalogrefIds = new ArrayList<Integer>();
		ArrayList<Catalogref> catalogrefs = new ArrayList<Catalogref>();
		String select = "select catalogref_id from dataset_catalogref where ";
		select+= "dataset_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, datasetId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				catalogrefIds.add(rs.getInt("catalogref_id"));
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
		for(int i=0; i<catalogrefIds.size(); i++){
			catalogrefs.add(getCatalogref(catalogrefIds.get(i)));
		}
		return catalogrefs;
	}

	public static ArrayList<Dataset> getDatasetBDataset(int datasetId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> datasetIds = new ArrayList<Integer>();
		ArrayList<Dataset> datasets = new ArrayList<Dataset>();
		String select = "select child_id from dataset_dataset where ";
		select+= "parent_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, datasetId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				datasetIds.add(rs.getInt("child_id"));
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
		for(int i=0; i<datasetIds.size(); i++){
			datasets.add(getDataset(datasetIds.get(i)));
		}
		return datasets;
	}

	public static DatasetNcml getDatasetNcml(int datasetNcmlId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		DatasetNcml datasetNcml = null;

		try {
			ps = pgCache.prepareStatement("select * from dataset_ncml where dataset_ncml_id=?");
			ps.setInt(1, datasetNcmlId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				int datasetId = rs.getInt("dataset_id");
				datasetNcml = new DatasetNcml(datasetNcmlId, datasetId);
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
		return datasetNcml;
	}

	public static ArrayList<DatasetNcml> getDatasetNcmlBDataset(int datasetId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> ncmlIds = new ArrayList<Integer>();
		ArrayList<DatasetNcml> ncmls = new ArrayList<DatasetNcml>();
		String select = "select dataset_ncml_id from dataset_ncml where ";
		select+= "dataset_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, datasetId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				ncmlIds.add(rs.getInt("ncml_id"));
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
		for(int i=0; i<ncmlIds.size(); i++){
			ncmls.add(getDatasetNcml(ncmlIds.get(i)));
		}
		return ncmls;
	}

	public static DatasetProperty getDatasetProperty(int datasetPropertyId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		DatasetProperty datasetProperty = null;

		try {
			ps = pgCache.prepareStatement("select * from dataset_property where dataset_property_id=?");
			ps.setInt(1, datasetPropertyId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String name = rs.getString("name");
				if (name == null)
					name = "";
				String value = rs.getString("value");
				if (value == null)
					value = "";
				int datasetId = rs.getInt("dataset_id");
				datasetProperty = new DatasetProperty(datasetPropertyId, name, value, datasetId);
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
		return datasetProperty;
	}

	public static ArrayList<DatasetProperty> getDatasetPropertyBDataset(int datasetId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> propertyIds = new ArrayList<Integer>();
		ArrayList<DatasetProperty> propertys = new ArrayList<DatasetProperty>();
		String select = "select dataset_property_id from dataset_property where ";
		select+= "dataset_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, datasetId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				propertyIds.add(rs.getInt("property_id"));
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
		for(int i=0; i<propertyIds.size(); i++){
			propertys.add(getDatasetProperty(propertyIds.get(i)));
		}
		return propertys;
	}

	public static ArrayList<Service> getServiceBDataset(int datasetId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> serviceIds = new ArrayList<Integer>();
		ArrayList<Service> services = new ArrayList<Service>();
		String select = "select service_id from dataset_service where ";
		select+= "dataset_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, datasetId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				serviceIds.add(rs.getInt("service_id"));
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
		for(int i=0; i<serviceIds.size(); i++){
			services.add(getService(serviceIds.get(i)));
		}
		return services;
	}

	public static ArrayList<Tmg> getTmgBDataset(int datasetId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> tmgIds = new ArrayList<Integer>();
		ArrayList<Tmg> tmgs = new ArrayList<Tmg>();
		String select = "select tmg_id from dataset_tmg where ";
		select+= "dataset_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, datasetId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				tmgIds.add(rs.getInt("tmg_id"));
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
		for(int i=0; i<tmgIds.size(); i++){
			tmgs.add(getTmg(tmgIds.get(i)));
		}
		return tmgs;
	}

	public static Metadata getMetadata(int metadataId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		Metadata metadata = null;

		try {
			ps = pgCache.prepareStatement("select * from metadata where metadata_id=?");
			ps.setInt(1, metadataId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String metadatatype = rs.getString("metadatatype");
				if (metadatatype == null)
					metadatatype = rs.getString("metadatatype_nonstandard");
				if (metadatatype == null)
					metadatatype = "";
				String inherited = rs.getString("inherited");
				if (inherited == null)
					inherited = rs.getString("inherited_nonstandard");
				if (inherited == null)
					inherited = "";
				metadata = new Metadata(metadataId, metadatatype, inherited);
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
		return metadata;
	}

	public static MetadataNamespace getMetadataNamespace(int metadataNamespaceId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		MetadataNamespace metadataNamespace = null;

		try {
			ps = pgCache.prepareStatement("select * from metadata_namespace where metadata_namespace_id=?");
			ps.setInt(1, metadataNamespaceId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String namespace = rs.getString("namespace");
				if (namespace == null)
					namespace = "";
				int metadataId = rs.getInt("metadata_id");
				metadataNamespace = new MetadataNamespace(metadataNamespaceId, namespace, metadataId);
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
		return metadataNamespace;
	}

	public static ArrayList<MetadataNamespace> getMetadataNamespaceBMetadata(int metadataId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> namespaceIds = new ArrayList<Integer>();
		ArrayList<MetadataNamespace> namespaces = new ArrayList<MetadataNamespace>();
		String select = "select metadata_namespace_id from metadata_namespace where ";
		select+= "metadata_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, metadataId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				namespaceIds.add(rs.getInt("namespace_id"));
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
		for(int i=0; i<namespaceIds.size(); i++){
			namespaces.add(getMetadataNamespace(namespaceIds.get(i)));
		}
		return namespaces;
	}

	public static ArrayList<Tmg> getTmgBMetadata(int metadataId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> tmgIds = new ArrayList<Integer>();
		ArrayList<Tmg> tmgs = new ArrayList<Tmg>();
		String select = "select tmg_id from metadata_tmg where ";
		select+= "metadata_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, metadataId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				tmgIds.add(rs.getInt("tmg_id"));
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
		for(int i=0; i<tmgIds.size(); i++){
			tmgs.add(getTmg(tmgIds.get(i)));
		}
		return tmgs;
	}

	public static MetadataXlink getMetadataXlink(int metadataXlinkId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		MetadataXlink metadataXlink = null;

		try {
			ps = pgCache.prepareStatement("select * from metadata_xlink where metadata_xlink_id=?");
			ps.setInt(1, metadataXlinkId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String value = rs.getString("value");
				if (value == null)
					value = "";
				String xlink = rs.getString("xlink");
				if (xlink == null)
					xlink = rs.getString("xlink_nonstandard");
				if (xlink == null)
					xlink = "";
				int metadataId = rs.getInt("metadata_id");
				metadataXlink = new MetadataXlink(metadataXlinkId, value, xlink, metadataId);
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
		return metadataXlink;
	}

	public static ArrayList<MetadataXlink> getMetadataXlinkBMetadata(int metadataId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> xlinkIds = new ArrayList<Integer>();
		ArrayList<MetadataXlink> xlinks = new ArrayList<MetadataXlink>();
		String select = "select metadata_xlink_id from metadata_xlink where ";
		select+= "metadata_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, metadataId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				xlinkIds.add(rs.getInt("xlink_id"));
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
		for(int i=0; i<xlinkIds.size(); i++){
			xlinks.add(getMetadataXlink(xlinkIds.get(i)));
		}
		return xlinks;
	}

	public static Service getService(int serviceId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		Service service = null;

		try {
			ps = pgCache.prepareStatement("select * from service where service_id=?");
			ps.setInt(1, serviceId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String suffix = rs.getString("suffix");
				if (suffix == null)
					suffix = "";
				String name = rs.getString("name");
				if (name == null)
					name = "";
				String base = rs.getString("base");
				if (base == null)
					base = "";
				String desc = rs.getString("desc");
				if (desc == null)
					desc = "";
				String servicetype = rs.getString("servicetype");
				if (servicetype == null)
					servicetype = rs.getString("servicetype_nonstandard");
				if (servicetype == null)
					servicetype = "";
				String status = rs.getString("status");
				if (status == null)
					status = "";
				service = new Service(serviceId, suffix, name, base, desc, servicetype, status);
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
		return service;
	}

	public static ServiceDatasetroot getServiceDatasetroot(int serviceDatasetrootId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		ServiceDatasetroot serviceDatasetroot = null;

		try {
			ps = pgCache.prepareStatement("select * from service_datasetroot where service_datasetroot_id=?");
			ps.setInt(1, serviceDatasetrootId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String path = rs.getString("path");
				if (path == null)
					path = "";
				String location = rs.getString("location");
				if (location == null)
					location = "";
				int serviceId = rs.getInt("service_id");
				serviceDatasetroot = new ServiceDatasetroot(serviceDatasetrootId, path, location, serviceId);
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
		return serviceDatasetroot;
	}

	public static ArrayList<ServiceDatasetroot> getServiceDatasetrootBService(int serviceId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> datasetrootIds = new ArrayList<Integer>();
		ArrayList<ServiceDatasetroot> datasetroots = new ArrayList<ServiceDatasetroot>();
		String select = "select service_datasetroot_id from service_datasetroot where ";
		select+= "service_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, serviceId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				datasetrootIds.add(rs.getInt("datasetroot_id"));
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
		for(int i=0; i<datasetrootIds.size(); i++){
			datasetroots.add(getServiceDatasetroot(datasetrootIds.get(i)));
		}
		return datasetroots;
	}

	public static ServiceProperty getServiceProperty(int servicePropertyId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		ServiceProperty serviceProperty = null;

		try {
			ps = pgCache.prepareStatement("select * from service_property where service_property_id=?");
			ps.setInt(1, servicePropertyId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String value = rs.getString("value");
				if (value == null)
					value = "";
				String name = rs.getString("name");
				if (name == null)
					name = "";
				int serviceId = rs.getInt("service_id");
				serviceProperty = new ServiceProperty(servicePropertyId, value, name, serviceId);
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
		return serviceProperty;
	}

	public static ArrayList<ServiceProperty> getServicePropertyBService(int serviceId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> propertyIds = new ArrayList<Integer>();
		ArrayList<ServiceProperty> propertys = new ArrayList<ServiceProperty>();
		String select = "select service_property_id from service_property where ";
		select+= "service_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, serviceId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				propertyIds.add(rs.getInt("property_id"));
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
		for(int i=0; i<propertyIds.size(); i++){
			propertys.add(getServiceProperty(propertyIds.get(i)));
		}
		return propertys;
	}

	public static ArrayList<Service> getServiceBService(int serviceId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> serviceIds = new ArrayList<Integer>();
		ArrayList<Service> services = new ArrayList<Service>();
		String select = "select child_id from service_service where ";
		select+= "parent_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, serviceId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				serviceIds.add(rs.getInt("child_id"));
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
		for(int i=0; i<serviceIds.size(); i++){
			services.add(getService(serviceIds.get(i)));
		}
		return services;
	}

	public static Tmg getTmg(int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		Tmg tmg = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg where tmg_id=?");
			ps.setInt(1, tmgId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				tmg = new Tmg(tmgId);
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
		return tmg;
	}

	public static TmgAuthority getTmgAuthority(int tmgAuthorityId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgAuthority tmgAuthority = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_authority where tmg_authority_id=?");
			ps.setInt(1, tmgAuthorityId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String authority = rs.getString("authority");
				if (authority == null)
					authority = "";
				int tmgId = rs.getInt("tmg_id");
				tmgAuthority = new TmgAuthority(tmgAuthorityId, authority, tmgId);
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
		return tmgAuthority;
	}

	public static ArrayList<TmgAuthority> getTmgAuthorityBTmg(int tmgId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> authorityIds = new ArrayList<Integer>();
		ArrayList<TmgAuthority> authoritys = new ArrayList<TmgAuthority>();
		String select = "select tmg_authority_id from tmg_authority where ";
		select+= "tmg_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				authorityIds.add(rs.getInt("authority_id"));
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
		for(int i=0; i<authorityIds.size(); i++){
			authoritys.add(getTmgAuthority(authorityIds.get(i)));
		}
		return authoritys;
	}

	public static TmgContributor getTmgContributor(int tmgContributorId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgContributor tmgContributor = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_contributor where tmg_contributor_id=?");
			ps.setInt(1, tmgContributorId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String role = rs.getString("role");
				if (role == null)
					role = "";
				String name = rs.getString("name");
				if (name == null)
					name = "";
				int tmgId = rs.getInt("tmg_id");
				tmgContributor = new TmgContributor(tmgContributorId, role, name, tmgId);
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
		return tmgContributor;
	}

	public static ArrayList<TmgContributor> getTmgContributorBTmg(int tmgId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> contributorIds = new ArrayList<Integer>();
		ArrayList<TmgContributor> contributors = new ArrayList<TmgContributor>();
		String select = "select tmg_contributor_id from tmg_contributor where ";
		select+= "tmg_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				contributorIds.add(rs.getInt("contributor_id"));
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
		for(int i=0; i<contributorIds.size(); i++){
			contributors.add(getTmgContributor(contributorIds.get(i)));
		}
		return contributors;
	}

	public static TmgCreator getTmgCreator(int tmgCreatorId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgCreator tmgCreator = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_creator where tmg_creator_id=?");
			ps.setInt(1, tmgCreatorId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				int tmgId = rs.getInt("tmg_id");
				tmgCreator = new TmgCreator(tmgCreatorId, tmgId);
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
		return tmgCreator;
	}

	public static ArrayList<TmgCreator> getTmgCreatorBTmg(int tmgId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> creatorIds = new ArrayList<Integer>();
		ArrayList<TmgCreator> creators = new ArrayList<TmgCreator>();
		String select = "select tmg_creator_id from tmg_creator where ";
		select+= "tmg_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				creatorIds.add(rs.getInt("creator_id"));
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
		for(int i=0; i<creatorIds.size(); i++){
			creators.add(getTmgCreator(creatorIds.get(i)));
		}
		return creators;
	}

	public static TmgCreatorContact getTmgCreatorContact(int tmgCreatorContactId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgCreatorContact tmgCreatorContact = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_creator_contact where tmg_creator_contact_id=?");
			ps.setInt(1, tmgCreatorContactId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String email = rs.getString("email");
				if (email == null)
					email = "";
				String url = rs.getString("url");
				if (url == null)
					url = "";
				int tmgCreatorId = rs.getInt("tmg_creator_id");
				tmgCreatorContact = new TmgCreatorContact(tmgCreatorContactId, email, url, tmgCreatorId);
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
		return tmgCreatorContact;
	}

	public static ArrayList<TmgCreatorContact> getTmgCreatorContactBTmgCreator(int tmgCreatorId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> contactIds = new ArrayList<Integer>();
		ArrayList<TmgCreatorContact> contacts = new ArrayList<TmgCreatorContact>();
		String select = "select tmg_creator_contact_id from tmg_creator_contact where ";
		select+= "tmg_creator_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgCreatorId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				contactIds.add(rs.getInt("contact_id"));
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
		for(int i=0; i<contactIds.size(); i++){
			contacts.add(getTmgCreatorContact(contactIds.get(i)));
		}
		return contacts;
	}

	public static TmgCreatorName getTmgCreatorName(int tmgCreatorNameId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgCreatorName tmgCreatorName = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_creator_name where tmg_creator_name_id=?");
			ps.setInt(1, tmgCreatorNameId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String value = rs.getString("value");
				if (value == null)
					value = "";
				String vocabulary = rs.getString("vocabulary");
				if (vocabulary == null)
					vocabulary = "";
				int tmgCreatorId = rs.getInt("tmg_creator_id");
				tmgCreatorName = new TmgCreatorName(tmgCreatorNameId, value, vocabulary, tmgCreatorId);
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
		return tmgCreatorName;
	}

	public static ArrayList<TmgCreatorName> getTmgCreatorNameBTmgCreator(int tmgCreatorId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> nameIds = new ArrayList<Integer>();
		ArrayList<TmgCreatorName> names = new ArrayList<TmgCreatorName>();
		String select = "select tmg_creator_name_id from tmg_creator_name where ";
		select+= "tmg_creator_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgCreatorId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				nameIds.add(rs.getInt("name_id"));
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
		for(int i=0; i<nameIds.size(); i++){
			names.add(getTmgCreatorName(nameIds.get(i)));
		}
		return names;
	}

	public static TmgDataformat getTmgDataformat(int tmgDataformatId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgDataformat tmgDataformat = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_dataformat where tmg_dataformat_id=?");
			ps.setInt(1, tmgDataformatId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String dataformat = rs.getString("dataformat");
				if (dataformat == null)
					dataformat = rs.getString("dataformat_nonstandard");
				if (dataformat == null)
					dataformat = "";
				int tmgId = rs.getInt("tmg_id");
				tmgDataformat = new TmgDataformat(tmgDataformatId, dataformat, tmgId);
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
		return tmgDataformat;
	}

	public static ArrayList<TmgDataformat> getTmgDataformatBTmg(int tmgId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> dataformatIds = new ArrayList<Integer>();
		ArrayList<TmgDataformat> dataformats = new ArrayList<TmgDataformat>();
		String select = "select tmg_dataformat_id from tmg_dataformat where ";
		select+= "tmg_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				dataformatIds.add(rs.getInt("dataformat_id"));
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
		for(int i=0; i<dataformatIds.size(); i++){
			dataformats.add(getTmgDataformat(dataformatIds.get(i)));
		}
		return dataformats;
	}

	public static TmgDatasize getTmgDatasize(int tmgDatasizeId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgDatasize tmgDatasize = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_datasize where tmg_datasize_id=?");
			ps.setInt(1, tmgDatasizeId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String value = rs.getString("value");
				if (value == null)
					value = "";
				String units = rs.getString("units");
				if (units == null)
					units = rs.getString("units_nonstandard");
				if (units == null)
					units = "";
				int tmgId = rs.getInt("tmg_id");
				tmgDatasize = new TmgDatasize(tmgDatasizeId, value, units, tmgId);
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
		return tmgDatasize;
	}

	public static ArrayList<TmgDatasize> getTmgDatasizeBTmg(int tmgId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> datasizeIds = new ArrayList<Integer>();
		ArrayList<TmgDatasize> datasizes = new ArrayList<TmgDatasize>();
		String select = "select tmg_datasize_id from tmg_datasize where ";
		select+= "tmg_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				datasizeIds.add(rs.getInt("datasize_id"));
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
		for(int i=0; i<datasizeIds.size(); i++){
			datasizes.add(getTmgDatasize(datasizeIds.get(i)));
		}
		return datasizes;
	}

	public static TmgDatatype getTmgDatatype(int tmgDatatypeId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgDatatype tmgDatatype = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_datatype where tmg_datatype_id=?");
			ps.setInt(1, tmgDatatypeId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String datatype = rs.getString("datatype");
				if (datatype == null)
					datatype = rs.getString("datatype_nonstandard");
				if (datatype == null)
					datatype = "";
				int tmgId = rs.getInt("tmg_id");
				tmgDatatype = new TmgDatatype(tmgDatatypeId, datatype, tmgId);
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
		return tmgDatatype;
	}

	public static ArrayList<TmgDatatype> getTmgDatatypeBTmg(int tmgId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> datatypeIds = new ArrayList<Integer>();
		ArrayList<TmgDatatype> datatypes = new ArrayList<TmgDatatype>();
		String select = "select tmg_datatype_id from tmg_datatype where ";
		select+= "tmg_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				datatypeIds.add(rs.getInt("datatype_id"));
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
		for(int i=0; i<datatypeIds.size(); i++){
			datatypes.add(getTmgDatatype(datatypeIds.get(i)));
		}
		return datatypes;
	}

	public static TmgDate getTmgDate(int tmgDateId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgDate tmgDate = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_date where tmg_date_id=?");
			ps.setInt(1, tmgDateId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String format = rs.getString("format");
				if (format == null)
					format = "";
				String value = rs.getString("value");
				if (value == null)
					value = "";
				String dateenum = rs.getString("dateenum");
				if (dateenum == null)
					dateenum = rs.getString("dateenum_nonstandard");
				if (dateenum == null)
					dateenum = "";
				int tmgId = rs.getInt("tmg_id");
				tmgDate = new TmgDate(tmgDateId, format, value, dateenum, tmgId);
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
		return tmgDate;
	}

	public static ArrayList<TmgDate> getTmgDateBTmg(int tmgId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> dateIds = new ArrayList<Integer>();
		ArrayList<TmgDate> dates = new ArrayList<TmgDate>();
		String select = "select tmg_date_id from tmg_date where ";
		select+= "tmg_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				dateIds.add(rs.getInt("date_id"));
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
		for(int i=0; i<dateIds.size(); i++){
			dates.add(getTmgDate(dateIds.get(i)));
		}
		return dates;
	}

	public static TmgDocumentation getTmgDocumentation(int tmgDocumentationId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgDocumentation tmgDocumentation = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_documentation where tmg_documentation_id=?");
			ps.setInt(1, tmgDocumentationId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String value = rs.getString("value");
				if (value == null)
					value = "";
				String documentationenum = rs.getString("documentationenum");
				if (documentationenum == null)
					documentationenum = rs.getString("documentationenum_nonstandard");
				if (documentationenum == null)
					documentationenum = "";
				int tmgId = rs.getInt("tmg_id");
				tmgDocumentation = new TmgDocumentation(tmgDocumentationId, value, documentationenum, tmgId);
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
		return tmgDocumentation;
	}

	public static ArrayList<TmgDocumentation> getTmgDocumentationBTmg(int tmgId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> documentationIds = new ArrayList<Integer>();
		ArrayList<TmgDocumentation> documentations = new ArrayList<TmgDocumentation>();
		String select = "select tmg_documentation_id from tmg_documentation where ";
		select+= "tmg_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				documentationIds.add(rs.getInt("documentation_id"));
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
		for(int i=0; i<documentationIds.size(); i++){
			documentations.add(getTmgDocumentation(documentationIds.get(i)));
		}
		return documentations;
	}

	public static TmgDocumentationNamespace getTmgDocumentationNamespace(int tmgDocumentationNamespaceId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgDocumentationNamespace tmgDocumentationNamespace = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_documentation_namespace where tmg_documentation_namespace_id=?");
			ps.setInt(1, tmgDocumentationNamespaceId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String namespace = rs.getString("namespace");
				if (namespace == null)
					namespace = "";
				int tmgDocumentationId = rs.getInt("tmg_documentation_id");
				tmgDocumentationNamespace = new TmgDocumentationNamespace(tmgDocumentationNamespaceId, namespace, tmgDocumentationId);
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
		return tmgDocumentationNamespace;
	}

	public static ArrayList<TmgDocumentationNamespace> getTmgDocumentationNamespaceBTmgDocumentation(int tmgDocumentationId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> namespaceIds = new ArrayList<Integer>();
		ArrayList<TmgDocumentationNamespace> namespaces = new ArrayList<TmgDocumentationNamespace>();
		String select = "select tmg_documentation_namespace_id from tmg_documentation_namespace where ";
		select+= "tmg_documentation_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgDocumentationId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				namespaceIds.add(rs.getInt("namespace_id"));
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
		for(int i=0; i<namespaceIds.size(); i++){
			namespaces.add(getTmgDocumentationNamespace(namespaceIds.get(i)));
		}
		return namespaces;
	}

	public static TmgDocumentationXlink getTmgDocumentationXlink(int tmgDocumentationXlinkId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgDocumentationXlink tmgDocumentationXlink = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_documentation_xlink where tmg_documentation_xlink_id=?");
			ps.setInt(1, tmgDocumentationXlinkId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String value = rs.getString("value");
				if (value == null)
					value = "";
				String xlink = rs.getString("xlink");
				if (xlink == null)
					xlink = rs.getString("xlink_nonstandard");
				if (xlink == null)
					xlink = "";
				int tmgDocumentationId = rs.getInt("tmg_documentation_id");
				tmgDocumentationXlink = new TmgDocumentationXlink(tmgDocumentationXlinkId, value, xlink, tmgDocumentationId);
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
		return tmgDocumentationXlink;
	}

	public static ArrayList<TmgDocumentationXlink> getTmgDocumentationXlinkBTmgDocumentation(int tmgDocumentationId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> xlinkIds = new ArrayList<Integer>();
		ArrayList<TmgDocumentationXlink> xlinks = new ArrayList<TmgDocumentationXlink>();
		String select = "select tmg_documentation_xlink_id from tmg_documentation_xlink where ";
		select+= "tmg_documentation_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgDocumentationId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				xlinkIds.add(rs.getInt("xlink_id"));
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
		for(int i=0; i<xlinkIds.size(); i++){
			xlinks.add(getTmgDocumentationXlink(xlinkIds.get(i)));
		}
		return xlinks;
	}

	public static TmgGeospatialcoverage getTmgGeospatialcoverage(int tmgGeospatialcoverageId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgGeospatialcoverage tmgGeospatialcoverage = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_geospatialcoverage where tmg_geospatialcoverage_id=?");
			ps.setInt(1, tmgGeospatialcoverageId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String upordown = rs.getString("upordown");
				if (upordown == null)
					upordown = rs.getString("upordown_nonstandard");
				if (upordown == null)
					upordown = "";
				int tmgId = rs.getInt("tmg_id");
				tmgGeospatialcoverage = new TmgGeospatialcoverage(tmgGeospatialcoverageId, upordown, tmgId);
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
		return tmgGeospatialcoverage;
	}

	public static ArrayList<TmgGeospatialcoverage> getTmgGeospatialcoverageBTmg(int tmgId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> geospatialcoverageIds = new ArrayList<Integer>();
		ArrayList<TmgGeospatialcoverage> geospatialcoverages = new ArrayList<TmgGeospatialcoverage>();
		String select = "select tmg_geospatialcoverage_id from tmg_geospatialcoverage where ";
		select+= "tmg_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				geospatialcoverageIds.add(rs.getInt("geospatialcoverage_id"));
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
		for(int i=0; i<geospatialcoverageIds.size(); i++){
			geospatialcoverages.add(getTmgGeospatialcoverage(geospatialcoverageIds.get(i)));
		}
		return geospatialcoverages;
	}

	public static TmgGeospatialcoverageEastwest getTmgGeospatialcoverageEastwest(int tmgGeospatialcoverageEastwestId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgGeospatialcoverageEastwest tmgGeospatialcoverageEastwest = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_geospatialcoverage_eastwest where tmg_geospatialcoverage_eastwest_id=?");
			ps.setInt(1, tmgGeospatialcoverageEastwestId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String size = rs.getString("size");
				if (size == null)
					size = "";
				String units = rs.getString("units");
				if (units == null)
					units = "";
				String start = rs.getString("start");
				if (start == null)
					start = "";
				String resolution = rs.getString("resolution");
				if (resolution == null)
					resolution = "";
				int tmgGeospatialcoverageId = rs.getInt("tmg_geospatialcoverage_id");
				tmgGeospatialcoverageEastwest = new TmgGeospatialcoverageEastwest(tmgGeospatialcoverageEastwestId, size, units, start, resolution, tmgGeospatialcoverageId);
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
		return tmgGeospatialcoverageEastwest;
	}

	public static ArrayList<TmgGeospatialcoverageEastwest> getTmgGeospatialcoverageEastwestBTmgGeospatialcoverage(int tmgGeospatialcoverageId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> eastwestIds = new ArrayList<Integer>();
		ArrayList<TmgGeospatialcoverageEastwest> eastwests = new ArrayList<TmgGeospatialcoverageEastwest>();
		String select = "select tmg_geospatialcoverage_eastwest_id from tmg_geospatialcoverage_eastwest where ";
		select+= "tmg_geospatialcoverage_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgGeospatialcoverageId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				eastwestIds.add(rs.getInt("eastwest_id"));
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
		for(int i=0; i<eastwestIds.size(); i++){
			eastwests.add(getTmgGeospatialcoverageEastwest(eastwestIds.get(i)));
		}
		return eastwests;
	}

	public static TmgGeospatialcoverageName getTmgGeospatialcoverageName(int tmgGeospatialcoverageNameId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgGeospatialcoverageName tmgGeospatialcoverageName = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_geospatialcoverage_name where tmg_geospatialcoverage_name_id=?");
			ps.setInt(1, tmgGeospatialcoverageNameId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String vocabulary = rs.getString("vocabulary");
				if (vocabulary == null)
					vocabulary = "";
				String value = rs.getString("value");
				if (value == null)
					value = "";
				int tmgGeospatialcoverageId = rs.getInt("tmg_geospatialcoverage_id");
				tmgGeospatialcoverageName = new TmgGeospatialcoverageName(tmgGeospatialcoverageNameId, vocabulary, value, tmgGeospatialcoverageId);
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
		return tmgGeospatialcoverageName;
	}

	public static ArrayList<TmgGeospatialcoverageName> getTmgGeospatialcoverageNameBTmgGeospatialcoverage(int tmgGeospatialcoverageId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> nameIds = new ArrayList<Integer>();
		ArrayList<TmgGeospatialcoverageName> names = new ArrayList<TmgGeospatialcoverageName>();
		String select = "select tmg_geospatialcoverage_name_id from tmg_geospatialcoverage_name where ";
		select+= "tmg_geospatialcoverage_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgGeospatialcoverageId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				nameIds.add(rs.getInt("name_id"));
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
		for(int i=0; i<nameIds.size(); i++){
			names.add(getTmgGeospatialcoverageName(nameIds.get(i)));
		}
		return names;
	}

	public static TmgGeospatialcoverageNorthsouth getTmgGeospatialcoverageNorthsouth(int tmgGeospatialcoverageNorthsouthId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgGeospatialcoverageNorthsouth tmgGeospatialcoverageNorthsouth = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_geospatialcoverage_northsouth where tmg_geospatialcoverage_northsouth_id=?");
			ps.setInt(1, tmgGeospatialcoverageNorthsouthId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String size = rs.getString("size");
				if (size == null)
					size = "";
				String resolution = rs.getString("resolution");
				if (resolution == null)
					resolution = "";
				String start = rs.getString("start");
				if (start == null)
					start = "";
				String units = rs.getString("units");
				if (units == null)
					units = "";
				int tmgGeospatialcoverageId = rs.getInt("tmg_geospatialcoverage_id");
				tmgGeospatialcoverageNorthsouth = new TmgGeospatialcoverageNorthsouth(tmgGeospatialcoverageNorthsouthId, size, resolution, start, units, tmgGeospatialcoverageId);
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
		return tmgGeospatialcoverageNorthsouth;
	}

	public static ArrayList<TmgGeospatialcoverageNorthsouth> getTmgGeospatialcoverageNorthsouthBTmgGeospatialcoverage(int tmgGeospatialcoverageId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> northsouthIds = new ArrayList<Integer>();
		ArrayList<TmgGeospatialcoverageNorthsouth> northsouths = new ArrayList<TmgGeospatialcoverageNorthsouth>();
		String select = "select tmg_geospatialcoverage_northsouth_id from tmg_geospatialcoverage_northsouth where ";
		select+= "tmg_geospatialcoverage_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgGeospatialcoverageId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				northsouthIds.add(rs.getInt("northsouth_id"));
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
		for(int i=0; i<northsouthIds.size(); i++){
			northsouths.add(getTmgGeospatialcoverageNorthsouth(northsouthIds.get(i)));
		}
		return northsouths;
	}

	public static TmgGeospatialcoverageUpdown getTmgGeospatialcoverageUpdown(int tmgGeospatialcoverageUpdownId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgGeospatialcoverageUpdown tmgGeospatialcoverageUpdown = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_geospatialcoverage_updown where tmg_geospatialcoverage_updown_id=?");
			ps.setInt(1, tmgGeospatialcoverageUpdownId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String start = rs.getString("start");
				if (start == null)
					start = "";
				String resolution = rs.getString("resolution");
				if (resolution == null)
					resolution = "";
				String size = rs.getString("size");
				if (size == null)
					size = "";
				String units = rs.getString("units");
				if (units == null)
					units = "";
				int tmgGeospatialcoverageId = rs.getInt("tmg_geospatialcoverage_id");
				tmgGeospatialcoverageUpdown = new TmgGeospatialcoverageUpdown(tmgGeospatialcoverageUpdownId, start, resolution, size, units, tmgGeospatialcoverageId);
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
		return tmgGeospatialcoverageUpdown;
	}

	public static ArrayList<TmgGeospatialcoverageUpdown> getTmgGeospatialcoverageUpdownBTmgGeospatialcoverage(int tmgGeospatialcoverageId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> updownIds = new ArrayList<Integer>();
		ArrayList<TmgGeospatialcoverageUpdown> updowns = new ArrayList<TmgGeospatialcoverageUpdown>();
		String select = "select tmg_geospatialcoverage_updown_id from tmg_geospatialcoverage_updown where ";
		select+= "tmg_geospatialcoverage_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgGeospatialcoverageId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				updownIds.add(rs.getInt("updown_id"));
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
		for(int i=0; i<updownIds.size(); i++){
			updowns.add(getTmgGeospatialcoverageUpdown(updownIds.get(i)));
		}
		return updowns;
	}

	public static TmgKeyword getTmgKeyword(int tmgKeywordId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgKeyword tmgKeyword = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_keyword where tmg_keyword_id=?");
			ps.setInt(1, tmgKeywordId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String value = rs.getString("value");
				if (value == null)
					value = "";
				String vocabulary = rs.getString("vocabulary");
				if (vocabulary == null)
					vocabulary = "";
				int tmgId = rs.getInt("tmg_id");
				tmgKeyword = new TmgKeyword(tmgKeywordId, value, vocabulary, tmgId);
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
		return tmgKeyword;
	}

	public static ArrayList<TmgKeyword> getTmgKeywordBTmg(int tmgId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> keywordIds = new ArrayList<Integer>();
		ArrayList<TmgKeyword> keywords = new ArrayList<TmgKeyword>();
		String select = "select tmg_keyword_id from tmg_keyword where ";
		select+= "tmg_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				keywordIds.add(rs.getInt("keyword_id"));
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
		for(int i=0; i<keywordIds.size(); i++){
			keywords.add(getTmgKeyword(keywordIds.get(i)));
		}
		return keywords;
	}

	public static ArrayList<Metadata> getMetadataBTmg(int tmgId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> metadataIds = new ArrayList<Integer>();
		ArrayList<Metadata> metadatas = new ArrayList<Metadata>();
		String select = "select metadata_id from tmg_metadata where ";
		select+= "tmg_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				metadataIds.add(rs.getInt("metadata_id"));
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
		for(int i=0; i<metadataIds.size(); i++){
			metadatas.add(getMetadata(metadataIds.get(i)));
		}
		return metadatas;
	}

	public static TmgProject getTmgProject(int tmgProjectId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgProject tmgProject = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_project where tmg_project_id=?");
			ps.setInt(1, tmgProjectId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String value = rs.getString("value");
				if (value == null)
					value = "";
				String vocabulary = rs.getString("vocabulary");
				if (vocabulary == null)
					vocabulary = "";
				int tmgId = rs.getInt("tmg_id");
				tmgProject = new TmgProject(tmgProjectId, value, vocabulary, tmgId);
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
		return tmgProject;
	}

	public static ArrayList<TmgProject> getTmgProjectBTmg(int tmgId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> projectIds = new ArrayList<Integer>();
		ArrayList<TmgProject> projects = new ArrayList<TmgProject>();
		String select = "select tmg_project_id from tmg_project where ";
		select+= "tmg_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				projectIds.add(rs.getInt("project_id"));
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
		for(int i=0; i<projectIds.size(); i++){
			projects.add(getTmgProject(projectIds.get(i)));
		}
		return projects;
	}

	public static TmgProperty getTmgProperty(int tmgPropertyId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgProperty tmgProperty = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_property where tmg_property_id=?");
			ps.setInt(1, tmgPropertyId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String name = rs.getString("name");
				if (name == null)
					name = "";
				String value = rs.getString("value");
				if (value == null)
					value = "";
				int tmgId = rs.getInt("tmg_id");
				tmgProperty = new TmgProperty(tmgPropertyId, name, value, tmgId);
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
		return tmgProperty;
	}

	public static ArrayList<TmgProperty> getTmgPropertyBTmg(int tmgId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> propertyIds = new ArrayList<Integer>();
		ArrayList<TmgProperty> propertys = new ArrayList<TmgProperty>();
		String select = "select tmg_property_id from tmg_property where ";
		select+= "tmg_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				propertyIds.add(rs.getInt("property_id"));
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
		for(int i=0; i<propertyIds.size(); i++){
			propertys.add(getTmgProperty(propertyIds.get(i)));
		}
		return propertys;
	}

	public static TmgPublisher getTmgPublisher(int tmgPublisherId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgPublisher tmgPublisher = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_publisher where tmg_publisher_id=?");
			ps.setInt(1, tmgPublisherId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				int tmgId = rs.getInt("tmg_id");
				tmgPublisher = new TmgPublisher(tmgPublisherId, tmgId);
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
		return tmgPublisher;
	}

	public static ArrayList<TmgPublisher> getTmgPublisherBTmg(int tmgId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> publisherIds = new ArrayList<Integer>();
		ArrayList<TmgPublisher> publishers = new ArrayList<TmgPublisher>();
		String select = "select tmg_publisher_id from tmg_publisher where ";
		select+= "tmg_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				publisherIds.add(rs.getInt("publisher_id"));
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
		for(int i=0; i<publisherIds.size(); i++){
			publishers.add(getTmgPublisher(publisherIds.get(i)));
		}
		return publishers;
	}

	public static TmgPublisherContact getTmgPublisherContact(int tmgPublisherContactId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgPublisherContact tmgPublisherContact = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_publisher_contact where tmg_publisher_contact_id=?");
			ps.setInt(1, tmgPublisherContactId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String url = rs.getString("url");
				if (url == null)
					url = "";
				String email = rs.getString("email");
				if (email == null)
					email = "";
				int tmgPublisherId = rs.getInt("tmg_publisher_id");
				tmgPublisherContact = new TmgPublisherContact(tmgPublisherContactId, url, email, tmgPublisherId);
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
		return tmgPublisherContact;
	}

	public static ArrayList<TmgPublisherContact> getTmgPublisherContactBTmgPublisher(int tmgPublisherId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> contactIds = new ArrayList<Integer>();
		ArrayList<TmgPublisherContact> contacts = new ArrayList<TmgPublisherContact>();
		String select = "select tmg_publisher_contact_id from tmg_publisher_contact where ";
		select+= "tmg_publisher_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgPublisherId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				contactIds.add(rs.getInt("contact_id"));
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
		for(int i=0; i<contactIds.size(); i++){
			contacts.add(getTmgPublisherContact(contactIds.get(i)));
		}
		return contacts;
	}

	public static TmgPublisherName getTmgPublisherName(int tmgPublisherNameId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgPublisherName tmgPublisherName = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_publisher_name where tmg_publisher_name_id=?");
			ps.setInt(1, tmgPublisherNameId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String value = rs.getString("value");
				if (value == null)
					value = "";
				String vocabulary = rs.getString("vocabulary");
				if (vocabulary == null)
					vocabulary = "";
				int tmgPublisherId = rs.getInt("tmg_publisher_id");
				tmgPublisherName = new TmgPublisherName(tmgPublisherNameId, value, vocabulary, tmgPublisherId);
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
		return tmgPublisherName;
	}

	public static ArrayList<TmgPublisherName> getTmgPublisherNameBTmgPublisher(int tmgPublisherId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> nameIds = new ArrayList<Integer>();
		ArrayList<TmgPublisherName> names = new ArrayList<TmgPublisherName>();
		String select = "select tmg_publisher_name_id from tmg_publisher_name where ";
		select+= "tmg_publisher_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgPublisherId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				nameIds.add(rs.getInt("name_id"));
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
		for(int i=0; i<nameIds.size(); i++){
			names.add(getTmgPublisherName(nameIds.get(i)));
		}
		return names;
	}

	public static TmgServicename getTmgServicename(int tmgServicenameId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgServicename tmgServicename = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_servicename where tmg_servicename_id=?");
			ps.setInt(1, tmgServicenameId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String servicename = rs.getString("servicename");
				if (servicename == null)
					servicename = "";
				int tmgId = rs.getInt("tmg_id");
				tmgServicename = new TmgServicename(tmgServicenameId, servicename, tmgId);
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
		return tmgServicename;
	}

	public static ArrayList<TmgServicename> getTmgServicenameBTmg(int tmgId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> servicenameIds = new ArrayList<Integer>();
		ArrayList<TmgServicename> servicenames = new ArrayList<TmgServicename>();
		String select = "select tmg_servicename_id from tmg_servicename where ";
		select+= "tmg_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				servicenameIds.add(rs.getInt("servicename_id"));
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
		for(int i=0; i<servicenameIds.size(); i++){
			servicenames.add(getTmgServicename(servicenameIds.get(i)));
		}
		return servicenames;
	}

	public static TmgTimecoverage getTmgTimecoverage(int tmgTimecoverageId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgTimecoverage tmgTimecoverage = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_timecoverage where tmg_timecoverage_id=?");
			ps.setInt(1, tmgTimecoverageId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String resolution = rs.getString("resolution");
				if (resolution == null)
					resolution = "";
				int tmgId = rs.getInt("tmg_id");
				tmgTimecoverage = new TmgTimecoverage(tmgTimecoverageId, resolution, tmgId);
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
		return tmgTimecoverage;
	}

	public static ArrayList<TmgTimecoverage> getTmgTimecoverageBTmg(int tmgId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> timecoverageIds = new ArrayList<Integer>();
		ArrayList<TmgTimecoverage> timecoverages = new ArrayList<TmgTimecoverage>();
		String select = "select tmg_timecoverage_id from tmg_timecoverage where ";
		select+= "tmg_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				timecoverageIds.add(rs.getInt("timecoverage_id"));
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
		for(int i=0; i<timecoverageIds.size(); i++){
			timecoverages.add(getTmgTimecoverage(timecoverageIds.get(i)));
		}
		return timecoverages;
	}

	public static TmgTimecoverageDuration getTmgTimecoverageDuration(int tmgTimecoverageDurationId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgTimecoverageDuration tmgTimecoverageDuration = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_timecoverage_duration where tmg_timecoverage_duration_id=?");
			ps.setInt(1, tmgTimecoverageDurationId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String duration = rs.getString("duration");
				if (duration == null)
					duration = "";
				int tmgTimecoverageId = rs.getInt("tmg_timecoverage_id");
				tmgTimecoverageDuration = new TmgTimecoverageDuration(tmgTimecoverageDurationId, duration, tmgTimecoverageId);
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
		return tmgTimecoverageDuration;
	}

	public static ArrayList<TmgTimecoverageDuration> getTmgTimecoverageDurationBTmgTimecoverage(int tmgTimecoverageId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> durationIds = new ArrayList<Integer>();
		ArrayList<TmgTimecoverageDuration> durations = new ArrayList<TmgTimecoverageDuration>();
		String select = "select tmg_timecoverage_duration_id from tmg_timecoverage_duration where ";
		select+= "tmg_timecoverage_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgTimecoverageId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				durationIds.add(rs.getInt("duration_id"));
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
		for(int i=0; i<durationIds.size(); i++){
			durations.add(getTmgTimecoverageDuration(durationIds.get(i)));
		}
		return durations;
	}

	public static TmgTimecoverageEnd getTmgTimecoverageEnd(int tmgTimecoverageEndId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgTimecoverageEnd tmgTimecoverageEnd = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_timecoverage_end where tmg_timecoverage_end_id=?");
			ps.setInt(1, tmgTimecoverageEndId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String format = rs.getString("format");
				if (format == null)
					format = "";
				String value = rs.getString("value");
				if (value == null)
					value = "";
				String dateenum = rs.getString("dateenum");
				if (dateenum == null)
					dateenum = rs.getString("dateenum_nonstandard");
				if (dateenum == null)
					dateenum = "";
				int tmgTimecoverageId = rs.getInt("tmg_timecoverage_id");
				tmgTimecoverageEnd = new TmgTimecoverageEnd(tmgTimecoverageEndId, format, value, dateenum, tmgTimecoverageId);
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
		return tmgTimecoverageEnd;
	}

	public static ArrayList<TmgTimecoverageEnd> getTmgTimecoverageEndBTmgTimecoverage(int tmgTimecoverageId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> endIds = new ArrayList<Integer>();
		ArrayList<TmgTimecoverageEnd> ends = new ArrayList<TmgTimecoverageEnd>();
		String select = "select tmg_timecoverage_end_id from tmg_timecoverage_end where ";
		select+= "tmg_timecoverage_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgTimecoverageId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				endIds.add(rs.getInt("end_id"));
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
		for(int i=0; i<endIds.size(); i++){
			ends.add(getTmgTimecoverageEnd(endIds.get(i)));
		}
		return ends;
	}

	public static TmgTimecoverageResolution getTmgTimecoverageResolution(int tmgTimecoverageResolutionId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgTimecoverageResolution tmgTimecoverageResolution = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_timecoverage_resolution where tmg_timecoverage_resolution_id=?");
			ps.setInt(1, tmgTimecoverageResolutionId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String duration = rs.getString("duration");
				if (duration == null)
					duration = "";
				int tmgTimecoverageId = rs.getInt("tmg_timecoverage_id");
				tmgTimecoverageResolution = new TmgTimecoverageResolution(tmgTimecoverageResolutionId, duration, tmgTimecoverageId);
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
		return tmgTimecoverageResolution;
	}

	public static ArrayList<TmgTimecoverageResolution> getTmgTimecoverageResolutionBTmgTimecoverage(int tmgTimecoverageId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> resolutionIds = new ArrayList<Integer>();
		ArrayList<TmgTimecoverageResolution> resolutions = new ArrayList<TmgTimecoverageResolution>();
		String select = "select tmg_timecoverage_resolution_id from tmg_timecoverage_resolution where ";
		select+= "tmg_timecoverage_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgTimecoverageId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				resolutionIds.add(rs.getInt("resolution_id"));
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
		for(int i=0; i<resolutionIds.size(); i++){
			resolutions.add(getTmgTimecoverageResolution(resolutionIds.get(i)));
		}
		return resolutions;
	}

	public static TmgTimecoverageStart getTmgTimecoverageStart(int tmgTimecoverageStartId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgTimecoverageStart tmgTimecoverageStart = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_timecoverage_start where tmg_timecoverage_start_id=?");
			ps.setInt(1, tmgTimecoverageStartId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String format = rs.getString("format");
				if (format == null)
					format = "";
				String value = rs.getString("value");
				if (value == null)
					value = "";
				String dateenum = rs.getString("dateenum");
				if (dateenum == null)
					dateenum = rs.getString("dateenum_nonstandard");
				if (dateenum == null)
					dateenum = "";
				int tmgTimecoverageId = rs.getInt("tmg_timecoverage_id");
				tmgTimecoverageStart = new TmgTimecoverageStart(tmgTimecoverageStartId, format, value, dateenum, tmgTimecoverageId);
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
		return tmgTimecoverageStart;
	}

	public static ArrayList<TmgTimecoverageStart> getTmgTimecoverageStartBTmgTimecoverage(int tmgTimecoverageId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> startIds = new ArrayList<Integer>();
		ArrayList<TmgTimecoverageStart> starts = new ArrayList<TmgTimecoverageStart>();
		String select = "select tmg_timecoverage_start_id from tmg_timecoverage_start where ";
		select+= "tmg_timecoverage_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgTimecoverageId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				startIds.add(rs.getInt("start_id"));
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
		for(int i=0; i<startIds.size(); i++){
			starts.add(getTmgTimecoverageStart(startIds.get(i)));
		}
		return starts;
	}

	public static TmgVariables getTmgVariables(int tmgVariablesId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgVariables tmgVariables = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_variables where tmg_variables_id=?");
			ps.setInt(1, tmgVariablesId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String vocabulary = rs.getString("vocabulary");
				if (vocabulary == null)
					vocabulary = rs.getString("vocabulary_nonstandard");
				if (vocabulary == null)
					vocabulary = "";
				int tmgId = rs.getInt("tmg_id");
				tmgVariables = new TmgVariables(tmgVariablesId, vocabulary, tmgId);
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
		return tmgVariables;
	}

	public static ArrayList<TmgVariables> getTmgVariablesBTmg(int tmgId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> variablesIds = new ArrayList<Integer>();
		ArrayList<TmgVariables> variabless = new ArrayList<TmgVariables>();
		String select = "select tmg_variables_id from tmg_variables where ";
		select+= "tmg_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				variablesIds.add(rs.getInt("variables_id"));
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
		for(int i=0; i<variablesIds.size(); i++){
			variabless.add(getTmgVariables(variablesIds.get(i)));
		}
		return variabless;
	}

	public static TmgVariablesVariable getTmgVariablesVariable(int tmgVariablesVariableId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgVariablesVariable tmgVariablesVariable = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_variables_variable where tmg_variables_variable_id=?");
			ps.setInt(1, tmgVariablesVariableId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String units = rs.getString("units");
				if (units == null)
					units = "";
				String name = rs.getString("name");
				if (name == null)
					name = "";
				String vocabularyName = rs.getString("vocabulary_name");
				if (vocabularyName == null)
					vocabularyName = "";
				int tmgVariablesId = rs.getInt("tmg_variables_id");
				tmgVariablesVariable = new TmgVariablesVariable(tmgVariablesVariableId, units, name, vocabularyName, tmgVariablesId);
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
		return tmgVariablesVariable;
	}

	public static ArrayList<TmgVariablesVariable> getTmgVariablesVariableBTmgVariables(int tmgVariablesId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> variableIds = new ArrayList<Integer>();
		ArrayList<TmgVariablesVariable> variables = new ArrayList<TmgVariablesVariable>();
		String select = "select tmg_variables_variable_id from tmg_variables_variable where ";
		select+= "tmg_variables_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgVariablesId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				variableIds.add(rs.getInt("variable_id"));
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
		for(int i=0; i<variableIds.size(); i++){
			variables.add(getTmgVariablesVariable(variableIds.get(i)));
		}
		return variables;
	}

	public static TmgVariablesVariablemap getTmgVariablesVariablemap(int tmgVariablesVariablemapId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		TmgVariablesVariablemap tmgVariablesVariablemap = null;

		try {
			ps = pgCache.prepareStatement("select * from tmg_variables_variablemap where tmg_variables_variablemap_id=?");
			ps.setInt(1, tmgVariablesVariablemapId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String value = rs.getString("value");
				if (value == null)
					value = "";
				String xlink = rs.getString("xlink");
				if (xlink == null)
					xlink = rs.getString("xlink_nonstandard");
				if (xlink == null)
					xlink = "";
				int tmgVariablesId = rs.getInt("tmg_variables_id");
				tmgVariablesVariablemap = new TmgVariablesVariablemap(tmgVariablesVariablemapId, value, xlink, tmgVariablesId);
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
		return tmgVariablesVariablemap;
	}

	public static ArrayList<TmgVariablesVariablemap> getTmgVariablesVariablemapBTmgVariables(int tmgVariablesId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> variablemapIds = new ArrayList<Integer>();
		ArrayList<TmgVariablesVariablemap> variablemaps = new ArrayList<TmgVariablesVariablemap>();
		String select = "select tmg_variables_variablemap_id from tmg_variables_variablemap where ";
		select+= "tmg_variables_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgVariablesId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				variablemapIds.add(rs.getInt("variablemap_id"));
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
		for(int i=0; i<variablemapIds.size(); i++){
			variablemaps.add(getTmgVariablesVariablemap(variablemapIds.get(i)));
		}
		return variablemaps;
	}



	/*begin insert methods*/

	public static int insertCatalog(String name, String expires, String version, String base, String xmlns, String status) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int catalogId = -1;

		try {
			ps = setPreparedStatement("insert_catalog", new String[]{name, expires, version, base, xmlns, status});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogId;
	}

	public static int insertCatalogDataset(int catalogId, int datasetId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int catalogDatasetId = -1;

		try {
			ps = setPreparedStatement("insert_catalog_dataset", new int[]{catalogId, datasetId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogDatasetId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogDatasetId;
	}

	public static int insertCatalogProperty(String name, String value, int catalogId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int catalogPropertyId = -1;

		try {
			ps = setPreparedStatement("insert_catalog_property", new String[]{name, value}, new int[]{catalogId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogPropertyId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogPropertyId;
	}

	public static int insertCatalogService(int catalogId, int serviceId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int catalogServiceId = -1;

		try {
			ps = setPreparedStatement("insert_catalog_service", new int[]{catalogId, serviceId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogServiceId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogServiceId;
	}

	public static int insertCatalogXlink(String value, String xlink, int catalogId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int catalogXlinkId = -1;

		try {
			ps = setPreparedStatement("insert_catalog_xlink", new String[]{value, xlink}, new int[]{catalogId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogXlinkId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogXlinkId;
	}

	public static int insertCatalogref(int childId, int parentId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int catalogrefId = -1;

		try {
			ps = setPreparedStatement("insert_catalogref", new int[]{childId, parentId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogrefId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogrefId;
	}

	public static int insertCatalogrefDocumentation(String value, String documentationenum, int catalogrefId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int catalogrefDocumentationId = -1;

		try {
			ps = setPreparedStatement("insert_catalogref_documentation", new String[]{value, documentationenum}, new int[]{catalogrefId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogrefDocumentationId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogrefDocumentationId;
	}

	public static int insertCatalogrefDocumentationNamespace(String namespace, int catalogrefDocumentationId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int catalogrefDocumentationNamespaceId = -1;

		try {
			ps = setPreparedStatement("insert_catalogref_documentation_namespace", new String[]{namespace}, new int[]{catalogrefDocumentationId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogrefDocumentationNamespaceId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogrefDocumentationNamespaceId;
	}

	public static int insertCatalogrefDocumentationXlink(String value, String xlink, int catalogrefDocumentationId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int catalogrefDocumentationXlinkId = -1;

		try {
			ps = setPreparedStatement("insert_catalogref_documentation_xlink", new String[]{value, xlink}, new int[]{catalogrefDocumentationId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogrefDocumentationXlinkId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogrefDocumentationXlinkId;
	}

	public static int insertCatalogrefXlink(String value, String xlink, int catalogrefId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int catalogrefXlinkId = -1;

		try {
			ps = setPreparedStatement("insert_catalogref_xlink", new String[]{value, xlink}, new int[]{catalogrefId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogrefXlinkId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogrefXlinkId;
	}

	public static int insertDataset(String harvest, String name, String alias, String authority, String dId, String servicename, String urlpath, String resourcecontrol, String collectiontype, String status, String datatype, String datasizeUnit) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int datasetId = -1;

		try {
			ps = setPreparedStatement("insert_dataset", new String[]{harvest, name, alias, authority, dId, servicename, urlpath, resourcecontrol, collectiontype, status, datatype, datasizeUnit});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetId;
	}

	public static int insertDatasetAccess(String urlpath, String servicename, String dataformat, int datasetId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int datasetAccessId = -1;

		try {
			ps = setPreparedStatement("insert_dataset_access", new String[]{urlpath, servicename, dataformat}, new int[]{datasetId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetAccessId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetAccessId;
	}

	public static int insertDatasetAccessDatasize(String value, String units, int datasetAccessId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int datasetAccessDatasizeId = -1;

		try {
			ps = setPreparedStatement("insert_dataset_access_datasize", new String[]{value, units}, new int[]{datasetAccessId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetAccessDatasizeId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetAccessDatasizeId;
	}

	public static int insertDatasetCatalogref(int catalogrefId, int datasetId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int datasetCatalogrefId = -1;

		try {
			ps = setPreparedStatement("insert_dataset_catalogref", new int[]{catalogrefId, datasetId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetCatalogrefId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetCatalogrefId;
	}

	public static int insertDatasetDataset(int parentId, int childId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int datasetDatasetId = -1;

		try {
			ps = setPreparedStatement("insert_dataset_dataset", new int[]{parentId, childId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetDatasetId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetDatasetId;
	}

	public static int insertDatasetNcml(int datasetId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int datasetNcmlId = -1;

		try {
			ps = setPreparedStatement("insert_dataset_ncml", new int[]{datasetId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetNcmlId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetNcmlId;
	}

	public static int insertDatasetProperty(String name, String value, int datasetId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int datasetPropertyId = -1;

		try {
			ps = setPreparedStatement("insert_dataset_property", new String[]{name, value}, new int[]{datasetId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetPropertyId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetPropertyId;
	}

	public static int insertDatasetService(int datasetId, int serviceId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int datasetServiceId = -1;

		try {
			ps = setPreparedStatement("insert_dataset_service", new int[]{datasetId, serviceId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetServiceId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetServiceId;
	}

	public static int insertDatasetTmg(int datasetId, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int datasetTmgId = -1;

		try {
			ps = setPreparedStatement("insert_dataset_tmg", new int[]{datasetId, tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetTmgId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetTmgId;
	}

	public static int insertMetadata(String metadatatype, String inherited) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int metadataId = -1;

		try {
			ps = setPreparedStatement("insert_metadata", new String[]{metadatatype, inherited});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			metadataId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return metadataId;
	}

	public static int insertMetadataNamespace(String namespace, int metadataId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int metadataNamespaceId = -1;

		try {
			ps = setPreparedStatement("insert_metadata_namespace", new String[]{namespace}, new int[]{metadataId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			metadataNamespaceId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return metadataNamespaceId;
	}

	public static int insertMetadataTmg(int tmgId, int metadataId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int metadataTmgId = -1;

		try {
			ps = setPreparedStatement("insert_metadata_tmg", new int[]{tmgId, metadataId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			metadataTmgId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return metadataTmgId;
	}

	public static int insertMetadataXlink(String value, String xlink, int metadataId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int metadataXlinkId = -1;

		try {
			ps = setPreparedStatement("insert_metadata_xlink", new String[]{value, xlink}, new int[]{metadataId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			metadataXlinkId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return metadataXlinkId;
	}

	public static int insertService(String suffix, String name, String base, String desc, String servicetype, String status) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int serviceId = -1;

		try {
			ps = setPreparedStatement("insert_service", new String[]{suffix, name, base, desc, servicetype, status});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			serviceId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return serviceId;
	}

	public static int insertServiceDatasetroot(String path, String location, int serviceId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int serviceDatasetrootId = -1;

		try {
			ps = setPreparedStatement("insert_service_datasetroot", new String[]{path, location}, new int[]{serviceId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			serviceDatasetrootId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return serviceDatasetrootId;
	}

	public static int insertServiceProperty(String value, String name, int serviceId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int servicePropertyId = -1;

		try {
			ps = setPreparedStatement("insert_service_property", new String[]{value, name}, new int[]{serviceId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			servicePropertyId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return servicePropertyId;
	}

	public static int insertServiceService(int childId, int parentId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int serviceServiceId = -1;

		try {
			ps = setPreparedStatement("insert_service_service", new int[]{childId, parentId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			serviceServiceId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return serviceServiceId;
	}

	public static int insertTmg() throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgId = -1;

		try {
			ps = setPreparedStatement("insert_tmg");

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgId;
	}

	public static int insertTmgAuthority(String authority, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgAuthorityId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_authority", new String[]{authority}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgAuthorityId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgAuthorityId;
	}

	public static int insertTmgContributor(String role, String name, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgContributorId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_contributor", new String[]{role, name}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgContributorId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgContributorId;
	}

	public static int insertTmgCreator(int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgCreatorId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_creator", new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgCreatorId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgCreatorId;
	}

	public static int insertTmgCreatorContact(String email, String url, int tmgCreatorId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgCreatorContactId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_creator_contact", new String[]{email, url}, new int[]{tmgCreatorId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgCreatorContactId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgCreatorContactId;
	}

	public static int insertTmgCreatorName(String value, String vocabulary, int tmgCreatorId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgCreatorNameId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_creator_name", new String[]{value, vocabulary}, new int[]{tmgCreatorId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgCreatorNameId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgCreatorNameId;
	}

	public static int insertTmgDataformat(String dataformat, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgDataformatId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_dataformat", new String[]{dataformat}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgDataformatId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgDataformatId;
	}

	public static int insertTmgDatasize(String value, String units, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgDatasizeId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_datasize", new String[]{value, units}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgDatasizeId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgDatasizeId;
	}

	public static int insertTmgDatatype(String datatype, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgDatatypeId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_datatype", new String[]{datatype}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgDatatypeId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgDatatypeId;
	}

	public static int insertTmgDate(String format, String value, String dateenum, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgDateId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_date", new String[]{format, value, dateenum}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgDateId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgDateId;
	}

	public static int insertTmgDocumentation(String value, String documentationenum, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgDocumentationId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_documentation", new String[]{value, documentationenum}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgDocumentationId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgDocumentationId;
	}

	public static int insertTmgDocumentationNamespace(String namespace, int tmgDocumentationId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgDocumentationNamespaceId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_documentation_namespace", new String[]{namespace}, new int[]{tmgDocumentationId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgDocumentationNamespaceId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgDocumentationNamespaceId;
	}

	public static int insertTmgDocumentationXlink(String value, String xlink, int tmgDocumentationId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgDocumentationXlinkId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_documentation_xlink", new String[]{value, xlink}, new int[]{tmgDocumentationId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgDocumentationXlinkId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgDocumentationXlinkId;
	}

	public static int insertTmgGeospatialcoverage(String upordown, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgGeospatialcoverageId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_geospatialcoverage", new String[]{upordown}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgGeospatialcoverageId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgGeospatialcoverageId;
	}

	public static int insertTmgGeospatialcoverageEastwest(String size, String units, String start, String resolution, int tmgGeospatialcoverageId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgGeospatialcoverageEastwestId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_geospatialcoverage_eastwest", new String[]{size, units, start, resolution}, new int[]{tmgGeospatialcoverageId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgGeospatialcoverageEastwestId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgGeospatialcoverageEastwestId;
	}

	public static int insertTmgGeospatialcoverageName(String vocabulary, String value, int tmgGeospatialcoverageId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgGeospatialcoverageNameId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_geospatialcoverage_name", new String[]{vocabulary, value}, new int[]{tmgGeospatialcoverageId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgGeospatialcoverageNameId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgGeospatialcoverageNameId;
	}

	public static int insertTmgGeospatialcoverageNorthsouth(String size, String resolution, String start, String units, int tmgGeospatialcoverageId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgGeospatialcoverageNorthsouthId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_geospatialcoverage_northsouth", new String[]{size, resolution, start, units}, new int[]{tmgGeospatialcoverageId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgGeospatialcoverageNorthsouthId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgGeospatialcoverageNorthsouthId;
	}

	public static int insertTmgGeospatialcoverageUpdown(String start, String resolution, String size, String units, int tmgGeospatialcoverageId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgGeospatialcoverageUpdownId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_geospatialcoverage_updown", new String[]{start, resolution, size, units}, new int[]{tmgGeospatialcoverageId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgGeospatialcoverageUpdownId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgGeospatialcoverageUpdownId;
	}

	public static int insertTmgKeyword(String value, String vocabulary, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgKeywordId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_keyword", new String[]{value, vocabulary}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgKeywordId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgKeywordId;
	}

	public static int insertTmgMetadata(int tmgId, int metadataId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgMetadataId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_metadata", new int[]{tmgId, metadataId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgMetadataId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgMetadataId;
	}

	public static int insertTmgProject(String value, String vocabulary, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgProjectId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_project", new String[]{value, vocabulary}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgProjectId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgProjectId;
	}

	public static int insertTmgProperty(String name, String value, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgPropertyId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_property", new String[]{name, value}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgPropertyId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgPropertyId;
	}

	public static int insertTmgPublisher(int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgPublisherId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_publisher", new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgPublisherId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgPublisherId;
	}

	public static int insertTmgPublisherContact(String url, String email, int tmgPublisherId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgPublisherContactId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_publisher_contact", new String[]{url, email}, new int[]{tmgPublisherId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgPublisherContactId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgPublisherContactId;
	}

	public static int insertTmgPublisherName(String value, String vocabulary, int tmgPublisherId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgPublisherNameId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_publisher_name", new String[]{value, vocabulary}, new int[]{tmgPublisherId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgPublisherNameId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgPublisherNameId;
	}

	public static int insertTmgServicename(String servicename, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgServicenameId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_servicename", new String[]{servicename}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgServicenameId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgServicenameId;
	}

	public static int insertTmgTimecoverage(String resolution, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgTimecoverageId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_timecoverage", new String[]{resolution}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgTimecoverageId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgTimecoverageId;
	}

	public static int insertTmgTimecoverageDuration(String duration, int tmgTimecoverageId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgTimecoverageDurationId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_timecoverage_duration", new String[]{duration}, new int[]{tmgTimecoverageId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgTimecoverageDurationId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgTimecoverageDurationId;
	}

	public static int insertTmgTimecoverageEnd(String format, String value, String dateenum, int tmgTimecoverageId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgTimecoverageEndId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_timecoverage_end", new String[]{format, value, dateenum}, new int[]{tmgTimecoverageId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgTimecoverageEndId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgTimecoverageEndId;
	}

	public static int insertTmgTimecoverageResolution(String duration, int tmgTimecoverageId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgTimecoverageResolutionId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_timecoverage_resolution", new String[]{duration}, new int[]{tmgTimecoverageId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgTimecoverageResolutionId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgTimecoverageResolutionId;
	}

	public static int insertTmgTimecoverageStart(String format, String value, String dateenum, int tmgTimecoverageId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgTimecoverageStartId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_timecoverage_start", new String[]{format, value, dateenum}, new int[]{tmgTimecoverageId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgTimecoverageStartId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgTimecoverageStartId;
	}

	public static int insertTmgVariables(String vocabulary, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgVariablesId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_variables", new String[]{vocabulary}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgVariablesId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgVariablesId;
	}

	public static int insertTmgVariablesVariable(String units, String name, String vocabularyName, int tmgVariablesId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgVariablesVariableId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_variables_variable", new String[]{units, name, vocabularyName}, new int[]{tmgVariablesId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgVariablesVariableId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgVariablesVariableId;
	}

	public static int insertTmgVariablesVariablemap(String value, String xlink, int tmgVariablesId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgVariablesVariablemapId = -1;

		try {
			ps = setPreparedStatement("insert_tmg_variables_variablemap", new String[]{value, xlink}, new int[]{tmgVariablesId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgVariablesVariablemapId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
				ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgVariablesVariablemapId;
	}





	/*begin update methods*/

	public static int updateCatalog(String name, String expires, String version, String base, String xmlns, String status) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int catalogId = -1;

		try {
			ps = setPreparedStatement("update_catalog", new String[]{name, expires, version, base, xmlns, status});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogId;
	}

	public static int updateCatalogDataset(int catalogId, int datasetId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int catalogDatasetId = -1;

		try {
			ps = setPreparedStatement("update_catalog_dataset", new int[]{catalogId, datasetId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogDatasetId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogDatasetId;
	}

	public static int updateCatalogProperty(String name, String value, int catalogId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int catalogPropertyId = -1;

		try {
			ps = setPreparedStatement("update_catalog_property", new String[]{name, value}, new int[]{catalogId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogPropertyId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogPropertyId;
	}

	public static int updateCatalogService(int catalogId, int serviceId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int catalogServiceId = -1;

		try {
			ps = setPreparedStatement("update_catalog_service", new int[]{catalogId, serviceId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogServiceId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogServiceId;
	}

	public static int updateCatalogXlink(String value, String xlink, int catalogId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int catalogXlinkId = -1;

		try {
			ps = setPreparedStatement("update_catalog_xlink", new String[]{value, xlink}, new int[]{catalogId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogXlinkId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogXlinkId;
	}

	public static int updateCatalogref(int childId, int parentId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int catalogrefId = -1;

		try {
			ps = setPreparedStatement("update_catalogref", new int[]{childId, parentId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogrefId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogrefId;
	}

	public static int updateCatalogrefDocumentation(String value, String documentationenum, int catalogrefId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int catalogrefDocumentationId = -1;

		try {
			ps = setPreparedStatement("update_catalogref_documentation", new String[]{value, documentationenum}, new int[]{catalogrefId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogrefDocumentationId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogrefDocumentationId;
	}

	public static int updateCatalogrefDocumentationNamespace(String namespace, int catalogrefDocumentationId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int catalogrefDocumentationNamespaceId = -1;

		try {
			ps = setPreparedStatement("update_catalogref_documentation_namespace", new String[]{namespace}, new int[]{catalogrefDocumentationId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogrefDocumentationNamespaceId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogrefDocumentationNamespaceId;
	}

	public static int updateCatalogrefDocumentationXlink(String value, String xlink, int catalogrefDocumentationId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int catalogrefDocumentationXlinkId = -1;

		try {
			ps = setPreparedStatement("update_catalogref_documentation_xlink", new String[]{value, xlink}, new int[]{catalogrefDocumentationId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogrefDocumentationXlinkId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogrefDocumentationXlinkId;
	}

	public static int updateCatalogrefXlink(String value, String xlink, int catalogrefId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int catalogrefXlinkId = -1;

		try {
			ps = setPreparedStatement("update_catalogref_xlink", new String[]{value, xlink}, new int[]{catalogrefId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogrefXlinkId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogrefXlinkId;
	}

	public static int updateDataset(String harvest, String name, String alias, String authority, String dId, String servicename, String urlpath, String resourcecontrol, String collectiontype, String status, String datatype, String datasizeUnit) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int datasetId = -1;

		try {
			ps = setPreparedStatement("update_dataset", new String[]{harvest, name, alias, authority, dId, servicename, urlpath, resourcecontrol, collectiontype, status, datatype, datasizeUnit});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetId;
	}

	public static int updateDatasetAccess(String urlpath, String servicename, String dataformat, int datasetId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int datasetAccessId = -1;

		try {
			ps = setPreparedStatement("update_dataset_access", new String[]{urlpath, servicename, dataformat}, new int[]{datasetId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetAccessId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetAccessId;
	}

	public static int updateDatasetAccessDatasize(String value, String units, int datasetAccessId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int datasetAccessDatasizeId = -1;

		try {
			ps = setPreparedStatement("update_dataset_access_datasize", new String[]{value, units}, new int[]{datasetAccessId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetAccessDatasizeId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetAccessDatasizeId;
	}

	public static int updateDatasetCatalogref(int catalogrefId, int datasetId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int datasetCatalogrefId = -1;

		try {
			ps = setPreparedStatement("update_dataset_catalogref", new int[]{catalogrefId, datasetId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetCatalogrefId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetCatalogrefId;
	}

	public static int updateDatasetDataset(int parentId, int childId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int datasetDatasetId = -1;

		try {
			ps = setPreparedStatement("update_dataset_dataset", new int[]{parentId, childId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetDatasetId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetDatasetId;
	}

	public static int updateDatasetNcml(int datasetId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int datasetNcmlId = -1;

		try {
			ps = setPreparedStatement("update_dataset_ncml", new int[]{datasetId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetNcmlId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetNcmlId;
	}

	public static int updateDatasetProperty(String name, String value, int datasetId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int datasetPropertyId = -1;

		try {
			ps = setPreparedStatement("update_dataset_property", new String[]{name, value}, new int[]{datasetId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetPropertyId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetPropertyId;
	}

	public static int updateDatasetService(int datasetId, int serviceId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int datasetServiceId = -1;

		try {
			ps = setPreparedStatement("update_dataset_service", new int[]{datasetId, serviceId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetServiceId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetServiceId;
	}

	public static int updateDatasetTmg(int datasetId, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int datasetTmgId = -1;

		try {
			ps = setPreparedStatement("update_dataset_tmg", new int[]{datasetId, tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetTmgId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetTmgId;
	}

	public static int updateMetadata(String metadatatype, String inherited) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int metadataId = -1;

		try {
			ps = setPreparedStatement("update_metadata", new String[]{metadatatype, inherited});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			metadataId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return metadataId;
	}

	public static int updateMetadataNamespace(String namespace, int metadataId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int metadataNamespaceId = -1;

		try {
			ps = setPreparedStatement("update_metadata_namespace", new String[]{namespace}, new int[]{metadataId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			metadataNamespaceId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return metadataNamespaceId;
	}

	public static int updateMetadataTmg(int tmgId, int metadataId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int metadataTmgId = -1;

		try {
			ps = setPreparedStatement("update_metadata_tmg", new int[]{tmgId, metadataId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			metadataTmgId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return metadataTmgId;
	}

	public static int updateMetadataXlink(String value, String xlink, int metadataId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int metadataXlinkId = -1;

		try {
			ps = setPreparedStatement("update_metadata_xlink", new String[]{value, xlink}, new int[]{metadataId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			metadataXlinkId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return metadataXlinkId;
	}

	public static int updateService(String suffix, String name, String base, String desc, String servicetype, String status) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int serviceId = -1;

		try {
			ps = setPreparedStatement("update_service", new String[]{suffix, name, base, desc, servicetype, status});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			serviceId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return serviceId;
	}

	public static int updateServiceDatasetroot(String path, String location, int serviceId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int serviceDatasetrootId = -1;

		try {
			ps = setPreparedStatement("update_service_datasetroot", new String[]{path, location}, new int[]{serviceId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			serviceDatasetrootId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return serviceDatasetrootId;
	}

	public static int updateServiceProperty(String value, String name, int serviceId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int servicePropertyId = -1;

		try {
			ps = setPreparedStatement("update_service_property", new String[]{value, name}, new int[]{serviceId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			servicePropertyId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return servicePropertyId;
	}

	public static int updateServiceService(int childId, int parentId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int serviceServiceId = -1;

		try {
			ps = setPreparedStatement("update_service_service", new int[]{childId, parentId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			serviceServiceId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return serviceServiceId;
	}

	public static int updateTmg() throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgId = -1;

		try {
			ps = setPreparedStatement("update_tmg");

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgId;
	}

	public static int updateTmgAuthority(String authority, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgAuthorityId = -1;

		try {
			ps = setPreparedStatement("update_tmg_authority", new String[]{authority}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgAuthorityId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgAuthorityId;
	}

	public static int updateTmgContributor(String role, String name, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgContributorId = -1;

		try {
			ps = setPreparedStatement("update_tmg_contributor", new String[]{role, name}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgContributorId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgContributorId;
	}

	public static int updateTmgCreator(int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgCreatorId = -1;

		try {
			ps = setPreparedStatement("update_tmg_creator", new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgCreatorId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgCreatorId;
	}

	public static int updateTmgCreatorContact(String email, String url, int tmgCreatorId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgCreatorContactId = -1;

		try {
			ps = setPreparedStatement("update_tmg_creator_contact", new String[]{email, url}, new int[]{tmgCreatorId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgCreatorContactId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgCreatorContactId;
	}

	public static int updateTmgCreatorName(String value, String vocabulary, int tmgCreatorId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgCreatorNameId = -1;

		try {
			ps = setPreparedStatement("update_tmg_creator_name", new String[]{value, vocabulary}, new int[]{tmgCreatorId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgCreatorNameId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgCreatorNameId;
	}

	public static int updateTmgDataformat(String dataformat, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgDataformatId = -1;

		try {
			ps = setPreparedStatement("update_tmg_dataformat", new String[]{dataformat}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgDataformatId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgDataformatId;
	}

	public static int updateTmgDatasize(String value, String units, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgDatasizeId = -1;

		try {
			ps = setPreparedStatement("update_tmg_datasize", new String[]{value, units}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgDatasizeId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgDatasizeId;
	}

	public static int updateTmgDatatype(String datatype, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgDatatypeId = -1;

		try {
			ps = setPreparedStatement("update_tmg_datatype", new String[]{datatype}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgDatatypeId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgDatatypeId;
	}

	public static int updateTmgDate(String format, String value, String dateenum, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgDateId = -1;

		try {
			ps = setPreparedStatement("update_tmg_date", new String[]{format, value, dateenum}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgDateId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgDateId;
	}

	public static int updateTmgDocumentation(String value, String documentationenum, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgDocumentationId = -1;

		try {
			ps = setPreparedStatement("update_tmg_documentation", new String[]{value, documentationenum}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgDocumentationId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgDocumentationId;
	}

	public static int updateTmgDocumentationNamespace(String namespace, int tmgDocumentationId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgDocumentationNamespaceId = -1;

		try {
			ps = setPreparedStatement("update_tmg_documentation_namespace", new String[]{namespace}, new int[]{tmgDocumentationId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgDocumentationNamespaceId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgDocumentationNamespaceId;
	}

	public static int updateTmgDocumentationXlink(String value, String xlink, int tmgDocumentationId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgDocumentationXlinkId = -1;

		try {
			ps = setPreparedStatement("update_tmg_documentation_xlink", new String[]{value, xlink}, new int[]{tmgDocumentationId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgDocumentationXlinkId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgDocumentationXlinkId;
	}

	public static int updateTmgGeospatialcoverage(String upordown, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgGeospatialcoverageId = -1;

		try {
			ps = setPreparedStatement("update_tmg_geospatialcoverage", new String[]{upordown}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgGeospatialcoverageId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgGeospatialcoverageId;
	}

	public static int updateTmgGeospatialcoverageEastwest(String size, String units, String start, String resolution, int tmgGeospatialcoverageId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgGeospatialcoverageEastwestId = -1;

		try {
			ps = setPreparedStatement("update_tmg_geospatialcoverage_eastwest", new String[]{size, units, start, resolution}, new int[]{tmgGeospatialcoverageId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgGeospatialcoverageEastwestId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgGeospatialcoverageEastwestId;
	}

	public static int updateTmgGeospatialcoverageName(String vocabulary, String value, int tmgGeospatialcoverageId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgGeospatialcoverageNameId = -1;

		try {
			ps = setPreparedStatement("update_tmg_geospatialcoverage_name", new String[]{vocabulary, value}, new int[]{tmgGeospatialcoverageId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgGeospatialcoverageNameId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgGeospatialcoverageNameId;
	}

	public static int updateTmgGeospatialcoverageNorthsouth(String size, String resolution, String start, String units, int tmgGeospatialcoverageId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgGeospatialcoverageNorthsouthId = -1;

		try {
			ps = setPreparedStatement("update_tmg_geospatialcoverage_northsouth", new String[]{size, resolution, start, units}, new int[]{tmgGeospatialcoverageId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgGeospatialcoverageNorthsouthId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgGeospatialcoverageNorthsouthId;
	}

	public static int updateTmgGeospatialcoverageUpdown(String start, String resolution, String size, String units, int tmgGeospatialcoverageId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgGeospatialcoverageUpdownId = -1;

		try {
			ps = setPreparedStatement("update_tmg_geospatialcoverage_updown", new String[]{start, resolution, size, units}, new int[]{tmgGeospatialcoverageId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgGeospatialcoverageUpdownId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgGeospatialcoverageUpdownId;
	}

	public static int updateTmgKeyword(String value, String vocabulary, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgKeywordId = -1;

		try {
			ps = setPreparedStatement("update_tmg_keyword", new String[]{value, vocabulary}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgKeywordId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgKeywordId;
	}

	public static int updateTmgMetadata(int tmgId, int metadataId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgMetadataId = -1;

		try {
			ps = setPreparedStatement("update_tmg_metadata", new int[]{tmgId, metadataId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgMetadataId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgMetadataId;
	}

	public static int updateTmgProject(String value, String vocabulary, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgProjectId = -1;

		try {
			ps = setPreparedStatement("update_tmg_project", new String[]{value, vocabulary}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgProjectId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgProjectId;
	}

	public static int updateTmgProperty(String name, String value, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgPropertyId = -1;

		try {
			ps = setPreparedStatement("update_tmg_property", new String[]{name, value}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgPropertyId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgPropertyId;
	}

	public static int updateTmgPublisher(int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgPublisherId = -1;

		try {
			ps = setPreparedStatement("update_tmg_publisher", new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgPublisherId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgPublisherId;
	}

	public static int updateTmgPublisherContact(String url, String email, int tmgPublisherId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgPublisherContactId = -1;

		try {
			ps = setPreparedStatement("update_tmg_publisher_contact", new String[]{url, email}, new int[]{tmgPublisherId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgPublisherContactId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgPublisherContactId;
	}

	public static int updateTmgPublisherName(String value, String vocabulary, int tmgPublisherId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgPublisherNameId = -1;

		try {
			ps = setPreparedStatement("update_tmg_publisher_name", new String[]{value, vocabulary}, new int[]{tmgPublisherId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgPublisherNameId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgPublisherNameId;
	}

	public static int updateTmgServicename(String servicename, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgServicenameId = -1;

		try {
			ps = setPreparedStatement("update_tmg_servicename", new String[]{servicename}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgServicenameId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgServicenameId;
	}

	public static int updateTmgTimecoverage(String resolution, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgTimecoverageId = -1;

		try {
			ps = setPreparedStatement("update_tmg_timecoverage", new String[]{resolution}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgTimecoverageId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgTimecoverageId;
	}

	public static int updateTmgTimecoverageDuration(String duration, int tmgTimecoverageId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgTimecoverageDurationId = -1;

		try {
			ps = setPreparedStatement("update_tmg_timecoverage_duration", new String[]{duration}, new int[]{tmgTimecoverageId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgTimecoverageDurationId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgTimecoverageDurationId;
	}

	public static int updateTmgTimecoverageEnd(String format, String value, String dateenum, int tmgTimecoverageId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgTimecoverageEndId = -1;

		try {
			ps = setPreparedStatement("update_tmg_timecoverage_end", new String[]{format, value, dateenum}, new int[]{tmgTimecoverageId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgTimecoverageEndId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgTimecoverageEndId;
	}

	public static int updateTmgTimecoverageResolution(String duration, int tmgTimecoverageId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgTimecoverageResolutionId = -1;

		try {
			ps = setPreparedStatement("update_tmg_timecoverage_resolution", new String[]{duration}, new int[]{tmgTimecoverageId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgTimecoverageResolutionId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgTimecoverageResolutionId;
	}

	public static int updateTmgTimecoverageStart(String format, String value, String dateenum, int tmgTimecoverageId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgTimecoverageStartId = -1;

		try {
			ps = setPreparedStatement("update_tmg_timecoverage_start", new String[]{format, value, dateenum}, new int[]{tmgTimecoverageId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgTimecoverageStartId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgTimecoverageStartId;
	}

	public static int updateTmgVariables(String vocabulary, int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgVariablesId = -1;

		try {
			ps = setPreparedStatement("update_tmg_variables", new String[]{vocabulary}, new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgVariablesId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgVariablesId;
	}

	public static int updateTmgVariablesVariable(String units, String name, String vocabularyName, int tmgVariablesId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgVariablesVariableId = -1;

		try {
			ps = setPreparedStatement("update_tmg_variables_variable", new String[]{units, name, vocabularyName}, new int[]{tmgVariablesId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgVariablesVariableId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgVariablesVariableId;
	}

	public static int updateTmgVariablesVariablemap(String value, String xlink, int tmgVariablesId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		int tmgVariablesVariablemapId = -1;

		try {
			ps = setPreparedStatement("update_tmg_variables_variablemap", new String[]{value, xlink}, new int[]{tmgVariablesId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgVariablesVariablemapId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgVariablesVariablemapId;
	}





	/*begin delete methods*/

	public static int deleteCatalog(int catalogId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_catalog", new int[]{catalogId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogId;
	}

	public static int deleteCatalogDataset(int catalogDatasetId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_catalog_dataset", new int[]{catalogDatasetId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogDatasetId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogDatasetId;
	}

	public static int deleteCatalogProperty(int catalogPropertyId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_catalog_property", new int[]{catalogPropertyId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogPropertyId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogPropertyId;
	}

	public static int deleteCatalogService(int catalogServiceId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_catalog_service", new int[]{catalogServiceId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogServiceId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogServiceId;
	}

	public static int deleteCatalogXlink(int catalogXlinkId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_catalog_xlink", new int[]{catalogXlinkId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogXlinkId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogXlinkId;
	}

	public static int deleteCatalogref(int catalogrefId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_catalogref", new int[]{catalogrefId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogrefId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogrefId;
	}

	public static int deleteCatalogrefDocumentation(int catalogrefDocumentationId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_catalogref_documentation", new int[]{catalogrefDocumentationId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogrefDocumentationId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogrefDocumentationId;
	}

	public static int deleteCatalogrefDocumentationNamespace(int catalogrefDocumentationNamespaceId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_catalogref_documentation_namespace", new int[]{catalogrefDocumentationNamespaceId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogrefDocumentationNamespaceId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogrefDocumentationNamespaceId;
	}

	public static int deleteCatalogrefDocumentationXlink(int catalogrefDocumentationXlinkId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_catalogref_documentation_xlink", new int[]{catalogrefDocumentationXlinkId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogrefDocumentationXlinkId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogrefDocumentationXlinkId;
	}

	public static int deleteCatalogrefXlink(int catalogrefXlinkId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_catalogref_xlink", new int[]{catalogrefXlinkId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			catalogrefXlinkId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return catalogrefXlinkId;
	}

	public static int deleteDataset(int datasetId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_dataset", new int[]{datasetId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetId;
	}

	public static int deleteDatasetAccess(int datasetAccessId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_dataset_access", new int[]{datasetAccessId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetAccessId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetAccessId;
	}

	public static int deleteDatasetAccessDatasize(int datasetAccessDatasizeId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_dataset_access_datasize", new int[]{datasetAccessDatasizeId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetAccessDatasizeId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetAccessDatasizeId;
	}

	public static int deleteDatasetCatalogref(int datasetCatalogrefId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_dataset_catalogref", new int[]{datasetCatalogrefId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetCatalogrefId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetCatalogrefId;
	}

	public static int deleteDatasetDataset(int datasetDatasetId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_dataset_dataset", new int[]{datasetDatasetId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetDatasetId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetDatasetId;
	}

	public static int deleteDatasetNcml(int datasetNcmlId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_dataset_ncml", new int[]{datasetNcmlId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetNcmlId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetNcmlId;
	}

	public static int deleteDatasetProperty(int datasetPropertyId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_dataset_property", new int[]{datasetPropertyId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetPropertyId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetPropertyId;
	}

	public static int deleteDatasetService(int datasetServiceId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_dataset_service", new int[]{datasetServiceId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetServiceId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetServiceId;
	}

	public static int deleteDatasetTmg(int datasetTmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_dataset_tmg", new int[]{datasetTmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			datasetTmgId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return datasetTmgId;
	}

	public static int deleteMetadata(int metadataId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_metadata", new int[]{metadataId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			metadataId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return metadataId;
	}

	public static int deleteMetadataNamespace(int metadataNamespaceId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_metadata_namespace", new int[]{metadataNamespaceId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			metadataNamespaceId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return metadataNamespaceId;
	}

	public static int deleteMetadataTmg(int metadataTmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_metadata_tmg", new int[]{metadataTmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			metadataTmgId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return metadataTmgId;
	}

	public static int deleteMetadataXlink(int metadataXlinkId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_metadata_xlink", new int[]{metadataXlinkId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			metadataXlinkId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return metadataXlinkId;
	}

	public static int deleteService(int serviceId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_service", new int[]{serviceId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			serviceId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return serviceId;
	}

	public static int deleteServiceDatasetroot(int serviceDatasetrootId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_service_datasetroot", new int[]{serviceDatasetrootId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			serviceDatasetrootId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return serviceDatasetrootId;
	}

	public static int deleteServiceProperty(int servicePropertyId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_service_property", new int[]{servicePropertyId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			servicePropertyId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return servicePropertyId;
	}

	public static int deleteServiceService(int serviceServiceId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_service_service", new int[]{serviceServiceId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			serviceServiceId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return serviceServiceId;
	}

	public static int deleteTmg(int tmgId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg", new int[]{tmgId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgId;
	}

	public static int deleteTmgAuthority(int tmgAuthorityId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_authority", new int[]{tmgAuthorityId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgAuthorityId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgAuthorityId;
	}

	public static int deleteTmgContributor(int tmgContributorId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_contributor", new int[]{tmgContributorId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgContributorId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgContributorId;
	}

	public static int deleteTmgCreator(int tmgCreatorId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_creator", new int[]{tmgCreatorId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgCreatorId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgCreatorId;
	}

	public static int deleteTmgCreatorContact(int tmgCreatorContactId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_creator_contact", new int[]{tmgCreatorContactId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgCreatorContactId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgCreatorContactId;
	}

	public static int deleteTmgCreatorName(int tmgCreatorNameId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_creator_name", new int[]{tmgCreatorNameId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgCreatorNameId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgCreatorNameId;
	}

	public static int deleteTmgDataformat(int tmgDataformatId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_dataformat", new int[]{tmgDataformatId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgDataformatId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgDataformatId;
	}

	public static int deleteTmgDatasize(int tmgDatasizeId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_datasize", new int[]{tmgDatasizeId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgDatasizeId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgDatasizeId;
	}

	public static int deleteTmgDatatype(int tmgDatatypeId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_datatype", new int[]{tmgDatatypeId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgDatatypeId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgDatatypeId;
	}

	public static int deleteTmgDate(int tmgDateId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_date", new int[]{tmgDateId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgDateId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgDateId;
	}

	public static int deleteTmgDocumentation(int tmgDocumentationId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_documentation", new int[]{tmgDocumentationId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgDocumentationId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgDocumentationId;
	}

	public static int deleteTmgDocumentationNamespace(int tmgDocumentationNamespaceId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_documentation_namespace", new int[]{tmgDocumentationNamespaceId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgDocumentationNamespaceId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgDocumentationNamespaceId;
	}

	public static int deleteTmgDocumentationXlink(int tmgDocumentationXlinkId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_documentation_xlink", new int[]{tmgDocumentationXlinkId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgDocumentationXlinkId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgDocumentationXlinkId;
	}

	public static int deleteTmgGeospatialcoverage(int tmgGeospatialcoverageId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_geospatialcoverage", new int[]{tmgGeospatialcoverageId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgGeospatialcoverageId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgGeospatialcoverageId;
	}

	public static int deleteTmgGeospatialcoverageEastwest(int tmgGeospatialcoverageEastwestId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_geospatialcoverage_eastwest", new int[]{tmgGeospatialcoverageEastwestId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgGeospatialcoverageEastwestId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgGeospatialcoverageEastwestId;
	}

	public static int deleteTmgGeospatialcoverageName(int tmgGeospatialcoverageNameId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_geospatialcoverage_name", new int[]{tmgGeospatialcoverageNameId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgGeospatialcoverageNameId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgGeospatialcoverageNameId;
	}

	public static int deleteTmgGeospatialcoverageNorthsouth(int tmgGeospatialcoverageNorthsouthId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_geospatialcoverage_northsouth", new int[]{tmgGeospatialcoverageNorthsouthId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgGeospatialcoverageNorthsouthId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgGeospatialcoverageNorthsouthId;
	}

	public static int deleteTmgGeospatialcoverageUpdown(int tmgGeospatialcoverageUpdownId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_geospatialcoverage_updown", new int[]{tmgGeospatialcoverageUpdownId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgGeospatialcoverageUpdownId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgGeospatialcoverageUpdownId;
	}

	public static int deleteTmgKeyword(int tmgKeywordId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_keyword", new int[]{tmgKeywordId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgKeywordId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgKeywordId;
	}

	public static int deleteTmgMetadata(int tmgMetadataId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_metadata", new int[]{tmgMetadataId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgMetadataId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgMetadataId;
	}

	public static int deleteTmgProject(int tmgProjectId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_project", new int[]{tmgProjectId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgProjectId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgProjectId;
	}

	public static int deleteTmgProperty(int tmgPropertyId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_property", new int[]{tmgPropertyId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgPropertyId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgPropertyId;
	}

	public static int deleteTmgPublisher(int tmgPublisherId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_publisher", new int[]{tmgPublisherId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgPublisherId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgPublisherId;
	}

	public static int deleteTmgPublisherContact(int tmgPublisherContactId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_publisher_contact", new int[]{tmgPublisherContactId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgPublisherContactId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgPublisherContactId;
	}

	public static int deleteTmgPublisherName(int tmgPublisherNameId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_publisher_name", new int[]{tmgPublisherNameId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgPublisherNameId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgPublisherNameId;
	}

	public static int deleteTmgServicename(int tmgServicenameId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_servicename", new int[]{tmgServicenameId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgServicenameId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgServicenameId;
	}

	public static int deleteTmgTimecoverage(int tmgTimecoverageId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_timecoverage", new int[]{tmgTimecoverageId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgTimecoverageId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgTimecoverageId;
	}

	public static int deleteTmgTimecoverageDuration(int tmgTimecoverageDurationId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_timecoverage_duration", new int[]{tmgTimecoverageDurationId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgTimecoverageDurationId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgTimecoverageDurationId;
	}

	public static int deleteTmgTimecoverageEnd(int tmgTimecoverageEndId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_timecoverage_end", new int[]{tmgTimecoverageEndId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgTimecoverageEndId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgTimecoverageEndId;
	}

	public static int deleteTmgTimecoverageResolution(int tmgTimecoverageResolutionId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_timecoverage_resolution", new int[]{tmgTimecoverageResolutionId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgTimecoverageResolutionId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgTimecoverageResolutionId;
	}

	public static int deleteTmgTimecoverageStart(int tmgTimecoverageStartId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_timecoverage_start", new int[]{tmgTimecoverageStartId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgTimecoverageStartId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgTimecoverageStartId;
	}

	public static int deleteTmgVariables(int tmgVariablesId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_variables", new int[]{tmgVariablesId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgVariablesId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgVariablesId;
	}

	public static int deleteTmgVariablesVariable(int tmgVariablesVariableId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_variables_variable", new int[]{tmgVariablesVariableId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgVariablesVariableId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgVariablesVariableId;
	}

	public static int deleteTmgVariablesVariablemap(int tmgVariablesVariablemapId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = setPreparedStatement("delete_tmg_variables_variablemap", new int[]{tmgVariablesVariablemapId});

			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			rs.next();
			tmgVariablesVariablemapId = rs.getInt(1);
		}
		catch (SQLException e) {
			log.error("Caching: Could not access the database/cache. {}", e);
			throw new Exception("SQLException: " + e.getMessage());
		} finally {
			try {
			ps.close();
//				rs.close();
			}
			catch (SQLException e) {
				log.error("Cache read: Could not close the prepared statement. {}", e);
			}
		}
		return tmgVariablesVariablemapId;
	}

}
