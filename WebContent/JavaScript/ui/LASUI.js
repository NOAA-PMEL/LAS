/**
 * LASUI.js
 * Main class for LAS UI widget management and communication with the LAS json services.
 * author: Jeremy Malczyk -- 2007
 */
/**
 * The  LASUI class
 */
function LASUI () {
	// references for everything the UI builds
	this.refs = {
		"plot" : {}, 
		"DW" : {"widgetType" : null}, 
		"DepthWidget" : {"widgetType":null}, 
		"analysis" : {"enabled":false},
		"options": {"plot" :{},"download":{},"external":{}},
		"operations": {"plot" :{},"download":{},"external":{}}

	};
		
	// server side resource urls
	this.hrefs = {
		"getProduct"  : {"url" : "ProductServer.do"},
		"getCategories" : {"url" : "getCategories.do"},
		"getDataConstraints" : {"url" : "getDataConstraints.do"},
		"getGrid" : {"url" : "getGrid.do"},
		"getViews" : {"url" : "getViews.do"},
		"getOperations" : {"url" : "getOperations.do"},
		"getOptions" : {"url" : "getOptions.do"},
		"getMetadata" : {"url" : "getMetadata.do"}
	};

	//application state
	this.state = {
		"dataset" : null, 
		"variables" : {},
		"operation" : {"plot" :null,"download":null,"external":null},
		"properties" : {"plot" :[],"download":[],"external":[]},
		"view" :  {"plot" :null,"download":null,"external":null,"widgets":null},
		"embed" : true,
		"xybox" : {},
		"categorynames" : [],
		"analysis" : {"type":null,"axes":{}}
	};
					
	//DOM anchor ids.
	this.anchors = {
		"tree" : "tree",
		"output" : "output",
		"map" : "MapWidget",
		"date" : "date",
		"depth" : "depth",
		"header" : "header",
		"breadcrumb" : "breadcrumb",
		"variables" : "variables",
		"inputs" : {
			"maxX" : "input_maxX",
			"maxY" : "input_maxY",
			"minX" : "input_minX",
			"minY" : "input_minY"
		 },
		"analysis" : "analysis"
	} 
							
	this.request = new LASRequest();
			
	this.autoupdate=false;	

	for(var f in this)	
		if(typeof this[f] == "function")
			this[f].LASBind = function(object) {
		var __method = this;
		var args = [];	
		for (var i = 0, length = arguments.length; i < length; i++)
      			args.push(arguments[i]);	
		var object = args.shift();
		return function(event) {
			return __method.apply(object, [event || window.event].concat(args));
		}
	}
}

/**
 * Method to initialize the UI and begin AJAX interactions.
 * @param {string} anchorId the id of the page element to build the tree inside.
 */
LASUI.prototype.initUI = function (anchorId)
{

	if(this.params.dsid) {
		this.state.dataset = this.params.dsid;
		this.state.categories = this.params.catid;
	        this.state.variables[this.params.dsid] = [];
		this.state.variable = this.params.varid;
		this.state.operation.plot = this.params.plot;
		//this.state.xybox = eval("("+unescape(this.params.bbox)+")");
		this.autoupdate = this.params.autoupdate;
			
	}	
		this.UIMask = document.createElement("DIV");
	this.UIMask.className = "LASUIMask";
	this.toggleUIMask('none');
	document.body.appendChild(this.UIMask);
	this.firstload=true;
	if(this.autoupdate)
		this.submitOnLoad = true;
	else	
		this.submitOnLoad = false;
	this.submitOnLoad=true;
	this.refs.operations.plot.DOMNode = document.getElementById("plotType");
	this.refs.operations.download.DOMNode = document.getElementById("downloadType");
	this.refs.options.plot.DOMNode = document.getElementById("plotOptions");
	this.refs.options.download.DOMNode = document.getElementById("downloadOptions");
	this.refs.options.external.DOMNode = document.getElementById("externalOptions");
	this.refs.analysis.type = {"op" : document.getElementById("analysis_op"),
				   "axes" : document.getElementById("analysis_axes")
				  };
	this.refs.analysis.axes = {
				"xy": document.getElementById("xy_analysis"),
				"x": document.getElementById("x_analysis"),
				"y": document.getElementById("y_analysis"),
				"z": document.getElementById("z_analysis"),
				"t": document.getElementById("t_analysis")
				};
	
	//for(var a in this.refs.analysis.axes) {
		//this.refs.analysis.axes[a].checkbox.onclick = this.selectAnalysisAxis.LASBind(this,a);
		this.refs.analysis.type.axes.onchange = this.selectAnalysisAxis.LASBind(this,null,true);
	//}
	this.refs.analysis.type.op.onchange = this.selectAnalysisType.LASBind(this);


	//grab references to the map constraint inputs
	this.refs.inputs = {};
	for(var i in this.anchors.inputs)
		this.refs.inputs[i] = document.getElementById(this.anchors.inputs[i]);
   	this.refs.inputs.maxX.onchange = this.setMaxX.LASBind(this);
	this.refs.inputs.maxY.onchange = this.setMaxY.LASBind(this);
	this.refs.inputs.minX.onchange = this.setMinX.LASBind(this);
	this.refs.inputs.minY.onchange = this.setMinY.LASBind(this);				
	//inititialize the map widget
	this.initMap('MapWidget');

	if(document.getElementById("categories")) {
		this.refs.categories = {};
		this.refs.categories.LINode = document.getElementById("categories");
		this.refs.categories.title = document.createElement("SPAN");
		this.refs.categories.title.innerHTML = "Select a dataset category.";
		this.refs.categories.title.className = "LASTreeTitleNode"; 		
		this.refs.categories.ULNode= document.createElement("UL");
		this.refs.categories.ULNode.className = "LASTreeULNode";		
		this.refs.categories.LINode.appendChild(this.refs.categories.title);
		this.refs.categories.LINode.appendChild(this.refs.categories.ULNode);
		this.refs.categories.isExpanded = true;
		
		if(!document.all)
			var req = new XMLHttpRequest(this);
		else
			var req = new ActiveXObject("Microsoft.XMLHTTP"); 
		
		req.onreadystatechange = this.AJAXhandler.LASBind(this, req, "this.setCategoryTreeNode(req.responseText,this.refs.categories,'categories');");
		req.open("GET", this.hrefs.getCategories.url);
		req.send(null);
	}

}
LASUI.prototype.toggleUIMask = function(display) {
	this.UIMask.style.height=(document.body.offsetHeight+100)+'px';
	this.UIMask.style.width=(document.body.offsetWidth+30)+'px';
	this.UIMask.style.display=display;
}
LASUI.prototype.AJAXhandler = function  (app) {
		var args = arguments;
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
	var args = arguments;
	var node = args[1];
	var i = args[2];
	if(node.category)
		if(node.category.getChildID(i))
			if(node.category.getChildChildrenType(i)=="variables")
				window.open(this.hrefs.getMetadata.url + '?dsid=' + node.category.getChildID(i));
			else
				window.open(this.hrefs.getMetadata.url + '?catid=' + node.category.getChildID(i));
		
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
	node.children[i].IMGNode.onclick = this.selectCategory.LASBind(this, node, i);

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
	td2.onclick = this.selectCategory.LASBind(this, node, i);
	td2.innerHTML = node.category.getChildName(i);
	td2.className = "LASTreeTableCell";
	td2.style.textAlign  = "left";
	tr.appendChild(td1);
	tr.appendChild(td2);
	if(node.category.getChildChildrenType(i)=="variables") {
		var td3 = document.createElement("TD");
		node.children[i].A = document.createElement("A");
		node.children[i].A.innerHTML = "<img src='images/icon_info.gif'>";
		node.children[i].A.onclick = this.showInfo.LASBind(this,node,i);
		td3.appendChild(node.children[i].A);
		td3.className = "LASTreeTableCell";
		td3.width="12pt";	
		tr.appendChild(td3);
	}
	tbody.appendChild(tr);
	table.appendChild(tbody);
	node.children[i].LINode.appendChild(table);
	node.children[i].LINode.appendChild(node.children[i].ULNode);
	//do not add categories that do not have children 
	if((node.category.getChildChildrenType(i) == "variables" && node.category.getChild(i).children_dsid)||node.category.getChildChildrenType(i) != "variables")
		node.ULNode.appendChild(node.children[i].LINode);		
	//if(node.category.getChildType(i)=='dataset' && node.category.getChildID(i)==this.state.dataset) 
	//	this.getCategory(node, i);		

	if(this.refs.categories[node.category.getChild(i).ID]||node.category.getChild(i).ID==this.state.dataset)
		this.selectCategory(null,node,i);
}
LASUI.prototype.createVariableOptionNode = function (node, i) {	
	var OPTIONNode = document.createElement("OPTION");
	OPTIONNode.innerHTML = node.category.getChildName(i);
	OPTIONNode.id = "OPTION_" + node.category.getChildID(i);
	OPTIONNode.onselect=this.setVariable.LASBind(this, node, i, true, true);
	document.getElementById(this.anchors.variables).appendChild(OPTIONNode);
	document.getElementById(this.anchors.variables).onchange = function (evt) {this.options[this.selectedIndex].onselect({"target" : {"selected" :true}});}
	if(this.state.variables && this.state.dataset)
		if(this.state.variables[this.state.dataset])
				if(this.state.variable==node.category.getChildID(i)||this.state.variables[this.state.dataset]==node.category.getChild(i)){ 
					OPTIONNode.selected=true;
				}

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
	if(!node.children[i])
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
	node.children[i].INPUTNode.onclick=this.setVariable.LASBind(this, node, i);
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
	
 //  var td3 = document.createElement("TD");
//	td3.align = "right";
//	td3.className = "LASTreeTableCell";
  // td3.width="12px";
   //node.children[i].A = document.createElement("A");
	//node.children[i].A.innerHTML = "<img src='images/icon_info.gif'>";
	//node.children[i].A.onclick = this.showInfo.LASBind(this,node,i);
	//td3.appendChild(node.children[i].A);
	
	tr.appendChild(td1);
	tr.appendChild(td2);
//	tr.appendChild(td3);
	tbody.appendChild(tr);
	table.appendChild(tbody);	
	node.children[i].LINode.appendChild(table);
	
	if(this.state.variables && this.state.dataset)
		if(this.state.variables[this.state.dataset])
				if(this.state.variable==node.category.getChildID(i)||this.state.variables[this.state.dataset]==node.category.getChild(i)){ 
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
		
				if(!document.all)
			var req = new XMLHttpRequest(this);
		else
			var req = new ActiveXObject("Microsoft.XMLHTTP"); 
		req.onreadystatechange = this.AJAXhandler.LASBind(this, req, "this.setCategoryTreeNode(req.responseText,args[3].children[args[4]],args[3].category.getChild(args[4]));", parentNode, i);
		req.open("GET", this.hrefs.getCategories.url + "?catid=" + parentNode.category.getChildID(i));
		req.send(null);

	} 
/*	if(parentNode.children[i].ULNode.style.display=="none") {
		for(var c=0;c< parentNode.children.length;c++)
			parentNode.children[c].ULNode.style.display="none";
		parentNode.children[i].ULNode.style.display="";	//expand the category if it has been selected 
		if(parentNode.children[i].category) //if the category is a dataset set it as the selected dataset
			if(parentNode.children[i].category.getCategoryType()=="dataset")
			{
					this.setDataset(parentNode.category.getChildID(i));
			}
	} //else*/
		//parentNode.children[i].ULNode.style.display="none";
}
LASUI.prototype.onSetVariable = function() {
				document.getElementById('categories').style.display='none';
				document.getElementById('XYZTControls').style.display='';
				this.toggleUIMask('none');		
				var categories = this.state.categorynames[0];
				for(var i=1;i<this.state.categorynames.length;i++)
					categories += ' / ' + this.state.categorynames[i];
				//var variables = this.state.variables[this.state.dataset].name;
				
				document.getElementById(this.anchors.breadcrumb).innerHTML = '<a class="LASLink" onclick="window.open(\'getMetadata.do?dsid='+this.state.dataset+'\')"><img src="images/icon_info.gif"></a>&nbsp;<b>' + categories + ' : <select id="variables"/></b>'; 	
								
				
				if(this.refs.analysis.enabled) {
					this.hideAnalysis();
					this.showAnalysis();
				}									
}
/**
 * Event handler for category selection, bind to category DOM object events. 
 * @param {object} evt The event object
 * @param {object} arguments arguments added using function.prototype.LASBind<br> 
 *					this -- the context this function is run in<br>
 *					parentNode -- parent node in this.refs<br>
 *					i -- index of the category within the parentNode
 */ 
LASUI.prototype.selectCategory = function (evt) {
	var args = arguments;
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
			
		if(parentNode == this.refs.categories)			
			this.state.categorynames = [];
		this.state.categorynames.push(parentNode.category.getChildName(i));
	} else
		this.collapse(parentNode.children[i]);
	if(!parentNode.children[i].category) {
		parentNode.children[i].IMGNode.src = "JavaScript/components/mozilla_blu.gif";
				if(!document.all)
			var req = new XMLHttpRequest(this);
		else
			var req = new ActiveXObject("Microsoft.XMLHTTP"); 
		req.onreadystatechange = this.AJAXhandler.LASBind(this, req, "this.setCategoryTreeNode(req.responseText,args[3].children[args[4]],args[3].category.getChild(args[4]));", parentNode, i);
		req.open("GET", this.hrefs.getCategories.url + "?catid=" + parentNode.category.getChildID(i));
		req.send(null);
	}		
}
/**
 *Event handler for variable selection, bind to variable DOM object events. 
 *@param {object} evt The event object
 *@param {object} arguments Arguments added using function.prototype.LASBind<br> 
 *						this -- context setVariable is being run in<br>
 * 					dataset -- a LASGetCategoriesReponse dataset object<br> 
 * 					i -- index the variable within the category or dataset   
 */ 
LASUI.prototype.setVariable = function (evt) {
	var args = arguments
	var dataset = args[1];
	var i = args[2];

	if(evt.target){ 
		if(evt.target.checked)			
			var loadVariable = evt.target.checked;
		if(evt.target.selected)
			var loadVariable = evt.target.selected;
	}
	else if (evt.srcElement){ 
		if(evt.target.checked)			
			var loadVariable = evt.srcElement.checked;
		if(evt.target.selected)
			var loadVariable = evt.srcElement.selected;
	}
	else if(args.length>3)
		var loadVariable = args[3];
	var datasetID = dataset.category.getDatasetID();
	var variableID = dataset.category.getChildID(i);
	var variable = dataset.category.getChild(i);
	var variableLINode = dataset.children[i].LINode;
	var variableINPUTNode = dataset.children[i].INPUTNode;
	variableINPUTNode.checked = loadVariable;	
	if (loadVariable) {			
		//start an array of selected variables for this dataset if we havent already
		if(typeof this.state.variables[datasetID] != 'object') 
			this.state.variables[datasetID] = [];
			
		this.state.variables[datasetID] = variable; 
						
		//get all the other data for this dataset/variable combo
		this.state.dataset = datasetID;
		this.state.variable = variableID;
		this.getGrid(datasetID,variableID);
		this.getDataConstraints(datasetID,variableID);
		this.getViews(datasetID,variableID);

	}	else {
			if (typeof this.state.variables[datasetID] == 'object')
				delete(this.state.variables[datasetID]=null);
			this.state.variable ="";
		}		 		
	if(this.onSetVariable)
		this.onSetVariable();
	
	for(var i=0;i<dataset.children.length;i++)
		this.createVariableOptionNode(dataset,i);

}
LASUI.prototype.setVariable = function (evt) {
	var args = arguments
	var dataset = args[1];
	var i = args[2];

	if(evt.target){ 
		if(evt.target.checked)			
			var loadVariable = evt.target.checked;
		if(evt.target.selected)
			var loadVariable = evt.target.selected;
	}
	else if (evt.srcElement){ 
		if(evt.target.checked)			
			var loadVariable = evt.srcElement.checked;
		if(evt.target.selected)
			var loadVariable = evt.srcElement.selected;
	}
	else if(args.length>3)
		var loadVariable = args[3];
	
	if(args.length>4)
		var switchVar = args[4]; // dont do the following AJAX calls
	var datasetID = dataset.category.getDatasetID();
	var variableID = dataset.category.getChildID(i);
	var variable = dataset.category.getChild(i);
	var variableLINode = dataset.children[i].LINode;
	var variableINPUTNode = dataset.children[i].INPUTNode;
	variableINPUTNode.checked = loadVariable;	
	if (loadVariable) {			
		//start an array of selected variables for this dataset if we havent already
		if(typeof this.state.variables[datasetID] != 'object') 
			this.state.variables[datasetID] = [];
			
		this.state.variables[datasetID] = variable; 
						
		//get all the other data for this dataset/variable combo
		this.state.dataset = datasetID;
		this.state.variable = variableID;
		if(!switchVar) {
			this.getGrid(datasetID,variableID);
			this.getDataConstraints(datasetID,variableID);
			this.getViews(datasetID,variableID);
		}

	}	else {
			if (typeof this.state.variables[datasetID] == 'object')
				delete(this.state.variables[datasetID]=null);
			this.state.variable ="";
		}		 		
	if(this.onSetVariable)
		this.onSetVariable();
	
	for(var i=0;i<dataset.children.length;i++)
		this.createVariableOptionNode(dataset,i);

}
/**
 * Method to set the active dataset and call getGrid if appropriate
 * @params {string} dataset A dataset id 
 */ 
LASUI.prototype.setDataset = function (dataset) {
	this.state.dataset = dataset;
	this.state.newgrid = true;
	if(typeof this.state.variables[dataset] == 'object') {
		this.getGrid(dataset, this.state.variable);
	}
	if(this.onSetDataset)
		this.onSetDataset();
}
/**
 * Method to query the server for a category
 * @param {string} dataset The selected dataset id
 * @param {string} variable The selected variable id
 *	@param {string} view The selected view id 
 */	
LASUI.prototype.getDataConstraints = function (dataset, variable) {
		if(!document.all)
			var req = new XMLHttpRequest(this);
		else
			var req = new ActiveXObject("Microsoft.XMLHTTP"); 
		req.onreadystatechange = this.AJAXhandler.LASBind(this, req, "this.setDataConstraints(req.responseText);");
		req.open("GET", this.hrefs.getDataConstraints.url + '?dsid=' + dataset + '&varid=' + variable);
		req.send(null);
}
/**
 * Method to query the server for a category
 * @param {string} dataset The selected dataset id
 * @param {string} variable The selected variable id
 *	@param {string} view The selected view id 
 */	
LASUI.prototype.setDataConstraints = function (strJson) {
	if(strJson) {
		var response = eval("(" + strJson + ")");
		if(!this.state.constraints)
			this.state.constraints = {};
		this.state.lastconstraints = this.state.constraints;
		this.state.constraints = response.constraints;
	}
	if(this.state.lastconstraints != this.state.constraints) {
		document.getElementById('DataConstraints').innerHTML='';
		document.getElementById('DataConstraints').style.display="";

		if(this.state.constraints.constraint) {

/*			if(this.state.constraints.constraint.constraint) {
				var temp = this.state.constraints.constraint.constraint;
				this.state.constraints.constraint = [];
				this.state.constraints.constraint[0]= temp;
			}*/
		
			this.refs.constraints = [];
		for(var c=0;c<this.state.constraints.constraint.length;c++) {
			var constraint = {};
			var DIVNode = document.createElement("DIV");
			constraint.apply = document.createElement("INPUT");
			constraint.apply.type="checkbox";
			DIVNode.appendChild(constraint.apply);	
			if(this.state.constraints.constraint[c].constraint.menu.content) {
				var temp =this.state.constraints.constraint[c].constraint.menu;
				this.state.constraints.constraint[c].constraint.menu = [];
				this.state.constraints.constraint[c].constraint.menu[0] = temp
			}		
			for(var m=0;m < this.state.constraints.constraint[c].constraint.menu.length;m++) {
				//if(this.state.constraints.constraint[c].constraint.menu[m]) {
					eval('constraint.' + this.state.constraints.constraint[c].constraint.menu[m].position + ' = document.createElement("SELECT")');		
				
					if(this.state.constraints.constraint[c].constraint.menu[m].item.content) {
						var temp =this.state.constraints.constraint[c].constraint.menu[m].item;
						this.state.constraints.constraint[c].constraint.menu[m].item = [];
						this.state.constraints.constraint[c].constraint.menu[m].item[0] = temp
					}
					for(var i=0;i<this.state.constraints.constraint[c].constraint.menu[m].item.length;i++) {
						var option = document.createElement('OPTION');
						option.value = 	this.state.constraints.constraint[c].constraint.menu[m].item[i].values;
						option.innerHTML = this.state.constraints.constraint[c].constraint.menu[m].item[i].content;
						eval('constraint.' + this.state.constraints.constraint[c].constraint.menu[m].position + '.appendChild(option)');
					}
				//}				
			} 
			if(constraint.lhs)DIVNode.appendChild(constraint.lhs);
			if(constraint.ops)DIVNode.appendChild(constraint.ops);
			if(constraint.rhs)
				DIVNode.appendChild(constraint.rhs);
			else {
				constraint.rhs = document.createElement('INPUT');
				constraint.rhs.type="text";
				DIVNode.appendChild(constraint.rhs);
			}
			constraint.type = this.state.constraints.constraint[c].constraint.type;
			document.getElementById("DataConstraints").appendChild(DIVNode);
			this.refs.constraints.push(constraint);

		}
		}			
	}
}

/**
 * Method to query the server for the available operations
 * @param {string} dataset The selected dataset id
 * @param {string} variable The selected variable id
 *	@param {string} view The selected view id 
 */	
LASUI.prototype.getOperations = function (dataset, variable, view) {

				if(!document.all)
			var req = new XMLHttpRequest(this);
		else
			var req = new ActiveXObject("Microsoft.XMLHTTP"); 
		req.onreadystatechange = this.AJAXhandler.LASBind(this, req, "this.setOperationList(req.responseText);");
		req.open("GET", this.hrefs.getOperations.url + '?dsid=' + dataset + '&varid=' + variable + '&view=' + view);
		req.send(null);
		
		
}/**
 *	Event handler to set the operation
 *	@param {object} evt The event object
 * @arguments Arguments added using function.prototype.LASBind<br>
 *			this -- the context the handler is executing in.<br>
 *get			id -- an operation id
 */
LASUI.prototype.setDownloadOperation = function (evt) {
	var args = arguments;
	var id = args[1];
	var optiondef = args[2];

	this.state.operation.download=id;
	this.refs.options.download.DOMNode.innerHTML="";
	
	var view = args[2];
	var optiondef = args[3];	
	this.state.view.download = view;	
	this.updateConstraints(view);	
	
	if(optiondef) {
		var cancel = document.createElement("INPUT");		
		cancel.type = "submit";
		cancel.value=	"Close";
		cancel.className = "LASSubmitInputNode";
		cancel.onclick = this.genericHandler.LASBind(this,"this.refs.options.download.DOMNode.style.display='none';this.toggleUIMask('none');");
		this.refs.options.download.DOMNode.innerHTML = "";		
		this.refs.options.download.DOMNode.appendChild(cancel);
		this.getOptions(optiondef, this.refs.options.download.DOMNode);
	}
	
}
/**
 *	Event handler to set the operation
 *	@param {object} evt The event object
 * @arguments Arguments added using function.prototype.LASBind<br>
 *			this -- the context the handler is executing in.<br>
 *get			id -- an operation id
 */
LASUI.prototype.setOperation = function (evt) {
	var args = arguments;
	var id = args[1];
	var optiondef = args[3];
	var type = args[4];
	if(!type)
		type="plot";
	this.state.operation[type]=id;
	this.refs.options[type].DOMNode.innerHTML="";
	
	var view = args[2];

	this.state.view[type] = view;	
	if(this.refs.analysis.enabled)
		for(var a in this.state.analysis.axes)
			if(view.indexOf(a)<0){
				view += a;
				this.refs.analysis.axes[a].selected = true;
			}
			else {
				this.refs.analysis.axes[a].selected = false;
				if(a=="x"||a=="y")				
					this.refs.analysis.axes["xy"].selected = false;
				delete this.state.analysis.axes[a];	
			}
	this.state.view.widgets = view;
	
	this.updateConstraints();	
	
	if(optiondef) {
		var cancel = document.createElement("INPUT");		
		cancel.type = "submit";
		cancel.value=	"Close";
		cancel.className = "LASSubmitInputNode";
		cancel.onclick = this.genericHandler.LASBind(this,"this.refs.options.plot.DOMNode.style.display='none';this.toggleUIMask('none');");
		this.refs.options.plot.DOMNode.innerHTML = "";		
		this.refs.options.plot.DOMNode.appendChild(cancel);
		this.getOptions(optiondef, this.refs.options.plot.DOMNode);
	}
	
	this.getOperations(this.state.dataset,this.state.variables[this.state.dataset].ID,this.state.view.plot);	
	
	if (this.autoupdate)
		this.makeRequest();
	else
		this.showUpdateLink();

}	
LASUI.prototype.genericHandler = function (evt) {
	if(arguments[1])
		eval(arguments[1]);
}
/**
 *  Method to populate the list of avialable operations from a json response from the server
 *  @param {string} strJson A json string compatibe with the LASGetOperationsResponse class
 */	
LASUI.prototype.setOperationList = function (strJson) {
	
	var response = eval("(" + strJson + ")");
	var setDefault = true;
	this.refs.operations.external.operations = new LASGetOperationsResponse(response);
	if(!this.refs.operations.external.operations.DIVNode) {	
		this.refs.operations.external.operations.DIVNode= document.createElement('DIV');
		this.refs.operations.external.operations.DIVNode.className='LASPopupDIVNode';
		this.refs.operations.external.operations.DIVNode.style.display="none";
	}
	//disable all nodes first
	for(var row=0;row<document.getElementById("productButtons").childNodes.length;row++)
		for(var cell=0;cell<document.getElementById("productButtons").childNodes[row].childNodes.length;cell++)
			for(var button=0;button<document.getElementById("productButtons").childNodes[row].childNodes[cell].childNodes.length;button++)			
				if(document.getElementById("productButtons").childNodes[row].childNodes[cell].childNodes[button].tagName == "INPUT")
					document.getElementById("productButtons").childNodes[row].childNodes[cell].childNodes[button].disabled="true";

	document.body.appendChild(this.refs.operations.external.operations.DIVNode);
	if(!this.refs.operations.external.operations.response.operations.error) 
		for(var i=0;i<this.refs.operations.external.operations.getOperationCount();i++) 
			this.setOperationNode(this.refs.operations.external.operations.getOperationID(i), this.refs.operations.external.operations.getOperationName(i));	
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
		button.onclick=this.doProductIconClick.LASBind(this, id);
	}
}
LASUI.prototype.doProductIconClick = function (evt) {
	var args = arguments;
	var id = args[1];
	this.state.operation.external = id;
	this.toggleUIMask('');
	this.refs.operations.external.operations.DIVNode.innerHTML = "";
	var submit = document.createElement("INPUT");
	var cancel = document.createElement("INPUT");
	submit.type = "submit";
	submit.value = "Submit";
	submit.className = "LASSubmitInputNode";
	submit.onclick = this.genericHandler.LASBind(this, "this.refs.operations.external.operations.DIVNode.style.display='none';this.launchExternalProduct();this.toggleUIMask('none')");
	cancel.type = "submit";
	cancel.value="Close";
	cancel.className = "LASSubmitInputNode";
	cancel.onclick = this.genericHandler.LASBind(this, "this.refs.operations.external.operations.DIVNode.style.display='none';this.toggleUIMask('none')");
	
	this.refs.operations.external.operations.DIVNode.appendChild(submit);
	this.refs.operations.external.operations.DIVNode.appendChild(cancel);
	if(this.refs.operations.external.operations.getOperationByID(id))
		if(this.refs.operations.external.operations.getOperationByID(id).optiondef)
			this.getOptions(this.refs.operations.external.operations.getOperationByID(id).optiondef.IDREF, this.refs.operations.external.operations.DIVNode);	
		else if(this.refs.operations.external.operations.getOperationByID(id).optionsdef)
			this.getOptions(this.refs.operations.external.operations.getOperationByID(id).optionsdef.IDREF, this.refs.operations.external.operations.DIVNode);	
		else {		
			this.refs.operations.external.operations.DIVNode.style.display='none';
			this.toggleUIMask('none');		
			this.launchExternalProduct(); 
			return;
		}	
}
/**
 * Method to query the server for the available grids
 * @param {string} dataset A dataset id
 * @param {string} variable A variable id within the dataset 
 */
LASUI.prototype.getGrid = function (dataset, variable) {
			if(!document.all)
			var req = new XMLHttpRequest(this);
		else
			var req = new ActiveXObject("Microsoft.XMLHTTP"); 
		req.onreadystatechange = this.AJAXhandler.LASBind(this, req, "this.setGrid(req.responseText);");
		req.open("GET",  this.hrefs.getGrid.url + '?dsid=' + dataset + '&varid=' + variable);
		req.send(null);
			

	
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
		this.state.lastgrid = this.clone(this.state.grid);
	
		
			
	this.state.grid = new LASGetGridResponse(response);
	if(!this.state.lastgrid)
		this.state.lastgrid = this.clone(this.state.grid);

}
/**
 * Method to query the server for a list of allowed views
 * @param {string} dataset A dataset id
 * @param {string} variable A variable id 
 */
LASUI.prototype.getViews = function (dataset,variable) {	
				if(!document.all)
			var req = new XMLHttpRequest(this);
		else
			var req = new ActiveXObject("Microsoft.XMLHTTP"); 
		req.onreadystatechange = this.AJAXhandler.LASBind(this, req, "this.setViews(req.responseText);");
		req.open("GET", this.hrefs.getViews.url + '?dsid=' + dataset + '&varid=' +variable);
		req.send(null);
	
}
/**
 * Update the views list with the allowed views
 * @param {string} strJson A string compatible with LASGetViewsResposne.js
 */	
LASUI.prototype.setViews = function (strJson) {
	//clear the current view list and state
	var response = eval("(" + strJson + ")");
	if(!this.refs.views)
		this.refs.views = {};
	this.refs.views.views = new LASGetViewsResponse(response);
	this.setDefaultProductMenu();
}
LASUI.prototype.setDefaultProductMenu = function () {
	this.refs.operations.plot.DOMNode.innerHTML = "";
	this.refs.operations.download.DOMNode.innerHTML = "";
	for(var type in this.refs.operations)	
		this.refs.operations[type].children ={};

	var setPlotDefault = "true";
	var setDownloadDefault = "true";
	var defaultPlotProduct = null;
	var defaultDownloadProduct = null;
	for (var type in this.products)
		for(var product in this.products[type])
			if(this.refs.views.views.getViewByID(this.products[type][product].view) || type == "Download Data" || this.products[type][product].view == "") {
				if(!this.refs.operations.plot.children)
					this.refs.operations.plot.children = {};
				if(!this.refs.operations.plot.children[type]&&!this.refs.operations.download.SELECTNode)
					this.setProductTypeNode(type);
				this.setProductNode(type, product);
				if(defaultPlotProduct == null && type != "Download Data"){
					var defaultPlotProduct = this.products[type][product];
					var defaultPlotProductName = product;				
				}
				if(defaultDownloadProduct == null && type == "Download Data"){
					var defaultDownloadProduct = this.products[type][product];
					var defaultDownloadProductName = product;				
				}				
				if(this.state.operation.plot == this.products[type][product].id && type != "Download Data"/*&& this.state.view.plot == this.products[type][product].view*/ ) {
					setPlotDefault = false;
					this.refs.operations.plot.children[product].radio.checked = true;
					this.setOperation(this,this.products[type][product].id,this.products[type][product].view,this.products[type][product].optiondef);								
				}
				if(this.state.operation.download == this.products[type][product].id && type == "Download Data"/*&& this.state.view.plot == this.products[type][product].view*/ ) {
					setDownloadDefault = false;
					//this.refs.operations.download.children[product].DOMNode.selected = true;
					this.setDownloadOperation(this,this.products[type][product].id,this.products[type][product].view,this.products[type][product].optiondef);								
				}
			}
	
	if(setPlotDefault) {
		this.setOperation(this,defaultPlotProduct.id,defaultPlotProduct.view,defaultPlotProduct.optiondef);
		this.refs.operations.plot.children[defaultPlotProductName].radio.checked = true;

	}/*
	if(setDownloadDefault) {
		this.setDownloadOperation(this,defaultDownloadProduct.id,defaultProduct.view,defaultProduct.optiondef);
		this.refs.operations.download.children[defaultDownloadProductName].DOMNode.selected = true;

	}*/
	//this.updating=true;
	this.refs.XYSelect.updatePixelExtents();
	//this.updating=false;
}
LASUI.prototype.setProductTypeNode = function(type) {

	if(type=="Download Data") {
		var title = document.createElement("DIV");
		title.innerHTML = "<b>" + type + "</b>";
		this.refs.operations.download.DOMNode.className = "LASPlotCategory";
		this.refs.operations.download.DOMNode.appendChild(title);	;
		this.refs.operations.download.SELECTNode = document.createElement("SELECT");
		var format = document.createElement("OPTION");
		format.innerHTML = "Select format...";
		this.refs.operations.download.SELECTNode.appendChild(format); 
		//this.refs.operations.plot.children[type].radio = document.createElement("INPUT");
		this.refs.operations.download.SELECTNode.onchange = function (evt) {this.options[this.selectedIndex].onselect();};
		var label = document.createElement("LI");
		label.className="LASPlotType";
		label.appendChild(this.refs.operations.download.SELECTNode);
		this.refs.operations.download.DOMNode.appendChild(label);
		this.refs.operations.download.INPUTNode = document.createElement("INPUT");
		this.refs.operations.download.INPUTNode.type = "button"
		this.refs.operations.download.INPUTNode.value = "Download";
		this.refs.operations.download.INPUTNode.onclick = this.makeRequest.LASBind(this,"download");	
		this.refs.operations.download.DOMNode.appendChild(this.refs.operations.download.INPUTNode);
	} else 
		if(type!="Point Data") {
			this.refs.operations.plot.children[type] = {};
			this.refs.operations.plot.children[type].LINode = document.createElement("LI");
			this.refs.operations.plot.children[type].LINode
			this.refs.operations.plot.children[type].title = document.createElement("TEXT");
			this.refs.operations.plot.children[type].title.innerHTML = "<b>" + type + "</b>";
			this.refs.operations.plot.children[type].LINode.className = "LASPlotCategory";
			this.refs.operations.plot.children[type].LINode.appendChild(this.refs.operations.plot.children[type].title);
		//this.refs.operations.plot.children[type].LINode.appendChild(this.refs.operations.plot.children[type].ULNode);
			this.refs.operations.plot.DOMNode.appendChild(this.refs.operations.plot.children[type].LINode);
		}
}
LASUI.prototype.downloadData = function() {

}
LASUI.prototype.setProductNode = function(type, product) {
	if(type!="Download Data") {
		this.refs.operations.plot.children[product] = {};
		this.refs.operations.plot.children[product].LINode = document.createElement("LI");
		this.refs.operations.plot.children[product].LINode.className = "LASPlotType";
		if(this.products[type][product].view.length==0)
			this.refs.operations.plot.children[product].LINode.style.display = "none";
		this.refs.operations.plot.children[product].title = document.createElement("TEXT");
		this.refs.operations.plot.children[product].title.innerHTML =  product;
		this.refs.operations.plot.children[product].radio = document.createElement("INPUT");
		this.refs.operations.plot.children[product].radio.type = "radio";
		this.refs.operations.plot.children[product].radio.name = "product";
		this.refs.operations.plot.children[product].radio.className = "LASRadioInputNode";
		this.refs.operations.plot.children[product].radio.value = product.id;
		this.refs.operations.plot.children[product].radio.onselect = this.setOperation.LASBind(this,this.products[type][product].id,this.products[type][product].view,this.products[type][product].optiondef,"plot" );
		this.refs.operations.plot.children[product].radio.onclick = this.setOperation.LASBind(this,this.products[type][product].id,this.products[type][product].view,this.products[type][product].optiondef,"plot");	
		this.refs.operations.plot.children[product].LINode.appendChild(this.refs.operations.plot.children[product].radio);	
		this.refs.operations.plot.children[product].LINode.appendChild(this.refs.operations.plot.children[product].title);
		this.refs.operations.plot.DOMNode.appendChild(this.refs.operations.plot.children[product].LINode);
	} else {
		this.refs.operations.download.children[product] = {};
		this.refs.operations.download.children[product].OPTIONNode = document.createElement("OPTION");
		this.refs.operations.download.children[product].OPTIONNode.innerHTML =  product;
		this.refs.operations.download.children[product].OPTIONNode.value = product.id;
		this.refs.operations.download.children[product].OPTIONNode.onselect = this.setDownloadOperation.LASBind(this,this.products[type][product].id,this.products[type][product].view,this.products[type][product].optiondef);
		//this.refs.operations.plot.children[product].OPTIONNode.onclick = this.setOperation.LASBind(this,this.products[type][product].id,this.products[type][product].view,this.products[type][product].optiondef);	
		this.refs.operations.download.SELECTNode.appendChild(this.refs.operations.download.children[product].OPTIONNode);	
	}
}
LASUI.prototype.onPlotLoad = function () {
	if(document.getElementById(this.anchors.output).contentWindow.myMapWidget) {
		this.refs.plot = document.getElementById(this.anchors.output).contentWindow.myMapWidget;
		this.updateConstraints();
		document.getElementById(this.anchors.output).contentWindow.onPlotLoad = this.onPlotLoad.LASBind(this);
	} else
		this.refs.plot = {};
	document.getElementById("wait").style.visibility="hidden";
	document.getElementById("wait_msg").style.display="none";
	document.getElementById('output').style.display = '';

}

LASUI.prototype.onFirstPlotLoad = function () {
	this.firstload =false;
	document.getElementById(this.anchors.output).contentWindow.onPlotLoad = this.onPlotLoad.LASBind(this);
	document.getElementById(this.anchors.output).onload = this.onPlotLoad.LASBind(this);
	document.getElementById("wait").style.visibility="hidden";
	document.getElementById("wait_msg").style.display="none";
	document.getElementById('output').style.display = '';

}
/**
 * Update the 4D Constraints selectors
 */	
LASUI.prototype.updateConstraints = function (view) {
	if(view==null&& view !="")
		var view = this.state.view.widgets;
	else
		this.state.view.widgets = view;
	this.updating = true;			
	document.getElementById("Date").innerHTML = "";
	document.getElementById("Depth").innerHTML = "";
	
	if(this.state.grid.getAxis('x') || this.state.grid.getAxis('y')) {
		if(!this.refs.XYSelect.enabled)  			
			this.refs.XYSelect.enable();
	}
	var reset=false;
				
	if(this.state.lastgrid) {
		if(this.state.grid.response.grid.ID!=this.state.lastgrid.response.grid.ID)
			reset=true;
	}
	if(!this.initialized)
		reset=true;
		
	if(view.indexOf('x')>=0&&view.indexOf('y')>=0) 
		this.initXYSelect("xy",reset);
	else if(view.indexOf('x')>=0&&view.indexOf('y')<0) 
		this.initXYSelect("x",reset);	
	else if(view.indexOf('x')<0&&view.indexOf('y')>=0) 
		this.initXYSelect("y",reset); 
	else if(view.indexOf('x')<0&&view.indexOf('y')<0) 
		this.initXYSelect("point",reset);
	
	for(var d=0;d<this.state.grid.response.grid.axis.length;d++) 
		if(view.indexOf(this.state.grid.response.grid.axis[d].type) < 0) 
			if(this.state.variables[this.state.dataset].grid_type!="scattered"&&!this.refs.analysis.enabled)
				eval("this.init" + this.state.grid.response.grid.axis[d].type.toUpperCase() + "Constraint('point',reset)");
			else
				eval("this.init" + this.state.grid.response.grid.axis[d].type.toUpperCase() + "Constraint('range',reset)");					
		else
			eval("this.init" + this.state.grid.response.grid.axis[d].type.toUpperCase() + "Constraint('range',reset)");	
	
	this.refs.XYSelect.updatePixelExtents();
	this.updating = false;
	if(this.refs.analysis.enabled)
		for(var d=0;d<this.state.grid.response.grid.axis.length;d++) 
			if(view.indexOf(this.state.grid.response.grid.axis[d].type) < 0) 
				switch(this.state.grid.response.grid.axis[d].type) {
					case 'z' : this.refs.DepthWidget[this.refs.DepthWidget.widgetType][1].disabled = true; break;
					case 't' : if(this.refs.DW.widgetType)
							this.refs.DW.disable('hi');
						  else
							this.refs.DW[1].disabled = true;
							 break;
				}
			else
				switch(this.state.grid.response.grid.axis[d].type) {
					case 'z' : this.refs.DepthWidget[this.refs.DepthWidget.widgetType][1].disabled = false; break;
					case 't' : if(this.refs.DW.widgetType)
							this.refs.DW.enable('hi');
						  else
							this.refs.DW[1].disabled = false;
							 break;
				}				
		
	
	this.initialized=true;
}
/**
 * Initialize the XY select widget to the grid
 */
LASUI.prototype.initXYSelect = function (mode, reset) {
	if(!this.products) 
		document.getElementById("XYRegionType").style.display = "";	
	if(this.refs.XYSelect && this.state.view.plot && this.state.dataset && this.state.variables)
		this.refs.XYSelect.enable();
	if(this.state.grid.getAxis('x') && this.state.grid.getAxis('y') && mode)
	 {
		var grid = {"x": {"min" : 0, "max" :0}, "y" : {"min" :0, "max" : 0}};

		if(this.refs.plot.extents) 
			var sel = this.refs.plot.extents.selection.grid;
		else
			var sel = {"x" : {"min" : this.refs.XYSelect.getSelectionGridXMin(),
					 "max" : this.refs.XYSelect.getSelectionGridXMax()
					},
				 "y" : {"min" : this.refs.XYSelect.getSelectionGridYMin(),
					 "max" : this.refs.XYSelect.getSelectionGridYMax()
					}
			}
			
		if(reset==true ) {		
			if(this.state.grid.hasArange('x')||this.state.grid.hasMenu('x')) {
				grid.x.min = parseFloat(this.state.grid.getLo('x'));
				grid.x.max = parseFloat(this.state.grid.getHi('x'));
			} 
			
			if(this.state.grid.hasArange('y')||this.state.grid.hasMenu('y')) 
			{
				grid.y.min = parseFloat(this.state.grid.getLo('y'));
				grid.y.max = parseFloat(this.state.grid.getHi('y'));
			}
		} else
			grid = {"x" : {"min" : this.refs.XYSelect.getPlotGridXMin(),
					 "max" : this.refs.XYSelect.getPlotGridXMax()
					},
				 "y" : {"min" : this.refs.XYSelect.getPlotGridYMin(),
					 "max" : this.refs.XYSelect.getPlotGridYMax()
					}				
				}

		if (sel.x.min>grid.x.min&&sel.x.min<grid.x.max && this.firstload!=true) {
			if(sel.x.min == sel.x.max&&mode!='y'&&mode!='point') {
				if(this.state.xybox.width) {
					if(sel.x.min-this.state.xybox.width/2 > grid.x.min)
						sel.x.min=sel.x.min-this.state.xybox.width/2;
					else
						sel.x.min=grid.x.min;
					if(sel.x.max+this.state.xybox.width/2 < grid.x.max)	
 						sel.x.max=sel.x.max+this.state.xybox.width/2;
					else
						sel.x.max=grid.x.max;
				} else {
					sel.x.min=grid.x.min;
 					sel.x.max=grid.x.max;
			   	}
			}
		} else
			sel.x.min=grid.x.min;

		if (sel.x.max>grid.x.min&&sel.x.max<grid.x.max&&this.firstload!=true) {
			if(sel.x.min == sel.x.max&&mode!='y'&&mode!='point') {
				if(this.state.xybox.width) {
					if(sel.x.min-this.state.xybox.width/2 > grid.x.min)
						sel.x.min=sel.x.min-this.state.xybox.width/2;
					else
						sel.x.min=grid.x.min;
					if(sel.x.max+this.state.xybox.width/2 < grid.x.max)	
 						sel.x.max=sel.x.max+this.state.xybox.width/2;
					else
						sel.x.max=grid.x.max;
				} else {
					sel.x.min=grid.x.min;
 					sel.x.max=grid.x.max;
				}		
			}
		} else
			sel.x.max=grid.x.max;

		if (sel.y.min>grid.y.min&&sel.y.min<grid.y.max&&this.firstload!=true){
			if(sel.y.min == sel.y.max&&mode!='x'&&mode!='point') {
				if(this.state.xybox.height) {
					if(sel.y.min-this.state.xybox.height/2 > grid.y.min)
						sel.y.min=sel.y.min-this.state.xybox.height/2;
					else
						sel.y.min=grid.y.min;
					if(sel.y.max+this.state.xybox.height/2 < grid.y.max)	
 						sel.y.max=sel.y.max+this.state.xybox.height/2;
					else
						sel.y.max=grid.y.max;
				} else {
					sel.y.min=grid.y.min;
	 				sel.y.max=grid.y.max;
				}			
			}
		} else
			sel.y.min=grid.y.min;

		if (sel.y.max>grid.y.min&&sel.y.max<grid.y.max&&this.firstload!=true) {
			if(sel.y.min == sel.y.max&&mode!='x'&&mode!='point') {
				if(this.state.xybox.height) {
					if(sel.y.min-this.state.xybox.height/2 > grid.y.min)
						sel.y.min=sel.y.min-this.state.xybox.height/2;
					else
						sel.y.min=grid.y.min;
					if(sel.y.max+this.state.xybox.height/2 < grid.y.max)	
 						sel.y.max=sel.y.max+this.state.xybox.height/2;
					else
						sel.y.max=grid.y.max;
				} else {				
					sel.y.min=grid.y.min;
 					sel.y.max=grid.y.max;
				}			
			}
		} else
			sel.y.max=grid.y.max;

		this.refs.XYSelect.zoomOnBBox(grid);		
		if(reset==true) {
			this.refs.XYSelect.setDataGridBBox(grid);
			this.refs.XYSelect.setSelectionGridBBox(grid);		
		}
				
		if(this.state.newgrid)
			this.refs.XYSelect.setSelectionGridBBox(grid);	
		else
			this.refs.XYSelect.setSelectionGridBBox(sel);
		delete(this.state.newgrid);					
		if(this.submitOnLoad && this.params){
			var bbox = {};			
			if(this.params.x)				
				bbox.x = this.params.x;
			if(this.params.y)				
				bbox.y = this.params.y;
			this.refs.XYSelect.setSelectionGridBBox(sel);	
				
		}
		
		if(this.submitOnLoad && this.params){
			if(this.params.viewx)				
				bbox.x = this.params.viewx;
			if(this.params.viewy)				
				bbox.y = this.params.viewy;
		}
		//this.refs.XYSelect.zoomOnBBox(bbox);
		//save some params in case we return		
		if(sel.x.min!=sel.x.max)
			this.state.xybox.width=(sel.x.max-sel.x.min);
		if(sel.y.min!=sel.y.max)
			this.state.xybox.height=(sel.y.max-sel.y.min);
						
		
		this.refs.XYSelect.setView(mode);
		
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
LASUI.prototype.initZConstraint = function (mode, reset) {	
	document.getElementById("Depth").innerHTML="";
	if(this.state.grid.hasMenu('z')) 	
		if (reset || this.refs.DepthWidget.widgetType != "menu") {
			this.refs.DepthWidget.menu = [document.createElement("SELECT"),document.createElement("SELECT")];
			this.refs.DepthWidget.widgetType = "menu";
			for(var m=0;m<this.refs.DepthWidget[this.refs.DepthWidget.widgetType].length;m++) {
				for(var v=0;v<this.state.grid.getMenu('z').length;v++) {
					var _opt = document.createElement("OPTION");
					_opt.value = this.state.grid.getMenu('z')[v][1];
					_opt.className = "LASOptionNode";
					_opt.innerHTML=this.state.grid.getMenu('z')[v][0];
					if(m==1 && v >= this.state.grid.getMenu('z').length-1)
						_opt.selected=true;
					if(m==0 && v == 0)
						_opt.selected=true;
					this.refs.DepthWidget[this.refs.DepthWidget.widgetType][m].appendChild(_opt);
				}
				this.refs.DepthWidget[this.refs.DepthWidget.widgetType][m].className = "LASSelectNode";				
				this.refs.DepthWidget[this.refs.DepthWidget.widgetType][m].onchange=this.handleDepthRangeChange.LASBind(this);
			}
		}
	if(this.state.grid.hasArange('z'))
		if (reset || this.refs.DepthWidget.widgetType != "arange") {
			this.refs.DepthWidget.arange = [document.createElement("SELECT"),document.createElement("SELECT")];
			this.refs.DepthWidget.widgetType = "arange";
			for(var m=0;m<this.refs.DepthWidget[this.refs.DepthWidget.widgetType].length;m++) {
				this.refs.DepthWidget[this.refs.DepthWidget.widgetType][m].className = "LASSelectNode";
				for(var v=parseFloat(this.state.grid.getLo('z'));v<=parseFloat(this.state.grid.getHi('z'));v+=parseFloat(this.state.grid.getDelta('z'))) {
					var _opt = document.createElement("OPTION");
					_opt.value = v;
					_opt.className = "LASOptionNode";
					_opt.innerHTML=v;
					if(m==1 && v == this.state.grid.getHi('z'))
						_opt.selected=true;
					if(m==0 && v == this.state.grid.getLo('z'))
						_opt.selected=true;
					this.refs.DepthWidget[this.refs.DepthWidget.widgetType][m].appendChild(_opt);
				}
				this.refs.DepthWidget[this.refs.DepthWidget.widgetType][m].onchange=this.handleDepthRangeChange.LASBind(this);
			}
		}
	switch (mode) {
			case 'range':		
				var depth_label2 =document.createElement("STRONG");
				depth_label2.innerHTML = "Minimum Depth (" + this.state.grid.getAxis('z').units +") : ";
				document.getElementById("Depth").appendChild(depth_label2);
				document.getElementById("Depth").appendChild(this.refs.DepthWidget[this.refs.DepthWidget.widgetType][0]);
				document.getElementById("Depth").appendChild(document.createElement("BR"));
				var depth_label3 =document.createElement("STRONG");
				depth_label3.innerHTML = "Maximum Depth (" + this.state.grid.getAxis('z').units +") : ";
				document.getElementById("Depth").appendChild(depth_label3);
				document.getElementById("Depth").appendChild(this.refs.DepthWidget[this.refs.DepthWidget.widgetType][1]);
				document.getElementById("Depth").style.display="";
				break;
			case 'point':
				var depth_label = document.createElement("STRONG");
				depth_label.innerHTML="Depth (" + this.state.grid.getAxis('z').units + ") : ";
				document.getElementById("Depth").appendChild(depth_label);	
				document.getElementById("Depth").appendChild(this.refs.DepthWidget[this.refs.DepthWidget.widgetType][0]);
				document.getElementById("Depth").style.display="";
				break;
	}
	if(!this.updating&&this.autoupdate) 
		this.makeRequest();
	else
		this.showUpdateLink();

}
/**
 * Initialize an T grid  control
 * @param {string} mode The axis mode. "range" or "point"
 */
LASUI.prototype.initTConstraint = function (mode,reset) {
	document.getElementById("Date").style.display="";
	switch(this.state.grid.getDisplayType('t')) {
		case "widget":	
			if(reset || !this.refs.DW)
				if(reset || this.refs.DW.widgetType != "DateWidget")	{
					document.getElementById("Date").innerHTML="";
					this.refs.DW = new DateWidget(this.state.grid.getLo('t'),this.state.grid.getHi('t')); 
					this.refs.DW.callback = this.handleDateRangeChange.LASBind(this);
				}

			switch(mode) {
				case 'range': 	
					document.getElementById("Date").innerHTML="<tbody><tr><td><table><tbody><tr><td><strong>Start Date :</strong></td></tr><tr><td><strong>End Date :</strong></tr></tbody></table></td><td id='DWAnchor'></td></tr></tbody></table>";						
					this.refs.DW.render("DWAnchor","MDY","MDY");
					break;
				case 'point':
					document.getElementById("Date").innerHTML="<tbody><tr><td><table><tbody><tr><td><strong>Date :</strong></td><td id='DWAnchor'></td></tr></tbody></table>";						
					this.refs.DW.render("Date","MDY");
					break;
			}	
			break;
		case "menu": 
			if(reset || !this.refs.DW)
				if(reset || this.refs.DW[0].tagName != "SELECT")	{
					this.refs.DW = [document.createElement("SELECT"),document.createElement("SELECT")];
					for(var m=0;m<this.refs.DW.length;m++) {
						this.refs.DW[m].onchange=this.handleDateRangeChange.LASBind(this);
						this.refs.DW[m].className = "LASSelectNode";
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
				}
			switch(mode) {
				case 'range':
					document.getElementById("Date").innerHTML="<strong>Start Date : </strong>";	
					document.getElementById("Date").appendChild(this.refs.DW[0]);
					document.getElementById("Date").appendChild(document.createElement("BR"));
					var label = document.createElement("STRONG");
					document.getElementById("Date").appendChild(label);
					label.innerHTML="<strong>End Date : </strong>";
					document.getElementById("Date").appendChild(this.refs.DW[1]);
					break;
				case 'point':
					var date_label = document.createElement("STRONG");
					date_label.innerHTML = "Date : ";
					document.getElementById("Date").appendChild(date_label);	
					document.getElementById("Date").appendChild(this.refs.DW[0]);
					break;
			}
			break;
	 }		

	
	 
	 		if(!this.updating&&this.autoupdate) 
				this.makeRequest();		
			else
				this.showUpdateLink();

}
LASUI.prototype.showUpdateLink = function (){
	try	{
		if(!this.updating&&this.refs.XYSelect.enabled&&this.state.dataset!=null&&this.state.variables[this.state.dataset]!=null&&this.state.variable&&this.state.view.plot!=null&&this.state.operation.plot!=null) 
		document.getElementById('update').style.backgroundColor='gold';
	else
		document.getElementById('update').style.backgroundColor='';
	} 
	catch (err){ 
		document.getElementById('update').style.backgroundColor='';	
}	}	

LASUI.prototype.makeDownloadRequest = function (){

}
		
/**
 * Put together and submit an LAS request
 */
LASUI.prototype.makeRequest = function (type) {
	if(!type)
		var type = 'plot';
	if(!this.updating) {
		this.request = null;
		this.uirequest = '';
		this.request = new LASRequest('');
		
		this.request.removeVariables();
		this.request.removeConstraints();		
		

		if(this.state.dataset==null) {alert("Please select a dataset and variables."); return;}
		if(this.state.variables[this.state.dataset]==null) {alert("Please select variables in the selected dataset."); return;}
		if(this.state.variables[this.state.dataset].length==0) {alert("Please select variables in the selected dataset."); return;}
		if(this.state.view[type]==null) {alert("Please select a view."); return;}
		if(this.state.operation[type]==null) {alert("Please select an output."); return;}
			
		//add the operation
		this.request.setOperation(this.state.operation[type]);

		this.uirequest = '&plot=' + this.state.operation[type];
		//this.uirequest.setProperty('ui','state',JSON.stringify(this.state));
		var uioptions = '';			
		//set the options
		for(var p in this.state.properties[type])	
			if((typeof this.state.properties[type][p] != "function") && (typeof this.state.properties[type][p] == "object")) { 
				this.request.setProperty(this.state.properties[type][p].type, p, escape(this.state.properties[type][p].value));
				uioptions[this.state.properties[type][p].type] = {p : escape(this.state.properties[type][p].value)};
			}
		var view = this.state.view[type];
		//if(view=="") view = "d";
		this.request.setProperty("ferret","view",view);
		this.uirequest+="&options=" +  escape(JSON.stringify(uioptions));
	
		this.request.removeRegion();
	
		//add the variables
		
		this.request.setVariable(this.state.dataset, this.state.variable);
		

		this.request.addRegion();
		//do the analysis, if required.
		if(this.refs.analysis.enabled && this.state.analysis.name) {
			var Analysis = {"label" : this.state.analysis.name + ' ' + this.state.variables[this.state.dataset].name, "axis" : []};
			for(var axis_id in this.state.analysis.axes) {
				var Axis= {"type" : axis_id, "op" : this.state.analysis.type};
				if(this.state.grid.hasAxis(axis_id)){
					var Axis = {"type" : axis_id, "op" : this.state.analysis.type};
					switch(axis_id) {
						case 'x' : 
							Axis.lo=this.refs.XYSelect.extents.selection.grid.x.min;
							Axis.hi =this.refs.XYSelect.extents.selection.grid.x.max;
							break;
						case 'y' : 
						 	Axis.lo=this.refs.XYSelect.extents.selection.grid.y.min;
							Axis.hi=this.refs.XYSelect.extents.selection.grid.y.max; 
							break;
							case 't' : 
							if(this.state.grid.hasMenu('t')){
								Axis.lo=this.refs.DW[0].value;
								Axis.hi=this.refs.DW[1].value; 
							} else {
								Axis.lo=this.refs.DW.getDate1_Ferret();
								Axis.hi=this.refs.DW.getDate2_Ferret();
							}
							break;
						case 'z' :
							Axis.lo=this.refs.DepthWidget[this.refs.DepthWidget.widgetType][0].value;
							Axis.hi=this.refs.DepthWidget[this.refs.DepthWidget.widgetType][1].value; 						
							break;
					} 
					Analysis.axis.push(Axis);
				}
				
			}
			if(Analysis.axis.length>0 && this.state.analysis.name !="None")			
				this.request.setAnalysis(0,Analysis);
		}

		for(var d=0;d<this.state.grid.response.grid.axis.length;d++) 
			switch(this.state.grid.response.grid.axis[d].type) {
				case 'x' : 
					this.request.addRange('x',this.refs.XYSelect.extents.selection.grid.x.min,this.refs.XYSelect.extents.selection.grid.x.max); 
					this.uirequest+="&x=" + escape("{ 'min' : " + this.refs.XYSelect.extents.selection.grid.x.min + ", 'max' : " + this.refs.XYSelect.extents.selection.grid.x.max + "}"); 
					this.uirequest+="&viewx="+escape("{ 'min' : " + this.refs.XYSelect.extents.data.grid.x.min + ", 'max' : " + this.refs.XYSelect.extents.data.grid.x.max + "}"); 

					break;
				case 'y' : 
				 	this.request.addRange('y',this.refs.XYSelect.extents.selection.grid.y.min,this.refs.XYSelect.extents.selection.grid.y.max); 
				 	this.uirequest+="&y="+escape("{ 'min' : " + this.refs.XYSelect.extents.selection.grid.y.min + ", 'max' : " + this.refs.XYSelect.extents.selection.grid.y.max + "}"); 
				 	this.uirequest+="&viewy="+escape("{ 'min' : " + this.refs.XYSelect.extents.data.grid.y.min + ", 'max' : " + this.refs.XYSelect.extents.data.grid.y.max + "}"); 
					break;
				case 't' : 
					if(this.state.view[type].indexOf('t')>=0||this.state.variables[this.state.dataset].grid_type=="scattered") 
						if(this.state.grid.hasMenu('t')){
							this.request.addRange('t',this.refs.DW[0].value,this.refs.DW[1].value); 
							this.uirequest+="&t="+escape("{ 'min' : '" + this.refs.DW[0].value+ "', 'max' : '" + this.refs.DW[1].value + "'}");					
						} else {
							this.request.addRange('t',this.refs.DW.getDate1_Ferret(),this.refs.DW.getDate2_Ferret());
							this.uirequest+="&t="+escape("{ 'min' : '" + this.refs.DW.getDate1_Ferret()+ "', 'max' : '" + this.refs.DW.getDate2_Ferret()+ "'}");
						}
					else
						if(this.state.grid.hasMenu('t')){
							this.request.addRange('t',this.refs.DW[0].value); 
							this.uirequest+="&t=" + escape("{ 'min' : '" + this.refs.DW[0].value+ "', 'max' : '" + this.refs.DW[0].value + "'}");	
						}
						else {
							this.request.addRange('t',this.refs.DW.getDate1_Ferret());
							this.uirequest+="&t=" + escape("{ 'min' : '" + this.refs.DW.getDate1_Ferret()+ "', 'max' : '" + this.refs.DW.getDate1_Ferret()+ "'}");
						}
					break;
				case 'z' :
					if(this.state.view[type].indexOf('z')>=0||this.state.variables[this.state.dataset].grid_type=="scattered") {
							this.request.addRange('z',this.refs.DepthWidget[this.refs.DepthWidget.widgetType][0].value,this.refs.DepthWidget[this.refs.DepthWidget.widgetType][1].value); 
							this.uirequest+="&z=" + escape("{ 'min' : '" + this.refs.DepthWidget[this.refs.DepthWidget.widgetType][0].value+ "', 'max' : '" + this.refs.DepthWidget[this.refs.DepthWidget.widgetType][1].value + "'}");		
						}
						else {
							this.request.addRange('z',this.refs.DepthWidget[this.refs.DepthWidget.widgetType][0].value); 
							this.uirequest+="&z=" + escape("{ 'min' : '" + this.refs.DepthWidget[this.refs.DepthWidget.widgetType][0].value+ "', 'max' : '" + this.refs.DepthWidget[this.refs.DepthWidget.widgetType][1].value + "'}");					
						}						
					break;
			}
		if(this.refs.constraints)		
		for(var c=0; c<this.refs.constraints.length;c++){
			if(this.refs.constraints[c].apply.checked)
			switch(this.refs.constraints[c].type) {
				case 'text' :
					this.request.addTextConstraint(escape(this.refs.constraints[c].lhs.value),escape(this.refs.constraints[c].ops.value),escape(this.refs.constraints[c].rhs.value));
				break;
				case 'variable' :
					this.request.addVariableConstraint(escape(this.state.dataset), escape(this.refs.constraints[c].lhs.value),escape(this.refs.constraints[c].ops.value),escape(this.refs.constraints[c].rhs.value));
				break;
			}
		}


		this.uirequest+="&catid=" + escape(JSON.stringify(this.state.categories));
		this.uirequest+="&dsid=" + this.state.dataset;
		this.uirequest+="&varid=" + this.state.variable;
		//prompt('ui req', this.uirequest.getXMLText());
		
		if(this.state.embed){
			if(document.getElementById("wait"))
				document.getElementById("wait").style.visibility="visible";
			if(document.getElementById("wait_msg"))
				document.getElementById("wait_msg").style.display="";
			document.getElementById('output').style.display = "none";
			if (this.firstload==true) document.getElementById(this.anchors.output).onload = this.onFirstPlotLoad.LASBind(this);
			document.getElementById('output').src = (this.hrefs.getProduct.url + '?xml=' + escape(this.request.getXMLText()));
			
			//alert((this.hrefs.getProduct.url + '?xml=' + escape(this.request.getXMLText())));	
		} else
			window.open(this.hrefs.getProduct.url + '?xml=' +  escape(this.request.getXMLText()));
	}
	document.getElementById('update').style.backgroundColor='';
	
		
}
/**
 * Put together and submit an LAS request
 */
LASUI.prototype.launchExternalProduct = function () {
	if(!this.updating&&this.state.view.plot!=""&&this.refs.XYSelect.enabled) {
		this.request = null;
		this.request = new LASRequest('');
		this.request.removeVariables();
		this.request.removeConstraints();		
		if(this.state.dataset==null) {return;}
		if(this.state.variables[this.state.dataset]==null) { return;}
		if(this.state.variables[this.state.dataset].length==0) { return;}
		if(this.state.view.plot==null) { return;}
		if(this.state.operation.external==null) {return;}
			
		//add the operation
		this.request.setOperation(this.state.operation.external);
			
		//set the options
		for(var p in this.state.properties.external)	
			if((typeof this.state.properties.external[p] != "function") && (typeof this.state.properties.external[p] == "object")) { 
				this.request.setProperty(this.state.properties.external[p].type, p, escape(this.state.properties.external[p].value));
			}
		this.request.setProperty("ferret","view",this.state.view.plot);
		
		this.request.removeRegion();
		//add the variables
		this.request.addVariable(this.state.dataset, this.state.variable);
			
		this.request.addRegion();	
for(var d=0;d<this.state.grid.response.grid.axis.length;d++) 
			switch(this.state.grid.response.grid.axis[d].type) {
				case 'x' : 
					this.request.addRange('x',this.refs.XYSelect.extents.selection.grid.x.min,this.refs.XYSelect.extents.selection.grid.x.max); 
					this.uirequest+="&x=" + escape("{ 'min' : " + this.refs.XYSelect.extents.selection.grid.x.min + ", 'max' : " + this.refs.XYSelect.extents.selection.grid.x.max + "}"); 
					this.uirequest+="&viewx="+escape("{ 'min' : " + this.refs.XYSelect.extents.data.grid.x.min + ", 'max' : " + this.refs.XYSelect.extents.data.grid.x.max + "}"); 

					break;
				case 'y' : 
				 	this.request.addRange('y',this.refs.XYSelect.extents.selection.grid.y.min,this.refs.XYSelect.extents.selection.grid.y.max); 
				 	this.uirequest+="&y="+escape("{ 'min' : " + this.refs.XYSelect.extents.selection.grid.y.min + ", 'max' : " + this.refs.XYSelect.extents.selection.grid.y.max + "}"); 
				 	this.uirequest+="&viewy="+escape("{ 'min' : " + this.refs.XYSelect.extents.data.grid.y.min + ", 'max' : " + this.refs.XYSelect.extents.data.grid.y.max + "}"); 
					break;
				case 't' : 
					if(this.state.view.plot.indexOf('t')>=0||this.state.variables[this.state.dataset].grid_type=="scattered") 
						if(this.state.grid.hasMenu('t')){
							this.request.addRange('t',this.refs.DW[0].value,this.refs.DW[1].value); 
							this.uirequest+="&t="+escape("{ 'min' : '" + this.refs.DW[0].value+ "', 'max' : '" + this.refs.DW[1].value + "'}");					
						} else {
							this.request.addRange('t',this.refs.DW.getDate1_Ferret(),this.refs.DW.getDate2_Ferret());
							this.uirequest+="&t="+escape("{ 'min' : '" + this.refs.DW.getDate1_Ferret()+ "', 'max' : '" + this.refs.DW.getDate2_Ferret()+ "'}");
						}
					else
						if(this.state.grid.hasMenu('t')){
							this.request.addRange('t',this.refs.DW[0].value); 
							this.uirequest+="&t=" + escape("{ 'min' : '" + this.refs.DW[0].value+ "', 'max' : '" + this.refs.DW[0].value + "'}");	
						}
						else {
							this.request.addRange('t',this.refs.DW.getDate1_Ferret());
							this.uirequest+="&t=" + escape("{ 'min' : '" + this.refs.DW.getDate1_Ferret()+ "', 'max' : '" + this.refs.DW.getDate1_Ferret()+ "'}");
						}
					break;
				case 'z' :
					if(this.state.view.plot.indexOf('z')>=0||this.state.variables[this.state.dataset].grid_type=="scattered") {
							this.request.addRange('z',this.refs.DepthWidget[this.refs.DepthWidget.widgetType][0].value,this.refs.DepthWidget[this.refs.DepthWidget.widgetType][1].value); 
							this.uirequest+="&z=" + escape("{ 'min' : '" + this.refs.DepthWidget[this.refs.DepthWidget.widgetType][0].value+ "', 'max' : '" + this.refs.DepthWidget[this.refs.DepthWidget.widgetType][1].value + "'}");		
						}
						else {
							this.request.addRange('z',this.refs.DepthWidget[this.refs.DepthWidget.widgetType][0].value); 
							this.uirequest+="&z=" + escape("{ 'min' : '" + this.refs.DepthWidget[this.refs.DepthWidget.widgetType][0].value+ "', 'max' : '" + this.refs.DepthWidget[this.refs.DepthWidget.widgetType][1].value + "'}");					
						}						
					break;
			}
		if(this.refs.constraints)		
		for(var c=0; c<this.refs.constraints.length;c++){
			switch(this.refs.constraints[c].type) {
				case 'text' :
					this.request.addTextConstraint(escape(this.refs.constraints[c].lhs.value),escape(this.refs.constraints[c].ops.value),escape(this.refs.constraints[c].rhs.value));
				break;
				case 'variable' :
					this.request.addVariableConstraint(escape(this.state.dataset), escape(this.refs.constraints[c].lhs.value),escape(this.refs.constraints[c].ops.value),escape(this.refs.constraints[c].rhs.value));
				break;
			}
		}
				

		
			window.open(this.hrefs.getProduct.url + '?xml=' +  escape(this.request.getXMLText()));
	}
}
/**
 * Method to query the server for an options object and pass json response to setOptionList
 * @param {string} optiondef Id of the option set to query the server for.
 */	
LASUI.prototype.getOptions = function (optiondef, DOMNode) {

			if(!document.all)
			var req = new XMLHttpRequest(this);
		else
			var req = new ActiveXObject("Microsoft.XMLHTTP"); 
	req.onreadystatechange = this.AJAXhandler.LASBind(this, req, "this.setOptionList(req.responseText,args[3]);",DOMNode);
	req.open("GET", this.hrefs.getOptions.url + '?opid=' + optiondef);
	req.send(null);
	

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
	this.state.properties.plot = [];		
	this.refs.options.plot.options = new LASGetOptionsResponse(response);
	var ct = this.refs.options.plot.options.getOptionCount();
	if(ct) 
		for(var i=0;i<ct;i++) {
			this.setOptionTRNode(this.refs.options.plot.options.getOptionID(i),DOMNode.TBODYNode);	
			
			switch(this.refs.options.plot.options.getOptionType(i)) {
				case "menu" : 
					if(DOMNode == this.refs.options.plot.DOMNode)			 
						this.state.properties.plot[this.refs.options.plot.options.getOptionID(i)]={"type":"ferret", "value": this.refs.options.plot.options.getOption(i).menu.item[0].values};
					else {
						this.state.properties.external[this.refs.options.plot.options.getOptionID(i)]={"type":"ferret", "value": this.refs.options.plot.options.getOption(i).menu.item[0].values};
						   DOMNode.style.display="";					
					}					
					break;
				case "text":
					if(DOMNode == this.refs.options.plot.DOMNode)
						this.state.properties.plot[this.refs.options.plot.options.getOptionID(i)]={"type":"ferret", "value":""};
					else {
						this.state.properties.external[this.refs.options.plot.options.getOptionID(i)]={"type":"ferret", "value":""};
						   DOMNode.style.display="";
					}					
					break;
			}
		}
	if(this.autoupdate || this.submitOnLoad)
		this.makeRequest();
	else
		this.showUpdateLink();

	this.submitOnLoad = false;
}
/** 
 * Method to create an option tree node and add it to the DOM
 * @param {string} id An option id	
 */
LASUI.prototype.setOptionTRNode = function (id,TBODYNode) {
	if(!this.refs.options.plot.cache)
		this.refs.options.plot.cache = {};	
	if(!this.refs.options.plot.cache[id])
	{
		this.refs.options.plot.cache[id] =  this.refs.options.plot.options.getOptionByID(id);

		this.refs.options.plot.cache[id].TRNode = document.createElement("TR");	
		var TD1 = document.createElement("TD");
		TD1.width="45%";
		TD1.innerHTML =this.refs.options.plot.cache[id].title
		var TD2 = document.createElement("TD");
		if(this.refs.options.plot.cache[id].menu) {
			this.refs.options.plot.cache[id].SELECTNode = document.createElement("SELECT");
			this.refs.options.plot.cache[id].SELECTNode.setAttribute('name', id);
  			for (var i=0;i<this.refs.options.plot.cache[id].menu.item.length;i++) {
   			var option = document.createElement("OPTION");
     			option.value=this.refs.options.plot.cache[id].menu.item[i].values;
     			option.text=this.refs.options.plot.cache[id].menu.item[i].content;
    			//code branch for add() method differences between IE and FF
     			try {this.refs.options.plot.cache[id].SELECTNode.add(option);}
     			catch(err) {this.refs.options.plot.cache[id].SELECTNode.add(option,null);}
     		}
			TD2.appendChild(this.refs.options.plot.cache[id].SELECTNode);
		} else {			
			this.refs.options.plot.cache[id].INPUTNode = document.createElement("INPUT");
			this.refs.options.plot.cache[id].INPUTNode.type = "text";
			this.refs.options.plot.cache[id].INPUTNode.className="LASTextInputNode";
			TD2.appendChild(this.refs.options.plot.cache[id].INPUTNode);
		}
		var TD3 = document.createElement("TD");		
		var A = document.createElement("A");
		A.innerHTML = "<img src='images/icon_info.gif'>";
		A.onclick = this.showOptionInfo.LASBind(this,this.refs.options.plot.cache[id].help);
		TD3.appendChild(A);
		this.refs.options.plot.cache[id].TRNode.appendChild(TD1);	
		this.refs.options.plot.cache[id].TRNode.appendChild(TD2);
		this.refs.options.plot.cache[id].TRNode.appendChild(TD3);
	} 	

	if(TBODYNode == this.refs.options.plot.DOMNode.TBODYNode)	{
		if(this.refs.options.plot.cache[id].SELECTNode)		
			this.refs.options.plot.cache[id].SELECTNode.onchange = this.setOption.LASBind(this,id,"properties", this.refs.options.plot.cache[id]);
		if(this.refs.options.plot.cache[id].INPUTNode)
			this.refs.options.plot.cache[id].INPUTNode.onchange = this.setOption.LASBind(this,id,"properties", this.refs.options.plot.cache[id]);
		TBODYNode.appendChild(this.refs.options.plot.cache[id].TRNode);	//first time, add it to the product
	}
	else {
		var obj = this.refs.options.plot.cache[id];
		if(obj.SELECTNode)		//second time, we need a copy
			obj.SELECTNode.onchange = this.setOption.LASBind(this,id,"externalproperties",obj);
		if(obj.INPUTNode)		
			obj.INPUTNode.onchange = this.setOption.LASBind(this,id,"externalproperties",obj);
		TBODYNode.appendChild(obj.TRNode.cloneNode(true));	
	}
}
LASUI.prototype.showOptionInfo = function(evt) {
	var div = document.createElement("DIV");
	var close = document.createElement("INPUT");
	var center = document.createElement("CENTER");

	close.type = "submit";
	close.onclick = this.hideOptionInfo.LASBind(this,div);
	close.name = "Close";
	close.value = "Close";
	div.innerHTML += arguments[1];
	div.className = "LASPopupDIVNode";
	div.style.left = evt.clientLeft + 20;
	div.style.top = evt.clientTop + 20;
	
	center.appendChild(close);
	div.appendChild(center);
	document.body.appendChild(div);
}
LASUI.prototype.hideOptionInfo = function () {
	var div = arguments[1];
	div.parentNode.removeChild(div);
}
/**
 * Event handler to respond to option changes
 * @param {object} evt The event object
 *	@param {object} arguments Arguments added with function.prototype.LASBind<br>
 *			this -- context<br>
 *			id -- option id
 */
LASUI.prototype.setOption = function (evt) {
	var args = arguments;
	var id = args[1];
	if(args[3].SELECTNode)
		this.state[args[2]][id]={"type" : "ferret", "value" : evt.target.options[evt.target.selectedIndex].value};
	else
		this.state[args[2]][id]={"type" : "ferret", "value" : evt.target.value};
	if(this.autoupdate&&args[2]=="properties")
		this.makeRequest();
	else
		this.showUpdateLink();
}
/**
 * initMap()
 * Method to initialize the mapwidget
 * @param {object} mapid The id of the map container object in the DOM
 */
LASUI.prototype.initMap = function (mapid) {
  
  var args = {
  				  'DOMNode' : document.getElementById(mapid),
  				  'ondraw' : this.displayCoords.LASBind(this),
  				  'onafterdraw' : this.onafterdraw.LASBind(this),
  				  'plot_area' : {
  				  		'offX' : 0,
  				  		'offY' : 0,
  				  		'width' : 200,
  				  		'height' : 100
  				  },
  				  'img' : {
  				  		'src' : '',
  				  		'width' : 200,
  				  		'height' :100,
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
 * Event handler to be attached to the MapWidget onafterdraw function
 * @param {object} evt The event object		
 */
LASUI.prototype.onafterdraw = function (evt) {
	
	if(this.autoupdate) {
		
		this.makeRequest();
	}	else
		this.showUpdateLink();

}
/**
 * Event handler called on depth widget/menu changes
 * @param {object} evt The event object
 */
LASUI.prototype.handleDepthRangeChange = function (evt) {
	
	if(this.autoupdate) {
		
		this.makeRequest();
				
	}	else
		this.showUpdateLink();

}
/**
 * Event handler called on depth widget/menu changes
 * @param {object} evt The event object		
 */
LASUI.prototype.handleDepthChange = function (evt) {
	
	if(this.autoupdate)
		this.makeRequest();
	else
		this.showUpdateLink();

}
/**
 * Event handler called on date range widget/menu changes
 * @params {object} evt The event object		
 */
LASUI.prototype.handleDateRangeChange = function (evt) {
	
	if(this.autoupdate)
		this.makeRequest();
	else
		this.showUpdateLink();

}
/**
 * Event handler called on date widget/menu changes
 * @params {object} evt The event object		
 */
LASUI.prototype.handleDateChange = function (evt) {
		
	if(this.autoupdate)
		this.makeRequest();
	else
		this.showUpdateLink();

}
/**
 * Event handler to collapse all root nodes in the tree
 * @param {object} evt The event object
 * @param {object} arguments Arguements added with function.prototype.LASBind<br>
 * 	this -- the LASUI context<br> 
 *		node -- tree nod reference in this.refs	
 */
LASUI.prototype.collapseRootNodes = function (evt) {
	var args = arguments;
	if(!this.refs[args[1]].isExpanded) {
		this.expand(this.refs[args[1]]);
		if(args[1]!="categories") this.collapse(this.refs.categories);
		if(args[1]!="views") this.collapse(this.refs.views);
		if(args[1]!="operations") this.collapse(this.refs.operations.plot);
		if(args[1]!="options") this.collapse(this.refs.options);
	} else
		this.collapse(this.refs[args[1]]);
}
LASUI.prototype.showAnalysis = function () {
	this.refs.analysis.enabled = true;
	var reset=false;
	//this.initXYSelect('xy',reset)
	for(var a in this.refs.analysis.axes)
		this.refs.analysis.axes[a].style.display="none";
	//turn on the "area" analysis switch	
	if(this.state.grid.hasAxis('x')&&this.state.grid.hasAxis('y'))
		this.refs.analysis.axes.xy.style.display="";
	//turn on the other axes switches
	for(var d=0;d<this.state.grid.response.grid.axis.length;d++) {
		eval("this.init" + this.state.grid.response.grid.axis[d].type.toUpperCase() + "Constraint('range',reset)");
		this.refs.analysis.axes[this.state.grid.response.grid.axis[d].type.toLowerCase()].style.display="";
		

		if(this.state.view.plot.indexOf(this.state.grid.response.grid.axis[d].type.toLowerCase())<0&&!this.state.analysis.axes[this.state.grid.response.grid.axis[d].type.toLowerCase()]) {
			switch(this.state.grid.response.grid.axis[d].type.toLowerCase()) {
				case 'z': this.refs.DepthWidget[this.refs.DepthWidget.widgetType][1].disabled = true; break;
				case 't': if(!this.refs.DW.widgetType) 
						this.refs.DW[1].disabled = true;
					  else	    
						this.refs.DW.disable('hi');
					  break;
			
			} 
		} else {
switch(this.state.grid.response.grid.axis[d].type.toLowerCase()) {
				case 'z': this.refs.DepthWidget[this.refs.DepthWidget.widgetType][1].disabled = false; break;
				case 't': if(!this.refs.DW.widgetType) 
						this.refs.DW[1].disabled = false;
					  else	    
						this.refs.DW.enable('hi');
					  break;
			
			}
		} 
	}

	document.getElementById(this.anchors.analysis).style.display="";
	if(this.state.analysis.axes.x&&this.state.analysis.axes.y)
		this.selectAnalysisAxis(null,"xy",true);
	else
		for(var a in this.state.analysis.axes)
			this.selectAnalysisAxis(null,a,true);

	if(this.state.analysis.type)
		this.selectAnalysisType(null,this.state.analysis.type,true);

}
LASUI.prototype.hideAnalysis = function () {
	this.refs.analysis.enabled = false;
	var reset = false;
	document.getElementById(this.anchors.analysis).style.display="none";
	for(var d=0;d<this.state.grid.response.grid.axis.length;d++) 
		if(this.state.view.plot.indexOf(this.state.grid.response.grid.axis[d].type.toLowerCase())<0) 
			switch(this.state.grid.response.grid.axis[d].type.toLowerCase()) {
				case 'x': break;
				case 'y': break;
				case 'z': this.refs.DepthWidget[this.refs.DepthWidget.widgetType][1].disabled = false; break;
				case 't': if(!this.refs.DW.widgetType) 
						this.refs.DW[1].disabled = false;
					  else	    
						this.refs.DW.enable('hi')
					  break;
			
			}



	this.updateConstraints(this.state.view.plot);
	if(this.autoupdate)
		this.makeRequest();
	else
		this.showUpdateLink();	
}
LASUI.prototype.selectAnalysisType = function (evt) {
	if(evt) {
		this.state.analysis.type = evt.target.options[evt.target.selectedIndex].value;
		this.state.analysis.name = evt.target.options[evt.target.selectedIndex].innerHTML;			
	} 

	
	if(this.state.analysis.axes.x && this.state.analysis.axes.y)
		this.selectAnalysisAxis(null,"xy", true);
	else
		for(var a in this.state.analysis.axes)
			if(this.state.grid.hasAxis(a)) 
				this.selectAnalysisAxis(null,a, true);			
	


	if(this.autoupdate)
		this.makeRequest();
	else
		this.showUpdateLink();
}
LASUI.prototype.selectAnalysisAxis = function (evt) {
	var axes =arguments[1];
	if(axes=={}|| axes==null){
		try {axes =evt.target.value;}
		catch (e) {axes = ""};
	}
	//turn all axes off
	for(var a in this.state.analysis.axes) {
			//remove the analysis axis from the widget view
			if(this.state.view.widgets.indexOf(a)>=0)//&&this.state.view.plot.indexOf(arguments[1])<0)			
				this.state.view.widgets=this.state.view.widgets.substr(0,this.state.view.widgets.indexOf(a))+this.state.view.widgets.substr(this.state.view.widgets.indexOf(a)+1,this.state.view.widgets.length);
	}
	this.state.analysis.axes = {};

	if(arguments[2]==true||evt)
	{
		
		var changeVis= false;
		//turn the analysis axis on
		for(var i=0; i< axes.length; i++) {
			this.state.analysis.axes[axes[i]] = true;//this.refs.analysis.type.op.value;
			//
			if(this.state.view.plot.indexOf(axes[i])>=0&&this.state.analysis.type && this.state.analysis.name != "None")
				changeVis = true;
			
			if(this.state.view.widgets.indexOf(axes[i])<0&&this.state.analysis.type && this.state.analysis.name != "None")			
				this.state.view.widgets+=axes[i];
		}	
	

		
	} else {
		//turning the analysis axis off

	}
	if(this.state.analysis.type && this.state.analysis.name != "None")
		this.updateConstraints(this.state.view.widgets);
	else
		this.updateConstraints(this.state.view.plot);
	if(changeVis)
		this.setVisualization(axes);

	if(this.autoupdate)
		this.makeRequest();
	else
		this.showUpdateLink();
					
}
LASUI.prototype.setVisualization = function (d) {
	
	var stop = false;
	for(var t in this.products)
		for (var p in this.products[t])
			if(this.products[t][p].view.indexOf(d)<0 && !stop)
				if(this.refs.operations.plot.children[p])
					if(this.refs.operations.plot.children[p].radio){ 
						this.refs.operations.plot.children[p].radio.checked = true;
						this.refs.operations.plot.children[p].radio.onclick();
						stop = true;
					}	
	this.refs.analysis.axes[d].selected="true";
}	
/**
 * Method to collapse a tree node
 * @param {object} obj Object reference in this.refs
 */
LASUI.prototype.collapse = function (obj) {
		if(obj.ULNode) obj.ULNode.style.display = "none";this.state.view.widgets
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
