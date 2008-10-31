package gov.noaa.pmel.tmap.addxml;

import java.util.*;

import org.jdom.*;

/**
 * <p>Title: addXML</p>
 *
 * <p>Description: Reads local or OPeNDAP netCDF files and generates LAS XML
 * configuration information.</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: NOAA/PMEL/TMAP</p>
 *
 * @author RHS
 * @version 1.0
 */
public class DatasetBean extends LasBean {
  private String name;
  private String doc;
  private String url;
  private Vector variables = new Vector();
  private String comment;
  private String version;
  private String creator;
  private String group_name;
  private String group_type;
  private String group_id;
  private String update_time;
  private String update_interval;
  private String created;
  private String expires;
  private long nextUpdate;

  /**
 * @return the nextUpdate
 */
public long getNextUpdate() {
	return nextUpdate;
}

/**
 * @param nextUpdate the nextUpdate to set
 */
public void setNextUpdate(long nextUpdate) {
	this.nextUpdate = nextUpdate;
}

public DatasetBean() {
  }

public void addAllVariables(Vector var) {
    if (variables == null) {
      variables = new Vector();
    }
    this.variables.addAll(var);
  }

public void addVariable(VariableBean var) {
    if (variables == null) {
      variables = new Vector();
    }
    this.variables.add(var);
  }

  public String getComment() {
    return comment;
  }

  /**
 * @return the created
 */
public String getCreated() {
	return created;
}

  public String getCreator() {
    return creator;
  }

  public String getDoc() {
    return doc;
  }

  /**
 * @return the expires
 */
public String getExpires() {
	return expires;
}

  /**
 * @return the group_id
 */
public String getGroup_id() {
	return group_id;
}

  /**
 * @return the group_name
 */
public String getGroup_name() {
	return group_name;
}

  /**
 * @return the group_type
 */
public String getGroup_type() {
	return group_type;
}

  public String getName() {
    return name;
  }

  /**
 * @return the update_interval
 */
public String getUpdate_interval() {
	return update_interval;
}

  /**
 * @return the update_time
 */
public String getUpdate_time() {
	return update_time;
}

  public String getUrl() {
    return url;
  }

  public Vector getVariables() {
    return variables;
  }

  public String getVersion() {
    return version;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  /**
 * @param created the created to set
 */
public void setCreated(String created) {
	this.created = created;
}

  public void setCreator(String creator) {
    this.creator = creator;
  }

public void setDoc(String doc) {
    this.doc = doc;
  }

/**
 * @param expires the expires to set
 */
public void setExpires(String expires) {
	this.expires = expires;
}

/**
 * @param group_id the group_id to set
 */
public void setGroup_id(String group_id) {
	this.group_id = group_id;
}

/**
 * @param group_name the group_name to set
 */
public void setGroup_name(String group_name) {
	this.group_name = group_name;
}

/**
 * @param group_type the group_type to set
 */
public void setGroup_type(String group_type) {
	this.group_type = group_type;
}

public void setName(String name) {
    this.name = name;
  }

/**
 * @param update_interval the update_interval to set
 */
public void setUpdate_interval(String update_interval) {
	this.update_interval = update_interval;
}

/**
 * @param update_time the update_time to set
 */
public void setUpdate_time(String update_time) {
	this.update_time = update_time;
}

public void setUrl(String url) {
    this.url = url;
  }

public void setVariables(Vector variables) {
    this.variables = variables;
  }

public void setVersion(String version) {
    this.version = version;
  }

public Element toXml() {
    Element dataset = new Element(this.getElement());
    if ( comment != null ) {
      dataset.addContent(new Comment(comment));
    }
    dataset.setAttribute("name",this.getName());
    
    if (this.getGroup_name() != null) {
    	dataset.setAttribute("group_name", this.getGroup_name());
    }
    
    if ( this.getGroup_type() != null ) {
    	dataset.setAttribute("group_type", this.getGroup_type());
    }
    
    if ( this.getGroup_id() != null ) {
    	dataset.setAttribute("group_id", this.getGroup_id());
    }
    
    if ( this.getUpdate_time() != null ) {
    	dataset.setAttribute("update_time", this.getUpdate_time());
    }

    if ( this.getUpdate_interval() != null ) {
    	dataset.setAttribute("update_interval", this.getUpdate_interval());
    }
    
    if ( this.getCreated() != null ) {
    	dataset.setAttribute("created", this.getCreated());
    }
    
    if ( this.getExpires() != null ) {
    	dataset.setAttribute("expires", this.getExpires());
    }
    
    if ( this.getUrl() != null ) {
      dataset.setAttribute("url", this.getUrl());
    }
    Element variablesElement = new Element("variables");
    Iterator vars = variables.iterator();
    while (vars.hasNext()) {
      VariableBean var = (VariableBean) vars.next();
      Element varElement = var.toXml();
      variablesElement.addContent(varElement);
    }
    dataset.addContent(variablesElement);
    return dataset;
  }
}
