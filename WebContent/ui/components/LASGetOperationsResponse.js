/*
 *
 * @author Jeremy Malczyk
 *
 * A wrapper to handle the LASGetOperationsResponse
 * 
 *
 */

function LASGetOperationsResponse(JSONObject) {

  this.response = JSONObject;
  if(!this.response) return false;
 		
// Add methods to this object
  this.getAllOperations = LASGetOperationsResponse_getAllOperations;  
  this.getOperationCount = LASGetOperationsResponse_getOperationCount;  
  this.getOperation = LASGetOperationsResponse_getOperation; 
  this.getOperationID = LASGetOperationsResponse_getOperationID;
  this.getOperationByID = LASGetOperationsResponse_getOperationByID;  	  	
  this.getOperationName = LASGetOperationsResponse_getOperationName;  
}
////////////////////////////////////////////////////////////
// Methods of the LASGetOperationsResponse object.
////////////////////////////////////////////////////////////


////////////////////////////////////////////////////////////
// Public methods
////////////////////////////////////////////////////////////




/**
 * Returns an array of all Operation objects in the response.
 * @return value if the categories object has properties.
 * @type Object
 */
function LASGetOperationsResponse_getAllOperations() {
	if(this.response.operations.operation)
		return this.response.operations.operation;
	else
		return null;
}
/**
 * Returns the number of Operations available.
 * @return Value or -1.
 * @type Value
 */
function LASGetOperationsResponse_getOperationCount() {
	if(this.getAllOperations())
		return this.getAllOperations().length;
	else
		return -1;
}
/**
 * Returns a Operation object.
 * @param {integer} index of the Operation
 * @return Object
 * @type Object or null
 */
function LASGetOperationsResponse_getOperation(i) {
	if(i>=0&&i<this.getOperationCount())
		return this.getAllOperations()[i];
	else
		return null;
}

/**
 * Returns a Operation ID from the response.
 * @param {integer} index of the Operation
 * @return value True if the &lt;v&gt; element is present.
 * @type Boolean
 */
function LASGetOperationsResponse_getOperationID(i) {
   if(this.getOperation(i)) {
   	return this.getOperation(i).ID;
	}   else
   	return null;
}
/**
 * Returns a Operation object from the response.
 * @param {string} the id of the Operation
 * @return Object or null
 * @type Object
 */
function LASGetOperationsResponse_getOperationByID(id) {
   for(var i=0;i<this.getOperationCount();i++) {
   	if(this.getOperation(i).ID == id)
			return this.getOperation(i);
	} 
   	return null;
}
/**
 * Returns the name of a Operation
 * @param {int} the index of the Operation
 * @return string 
 * @type string
 */
function LASGetOperationsResponse_getOperationName(i) {
  if(this.getOperation(i)) {
		return this.getOperation(i).name	
	} else
		return null;
}
