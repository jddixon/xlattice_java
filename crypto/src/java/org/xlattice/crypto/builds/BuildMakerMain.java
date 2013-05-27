/* BuildMakerMain.java */
package org.xlattice.crypto.builds;

import java.io.IOException;

import org.xlattice.CryptoException;
import org.xlattice.crypto.RSAKey;
import org.xlattice.crypto.RSAPublicKey;
import org.xlattice.crypto.SHA1Digest;
import org.xlattice.util.Base64Coder;
import org.xlattice.util.Timestamp;
import org.xlattice.util.cmdline.Bindery;
import org.xlattice.util.cmdline.BooleanOpt;
import org.xlattice.util.cmdline.CmdLineOpt;
import org.xlattice.util.cmdline.IntOpt;
import org.xlattice.util.cmdline.StringOpt;

/**
 * @author Jim Dixon
 */

public class BuildMakerMain {
   
    private Timestamp timestamp = new Timestamp();

    static CmdLineOpt[] options = {
        new BooleanOpt ("h", "showHelp", "show this help message"),
        new StringOpt  ("k", "keyDir",   "cryptographic key directory"),
        new StringOpt  ("s", "srcDir",   "source directory"),
        // XXX BUG: software did not detect two options using same 
        // XXX one-letter abbreviation ('t')
        new StringOpt  ("t", "title",    "title"),
        new StringOpt  ("u", "uDir",  "destination (hash) directory"),
        new BooleanOpt ("v", "verbose",  "show version number and exit"),
        // NO_DIR=min, FLAT=max 
        new IntOpt     ("y", "strategy",  "storage strategy (1 = FLAT)",
                                            BuildMaker.NO_DIR, BuildMaker.FLAT),
    };
    // XXX The directly normally used should be projectDir/.xlattice/keys/
    private String  keyDir = "~/.xlattice/keys/";
    private String  srcDir = "./";
    private String  title;              // error if not specified
    private String  destDir = "~/.xlattice/u/";
    
    private int     strategy = BuildMaker.FLAT;

    private boolean showHelp;           // defaults to false
    private boolean verbose;
   
    // XXX NO WAY TO READ THESE YET
    private RSAKey key;
    private RSAPublicKey pubKey;
    
    public BuildMakerMain () {
    }
    public void usage(String warning) {
        if (warning != null)
            System.out.println(warning);
        System.out.println(
            "usage: java org.xlattice.crypto.BuildMakerMain [options]"
        + "\nwhere the options are:");
        for (int i = 0; i < options.length; i++)
            System.out.println(
                    "  -" + options[i].getOptionName()
                  + "   " + options[i].getDescription() );
    }
    public void handleCmdLine (String[] args, int next) {
        boolean ok = true;
        if (verbose) {
            String strat;
            switch (strategy) {
                case BuildMaker.NO_DIR: strat = "NO_DIR";     break;
                case BuildMaker.FLAT:   strat = "FLAT";     break;
                case BuildMaker.DIR256: strat = "DIR256";   break;
                case BuildMaker.DIR256x16: 
                                        strat = "DIR256x16";break;
                default:    strat = "** UNKNOWN STORAGE STRATEGY ***";
            }
            System.out.println(
                "BuildMaker run at " + timestamp
                + "\n    -k = " + keyDir
                + "\n    -d = " + destDir
                + "\n    -s = " + srcDir
                + "\n    -t = " + title
                + "\n    -y = " + strat
                + "\n    -h = " + showHelp
                + "\n    -v = " + verbose
            );
            // DEBUG
            ok = false;
            // END
        } 
        if (ok && next < args.length) {
            // unhandled arguments
            usage("unhandled arguments");
            ok = false;
        }
        if (ok) {
            try {
                BuildMaker maker = new BuildMaker (key, pubKey, 
                                        srcDir, title,
                                        strategy, destDir);  
                BuildList list = maker.makeBuildList();
                byte[] hash = list.getHash();
                String encoded = Base64Coder.encode(hash);
                System.out.println(
                        "build list is in " + encoded 
                            + "\n  " + list.size() + " files processed");
                
            } catch (IOException ioe) {
                System.err.println("unexpected I/O problem: "
                        + ioe);
            } catch (CryptoException ce) {
                System.err.println("unexpected crypto fault: "
                        + ce);
            }
        }
    }

    public static void main (String[] args) {
        BuildMakerMain configurer = new BuildMakerMain();
        int next = Bindery.bind(args, options, configurer);
        configurer.handleCmdLine (args, next);
    }
}
