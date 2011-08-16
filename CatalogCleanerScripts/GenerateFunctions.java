

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.*;
import java.lang.Character;

class GenerateFunctions {

	public static ArrayList<String> jointables = new ArrayList<String>();

	public static ArrayList<String> singletables = new ArrayList<String>();
	public static ArrayList<String> CatalogParams = new ArrayList<String>();
	public static ArrayList<String> DatasetParams = new ArrayList<String>();
	public static ArrayList<String> ServiceParams = new ArrayList<String>();
	public static ArrayList<String> TmgParams = new ArrayList<String>();
	public static ArrayList<String> MetadataParams = new ArrayList<String>();

	public static Hashtable<String, ArrayList<String>> hash = new Hashtable<String, ArrayList<String>>();

    public static void main(String[] args) throws Exception{

		jointables.add("catalog_dataset");
		jointables.add("catalog_service");
		jointables.add("dataset_catalogref");
		jointables.add("dataset_dataset");
		jointables.add("dataset_service");
		jointables.add("dataset_tmg");
		jointables.add("metadata_tmg");
		jointables.add("service_service");
		jointables.add("tmg_metadata");

		singletables.add("catalog");
		singletables.add("dataset");
		singletables.add("service");
		singletables.add("tmg");
		singletables.add("metadata");

		hash.put("catalog", CatalogParams);
		hash.put("dataset", DatasetParams);
		hash.put("service", ServiceParams);
		hash.put("tmg", TmgParams);
		hash.put("metadata", MetadataParams);

		BufferedReader br = new BufferedReader(new FileReader("CatalogCleanerScripts/tables_columns.txt"));
		BufferedWriter bw_insert_methods = null;
		BufferedWriter bw_update_methods = null;
		BufferedWriter bw_delete_methods = null;
		BufferedWriter bw_get_methods = null;
		BufferedWriter bw_insert_functions = null;
		BufferedWriter bw_update_functions = null;
		BufferedWriter bw_delete_functions = null;
		BufferedWriter bw_dropfunctions = null;

		BufferedReader br_functions = null;
		BufferedWriter bw_functions = null;
		BufferedReader br_methods = null;
		BufferedWriter bw_methods = null;

		BufferedWriter bw_classes = null;

		boolean doFunctions = false;
		boolean doMethods = false;
		boolean dropFunctions = false;
		boolean doClasses = false;

		for(int i = 0; i<args.length; i++){
			if(args[i].equals("f")){
				doFunctions = true;
				bw_insert_functions = new BufferedWriter(new FileWriter("CatalogCleanerScripts/output/insert_functions.sql"));
				bw_update_functions = new BufferedWriter(new FileWriter("CatalogCleanerScripts/output/update_functions.sql"));
				bw_delete_functions = new BufferedWriter(new FileWriter("CatalogCleanerScripts/output/delete_functions.sql"));
				// go ahead and write the drop functions too, you never know when you will need them
				dropFunctions = true;
				bw_dropfunctions = new BufferedWriter(new FileWriter("CatalogCleanerScripts/output/dropfunctions.sql"));
			}
			else if(args[i].equals("m")){
				doMethods = true;
				bw_insert_methods = new BufferedWriter(new FileWriter("CatalogCleanerScripts/output/insert_methods.stub"));
				bw_update_methods = new BufferedWriter(new FileWriter("CatalogCleanerScripts/output/update_methods.stub"));
				bw_delete_methods = new BufferedWriter(new FileWriter("CatalogCleanerScripts/output/delete_methods.stub"));
				bw_get_methods = new BufferedWriter(new FileWriter("CatalogCleanerScripts/output/get_methods.stub"));
			}
			else if(args[i].equals("d")){
				dropFunctions = true;
				bw_dropfunctions = new BufferedWriter(new FileWriter("CatalogCleanerScripts/output/dropfunctions.stub"));
			}
			else if(args[i].equals("c")){
				doClasses = true;
			}
		}

		try{
			String oneline = "";
			String tablename = "";
			ArrayList<String> textvars = new ArrayList<String>();
			ArrayList<String> intvars = new ArrayList<String>();
			ArrayList<String> userdefined = new ArrayList<String>();
			String childName = "";
			String parentName = "";
			while ((oneline=br.readLine()) != null) {

				StringTokenizer st = new StringTokenizer(oneline, ",");
				String newTablename = st.nextToken().trim();
				if(tablename.equals(""))
					tablename = newTablename;
				else if(!tablename.equals(newTablename)){
					if(doFunctions){
						String function = writeInsertFunction(tablename, textvars, intvars, userdefined);
						bw_insert_functions.write(function);
						if(!jointables.contains(tablename)){
							function = writeUpdateFunction(tablename, textvars, intvars, userdefined);
							bw_update_functions.write(function);
							function = writeDeleteFunction(tablename);
							bw_delete_functions.write(function);
						}
						else{
							function = writeDeleteJoinedFunction(tablename);
							bw_delete_functions.write(function);
						}
					}
					if(doMethods){
						String method = writeInsertMethod(tablename, textvars, intvars, userdefined);
						bw_insert_methods.write(method);
						method = writeUpdateMethod(tablename, textvars, intvars, userdefined);
						bw_update_methods.write(method);
						method = writeDeleteMethod(tablename);
						bw_delete_methods.write(method);
						if(!jointables.contains(tablename)){
							method = writeGetMethod(tablename, textvars, intvars, userdefined);
							bw_get_methods.write(method);
							if(!parentName.equals("")){
								method = writeGetBelonging(childName, parentName, tablename);
								bw_get_methods.write(method);
							}
						}
						else{
							if(!parentName.equals("")){
								method = writeGetBelonging(childName, parentName, tablename);
								bw_get_methods.write(method);
							}
						}
						childName = "";
						parentName = "";
					}
					if(dropFunctions){
						String function = dropFunction(tablename, textvars, intvars, userdefined);
						bw_dropfunctions.write(function);
					}
					if(doClasses){
						if(!jointables.contains(tablename)){
							String jclass = writeClass(tablename, textvars, intvars, userdefined);
							bw_classes = new BufferedWriter(new FileWriter("JavaSource/gov/noaa/pmel/tmap/catalogcleaner/data/" + capitalize(camelCase(tablename)) + ".java"));
							bw_classes.write(jclass);
							bw_classes.close();
						}
					}
					if(singletables.contains(tablename)){
						ArrayList<String> theList = hash.get(tablename);
						for(int i = 0; i<textvars.size(); i++){
							theList.add(textvars.get(i));
						}
						for(int i = 0; i<userdefined.size(); i++){
							theList.add(userdefined.get(i));
						}
					}
					tablename = newTablename.trim();
					textvars = new ArrayList<String>();
					intvars = new ArrayList<String>();
					userdefined = new ArrayList<String>();
				}
				String var = st.nextToken();
				String type = st.nextToken();
				if (type.equals("text")){
					if(var.indexOf("nonstandard") == -1)
						textvars.add(var);
				}
				else if(type.equals("integer")){
					if(!var.equals(tablename + "_id")){
						intvars.add(var);
						if(tablename.indexOf('_') > 0 && !tablename.equals("catalogref")){	// <-- catalogref is problematic in all sorts of ways!
							parentName = tablename.substring(0, tablename.lastIndexOf('_'));
							childName = tablename.substring(tablename.lastIndexOf('_') + 1, tablename.length());
						}
					}
				}
				else if (type.equals("USER-DEFINED"))
					userdefined.add(var);
				else
					throw new Exception(tablename + " has variable of type " + type);
			}
			if(doClasses){
				writeJoinClasses();
			}
			if(dropFunctions && !doFunctions){
				bw_dropfunctions.close();
			}
			if(doFunctions){
				bw_insert_functions.close();
				bw_update_functions.close();
				bw_delete_functions.close();
				bw_dropfunctions.close();

				bw_functions = new BufferedWriter(new FileWriter("CatalogCleanerScripts/output/functions.sql"));
				br_functions = new BufferedReader(new FileReader("CatalogCleanerScripts/output/insert_functions.sql"));
				while ((oneline=br_functions.readLine()) != null) {
					bw_functions.write(oneline + "\n");
				}
				br_functions.close();
				bw_functions.write("\n\n");
				br_functions = new BufferedReader(new FileReader("CatalogCleanerScripts/output/update_functions.sql"));
				while ((oneline=br_functions.readLine()) != null) {
					bw_functions.write(oneline + "\n");
				}
				br_functions.close();
				bw_functions.write("\n\n");
				br_functions = new BufferedReader(new FileReader("CatalogCleanerScripts/output/delete_functions.sql"));
				while ((oneline=br_functions.readLine()) != null) {
					bw_functions.write(oneline + "\n");
				}
				br_functions.close();
				bw_functions.close();
			}
			if(doMethods){
				bw_insert_methods.close();
				bw_update_methods.close();
				bw_delete_methods.close();
				bw_get_methods.close();

				br_methods = new BufferedReader(new FileReader("CatalogCleanerScripts/DataAccessStub.stub"));
				bw_methods = new BufferedWriter(new FileWriter("JavaSource/gov/noaa/pmel/tmap/catalogcleaner/DataAccess.java"));
				while ((oneline=br_methods.readLine()) != null) {
					bw_methods.write(oneline + "\n");
				}
				br_methods.close();
				bw_methods.write("\n\n\t/*begin get methods*/\n\n");
				br_methods = new BufferedReader(new FileReader("CatalogCleanerScripts/output/get_methods.stub"));
				while ((oneline=br_methods.readLine()) != null) {
					bw_methods.write(oneline + "\n");
				}
				br_methods.close();
				bw_methods.write("\n\n\t/*begin insert methods*/\n\n");
				br_methods = new BufferedReader(new FileReader("CatalogCleanerScripts/output/insert_methods.stub"));
				while ((oneline=br_methods.readLine()) != null) {
					bw_methods.write(oneline + "\n");
				}
				br_methods.close();
				bw_methods.write("\n\n");
				bw_methods.write("\n\n\t/*begin update methods*/\n\n");
				br_methods = new BufferedReader(new FileReader("CatalogCleanerScripts/output/update_methods.stub"));
				while ((oneline=br_methods.readLine()) != null) {
					bw_methods.write(oneline + "\n");
				}
				br_methods.close();
				bw_methods.write("\n\n");
				bw_methods.write("\n\n\t/*begin delete methods*/\n\n");
				br_methods = new BufferedReader(new FileReader("CatalogCleanerScripts/output/delete_methods.stub"));
				while ((oneline=br_methods.readLine()) != null) {
					bw_methods.write(oneline + "\n");
				}
				bw_methods.write("}\n");
				br_methods.close();
				bw_methods.close();
			}
		}
		catch(IOException e){
			System.out.println(e);
			System.exit(0);
		}
		finally{
			try{
				br.close();
			}
			catch(Exception ee){} // do nothing, must already be closed.

		}
    }

    public static void writeJoinClasses() throws Exception{
    	for(int i = 0; i<jointables.size(); i++){
    		String tablename = jointables.get(i);
    		String[] parentchild = tablename.split("_");
    		String parentName = parentchild[0];
    		String childName = parentchild[1];
			if(!childName.equals("catalogref")){
				String jclass = writeClass(parentName, childName, hash.get(childName));
				BufferedWriter bw_classes = new BufferedWriter(new FileWriter("JavaSource/gov/noaa/pmel/tmap/catalogcleaner/data/" + capitalize(camelCase(parentName + "_" + childName)) + ".java"));
				bw_classes.write(jclass);
				bw_classes.close();
			}
		}
    }

    /*
    CREATE OR REPLACE FUNCTION insertThreddsMetadataGroup(p_datatype text, p_dataformat text, p_servicename text, p_authority text) RETURNS int AS $$
	DECLARE
		id int;
	BEGIN
		insert into threddsmetadatagroup(servicename, authority) values (p_servicename, p_authority);
		select currval('threddsmetadatagroup_threddsmetadatagroup_id_seq') into id;

		BEGIN
			update dataset set datatype = cast(p_datatype as datatype);
		EXCEPTION
			when others then
				update dataset set datatype_nonstandard = p_datatype;
		END;
		BEGIN
			update dataset set dataformat = cast(p_dataformat as datatype);
		EXCEPTION
			when others then
				update dataset set dataformat_nonstandard = p_dataformat;
		END;
		return id;
	END;
	$$ LANGUAGE plpgsql;
	*/
    public static String writeInsertFunction(String tablename, ArrayList<String> textvars, ArrayList<String> intvars, ArrayList<String> userdefined){
		String st = "CREATE OR REPLACE FUNCTION insert_" + tablename +"(";
		boolean hasParams = false;
		for(int i = 0; i<textvars.size(); i++){
			String ii = textvars.get(i);
			if(!ii.equals("not_empty")){
				st += "p_" + textvars.get(i) + " text, ";
				hasParams = true;
			}
		}
		for(int i = 0; i<userdefined.size(); i++){
			st += "p_" + userdefined.get(i) + " text, ";
			hasParams = true;
		}
		for(int i = 0; i<intvars.size(); i++){
			st += "p_" + intvars.get(i) + " int, ";
			hasParams = true;
		}
		if(hasParams)
			st = st.substring(0, st.length()-2);
		st += ") ";
		st += "RETURNS int ";
		st += "AS $$\nDECLARE\n\tid int;\nBEGIN\n";
		st += "\t\tinsert into " + tablename + "(";
		for(int i = 0; i<textvars.size(); i++){
			st += "\"" + textvars.get(i) + "\", ";
		}
		for(int i = 0; i<intvars.size(); i++){
			st += "\"" + intvars.get(i) + "\", ";
		}
		st = st.substring(0, st.length()-2);
		st += ") values (";
		for(int i = 0; i<textvars.size(); i++){
			String ii = textvars.get(i);
			if(!ii.equals("not_empty"))
				st += "p_" + ii + ", ";
			else
				st += "'true', ";
		}
		for(int i = 0; i<intvars.size(); i++){
			st += "p_" + intvars.get(i) + ", ";
		}
		st = st.substring(0, st.length()-2);
		st += ");\n";
		if(!jointables.contains(tablename))
			st += "\t\tselect currval('" + tablename + "_" + tablename + "_" + "id_seq') into id;\n";
		for(int i = 0; i<userdefined.size(); i++){
			String var = userdefined.get(i);
			if(var.equals("status")){
				st += "\t\tupdate " + tablename + " set " + var + " = cast(p_" + var + " as " + var + ") where " + tablename + "_id=id;\n";
			}
			else{
				st += "\t\tBEGIN\n";
				st += "\t\t\tupdate " + tablename + " set " + var + " = cast(p_" + var + " as " + var + ") where " + tablename + "_id=id;\n";
				st += "\t\tEXCEPTION\n";
				st += "\t\t\twhen others then\n";
				st += "\t\t\t\tupdate " + tablename + " set " + var + "_nonstandard = p_" + var + " where " + tablename + "_id=id;\n";
				st += "\t\tEND;\n";
			}
		}
		if(!jointables.contains(tablename))
			st += "\n\t\treturn id;";
		else
			st += "\n\t\treturn 1;";
		st += "\nEND;\n";
		st += "$$ LANGUAGE plpgsql;\n\n";

		return st;
	}
	/*CREATE OR REPLACE FUNCTION update_catalog(p_catalog_id int, p_version text, p_name text, p_base text, p_xmlns text, p_expires text, p_status text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select catalog_id into id from catalog where catalog_id=p_catalog_id;
		if(id is null) then
			return -1;
		end if;
		update catalog set "version"=p_version, "name"=p_name, "base"=p_base, "xmlns"=p_xmlns, "expires"=p_expires where catalog_id=id;
		update catalog set status = cast(p_status as status) where catalog_id=id;

		BEGIN
					update dataset set datatype = cast(p_datatype as datatype), datatype_nonstandard=null where dataset_id=id;
				EXCEPTION
					when others then
						update dataset set datatype_nonstandard = p_datatype, datatype=null where dataset_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;*/
	public static String writeUpdateFunction(String tablename, ArrayList<String> textvars, ArrayList<String> intvars, ArrayList<String> userdefined){
		if(textvars.contains("not_empty"))
			textvars.remove("not_empty");	// update doesn't use this, it's just a placeholder used to get an ID back in the database initially
		String st = "CREATE OR REPLACE FUNCTION update_" + tablename +"(p_" + tablename + "_id int, ";
		for(int i = 0; i<textvars.size(); i++){
			st += "p_" + textvars.get(i) + " text, ";
		}
		for(int i = 0; i<userdefined.size(); i++){
			st += "p_" + userdefined.get(i) + " text, ";
		}
		for(int i = 0; i<intvars.size(); i++){
			st += "p_" + intvars.get(i) + " int, ";
		}
		st = st.substring(0, st.length()-2);
		st += ") ";
		st += "RETURNS int ";
		st += "AS $$\nDECLARE\n\tid int;\nBEGIN\n";
		if(!jointables.contains(tablename)){
			st += "\t\tselect " + tablename + "_id into id from " + tablename + " where " + tablename + "_id=p_" + tablename + "_id;\n";
			st += "\t\tif(id is null) then\n";
			st += "\t\t\treturn -1;\n";
			st += "\t\tend if;\n";
		}
		if(textvars.size() + intvars.size() > 0){
			st += "\t\tupdate " + tablename + " set ";
			for(int i = 0; i<textvars.size(); i++){
				st += "\"" + textvars.get(i) + "\"=p_" + textvars.get(i)+", ";
			}
			for(int i = 0; i<intvars.size(); i++){
				st += "\"" + intvars.get(i) + "\"=p_" + intvars.get(i)+", ";
			}
			st = st.substring(0, st.length()-2);
			st += " where " + tablename + "_id=id;\n";
		}
		for(int i = 0; i<userdefined.size(); i++){
			String var = userdefined.get(i);
			if(var.equals("status")){
				st += "\t\tupdate " + tablename + " set " + var + " = cast(p_" + var + " as " + var + ") where " + tablename + "_id=id;\n";
			}
			else{
				st += "\t\tBEGIN\n";
				st += "\t\t\tupdate " + tablename + " set " + var + " = cast(p_" + var + " as " + var + ") where " + tablename + "_id=id;\n";
				st += "\t\tEXCEPTION\n";
				st += "\t\t\twhen others then\n";
				st += "\t\t\t\tupdate " + tablename + " set " + var + "_nonstandard = p_" + var + " where " + tablename + "_id=id;\n";
				st += "\t\tEND;\n";
			}
		}
		if(!jointables.contains(tablename))
			st += "\n\t\treturn id;";
		else
			st += "\n\t\treturn 1;";
		st += "\nEND;\n";
		st += "$$ LANGUAGE plpgsql;\n\n";

		return st;
	}
/*
CREATE OR REPLACE FUNCTION delete_catalog(p_catalog_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select catalog_id into id from catalog where catalog_id=p_catalog_id;
		if(id is null) then
			return -1;
		end if;
		delete from catalog where catalog_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;
*/

// TODO: cascade? Not sure yet how this will work, so just plugging this general stub in
	public static String writeDeleteFunction(String tablename){
		String st = "CREATE OR REPLACE FUNCTION delete_" + tablename + "(p_" + tablename + "_id int) RETURNS int AS $$\n";
		st += "DECLARE\n";
		st += "\tid int;\n";
		st += "BEGIN\n";
		st += "\tselect " + tablename + "_id into id from " + tablename + " where " + tablename + "_id=p_" + tablename + "_id;\n";
		st += "\tif(id is null) then\n";
		st += "\t\treturn -1;\n";
		st += "\tend if;\n";
		st += "\tdelete from " + tablename + " where " + tablename + "_id=id;\n";
		st += "\n";
		st += "\treturn id;\n";
		st += "END;\n";
		st += "$$ LANGUAGE plpgsql;\n\n";
		return st;
	}
/*
CREATE OR REPLACE FUNCTION delete_catalog_service(p_catalog_id int, p_service_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select count(*) into id from catalog_service where catalog_id=p_catalog_id and service_id=p_service_id;
		if(id=0) then
			return -1;
		end if;
		delete from catalog_service where catalog_id=p_catalog_id and service_id=p_service_id;

		return id;
END;
$$ LANGUAGE plpgsql;
*/
// TODO: cascade? Not sure yet how this will work, so just plugging this general stub in
	public static String writeDeleteJoinedFunction(String tablename){
		String[] tables = tablename.split("_");
		String table1 = tables[0];
		String table2 = tables[1];
		if(table1.equals(table2)){
			table1="parent";
			table2="child";
		}
		String st = "CREATE OR REPLACE FUNCTION delete_" + tablename + "(p_" + table1 + "_id int, p_" + table2 + "_id int) RETURNS int AS $$\n";
		st += "DECLARE\n";
		st += "\tid int;\n";
		st += "BEGIN\n";
		st += "\tselect count(*) into id from " + tablename + " where " + table1 + "_id=p_" + table1 + "_id and " + table2 + "_id=p_" + table2 + "_id;\n";
		st += "\tif(id=0) then\n";
		st += "\t\treturn -1;\n";
		st += "\tend if;\n";
		st += "\tdelete from " + tablename + " where " + table1 + "_id=p_" + table1 + "_id and " + table2 + "_id=p_" + table2 + "_id;\n";
		st += "\n";
		st += "\treturn id;\n";
		st += "END;\n";
		st += "$$ LANGUAGE plpgsql;\n\n";
		return st;
	}

	//DROP FUNCTION IF EXISTS insert_catalogref_documentation_xlink(text, text, text, integer);
	public static String dropFunction(String tablename, ArrayList<String> textvars, ArrayList<String> intvars, ArrayList<String> userdefined){
			String st = "DROP FUNCTION IF EXISTS insert_" + tablename +"(";
			for(int i = 0; i<textvars.size(); i++){
				st += "text, ";
			}
			for(int i = 0; i<userdefined.size(); i++){
				st += "text, ";
			}
			for(int i = 0; i<intvars.size(); i++){
				st += "int, ";
			}
			if(textvars.size() + userdefined.size() + intvars.size() > 0)
				st = st.substring(0, st.length()-2);
			st += ");\n";
			if(!jointables.contains(tablename)){
				st += "DROP FUNCTION IF EXISTS update_" + tablename +"(int, ";
				for(int i = 0; i<textvars.size(); i++){
					st += "text, ";
				}
				for(int i = 0; i<userdefined.size(); i++){
					st += "text, ";
				}
				for(int i = 0; i<intvars.size(); i++){
					st += "int, ";
				}
				st = st.substring(0, st.length()-2);
				st += ");\n";
				st += "DROP FUNCTION IF EXISTS delete_" + tablename +"(int);\n";
			}
			else{
				st += "DROP FUNCTION IF EXISTS delete_" + tablename +"(int, int);\n";
			}
			return st;
	}

	/*
		public static int insert_tmg_variables_variablemap(String value, String xlink, int tmg_variables_id) throws Exception{

			PreparedStatement ps = null;
			ResultSet rs = null;
			int tmg_variables_variablemap_id = -1;

			try {
				ps = setPreparedStatement("insert_tmg_variables_variablemap", new String[]{value, xlink}, new int[]{tmg_variables_id});

				log.debug("About to send: {} to the database.", ps.toString());
				rs = ps.executeQuery();
				rs.next();
				tmg_variables_variablemap_id = rs.getInt(1);
			}
			catch (SQLException e) {
				log.error("Caching: Could not access the database/cache. {}", e);
				throw new Exception("SQLException: " + e.getMessage());
			} finally {
				try {
					ps.close();
					//						rs.close();
				}
				catch (SQLException e) {
					log.error("Cache read: Could not close the prepared statement. {}", e);
				}
			}
			return tmg_variables_variablemap_id;
	}
    */
	public static String writeInsertMethod(String tablename, ArrayList<String> textvars, ArrayList<String> intvars, ArrayList<String> userdefined) throws Exception{
		String functionname = tablename;
		if(textvars.contains("not_empty"))
			textvars.remove("not_empty");	// front end doesn't use this, it's just a placeholder used to get an ID back in the database
		boolean hasParams = false;
		String id = camelCase(tablename) + "Id";
		tablename = capitalize(camelCase(tablename));
		textvars = camelCase(textvars);
		intvars = camelCase(intvars);
		userdefined = camelCase(userdefined);

		String st = "\tpublic static int insert" + tablename + "(";

		//String name, String base, String suffix, String description, String serviceType, String status
		for(int i = 0; i<textvars.size(); i++){
			st += "String " + textvars.get(i) + ", ";
			hasParams = true;
		}
		for(int i = 0; i<userdefined.size(); i++){
			st += "String " + userdefined.get(i) + ", ";
			hasParams = true;
		}
		for(int i = 0; i<intvars.size(); i++){
			st += "int " + intvars.get(i) + ", ";
			hasParams = true;
		}
		if(hasParams)
			st = st.substring(0, st.length()-2);

		st += ") throws Exception{\n\n";
		st += "\t\tPreparedStatement ps = null;\n";
		st += "\t\tResultSet rs = null;\n";
		st += "\t\tint " + id + " = -1;\n";
		st += "\n\t\ttry {\n\t\t\tps = ";
		st += "setPreparedStatement";


		st += "(\"insert_" + functionname + "\", ";
		if(textvars.size() + userdefined.size() > 0){
			st += "new String[]{";

			//name, base, suffix, description, serviceType, status
			for(int i = 0; i<textvars.size(); i++){
				st += textvars.get(i) + ", ";
			}
			for(int i = 0; i<userdefined.size(); i++){
				st += userdefined.get(i) + ", ";
			}
			st = st.substring(0, st.length()-2);
			st += "}";
			if(intvars.size() > 0)
				st += ", ";
		}

		if(intvars.size() > 0){
			st += "new int[]{";
			for(int i = 0; i<intvars.size(); i++){
				st += intvars.get(i) + ", ";
			}
			st = st.substring(0, st.length()-2);
			st += "}";
		}
		if(!hasParams)
			st = st.substring(0, st.length()-2);


		st += ");\n\n";

		st += "\t\t\tlog.debug(\"About to send: {} to the database.\", ps.toString());\n";
		st += "\t\t\trs = ps.executeQuery();\n";
		st += "\t\t\trs.next();\n";
		st += "\t\t\t" + id + " = rs.getInt(1);\n";
		st += "\t\t}\n";
		st += "\t\tcatch (SQLException e) {\n";
		st += "\t\t\tlog.error(\"Caching: Could not access the database/cache. {}\", e);\n";
		st += "\t\t\tthrow new Exception(\"SQLException: \" + e.getMessage());\n";
		st += "\t\t} finally {\n";
		st += "\t\t\ttry {\n";
		st += "\t\t\t\tps.close();\n";
		st += "//				rs.close();\n";
		st += "\t\t\t}\n";
		st += "\t\t\tcatch (SQLException e) {\n";
		st += "\t\t\t\tlog.error(\"Cache read: Could not close the prepared statement. {}\", e);\n";
		st += "\t\t\t}\n";
		st += "\t\t}\n";
		st += "\t\treturn " + id + ";\n";
		st += "\t}\n\n";
		return st;
	}
/*
		public static int update_tmg_variables_variablemap(int tmg_variables_variablemap_id, String value, String xlink, int tmg_variables_id) throws Exception{

			PreparedStatement ps = null;
			ResultSet rs = null;
			int tmg_variables_variablemap_id = -1;

			try {
				ps = setPreparedStatement("inserttmg_variables_variablemap", new String[]{value, xlink}, new int[]{tmg_variables_id});

				log.debug("About to send: {} to the database.", ps.toString());
				rs = ps.executeQuery();
				rs.next();
				tmg_variables_variablemap_id = rs.getInt(1);
			}
			catch (SQLException e) {
				log.error("Caching: Could not access the database/cache. {}", e);
				throw new Exception("SQLException: " + e.getMessage());
			} finally {
				try {
					ps.close();
					//						rs.close();
				}
				catch (SQLException e) {
					log.error("Cache read: Could not close the prepared statement. {}", e);
				}
			}
			return tmg_variables_variablemap_id;
	}
*/
	public static String writeUpdateMethod(String tablename, ArrayList<String> textvars, ArrayList<String> intvars, ArrayList<String> userdefined) throws Exception{
		String functionname = tablename;
		if(textvars.contains("not_empty"))
			textvars.remove("not_empty");	// front end doesn't use this, it's just a placeholder used to get an ID back in the database
		boolean hasParams = false;
		String id = camelCase(tablename) + "Id";
		tablename = capitalize(camelCase(tablename));
		textvars = camelCase(textvars);
		intvars = camelCase(intvars);
		userdefined = camelCase(userdefined);

		String st = "\tpublic static int update" + capitalize(camelCase(functionname)) + "(";

		//String name, String base, String suffix, String description, String serviceType, String status
		for(int i = 0; i<textvars.size(); i++){
			st += "String " + textvars.get(i) + ", ";
			hasParams = true;
		}
		for(int i = 0; i<userdefined.size(); i++){
			st += "String " + userdefined.get(i) + ", ";
			hasParams = true;
		}
		for(int i = 0; i<intvars.size(); i++){
			st += "int " + intvars.get(i) + ", ";
			hasParams = true;
		}
		if(hasParams)
			st = st.substring(0, st.length()-2);

		st += ") throws Exception{\n\n";
		st += "\t\tPreparedStatement ps = null;\n\t\tResultSet rs = null;\n\t\tint " + id + " = -1;\n";
		st += "\n\t\ttry {\n\t\t\tps = ";
		st += "setPreparedStatement";


		st += "(\"update_" + functionname + "\", ";
		if(textvars.size() + userdefined.size() > 0){
			st += "new String[]{";

			//name, base, suffix, description, serviceType, status
			for(int i = 0; i<textvars.size(); i++){
				st += textvars.get(i) + ", ";
			}
			for(int i = 0; i<userdefined.size(); i++){
				st += userdefined.get(i) + ", ";
			}
			st = st.substring(0, st.length()-2);
			st += "}";
			if(intvars.size() > 0)
				st += ", ";
		}

		if(intvars.size() > 0){
			st += "new int[]{";
			for(int i = 0; i<intvars.size(); i++){
				st += intvars.get(i) + ", ";
			}
			st = st.substring(0, st.length()-2);
			st += "}";
		}
		if(!hasParams)
			st = st.substring(0, st.length()-2);
		st += ");\n\n";

		st += "\t\t\tlog.debug(\"About to send: {} to the database.\", ps.toString());\n";
		st += "\t\t\trs = ps.executeQuery();\n";
		st += "\t\t\trs.next();\n";
		st += "\t\t\t" + id + " = rs.getInt(1);\n";
		st += "\t\t}\n";
		st += "\t\tcatch (SQLException e) {\n";
		st += "\t\t\tlog.error(\"Caching: Could not access the database/cache. {}\", e);\n";
		st += "\t\t\tthrow new Exception(\"SQLException: \" + e.getMessage());\n";
		st += "\t\t} finally {\n";
		st += "\t\t\ttry {\n";
		st += "\t\t\tps.close();\n";
		st += "//				rs.close();\n";
		st += "\t\t\t}\n";
		st += "\t\t\tcatch (SQLException e) {\n";
		st += "\t\t\t\tlog.error(\"Cache read: Could not close the prepared statement. {}\", e);\n";
		st += "\t\t\t}\n";
		st += "\t\t}\n";
		st += "\t\treturn " + id + ";\n";
		st += "\t}\n\n";
		return st;
	}

	public static String writeDeleteMethod(String tablename){
		String functionname = tablename;
		String id = camelCase(tablename) + "Id";
		tablename = capitalize(camelCase(tablename));

		String st = "\tpublic static int delete" + capitalize(camelCase(functionname)) + "(int " + id;

		st += ") throws Exception{\n\n";
		st += "\t\tPreparedStatement ps = null;\n";
		st += "\t\tResultSet rs = null;\n";
		st += "\n\t\ttry {\n\t\t\tps = ";
		st += "setPreparedStatement";


		st += "(\"delete_" + functionname + "\", ";
		st += "new int[]{" + id + "}";

		st += ");\n\n";

		st += "\t\t\tlog.debug(\"About to send: {} to the database.\", ps.toString());\n";
		st += "\t\t\trs = ps.executeQuery();\n";
		st += "\t\t\trs.next();\n";
		st += "\t\t\t" + id + " = rs.getInt(1);\n";
		st += "\t\t}\n";
		st += "\t\tcatch (SQLException e) {\n";
		st += "\t\t\tlog.error(\"Caching: Could not access the database/cache. {}\", e);\n";
		st += "\t\t\tthrow new Exception(\"SQLException: \" + e.getMessage());\n";
		st += "\t\t} finally {\n";
		st += "\t\t\ttry {\n";
		st += "\t\t\tps.close();\n";
		st += "//				rs.close();\n";
		st += "\t\t\t}\n";
		st += "\t\t\tcatch (SQLException e) {\n";
		st += "\t\t\t\tlog.error(\"Cache read: Could not close the prepared statement. {}\", e);\n";
		st += "\t\t\t}\n";
		st += "\t\t}\n";
		st += "\t\treturn " + id + ";\n";
		st += "\t}\n\n";
		return st;
	}

/*
package gov.noaa.pmel.tmap.catalogcleaner;

public class Catalog {
	private int catalogId;
	private String expires;
	private int cleanCatalogId;

	public void setExpires(String input){
		this.expires = input;
	}
	public void setCleanCatalogId(int input){
		this.cleanCatalogId = input;
	}
	public String getExpires(){
		return this.expires;
	}
	public int getCleanCatalogId(){
		return this.cleanCatalogId;
	}
	public Catalog(int id){
		this.catalogId=id;
	}
	public Catalog(int id, String xmlns, String name, String status, String base, String version, String expires){
		this.catalogId=id;
		this.xmlns = xmlns;
		this.name = name;
		this.status = status;
		this.base = base;
		this.version = version;
		this.expires = expires;
	}
	public Catalog(int id, String xmlns, String name, String status, String base, String version, String expires, int cleanCatalogId){
		this.catalogId=id;
		this.xmlns = xmlns;
		this.name = name;
		this.status = status;
		this.base = base;
		this.version = version;
		this.expires = expires;
		this.cleanCatalogId = cleanCatalogId;
	}
}
*/
	public static String writeClass(String tablename, ArrayList<String> textvars, ArrayList<String> intvars, ArrayList<String> userdefined) throws Exception{
		if(textvars.contains("not_empty"))
			textvars.remove("not_empty");	// front end doesn't use this, it's just a placeholder used to get an ID back in the database
		String id = camelCase(tablename) + "Id";
		tablename = capitalize(camelCase(tablename));
		textvars = camelCase(textvars);
		intvars = camelCase(intvars);
		userdefined = camelCase(userdefined);
		String st = "package gov.noaa.pmel.tmap.catalogcleaner.data;\n";
		st += "\n";
		st += "public class " + tablename + " {\n";
		st += "\tprotected int " + id + ";\n";
		for(int i = 0; i<intvars.size(); i++){
			st += "\tprotected int "+ intvars.get(i) + ";\n";
		}
		for(int i = 0; i<textvars.size(); i++){
			st += "\tprotected String "+ textvars.get(i) + ";\n";
		}
		for(int i = 0; i<userdefined.size(); i++){
			st += "\tprotected String "+ userdefined.get(i) + ";\n";
		}

		for(int i = 0; i<intvars.size(); i++){
			String ii = intvars.get(i);
			st += "\tpublic void set" + capitalize(ii) + "(int " + ii + "){\n";
			st += "\t\tthis." + ii + " = " + ii + ";\n";
			st += "\t}\n";
		}
		for(int i = 0; i<textvars.size(); i++){
			String ii = textvars.get(i);
			st += "\tpublic void set" + capitalize(ii) + "(String " + ii + "){\n";
			st += "\t\tthis." + ii + " = " + ii + ";\n";
			st += "\t}\n";
		}
		for(int i = 0; i<userdefined.size(); i++){
			String ii = userdefined.get(i);
			st += "\tpublic void set" + capitalize(ii) + "(String " + ii + "){\n";
			st += "\t\tthis." + ii + " = " + ii + ";\n";
			st += "\t}\n";
		}
		st += "\tpublic int get" + capitalize(id) + "(){\n";
		st += "\t\treturn this." + id + ";\n";
		st += "\t}\n";

		for(int i = 0; i<intvars.size(); i++){
			String ii = intvars.get(i);
			st += "\tpublic int get" + capitalize(ii) + "(){\n";
			st += "\t\treturn this." + ii + ";\n";
			st += "\t}\n";
		}
		for(int i = 0; i<textvars.size(); i++){
			String ii = textvars.get(i);
			st += "\tpublic String get" + capitalize(ii) + "(){\n";
			st += "\t\treturn this." + ii + ";\n";
			st += "\t}\n";
		}
		for(int i = 0; i<userdefined.size(); i++){
			String ii = userdefined.get(i);
			st += "\tpublic String get" + capitalize(ii) + "(){\n";
			st += "\t\treturn this." + ii + ";\n";
			st += "\t}\n";
		}
		st += "\n";
		st += "\tpublic " + tablename + "(int id){\n";
		st += "\t\tthis." + id + "=id;\n";
		st += "\t}\n";
		if(textvars.size() + userdefined.size() + intvars.size() > 0){
			st += "\tpublic " + tablename + "(int " + id + ", ";
			for(int i = 0; i<textvars.size(); i++){
				st += "String " + textvars.get(i) + ", ";
			}
			for(int i = 0; i<userdefined.size(); i++){
				st += "String " + userdefined.get(i) + ", ";
			}
			for(int i = 0; i<intvars.size(); i++){
				st += "int " + intvars.get(i) + ", ";
			}
			st = st.substring(0, st.length() - 2);
			st += "){\n";
			st += "\t\tthis." + id + "=" + id + ";\n";
			for(int i = 0; i<textvars.size(); i++){
				String ii = textvars.get(i);
				st += "\t\tthis." + ii + "=" + ii + ";\n";
			}
			for(int i = 0; i<userdefined.size(); i++){
				String ii = userdefined.get(i);
				st += "\t\tthis." + ii + "=" + ii + ";\n";
			}
			for(int i = 0; i<intvars.size(); i++){
				String ii = intvars.get(i);
				if(ii.indexOf("clean")!=0)
					st += "\t\tthis." + ii + "=" + ii + ";\n";
			}
			st += "\t}\n";
		}
		st += "}\n";
		return st;
	}
/*
 * package gov.noaa.pmel.tmap.catalogcleaner.data;

public class ServiceService extends Service {
	private int parentId;

	public int getParentId(){
		return this.parentId;
	}

	public ServiceService(int parentId, int serviceId){
		super(serviceId);
		this.parentId = parentId;
	}
	public ServiceService(int parentId, int serviceId, String base, String name, String suffix, String desc, String status, String servicetype){
		super(serviceId, base, name, suffix, desc, status, servicetype);
		this.parentId = parentId;
	}
}
 */
	public static String writeClass(String parentName, String childName, ArrayList<String> userdefined) throws Exception{
		String parentClassName = capitalize(parentName);
		String childClassName = capitalize(childName);
		String parentId = parentName;
		parentId = "parentId";
		String childId = childName + "Id";

		String st = "";
		st += "package gov.noaa.pmel.tmap.catalogcleaner.data;\n";
		st += "\n";
		st += "public class " + parentClassName + childClassName + " extends " + childClassName + " {\n";
		st += "\tprivate int parentId;\n";
		st += "\n";
		st += "\tpublic int getParentId(){\n";
		st += "\t\treturn this." + parentId + ";\n";
		st += "\t}\n";
		st += "\n";
		st += "\tpublic " + parentClassName + childClassName + "(int " + parentId + ", int " + childId + "){\n";
		st += "\t\tsuper(" + childId + ");\n";
		st += "\t\tthis." + parentId + " = " + parentId+ ";\n";
		st += "\t}\n";
		if(userdefined.size() > 0){
			st += "\tpublic " + parentClassName + childClassName + "(int " + parentId + ", int " + childId + ", ";
			for(int i = 0; i<userdefined.size(); i++){
				st += "String " + userdefined.get(i) + ", ";
			}
			st = st.substring(0, st.length() - 2);
			st += "){\n";
			st += "\t\tsuper(" + childId + ", ";
			for(int i = 0; i<userdefined.size(); i++){
				st += userdefined.get(i) + ", ";
			}
			st = st.substring(0, st.length() - 2);
			st += ");\n";
			st += "\t\tthis.parentId = " + parentId + ";\n";
			st += "\t}\n";
		}
		st += "}\n";
		st += "\n";

		return st;
	}
/*
 * public static Catalog getCatalog(int catalogId) throws Exception{

		PreparedStatement ps = null;
		ResultSet rs = null;
		Catalog catalog = null;

		try {
			ps = setPreparedStatement("select * from catalog where catalog_id=?");
			ps.setInt(1, catalogId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			 while (rs.next()) {
				String name = rs.getString("name");
				String expires = rs.getString("expires");
				String xmlns = rs.getString("xmlns");
				// etc.
				int cleanCatalogId = rs.getInt("clean_catalog_id");
				catalog = new Catalog(catalogId, name, expires, xmlns, base, version, status, cleanCatalogId);
    		}
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
		return catalog;
	}
 */
	public static String writeGetMethod(String tablename, ArrayList<String> textvars, ArrayList<String> intvars, ArrayList<String> userdefined) throws Exception{
		String objname = tablename;
		String id = camelCase(tablename) + "Id";
		tablename = capitalize(camelCase(tablename));
		//textvars = camelCase(textvars);
		//intvars = camelCase(intvars);
		//userdefined = camelCase(userdefined);
		String st = "\tpublic static " + tablename + " get" + tablename + "(int " + id + ") throws Exception{\n";
		st += "\n";
		st += "\t\tPreparedStatement ps = null;\n";
		st += "\t\tResultSet rs = null;\n";
		st += "\t\t" + tablename + " " + camelCase(objname) + " = null;\n";
		st += "\n";
		st += "\t\ttry {\n";
		st += "\t\t\tps = pgCache.prepareStatement(\"select * from " + objname + " where " + objname + "_id=?\");\n";
		st += "\t\t\tps.setInt(1, " + id + ");\n";
		st += "\t\t\tlog.debug(\"About to send: {} to the database.\", ps.toString());\n";
		st += "\t\t\trs = ps.executeQuery();\n";
		st += "\t\t\twhile (rs.next()) {\n";
		for(int i = 0; i<textvars.size(); i++){
			String ii = textvars.get(i);
			st += "\t\t\t\tString " + camelCase(ii) + " = rs.getString(\"" + ii + "\");\n";
			st += "\t\t\t\tif (" + camelCase(ii) + " == null)\n";
			st += "\t\t\t\t\t" + camelCase(ii) + " = \"\";\n";
		}
		for(int i = 0; i<userdefined.size(); i++){
			String ii = userdefined.get(i);
			st += "\t\t\t\tString " + camelCase(ii) + " = rs.getString(\"" + ii + "\");\n";
			if(!ii.equals("status")){
				st += "\t\t\t\tif (" + camelCase(ii) + " == null)\n";
				st += "\t\t\t\t\t" + camelCase(ii) + " = rs.getString(\"" + ii + "_nonstandard\");\n";
			}
			st += "\t\t\t\tif (" + camelCase(ii) + " == null)\n";
			st += "\t\t\t\t\t" + camelCase(ii) + " = \"\";\n";
		}
		for(int i = 0; i<intvars.size(); i++){
			String ii = intvars.get(i);
			if(!ii.equals(objname + "_id"))
				st += "\t\t\t\tint " + camelCase(ii) + " = rs.getInt(\"" + ii + "\");\n";
		}
		st += "\t\t\t\t" + camelCase(objname) + " = new " + tablename + "(" + id + ", ";
		for(int i = 0; i<textvars.size(); i++){
			String ii = textvars.get(i);
			st += camelCase(ii) + ", ";
		}
		for(int i = 0; i<userdefined.size(); i++){
			String ii = userdefined.get(i);
			st += camelCase(ii) + ", ";
		}
		for(int i = 0; i<intvars.size(); i++){
			String ii = intvars.get(i);
			st += camelCase(ii) + ", ";
		}
		st = st.substring(0, st.length() - 2);
		st += ");\n";
	    st += "\t\t\t}\n";
	    st += "\t\t}\n";
	    st += "\t\tcatch (SQLException e) {\n";
	    st += "\t\t\tlog.error(\"Caching: Could not access the database/cache. {}\", e);\n";
	    st += "\t\t\tthrow new Exception(\"SQLException: \" + e.getMessage());\n";
	    st += "\t\t} finally {\n";
	    st += "\t\t\ttry {\n";
	    st += "\t\t\t\tps.close();\n";
	    st += "//					rs.close();\n";
	    st += "\t\t\t}\n";
	    st += "\t\t\tcatch (SQLException e) {\n";
	    st += "\t\t\t\tlog.error(\"Cache read: Could not close the prepared statement. {}\", e);\n";
	    st += "\t\t\t}\n";
	    st += "\t\t}\n";
	    st += "\t\treturn " + camelCase(objname) + ";\n";
	    st += "\t}\n\n";
		return st;
	}

/*
 *

	public static ArrayList<TmgAuthority> getTmgAuthoritiesBTmg(int tmgId) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> tmgAuthorityIds = new ArrayList<Integer>();
		ArrayList<TmgAuthority> tmgAuthoritys = new ArrayList<TmgAuthority>();
		String select = "select tmg_authority_id from tmg_authority where ";
		if(isClean)
			select += "clean_";
		select+= "tmg_id=?";
		try {
			ps = pgCache.prepareStatement(select);
			ps.setInt(1, tmgId);
			log.debug("About to send: {} to the database.", ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				tmgAuthorityIds.add(rs.getInt("tmg_authority_id"));
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
		for(int i=0; i<tmgAuthorityIds.size(); i++){
			tmgAuthoritys.add(getTmgAuthority(tmgAuthorityIds.get(i)));
		}
		return tmgAuthoritys;
	}
 */
	public static String writeGetBelonging(String childName, String parentName, String tablename){
		boolean isJoin = jointables.contains(tablename);
		String child_id = childName + "_id";
		String parent_id = parentName + "_id";
		if(childName.equals(parentName)){
			parent_id="parent_id";
			child_id="child_id";
		}
		String className = "";
		if(!jointables.contains(tablename)){
			className = capitalize(camelCase(parentName)) + capitalize(camelCase(childName));
		}
		else{
			className = capitalize(camelCase(childName));
		}
		String childId = camelCase(childName) + "Id";
		String parentId = camelCase(parentName) + "Id";
		String childArray = camelCase(childName) + "s";
		String parent_name = parentName;
		parentName = capitalize(camelCase(parentName));
		String st = "";
		st += "\tpublic static ArrayList<" + className + "> get" + className + "B" + parentName + "(int " + parentId + ") throws Exception{\n";
		st += "\t\tPreparedStatement ps = null;\n";
		st += "\t\tResultSet rs = null;\n";
		st += "\t\tArrayList<Integer> " + childId + "s = new ArrayList<Integer>();\n";
		st += "\t\tArrayList<" + className + "> " + childArray + " = new ArrayList<" + className + ">();\n";
		st += "\t\tString select = \"select ";
		if(!isJoin)
			st += parent_name + "_";
		st += child_id + " from " + parent_name + "_" + childName + " where \";\n";
		st += "\t\tselect+= \"" + parent_id + "=?\";\n";
		st += "\t\ttry {\n";
		st += "\t\t\tps = pgCache.prepareStatement(select);\n";
		st += "\t\t\tps.setInt(1, " + parentId + ");\n";
		st += "\t\t\tlog.debug(\"About to send: {} to the database.\", ps.toString());\n";
		st += "\t\t\trs = ps.executeQuery();\n";
		st += "\t\t\twhile (rs.next()) {\n";
		st += "\t\t\t\t" + childId + "s.add(rs.getInt(\"" + child_id + "\"));\n";
		st += "\t\t\t}\t\t\n";
		st += "\t\t}\n";
		st += "\t\tcatch (SQLException e) {\n";
		st += "\t\t\tlog.error(\"Caching: Could not access the database/cache. {}\", e);\n";
		st += "\t\t\tthrow new Exception(\"SQLException: \" + e.getMessage());\n";
		st += "\t\t} finally {\n";
		st += "\t\t\ttry {\n";
		st += "\t\t\t\tps.close();\n";
		st += "//\t\t\t\t\trs.close();\n";
		st += "\t\t\t}\n";
		st += "\t\t\tcatch (SQLException e) {\n";
		st += "\t\t\t\tlog.error(\"Cache read: Could not close the prepared statement. {}\", e);\n";
		st += "\t\t\t}\n";
		st += "\t\t}\n";
		st += "\t\tfor(int i=0; i<" + childId + "s.size(); i++){\n";
		st += "\t\t\t" + childArray + ".add(get" + className + "(" + childId + "s.get(i)));\n";
		st += "\t\t}\n";
		st += "\t\treturn " + childArray + ";\n";
		st += "\t}\n\n";

		return st;
	}

	public static ArrayList<String> camelCase(ArrayList<String> arr){
		ArrayList<String> ret = new ArrayList<String>();
		for(int i = 0; i<arr.size(); i++){
			ret.add(camelCase(arr.get(i)));
		}
		return ret;
	}
	public static String camelCase(String raw){
		if(raw.indexOf("_") == -1)
			return raw;
		String[] words = raw.split("_");
		String ret = words[0];
		for(int i = 1; i<words.length; i++){
			ret += capitalize(words[i]);
		}
		return ret;
	}
	public static String capitalize(String raw){
		String first = raw.substring(0, 1).toUpperCase();
		String last = raw.substring(1, raw.length());
		return first + last;
	}

}
