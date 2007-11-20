/**
 * LASUI.js
 * Main class for LAS UI widget management and communication with the LAS json services.
 * author: Jeremy Malczyk -- 2007
 */
	
/** this class requires dojo.js for debugger window and AJAX communications */
	dojo.require("dojo.debug.console");	
	dojo.require("dojo.lang.*");
	dojo.require("dojo.io.*");
	dojo.require("dojo.event.*");

/** bindAsEventListener can also be provided via prototype.js*/	
if ( ! Function.prototype.bindAsEventListener ) {
    Function.prototype.bindAsEventListener = function(object) {
        var __method = this;
        return function(event) {
            __method.call(object, event || window.event);
        };
    };
}

/**
 * The LASUI class
 */
function LASUI () {
	// references for everything the UI builds
	this.refs = {};
		
	// server side resource urls
	this.hrefs = {
		"getProduct"  : {"url" : "ProductServer.do"},
		"getCategories" : {"url" : "getCategories.do"},
		"getGrid" : {"url" : "getGrid.do"},
		"getViews" : {"url" : "getViews.do"},
		"getOperations" : {"url" : "getOperations.do"},
		"getOptions" : {"url" : "getOptions.do"}
	};
	//application state
	this.state = {
		"dataset" : null, 
		"variables" : {},
		"operation" : null,
		"externaloperation" :null,
		"properties" : [],
		"externalproperties" : [],
		"view" : null,
		"embed" : true
	};
					
	//DOM anchor ids.
	this.anchors = {
		"tree" : "tree",
		"map" : "MapWidget",
		"date" : "date",
		"depth" : "depth",
		"header" : "header",
		"inputs" : {
			"maxX" : "input_maxX",
			"maxY" : "input_maxY",
			"minX" : "input_minX",
			"minY" : "input_minY"
		 }
	} 
							
	this.request = new LASRequest();
	
			
	this.autoupdate=true;	

}
/**
 *
 *
 */
LASUI.prototype.updateHash = function () {
	
}

/**
 * Method to turn on floating debugger window.
 */
LASUI.prototype.showDebugWindow = function () {
		dojo.require("dojo.widget.ContentPane");
		dojo.require("dojo.widget.FloatingPane");
		dojo.require("dojo.widget.ResizeHandle");

			var properties = {
				hasShadow: true,
				displayMinimizeAction: false,
				displayMaximizeAction: true,
				displayCloseAction: true,
				executeScripts: true,
				constrainToContainer: false,
				style: "",
				title: "LAS UI Debugger",
				titleHeight: "20",
				id: "dojoWindowId"
			};
			node = document.createElement("div");
			node.style.width = "540px";	
			node.style.height = "300px";
			node.style.left = "100px";
			node.style.top = "100px";
			node.style.zIndex = 10;
	
			var req_text = document.createElement("TEXTAREA");
			req_text.cols = 80;
			req_text.rows = 10;
			req_text.value= "alert('Click the button below to evaluate script in this box.');";
			var req_submit = document.createElement("INPUT");
			req_submit.type = "submit";
			req_submit.className = "LASRadioInputNode";
			req_submit.value = "Submit debug request";
			req_submit.onclick  = function (evt) {
				var args = $A(arguments);
				var req_text = args[1];
				eval(req_text.value);
			}.bindAsEventListener(this,req_text);
			node.appendChild(document.createElement("BR"));			
			node.appendChild(req_text);
			node.appendChild(document.createElement("BR"));			
			node.appendChild(req_submit);
			document.body.appendChild(node);
			dojoWindow = dojo.widget.createWidget("FloatingPane",properties,node);		
}
/**
 * Method to initialize the UI and begin AJAX interactions.
 * @param {string} anchorId the id of the page element to build the tree inside.
 */
LASUI.prototype.initUI = function (anchorId)
{

	if(this.params) {
		this.state.dataset = this.params.dsid;
		this.state.categories = this.params.categories
	        this.state.variables[this.params.dsid] = this.params.varid;
		this.state.operation = this.params.plot;
		this.state.view = this.params.view
		this.submitOnLoad = true;
	}
	else
		this.submitOnLoad = false;


	this.refs.externaloperations = {};
	this.refs.operations = {};
	this.refs.operations.ULNode = document.getElementById("plotType");
	this.refs.options = {};
	this.refs.options.ULNode = document.getElementById("plotOptions");

	if(document.getElementById("categories")) {
		this.refs.categories = {};
		this.refs.categories.LINode = document.getElementById("categories");
		this.refs.categories.LINode.className = "LASTreeLINode"			
		this.refs.categories.title = document.createElement("SPAN");
		this.refs.categories.title.innerHTML = "Select a dataset category.";
		this.refs.categories.title.className = "LASTreeTitleNode"; 		
		this.refs.categories.ULNode= document.createElement("UL");
		this.refs.categories.ULNode.className = "LASTreeULNode";		
		this.refs.categories.LINode.appendChild(this.refs.categories.title);
		this.refs.categories.LINode.appendChild(this.refs.categories.ULNode);
		this.refs.categories.isExpanded = true;

		var req = new XMLHttpRequest(this);
		req.onreadystatechange = this.AJAXhandler.bindAsEventListener(this, req, "this.setCategoryTreeNode(req.responseText,this.refs.categories,'categories');");
		req.open("GET", this.hrefs.getCategories.url);
		req.send();
	}
	//grab references to the map constraint inputs
	this.refs.inputs = {};
	for(var i in this.anchors.inputs)
		this.refs.inputs[i] = document.getElementById(this.anchors.inputs[i]);
		
	this.refs.inputs.maxX.onchange = this.setMaxX.bindAsEventListener(this);
	this.refs.inputs.maxY.onchange = this.setMaxY.bindAsEventListener(this);
	this.refs.inputs.minX.onchange = this.setMinX.bindAsEventListener(this);
	this.refs.inputs.minY.onchange = this.setMinY.bindAsEventListener(this);		
		
	//inititialize the map widget
	this.initMap('MapWidget');
}

LASUI.prototype.AJAXhandler = function  (app) {
		var args = $A(arguments);
		var req = args[1];
		var callback = args[2];

		if(req.readyState == 4 && req.status == 200) 
			eval(callback);
	}

/**
 * Method to load a UI category tree node from a json response
 * @param {string} strJson a json string compatible with LASGetCategoriesResponse
 * @param {object} node a parent node in this.refs
 * @param {string} id the id of the category or dataset to be created
 */
LASUI.prototype.setCategoryTreeNode = function (strJson, node, id) {


	var response = eval("(" + strJson + ")");	
	node.category = new LASGetCategoriesResponse(response);					
	if(node.category.getCategoryType()=="category")
		node.children=[];
	for(var i=0; i<node.category.getCategorySize();i++)
		this.setCategoryTreeSubNode(node, i,id);		
	this.expand(node);
}
/**
 * Method to show category and variable metadata.
 */
LASUI.prototype.showInfo = function (evt) {
	var args = $A(arguments);
	var node = args[1];
	var i = args[2];
	if(node.category)
		if(node.category.getChild(i))
			if(node.category.getChild(i).properties) {
			var infobox = document.createElement("DIV");
			infobox.className = "LASMetadataDIVNode"; 
			infobox.style.left = evt.clientX + "pt";
			infobox.style.top = evt.clientY + "pt";
			var cancel = document.createElement("A");
			cancel.className = "LASCancelButton";
			cancel.innerHTML = "<B>x</B>";
			cancel.onclick = function (evt) {infobox.parentNode.removeChild(infobox)}.bindAsEventListener(infobox);
			infobox.appendChild(cancel);
			for(var n in node.category.getChild(i).properties) {
				var group = node.category.getChild(i).properties[n];
				var table = document.createElement("table");
				table.border=1;
				
				var tbody = document.createElement("tbody");
				var title = document.createElement("td");
				var tr = document.createElement("tr");
				title.innerHTML = "<b>Property group " + group.type + "</b>";
				//title.colSpan =(group.property.length-1);
				tr.appendChild(title);
				tbody.appendChild(tr);
				var tr = document.createElement("tr");
				var	 td = document.createElement("td");
				td.innerHTML = "<b>Property name</b>";
				tr.appendChild(td);
				for(var i=0;i<group.property.length;i++) {
					var	 td = document.createElement("td");
					td.innerHTML = group.property[i].name;
					tr.appendChild(td);
				}
				tbody.appendChild(tr);
				var tr = document.createElement("tr");
				var	 td = document.createElement("td");
				td.innerHTML = "<b>Property value</b>";
				tr.appendChild(td);
				for(var i=0;i<group.property.length;i++) {
					var	 td = document.createElement("td");
					td.innerHTML = group.property[i].value;
					tr.appendChild(td);
				}
				tbody.appendChild(tr);
			}
			table.appendChild(tbody);
			infobox.appendChild(table);
			
			document.body.appendChild(infobox);			
		}
}
/**
 * Sub method to load a UI category or variable tree node from a json response
 * @param {string} node parent node in this.refs
 * @param {integer} i index of the childnode to be set 
 * @param {string} id a category id
 */
LASUI.prototype.setCategoryTreeSubNode = function (node, i, id) {		
	switch(node.category.getCategoryType()) {
		case "category":
			this.createCategoryTreeNode(node,i,id); 
			break;
		case "dataset":
			this.createVariableTreeNode(node,i);
			break;
	}
}
/**
 * Sub method to create category tree node and add it to the DOM
 * @param {object} node parent node in this.refs
 * @param {integer} i index of the childnode to be set 
 * @param {string} id a category id
 */
LASUI.prototype.createCategoryTreeNode = function (node, i, id) {	
	if(node==this.refs.categories && node.category.getChildChildrenType(i)=="variables")
		this.refs.categories.title.innerHTML="Datasets";
	node.children[i] = {};
	node.children[i].LINode = document.createElement("LI"); 	
	node.children[i].LINode.className = "LASTreeLINode";

	node.children[i].IMGNode =  document.createElement("IMG");
	node.children[i].IMGNode.onclick = this.selectCategory.bindAsEventListener(this, node, i);

	node.children[i].IMGNode.src = "JavaScript/ui/plus.gif";
	node.children[i].IMGNode.className = "LASCategoryIMGNode";
	node.children[i].isExpanded = false;

	node.children[i].ULNode = document.createElement("ul");
	
	var table = document.createElement("TABLE");

	table.className = "LASTreeTableNode";
	table.width="100%";

	var tbody = document.createElement("TBODY");
	tbody.style.padding=0;
	tbody.style.margin=0;
	var tr = document.createElement("TR");
	tr.style.padding=0;
	tr.style.margin=0;
	var td1 = document.createElement("TD");
	td1.style.verticalAlign="top";
	td1.width="12px";
	td1.appendChild(node.children[i].IMGNode);
	td1.className = "LASTreeTableCell";	
	var td2 = document.createElement("TD");
	td2.style.verticalAlign = "top";
	td2.onclick = this.selectCategory.bindAsEventListener(this, node, i);
	td2.innerHTML = node.category.getChildName(i);
	td2.className = "LASTreeTableCell";
	td2.style.textAlign  = "left";
	var td3 = document.createElement("TD");
	node.children[i].A = document.createElement("A");
	node.children[i].A.innerHTML = "?";
	node.children[i].A.onclick = this.showInfo.bindAsEventListener(this,node,i);
	td3.appendChild(node.children[i].A);
	td3.className = "LASTreeTableCell";
	td3.width="12pt";	
	tr.appendChild(td1);
	tr.appendChild(td2);
	tr.appendChild(td3);
	tbody.appendChild(tr);
	table.appendChild(tbody);
	node.children[i].LINode.appendChild(table);
	node.children[i].LINode.appendChild(node.children[i].ULNode);
	//do not add categories that do not have children 
	if((node.category.getChildChildrenType(i) == "variables" && node.category.getChild(i).children_dsid)||node.category.getChildChildrenType(i) != "variables")
		node.ULNode.appendChild(node.children[i].LINode);		
	if(node.category.getChildType(i)=='dataset' && node.category.getChildID(i)==this.state.dataset) 
		this.getCategory(node, i);		
	if(this.refs.categories[node.category.getChild(i).ID])
		this.selectCategory().bindAsEvetListener(this,node,i);
}
/**
 * Sub method to create variable tree node and add it to the DOM
 *  parameters: 
 *  @param {object} node parent node in this.refs
 *  @param {integer} i index of the variable node within the category/dataset 
 */
LASUI.prototype.createVariableTreeNode = function (node, i) {	
	if(!node.children)
		node.children=[];
	node.children[i] = {};
	node.children[i].LINode = document.createElement("LI");
	node.children[i].LINode.style.listStyleType = "none";
	node.children[i].LINode.style.listStyleImage = "none";
	node.children[i].LINode.className = "LASTreeLINode";
	node.marginLeft="5pt";
	if(document.all) {
		var elem_nm = "<INPUT NAME='" + node.category.getDatasetID()+"'>";
		node.children[i].INPUTNode = document.createElement(elem_nm);
	} else {
		node.children[i].INPUTNode = document.createElement("INPUT");
		node.children[i].INPUTNode.name=node.category.getDatasetID();
	}
	node.children[i].INPUTNode.type="radio";
	node.children[i].className = "LASRadioInputNode";
	node.children[i].INPUTNode.onclick=this.setVariable.bindAsEventListener(this, node, i);
	node.children[i].INPUTNode.id = node.category.getChildID(i);
		
	var table = document.createElement("TABLE");	
	table.width="100%";
	var tbody = document.createElement("TBODY");
	var tr = document.createElement("TR");
	
	var td1 = document.createElement("TD");
	td1.appendChild(node.children[i].INPUTNode);
	td1.className = "LASTreeTableCell";	
	td1.width="12pt";
	var td2 = document.createElement("TD");
	td2.innerHTML= node.category.getChildName(i);
	td2.align = "left";
	td2.className = "LASTreeTableCell";
	
   var td3 = document.createElement("TD");
	td3.align = "right";
	td3.className = "LASTreeTableCell";
   td3.width="12px";
   node.children[i].A = document.createElement("A");
	node.children[i].A.innerHTML = "?";
	node.children[i].A.onclick = this.showInfo.bindAsEventListener(this,node,i);
	td3.appendChild(node.children[i].A);
	
	tr.appendChild(td1);
	tr.appendChild(td2);
	tr.appendChild(td3);
	tbody.appendChild(tr);
	table.appendChild(tbody);	
	node.children[i].LINode.appendChild(table);
	
	if(this.state.variables && this.state.dataset)
		if(this.state.variables[this.state.dataset])
			for(var v=0;v<this.state.variables[this.state.dataset].length;v++)
				if(this.state.variables[this.state.dataset][v]==node.category.getChildID(i)||this.state.variables[this.state.dataset][v]==node.category.getChild(i)){ 
					this.setVariable({}, node, i, true);
					node.children[i].INPUTNode.checked=true;
				}
	node.ULNode.appendChild(node.children[i].LINode);
}
/** 
 * Method to query the server for a category
 * @param {object} node parent node in this.refs
 * @param {integer} i index of the child category to retrieve 
 */
LASUI.prototype.getCategory = function (parentNode, i) {
	if(!parentNode.children[i].category) {
		
		var req = new XMLHttpRequest(this);
		req.onreadystatechange = this.AJAXhandler.bindAsEventListener(this, req, "this.setCategoryTreeNode(req.responseText,args[3].children[args[4]],args[3].category.getChild(args[4]));", parentNode, i);
		req.open("GET", this.hrefs.getCategories.url + "?catid=" + parentNode.category.getChildID(i));
		req.send();
		
	/*	var _bindArgs = {	
				url: this.hrefs.getCategories.url + "?catid=" + parentNode.category.getChildID(i),
				mimetype: "text/plain",
				error: function(type,error) {alert('There was a problem communicating with the server. getCategory AJAX error.' + error.type + ' ' + error.message);},
				sync : false,
				load:	dojo.lang.hitch(this, (function(type,data,event) {this.setCategoryTreeNode(data, parentNode.children[i], parentNode.category.getChildID(i)); }))
		};
		var _request = dojo.io.bind(_bindArgs);*/
	} 
	if(parentNode.children[i].ULNode.style.display=="none") {
		for(var c=0;c< parentNode.children.length;c++)
			parentNode.children[c].ULNode.style.display="none";
		parentNode.children[i].style.display="";	//expand the category if it has been selected 
		if(parentNode.children[i].category) //if the category is a dataset set it as the selected dataset
			if(parentNode.children[i].category.getCategoryType()=="dataset"){
					this.setDataset(parentNode.category.getChildID(i));
			}
	} else
		parentNode.children[i].ULNode.style.display="none";
}
/**
 * Event handler for category selection, bind to category DOM object events. 
 * @param {object} evt The event object
 * @param {object} arguments arguments added using function.prototype.bindAsEventListener<br> 
 *					this -- the context this function is run in<br>
 *					parentNode -- parent node in this.refs<br>
 *					i -- index of the category within the parentNode
 */ 
LASUI.prototype.selectCategory = function (evt) {
	var args = $A(arguments);
	var parentNode = args[1];
	var i = args[2];		
	if(!parentNode.children[i].isExpanded) {
		if(parentNode == this.refs.categories) {
			this.state.categories = {};
			this.state.categories[parentNode.category.getChild(i).ID]={};		
		}
		for(var c=0;c< parentNode.children.length;c++)
			this.collapse(parentNode.children[c]);
		this.expand(parentNode.children[i]);	//expand the category if it has been selected 
			if(parentNode.category.getChildChildrenType(i)=="variables")
				this.setDataset(parentNode.category.getChildDatasetID(i));
	} else
		this.collapse(parentNode.children[i]);
	if(!parentNode.children[i].category) {
		parentNode.children[i].IMGNode.src = "JavaScript/components/mozilla_blu.gif";
		var _bindArgs = {	
				url: this.hrefs.getCategories.url + "?catid=" + parentNode.category.getChildID(i),
				mimetype: "text/plain",
				sync : false,
				error: function(type,error) {alert('selectCategory AJAX error.' + error.type + ' ' + error.message);},
				load:	dojo.lang.hitch(this, (function(type,data,event) {this.setCategoryTreeNode(data, parentNode.children[i], parentNode.category.getChildID(i)); }))
		};
		var _request = dojo.io.bind(_bindArgs);
	} 
}
/**
 *Event handler for variable selection, bind to variable DOM object events. 
 *@param {object} evt The event object
 *@param {object} arguments Arguments added using function.prototype.bindAseventListener<br> 
 *						this -- context setVariable is being run in<br>
 * 					dataset -- a LASGetCategoriesReponse dataset object<br> 
 * 					i -- index the variable within the category or dataset   
 */ 
LASUI.prototype.setVariable = function (evt) {
	var args = $A(arguments)
	var dataset = args[1];
	var i = args[2];

	if(evt.target) 			
		var loadVariable = evt.target.checked;
	else if (evt.srcElement)
		var loadVariable = evt.srcElement.checked;
	else if(args.length>3)
		var loadVariable = args[3];
	var datasetID = dataset.category.getDatasetID();
	var variableID = dataset.category.getChildID(i);
	var variable = dataset.category.getChild(i);
	var variableLINode = dataset.children[i].LINode;
		
	if (loadVariable) {			
		//start an array of selected variables for this dataset if we havent already
		if(typeof this.state.variables[datasetID] != 'object') 
			this.state.variables[datasetID] = [];
			
		//REMOVE TO ENABLE MULTI-VARIABLE SELECTION
		this.state.variables[datasetID] = [];
		
		//if this variable has not already been selected, add it to the list for that dataset
		this.state.variables[datasetID]=this.state.variables[datasetID].without(variable); //ID);
		this.state.variables[datasetID].push(variable); //ID);
						
		//get the grids for this dataset/variable combo
		this.state.dataset = datasetID;
		this.getGrid(datasetID,variableID);	
	}	else if (typeof this.state.variables[datasetID] == 'object')
		this.state.variables[datasetID]=this.state.variables[datasetID].without(variableID);		
}
/**
 * Method to set the active dataset and call getGrid if appropriate
 * @params {string} dataset A dataset id 
 */ 
LASUI.prototype.setDataset = function (dataset) {
	this.state.dataset = dataset;
	if(typeof this.state.variables[dataset] == 'object') {
		this.getGrid(dataset, this.state.variables[dataset].last().ID);
	}
}
/**
 * Event handler to set the view, bind to view  DOM object events. 
 * @param {object} evt The event object
 * @param {object} arguments Arguments added using function.prototype.bindAseventListener<br> 
 *						this -- the context setView is being called in<br>
 *						view -- a view id 
 */ 
LASUI.prototype.getView = function () {
	if(this.state.view && !this.updating && !this.products) {
		this.state.lastView = this.state.view;
		this.state.view= "";
		if(this.state.grid)
			for(var d=0;d<this.state.grid.response.grid.axis.length;d++) 
				switch(this.state.grid.response.grid.axis[d].type) {
					case 'x' : 
						if(this.refs.XYSelect.extents.selection.grid.x.min!=this.refs.XYSelect.extents.selection.grid.x.max)
							this.state.view += "x"; 
							break;
					case 'y' : 
					 	if(this.refs.XYSelect.extents.selection.grid.y.min!=this.refs.XYSelect.extents.selection.grid.y.max)
					 		this.state.view += "y"; 
						break;
					case 't' : 
						if(document.getElementById("DateTypeSelect").options[document.getElementById("DateTypeSelect").selectedIndex].value!="point")
							this.state.view += "t"; 
						break;
					case 'z' :
						if(document.getElementById("DepthTypeSelect").options[document.getElementById("DepthTypeSelect").selectedIndex].value!="point")
							this.state.view+="z";
						break;
				}
		if(this.refs.views){
			while(!this.refs.views.views.getViewByID(this.state.view)&&this.state.view.length>0) {
				var temp="";
				for(var i=0;i<this.state.view.length-1;i++)
					temp += this.state.view[i];
				this.state.view = temp;
			}
			this.refs.externaloperations.ULNode.innerHTML="";
			this.state.properties = {};	
			if(this.state.view.length!=0)
				this.getOperations(this.state.dataset,this.state.variables[this.state.dataset].last().ID,this.state.view);
			else {
				alert("There are no products available for the region you specified.");
				this.state.view=this.state.lastView;
				this.updateConstraints();	
			}
		}
	}
}
/**
 * Event handler to set the view, bind to view  DOM object events. 
 * @param {object} evt The event object
 * @param {object} arguments Arguments added using function.prototype.bindAseventListener<br> 
 *						this -- the context setView is being called in<br>
 *						view -- a view id 
 */ 
LASUI.prototype.setView = function (evt) {
	var args = $A(arguments)
	var view = args[1];	
	this.state.view = view;
	if(!this.products)
		this.refs.operations.ULNode.innerHTML="";
	
	//this.refs.options.ULNode.innerHTML="";
	this.state.properties = {};	
	
	this.updateConstraints();
	this.getOperations(this.state.dataset,this.state.variables[this.state.dataset].last().ID,this.state.view);
}
/**
 * Method to query the server for a category
 * @param {string} dataset The selected dataset id
 * @param {string} variable The selected variable id
 *	@param {string} view The selected view id 
 */	
LASUI.prototype.getOperations = function (dataset, variable, view) {
	
	//populate the outputs tree with getOperations	
		var _bindArgs = {	
				url: this.hrefs.getOperations.url + '?dsid=' + dataset + '&varid=' + variable + '&view=' + view,
				mimetype: "text/plain",
				error: function(type,error) {alert('There was a problem communicating with the server. getOperations AJAX error.' + error.type + ' ' + error.message);},
				load: dojo.lang.hitch(this, (function(type,data,event) {this.setOperationList(data);})) 
			};
		var _request = dojo.io.bind(_bindArgs);
		
}
/**
 *	Event handler to set the operation
 *	@param {object} evt The event object
 * @arguments Arguments added using function.prototype.bindAsEventListener<br>
 *			this -- the context the handler is executing in.<br>
 *			id -- an operation id
 */
LASUI.prototype.setOperation = function (evt) {
	var args = $A(arguments);
	var id = args[1];
	var optiondef = args[2];

	this.state.operation=id;
	this.refs.options.ULNode.innerHTML="";
	
	var view = args[2];
	var optiondef = args[3];	
	this.state.view = view;	
	this.updateConstraints();	
	
	if(optiondef) {

		var cancel = document.createElement("INPUT");		
		cancel.type = "submit";
		cancel.value="Cancel";
		cancel.className = "LASSubmitInputNode";
		cancel.onclick = function (evt) {this.refs.options.ULNode.style.display='none';}.bindAsEventListener(this);
		this.refs.options.ULNode.innerHTML = "";		
		this.refs.options.ULNode.appendChild(cancel);
		this.getOptions(optiondef, this.refs.options.ULNode);
	}
	
	this.getOperations(this.state.dataset,this.state.variables[this.state.dataset].last().ID,this.state.view);	
	
	if (this.autoupdate)
		this.makeRequest();
}	
/**
 *  Method to populate the list of avialable operations from a json response from the server
 *  @param {string} strJson A json string compatibe with the LASGetOperationsResponse class
 */	
LASUI.prototype.setOperationList = function (strJson) {
	
	var response = eval("(" + strJson + ")");
	var setDefault = true;
	this.refs.externaloperations.operations = new LASGetOperationsResponse(response);
	if(!this.refs.externaloperations.operations.DIVNode) {	
		this.refs.externaloperations.operations.DIVNode= document.createElement('DIV');
		this.refs.externaloperations.operations.DIVNode.className='LASOptionsDIVNode';
		this.refs.externaloperations.operations.DIVNode.style.display="none";
	}
	//disable all nodes first
	for(var row=0;row<document.getElementById("productButtons").childNodes.length;row++)
		for(var cell=0;cell<document.getElementById("productButtons").childNodes[row].childNodes.length;cell++)
			for(var button=0;button<document.getElementById("productButtons").childNodes[row].childNodes[cell].childNodes.length;button++)			
				if(document.getElementById("productButtons").childNodes[row].childNodes[cell].childNodes[button].tagName == "INPUT")
					document.getElementById("productButtons").childNodes[row].childNodes[cell].childNodes[button].disabled="true";

	document.body.appendChild(this.refs.externaloperations.operations.DIVNode);
	if(!this.refs.externaloperations.operations.response.operations.error) 
		for(var i=0;i<this.refs.externaloperations.operations.getOperationCount();i++) 
			this.setOperationNode(this.refs.externaloperations.operations.getOperationID(i), this.refs.externaloperations.operations.getOperationName(i));	
}

/**
 * Method to create an operation radio button and add it to the operations tree node.
 * @param {string} id The operation id
 * @param {string} name The name of the operation
 */
LASUI.prototype.setOperationNode = function (id, name) {

	var button = document.getElementById(name);
	if(button) { 	
		button.disabled = false;
		button.onclick=this.doProductIconClick.bindAsEventListener(this, id);
	}
}
LASUI.prototype.doProductIconClick = function (evt) {
	var args = $A(arguments);
	var id = args[1];
	this.state.externaloperation = id;
	this.refs.externaloperations.operations.DIVNode.innerHTML = "";
		if(this.refs.externaloperations.operations.getOperationByID(id).optiondef)
			this.getOptions(this.refs.externaloperations.operations.getOperationByID(id).optiondef.IDREF, this.refs.externaloperations.operations.DIVNode);	
		else if(this.refs.externaloperations.operations.getOperationByID(id).optionsdef)
			this.getOptions(this.refs.externaloperations.operations.getOperationByID(id).optionsdef.IDREF, this.refs.externaloperations.operations.DIVNode);	
		else {		
			this.refs.externaloperations.operations.DIVNode.style.display='none';
			this.launchExternalProduct(); 
			return;
		}	
			
		var submit = document.createElement("INPUT");
		var cancel = document.createElement("INPUT");
		submit.type = "submit";
		submit.value = "Submit";
		submit.className = "LASSubmitInputNode";
		submit.onclick = function (evt) {this.refs.externaloperations.operations.DIVNode.style.display='none';this.launchExternalProduct()}.bindAsEventListener(this);
		cancel.type = "submit";
		cancel.value="Cancel";
		cancel.className = "LASSubmitInputNode";
		cancel.onclick = function (evt) {this.refs.externaloperations.operations.DIVNode.style.display='none';}.bindAsEventListener(this);
		
		this.refs.externaloperations.operations.DIVNode.appendChild(submit);
		this.refs.externaloperations.operations.DIVNode.appendChild(cancel);
		this.refs.externaloperations.operations.DIVNode.style.left="200pt";
		this.refs.externaloperations.operations.DIVNode.style.top="100pt";
		this.refs.externaloperations.operations.DIVNode.zIndex =0;
	}
/**
 * Method to query the server for the available grids
 * @param {string} dataset A dataset id
 * @param {string} variable A variable id within the dataset 
 */
LASUI.prototype.getGrid = function (dataset, variable) {
	var _bindArgs = {	
		url: this.hrefs.getGrid.url + '?dsid=' + dataset + '&varid=' + variable,
		mimetype: "text/plain",
		error: function(type,error) {alert('There was a problem communicating with the server. getGrid AJAX error.' + error.type + ' ' + error.message);},
		load: dojo.lang.hitch(this, (function(type,data,event) {this.setGrid(data);})),
		timeout: dojo.lang.hitch(this, (function() { alert("The dataset you selected is currently unavailable.")})),
		timeoutSeconds: 3 //The number of seconds to wait until firing timeout callback in case of timeout. 
	};
	var _request = dojo.io.bind(_bindArgs);
}
/** 
 * Method to evaluate a getGrid response and call getViews
 * @param {string} strJson A json string compatible with LASGetGridResponse.js
 */	
LASUI.prototype.setGrid = function (strJson) {
	if(strJson=="") {
		alert("This dataset is currently unavailable.");
		return -1;
	}
	eval("var response = (" + strJson + ")");
	if(this.state.grid)
		this.state.lastgrid = this.state.grid;
	else
		this.state.lastgrid={};		
			
	this.state.grid = new LASGetGridResponse(response);
		
	this.getViews(this.state.dataset,this.state.variables[this.state.dataset].last().ID);
}
/**
 * Method to query the server for a list of allowed views
 * @param {string} dataset A dataset id
 * @param {string} variable A variable id 
 */
LASUI.prototype.getViews = function (dataset,variable) {	
	var _bindArgs = {	
		url: this.hrefs.getViews.url + '?dsid=' + dataset + '&varid=' +variable,
		mimetype: "text/plain",
		error: function(type,error) {alert('There was a problem communicating with the server. getViews AJAX error.' + error.type + ' ' + error.message);},
		load: dojo.lang.hitch(this, (function(type,data,event) {this.setViewList(data);})),
		timeout: dojo.lang.hitch(this, (function() { alert("The dataset you selected is currently unavailable.")})),
		timeoutSeconds: 3 //The number of seconds to wait until firing timeout callback in case of timeout. 
	};
	var _request = dojo.io.bind(_bindArgs);
}
/**
 * Update the views list with the allowed views
 * @param {string} strJson A string compatible with LASGetViewsResposne.js
 */	
LASUI.prototype.setViewList = function (strJson) {
	//clear the current view list and state
	var response = eval("(" + strJson + ")");
	if(!this.refs.views)
		this.refs.views = {};
	this.refs.views.views = new LASGetViewsResponse(response);
	

//	this.refs.views.ULNode.innerHTML="";
if(!this.products) {
	var setDefault = true;
	for(var i=0;i<this.refs.views.views.getViewCount();i++) {
		var useView = true;
		for(var v=0;v<this.refs.views.views.getViewID(i).length;v++)
			if(!this.state.grid.hasAxis(this.refs.views.views.getViewID(i).charAt(v)))
				useView = false;
		if(useView) {
	//		this.setViewNode(this.refs.views.views.getViewID(i), this.refs.views.views.getViewName(i));	
			if(this.refs.views.views.getViewID(i)==this.state.view)
				setDefault = false;
		}
	}
	if(setDefault) {
		this.state.view=this.refs.views.views.getViewID(0);
		//this.refs.views.views.getViewByID(this.state.view).INPUTNode.checked=true;
	}
	this.getOperations(this.state.dataset,this.state.variables[this.state.dataset].last().ID,this.state.view);

	if(this.state.lastgrid.response){
		if(this.state.lastgrid.response.grid.ID!=this.state.grid.response.grid.ID)
			this.updateConstraints();
	} else
		this.updateConstraints();
	} else
		this.setDefaultProductMenu();
}
LASUI.prototype.setDefaultProductMenu = function () {
	this.refs.operations.ULNode.innerHTML = "";
	//this.refs.operations.LINode.style.display = "";
	this.refs.operations.children ={};
	//this.collapseRootNodes.bindAsEventListener(this, 'operations');
	//this.collapse(this.refs.categories);
	//this.expand(this.refs.operations);
	var setDefault = "true";
	var defaultProduct = null;
	for (var type in this.products)
		for(var product in this.products[type])
			if(this.refs.views.views.getViewByID(this.products[type][product].view)) {
				if(!this.refs.operations.children)
					this.refs.operations.children = {};
				if(!this.refs.operations.children[type])
					this.setProductTypeNode(type);
				this.setProductNode(type, product);
				if(defaultProduct == null){
					var defaultProduct = this.products[type][product];
					var defaultProductName = product;				
				}				
				if(this.state.operation == this.products[type][product].id && this.state.view == this.products[type][product].view) {
					setDefault = false;
					this.refs.operations.children[product].radio.checked = true;
					this.setOperation(this,this.products[type][product].id,this.products[type][product].view,this.products[type][product].optiondef);				
				}
			}
	if(setDefault) {
		this.setOperation(this,defaultProduct.id,defaultProduct.view,defaultProduct.optiondef);
		this.refs.operations.children[defaultProductName].radio.checked = true;

	}
}
LASUI.prototype.setProductTypeNode = function(type) {
	this.refs.operations.children[type] = {};
	this.refs.operations.children[type].LINode = document.createElement("LI");
	this.refs.operations.children[type].title = document.createElement("TEXT");
	this.refs.operations.children[type].title.innerHTML = "<b>" + type + "</b>";
	this.refs.operations.children[type].LINode.style.listStyleType = "none";
	this.refs.operations.children[type].LINode.style.padding = "0";
	this.refs.operations.children[type].LINode.style.margin = "-4pt";
	this.refs.operations.children[type].LINode.appendChild(this.refs.operations.children[type].title);
	this.refs.operations.ULNode.appendChild(this.refs.operations.children[type].LINode);
}
LASUI.prototype.setProductNode = function(type, product) {
	
	this.refs.operations.children[product] = {};
	this.refs.operations.children[product].LINode = document.createElement("LI");
	this.refs.operations.children[product].LINode.style.listStyleType = "none";
	this.refs.operations.children[product].LINode.style.padding = "0";
	this.refs.operations.children[product].LINode.style.margin = "-4pt";
	this.refs.operations.children[product].title = document.createElement("TEXT");
	this.refs.operations.children[product].title.innerHTML =  product;
	this.refs.operations.children[product].radio = document.createElement("INPUT");
	this.refs.operations.children[product].radio.type = "radio";
	this.refs.operations.children[product].radio.name = "product";
	this.refs.operations.children[product].radio.className = "LASRadioInputNode";
	this.refs.operations.children[product].radio.value = product.id;
	this.refs.operations.children[product].radio.onselect = this.setOperation.bindAsEventListener(this,this.products[type][product].id,this.products[type][product].view,this.products[type][product].optiondef );
	this.refs.operations.children[product].radio.onclick = this.setOperation.bindAsEventListener(this,this.products[type][product].id,this.products[type][product].view,this.products[type][product].optiondef);	
	this.refs.operations.children[product].LINode.appendChild(this.refs.operations.children[product].radio);	
	this.refs.operations.children[product].LINode.appendChild(this.refs.operations.children[product].title);
	this.refs.operations.ULNode.appendChild(this.refs.operations.children[product].LINode);
}

/**
 * Method to add a view radio button to the tree
 * @param {string} id A view id
 * @param {string} name A view name
 */
LASUI.prototype.setViewNode = function (id, name) {
	this.refs.views.views.getViewByID(id).LINode = document.createElement("LI");
	this.refs.views.views.getViewByID(id).LINode.style.listStyleType = "none";	
	this.refs.views.views.getViewByID(id).LINode.className = "LASTreeLINode";
	var title = document.createElement("TEXT");
	title.innerHTML = name;
	if(document.all) {
		var elem_nm = "<INPUT NAME='views'>";
		this.refs.views.views.getViewByID(id).INPUTNode =  document.createElement(elem_nm);
	} else {
		this.refs.views.views.getViewByID(id).INPUTNode =  document.createElement("INPUT");
		this.refs.views.views.getViewByID(id).INPUTNode.name="views";
	}
	this.refs.views.views.getViewByID(id).INPUTNode.type="radio";
	this.refs.views.views.getViewByID(id).INPUTNode.className = "LASRadioInputNode";
	this.refs.views.views.getViewByID(id).INPUTNode.onclick=this.setView.bindAsEventListener(this, id);
	
	if (this.state.view == id)
		this.refs.views.views.getViewByID(id).INPUTNode.checked = true;		
	this.refs.views.views.getViewByID(id).INPUTNode.id = id;
	this.refs.views.views.getViewByID(id).LINode.appendChild(this.refs.views.views.getViewByID(id).INPUTNode);
	this.refs.views.views.getViewByID(id).LINode.appendChild(title);	
	this.refs.views.ULNode.appendChild(this.refs.views.views.getViewByID(id).LINode);	
}
/**
 * Update the 4D Constraints selectors
 */	
LASUI.prototype.updateConstraints = function () {
	this.updating = true;		
	document.getElementById("Date").innerHTML = "";
	document.getElementById("Depth").innerHTML = "";

	if(this.state.grid.getAxis('x') || this.state.grid.getAxis('y')) {
		if(!this.refs.XYSelect.enabled) 			
			this.refs.XYSelect.enable();
	}			
	
	if(!this.products) {
		if(this.state.view.indexOf('x')>=0||this.state.view.indexOf('y')>=0) 
			document.getElementById("XYRegionType").style.display="";
	}
	if(this.state.view.indexOf('x')>=0&&this.state.view.indexOf('y')>=0) {
		this.initXYSelect("xy");
		if(!this.products)
			document.getElementById("RegionType").selectedIndex = 0;
	}
	else if(this.state.view.indexOf('x')>=0&&this.state.view.indexOf('y')<0) {
		this.initXYSelect("x");	
		if(!this.products)
			document.getElementById("RegionType").selectedIndex = 2;
	}
	else if(this.state.view.indexOf('x')<0&&this.state.view.indexOf('y')>=0) {
		this.initXYSelect("y"); 
		if(!this.products)
			document.getElementById("RegionType").selectedIndex = 3;
	}
	else if(this.state.view.indexOf('x')<0&&this.state.view.indexOf('y')<0) {
		this.initXYSelect("point");	
		if(!this.products)
			document.getElementById("RegionType").selectedIndex = 1;
	}	

/*	for(var d=0;d<this.state.view.length;d++)
		eval("this.init" + this.state.view.charAt(d).toUpperCase() + "Constraint('range')");
*/
	for(var d=0;d<this.state.grid.response.grid.axis.length;d++) 
		if(this.state.view.indexOf(this.state.grid.response.grid.axis[d].type) < 0) 
			if(this.state.variables[this.state.dataset].last().grid_type!="scattered")
				eval("this.init" + this.state.grid.response.grid.axis[d].type.toUpperCase() + "Constraint('point')");
			else
				eval("this.init" + this.state.grid.response.grid.axis[d].type.toUpperCase() + "Constraint('range')");					
		else
			eval("this.init" + this.state.grid.response.grid.axis[d].type.toUpperCase() + "Constraint('range')");	
	
	this.updating = false;
}
/**
 * Initialize the XY select widget to the grid
 */
LASUI.prototype.initXYSelect = function (mode) {
	if(!this.products) 
		document.getElementById("XYRegionType").style.display = "";	
	if(this.refs.XYSelect && this.state.view && this.state.dataset && this.state.variables)
		this.refs.XYSelect.enable();
	if(this.state.grid.getAxis('x') && this.state.grid.getAxis('y') && this.state.view)
	 {
		var bbox = {"x": {"min" : 0, "max" :0}, "y" : {"min" :0, "max" : 0}};

		if(this.state.grid.hasArange('x')||this.state.grid.hasMenu('x')) {
			bbox.x.min = parseFloat(this.state.grid.getLo('x'));
			bbox.x.max = parseFloat(this.state.grid.getHi('x'));
		} 
			
		if(this.state.grid.hasArange('y')||this.state.grid.hasMenu('y')) {
			bbox.y.min = parseFloat(this.state.grid.getLo('y'));
			bbox.y.max = parseFloat(this.state.grid.getHi('y'));
		}
		if(this.submitOnLoad && this.params){
			if(this.params.x)				
				bbox.x = this.params.x;
			if(this.params.y)				
				bbox.y = this.params.y;
		}
			
			
		this.refs.XYSelect.zoomOnBBox(bbox);
		this.refs.XYSelect.setDataGridBBox(bbox);
		this.refs.XYSelect.setSelectionGridBBox(bbox);
		this.refs.XYSelect.setView(mode);
		
	}
		
			if(!this.updating&&this.autoUpdate) {
				if(!this.products)
					this.getView();
			//		this.makeRequest();
			}
}
/**
 * Initialize an X grid control
 * @param {string} mode The axis mode. "range" or "point"
 */
LASUI.prototype.initXConstraint = function (mode) {		
}
/**
 * Initialize an Y grid control
 * @param {string} mode The axis mode. "range" or "point"
 */
LASUI.prototype.initYConstraint = function (mode) {		
}
/**
 * Initialize an Z grid control
 * @param {string} mode The axis mode. "range" or "point"
 */
LASUI.prototype.initZConstraint = function (mode) {	
	document.getElementById("Depth").innerHTML="";
	if(this.state.grid.hasMenu('z')) {			
		switch (mode) {
			case 'range':
				this.refs.DepthSelect = [document.createElement("SELECT"),document.createElement("SELECT")];
				this.refs.DepthSelect[0].className = "LASSelectNode";
				this.refs.DepthSelect[1].className = "LASSelectNode";
				for(var m=0;m<this.refs.DepthSelect.length;m++) {
					for(var v=0;v<this.state.grid.getMenu('z').length;v++) {
						var _opt = document.createElement("OPTION");
						_opt.value = this.state.grid.getMenu('z')[v][1];
						_opt.className = "LASOptionNode";
						_opt.innerHTML=this.state.grid.getMenu('z')[v][0];
						if(m==1 && v >= this.state.grid.getMenu('z').length-1)
							_opt.selected=true;
						if(m==0 && v == 0)
							_opt.selected=true;
						this.refs.DepthSelect[m].appendChild(_opt);
					}
					this.refs.DepthSelect[m].onchange=this.handleDepthRangeChange.bindAsEventListener(this);
				}
				var depth_label2 =document.createElement("STRONG");
				depth_label2.innerHTML = "Minimum Depth (" + this.state.grid.getAxis('z').units +") : ";
				document.getElementById("Depth").appendChild(depth_label2);
				document.getElementById("Depth").appendChild(this.refs.DepthSelect[0]);
				document.getElementById("Depth").appendChild(document.createElement("BR"));
				var depth_label3 =document.createElement("STRONG");
				depth_label3.innerHTML = "Maximum Depth (" + this.state.grid.getAxis('z').units +") : ";
				document.getElementById("Depth").appendChild(depth_label3);
				document.getElementById("Depth").appendChild(this.refs.DepthSelect[1]);
				document.getElementById("Depth").style.display="";
				break;
			case 'point':
				this.refs.DepthSelect = [document.createElement("SELECT")];
				this.refs.DepthSelect[0].className = "LASSelectNode";
				for(var v=0;v<this.state.grid.getMenu('z').length;v++) {
					var _opt = document.createElement("OPTION");
					_opt.value = this.state.grid.getMenu('z')[v][1];
					_opt.className = "LASOptionNode";
					_opt.innerHTML=this.state.grid.getMenu('z')[v][0];
					this.refs.DepthSelect[0].appendChild(_opt);
				}
				this.refs.DepthSelect[0].onchange=this.handleDepthChange.bindAsEventListener(this);
				var depth_label = document.createElement("STRONG");
				depth_label.innerHTML="Depth (" + this.state.grid.getAxis('z').units + ") : ";
				document.getElementById("Depth").appendChild(depth_label);	
				document.getElementById("Depth").appendChild(this.refs.DepthSelect[0]);
				document.getElementById("Depth").style.display="";
				break;
		}
	}
	if(this.state.grid.hasArange('z')){
		this.refs.DepthSelect = [];
		switch (mode) {
			case 'range':
				this.refs.DepthSelect = [document.createElement("SELECT"),document.createElement("SELECT")];
				this.refs.DepthSelect[0].className = "LASSelectNode";
				this.refs.DepthSelect[1].className = "LASSelectNode";

				for(var m=0;m<this.refs.DepthSelect.length;m++) {
					for(var v=parseFloat(this.state.grid.getLo('z'));v<=parseFloat(this.state.grid.getHi('z'));v+=parseFloat(this.state.grid.getDelta('z'))) {
						var _opt = document.createElement("OPTION");
						_opt.value = v;
						_opt.className = "LASOptionNode";
						_opt.innerHTML=v;
						if(m==1 && v == this.state.grid.getHi('z'))
							_opt.selected=true;
						if(m==0 && v == this.state.grid.getLo('z'))
							_opt.selected=true;
						this.refs.DepthSelect[m].appendChild(_opt);
					}
					this.refs.DepthSelect[m].onchange=this.handleDepthRangeChange.bindAsEventListener(this);
				}
				var depth_label2 =document.createElement("STRONG");
				depth_label2.innerHTML = "Minimum Depth (" + this.state.grid.getAxis('z').units +") : ";
				document.getElementById("Depth").appendChild(depth_label2);
				document.getElementById("Depth").appendChild(this.refs.DepthSelect[0]);
				document.getElementById("Depth").appendChild(document.createElement("BR"));
				var depth_label3 =document.createElement("STRONG");
				depth_label3.innerHTML = "Maximum Depth (" + this.state.grid.getAxis('z').units +") : ";
				document.getElementById("Depth").appendChild(depth_label3);
				document.getElementById("Depth").appendChild(this.refs.DepthSelect[1]);
				document.getElementById("Depth").style.display="";
				break;
			case 'point':
			
				this.refs.DepthSelect = [document.createElement("SELECT")];
				this.refs.DepthSelect[0].className = "LASSelectNode";

				for(var v=parseFloat(this.state.grid.getLo('z'));v<=parseFloat(this.state.grid.getHi('z'));v+=parseFloat(this.state.grid.getDelta('z'))) {
					var _opt = document.createElement("OPTION");
					_opt.value = v;
					_opt.className = "LASOptionNode";
					_opt.innerHTML=v;
					this.refs.DepthSelect[0].appendChild(_opt);
				}
				this.refs.DepthSelect[0].onchange=this.handleDepthChange.bindAsEventListener(this);
				var depth_label = document.createElement("STRONG");
				depth_label.innerHTML="Depth (" + this.state.grid.getAxis('z').units + ") : ";
				document.getElementById("Depth").appendChild(depth_label);	
				document.getElementById("Depth").appendChild(this.refs.DepthSelect[0]);
				document.getElementById("Depth").style.display="";
				break;
		}
	}
	
			if(!this.updating&&this.autoUpdate) {
				this.getView();
				this.makeRequest();
			}
}
/**
 * Initialize an T grid  control
 * @param {string} mode The axis mode. "range" or "point"
 */
LASUI.prototype.initTConstraint = function (mode) {
	document.getElementById("Date").innerHTML="";
	switch(this.state.grid.getDisplayType('t')) {
		case "widget":	
			switch(mode) {
				case 'range': 

					document.getElementById("Date").style.display="";
					this.refs.DW = new DateWidget(this.state.grid.getLo('t'),this.state.grid.getHi('t')); 
					this.refs.DW.callback = this.handleDateRangeChange.bindAsEventListener(this);
					this.refs.DW.render("Date","MDY","MDY");
					document.getElementById("Date").style.display="";				
					var label = document.createElement('strong');
					label.innerHTML="Start Date : ";
					document.getElementById("DW_td1").insertBefore(label,document.getElementById("DW_td1").firstChild);
					var label = document.createElement('strong');
					label.innerHTML="End Date : ";
					document.getElementById("DW_td2").insertBefore(label,document.getElementById("DW_td2").firstChild);
					break;
				case 'point':

					this.refs.DW = new DateWidget(this.state.grid.getLo('t'),this.state.grid.getHi('t')); 
					this.refs.DW.callback = this.handleDateChange.bindAsEventListener(this);
					this.refs.DW.render("Date","MDY");
					document.getElementById("Date").style.display="";
					var label = document.createElement('strong');
					label.innerHTML="Date : ";
					document.getElementById("DW_td1").insertBefore(label,document.getElementById("DW_td1").firstChild);
					break;
			}	
			break;
		case "menu": 
			switch(mode) {
				case 'range':

					document.getElementById("Date").style.display="";
					this.refs.DW = [document.createElement("SELECT"),document.createElement("SELECT")];
				this.refs.DW[0].className = "LASSelectNode";
				this.refs.DW[1].className = "LASSelectNode";

					for(var m=0;m<this.refs.DW.length;m++) {
						//this.refs.DW[m].id = "DW" + m;
						this.refs.DW[m].onchange=this.handleDateRangeChange.bindAsEventListener(this);
						for(var v=0;v<this.state.grid.getMenu('t').length;v++) {
							var _opt = document.createElement("OPTION");
							_opt.value = this.state.grid.getMenu('t')[v][1];				
							_opt.className = "LASOptionNode";
							_opt.innerHTML=this.state.grid.getMenu('t')[v][0];
							if(m==1 && v >= this.state.grid.getMenu('t').length-1)
								_opt.selected=true;
							if(m==0 && v == 0)
								_opt.selected=true;
							this.refs.DW[m].appendChild(_opt);
						}
					}
					document.getElementById("Date").innerHTML="<strong>Start Date : </strong>";	
					document.getElementById("Date").appendChild(this.refs.DW[0]);
					document.getElementById("Date").appendChild(document.createElement("BR"));
					var label = document.createElement("STRONG");
					document.getElementById("Date").appendChild(label);
					label.innerHTML="<strong>Stop Date : </strong>";	
					document.getElementById("Date").appendChild(this.refs.DW[1]);
					document.getElementById("Date").style.display="";
					break;
				case 'point':

					this.refs.DW = [document.createElement("SELECT")];
					this.refs.DW[0].onchange=this.handleDateChange.bindAsEventListener(this);
				this.refs.DW[0].className = "LASSelectNode";

					for(var v=0;v<this.state.grid.getMenu('t').length;v++) {
						var _opt = document.createElement("OPTION");
						_opt.value = this.state.grid.getMenu('t')[v][1];
						_opt.className = "LASOptionNode";
						_opt.id = "date";
						_opt.innerHTML=this.state.grid.getMenu('t')[v][0];
						this.refs.DW[0].appendChild(_opt);
					}
					//document.getElementById("Date").appendChild(document.createElement("BR"));
					var date_label = document.createElement("STRONG");
					date_label.innerHTML = "Date : ";
					document.getElementById("Date").appendChild(date_label);	
					document.getElementById("Date").appendChild(this.refs.DW[0]);
					document.getElementById("Date").style.display="";
					break;
			}
			break;
	 }		
	 
	 		if(!this.updating&&this.autoUpdate) {
				this.getView();
				this.makeRequest();		
			}
}			
/**
 * Put together and submit an LAS request
 */
LASUI.prototype.makeRequest = function () {
	if(!this.updating&&this.state.view!=""&&this.refs.XYSelect.enabled) {
		this.request = null;
		this.uirequest = null;
		this.request = new LASRequest('');
		this.uirequest = new LASRequest('');
		
		this.request.removeVariables();
		this.request.removeConstraints();		
		this.uirequest.removeVariables();
		this.uirequest.removeConstraints();		


		if(this.state.dataset==null) {alert("Please select a dataset and variables."); return;}
		if(this.state.variables[this.state.dataset]==null) {alert("Please select variables in the selected dataset."); return;}
		if(this.state.variables[this.state.dataset].length==0) {alert("Please select variables in the selected dataset."); return;}
		if(this.state.view==null) {alert("Please select a view."); return;}
		if(this.state.operation==null) {alert("Please select an output."); return;}
			
		//add the operation
		this.request.setOperation(this.state.operation);

		this.uirequest.setOperation("V7UI");	
		this.uirequest.setProperty('ui','plot',this.state.operation);
		this.uirequest.setProperty('ui','state',JSON.stringify(this.state));
		var uioptions = {};			
		//set the options
		for(var p in this.state.properties)	
			if((typeof this.state.properties[p] != "function") && (typeof this.state.properties[p] == "object")) { 
				this.request.setProperty(this.state.properties[p].type, p, escape(this.state.properties[p].value));
				uioptions[this.state.properties[p].type] = {p : escape(this.state.properties[p].value)};
			}
		this.request.setProperty("ferret","view",this.state.view);
		this.uirequest.setProperty("ui", "view",this.state.view);
		this.uirequest.setProperty("ui","options", JSON.stringify(uioptions));
	
		this.request.removeRegion();
		this.request.addRegion();
		this.uirequest.removeRegion();
	
		for(var d=0;d<this.state.grid.response.grid.axis.length;d++) 
			switch(this.state.grid.response.grid.axis[d].type) {
				case 'x' : 
					this.request.addRange('x',this.refs.XYSelect.extents.selection.grid.x.min,this.refs.XYSelect.extents.selection.grid.x.max); 
					this.uirequest.setProperty('ui','x',"{ 'min' : " + this.refs.XYSelect.extents.selection.grid.x.min + ", 'max' : " + this.refs.XYSelect.extents.selection.grid.x.max + "}"); 

					break;
				case 'y' : 
				 	this.request.addRange('y',this.refs.XYSelect.extents.selection.grid.y.min,this.refs.XYSelect.extents.selection.grid.y.max); 
				 	this.uirequest.setProperty('ui','y',"{ 'min' : " + this.refs.XYSelect.extents.selection.grid.y.min + ", 'max' : " + this.refs.XYSelect.extents.selection.grid.y.max + "}"); 

					break;
				case 't' : 
					if(this.state.view.indexOf('t')>=0||this.state.variables[this.state.dataset].last().grid_type=="scattered") 
						if(this.state.grid.hasMenu('t')){
							this.request.addRange('t',this.refs.DW[0].value,this.refs.DW[1].value); 
							this.uirequest.setProperty('ui','t',"{ 'min' : '" + this.refs.DW[0].value+ "', 'max' : '" + this.refs.DW[1].value + "'}");					
						} else {
							this.request.addRange('t',this.refs.DW.getDate1_Ferret(),this.refs.DW.getDate2_Ferret());
							this.uirequest.setProperty('ui','t',"{ 'min' : '" + this.refs.DW.getDate1_Ferret()+ "', 'max' : '" + this.refs.DW.getDate2_Ferret()+ "'}");
						}
					else
						if(this.state.grid.hasMenu('t')){
							this.request.addRange('t',this.refs.DW[0].value); 
							this.uirequest.setProperty('ui','t',"{ 'min' : '" + this.refs.DW[0].value+ "', 'max' : '" + this.refs.DW[0].value + "'}");	
						}
						else {
							this.request.addRange('t',this.refs.DW.getDate1_Ferret());
							this.uirequest.setProperty('ui','t',"{ 'min' : '" + this.refs.DW.getDate1_Ferret()+ "', 'max' : '" + this.refs.DW.getDate1_Ferret()+ "'}");
						}
					break;
				case 'z' :
					if(this.refs.DepthSelect)
						if(this.refs.DepthSelect.length>1) {
							this.request.addRange('z',this.refs.DepthSelect[0].value,this.refs.DepthSelect[1].value); 
							this.uirequest.setProperty('ui','z',"{ 'min' : '" + this.refs.DepthSelect[0].value+ "', 'max' : '" + this.refs.DepthSelect[1].value + "'}");		
						}
						else {
							this.request.addRange('z',this.refs.DepthSelect[0].value); 
							this.uirequest.setProperty('ui','z',"{ 'min' : '" + this.refs.DepthSelect[0].value+ "', 'max' : '" + this.refs.DepthSelect[0].value + "'}");					
						}						
					break;
			}
				
		//add the variables
		var uivarid = "[";
		for(var v in this.state.variables[this.state.dataset]) 
			if(typeof this.state.variables[this.state.dataset][v] != "function" && typeof this.state.variables[this.state.dataset][v] =="object") {
				this.request.addVariable(this.state.dataset, this.state.variables[this.state.dataset][v].ID);
				uivarid+= "'" + this.state.variables[this.state.dataset][v].ID + "',";			
			}
		uivarid = uivarid.substring(0,(uivarid.length-1)) + "]";

		this.uirequest.setProperty("ui","catid", JSON.stringify(this.state.categories));
		this.uirequest.setProperty("ui","dsid", this.state.dataset);
		this.uirequest.setProperty("ui","varid", uivarid);
		//prompt('ui req', this.uirequest.getXMLText());
		
		if(this.state.embed){
			if(document.getElementById("wait"))
				document.getElementById("wait").style.visibility="visible";
			if(document.getElementById("wait_msg"))
				document.getElementById("wait_msg").style.display="";
			document.getElementById('output').style.display = "none";
			document.getElementById('output').onload = function (evt) {document.getElementById("wait").style.visibility="hidden";document.getElementById("wait_msg").style.display="none";document.getElementById('output').style.display = ""};
			document.getElementById('output').src = (this.hrefs.getProduct.url + '?xml=' + escape(this.request.getXMLText()));
				
		}else
			window.open(this.hrefs.getProduct.url + '?xml=' +  escape(this.request.getXMLText()));
	}
}
/**
 * Put together and submit an LAS request
 */
LASUI.prototype.launchExternalProduct = function () {
	if(!this.updating&&this.state.view!=""&&this.refs.XYSelect.enabled) {
		this.request = null;
		this.request = new LASRequest('');
		this.request.removeVariables();
		this.request.removeConstraints();		
		if(this.state.dataset==null) {return;}
		if(this.state.variables[this.state.dataset]==null) { return;}
		if(this.state.variables[this.state.dataset].length==0) { return;}
		if(this.state.view==null) { return;}
		if(this.state.externaloperation==null) {return;}
			
		//add the operation
		this.request.setOperation(this.state.externaloperation);
			
		//set the options
		for(var p in this.state.externalproperties)	
			if((typeof this.state.externalproperties[p] != "function") && (typeof this.state.externalproperties[p] == "object")) { 
				this.request.setProperty(this.state.externalproperties[p].type, p, escape(this.state.externalproperties[p].value));
			}
		this.request.setProperty("ferret","view",this.state.view);
		
		this.request.removeRegion();
		this.request.addRegion();	
		for(var d=0;d<this.state.grid.response.grid.axis.length;d++) 
			switch(this.state.grid.response.grid.axis[d].type) {
				case 'x' : 
					this.request.addRange('x',this.refs.XYSelect.extents.selection.grid.x.min,this.refs.XYSelect.extents.selection.grid.x.max); 
					break;
				case 'y' : 
				 	this.request.addRange('y',this.refs.XYSelect.extents.selection.grid.y.min,this.refs.XYSelect.extents.selection.grid.y.max); 
					break;
				case 't' : 
					if(this.state.view.indexOf('t')>=0||this.state.variables[this.state.dataset].last().grid_type=="scattered") 
						if(this.state.grid.hasMenu('t'))
							this.request.addRange('t',this.refs.DW[0].value,this.refs.DW[1].value); 
						else
							this.request.addRange('t',this.refs.DW.getDate1_Ferret(),this.refs.DW.getDate2_Ferret());
					else
						if(this.state.grid.hasMenu('t'))
							this.request.addRange('t',this.refs.DW[0].value); 
						else
							this.request.addRange('t',this.refs.DW.getDate1_Ferret());
					break;
				case 'z' :
					if(this.refs.DepthSelect)
						if(this.refs.DepthSelect.length>1) 
							this.request.addRange('z',this.refs.DepthSelect[0].value,this.refs.DepthSelect[1].value); 
						else
							this.request.addRange('z',this.refs.DepthSelect[0].value); 
					break;
			}
				
		//add the variables
		for(var v in this.state.variables[this.state.dataset]) 
			if(typeof this.state.variables[this.state.dataset][v] != "function" && typeof this.state.variables[this.state.dataset][v] =="object")
				this.request.addVariable(this.state.dataset, this.state.variables[this.state.dataset][v].ID);
			
		
			window.open(this.hrefs.getProduct.url + '?xml=' +  escape(this.request.getXMLText()));
	}
}
/**
 * Method to query the server for an options object and pass json response to setOptionList
 * @param {string} optiondef Id of the option set to query the server for.
 */	
LASUI.prototype.getOptions = function (optiondef, DOMNode) {

		
	//populate the outputs tree with getOperations	
	var _bindArgs = {	
			url: this.hrefs.getOptions.url + '?opid=' + optiondef,
			mimetype: "text/plain",
			error: function(type,error) {alert('There was a problem communicating with the server. getOptions AJAX error.' + error.type + ' ' + error.message);},
			load: dojo.lang.hitch(DOMNode, dojo.lang.hitch(this,function(type,data,event){this.setOptionList(data,DOMNode)}))
		};
	var _request = dojo.io.bind(_bindArgs);

}
/**
 * Method to create an option list in the tree and add it to the DOM 
 * @param {object} strJson A json response compatible with LASGetOptionsResponse.js
 */	
LASUI.prototype.setOptionList = function (strJson,DOMNode) {
 
	

	var table = document.createElement("TABLE");
	table.style.margin = "-4pt";
	table.style.marginLeft = "6pt";
	table.cellpadding = "0";
	table.cellspacing = "0";
	DOMNode.TBODYNode = document.createElement("TBODY");
	table.appendChild(DOMNode.TBODYNode);
	DOMNode.appendChild(table);

	var response = eval("(" + strJson + ")");		

	var setDefault = true;
	this.state.properties = [];		
	this.refs.options.options = new LASGetOptionsResponse(response);
	var ct = this.refs.options.options.getOptionCount();
	if(ct) 
		for(var i=0;i<ct;i++) {
			this.setOptionTRNode(this.refs.options.options.getOptionID(i),DOMNode.TBODYNode);	
			
			switch(this.refs.options.options.getOptionType(i)) {
				case "menu" : 
					if(DOMNode == this.refs.options.ULNode)			 
						this.state.properties[this.refs.options.options.getOptionID(i)]={"type":"ferret", "value": this.refs.options.options.getOption(i).menu.item[0].values};
					else {
						this.state.externalproperties[this.refs.options.options.getOptionID(i)]={"type":"ferret", "value": this.refs.options.options.getOption(i).menu.item[0].values};
						   DOMNode.style.display="";					
					}					
					break;
				case "text":
					if(DOMNode == this.refs.options.ULNode)
						this.state.properties[this.refs.options.options.getOptionID(i)]={"type":"ferret", "value":""};
					else {
						this.state.externalproperties[this.refs.options.options.getOptionID(i)]={"type":"ferret", "value":""};
						   DOMNode.style.display="";
					}					
					break;
			}
		}
	if(this.autoupdate || this.submitOnLoad)
		this.makeRequest();
	this.submitOnLoad = false;
}
/** 
 * Method to create an option tree node and add it to the DOM
 * @param {string} id An option id	
 */
LASUI.prototype.setOptionTRNode = function (id,TBODYNode) {
	if(!this.refs.options.cache)
		this.refs.options.cache = {};	
	if(!this.refs.options.cache[id])
	{
		this.refs.options.cache[id] =  this.refs.options.options.getOptionByID(id);

		this.refs.options.cache[id].TRNode = document.createElement("TR");	
		var TD1 = document.createElement("TD");
		TD1.width="45%";
		TD1.innerHTML =this.refs.options.cache[id].title
		if(this.refs.options.cache[id].menu) {
			var obj = document.createElement("SELECT");
			obj.setAttribute('name', id);
  			for (var i=0;i<this.refs.options.cache[id].menu.item.length;i++) {
   			var option = document.createElement("OPTION");
     			option.value=this.refs.options.cache[id].menu.item[i].values;
     			option.text=this.refs.options.cache[id].menu.item[i].content;
    			//code branch for add() method differences between IE and FF
     			try {obj.add(option);}
     			catch(err) {obj.add(option,null);}
     		}
		} else {			
			var obj = document.createElement("INPUT");
			obj.type = "text";
			obj.className="LASTextInputNode";
		}
		if(TBODYNode == this.refs.options.ULNode.TBODYNode)		
			obj.onchange = this.setOption.bindAsEventListener(this,id,"properties");
		else
			obj.onchange = this.setOption.bindAsEventListener(this,id,"externalproperties");
		var TD2 = document.createElement("TD");
		TD2.appendChild(obj);
		this.refs.options.cache[id].TRNode.appendChild(TD1);	
		this.refs.options.cache[id].TRNode.appendChild(TD2);
	} 
	if(TBODYNode == this.refs.options.ULNode.TBODYNode)			 
		TBODYNode.appendChild(this.refs.options.cache[id].TRNode);	//first time, add it to the product
	else
		TBODYNode.appendChild(this.refs.options.cache[id].TRNode.cloneNode(true));	//second time, we need a copy
}
/**
 * Event handler to respond to option changes
 * @param {object} evt The event object
 *	@param {object} arguments Arguments added with function.prototype.bindAsEventListener<br>
 *			this -- context<br>
 *			id -- option id
 */
LASUI.prototype.setOption = function (evt) {
	var args = $A(arguments);
	var id = args[1];
	if(this.refs.options.options.getOptionByID(id).menu)
		this.state.properties[id]={"type" : "ferret", "value" : evt.target.options[evt.target.selectedIndex].value};
	else
		this.state.properties[id]={"type" : "ferret", "value" : evt.target.value};
	if(this.autoupdate)
		this.makeRequest();
}
/**
 * initMap()
 * Method to initialize the mapwidget
 * @param {object} mapid The id of the map container object in the DOM
 */
LASUI.prototype.initMap = function (mapid) {
  
  var args = {
  				  'DOMNode' : document.getElementById(mapid),
  				  'ondraw' : this.displayCoords.bindAsEventListener(this),
  				  'onafterdraw' : this.onafterdraw.bindAsEventListener(this),
  				  'plot_area' : {
  				  		'offX' : 0,
  				  		'offY' : 0,
  				  		'width' : 300,
  				  		'height' : 150
  				  },
  				  'img' : {
  				  		'src' : '',
  				  		'width' : 300,
  				  		'height' :150,
  				  		'extent' : {
  				  			'x' : {
  				  				'min' : -180,
  				  				'max' : 180
  				  			},
  				  			'y' : {
  				  				'min' : -90,
  				  				'max' : 90
  				  			}
  				  		}
  				  	}
  				 };				  
  	var req = new LASRequest();
  	req.removeVariables();
  	req.removeRegion();
	req.setOperation("xy_map");
	req.setRange("x",-180,180);
	req.setRange("y",-90,90);
	args.img.src = this.hrefs.getProduct.url + "?xml=" + escape(req.getXMLText()) + "&stream=true&stream_ID=plot_image";
  	this.refs.XYSelect = new MapWidget(args);
  	this.refs.XYSelect.disable();
  
}
/**
 * Method to update the xy constraints textboxes
 *	@param {object} XYSelect A MapWidget object	
 */
LASUI.prototype.displayCoords = function (XYSelect) {
	this.refs.inputs.minY.value=XYSelect.getSelectionGridYMin();
	this.refs.inputs.maxY.value=XYSelect.getSelectionGridYMax();
	this.refs.inputs.minX.value=XYSelect.getSelectionGridXMin();
	this.refs.inputs.maxX.value=XYSelect.getSelectionGridXMax();
}
/**
 * Event handler for the min X constraint textbox
 * @param {object} evt The event object		
 */
LASUI.prototype.setMinX = function (evt) {
	this.refs.XYSelect.updateSelectionGridXMin(this.refs.inputs.minX.value);
}
/**
 * Event handler for the max X constraint textbox
 * @param {object} evt The event object		
 */
LASUI.prototype.setMaxX = function (evt) {
	this.refs.XYSelect.updateSelectionGridXMax(this.refs.inputs.maxX.value);
}
/**
 * Event handler for the min Y constraint textbox
 * @param {object} evt The event object		
 */
LASUI.prototype.setMinY = function (evt) {
	this.refs.XYSelect.updateSelectionGridYMin(this.refs.inputs.minY.value);
}
/**
 * Event handler for the max Y constraint textbox
 * @param {object} evt The event object			
 */
LASUI.prototype.setMaxY = function (evt) {
	this.refs.XYSelect.updateSelectionGridYMax(this.refs.inputs.maxY.value);
}
/**
 * Event handler for the auto update toggle checkbox
 * @param {object} evt The event object
 */
LASUI.prototype.autoUpdate = function (evt) {
	this.autoupdate = evt.target.checked;
}
/**
 * Event handler to be attached to the MapWidget onafterdraw function
 * @param {object} evt The event object		
 */
LASUI.prototype.onafterdraw = function (evt) {
	
	if(this.autoupdate) {
		this.getView();
		this.makeRequest();
	}
}
/**
 * Event handler called on depth widget/menu changes
 * @param {object} evt The event object
 */
LASUI.prototype.handleDepthRangeChange = function (evt) {
	this.getView();
	if(this.autoupdate) {
		
		this.makeRequest();
				
	}
}
/**
 * Event handler called on depth widget/menu changes
 * @param {object} evt The event object		
 */
LASUI.prototype.handleDepthChange = function (evt) {
	this.getView();
	if(this.autoupdate)
		this.makeRequest();
}
/**
 * Event handler called on date range widget/menu changes
 * @params {object} evt The event object		
 */
LASUI.prototype.handleDateRangeChange = function (evt) {
	this.getView();
	if(this.autoupdate)
		this.makeRequest();
}
/**
 * Event handler called on date widget/menu changes
 * @params {object} evt The event object		
 */
LASUI.prototype.handleDateChange = function (evt) {
	this.getView();	
	if(this.autoupdate)
		this.makeRequest();
}
/**
 * Event handler to collapse all root nodes in the tree
 * @param {object} evt The event object
 * @param {object} arguments Arguements added with function.prototype.bindAsEventListener<br>
 * 	this -- the LASUI context<br> 
 *		node -- tree nod reference in this.refs	
 */
LASUI.prototype.collapseRootNodes = function (evt) {
	var args = $A(arguments);
	if(!this.refs[args[1]].isExpanded) {
		this.expand(this.refs[args[1]]);
		if(args[1]!="categories") this.collapse(this.refs.categories);
		if(args[1]!="views") this.collapse(this.refs.views);
		if(args[1]!="operations") this.collapse(this.refs.operations);
		if(args[1]!="options") this.collapse(this.refs.options);
	} else
		this.collapse(this.refs[args[1]]);
}
/**
 * Method to collapse a tree node
 * @param {object} obj Object reference in this.refs
 */
LASUI.prototype.collapse = function (obj) {
		if(obj.ULNode) obj.ULNode.style.display = "none";
		if(obj.IMGNode) obj.IMGNode.src = "JavaScript/ui/plus.gif";
		obj.isExpanded = false;
}
/**
 * Method to expand a tree node
 * @param {object} obj Object reference in this.refs
 */
LASUI.prototype.expand = function (obj) {
	if(obj.ULNode) obj.ULNode.style.display = "";
	if(obj.IMGNode) obj.IMGNode.src = "JavaScript/ui/minus.gif";
	obj.isExpanded = true;
}
//generic function to clone objects
LASUI.prototype.clone = function (obj) {
	if(typeof obj !='object')
		return obj;
	var myclone = new Object();
	
	for(var i in obj)
		myclone[i] = this.clone(obj[i]);
	return myclone;
 }
