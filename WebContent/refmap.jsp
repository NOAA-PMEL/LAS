
<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-template.tld" prefix="template" %>
<%@ taglib uri="/WEB-INF/struts-nested.tld" prefix="nested" %>

<script language="JavaScript" src="./js/TestImages.js"></script>
<script language="JavaScript" src="./js/LASRequest.js"></script>

<script language="JavaScript">
var currImage = new Object();
currImage = images[4];
var currView = null;

var drawObj = "jdfjhf";
var Img = null;

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

function getMapValue(p, low, high, length) {
  return ((high - low)/length) * p + low;
}

//do not need idx here when switch to use server object because there is only one image
function submitXML(x0, x1, y0, y1) {

  return;
  
  
  lowX = round(getMapValue(x0 - currImage.offX, currImage.lowX, currImage.highX, currImage.width), 2);
  highX = round(getMapValue((x1 - currImage.offX), currImage.lowX, currImage.highX, currImage.width), 2);
  lowY = round(getMapValue((y0 - currImage.offY), currImage.lowY, currImage.highY, currImage.height), 2);
  highY = round(getMapValue((y1 - currImage.offY), currImage.lowY, currImage.highY, currImage.height), 2);

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

/* doe not work in IE
//  var dataWindow = window.open("", "DataWindowName", winCom);
//  var dataWindow = window.open("");
//  dataWindow.focus();

 // document.forms[0].target = dataWindow;
//  document.forms[0].target = "DataWindowName";
 // document.forms[0].submit();
 */
}

 function displayValue(id, val) {
   ele = document.getElementById(id);
   if (ele) {
     ele.value = val;
   }
 }

 function displayCoords(x0, y0, x1, y1) {
  l_lowX = round(getMapValue(x0 - currImage.offX, currImage.lowX, currImage.highX, currImage.width), 2);
  l_highX = round(getMapValue((x1 - currImage.offX), currImage.lowX, currImage.highX, currImage.width), 2);
  l_lowY = round(getMapValue((y0 - currImage.offY), currImage.lowY, currImage.highY, currImage.height), 2);
  l_highY = round(getMapValue((y1 - currImage.offY), currImage.lowY, currImage.highY, currImage.height), 2);

  displayValue("lowX", l_lowX);
  displayValue("highX", l_highX);
  displayValue("lowY", l_lowY);
  displayValue("highY", l_highY);
 }

//test function
function switchImage(v) {
/*
  if (v == "point" || v == "h_line" || v == "v_line")
    currImage = images[0];
  else if (v == "v_range")
    currImage = images[3];
  else if (v == "h_range")
    currImage = images[2];
  else
    currImage = images[1];
*/
  currView = v;
  document.getElementById("imgWindow").src = "./draw.htm";
}

function test () {
	var o = document.getElementById("imgWindow");
	alert(o.parentNode.childNodes[1].name);
}

function go() {
s = "view: " + document.getElementById("view")[document.getElementById("view").selectedIndex].value;
s += "\n" + "time: " + document.forms[0].t.value;

if (typeof l_lowX != "undefined")
  s += "\n" + "lowX: " + l_lowX;
if (typeof l_highX != "undefined")
  s += "\n" + "highX: " + l_highX;
if (typeof l_lowY != "undefined")
  s += "\n" + "lowY: " + l_lowY;
if (typeof l_highY != "undefined")
  s += "\n" + "highY: " + l_highY;

alert("submit form:\n" + s);
}

//alert("drawObj: " + drawObj);
currView = "rect";
//alert("currView: " + currView);

</script>



<input type="hidden" name="xml" value="">

<table border="0" width="100%">
<tr>
<td id="img_table_cell">
<iframe src="./html/draw.htm" id="imgWindow" name="imgWindow" frameborder="1" scrolling="no" style="width:720px; height:485px">
11 Sorry, this is a floating frame which is not supported by your browser.
</iframe>
</td>
<td align="left">
Drawing Tools:<br>
<tiles:insert page="/drawtools.jsp" flush="true"/>
</td>
</tr>
</table>





