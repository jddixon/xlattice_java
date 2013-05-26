/* SeqTemplate.java */
package org.xlattice.util.template;

import java.util.ArrayList;

import org.xlattice.Context;
import org.xlattice.Template;

/**
 *
 * Operations require external synchronization.
 *
 * @author Jim Dixon
 */
public class SeqTemplate extends TemplateImpl {

    private final ArrayList list;
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    protected SeqTemplate (Template[] tArray) {
        super(TPL_SEQ);
        list = new ArrayList(tArray.length);
        for (int i = 0; i < tArray.length; i++)
            list.add(tArray[i]);
    }
    // PROPERTIES ///////////////////////////////////////////////////
    public void add(Template t) {
        if (t == null)
            throw new IllegalArgumentException(
                    "cannot add null Template");
        synchronized (list) {
            list.add(t);
        }
    }
    public int size() {
        synchronized (list) {
            return list.size();
        }
    }
    // INTERFACE TEMPLATE ///////////////////////////////////////////
    /**
     *
     */
    public String toString(Context ctx) {
        synchronized (list) {
            int count = list.size();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < count; i++) 
                sb.append( ((Template)list.get(i)).toString(ctx) );
            return sb.toString();
        }
    }
    /**
     *
     */
    public byte[] getBytes(Context ctx) {
        byte[] data;
        synchronized (list) {
            int count = list.size();
            int len   = 0;
            byte[][] refs = new byte[count][];
            for (int i = 0; i < count; i++) {
               refs[i] = ((Template)list.get(i)).getBytes(ctx);
               len += refs[i].length;
            }
            data = new byte[len];
            int base = 0;
            for (int i = 0 ; i < count; i++) {
                for (int j = 0; j < refs[i].length; j++) 
                    data[base + j] = refs[i][j];
                base += refs[i].length;
            }
        }
        return data;
    }
}
