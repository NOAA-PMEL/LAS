//Global variables
//view definition
//var g_box = "rect";
var g_box = parent.g_box;
//var g_vLine = "v_line";
var g_vLine = "vert";
//var g_hLine = "h_line";
var g_hLine = "horiz";
var g_point = "point";
var g_vRange = "v_range";
var g_hRange = "h_range";

var drawObj = null;
var X0 = 1;
var Y0 = 1;
var X1 = -1;
var Y1 = -1;
 
function LASDrawingObj() {
  this.getState = getState;
  this.setState = setState;
  this.setView = setView;
  this.getView = getView;
  this.setCursor = setCursor;
  this.getX = getX;
  this.getY = getY;
  this.setX0 = setX0;
  this.setX1 = setX1;
  this.setY0 = setY0;
  this.setY1 = setY1;  
  this.minX = -1;
  this.minY = -1;
  this.maxX = -1;
  this.maxY =-1;
  this.getMinX = getMinX;
  this.setMinX = setMinX;
  this.getMinY = getMinY;
  this.setMinY = setMinY;
  this.getMaxX = getMaxX;
  this.setMaxX = setMaxX;
  this.getMaxY = getMaxY;
  this.setMaxY = setMaxY;
  this.setObj = setObj;
  this.displayCentralBox = displayCentralBox;
  this.displayBox = displayBox;
  this.ChangeTool = ChangeTool;
  this.redraw = redraw;   

  //cnteral small box for moving the drawed object
  this.setCBoxSize = setCBoxSize;
}

//function redraw(x0, x1, y0, y1) {
function redraw() {
  var currView = drawObj.getView();
  var showCentBox = true;

  if (currView == g_point) { //not diplay the center box
     showCentBox = false;
  }

//  if (currView == g_box) {
    X0 = parent.x_lo_px;
    X1 = parent.x_hi_px;
    Y0 = parent.y_lo_px;
    Y1 = parent.y_hi_px;
//  }
  displayBox(true);
  displayCentralBox(showCentBox);  
}

function setObj(obj) {
  drawObj = obj;
}

function getState() {
//alert(this.state);
  return this.state;
}

function setState(state) {
//alert("state=" + state + "   this.getStae()=" + this.getState());
  this.preState = this.state;
  this.state = state;
}

function setStateBack() {
  //this.state = this.prevState;
  setState(this.prevState);
}

function getView() {
  return this.view;
}
 
function setView(view) {
  this.view = view;
}

function setX0(x) {
  this.X0 = x;
}

function setX1(x) {
  this.X1 = x;
}

function setY0(y) {
  this.Y0 = y;
}

function setY1(y) {
  this.Y1 = y;
}

function getMinX() {
  return this.minX;
}

function setMinX(x) {
  this.minX = x;
}

function getMinY() {
  return this.minY;
}

function setMinY(y) {
  this.minY = y;
}

function getMaxX() {
  return this.maxX;
}

function setMaxX(x) {
  this.maxX = x;
}

function getMaxY() {
  return this.maxY;
}

function setMaxY(y) {
  this.maxY = y;
}

function getRubberObj(id) {
   if (document.all)
     return document.all[id];
   if (document.getElementById)
     return document.getElementById(id);
   return null;
}

function setCursor(id, c_name) {
  var o = getRubberObj(id);   
  o.style.cursor = c_name;  
}

function setBoxStartPosition(r, left, top) {
  if (r != null) {
     if (document.all) {
       r.style.pixelLeft = left;
       r.style.pixelTop  = top;
     }
     else if (document.getElementById) {
       r.style.left = left + 'px';
       r.style.top = top + 'px';
     }  
  }
}

function setBoxSize(r, w, h) {
  if (r != null) {
     r.style.width = w;
     r.style.height = h;  
  }
}

function ChangeTool(prevView, currView) {
//for test
var l_x0 = X0;
var l_x1 = X1;
var l_y0 = Y0;
var l_y1 = Y1;

  var showBox;
  var showCentBox = true;
  if (prevView == g_box && currView == g_hLine) { //box to h_line
    Y1 = Y0 + (Y1-Y0)/2;
    Y0 = Y1;
    showBox = true;
  } else if (prevView == g_box && currView == g_vLine) { //box to v_line
    X1 = X0 + (X1-X0)/2;
    X0 = X1;
    showBox = true;
  } else if (prevView == g_box && currView == g_point) { //box to point
    X1 = X0 + (X1-X0)/2;
    X0 = X1;
    Y1 = Y0 + (Y1-Y0)/2;
    Y0 = Y1;
    showBox = true; 
    showCentBox = false;
  } else if ((prevView == g_vLine || prevView == g_hLine) && currView == g_point) { //line to point
    X1 = X0 + (X1-X0)/2;
    X0 = X1;
    Y1 = Y0 + (Y1-Y0)/2;
    Y0 = Y1;
    showBox = true; 
    showCentBox = false;    
  } else {
    X0=X1;
    Y0=Y1;
    showBox = false;
    showCentBox = false;
  }
  displayBox(showBox);
  displayCentralBox(showCentBox);
  
//  window.status = "before: " + l_x0 + " " + l_x1 + ', ' + l_y0 + " " + l_y1 + " after: " + X0 + "/" + X1 + " " + Y0 + "/" + Y1;
}

function changeBoxToHLine() {
   Y1 = Y0;
   
   displayBox(true);   
   displayCentralBox(true);
}

function setBoxVisible(r, showIt) {
  if (r != null) {
    if (showIt == true)
      r.style.visibility = 'visible';
    else
      r.style.visibility = 'hidden';
  }
}

function getX (evt) {
   var x = null;
   if (document.all) {
     if (event.x < drawObj.getMinX())
       x = drawObj.getMinX();
     else if (event.x > drawObj.getMaxX())
       x = drawObj.getMaxX();
     else
       x = event.x;
   } else if (document.getElementById) {
     if (evt.clientX < drawObj.getMinX())
       x = drawObj.getMinX();
     else if (evt.clientX > drawObj.getMaxX())
       x = drawObj.getMaxX();
     else
       x = evt.clientX;
   }
 
   return x;
}
 
function getY (evt) {
   var y = null;
   if (document.all) {
     if (event.y < drawObj.getMinY())
       y = drawObj.getMinY();
     else if (event.y > drawObj.getMaxY())
       y = drawObj.getMaxY();
     else
       y = event.y;
   } else if (document.getElementById) {
     if (evt.clientY < drawObj.getMinY())
       y = drawObj.getMinY();
     else if (evt.clientY > drawObj.getMaxY())
       y = drawObj.getMaxY();
     else
       y = evt.clientY;
   }
  
   return y;
} 

function moveHorizonalLine (evt) {
   if (drawObj.state == "")
	return;

   X1 = getX(evt);
   Y1 = getY(evt);
     
   displayBox(true);

   parent.displayCoords(X0, Y0, X1, Y1);
}

function drawVLine (evt) {
   if (drawObj.state == "")
	return;

   var r = null;
   if (document.all) {
     r = document.all.rubberBand;

     X1 = getX(evt);
     Y1 = getY(evt);
     r.style.pixelLeft = X0;
     
     if (Y1 < Y0)
       r.style.pixelTop = Y1;
     else
       r.style.pixelTop = Y0;

     r.style.width = 0;
     r.style.height = Math.abs(Y1 - Y0);
   }
   else if (document.getElementById) {
     r = document.getElementById('rubberBand');

     X1 = getX(evt);
     Y1 = getY(evt);
     
     r.style.left = X0 + 'px';

     if (Y1 < Y0)
       r.style.top = Y1 + 'px';
     else
       r.style.top = Y0 + 'px';   

     r.style.width = 0;
     r.style.height = Math.abs(Y1 - Y0);
   }

//   if (r)
//     r.style.border = 1;
     
   parent.displayCoords(X0, Y0, X1, Y1);
}

function displayBox(showIt) {
   var r = getRubberObj("rubberBand");
   var left = Math.min(X0, X1);
   var top = Math.min(Y0, Y1);
   var w = Math.abs(X1 - X0);
   var h = Math.abs(Y1 - Y0);
   
   setBoxStartPosition(r, left, top);
   setBoxSize(r, w, h);
   setBoxBorderStyle(r, "solid");
   setBoxVisible(r, showIt);
}

function getBoxLetfTop(o) {
  v = o.style.leftTop;
  i = v.indexOf("px");
  return parseInt(v.substring(0, i));
}

function getBoxStartX(o) {
  v = o.style.pixelLeft;
  i = v.indexOf("px");
  return parseInt(v.substring(0, i));
}

function getBoxStartY(o) {
  v = o.style.pixelTop;
  i = v.indexOf("px");
  return parseInt(v.substring(0, i));
}

function getBoxWidth(o) {
  v = o.style.width;
  i = v.indexOf("px");
  return parseInt(v.substring(0, i));
}

function getBoxHeight(o) {
  v = o.style.height;
  i = v.indexOf("px");
  return parseInt(v.substring(0, i));
}

//assume the central box is always a square
function getCBoxSize(o) {
  v = o.style.width;
  i = v.indexOf("px");
  return parseInt(v.substring(0, i));
}

function setCBoxSize(w) {
  var r_c = getRubberObj("rubberBand_c");
  if (r_c) {
    if (document.all) {  //IE
      r_c.style.height = w;
      r_c.style.width = w;
      r_c.style.cursor = "hand";
    }
    else if (document.getElementById) { //Mozalla
      r_c.style.height = w-2;
      r_c.style.width = w-2;
      r_c.style.cursor = "pointer; hand";
    }
  }
}

function displayCentralBox(showIt) {
  var r_c = getRubberObj("rubberBand_c");
  var s = getCBoxSize(r_c);
  
  setBoxVisible(r_c, showIt);
  
  if (Math.abs(X1 - X0) < s && Math.abs(Y1 - Y0) < s)
    return;

  var x = (Math.abs(X1 - X0)/2) - s;
  var y = (Math.abs(Y1 - Y0)/2) - s;
  
//for test   
//window.status = x + "---nn---" + y;

  setBoxStartPosition(r_c, x, y);
}

function drawBox (evt) {
   if (drawObj.getState() == null || drawObj.getView() != g_box)
	return;
   
   X1 = getX(evt);
   Y1 = getY(evt);
   
   displayBox(true);   
   displayCentralBox(true);

   parent.displayCoords(X0, Y0, X1, Y1);   
}

function drawHLine(evt) {
 //  window.status = drawObj.getView();

   if (drawObj.getState() == null || drawObj.getView() != g_hLine)
	return;
   
   X1 = getX(evt);
   Y1 = Y0;
   
   displayBox(true);   
   displayCentralBox(true);

   parent.displayCoords(X0, Y0, X1, Y1);   
}

function drawVLine(evt) {
//   window.status = drawObj.getView();

   if (drawObj.getState() == null || drawObj.getView() != g_vLine)
	return;
   
   X1 = X0
   Y1 = getY(evt);
   
   displayBox(true);   
   displayCentralBox(true);

   parent.displayCoords(X0, Y0, X1, Y1);   
}

function drawPoint(evt) {
//   window.status = drawObj.getView();

   if (drawObj.getState() == null || drawObj.getView() != g_point)
	return;
   
   X1 = X0;
   Y1 = Y0;

   //displayBox(true);   
   //displayCentralBox(true);

   parent.displayCoords(X0, Y0, X1, Y1);   
}

function drawRange(evt) {
 //  window.status = drawObj.getView();

   if (drawObj.getState() == null || (drawObj.getView() != g_vRange && drawObj.getView() != g_hRange)) {
     alert("incorrect view");
     return;
   }
   
   X1 = getX(evt);
   Y1 = getY(evt);
   
   displayRange(drawObj.getView(), evt);   
   displayCentralBox(true);

   parent.displayCoords(X0, Y0, X1, Y1);   
}

function displayRange(view, evt) {   
   if (view == g_vRange) {
     X0 = drawObj.getMinX();
     X1 = drawObj.getMaxX();     
     Y1 = getY(evt);
   } else {
     Y0 = drawObj.getMinY();
     Y1 = drawObj.getMaxY();
     X1 = getX(evt);
   }

   displayRangeBox(view);   
   displayCentralBox(true);

   parent.displayCoords(X0, Y0, X1, Y1);  
}

function displayRangeBox(view) {
   var r = getRubberObj("rubberBand");
   var left = Math.min(X0, X1);
   var top = Math.min(Y0, Y1);
   var w = Math.abs(X1 - X0);
   var h = Math.abs(Y1 - Y0);
   
   if (view == g_vRange) 
     setBoxBorderStyle(r, "solid none");
   else if (view == g_hRange)
     setBoxBorderStyle(r, "none solid");
   else
     alert("unknow range-view");

 //  setBoxBorderColor(r, "red");
   setBoxStartPosition(r, left, top);
   setBoxSize(r, w, h);

//   setBoxBorderColor(getRubberObj("rubberBand_c"), "red");
}

function setBoxBorderStyle(obj, s) {
  obj.style.borderStyle = s;
}

function setBoxBorderColor(obj, c) {
  obj.style.borderColor = c;
}

function startDrawing (evt) {
   if (drawObj.getState() == "moving") {
     document.onmousemove = moveBox;
     return;
   }

   drawObj.setState("drawing");
  
   var r = getRubberObj("rubberBand");
   var currView = drawObj.getView();
     
   X0 = getX(evt);
   Y0 = getY(evt);  

   displayCentralBox(false);

   setBoxStartPosition(r, X0, Y0);
   setBoxSize(r, 0, 0);  
   setBoxVisible(r, true);

   //for test
   //window.status = currView;
   
   if (currView == g_box)
     document.onmousemove = drawBox;
   else if (currView == g_hLine)
     document.onmousemove = drawHLine;   
   else if (currView == g_vLine)
     document.onmousemove = drawVLine;     
   else if (currView == g_point)
     document.onmousemove = drawPoint;
   else if (currView == g_hRange || currView == g_vRange)
     document.onmousemove = drawRange; 
   else
     alert("unkown view");
}

function stopDrawing (evt) {
   if (drawObj.getState() == "moving") {
     drawObj.setState(null);
     return;
   }   
   
   X1 = getX(evt);
   Y1 = getY(evt);
   
   //may need to swap
   l_x = Math.max(X0, X1);
   if (X1 != l_x) {
     X0 = X1;
     X1 = l_x;
   }
   l_y = Math.max(Y0, Y1);
   if (Y1 != l_y) {
     Y0 = Y1;
     Y1 = l_y;
   }
      
   //if do not want display the box when the mouse up
   //setBoxVisible(getRubberObj("rubberBand"), false);

   document.onmousemove = null;      
 //  parent.submitXML(X0, X1, Y0, Y1);
}

function moveBox(evt) {
   if (drawObj.getState() != "moving")
     return;

   var r = getRubberObj("rubberBand");
   var r_x0 = r.style.pixelLeft;
   var r_y0 = r.style.pixelTop;
   //var r_x0 = getBoxStartX(r);
   //var r_y0 = getBoxStartY(r);
   var r_w = getBoxWidth(r);
   var r_h = getBoxHeight(r);

   var m_x = getX(evt);
   var m_y = getY(evt);  

   //This is needed for the FireFox
   r_w = isNaN(r_w)? 0 : r_w;
   r_h = isNaN(r_h)? 0 : r_h;
   
   //moving the box
   m_x0 = m_x - r_w/2;
   if (m_x0 < drawObj.getMinX())
     m_x0 = drawObj.getMinX();
   else if ((m_x0 + r_w) > drawObj.getMaxX())
     m_x0 = drawObj.getMaxX() - r_w;
     
   m_y0 = m_y - r_h/2;
   if (m_y0 < drawObj.getMinY())
     m_y0 = drawObj.getMinY();
   else if ((m_y0 + r_h) > drawObj.getMaxY())
     m_y0 = drawObj.getMaxY() - r_h;
     
   setBoxStartPosition(r, m_x0, m_y0);
   setBoxSize(r, r_w, r_h);  
   
   
   parent.displayCoords(m_x0, m_y0, m_x0+r_w, m_y0+r_h);
}

/*need not it
 function onMouseMove(evt) {
   //if (inDrawing == false)
   //  parent.displayCoords(evt);
   //parent.displayCoords(X1, Y1);
   parent.displayCoords(X0, Y0, X1, Y1);
 }
*/

/*needn't it
function changeView(view) {
window.status = currView;
  this.preView = currView;
  currView = view;
//  drawObj.setState(view);
}

function changeViewBack() {
  currView = preView;
//  drawObj.setState("drawing");
}
*/








