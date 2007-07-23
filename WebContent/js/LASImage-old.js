
////////////////////////////////////////////////////////////
//                                                        //
// Define methods of the LASImage object                  //
//                                                        //
////////////////////////////////////////////////////////////
function LASImg_setView(view) {
  this.drawObj.setView(view);
}

function LASImg_getView() {
  return this.drawObj.getView();
}

function LASImg_setState(s) {
  this.drawObj.setState(s);
}

function LASImg_getState() {
  return this.drawObj.getState();
}

function LASImg_getRubberband() {
//  return this.rubberband;
   if (document.all) {
     return document.all.rubberBand;
   }
   else if (document.getElementById) {
     return document.getElementById('rubberBand');
   } else {
     return null;
   }
}

function LASImg_setCursor(id, c_name) {
  this.drawObj.setCursor(id, c_name);
}

function LASImg_setDrawingArea(x0, y0, x1, y1) {
  this.drawObj = new LASDrawingObj();
  this.drawObj.setObj(this.drawObj);
  this.drawObj.setMinX(x0);
  this.drawObj.setMaxX(x1);
  this.drawObj.setMinY(y0);
  this.drawObj.setMaxY(y1);
  this.drawObj.setCBoxSize(6);
  this.drawObj.setState("drawing");
//  document.onmousedown = this.drawObj.startDrawing;
//  document.onmouseup = this.drawObj.stopDrawing;
}

function LASImg_setViewColor(color) {
  var r = this.getRubberband();
  if (r)
    r.style.borderColor = color;
}

function LASImg_setImageWindow(obj) {
  this.ImageWindow = obj;
}

function LASImg_getImageWindow() {
  return this.ImageWindow;
}

////////////////////////////////////////////////////////////
//                                                        //
// Define the LASImage object                             //
//                                                        //
////////////////////////////////////////////////////////////
 
function LASImage(obj) {
  this.setView = LASImg_setView;
  this.getView = LASImg_getView;
  this.setState = LASImg_setState;
  this.getState = LASImg_getState;
  this.getRubberband = LASImg_getRubberband;  
  this.setDrawingArea = LASImg_setDrawingArea;  
  this.setViewColor = LASImg_setViewColor;
  this.getImageWindow = LASImg_getImageWindow;
  this.setImageWindow = LASImg_setImageWindow;
  this.setCursor = LASImg_setCursor;  
  this.Image = obj;
    
  document.onmousedown = startDrawing;
  document.onmouseup = stopDrawing;
}







