package org.iges.anagram;

import org.iges.util.ChainedException;

/** The base class for all exceptions thrown by Anagram objects. */
public class AnagramException 
    extends ChainedException {

    /** Creates an AnagramException with no message */
    public AnagramException() {
	super();
    }
    
    /** Creates an AnagramException with the message given */
    public AnagramException(String message) {
	super(message);
    }
    
    /** Creates an AnagramException with the message and cause given */
    public AnagramException(String message, Throwable cause) {
	super(message, cause);
    }

}
