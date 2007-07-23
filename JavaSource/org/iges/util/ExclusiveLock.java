package org.iges.util;
import java.util.*;



/** An extension of the thread-lock concept
 *  which supports the concept of exclusive vs non-exclusive locking. 
 *  This concept is a way to eliminate unnecessary thread blocks 
 *  on operations that do not modify an object's state.<p>
 * 
 *  Non-exclusive locks may be simultaneously held on the same Locker by any 
 *  number of threads.<p>
 * 
 *  In contrast, only one thread at a time may hold an exclusive lock, 
 *  and the granting 
 *  of an exclusive lock guarantees that all non-exclusive locks have been
 *  released. <p>
 * 
 *  Furthermore, requests for exclusive locks take priority over
 *  requests for non-exclusive locks.<p>
 *  
 *  The envisioned use of this system is to require exclusive locks for all 
 *  state-change operations on an
 *  object, and non-exclusive locks for all state-query operations. It is then
 *  possible to guarantee that:<p>
 *  
 *  1) any number of threads may simultaneously query the object's state<br>
 *  2) no two threads can simultaneously attempt to change 
 *     the object's state<br>
 *  3) the object will never change state while it is being queried <br>
 *  4) any thread can temporarily block the initiation of new query operations
 *     in order to change the Object's state <p>
 *
 *  The lock keeps track of how many times <code>lock()</code>, <code>release()</code>,
 *  <code>lockExclusive()</code> and <code>releaseExclusive()</code> have been called.
 *  So this lock can be used in recursive functions.<p>
 *
 *  Modified by Yonghua Wei (<a href="mailto:Yonghua.Wei@noaa.gov">Yonghua.Wei@noaa.gov</a>)so that lock can be used in recursive functions
 *  and automative transition between exclusive mode and shared mode.<p>
 */ 

public class ExclusiveLock {

    /** Creates a new ExclusiveLock */
    public ExclusiveLock() {
	this.exclusive = null;
        this.numExclusiveRequests = 0;
	this.locks = new HashMap();
        this.storedLocks = new HashMap();
    }

    /** Creates a new ExclusiveLock with sufficient storage for 
     *  the expected maximum number of non-exclusive locks, to prevent
     *  frequent reallocation of internal storage.
     */
    public ExclusiveLock(int expectedMaxLocks) {
	this.exclusive = null;
        this.numExclusiveRequests = 0;
	this.locks = new HashMap(expectedMaxLocks);
	this.storedLocks = new HashMap(expectedMaxLocks);
    }

    /** Obtains an exclusive lock for the current thread, 
     *  blocking until the lock is available. Requests for non-exclusive locks
     *  will block starting from when this method is <i>called</i> (not  
     *  from when it returns), until the resulting exclusive
     *  lock is released. If the current thread 
     *  already owns an exclusive lock, does nothing. */
    public synchronized void lockExclusive() {
	if (isLockedExclusive()) {
            numExclusiveRequests++;
	    if (DEBUG) debug("locked ex " + numExclusiveRequests);
	    return;
	}
        if(isLocked()){
            int numLockRequests = ((Integer)locks.get(Thread.currentThread())).intValue();
            storedLocks.put(Thread.currentThread(),new Integer(numLockRequests));
            locks.remove(Thread.currentThread());
            if(locks.size()==0){
                notifyAll();
	    }
	}
	while (exclusive != null) {
	    if (DEBUG) debug("block on ex for ex");
	    try {
		this.wait(0);
	    } catch (InterruptedException ie) {}
	}
	exclusive = Thread.currentThread();
	while (locks.size() >0) {
	    if (DEBUG) debug("block on non-ex for ex");
	    try {
		this.wait(0);
	    } catch (InterruptedException ie) {}
	}
        numExclusiveRequests = 1;
	if (DEBUG) debug("locked ex 1");
    }
	
    /** Test if current thread owns an exclusive lock. 
     * @return True if the current thread owns an exclusive
     *  lock. */
    public synchronized boolean isLockedExclusive() {
	return exclusive == Thread.currentThread();
    }
		
    /** Tries to obtain an exclusive lock for the current thread. 
     *  This method always returns immediately but does not 
     *  guarantee that the lock will be obtained. If the current thread 
     *  already owns an exclusive lock, does nothing.
     *  @return True if the exclusive lock was succesfully obtained. 
     */
    public synchronized boolean tryLockExclusive() {
	if (isLockedExclusive()) {
	    numExclusiveRequests++;
            if (DEBUG) debug("locked ex " + numExclusiveRequests);
	    return true;
	}
	if (exclusive == null && 
             ( locks.size()==0 ||
               (locks.size()==1&&locks.containsKey(Thread.currentThread())))) {
            if(locks.size()==1&&locks.containsKey(Thread.currentThread())){
                int numLockRequests = ((Integer)locks.get(Thread.currentThread())).intValue();
                storedLocks.put(Thread.currentThread(),new Integer(numLockRequests));
                locks.remove(Thread.currentThread());
	    }
	    exclusive = Thread.currentThread();
	    numExclusiveRequests = 1;
	    if (DEBUG) debug("try for ex succeeded");
	    return true;
        } else {
	    if (DEBUG) debug("try for ex failed");
	    return false;
	}
    }

    /** Tries to obtain an exclusive lock for the current thread.
     *  This method will return within the timeout given, more or
     *  less, but does not guarantee that the lock will be
     *  obtained. If the current thread already owns an exclusive
     *  lock, does nothing.
     *  @param timeout Maximum time in milliseconds to wait for an
     *  exclusive lock
     *  @return True if the exclusive lock was succesfully obtained. 
     */
    public synchronized boolean tryLockExclusive(long timeout) {
	if (isLockedExclusive()) {
	    numExclusiveRequests++;
	    return true;
	}
        
	if (DEBUG) debug("trying " + timeout + "ms for ex");
	long start = System.currentTimeMillis();
	long remaining = timeout;
	while (remaining > 0) {
	    if (tryLockExclusive()) {
		return true;
	    }
	    if (DEBUG) debug("waiting " + remaining + "ms for ex");
	    try {
		this.wait(remaining);
	    } catch (InterruptedException ie) {}
	    remaining = timeout - (System.currentTimeMillis() - start);
	}
	if (DEBUG) debug("timed out waiting for ex");
	return false;
    }

    /**Test if the current thread owns a non-exclusive lock. 
     *@return True if the current thread owns a non-exclusive lock. 
     */
    public synchronized boolean isLocked() {
	return locks.containsKey(Thread.currentThread());
    }
	
    /** Obtains a non-exclusive lock for the current thread, 
     *  blocking until the lock is available. If the current thread already
     *  owns a non-exclusive lock, does nothing. */
    public synchronized void lock() {
	while (!tryLock()) {
	    if (DEBUG) debug("block for non-ex");
	    try {
		this.wait(0);
	    } catch (InterruptedException ie) {}
	}
    }
	
     /** Releases all the locks the current thread holds,
      * including exclusive and non-exclusive locks.
     *  If the current thread does not own a lock,
     *  does nothing. */
 
    public synchronized void releaseAll() {
        boolean notify = false;
	if (isLockedExclusive()) {
	    exclusive = null;
            numExclusiveRequests = 0;
            storedLocks.remove(Thread.currentThread());
	    if (DEBUG) debug("released ex all");
	    notify = true;
	}

	if(isLocked()){
            locks.remove(Thread.currentThread());
	    if (DEBUG) debug("released non-ex all");
	    if (locks.size()==0) {
		notify = true;
	    }
	}
     
        if(notify){
           this.notifyAll();
	}
    }
     /** Releases the current thread's non-exclusive lock.
     *  If the current thread does not own a non-exclusive lock,
     *  does nothing. */
    public synchronized void release() {
        if(isLocked()){
            int numLockRequests = ((Integer)locks.get(Thread.currentThread())).intValue();
            numLockRequests--;
            if(numLockRequests>0){
                locks.put(Thread.currentThread(),new Integer(numLockRequests));
	    } else {
                locks.remove(Thread.currentThread());
	        if (locks.size()==0) {
		    this.notifyAll();
	        }
	    }
	    if (DEBUG) debug("released non-ex "+(numLockRequests+1));
	}
    }

    /** Releases the current thread's exclusive lock.
     *  If the current thread does not own an exclusive lock,
     *  does nothing. */

    public synchronized void releaseExclusive(){
	if (isLockedExclusive()) {
            numExclusiveRequests--;
            if(numExclusiveRequests<=0){
	       exclusive = null;
               numExclusiveRequests = 0;
               if(storedLocks.containsKey(Thread.currentThread())){
                    int numLockRequests = ((Integer)storedLocks.get(Thread.currentThread())).intValue();
                    storedLocks.remove(Thread.currentThread());
                    locks.put(Thread.currentThread(),new Integer(numLockRequests));
	       }
               this.notifyAll();
	    }
	    if (DEBUG) debug("released ex " + (numExclusiveRequests+1));
	}
    }

    /** Tries to obtain a non-exclusive lock for the current thread. 
     *  This method always returns immediately but does not 
     *  guarantee that the lock will be obtained. If the current thread 
     *  already owns a non-exclusive lock or exclusive lock, does nothing.
     *  @return True if a non-exclusive was succesfully obtained. 
     */
    public synchronized boolean tryLock() {
        if (isLockedExclusive()) {
            return true;
	}
	if (isLocked()) {
            int numLockRequests = 0;
            if(locks.containsKey(Thread.currentThread())){
		numLockRequests = ((Integer)locks.get(Thread.currentThread())).intValue();
	    }
            numLockRequests++;
            locks.put(Thread.currentThread(),new Integer(numLockRequests));
            if (DEBUG) debug("locked non-ex "+numLockRequests);
	    return true;
	}

	if (exclusive == null) {
            locks.put(Thread.currentThread(),new Integer(1));
	    if (DEBUG) debug("locked non-ex 1");
	    return true;
	} else {
	    return false;
	}
    }

    /** Prints a list of the threads that currently own locks on this 
     *  object. */
    public synchronized String toString() {
        boolean hasLock = false;
	StringBuffer sb = new StringBuffer();
	sb.append(hashCode());
	Iterator it = locks.keySet().iterator();
	if (exclusive != null) {
	    sb.append(" ex = ");
	    sb.append(exclusive.getName());
            hasLock = true;
	} 
        if (locks.size() > 0) {
	    sb.append(" non-ex = [ ");
	    while (it.hasNext()) {
		sb.append(((Thread)it.next()).getName());
		sb.append(" ");
	    }
	    sb.append("]");
            hasLock = true;
	}
        if(!hasLock){
	    sb.append(" no locks");
	}
	return sb.toString();
    }
    
    protected Thread exclusive;
    protected int numExclusiveRequests;
    protected Map locks;
    protected Map storedLocks;
    

    private static boolean DEBUG = false;

    private void debug(String msg) {
	System.err.println("(" + Thread.currentThread().getName() + "/" + 
			   (int)(System.currentTimeMillis() % 1e6) + ")" +
			   this + " --- " + msg);
    }

}
