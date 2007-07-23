//
// $Id: LASRequest.js,v 1.3 2005/02/01 19:11:19 callahan Exp $
//
// Javascript methods that can form an LAS XML request string.
// The basic LAS request looks something like this:
// 
// <?xml version="1.0"?>
// <lasRequest href="file:las.xml">
//   <link match="/lasdata/operations/shade" />
//   <args>
//     <link match="/lasdata/datasets/COADS_1degree_Enhanced/variables/air947"></link>
//     <region>
//       <range type="x" low="-80.0" high="80.0"/>
//       <range type="y" low="-89.5" high="89.5"/>
//       <point type="t" v="01-Jan-1960"/>
//     </region>
//   </args>
// </lasRequest>


////////////////////////////////////////////////////////////
//                                                        //
// Define methods of the LASRequest object                //
//                                                        //
////////////////////////////////////////////////////////////

// Some operations require that a Ferret property named <format>
// be defined.  Set this if it is passed in.

function LASReq_setOperation(operation,format) {
  this.operation = '<link match="/lasdata/operations/' + operation + '"/>\n';
  this.setFerretProperty("format",format);
}

function LASReq_setVariable(variable) {
  this.variable = '<link match="/lasdata/datasets/' + variable + '"/>\n';
}

function LASReq_addVariable(variable) {
  this.variable += '<link match="/lasdata/datasets/' + variable + '"/>\n';
}

function LASReq_setXRange(lo,hi) {
  if (LASReq_setXRange.arguments.length == 0) {
    this.x_range = '\n';
  } else if (LASReq_setXRange.arguments.length == 1) {
    this.x_range = '<point type="x" v="' + lo + '"/>\n';
  } else {
    this.x_range = '<range type="x" low="' + lo + '" high="' + hi + '"/>\n';
  }
}

function LASReq_setYRange(lo,hi) {
  if (LASReq_setYRange.arguments.length == 0) {
    this.y_range = '\n';
  } else if (LASReq_setYRange.arguments.length == 1) {
    this.y_range = '<point type="y" v="' + lo + '"/>\n';
  } else {
    this.y_range = '<range type="y" low="' + lo + '" high="' + hi + '"/>\n';
  }
}

function LASReq_setZRange(lo,hi) {
  if (LASReq_setZRange.arguments.length == 0) {
    this.z_range = '\n';
  } else if (LASReq_setZRange.arguments.length == 1) {
    this.z_range = '<point type="z" v="' + lo + '"/>\n';
  } else {
    this.z_range = '<range type="z" low="' + lo + '" high="' + hi + '"/>\n';
  }
}

function LASReq_setTRange(lo,hi) {
  if (LASReq_setTRange.arguments.length == 0) {
    this.t_range = '\n';
  } else if (LASReq_setTRange.arguments.length == 1) {
    this.t_range = '<point type="t" v="' + lo + '"/>\n';
  } else {
    this.t_range = '<range type="t" low="' + lo + '" high="' + hi + '"/>\n';
  }
}

function LASReq_setFerretProperty(property,value) {
  this.properties = 1;
  this.ferret_properties = '<' + property + '>' + value + '</' + property + '>';
}

function LASReq_addFerretProperty(property,value) {
  this.properties = 1;
  this.ferret_properties += '<' + property + '>' + value + '</' + property + '>';
}

function LASReq_setProductServerProperty(property,value) {
  this.properties = 1;
  this.product_server_properties = '<' + property + '>' + value + '</' + property + '>';
}

function LASReq_addProductServerProperty(property,value) {
  this.properties = 1;
  this.product_server_properties += '<' + property + '>' + value + '</' + property + '>';
}

// All of the required elements in an LAS XML request are set as
// char string properties of the LASReq object.  Elements under
// <properties> in the LAS request are optional so we need a 
// method to determine whether we have any <properties> and what
// the associated string should look like.
 
function LASReq_getProperties() {
  var string = '';
  if (this.properties) {
    string = '<properties>\n';
    if (this.product_server_properties) {
      string += '<product_server>\n' + this.product_server_properties +  '\n</product_server>\n';
    }
    if (this.ferret_properties) {
      string += '<ferret>\n' + this.ferret_properties +  '\n</ferret>\n';
    }
    string += '</properties>\n';
  }
  return string;
}

function LASReq_toString() {
  return this.version +
         this.href +
         this.operation +
         this.getProperties() +
         '<args>\n' +
         this.variable +
         '<region>\n' +
         this.x_range +
         this.y_range +
         this.z_range +
         this.t_range +
         '</region>\n</args>\n</lasRequest>';
}

////////////////////////////////////////////////////////////
//                                                        //
// Define the LASRequest object                           //
//                                                        //
////////////////////////////////////////////////////////////

function LASRequest(version,href) {
  this.version = '<?xml version="' + version + '"?>\n';
  this.href = '<lasRequest href="' + href + '">\n';
  this.properties = 0;

  this.setOperation = LASReq_setOperation;
  this.setVariable = LASReq_setVariable;
  this.addVariable = LASReq_addVariable;
  this.setXRange = LASReq_setXRange;
  this.setYRange = LASReq_setYRange;
  this.setZRange = LASReq_setZRange;
  this.setTRange = LASReq_setTRange;
  this.setFerretProperty = LASReq_setFerretProperty;
  this.addFerretProperty = LASReq_addFerretProperty;
  this.setProductServerProperty = LASReq_setProductServerProperty;
  this.addProductServerProperty = LASReq_addProductServerProperty;

  this.getProperties = LASReq_getProperties;
  this.toString = LASReq_toString;
}


