/* Context.java */
package org.xlattice;

import java.util.HashMap;

/**
 * A naming context consisting of a possibly nested set of name-to-object
 * bindings.  If there is a parent context and a key cannot be resolved
 * in this context, an attempt will be made to resolve it in the parent,
 * recursively.
 * <p/>
 * Names added to the context must not be null.
 * <p/>
 * This implementation is intended to be thread-safe.
 * 
 * @author Jim Dixon
 */
public class Context {
    private final HashMap ctx;
    private Context parent;

    // CONSTRUCTORS /////////////////////////////////////////////////
    /** Create a context without a parent. */
    public Context () {
        ctx = new HashMap();
    }
    /** Create a context with a parent Context. */
    public Context (Context parent) {
        this();
        this.parent = parent;
    }
    // METHODS //////////////////////////////////////////////////////
    /**
     * Bind a name to an Object at this Context level.  Neither name
     * nor object may be null.
     * <p/>
     * If this context has a parent, the binding at this level will
     * mask any bindings in the parent and above.
     * 
     * @param name the name being bound
     * @param o    the Object it is bound to
     * @throws IllegalArgumentException if either is null. 
     */
    public Context bind (String name, Object o) {
        if (name == null || o == null) 
            throw new IllegalArgumentException("null name or object");
        synchronized (this) {
            ctx.put(name, o);
        }
        return this;
    }
    
    /**
     * Looks up a name recursively.  If the name is bound at this level,
     * the object it is bound to is returned.  Otherwise, if there is 
     * a parent Context, the value returned by a lookup in the parent
     * Context is returned.  If there is no parent and no match, returns
     * null.
     *
     * @param name the name we are attempting to match
     * @return     the value the name is bound to at this or a higher level
     *             or null if there is no such value
     */
    public Object lookup (String name) {
        if (name == null) 
            throw new IllegalArgumentException("null name");
        Object o;
        synchronized (this) {
            o = ctx.get(name);
            if (o == null && parent != null) 
                o = parent.lookup(name);
        }
        return o;
    }

    /** 
     * Remove a binding from the Context.  If there is no such binding,
     * silently ignore the request.  Any binding at a higher level, in
     * the parent Context or above, is unaffected by this operation.
     * 
     * @param name Name to be unbound.
     */
    public synchronized void unbind (String name) {
        if (name == null) 
            throw new IllegalArgumentException("null name");
        ctx.remove(name);
    }
    // PROPERTIES /////////////////////////////////////////////////// 
    /**
     * @return the number of bindings at this level 
     */
    public synchronized int size() {
        return ctx.size();
    }
    /**
     * @return a reference to the parent Context or null if there is none
     */
    public synchronized Context getParent () {
        return parent;
    }
    /** 
     * Change the parent Context. This method returns a reference to 
     * this instance, to allow method calls to be chained.
     *
     * @param  newParent New parent Context, possibly null.
     * @return a reference to the current Context
     */
    public synchronized Context setParent(Context newParent) {
        parent = newParent;
        return this;
    }
}
