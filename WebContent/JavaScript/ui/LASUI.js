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
		"getProduct"  : {"url" : "ProductServer.do?"},
		"getCategories" : {"url" : "getCategories.do?"},
		"getDataConstraints" : {"url" : "getDataConstraints.do?"},
		"getGrid" : {"url" : "getGrid.do?"},
		"getViews" : {"url" : "getViews.do?"},
		"getOperations" : {"url" : "getOperations.do?"},
		"getOptions" : {"url" : "getOptions.do?"},
		"getMetadata" : {"url" : "getMetadata.do?"}
	};

	//application state
	this.state = {
		"extra_args" : "",
		"dataset" : null,
		"datasets" : {},
		"operation" : {"plot" :null,"download":null,"external":null},
		"properties" : {"plot" :[],"download":[],"external":[]},
		"optiondefs" : {"plot" : "", "download" : "", "external" : ""},
		"newproperties" : {"plot" :[],"download":[],"external":[]},
		"view" :  {"plot" :null,"download":null,"external":null,"widgets":null},
		"embed" : true,
		"xybox" : {},
		"categorynames" : [],
		"analysis" : {"type":null,"axes":{}},
		"selection" : {"x":{"min":null,"max" :null},"y":{"min":null,"max" :null},"z":{"min":null,"max" :null},"t":{"min":null,"max":null}}
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
	};
	this.AJAX_cache={};
	this.request = new LASRequest();
	this.info_icon=document.createElement("img");
	this.info_icon.src="images/icon_info.gif";
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
	if(this.params.openid)
		this.state.extra_args+="openid="+this.params.openid+"&";

	if((this.params.dsid||this.params.catid)&&this.params.varid) {
		this.state.dataset = this.params.dsid;
		this.state.catid = this.params.catid;
		this.state.lastDataset = "";
		this.state.lastVariable = "";
		this.state.variable = this.params.varid;
		this.state.operation.plot = this.params.plot;
		this.state.view.plot = this.params.view;
		//this.state.xybox = eval("("+unescape(this.params.bbox)+")");
	
		this.autoupdate = this.params.autoupdate;
		if((this.state.dataset!=""||this.params.catid!="")&&this.state.variable!="") {
			this.submitOnLoad=true;
			 document.getElementById("ol_map_widget").onmouseover = null;
		}
		else
			this.submitOnLoad=false;

	} else
		this.submitOnLoad =false;
	this.state.xybox ={};
	this.fullXYExtent=true;
		this.UIMask = document.createElement("DIV");
	this.UIMask.className = "LASUIMask";
	this.toggleUIMask('none');
	document.body.appendChild(this.UIMask);
	this.firstload=true;
	this.expired=false;
	this.refs.operations.plot.DOMNode = document.getElementById("plotType");
	this.refs.options.plot.DOMNode = document.getElementById("plotOptions");
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
	
	if(document.getElementById(this.anchors.output).addEventListener&&!document.all) {
		document.getElementById(this.anchors.output).addEventListener("load",this.onPlotLoad.LASBind(this),true);
	} else if (document.getElementById(this.anchors.output).attachEvent&&!document.all) {
		 document.getElementById(this.anchors.output).attachEvent("load",this.onPlotLoad.LASBind(this));
	} else if (document.all)
		document.getElementById(this.anchors.output).onreadystatechange=this.onPlotLoad.LASBind(this);
	else
		document.getElementById(this.anchors.output).onload=this.onPlotLoad.LASBind(this);

	//grab references to the map constraint inputs
	this.refs.inputs = {};
	for(var i in this.anchors.inputs)
		this.refs.inputs[i] = document.getElementById(this.anchors.inputs[i]);


	if(document.getElementById("categories")) {
		this.refs.categories = {};

		this.refs.categories.LINode = document.getElementById("categories");
		var cancel = document.createElement("INPUT");
		cancel.type = "submit";
		cancel.value=	"Close";
		cancel.className = "LASSubmitInputNode";
		cancel.onclick = this.genericHandler.LASBind(this,"this.hideCategories()");
		this.refs.categories.LINode.appendChild(cancel);
		this.refs.categories.LINode.appendChild(document.createElement("BR"));
		this.refs.categories.title = document.createElement("SPAN");
		this.refs.categories.title.appendChild(document.createTextNode("Select a dataset category."));
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
		var url = this.hrefs.getCategories.url + this.state.extra_args;
		req.onreadystatechange = this.AJAXhandler.LASBind(this, req, "this.AJAX_cache['"+escape(url)+"']=req.responseText;this.setCategoryTreeNode(req.responseText,this.refs.categories,'categories');");
		this.AJAX_cache[url]="waiting";
		req.open("GET", url);
		req.send(null);
	}

	if((this.state.dataset!=""||this.params.catid!="")&&this.state.variable!="") {

		if(this.params.catid) {
			var catid=this.params.catid;

		}
		else
			var catid=this.state.dataset;
		if(!document.all)
			var req = new XMLHttpRequest(this);
		else
			var req = new ActiveXObject("Microsoft.XMLHTTP");
		req.onreadystatechange = this.AJAXhandler.LASBind(this, req, "this.setInitialVariable(req.responseText);");
		req.open("GET", this.hrefs.getCategories.url + this.state.extra_args + "catid=" + catid);
		req.send(null);
	}
}
LASUI.prototype.setInitialVariable = function(strJson) {
	var response = eval("(" + strJson + ")");
	if(response.categories)
	if(response.categories.status)
	if(response.categories.status=="ok") {
	var category = new LASGetCategoriesResponse(response);

	this.state.dataset = category.getDatasetID(0);
	this.state.datasets[this.state.dataset] = category;
	this.getGrid(this.state.dataset,this.state.variable);
	this.getDataConstraints(this.state.dataset,this.state.variable);

	var info = this.info_icon.cloneNode(true);
	info.onclick = this.getMetadata.LASBind(this);
	

	if(document.getElementsByName(this.anchors.breadcrumb).item(0)) {
		while (document.getElementsByName(this.anchors.breadcrumb).item(0).firstChild)
			document.getElementsByName(this.anchors.breadcrumb).item(0).removeChild(document.getElementsByName(this.anchors.breadcrumb).item(0).firstChild);

		document.getElementsByName(this.anchors.breadcrumb).item(0).appendChild(document.createTextNode('\u00a0'));
		document.getElementsByName(this.anchors.breadcrumb).item(0).appendChild(info);
		document.getElementsByName(this.anchors.breadcrumb).item(0).appendChild(document.createTextNode('\u00a0Dataset:&nbsp;'));
		document.getElementsByName(this.anchors.breadcrumb).item(0).appendChild(document.createTextNode(category.getDatasetName()));
		document.getElementsByName(this.anchors.breadcrumb).item(0).appendChild(document.createTextNode('\u00a0Variable:&nbsp;'));
		document.getElementsByName(this.anchors.breadcrumb).item(0).appendChild(varlist1);
	}
	//document.getElementById(this.anchors.variables).onchange = function (evt) {this.options[this.selectedIndex].onselect({"target" : {"selected" :true}})}
	for(var i=0;i<category.getCategorySize();i++) {
		if(this.state.variable==category.getChildID(i))
			var selected = true;
		else
		var selected = false;
		var OPTIONNode = new Option(category.getChildName(i),category.getChildID(i),false,selected);
		OPTIONNode.onselect = this.getOperations.LASBind(this);//this.setVariable.LASBind(this, {category : category}, i, true);
		OPTIONNode.id = category.getChildID(i);
		OPTIONNode.value= "/lasdata/datasets/"+category.getDatasetID()+"/variables/"+category.getChildID(i);
		document.getElementById('variables').options[document.getElementById('variables').length] = OPTIONNode;
	}
	

	var varObj = this.state.datasets[this.state.dataset].getChildByID(this.state.variable);
	if(varObj) {
				if(varObj.grid_type!="scattered"){
					if(this.refs.analysis.enabled) {
						this.hideAnalysis();
						this.showAnalysis();
					}
					document.getElementById("analysisWrapper").style.display="";
				} else {
					document.getElementById("analysisWrapper").style.display="none";
					this.refs.analysis.enabled = false;
				}
				//document.getElementById("V6").href="servlets/datasets?dset=" + this.urlencode(categories + "/" + varObj.name);
		}
	}
}
LASUI.prototype.printPlot = function () {
	var plot = window.frames[this.anchors.output].document.getElementById('plot');
	if(plot != null) {
		var print_win = window.open(plot.src + "?" + this.state.extra_args);
		print_win.onload = function(){this.print()}
	}

}

LASUI.prototype.getMetadata = function (evt) {
	window.open(this.hrefs.getMetadata.url + this.state.extra_args + 'dsid=' + this.state.dataset);
}
LASUI.prototype.hideCategories = function() {
	this.refs.categories.LINode.style.display="none";
	this.toggleUIMask('none');
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
				window.open(this.hrefs.getMetadata.url + this.state.extra_args + 'dsid=' + node.category.getChildDatasetID(i));
			else
				window.open(this.hrefs.getMetadata.url + this.state.extra_args + 'catid=' + node.category.getChildID(i));

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
	td2.appendChild(document.createTextNode(node.category.getChildName(i)));
	td2.className = "LASTreeTableCell";
	td2.style.textAlign  = "left";
	tr.appendChild(td1);
	tr.appendChild(td2);
	if(node.category.getChildChildrenType(i)=="variables") {
		var td3 = document.createElement("TD");
		node.children[i].A = document.createElement("A");
		var img = this.info_icon.cloneNode(true);
		node.children[i].A.appendChild(img);
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
LASUI.prototype.createVariableList = function () {
	
	var select = document.createElement("SELECT"); 
	select.name='variables';
	for(var i=0;i<this.state.datasets[this.state.dataset].getCategorySize();i++) {
		//select.onchange = function (evt) {this.options[this.selectedIndex].onselect({"target" : {"selected" :true}})}
		var node = this.state.datasets[this.state.dataset];
		if(this.state.variable==node.getChildID(i))
			var selected = true;
		else
			var selected = false;

		var OPTIONNode = new Option(node.getChildName(i),node.getChildID(i),false,selected);
		//OPTIONNode.onselect = this.setVariable.LASBind(this, node, i, true);
		OPTIONNode.id = node.getChildID(i);
		OPTIONNode.value ="/lasdata/datasets/"+node.getDatasetID()+"/variables/"+node.getChildID(i);
		select.options[select.length] = OPTIONNode;
	}
	document.getElementsByName(this.anchors.breadcrumb).item(0).appendChild(select);
	

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
		var elem_nm = "<INPUT NAME='" + node.category.getDatasetID(i)+"'>";
		node.children[i].INPUTNode = document.createElement(elem_nm);
	} else {
		node.children[i].INPUTNode = document.createElement("INPUT");
		node.children[i].INPUTNode.name=node.category.getDatasetID(i);
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
	tr.appendChild(td1);
	tr.appendChild(td2);
	tbody.appendChild(tr);
	table.appendChild(tbody);
	node.children[i].LINode.appendChild(table);


	if(this.state.variable==node.category.getChildID(i))
		node.children[i].INPUTNode.checked=true;

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
		req.open("GET", this.hrefs.getCategories.url + this.state.extra_args + "catid=" + parentNode.category.getChildID(i));
		req.send(null);

	}
}
LASUI.prototype.onSetVariable = function() {
		document.getElementById('constraints').style.visibility="visible";
		if(document.getElementById('categories'))
			document.getElementById('categories').style.display='none';
		this.toggleUIMask('none');
		if(this.state.categorynames)
		if(this.state.categorynames.length>0) {
			var categories = this.state.categorynames[0];
			for(var i=1;i<this.state.categorynames.length;i++)
				categories += ' / ' + this.state.categorynames[i];
		}
		else
			var categories = this.state.datasets[this.state.dataset].getDatasetName();

		var info = this.info_icon.cloneNode(true);
		info.onclick = this.getMetadata.LASBind(this);
		var delvar = document.createElement("A");
		delvar.onclick=this.removeVariable.LASBind(this);
		delvar.appendChild(document.createTextNode('remove variable'));
		delvar.name="del";
		delvar.href="javascript:";
		delvar.style.display='none';
		var addvar=document.createElement("A");
		addvar.name="add";
		addvar.href="javascript:"
		addvar.onclick = this.addVariable.LASBind(this); 
	 	addvar.appendChild(document.createTextNode('add variable'));

		var cats = document.createElement("TEXT");
		cats.innerHTML= categories;
 		
		if(document.getElementsByName(this.anchors.breadcrumb).item(0)) {
			while (document.getElementsByName(this.anchors.breadcrumb).item(0).firstChild)
			  document.getElementsByName(this.anchors.breadcrumb).item(0).removeChild(document.getElementsByName(this.anchors.breadcrumb).item(0).firstChild);
			document.getElementsByName(this.anchors.breadcrumb).item(0).appendChild(document.createTextNode('\u00a0'));
			document.getElementsByName(this.anchors.breadcrumb).item(0).appendChild(info);
			document.getElementsByName(this.anchors.breadcrumb).item(0).appendChild(document.createTextNode('\u00a0'));
			document.getElementsByName(this.anchors.breadcrumb).item(0).appendChild(cats);
			document.getElementsByName(this.anchors.breadcrumb).item(0).appendChild(document.createTextNode('\u00a0'));
		}	
		this.createVariableList({"category" : this.state.datasets[this.state.dataset]},i);
				
		document.getElementsByName(this.anchors.breadcrumb).item(0).appendChild(document.createTextNode('\u00a0'));
                document.getElementsByName(this.anchors.breadcrumb).item(0).appendChild(addvar);
		document.getElementsByName(this.anchors.breadcrumb).item(0).appendChild(document.createTextNode('\u00a0'));
		document.getElementsByName(this.anchors.breadcrumb).item(0).appendChild(delvar);

		var varObj = this.state.datasets[this.state.dataset].getChildByID(this.state.variable);
 		if(varObj) {
			if(varObj.grid_type!="scattered"&&varObj.grid_type!="vector"){
				if(this.refs.analysis.enabled) {
					this.hideAnalysis();
					this.showAnalysis();
				}
				document.getElementById("analysisWrapper").style.display="";
			} else {
				document.getElementById("analysisWrapper").style.display="none";
				this.refs.analysis.enabled = false;
			}
			if(document.getElementById("V6"))
				document.getElementById("V6").href="servlets/datasets?dset=" + this.urlencode(categories + "/" + varObj.name);
		}
		 document.getElementById("ol_map_widget").onmouseover = null;
		//if(this.state.dataset==this.state.lastDataset)
			this.refresh();
}
LASUI.prototype.addVariable = function(evt) {
	var elm = evt.target.parentNode;
        var newvar = elm.cloneNode(true);
	elm.parentNode.insertBefore(newvar,elm);
	if(document.getElementsByName('variables').length>=2)
		for(var i=0;i<document.getElementsByName('del').length;i++)
			document.getElementsByName('del').item(i).style.display='';
	this.getViews(null,null,null);
        for(var i=0; i<newvar.children.length;i++) {
		if(newvar.children.item(i).name=="add")
			newvar.children.item(i).onclick = this.addVariable.LASBind(this);	
                if(newvar.children.item(i).name=="del")
                        newvar.children.item(i).onclick = this.removeVariable.LASBind(this);
	}

}
LASUI.prototype.removeVariable = function(evt) {

	if(document.getElementsByName('variables').length>=2)
		evt.target.parentNode.parentNode.removeChild(evt.target.parentNode);
	if(document.getElementsByName('del').length==1)
		for(var i=0;i<document.getElementsByName('del').length;i++)
                        document.getElementsByName('del').item(i).style.display='none';
	this.getViews(null,null,null);
}
/**
 * Event handler for category selection, bind to category DOM object events.
 * @param {object} evt The event objectselect
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
		for(var c=0;c< parentNode.children.length;c++) {
			this.collapse(parentNode.children[c]);
			for(var b=0; b<this.state.categorynames.length;b++)
				if(this.state.categorynames[b]==parentNode.category.getChildName(c))
					this.state.categorynames.splice(b,this.state.categorynames.length-b);
		}
		this.expand(parentNode.children[i]);	//expand the category if it has been selected
		//if(parentNode.category.getChildChildrenType(i)=="variables")
		//	this.setDataset(parentNode.category.getChildDatasetID(i));

		if(parentNode == this.refs.categories)
			this.state.categorynames = [];
		this.state.categorynames.push(parentNode.category.getChildName(i));
	} else	{
		this.collapse(parentNode.children[i]);
		for(var c=0; c<this.state.categorynames.length;c++)
			if(this.state.categorynames[c]==parentNode.category.getChildName(i))
				this.state.categorynames.splice(c,this.state.categorynames.length-c);

	}
	if(!parentNode.children[i].category) {
		parentNode.children[i].IMGNode.src = "JavaScript/components/mozilla_blu.gif";
				if(!document.all)
			var req = new XMLHttpRequest(this);
		else
			var req = new ActiveXObject("Microsoft.XMLHTTP");
		req.onreadystatechange = this.AJAXhandler.LASBind(this, req, "this.setCategoryTreeNode(req.responseText,args[3].children[args[4]],args[3].category.getChild(args[4]));", parentNode, i);
		req.open("GET", this.hrefs.getCategories.url + this.state.extra_args + "catid=" + parentNode.category.getChildID(i));
		req.send(null);
		this.state.catid=parentNode.category.getChildID(i);
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

	if(dataset.category) {
	var datasetID = dataset.category.getDatasetID(i);
	var variableID = dataset.category.getChildID(i);
	var variable = dataset.category.getChild(i);
	this.state.datasets[datasetID] = dataset.category;
	} else
	{       
		var datasetID = dataset.getDatasetID(i);
        	var variableID = dataset.getChildID(i);
        	var variable = dataset.getChild(i);
		this.state.datasets[datasetID] = dataset;
	}
	
	//clear the breadcrumbs, we dont have cross-dataset multivariable support, yet
	if(datasetID != this.state.dataset)
        	while(document.getElementsByName(this.anchors.breadcrumb).length>1)
        	        document.getElementsByName(this.anchors.breadcrumb).item(document.getElementsByName(this.anchors.breadcrumb).length-1).parentNode.removeChild(document.getElementsByName(this.anchors.breadcrumb).item(document.getElementsByName(this.anchors.breadcrumb).length-1));



	//start an array of selected variables for this dataset this.state.datasets[this.state.dataset] if we havent already
        this.state.lastDataset = this.state.dataset;
	this.state.dataset = datasetID;
	this.state.variable = variableID;
	this.getGrid(datasetID,variableID);
	
	this.getDataConstraints(datasetID,variableID);
	this.newVariable=true;
	
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

		var url = this.hrefs.getDataConstraints.url + this.state.extra_args + 'dsid=' + dataset + '&varid=' + variable;

		if(!this.AJAX_cache[url]) {
			this.AJAX_cache[url]="waiting";
			req.onreadystatechange = this.AJAXhandler.LASBind(this, req, "this.AJAX_cache['"+url+"']=req.responseText;this.setDataConstraints(req.responseText);");
			req.open("GET", this.hrefs.getDataConstraints.url + this.state.extra_args + 'dsid=' + dataset + '&varid=' + variable);
			req.send(null);
		} else if (this.AJAX_cache[url]!="waiting")
				this.setDataConstraints(this.AJAX_cache[url]);
			
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
						var option = document.createElement('option');
						option.value = 	this.state.constraints.constraint[c].constraint.menu[m].item[i].values;
						option.appendChild(document.createTextNode(this.state.constraints.constraint[c].constraint.menu[m].item[i].content));
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
		if(!view) var view = this.state.view.plot;
	
		var url = this.hrefs.getOperations.url + this.state.extra_args;// + 'view=' +view;
		if(document.getElementsByName('variables').length>0) {
			this.state.xpaths=[];
			for (var x=0; x<document.getElementsByName('variables').length;x++) {
				url += '&xpath=' + document.getElementsByName('variables').item(x).value
				this.state.xpaths.push(document.getElementsByName('variables').item(x).value);
		}
		} else	{
			url += '&xpath=/lasdata/datasets/' + dataset + '/variables/' + variable;
			this.state.variable=variable;
			this.state.dataset=dataset;
		}
		//if(view) {
			if(!this.AJAX_cache[url]) {
				this.AJAX_cache[url]="waiting";
				req.onreadystatechange = this.AJAXhandler.LASBind(this, req, "this.AJAX_cache['"+url+"'] = req.responseText; this.setOperationList(req.responseText);");
				req.open("GET", url);
				req.send(null);
			} else if(this.AJAX_cache[url]!="waiting")
				this.setOperationList(this.AJAX_cache[url]);
		//}
		//this.resetOptions("plot");

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
	if(!type) {
		type="plot";
		
	}
	if (type == "plot" && id !="init")
		try {
			if(this.state.operations)
				if(this.state.operations.getOperationByID(id).optiondef)
				if(this.state.operations.getOperationByID(id).optiondef.IDREF)
					optiondef = this.state.operations.getOperationByID(id).optiondef.IDREF;
		} catch (e) {}
	//for ie radio button bug
 	if(type=="plot"&&document.all&&evt.srcElement) {

 		for(var t in this.refs.operations.plot.children)
 			if(this.refs.operations.plot.children[t].radio) {
 				this.refs.operations.plot.children[t].radio.checked=false;
		}

			evt.srcElement.checked=true;

	}

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

	this.updateConstraints(view);
	this.state.lastVariable = this.state.variable;
	this.state.lastDataset = this.state.dataset;
	if(type=='plot'&&this.state.operation.plot=='init')
		this.getOperations(this.state.dataset,this.state.variable,this.state.view.plot,this.state.xpaths);
	if(optiondef)
		this.getOptions(optiondef, "plot",false);
	else
		this.onSetVariable();
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
	if(response.operations.error=="No operations found.") {
		if(this.state.view.plot.length>1) {
			this.state.view.plot=this.state.view.plot[0];
			this.setVisualization(this.state.view.plot);
			return null;
		}
	}
	this.state.operations = new LASGetOperationsResponse(response);
	//disable all nodes first
	for(var row=0;row<document.getElementById("productButtons").childNodes.length;row++)
		for(var cell=0;cell<document.getElementById("productButtons").childNodes[row].childNodes.length;cell++)
			try {
					document.getElementById("productButtons").childNodes[row].style.visibility="hidden";
				} catch (e) {}


	//document.body.appendChild(this.refs.options.external.DOMNode);
	//disable all nodes first
	var setDefaultVis = true;
	if(!this.state.operations.response.operations.error)
		this.setDefaultProductMenu();
	/*	for(var i=0;i<this.state.operations.getOperationCount();i++) {
			this.setOperationNode(this.state.operations.getOperationID(i), this.state.operations.getOperationName(i));
			if(this.state.operations.getOperation(i).category == "visualization")
			 	if(this.state.operations.getOperation(i)["default"]=="true") {
					this.state.operation.plot = this.state.operations.getOperationID(i);
					setDefaultVis = false;
				} else
					var defaultVis = this.state.operations.getOperationID(i);
		}

	if(setDefaultVis==true && defaultVis) {
		this.state.operation.plot = defaultVis;
	}
*/
	 for(var i=0;i<this.state.operations.getOperationCount();i++) 
		if(document.getElementById(this.state.operations.getOperationName(i))) {
			document.getElementById(this.state.operations.getOperationName(i)).style.visibility='visible';
			document.getElementById(this.state.operations.getOperationName(i)).onclick="LAS.state.operation.external='"+this.state.operations.getOperationID(i)+"';LAS.makeRequest('external')";
		}

	if(this.state.operations.getOperationByID(this.state.operation.plot).optiondef) {
		if(this.state.operations.getOperationByID(this.state.operation.plot).optiondef.IDREF) {
			this.getOptions(this.state.operations.getOperationByID(this.state.operation.plot).optiondef.IDREF, "plot", true);
		} else {
			document.getElementById('plotOptionsButton').style.visibility='hidden';
			this.onSetVariable();
	} } else {
		document.getElementById('plotOptionsButton').style.visibility='hidden';
		this.onSetVariable();
	}

	if(this.refs.analysis.enabled||!this.state.grid.hasAxis('t'))
		document.getElementById('Animation').style.visibility='hidden';
}

LASUI.prototype.refresh = function() {
         if(!this.updating)
                if(this.autoupdate||this.submitOnLoad||this.newVariable){
                        this.submitOnLoad =false;
                        this.newVariable=false;
                        this.makeRequest();

                } else
                        this.showUpdateLink();
}
/**
 * Method to create an operation radio button and add it to the operations tree node.
 * @param {string} id The operation id
 * @param {string} name The name of the operation
 */
LASUI.prototype.setOperationNode = function (id, name) {

	var button = document.getElementById(name);
	if(button) {
		button.style.visibility = "visible";
		button.onclick=this.doProductIconClick.LASBind(this, id);
	}
	var option = document.getElementById("OPTION_DOWNLOAD_" + id);
	if(option) {
		option.disabled="";
	}
}
LASUI.prototype.doProductIconClick = function (evt) {
	var args = arguments;
	var id = args[1];

	this.state.operation.external = id;
	this.state.view.external = this.state.view.plot;
	this.toggleUIMask('');

	if(this.state.operations.getOperationByID(id))
		if(this.state.operations.getOperationByID(id).optiondef) {
			this.getOptions(this.state.operations.getOperationByID(id).optiondef.IDREF, "external", true);
			this.refs.options.external.DOMNode.style.display="";
		} else {
			this.refs.options.external.DOMNode.style.display='none';
			this.toggleUIMask('none');
			this.makeRequest({},'external');
			return;
		}
}
/**
 * Method to query the server for the available grids
 * @param {string} dataset A dataset id
 * @param {string} variable A variable id within the dataset
 */
LASUI.prototype.getGrid = function (dataset, variable) {

		var url = this.hrefs.getGrid.url + this.state.extra_args + 'dsid=' + dataset + '&varid=' + variable;
		
		if(!this.AJAX_cache[url]) {
			this.AJAX_cache[url]="waiting";
			if(!document.all)
				var req = new XMLHttpRequest(this);
			else
				var req = new ActiveXObject("Microsoft.XMLHTTP");
			req.onreadystatechange = this.AJAXhandler.LASBind(this, req, "this.AJAX_cache['"+url+"']=req.responseText;this.setGrid(req.responseText);");
			req.open("GET",  this.hrefs.getGrid.url + this.state.extra_args + 'dsid=' + dataset + '&varid=' + variable);
			req.send(null);
		} else if (this.AJAX_cache[url]!="waiting")
			this.setGrid(this.AJAX_cache[url]);


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
	this.getViews(this.state.dataset,this.state.variable);
}
/**
 * Method to query the server for a list of allowed views
 * @param {string} dataset A dataset id
 * @param {string} variable A variable id
 */
LASUI.prototype.getViews = function (dataset,variable, xpath) {
		//var url = this.hrefs.getViews.url + this.state.extra_args + 'dsid=' + dataset + '&varid=' +variable;
		
	        var url = this.hrefs.getViews.url + this.state.extra_args;// + 'view=' +view;
                if(document.getElementsByName('variables').length>0)
                        for (var x=0; x<document.getElementsByName('variables').length;x++)
                                url += '&xpath=' + document.getElementsByName('variables').item(x).value
                else        
			url += '&dsid='+dataset+"&varid="+variable;
	
			
		if(!this.AJAX_cache[url]) {
                        this.AJAX_cache[url]="waiting";
			if(!document.all)
				var req = new XMLHttpRequest(this);
			else
				var req = new ActiveXObject("Microsoft.XMLHTTP");
			req.onreadystatechange = this.AJAXhandler.LASBind(this, req, "this.AJAX_cache['"+url+"']=req.responseText;this.setViews(req.responseText);");
			req.open("GET", url);
			req.send(null);
		} if (this.AJAX_cache[url]!="waiting")
			this.setViews(this.AJAX_cache[url]);

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
	this.getOperations(this.state.dataset,this.state.variable);
}
LASUI.prototype.setDefaultProductMenu = function () {
	//clear the menu
	while(this.refs.operations.plot.DOMNode.firstChild)
		this.refs.operations.plot.DOMNode.removeChild(this.refs.operations.plot.DOMNode.firstChild);
	
	for(var type in this.refs.operations)
		this.refs.operations[type].children ={};
	var setPlotDefault = "true";
	var setDownloadDefault = "true";
	var defaultPlotProduct = null;
	var defaultDownloadProduct = null;
	var viz_types = {}


	//for each product category, cycle through the views it contains, and the operations that 
	for (var type in this.state.operations.getAllOperationTypes())
		for(var view in this.state.operations.getAllIntervals())
			if(this.state.grid.hasView(view))
			for(var op in this.state.operations.getOperationsByType(type))
				if(this.state.operations.getOperationByID(op).default=="true"&&this.state.operations.getOperationByID(op).category=="visualization"&&this.state.operations.hasInterval(op,view)) {
					if(!this.refs.operations.plot.children[type])
	                                        this.setProductTypeNode(type);
					if(!defaultPlotProduct||(this.state.operation.plot=op&&this.state.view.plot==view))
						defaultPlotProduct=this.setProductNode(op, view, this.state.operations.getOperationIntervals(op)[view].title);				       
					else 
						this.setProductNode(op, view, this.state.operations.getOperationIntervals(op)[view].title);
					
				}
			

	if(defaultPlotProduct) {
		//this.setOperation(this,defaultPlotProduct.id,defaultPlotProduct.view,defaultPlotProduct.optiondef);
		defaultPlotProduct.radio.onclick();
		defaultPlotProduct.radio.checked="checked";
		 defaultPlotProduct.radio.selected=true;
	}
	
	if(this.refs.analysis.enabled)
		this.showAnalysis();

}
LASUI.prototype.setProductTypeNode = function(type) {
			this.refs.operations.plot.children[type] = {};
			this.refs.operations.plot.children[type].LINode = document.createElement("LI");
			this.refs.operations.plot.children[type].LINode
			this.refs.operations.plot.children[type].title = document.createElement("TEXT");
			this.refs.operations.plot.children[type].title.innerHTML = "<b>" + type + "</b>";
			this.refs.operations.plot.children[type].LINode.className = "LASPlotCategory";
			this.refs.operations.plot.children[type].LINode.appendChild(this.refs.operations.plot.children[type].title);
			this.refs.operations.plot.DOMNode.appendChild(this.refs.operations.plot.children[type].LINode);
}
LASUI.prototype.setProductNode = function(id, view, product){i
	        if(this.state.operations.getOperationByID(id).optiondef)
                                var optiondef = this.state.operations.getOperationByID(id).optiondef.IDREF;
                else
                                var optiondef = "";
 
		this.refs.operations.plot.children[product] = {};
		this.refs.operations.plot.children[product].LINode = document.createElement("LI");
		this.refs.operations.plot.children[product].LINode.className = "LASPlotType";
		//if(this.products[type][product].view.length==0)
		//	this.refs.operations.plot.children[product].LINode.style.display = "none";
		this.refs.operations.plot.children[product].title = document.createTextNode(product);
		this.refs.operations.plot.children[product].radio = document.createElement("INPUT");
		this.refs.operations.plot.children[product].radio.type = "radio";
		this.refs.operations.plot.children[product].radio.name = "product";
		this.refs.operations.plot.children[product].radio.className = "LASRadioInputNode";
		this.refs.operations.plot.children[product].radio.value = id;
		this.refs.operations.plot.children[product].radio.onselect = this.setOperation.LASBind(this,id,view,optiondef,"plot" );
		this.refs.operations.plot.children[product].radio.onclick = this.setOperation.LASBind(this,id,view,optiondef,"plot");
		this.refs.operations.plot.children[product].LINode.appendChild(this.refs.operations.plot.children[product].radio);
		this.refs.operations.plot.children[product].LINode.appendChild(this.refs.operations.plot.children[product].title);
		this.refs.operations.plot.DOMNode.appendChild(this.refs.operations.plot.children[product].LINode);
		return {id:id, view:view, optiondef:optiondef, radio : this.refs.operations.plot.children[product].radio};
}
LASUI.prototype.onPlotLoad = function (e) {
	
	var urlArr = window.frames[this.anchors.output].document.location.search.replace("?","").split("&");
	for(var i=0;i<urlArr.length;i++)
		if(urlArr[i].substr(0,3)=="xml")
			var Req = new LASRequest(unescape(urlArr[i].substr(4,urlArr[i].length)).replace(/\+/g," "));

	if(Req)  {
			if(Req.getXMLText&&!isFeatureEditing()) {
				var minx = parseFloat(Req.getRangeLo('x'));
				var miny = parseFloat(Req.getRangeLo('y'));
				var maxx = parseFloat(Req.getRangeHi('x'));
				var maxy = parseFloat(Req.getRangeHi('y'));
				var mint = parseFloat(Req.getRangeLo('t'));
                                var minz = parseFloat(Req.getRangeLo('z'));
                                var maxt = parseFloat(Req.getRangeHi('t'));
                                var maxz = parseFloat(Req.getRangeHi('z'));
				if(!maxx&&minx)maxx=minx;
				if(!maxy&&minx)maxy=miny;
                                if(!maxz&&minz)maxz=minz;
                                if(!maxt&&mint)maxt=mint;
				if(minz>maxz){var z = minz; minz = maxz; maxz = z}			
	
				
				if(miny&&minx&&maxy&&maxx)
				 setMapCurrentSelection(miny,maxy,minx,maxx);
				if(minx) this.state.selection.x.min = minx;
				if(maxx) this.state.selection.x.max = maxx;
				if(miny) this.state.selection.z.min = miny;
                                if(maxy) this.state.selection.z.max = maxy;
                                if(minz) this.state.selection.z.min = minz;
                                if(maxz) this.state.selection.z.max = maxz;
	                        if(mint) this.state.selection.t.min = mint;
                                if(maxt) this.state.selection.t.max = maxt;
				
				if(this.state.view.widgets.indexOf('z')>=0) this.initZConstraint('range',true);
			}	
		this.request = Req;
	}
	if(document.getElementById("wait"))
		document.getElementById("wait").style.visibility="hidden";
	if(document.getElementById("wait_msg"))
		document.getElementById("wait_msg").style.display="none";
	if(document.getElementById('output')) 
		document.getElementById("output").style.visibility="visible";
	 if(document.getElementById('update')) 
	document.getElementById('update').style.color='';
	
}
LASUI.prototype.onPlotUnload = function (e) {

        if(document.getElementById("wait"))
                document.getElementById("wait").style.visibility="visible";
        if(document.getElementById("wait_msg"))
                document.getElementById("wait_msg").style.display="";
        if(document.getElementById('output'))
                document.getElementById("output").style.visibility="hidden";

}
LASUI.prototype.roundGrid = function(grid) {
	var outgrid = {"x" : {"min": 0, "max": 0}, "y" : {"min": 0, "max": 0}};
	outgrid.x.min = Math.round(grid.x.min*1000)/1000;
	outgrid.x.max = Math.round(grid.x.max*1000)/1000;
	outgrid.y.min = Math.round(grid.y.min*1000)/1000;
	outgrid.y.max = Math.round(grid.y.max*1000)/1000;
	return(outgrid);
}
/**
 * Update the 4D Constraints selectors
 */
LASUI.prototype.updateConstraints = function (view) {
	if(!this.state.grid)
		return;
	if(view==null&& view !="")
		view = this.state.view.widgets;
	else
		this.state.view.widgets = view;

	while(document.getElementById("Date").firstChild)
		document.getElementById("Date").removeChild(document.getElementById("Date").firstChild);
	while(document.getElementById("Depth").firstChild)
		document.getElementById("Depth").removeChild(document.getElementById("Depth").firstChild);



	var reset=false;
	var resetXY=false;
	if(this.state.lastgrid) {
		if(this.state.grid.response.grid.ID!=this.state.lastgrid.response.grid.ID)
			reset=true;
	}




	if(this.state.lastDataset!=this.state.dataset||this.resetXY) {//&&this.state.selectGlobal)||this.state.selectGlobal||this.resetXY||!this.initialized) {
		var resetXY =true;
		this.resetXY = false;
		this.state.selectGlobal = false;
	}


	if(this.state.lastDataset!=this.state.dataset)
		var reset=true;

	if(!this.initialized)
		reset=true;

	if(view.indexOf('x')>=0&&view.indexOf('y')>=0)
		this.initXYSelect("xy",resetXY);
	else if(view.indexOf('x')>=0&&view.indexOf('y')<0)
		this.initXYSelect("x",resetXY);
	else if(view.indexOf('x')<0&&view.indexOf('y')>=0)
		this.initXYSelect("y",resetXY);
	else if(view.indexOf('x')<0&&view.indexOf('y')<0)
		this.initXYSelect("pt",resetXY);

	for(var d=0;d<this.state.grid.response.grid.axis.length;d++)
		if(view.indexOf(this.state.grid.response.grid.axis[d].type) < 0)
			if(this.state.datasets[this.state.dataset].getChildByID(this.state.variable).grid_type!="scattered"&&!this.refs.analysis.enabled)
				eval("this.init" + this.state.grid.response.grid.axis[d].type.toUpperCase() + "Constraint('point',reset)");
			else
				eval("this.init" + this.state.grid.response.grid.axis[d].type.toUpperCase() + "Constraint('range',reset)");
		else
			eval("this.init" + this.state.grid.response.grid.axis[d].type.toUpperCase() + "Constraint('range',reset)");


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
	if(!this.updating)
		if(this.autoupdate){
			this.makeRequest();

		} else
			this.showUpdateLink();

	this.updating = false;
	this.state.lastgrid=this.clone(this.state.grid);
}
/**
 * Initialize the XY select widget to the grid
 */
LASUI.prototype.initXYSelect = function (mode, reset) {
	if(this.state.grid.getAxis('x') && this.state.grid.getAxis('y') && reset)
	 {	
		setMapTool(mode);
        setMapDataExtent(parseFloat(this.state.grid.getLo('y')), parseFloat(this.state.grid.getHi('y')), parseFloat(this.state.grid.getLo('x')), parseFloat(this.state.grid.getHi('x')),parseFloat(this.state.grid.getDelta('x')));
		
	} else {
		setMapTool(mode);
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
	while(document.getElementById("Depth").firstChild)
		document.getElementById("Depth").removeChild(document.getElementById("Depth").firstChild);

	if(!this.state.selection.z.min) this.state.selection.z.min = this.state.grid.getLo('z');
	if(!this.state.selection.z.max) this.state.selection.z.max = this.state.grid.getHi('z');


	if(this.state.grid.hasMenu('z'))
		if (reset || this.refs.DepthWidget.widgetType != "menu") {
			this.refs.DepthWidget.menu = [document.createElement("SELECT"),document.createElement("SELECT")];
			this.refs.DepthWidget.widgetType = "menu";
			for(var m=0;m<this.refs.DepthWidget[this.refs.DepthWidget.widgetType].length;m++) {
				selected = null;
				for(var v=0;v<this.state.grid.getMenu('z').length;v++) {
					var _opt = document.createElement("OPTION");
					_opt.value = this.state.grid.getMenu('z')[v][1];
					_opt.className = "LASOptionNode";
					_opt.appendChild(document.createTextNode(this.state.grid.getMenu('z')[v][0]));
					if(m==1 && parseFloat(this.state.grid.getMenu('z')[v][0]) >= this.state.selection.z.max){
						if(selected){
                                                        if(  parseFloat(this.state.grid.getMenu('z')[v][0])<selected) {
                                                                _opt.selected=true;
                                                                selected =  parseFloat(this.state.grid.getMenu('z')[v][0]);
                                                        }
                                                } else {
							 _opt.selected=true;
                                                        selected= parseFloat(this.state.grid.getMenu('z')[v][0]);
						}
					}
					if(m==0 &&  parseFloat(this.state.grid.getMenu('z')[v][0])<= this.state.selection.z.min){
					        if(selected) {
                                                        if(  parseFloat(this.state.grid.getMenu('z')[v][0])>selected) {
                                                                _opt.selected=true;
                                                                selected =   parseFloat(this.state.grid.getMenu('z')[v][0]);
                                                        }
                                                } else {
							 _opt.selected=true;
                                                        selected=  parseFloat(this.state.grid.getMenu('z')[v][0]);
						}
					}

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
				var selected = null;
				for(var v=parseFloat(this.state.grid.getLo('z'));v<=parseFloat(this.state.grid.getHi('z'));v+=parseFloat(this.state.grid.getDelta('z'))) {
					var _opt = document.createElement("option");
					_opt.value = v;
					_opt.className = "LASOptionNode";
					_opt.appendChild(document.createTextNode(v));
					if(m==1 && v >= this.state.selection.z.max) {
						if(selected){
							if(v<selected) {
								_opt.selected=true;
								selected = v
							}
						} else {
							 _opt.selected=true;
							selected=v;
						}
					}
					if(m==0 && v <= this.state.selection.z.min) {
						 if(selected){
                                                        if(v>selected) {
                                                                _opt.selected=true;
                                                                selected = v
                                                        }
                                                } else {
                                                        selected=v;
							 _opt.selected=true;
						}
					}


					this.refs.DepthWidget[this.refs.DepthWidget.widgetType][m].appendChild(_opt);

				}
				this.refs.DepthWidget[this.refs.DepthWidget.widgetType][m].onchange=this.handleDepthRangeChange.LASBind(this);
			}
		}
	switch (mode) {
			case 'range':
				var depth_label2 =document.createElement("STRONG");
				depth_label2.appendChild(document.createTextNode("Minimum Depth (" + this.state.grid.getAxis('z').units +") : "));
				document.getElementById("Depth").appendChild(depth_label2);
				document.getElementById("Depth").appendChild(this.refs.DepthWidget[this.refs.DepthWidget.widgetType][0]);
				document.getElementById("Depth").appendChild(document.createElement("BR"));
				var depth_label3 =document.createElement("STRONG");
				depth_label3.appendChild(document.createTextNode("Maximum Depth (" + this.state.grid.getAxis('z').units +") : "));
				document.getElementById("Depth").appendChild(depth_label3);
				document.getElementById("Depth").appendChild(this.refs.DepthWidget[this.refs.DepthWidget.widgetType][1]);
				document.getElementById("Depth").style.display="";
				break;
			case 'point':
				var depth_label = document.createElement("STRONG");
				depth_label.appendChild(document.createTextNode("Depth (" + this.state.grid.getAxis('z').units + ") : "));
				document.getElementById("Depth").appendChild(depth_label);
				document.getElementById("Depth").appendChild(this.refs.DepthWidget[this.refs.DepthWidget.widgetType][0]);
				document.getElementById("Depth").style.display="";
				break;
	}

}
/**
 * Initialize an T grid  control
 * @param {string} mode The axis mode. "range" or "point"
 */
LASUI.prototype.initTConstraint = function (mode,reset) {
	document.getElementById("Date").style.display="";
	while(document.getElementById("Date").firstChild)
							document.getElementById("Date").removeChild(document.getElementById("Date").firstChild);

        if(!this.state.selection.t)
                this.state.selection.t = {min : null, max: null};

	switch(this.state.grid.getDisplayType('t')) {
		case "widget":
			if(reset || !this.refs.DW)
				if(reset || this.refs.DW.widgetType != "DateWidget")	{
					this.refs.DW = new DateWidget(this.state.grid.getLo('t'),this.state.grid.getHi('t'),this.state.grid.getMinuteInterval('t'));
					this.refs.DW.callback = this.handleDateRangeChange.LASBind(this);
				}

				var DWDisplay = ""
				if(this.state.grid.getAxis('t').monthNeeded=="true")
					DWDisplay += "M";
				if(this.state.grid.getAxis('t').dayNeeded=="true")
					DWDisplay += "D";
				if(this.state.grid.getAxis('t').yearNeeded=="true")
					DWDisplay += "Y";
				if(this.state.grid.getAxis('t').hourNeeded=="true"||this.state.grid.getAxis('t').hoursNeeded=="true")
					DWDisplay += "T";


			switch(mode) {
				case 'range':
					document.getElementById("Date").innerHTML="<table><tbody><tr><td><table><tbody><tr><td><strong>Start Date :</strong></td></tr><tr><td><strong>End Date :</strong></tr></tbody></table></td><td id='DWAnchor'></td></tr></tbody></table>";
					this.refs.DW.render("DWAnchor", DWDisplay, DWDisplay);
					break;
				case 'point':
					document.getElementById("Date").innerHTML="<table><tbody><tr><td><table><tbody><tr><td><strong>Date :</strong></td><td id='DWAnchor'></td></tr></tbody></table>";
					this.refs.DW.render("DWAnchor", DWDisplay);
					break;
			}
			break;
		case "menu":
			if(reset || !this.refs.DW)
				if(reset || this.refs.DW[0].tagName != "select")	{
					this.refs.DW = [document.createElement("select"),document.createElement("select")];
					for(var m=0;m<this.refs.DW.length;m++) {
						this.refs.DW[m].onchange=this.handleDateRangeChange.LASBind(this);
						this.refs.DW[m].className = "LASSelectNode";
						for(var v=0;v<this.state.grid.getMenu('t').length;v++) {
							var _opt = document.createElement("option");
							_opt.value = this.state.grid.getMenu('t')[v][1];
							_opt.className = "LASOptionNode";
							_opt.appendChild(document.createTextNode(this.state.grid.getMenu('t')[v][0]));
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
					var date_label = document.createElement("STRONG");
					date_label.appendChild(document.createTextNode("Start Date : "));
					document.getElementById("Date").appendChild(date_label);
					document.getElementById("Date").appendChild(this.refs.DW[0]);
					document.getElementById("Date").appendChild(document.createElement("BR"));
					var label = document.createElement("STRONG");
					label.appendChild(document.createTextNode("End Date : "));
					document.getElementById("Date").appendChild(label);
					document.getElementById("Date").appendChild(this.refs.DW[1]);
					break;
				case 'point':
					var date_label = document.createElement("STRONG");
					date_label.appendChild(document.createTextNode("Date : "));
					document.getElementById("Date").appendChild(date_label);
					document.getElementById("Date").appendChild(this.refs.DW[0]);
					break;
			}
			break;
	 }


}
LASUI.prototype.showUpdateLink = function () {
	this.expired = true;
	document.getElementById('update').style.color='#f5ed52';
	document.getElementById('update').style.visibility='visible';
	if(document.getElementById("wait"))
		document.getElementById("wait").style.visibility="hidden";
	if(document.getElementById("wait_msg"))
		document.getElementById("wait_msg").style.display="none";
	if(document.getElementById('output'))
		document.getElementById("output").style.visibility="visible";


}
LASUI.prototype.toggleAutoUpdate = function (e, toggle) {

	this.autoupdate = toggle;

	if(this.autoupdate&&this.expired)
		this.makeRequest();
	var e = e||event;/* get IE event ( not passed ) */
    e.stopPropagation? e.stopPropagation() : e.cancelBubble = true;
	return false;
}

/**
 * Put together and submit an LAS request
 */
LASUI.prototype.makeRequest = function (evt, type) {
	if(!type)
		var type = 'plot';
	if(!this.updating||this.expired) {
		if(type=='plot'&&this.state.operation.plot=='init') {
			this.getOperations(this.state.dataset,this.state.variable,this.state.view.plot)
			return null;
		}

		document.getElementById('output').height="100%";
		document.getElementById('output').width="100%";
		document.getElementById('update').style.visibility='visible';
		
		this.request = null;
		this.uirequest = '';
		this.request = new LASRequest('');
		this.state.view.download=this.state.view.plot;
		this.state.view.external=this.state.view.plot;

		this.request.removeVariables();
		this.request.removeConstraints();


		//if(this.state.dataset==null) {alert("Please select a dataset and variables."); return;}
		//if(this.state.variable==null) {alert("Please select variables in the selected dataset."); return;}
		//if(this.state.operation[type]==null) {alert("Please select a file format to download."); return;}

		//add the operation
		this.request.setOperation(this.state.operation[type]);
		this.uirequest+="dsid=" + this.state.dataset;
		this.uirequest+="&catid=" + this.state.catid;
		this.uirequest+="&varid=" + this.state.variable;
		this.uirequest+='&plot=' + this.state.operation.plot;
		this.uirequest+='&view=' + this.state.view.plot;

		//this.uirequest.setProperty('ui','state',JSON.stringify(this.state));
		var uioptions = '';
		//set the options
		for(var p in this.state.properties[type])
			if((typeof this.state.properties[type][p] != "function") && (typeof this.state.properties[type][p] == "object")) {
				this.request.setProperty(this.state.properties[type][p].type, p, this.urlencode(this.state.properties[type][p].value));
				//uioptions[this.state.properties.plot[p].type] = {p : escape(this.state.properties[type][p].value)};
			}
		var view = this.state.view[type];
		//if(view=="") view = "d";
		this.request.setProperty("ferret","view",view);

		this.request.removeRegion();

		//add the variables
		if(this.state.datasets[this.state.dataset].getChildByID(this.state.variable).grid_type=="vector") {
			for(var v=0;v<this.state.datasets[this.state.dataset].getChildByID(this.state.variable).variable.length;v++)
				 this.request.addVariable(this.state.dataset,this.state.datasets[this.state.dataset].getChildByID(this.state.variable).variable[v].IDREF)	
			this.request.setProperty("ferret","vector_name",this.state.datasets[this.state.dataset].getChildByID(this.state.variable).name);
		} else
		for(var v=0;v<document.getElementsByName(this.anchors.breadcrumb).length;v++) {
			this.request.addVariable(this.state.dataset,document.getElementsByName(this.anchors.breadcrumb).item(v).children.namedItem('variables').options[document.getElementsByName(this.anchors.breadcrumb).item(v).children.namedItem('variables').selectedIndex].id);
		///this.request.setVariable(this.state.dataset, this.state.variable);
		}


		this.request.addRegion();
		//do the analysis, if required.
		if(this.refs.analysis.enabled && this.state.analysis.enabled) {
			var Analysis = {"label" : this.state.analysis.name + ' ' + this.state.datasets[this.state.dataset].getChildByID(this.state.variable).name, "axis" : []};
			for(var axis_id in this.state.analysis.axes) {
				var Axis= {"type" : axis_id, "op" : this.state.analysis.type};
				if(this.state.grid.hasAxis(axis_id)){
					var Axis = {"type" : axis_id, "op" : this.state.analysis.type};
					switch(axis_id) {
						case 'x' :
							Axis.lo=getMapXlo();
							Axis.hi=getMapXhi();
							break;
						case 'y' :
						 	Axis.lo=getMapYlo();
							Axis.hi=getMapYhi();
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
			if(!(this.refs.analysis.enabled && this.state.analysis.enabled && this.state.analysis.axes[this.state.grid.response.grid.axis[d].type]))
				switch(this.state.grid.response.grid.axis[d].type) {
					case 'x' :
						if(this.state.view[type].indexOf('x')>=0||this.state.datasets[this.state.dataset].getChildByID(this.state.variable).grid_type=="scattered")
							this.request.addRange('x',getMapXlo(),getMapXhi());
						else
							this.request.addRange('x',(getMapXlo()+getMapXhi())/2,(getMapXlo()+getMapXhi())/2);
						break;
					case 'y' :
						if(this.state.view[type].indexOf('y')>=0||this.state.datasets[this.state.dataset].getChildByID(this.state.variable).grid_type=="scattered")
							this.request.addRange('y',getMapYlo(),getMapYhi());
						else
							this.request.addRange('y',(getMapYlo()+getMapYhi())/2,(getMapYlo()+getMapYhi())/2);

						//this.uirequest+="&y="+escape("{ 'min' : " + getMapYlo() + ", 'max' : " + getMapYhi() + "}");
					 	//this.uirequest+="&viewy="+escape("{ 'min' : " + this.refs.XYSelect.extents.data.grid.y.min + ", 'max' : " + this.refs.XYSelect.extents.data.grid.y.max + "}");
						break;
					case 't' :
						if(this.state.view[type].indexOf('t')>=0||this.state.datasets[this.state.dataset].getChildByID(this.state.variable).grid_type=="scattered")
							if(this.state.grid.hasMenu('t')){
								this.request.addRange('t',this.refs.DW[0].value,this.refs.DW[1].value);
								//this.uirequest+="&t="+escape("{ 'min' : '" + this.refs.DW[0].value+ "', 'max' : '" + this.refs.DW[1].value + "'}");
						} else {
							this.request.addRange('t',this.refs.DW.getDate1_Ferret(),this.refs.DW.getDate2_Ferret());
							//this.uirequest+="&t="+escape("{ 'min' : '" + this.refs.DW.getDate1_Ferret()+ "', 'max' : '" + this.refs.DW.getDate2_Ferret()+ "'}");
						}
					else
						if(this.state.grid.hasMenu('t')){
							this.request.addRange('t',this.refs.DW[0].value);
						//	this.uirequest+="&t=" + escape("{ 'min' : '" + this.refs.DW[0].value+ "', 'max' : '" + this.refs.DW[0].value + "'}");
						}
						else {
							this.request.addRange('t',this.refs.DW.getDate1_Ferret());
						//	this.uirequest+="&t=" + escape("{ 'min' : '" + this.refs.DW.getDate1_Ferret()+ "', 'max' : '" + this.refs.DW.getDate1_Ferret()+ "'}");
						}
					break;
				case 'z' :
					if(this.state.view[type].indexOf('z')>=0||this.state.datasets[this.state.dataset].getChildByID(this.state.variable).grid_type=="scattered") {
							this.request.addRange('z',this.refs.DepthWidget[this.refs.DepthWidget.widgetType][0].value,this.refs.DepthWidget[this.refs.DepthWidget.widgetType][1].value);
						//	this.uirequest+="&z=" + escape("{ 'min' : '" + this.refs.DepthWidget[this.refs.DepthWidget.widgetType][0].value+ "', 'max' : '" + this.refs.DepthWidget[this.refs.DepthWidget.widgetType][1].value + "'}");
						}
						else {
							this.request.addRange('z',this.refs.DepthWidget[this.refs.DepthWidget.widgetType][0].value);
						//	this.uirequest+="&z=" + escape("{ 'min' : '" + this.refs.DepthWidget[this.refs.DepthWidget.widgetType][0].value+ "', 'max' : '" + this.refs.DepthWidget[this.refs.DepthWidget.widgetType][1].value + "'}");
						}
					break;
			}
		if(this.refs.constraints)
		for(var c=0; c<this.refs.constraints.length;c++){
			if(this.refs.constraints[c].apply.checked)
			switch(this.refs.constraints[c].type) {
				case 'text' :
					this.request.addTextConstraint(this.urlencode(this.refs.constraints[c].lhs.value),this.urlencode(this.refs.constraints[c].ops.value),this.urlencode(this.refs.constraints[c].rhs.value));
				break;
				case 'variable' :
					this.request.addVariableConstraint(this.urlencode(this.state.dataset), this.urlencode(this.refs.constraints[c].lhs.value),this.urlencode(this.refs.constraints[c].ops.value),this.urlencode(this.refs.constraints[c].rhs.value));
				break;
			}
		}
		 document.getElementById("ol_map_widget").onmouseover = null;

		
		if(type=="external"&&this.state.operation.external == "Interactive_Download")
			var window_options = "height=500,width=400,status=no,toolbar=no,menubar=no,location=no"; 
		else
			var window_options = "";

		if(this.state.embed && type == "plot"){
			if(document.getElementById("wait"))
				document.getElementById("wait").style.visibility="visible";
			if(document.getElementById("wait_msg"))
				document.getElementById("wait_msg").style.display="";
			document.getElementById(this.anchors.output).style.visibility="hidden";
			document.getElementById(this.anchors.output).src = (this.hrefs.getProduct.url + this.state.extra_args + 'xml=' + this.urlencode(this.request.getXMLText()));
		} else
			window.open(this.hrefs.getProduct.url + this.state.extra_args + 'xml=' +  this.urlencode(this.request.getXMLText()),null,window_options);
	}

	this.updating =false;
	//get all the other data for this dataset/variable combo
	this.state.lastVariable = this.state.variable;
	this.state.lastDataset = this.state.dataset;
	this.state.selectGlobal=false;

	if(this.state.grid.hasAxis('x')&&this.state.grid.hasAxis('y'))
	if(
		Math.abs((getMapXhi()-getMapXlo())-(this.state.grid.getHi("x") - this.state.grid.getLo("x")))<10&&
		Math.abs((getMapYhi()-getMapYlo())-(this.state.grid.getHi("y") - this.state.grid.getLo("y")))<5

	)
		this.state.selectGlobal=true;


	if(type=='plot') {
		this.expired=false;	
		document.getElementById('update').style.color='';
		document.getElementById('print').style.visibility='visible';
	}

}
/**
 * Method to query the server for an options object and pass json response to setOptionList
 * @param {string} optiondef Id of the option set to query the server for.
 */
LASUI.prototype.getOptions = function (optiondef, type, reset) {


	var url =  this.hrefs.getOptions.url + this.state.extra_args + 'opid=' + optiondef;

	
		var submit = document.createElement("INPUT");
		var cancel = document.createElement("INPUT");

		submit.type = "submit";
		submit.value=	"OK";
		submit.className = "LASSubmitInputNode";
		var strHandler = "this.setChangedOptions('"+ type+ "');this.hideOptions('"+ type+ "')";
		if(type!="plot") 
			strHandler += ";this.makeRequest({},'"+type+"')"
		submit.onclick = this.genericHandler.LASBind(this,strHandler);
	
		cancel.type = "submit";
		cancel.value=	"Cancel";
		cancel.className = "LASSubmitInputNode";
		cancel.onclick = this.genericHandler.LASBind(this,"this.cancelChangedOptions('"+ type+ "');this.hideOptions('"+ type+ "')");
		var reset = document.createElement("INPUT");
		reset.type = "submit";
		reset.onclick =  this.genericHandler.LASBind(this,"this.resetOptions('" +type + "')");
		reset.name = "Reset";
		reset.value = "Reset";
		while(this.refs.options[type].DOMNode.firstChild)
			this.refs.options[type].DOMNode.removeChild(this.refs.options[type].DOMNode.firstChild);
		this.refs.options[type].DOMNode.appendChild(submit);
		this.refs.options[type].DOMNode.appendChild(reset);
		this.refs.options[type].DOMNode.appendChild(cancel);
	

	if(!this.AJAX_cache[url]) {
		this.AJAX_cache[url]="waiting";
		if(!document.all)
			var req = new XMLHttpRequest(this);
		else
			var req = new ActiveXObject("Microsoft.XMLHTTP");
		req.onreadystatechange = this.AJAXhandler.LASBind(this, req, "this.AJAX_cache['"+url+"']=req.responseText;this.setOptionList(req.responseText,args[3],args[4],args[5]);",this.refs.options[type].DOMNode,type,reset);
		req.open("GET", this.hrefs.getOptions.url + this.state.extra_args + 'opid=' + optiondef);
		req.send(null);
	} else if (this.AJAX_cache[url]!="waiting")
		this.setOptionList(this.AJAX_cache[url],this.refs.options[type].DOMNode,type,reset);

}
/**
 * Method to create an option list in the tree and add it to the DOM
 * @param {object} strJson A json response compatible with LASGetOptionsResponse.js
 */
LASUI.prototype.setOptionList = function (strJson,DOMNode,type,reset) {


	document.getElementById('plotOptionsButton').style.visibility='visible';
	var table = document.createElement("TABLE");
	table.style.margin = "-4pt";
	table.style.marginLeft = "6pt";
	table.cellpadding = "0";
	table.cellspacing = "0";
	var tbody = document.createElement("TBODY");
	table.appendChild(tbody);
	DOMNode.appendChild(table);

	var response = eval("(" + strJson + ")");

	var setDefault = true;
	this.state.properties[type] = [];
	this.refs.options[type].options = new LASGetOptionsResponse(response);
	var ct = this.refs.options[type].options.getOptionCount();
	if(ct)
		for(var i=0;i<ct;i++)
			this.setOptionTRNode(this.refs.options[type].options.getOptionID(i),tbody,type,reset);

	if(this.newVariable&&this.onSetVariable)
               this.onSetVariable();
		 
}
/**
 * Method to create an option tree node and add it to the DOM
 * @param {string} id An option id
 */
LASUI.prototype.setOptionTRNode = function (id,TBODYNode,type,reset) {
	if(!this.refs.options.cache)
		this.refs.options.cache = {};
	if(!this.refs.options.cache[id]||reset)
	{
		this.refs.options.cache[id] =  this.refs.options[type].options.getOptionByID(id);

		this.refs.options.cache[id].TRNode = document.createElement("TR");
		this.refs.options.cache[id].TD1 = document.createElement("TD");
		this.refs.options.cache[id].TD1.width="45%";
		this.refs.options.cache[id].TD1.appendChild(document.createTextNode(this.refs.options.cache[id].title));
		this.refs.options.cache[id].TD2 = document.createElement("TD");
		if(this.refs.options.cache[id].menu) {
			this.refs.options.cache[id].SELECTNode = document.createElement("SELECT");
			this.refs.options.cache[id].SELECTNode.setAttribute('name', id);
  			if(!this.state.properties[type][id])
  				this.state.properties[type][id] = {"type" : "ferret", "value" : this.refs.options.cache[id].menu.item[0].values};
  			for (var i=0;i<this.refs.options.cache[id].menu.item.length;i++) {
   				var option = document.createElement("OPTION");
     				option.value=this.refs.options.cache[id].menu.item[i].values;
     				option.appendChild(document.createTextNode(this.refs.options.cache[id].menu.item[i].content));
    				//code branch for add() method differences between IE and FF
     				this.refs.options.cache[id].SELECTNode.appendChild(option);
     			}
			this.refs.options.cache[id].TD2.appendChild(this.refs.options.cache[id].SELECTNode);
		} else {
			if(!this.state.properties[type][id])
  				this.state.properties[type][id] = {"type" : "ferret", "value" : ""};
			this.refs.options.cache[id].INPUTNode = document.createElement("INPUT");
			this.refs.options.cache[id].INPUTNode.type = "text";
			this.refs.options.cache[id].INPUTNode.className="LASTextInputNode";
			this.refs.options.cache[id].TD2.appendChild(this.refs.options.cache[id].INPUTNode);
		}
		this.refs.options.cache[id].TD3 = document.createElement("TD");
		this.refs.options.cache[id].A = document.createElement("A");
		this.refs.options.cache[id].A.appendChild(this.info_icon.cloneNode(true));
		this.refs.options.cache[id].A.onclick = this.showOptionInfo.LASBind(this,this.refs.options.cache[id].help);
		this.refs.options.cache[id].TD3.appendChild(this.refs.options.cache[id].A);
		this.refs.options.cache[id].TRNode.appendChild(this.refs.options.cache[id].TD1);
		this.refs.options.cache[id].TRNode.appendChild(this.refs.options.cache[id].TD2);
		this.refs.options.cache[id].TRNode.appendChild(this.refs.options.cache[id].TD3);

	}

//cache everything in the plot, clone for others
		if(type=="plot") {
			if(this.refs.options.cache[id].SELECTNode)
				this.refs.options.cache[id].SELECTNode.onchange = this.setOption.LASBind(this,id,"plot", this.refs.options.cache[id]);
			if(this.refs.options.cache[id].INPUTNode)
				this.refs.options.cache[id].INPUTNode.onchange = this.setOption.LASBind(this,id,"plot", this.refs.options.cache[id]);
			TBODYNode.appendChild(this.refs.options.cache[id].TRNode);
		} else {
			var clone = this.clone(this.refs.options.cache[id]);

			clone.TD2.innerHTML="";

			if(clone.SELECTNode) {
				clone.SELECTNode.onchange = this.setOption.LASBind(this,id, type, clone);
				clone.SELECTNode.selectedIndex = this.refs.options.cache[id].SELECTNode.selectedIndex;
				clone.SELECTNode.onchange({"target": clone.SELECTNode},id, type, clone);
				clone.TD2.appendChild(clone.SELECTNode);
			}
			if(clone.INPUTNode) {
				clone.INPUTNode.onchange = this.setOption.LASBind(this,id, type, clone);
				clone.INPUTNode.value = this.refs.options.cache[id].INPUTNode.value;
				clone.INPUTNode.onchange({"target": clone.INPUTNode},id, type, clone);
				clone.TD2.appendChild(clone.INPUTNode);
			}
			while(clone.TRNode.firstChild)
				clone.TRNode.removeChild(clone.TRNode.firstChild);

			clone.TRNode.appendChild(clone.TD1);
			clone.TRNode.appendChild(clone.TD2);
			clone.TRNode.appendChild(this.refs.options.cache[id].TD3);
			TBODYNode.appendChild(clone.TRNode);
		}

			//first time, add it to the product

}
LASUI.prototype.showOptions = function(type)  {
	if(this.refs.options[type].options){
		this.toggleUIMask('');
		document.getElementById(type + 'Options').style.display='';
	}
}
LASUI.prototype.hideOptions= function(type)  {
	this.refs.options[type].DOMNode.style.display='none';
	this.toggleUIMask('none');


}
LASUI.prototype.resetOptions= function(type)  {
	while(this.refs.options[type].DOMNode.firstChild)
		this.refs.options[type].DOMNode.removeChild(this.refs.options[type].DOMNode.firstChild);

	this.getOptions(this.state.operations.getOperationByID(this.state.operation[type]).optiondef.IDREF, type,true);
	this.showOptions(type);

}
LASUI.prototype.showOptionInfo = function(evt) {
	if(this.optionInfo)
		if(this.optionInfo.parentNode)
			this.optionInfo.parentNode.removeChild(this.optionInfo);

	this.optionInfo = document.createElement("DIV");
	document.body.appendChild(this.optionInfo);
	this.optionInfo.className = "LASPopupDIVNode";
	this.optionInfo.style.left = (this.optionInfo.offsetLeft + 3) + "px";
	this.optionInfo.style.top =(this.optionInfo.offsetTop + 3) + "px";
	var close = document.createElement("INPUT");
	var center = document.createElement("CENTER");
	close.type = "submit";
	close.onclick = this.hideOptionInfo.LASBind(this,this.optionInfo);
	close.name = "Close";
	close.value = "Close";
	this.optionInfo.innerHTML += arguments[1];
	center.appendChild(close);
	this.optionInfo.appendChild(center);
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
	if(evt.target)
		var node = evt.target;
	else if(evt.srcElement)
		var node = evt.srcElement;
	else
		return;



	if(args[3].SELECTNode)
		this.state.newproperties[args[2]][id]={"type" : "ferret", "value" : node.options[node.selectedIndex].value};
	else
		this.state.newproperties[args[2]][id]={"type" : "ferret", "value" : node.value};

}
LASUI.prototype.setChangedOptions = function (type) {
	var ct = 0;
	for(var id in this.state.newproperties[type]) {
		this.state.properties[type][id]=this.state.newproperties[type][id];
		ct++;
	}

	if(!this.updating&&type=="plot"&&ct>0)
		if(this.autoupdate)
			this.makeRequest();
		else
			this.showUpdateLink();
	this.state.newproperties = {"plot":[],"external":[],"download":[]};

}
LASUI.prototype.cancelChangedOptions = function () {
	for(var type in this.state.newproperties)
		for(var id in this.state.newproperties[type]) {
			if(this.refs.options.cache[id]&&this.state.properties[type][id]) {
				if(this.refs.options.cache[id].SELECTNode) {
					for(var i=0; i< this.refs.options.cache[id].SELECTNode.length;i++)
							if(this.refs.options.cache[id].SELECTNode.options[i].value==this.state.properties[type][id].value) {
								this.refs.options.cache[id].SELECTNode.selectedIndex = i;
								this.refs.options.cache[id].SELECTNode.options[i].selected = true;
							}
				} else
					if(this.refs.options.cache[id].INPUTNode)
						this.refs.options.cache[id].INPUTNode.value = this.state.properties[type][id].value;
			}
		}
}


LASUI.prototype.onafterdraw = function (evt) {

	this.state.selection.x.min=getMapXlo();
	this.state.selection.x.max=getMapXhi();
	this.state.selection.y.min=getMapYlo();
	this.state.selection.y.max=getMapYhi();

	if(!this.updating)
		if(this.autoupdate) {
			this.makeRequest();
		} else
		this.showUpdateLink();

}
/**
 * Event handler called on depth widget/menu changes
 * @param {object} evt The event object
 */
LASUI.prototype.handleDepthRangeChange = function (evt) {

	if(!this.updating)if(this.autoupdate) {

		this.makeRequest();

	}	else
		this.showUpdateLink();

}
/**
 * Event handler called on depth widget/menu changes
 * @param {object} evt The event object
 */
LASUI.prototype.handleDepthChange = function (evt) {

	if(!this.updating)if(this.autoupdate)
		this.makeRequest();
	else
		this.showUpdateLink();

}
/**
 * Event handler called on date range widget/menu changes
 * @params {object} evt The event object
 */
LASUI.prototype.handleDateRangeChange = function (evt) {

	if(!this.updating)if(this.autoupdate)
		this.makeRequest();
	else
		this.showUpdateLink();

}
/**
 * Event handler called on date widget/menu changes
 * @params {object} evt The event object
 */
LASUI.prototype.handleDateChange = function (evt) {

	if(!this.updating)if(this.autoupdate)
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
	if(this.state.datasets[this.state.dataset].getChildByID(this.state.variable).grid_type=="scattered")
		return;

	this.refs.analysis.enabled = true;
	var reset=false;
	document.getElementById('Animation').style.visibility='hidden';
	for(var a in this.refs.analysis.axes)
		this.refs.analysis.axes[a].style.display="none";

	document.getElementById("analysis_axes").selectedIndex=0;	
	//turn on the other axes switches
	for(var d=0;d<this.state.grid.response.grid.axis.length;d++) {
		this.refs.analysis.axes[this.state.grid.response.grid.axis[d].type.toLowerCase()].style.display="";

	}
	if(this.state.grid.hasAxis('x')&&this.state.grid.hasAxis('y'))
		this.refs.analysis.axes.xy.style.display="";
	
	document.getElementById(this.anchors.analysis).style.display="";
	
	//select the analysis view	
	if(this.state.analysis.axes.x&&this.state.analysis.axes.y&&this.state.grid.hasAxis('x')&&this.state.grid.hasAxis('y'))
		this.selectAnalysisAxis(null,"xy",true);
	else
		this.selectAnalysisAxis(null,"xy",false);

	for(var a in this.state.analysis.axes)
			if(a!='xy')
				if(this.state.grid.hasAxis(a))
					this.selectAnalysisAxis(null,a,true);
				else
					this.selectAnalysisAxis(null,a,false);
	

	if(this.state.analysis.type)
		this.selectAnalysisType(null,this.state.analysis.type,true);

}
LASUI.prototype.hideAnalysis = function () {



	this.refs.analysis.enabled = false;
	var reset = false;

	if(!this.state.operations.response.operations.error&&this.state.grid.hasAxis('t'))
		for(var i=0;i<this.state.operations.getOperationCount();i++)
			if(this.state.operations.getOperationName(i)=="Animation")
				document.getElementById('Animation').style.visibility="visible";

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
}
LASUI.prototype.selectAnalysisType = function (evt) {
	if(evt) {
		if(evt.target)
			var DOMNode = evt.target
		else if (evt.srcElement)
			var DOMNode = evt.srcElement;

		this.state.analysis.type = DOMNode.options[DOMNode.selectedIndex].value;
		this.state.analysis.name = DOMNode.options[DOMNode.selectedIndex].innerHTML;	
	}		
		 this.state.analysis.enabled = false;
		if(this.state.analysis.type&&this.state.analysis.axes) {
			
			for(var axis in this.state.analysis.axes)
                                if(this.state.analysis.axes[axis])
                                        this.state.analysis.enabled = true;
			if(this.state.analysis.enabled) {
				for(var axis in this.state.analysis.axes)
					if(!this.state.view.widgets.indexOf(axis)>=0&&this.state.analysis.axes[axis])
						this.state.view.widgets+=axis;
			} 
		}
		
		if(this.state.analysis.enabled)
         		this.updateConstraints(this.state.view.widgets);
	        else
               		this.updateConstraints(this.state.view.plot);

			
}
LASUI.prototype.selectAnalysisAxis = function (evt) {
	var axes =arguments[1];
	if(axes=="" || axes == {} || axes==null){
		try {axes =evt.target.value;}
		catch (e) {axes = ""};
	}
	if(axes=="" || axes == {} || axes==null){
			try {axes =evt.srcElement.value;}
			catch (e) {axes = ""};
	}
        this.state.analysis.axes = {};
	var changeVis=false;

	if((arguments[2]==true||evt)) {
		//turn the analysis axes on
		for(var i=0; i< axes.length; i++) {
			this.state.analysis.axes[axes.charAt(i)] = true;//this.refs.analysis.type.op.value;
			if(this.state.view.plot.indexOf(axes.charAt(i))>=0&&this.state.analysis.type)
				changeVis = true;

			if(this.state.view.widgets.indexOf(axes.charAt(i))<0&&this.state.analysis.type)
				this.state.view.widgets+=axes.charAt(i);
		}
		
	}

	this.state.analysis.enabled = false; 
	if(this.state.analysis.type&&this.state.analysis.axes) {
                        for(var axis in this.state.analysis.axes)
				if(this.state.analysis.axes[axis])
					this.state.analysis.enabled = true;
        } 

	if(this.state.analysis.enabled)
		this.updateConstraints(this.state.view.widgets);
	else
		this.updateConstraints(this.state.view.plot);

	if(changeVis)
		this.setVisualization(axes);

}
LASUI.prototype.setVisualization = function (d) {

	var stop = false;
	var bestView = "";
	if(this.state.view.plot.indexOf(d)>=0)
		 bestView = this.state.view.plot.substr(0,this.state.view.plot.indexOf(d)) + this.state.view.plot.substr(this.state.view.plot.indexOf(d)+d.length,this.state.view.plot.length);

	if(bestView == "")
		for(var i in this.state.grid.response.grid.axis)
			if(d.indexOf(this.state.grid.response.grid.axis[i].type)<0)
				bestView=this.state.grid.response.grid.axis[i].type;

	for(var t in this.products)
		for (var p in this.products[t])
			if(this.products[t][p].view==bestView  && !stop)
				if(this.refs.operations.plot.children[p])
					if(this.refs.operations.plot.children[p].radio){
						this.refs.operations.plot.children[p].radio.checked = true;
						this.refs.operations.plot.children[p].radio.onclick({"srcElement" : this.refs.operations.plot.children[p].radio});
						stop = true;
					}
	if(!stop)
	   for(var t in this.products)
                for (var p in this.products[t])
                        if(this.products[t][p].view.indexOf(d)<0  && !stop)
                                if(this.refs.operations.plot.children[p])
                                        if(this.refs.operations.plot.children[p].radio){
                                                this.refs.operations.plot.children[p].radio.checked = true;
                                                this.refs.operations.plot.children[p].radio.onclick({"srcElement" : this.refs.operations.plot.children[p].radio});
                                                stop = true;
                                        }



	this.refs.analysis.axes[d].selected=true;
}
/**
 * Method to collapse a tree node
 * @param {object} obj Object reference in this.refs
 */
LASUI.prototype.collapse = function (obj) {
		if(obj.ULNode) obj.ULNode.style.display = "none";
		if(obj.IMGNode) 
			if(obj.IMGNode.src != "JavaScript/ui/plus.gif")
				obj.IMGNode.src = "JavaScript/ui/plus.gif";
		obj.isExpanded = false;
}
/**
 * Method to expand a tree node
 * @param {object} obj Object reference in this.refs
 */
LASUI.prototype.expand = function (obj) {
	if(obj.ULNode) obj.ULNode.style.display = "";
	if(obj.IMGNode) 
		if(obj.IMGNode.src != "JavaScript/ui/minus.gif")
			obj.IMGNode.src = "JavaScript/ui/minus.gif";
	obj.isExpanded = true;
}
//generic function to clone objects
LASUI.prototype.clone = function (obj) {
	if(typeof obj !='object')
		return obj;
	if(obj.cloneNode)
		return obj.cloneNode(true);

	var myclone = new Object();

	for(var i in obj)
		myclone[i] = this.clone(obj[i]);
	return myclone;
 }
LASUI.prototype.urlencode = function ( str ) {
    var histogram = {}, histogram_r = {}, code = 0, tmp_arr = [];
    var ret = str.toString();

    var replacer = function(search, replace, str) {
        var tmp_arr = [];
        tmp_arr = str.split(search);
        return tmp_arr.join(replace);
    };

    // The histogram is identical to the one in urldecode.
    histogram['!']   = '%21';
    histogram['%20'] = '+';

    // Begin with encodeURIComponent, which most resembles PHP's encoding functions
    ret = encodeURIComponent(ret);

    for (search in histogram) {
        replace = histogram[search];
        ret = replacer(search, replace, ret) // Custom replace. No regexing
    }

    // Uppercase for full PHP compatibility
    return ret.replace(/(\%([a-z0-9]{2}))/g, function(full, m1, m2) {
        return "%"+m2.toUpperCase();
    });
}
