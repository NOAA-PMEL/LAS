// $Id: Options_compareBean.java,v 1.2 2002/06/26 18:17:53 sirott Exp $
package gov.noaa.pmel.tmap.las.luis;
public class Options_compareBean extends OptionsBean {
  public Options_compareBean() {
    setConstraintError("noconstraint_compare");
  }
  protected void setHandler(TemplateSession session) {
    mHandler = new FormParamHandler.CompareVariable(0,session);
  }
    
}
