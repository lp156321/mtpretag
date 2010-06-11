//
//    Copyright 2010 Paul White
//
//    This file is part of MTPRetag.
//
//    MTPRetag is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.

//    MTPRetag is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with MTPRetag.  If not, see <http://www.gnu.org/licenses/>.
//

package mtpretag;

import javax.swing.*;
import javax.swing.tree.*;
import java.io.*;
import java.net.*;
import entagged.audioformats.*;
import jpmp.manager.*;
import jpmp.device.*;
import java.util.*;
import java.beans.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class represents the main window of the MtpRetag
 * tool.
 */
public class MtpRetag extends javax.swing.JFrame
        implements PropertyChangeListener {

    /**
     * This method is used to signal threads waiting
     * on this object's lock to wake up.
     *
     * In particular the file copy worker thread uses it
     * to notify the gui thread that it is finished. This
     * is used when the gui is waiting for the file copy
     * worker thread to terminate in the terminate() method.
     */
    public synchronized void notifyMe() {
        notifyAll();
    }

    /**
     * This method centralises all the logging so that
     * it can be done in a uniform way.
     *
     * @param message The message to send to the log.
     */
    public void log(String message)
    {
        System.out.println(message);
        if (logPrintStream != null)
        {
            logPrintStream.println(message);
        }
    }

    /**
     * Sets debug mode on or off. Currently not used.
     *
     * @param newValue new value for debug mode
     */
    private void setDebugMode(boolean newValue)
    {
        debugMode = newValue;
    }

    /**
     * This method handles property change events on bound
     * properties.
     *
     * @param evt
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("filesCopied")) {
            if (    ((int []) evt.getNewValue())[0] !=
                    ((int []) evt.getOldValue())[0] ) 
            {
                // This property changes each time a file is copied. It
                // also changes before the first file is copied.
                stepProgressBar();
            }
            log("Property filesCopied changed.");
        } else if (evt.getPropertyName().equals("state") ) {
            // This event indicates the state of the worker thread.
        } else {
            log("received unhandled property change event");
        }
    }

    /**
     * Default constructor. Used to build all the gui components
     * and perform other initialisation.
     */
    public MtpRetag() {
        // Ensure that only one instance of this program is running.
        try {
            processLockSocket = new ServerSocket(PROCESS_LOCK_PORT);
        } catch (IOException e) {
            log("Could not listen on port: " + PROCESS_LOCK_PORT);
            JOptionPane.showMessageDialog(null, "Another instance of this program is running. You can only run one instance at a time.");
            System.exit(-1);
        }

        // Open the output streams for the logfile.
        try {
            logOutputStream = new FileOutputStream("MtpRetagLog.txt");
            logPrintStream = new PrintStream(logOutputStream);
        } catch (Exception e) {e.printStackTrace();}

        // Initialise the gui components
        initComponents();

        // Point the tree widget at the user's home directory
        File defaultTreePath = new File(System.getProperty("user.home") + File.separator + "Music");
        if (defaultTreePath.exists()) {
            navigateTreeToPath(defaultTreePath);
        }

        // Try to connect to the usb service provider
        try {
            dm = DeviceManager.getInstance();
        } catch (Throwable e) {
            e.printStackTrace();
            System.err.println("Cannot connect with USB service provider.");
            System.exit(1);
        }
        dm.createInstance();
        dm.setVerbose(1);         // turn on vervose logging for the service provider
        setConnectedState(false); // note that we are not connected to a usb device
        progressBar.setMinimum(0);
        progressBar.setValue(0);
        transferQueue = new LinkedBlockingQueue<CopyFileDetails>();
        thisRef = this;     // store a pointer to the current object, for use in inner classes
    }

    /**
     * This method will navigate the tree widget to the specified
     * file in the directory hierarchy. This will populate the tree
     * as necessary.
     *
     * @param defaultTreePath Where to point to
     */
    private void navigateTreeToPath(File defaultTreePath) {
        TreeModel model = fileTree.getModel();
        TreeNode thisNode = (DefaultMutableTreeNode) model.getRoot();

        log(thisNode.getChildAt(0) + " " + defaultTreePath + " " + File.separator);
        String[] paths = defaultTreePath.toString().split(File.separator + File.separator);

        String EMPTY_STRING = "";
        String prefix = EMPTY_STRING;
        String suffix = EMPTY_STRING;
        int currentDepth = 0;
        Object[] treePath = new Object[paths.length + 1];
        treePath[0] = thisNode;
        while (currentDepth < paths.length) {
            int childIndex = 0;
            for (childIndex = 0; childIndex < thisNode.getChildCount(); childIndex++) {
                if (currentDepth == 0) {
                    prefix = EMPTY_STRING;
                    suffix = File.separator;
                } else {
                    prefix = File.separator;
                    suffix = EMPTY_STRING;
                }
                
                if (thisNode.getChildAt(childIndex).toString().endsWith(prefix + paths[currentDepth] + suffix)) {
                    currentDepth++;
                    break;
                }

            }
            thisNode = thisNode.getChildAt(childIndex);
            treePath[currentDepth] = thisNode;
            populateSubtree((DefaultMutableTreeNode) thisNode, 2);
        }
        TreePath selectedPath = new TreePath(treePath);

        fileTree.setSelectionPath(selectedPath);
        fileTree.expandPath(selectedPath);
        fileTree.scrollPathToVisible(selectedPath);
    }

    /**
     * This method populates the top 2 levels of the fileTree.
     * @param top The root node of the tree.
     */
    private void createNodes(DefaultMutableTreeNode top) {
        File[] roots = File.listRoots();
        for (int i = 0; i < roots.length; i++) {
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(roots[i]);
            top.add(newNode);
            populateSubtree(newNode, 1);
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        statusBar = new javax.swing.JTextField();
        progressBar = new javax.swing.JProgressBar();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        //Create the nodes.
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("My Computer");
        createNodes(top);
        fileTree = new javax.swing.JTree(top);
        jScrollPane2 = new javax.swing.JScrollPane();
        listModel = new DefaultListModel();
        jList1 = new javax.swing.JList(listModel);
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        menuItemConnect = new javax.swing.JCheckBoxMenuItem();
        menuItemCopy = new javax.swing.JMenuItem();
        menuItemExit = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("MTP Retag");
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                MtpRetag.this.windowClosing(evt);
            }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                mouseDraggedOnDivider(evt);
            }
        });

        statusBar.setBackground(new java.awt.Color(212, 208, 200));
        statusBar.setEditable(false);
        statusBar.setText("jTextField1");
        statusBar.setMaximumSize(new java.awt.Dimension(2147483647, 20));
        statusBar.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusBarActionPerformed(evt);
            }
        });

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        fileTree.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {

            public void treeWillCollapse(javax.swing.event.TreeExpansionEvent evt) throws javax.swing.tree.ExpandVetoException {
            }

            public void treeWillExpand(javax.swing.event.TreeExpansionEvent evt) throws javax.swing.tree.ExpandVetoException {
                treeWillExpandHandler(evt);
            }
        });
        fileTree.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                keyPressedOnTree(evt);
            }
        });
        jScrollPane1.setViewportView(fileTree);

        jSplitPane1.setTopComponent(jScrollPane1);

        jScrollPane2.setViewportView(jList1);

        jSplitPane1.setRightComponent(jScrollPane2);

        jMenu1.setText("File");

        menuItemConnect.setText("Connect to Device");
        menuItemConnect.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemConnectAction(evt);
            }
        });
        jMenu1.add(menuItemConnect);

        menuItemCopy.setText("Copy files to Device");
        menuItemCopy.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemCopyFilesAction(evt);
            }
        });
        jMenu1.add(menuItemCopy);

        menuItemExit.setText("Exit");
        menuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemExitAction(evt);
            }
        });
        jMenu1.add(menuItemExit);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addComponent(statusBar, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)).addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)).addContainerGap()));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(statusBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))));

        pack();
    }// </editor-fold>

    private void statusBarActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    /**
     * Called just before a tree nodes expands. This needs to
     * populate enough of the tree so that the tree view of complete.
     * @param evt
     * @throws javax.swing.tree.ExpandVetoException
     */
    private void treeWillExpandHandler(javax.swing.event.TreeExpansionEvent evt) throws javax.swing.tree.ExpandVetoException {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) evt.getPath().getLastPathComponent();

        if (node == null) {
            return;
        }

        populateSubtree(node, 2);
    }

    /**
     * This is called when we wish to terminate the application gracefully.
     */
    protected synchronized void terminate() {
        // Disconnect from USB device
        if (menuItemConnect.getState() == true) {
            usbDisconnect();
        }

        //  Cancel our worker thead which may be copying files
        if (worker != null && worker.isDone() == false) {
            log("Terminate worker thread");

            // Cancel the worker thread. It should finishing copying
            // the current file then terminate.
            worker.cancel(false);
            while (worker.getProgress() != 100) {
                log("waiting on worker thread to die");
                try {
                   wait();
                } catch (InterruptedException e) {}
            }
        }

        // Dispose of window resouces, then exit.
        log("Exit gracefully");

        // Close output file stream(s)
        logPrintStream.close();

        this.dispose();
        System.exit(0);
    }

    /**
     * Called just when we click the close button.
     * @param evt
     */
    private void windowClosing(java.awt.event.WindowEvent evt) {
        log("Window closing");
        terminate();
    }

    private void mouseDraggedOnDivider(java.awt.event.MouseEvent evt) {
        // TODO add your handling code here:
    }

    /**
     * This method will populate the subtree below a specific node
     * in the tree. It will expand it by a certain depth.
     * @param node The node under which we will perform the expansion
     * @param levels The depth of levels below the above node to expand.
     */
    private void populateSubtree(DefaultMutableTreeNode node, int levels) {
        if (node == null || levels == 0) {
            return;
        }

        // For leaf nodes we need to perform the expansion, for non-leaf
        // nodes we just recurse.
        if (node.isLeaf()) {
            File thisNode = (File) node.getUserObject();
            if (thisNode.toString().endsWith("\\.") == false) {
                if (thisNode.isDirectory()) {
                    // We only need to delve deeper for directories

                    String[] children = thisNode.list();
                    if (children != null) {
                        boolean someNodesAdded = false;
                        for (int i = 0; i < children.length; i++) {
                            if (new File(thisNode, children[i]).isDirectory() || children[i].endsWith("mp3")) {
                                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new File(thisNode, children[i]));
                                node.add(newNode);
                                someNodesAdded = true;
                                populateSubtree(newNode, levels - 1);
                            }
                        }
                        if (someNodesAdded == false) {
                            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new File(thisNode, "."));
                            node.add(newNode);
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                DefaultMutableTreeNode thisChild = (DefaultMutableTreeNode) node.getChildAt(i);
                populateSubtree(thisChild, levels - 1);
            }
        }
    }

    /**
     * This method adds the current selection from the tree view
     * to the current list of selected files.
     */
    private void addTreeSelectionToList() {
        TreePath[] treePaths = fileTree.getSelectionPaths();

        for (TreePath thisPath : treePaths) {
            DefaultMutableTreeNode thisNode = (DefaultMutableTreeNode) thisPath.getLastPathComponent();            
            addNodeToList(thisNode.toString());
        }
        statusBar.setText(listModel.getSize() + " files in copy list");
    }

    /**
     * This method adds the specified node (and its children) to
     * the current list of selected files.
     * @param pathToAdd
     * @return
     */
    protected boolean addNodeToList(String pathToAdd)
    {
        boolean retVal = true;        
        File file = new File(pathToAdd);

        if (file.isFile())
        {
            if (pathToAdd.endsWith("mp3")) {
                Object thisObject;
                int index = 0;
                boolean match = false;

                // Search through existing list to make sure we don't have duplicates
                for (index = 0; index < listModel.getSize(); index++) {
                    thisObject = listModel.getElementAt(index);

                    if (pathToAdd.toString().equals(thisObject.toString())) {
                        match = true;
                    }
                }

                if (match == false) {
                    // Dump a warning when the list gets long...
                    final int WARNING_NUMBER_OF_FILES = 1000;
                    if (listModel.size() == WARNING_NUMBER_OF_FILES) {
                        if (JOptionPane.showConfirmDialog(null, "Your list already contains " + WARNING_NUMBER_OF_FILES + " files, are you sure you wish to continue?", "Error", JOptionPane.OK_CANCEL_OPTION) != 0) 
                        {
                            retVal = false;
                        }                        
                    }

                    listModel.addElement(pathToAdd);
                }
            }
        }
        else if (file.isDirectory())
        {
            // For directories we should recurse so all its children are
            // added to the list
            String[] children = file.list();
            for (String thisChild : children)
            {
                if (addNodeToList(pathToAdd + File.separator + thisChild) == false)
                {
                    retVal = false;
                    break;
                }
            }
        }
        else
        {
            log("Error: This node is neither a file nor a directory.");
        }

        return retVal;
    }

    // Handle key strokes on the tree
    private void keyPressedOnTree(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == 10) {
            addTreeSelectionToList();
        }
    }

    public JTextField getStatusBar() {
        return statusBar;
    }

    /**
     * This method disconnects from the USB service provider.
     */
    private void usbDisconnect() {
        dm.releaseDevice(usbDevice.getCanonical());
        usbDevice = null;
        setConnectedState(false);
    }

    /**
     * Handle a click on the menu item for connecting.
     * @param evt
     */
    private void menuItemConnectAction(java.awt.event.ActionEvent evt) {
        if (menuItemConnect.getState() == true) {
            usbConnect();
        } else {
            usbDisconnect();
        }
    }

    private void menuItemExitAction(java.awt.event.ActionEvent evt) {
        terminate();
    }

    /**
     * Step the progress bar. This should be called after each file is copied
     * and also just before the first file is copied.
     */
    public void stepProgressBar() {
        int oldValue = progressBar.getValue();
        if (progressBar.getMaximum() - oldValue > 1) {
            progressBar.setValue(oldValue + 1);
            log("new progress value " + progressBar.getValue());
            statusBar.setText("Copying files to device... (" + progressBar.getValue() + " of " + progressBar.getMaximum() + ")");
        } else {
            if (progressBar.getMaximum() - oldValue == 1) {
                progressBar.setValue(progressBar.getMaximum());
                statusBar.setText("Copying files to device... (" + progressBar.getValue() + " of " + progressBar.getMaximum() + ")");
            } else {
                progressBar.setValue(0);
                int failed = progressBar.getMaximum() - transferredOk;
                statusBar.setText("Finished copying (" + failed + " of " + progressBar.getMaximum() + " failed)");
            }
        }
        progressBar.repaint();
    }

    /**
     * Connect to the USB device
     */
    private void usbConnect() {
        boolean success = false;
        // find first device
        try {
            dm.scanDevices();
            if (dm.getDeviceList() != null && dm.getDeviceList().size() > 0) {
                for (Iterator it = dm.getDeviceList().keySet().iterator(); it.hasNext();) {
                    String devkey = (String) it.next();
                    usbDevice = (UsbDevice) dm.getDeviceList().get(devkey);
                    if (usbDevice != null) {
                        success = true;
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        setConnectedState(success);
        if (!success) {
            JOptionPane.showMessageDialog(null, "Error: could not connect to an MTP device");
        }
    }

    /**
     * Handle a click on the copy files menu item.
     * @param evt
     */
    private void menuItemCopyFilesAction(java.awt.event.ActionEvent evt) {
        // Ensure we're connected to the USB device
        if (menuItemConnect.getState() == false) {
            if (JOptionPane.showConfirmDialog(null, "Please connect USB device.", "Error", JOptionPane.OK_CANCEL_OPTION) != 0) {
                return;
            }
            usbConnect();
        }

        String artist = null, title = null;
        Object thisObject;

        // Setup the appropriate min and max for the progress bar
        int maxProgressBarValue = listModel.getSize();
        if (progressBar.getValue() != 0) {
            maxProgressBarValue += progressBar.getValue();
        }
        progressBar.setMaximum(maxProgressBarValue);
        progressBar.setMinimum(0);
        progressBar.setValue(0);
        log("max progress bar " + maxProgressBarValue);

        transferredOk = 0;
        for (int index = 0; index < listModel.getSize(); index++) {
            thisObject = listModel.getElementAt(index);

            // Form the album name
            String tokeniser = "\\\\";
            String tokens[] = thisObject.toString().split(tokeniser);
            String album = new String();
            int levels = 2;
            while (levels-- > 0) {
                if (tokens.length - levels - 2 >= 0) {
                    album = album.concat(tokens[tokens.length - levels - 2] + "/");
                }
            }
            if (album.length() == 0) {
                album = "unknown album";
            }

            // Read the mp3 tags
            try {
                AudioFile audioFile = AudioFileIO.read(new File(thisObject.toString())); //Reads the given file.
                if (audioFile.getTag().getArtist().size() > 0) {
                    artist = (String) audioFile.getTag().getArtist().get(0).toString();
                } else {
                    artist = tokens[tokens.length - 2];
                }
                if (audioFile.getTag().getTitle().size() > 0) {
                    title = (String) audioFile.getTag().getTitle().get(0).toString();
                } else {
                    title = tokens[tokens.length - 1];
                }
            } catch (entagged.audioformats.exceptions.CannotReadException e) {
                title = "unknown title";
                artist = "unknown artist";
            }

            String output = "Adding file to copy list:" + thisObject.toString() + ", album:" + album + ", title:" + title + ", artist:" + artist;
            log(output);

            // Put the files into the transfer queue
            addFileToTransferQueue(thisObject.toString(),
                    album,
                    title,
                    artist);
        }

        // Form a worker background thread which will perform the
        // actual copying.
        worker = new SwingWorker<Void, Void>() {
            @Override
            public Void doInBackground() {
                CopyFileDetails details;
                int filesCopied = 0;
                try {
                    setProgress(0);
                    int [] oldValue = new int[1];
                    int [] newValue = new int[1];
                    oldValue[0] = -1;
                    newValue[0] = 0;

                    // Fire a property change event at the start
                    firePropertyChange("filesCopied", oldValue, newValue);
                    while ((details = transferQueue.remove()) != null) {
                        if (isCancelled() == true) {
                            log("Copying cancelled.");
                            break;
                        }
                        if (usbDevice.sendFile(
                                details.getSource(),
                                details.getDestination(),
                                details.getTn(),
                                details.getFm()) != 0) {
                            System.err.println("Error: could not copy file to device : " + details.getSource());
                        } else {
                            transferredOk++;
                        }
                        oldValue[0] = filesCopied;
                        newValue[0] = filesCopied+1;
                        // Fire a property change event after each file is copied
                        firePropertyChange("filesCopied", oldValue, newValue);
                        filesCopied++;
                    }
                    // Indicate copying has finished
                    setProgress(100);
                    // Notify the app incase it was waiting on the transfer to
                    // finish (eg. in case of terminate())
                    notifyMe();
                } catch (NoSuchElementException n) {
                    log("reached end of transfer queue");
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        // Ensure that this MtpRetag object is notified of property change
        // events on this worker thread.
        worker.addPropertyChangeListener(this);
        // Start the worker thread.
        worker.execute();
        listModel.clear();
    }

    /**
     * Add a specific file to the transfer queue
     * @param fileName
     * @param album
     * @param title
     * @param artist
     * @return
     */
    private boolean addFileToTransferQueue(
            String fileName,
            String album,
            String title,
            String artist) {
        FileMetadata fm = new FileMetadata();
        fm.setAlbum(album);
        fm.setArtist(artist);
        fm.setGenre("MtpCopyGenre");
        fm.setRating(0);
        fm.setTitle(title);
        TransferNotifier tm = new TransferNotifier();

        // Set the destination directory on the USB device
        String destination = "/Music/" + fileName.substring(fileName.lastIndexOf("\\") + 1);

        CopyFileDetails details = new CopyFileDetails(fileName, destination, fm, tm);
        transferQueue.add(details);

        return false;
    }

    /**
     * This is the applications entry point
     * @param args the command line arguments
     */
    public static void main(final String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(
                            UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    System.err.println("Couldn't use system look and feel.");
                }
                MtpRetag myApp = new MtpRetag();
                myApp.setVisible(true);
                if (args.length >= 1 && args[0].equals("-debug"))
                {
                    myApp.setDebugMode(true);
                    myApp.log("Debug mode enabled");
                }
              
            }
        });
    }

    /**
     * Set if we are connected to the USB device.
     * @param state
     */
    public void setConnectedState(boolean state) {
        menuItemConnect.setState(state);
        if (state == true) {
            this.getStatusBar().setText("Connected to device");
        } else {
            this.getStatusBar().setText("Not connected to device");
        }
    }

    private final int PROCESS_LOCK_PORT = 27016;
    DefaultListModel listModel = null;
    ServerSocket processLockSocket = null;
    protected UsbDevice usbDevice = null;
    protected DeviceManager dm = null;
    protected int transferredOk;
    protected LinkedBlockingQueue<CopyFileDetails> transferQueue;
    protected SwingWorker worker = null;
    protected MtpRetag thisRef = null;
    protected boolean debugMode = false;
    protected FileOutputStream logOutputStream = null;
    protected PrintStream logPrintStream = null;

    // Variables declaration - do not modify
    private javax.swing.JTree fileTree;
    private javax.swing.JList jList1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JCheckBoxMenuItem menuItemConnect;
    private javax.swing.JMenuItem menuItemCopy;
    private javax.swing.JMenuItem menuItemExit;
    private javax.swing.JTextField statusBar;
    // End of variables declaration
}
