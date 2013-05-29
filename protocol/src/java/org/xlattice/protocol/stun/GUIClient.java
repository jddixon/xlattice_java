/* GUIClient.java */
package org.xlattice.protocol.stun;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.naming.NamingException;
import javax.swing.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.UnknownHostException;

import org.xlattice.CryptoException;
import org.xlattice.protocol.Version;

/** 
 * GUI STUN protocol client.
 */
public class GUIClient      extends JPanel implements ItemListener {

    private Client client;

    protected String version;
    
    protected JMenuBar menuBar;
    protected JMenu    serverMenu;
    
    protected JMenuItem amsIXServer = new JMenuItem("stun1.noc.ams-ix.net");
    protected JMenuItem fwdnetServer = new JMenuItem("stun.fwdnet.net");
    protected JMenuItem sipphoneServer = new JMenuItem("stun01.sipphone.com");
    protected JMenuItem softjoysServer = new JMenuItem("stun.softjoys.com");
    protected JMenuItem voipbusterServer = new JMenuItem("stun.voipbuster.com");
    protected JMenuItem voxgratiaServer = new JMenuItem("stun.voxgratia.org");
    protected JMenuItem xlatticeServer = new JMenuItem("stun.xlattice.org");
    protected JMenuItem xtenServer = new JMenuItem("stun.xten.net");

protected JMenu    optionMenu;
    protected JCheckBoxMenuItem authMenuItem;
    protected JCheckBoxMenuItem verboseMenuItem;
    
    protected JTextField domainName 
                                = new JTextField("enter domain name");
    protected JTextField serverName 
                                = new JTextField("enter server name");
    protected JTextArea msgArea;

    protected boolean authenticating;
    protected boolean verbose;

    // CONSTRUCTORS /////////////////////////////////////////////////
    public GUIClient(String initMsg) {
        super(new GridBagLayout());

        // MENU BAR ///////////////////////////////////////
        menuBar = new JMenuBar();
        serverMenu = new JMenu("public servers");
        serverMenu.setMnemonic(KeyEvent.VK_S);
        serverMenu.getAccessibleContext().setAccessibleDescription(
                "choose a public STUN server");
        menuBar.add(serverMenu);

        optionMenu = new JMenu("options");
        optionMenu.setMnemonic(KeyEvent.VK_O);
        optionMenu.getAccessibleContext().setAccessibleDescription(
                "set client options");
        menuBar.add(optionMenu);
        
        // PUBLIC SERVERS ///////////////////////
    
        amsIXServer.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae) {
                serverName.setText( "stun1.noc.ams-ix.net");
            }
        });
        serverMenu.add(amsIXServer);
        
        fwdnetServer.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae) {
                serverName.setText( "stun.fwdnet.net");
            }
        });
        serverMenu.add(fwdnetServer);
        
        serverMenu.add(sipphoneServer);
        sipphoneServer.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae) {
                serverName.setText( "stun01.sipphone.com");
            }
        });

        softjoysServer.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae) {
                serverName.setText("stun.softjoys.com");
            }
        });
        serverMenu.add(softjoysServer);

        voipbusterServer.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae) {
                serverName.setText( "stun.voipbuster.com");
            }
        });
        serverMenu.add(voipbusterServer);

        voxgratiaServer.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae) {
                serverName.setText( "stun.voxgratia.org");
            }
        });
        serverMenu.add(voxgratiaServer);
        
        xlatticeServer.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae) {
                serverName.setText( "stun.xlattice.org");
            }
        });
        serverMenu.add(xlatticeServer);
        
        xtenServer.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae) {
                serverName.setText( "stun.xten.net");
            }
        });
        serverMenu.add(xtenServer);
 
        // OPTIONS //////////////////////////////
        authMenuItem = new JCheckBoxMenuItem("enable authentication");
        authMenuItem.setSelected(false);
        authMenuItem.addItemListener(this);
        optionMenu.add(authMenuItem);
        
        verboseMenuItem = new JCheckBoxMenuItem(
                "verbose messages");
        verboseMenuItem.setSelected(false);
        verboseMenuItem.addItemListener(this);
        optionMenu.add(verboseMenuItem);
        
        
        // UPPER PANEL ////////////////////////////////////
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel upper = new JPanel(gridbag);
        upper.setFont(new Font("Helvetica", Font.PLAIN, 14));

        // TOP ROW //////////////////////////////
        c.fill = GridBagConstraints.BOTH;
        //c.weightx = 1.0;
        c.gridx = 0;
        c.gridy = 0;
        upper.add(domainName, c);
        
        c.gridx = 1;
        JButton discover = new JButton ("discover servers");
        discover.setMnemonic('d');
        discover.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae) {
                doDiscoverServers();
            }
        });
        upper.add(discover, c);
        
        c.gridx = 2;
        // XXX SHOULD SUPPORT both tcp and udp
        JLabel discoverLabel = new JLabel(
                            "find STUN UDP server for the domain named");
        upper.add(discoverLabel, c);
        
        // MIDDLE ROW ///////////////////////////
        c.gridx = 0;
        c.gridy = 1;
        c.gridheight = 2;
        upper.add (serverName, c);
        c.gridheight = 1;
        
        c.gridx = 1;
        JButton natType  = new JButton ("NAT type");
        natType.setMnemonic('n');
        natType.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                doCheckNAT();
            }
        }); 
        upper.add(natType, c);
        
        c.gridx = 2;
        JLabel natTypeLabel = new JLabel(
        "check type of NAT between this machine and the STUN server, if any");
        upper.add(natTypeLabel, c);
        
        // BOTTOM ROW ///////////////////////////
        c.gridx = 1;
        c.gridy = 2;
        JButton lifetime = new JButton ("binding lifetime");
        lifetime.setMnemonic('l');
        lifetime.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                doBindingLifetime();
            }
        }); 
        upper.add(lifetime, c);
        
        c.gridx = 2;
        JLabel lifetimeLabel = new JLabel(
                    "determine binding lifetime (WARNING: takes a long time!)");
        upper.add(lifetimeLabel, c);
        
        // LOWER PANEL ////////////////////////////////////
        GridBagLayout lowBag = new GridBagLayout();
        GridBagConstraints lc = new GridBagConstraints();
        JPanel lower = new JPanel(lowBag);
        lower.setFont(new Font("Helvetica", Font.PLAIN, 14));

        // MESSAGE AREA /////////////////////////
        msgArea = new JTextArea(initMsg, 12, 50); 
        msgArea.setEditable(false);
        JScrollPane scroller = new JScrollPane(msgArea);
        lc.gridx = 0;
        lc.gridy = 0;
        lc.fill = GridBagConstraints.HORIZONTAL;
        lower.add(scroller, lc);
        
        // SHOW AND TELL //////////////////////////////////
        GridBagConstraints k = new GridBagConstraints();
        k.fill = GridBagConstraints.HORIZONTAL;
        k.gridx = 0;
        k.gridy = 0;
        add(menuBar, k);
        k.gridy = 1;
        add(upper,    k);
        k.gridy = 2;
        add(lower, k);
    }
   
    // UTILITY METHODS //////////////////////////////////////////////
    protected void errMsg(String s) {
        msgArea.append ( 
            new StringBuffer("* ").append(s).append(" *\n")
                .toString());
    }
    protected void errMsg(String s1, String s2) {
        errMsg( new StringBuffer (s1) .append(s2) .toString() );
    }

    protected Inet4Address getInet4Addr(String s) {
        Inet4Address addr;
        try {
            addr = (Inet4Address) InetAddress.getByName(s);
        } catch (ClassCastException cce) {
            errMsg("not a valid domain name or IPv4 address: ",  s);
            addr = null;
        } catch (UnknownHostException uhe) {
            errMsg("unknown host: ", s);
            addr = null;
        }
        return addr;
    }
    // ACTIONS FOR BUTTONS //////////////////////////////////////////
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        if (source  == authMenuItem) {
            authenticating = !authenticating;
        } else if (source == verboseMenuItem) {
            verbose = !verbose;
        }
        // DEBUG
        errMsg("authenticating = " + authenticating 
                + "; verbose = " + verbose);
        // END
    }
    protected void doDiscoverServers() {
        String domain = domainName.getText();
        if (domain == null || domain.equals("")
                           || domain.equals("enter domain name")) {
            errMsg("you must provide a domain name");
            return;
        }
        // XXX NEED TO PICK UP SERVER PORT!
        String server = null;
        try {
            ServerInfo [] info = Client
                            .discoverServers(domain, !authenticating);
            if (info.length > 0)
                server = info[0].name;
        } catch (NullPointerException npe) {
            errMsg("INTERNAL ERROR: null pointer exception");
        } catch (NamingException ne) {
            server = null;
        }
        if (server == null) {
            errMsg("no " 
                  + (authenticating ? "tls/tcp" : "udp" ) + " server found");
            return;
        }
        serverName.setText(server);
    }
    protected void doCheckNAT() {
        Inet4Address pA = null;
        int pP = 3478;                  // default primary port
        
        String name = serverName.getText();
        boolean ok = true;
        if (name == null || name.equals("") 
                         || name.equals("enter server name")) {
            errMsg("you must provide a server name");
            ok = false;
        } 
        if (ok) {
            String[] s = name.split(":");
            if (s.length == 1) {
                pA = getInet4Addr(s[0]);
            } else if (s.length == 2) {
                pA = getInet4Addr(s[0]);
                pP = -1;
                try {
                    pP = Integer.parseInt(s[1]);
                } catch (NumberFormatException nfe) {
                   ok = false;
                } 
                if (!ok || (pP < 0 || pP > 65535)) {
                    ok = false;
                    errMsg("bad port number: ", s[1]);
                }
            } else {
                errMsg ("can't understand ",  name);
                ok = false;
            }
        }
        if (pA == null)
            ok = false;
       
        if (ok) {
            client = new Client(pA, pP, authenticating, verbose);
            String desc = null;
            try {
                desc = client.getNatDescription();
            } catch (CryptoException ce) {
                errMsg("authentication failure during NAT type detection: ",
                                                ce.toString());
            } catch (IOException ioe) {
                errMsg("unexpected fault during NAT type detection: ", 
                                                ioe.toString());
            } catch (StunException se) {
                errMsg(se.toString());
            }
            if (desc != null)
                msgArea.append(desc);
        }
    }
    protected void doBindingLifetime() {
        doCheckNAT();                       // creates Client instance
        if (client != null) {
            msgArea.append(
            "* THIS WILL TAKE A LONG TIME *\nbut can be interrupted\n");
            msgArea.repaint();              // has no effect :-(
            int lifetime = client.bindingLifetime();
            msgArea.append(
                    "binding lifetime is approximately " 
                    + lifetime + "s\n");
        }
    }
        
    // STATIC METHODS  //////////////////////////////////////////////
    /**
     * Frivolity: create a 16x16 XLattice graphic.
     */
    protected static Image createXLattice() {
        BufferedImage buffy = new BufferedImage(
                                16, 16, BufferedImage.TYPE_INT_RGB);
        Graphics g = buffy.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 15, 15);
        // N horizontal lines, N not quite vertical
        g.setColor(Color.GRAY);
        for (int i = 0; i < 4; i++) {
            g.drawLine(      1, 2 + 3*i,      14, 2 + 3*i); // H
            g.drawLine(3 + 3*i,       1, 1 + 3*i,      14); // V
        }
        // red X
        g.setColor(Color.RED);
        g.drawLine(  2,  2, 14, 14);
        g.drawLine( 14,  2,  2, 14);
        g.dispose();
        // a few tiny blue nodes
        g.setColor(Color.BLUE);
        // upper left corner x, y; width; height
        g.fillOval(  3,  5,  3,  3);
        g.fillOval(  3, 13,  3,  3);
        g.fillOval( 12,  5,  3,  3);
        g.fillOval( 11, 13,  3,  3);

        return buffy;
    }
    /** @return mini-XLattice logo from disk or null */
    protected static Image getXLattice() {
        java.net.URL imgURL = GUIClient.class.getResource(
                "images/smallXLattice.jpg");
        if (imgURL != null) 
            return new ImageIcon(imgURL).getImage();
        else
            return null;
    }

    public static void createGUI(String msg) {
        // XXX add version number ?
        JFrame jf = new JFrame("XLattice STUN Client");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Image myIcon = getXLattice();       // that logo
        if (myIcon == null)
            myIcon = createXLattice();
        jf.setIconImage (myIcon);
        
        JComponent newContentPane = new GUIClient(msg);
        newContentPane.setOpaque(true);
        jf.setContentPane(newContentPane);
        jf.pack();
        jf.setVisible(true);
    }
    // MAIN /////////////////////////////////////////////////////////
    public static void main (String [] args) {
        Version v = new Version();
        StringBuffer sb = new StringBuffer("XLattice STUN Client v");
        sb.append (v.getMajor())
          .append ('.')
          .append (v.getMinor());
        int decimal = v.getDecimal();
        if (decimal > 0)
            sb.append('.').append(decimal);
        int build   = v.getBuild();
        if (build > 0)
            sb.append(" build ").append(build);
        sb.append (".  This is copyrighted material.\n")
          .append (
  "  See http://www.xlattice.org/community/license.html for details\n\n")
          .append (
  "If you need to find a server, enter the domain name to the left of the discover servers button.\n")
          .append(
  "Otherwise enter a STUN server name to the left of the NAT type button.\n\n");
        
        final String initMsg = sb.toString();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createGUI(initMsg);
            }
        });
    }
}
