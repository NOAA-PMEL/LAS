package org.iges.anagram;

import java.util.*;
import org.w3c.dom.*;
import org.iges.util.*;
import org.iges.anagram.filter.AnalysisFilter;
/** An extension of Setting which represents a set of privileges that can be 
 *  associated with a given request. */
public class Privilege 
    extends Setting {

    /** Creates a privilege set with the given name, 
     * based on the XML tag provided. */
    public Privilege(String name, Element xml) {
	super(xml);
	this.name = name;
	allowPaths = sortPathTags("allow");
	denyPaths = sortPathTags("deny");
        allowCommands = sortCommandTags("allow");
        denyCommands = sortCommandTags("deny");
    }

    /** Used by the privilege manager when building the privilege
     *  hierarchy. 
     */
    protected void setParent(Privilege parent) 
	throws AnagramException {
	
	Privilege ancestor = parent;
	while (ancestor != null) {
	    if (ancestor == this) {
		throw new AnagramException("circular relationship between " + 
					   "privileges " + this.getName() + 
					   " and " + parent.getName());
	    }
	    ancestor = parent.getParent();
	} 

	this.parent = parent;
    }
    
    protected Privilege getParent() {
	return parent;
    }

    /** Returns the name of this privilege set. */
    public String getName() {
	return name;
    }

    public String toString() {
	return name;
    }

    public String getAttribute(String name, String defaultValue) {
	String value = super.getAttribute(name, "");
	if (value.equals("")) {
	    if (parent == null) {
		value = defaultValue;
	    } else {
		value = getParent().getAttribute(name, defaultValue);
	    }
	}
	return value;
    }

    public long getNumAttribute(String name, long defaultValue) {
	long value = super.getNumAttribute(name, Long.MIN_VALUE);
	if (value == Long.MIN_VALUE) {
	    if (parent == null) {
		value = defaultValue;
	    } else {
		value = getParent().getNumAttribute(name, defaultValue);
	    }
	}
	return value;
    }

    /** Returns true if this privilege set permits access to the catalog
     *  path given. */
    public boolean allowsPath(String path) 
        throws ModuleException {
        path = standardizePath(path);
        if(path.indexOf(AnalysisFilter.ANALYSIS_PREFIX)<0||path.startsWith("http://"))
           return allowsPath(path, null, null);
        else
           return Server.getServer().getTool().allowAnalysis(path,this); 
    }

    protected boolean allowsPath(String path, String childAllowPath, String childDenyPath) {
        String localAllow=getClosestPathMatch(childAllowPath, allowPaths, path);
        String localDeny=getClosestPathMatch(childDenyPath, denyPaths, path);
        boolean allAllowed = allowPaths.contains("all");
        boolean allDenied = denyPaths.contains("all");

        if(allAllowed||allDenied){
            if(localAllow==null){
               if(allAllowed)
                  return localDeny==null;
               else
                  return false;
            }
            if(localDeny==null)
               return true;

            if(localAllow.length()>=localDeny.length())
               return true;
            else
               return false;
        }

	if (parent != null) {
	    return parent.allowsPath(path, localAllow, localDeny);
	} else {
            if(localDeny==null)
                return true;
            if(localAllow==null)
                return false;

            if(localAllow.length()>=localDeny.length())
               return true;
            else
               return false;
	}
    }

    /** Returns true if there is any possibility that the privilege
     *  set will permit access to a sub-path of the path given.
     */
    public boolean everAllowsPath(String path) {
        path = standardizePath(path);
        try {
	   if (allowsPath(path)) {
	       return true;
	   } else {
	       return checkForAllowedSubPath(path);
	   }
        }
        catch (Exception e){
           return false;
        }
    }

    protected boolean checkForAllowedSubPath(String path) {
        if(allowPaths.contains("all"))
           return true;
	Iterator it = allowPaths.headSet(path).iterator();
	while (it.hasNext()) {
	    String next = (String)it.next();
	    if (isParentPath(next,path)) {
		return true;
	    }
	}
        if(denyPaths.contains("all"))
           return false;
	if (parent != null) {
	    return parent.checkForAllowedSubPath(path);
	} else {
	    return false;
	}
    }

    protected String getClosestPathMatch(String childMatch, SortedSet pathSet, 
				            String path) {
        String localMatch = null;

	Iterator it = pathSet.tailSet(path).iterator();
	while (it.hasNext()) {
	    String next = (String)it.next();
	    if (isParentPath(path, next)) {
		localMatch = next;
	    }
	}

        if(childMatch==null)
            return localMatch;
        if(localMatch==null)
            return childMatch;
        if(localMatch.length()>=childMatch.length())
            return localMatch;
        else
            return childMatch;
    }

    protected boolean isParentPath(String childPath, String parentPath) {

        if(childPath.equals(parentPath)) {
            return true;
	}

        if(childPath.startsWith(parentPath)) {
            char ch = childPath.charAt(parentPath.length());
            if(ch=='/'){
                return true;
            }
            else if(parentPath.startsWith("http:/")&&ch==':'){
                 return true;
            }
            else {
                return false;
            }
	} else {
            return false;
	}
    }

    protected SortedSet sortPathTags(String tagName) {

	SortedSet returnVal = new TreeSet(Collections.reverseOrder());
	Iterator it = getSubSettings(tagName).iterator();
	while (it.hasNext()) {
	    String path = ((Setting)it.next()).getAttribute("path");
            if(path.equals("")){
                continue;
	    }
            path=standardizePath(path);
	    returnVal.add(path);
	}
	return returnVal;
    }

    /** Gets a standardize path from a input path */
    protected String standardizePath(String path) {
        path = FDSUtils.stripSpacesFrom(path);
        if(!(path.equals("")||path.equals("/")||path.startsWith("http:/")||path.equals("all"))){
            if(!path.startsWith("/")) {
                path = "/" + path;
            }

	    if(path.endsWith("/")) {
                path = path.substring(0, path.length()-1);
	    }
        }
        return path;
    }

    public boolean allowsCommand(String cmmd) 
        throws AnagramException {

        cmmd=FDSUtils.compactSpacesFor(cmmd);

        return allowsCommand(cmmd, null, null);

    }

    protected boolean allowsCommand(String cmmd, String childAllowCommand, String childDenyCommand) {
        if(cmmd.toUpperCase().indexOf("SPAWN")>=0)
           return false;

        String localAllow=getClosestCommandMatch(childAllowCommand, allowCommands, cmmd);
        String localDeny=getClosestCommandMatch(childDenyCommand, denyCommands, cmmd);
        boolean allAllowed = allowCommands.contains("all");
        boolean allDenied = denyCommands.contains("all");

        if(allAllowed||allDenied){
            if(localAllow==null){
               if(allAllowed)
                  return localDeny==null;
               else
                  return false;
            }
            if(localDeny==null)
               return true;

            if(localAllow.length()>=localDeny.length())
               return true;
            else
               return false;
        }

	if (parent != null) {
	    return parent.allowsCommand(cmmd, localAllow, localDeny);
	} else {
            if(localDeny==null)
                return true;
            if(localAllow==null)
                return false;

            if(localAllow.length()>=localDeny.length())
               return true;
            else
               return false;
	}
    }

    protected String getClosestCommandMatch(String childMatch, SortedSet cmmdSet, 
				            String cmmd) {
        String localMatch = null;

	Iterator it = cmmdSet.tailSet(cmmd).iterator();
	while (it.hasNext()) {
	    String next = (String)it.next();
	    if (isParentCommand(cmmd, next)) {
		localMatch = next;
	    }
	}

        if(childMatch==null)
            return localMatch;
        if(localMatch==null)
            return childMatch;
        if(localMatch.length()>=childMatch.length())
            return localMatch;
        else
            return childMatch;
    }

    protected boolean isParentCommand(String childCommand, String parentCommand) {

        if(childCommand.equals(parentCommand)) {
            return true;
	}

        if(childCommand.startsWith(parentCommand)) {
            char c=childCommand.charAt(parentCommand.length());
            if(!((c>='0'&&c<='9')||(c>='a'&&c<='z')||(c>='A'&&c<='Z')))
                return true;
            else
                return false;
	} else {
            return false;
	}
    }

    protected SortedSet sortCommandTags(String tagName) {

	SortedSet returnVal = new TreeSet(Collections.reverseOrder());;
	Iterator it = getSubSettings(tagName).iterator();
	while (it.hasNext()) {
	    String commands = ((Setting)it.next()).getAttribute("commands");
            if(commands.equals("")){
                continue;
	    }
            StringTokenizer cmmdList = new StringTokenizer(commands,",");
	    while (cmmdList.hasMoreTokens()) {
                String cmmd = cmmdList.nextToken();
                if(cmmd != null && !cmmd.equals("")){
                   try {
                      cmmd=FDSUtils.compactSpacesFor(cmmd);
                   }
                   catch(AnagramException ae){
                      continue;
                   }
	           returnVal.add(cmmd);
		}
	    }
	}
	return returnVal;
    }


    protected SortedSet allowPaths;
    protected SortedSet denyPaths;

    protected SortedSet allowCommands;
    protected SortedSet denyCommands;

    protected String name;
    protected Privilege parent;

}
