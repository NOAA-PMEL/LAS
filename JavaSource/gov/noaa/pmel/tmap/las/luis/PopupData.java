// $Id: PopupData.java,v 1.2 2003/07/02 20:11:14 rhs Exp $
package gov.noaa.pmel.tmap.las.luis;
public class PopupData {
  boolean mDoPopup = false;
  String mTemplate = "data_popup";
  String mNewOutputWindowName = "data";

  public void setDoPopup(boolean arg) {
    mDoPopup = arg;
  }

  public boolean doPopup() {
    return mDoPopup;
  }

  public void setTemplate(String arg){
    mTemplate = arg;
  }

  public String getTemplate() {
    return mTemplate;
  }

  public void setNewOutputWindowName(String arg){
    this.mNewOutputWindowName = arg;
  }

  public String getNewOutputWindowName() {
    return this.mNewOutputWindowName;
  }

  public boolean isAlwaysNew() {
     if (mNewOutputWindowName.equals("_blank")) {
        return true;
     }
     else {
        return false;
     }
  }

  public boolean isSingleNew() {
     if (mNewOutputWindowName.equals("data")) {
        return true;
     }
     else {
        return false;
     }
  }

}
