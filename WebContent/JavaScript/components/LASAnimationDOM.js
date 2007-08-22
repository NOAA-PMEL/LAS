
function LASAnimation(xml) {
    if (!xml) xml = "<?xml version=\"1.0\"?><lasAnimation><dataset>coads_climatology_cdf</dataset><frame>15-Jan</frame><frame>15-Feb</frame><frame>15-Mar</frame><frame>15-Apr</frame><frame>15-May</frame><frame>15-Jun</frame><frame>15-Jul</frame><frame>15-Aug</frame><frame>15-Sep</frame><frame>15-Oct</frame><frame>15-Nov</frame><frame>15-Dec</frame></lasAnimation>";
    this.DOM = new XMLDoc(xml,_LASAni_parseError);
    
    this.getFillLevels = LASAni_getFillLevels;
    this.getHasT = LASAni_getHasT;
    this.getFrames     = LASAni_getFrames;
    this.getNumFrames  = LASAni_getNumFrames;
    this.getTimeUnits = LASAni_getTimeUnits;
    this.getTimeResolution = LASAni_getTimeResolution;
    this.getDepAxisScale = LASAni_getDepAxisScale;
}

function LASAni_getFillLevels(){
    var filllevelsNode = this.DOM.selectNode("/fill_levels");
    return filllevelsNode.getText();
}

function LASAni_getDepAxisScale(){
    var depAxisScaleNode = this.DOM.selectNode("/dep_axis_scale");
    return depAxisScaleNode.getText();
}

function LASAni_getHasT(){
    var hasTNode = this.DOM.selectNode("/hasT");
    return hasTNode.getText();
}

function LASAni_getFrames() {
    //===get all the time frames
    var framesNode = this.DOM.selectNode("/frames");
    var frameElements = framesNode.getElements("frame");
 
    var unitsNode = this.DOM.selectNode("/units");
    var resolutionNode = this.DOM.selectNode("/resolution"); 

    theTimes = new Array();
    for (var i = 0; i < frameElements.length; i++){
        //if((unitsNode.getText() == "months")&&(resolutionNode.getText() == "months")){
        //append day(dd) in front of a date string, e.g., JAN-1997
        if(unitsNode.getText() == "months"){
            theTimes[i]= "15-"+frameElements[i].getText();
        }else if(unitsNode.getText() == "years"){
            theTimes[i]= "15-DEC"+frameElements[i].getText();
        }else{
            theTimes[i]=frameElements[i].getText();
        } 
    };
    return theTimes;
}

function LASAni_getNumFrames() {
    var framesNode = this.DOM.selectNode("/frames");
    var frameElements = framesNode.getElements("frame");
    return frameElements.length;
}

function LASAni_getTimeUnits(){
    var unitsNode = this.DOM.selectNode("/units");
    return unitsNode.getText();
}

function LASAni_getTimeResolution(){
    var resolutionNode = this.DOM.selectNode("/resolution");
    return resolutionNode.getText();
}

/**
 * Error handler passed to XMLDoc creation method.
 * @private
 */
function _LASAni_parseError(e) {
  alert(e);
}

