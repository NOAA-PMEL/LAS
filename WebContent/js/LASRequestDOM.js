/**
 * @fileoverview This file is to be included in any HTML documents that
 * wish to issue LASRequests to a Live Access Server.  This file and
 * the required 'xmldom.js' file should be included in the following
 * manner in the head of the HTML file:
 * <pre>
 * &lt;head>
 *   &lt;script language="JavaScript" src="xmldom.js"&gt;&lt;/script&gt;
 *   &lt;script language="JavaScript" src="LASRequestDOM.js"&gt;&lt;/script&gt;
 *   ...
 * &lt;/head></pre>
 * For more information about LAS and the LASRequest please see:
 * {@link http://ferret.pmel.noaa.gov/armstrong/ Armstrong Documentation}.<br>
 *
 * For more information about xmldom.js please visit:
 * {@link http://xmljs.sourceforge.net/ XML for script} and read the
 * documentation on the '<i>Classic DOM</i>'.
 *
 * @author Jonathan Callahan
 * @version $Revision: 783 $
 */

/**
 * Constructs a new LASRequest object.<br>
 * An LASRequest object contains all the information needed to generate an XML
 * representation of an LASRequest.  Javascript within an HTML page can modify
 * this LASRequest object and then obtain the XML string to be sent to a
 * Live Access Server (LAS).
 * @class This is the basic LASRequest class.
 * @constructor
 * @param {string} xml XML representation of an LASRequest used to initialize the LASRequest object
 * @return A new LASRequest object
 */
function LASRequest(xml) {

  if (!xml) xml = '<?xml version=\"1.0\"?><lasRequest href=\"file:las.xml\"><link match=\"/lasdata/operations/shade\"/><args><link match=\"/lasdata/datasets/coads_climatology_cdf/variables/airt\"></link><region><range type=\"x\" low=\"-180.0\" high=\"180.0\"/><range type=\"y\" low=\"-89.5\" high=\"89.5\"/><point type=\"t\" v=\"15-Jan\"/></region></args></lasRequest>';

/**
 * Internal DOM representation of the LASRequest as returned by
 * the XMLDoc() method defined in xmldom.js.
 * @private
 * @type XMLDoc
 */
  this.DOM = new XMLDoc(xml,_LASReq_parseError);
  var operationNode = this.DOM.selectNode("/link");

/*
 * Initialize additional nodes for <properties><ferret> and
 * <properties><product_server> if they are not described by
 * the incoming XML.
 */
  if (!this.DOM.selectNode('/properties')) {
    var Node = this.DOM.createXMLNode('<properties></properties>');
    this.DOM = this.DOM.insertNodeAfter(operationNode,Node);
  }

// Add methods to this object
  this.getOperation = LASReq_getOperation;
  this.setOperation = LASReq_setOperation;

  this.addPropertyGroup = LASReq_addPropertyGroup;
  this.removePropertyGroup = LASReq_removePropertyGroup;

  this.getProperty = LASReq_getProperty;
  this.setProperty = LASReq_setProperty;
  this.addProperty = LASReq_addProperty;
  this.removeProperty = LASReq_removeProperty;

  this.getVariable = LASReq_getVariable;
  this.setVariable = LASReq_setVariable;
  this.addVariable = LASReq_addVariable;
  this.removeVariable = LASReq_removeVariable;
  this.removeVariables = LASReq_removeVariables;

  this.getDataset = LASReq_getDataset;

  // NOTE:  The current XML representation of Constraints makes it difficult to identify constraints
  // NOTE:  or to return useful stringified versions of them.  The getConstraint(), setConstraint()
  // NOTE:  and removeConstraint() methods will not be implemented at this time.
  //this.getConstraint ??
  //this.setconstraint ??
  this.addTextConstraint = LASReq_addTextConstraint;
  this.addVariableConstraint = LASReq_addVariableConstraint;
  //this.removeConstraint = LASReq_removeConstraints ??
  this.removeConstraints = LASReq_removeConstraints;

  // NOTE:  Some guidance is needed on the best way to return the contents of an Analysis.  The
  // NOTE:  getAnalysis() method not be implemented in this version of the code.
  //this.getAnalysis ??
  //this.setAnalysis ??
  this.addAnalysis = LASReq_addAnalysis;
  this.removeAnalysis = LASReq_removeAnalysis;
  this.removeAnalyses = LASReq_removeAnalyses;

  this.addRegion = LASReq_addRegion;
  this.removeRegion = LASReq_removeRegion;

  this.getRangeLo = LASReq_getRangeLo;
  this.getRangeHi = LASReq_getRangeHi;
  this.getAxisType = LASReq_getAxisType;
  this.setRange = LASReq_setRange;
  this.addRange = LASReq_addRange;
  this.removeRange = LASReq_removeRange;

  this.toString = LASReq_toString;

  this.getVariableNode = LASReq_getVariableNode;
}

////////////////////////////////////////////////////////////
// Methods of the LASRequest object.
////////////////////////////////////////////////////////////

// <link match="/lasdata/datasets/NOAA-CIRES-CDC-CDC_Derived_NCEP_Reanalysis_Products_Surface_Flux/variables/CDC_DS40-uflx12361" ><analysis label="Monthly Mean of Momentum Flux, U-Component_1" ><axis hi="180.0" type="x" lo="-180.0" op="ave" /></analysis></link>

////////////////////////////////////////////////////////////
// Operation methods
////////////////////////////////////////////////////////////

/**
 * Returns the Operation currently assigned in the top level <code>&lt;link match=...></code> element of the LASRequest.
 * By default, the 'Armstrong' style of Operation XML is assumed, failing over to version6.x style if no operation
 * is found.<br>
 * @param {string} style (optional) style of XML for backwards compatibility:  'LAS6' for version 6.x style requests, 
 * @return named operation defined in <code>operations.xml</code>
 * @see #setOperation
 */
function LASReq_getOperation(style) {
  var operationNode = this.DOM.selectNode("/link");
  var matchString = new String(operationNode.getAttribute("match"));
  var pieces = matchString.split('/');
  var operation = "";
  if (style == 'LAS6') {
    /* /lasdata/operations/operation_name */
    operation = pieces[3];
  } else {
    /* /lasdata/operations/operation[@ID='operation_name'] */
    var operationIDString = new String(pieces[3]);
    var subpieces = operationIDString.split("'");
    operation = subpieces[1];
    if (!operation) {
      operation = this.getOperation('LAS6');
    }
  }
  return operation;
}

/**
 * Replaces the top level <code>&lt;link match=...></code> element in the LASRequest.
 * If no style is provided an 'Armstrong' style is assumed.<br>
 * @param {string} operation named operation defined in operations.xml
 * @param {string} style (optional) style of XML for backwards compatibility:  'LAS6' for version 6.x requests, 
 * anything else for 'Armstrong' requests.
 * @see #getOperation
 */
function LASReq_setOperation(operation,style) {
  var linkMatch = '';
  if (style == 'LAS6') {
    linkMatch = '/lasdata/operations/' + operation;
  } else {
    linkMatch = '/lasdata/operations/operation[@ID=\'' + operation + '\']';
  }
  var operationNode = this.DOM.selectNode("/link");
  operationNode.removeAttribute("match");
  operationNode.addAttribute("match",linkMatch);
}

////////////////////////////////////////////////////////////
// PropertyGroup methods
////////////////////////////////////////////////////////////

/**
 * Adds a <code>&lt;properties>&lt;<i>group</i>>&lt;<i>/group</i>>&lt;/properties></code> element to the LASRequest.<br>
 * If the named PropertyGroup already exists, no action is taken.
 * @param {string} group named PropertyGroup (<i>e.g.</i>ferret, database_access)
 * @see #removePropertyGroup
 * @see #setProperty
 */
function LASReq_addPropertyGroup(group) {
  if (!this.DOM.selectNode('/properties')) {
    var newNode = this.DOM.createXMLNode('<properties></properties>');
    this.DOM = this.DOM.insertNodeAfter(operationNode,newNode);
  }
  var propertiesNode = this.DOM.selectNode('/properties');
  var nodePath = '/properties/' + group;
  if (!this.DOM.selectNode(nodePath)) {
    var nodeXML = '<' + group + '>' + '</' + group + '>';
    var newNode = this.DOM.createXMLNode(nodeXML);
    this.DOM = this.DOM.insertNodeInto(propertiesNode,newNode);
  }
}

/**
 * Removes an entire <code>&lt;properties>&lt;<i>group</i>>&lt;/<i>group</i>>&lt;/properties></code> element from the LASRequest.<br>
 * This will remove all properties within the named <code><i>group</i></code> from the LASRequest.
 * @param {string} group named propertyGroup (<i>e.g.</i>ferret, database_access)
 * @see #addPropertyGroup
 */
function LASReq_removePropertyGroup(group) {
  var nodePath = '/properties/' + group;
  var propertyGroupNode = this.DOM.selectNode(nodePath);
  this.DOM = this.DOM.removeNodeFromTree(propertyGroupNode);
}

////////////////////////////////////////////////////////////
// Property methods
////////////////////////////////////////////////////////////

/**
 * Returns the value named Property defined in the named PropertyGroup of the LASRequest.<br>
 * A <code>null</code> value is returned if the Property is not found.
 * @param {string} group named propertyGroup (<i>e.g.</i>ferret, database_access)
 * @param {string} property name of the property 
 * @return value or <code>null</code> if not found
 * @see #setProperty
 * @see #addProperty
 * @see #removeProperty
 */
function LASReq_getProperty(group,property) {
  var nodePath = '/properties/' + group + '/' + property;
  var Node = this.DOM.selectNode(nodePath);
  if (Node) {
    return this.DOM.selectNodeText(nodePath);
  } else {
    return null;
  }
}

/**
 * Replaces the value of a Property element in the named PropertyGroup of the LASRequest.<br>
 * If the property is not found a new Property element will be created.
 * @param {string} group named propertyGroup (<i>e.g.</i>ferret, database_access)
 * @param {string} property name of the property 
 * @param {string} value value
 * @see #getProperty
 * @see #addProperty
 * @see #removeProperty
 */
function LASReq_setProperty(group,property,value) {
  var nodePath = '/properties/' + group + '/' + property;
  var Node = this.DOM.selectNode(nodePath);
  if (Node) {
    this.DOM = this.DOM.replaceNodeContents(Node,value);
  } else {
    this.addProperty(group,property,value);
  }
}

/**
 * Adds a new <code>&lt;property>value&lt;property></code> element inside the named PropertyGroup element of the LASRequest.<br>
 * If the propertyGroup is missing it will be added.<br>
 * If the property already exists its value will be replaced with the incoming value.<br>
 * @param {string} group named propertyGroup (<i>e.g.</i>ferret, database_access)
 * @param {string} property name of the property 
 * @see #getProperty
 * @see #setProperty
 * @see #removeProperty
 */
function LASReq_addProperty(group,property,value) {
  var parentPath = '/properties/' + group;
  var parentNode = this.DOM.selectNode(parentPath);
  if (!parentNode) {
    this.addPropertyGroup(group);
  }
  var nodePath = '/properties/' + group + '/' + property;
  var Node = this.DOM.selectNode(nodePath);
  if (Node) {
    this.setProperty(group,property,value);
  } else {
    var parentPath = '/properties/' + group;
    var parentNode = this.DOM.selectNode(parentPath);
    var nodeXML = '<' + property + '>' + value + '</' + property + '>';
    var newNode = this.DOM.createXMLNode(nodeXML);
    this.DOM = this.DOM.insertNodeInto(parentNode,newNode);
  }
}

/**
 * Removes the named Property element defined in the named propertyGroup element of the LASRequest.<br>
 * @param {string} group propertyGroup (e.g. 'database_access', 'ferret', ...)
 * @param {string} property name of the property 
 * @see #getProperty
 * @see #setProperty
 * @see #addProperty
 */
function LASReq_removeProperty(group,property) {
  var nodePath = '/properties/' + group + '/' + property;
  var Node = this.DOM.selectNode(nodePath);
  this.DOM = this.DOM.removeNodeFromTree(Node);
}

////////////////////////////////////////////////////////////
// Variable methods
////////////////////////////////////////////////////////////

/**
 * Returns the dataset string from the <code>&lt;link match=...></code> element in the <code>&lt;args></code> section of the LASRequest.<br>
 * @param {int} data_ID (optional) index of the <code>&link match=...></code> element.  Defaults to <code>0</code> which is appropriate
 * for single variable requests.
 * @return dataset name or <code>null</code> if not found
 * @see #getVariable
 * @see #setVariable
 * @see #addVariable
 * @see #removeVariable
 * @see #removeVariables
 */
function LASReq_getDataset(data_ID) {
  var index = (data_ID) ? data_ID : 0;
  var variableNode = this.DOM.selectNode('/args/link[' + index + ']');
  matchString = new String(variableNode.getAttribute("match"));
  var pieces = matchString.split('/');
  return pieces[3];
}

/**
 * Returns the variable string from the <code>&lt;link match=...></code> element in the <code>&lt;args></code> section of the LASRequest.<br>
 * @param {int} data_ID (optional) index of the <code>&lt;link match=...></code> element.  Defaults to <code>0</code> which is appropriate
 * for single variable requests.
 * @return variable name or <code>null</code> if not found
 * @see #getDataset
 * @see #setVariable
 * @see #addVariable
 * @see #removeVariable
 * @see #removeVariables
 */
function LASReq_getVariable(data_ID) {
  var index = (data_ID) ? data_ID : 0;
  var variableNode = this.DOM.selectNode('/args/link[' + index + ']');
  matchString = new String(variableNode.getAttribute("match"));
  var pieces = matchString.split('/');
  return pieces[5];
}

/**
 * Replaces all existing dataset-variable Link elements defined in the <code>&lt;args></code> section of the LASRequest
 * with the incoming pair.<br>
 * <b>NOTE:</b> The <code>setVariable(...)</code> method is deprecated.  Multiple variables
 * are possible and the ability to modify only a single variable would normally require
 * that you identify an existing variable you wish to modify.  The current implementation of
 * the <code>setVariable(...)</code> method removes any additional variables that may be defined in the request.<p>
 * It is recommended, instead, that you clear out all variables explicitly with 
 * removeVariables() and then addVariable(...) for each variable you wish to add.
 * @param {string} dataset dataset name
 * @param {string} variable variable name
 * @see #getDataset
 * @see #getVariable
 * @see #addVariable
 * @see #removeVariable
 * @see #removeVariables
 */
function LASReq_setVariable(dataset,variable) {
  this.removeVariables();
  this.addVariable(dataset,variable);
}

/**
 * Adds a <code>&lt;link match=.../></code> element to the <code>&lt;args></code> section of the LASRequest.<br>
 * This will add a new dataset-variable pair to the LASRequest.<br>
 * @param {string} dataset dataset name
 * @param {string} variable variable name
 * @see #getDataset
 * @see #getVariable
 * @see #setVariable
 * @see #removeVariable
 * @see #removeVariables
 */
function LASReq_addVariable(dataset,variable) {
  var argsNode = this.DOM.selectNode("/args");
  var nodeXML = '<link match=\"/lasdata/datasets/' + dataset + '/variables/' + variable + '\"></link>';
  var newNode = this.DOM.createXMLNode(nodeXML);
  this.DOM = this.DOM.insertNodeInto(argsNode,newNode);
}

/**
 * Removes a single <code>&lt;link match=...></code> element defined in the <code>&lt;args></code> section of the LASRequest.<br>
 * @param {string} dataset dataset name
 * @param {string} variable variable name
 * @see #getDataset
 * @see #getVariable
 * @see #setVariable
 * @see #addVariable
 * @see #removeVariables
 */
function LASReq_removeVariable(dataset,variable) {
  var argsNode = this.DOM.selectNode("/args");
  var variableNodes = argsNode.getElements('link');
  for (i=0;i<variableNodes.length;i++) {
    matchString = new String(variableNodes[i].getAttribute("match"));
    var pieces = matchString.split('/');
    if (pieces[5] == variable) {
      this.DOM = this.DOM.removeNodeFromTree(variableNodes[i]);
    }
  }
}

/**
 * Removes all <code>&lt;link match=...></code> elements defined in the <code>&lt;args></code> section of the LASRequest.<br>
 * Clears out all dataset-variable pairs defined in the LASRequest.
 * @see #getDataset
 * @see #getVariable
 * @see #setVariable
 * @see #addVariable
 * @see #removeVariable
 */
function LASReq_removeVariables() {
  var argsNode = this.DOM.selectNode("/args");
  var variableNodes = argsNode.getElements('link');
  for (i=0;i<variableNodes.length;i++) {
    this.DOM = this.DOM.removeNodeFromTree(variableNodes[i]);
  }
}

////////////////////////////////////////////////////////////
// Constraint methods
////////////////////////////////////////////////////////////

/**
 * Adds a Constraint element of type 'text' to the <code>&lt;args></code> section of the LASRequest.<br>
 * Constraints are also known as 'data options' and are used to modify or subset the data before
 * the product is created.
 * @param {string} variable variable name as left hand side
 * @param {string} operator operator (e.g. '&lt; '&lt; etc.)
 * @param {string} value right hand side
 * @see #addVariableConstraint
 * @see #removeConstraints
 */
function LASReq_addTextConstraint(variable,operator,value) {
  var argsNode = this.DOM.selectNode("/args");
  var nodeXML = '<constraint type=\"text\"><v>' + variable + '</v><v>' + operator + '</v><v>' + value + '</v></constraint>';
  var newNode = this.DOM.createXMLNode(nodeXML);
  this.DOM = this.DOM.insertNodeInto(argsNode,newNode);
}

/**
 * Adds a Constraint element of type 'variable' to the <code>&lt;args></code> section of the LASRequest.<br>
 * Constraints of type 'variable' contain dataset-variable xpath information as the left hand side.
 * @param {string} dataset dataset name for the left hand side
 * @param {string} variable variable name for the left hand side
 * @param {string} operator operator (e.g. '&lt; '&lt; etc.)
 * @param {string} value right hand side
 * @see #addTextConstraint
 * @see #removeConstraints
 */
function LASReq_addVariableConstraint(dataset,variable,operator,value) {
  var argsNode = this.DOM.selectNode("/args");
  var linkXML = '<link match=\"/lasdata/datasets/' + dataset + '/variables/' + variable + '\"/>';
  var nodeXML = '<constraint type=\"variable\" op=\"' + operator + '\">' + linkXML + '<v>' + value + '</v></constraint>';
  var newNode = this.DOM.createXMLNode(nodeXML);
  this.DOM = this.DOM.insertNodeInto(argsNode,newNode);
}

/**
 * Removes all Constraint elements defined in the <code>&lt;args></code> section of the LASRequest.<br>
 * No 'data options' will be applied before creating the product.
 * @see #addTextConstraint
 * @see #addVariableCosntraint
 */
function LASReq_removeConstraints() {
  var argsNode = this.DOM.selectNode("/args");
  var constraintNodes = argsNode.getElements('constraint');
  for (i=0;i<constraintNodes.length;i++) {
    this.DOM = this.DOM.removeNodeFromTree(constraintNodes[i]);
  }
}

////////////////////////////////////////////////////////////
// Analysis methods
////////////////////////////////////////////////////////////

/**
 * Adds an Analysis element to an existing Variable in the <code>&lt;args></code> section of the LASRequest.<br>
 * Each analysis is applied to a single axis and will 
 * typically be a Ferret axis-compressing transform like SUM, AVE, etc.
 * @param {string} dataset dataset name
 * @param {string} variable variable name
 * @param {string} label identifier for the analysis
 * @param {string} xyzt axis to which this analysis is applied: 'x','y','z' or 't'
 * @param {string} lo lower bound of region of interest (axis specific)
 * @param {string} hi upper bound of region of interest
 * @param {string} transform named Ferret transform (e.g. SUM, AVE, etc.)
 * @see #removeAnalysis
 * @see #removeAnalyses
 */
function LASReq_addAnalysis(dataset,variable,label,xyzt,lo,hi,transform) {
  var variableNode = this.getVariableNode(dataset,variable)
  var nodeXML = '<analysis label=\"' + label + '\"><axis type=\"' + xyzt + '\" lo=\"' + lo + '\" hi=\"' + hi + '\" op=\"' + transform + '\"/></analysis>';
  var newNode = this.DOM.createXMLNode(nodeXML);
  this.DOM = this.DOM.insertNodeInto(variableNode,newNode);
}

/**
 * Removes a single Analysis element, identified by the <code>label</code>, from an existing Variable defined in the <code>&lt;args></code> section of the LASRequest.<br>
 * @param {string} dataset dataset name
 * @param {string} variable variable name
 * @param {string} label identifier for the analysis
 * @see #addAnalysis
 * @see #removeAnalyses
 */
function LASReq_removeAnalysis(dataset,variable,label) {
  var variableNode = this.getVariableNode(dataset,variable)
  var analysisNodes = variableNode.getElements('analysis');
  for (i=0;i<analysisNodes.length;i++) {
    if (label == String(analysisNodes[i].getAttribute("label"))) {
      this.DOM = this.DOM.removeNodeFromTree(analysisNodes[i]);
    }
  }
}

/**
 * Removes all Analysis elements from an existing Variable defined in the <code>&lt;args></code> section of the LASRequest.<br>
 * No 'data options' will be applied before creating the product.
 * @param {string} dataset dataset name
 * @param {string} variable variable name
 * @see #addAnalysis
 * @see #removeAnalysis
 */
function LASReq_removeAnalyses(dataset,variable) {
  var variableNode = this.getVariableNode(dataset,variable)
  var analysisNodes = variableNode.getElements('analysis');
  for (i=0;i<analysisNodes.length;i++) {
    this.DOM = this.DOM.removeNodeFromTree(analysisNodes[i]);
  }
}

////////////////////////////////////////////////////////////
// Region methods
////////////////////////////////////////////////////////////

/**
 * Adds a Region element to the <code>&lt;args></code> section of the LASRequest.<br>
 * The region added will initially be empty.
 * The <code>setRange()</code> method is used to populate the 'region' with axis ranges<br>
 * @param {int} region_ID (optional) index of Region.  Defaults to <code>0</code> which is appropriate for
 * single region requests.
 * @see #removeRegion
 */
function LASReq_addRegion() {
  var argsNode = this.DOM.selectNode("/args");
  var nodeXML = '<region></region>';
  var newNode = this.DOM.createXMLNode(nodeXML);
  this.DOM = this.DOM.insertNodeInto(argsNode,newNode);
}

/**
 * Removes a Region element, optionally identified by <code>region_ID</code>, from the <code>&lt;args></code> section of the LASRequest.<br>
 * If no <code>region_ID</code> is specified, a value of <code>0</code> is assumed resulting in the removal of the
 * first (or only) Region in the request.
 * @param {int} region_ID (optional) index of Region.  Defaults to <code>0</code> which is appropriate for
 * single region requests.
 * @see #addRegion
 */
function LASReq_removeRegion(region_ID) {
  var index = (region_ID) ? region_ID : 0;
  var argsNode = this.DOM.selectNode("/args");
  var regionNodes = argsNode.getElements('region');
  this.DOM = this.DOM.removeNodeFromTree(regionNodes[index]);
}

////////////////////////////////////////////////////////////
// Range methods
////////////////////////////////////////////////////////////

/**
 * Returns the value representing the 'lo' end of the Range if this axis is defined, <code>null</code> otherwise.<br>
 * @param {string} xyzt axis with which this range is associated: 'x','y','z' or 't'
 * @param {int} region_ID (optional) index of the Region.  Defaults to <code>0</code> which is appropriate for
 * single region requests.
 * @return lo value of the Range or <code>null</code>
 * @see #getRangeHi
 * @see #getAxisType
 * @see #setRange
 * @see #addRange
 * @see #removeRange
 */
function LASReq_getRangeLo(xyzt,region_ID) {
  var lo = null;
  var type = this.getAxisType(xyzt,region_ID);
  if (type == 'range') {
    var index = (region_ID) ? region_ID : 0;
    var Node = this.DOM.selectNode('/args/region[' + index + ']/range[@type=\"' + xyzt + '\"]');
    lo = Node.getAttribute('low');
  } else if (type == 'point') {
    var index = (region_ID) ? region_ID : 0;
    var Node = this.DOM.selectNode('/args/region[' + index + ']/point[@type=\"' + xyzt + '\"]');
    lo = Node.getAttribute('v');
  } else {
    lo = null;
  } 
  return lo;
}

/**
 * Returns the value representing the 'hi' end of the Range if is of type 'range', 
 * or a <code>null</code> if it is of type 'point' or is not defined.
 * @param {string} xyzt axis with which this range is associated: 'x','y','z' or 't'
 * @param {int} region_ID (optional) index of Region.  Defaults to <code>0</code> which is appropriate for
 * single region requests.
 * @return hi value of the Range or <code>null</code>
 * @see #getRangeLo
 * @see #getAxisType
 * @see #setRange
 * @see #addRange
 * @see #removeRange
 */
function LASReq_getRangeHi(xyzt,region_ID) {
  var hi = null;
  var type = this.getAxisType(xyzt,region_ID);
  if (type == 'range') {
    var index = (region_ID) ? region_ID : 0;
    var Node = this.DOM.selectNode('/args/region[' + index + ']/range[@type=\"' + xyzt + '\"]');
    hi = Node.getAttribute('high');
  } else {
    hi = null;
  }
  return hi;
}

/**
 * Returns the axis type ('point' or 'range') if it is found, <code>null</code> otherwise.
 * @param {string} xyzt axis with which this Range is associated: 'x','y','z' or 't'
 * @param {int} region_ID (optional) index of Region.  Defaults to <code>0</code> which is appropriate for
 * single region requests.
 * @return axis type ('point' or 'range') or <code>null</code> 
 * @see #getRangeLo
 * @see #getRangeHi
 * @see #setRange
 * @see #addRange
 * @see #removeRange
 */
function LASReq_getAxisType(xyzt,region_ID) {
  var type = null;
  var index = (region_ID) ? region_ID : 0;
  var Node = this.DOM.selectNode('/args/region[' + index + ']/range[@type=\"' + xyzt + '\"]');
  if (Node) {
    type = 'range';
  } else {
    Node = this.DOM.selectNode('/args/region[' + index + ']/point[@type=\"' + xyzt + '\"]');
    if (Node) {
      type = 'point';
    }
  }
  return type;
}

/**
 * Adds a new Range element to the <code>&lt;region></code> section of the LASRequest.<br>
 * If a Range along the desired axis already exists it will be replaced.
 * Otherwise, the incoming range will be added to the existing <code>&lt;region></code>.<br>
 * @param {string} xyzt axis with which this region is associated: 'x','y','z' or 't'
 * @param {string} lo lower bound of region of interest (axis specific)
 * @param {string} hi upper bound of region of interest
 * @param {int} region_ID (optional) index of Region.  Defaults to <code>0</code> which is appropriate for
 * single region requests.
 * @see #getRangeLo
 * @see #getRangeHi
 * @see #getAxisType
 * @see #addRange
 * @see #removeRange
 */
function LASReq_setRange(xyzt,lo,hi,region_ID) {
  this.removeRange(xyzt,region_ID);
  this.addRange(xyzt,lo,hi,region_ID);
}

/**
 * Adds a new Range element to <code>&lt;region></code> section of the LASRequest.<br>
 * If no Region with this region_ID is found, one will be created.<br>
 * If a Range along the desired axis already exists it will be replaced.
 * @param {string} xyzt axis with which this region is associated: 'x','y','z' or 't'
 * @param {string} lo lower bound of region of interest (axis specific)
 * @param {string} hi upper bound of region of interest
 * @param {int} region_ID (optional) index of Region.  Defaults to <code>0</code> which is appropriate for
 * single region requests.
 * @see #getRangeLo
 * @see #getRangeHi
 * @see #getAxisType
 * @see #setRange
 * @see #removeRange
 */
function LASReq_addRange(xyzt,lo,hi,region_ID) {
  var index = (region_ID) ? region_ID : 0;
  var regionNode = this.DOM.selectNode('/args/region[' + index + ']');
  if (!regionNode) {
    this.addRegion();
  }
  var nodeXML = '';
  if (hi && (hi != lo)) {
    nodeXML = '<range type=\"' + xyzt + '\" low=\"' + lo + '\" high=\"' + hi + '\"/>';
  } else {
    nodeXML = '<point type=\"' + xyzt + '\" v=\"' + lo + '\"/>';
  }
  var newNode = this.DOM.createXMLNode(nodeXML);
  regionNode = this.DOM.selectNode('/args/region[' + index + ']');
  this.DOM = this.DOM.insertNodeInto(regionNode,newNode);
}

/**
 * Removes a Range element tfrom the <code>&lt;region></code> section of the LASRequest.<br>
 * @param {string} xyzt axis with which this region is associated: 'x','y','z' or 't'
 * @param {int} region_ID (optional) index of Region.  Defaults to <code>0</code> which is appropriate for
 * single region requests.
 * @see #getRangeLo
 * @see #getRangeHi
 * @see #getAxisType
 * @see #setRange
 * @see #addRange
 */
function LASReq_removeRange(xyzt,region_ID) {
  var index = (region_ID) ? region_ID : 0;
  var regionNode = this.DOM.selectNode('/args/region[' + index + ']');
  // If the regionNode is found, remove the <range...> or <point...> node whose
  // 'type' attribute = xyzt.
  if (regionNode) {
    var Node = this.DOM.selectNode('/args/region[' + index + ']/range[@type=\"' + xyzt + '\"]');
    if (Node) {
      this.DOM = this.DOM.removeNodeFromTree(Node);
    } else {
      Node = this.DOM.selectNode('/args/region[' + index + ']/point[@type=\"' + xyzt + '\"]');
      if (Node) {
        this.DOM = this.DOM.removeNodeFromTree(Node);
      }
    }
  }
}

////////////////////////////////////////////////////////////
// Utility methods
////////////////////////////////////////////////////////////

/**
 * Returns a XML string representation of the LASRequest object.
 * @return XML version of LASRequest
 * @type string
 */
function LASReq_toString() {
  return this.DOM.getUnderlyingXMLText();
}

////////////////////////////////////////////////////////////
// Private methods
////////////////////////////////////////////////////////////

/**
 * Returns the Link node associated with a particular dataset,variable pair.<br>
 * @private
 * @param {string} dataset dataset name
 * @param {string} variable variable name
 */
function LASReq_getVariableNode(dataset,variable) {
  var argsNode = this.DOM.selectNode("/args");
  var variableNodes = argsNode.getElements('link');
  for (i=0;i<variableNodes.length;i++) {
    matchString = new String(variableNodes[i].getAttribute("match"));
    var pieces = matchString.split('/');
    if (pieces[5] == variable) {
      return variableNodes[i];
    }
  }
}

/**
 * Error handler passed to XMLDoc creation method.
 * @private
 */
function _LASReq_parseError(e) {
  alert(e);
}

