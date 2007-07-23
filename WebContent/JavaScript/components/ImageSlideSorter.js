/**
 * @fileoverview Javascript object and methods to manage select menu creation
 * for a table with images and select menus in each cell.  The goal of 
 * this code is to emulate a Slide Sorter lightbox which allows
 * users to arrange slides however they want.
 * <p>
 * Inspiration for this functionality came from the 
 * {@link http://www.ngdc.noaa.gov/wist/ Web Image Slide Tray}.
 * <p>
 * Functionality includes automated generation of select menus
 * and automatic replacement of images in the table cells
 * whenever a menu option is selected.
 * <p>
 * Throughout this code the word 'Select' will refer to the 
 * form elements that are updated and the word 'Menu' will refer
 * to an element from the ISSInitObject[] array of objects that contain
 * the contents used to populate the Select objects.
 * <p>
 * The basic structure of the HTML table is shown below with
 * individual cell select objects named 'widgetCell#'.  The full suite of
 * available menus are given by the 'iss_G#' objects.
 *
 * <pre>
 *     ____________________
 *     !  G1   G2   G3   !
 *     !_________________!_
 *     !     !     !     !
 *     ! S11 ! S12 ! S13 !
 *     !_____!_____!_____!_
 *     !     !     !     !
 *     ! S21 ! S22 ! S23 !
 *     !_____!_____!_____!_
 *     !     !     !     !</pre>
 *
 * @author Jonathan Callahan
 * @version $Revision: 1293 $
 */ 

/**
 * Construct a new ImageSlideSorter object.<br>
 * @class This is the basic ImageSlideSorter class.
 * @constructor
 * @param {string} form name of the form in which the ImageSlideSorter is located
 * @param {int} numRows number of rows in the table
 * @param {int} numCols number of columns in the table
 * @param {string} chosenMenuName name of the 'Menu' that will be used for the initial
 * population of the table
 * @return A new ImageSlideSorter object
 */
function ImageSlideSorter(form,numRows,numCols,chosenMenuName) {

/*
 * The ImageSlideSorter object must be attached to the document
 * so that it is available to the 'ImageSlideSorter_cellWidgetChoice' event
 * handler which is a function of the document and not of
 * the ImageSlideSorter object.
 */

  document.ISS = this;

// Internal Constants


// Public functions

  this.render = ImageSlideSorter_render;

  this.createCellWidgets = ImageSlideSorter_createCellWidgets;
  this.createGlobalWidgets = ImageSlideSorter_createGlobalWidgets;
  this.createGlobalRadios = ImageSlideSorter_createGlobalRadios;
  //this.loadCellImage = ImageSlideSorter_loadCellImage;
  this.loadAllImages = ImageSlideSorter_loadAllImages;
  this.loadContentCell = ImageSlideSorter_loadContentCell;
//  this.handleLASResponse = ImageSlideSorter_handleLASResponse;

// Internal functions

// Callback functions (when a Widget event is triggered)

  document.globalWidgetChoice = ImageSlideSorter_globalWidgetChoice;
  document.cellWidgetChoice = ImageSlideSorter_cellWidgetChoice;

// Event handlers

  document.radioChoice = ImageSlideSorter_radioChoice;
  document.imgComplete = ImageSlideSorter_imgComplete;

// Initialization

  this.form = form;
  this.numRows = numRows;
  this.numCols = numCols;
  this.chosenMenuName = chosenMenuName;
  this.Widgets = new Object();
}

////////////////////////////////////////////////////////////
//                                                        //
// Define methods of the ImageSlideSorter object          //
//                                                        //
////////////////////////////////////////////////////////////

/**
 * Creates the SlideSorter table with the desired number of
 * rows and columns.  The number of widgets to create is
 * determined from the length of the ISSInitObject.
 * @param {string} element_id 'id' attribute of the element 
 *        into which the ImageSlideSorter is inserted.
 * @param {int} rows number of rows in the SlideSorter
 * @param {int} cols number of cols in the SlideSorter
 */
function ImageSlideSorter_render(element_id,rows,cols) {

  this.element_id = element_id;
  this.numRows = rows;
  this.numCols = cols;

  var node = document.getElementById(this.element_id);
  var children = node.childNodes;
  var num_children = children.length;

  // Remove any children of element_id
  // NOTE:  Start removing children from the end.  Otherwise, what was
  // NOTE:  children[1]  becomes children[0] when children[0] is removed.
  for (var i=num_children-1; i>=0; i--) {
    var child = children[i];
    if (child) {
      node.removeChild(child);
    }
  }

  // Create the ImageSlideSorter table

  var id_text;

  // Overall table
  var ISS_table = document.createElement('table');
  node.appendChild(ISS_table);

  var ISS_tbody = document.createElement('tbody');
  ISS_table.appendChild(ISS_tbody);

  // Control row, cell, table and tbody
  var ISS_controlRow = document.createElement('tr');
  id_text = 'ISS_controlRow';
  ISS_controlRow.setAttribute('id',id_text);
  ISS_tbody.appendChild(ISS_controlRow);

  var ISS_controlCell = document.createElement('td');
  id_text = 'ISS_controlCell';
  ISS_controlCell.setAttribute('id',id_text);
  ISS_controlRow.appendChild(ISS_controlCell);

  var ISS_controlTable = document.createElement('table');
  id_text = 'ISS_controlTable';
  ISS_controlTable.setAttribute('id',id_text);
  ISS_controlCell.appendChild(ISS_controlTable);

  var ISS_controlBody = document.createElement('tbody');
  ISS_controlTable.appendChild(ISS_controlBody);

  // One row for the basic menus and one for the differencing menus
  var ISS_basicMenusRow = document.createElement('tr');
  id_text = 'ISS_basicMenusRow';
  ISS_basicMenusRow.setAttribute('id',id_text);
  ISS_controlBody.appendChild(ISS_basicMenusRow);

  var ISS_diffMenusRow = document.createElement('tr');
  id_text = 'ISS_diffMenusRow';
  ISS_diffMenusRow.setAttribute('id',id_text);
  ISS_controlBody.appendChild(ISS_diffMenusRow);

  // Now add a cell for the diffencging-mode checkbox and
  // one additional cell for each 'Menu' defined in ISSInitObject

  var ISS_basicCheckboxCell = document.createElement('td');
  id_text = 'ISS_basicCheckboxCell';
  ISS_basicCheckboxCell.setAttribute('id',id_text);
  ISS_basicMenusRow.appendChild(ISS_basicCheckboxCell);

  var ISS_diffCheckboxCell = document.createElement('td');
  id_text = 'ISS_diffCheckboxCell';
  ISS_diffCheckboxCell.setAttribute('id',id_text);
  ISS_diffMenusRow.appendChild(ISS_diffCheckboxCell);

  var i=1;
  for (var menuName in ISSInitObject) {
// NOTE:  json.js breaks for loops by adding the toJSONString() method.
// NOTE:  See:  http://yuiblog.com/blog/2006/09/26/for-in-intrigue/
    if (typeof ISSInitObject[menuName] !== 'function') { 

/*
    <td id="basicCell'>
      <table id="basicTable#">
        <tbody id="basicbody#">
          <tr id="basicRow#">
            <td id="basicRadioCell#"> </td>
            <td id="basicTitleCell#"> </td>
            <td id="basicWidgetCell#"> </td>
          </tr>
        </tbody>
      </table>
    </td>
*/
      var basicCell = document.createElement('td');
      id_text = 'basicCell' + i;
      basicCell.setAttribute('id',id_text);
      ISS_basicMenusRow.appendChild(basicCell);

      var basicTable = document.createElement('table');
      id_text = 'basicTable' + i;
      basicTable.setAttribute('id',id_text);
      basicCell.appendChild(basicTable);

      var basicBody = document.createElement('tbody');
      basicTable.appendChild(basicBody);

      var basicRow = document.createElement('tr');
      id_text = 'basicRow' + i;
      basicRow.setAttribute('id',id_text);
      basicBody.appendChild(basicRow);

      var basicRadioCell = document.createElement('td');
      id_text = 'basicRadioCell' + i;
      basicRadioCell.setAttribute('id',id_text);
      basicRow.appendChild(basicRadioCell);

      var basicRadioButton = document.createElement('input');
      id_text = 'basicRadioButton' + i;
      basicRadioButton.setAttribute('id',id_text);
      basicRadioButton.setAttribute('type','radio');
      basicRadioButton.setAttribute('class','basicRadios');
      basicRadioButton.setAttribute('name','basicRadios');
      basicRadioCell.appendChild(basicRadioButton);

      var basicTitleCell = document.createElement('td');
      id_text = 'basicTitleCell' + i;
      basicTitleCell.setAttribute('id',id_text);
      basicRow.appendChild(basicTitleCell);

      var basicWidgetCell = document.createElement('td');
      id_text = 'basicWidgetCell' + i;
      basicWidgetCell.setAttribute('id',id_text);
      basicRow.appendChild(basicWidgetCell);

/*
    <td id="diffCell'>
      <table id="diffTable#">
        <tbody id="diffbody#">
          <tr id="diffRow#">
            <td id="diffRadioCell#"> </td>
            <td id="diffTitleCell#"> </td>
            <td id="diffWidgetCell#"> </td>
          </tr>
        </tbody>
      </table>
    </td>
*/
      var diffCell = document.createElement('td');
      id_text = 'diffCell' + i;
      diffCell.setAttribute('id',id_text);
      ISS_diffMenusRow.appendChild(diffCell);

      var diffTable = document.createElement('table');
      id_text = 'diffTable' + i;
      diffTable.setAttribute('id',id_text);
      diffCell.appendChild(diffTable);

      var diffBody = document.createElement('tbody');
      diffTable.appendChild(diffBody);

      var diffRow = document.createElement('tr');
      id_text = 'diffRow' + i;
      diffRow.setAttribute('id',id_text);
      diffBody.appendChild(diffRow);

      var diffRadioCell = document.createElement('td');
      id_text = 'diffRadioCell' + i;
      diffRadioCell.setAttribute('id',id_text);
      diffRow.appendChild(diffRadioCell);

      var diffTitleCell = document.createElement('td');
      id_text = 'diffTitleCell' + i;
      diffTitleCell.setAttribute('id',id_text);
      diffRow.appendChild(diffTitleCell);

      var diffWidgetCell = document.createElement('td');
      id_text = 'diffWidgetCell' + i;
      diffWidgetCell.setAttribute('id',id_text);
      diffRow.appendChild(diffWidgetCell);

      diffRow.style.display = 'none';

      i++;
    }
  }


  // Images row, cell, table and tbody
  var ISS_imagesRow = document.createElement('tr');
  id_text = 'ISS_imagesRow';
  ISS_imagesRow.setAttribute('id',id_text);
  ISS_tbody.appendChild(ISS_imagesRow);

  var ISS_imagesCell = document.createElement('td');
  ISS_imagesRow.appendChild(ISS_imagesCell);

  var ISS_imagesTable = document.createElement('table');
  id_text = 'ISS_imagesTable';
  ISS_imagesTable.setAttribute('id',id_text);
  ISS_imagesCell.appendChild(ISS_imagesTable);

  var ISS_imagesBody = document.createElement('tbody');
  ISS_imagesTable.appendChild(ISS_imagesBody);

// Now create rows and columns of imageCells

/*
    		<tr class="iss_imageRow">
          <td class="iss_imageCell">
            <table>
              <tbody>
                <tr> <td id="iss_ContentCell11" colspan="3"> <a id="iss_A11"><img id="iss_Img11"></img></a> </td> </tr>
                <tr>
                  <td id="iss_Widget11"></td>
                  <td><img id="iss_AnimatedGif11" src="mozilla_blu.gif"></img></td>
                  <td><a id="iss_Refresh11" href="javascript: ISS.loadContentCell(1,1)">Refresh</a></td>
                </tr>
              </tbody>
            </table>
          </td>
          ...
*/
  for (var i=1; i<=this.numRows; i++) {

    var imageRow = document.createElement('tr');
    imageRow.setAttribute('class','ISS_imageRow');
    ISS_imagesBody.appendChild(imageRow);

    for (var j=1; j<=this.numCols; j++) {

      var imageCell = document.createElement('td');
      imageCell.setAttribute('class','ISS_imageCell');
      imageRow.appendChild(imageCell);

      var imageCellTable = document.createElement('table');
      imageCellTable.setAttribute('class','ISS_imageCellTable');
      imageCell.appendChild(imageCellTable);

      var imageCellBody = document.createElement('tbody');
      imageCellTable.appendChild(imageCellBody);

      var imageContentRow = document.createElement('tr');
      imageCellBody.appendChild(imageContentRow);

      // Inserting another table here instead of just using 'colspan'
      // because support for colspan is broken in IE7
      var cell = document.createElement('td');
      imageContentRow.appendChild(cell);
      var table = document.createElement('table');
      cell.appendChild(table);
      var tbody = document.createElement('tbody');
      table.appendChild(tbody);
      var tr = document.createElement('tr');
      tbody.appendChild(tr);
      // End of IE7 hack

      var contentCell = document.createElement('td');
      id_text = 'ISS_ContentCell' + i + j;
      contentCell.setAttribute('id',id_text);
      //contentCell.setAttribute('colspan',3);
      //imageContentRow.appendChild(contentCell);
      tr.appendChild(contentCell);

      var imageWidgetRow = document.createElement('tr');
      imageCellBody.appendChild(imageWidgetRow);

      // Inserting another table here instead of just using 'colspan'
      // because support for colspan is broken in IE7
      var cell = document.createElement('td');
      imageWidgetRow.appendChild(cell);
      var table = document.createElement('table');
      cell.appendChild(table);
      var tbody = document.createElement('tbody');
      table.appendChild(tbody);
      var tr = document.createElement('tr');
      tbody.appendChild(tr);
      // End of IE7 hack

      var widgetCell = document.createElement('td');
      id_text = 'widgetCell' + i + j;
      widgetCell.setAttribute('id',id_text);
      widgetCell.setAttribute('class','widgetCell');
      //imageWidgetRow.appendChild(widgetCell);
      tr.appendChild(widgetCell);

      var aGifCell = document.createElement('td');
      aGifCell.setAttribute('class','aGifCell');
      //imageWidgetRow.appendChild(aGifCell);
      tr.appendChild(aGifCell);

      var aGif = document.createElement('gif');
      id_text = 'aGif' + i + j;
      aGif.setAttribute('id',id_text);
      aGif.setAttribute('src','mozilla_blu.gif');
      aGifCell.appendChild(aGif);

      var refreshCell = document.createElement('td');
      refreshCell.setAttribute('class','refreshCell');
      //imageWidgetRow.appendChild(refreshCell);
      tr.appendChild(refreshCell);

      var refresh = document.createElement('a');
      id_text = 'refresh' + i + j;
      refresh.setAttribute('id',id_text);
      var href = 'javascript: ISS.loadContentCell(' + i + ',' + j + ')';
      refresh.setAttribute('href',href);
      refreshCell.appendChild(refresh);

      var textNode = document.createTextNode('Refresh');
      refresh.appendChild(textNode);

    }
  }

  // Now that all the framework elements are in place, populate
  // them with text, widgets, etc.

  this.createGlobalRadios();
  this.createGlobalWidgets();
  this.createCellWidgets(this.chosenMenuName);
  this.loadAllImages();

}


/**
 * Associates values and onclick() event handlers with the 'iss_G#Radio' 
 * radio buttons defined in the HTML page.
 */
function ImageSlideSorter_createGlobalRadios() {
// NOTE:  Radio buttons are handled directly by the ImageSlideSorter.
// NOTE:  There is no 'widget interface' abstraction layer.
  var i = 1;
/*
  var first = 1;
  var firstRadio;
*/
  var num_radios = 0;

  for (var menuName in ISSInitObject) {
// NOTE:  json.js breaks for loops by adding the toJSONString() method.
// NOTE:  See:  http://yuiblog.com/blog/2006/09/26/for-in-intrigue/
    if (typeof ISSInitObject[menuName] !== 'function') { 
      var radioButton_id = 'basicRadioButton' + i;
      var titleCell_id = 'basicTitleCell' + i;
      var Menu = ISSInitObject[menuName];
      var RadioButton = document.getElementById(radioButton_id);
      var TitleCell = document.getElementById(titleCell_id);

// TODO:  Is this a robust way of changing the title?
      if (TitleCell.firstchild) {
        TitleCell.firstChild.nodeValue = Menu.title;
      } else {
        var textNode = document.createTextNode(Menu.title);
        TitleCell.appendChild(textNode);
      }
      RadioButton.value = menuName;
      RadioButton.onclick = document.radioChoice;
      RadioButton.ISS = this;
      //if (first) {
      if (menuName == this.chosenMenuName) {
        RadioButton.checked = true;
        firstRadio = RadioButton;
        first = 0;
        TitleCell.style.fontWeight = "bold";
        TitleCell.style.color = "#D33";
      }
      i++;
      num_radios++;
    }
  }
  // TODO:  Make this more robust by looking for a tag name
  // NOTE:  If there is only one radio button, it will be disabled.
  // NOTE:  Better off not showing it at all.

  if (num_radios == 1) {
    firstRadio.parentNode.style.display = 'none';
    //firstRadio.parentNode.parentNode.style.display = 'none';
  }

}


/**
 * Populates each of the global Widget objects with options
 * from the 'Menu's defined in the HTML page.
 */
function ImageSlideSorter_createGlobalWidgets() {
  var i = 1;
  for (var menuName in ISSInitObject) {
// NOTE:  json.js breaks for loops by adding the toJSONString() method.
// NOTE:  See:  http://yuiblog.com/blog/2006/09/26/for-in-intrigue/
    if (typeof ISSInitObject[menuName] !== 'function') { 
      var basicWidgetCell_id = "basicWidgetCell" + i;
      var Widget;
      var Menu = ISSInitObject[menuName];

      switch (Menu.type) {
        case 'latitudeWidget':
          if (Menu.data.delta) {
            Widget = new LatitudeWidget(Menu.data.lo, Menu.data.hi, Menu.data.delta);
          } else {
            Widget = new LatitudeWidget(Menu.data.lo, Menu.data.hi);
          }
          Widget.render(basicWidgetCell_id);
          break;
        case 'longitudeWidget':
          if (Menu.data.delta) {
            Widget = new LongitudeWidget(Menu.data.lo, Menu.data.hi, Menu.data.delta);
          } else {
            Widget = new LongitudeWidget(Menu.data.lo, Menu.data.hi);
          }
          Widget.render(basicWidgetCell_id);
          break;
        case 'menuWidget':
        case 'menuWidget':
          Widget = new MenuWidget(Menu.data);
          Widget.render(basicWidgetCell_id);
          break;
        case 'dateWidget':
          Widget = new DateWidget(Menu.data.lo, Menu.data.hi);
          if (Menu.render_format) {
            Widget.render(basicWidgetCell_id,Menu.render_format);
          } else {
            Widget.render(basicWidgetCell_id,"MY");
          }
          break;
      }

      Widget.setCallback(document.globalWidgetChoice);
      if (menuName == this.chosenMenuName) { Widget.disable() };
      this.Widgets[basicWidgetCell_id] = Widget;
      i++;
    }
  }
}


/**
 * Populates each of the image cell Widget objects with the first N
 * options defined in a single 'Menu'.
 */
function ImageSlideSorter_createCellWidgets(chosenMenuName) {
  var index = 0;
  for (var i=0; i<this.numRows; i++) {
    for (var j=0; j<this.numCols; j++) {
      var inum = i+1;
      var jnum = j+1;
      var widget_id = "widgetCell" + inum + jnum;
      var Widget;
      var Menu = ISSInitObject[chosenMenuName];
      switch (Menu.type) {
        case 'latitudeWidget':
          if (Menu.data.delta) {
            Widget = new LatitudeWidget(Menu.data.lo, Menu.data.hi, Menu.data.delta);
          } else {
            Widget = new LatitudeWidget(Menu.data.lo, Menu.data.hi);
          }
          Widget.render(widget_id);
          Widget.setValueByIndex(index);
          break;
        case 'longitudeWidget':
          if (Menu.data.delta) {
            Widget = new LongitudeWidget(Menu.data.lo, Menu.data.hi, Menu.data.delta);
          } else {
            Widget = new LongitudeWidget(Menu.data.lo, Menu.data.hi);
          }
          Widget.render(widget_id);
          Widget.setValueByIndex(index);
          break;
        case 'menuWidget':
          Widget = new MenuWidget(Menu.data);
          Widget.render(widget_id);
          Widget.setValueByIndex(index);
          break;
        case 'dateWidget':
          Widget = new DateWidget(Menu.data.lo, Menu.data.hi);
          if (Menu.render_format) {
            Widget.render(widget_id,Menu.render_format);
          } else {
            Widget.render(widget_id,"MY");
          }
          // TODO:  Add setValueByIndex() method to DateWidget
          break;
      }

      Widget.setCallback(document.cellWidgetChoice);
      this.Widgets[widget_id] = Widget;
      index++;
    }
  }
}

/**
 * Reloads all the images in the table based on the current values
 * of the Widget objects.
 */
function ImageSlideSorter_loadAllImages() {
  for (var i=1; i<=this.numRows; i++) {
    for (var j=1; j<=this.numCols; j++) {
      this.loadContentCell(i,j);
    }
  }
}

/**
 * Loads in image into a specified cell based on the current values
 * of the Widget objects.
 */
function ImageSlideSorter_loadContentCell(row,col) {

// Modify the background color until the image is loaded
// TODO:  Clean up ugly parentNode.parentNode... stuff
// TODO:  use style sheet properties instead of hardcoding the color
  var aGifID = "aGif" + row + col;
  var aGif = document.getElementById(aGifID);
  var bop = aGif.parentNode.parentNode.parentNode;
  bop.style.backgroundColor = '#ECA';
  aGif.style.visibility = 'visible';

// Create an Associative Array that will consist of name:value pairs

  var AA = new Object;

// Get the values from the enabled Global Widgets 

  var i = 1;
  for (var menuName in ISSInitObject) {
// NOTE:  json.js breaks for loops by adding the toJSONString() method.
// NOTE:  See:  http://yuiblog.com/blog/2006/09/26/for-in-intrigue/
    if (typeof ISSInitObject[menuName] !== 'function') { 
      var widget_id = "basicWidgetCell" + i;
      var Widget = this.Widgets[widget_id];
      if (!Widget.disabled) {
        AA[menuName] = Widget.getValue();
      } 
      i++;
    }
  }

// Now get the values from the Cell Widgets
// Pass the Associative Array to the user defined createLASRequest()
// Use the returned LASRequest to create a URL
// Use sarissa.js to send an AJAX request to the server

  var widget_id = "widgetCell" + row + col;
  var Widget = this.Widgets[widget_id];
  AA[this.chosenMenuName] = Widget.getValue();
  var Request = createLASRequest(AA);

// TODO:  The Request prefix needs accessor methods that allow you
// TODO:  to specify the type of Request you are sending
// TODO:    - ProductServer.do
// TODO:    - GetDatasets.do
// TODO:    - GetVariables.do
// TODO:    - GetGrids.do
// TODO:    - etc.
// TODO:  and in what format the  response should come back
// TODO:    - xml
// TODO:    - json
// TODO:    - html
// TODO:    - etc.

  Request.setProperty('las','output_type','json');
  var prefix = Request.prefix; 
  var url = prefix + escape(Request.getXMLText()); 

  // 2) Send LAS Request URL using Sarissa methods
  //    Register handleLASResponse(...) as callback routine

  var contentCell_id = "ISS_ContentCell" + row + col; 

  var xmlhttp = new XMLHttpRequest();
  xmlhttp.open('GET', url, true);
  xmlhttp.onreadystatechange = function() {
    if (xmlhttp.readyState == 4) {
      //this.handleLASResponse(xmlhttp.responseText,contentCell_id);
      handleLASResponse(xmlhttp.responseText,row,col);
    }
  }
  xmlhttp.send(null);
}

// TODO:  Is this an event handler?
/**
 * Handles the LASResponse returned from the XMLHTTP request,
 * creates the appropriate DOM elements based on the LASResponse and
 * inserts appropriate content into those DOM elements.
 */
function OLD_ImageSlideSorter_handleLASResponse(LASResponseText,row,col) {
  // First, make sure we can parse the LASResponse
  var Response;
  try {
    Response = new LASResponse(LASResponseText);
  } catch(e) {
    // TODO:  Create Error Cell with this text?
    alert('Error parsing LASResponseText as JSON');
    return;
  }

  // We have a valid JSON object.

  var contentCell_id = "ISS_ContentCell" + row + col; 
  var CCell = new LASContentCell();
  CCell.render(contentCell_id);

  if ( Response.isError() ) {
    //CCell.display(Response,'las_message');
    CCell.display(Response,'full_error');
  } else {
    CCell.display(Response,'plot_image');
  }           
}

////////////////////////////////////////////////////////////
//                                                        //
// Define event handlers that are functions of document.  //
//                                                        //
////////////////////////////////////////////////////////////

/**
 * Change the background color back to normal after an image is loaded.
 */
function ImageSlideSorter_imgComplete(e) {
  // Cross-browser discovery of the event target
  // By Stuart Landridge in "DHTML Utopia ..."
  // NOTE:  'load' events require 'e.currentTarget' instead of 'e.target'. 
  var target;
  if (window.event && window.event.srcElement) {
    target = window.event.srcElement;
  } else if (e && e.currentTarget) {
    target = e.currentTarget;
  } else {
    alert('ImageSlideSorter ERROR:\n> selectChange:  This browser does not support standard javascript events.');
    return;
  }
  // TODO:  use style sheet properties instead of hardcoding the color

  var Img = target;
  var bop = Img.parentNode.parentNode.parentNode.parentNode.parentNode;
  bop.style.backgroundColor = '#ACE';
  Img.aGif.style.visibility = 'hidden';

}

/**
 * The ImageSlideSorter_cellWidgetChoice() function is registered as the
 * 'onchange' event handler for all automatically generated cell Widgets.
 * @param {object} Widget object that responded to the event
 * @ignore
 */
function ImageSlideSorter_cellWidgetChoice(Widget) {
  var i = Number(Widget.element_id.charAt(10));
  var j = Number(Widget.element_id.charAt(11));
  var ISS = document.ISS;
  //ISS.loadCellImage(i,j);
  ISS.loadContentCell(i,j);
}

/**
 * The ImageSlideSorter_globalWidgetChoice() function is registered as the
 * callback function called during event processing for all automatically 
 * generated global Widgets.
 * @param {object} Widget object that responded to the event
 * @ignore
 */
function ImageSlideSorter_globalWidgetChoice(Widget) {
  document.ISS.loadAllImages();
}

/**
 * The ImageSlideSorter_radioChoice() function is registered as the
 * 'onclick' event handler for all global radio buttons.
 * These are the buttons that cause a different 'Menu' to be used
 * to populate the individual cell Widgets.
 *
 * Inside of this function 'this' refers to the widget that registered
 * the event, not to the ImageSlideSorter object.  Hence the
 * need to access 'document.ISS'.
 * @param {Event} e selection event
 * @ignore
 */
function ImageSlideSorter_radioChoice(e) {

  // Cross-browser discovery of the event target
  // By Stuart Landridge in "DHTML Utopia ..."
  var target;
  if (window.event && window.event.srcElement) {
    target = window.event.srcElement;
  } else if (e && e.target) {
    target = e.target;
  } else {
    alert('ImageSlideSorter ERROR:\n> selectChange:  This browser does not support standard javascript events.');
    return;
  }

  var Radio = target;
  var menuName = Radio.value;
  var ISS = Radio.ISS;

// Create new select objects for each cell
  ISS.createCellWidgets(menuName);
  ISS.chosenMenuName = menuName;

// Dis/en-able Global selects so that only those
// 'orthogonal' to the rows and columns are available.

  var i = 0;
  for (var menuName in ISSInitObject) {
// NOTE:  json.js breaks for loops by adding the toJSONString() method.
// NOTE:  See:  http://yuiblog.com/blog/2006/09/26/for-in-intrigue/
    if (typeof ISSInitObject[menuName] !== 'function') { 
      var num = i+1;
      var widget_id = "basicWidgetCell" + num;
      var title_id = "basicTitleCell" + num;
      var radio_id = "basicRadioButton" + num;
      var Widget = ISS.Widgets[widget_id];
      var Title = document.getElementById(title_id);
      var Radio = document.getElementById(radio_id);
      if (Radio.checked) {
        Widget.disable();
        Title.style.fontWeight = "bold";
        Title.style.color = "#D33";
      } else {
        Widget.enable();
        Title.style.fontWeight = "normal";
        Title.style.color = "#000";
      }
      i++;
    }
  }

  ISS.loadAllImages();
}
