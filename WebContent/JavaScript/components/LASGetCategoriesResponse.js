/*
 *
 * @author Jeremy Malczyk
 *
 */

function LASGetCategoriesResponse(JSONObject) {

  this.response = JSONObject;
  if(!this.response) return false;
 		
// Add methods to this object
  this.getDatasetID = LASGetCategoriesResponse_getDatasetID;  //what kind of category is this -- category or dataset?
  this.getCategoryType = LASGetCategoriesResponse_getCategoryType;  //what kind of category is this -- category or dataset?
  this.getCategorySize = LASGetCategoriesResponse_getCategorySize; //how many children does this category or dataset have?
  this.getChild = LASGetCategoriesResponse_getChild;  //return a child of this category (pass index # < getCategorySize)	
  this.getChildrenType = LASGetCategoriesResponse_getChildrenType;  //what kind of children does the category have (dataset, variable, or category)?
  this.getChildID =  LASGetCategoriesResponse_getChildID;  //the ID of the child
  this.getChildType =  LASGetCategoriesResponse_getChildType;  //the type of the child
  this.getChildChildrenType = LASGetCategoriesResponse_getChildChildrenType;  //what kind of children (dataset, variable, or category)?
  this.getChildName = LASGetCategoriesResponse_getChildName;  //what is the name of this child category?
}
////////////////////////////////////////////////////////////
// Methods of the LASGetCategoriesResponse object.
////////////////////////////////////////////////////////////


////////////////////////////////////////////////////////////
// Public methods
////////////////////////////////////////////////////////////




/**
 * Returns the string type of the root category.
 * @return value if the categories object has properties.
 * @type String or Boolean
 */
function LASGetCategoriesResponse_getCategoryType() {
	if(this.response.categories.category)
		if(this.response.categories.category[0])
			if(this.response.categories.category[0].dataset)
				return "dataset";
			else
				return "category";
		else
			return null;
	else
		return null;
}
/**
 * Returns the string type of the root category.
 * @return value if the categories object has properties.
 * @type String or Boolean
 */
function LASGetCategoriesResponse_getDatasetID() {
	if(this.getCategoryType() == "dataset")
		return this.response.categories.category[0].dataset.ID;
	else
		return "category";
}
/**
 * Returns Integar length of the category children.
 * @return value if this.response.categories[this.getCategoryType()] exists, else returns Boolean false
 * @type Int or Boolean
 */
function LASGetCategoriesResponse_getCategorySize() {
	if(this.getCategoryType() == "dataset") {
		if(this.response.categories.category[0].dataset.variables.variable)
			return this.response.categories.category[0].dataset.variables.variable.length;
	} else 
		if(this.response.categories[this.getCategoryType()])
			return this.response.categories[this.getCategoryType()].length;	
		else
			return false;
}

/**
 * Returns a category/dataset or variable object from the response.
 * @param {integer} index of child eleement
 * @return value True if the &lt;v&gt; element is present.
 * @type Boolean
 */
function LASGetCategoriesResponse_getChild(i) {
   if(this.getCategoryType()=="dataset")
   	return this.response.categories.category[0].dataset.variables.variable[i];
   else
   	if(this.response.categories[this.getCategoryType()]);
   		return this.response.categories[this.getCategoryType()][i];
   	return false;
}

/**
 * Returns the type of children this category has
 * @return string "dataset", "category", or "variable"
 * @type string
 */
function LASGetCategoriesResponse_getChildrenType(i) {
   switch(this.getCategoryType()) {
   	case "category": return "categories"; break;
   	case "dataset": return "variables"; break;
   	default: return false;
   }
}
/**
 * Returns the type of children this category has
 * @return string "dataset", "category", or "variable"
 * @type string
 */
function LASGetCategoriesResponse_getChildType(i) {
   switch(this.getChildChildrenType(i)) {
   	case "variables": return "dataset"; break;
   	case "categories": return "category"; break;
   	default: return false;
   }
}
/**
 * Returns the type of children of the child referenced by the i parameter
 * @param {integer} index of the child
 * @return string type of children
 * @type string
 */
function LASGetCategoriesResponse_getChildChildrenType(i) {
  if(this.getChild(i).children) return this.getChild(i).children;
  else return false;
}

/**
 * Returns the ID of the child referenced by the i parameter
 * @param {integer} index of the child
 * @return string id of child
 * @type string
 */
function LASGetCategoriesResponse_getChildID(i) {
  if(this.getChild(i).ID) return this.getChild(i).ID;
}

/**
 * Returns the ID of the child referenced by the i parameter
 * @param {integer} index of the child
 * @return string id of child
 * @type string
 */
function LASGetCategoriesResponse_getChildName(i) {
  if(this.getChild(i).ID) return this.getChild(i).name;
}
