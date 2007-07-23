
function LASAnimation(xml) {
    if (!xml) xml = "<?xml version=\"1.0\"?><lasAnimation><dataset>coads_climatology_cdf</dataset><frame>15-Jan</frame><frame>15-Feb</frame><frame>15-Mar</frame><frame>15-Apr</frame><frame>15-May</frame><frame>15-Jun</frame><frame>15-Jul</frame><frame>15-Aug</frame><frame>15-Sep</frame><frame>15-Oct</frame><frame>15-Nov</frame><frame>15-Dec</frame></lasAnimation>";
    this.DOM = new XMLDoc(xml,_LASAni_parseError);
    
    this.getDataset    = LASAni_getDataset;
    this.getVariable    = LASAni_getVariable;
    this.getOperation  = LASAni_getOperation;
    this.getFillLevels = LASAni_getFillLevels;
    this.getXLo        = LASAni_getXLo;
    this.getXHi        = LASAni_getXHi;
    this.getYLo        = LASAni_getYLo;
    this.getYHi        = LASAni_getYHi;
    this.getFrames     = LASAni_getFrames;
    this.getNumFrames  = LASAni_getNumFrames;
    this.getImageSizeX = LASAni_getImageSizeX;
    this.getImageSizeY = LASAni_getImageSizeY;
}

function LASAni_getDataset(){
    var nodePath = '/dataset';
    var Node = this.DOM.selectNode(nodePath);
    if (Node) {
        return this.DOM.selectNodeText(nodePath);
    } else {
        return null;
    }
    //return 'coads_climatology_cdf';
}

function LASAni_getVariable(){
    return 'sst';
}

function LASAni_getOperation() {
    return 'Plot_2D_XY';
}

function LASAni_getFillLevels(){
    //return '(-INF)(-2 32,1)(INF)';
    var filllevelsNode = this.DOM.selectNode("/fill_levels");
    return filllevelsNode.getText();
}

function LASAni_getXLo(){
    return -180;
}

function LASAni_getXHi(){
    return 180;
}

function LASAni_getYLo(){
    return -90;
}

function LASAni_getYHi(){
    return 90;
}

function LASAni_getFrames() {
    //===get all the time frames
    //var domTree = this.DOM.docNode;
    //var framesNode = domTree.getElements("frames")[0];
    var framesNode = this.DOM.selectNode("/frames");
    //var frameElements = domTree.getElements("frame");
    var frameElements = framesNode.getElements("frame");
    //document.write(frameElements.length);
    //document.write(frameElements[11].getText());
 
    theTimes = new Array();

    //theTimes[0]='15-Jan';
    //theTimes[0]=frameElements[0].getText();
    //theTimes[1]=frameElements[1].getText();
    //theTimes[2]=frameElements[2].getText();
    //theTimes[3]=frameElements[3].getText();
    //theTimes[4]=frameElements[4].getText();
    //theTimes[5]=frameElements[5].getText();
    //theTimes[6]=frameElements[6].getText();
    //theTimes[7]=frameElements[7].getText();
    //theTimes[8]=frameElements[8].getText();
    //theTimes[9]=frameElements[9].getText();
    //theTimes[10]=frameElements[10].getText();
    //theTimes[11]=frameElements[11].getText();

    for (var i = 0; i < frameElements.length; i++){
        theTimes[i]=frameElements[i].getText(); 
    };

    return theTimes;
}

function LASAni_getNumFrames() {
    //var domTree = this.DOM.docNode;
    var framesNode = this.DOM.selectNode("/frames");
    var frameElements = framesNode.getElements("frame");
    
    return frameElements.length;
}

function LASAni_getImageSizeX() {
    //===get all the time frames
    var domTree = this.DOM.docNode;
    imageSizeX_Elements = domTree.getElements("x_image_size");
    return imageSizeX_Elements[0].getText();
}

function LASAni_getImageSizeY() {
    //===get all the time frames
    var domTree = this.DOM.docNode;
    imageSizeY_Elements = domTree.getElements("y_image_size");
    return imageSizeY_Elements[0].getText();
}

/**
 * Error handler passed to XMLDoc creation method.
 * @private
 */
function _LASAni_parseError(e) {
  alert(e);
}

