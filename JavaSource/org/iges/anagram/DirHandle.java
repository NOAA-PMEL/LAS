package org.iges.anagram;

import java.util.*;

/** Represents a sub-directory of the server catalog. 
 *  Directory names do not end with '/'. 
 *  The one exception is that the root directory of the catalog has 
 *  the name "/". 
 */
public class DirHandle
    extends Handle {

    public DirHandle(String completeName)
	throws AnagramException {

	super(completeName);
	this.entries = new TreeMap();
    }

    /** Used by Catalog */
    public void add(Handle handle) {
	entries.put(handle.getCompleteName(), handle);
    }

    /** Used by Catalog */
    public void remove(String completeName) {
	entries.remove(completeName);
    }

    /** Returns true if this directory contains a handle that
     *  matches the name given */     
    public boolean contains(String completeName) {
	return entries.keySet().contains(completeName);
    }

    
    /** Used by Catalog */
    public Handle get(String completeName) {
	return (Handle)entries.get(completeName);
    }

    public boolean isEmpty() {
        return getEntries(false).size()==0;
    }

    /** Returns a Map containing all entries in this directory.
     *  The keys are the names of the entries, and the values are the
     *  Handle objects associated with those names.
     *  @param recurse If true, the Map will also contain all entries in all
     *   sub-directories. In this case, handles for the subdirectories 
     *   themselves will be omitted.
     */
    public Map getEntries(boolean recurse) {
	if (recurse) {
	    SortedMap recursedEntries = new TreeMap();
	    Iterator it = entries.values().iterator();
	    while (it.hasNext()) {
		Handle next = (Handle)it.next();
                recursedEntries.put(next.getCompleteName(), next);
		if (next instanceof DirHandle) {
		    recursedEntries.putAll(((DirHandle)next).getEntries(true));
		}
	    }
	    return recursedEntries;
	} else {
	    return entries;
	}
    }

    public boolean equals(Object o) {
        if(o==null)
           return false;
        if(!o.getClass().getName().equals(this.getClass().getName()))
           return false;
        DirHandle o1 = (DirHandle)o;
        if(isEqual(o1.getCompleteName(), getCompleteName()))
           return true;
        else
           return false;
    }

    protected SortedMap entries;
}
