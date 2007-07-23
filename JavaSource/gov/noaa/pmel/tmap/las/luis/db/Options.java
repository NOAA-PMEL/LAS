package gov.noaa.pmel.tmap.las.luis.db;

import java.sql.SQLException;
import java.util.Vector;
import gov.noaa.pmel.tmap.las.luis.*;



/**
 * @author $Author: sirott $
 * @version $Version$
 */
public class Options   extends DbaseObject {

  String title;
  String help;
  String type;
  String op;
  boolean escapedHelp = false;
  public Options() {
    super("Options");
    addField("op");
    addField("title");
    addField("type");
    addField("help");
  }
  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  public void setHelp(String help) {
    this.help = help;
  }
  public String getHelp() {
    if (!escapedHelp){
      help = Utils.substitute("s/\\'/\\\\'/g", help);
      escapedHelp = true;
    }
    return help;
  }
  
  /**
   * Gets the value of type
   *
   * @return the value of type
   */
  public String getType() {
    return this.type;
  }

  /**
   * Sets the value of type
   *
   * @param argType Value to assign to this.type
   */
  public void setType(String argType){
    this.type = argType;
  }

  /**
   * Gets the value of op
   *
   * @return the value of op
   */
  public String getOp() {
    return this.op;
  }

  /**
   * Sets the value of op
   *
   * @param argOp Value to assign to this.op
   */
  public void setOp(String argOp){
    this.op = argOp;
  }

  public OptionsWidget getOptionsWidget() throws SQLException {
    OptionsWidget widget = new OptionsWidget();
    return (OptionsWidget)widget.deserializeAggregate(oid, "option_id").elementAt(0);
  }

  static public void main(String[] args) throws java.sql.SQLException {
    Options options = new Options();
    options.deserialize("1");
    OptionsWidget widget = options.getOptionsWidget();
    System.out.println(widget);
    for (java.util.Iterator i = widget.getItems().iterator(); i.hasNext(); ){
      System.out.println(i.next());
    }
    
  }
}
