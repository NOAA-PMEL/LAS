// $Id: DbaseObject.java,v 1.13 2004/12/17 14:57:17 rhs Exp $
package gov.noaa.pmel.tmap.las.luis.db;

import java.util.*;
import gov.noaa.pmel.tmap.las.luis.*;
import java.sql.*;
import java.lang.Class;
import java.lang.reflect.*;
import java.lang.Integer;

/**
 * Deserialize objects from and serialize objects to the MySQL database.
 * <p>A subclass of DbaseObject can specify which fields it wants
 * to deserialize from the database, and reflection is used to
 * stuff data deserialized from the database into the object.<p>
 * A object can also be serialized to a table. By default, it is assumed
 * that this table has a column named "sessionid" that associates the
 * row with a session id number
 * <p>Only supports String, int, and boolean SQL types
 * <p> Example:
 * <pre>
// Define an object to be associated with the MyObject table
public class MyObject extends DbaseObject {

  public String name;
  public String type;

  public MyObject(){
    super("MyObject"); // Associate this object with MyObject table in db
    addField("name");  // The name field will be deserialized from the name
                       // column of the MyObject table in the db
    addField("type");
  }
  ...
}

MyObject o = new MyObject();
o.deserialize("5");   // Get the row with an id of 5
System.out.println(o.name);

 * </pre>
 * @author $Author: rhs $
 * @version $Revision: 1.13 $
 */

public class DbaseObject implements IDbaseObject {
  static final String SESSIONID = "sessionid";
  String mTable;
  Vector mSerializable = new Vector();
  Vector mFields = new Vector();
  static final String OID = "oid";
  String oid;

  public String toString() {
    StringBuffer rval = new StringBuffer();
    rval.append(getClass().getName() + ":");
    for(Iterator i = mFields.iterator(); i.hasNext(); ){
      Field f = (Field)i.next();
      try {
	Object o = f.get(this);
	rval.append("\t" + f.getName() + ":");
	if (o == null){
	  rval.append("null\n");
	} else {
	  rval.append(o.toString() + "\n");
	}
      } catch (Exception e){
	e.printStackTrace();
      }
    }
    return rval.toString();
  }

  /**
   * Get the object id of this object
   * @return object id
   */
  public String getOid() {
    return this.oid;
  }

  /**
   * Set the object id of this object
   */
  public void setOid(String oid) { 
    this.oid = oid;
  }

  /**
   * Set the object id of this object
   */
  public void setOid(int oid) { 
    this.oid = Integer.toString(oid);
  }

  /**
   * Get the last oid in this table
   */
  public int getLastOid() throws SQLException {
    Connection c = ConnectionManager.getConnection();
    Statement stmt = c.createStatement();
    String command = "SELECT max(oid) from " + mTable;
    ResultSet rs = stmt.executeQuery(command);
    rs.next();
    int rval = rs.getInt(1);
    rs.close();
    stmt.close();
    ConnectionManager.freeConnection(c);
    return rval;
  }

  /**
   * Add a field from this class to the list of columns to be deserialized
   * from the table associated with this object.
   * <p>The field name must be the same as the column name
   * @param name field name to add
   */
  protected void addField(String name) {
    addField(name,name);
  }

  /**
   * Add a field from this class to the list of columns to be deserialized
   * from the table associated with this object.
   * <p>The field name does not have to be the same as the column name
   * @param name field name to add
   * @param column name of table to associate with field name
   */
  protected void addField(String fname, String cname){
    mSerializable.addElement(new DbaseMap(fname,cname));
  }

  /**
   * Instantiate a new DbaseObject to be associated with a db table
   * @param name name of db table
   */
  protected DbaseObject(String name){
    mTable = name;
  }

  protected void setup() throws SQLException {
    // Always add oid
    if (mSerializable.size() > 0){
      DbaseMap first = (DbaseMap)mSerializable.elementAt(0);
      if (!first.getColumnName().equals(OID)){
	mSerializable.insertElementAt(new DbaseMap("oid", OID),0);
      }
    } else {
      mSerializable.insertElementAt(new DbaseMap("oid", OID),0);
    }

    for (Iterator i = mSerializable.iterator(); i.hasNext(); ){
      Class c = this.getClass();
      Class orig_c = c;
      DbaseMap map = (DbaseMap)i.next();
      String field = map.getFieldName();
      while(c != null){
	try {
	  mFields.addElement(c.getDeclaredField(field));
	  break;
	} catch (Exception e){
	  c = c.getSuperclass();
	}
      }
      if (c == null){
	String mess = "Can't access or find field named '" +
	  field + "' in class or superclasses of " + orig_c.getName();
	throw new SQLException(mess);
      }
    }
  }


  protected String convert(Field f) throws SQLException {
    String result;
    String type = f.getType().getName();
    try {
      result = f.get(this).toString();
      if (result.equals("true")){
	result="1";
      } else if (result.equals("false")){
	result="0";
      }
    } catch (Exception e){
      String mess = "Can't get field named '" +
	f + "' in class " + this.getClass().getName();
      e.printStackTrace();
      throw new SQLException(mess);
    }
    if (type.equals("java.lang.String")){
      return "'" + result + "'";
    } else {
      return result;
    }
  }

  protected void deconvert(Field f, ResultSet rs, int column)
    throws SQLException {
    Object value;
    String type = f.getType().getName();
    if (type.equals("java.lang.String")){
      value = rs.getString(column);
    } else if (type.equals("int")){
      value = new Integer(rs.getInt(column));
    } else if (type.equals("boolean")){
      value = new Boolean(rs.getBoolean(column));
    } else if (type.equals("[B")){
      value = rs.getBytes(column);
    } else {
      throw new SQLException("Unsupported convert type: " + type);
    }
    try {
      f.set(this,value);
    } catch (Exception e){
      String mess = "Can't set field named '" +
	f + "' in class " + this.getClass().getName();
      e.printStackTrace();
      throw new SQLException(mess);
    }
  }


  Vector doDeserializeAggregate(String id, String fk,
			      String joinTable, String joinTableKey,
			      String addedConstraint)
    throws SQLException {
    setup();
    Connection c = ConnectionManager.getConnection();
    Statement stmt = c.createStatement();
    Vector rval = new Vector();

    Vector cvec = new Vector();
    for (Iterator i = mSerializable.iterator(); i.hasNext(); ){
      cvec.addElement(mTable + "." + ((DbaseMap)i.next()).getColumnName());
    }
    String fieldList = Utils.join(",", cvec);
    String command = "SELECT " + fieldList + " from " + mTable;
    if (joinTable != null){
      command += "," + joinTable;
    }

    if (!(id == null || fk == null)){
      command += " where ";
      if (joinTable != null && joinTableKey != null){
	command += mTable + "." + OID + "=" +
	  joinTable + "." + joinTableKey + " and ";
      }
      command += fk + "=" + id;
    }

    if (addedConstraint != null){
      command += " " + addedConstraint;
    }

    ResultSet rs = stmt.executeQuery(command);
    int count = 0;
    
    while (rs.next()){
      DbaseObject nc;
      try {
	nc = (DbaseObject)this.getClass().newInstance();
	nc.setup();
      } catch (Exception e){
	e.printStackTrace();
	throw new SQLException("Can't instantiate new object");
      }
      for (int i=1; i <= mSerializable.size(); i++){
	Field f = (Field)mFields.elementAt(i-1);
	nc.deconvert(f, rs, i);
      }
      nc.postDeserialize();
      rval.addElement(nc);
    } 

    rs.close();
    stmt.close();
    ConnectionManager.freeConnection(c);
    return rval;
  }

  /**
   * Deserialize a db table into a vector of objects.
   * <p>This method handles the case where there is a many to many relationship
   * between two tables. It is assumed that a separate join table is used
   * to quantify this relationship.
   * <p> For instance if the fk is "varid", the joinTable "AxisVariableJoin",
   * and the joinTableKey "axisid", the following SQL statement might be issued:
   * <pre>
   SELECT Axis... from Axis,AxisVariableJoin where Axis.oid=AxisVariableJoin.axisid and varid=1
   * </pre>
   * @return Vector of deserialized objects
   * @param id key to use with the "other" key in join table
   * @param fk foreign key of this object into the join table
   * @param joinTable name of the join table
   * @param joinTableKey other key to use in join table
   * @param addedConstraint any other SQL parameters
   */
  public Vector deserializeManyToMany(String id, String fk,
			      String joinTable, String joinTableKey,
			       String addedConstraint)
    throws SQLException {
    return doDeserializeAggregate(id,fk,joinTable,joinTableKey,
				  addedConstraint);
  }

  /**
   * @return Vector of deserialized objects
   * @param id key to use with the "other" key in join table
   * @param fk foreign key of this object into the join table
   * @param joinTable name of the join table
   * @param joinTableKey other key to use in join table
   * @see #deserializeManyToMany
   */
  public Vector deserializeManyToMany(String id, String fk,
			      String joinTable, String joinTableKey)
    throws SQLException {
    return doDeserializeAggregate(id,fk,joinTable,joinTableKey,
				  null);
  }

  /**
   * Deserialize a db table into a vector of objects.
   * <p>This method handles the case where there is a one to many relationship
   * between two tables. 
   * @return Vector of deserialized objects
   * @param id value of key
   * @param fk name of key to use
   * @param addedConstraint any other SQL parameters
   */
  public Vector deserializeAggregate(String id, String fk,
			      String addedConstraint) throws SQLException {
    return doDeserializeAggregate(id,fk,null,null,addedConstraint);
  }

  /**
   * Deserialize a db table into a vector of objects.
   * <p>All table rows are returned.
   * @return Vector of deserialized objects
   */
  public Vector deserializeAggregate() throws SQLException {
    return this.deserializeAggregate(null, null);
  }

  /**
   * Deserialize a db table into a vector of objects.
   * <p>This method handles the case where there is a one to many relationship
   * between two tables. 
   * @return Vector of deserialized objects
   * @param id value of key
   * @param fk name of key to use
   */
  public Vector deserializeAggregate(String id, String fk) throws SQLException {
    return this.deserializeAggregate(id, fk, null);
  }

  /**
   * Deserialize a row of a table into this object using key this.OID
   * @param id key value to deserialize
   */
  public void deserialize(String id) throws SQLException {
    deserialize(id, OID, null);
  }

  /**
   * Deserialize a row of a table into this object.
   * @param id key value to deserialize
   * @param key name of table column to use as key
   */
  public void deserialize(String id, String key) throws SQLException {
    deserialize(id,key,null);
  }

  /**
   * Hook to allow any extra object initialization that might be required
   * after fields in this object have been deserialized
   */
  public void postDeserialize() throws SQLException {
  }

  public int size(String addedConstraint) throws SQLException {
     String command;
     setup();
     Connection c = ConnectionManager.getConnection();
     Statement stmt = c.createStatement();
     if ( addedConstraint != null ) {
        command = "SELECT COUNT(*) from "+mTable+" where "+addedConstraint;
     }
     else {
        command = "SELECT COUNT(*) from "+mTable;
     }
     ResultSet rs = stmt.executeQuery(command);
     rs.next();
     return rs.getInt(1);
  }
  public int size() throws SQLException {
     return size(null);
  }


  public Vector selectColumn(String column, String addedConstraint, boolean distinct) throws SQLException {
     Log.debug(this, "Selecting column "+column+" from "+mTable);
     setup();
     Vector columnValues = new Vector();;
     Connection c = ConnectionManager.getConnection();
     Statement stmt = c.createStatement();
     String distinctQualifier = "";
     if ( distinct ) {
        distinctQualifier = "DISTINCT ";
     }

     Vector cvec = new Vector();
     int columnNumber = 0;
     for (ListIterator i = mSerializable.listIterator(); i.hasNext(); ){
        if ( column.equals(((DbaseMap)i.next()).getColumnName()) ) {
           columnNumber = i.nextIndex() - 1;
        }
     }

     String command = "SELECT "+distinctQualifier+mTable+"."+column+" from "+mTable;

     if (addedConstraint != null){
       command += " " + addedConstraint;
     }

     Log.debug(this, "Query: "+command);
     ResultSet rs = stmt.executeQuery(command);

     while ( rs.next() ) {
        Field f = (Field)mFields.elementAt(columnNumber);
        Object value;
        String type = f.getType().getName();
        if (type.equals("java.lang.String")){
          value = rs.getString(column);
        } else if (type.equals("int")){
          value = new Integer(rs.getInt(column));
        } else if (type.equals("boolean")){
          value = new Boolean(rs.getBoolean(column));
        } else if (type.equals("[B")){
          value = rs.getBytes(column);
        } else {
          throw new SQLException("Unsupported convert type: " + type);
        }
        Log.debug(this,"Adding value "+value+" to column vector.");
        columnValues.addElement(value);
     }
     rs.close();
     stmt.close();
     ConnectionManager.freeConnection(c);
     return columnValues;
  }

  public void remove (String id) throws SQLException {
     remove(id, OID, null);
  }
  
  public void remove (String id, String key) {
     remove (id, key);
  }

  public void remove (String id, String key, String addedConstraint)
     throws SQLException {
        setup();
        Connection c = ConnectionManager.getConnection();
        Statement stmt = c.createStatement();
        String command;
        command = "DELETE from "+mTable+" where "+key+"="+id;
        if (addedConstraint != null){
          command += " " + addedConstraint;
        }
        Log.debug(this, "SQL: "+command);
        int rs = stmt.executeUpdate(command);
     }

  /**
   * Deserialize a row of a table into this object.
   * @param id key value to deserialize
   * @param key name of table column to use as key
   * @param addedConstraint any other SQL parameters
   */
  public void deserialize(String id, String key, String addedConstraint)
    throws SQLException {
    setup();
    Connection c = ConnectionManager.getConnection();
    Statement stmt = c.createStatement();

    Vector cvec = new Vector();
    for (Iterator i = mSerializable.iterator(); i.hasNext(); ){
      cvec.addElement(((DbaseMap)i.next()).getColumnName());
    }
    String fieldList = Utils.join(",", cvec);
    String command = "SELECT " + fieldList + " from " + mTable +
      " where " + key + "=" + id;

    if (addedConstraint != null){
      command += " " + addedConstraint;
    }

    ResultSet rs = stmt.executeQuery(command);
    int count = 0;
    
    if (rs.next()){
      for (int i=1; i <= mSerializable.size(); i++){
	Field f = (Field)mFields.elementAt(i-1);
	deconvert(f, rs, i);
      }
    } else {
      throw new IdNotFoundException("No match for id=" + id + " for class " +
			     getClass().getName());
    }

    postDeserialize();
    rs.close();
    stmt.close();
    ConnectionManager.freeConnection(c);
  }

  /**
   * Serialize this object into a row of a table
   */
  public void serialize() throws SQLException {
    setup();
    Connection c = ConnectionManager.getConnection();
    Statement stmt = c.createStatement();

    Vector cvec = new Vector();
    for (Iterator i = mSerializable.iterator(); i.hasNext(); ){
      String colName = ((DbaseMap)i.next()).getColumnName();
      cvec.addElement(colName);
    }
    String fieldList = Utils.join(",", cvec);

    Vector valuesList = new Vector();
    int j=0;
    for (Iterator i = mSerializable.iterator(); i.hasNext(); ++j){
      String colName = ((DbaseMap)i.next()).getColumnName();
      Field f = (Field)mFields.elementAt(j);
      valuesList.add(convert(f));
    } 
    String values = Utils.join(",", valuesList);

    String command = "REPLACE " + mTable + "(" + fieldList +
      ") VALUES (" + values  + ")";
    stmt.executeUpdate(command);
    stmt.close();
    ConnectionManager.freeConnection(c);
  }

  class DbaseMap {
    String fname, cname;
    public DbaseMap(String fname, String colname){
      this.fname = fname;
      this.cname = colname;
    }
    public String getFieldName() {
      return this.fname;
    }
    public String getColumnName() {
      return this.cname;
    }
  }

}
