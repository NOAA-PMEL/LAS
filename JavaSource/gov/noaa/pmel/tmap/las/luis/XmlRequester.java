// $Id: XmlRequester.java,v 1.20 2004/12/08 01:01:41 rhs Exp $
package gov.noaa.pmel.tmap.las.luis;
import java.lang.String;
import java.lang.StringBuffer;
import java.util.*;
import java.sql.*;
import gov.noaa.pmel.tmap.las.luis.db.*;
import javax.servlet.*;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.regex.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * Compose an XML request to a LAS data server based on info submitted
 * from the LAS UI Constrain page
 * @author $Author: rhs $
 * @version $Version$
 */

public class XmlRequester {
  String mXmlOut;
  Vector mCustomVars = new Vector();
  FormParameters mParam, mOptionParams, mCustomParams;
  RegionConstraint mRc, mCompareRc;

  private XmlRequester(FormParameters param, RegionConstraint rc){
    mParam = param;
    mRc = rc;
  }

  public void setOptions(FormParameters params){
    mOptionParams = params;
  }

  public void setCustom(FormParameters params){
    mCustomParams = params;
  }

  public void setCompareConstraints(RegionConstraint rc){
    mCompareRc = rc;
  }

  private void genXml(FormParameters param, RegionConstraint rc,
                       FormParameters optionParams)
    throws SQLException{
    StringBuffer theXml = new StringBuffer();
    ICategory cat = rc.getCategory();
    Config config = cat.getConfig();
    theXml.append("<?xml version=\"1.0\"?>");

    XmlTag aTag = new XmlTag("lasRequest");
    aTag.addAtt("href", config.getHref());
    aTag.addAtt("package", config.getPackageid());

    XmlTag linkTag = new XmlTag("link");
    String output = param.get("output")[0];
    Vector op = Utils.split("/,/", output);
    String opname = (String) op.elementAt(0);
    Pattern p = Pattern.compile("^[A-Z].*");
    Matcher m = p.matcher(opname);
    Log.debug(this, "opname="+opname);
    if ( m.matches() ) {
       opname = "operation[@ID='"+opname+"']";
       Log.debug(this, "Op name matched, opname="+opname);
    }
    linkTag.addAtt("match", "/lasdata/operations/" + opname);
    aTag.addChild(linkTag);

    XmlTag propTag = new XmlTag("properties");
    XmlTag ferTag = new XmlTag("ferret");
    //XmlTag sizeTag = new XmlTag("size");
    //sizeTag.addText(".5");
    //ferTag.addChild(sizeTag);
    XmlTag viewTag = new XmlTag("view");
    viewTag.addText(rc.getView());
    ferTag.addChild(viewTag);
    XmlTag formTag = new XmlTag("format");
    formTag.addText((String)op.elementAt(1));
    ferTag.addChild(formTag);
    propTag.addChild(ferTag);
    aTag.addChild(propTag);

    if (optionParams != null){
       Log.debug(this,"Setting up option parameters.\n");
      setupCustom(optionParams, rc, ferTag);
    }

    XmlTag argTag = new XmlTag("args");
                                // Constraints
    boolean isConstrained = rc.getUI().isConstrained();
    if (isConstrained){
      for (int i=0; ;++i){
        Log.debug(this,"Constrain "+i);
        String nameRoot = "constrain" + i;
        String[] typeVal = param.get(nameRoot + "_type");
        if (typeVal == null || typeVal[0] == null){
           Log.debug(this,"Constrain "+i+" has no type.");
          break;
        }
        Vector cTags = new Vector();
        if (typeVal[0].equals("variable")){
          XmlTag cTag = getVariableConstraints(nameRoot, param);
          if (cTag != null){
            cTags.add(cTag);
          }
        } else if (typeVal[0].equals("text")){
          Vector tags = getTextConstraints(nameRoot,param);
          if (tags != null){
            cTags.addAll(tags);
          }
        } else if (typeVal[0].equals("textfield")){
          XmlTag cTag = getTextFieldConstraints(nameRoot, param);
          if (cTag != null){
            cTags.add(cTag);
          }
        } else {
          throw new SQLException("Unknown constraint type: " +
                                 typeVal[0]);
        }
        Log.debug(this, "adding "+cTags.size()+" constraints.");
        for (Iterator itr = cTags.iterator(); itr.hasNext(); ){
          XmlTag cTag = (XmlTag)itr.next();
          argTag.addChild(cTag);
        }
      }
    }


    ICategory[] cats = rc.getCategories();
    if (mCustomVars.size() > 0){ // Use these variables instead
      cats = new Category[mCustomVars.size()];
      for (int i=0; i < cats.length; ++i){
        cats[i] = (Category)mCustomVars.elementAt(i);
      }
    }

    // TODO -- Add error checking for number of variable arguments
    Log.debug(this, "Got " + cats.length + " variables");
    for (int i=0; i < cats.length; ++i){
      IVariableInfo theVarInfo = cats[i].getVariableInfo();
      String url1 = theVarInfo.getUrl1();
      String url2 = theVarInfo.getUrl2();
      linkTag = new XmlTag("link");
      linkTag.addAtt("match", "/lasdata/datasets/" + url1);
      argTag.addChild(linkTag);

      // Custom parameters
      if (mCustomParams != null){
        XmlTag custPropTag = new XmlTag("properties");
        XmlTag custReq = new XmlTag("customRequest");
        linkTag.addChild(custPropTag);
        custPropTag.addChild(custReq);
        for (Iterator itr = mCustomParams.keys(); itr.hasNext(); ){
          String key = (String)itr.next();
          String[] values = (String[])mCustomParams.get(key);
          if (values != null && values[0] != null){
            XmlTag ntag = new XmlTag(key);
            ntag.addText(values[0]);
            custReq.addChild(ntag);
          }
        }
      }

      addAnalysis(linkTag, cats[i]);

      if (url2 != null && !url2.equals("")){
        linkTag = new XmlTag("link");
        linkTag.addAtt("match", "/lasdata/datasets/" + url2);
        argTag.addChild(linkTag);
      }
    }

    IVariableInfo varInfo = cat.getVariableInfo();
    XmlTag regTag = new XmlTag("region");
    argTag.addChild(regTag);
    aTag.addChild(argTag);
    for (Iterator i = varInfo.getAxes().iterator(); i.hasNext(); ){
      IAxis ax = (IAxis)i.next();
      if (ax.isAnalysis()){
        continue;
      }

      // Prevent XML Request from including a vertical dimension if
      // the grid contains 0 or 1 vertical dimension.  This avoids
      // asking for the vertical dimension of the previous request.
      // Bugzilla #531.
      if (ax.getType().equals("z") && ax.getSize() <= 1){
        continue;
      }

      String type = ax.getType();
      XmlTag rtag;
      String loName = type + "_lo";
      String hiName = type + "_hi";
      // Make sure form value is present. Bugzilla 464
      String[] loArray = param.get(loName);
      String[] hiArray = param.get(hiName);
      if (rc.isRange(type)){
        if (loArray != null && hiArray != null){
          String lo = loArray[0]; 
          String hi = hiArray[0]; 
          if (lo != null && lo != "" && hi != null && hi != ""){
            rtag = new XmlTag("range");
            rtag.addAtt("low", lo);
            rtag.addAtt("high", hi);
            rtag.addAtt("type", type);
            regTag.addChild(rtag);
          }
        }
      } else {
        if (loArray != null){
          String lo = loArray[0]; 
          if (lo != null && lo != ""){
            rtag = new XmlTag("point");
            rtag.addAtt("v", lo);
            rtag.addAtt("type", type);
            regTag.addChild(rtag);
          }
        }
      }
    }

    if (mCompareRc != null){
     addCompare(argTag);
    }
        

    theXml.append(aTag.toString());
    mXmlOut = theXml.toString();

  }

  void addAnalysis(XmlTag linkTag, ICategory cat) throws SQLException {
    if (! (cat instanceof DerivedCategory)){
      return;
    }
    DerivedCategory dc = (DerivedCategory)cat;
    IVariableInfo theVarInfo = dc.getVariableInfo();
    XmlTag anTag = new XmlTag("analysis");
    linkTag.addChild(anTag);
    anTag.addAtt("label", cat.getName());
    if (dc.isLandMask()){
      anTag.addAtt("landmask", "true");
    }
    if (dc.isOceanMask()){
      anTag.addAtt("oceanmask", "true");
    }
    for (Iterator itr = theVarInfo.getAxes().iterator(); itr.hasNext(); ){
      IAxis aAxis = (IAxis)itr.next();
      if (aAxis.isAnalysis()){
        DerivedAxis daxis = (DerivedAxis)aAxis;
        XmlTag axTag = new XmlTag("axis");
        anTag.addChild(axTag);
        axTag.addAtt("op", daxis.getAnalysisType());
        axTag.addAtt("lo", daxis.getLo());
        axTag.addAtt("hi", daxis.getHi());
        axTag.addAtt("type", daxis.getType());
      }
    }
  }

  void addCompare(XmlTag argTag) throws SQLException {
    ICategory[] cats = mCompareRc.getCategories();
    FormParameters param = mParam;
    if (mCustomVars.size() > 0){ // Use these variables instead
      cats = new Category[mCustomVars.size()];
      for (int i=0; i < cats.length; ++i){
        cats[i] = (Category)mCustomVars.elementAt(i);
      }
    }

    ICategory cat = mCompareRc.getCategory();

    // TODO -- Add error checking for number of variable arguments
    Log.debug(this, "Got " + cats.length + " variables");
    XmlTag linkTag = new XmlTag("link");
    addAnalysis(linkTag, cat);

    IVariableInfo theVarInfo = cats[0].getVariableInfo();
    String url1 = theVarInfo.getUrl1();
    linkTag.addAtt("match", "/lasdata/datasets/" + url1);
    argTag.addChild(linkTag);

    IVariableInfo varInfo = cat.getVariableInfo();
    XmlTag regTag = new XmlTag("region");
    argTag.addChild(regTag);
    String prefix = "compare_";
    for (Iterator i = varInfo.getAxes().iterator(); i.hasNext(); ){
      IAxis ax = (IAxis)i.next();
      String type = ax.getType();
      if (!mCompareRc.isRange(type)){
        XmlTag rtag;
        String loName = prefix + type + "_lo";
        String hiName = prefix + type + "_hi";
        if (param.get(loName) != null){
          rtag = new XmlTag("point");
          rtag.addAtt("v", param.get(loName)[0]);
          rtag.addAtt("type", type);
          regTag.addChild(rtag);
        }
      }
    }
  }

  XmlTag getVariableConstraints(String nameRoot, FormParameters param){
    XmlTag cTag = null;
    String[] selectVal = param.get(nameRoot + "_select");
    boolean useThisConstraint = param.get(nameRoot + "_apply")!=null &&
      param.get(nameRoot + "_apply")[0].equals("apply");
    if (useThisConstraint){
      cTag = new XmlTag("constraint");
      String cVar =  "/lasdata/datasets/" + selectVal[0];
          
      String cOp = param.get(nameRoot + "_ops")[0];
      String cText = param.get(nameRoot + "_text")[0];
      cTag.addAtt("type", "variable");
      cTag.addAtt("op", cOp);
          
      XmlTag cLinkTag = new XmlTag("link");
      cLinkTag.addAtt("match", cVar);
      cTag.addChild(cLinkTag);
          
      XmlTag cValueTag = new XmlTag("v");
      cValueTag.addText(cText);
      cTag.addChild(cValueTag);
    }
    return cTag;
  }

  XmlTag getTextFieldConstraints(String nameRoot, FormParameters param){
    XmlTag cTag = null;
    boolean useThisConstraint = param.get(nameRoot + "_apply")!=null &&
      param.get(nameRoot + "_apply")[0].equals("apply");
    if (useThisConstraint){
      cTag = new XmlTag("constraint");
      cTag.addAtt("type", "textfield");
      String cText = param.get(nameRoot + "_text")[0];
      cTag.addAtt("value", cText);
    }
    return cTag;
  }

  Vector getTextConstraints(String nameRoot, FormParameters param){
    Vector cTags = new Vector();
// debug
    String[] yo = param.get(nameRoot + "_apply");
    if ( yo != null ) {
       Log.debug(this,"looking at "+nameRoot+" with "+param.get(nameRoot + "_apply")[0]);
    }
    else {
       Log.debug(this,"apply param null for "+nameRoot);
    }
// end debug
    boolean useThisConstraint = param.get(nameRoot + "_apply")!=null &&
      param.get(nameRoot + "_apply")[0].equals("apply");
    if (useThisConstraint){
      // Get number of widgets
      int numWidgets;
      for (numWidgets=0; ;numWidgets++){
        String pname = nameRoot + "_select_" + numWidgets;
        String[] selectVal = param.get(pname);
        if (selectVal == null || selectVal[0] == null){
          break;
        }
      }
      int numWidgetsm1 = numWidgets-1;
      // Last widget might be a multiple select. Each selection should
      // result in a new constraint
      String name = nameRoot + "_select_" + numWidgetsm1;
      String[] multiSelects = param.get(name);

      for (int j=0; j < multiSelects.length; ++j){
        XmlTag cTag = new XmlTag("constraint");
        cTag.addAtt("type", "text");
        for (int i=0; i < numWidgets-1;i++){
          String pname = nameRoot + "_select_" + i;
          String[] selectVal = param.get(pname);
          Log.debug(this, "Constraint: " +  pname + ":" + selectVal);
          Log.debug(this, "Constraint value: " + i + ":" + selectVal[0]);
          XmlTag vTag = new XmlTag("v");
          vTag.addText(selectVal[0]);
          cTag.addChild(vTag);
        }
        XmlTag vTag = new XmlTag("v");
        vTag.addText(multiSelects[j]);
        cTag.addChild(vTag);
        cTags.add(cTag);
      }
    }
    return cTags;
  }

  // TODO -- escape weird tags
  void setupCustom(FormParameters optionParams, RegionConstraint rc,
                   XmlTag propTag) throws SQLException {
    for (Iterator itr = optionParams.keys(); itr.hasNext();){
      String key = (String)itr.next();
      String[] values = optionParams.get(key);
    /* Following is special hack that supports variable names as options
     * The string _var_ is prepended to the name to flag this as 
     * a special property. When XmlRequester is called, it looks for
     * properties with this prepended string and substitutes the variable
     * in the request to the LAS data server. 
     */
      if (key.startsWith("_var_")) {
        ICategory theCat = null;
        ICategory[] cats = rc.getCategories();
        for (int i=0; i < cats.length; ++i){
          if (cats[i].getOid().equals(values[0])){
            theCat = cats[i];
            break;
          }
        }
        if (theCat == null){
          throw new SQLException("Can't find requested custom variable");
        }
        Log.debug(this, "Adding custom variable:" + theCat.getName());
        mCustomVars.addElement(theCat);
      } else {
          // These parameters are now a jumble of contrain page
          // and option page parameters.
          // If the deserialize works, it's a legit option.
          try {
             OptionsWidget opw = new OptionsWidget();
             opw.deserialize(Utils.quote(key), "name");
             for (int i=0; i < values.length; ++i){
                XmlTag newTag = new XmlTag(key);
                if ( key.equals("expression") ) {
                   try {
                      newTag.addText( URLEncoder.encode(values[i], "UTF-8") );
                   } catch (UnsupportedEncodingException e) {
                      // Well, we tried...
                   }
                } else {
                   newTag.addText(values[i]);
                }
                propTag.addChild(newTag);
             }
          } catch (IdNotFoundException e) {
             // Not legit, go on...
                ;
          }
      }
    }
  }

  public String toString() {
    if (mXmlOut == null){
      try {
        genXml(mParam, mRc, mOptionParams);
      } catch (SQLException se){
        Log.debug(this, "SQLException:" + se.getMessage());
        se.printStackTrace();
      }
    }
    return mXmlOut;
  }
  
  static public XmlRequester getInstance(FormParameters params,
                                         RegionConstraint rc){
    return new XmlRequester(params, rc);
  }

  class XmlTag {
    StringBuffer mStart = new StringBuffer();
    String mTagName, mText;
    Properties mAtts = new Properties();
    Vector mChildren = new Vector();
    public XmlTag(String tagName) {
      mTagName = tagName;
      mStart.append("<" + tagName + " ");
    }
    public void addAtt(String name, String value) {
      mAtts.put(name,value);
    }
    public void addText(String text) {
      mText = text;
    }
    public String toString() {
      for (Enumeration e = mAtts.keys(); e.hasMoreElements(); ){
        String name = (String)e.nextElement();
        String value = (String)mAtts.get(name);
        mStart.append(name).append("=\"").append(value).append("\" ");
      }
      if (mText == null && mChildren.size() == 0){
        return mStart.append("/>").toString();
      }
      mStart.append(">");
      if (mText != null){
        mStart.append(mText);
      }
      
      StringBuffer children = new StringBuffer();
      for (Iterator i = mChildren.iterator(); i.hasNext(); ){
        children.append(i.next().toString());
      }
      return mStart.append(children).append("</").append(mTagName).
        append(">").toString();
    }
    public void addChild(XmlTag tag) {
      mChildren.addElement(tag);
    }
  }
}
