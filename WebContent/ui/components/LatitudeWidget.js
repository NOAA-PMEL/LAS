/**
 * @private
 * @fileoverview 
 * The LatitudeWidget object manages the initialization and rendering of a
 * latitude Selector.
 * <p>
 * The LatitudeWidget object is initialized with a valid range (LatLo <--> LatHi)
 * and allows the user to select a latitude within this range.
 * (LatLo <= Lat <= LatHi).
 * <p>
 * TODO:  The LatitudeWidget should create either a single Selector or a pair
 * TODO:  of Selectors depending on whether a point or range is required.
 * TODO:  This would match the functionality provided by the DateWidget.
 * TODO:  In order to release a beta version of the new LAS by July 9, 2007
 * TODO:  (5 weeks away), this version of LatitudeWidget will only support
 * TODO:  a single Selector to satisfy the needs of the ImageSlideSorter.
 * TODO:
 * TODO:  Another way to handle this, of course, would be to use a mult-select
 * TODO:  widget.  Hmmm ... I'll have to think about which is better.
 *
 * @author Jonathan Callahan
 * @version 1.0
 */

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// Define the LatitudeWidget object                                           //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////

/**
 * Constructs a new LatitudeWidget object.
 * @constructor
 * <p>
 * @param {float} lo end of the domain of valid latitudes in decimal degrees
 * @param {float} hi end of the domain of valid latitudes in decimal degrees
 * @param {float} delta interval between latitudes in decimal degrees
 * @return new LatitudeWidget object
 */
function LatitudeWidget(lo, hi, delta) {

  // Public methods

  this.render = LatitudeWidget_render;
  this.disable = LatitudeWidget_disable;
  this.enable = LatitudeWidget_enable;
  this.show = LatitudeWidget_show;
  this.hide = LatitudeWidget_hide;
  this.getValue = LatitudeWidget_getValue;
  this.getValues = LatitudeWidget_getValues;

  this.setCallback = LatitudeWidget_setCallback;
  this.setValue = LatitudeWidget_setValue;
  this.setValueByIndex = LatitudeWidget_setValueByIndex;

  // Event handlers      

  this.selectChange = LatitudeWidget_selectChange;

  // Initialization

  this.lo = Number(lo);
  this.hi = Number(hi);
  this.delta = Number(delta);
  this.type = 'select-one';
  this.widgetType = 'LatitudeWidget';
  this.disabled = 0;
  //this.callback = callback;
}

/**
 * Creates the javascript Select object associated with the LatitudeWidget.
 * <p>
 * Any children of element_id will be removed and replaced with a Select object
 * <p>
 * @param {string} element_id 'id' attribute of the element into which the Select is inserted.
 * @param {string} type javascript Select type ['select-one' | 'select-multiple']
 * TODO:  For now only supports 'select-one'.
 */
function LatitudeWidget_render(element_id,type) {

  this.element_id = element_id;
  if (type) { this.type = type; }

  var node = document.getElementById(this.element_id);
  var children = node.childNodes;
  var num_children = children.length;

  // Remove any children of this widget
  // NOTE:  Start removing children from the end.  Otherwise, what was
  // NOTE:  children[1]  becomes children[0] when children[0] is removed.
  for (var i=num_children-1; i>=0; i--) {
    var child = children[i];
    if (child) {
      node.removeChild(child);
    }
  }

  // Create and populate the Select object

  var id_text = this.element_id + '_Select';
  var Select = document.createElement('select');
  Select.setAttribute('id',id_text);

  //TODO:  deal with setting the 'type'
  // NOTE:  IE doesn't support the setAttribute() method for the 'type' attribute
  // NOTE:  We just remove this capability for now (defaults to 'select-one'
  //Select.setAttribute('type', this.type);
  Select.setAttribute('name', id_text);

  var length = (this.hi - this.lo) / this.delta;
  var value = this.lo - this.delta;
  var text = "";
  // NOTE:  By default the appearance of options in a menu is from 
  // NOTE:  top (first) to bottom (last).  For the LatitudeWidget
  // NOTE:  we add options in reverse order so that low values will
  // NOTE:  appear at the bottom (South). 
  for (var i=length-1; i>=0; i--) {
    value += this.delta;
    if (value < 0) {
      text = Math.abs(value).toString() + ' S';
    } else {
      text = Math.abs(value).toString() + ' N';
    }
    Select.options[i]=new Option(text, value);
  }

  Select.selectedIndex = 0;
  Select.options[0].selected = true;
  Select.widget = this;
  Select.onchange = this.selectChange;
  node.appendChild(Select);

  // Store the pointer to the Select object inside the LatitudeWidget object

  this.Select = Select;
}

/**
 * Returns the selected value associated with a LatitudeWidget of type 'select-one'.
 * @return {string} 
 */
function LatitudeWidget_getValue() {
  return this.Select.options[this.Select.selectedIndex].value;
}

/**
 * Returns the selected values associated with a LatitudeWidget of type 'select-multiple'.
 * @return {string} 
 */
function LatitudeWidget_getValues() {
// TODO:  Implement getValues() method for 'select-multiple'
  return this.getValue();
}

/**
 * Disables the Menu widget.
 */
function LatitudeWidget_disable() {
  this.disabled = 1;
  this.Select.disabled = 1;
}

/**
 * Enables the Menu widget.
 */
function LatitudeWidget_enable() {
  this.disabled = 0;
  this.Select.disabled = 0;
}

/**
 * Set's the Widget container's visibility to 'visible'
 */
function LatitudeWidget_show() {
  var node = document.getElementById(this.element_id);
  node.style.visibility = 'visible';
  this.visible = 1;
}

/**
 * Set's the Widget container's visibility to 'hidden'
 */
function LatitudeWidget_hide() {
  var node = document.getElementById(this.element_id);
  node.style.visibility = 'hidden';
  this.visible = 0;
}

/**
 * Sets the callback function to be attached to onChange events.
 * @param {string} function name
 */
function LatitudeWidget_setCallback(callback) {
  this.callback = callback
}

/**
 * Sets the selected option of the Select menu to the value.
 * closest to the incoming value.
 * @param {float} lat numeric value to be selected
 */
function LatitudeWidget_setValue(lat) {
//TODO:  Throw error if lat < -90 || lat > 90
  var internal_lat = Number(lat);
  var i = 0;
// NOTE:  Latitude widget values are ordered from high -> low
  while (i <= this.Select.length) {
    var option_val = Number(this.Select.options[i].value);
    if (internal_lat >= option_val) {
      this.Select.selectedIndex = i;
      this.Select.options[i].selected = true;
      return;
    // TODO:  include logic to find closest match if lat is in between option_vals
    }
    i++;
  }
}

/**
 * Sets the selected Option.
 * @param {int} index index into the Options array [0 <= index <= N]
 * @throws {string} throws exception if index is outside of Options array
 */
function LatitudeWidget_setValueByIndex(index) {
  if (index < 0 || index >= this.Select.length) {
    throw('ERROR:  LatitudeWidget_setValueByIndex: index [' + index + '] does not match any options.');
  } else {
    this.Select.selectedIndex = index;
    this.selectedIndex = index;
  }
}

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// Event handlers.                                                            //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////

/**
 * Process the event and call the user specified callback.
 * @param e event
 */
function LatitudeWidget_selectChange(e) {

  // Cross-browser discovery of the event target
  // By Stuart Landridge in "DHTML Utopia ..."
  var target;
  if (window.event && window.event.srcElement) {
    target = window.event.srcElement;
  } else if (e && e.target) {
    target = e.target;
  } else {
    alert('LatitudeWidget ERROR:\n> selectChange:  This browser does not support standard javascript events.');
    return;
  }

  if (target.nodeName.toLowerCase() != 'select') {
    alert('LatitudeWidget ERROR:\n> selectChange:  event target [' + target.nodeName.toLowerCase() + '] should instead be [select]');
    return;
  }
// NOTE:  I believe that this function is evaluated outside the context of the LatitudeWidget.
// NOTE:  Hence the need to extract the Widget from the Select object.
  var MW = target.widget;

  if (MW.callback) {
    MW.callback(MW);
  }
}
