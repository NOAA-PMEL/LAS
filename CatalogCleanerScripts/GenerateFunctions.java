

import java.io.*;
import java.util.*;


class GenerateFunctions {

	public static ArrayList<String> jointables = new ArrayList<String>();

	public static ArrayList<String> singletables = new ArrayList<String>();

	public static Hashtable<String, ArrayList<String>> hash = new Hashtable<String, ArrayList<String>>();
	public static Hashtable<String, ArrayList<String>> hash2 = new Hashtable<String, ArrayList<String>>();
	
	public static Hashtable<String, String> columnToElement = new Hashtable<String, String>();

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
		
		columnToElement.put("d_id", "ID");
		columnToElement.put("documentationenum", "type");
		// TODO: the rest of these

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
		boolean doCrawler = false;

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
			if(args[i].equals("r")){
				doCrawler = true;
			}
		}

		try{
			String oneline = "";
			String tablename = "";
			ArrayList<String> textvars = new ArrayList<String>();
			//ArrayList<String> intvars = new ArrayList<String>();
			ArrayList<String> userdefined = new ArrayList<String>();
			String child_id = "";
			String parent_id = "";
			String childName = "";
			String parentName = "";
			while ((oneline=br.readLine()) != null) {

				StringTokenizer st = new StringTokenizer(oneline, ",");
				String newTablename = st.nextToken().trim();
				if(tablename.equals(""))
					tablename = newTablename;
				else if(!tablename.equals(newTablename)){
					if(parent_id.equals(child_id)){
						parent_id="parent_id";
						child_id="child_id";
					}
					else if((parentName + "_" + parentName).equals(childName))
						childName = parentName;
					if(doFunctions){
						String function = writeInsertFunction(tablename, textvars, userdefined, parent_id, child_id);
						bw_insert_functions.write(function);
						if(!jointables.contains(tablename)){
							function = writeUpdateFunction(tablename, textvars, userdefined, parent_id, child_id);
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
						String method = writeInsertMethod(tablename, textvars, userdefined, parent_id, child_id);
						bw_insert_methods.write(method);
						if(!jointables.contains(tablename)){
							method = writeUpdateMethod(tablename, textvars, userdefined, parent_id);
							bw_update_methods.write(method);
						}
						method = writeDeleteMethod(tablename);
						bw_delete_methods.write(method);
						if(!jointables.contains(tablename)){
							method = writeGetMethod(tablename, textvars, userdefined, parent_id, child_id);
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
					}
					if(dropFunctions){
						String function = dropFunction(tablename, textvars, userdefined, parent_id, child_id);
						bw_dropfunctions.write(function);
					}
					if(doClasses){
						if(!jointables.contains(tablename)){
							String jclass = writeClass(tablename, textvars, userdefined, parent_id, child_id);
							bw_classes = new BufferedWriter(new FileWriter("JavaSource/gov/noaa/pmel/tmap/catalogcleaner/data/" + capitalize(camelCase(tablename)) + ".java"));
							bw_classes.write(jclass);
							bw_classes.close();
						}
					}
					if(singletables.contains(tablename)){
						ArrayList<String> theList = hash.get(tablename + "_text");
						if(theList == null)
							theList = new ArrayList<String>();
						for(int i = 0; i<textvars.size(); i++){
							theList.add(textvars.get(i));
						}
						hash.put(tablename + "_text", theList);
						ArrayList<String> theList2 = hash.get(tablename + "_usd");
						if(theList2 == null)
							theList2 = new ArrayList<String>();
						for(int i = 0; i<userdefined.size(); i++){
							theList2.add(userdefined.get(i));
						}
						hash.put(tablename + "_usd", theList2);
					}
					// for crawler
					else {
						if(!tablename.equals("catalogref") && !childName.equals("not_empty")){
							ArrayList<String> theList = hash2.get(parentName);
							if(theList == null)
								theList = new ArrayList<String>();
							theList.add(childName);
							hash2.put(parentName, theList);
							ArrayList<String> theList2 = hash2.get(tablename);
							if(theList2 == null)
								theList2 = new ArrayList<String>();
							hash2.put(tablename, theList2);
							
							ArrayList<String> theList3 = hash.get(tablename + "_text");
							if(theList3 == null)
								theList3 = new ArrayList<String>();
							for(int i = 0; i<textvars.size(); i++){
								theList3.add(textvars.get(i));
							}
							hash.put(tablename + "_text", theList3);
							ArrayList<String> theList4 = hash.get(tablename + "_usd");
							if(theList4 == null)
								theList4 = new ArrayList<String>();
							for(int i = 0; i<userdefined.size(); i++){
								theList4.add(userdefined.get(i));
							}
							hash.put(tablename + "_usd", theList4);
						}
					}
					tablename = newTablename.trim();
					textvars = new ArrayList<String>();
					userdefined = new ArrayList<String>();
					child_id = "";
					parent_id = "";
					childName = "";
					parentName = "";
				}
				String var = st.nextToken();
				String type = st.nextToken();
				if (type.equals("text")){
					if(var.indexOf("nonstandard") == -1)
						textvars.add(var);
				}
				else if(type.equals("integer")){
					if(!var.equals(tablename + "_id")){
						if(tablename.indexOf('_') > 0){	
							parentName = tablename.substring(0, tablename.lastIndexOf('_'));
							parent_id = parentName + "_id";
							childName = tablename.substring(tablename.lastIndexOf('_') + 1, tablename.length());
							child_id = childName + "_id";
						}
					}
					else
						child_id = var;
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
				
				br_functions = new BufferedReader(new FileReader("CatalogCleanerScripts/output/functions.stub"));
				while ((oneline=br_functions.readLine()) != null) {
					bw_functions.write(oneline + "\n");
				}
				br_functions.close();
				bw_functions.write("\n\n");
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
			if(doCrawler){
				writeCrawler();
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
				ArrayList<String> all = new ArrayList<String>();
				all.addAll(hash.get(childName + "_text"));
				all.addAll(hash.get(childName + "_usd"));
				String jclass = writeClass(parentName, childName, all);
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
    public static String writeInsertFunction(String tablename, ArrayList<String> textvars, ArrayList<String> userdefined, String parent_id, String child_id){
		if(tablename.equals("catalog"))
			return "";
		String st = "CREATE OR REPLACE FUNCTION insert_" + tablename +"(";
		boolean hasParams = false;
		if(!parent_id.isEmpty()){
			st += "p_" + parent_id + " int, ";
			hasParams = true;
		}
		if(jointables.contains(tablename)){
			st += "p_" + child_id + " int, ";
			hasParams = true;
		}
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
		if(hasParams)
			st = st.substring(0, st.length()-2);
		st += ") ";
		st += "RETURNS int ";
		st += "AS $$\nDECLARE\n\tid int;\nBEGIN\n";
		st += "\t\tinsert into " + tablename + "(";
		boolean addNotEmpty = true;
		if(!parent_id.isEmpty())
			st += "\"" + parent_id + "\", ";
		if(jointables.contains(tablename)){
			st += "\"" + child_id + "\", ";
		}
		
		for(int i = 0; i<textvars.size(); i++){
			st += "\"" + textvars.get(i) + "\", ";
			if(textvars.get(i).equals("not_empty"))
				addNotEmpty=false;
		}
		if(hasParams || addNotEmpty==false)
			st = st.substring(0, st.length()-2);
		else
			if(addNotEmpty)
				st += "\"not_empty\"";
		st += ") values (";
		
		if(!parent_id.isEmpty())
			st += "p_" + parent_id + ", ";
		if(jointables.contains(tablename)){
			st += "p_" + child_id + ", ";
		}
		
		for(int i = 0; i<textvars.size(); i++){
			String ii = textvars.get(i);
			if(!ii.equals("not_empty"))
				st += "p_" + ii + ", ";
			else
				st += "'true', ";
		}
		if(hasParams || addNotEmpty==false)
			st = st.substring(0, st.length()-2);
		else
			st += "'true'";
		st += ");\n";
		if(!jointables.contains(tablename))
			st += "\t\tselect currval('" + tablename + "_" + tablename + "_" + "id_seq') into id;\n";
		for(int i = 0; i<userdefined.size(); i++){
			String var = userdefined.get(i);
			if(var.equals("status")){
				st += "\t\tupdate " + tablename + " set \"" + var + "\" = cast(p_" + var + " as " + var + ") where " + tablename + "_id=id;\n";
			}
			else{
				st += "\t\tBEGIN\n";
				st += "\t\t\tupdate " + tablename + " set \"" + var + "\" = cast(p_" + var + " as " + var + ") where " + tablename + "_id=id;\n";
				st += "\t\tEXCEPTION\n";
				st += "\t\t\twhen others then\n";
				st += "\t\t\t\tupdate " + tablename + " set \"" + var + "_nonstandard\" = p_" + var + " where " + tablename + "_id=id;\n";
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
	public static String writeUpdateFunction(String tablename, ArrayList<String> textvars, ArrayList<String> userdefined, String parent_id, String child_id){
		if(tablename.equals("catalog"))
			return "";
		if(textvars.contains("not_empty"))
			textvars.remove("not_empty");	// update doesn't use this, it's just a placeholder used to get an ID back in the database initially
		String st = "CREATE OR REPLACE FUNCTION update_" + tablename +"(";
		if(!parent_id.isEmpty())
			st += "p_" + parent_id + " int, ";
		st += "p_" + tablename + "_id int, ";
		for(int i = 0; i<textvars.size(); i++){
			st += "p_" + textvars.get(i) + " text, ";
		}
		for(int i = 0; i<userdefined.size(); i++){
			st += "p_" + userdefined.get(i) + " text, ";
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
		if(textvars.size() > 0 || !parent_id.isEmpty()){
			st += "\t\tupdate " + tablename + " set ";

			if(!parent_id.isEmpty())
				st += "\"" + parent_id + "\"=p_" + parent_id +", ";
			
			for(int i = 0; i<textvars.size(); i++){
				st += "\"" + textvars.get(i) + "\"=p_" + textvars.get(i)+", ";
			}
			st = st.substring(0, st.length()-2);
			st += " where " + tablename + "_id=id;\n";
		}
		for(int i = 0; i<userdefined.size(); i++){
			String var = userdefined.get(i);
			if(var.equals("status")){
				st += "\t\tupdate " + tablename + " set \"" + var + "\" = cast(p_" + var + " as " + var + ") where " + tablename + "_id=id;\n";
			}
			else{
				st += "\t\tBEGIN\n";
				st += "\t\t\tupdate " + tablename + " set \"" + var + "\" = cast(p_" + var + " as " + var + ") where " + tablename + "_id=id;\n";
				st += "\t\tEXCEPTION\n";
				st += "\t\t\twhen others then\n";
				st += "\t\t\t\tupdate " + tablename + " set \"" + var + "_nonstandard\" = p_" + var + " where " + tablename + "_id=id;\n";
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
	public static String dropFunction(String tablename, ArrayList<String> textvars, ArrayList<String> userdefined, String parent_id, String child_id){
			String st = "DROP FUNCTION IF EXISTS insert_" + tablename +"(";
			if(!parent_id.isEmpty())
				st += "int, ";
			if(!child_id.isEmpty())
				st += "int, ";
			for(int i = 0; i<textvars.size(); i++){
				st += "text, ";
			}
			for(int i = 0; i<userdefined.size(); i++){
				st += "text, ";
			}
			if(textvars.size() + userdefined.size() > 0 || !parent_id.isEmpty())
				st = st.substring(0, st.length()-2);
			st += ");\n";
			if(!jointables.contains(tablename)){
				st += "DROP FUNCTION IF EXISTS update_" + tablename +"(";
				if(!parent_id.isEmpty())
					st += "int, ";
				if(!child_id.isEmpty())
					st += "int, ";
				for(int i = 0; i<userdefined.size(); i++){
					st += "text, ";
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
	 * 
	public static int insertService(Datavalue suffix, Datavalue name, Datavalue base, Datavalue desc, Datavalue servicetype, Datavalue status) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_service", new Datavalue[]{suffix, name, base, desc, servicetype, status});
		return runStatement(ps);
	}
	public static int insertService(Service service) throws Exception{
		PreparedStatement ps = setPreparedStatement("insert_service", new Datavalue[]{service.getSuffix(), service.getName(), service.getBase(), service.getDesc(), service.getServicetype(), service.getStatus()});
		return runStatement(ps);
	}
    */
	public static String writeInsertMethod(String tablename, ArrayList<String> textvars, ArrayList<String> userdefined, String parent_id, String child_id) throws Exception{
		String functionname = tablename;
		if(textvars.contains("not_empty"))
			textvars.remove("not_empty");	// front end doesn't use this, it's just a placeholder used to get an ID back in the database
		boolean hasParams = false;
		tablename = capitalize(camelCase(tablename));
		textvars = camelCase(textvars);
		userdefined = camelCase(userdefined);

		String st = "\tpublic static int insert" + tablename + "(";

		//String name, String base, String suffix, String description, String serviceType, String status
		if(!parent_id.isEmpty()){
			st += "int " + camelCase(parent_id) + ", ";
			hasParams = true;
		}
		if(jointables.contains(functionname)){
			st += "int " + camelCase(child_id) + ", ";
			hasParams = true;
		}
		if(functionname.equals("catalog"))
			st += "Datavalue cleanCatalogId, ";
		for(int i = 0; i<textvars.size(); i++){
			st += "Datavalue " + textvars.get(i) + ", ";
			hasParams = true;
		}
		for(int i = 0; i<userdefined.size(); i++){
			st += "Datavalue " + userdefined.get(i) + ", ";
			hasParams = true;
		}
		if(hasParams)
			st = st.substring(0, st.length()-2);

		st += ") throws Exception{\n";
		st += "\t\tPreparedStatement ps = setPreparedStatement";

		st += "(\"insert_" + functionname.toLowerCase() + "\", ";
		if(jointables.contains(functionname)){
			st += "new int[]{" + camelCase(parent_id) + ", " + camelCase(child_id) + "}";
		}
		else if(!parent_id.isEmpty()){
			st += "new int[]{" + camelCase(parent_id) + "}";
		}
		if(textvars.size() + userdefined.size() > 0){
			if(!parent_id.isEmpty())
				st += ", ";
			st += "new Datavalue[]{";
			if(functionname.equals("catalog"))
				st += "cleanCatalogId, ";
			//name, base, suffix, description, serviceType, status
			for(int i = 0; i<textvars.size(); i++){
				st += textvars.get(i) + ", ";
			}
			for(int i = 0; i<userdefined.size(); i++){
				st += userdefined.get(i) + ", ";
			}
			st = st.substring(0, st.length()-2);
			st += "}";
		}
		if(!hasParams)
			st = st.substring(0, st.length()-2);


		st += ");\n";
		st += "\t\treturn runStatement(ps);\n";
		st += "\t}\n";
		if(functionname.indexOf("catalogref") > 0)
			return st;
		st += "\tpublic static int insert" + tablename + "(" + tablename + " " + camelCase(functionname);

		st += ") throws Exception{\n";
		st += "\t\tPreparedStatement ps = setPreparedStatement";

		st += "(\"insert_" + functionname + "\", ";
		if(jointables.contains(functionname)){
			st += "new int[]{" + camelCase(functionname) + ".getParentId(), " + camelCase(functionname) + ".getChildId()}";
		}
		else if(!parent_id.isEmpty()){
			st += "new int[]{" + camelCase(functionname) + ".get" + capitalize(camelCase(parent_id)) + "()}";
		}
		if(textvars.size() + userdefined.size() > 0){
			if(!parent_id.isEmpty())
				st += ", ";
			st += "new Datavalue[]{";

			if(functionname.equals("catalog"))
				st += "catalog.getCleanCatalogId(), ";
			//name, base, suffix, description, serviceType, status
			for(int i = 0; i<textvars.size(); i++){
				st += camelCase(functionname) + ".get" + capitalize(camelCase(textvars.get(i))) + "(), ";
			}
			for(int i = 0; i<userdefined.size(); i++){
				st += camelCase(functionname) + ".get" + capitalize(camelCase(userdefined.get(i))) + "(), ";
			}
			st = st.substring(0, st.length()-2);
			st += "}";
		}
		if(!hasParams)
			st = st.substring(0, st.length()-2);


		st += ");\n";
		st += "\t\treturn runStatement(ps);\n";
		st += "\t}\n";
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
	public static String writeUpdateMethod(String tablename, ArrayList<String> textvars, ArrayList<String> userdefined, String parentId) throws Exception{
		if(jointables.contains(tablename))
			return "";
		String functionname = tablename;
		if(textvars.contains("not_empty"))
			textvars.remove("not_empty");	// front end doesn't use this, it's just a placeholder used to get an ID back in the database
		tablename = capitalize(camelCase(tablename));
		textvars = camelCase(textvars);
		userdefined = camelCase(userdefined);

		String st = "\tpublic static int update" + capitalize(camelCase(functionname)) + "(";
		
		if(!parentId.isEmpty())
			st += "int " + camelCase(parentId) + ", ";
		//int id, String name, String base, String suffix, String description, String serviceType, String status
		st += "int " + camelCase(functionname) + "Id, ";
		
		for(int i = 0; i<textvars.size(); i++){
			st += "Datavalue " + textvars.get(i) + ", ";
		}
		for(int i = 0; i<userdefined.size(); i++){
			st += "Datavalue " + userdefined.get(i) + ", ";
		}
		st = st.substring(0, st.length()-2);

		st += ") throws Exception{\n";
		st += "\t\tPreparedStatement ps = setPreparedStatement";

		st += "(\"update_" + functionname.toLowerCase() + "\", ";
		st += "new int[]{";
		if(!parentId.isEmpty())
			st += camelCase(parentId) + ", ";
		st += camelCase(functionname) + "Id}";
		if(textvars.size() + userdefined.size() > 0){
			//if(!parent_id.isEmpty())
			st += ", ";
			st += "new Datavalue[]{";

			//name, base, suffix, description, serviceType, status
			for(int i = 0; i<textvars.size(); i++){
				st += textvars.get(i) + ", ";
			}
			for(int i = 0; i<userdefined.size(); i++){
				st += userdefined.get(i) + ", ";
			}
			st = st.substring(0, st.length()-2);
			st += "}";
		}
		st += ");\n";
		st += "\t\treturn runStatement(ps);\n";
		st += "\t}\n";
		
		st += "\tpublic static int update" + capitalize(camelCase(functionname)) + "(";

		//int id, String name, String base, String suffix, String description, String serviceType, String status
		st += capitalize(camelCase(functionname)) + " " + camelCase(functionname);
		
		st += ") throws Exception{\n";
		st += "\t\tPreparedStatement ps = setPreparedStatement";

		st += "(\"update_" + functionname + "\", ";
		st += "new int[]{";
		if(!parentId.isEmpty())
			st += camelCase(functionname) + ".get" + capitalize(camelCase(parentId)) + "(), ";
		st += camelCase(functionname) + ".get" + capitalize(camelCase(functionname)) + "Id()}";
		if(textvars.size() + userdefined.size() > 0){
			//if(!parent_id.isEmpty())
			st += ", ";
			st += "new Datavalue[]{";
			if(functionname.equals("catalog"))
				st += "catalog.getCleanCatalogId(), ";
			//name, base, suffix, description, serviceType, status
			for(int i = 0; i<textvars.size(); i++){
				st += camelCase(functionname) + ".get" + capitalize(textvars.get(i)) + "(), ";
			}
			for(int i = 0; i<userdefined.size(); i++){
				st += camelCase(functionname) + ".get" + capitalize(userdefined.get(i)) + "(), ";
			}
			st = st.substring(0, st.length()-2);
			st += "}";
		}
		st += ");\n";
		st += "\t\treturn runStatement(ps);\n";
		st += "\t}\n";
		return st;
	}

	public static String writeDeleteMethod(String tablename){
		if(jointables.contains(tablename))
			return writeJoinDeleteMethod(tablename);
		String functionname = tablename;
		String id = camelCase(tablename) + "Id";
		tablename = capitalize(camelCase(tablename));

		String st = "\tpublic static int delete" + capitalize(camelCase(functionname)) + "(int " + id;

		st += ") throws Exception{\n";
		st += "\t\tPreparedStatement ps = setPreparedStatement";

		st += "(\"delete_" + functionname.toLowerCase() + "\", ";
		st += "new int[]{" + id + "}";
		st += ");\n";
		st += "\t\treturn runStatement(ps);\n";
		st += "\t}\n";
		st += "\tpublic static int delete" + capitalize(camelCase(functionname)) + "(" + capitalize(camelCase(functionname)) + " " + camelCase(functionname);

		st += ") throws Exception{\n";
		st += "\t\tPreparedStatement ps = setPreparedStatement";

		st += "(\"delete_" + functionname + "\", ";
		st += "new int[]{";
		st += camelCase(functionname) + ".get" + capitalize(camelCase(functionname)) + "Id()}";
		st += ");\n";
		st += "\t\treturn runStatement(ps);\n";
		st += "\t}\n";
		return st;
	}
	
	public static String writeJoinDeleteMethod(String tablename){
		if(tablename.indexOf("catalogref") > 0)
			return "";
		String functionname = tablename;
		tablename = capitalize(camelCase(tablename));

		String st = "\tpublic static int delete" + capitalize(camelCase(functionname)) + "(int parentId, int childId";

		st += ") throws Exception{\n";
		st += "\t\tPreparedStatement ps = setPreparedStatement";

		st += "(\"delete_" + functionname + "\", ";
		st += "new int[]{parentId, childId}";
		st += ");\n";
		st += "\t\treturn runStatement(ps);\n";
		st += "\t}\n";
		st += "\tpublic static int delete" + capitalize(camelCase(functionname)) + "(" + capitalize(camelCase(functionname)) + " " + camelCase(functionname);

		st += ") throws Exception{\n";
		st += "\t\tPreparedStatement ps = setPreparedStatement";

		st += "(\"delete_" + functionname + "\", ";
		st += "new int[]{";
		st += camelCase(functionname) + ".getParentId(), " + camelCase(functionname) + ".getChildId()}";
		st += ");\n";
		st += "\t\treturn runStatement(ps);\n";
		st += "\t}\n";
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
	public static String writeClass(String tablename, ArrayList<String> textvars, ArrayList<String> userdefined, String parent_id, String child_id) throws Exception{
		String functionname = tablename;
		if(textvars.contains("not_empty"))
			textvars.remove("not_empty");	// front end doesn't use this, it's just a placeholder used to get an ID back in the database
		tablename = capitalize(camelCase(tablename));
		textvars = camelCase(textvars);
		userdefined = camelCase(userdefined);
		String st = "package gov.noaa.pmel.tmap.catalogcleaner.data;\n";
		st += "\n";
		st += "import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;";
		st += "\n";
		st += "public class " + tablename + " {\n";

		
		if(!parent_id.isEmpty()){
			st += "\tprotected int " + camelCase(parent_id) + ";\n";
		}
		if(jointables.contains(functionname)){
			st += "\tprotected int " + camelCase(child_id) + ";\n";
		}
		else{
			st += "\tprotected int " + camelCase(functionname) + "Id" + ";\n";
		}
		if(functionname.equals("catalog"))
			st += "\tprotected Datavalue cleanCatalogId = new Datavalue(null);\n";
		for(int i = 0; i<textvars.size(); i++){
			st += "\tprotected Datavalue "+ textvars.get(i) + " = new Datavalue(null);\n";
		}
		for(int i = 0; i<userdefined.size(); i++){
			st += "\tprotected Datavalue "+ userdefined.get(i) + " = new Datavalue(null);\n";
		}

		if(!parent_id.isEmpty()){
			st += "\tpublic void set" + capitalize(camelCase(parent_id)) + "(int " + camelCase(parent_id) + "){\n";
			st += "\t\tthis." + camelCase(parent_id) + " = " + camelCase(parent_id) + ";\n";
			st += "\t}\n";
		}
		if(jointables.contains(functionname)){
			st += "\tpublic void set" + capitalize(camelCase(child_id)) + "(int " + camelCase(child_id) + "){\n";
			st += "\t\tthis." + camelCase(child_id) + " = " + camelCase(child_id) + ";\n";
			st += "\t}\n";
		}
		else{
			st += "\tpublic void set" + capitalize(camelCase(functionname)) + "Id(int " + camelCase(functionname) + "Id){\n";
			st += "\t\tthis." + camelCase(functionname) + "Id = " + camelCase(functionname) + "Id;\n";
			st += "\t}\n";
		}
		if(functionname.equals("catalog")){
			st += "\tpublic void setCleanCatalogId(String cleanCatalogId){\n";
			st += "\t\t\tthis.cleanCatalogId = new Datavalue(cleanCatalogId);\n";
			st += "\t\t}\n";
		}
		for(int i = 0; i<textvars.size(); i++){
			String ii = textvars.get(i);
			st += "\tpublic void set" + capitalize(ii) + "(String " + ii + "){\n";
			st += "\t\tthis." + ii + " = new Datavalue(" + ii + ");\n";
			st += "\t}\n";
		}
		for(int i = 0; i<userdefined.size(); i++){
			String ii = userdefined.get(i);
			st += "\tpublic void set" + capitalize(ii) + "(String " + ii + "){\n";
			st += "\t\tthis." + ii + " = new Datavalue(" + ii + ");\n";
			st += "\t}\n";
		}
		if(!parent_id.isEmpty()){
			st += "\tpublic int get" + capitalize(camelCase(parent_id)) + "(){\n";
			st += "\t\treturn this." + camelCase(parent_id) + ";\n";
			st += "\t}\n";
		}
		if(jointables.contains(functionname)){
			st += "\tpublic int get" + capitalize(camelCase(child_id)) + "(){\n";
			st += "\t\treturn this." + camelCase(child_id) + ";\n";
			st += "\t}\n";
		}
		else{
			st += "\tpublic int get" + capitalize(camelCase(functionname)) + "Id(){\n";
			st += "\t\treturn this." + camelCase(functionname) + "Id;\n";
			st += "\t}\n";
		}
		if(functionname.equals("catalog")){
			st += "\tpublic Datavalue getCleanCatalogId(){\n";
			st += "\t\treturn this.cleanCatalogId;\n";
			st += "\t}\n";
		}
		for(int i = 0; i<textvars.size(); i++){
			String ii = textvars.get(i);
			st += "\tpublic Datavalue get" + capitalize(ii) + "(){\n";
			st += "\t\treturn this." + ii + ";\n";
			st += "\t}\n";
		}
		for(int i = 0; i<userdefined.size(); i++){
			String ii = userdefined.get(i);
			st += "\tpublic Datavalue get" + capitalize(ii) + "(){\n";
			st += "\t\treturn this." + ii + ";\n";
			st += "\t}\n";
		}
		st += "\n";
		st += "\tpublic " + tablename + "(){\n";
		if(jointables.contains(functionname)){
			st += "\t\tthis." + camelCase(child_id) + " = -1;\n";
		}
		else{
			st += "\t\tthis." + camelCase(functionname) + "Id = -1;\n";
		}
		st += "\t}\n";
		if(jointables.contains(functionname)){
			st += "\tpublic " + tablename + "(int " + camelCase(child_id) + "){\n";
			st += "\t\tthis." + camelCase(child_id) + " = " + camelCase(child_id) + ";\n";
		}
		else{
			st += "\tpublic " + tablename + "(int " + camelCase(functionname) + "){\n";
			st += "\t\tthis." + camelCase(functionname) + "Id = " + camelCase(functionname) + ";\n";
		}
		st += "\t}\n";
		if(textvars.size() + userdefined.size() > 0 || !parent_id.isEmpty()){
			st += "\tpublic " + tablename + "(";
			if(!parent_id.isEmpty()){
				st += "int " + camelCase(parent_id) + ", ";
			}
			if(jointables.contains(functionname)){
				st += "int " + camelCase(child_id) + ", ";
			}
			else{
				st += "int " + camelCase(functionname) + "Id, ";
			}
			if(functionname.equals("catalog"))
				st += "Datavalue cleanCatalogId, ";
			for(int i = 0; i<textvars.size(); i++){
				st += "Datavalue " + textvars.get(i) + ", ";
			}
			for(int i = 0; i<userdefined.size(); i++){
				st += "Datavalue " + userdefined.get(i) + ", ";
			}
			st = st.substring(0, st.length() - 2);
			st += "){\n";

			if(!parent_id.isEmpty()){
				st += "\t\tthis." + camelCase(parent_id) + " = " + camelCase(parent_id) + ";\n";
			}
			if(jointables.contains(functionname)){
				st += "\t\tthis." + camelCase(child_id) + " = " + camelCase(child_id) + ";\n";
			}
			else{
				st += "\t\tthis." + camelCase(functionname) + "Id = " + camelCase(functionname) + "Id;\n";
			}
			if(functionname.equals("catalog"))
				st += "\t\tthis.cleanCatalogId = cleanCatalogId;\n";
			for(int i = 0; i<textvars.size(); i++){
				String ii = textvars.get(i);
				st += "\t\tthis." + ii + "=" + ii + ";\n";
			}
			for(int i = 0; i<userdefined.size(); i++){
				String ii = userdefined.get(i);
				st += "\t\tthis." + ii + "=" + ii + ";\n";
			}

			st += "\t}\n";
		}

		st += "\tpublic " + tablename + " clone(){\n";
		st += "\t\t" + tablename + " clone = new " + tablename + "(";
		if(!parent_id.isEmpty()){
			st += "this." + camelCase(parent_id) + ", ";
		}
		st += "-1, ";
		if(functionname.equals("catalog"))
			st += "new Datavalue(null), ";
		for(int i = 0; i<textvars.size(); i++){
			st += "this." + textvars.get(i) + ", ";
		}
		for(int i = 0; i<userdefined.size(); i++){
			st += "this." + userdefined.get(i) + ", ";
		}
		st = st.substring(0, st.length() - 2);
		st += ");\n";
		st += "\t\treturn clone;\n";
		st += "\t}\n";
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
		st += "import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;";
		st += "\n";
		st += "public class " + parentClassName + childClassName + " extends " + childClassName + " {\n";
		st += "\tprivate int parentId;\n";
		st += "\n";
		st += "\tpublic int getParentId(){\n";
		st += "\t\treturn this." + parentId + ";\n";
		st += "\t}\n";
		st += "\n";
		st += "\tpublic int getChildId(){\n";
		st += "\t\treturn this." + childId + ";\n";
		st += "\t}\n";
		st += "\n";
		st += "\tpublic " + parentClassName + childClassName + "(int " + parentId + ", int " + childId + "){\n";
		st += "\t\tsuper(" + childId + ");\n";
		st += "\t\tthis." + parentId + " = " + parentId+ ";\n";
		st += "\t}\n";
		if(userdefined.size() > 0){
			st += "\tpublic " + parentClassName + childClassName + "(int " + parentId + ", int " + childId + ", ";
			for(int i = 0; i<userdefined.size(); i++){
				st += "Datavalue " + userdefined.get(i) + ", ";
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
 *
	public static CatalogProperty getCatalogProperty(int catalogPropertyId) throws Exception{

		CatalogProperty catalogProperty = null;
		Hashtable<String, String> hash = getObject("CatalogProperty", catalogPropertyId);
		int catalogId = Integer.parseInt(hash.get("catalog_id"));
		Datavalue name = new Datavalue(hash.get("name"));
		Datavalue value = new Datavalue(hash.get("value"));
		catalogProperty = new CatalogProperty(catalogId, catalogPropertyId, name, value);
		return catalogProperty;
	}
	}
 */
	public static String writeGetMethod(String tablename, ArrayList<String> textvars, ArrayList<String> userdefined, String parent_id, String child_id) throws Exception{
		String objname = tablename;
		String id = camelCase(tablename) + "Id";
		tablename = capitalize(camelCase(tablename));
		//textvars = camelCase(textvars);
		//intvars = camelCase(intvars);
		//userdefined = camelCase(userdefined);
		String st = "\tpublic static " + tablename + " get" + tablename + "(int " + id + ") throws Exception{\n";
		st += "\n";
		st += "\t\t" + tablename + " " + camelCase(objname) + " = null;\n";
		st += "\t\tHashtable<String, String> hash = getObject(\"" + objname + "\", " + id + ");\n";
		if(!parent_id.isEmpty()){
			st += "\t\tint " + camelCase(parent_id) + " = Integer.parseInt(hash.get(\"" + parent_id + "\"));\n";
		}
		if(objname.equals("catalog"))
			st += "\t\tDatavalue cleanCatalogId = new Datavalue(hash.get(\"cleanCatalogId\"));\n";
		for(int i = 0; i<textvars.size(); i++){
			String ii = textvars.get(i);
			st += "\t\tDatavalue " + camelCase(ii) + " = new Datavalue(hash.get(\"" + ii + "\"));\n";
		}
		for(int i = 0; i<userdefined.size(); i++){
			String ii = userdefined.get(i);
			st += "\t\tDatavalue " + camelCase(ii) + " = new Datavalue(hash.get(\"" + ii + "\"));\n";
			if(!ii.equals("status")){
				st += "\t\tif (" + camelCase(ii) + ".isNull())\n";
				st += "\t\t\t" + camelCase(ii) + " = new Datavalue(hash.get(\"" + ii + "_nonstandard\"));\n";
			}
		}
		st += "\t\t" + camelCase(objname) + " = new " + tablename + "(";
		if(!parent_id.isEmpty()){
			st += camelCase(parent_id) + ", ";
		}
		st += id + ", ";
		if(objname.equals("catalog"))
			st += "cleanCatalogId, ";
		for(int i = 0; i<textvars.size(); i++){
			String ii = textvars.get(i);
			st += camelCase(ii) + ", ";
		}
		for(int i = 0; i<userdefined.size(); i++){
			String ii = userdefined.get(i);
			st += camelCase(ii) + ", ";
		}
		st = st.substring(0, st.length() - 2);
		st += ");\n";
	    st += "\t\treturn " + camelCase(objname) + ";\n";
	    st += "\t}\n";
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
	public static ArrayList<TmgTimecoverageStart> getTmgTimecoverageStartBTmgTimecoverage(int tmgTimecoverageId) throws Exception{
		ArrayList<TmgTimecoverageStart> starts = new ArrayList<TmgTimecoverageStart>();
		ArrayList<Integer> startIds = getObjects("tmg_timecoverage_start", "tmg_timecoverage", tmgTimeCoverageId);
		for(int i=0; i<startIds.size(); i++){
			starts.add(getTmgTimecoverageStart(startIds.get(i)));
		}
		return starts;
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
		String childId = camelCase(child_id);
		String parentId = camelCase(parent_id);
		String childArray = camelCase(childName) + "s";
		String parent_name = parentName;
		parentName = capitalize(camelCase(parentName));
		String st = "";
		st += "\tpublic static ArrayList<" + className + "> get" + className + "B" + parentName + "(int " + parentId + ") throws Exception{\n";
		st += "\t\tArrayList<" + className + "> " + childArray + " = new ArrayList<" + className + ">();\n";
		st += "\t\tArrayList<Integer> " + childId + "s = getObjects(\"" + tablename + "\", \"";
		if(parent_name.equals(childName)){
			parent_name="parent";
			childName = "child";
		}
		st += parent_name + "\", ";
		if(isJoin)
			st += "\"" + childName + "\", ";
		st += parentId + ");\n";
		st += "\t\tfor(int i=0; i<" + childId + "s.size(); i++){\n";
		st += "\t\t\t" + childArray + ".add(get" + className + "(" + childId + "s.get(i)));\n";
		st += "\t\t}\n";
		st += "\t\treturn " + childArray + ";\n";
		st += "\t}\n";

		return st;
	}

/*
 * public void crawlNewTmgCreator(int parentId, Element child) throws Exception{
		int childId = Applier.applyTmgCreatorRules(parentId, child);	
		
		List all = child.getChildren();
		for(int i = 0; i<all.size(); i++){
			Element newchild = (Element) all.get(i);
			if(newchild.getName().equals("name"))
				crawlNewTmgCreatorName(childId, newchild);
			else if(newchild.getName().equals("contact"))
				crawlNewTmgCreatorContact(childId, newchild);
		}
	}
 */
	public static void writeCrawler() throws IOException{
		BufferedReader br_methods = new BufferedReader(new FileReader("CatalogCleanerScripts/testCrawler.stub"));
		BufferedWriter bw_writer = new BufferedWriter(new FileWriter("JavaSource/gov/noaa/pmel/tmap/catalogcleaner/Crawler.java"));
		String oneline = "";
		while ((oneline=br_methods.readLine()) != null) {
			bw_writer.write(oneline + "\n");
		}
		br_methods.close();
		
		String tablename = "";
		Enumeration<String> e = hash2.keys();
		ArrayList<String> text;
		ArrayList<String> usd;
		while(e.hasMoreElements()){
			String key = e.nextElement();
			ArrayList<String> values = hash2.get(key);
			for(int i = 0; i<values.size(); i++){
				if(!singletables.contains(values.get(i))){
					text = hash.get(key + "_" + values.get(i) + "_text");
					usd = hash.get(key + "_" + values.get(i) + "_usd");
				}
				else{
					text = hash.get(values.get(i) + "_text");
					usd = hash.get(values.get(i) + "_usd");
				}
				String jclass = writeCrawler(key, values.get(i), text, usd);
				bw_writer.write(jclass);
			}
		}
		bw_writer.write("}");
		bw_writer.close();
	}
	public static String writeCrawler(String parentName, String childName, ArrayList<String> textvars, ArrayList<String> userdefined){
		String st = "";
		String newParentName = "";
		ArrayList<String> theList = null;
		boolean hasValue = false;
		if(textvars.contains("value")){
			hasValue = true;
		}
		if(textvars.contains("not_empty"))
			textvars.remove("not_empty");
		if(!jointables.contains(parentName + "_" + childName)){
			theList = hash2.get(parentName + "_" + childName);
			newParentName = capitalize(camelCase(parentName)) + capitalize(camelCase(childName));
		}
		else{
			theList = hash2.get(childName);
			newParentName = capitalize(camelCase(childName));
		}
		if(!(parentName.equals("catalog") && childName.equals("xlink"))){
			st += "\tpublic void crawlNew" + capitalize(camelCase(parentName)) + capitalize(camelCase(childName)) + "(int parentId, Element child) throws Exception{\n";
			
			if(textvars.size() + userdefined.size() > 0){
				for(int i = 0; i<textvars.size(); i++){
					st += "\t\tDatavalue " + camelCase(textvars.get(i)) + " = new Datavalue();\n";
				}
				for(int i = 0; i<userdefined.size(); i++){
					st += "\t\tDatavalue " + camelCase(userdefined.get(i)) + " = new Datavalue();\n";
				}
			}
			st += "\n";
			boolean useElse = false;
			if(textvars.size() + userdefined.size() > 0){
				st += "\t\tArrayList<Attribute> values = getAttributes(child.getAttributes());\n";
				st += "\t\tfor(int i=0; i<values.size(); i++){\n";
				st += "\t\t\tAttribute a = values.get(i);\n";
				for(int i = 0; i<textvars.size(); i++){
					if(!textvars.get(i).equals("value")){
						st += "\t\t\t";
						if(useElse)
							st += "else ";
						else
							useElse = true;
						String colname = textvars.get(i);
						if(columnToElement.containsKey(colname))
							colname = columnToElement.get(colname);
						st += "if(a.getName().equals(\"" + colname + "\"))\n";
						st += "\t\t\t\t" + camelCase(textvars.get(i)) + ".NOTNULL(a.getValue());\n";
					}
				}
				for(int i = 0; i<userdefined.size(); i++){
					st += "\t\t\t";
					if(useElse)
						st += "else ";
					else
						useElse = true;
					String colname = userdefined.get(i);
					if(columnToElement.containsKey(colname))
						colname = columnToElement.get(colname);
					st += "if(a.getName().equals(\"" + colname + "\"))\n";
					st += "\t\t\t\t" + camelCase(userdefined.get(i)) + ".NOTNULL(a.getValue());\n";
				}
				st += "\t\t}\n";
			}
			useElse = false;
			if(textvars.size() + userdefined.size() > 0){
				st += "\t\tArrayList<Element> values2 = getElements(child.getChildren());\n";
				st += "\t\tfor(int i=0; i<values2.size(); i++){\n";
				st += "\t\t\tElement a = values2.get(i);\n";
				for(int i = 0; i<textvars.size(); i++){
					if(!textvars.get(i).equals("value")){
						st += "\t\t\t";
						if(useElse)
							st += "else ";
						else
							useElse = true;
						String colname = textvars.get(i);
						if(columnToElement.containsKey(colname))
							colname = columnToElement.get(colname);
						st += "if(a.getName().equals(\"" + colname + "\"))\n";
						st += "\t\t\t\t" + camelCase(textvars.get(i)) + ".NOTNULL(a.getValue());\n";
					}
				}
				for(int i = 0; i<userdefined.size(); i++){
					st += "\t\t\t";
					if(useElse)
						st += "else ";
					else
						useElse = true;
					String colname = userdefined.get(i);
					if(columnToElement.containsKey(colname))
						colname = columnToElement.get(colname);
					st += "if(a.getName().equals(\"" + colname + "\"))\n";
					st += "\t\t\t\t" + camelCase(userdefined.get(i)) + ".NOTNULL(a.getValue());\n";
				}
				st += "\t\t}\n";
			}
			if(hasValue){
				st += "\t\tString val = \"\";\n";
				st += "\t\tArrayList<org.jdom.Text> values3 = getContent(child.getContent());\n";
				st += "\t\tfor(int i=0; i<values3.size(); i++){\n";
				st += "\t\t\tval += values3.get(i).getText();\n";
				st += "\t\t}\n";
				st += "\t\tvalue.NOTNULL(val);\n";
			}
			
			st += "\n";
			
			if(!jointables.contains(parentName + "_" + childName)){
				st += "\t\t";
				if(theList.size() > 0)
					st += "int childId = ";
				st += "DataAccess.insert" + capitalize(camelCase(newParentName)) + "(parentId, ";
				for(int i = 0; i<textvars.size(); i++){
					st += camelCase(textvars.get(i)) + ", ";
				}
				for(int i = 0; i<userdefined.size(); i++){
					st += camelCase(userdefined.get(i)) + ", ";
				}
				st = st.substring(0, st.length() - 2);
				st += ");\n\n";
			}
			else{
				if(singletables.contains(childName)){
					st += "\t\tint childId = DataAccess.insert" + capitalize(childName) + "(";
					for(int i = 0; i<textvars.size(); i++){
						st += camelCase(textvars.get(i)) + ", ";
					}
					for(int i = 0; i<userdefined.size(); i++){
						st += camelCase(userdefined.get(i)) + ", ";
					}
					if(textvars.size() + userdefined.size() > 0)
						st = st.substring(0, st.length() - 2);
					st += ");\n\n";
				}
				else{
					if(theList.size() > 0)
						st += "\t\tint childId = ";
					st += "DataAccess.insert" + capitalize(childName) + "();\n";
				}
				st += "\t\tDataAccess.insert" + capitalize(camelCase(parentName)) + capitalize(childName) + "(parentId, childId);\n\n";
			}
			boolean hasTmg = false;
			if(theList.size() > 0){
				st += "\t\tArrayList<Element> all = getElements(child.getChildren());\n";
				st += "\t\tfor(int i = 0; i<all.size(); i++){\n";
				st += "\t\t\tElement newchild = all.get(i);\n";
				if(!jointables.contains(parentName + "_" + childName)){
					theList = hash2.get(parentName + "_" + childName);
					//parentName = capitalize(camelCase(parentName)) + capitalize(camelCase(childName));
				}
				else{
					theList = hash2.get(childName);
					//parentName = capitalize(camelCase(childName));
				}
				if(theList.contains("catalogref"))
					theList.remove("catalogref");
				if(!theList.isEmpty()){
					if(theList.size() >0){
						if(!theList.get(0).equals("tmg")){
							st += "\t\t\tif(newchild.getName().equals(\"" + theList.get(0) + "\"))\n";
							st+= "\t\t\t\tcrawlNew" + newParentName + capitalize(theList.get(0)) + "(childId, newchild);\n";
						}
					}
					else
						hasTmg = true;
					for(int i = 1; i<theList.size(); i++){
						if(!theList.get(i).equals("tmg")){
							st += "\t\t\telse if(newchild.getName().equals(\"" + theList.get(i) + "\"))\n";
							st+= "\t\t\t\tcrawlNew" + newParentName + capitalize(theList.get(i)) + "(childId, newchild);\n";
						}
						else
							hasTmg = true;
					}
				}
				st+= "\t\t}\n";
				if(hasTmg){
					st+= "\t\tcrawlNew" + newParentName + "Tmg(childId, child);\n";
				}
			}
			st+= "\t}\n";
		}
/*
	public void crawlRawCatalogService(int cleanCatalogId, Service rawService) throws Exception{
															// for applyDatasetAccessRules, for example
		Service cleanService = rawService.clone();				// DatasetAccess cleanDatasetAccess = rawDatasetAccess.clone();
																// cleanDatasetAccess.setDatasetId(parentId);	// has to be populated before inserting
		int cleanServiceId = DataAccess.insertService(cleanService);		// childId = DataAccess.insertDatasetAccess(cleanDatasetAccess);
		DataAccess.insertCatalogService(cleanCatalogId, cleanServiceId);														// can't be inserted before populating 
		cleanService.setServiceId(cleanServiceId);						// cleanDatasetAccess.setDatasetAccessId(childId);
		
		cleanService = Applier.applyCatalogServiceRules(cleanCatalogId, cleanService);
		
		if(cleanService == null){
			DataAccess.deleteCatalogService(cleanCatalogId, cleanServiceId);
			DataAccess.deleteService(cleanServiceId);
			return;
		}
		else
			DataAccess.updateService(cleanService);
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
	*/
		
		String tablename = capitalize(camelCase(parentName + "_" + childName));
		String parentId = capitalize(camelCase(parentName));
		String childId = "";
		if(!jointables.contains(parentName + "_" + childName))
			childId += capitalize(camelCase(parentName));
		childId += capitalize(camelCase(childName));
		String theObject = "";
		if(!jointables.contains(parentName + "_" + childName))
			theObject += capitalize(camelCase(parentName));
		theObject += capitalize(camelCase(childName));
		
		String realId = parentId;
		if(parentName.equals(childName))
			realId = "parent";
		
		st += "\tpublic void crawlRaw" + tablename + "(int clean" + realId + "Id, " + theObject + " raw" + theObject + ") throws Exception{\n";
		st += "\t\t" + theObject + " clean" + theObject + " = raw" + theObject + ".clone();\n";
		if(!jointables.contains(parentName + "_" + childName))
			st += "\t\tclean" + theObject + ".set" + parentId + "Id(clean" + parentId + "Id);\n";
		st += "\t\tint clean" + childId + "Id = DataAccess.insert" + theObject + "(clean" + theObject + ");\n";
		st += "\t\tclean" + theObject + ".set" + childId + "Id(clean" + childId + "Id);\n";
		st += "\n";
		st += "\t\tclean" + theObject + " = Applier.apply" + tablename + "Rules(clean" + realId + "Id, clean" + theObject + ");\n";
		st += "\n";
		st += "\t\tif(clean" + theObject + " == null){\n";
		st += "\t\t\tDataAccess.delete" + theObject + "(clean" + childId + "Id);\n";
		st += "\t\t\treturn;\n";
		st += "\t\t}\n";
		st += "\t\tDataAccess.update" + theObject + "(clean" + theObject + ");\n";
		if(theList.size() > 0){
			if(jointables.contains(parentName + "_" + childName))
				st += "\t\tDataAccess.insert" + tablename + "(clean" + realId + "Id, clean" + childId + "Id);\n";
			st += "\t\tint raw" + childId + "Id = raw" + theObject + ".get" + childId + "Id();\n";
			st += "\n";
		}
		for(int i = 0; i<theList.size(); i++){
			st += "\t\tArrayList<";
			if(!singletables.contains(theList.get(i)))
				st += theObject;
			st += capitalize(camelCase(theList.get(i)));
			st += "> " + camelCase(theList.get(i)) + "s = DataAccess.get";

			if(!singletables.contains(theList.get(i)))
				st += theObject;
			st += capitalize(camelCase(theList.get(i))) + "B";
			st += theObject;
			
			st += "(raw" + childId + "Id);\n";
			st += "\t\tfor(int i = 0; i<" + camelCase(theList.get(i)) + "s.size(); i++){\n";
			st += "\t\t\tcrawlRaw";

			if(!singletables.contains(childId))
				st += theObject;
			else
				st += childName;
			st += capitalize(camelCase(theList.get(i))) + "(clean" + childId + "Id, " + camelCase(theList.get(i)) + "s.get(i));\n";
			st += "\t\t}\n";
		}
		st+= "\t}\n";
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
