package org.xlattice.crypto.u;

import java.io.File;
import java.io.IOException;

/**
 * @author Jim Dixon
 */
public class Walker {
    private final Visitor _v;
    private final String  _pathToU;
    private File         _uDir;

    public Walker( Visitor v, final String pathToU ) throws IOException {
        if (v == null)
            throw new IllegalArgumentException("null visitor");
        _v = v;
        if (pathToU == null || pathToU.equals(""))
            throw new IllegalArgumentException("null or empty pathToU");
        _pathToU = pathToU;
        _uDir = new File(_pathToU);
        if (!_uDir.exists())
            throw new IllegalArgumentException (_pathToU + " does not exist ");
    }
    private void walkDir(File dir) throws IOException {
        File listFile[] = dir.listFiles();
        if(listFile != null) {
            for(int i=0; i<listFile.length; i++) {
                if(listFile[i].isDirectory()) {
                    walkDir(listFile[i]);
                } else { 
                    _v.visitFile(listFile[i].getPath());
                }
            }
        }
    } 
    public void walk() throws IOException {
        _v.enterU(_pathToU);
        walkDir(new File(_pathToU));
        _v.exitU();
    }
}


