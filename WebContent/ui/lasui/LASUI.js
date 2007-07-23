
	dojo.require("dojo.debug.console");
	dojo.require("dojo.lang.*");
	dojo.require("dojo.io.*");
	dojo.require("dojo.event.*");
	dojo.require("dojo.widget.Tree");
	dojo.require("dojo.widget.TreeNode");
	dojo.require("dojo.widget.TreeSelector");
	dojo.require("dojo.widget.TreeBasicController");
	dojo.require("dojo.undo.browser");
	
 	var djConfig = new Object;
 	djConfig.isDebug          = true;
 	djConfig.debugAtAllCosts  = true;
 	djConfig.debugContainerId = "_dojoDebugConsole";
 	djConfig.baseScriptUri = "dojo/dojo.js";
	
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
					"getProduct"  : {"url" : "../ProductServer.do"},
					"getCategories" : {"url" : "../getCategories.do"},
					"getDatasets" : {"url" : "../getDatasets.do"},
					"getVariables" : {"url" : "../getVariables.do"},
					"getGrid" : {"url" : "../getGrid.do"},
					"getViews" : {"url" : "../getViews.do"},
					"getOperations" : {"url" : "../getOperations.do"},
					"getOptions" : {"url" : "../getOptions.do"}
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
	}
	/*
	 * function to initialize the control tree
	 * parameters:
	 * 	anchorId - id of the page element to build the tree inside.
	 */
	LASUI.prototype.initDojoTree = function (anchorId)
	{
		
		// create 1st level tree and nodes.
		var _tree = dojo.widget.createWidget("Tree");
		dojo.byId(anchorId).appendChild(_tree.domNode);
		
		var _rootNodes  = [
									{"title" : "Categories","isFolder" : "true", "id" : "categories"},
									{"title" : "Views","isFolder" : "true", "id" : "views", "onTreeClick" : "function"},
									{"title" : "Output Types", "isFolder" : "true", "id" : "operations"},
									{"title" : "Output Options", "isFolder" : "true", "id" : "options"}
								];
		//var _treeNode = dojo.widget.createWidget("TreeNode");
		//var a = document.createElement("A");
		//a.onclick = this.makeRequest.bindAsEventListener(this);
		//a.innerHTML = "Make Request";
		//_treeNode.titleNode.appendChild(a);
		//_tree.addChild(_treeNode);
		for(var i=0;i<_rootNodes.length;i++) {
			var _treeNode = dojo.widget.createWidget("TreeNode");
			for(var s in _rootNodes[i]) 
				eval("_treeNode.edit({'" + s + "' : '" + _rootNodes[i][s]  + "'})");
			
			// keep a references to the root tree nodes so we can easily grab them later
			eval("this.refs." + _rootNodes[i].id + "={'DojoNode' : _treeNode};");
			//if(_rootNodes[i].id!="categories")
				_treeNode.onTreeClick = this.collapseRootNodes.bindAsEventListener(this,  _rootNodes[i].id);
			_tree.addChild(_treeNode); 
		}
	
		
			//get the datasets
		
		var _bindArgs = {	
								url: this.hrefs.getCategories.url,
								mimetype: "text/plain",
								error: function(type,error) {alert(error.type + ' ' + error.message);},
								load:	dojo.lang.hitch(this, (function(type,data,event) {this.setCategoryTreeNode(data,this.refs.categories,"categories"); }))
						};
		var _request = dojo.io.bind(_bindArgs);	
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
					this.refs.categories.DojoNode.edit("title","Datasets");
	
		node.children[i] = {};
		node.children[i].DojoNode = dojo.widget.createWidget("TreeNode");	
		node.children[i].DojoNode.edit({'isFolder' : true});
		var title = document.createElement("TEXT");
		title.innerHTML = node.category.getChildName(i);
		//var radio = document.createElement("INPUT");
		//radio.onclick=this.selectCategory.bindAsEventListener(this, node, i);
		//radio.name = id;
		//radio.type="radio";
		//radio.id = node.category.getDatasetID();
		//node.children[i].DojoNode.titleNode.appendChild(radio);
		node.children[i].DojoNode.titleNode.appendChild(title);	
		node.children[i].DojoNode.onTreeClick = this.selectCategory.bindAsEventListener(this, node, i);		

		node.DojoNode.addChild(node.children[i].DojoNode);
		
		if(!node.DojoNode.isExpanded) node.DojoNode.expand();
		
	}
	LASUI.prototype.createVariableTreeNode = function (node, i) {
	
		if(!node.children)
			node.children=[];
		node.children[i] = {};
		node.children[i].DojoNode = dojo.widget.createWidget("TreeNode");	
		node.children[i].DojoNode.edit({'isFolder' : false});
		var title = document.createElement("TEXT");
		title.innerHTML = node.category.getChildName(i);
		var checkbox = document.createElement("INPUT");
		checkbox.type="checkbox";
		checkbox.onclick=this.setVariable.bindAsEventListener(this, node, i);
//		node.DojoNode.onTreeClick = function(){if(this.isExpanded) this.collapse(); else this.expand();};
		checkbox.name = node.category.getChildName(i);
		checkbox.id = node.category.getChildName(i);
		node.children[i].DojoNode.titleNode.appendChild(checkbox);
		node.children[i].DojoNode.titleNode.appendChild(title);	
		node.DojoNode.addChild(node.children[i].DojoNode);
		if(!node.DojoNode.isExpanded) node.DojoNode.expand();
		
	}
	
	LASUI.prototype.selectCategory = function (evt) {
		var args = $A(arguments);
		var parentNode = args[1];
		var i = args[2];		

		if(!parentNode.children[i].category) {
			var _bindArgs = {	
					url: this.hrefs.getCategories.url + "?catid=" + parentNode.category.getChildID(i),
					mimetype: "text/plain",
					error: function(type,error) {alert(error.type + ' ' + error.message);},
									load:	dojo.lang.hitch(this, (function(type,data,event) {this.setCategoryTreeNode(data, parentNode.children[i], parentNode.category.getChildID(i)); }))
							};
			var _request = dojo.io.bind(_bindArgs);
		} 
		if(!parentNode.children[i].DojoNode.isExpanded) {
			for(var c=0;c< parentNode.children.length;c++)
				parentNode.children[c].DojoNode.collapse();
			parentNode.children[i].DojoNode.expand();	//expand the category if it has been selected 
			if(parentNode.children[i].category) //if the category is a dataset set it as the selected dataset
				if(parentNode.children[i].category.getCategoryType()=="dataset"){
						this.setDataset(parentNode.category.getChildID(i));
				}
		} else
			parentNode.children[i].DojoNode.collapse();
			
			
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
		var variableDojoNode = dataset.children[i].DojoNode;
		
		if (loadVariable) {
			//add a variable to the state
			//if we havent set a dataset, set this one
			//if(this.state.dataset == null) {
				this.state.dataset = datasetID;
				//check its radio button
				//try {dataset.DojoNode.titleNode.firstChild.checked = true;}
				//catch (e) {}
			//}
			
			//start an array of selected variables for this dataset if we havent already
			if(typeof this.state.variables[datasetID] != 'object') 
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
		this.refs.operations.DojoNode.destroyChildren();
		this.refs.options.DojoNode.destroyChildren();
		this.state.properties = {};	
		

		this.getOperations(this.state.dataset,this.state.variables[this.state.dataset].last(),this.state.view);
		this.updateConstraints();
	}	
	LASUI.prototype.getOperations= function (dataset, variable, view) {
	
		//populate the outputs tree with getOperations	
		var _bindArgs = {	
				url: this.hrefs.getOperations.url + '?dsid=' + dataset + '&varid=' + variable + '&view=' + view,
				mimetype: "text/plain",
				error: function(type,error) {alert(error.type + ' ' + error.message);},
				load: dojo.lang.hitch(this, (function(type,data,event) {this.setOperationList(data);})) 
			};
		var _request = dojo.io.bind(_bindArgs);
		
	}
	LASUI.prototype.setOperation = function (evt) {
		var args = $A(arguments);
		var id = args[1];
		this.state.operation=id;
		this.refs.options.DojoNode.destroyChildren();
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
		this.refs.operations.DojoNode.destroyChildren();
		var response = eval("(" + strJson + ")");
		var setDefault = true;
		this.refs.operations.operations = new LASGetOperationsResponse(response);
		for(var i=0;i<this.refs.operations.operations.getOperationCount();i++) {
			this.setOperationDojoNode(this.refs.operations.operations.getOperationID(i), this.refs.operations.operations.getOperationName(i));	
			if(this.refs.operations.operations.getOperationID(i)==this.state.operation) 
				setDefault = false;
		}
		if(setDefault) {
			this.state.operation=this.refs.operations.operations.getOperationID(0);
			this.refs.operations.operations.getOperationByID(this.state.operation).DojoNode.titleNode.firstChild.checked=true;		
		}
		if(this.refs.operations.operations.getOperationByID(this.state.operation).optiondef)
			this.getOptions(this.refs.operations.operations.getOperationByID(this.state.operation).optiondef.IDREF);	
		else if (this.refs.operations.operations.getOperationByID(this.state.operation).optionsdef)
			this.getOptions(this.refs.operations.operations.getOperationByID(this.state.operation).optionsdef.IDREF);	

		//if(!this.refs.operations.DojoNode.isExpanded) this.refs.operations.DojoNode.expand();
	}
	LASUI.prototype.setOperationDojoNode = function (id, name) {
		this.refs.operations.operations.getOperationByID(id).DojoNode = dojo.widget.createWidget("TreeNode");	
		this.refs.operations.operations.getOperationByID(id).DojoNode.edit({'isFolder' : false});
		var title = document.createElement("TEXT");
		title.innerHTML = name;
		var radio = document.createElement("INPUT");
		radio.type="radio";
		radio.onclick=this.setOperation.bindAsEventListener(this, id);
		radio.name = "operations";
		if (this.state.operation == id)
			radio.checked = true;		
		radio.id = id;
		this.refs.operations.operations.getOperationByID(id).DojoNode.titleNode.appendChild(radio);
		this.refs.operations.operations.getOperationByID(id).DojoNode.titleNode.appendChild(title);	
		this.refs.operations.DojoNode.addChild(this.refs.operations.operations.getOperationByID(id).DojoNode);	
}
	LASUI.prototype.getGrid = function (dataset, variable) {
				var _bindArgs = {	
						url: this.hrefs.getGrid.url + '?dsid=' + dataset + '&varid=' + variable,
						mimetype: "text/plain",
						error: function(type,error) {alert(error.type + ' ' + error.message);},
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
			error: function(type,error) {alert(error.type + ' ' + error.message);},
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
		this.refs.views.DojoNode.destroyChildren();
		var response = eval("(" + strJson + ")");
		var setDefault = true;
		this.refs.views.views = new LASGetViewsResponse(response);
		for(var i=0;i<this.refs.views.views.getViewCount();i++) {
			var useView = true;
			for(var v=0;v<this.refs.views.views.getViewID(i).length;v++)
				if(!this.state.grid.hasAxis(this.refs.views.views.getViewID(i)[v]))
					useView = false;
			if(useView) {
				this.setViewDojoNode(this.refs.views.views.getViewID(i), this.refs.views.views.getViewName(i));	
				if(this.refs.views.views.getViewID(i)==this.state.view)
					setDefault = false;
			}
		}
		if(setDefault) {
			this.state.view=this.refs.views.views.getViewID(0);
			this.refs.views.views.getViewByID(this.state.view).DojoNode.titleNode.firstChild.checked=true;
			
		}
		this.getOperations(this.state.dataset,this.state.variables[this.state.dataset].last(),this.state.view);
		//if(!this.refs.views.DojoNode.isExpanded) 
		//	this.refs.views.DojoNode.expand();
		
		this.updateConstraints();
	}
	LASUI.prototype.setViewDojoNode = function (id, name) {
		this.refs.views.views.getViewByID(id).DojoNode = dojo.widget.createWidget("TreeNode");	
		this.refs.views.views.getViewByID(id).DojoNode.edit({'isFolder' : false});
		var title = document.createElement("TEXT");
		title.innerHTML = name;
		var radio = document.createElement("INPUT");
		radio.type="radio";
		radio.onclick=this.setView.bindAsEventListener(this, id);
		radio.name = "views";
		if (this.state.view == id)
			radio.checked = true;		
		radio.id = id;
		this.refs.views.views.getViewByID(id).DojoNode.titleNode.appendChild(radio);
		this.refs.views.views.getViewByID(id).DojoNode.titleNode.appendChild(title);	
		this.refs.views.DojoNode.addChild(this.refs.views.views.getViewByID(id).DojoNode);	
}
	/*
	 * update the 4D Constraints selectors
	 */	
	LASUI.prototype.updateConstraints = function () {
			
		if(this.state.grid.getAxis('x') || this.state.grid.getAxis('y')) {
			if(!this.refs.XYSelect.enabled) {
				this.initXYSelect();			
				this.refs.XYSelect.enable();
			} else {
				this.initXYSelect();
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
		
		
		document.getElementById("Date").style.innerHTML = "<br><br>";
		document.getElementById("Depth").style.innerHTML = "<br><br>";
		
		for(var d=0;d<this.state.view.length;d++) 
			eval("this.init" + this.state.view[d].toUpperCase() + "Constraint('range')");
		for(var d=0;d<this.state.grid.response.grid.axis.length;d++) 
			if(this.state.view.indexOf(this.state.grid.response.grid.axis[d].type) < 0) 
				eval("this.init" + this.state.grid.response.grid.axis[d].type.toUpperCase() + "Constraint('point')");
	}
	
	/*
	* Initialize an X grid control
	*/
	LASUI.prototype.initXYSelect = function () {	
		if(this.refs.XYSelect && this.state.view)
			this.refs.XYSelect.enable();
		if(this.state.grid.getAxis('x') && this.state.grid.getAxis('y') && this.state.view)	
			if(this.state.view.indexOf('x')>=0 && this.state.view.indexOf('y')>=0) 
				this.refs.XYSelect.setView("xy");
			else if(this.state.view.indexOf('x')>=0 && this.state.view.indexOf('y')<0)
				this.refs.XYSelect.setView("x");
			else if(this.state.view.indexOf('x')<0 && this.state.view.indexOf('y')>=0)
				this.refs.XYSelect.setView("y");
			else if(this.state.view.indexOf('x')<0 && this.state.view.indexOf('y')<0)
				this.refs.XYSelect.setView("point");
			
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
		
		document.getElementById("Date").style.innerHTML="<br><br>";
		switch(this.state.grid.getDisplayType('t')) {
			case "widget":	
				switch(mode) {
					case 'range': 
						document.getElementById("Date").style.display="";
						this.refs.DW = new DateWidget(this.state.grid.getLo('t'),this.state.grid.getHi('t')); 
						this.refs.DW.render("Date","MDY","MDY");
						document.getElementById("Date").firstChild.align="center";parentNode.children[i].
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
						
						this.refs.DW = [document.createElement("SELECT"),document.createElement("SELECT")];
						for(var m=0;m<this.refs.DW.length;m++) {
							this.refs.DW[m].id = "DW" + m;
							this.refs.DW[m].onchange=this.handleDateRangeChange.bindAsEventListener(this);
							for(var v=0;v<this.state.grid.getMenu('t').length;v++) {
								var _opt = document.createElement("OPTION");
								_opt.value = this.state.grid.getMenu('t')[v][1];
								_opt.id = "date" + m;
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
						document.getElementById("Date").innerHTML+="<br><strong>Stop Date : </strong>";	
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
				this.request.setProperty(this.state.properties[p].type, p, this.state.properties[p].value);
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
					if(this.refs.DW.length>1) 
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
	
	
		LASUI.prototype.getOptions= function (optiondef) {
	
		//populate the outputs tree with getOperations	
		var _bindArgs = {	
				url: this.hrefs.getOptions.url + '?opid=' + optiondef,
				mimetype: "text/plain",
				error: function(type,error) {alert(error.type + ' ' + error.message);},
				load: dojo.lang.hitch(this, (function(type,data,event) {this.setOptionList(data);})) 
			};
		var _request = dojo.io.bind(_bindArgs);
		
	}
		
	/*
	 *  Update the views list with the allowed views
	 */	
	LASUI.prototype.setOptionList = function (strJson) {
		//clear the current view list and state
		this.refs.options.DojoNode.destroyChildren();
		var response = eval("(" + strJson + ")");
		var setDefault = true;
		this.state.properties = [];		
		this.refs.options.options = new LASGetOptionsResponse(response);
		var ct = this.refs.options.options.getOptionCount();
		if(ct) 
		for(var i=0;i<ct;i++) {
			this.setOptionDojoNode(this.refs.options.options.getOptionID(i));	
			switch(this.refs.options.options.getOptionType(i)) {
				case "menu" : 
					this.state.properties[this.refs.options.options.getOptionID(i)]={"type":"ferret", "value":this.refs.options.options.getOption(i).menu.item[0].values};
					break;
				case "text":
					this.state.properties[this.refs.options.options.getOptionID(i)]={"type":"ferret", "value":""};
					break;
			}
		}


		//if(!this.refs.options.DojoNode.isExpanded) this.refs.options.DojoNode.expand();
	}
	LASUI.prototype.setOptionDojoNode = function (id) {
		this.refs.options.options.getOptionByID(id).DojoNode = dojo.widget.createWidget("TreeNode");	
		this.refs.options.options.getOptionByID(id).DojoNode.edit({'isFolder' : false});
		var title = document.createElement("TEXT");
		title.innerHTML = this.refs.options.options.getOptionByID(id).title
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
		this.refs.options.options.getOptionByID(id).DojoNode.titleNode.appendChild(title);	
		this.refs.options.options.getOptionByID(id).DojoNode.titleNode.appendChild(obj);	
		this.refs.options.DojoNode.addChild(this.refs.options.options.getOptionByID(id).DojoNode);
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
  				  		'src' : '../ProductServer.do?xml=%3C%3Fxml%20version%3D%221.0%22%3F%3E%3ClasRequest%20href%3D%22file%3Alas.xml%22%3E%3Clink%20match%3D%22/lasdata/operations/operation%5B@ID%3D%27xy_map%27%5D%22%3E%3C/link%3E%3Cproperties%3E%3Cferret%3E%3Cview%3Exy%3C/view%3E%3C/ferret%3E%3C/properties%3E%3Cargs%3E%3Clink%20match%3D%22/lasdata/datasets/coads_climatology_cdf/variables/airt%22%3E%3C/link%3E%3Cregion%3E%3Crange%20type%3D%22y%22%20low%3D%22-90%22%20high%3D%2290%22%3E%3C/range%3E%3Crange%20type%3D%22x%22%20low%3D%22-180%22%20high%3D%22180%22%3E%3C/range%3E%3Cpoint%20type%3D%22t%22%20v%3D%2215-Jan%22%3E%3C/point%3E%3C/region%3E%3C/args%3E%3C/lasRequest%3E&stream=true&stream_ID=plot_image',
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
	if(!this.refs[args[1]].DojoNode.isExpanded) {
		this.refs[args[1]].DojoNode.expand();
		if(args[1]!="categories") this.refs.categories.DojoNode.collapse();
		if(args[1]!="views") this.refs.views.DojoNode.collapse();
		if(args[1]!="operations") this.refs.operations.DojoNode.collapse();
		if(args[1]!="options") this.refs.options.DojoNode.collapse();
	} else
		this.refs[args[1]].DojoNode.collapse();
}
