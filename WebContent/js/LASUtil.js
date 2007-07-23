
////////////////////////////////////////////////////////////
//                                                        //
// Define methods of the LAS utility functions            //
//                                                        //
////////////////////////////////////////////////////////////
var currImage = new Object();
//currImage = images[4];
//currImage.url = "../images/world.jpg";
currImage.url = "../images/ref_map.gif";
currImage.offX = 0;
currImage.offY = 0;
//currImage.wholeImageWidth = 344;
currImage.wholeImageWidth = 516;
//currImage.wholeImageHeight = 192;
currImage.wholeImageHeight = 383;
//currImage.width = 344;
currImage.width = 516;
//currImage.height = 192;
currImage.height = 383;
//currImage.lowX = 122.0;
//currImage.lowX = -170.0; ok but not match with the drawing
currImage.lowX = 21.0;
//currImage.lowX = -180;
//currImage.highX = 288.5;
//currImage.highX = 188.0;  ok but not match with the drawing
currImage.highX = 379.0;
//currImage.highX = 180.0;
//currImage.lowY = -35.0;
currImage.lowY = -89.0;
//currImage.highY = 45.0;
currImage.highY = 89.0;

var currView = null;

var drawObj = null;
var Img = null;

//Global variables
//view definition
var g_box = "rect";
//var g_vLine = "v_line";
var g_vLine = "vert";
//var g_hLine = "h_line";
var g_hLine = "horiz";
var g_point = "point";
var g_vRange = "v_range";
var g_hRange = "h_range";

var x_lo = null;
var x_hi = null;
var y_lo = null;  
var y_hi = null;
var x_lo_px = null;
var x_hi_px = null;
var y_lo_px = null;  
var y_hi_px = null;
             
//do not submit to the popup window
function mysubmit() {
  document.constrain.target = "_self";
  document.constrain.submit();
}

function isBadSelection() {
  //alert(document.forms[0].
}
 
//could not pass the float number back to caller ??!!
function getMiddlePoint(v_lo, v_hi) {
  var l_v_lo = parseFloat(v_lo);
  var l_v_hi = parseFloat(v_hi);

//  alert("before return: " + round((l_v_lo + (l_v_hi - l_v_lo)/2)), 2);
//  return parse(round((l_v_lo + (l_v_hi - l_v_lo)/2)), 2);  
  return (v_lo + (v_hi - v_lo)/2.0);
}

function changeT(tool_t) {
//alert("curr="+currToolT + "  t="+tool_t);
  if (currToolT != tool_t) {
    currToolT = tool_t;
    mysubmit();
  }
}

function changeZ(tool_z) {
//alert("curr="+currZTool + "  z="+tool_z);
  if (currToolZ != tool_z) {
    currToolZ = tool_z;
    mysubmit();
  }
}

function changeDrawingTool(changeTo) {
  //put it in later in v1.0
  if (currView != changeTo) {
    currView = changeTo;
    mysubmit(); 
  }    
  return;
  
/* useless
  with (document.constrain) {
    if (changeTo == g_hLine) {
      y_lo = parseFloat(current_y_lo.value);
      y_hi = parseFloat(document.constrain.current_y_hi? document.constrain.current_y_hi.value : document.constrain.current_y_lo.value);      
    //y_hi = parseFloat(document.constrain.current_y_hi.value);    
    document.constrain.current_y_lo.value = round(y_lo + (y_hi-y_lo)/2, 2);
  } else if (changeTo == g_vLine) {
    x_lo = parseFloat(document.forms[0].current_x_lo.value);
    x_hi = parseFloat(document.forms[0].current_x_hi? document.forms[0].current_x_hi.value : document.forms[0].current_x_lo.value);  
//    x_hi = parseFloat(document.forms[0].current_x_hi.value);    
    document.forms[0].current_x_lo.value = round(x_lo + (x_hi-x_lo)/2, 2);    
  } else if (changeTo == g_point) {
    x_lo = parseFloat(document.forms[0].current_x_lo.value);
    x_hi = parseFloat(document.forms[0].current_x_hi? document.forms[0].current_x_hi.value : document.forms[0].current_x_lo.value);  
    y_lo = parseFloat(document.forms[0].current_y_lo.value);
    y_hi = parseFloat(document.forms[0].current_y_hi? document.forms[0].current_y_hi.value : document.forms[0].current_y_lo.value);      
    //y_hi = parseFloat(document.forms[0].current_y_hi.value);        
    document.forms[0].current_x_lo.value = round(x_lo + (x_hi-x_lo)/2, 2);   
    document.forms[0].current_y_lo.value = round(y_lo + (y_hi-y_lo)/2, 2);
  }
  document.forms[0].submit();
*/
}

function initializeDrawing() {
  var x, y;
  if (currView == g_box) {
    x_lo_px = valueToPix(x_lo, currImage.lowX, currImage.highX, currImage.width, currImage.offX);
    y_lo_px = valueToPix(y_lo, currImage.lowY, currImage.highY, currImage.height, currImage.offY);
    x_hi_px = valueToPix(x_hi, currImage.lowX, currImage.highX, currImage.width, currImage.offX);
    y_hi_px = valueToPix(y_hi, currImage.lowY, currImage.highY, currImage.height, currImage.offY);      
  } else if (currView == g_hLine) {
    x_lo_px = valueToPix(x_lo, currImage.lowX, currImage.highX, currImage.width, currImage.offX);
    x_hi_px = valueToPix(x_hi, currImage.lowX, currImage.highX, currImage.width, currImage.offX);
    y = parseFloat(y_lo) + (parseFloat(y_hi) - parseFloat(y_lo))/2.0;
//    y = getMiddlePoint(y_lo, y_hi);
    y_hi_px = valueToPix(y, currImage.lowY, currImage.highY, currImage.height, currImage.offY);      
    y_lo_px = y_hi_px;
//    alert("AAA y_lo=" + y_lo + " y_hi= "+y_hi+" y="+y);
  } else if (currView == g_vLine) {
    y_lo_px = valueToPix(y_lo, currImage.lowY, currImage.highY, currImage.height, currImage.offY);
    y_hi_px = valueToPix(y_hi, currImage.lowY, currImage.highY, currImage.height, currImage.offY);  
    x = parseFloat(x_lo) + (parseFloat(x_hi) - parseFloat(x_lo))/2.0;
    x_hi_px = valueToPix(x, currImage.lowX, currImage.highX, currImage.width, currImage.offX);
    x_lo_px = x_hi_px;    
  } else if (currView == g_point) {
    x = parseFloat(x_lo) + (parseFloat(x_hi) - parseFloat(x_lo))/2.0;
    y = parseFloat(y_lo) + (parseFloat(y_hi) - parseFloat(y_lo))/2.0;
    x_lo_px = valueToPix(x, currImage.lowX, currImage.highX, currImage.width, currImage.offX);
    x_hi_px = x_lo_px;
    y_lo_px = valueToPix(y, currImage.lowY, currImage.highY, currImage.height, currImage.offY);
    y_hi_px = y_lo_px;
  } else {
    alert("unknown view at initilization!");
  }
  Img.setView(currView);
  Img.redraw();
}

function initPage() {
   initializeDateWidget();
   initializeDrawing();
}

function round (n, d) {
  n = n - 0; // force number
  if (d == null) d = 2;
  var f = Math.pow(10, d);
  n += Math.pow(10, - (d + 1)); // round first
  n = Math.round(n * f) / f;
  n += Math.pow(10, - (d + 1)); // and again
  n += ''; // force string
  return d == 0 ? n.substring(0, n.indexOf('.')) :
      n.substring(0, n.indexOf('.') + d + 1);
}

function pixToValue(p, low, high, length) {
  return ((high - low)/length) * p + low;
}

function valueToPix(v, v_lo, v_hi, length_px, start_px) {
  return round((start_px + length_px * (v-v_lo)/(v_hi-v_lo)), 2);
}

function submitXML(x0, x1, y0, y1) {
  return; //test
  
/* useless
  lowX = round(pixToValue(x0 - currImage.offX, currImage.lowX, currImage.highX, currImage.width), 2);
  highX = round(pixToValue((x1 - currImage.offX), currImage.lowX, currImage.highX, currImage.width), 2);
  lowY = round(pixToValue((y0 - currImage.offY), currImage.lowY, currImage.highY, currImage.height), 2);
  highY = round(pixToValue((y1 - currImage.offY), currImage.lowY, currImage.highY, currImage.height), 2);

  Req = new LASRequest("1.0","file:las.xml");
  Req.setOperation("shade","gif");
  Req.setVariable(currImage.dataSet);
  Req.addFerretProperty("size",".5");
  Req.setXRange(lowX, highX);
  Req.setYRange(lowY, highY);
  Req.setZRange();
  Req.setTRange("05-JAN-1982 06:40");
  Req.setFerretProperty("use_ref_map", "false");


  document.forms[0].xml.value = Req.toString();
  document.forms[0].method = "GET";
//  document.forms[0].action = "http://stout.pmel.noaa.gov/SAT_DEMO_LAS-bin/LASserver.pl";
  document.forms[0].action = "http://tmap.pmel.noaa.gov:8180/LPS/ProductServer.do";

  var l_url = document.forms[0].action + "?xml=" + escape(document.forms[0].xml.value);

//alert(l_url);

  var winCom = "menubar=yes, toobar=yes, resizable=yes, scrollbars=yes";
//  var dataWindow = window.open(l_url, "DataWindowName", winCom);
*/

/* doe not work in IE
//  var dataWindow = window.open("", "DataWindowName", winCom);
//  var dataWindow = window.open("");
//  dataWindow.focus();

 // document.forms[0].target = dataWindow;
//  document.forms[0].target = "DataWindowName";
 // document.forms[0].submit();
 */
}

//This function does not work in the output/prod window
/*
function displayValue(ele, val) {
//window.status = "DDDD  " + ele + "  val=" + val;

   if (ele) {
     ele.value = val;
   }
}
*/

function displayValue(id, val) {
//window.status = "DDDD  " + ele + "  val=" + val;
  var _ele = document.getElementById(id);
  if (_ele) {
     _ele.value = val;
  }
}

 function displayCoords(x0, y0, x1, y1) {
  var _x_lo, _x_hi, _y_lo, _y_hi;
  if (window.name == "constrain") {
    _x_lo = document.constrain.current_x_lo;
    _x_hi = document.constrain.current_x_hi;
    _y_lo = document.constrain.current_y_lo;
    _y_hi = document.constrain.current_y_hi;
  } else if (window.name = "data") {
    _x_lo = document.prod.current_x_lo;
    _x_hi = document.prod.current_x_hi;
    _y_lo = document.prod.current_y_lo;
    _y_hi = document.prod.current_y_hi;
  }

  l_lowX = round(pixToValue(x0 - currImage.offX, currImage.lowX, currImage.highX, currImage.width), 2);
  l_highX = round(pixToValue((x1 - currImage.offX), currImage.lowX, currImage.highX, currImage.width), 2);
  l_lowY = round(pixToValue((y0 - currImage.offY), currImage.lowY, currImage.highY, currImage.height), 2);
  l_highY = round(pixToValue((y1 - currImage.offY), currImage.lowY, currImage.highY, currImage.height), 2);

  if (x0 <= x1) {
    _x_lo.value = l_lowX;
    _x_hi.value = l_highX;
//    displayValue(_x_lo, l_lowX);
//    displayValue(_x_hi, l_highX);
  } else {
    _x_lo.value = l_highX;
    _x_hi.value = l_lowX;
//    displayValue(_x_lo, l_highX);
//    displayValue(_x_hi, l_lowX);
  }

  if (y0 <= y1) {
    _y_lo.value = l_lowY;
    _y_hi.value = l_highY;
//    displayValue(_y_lo, l_lowY);
//    displayValue(_y_hi, l_highY);
  } else {
    _y_lo.value = l_highY;
    _y_hi.value = l_lowY;
//    displayValue(_y_lo, l_highY);
//    displayValue(_y_hi, l_lowY);
  }

 // window.status = currView;
}

function switchDisplay(id, inLine) {  
  var obj = document.getElementById(id);
  if (obj) {
    obj.style.display = (obj.style.display != "none"? "none" : (inLine? "inline" : "block"));
  }
}

function switchDisplays() {
  var id = arguments[0];
  var id1= arguments[1];
  var inLine = arguments[2];
  if (!id) {
    alert("missing id!");
    return;
  }  
  switchDisplay(id, inLine);
  if (id1) {
    switchDisplay(id1, inLine);
  }
}

function displayObject(id, showIt) {
  var obj = document.getElementById(id);
  if (obj) {
    if (showIt)
      obj.style.display = "block";
    else
      obj.style.display = "none"
  }
}

//Make a XML string more readable
function formatXML(s) {
  var retStr = "";
  var idx = s.indexOf("xml=");
  s = s.substr(idx, s.length - idx);
  for (i=0; i<s.length; i++) {
    if (s[i] == '<' && (s[i+1] != '/' || s[i-1] == '>'))
      retStr += "\n<";
    else if (s[i] == '+')
      retStr += " ";
    else
      retStr += s[i];
  }
  return retStr;
}


//from output.vm
function submitXML() {
  if (hasNoBox()) {
    alert("Please draw a box");
    return;
  }

  var url = new String(location.href);
  url = url.substring(0, url.indexOf(location.search)) + "?xml=";
  var newXml = createXML();
  if (newXml != null) {
    /*for test
    url += newXml;
    alert(formatXML(url));
    return;
    */
    url += escape(newXml);
//    document.write("<textarea cols='80' rows='50'>" + url + "</textarea>");
    window.location = url;
  }
}

function showXML() {
  var s = new String(unescape(document.URL));
  alert(formatXML(s));
}

function confirmUpdate(xmlString ) {
  if (hasNoBox()) {
    alert("Please draw a box");
    return;
  }

  var url = new String(location.href);
  url = url.substring(0, url.indexOf(location.search)) + "?xml=";
  var newXml = createXML(xmlString );

  if (newXml != null) {
   /*for test
    url += newXml;
    alert(formatXML(url));
    return;
    */
    if (confirmSubmit(url + newXml)) {
      url += escape(newXml);
      window.location = url;
    }
  }
}

function confirmSubmit(xmlStr)
{
var agree=confirm("Are you sure you wish to submit?\n" + formatXML(xmlStr));
if (agree)
	return true ;
else
	return false ;
}

function hasNoBox() {
  with (document.prod) {
    return (current_x_lo.value.length == 0 || current_x_hi.value.length == 0 || current_y_lo.value.length == 0 || current_y_hi.value/length == 0);
  }
	  return false;
}


