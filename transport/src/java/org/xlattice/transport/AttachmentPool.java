/* AttachmentPool.java */
package org.xlattice.transport;

import org.xlattice.util.Stack;

class AttachmentPool {

    public static final int MIN_MAX     =   8;
    public static final int DEFAULT_MAX =  64;
    public static final int MAX_MAX     = 512;
    
    private int   _max;
    private Stack attachments;
   
    public AttachmentPool () {
        this(DEFAULT_MAX);
    }
    public AttachmentPool (int maxStack) {
        if (maxStack < MIN_MAX)
            maxStack = MIN_MAX;
        else if (maxStack > MAX_MAX)
            maxStack = MAX_MAX;
        _max = maxStack;
        
        attachments = new Stack();
    }
    public Attachment get(int t, Object o) {
        synchronized (attachments) {
            if (! attachments.isEmpty() ) {
               Attachment atta = (Attachment)attachments.pop();
               atta.type = t;
               atta.obj  = o;
               return atta;
            }
        }
        return new Attachment(t, o);
    } 
    public void dispose(Attachment a) {
        a.type = Attachment.NIX_A;
        a.obj  = null;
        synchronized (attachments) {
            attachments.push(a);
        }
    }
    public int size() {
        synchronized (attachments) {
            return attachments.size();
        }
    }
}

