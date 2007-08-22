
	dojo.require("dojo.debug.console");
	dojo.require("dojo.lang.*");
	dojo.require("dojo.io.*");
	dojo.require("dojo.event.*");
	//dojo.require("dojo.undo.browser");
	
 //	var djConfig = new Object;
 //	djConfig.isDebug          = false;//true;
 //	djConfig.debugAtAllCosts  = false; //true;
 //	djConfig.debugContainerId = "_dojoDebugConsole";
 //	djConfig.baseScriptUri = "dojo/dojo.js";
	
	if ( ! Function.prototype.bindAsEventListener ) {
    Function.prototype.bindAsEventListener = function(object) {
        var __method = this;
        return function(event) {
            __method.call(object, event || window.event);
        };
    };
}
 
	  			  		  			  		
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
							"variables" : [],
							"operation" : null,
							"properties" : [],
							"view" : "",
							"embed" : true
						 };
					
		//DOM anchors for stuff
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
		this.qs = new Querystring();
		
		if(this.qs.params.dsid) {
			this.state.dataset = this.qs.params.dsid;
			if(this.qs.params.varid)
				this.state.variables[this.qs.params.dsid] = this.qs.params.varid.split(",");
		}
		if(this.qs.params.view) 
			this.state.view = this.qs.params.view;
		
		if(this.qs.params.opid) 
			this.state.operation = this.qs.params.opid;
		
		if(this.qs.params.dsid&&this.qs.params.varid) 
			this.submitOnLoad = true;
		else
			this.submitOnLoad = false;
	
	}
											
	LASUI.prototype.initUI = function ()
	{
		this.initDojoTree(this.anchors.tree);
		
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
		if(this.qs.params.debug) 
			this.showDebugWindow();
	}
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
	
	/*
	 * function to initialize the control tree
	 * parameters:
	 * 	anchorId - id of the page element to build the tree inside.
	 */
	LASUI.prototype.initDojoTree = function (anchorId)
	{
		
		// create 1st level tree and nodes.
		var _tree = document.createElement("UL");//dojo.widget.createWidget("Tree");
	   _tree.style.paddingLeft ="0px";
	   _tree.style.marginLeft ="0px";
	   
		dojo.byId(anchorId).appendChild(_tree);
		
		var _rootNodes  = [
									{"title" : "Categories","isFolder" : "true", "id" : "categories"},
									{"title" : "Views","isFolder" : "true", "id" : "views", "onTreeClick" : "function"},
									{"title" : "Output Types", "isFolder" : "true", "id" : "operations"},
									{"title" : "Output Options", "isFolder" : "true", "id" : "options"}
								];

		for(var i=0;i<_rootNodes.length;i++) {
			this.refs[_rootNodes[i].id] = {};
			this.refs[_rootNodes[i].id].LINode = document.createElement("LI");//dojo.widget.createWidget("TreeNode");
			this.refs[_rootNodes[i].id].title = document.createElement("SPAN");
			this.refs[_rootNodes[i].id].IMGNode = document.createElement("IMG"); 
			this.refs[_rootNodes[i].id].title.onclick = this.collapseRootNodes.bindAsEventListener(this,  _rootNodes[i].id);
			this.refs[_rootNodes[i].id].title.innerHTML = _rootNodes[i].title;
			this.refs[_rootNodes[i].id].title.style.cursor = "pointer";
			this.refs[_rootNodes[i].id].title.style.cursor = "hand";
			this.refs[_rootNodes[i].id].title.style.verticalAlign = "middle";
			this.refs[_rootNodes[i].id].IMGNode.src = "http://www.mattkruse.com/javascript/mktree/plus.gif";
			this.refs[_rootNodes[i].id].IMGNode.style.cursor = "pointer";
			this.refs[_rootNodes[i].id].IMGNode.style.cursor = "hand";
			this.refs[_rootNodes[i].id].IMGNode.style.verticalAlign = "middle";
			this.refs[_rootNodes[i].id].IMGNode.onclick = this.collapseRootNodes.bindAsEventListener(this,  _rootNodes[i].id);
			
			this.refs[_rootNodes[i].id].LINode.style.listStyleType = "none";
			this.refs[_rootNodes[i].id].LINode.style.padding = "0";
			this.refs[_rootNodes[i].id].LINode.style.margin = "-4pt";
			
			if(_rootNodes[i].id == "options")
				this.refs[_rootNodes[i].id].ULNode = document.createElement("DIV");
			else
				this.refs[_rootNodes[i].id].ULNode = document.createElement("UL");
			
			this.refs[_rootNodes[i].id].ULNode.style.display="none";
			this.refs[_rootNodes[i].id].ULNode.style.marginLeft="6pt";
			this.refs[_rootNodes[i].id].ULNode.style.paddingLeft="6pt";
			this.refs[_rootNodes[i].id].ULNode.style.marginTop="0";
			this.refs[_rootNodes[i].id].ULNode.style.paddingTop="0";
			this.refs[_rootNodes[i].id].ULNode.style.marginBottom="0";
			this.refs[_rootNodes[i].id].ULNode.style.paddingBottom="0";
			
			
			
			
			this.refs[_rootNodes[i].id].LINode.appendChild(this.refs[_rootNodes[i].id].IMGNode);
			this.refs[_rootNodes[i].id].LINode.appendChild(this.refs[_rootNodes[i].id].title);
			this.refs[_rootNodes[i].id].LINode.appendChild(this.refs[_rootNodes[i].id].ULNode);
			this.refs[_rootNodes[i].id].isExpanded = false;
			_tree.appendChild(this.refs[_rootNodes[i].id].LINode); 
		}
	
		
			//get the datasets
		
		var _bindArgs = {	
								url: this.hrefs.getCategories.url,
								mimetype: "text/plain",
								sync : false,
								error: function(type,error) {alert('initDojoTree AJAX error.' + error.type + ' ' + error.message);},
								load:	dojo.lang.hitch(this, (function(type,data,event) {this.setCategoryTreeNode(data,this.refs.categories,"categories"); }))
						};
		var _request = dojo.io.bind(_bindArgs);
			this.expand(this.refs.categories);	
	}
		/*
	 * function to load a UI category tree node from a json response
	 *  parameters: 
	 *   strJson - json string from server
	 *   parentNode = parent Node in this.refs
	 */
	LASUI.prototype.setCategoryTreeNode = function (strJson, node, id) {
		var response = eval("(" + strJson + ")");

		node.category = new LASGetCategoriesResponse(response);
					
		if(node.category.getCategoryType()=="category")
			node.children=[];
		for(var i=0; i<node.category.getCategorySize();i++)
			this.setCategoryTreeSubNode(node, i,id);
		
		
	}
	/*
	 * function to load a UI category tree node from a json response
	 *  parameters: 
	 *   strJson - json string from server
	 *   parentNode = parent Node in this.refs
	 */
	LASUI.prototype.setCategoryTreeSubNode = function (node, i, id) {
		
		switch(node.category.getCategoryType()) {
			case "category":
				this.createCategoryTreeNode(node,i,id); break;
			case "dataset":
				this.createVariableTreeNode(node,i);break;
		}
	}
	LASUI.prototype.createCategoryTreeNode = function (node, i, id) {
		
		if(node==this.refs.categories && node.category.getChildChildrenType(i)=="variables")
					this.refs.categories.title.innerHTML="Datasets";
	
		node.children[i] = {};
		node.children[i].LINode = document.createElement("LI"); //dojo.widget.createWidget("TreeNode");	
		node.children[i].LINode.style.padding = "0";
		node.children[i].LINode.style.margin = "-4pt";
		
		node.children[i].IMGNode =  document.createElement("IMG");
		node.children[i].IMGNode.onclick = this.selectCategory.bindAsEventListener(this, node, i);
		node.children[i].IMGNode.style.cursor = "pointer";
		node.children[i].IMGNode.style.cursor = "hand";
		//node.children[i].IMGNode.style.verticalAlign = "middle";
		node.children[i].IMGNode.src = "http://www.mattkruse.com/javascript/mktree/plus.gif";
		node.children[i].LINode.style.listStyleType = "none";
		node.children[i].isExpanded = false;

		node.children[i].ULNode = document.createElement("ul");
		node.children[i].ULNode.style.marginLeft = "6pt";
		node.children[i].ULNode.style.paddingLeft = "6pt";
	   node.children[i].ULNode.style.marginTop = "0";
		node.children[i].ULNode.style.paddingTop = "0";
	   node.children[i].ULNode.style.marginBottom = "0";
		node.children[i].ULNode.style.paddingBottom = "0";		
		var table = document.createElement("TABLE");
		table.cellpadding = "0";
		table.cellspacing = "0";
		table.style.padding=0;
		table.style.margin=0;
		var tbody = document.createElement("TBODY");
		tbody.style.padding=0;
		tbody.style.margin=0;
		var tr = document.createElement("TR");
		tr.style.padding=0;
		tr.style.margin=0;
		var td1 = document.createElement("TD");
		td1.style.verticalAlign="top";
		td1.appendChild(node.children[i].IMGNode);
		td1.style.padding=0;
		td1.style.margin=0;		
		var td2 = document.createElement("TD");
		td2.style.verticalAlign = "top";
		td2.onclick = this.selectCategory.bindAsEventListener(this, node, i);
		td2.innerHTML = node.category.getChildName(i);
		td2.style.cursor = "pointer";
		td2.style.cursor = "hand";
		td2.style.padding=0;
		td2.style.margin=0;
		
		
		td2.halign = "left";
		tr.appendChild(td1);
		tr.appendChild(td2);
		tbody.appendChild(tr);
		table.appendChild(tbody);
		
		//node.children[i].LINode.appendChild(node.children[i].IMGNode);
		node.children[i].LINode.appendChild(table);
		//node.children[i].LINode.appendChild(category_title);
		node.children[i].LINode.appendChild(node.children[i].ULNode);
		node.ULNode.appendChild(node.children[i].LINode);
		
		//if(!node.ULNode.style.display!="none") node.ULNode.style.display="";
		if(node.category.getChildType(i)=='dataset' && node.category.getChildID(i)==this.state.dataset) 
			this.getCategory(node, i);
		
	}
	LASUI.prototype.createVariableTreeNode = function (node, i) {
	
		if(!node.children)
			node.children=[];
		node.children[i] = {};
		node.children[i].LINode = document.createElement("LI");
		node.children[i].LINode.style.listStyleType = "none";
		node.children[i].LINode.style.listStyleImage = "none";
		node.children[i].LINode.style.margin ="-4pt";
		node.children[i].LINode.style.padding ="0";
		if(document.all) {
			var elem_nm = "<INPUT NAME='" + node.category.getDatasetID()+"'>";
			node.children[i].INPUTNode = document.createElement(elem_nm);
		} else {
			node.children[i].INPUTNode = document.createElement("INPUT");
			node.children[i].INPUTNode.name=node.category.getDatasetID();
		}
		node.children[i].INPUTNode.type="radio";
		node.children[i].INPUTNode.onclick=this.setVariable.bindAsEventListener(this, node, i);
		node.children[i].INPUTNode.id = node.category.getChildID(i);
		
		var table = document.createElement("TABLE");
		table.cellpadding = "0";
		table.cellspacing = "0";
		
		var tbody = document.createElement("TBODY");
		var tr = document.createElement("TR");
		var td1 = document.createElement("TD");
		td1.style.verticalAlign="top";
		td1.style.padding=0;
		td1.appendChild(node.children[i].INPUTNode);
		
		var td2 = document.createElement("TD");
		td2.style.verticalAlign = "top";
		td2.innerHTML= node.category.getChildName(i);
		td2.style.cursor = "pointer";
		td2.style.cursor = "hand";
		td2.style.padding=0;
		
		
		td2.halign = "left";
		tr.appendChild(td1);
		tr.appendChild(td2);
		tbody.appendChild(tr);
		table.appendChild(tbody);
		
		node.children[i].LINode.appendChild(table);
		
		
		if(this.state.variables && this.state.dataset)
			if(this.state.variables[this.state.dataset])
				for(var v=0;v<this.state.variables[this.state.dataset].length;v++)
					if(this.state.variables[this.state.dataset][v]==node.category.getChildID(i))
				  { 
					this.setVariable({}, node, i, true);
					node.children[i].INPUTNode.checked=true;
				}

		node.ULNode.appendChild(node.children[i].LINode);
	}
	
	LASUI.prototype.getCategory = function (parentNode, i) {
		if(!parentNode.children[i].category) {
			var _bindArgs = {	
					url: this.hrefs.getCategories.url + "?catid=" + parentNode.category.getChildID(i),
					mimetype: "text/plain",
					error: function(type,error) {alert('getCategory AJAX error.' + error.type + ' ' + error.message);},
					sync : false,
					load:	dojo.lang.hitch(this, (function(type,data,event) {this.setCategoryTreeNode(data, parentNode.children[i], parentNode.category.getChildID(i)); }))
			};
			var _request = dojo.io.bind(_bindArgs);
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
	LASUI.prototype.selectCategory = function (evt) {
		var args = $A(arguments);
		var parentNode = args[1];
		var i = args[2];		
		if(!parentNode.children[i].isExpanded) {
			for(var c=0;c< parentNode.children.length;c++)
				this.collapse(parentNode.children[c]);
			this.expand(parentNode.children[i]);	//expand the category if it has been selected 
			/*if(parentNode.children[i].category) //if the category is a dataset set it as the selected dataset
				if(parentNode.children[i].category.getCategoryType()=="dataset"){
						this.setDataset(parentNode.children[i].category.getDatasetID());
				}
			else*/
				if(parentNode.category.getChildChildrenType(i)=="variables")
					this.setDataset(parentNode.category.getChildDatasetID(i));
		} else
			this.collapse(parentNode.children[i]);

		if(!parentNode.children[i].category) {
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
		var variableLINode = dataset.children[i].LINode;
		
		if (loadVariable) {			
			//start an array of selected variables for this dataset if we havent already
			if(typeof this.state.variables[datasetID] != 'object') 
				this.state.variables[datasetID] = [];
			
			//REMOVE TO ENABLE MULTI-VARIABLE SELCTION
			this.state.variables[datasetID] = [];
		
			//if this variable has not already been selected, add it to the list for that dataset
			this.state.variables[datasetID]=this.state.variables[datasetID].without(variableID);
			this.state.variables[datasetID].push(variableID);
						
			//if we are in the selected dataset, get the grids for this dataset/variable combo
			if (this.state.dataset == datasetID) 
				this.getGrid(datasetID,variableID);				
		}	
		else if (typeof this.state.variables[datasetID] == 'object')
					 this.state.variables[datasetID]=this.state.variables[datasetID].without(variableID);
			
	}
	
	LASUI.prototype.setDataset = function (dataset) {
			this.state.dataset = dataset;
			if(typeof this.state.variables[dataset] == 'object') 
				this.getGrid(dataset, this.state.variables[dataset].last());
	}
	LASUI.prototype.setView = function (evt) {
		var args = $A(arguments)
		var view = args[1];	
		this.state.view = view;
		this.refs.operations.ULNode.innerHTML="";
		//this.refs.options.ULNode.innerHTML="";
		this.state.properties = {};	
		
		this.updateConstraints();
		this.getOperations(this.state.dataset,this.state.variables[this.state.dataset].last(),this.state.view);

	}	
	LASUI.prototype.getOperations= function (dataset, variable, view) {
	
		//populate the outputs tree with getOperations	
		var _bindArgs = {	
				url: this.hrefs.getOperations.url + '?dsid=' + dataset + '&varid=' + variable + '&view=' + view,
				mimetype: "text/plain",
				error: function(type,error) {alert('getOperations AJAX error.' + error.type + ' ' + error.message);},
				load: dojo.lang.hitch(this, (function(type,data,event) {this.setOperationList(data);})) 
			};
		var _request = dojo.io.bind(_bindArgs);
		
	}
	LASUI.prototype.setOperation = function (evt) {
		var args = $A(arguments);
		var id = args[1];
		this.state.operation=id;
		//this.refs.options.ULNode.innerHTML="";
		if(this.refs.operations.operations.getOperationByID(id).optiondef)
			this.getOptions(this.refs.operations.operations.getOperationByID(id).optiondef.IDREF);	
		else if (this.refs.operations.operations.getOperationByID(id).optionsdef)
			this.getOptions(this.refs.operations.operations.getOperationByID(id).optionsdef.IDREF);
	
	}
		
	/*
	 *  Update the views list with the allowed views
	 */	
	LASUI.prototype.setOperationList = function (strJson) {
		//clear the current view list and state
		this.refs.operations.ULNode.innerHTML="";
		var response = eval("(" + strJson + ")");
		var setDefault = true;
		this.refs.operations.operations = new LASGetOperationsResponse(response);
		for(var i=0;i<this.refs.operations.operations.getOperationCount();i++) {
			this.setOperationLINode(this.refs.operations.operations.getOperationID(i), this.refs.operations.operations.getOperationName(i));	
			if(this.refs.operations.operations.getOperationID(i)==this.state.operation) 
				setDefault = false;
		}
		if(setDefault) {
			this.state.operation=this.refs.operations.operations.getOperationID(0);
			this.refs.operations.operations.getOperationByID(this.state.operation).INPUTNode.checked=true;		
		}
		if(this.refs.operations.operations.getOperationByID(this.state.operation).optiondef)
			this.getOptions(this.refs.operations.operations.getOperationByID(this.state.operation).optiondef.IDREF);	
		else if (this.refs.operations.operations.getOperationByID(this.state.operation).optionsdef)
			this.getOptions(this.refs.operations.operations.getOperationByID(this.state.operation).optionsdef.IDREF);	

		//if(!this.refs.operations.ULNode.style.display!="none") this.refs.operations.ULNode.style.display="";
	}
	LASUI.prototype.setOperationLINode = function (id, name) {
		this.refs.operations.operations.getOperationByID(id).LINode = document.createElement("LI");	
		this.refs.operations.operations.getOperationByID(id).LINode.style.listStyleType = "none";
		var title = document.createElement("TEXT");
		title.innerHTML = name;
		this.refs.operations.operations.getOperationByID(id).INPUTNode =  document.createElement("INPUT");
				if(document.all) {
			var elem_nm = "<INPUT NAME='operations'>";
			
		this.refs.operations.operations.getOperationByID(id).INPUTNode =  document.createElement(elem_nm);
		} else {
			
		this.refs.operations.operations.getOperationByID(id).INPUTNode =   document.createElement("INPUT");
			
		this.refs.operations.operations.getOperationByID(id).INPUTNode.name="operations";
		}
		this.refs.operations.operations.getOperationByID(id).INPUTNode.type="radio";
		this.refs.operations.operations.getOperationByID(id).INPUTNode.onclick=this.setOperation.bindAsEventListener(this, id);
		if (this.state.operation == id)
			this.refs.operations.operations.getOperationByID(id).INPUTNode.checked = true;		
		this.refs.operations.operations.getOperationByID(id).INPUTNode.id = id;
		this.refs.operations.operations.getOperationByID(id).LINode.appendChild(this.refs.operations.operations.getOperationByID(id).INPUTNode);
		this.refs.operations.operations.getOperationByID(id).LINode.appendChild(title);	
		this.refs.operations.ULNode.appendChild(this.refs.operations.operations.getOperationByID(id).LINode);	
}
	LASUI.prototype.getGrid = function (dataset, variable) {
				var _bindArgs = {	
						url: this.hrefs.getGrid.url + '?dsid=' + dataset + '&varid=' + variable,
						mimetype: "text/plain",
						error: function(type,error) {alert('getGrid AJAX error.' + error.type + ' ' + error.message);},
						load: dojo.lang.hitch(this, (function(type,data,event) {this.setGrid(data);})),
						timeout: dojo.lang.hitch(this, (function() { alert("The dataset you selected is currently unavailable.")})),
						timeoutSeconds: 3 //The number of seconds to wait until firing timeout callback in case of timeout. 
				};
				var _request = dojo.io.bind(_bindArgs);
								
	}
	
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
		
		this.getViews(this.state.dataset,this.state.variables[this.state.dataset].last());
	}
	/*
	 * get the allowed views list
	 */	
	LASUI.prototype.getViews = function (dataset,variable) {
		
		var _bindArgs = {	
			url: this.hrefs.getViews.url + '?dsid=' + dataset + '&varid=' +variable,
			mimetype: "text/plain",
			error: function(type,error) {alert('getViews AJAX error.' + error.type + ' ' + error.message);},
			load: dojo.lang.hitch(this, (function(type,data,event) {this.setViewList(data);})),
			timeout: dojo.lang.hitch(this, (function() { alert("The dataset you selected is currently unavailable.")})),
			timeoutSeconds: 3 //The number of seconds to wait until firing timeout callback in case of timeout. 
		};
		var _request = dojo.io.bind(_bindArgs);
	}
	
	/*
	 *  Update the views list with the allowed views
	 */	
	LASUI.prototype.setViewList = function (strJson) {
		//clear the current view list and state
		this.refs.views.ULNode.innerHTML="";
		var response = eval("(" + strJson + ")");
		var setDefault = true;
		this.refs.views.views = new LASGetViewsResponse(response);
		for(var i=0;i<this.refs.views.views.getViewCount();i++) {
			var useView = true;
			for(var v=0;v<this.refs.views.views.getViewID(i).length;v++)
				if(!this.state.grid.hasAxis(this.refs.views.views.getViewID(i).charAt(v)))
					useView = false;
			if(useView) {
				this.setViewNode(this.refs.views.views.getViewID(i), this.refs.views.views.getViewName(i));	
				if(this.refs.views.views.getViewID(i)==this.state.view)
					setDefault = false;
			}
		}
		if(setDefault) {
			this.state.view=this.refs.views.views.getViewID(0);
			this.refs.views.views.getViewByID(this.state.view).INPUTNode.checked=true;
			
		}
		this.getOperations(this.state.dataset,this.state.variables[this.state.dataset].last(),this.state.view);
		//if(!this.refs.views.ULNode.style.display!="none") 
		//	this.refs.views.ULNode.style.display="";
		if(this.state.lastgrid.response){
			if(this.state.lastgrid.response.grid.ID!=this.state.grid.response.grid.ID)
				this.updateConstraints();
		}
		else
			this.updateConstraints();
	}
	LASUI.prototype.setViewNode = function (id, name) {
		this.refs.views.views.getViewByID(id).LINode = document.createElement("LI");
		this.refs.views.views.getViewByID(id).LINode.style.listStyleType = "none";	
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
		this.refs.views.views.getViewByID(id).INPUTNode.onclick=this.setView.bindAsEventListener(this, id);
	
		if (this.state.view == id)
			this.refs.views.views.getViewByID(id).INPUTNode.checked = true;		
		this.refs.views.views.getViewByID(id).INPUTNode.id = id;
		this.refs.views.views.getViewByID(id).LINode.appendChild(this.refs.views.views.getViewByID(id).INPUTNode);
		this.refs.views.views.getViewByID(id).LINode.appendChild(title);	
		this.refs.views.ULNode.appendChild(this.refs.views.views.getViewByID(id).LINode);	
}
	/*
	 * update the 4D Constraints selectors
	 */	
	LASUI.prototype.updateConstraints = function () {
		this.updating = true;		
		if(this.state.grid.getAxis('x') || this.state.grid.getAxis('y')) {
			if(!this.refs.XYSelect.enabled) 			
				this.refs.XYSelect.enable();
			this.initXYSelect();
		}
		
		
		document.getElementById("Date").innerHTML = "<br><br>";
		document.getElementById("Depth").innerHTML = "<br><br>";
		
		for(var d=0;d<this.state.view.length;d++)
			eval("this.init" + this.state.view.charAt(d).toUpperCase() + "Constraint('range')");
		
		for(var d=0;d<this.state.grid.response.grid.axis.length;d++) 
			if(this.state.view.indexOf(this.state.grid.response.grid.axis[d].type) < 0) 
				eval("this.init" + this.state.grid.response.grid.axis[d].type.toUpperCase() + "Constraint('point')");
		this.updating = false;
	}
	
	/*
	* Initialize the XY select widget to the grid
	*/
	LASUI.prototype.initXYSelect = function () {	
		if(this.refs.XYSelect && this.state.view)
			this.refs.XYSelect.enable();
		if(this.state.grid.getAxis('x') && this.state.grid.getAxis('y') && this.state.view)	 {

			var bbox = {"x": {"min" : 0, "max" :0}, "y" : {"min" :0, "max" : 0}};

			if(this.state.grid.hasArange('x')) {
				bbox.x.min = parseFloat(this.state.grid.getLo('x'));
				bbox.x.max = parseFloat(this.state.grid.getHi('x'));
			}	
			if(this.state.grid.hasArange('y')) {
				bbox.y.min = parseFloat(this.state.grid.getLo('y'));
				bbox.y.max = parseFloat(this.state.grid.getHi('y'));
			}
				
			this.refs.XYSelect.recenterOnDataBBox(bbox);
			this.refs.XYSelect.setSelectionGridBBox(bbox);
			if(this.state.view.indexOf('x')>=0 && this.state.view.indexOf('y')>=0) 
				this.refs.XYSelect.setView("xy");
			else if(this.state.view.indexOf('x')>=0 && this.state.view.indexOf('y')<0)
				this.refs.XYSelect.setView("x");
			else if(this.state.view.indexOf('x')<0 && this.state.view.indexOf('y')>=0)
				this.refs.XYSelect.setView("y");
			else if(this.state.view.indexOf('x')<0 && this.state.view.indexOf('y')<0)
				this.refs.XYSelect.setView("point");
			}
	}
	/*
	* Initialize an X grid control
	*/
	LASUI.prototype.initXConstraint = function (mode) {		
		
	}
	/*
	* Initialize an Y grid control
	*/
	LASUI.prototype.initYConstraint = function (mode) {		
	}
	/*
	* Initialize an Z grid control
	*/
	LASUI.prototype.initZConstraint = function (mode) {	
		document.getElementById("Depth").style.innerHTML="<br><br>";
		if(this.state.grid.hasMenu('z')) {			
			switch (mode) {
				case 'range':
					this.refs.DepthSelect = [document.createElement("SELECT"),document.createElement("SELECT")];
					for(var m=0;m<this.refs.DepthSelect.length;m++) {
						this.refs.DepthSelect[m].onchange=this.handleDepthRangeChange.bindAsEventListener(this);
						for(var v=0;v<this.state.grid.getMenu('z').length;v++) {
							var _opt = document.createElement("OPTION");
							_opt.value = this.state.grid.getMenu('z')[v][1];
							_opt.innerHTML=this.state.grid.getMenu('z')[v][0];
							if(m==1 && v >= this.state.grid.getMenu('z').length-1)
								_opt.selected=true;
							if(m==0 && v == 0)
								_opt.selected=true;
							this.refs.DepthSelect[m].appendChild(_opt);
						}
					}
					document.getElementById("Depth").innerHTML="<strong>Depth (" + this.state.grid.getAxis('z').units +")</strong><br><strong>Minimum : </strong>";	
					document.getElementById("Depth").appendChild(this.refs.DepthSelect[0]);
					document.getElementById("Depth").innerHTML+= "<br><strong>Maximum : </strong>";
					document.getElementById("Depth").appendChild(this.refs.DepthSelect[1]);
					document.getElementById("Depth").style.display="";
					break;
				case 'point':
					this.refs.DepthSelect = [document.createElement("SELECT")];
					this.refs.DepthSelect.onchange=this.handleDepthChange.bindAsEventListener(this);
					for(var v=0;v<this.state.grid.getMenu('z').length;v++) {
						var _opt = document.createElement("OPTION");
						_opt.value = this.state.grid.getMenu('z')[v][1];
						_opt.innerHTML=this.state.grid.getMenu('z')[v][0];
						this.refs.DepthSelect[0].appendChild(_opt);
					}
					var depth_label = document.createElement("STRONG");
					depth_label.innerHTML="Depth (" + this.state.grid.getAxis('z').units + ") : ";
					document.getElementById("Depth").innerHTML="<br>";					
					document.getElementById("Depth").appendChild(depth_label);	
					document.getElementById("Depth").appendChild(this.refs.DepthSelect[0]);
					document.getElementById("Depth").style.display="";
					break;
			}
		}
		if(this.state.grid.hasArange('z')){
			switch (mode) {
				case 'range':
					this.refs.DepthSelect = [document.createElement("SELECT"),document.createElement("SELECT")];
					for(var m=0;m<this.refs.DepthSelect.length;m++) {
						this.refs.DepthSelect[m].onchange=this.handleDepthRangeChange.bindAsEventListener(this);
						for(var v=parseFloat(this.state.grid.getLo('z'));v<=parseFloat(this.state.grid.getHi('z'));v+=parseFloat(this.state.grid.getDelta('z'))) {
							var _opt = document.createElement("OPTION");
							_opt.value = v;
							_opt.innerHTML=v;
							if(m==1 && v == this.state.grid.getHi('z'))
								_opt.selected=true;
							if(m==0 && v == this.state.grid.getLo('z'))
								_opt.selected=true;
							this.refs.DepthSelect[m].appendChild(_opt);
						}
					}
					document.getElementById("Depth").innerHTML="<strong>Depth (" + this.state.grid.getAxis('z').units +")</strong><br><strong>Minimum : </strong>";	
					document.getElementById("Depth").appendChild(this.refs.DepthSelect[0]);
					document.getElementById("Depth").innerHTML+= "<br><strong>Maximum : </strong>";
					document.getElementById("Depth").appendChild(this.refs.DepthSelect[1]);
					document.getElementById("Depth").style.display="";
					break;
				case 'point':
					this.refs.DepthSelect = [document.createElement("SELECT")];
					this.refs.DepthSelect.onchange=this.handleDepthChange.bindAsEventListener(this);
					for(var v=parseFloat(this.state.grid.getLo('z'));v<=parseFloat(this.state.grid.getHi('z'));v+=parseFloat(this.state.grid.getDelta('z'))) {
						var _opt = document.createElement("OPTION");
						_opt.value = v;
						_opt.innerHTML=v;
						this.refs.DepthSelect[0].appendChild(_opt);
					}
					document.getElementById("Depth").innerHTML="<strong>Depth (" + this.state.grid.getAxis('z').units + ") : </strong>";	
					document.getElementById("Depth").appendChild(this.refs.DepthSelect[0]);
					document.getElementById("Depth").style.display="";
					break;
			}
		}
	}
	/*
	* Initialize an T grid control
	*/
	LASUI.prototype.initTConstraint = function (mode) {
		
		document.getElementById("Date").innerHTML="<br><br>";
		switch(this.state.grid.getDisplayType('t')) {
			case "widget":	
				switch(mode) {
					case 'range': 
						document.getElementById("Date").style.display="";
						this.refs.DW = new DateWidget(this.state.grid.getLo('t'),this.state.grid.getHi('t')); 
						this.refs.DW.render("Date","MDY","MDY");
						document.getElementById("Date").firstChild.align="center";
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
						this.refs.DW.render("Date","MDY");
						document.getElementById("Date").firstChild.align="center";						
						document.getElementById("Date").style.display="";
						var label = document.createElement('strong');
						label.innerHTML="<br>Date : ";
						document.getElementById("DW_td1").insertBefore(label,document.getElementById("DW_td1").firstChild);
						break;
				}	
				break;
			case "menu": 
				switch(mode) {
					case 'range': 
						document.getElementById("Date").style.display="";
						this.refs.DW = [document.createElement("SELECT"),document.createElement("SELECT")];
						for(var m=0;m<this.refs.DW.length;m++) {
							//this.refs.DW[m].id = "DW" + m;
							this.refs.DW[m].onchange=this.handleDateRangeChange.bindAsEventListener(this);
							for(var v=0;v<this.state.grid.getMenu('t').length;v++) {
								var _opt = document.createElement("OPTION");
								_opt.value = this.state.grid.getMenu('t')[v][1];
								
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
						for(var v=0;v<this.state.grid.getMenu('t').length;v++) {
							var _opt = document.createElement("OPTION");
							_opt.value = this.state.grid.getMenu('t')[v][1];
							_opt.id = "date";
							_opt.innerHTML=this.state.grid.getMenu('t')[v][0];
							this.refs.DW[0].appendChild(_opt);
						}
						document.getElementById("Date").innerHTML="<br><strong>Date : </strong>";	
						document.getElementById("Date").appendChild(this.refs.DW[0]);
						document.getElementById("Date").style.display="";
						break;
				}
				break;
		 }				
	}			
	LASUI.prototype.setConstraint = function (axis,type,value) {
		switch(axis){}
			
	}
	/*
	 * Put together and submit an LAS request
	 */
	LASUI.prototype.makeRequest = function () {
		if(!this.updating) {
			this.request = null;
			this.request = new LASRequest('');
			this.request.removeVariables();
			this.request.removeConstraints();
			
			if(this.state.dataset==null) {alert("Please select a dataset and variables."); return;}
			if(this.state.variables[this.state.dataset]==null) {alert("Please select variables in the selected dataset."); return;}
			if(this.state.variables[this.state.dataset].length==0) {alert("Please select variables in the selected dataset."); return;}
			if(this.state.view==null) {alert("Please select a view."); return;}
			if(this.state.operation==null) {alert("Please select an output."); return;}
			
			//add the operation
			this.request.setOperation(this.state.operation);
			
			//set the options
			for(var p in this.state.properties)	
				if((typeof this.state.properties[p] != "function") && (typeof this.state.properties[p] == "object")) { 
					this.request.setProperty(this.state.properties[p].type, p, escape(this.state.properties[p].value));
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
						if(this.state.view.indexOf('t')>=0) 
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
				if(typeof this.state.variables[this.state.dataset][v] != "function" && typeof this.state.variables[this.state.dataset][v] =="string")
					this.request.addVariable(this.state.dataset, this.state.variables[this.state.dataset][v]);
			
				
			if(this.state.embed)
				document.getElementById('output').src = (this.hrefs.getProduct.url + '?xml=' + escape(this.request.getXMLText()));
			else
				window.open(this.hrefs.getProduct.url + '?xml=' +  escape(this.request.getXMLText()));
		}
	}
	
	
		LASUI.prototype.getOptions= function (optiondef) {
	
		//populate the outputs tree with getOperations	
		var _bindArgs = {	
				url: this.hrefs.getOptions.url + '?opid=' + optiondef,
				mimetype: "text/plain",
				error: function(type,error) {alert('getOptions AJAX error.' + error.type + ' ' + error.message);},
				load: dojo.lang.hitch(this, (function(type,data,event) {this.setOptionList(data);})) 
			};
		var _request = dojo.io.bind(_bindArgs);
		
	}
		
	/*
	 *  Update the options lis
	 */	
	LASUI.prototype.setOptionList = function (strJson) {
		//clear the current view list and state
		
		this.refs.options.ULNode.innerHTML = ""; 
		var table = document.createElement("TABLE");
		table.style.margin = "-4pt";
		table.style.marginLeft = "6pt";
		table.cellpadding = "0";
		table.cellspacing = "0";
		this.refs.options.TBODYNode = document.createElement("TBODY");
		table.appendChild(this.refs.options.TBODYNode);
		this.refs.options.ULNode.appendChild(table);
		

		
		var response = eval("(" + strJson + ")");
		
		var setDefault = true;
		this.state.properties = [];		
		this.refs.options.options = new LASGetOptionsResponse(response);
		var ct = this.refs.options.options.getOptionCount();
		if(ct) 
		for(var i=0;i<ct;i++) {
			this.setOptionTRNode(this.refs.options.options.getOptionID(i));	
			switch(this.refs.options.options.getOptionType(i)) {
				case "menu" : 
					this.state.properties[this.refs.options.options.getOptionID(i)]={"type":"ferret", "value":this.refs.options.options.getOption(i).menu.item[0].values};
					break;
				case "text":
					this.state.properties[this.refs.options.options.getOptionID(i)]={"type":"ferret", "value":""};
					break;
			}
		}
		
		if(this.autoupdate || this.submitOnLoad)
			this.makeRequest();
		
		this.submitOnLoad = false;

		//if(!this.refs.options.ULNode.style.display!="none") this.refs.options.ULNode.style.display="";
	}
	LASUI.prototype.setOptionTRNode = function (id) {
		this.refs.options.options.getOptionByID(id).TRNode = document.createElement("TR");	
		var TD1 = document.createElement("TD");
		TD1.width="45%";
		TD1.innerHTML = this.refs.options.options.getOptionByID(id).title
		if(this.refs.options.options.getOptionByID(id).menu) {
			var obj = document.createElement("SELECT");
			obj.setAttribute('name', id);
     		for (var i=0;i<this.refs.options.options.getOptionByID(id).menu.item.length;i++) {
     			var option = document.createElement("OPTION");
     			option.value=this.refs.options.options.getOptionByID(id).menu.item[i].values;
     			option.text=this.refs.options.options.getOptionByID(id).menu.item[i].content;
    			//code branch for add() method differences between IE and FF
     			try {obj.add(option);}
     			catch(err) {obj.add(option,null);}
     		}
		} else {			
			var obj = document.createElement("INPUT");
			obj.type = "text";
		}	
		obj.onchange = this.setOption.bindAsEventListener(this,id);
		var TD2 = document.createElement("TD");
		TD2.appendChild(obj);
		this.refs.options.options.getOptionByID(id).TRNode.appendChild(TD1);	
		this.refs.options.options.getOptionByID(id).TRNode.appendChild(TD2);	
		this.refs.options.TBODYNode.appendChild(this.refs.options.options.getOptionByID(id).TRNode);
	}

	
	LASUI.prototype.setOption = function (evt) {
		var args = $A(arguments);
		var id = args[1];
		if(this.refs.options.options.getOptionByID(id).menu)
			this.state.properties[id]={"type" : "ferret", "value" : evt.target.options[evt.target.selectedIndex].value};
		else
			this.state.properties[id]={"type" : "ferret", "value" : evt.target.value};
	}

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
LASUI.prototype.displayCoords = function (XYSelect) {
	this.refs.inputs.minY.value=XYSelect.getSelectionGridYMin();
	this.refs.inputs.maxY.value=XYSelect.getSelectionGridYMax();
	this.refs.inputs.minX.value=XYSelect.getSelectionGridXMin();
	this.refs.inputs.maxX.value=XYSelect.getSelectionGridXMax();
	//if(this.autoupdate)
	//	this.makeRequest();
}

LASUI.prototype.setMinX = function (evt) {
	this.refs.XYSelect.updateSelectionGridXMin(this.refs.inputs.minX.value);
}
LASUI.prototype.setMaxX = function (evt) {
	this.refs.XYSelect.updateSelectionGridXMax(this.refs.inputs.maxX.value);
}
LASUI.prototype.setMinY = function (evt) {
	this.refs.XYSelect.updateSelectionGridYMin(this.refs.inputs.minY.value);
}
LASUI.prototype.setMaxY = function (evt) {
	this.refs.XYSelect.updateSelectionGridYMax(this.refs.inputs.maxY.value);
}
LASUI.prototype.autoUpdate = function (evt) {
	this.autoupdate = evt.target.checked;
}
LASUI.prototype.onafterdraw = function (evt) {
	if(this.autoupdate)
		this.makeRequest();
}
LASUI.prototype.handleDepthRangeChange = function (evt) {
	if(this.autoupdate)
		this.makeRequest();
}

LASUI.prototype.handleDepthChange = function (evt) {
	if(this.autoupdate)
		this.makeRequest();
}
LASUI.prototype.handleDateRangeChange = function (evt) {
	if(this.autoupdate)
		this.makeRequest();
}

LASUI.prototype.handleDateChange = function (evt) {
	if(this.autoupdate)
		this.makeRequest();
}

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
LASUI.prototype.collapse = function (obj) {
		if(obj.ULNode) obj.ULNode.style.display = "none";
		if(obj.IMGNode) obj.IMGNode.src = "http://www.mattkruse.com/javascript/mktree/plus.gif";
		obj.isExpanded = false;
}
LASUI.prototype.expand = function (obj) {
	if(obj.ULNode) obj.ULNode.style.display = "";
	if(obj.IMGNode) obj.IMGNode.src = "http://www.mattkruse.com/javascript/mktree/minus.gif";
	obj.isExpanded = true;
}

