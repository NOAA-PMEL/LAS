// $Id: FormHandler.java,v 1.14 2005/03/21 23:47:40 callahan Exp $
package gov.noaa.pmel.tmap.las.luis;


import java.lang.String;
import java.lang.StringBuffer;
import java.util.*;
import java.text.SimpleDateFormat;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import org.apache.oro.text.perl.Perl5Util;
import javax.servlet.ServletException;
import gov.noaa.pmel.tmap.las.luis.db.*;

/**
 * Used by Velocity templates to create HTML form elements
 * 
 */

public class FormHandler {
  String mURI, mErrMess = "";;
  HttpServletRequest mReq;
  Perl5Util mRE = new Perl5Util();
  Hashtable mElements = new Hashtable();
  FormParameters mParams = null;
  static int mWidgetCounter = 0;

  public interface IFormWidget {
    public void addProperty(String name, String value);
    public void addProperty(String name);
    public String toString();
  }

  public class AbstractFormWidget implements IFormWidget {
    Properties mProperties = new Properties();
    String mName;
    AbstractFormWidget(String name, String properties){
      mName = name;
      addProperty("name", name);
      if (properties != null){
        addProperties(properties);
      }
    }
    public void addProperty(String name, String value){
      if (value == null){
        value = "";
      }
      mProperties.put(name,value);
    }

    public void addProperty(String name){
      addProperty(name,null);
    }

    void addProperties(String in){
      Vector v = new Vector();
      in = mRE.substitute("s/\\s+//g", in);
      mRE.split(v, "/\\,/",in);
      for (Iterator i = v.iterator(); i.hasNext();){
        String name_value = (String)i.next();
        String name, value = null;
        Vector nv = new Vector();
        mRE.split(nv, "/\\=/", name_value);
        name = (String)nv.elementAt(0);
        if (nv.size() > 1){
          value = (String)nv.elementAt(1);
        } else {
          value = "";
        }
        mProperties.put(name,value);
      }
    }

    protected String[] getParamValues(){
      String[] rval = null;
                                // Don't restore state for nexturl field
      if (!mName.equals("nexturl")){
        rval = mParams.get(mName);
      }
      return rval;
    }

    public String toString() {return ""; }

  }

  class FormWidget extends AbstractFormWidget {
    String mRep;
    String mValue;
    FormWidget(String initial, String name, String value,
               String properties){
      super(name,properties);
      mRep = initial;
      if (value != null){
        addProperty("value", value);
        mValue = value;
      }
      mElements.put(name,this);
    }
    FormWidget(String initial, String name, String value){
      this(initial,name,value, null);
    }
    FormWidget(String initial, String name){
      this(initial,name,null, null);
    }
    public String toString() {
      StringBuffer out = new StringBuffer(mRep);
      for (Enumeration e = mProperties.propertyNames();
           e.hasMoreElements();){
        String name = (String)e.nextElement();
        out.append(name);
        String value = mProperties.getProperty(name);
        if (!value.equals("")){
          out.append("=\"").append(value).append("\"");
        }
        out.append(" ");
      }
      out.append(">");
      return out.toString();
    }

    void setRadioOrCheckState() {
      if (mParams != null){
        String values[] = getParamValues();
        if (values != null && mValue != null){
          for (int i=0; i < values.length; ++i){
            if (values[i] != null && values[i].equals(mValue)){
              addProperty("checked");
              break;
            }
          }
        }
      }
    }

    void setValue(){
      if (mParams != null){
        String values[] = getParamValues();
        if (values != null){
          addProperty("value",values[0]);
        }
      }
    }

  }

  class SelectWidget extends FormWidget {
    String[] mLabels;
    String[] mValues;
    String mLastValue;
    String mName;
    int mSelectedIndex = 0;
    int mDefaultIndexLo = 0;
    int mDefaultIndexHi = 100;
    String mDefaultPosition = "first";
    String mDefaultType = "lo";

    SelectWidget(String name, String properties, Vector widgets){
      super("<select ", name, null, properties);
      mName = name;
      mLabels = new String[widgets.size()];
      mValues = new String[widgets.size()];
      int i=0;
      for (Iterator it = widgets.iterator(); it.hasNext(); i++){
        IWidgetItem wi = (IWidgetItem)it.next();
        mLabels[i] = wi.getLabel();
        mValues[i] = wi.getValue();
      }
      if (mParams != null){
        String pvalues[] = getParamValues();
        if (pvalues != null){
          mLastValue = pvalues[0];
        }
      }
    }

    public void setDefaults(int index, int lo, int hi, 
          String default_position, String type){
      // Keep track of the hi and low range as well as the selected index
      // to prevent a previous selection from forcing the menu out of range.
      mSelectedIndex = index;
      mDefaultIndexLo = lo;
      mDefaultIndexHi = hi;
      mDefaultPosition = default_position;
      mDefaultType = type;
    }

    public int getSelectedIndex() {
      // Two ways an option can be selected. If the value matches
      // mLabel, and if setDefaultSelected was called. The first takes
      // precedence over the second

      int selectedIndex = -1;
      for (int i=0; i < mLabels.length; ++i){
        if (mLastValue!=null && mLastValue.equals(mValues[i])){
           Log.debug(this, "Reset widget "+mName+" to "+i+" because of mLastValue.");
          selectedIndex = i;
          break;
        }
      }

      // If no value was previously set, use the index for this widget.

      if (selectedIndex == -1){
        selectedIndex = mSelectedIndex;
      }

      // If the index is out of range reset it to the default position
      if ( selectedIndex < mDefaultIndexLo || selectedIndex > mDefaultIndexHi ) {
           String debugit = "Reset widget "+mName+" from "+selectedIndex+" to ";
           if ( mDefaultType.equals("lo") ) {
              if ( mDefaultPosition.equals("last") ) {
                 selectedIndex = mDefaultIndexHi;
                 debugit += +mDefaultIndexHi;
              }
              else {
                 selectedIndex = mDefaultIndexLo;
                 debugit += +mDefaultIndexHi;
              }
           }
           else {
              selectedIndex = mDefaultIndexHi;
              debugit += +mDefaultIndexHi;
           }
           Log.debug(this, debugit);
      }

      return selectedIndex;
      
    }

    public String toString() {
      StringBuffer rval = new StringBuffer(super.toString());

      int selectedIndex = getSelectedIndex();

      for (int i=0; i < mLabels.length; ++i){
        String selected = "";
        if (i == selectedIndex){
          selected = " selected ";
        }
        rval.append("\n<option value=\"").append(mValues[i]).append("\" ")
          .append(selected).append(">")
          .append(mLabels[i]).append("</option>");
      }
      rval.append("\n</select>");
      return rval.toString();
    }
  }

  class RadioOrCheckWidget extends AbstractFormWidget {
    String[] mLabels;
    String[] mValues;
    String mLastValue;
    int mSelectedIndex = 0;
    boolean mIsRadio = true;

    RadioOrCheckWidget(String name, String properties, Vector widgets){
      super(name, properties);
      mLabels = new String[widgets.size()];
      mValues = new String[widgets.size()];
      int i=0;
      for (Iterator it = widgets.iterator(); it.hasNext(); i++){
        IWidgetItem wi = (IWidgetItem)it.next();
        mLabels[i] = wi.getLabel();
        mValues[i] = wi.getValue();
      }
      if (mParams != null){
        String pvalues[] = getParamValues();
        if (pvalues != null){
          mLastValue = pvalues[0];
        }
      }
    }

    public void setDefaultSelected(int index){
      mSelectedIndex = index;
    }

    public void setCheck() {
      mIsRadio = false;
    }

    public void setRadio() {
      mIsRadio = true;
    }

    public String toString() {
      Vector radios = new Vector();
      StringBuffer rval = new StringBuffer();
      // Two ways an option can be selected. If the value matches
      // mLabel, and if setDefaultSelected was called. The first takes
      // precedence over the second

      int selectedIndex = -1;
      for (int i=0; i < mLabels.length; ++i){
        if (mLastValue!=null && mLastValue.equals(mValues[i])){
          selectedIndex = i;
          break;
        }
      }
      if (selectedIndex == -1){
        selectedIndex = mSelectedIndex;
      }

      for (int i=0; i < mLabels.length; ++i){
        FormWidget widget;
        if (mIsRadio){
          widget = new Radio(mName, mValues[i], null);
        } else {
          widget = new CheckBox(mName, mValues[i], null);
        }
        if (i == selectedIndex){
          widget.addProperty("checked");
        }
        // TODO -- add properties to radio buttons
        rval.append(widget.toString()).append(mLabels[i]);
      }
      rval.append(" ");
      return rval.toString();
    }
  }

  /**
   * Create widgets to allow user to specify constraints
   */

  class ConstraintSelect {
    IFormWidget mSelect;
    Text mText;
    InputWidget mCheck;
    String mFormName, mBaseName;
    String mExtra;
    String mLabel;
    Hidden mType;
    final String mCompareOps[] = {"<", "<=", "=", ">=", ">"};
    final String mCompareOpsValues[] = {"lt", "le", "eq", "ge", "gt"};
    final static int MAX_LABEL_LENGTH = 32;
    Vector mCompareItems = new Vector();
    SelectWidget mCompare;
    Vector mResults = new Vector();

    public ConstraintSelect(String formName,Integer index,
                            Constraint constraint){
      mLabel = constraint.getLabel();
      mFormName = formName;
      mBaseName = "constrain" + index;
      mType = new Hidden(mBaseName + "_type", constraint.getType(), null);
      if (constraint.isRequired()){
        mCheck = new Hidden(mBaseName + "_apply","apply",null);
      } else {
        mCheck = new CheckBox(mBaseName + "_apply", "apply", null);
      }
      mExtra = constraint.getExtra();
    }

    public Vector getWidgetList() {
      return mResults;
    }

  }

  class VariableConstraintSelect extends ConstraintSelect {
    VariableConstraintSelect(String formName,Integer index,
                             Constraint constraint, Category category)
      throws SQLException,ServletException {

      super(formName,index,constraint);
      Vector datasets  = DatasetItem.getItems(category.getParentid());
      Vector widgetItems = new Vector();
      for (Iterator i = DatasetItem.getItems(category.getParentid()).iterator();
           i.hasNext(); ){
        IWidgetItem wi = new DbaseWidgetItem("");
        DatasetItem ditem = (DatasetItem)i.next();
        String label = ditem.getName();
        if (label.length() > MAX_LABEL_LENGTH){
          label = label.substring(0, MAX_LABEL_LENGTH);
          label += "...";
        }
        wi.setLabel(label);
        wi.setValue(ditem.getVariableInfo().getUrl1());
        widgetItems.addElement(wi);
      }

      String wname = mBaseName + "_select";
      if (constraint.getStyle().equals("check")){
        mSelect = new RadioOrCheckWidget(wname,null,widgetItems);
      } else {
        mSelect = new SelectWidget(wname,null,widgetItems);
      }
      mSelect.addProperty("size", constraint.getSize());
      
      mText = new Text(mBaseName + "_text", null, "size=12");
      
      for (int i=0; i < mCompareOps.length; ++i){
        IWidgetItem wi = new DbaseWidgetItem("");
        wi.setValue(mCompareOpsValues[i]);
        wi.setLabel(mCompareOps[i]);
        mCompareItems.addElement(wi);
      }
      mCompare = new SelectWidget(mBaseName + "_ops", null, mCompareItems);
      convertToStrings();
    }
    VariableConstraintSelect(String formName,Integer index,
    		Constraint constraint, DerivedCategory category)
    		throws SQLException,ServletException {

    	super(formName,index,constraint);
    	Vector datasets  = DatasetItem.getItems(category.getParentid());
    	Vector widgetItems = new Vector();
    	for (Iterator i = DatasetItem.getItems(category.getParentid()).iterator();
    	i.hasNext(); ){
    		IWidgetItem wi = new DbaseWidgetItem("");
    		DatasetItem ditem = (DatasetItem)i.next();
    		String label = ditem.getName();
    		if (label.length() > MAX_LABEL_LENGTH){
    			label = label.substring(0, MAX_LABEL_LENGTH);
    			label += "...";
    		}
    		wi.setLabel(label);
    		wi.setValue(ditem.getVariableInfo().getUrl1());
    		widgetItems.addElement(wi);
    	}

    	String wname = mBaseName + "_select";
    	if (constraint.getStyle().equals("check")){
    		mSelect = new RadioOrCheckWidget(wname,null,widgetItems);
    	} else {
    		mSelect = new SelectWidget(wname,null,widgetItems);
    	}
    	mSelect.addProperty("size", constraint.getSize());

    	mText = new Text(mBaseName + "_text", null, "size=12");

    	for (int i=0; i < mCompareOps.length; ++i){
    		IWidgetItem wi = new DbaseWidgetItem("");
    		wi.setValue(mCompareOpsValues[i]);
    		wi.setLabel(mCompareOps[i]);
    		mCompareItems.addElement(wi);
    	}
    	mCompare = new SelectWidget(mBaseName + "_ops", null, mCompareItems);
    	convertToStrings();
    }
    public void convertToStrings () {
      if (mCheck instanceof CheckBox){
        mResults.add("<b>Apply</b>:");
      }
      mResults.add(mCheck.toString());
      mResults.add(mType.toString());
      if (mLabel != null){
        mResults.add(mLabel);
      }
      mResults.add(mSelect.toString());
      mResults.add(mCompare.toString());
      mResults.add(mText.toString());
      mResults.add(mExtra);
    }

  }

  class TextConstraintSelect extends ConstraintSelect {
    Vector mWidgets = new Vector();
    TextConstraintSelect(String formName,Integer index,
                             Constraint constraint, Category category)
      throws SQLException,ServletException {
      super(formName, index, constraint);
      
      int count = 0;
      Vector widgets = constraint.getWidgets();
      int size = widgets.size();
      for (Iterator i = widgets.iterator(); i.hasNext(); count++){
        ConstraintWidget widget = (ConstraintWidget)i.next();
        Vector items = widget.getItems();
        String wname = mBaseName + "_select_" + count;
        IFormWidget newWidget;
        if (widget.getStyle().equals("check")){
          newWidget = new RadioOrCheckWidget(wname, null, items);
        } else {
          newWidget = new SelectWidget(wname, null, items);
        }
        newWidget.addProperty("size", widget.getSize());
        if (size == count+1 && constraint.isMultiselect()){
          if (newWidget instanceof RadioOrCheckWidget){
            ((RadioOrCheckWidget)newWidget).setCheck();
          } else {
            newWidget.addProperty("multiple");
          }
        }
        mWidgets.addElement(newWidget);
      }
      convertToStrings();
    }
    TextConstraintSelect(String formName,Integer index,
    		Constraint constraint, DerivedCategory category)
    		throws SQLException,ServletException {
    	super(formName, index, constraint);

    	int count = 0;
    	Vector widgets = constraint.getWidgets();
    	int size = widgets.size();
    	for (Iterator i = widgets.iterator(); i.hasNext(); count++){
    		ConstraintWidget widget = (ConstraintWidget)i.next();
    		Vector items = widget.getItems();
    		String wname = mBaseName + "_select_" + count;
    		IFormWidget newWidget;
    		if (widget.getStyle().equals("check")){
    			newWidget = new RadioOrCheckWidget(wname, null, items);
    		} else {
    			newWidget = new SelectWidget(wname, null, items);
    		}
    		newWidget.addProperty("size", widget.getSize());
    		if (size == count+1 && constraint.isMultiselect()){
    			if (newWidget instanceof RadioOrCheckWidget){
    				((RadioOrCheckWidget)newWidget).setCheck();
    			} else {
    				newWidget.addProperty("multiple");
    			}
    		}
    		mWidgets.addElement(newWidget);
    	}
    	convertToStrings();
    }
    public void convertToStrings() {
      if (mCheck instanceof CheckBox){
        mResults.add("<b>Apply</b>:");
      }
      mResults.add(mCheck.toString());
      mResults.add(mType.toString());
      if (mLabel != null){
        mResults.add(mLabel);
      }
      for (Iterator i = mWidgets.iterator(); i.hasNext(); ){
        IFormWidget theWidget = (IFormWidget)i.next();
        mResults.add(theWidget.toString());
      }
      mResults.add(mExtra);
    }

  }

  class TextFieldConstraintSelect extends ConstraintSelect {
    TextFieldConstraintSelect(String formName,Integer index,
                             Constraint constraint, Category category)
      throws SQLException,ServletException {

      super(formName,index,constraint);
      mText = new Text(mBaseName + "_text", null, "size=12");
      convertToStrings();
    }
    TextFieldConstraintSelect(String formName,Integer index,
    		Constraint constraint, DerivedCategory category)
    		throws SQLException,ServletException {

    	super(formName,index,constraint);
    	mText = new Text(mBaseName + "_text", null, "size=12");
    	convertToStrings();
    }
    public void convertToStrings() {
      if (mCheck instanceof CheckBox){
        mResults.add("<b>Apply</b>:");
      }
      mResults.add(mCheck.toString());
      mResults.add(mType.toString());
      if (mLabel != null){
        mResults.add(mLabel);
      }
      mResults.add(mText.toString());
      mResults.add(mExtra);
    }

  }

/**
   * Create a set of select menus and a text box to represent an
   * axis
   */

  class AxisSelect {
    Vector mSelect = new Vector();
    String mWidgetNames;
    Text mText;
    String mFormName, mCategory, mType;
    String mAxisLo, mAxisHi;
    
    AxisSelect(String type, String formName,String baseName, IAxis axis)
      throws SQLException,ServletException {
      mFormName = formName;
      mType = type;
      Vector nvec = new Vector();
      int count = 0;
      int selectedIndex = 0;
      mAxisLo = axis.getLo();
      mAxisHi = axis.getHi();
      for (Iterator i = axis.getWidgets().iterator(); i.hasNext(); count++){
        AxisWidget widget = (AxisWidget)i.next();
        if (mType.equals("lo")){
          if (widget.getDefaultType().equals("last")){
            selectedIndex = widget.getInitialIndexHi();
          } else {
            selectedIndex = widget.getInitialIndexLo();
          }
        } else if (mType.equals("hi")){
          selectedIndex = widget.getInitialIndexHi();
        } else {
          throw new ServletException("Invalid type:" + type);
        }
        String name = baseName + "_" + count;
        nvec.addElement(Utils.quote(name));
        SelectWidget sw = new SelectWidget(name,null,widget.getItems());
        sw.setDefaults(selectedIndex, widget.getInitialIndexLo(), widget.getInitialIndexHi(), widget.getDefaultType(), mType);
        mSelect.addElement(sw);
      }
      String tname = baseName;
      nvec.addElement(Utils.quote(tname));
      mWidgetNames = Utils.join(",", nvec);
      mText = new Text(tname, null, "size=20");
      mCategory = axis.getCategory();
    }

    public String toString () {
      StringBuffer rval = new StringBuffer();

      if ( mWidgetNames.indexOf("t_hi") >= 0 || mWidgetNames.indexOf("t_lo") >= 0 ) {
         setTimeWidgets();
      }

      for (Iterator i = mSelect.iterator(); i.hasNext();){
        SelectWidget widget = (SelectWidget)i.next();
        Log.debug(this, "Processing "+widget.mName+" to string.");
        rval.append(widget.toString()).append("\n");
      }
      rval.append(mText.toString()).append("\n");
      String widgetName = "widget" + getWidgetCounter();
      rval.append("<script language=\"JavaScript\">\n");
      rval.append("var " + widgetName + " = new MultiWidget(\"").append(mFormName).append("\", [");
      rval.append(mWidgetNames);
      rval.append("], \"").append(mCategory).append("\");");
      rval.append(widgetName + ".onChange();");
      rval.append("</script>\n");
      return rval.toString();
    }
    public void setTimeWidgets() {
         // Check that the combination of these widgets is in range.

         int dmyLength = Math.min(3, mSelect.size() );
         if (mCategory.equals("ctime")) {
            // We don't have to do this work for a climo axis.
            // Just setting the widgets should be good enough.  :-)
            return; 
         }
         String DateTimeString = "";
         for ( int it = 0; it < dmyLength; it++ ) {
            SelectWidget widget = (SelectWidget)mSelect.get(it);
            DateTimeString = DateTimeString + widget.mValues[widget.getSelectedIndex()];
            if ( it < dmyLength-1 ) {
               DateTimeString = DateTimeString + "-";
            }

         }
         
         if ( dmyLength < mSelect.size() ) {
            for ( int it = dmyLength; it < mSelect.size(); it++ ) {
               DateTimeString = DateTimeString + " ";
               SelectWidget widget = (SelectWidget)mSelect.get(it);
               DateTimeString += widget.mValues[widget.getSelectedIndex()]+":00:00";
            }
         }
         else {
            // Ferret time strings always have the 00:00:00 so tack it on.
            DateTimeString += " 00:00:00";
         }

         Log.debug(this, "Checking this time: "+DateTimeString+" with category="+mCategory+" for "+DateTimeString+" between "+mAxisLo+" and "+mAxisHi);

         SimpleDateFormat formatter =
            new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss",new Locale("en","US"));

         formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

         Date hi=null;
         Date lo=null;
         Date selected=null;

         try {

            hi = formatter.parse(mAxisHi);
            lo = formatter.parse(mAxisLo);
            selected = formatter.parse(DateTimeString);


         }
         catch (Exception e) {
            // We can't make sense of the time so punt.
            Log.warn(this,"Could not parse time. "+e.toString());
            return;

         }

         if ( selected != null && hi != null && lo != null ) {
            if ( selected.after(hi) || selected.before(lo) ) {
               // All of this work just to do this one simple thing.
               // If the date is out of range, we throw away the
               // previously selected values and the widgets
               // get set according to the display_hi, display_lo
               // or the axis widget ranges.
               for (Iterator i = mSelect.iterator(); i.hasNext();){
                  SelectWidget widget = (SelectWidget)i.next();
                  widget.mLastValue = null;
                  widget.mSelectedIndex = -1;
               }

               Log.debug(this,"This date is not in range!!!!");
            }
            else {
               Log.debug(this,"This date is in range."); 
            }
         }
         else {
            // If we can't parse the dates, then just return.  No harm done.
            return;
         }
     }
  }

  class InputWidget extends FormWidget {
    InputWidget(String initial, String name, String value,
                String properties){
      super("<input type=\"" + initial + "\" ",name,value,properties);
    }
  }

  class CheckBox extends InputWidget {
    CheckBox(String name, String value, String properties){
      super("checkbox", name, value, properties);
      setRadioOrCheckState();
    }
  }

  class Radio extends InputWidget {
    Radio(String name, String value, String properties){
      super("radio", name, value, properties);
      setRadioOrCheckState();
    }
  }

  class Text extends InputWidget {
    Text(String name, String value, String properties){
      super("text", name, value, properties);
      setValue();
    }
  }

  class Password extends InputWidget {
    Password(String name, String value, String properties){
      super("password", name, value, properties);
    }
  }

  class Hidden extends InputWidget {
    Hidden(String name, String value, String properties){
      super("hidden", name, value, properties);
      setValue();
    }
  }

  // Only for testing
  private FormHandler() {
  }

  protected static int getWidgetCounter() {
    return mWidgetCounter++;
  }

  private FormHandler(HttpServletRequest req){
    mReq = req;
    TemplateSession session = Utils.getSession(req);
    setFormParameters(session.getFormParameters(req));
    Vector v = new Vector();
    mRE.split(v, "/\\//", req.getRequestURI());
    String query = req.getQueryString();
    mURI = (String)v.elementAt(v.size()-1);
    if (null != query){
      mURI += "?" + req.getQueryString();
    }
  }

  public void setFormParameters(FormParameters params){
    mParams = params;
  }

  /**
   * Create a HTML checkbox
   */
  public String checkbox(String name) {
    return checkbox(name, null, null);
  }

  /**
   * Create a HTML checkbox
   */
  public String checkbox(String name, String value){
    return checkbox(name,value,null);
  }

  /**
   * Create a HTML checkbox
   */
  public String checkbox(String name, String value, String properties){
    return new CheckBox(name,value,properties).toString();
  }

  /**
   * Create a HTML radio button
   */
  public String radio(String name) {
    return radio(name, null, null);
  }

  /**
   * Create a HTML radio button
   */
  public String radio(String name, String value){
    return radio(name,value,null);
  }

  /**
   * Create a HTML radio button
   */
  public String radio(String name, String value, String properties){
    return new Radio(name,value,properties).toString();
  }

  /**
   * Create a HTML text field
   */
  public String text(String name) {
    return text(name, null, null);
  }

  /**
   * Create a HTML text field
   */
  public String text(String name, String value){
    return text(name,value,null);
  }

  /**
   * Create a HTML text field
   */
  public String text(String name, String value, String properties){
    return new Text(name,value,properties).toString();
  }

  /**
   * Create a HTML password field
   */
  public String password(String name) {
    return password(name, null, null);
  }

  /**
   * Create a HTML password field
   */
  public String password(String name, String value){
    return password(name,value,null);
  }

  /**
   * Create a HTML password field
   */
  public String password(String name, String value, String properties){
    return new Password(name,value,properties).toString();
  }

  /**
   * Create a HTML hidden field
   */
  public String hidden(String name) {
    return hidden(name, null, null);
  }

  /**
   * Create a HTML hidden field
   */
  public String hidden(String name, String value){
    return hidden(name,value,null);
  }

  /**
   * Create a HTML hidden field
   */
  public String hidden(String name, String value, String properties){
    return new Hidden(name,value,properties).toString();
  }

  /**
   * Create an axis select widget
   */
  public String axisSelect(String type, String formName, String base,
                           IAxis axis)
    throws SQLException,ServletException {
    return new AxisSelect(type, formName, base, axis).toString();
  }

  /**
   * Create widgets for selecting constraints
   */
  public Vector constraintSelect(String formName, Integer index,
                                 Constraint constraint,
                                 Category category)
    throws SQLException,ServletException {
    String type = constraint.getType();
    ConstraintSelect cs = null;
    if (type.equals("variable")){
      cs = new VariableConstraintSelect(formName,index,constraint, category);
    } else if (type.equals("text")){
      cs = new TextConstraintSelect(formName,index,constraint, category);
    } else if (type.equals("textfield")){
      cs = new TextFieldConstraintSelect(formName,index,constraint, category);
    }
    if (cs == null){
      throw new SQLException("Unknown constraint type: " + type);
    }
    return cs.getWidgetList();
  }
  /**
   * Create widgets for selecting constraints
   */
  public Vector constraintSelect(String formName, Integer index,
                                 Constraint constraint,
                                 DerivedCategory category)
    throws SQLException,ServletException {
    String type = constraint.getType();
    ConstraintSelect cs = null;
    if (type.equals("variable")){
      cs = new VariableConstraintSelect(formName,index,constraint, category);
    } else if (type.equals("text")){
      cs = new TextConstraintSelect(formName,index,constraint, category);
    } else if (type.equals("textfield")){
      cs = new TextFieldConstraintSelect(formName,index,constraint, category);
    }
    if (cs == null){
      throw new SQLException("Unknown constraint type: " + type);
    }
    return cs.getWidgetList();
  }


  /**
   * Create a HTML select menu
   */
  public String select(String name, Vector widgetItems) {
    return new SelectWidget(name, null, widgetItems).toString();
  }

  /**
   * Create a HTML select menu
   */
  public String select(String name, String properties,
                       Vector widgetItems) {
    return new SelectWidget(name, properties, widgetItems).toString();
  }

  /**
   * Create select menu from currently selected variables
   * This is a special hack that supports variable names as options
   * The string _var_ is prepended to the name to flag this as 
   * a special property. When XmlRequester is called, it looks for
   * properties with this prepended string and substitutes the variable
   * in the request to the LAS data server. 
   */

  public String selectedVariables(String name, RegionConstraint region)
  throws SQLException {
    ICategory[] cats = region.getCategories();
    Vector widgetItems = new Vector();
    for (int i=0; i < cats.length; ++i){
      ICategory cat = cats[i];
      WidgetItem wi = new WidgetItem(cat.getName(), cat.getOid());
      widgetItems.addElement(wi);
    }
    return select("_var_" + name, null, widgetItems);
  }

  /**
   * Create a HTML <form> start field
   */
  public String getStart() {
    return getStart("main", null);
  }

  /**
   * Create a HTML <form> start field
   */
  public String getStart(String name) {
    return getStart(name, null, null);
  }
  
  public String getStart(String name, String submitProc) {
	  return getStart(name, submitProc, null);
  }

  public String getStart(String name, String submitProc, String form_id){
    String outname = "", proc="", fid="";
    if (name != null){
      outname = "name=\"" + name + "\"";
    }
    if (submitProc != null){
      proc = "onSubmit=\"" + submitProc + "\"";
    }
    if ( form_id != null ) {
    	fid = "id=\"" + form_id + "\"";
    }
    String rval = "<form " + fid + " " + proc + " " + outname + " method=\"POST\" action=\"" + getTarget() + "\">";
    // All forms must have a nexturl field
    rval += new Hidden("nexturl", " ", null).toString();
    return rval;
  }

  /**
   * Create a HTML </form> end field
   */
  public String getEnd() {
    return "</form>";
  }

  public String getTarget() {
    return mURI;
  }

  public void setErrorMessage(String mess){
    mErrMess = mess;
  }

  public String getErrorMessage(){
    return mErrMess;
  }

  public static FormHandler getInstance(HttpServletRequest req) {
    return new FormHandler(req);
  }

  static Vector getBeanClassNames(String uri) throws ServletException, SQLException {
    Vector v = new Vector();
    v.addElement(Utils.getBeanRootFromURI(uri) + "FormBean");
    return v;
  }

  static public FormBean getBean(String uri) throws ServletException, SQLException {
    Vector v = getBeanClassNames(uri);
    FormBean fbean = (FormBean)Utils.getBean(v);
    if (fbean == null){
      Log.debug(FormHandler.class, "No bean class found for:" + uri);
    } else {
      Log.debug(FormHandler.class,
                "Found bean for template " + uri);
    }
    return fbean;
  }

  public static void main(String[] args){
    Vector v = new Vector();
    IWidgetItem wi = new DbaseWidgetItem("");
    wi.setValue("value1");
    wi.setLabel("label1");
    v.addElement(wi);
    wi = new DbaseWidgetItem("");
    wi.setValue("value2");
    wi.setLabel("label2");
    v.addElement(wi);
    System.out.println(new FormHandler().select("foo", "a=b", v));
  }

}

