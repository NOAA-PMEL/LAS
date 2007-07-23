
function LASAnimation(xml) {
    if (!xml) xml = "<?xml version=\"1.0\"?><lasAnimation><dataset>coads_climatology_cdf</dataset><frame>15-Jan</frame><frame>15-Feb</frame><frame>15-Mar</frame><frame>15-Apr</frame><frame>15-May</frame><frame>15-Jun</frame><frame>15-Jul</frame><frame>15-Aug</frame><frame>15-Sep</frame><frame>15-Oct</frame><frame>15-Nov</frame><frame>15-Dec</frame></lasAnimation>";
    this.DOM = new XMLDoc(xml,_LASAni_parseError);
    
    this.getFillLevels = LASAni_getFillLevels;
    this.getFrames     = LASAni_getFrames;
    this.getNumFrames  = LASAni_getNumFrames;
}

function LASAni_getFillLevels(){
    var filllevelsNode = this.DOM.selectNode("/fill_levels");
    return filllevelsNode.getText();
}

function LASAni_getFrames() {
    //===get all the time frames
    var framesNode = this.DOM.selectNode("/frames");
    var frameElements = framesNode.getElements("frame");
 
    theTimes = new Array();
    for (var i = 0; i < frameElements.length; i++){
        theTimes[i]=frameElements[i].getText(); 
    };

    return theTimes;
}

function LASAni_getNumFrames() {
    var framesNode = this.DOM.selectNode("/frames");
    var frameElements = framesNode.getElements("frame");
    return frameElements.length;
}

/**
 * Error handler passed to XMLDoc creation method.
 * @private
 */
function _LASAni_parseError(e) {
  alert(e);
}

