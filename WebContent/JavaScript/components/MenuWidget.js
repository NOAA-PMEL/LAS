/**
 * @private
 * @fileoverview 
 * The MenuWidget object manages the initialization, and rendering of
 * Selectors so that new MenuWidgets may be created at will when required
 * by a user ineterface.
 * <p>
 * The MenuWidget object is initialized with an array of {text,vaue} pairs.
 * <pre>
 *   Menu = [
 *     [text0,value0],
 *     [text1,value1],
 *     [text2,value2]
 *   ]
 * </pre>
 * <p>
 * TODO:  Mention 'unobtrusive' javascript.
 *
 * @author Jonathan Callahan
 * @version 1.0
 */

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// Define the MenuWidget object                                               //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////

/**
 * Constructs a new MenuWidget object.
 * @constructor
 * <p>
 * The 'lo' and 'hi' parameters must be of the form "YYYY-MM-DD [HH:mm:SS]'
 * or the string 'TODAY' or 'NOW' (see the parseDate() method).
 * <p>
 * The 'deltaMinutes' and 'offsetMinutes' parameters can be used to 
 * support, for example, 6 hourly forecats that appear 15 minutes after 
 * the hour.
 * <p>
 * @param {object} Menu menu object with {Array text, Array value}
 * @param {int} selectedIndex currently selected option (or first for multi-select)
 * @param {string} type javascript Select type ['select-one' | 'select-multiple']
 * @param {string} callback javascript function to be attached to the MenuWidget onChange event
 * @return new MenuWidget object
 */
function MenuWidget(Menu, selectedIndex, type, callback) {

  // Public methods

  this.render = MenuWidget_render;
  this.disable = MenuWidget_disable;
  this.enable = MenuWidget_enable;
  this.show = MenuWidget_show;
  this.hide = MenuWidget_hide;
  this.getValue = MenuWidget_getValue;
  this.getValues = MenuWidget_getValues;
  this.setCallback = MenuWidget_setCallback;
  this.setValue = MenuWidget_setValue;
  this.setValueByIndex = MenuWidget_setValueByIndex;

  // Event handlers      

  this.selectChange = MenuWidget_selectChange;

  // Initialization

  this.Menu = Menu;
  this.selectedIndex = selectedIndex ? selectedIndex : 0;
  this.type = type ? type : 'select-one';
  this.widgetType = 'MenuWidget';
  this.disabled = 0;
  this.visible = 0;
  this.callback = callback;
}

/**
 * Creates the javascript Select object associated with the MenuWidget.
 * <p>
 * Any children of element_id will be removed and replaced with a Select object
 * <p>
 * @param {string} element_id 'id' attribute of the element into which the Menu is inserted.
 * @param {string} type javascript Select type ['select-one' | 'select-multiple']
 */
function MenuWidget_render(element_id,type) {

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
  

  with (Select) {
    //TODO:  deal with setting the 'type'
    // NOTE:  IE doesn't support the setAttribute() method for the 'type' attribute
    // NOTE:  We just remove this capability for now (defaults to 'select-one'
    //Select.setAttribute('type', this.type);
    Select.setAttribute('name', id_text);
    for (var i=0; i<this.Menu.length; i++) {
      options[i]=new Option(this.Menu[i][0], this.Menu[i][1]);
      if (i == this.selectedIndex) {
        options[i].selected = true;
      }
    }
  }
  Select.widget = this;
  Select.onchange = this.selectChange;
  node.appendChild(Select);

  this.visible = 1;
  // Store the pointer to the Select object inside the MenuWidget object

  this.Select = Select;
}

/**
 * Returns the selected value associated with a MenuWidget of type 'select-one'.
 * @return {string} 
 */
function MenuWidget_getValue() {
  return this.Select.options[this.Select.selectedIndex].value;
}

/**
 * Returns the selected values associated with a MenuWidget of type 'select-multiple'.
 * @return {string} 
 */
function MenuWidget_getValues() {
// TODO:  Implement getValues() method for 'select-multiple'
  return this.getValue();
}

/**
 * Disables the Menu widget.
 */
function MenuWidget_disable() {
  this.disabled = 1;
  this.Select.disabled = 1;
}

/**
 * Enables the Menu widget.
 */
function MenuWidget_enable() {
  this.disabled = 0;
  this.Select.disabled = 0;
}

/**
 * Set's the Widget container's visibility to 'visible'
 */
function MenuWidget_show() {
  var node = document.getElementById(this.element_id);
  node.style.visibility = 'visible';
  this.visible = 1;
}

/**
 * Set's the Widget container's visibility to 'hidden'
 */
function MenuWidget_hide() {
  var node = document.getElementById(this.element_id);
  node.style.visibility = 'hidden';
  this.visible = 0;
}

/**
 * Sets the callback function to be attached to onChange events.
 * @param {string} function name
 */
function MenuWidget_setCallback(callback) {
  this.callback = callback
}

/**
 * Sets the selected Option to match the incoming value
 * @param {string} value Option value to match
 * @throw TODO:  throw exception if value is not found
 */
function MenuWidget_setValue(value) {
  var no_match = 1;
  for (var i=0; i<this.Select.length; i++) {
    if ( this.Select.options[i].value == value ) {
      this.Select.selectedIndex = i;
      this.selectedIndex = i;
      found_match = 0;
    }
  }

  if (no_match) { 
    throw('ERROR:  MenuWidget_setValue: value [' + value + '] does not match any options.');
  }
}

/**
 * Sets the selected Option.
 * @param {int} index index into the Options array [0 <= index <= N]
 * @throws {string} throws exception if index is outside of Options array
 */
function MenuWidget_setValueByIndex(index) {
  if (index < 0 || index >= this.Select.length) {
    throw('ERROR:  MenuWidget_setValueByIndex: index [' + index + '] does not match any options.');
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
function MenuWidget_selectChange(e) {

  // Cross-browser discovery of the event target
  // By Stuart Landridge in "DHTML Utopia ..."
  var target;
  if (window.event && window.event.srcElement) {
    target = window.event.srcElement;
  } else if (e && e.target) {
    target = e.target;
  } else {
    alert('MenuWidget ERROR:\n> selectChange:  This browser does not support standard javascript events.');
    return;
  }

  if (target.nodeName.toLowerCase() != 'select') {
    alert('MenuWidget ERROR:\n> selectChange:  event target [' + target.nodeName.toLowerCase() + '] should instead be [select]');
    return;
  }
// NOTE:  I believe that this function is evaluated outside the context of the MenuWidget.
// NOTE:  Hence the need to extract the Widget from the Select object.
  var MW = target.widget;

  if (MW.callback) {
    MW.callback(MW);
  }
}
