package gov.noaa.pmel.tmap.las.luis;
import gov.noaa.pmel.tmap.las.luis.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.lang.Boolean;

public class SessionObject {
  FormParameters constrainState;
  Boolean useOptions;
  FormParameters variables;
  FormParameters[] compareVariables = new FormParameters[2];
  FormParameters options;
  FormParameters analysis;
  FormParameters customParams;
  String lastTemplateName;
  boolean isCustom = false;
  
  /**
   * Gets the value of constrainState
   *
   * @return the value of constrainState
   */
  public FormParameters getConstrainState() {
    return this.constrainState;
  }

  /**
   * Sets the value of constrainState
   *
   * @param argConstrainState Value to assign to this.constrainState
   */
  public void setConstrainState(FormParameters argConstrainState){
    this.constrainState = argConstrainState;
  }

  /**
   * Gets the value of useOptions
   *
   * @return the value of useOptions
   */
  public Boolean getUseOptions() {
    return this.useOptions;
  }

  /**
   * Sets the value of useOptions
   *
   * @param argUseOptions Value to assign to this.useOptions
   */
  public void setUseOptions(Boolean argUseOptions){
    this.useOptions = argUseOptions;
  }

  /**
   * Gets the value of variables
   *
   * @return the value of variables
   */
  public FormParameters getVariables() {
    return this.variables;
  }

  /**
   * Sets the value of variables
   *
   * @param argVariables Value to assign to this.variables
   */
  public void setVariables(FormParameters argVariables){
    this.variables = argVariables;
  }

  /**
   * Gets the value of compare variables
   *
   * @return the value of compare variables
   */
  public FormParameters getCompareVariables(int index) {
    return this.compareVariables[index];
  }

  /**
   * Sets the value of compare variables
   *
   * @param argVariables Value to assign to this.compare variables
   */
  public void setCompareVariables(int index, FormParameters argVariables){
    this.compareVariables[index] = argVariables;
  }

  public void removeCompareVariables(){
    this.compareVariables[0] = null;
    this.compareVariables[1] = null;
  }

  /**
   * Gets the value of options
   *
   * @return the value of options
   */
  public FormParameters getOptions() {
    return this.options;
  }

  /**
   * Sets the value of options
   *
   * @param argOptions Value to assign to this.options
   */
  public void setOptions(FormParameters argOptions){
    this.options = argOptions;
  }

  /**
   * Gets the value of analysis
   *
   * @return the value of analysis
   */
  public FormParameters getAnalysis() {
    return this.analysis;
  }

  /**
   * Sets the value of analysis
   *
   * @param argAnalysis Value to assign to this.analysis
   */
  public void setAnalysis(FormParameters argAnalysis){
    this.analysis = argAnalysis;
  }

  /**
   * Gets the value of lastTemplateName
   *
   * @return the value of lastTemplateName
   */
  public String getLastTemplateName() {
    return this.lastTemplateName;
  }

  /**
   * Sets the value of lastTemplateName
   *
   * @param argLastTemplateName Value to assign to this.lastTemplateName
   */
  public void setLastTemplateName(String argLastTemplateName){
    if (!argLastTemplateName.equals("data_popup.vm") &&
        !argLastTemplateName.equals("data_compare_popup.vm") &&
        !argLastTemplateName.equals("metadata.vm") ) {
      this.lastTemplateName = argLastTemplateName;
    }
  }

  public void setCustom(boolean val){
    isCustom = val;
  }

  public boolean isCustom() {
    return isCustom;
  }

  public void setCustomFormParams(FormParameters params){
    customParams = params;
  }

  public FormParameters getCustomFormParameters() {
    return customParams;
  }
}
