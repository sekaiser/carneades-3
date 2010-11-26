/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * EditorView.java
 *
 * Created on Jul 8, 2010, 1:56:17 PM
 */

package carneades.editor.uicomponents;

import java.awt.Color;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

/**
 *
 * @author pal
 */
public class EditorApplicationView extends javax.swing.JFrame {

    /** Creates new form EditorView */
    public EditorApplicationView() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        jSeparator10 = new javax.swing.JPopupMenu.Separator();
        searchInButtonGroup = new javax.swing.ButtonGroup();
        jSeparator12 = new javax.swing.JPopupMenu.Separator();
        statusButtonGroup = new javax.swing.ButtonGroup();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        jSeparator8 = new javax.swing.JToolBar.Separator();
        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        leftTabbedPane = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        resultPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        jSeparator11 = new javax.swing.JPopupMenu.Separator();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        editMenu = new javax.swing.JMenu();
        jSeparator9 = new javax.swing.JPopupMenu.Separator();
        assistantMenu = new javax.swing.JMenu();
        helpMenu = new javax.swing.JMenu();

        tabPopupMenu.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        closeTabMenuItem.setText("Close");
        tabPopupMenu.add(closeTabMenuItem);

        lkifFilePopupMenu.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        newGraphMenuItem.setText("New Graph");
        lkifFilePopupMenu.add(newGraphMenuItem);

        closeLkifFileMenuItem.setText("Close");
        lkifFilePopupMenu.add(closeLkifFileMenuItem);
        lkifFilePopupMenu.add(jSeparator5);

        exportLkifFileMenuItem.setText("Export...");
        lkifFilePopupMenu.add(exportLkifFileMenuItem);

        graphPopupMenu.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        openGraphMenuItem.setText("Open");
        graphPopupMenu.add(openGraphMenuItem);

        closeGraphMenuItem.setText("Close");
        graphPopupMenu.add(closeGraphMenuItem);
        graphPopupMenu.add(jSeparator6);

        exportGraphMenuItem.setText("Export...");
        graphPopupMenu.add(exportGraphMenuItem);
        graphPopupMenu.add(jSeparator10);

        deleteGraphMenuItem.setText("Delete");
        graphPopupMenu.add(deleteGraphMenuItem);

        newPremiseMenuItem.setText("New premise");
        argumentPopupMenu.add(newPremiseMenuItem);

        addExistingPremiseMenuItem.setText("Link premise");
        argumentPopupMenu.add(addExistingPremiseMenuItem);

        deleteArgumentMenuItem.setText("Delete");
        argumentPopupMenu.add(deleteArgumentMenuItem);

        deletePremiseMenuItem.setText("Delete");
        premisePopupMenu.add(deletePremiseMenuItem);

        statusPopupMenu.setText("Status");

        statusButtonGroup.add(statedMenuItem);
        statedMenuItem.setText("Stated");
        statusPopupMenu.add(statedMenuItem);

        statusButtonGroup.add(questionedMenuItem);
        questionedMenuItem.setText("Questioned");
        statusPopupMenu.add(questionedMenuItem);

        statusButtonGroup.add(acceptedMenuItem);
        acceptedMenuItem.setText("Accepted");
        statusPopupMenu.add(acceptedMenuItem);

        statusButtonGroup.add(rejectedMenuItem);
        rejectedMenuItem.setText("Rejected");
        statusPopupMenu.add(rejectedMenuItem);

        statementPopupMenu.add(statusPopupMenu);

        editStatementMenuItem.setText("Edit");
        statementPopupMenu.add(editStatementMenuItem);

        newArgumentMenuItem.setText("New argument");
        statementPopupMenu.add(newArgumentMenuItem);

        deleteStatementMenuItem.setText("Delete");
        statementPopupMenu.add(deleteStatementMenuItem);
        statementPopupMenu.add(jSeparator12);

        mainIssueMenuItem.setSelected(true);
        mainIssueMenuItem.setText("Main issue");
        statementPopupMenu.add(mainIssueMenuItem);

        newStatementMenuItem.setText("New statement");
        mapPopupMenu.add(newStatementMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Carneades Editor");

        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        openFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/document-open.png"))); // NOI18N
        openFileButton.setFocusable(false);
        openFileButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        openFileButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(openFileButton);

        saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/document-save.png"))); // NOI18N
        saveButton.setFocusable(false);
        saveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(saveButton);
        toolBar.add(jSeparator4);

        undoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit-undo.png"))); // NOI18N
        undoButton.setFocusable(false);
        undoButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        undoButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(undoButton);

        redoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit-redo.png"))); // NOI18N
        redoButton.setFocusable(false);
        redoButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        redoButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(redoButton);
        toolBar.add(jSeparator8);

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view-refresh.png"))); // NOI18N
        refreshButton.setFocusable(false);
        refreshButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refreshButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(refreshButton);

        zoomResetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/zoomreset.png"))); // NOI18N
        zoomResetButton.setBorder(null);
        zoomResetButton.setBorderPainted(false);
        zoomResetButton.setFocusPainted(false);
        zoomResetButton.setFocusable(false);
        zoomResetButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        zoomResetButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(zoomResetButton);

        zoomOutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/zoomout.png"))); // NOI18N
        zoomOutButton.setBorder(null);
        zoomOutButton.setFocusable(false);
        zoomOutButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        zoomOutButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(zoomOutButton);

        zoomInButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/zoomin.png"))); // NOI18N
        zoomInButton.setBorder(null);
        zoomInButton.setFocusable(false);
        zoomInButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        zoomInButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(zoomInButton);

        jSplitPane1.setOneTouchExpandable(true);
        jSplitPane1.setRightComponent(mapPanel);

        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        leftTabbedPane.setMinimumSize(new java.awt.Dimension(60, 60));
        leftTabbedPane.setPreferredSize(new java.awt.Dimension(330, 320));

        jPanel1.setPreferredSize(new java.awt.Dimension(306, 200));

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        lkifsTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        lkifsTree.setMaximumSize(new java.awt.Dimension(32767, 32767));
        jScrollPane2.setViewportView(lkifsTree);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
        );

        leftTabbedPane.addTab("Files", jPanel1);

        searchScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        searchPanel.setMinimumSize(new java.awt.Dimension(0, 187));
        searchPanel.setPreferredSize(new java.awt.Dimension(306, 200));

        searchComboBox.setEditable(true);

        searchButton.setText("Search");
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        optionsButton.setText("Options...");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(optionsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 53, Short.MAX_VALUE)
                .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(optionsButton)))
        );

        resultPanel.setLayout(new javax.swing.BoxLayout(resultPanel, javax.swing.BoxLayout.Y_AXIS));
        resultPanel.add(searchProgressBar);

        searchResultTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Results"
            }
        ));
        searchResultTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane4.setViewportView(searchResultTable);

        resultPanel.add(jScrollPane4);

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchPanelLayout.createSequentialGroup()
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(resultPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE)
                    .addComponent(searchComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, 312, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addComponent(searchComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(resultPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE))
        );

        searchScrollPane.setViewportView(searchPanel);

        leftTabbedPane.addTab("Search", searchScrollPane);

        jSplitPane2.setLeftComponent(leftTabbedPane);

        propertiesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Properties"));
        propertiesPanel.setMinimumSize(new java.awt.Dimension(12, 370));
        propertiesPanel.setPreferredSize(new java.awt.Dimension(12, 200));
        propertiesPanel.setLayout(new javax.swing.BoxLayout(propertiesPanel, javax.swing.BoxLayout.PAGE_AXIS));
        jSplitPane2.setRightComponent(propertiesPanel);

        jSplitPane1.setLeftComponent(jSplitPane2);

        fileMenu.setText("File");

        newFileMenuItem.setText("New File...");
        fileMenu.add(newFileMenuItem);
        fileMenu.add(jSeparator11);

        openFileMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openFileMenuItem.setText("Open...");
        fileMenu.add(openFileMenuItem);

        closeFileMenuItem.setText("Close");
        fileMenu.add(closeFileMenuItem);
        fileMenu.add(jSeparator2);

        saveFileMenuItem.setText("Save");
        fileMenu.add(saveFileMenuItem);

        saveAsFileMenuItem.setText("Save As...");
        fileMenu.add(saveAsFileMenuItem);
        fileMenu.add(jSeparator3);

        exportFileMenuItem.setText("Export...");
        fileMenu.add(exportFileMenuItem);
        fileMenu.add(jSeparator7);

        printPreviewFileMenuItem.setText("Print Preview");
        fileMenu.add(printPreviewFileMenuItem);

        printFileMenuItem.setText("Print...");
        fileMenu.add(printFileMenuItem);
        fileMenu.add(jSeparator1);

        quitFileMenuItem.setText("Quit");
        fileMenu.add(quitFileMenuItem);

        menuBar.add(fileMenu);

        editMenu.setText("Edit");

        undoEditMenuItem.setText("Undo");
        editMenu.add(undoEditMenuItem);

        redoEditMenuItem.setText("Redo");
        editMenu.add(redoEditMenuItem);
        editMenu.add(jSeparator9);

        copyClipboardEditMenuItem.setText("Copy to clipboard");
        editMenu.add(copyClipboardEditMenuItem);

        selectAllEditMenuItem.setText("Select All");
        editMenu.add(selectAllEditMenuItem);

        menuBar.add(editMenu);

        assistantMenu.setText("Assistant");

        assistantFindArgumentsMenuItem.setText("Find Arguments...");
        assistantMenu.add(assistantFindArgumentsMenuItem);

        assistantFindGoalMenuItem.setText("Find Goal...");
        assistantMenu.add(assistantFindGoalMenuItem);

        assistantFormalizeStatementMenuItem.setText("Formalize Statement...");
        assistantMenu.add(assistantFormalizeStatementMenuItem);

        assistantInstantiateSchemeMenuItem.setText("Instantiate Scheme...");
        assistantMenu.add(assistantInstantiateSchemeMenuItem);

        menuBar.add(assistantMenu);

        helpMenu.setText("Help");

        aboutHelpMenuItem.setText("About");
        helpMenu.add(aboutHelpMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 821, Short.MAX_VALUE)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 821, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 611, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_searchButtonActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new EditorApplicationView().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public final javax.swing.JMenuItem aboutHelpMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JRadioButtonMenuItem acceptedMenuItem = new javax.swing.JRadioButtonMenuItem();
    public final javax.swing.JMenuItem addExistingPremiseMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JPopupMenu argumentPopupMenu = new javax.swing.JPopupMenu();
    public final javax.swing.JMenuItem assistantFindArgumentsMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem assistantFindGoalMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem assistantFormalizeStatementMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem assistantInstantiateSchemeMenuItem = new javax.swing.JMenuItem();
    private javax.swing.JMenu assistantMenu;
    public final javax.swing.JMenuItem closeFileMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem closeGraphMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem closeLkifFileMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem closeTabMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem copyClipboardEditMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem deleteArgumentMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem deleteGraphMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem deletePremiseMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem deleteStatementMenuItem = new javax.swing.JMenuItem();
    private javax.swing.JMenu editMenu;
    public final javax.swing.JMenuItem editStatementMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem exportFileMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem exportGraphMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem exportLkifFileMenuItem = new javax.swing.JMenuItem();
    private javax.swing.JMenu fileMenu;
    public final javax.swing.JPopupMenu graphPopupMenu = new javax.swing.JPopupMenu();
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator10;
    private javax.swing.JPopupMenu.Separator jSeparator11;
    private javax.swing.JPopupMenu.Separator jSeparator12;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JToolBar.Separator jSeparator8;
    private javax.swing.JPopupMenu.Separator jSeparator9;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTabbedPane leftTabbedPane;
    public final javax.swing.JPopupMenu lkifFilePopupMenu = new javax.swing.JPopupMenu();
    public final javax.swing.JTree lkifsTree = new javax.swing.JTree();
    public final javax.swing.JCheckBoxMenuItem mainIssueMenuItem = new javax.swing.JCheckBoxMenuItem();
    public final javax.swing.JTabbedPane mapPanel = new javax.swing.JTabbedPane();
    public final javax.swing.JPopupMenu mapPopupMenu = new javax.swing.JPopupMenu();
    private javax.swing.JMenuBar menuBar;
    public final javax.swing.JMenuItem newArgumentMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem newFileMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem newGraphMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem newPremiseMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem newStatementMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JButton openFileButton = new javax.swing.JButton();
    public final javax.swing.JMenuItem openFileMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem openGraphMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JButton optionsButton = new javax.swing.JButton();
    public final javax.swing.JPopupMenu premisePopupMenu = new javax.swing.JPopupMenu();
    public final javax.swing.JMenuItem printFileMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JMenuItem printPreviewFileMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JPanel propertiesPanel = new javax.swing.JPanel();
    public final javax.swing.JRadioButtonMenuItem questionedMenuItem = new javax.swing.JRadioButtonMenuItem();
    public final javax.swing.JMenuItem quitFileMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JButton redoButton = new javax.swing.JButton();
    public final javax.swing.JMenuItem redoEditMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JButton refreshButton = new javax.swing.JButton();
    public final javax.swing.JRadioButtonMenuItem rejectedMenuItem = new javax.swing.JRadioButtonMenuItem();
    private javax.swing.JPanel resultPanel;
    public final javax.swing.JMenuItem saveAsFileMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JButton saveButton = new javax.swing.JButton();
    public final javax.swing.JMenuItem saveFileMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JButton searchButton = new javax.swing.JButton();
    public final javax.swing.JComboBox searchComboBox = new javax.swing.JComboBox();
    private javax.swing.ButtonGroup searchInButtonGroup;
    public final javax.swing.JPanel searchPanel = new javax.swing.JPanel();
    public final javax.swing.JProgressBar searchProgressBar = new javax.swing.JProgressBar();
    public final javax.swing.JTable searchResultTable = new javax.swing.JTable();
    public final javax.swing.JScrollPane searchScrollPane = new javax.swing.JScrollPane();
    public final javax.swing.JMenuItem selectAllEditMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JRadioButtonMenuItem statedMenuItem = new javax.swing.JRadioButtonMenuItem();
    public final javax.swing.JPopupMenu statementPopupMenu = new javax.swing.JPopupMenu();
    private javax.swing.ButtonGroup statusButtonGroup;
    public final javax.swing.JMenu statusPopupMenu = new javax.swing.JMenu();
    public final javax.swing.JPopupMenu tabPopupMenu = new javax.swing.JPopupMenu();
    public final javax.swing.JToolBar toolBar = new javax.swing.JToolBar();
    public final javax.swing.JButton undoButton = new javax.swing.JButton();
    public final javax.swing.JMenuItem undoEditMenuItem = new javax.swing.JMenuItem();
    public final javax.swing.JButton zoomInButton = new javax.swing.JButton();
    public final javax.swing.JButton zoomOutButton = new javax.swing.JButton();
    public final javax.swing.JButton zoomResetButton = new javax.swing.JButton();
    // End of variables declaration//GEN-END:variables

    private final static String APPLICATION_NAME = "Carneades Editor";

    // our modifications:
    static {
        // mac os x:
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name",
                APPLICATION_NAME);

        // set nimbus theme
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
        }

    }

    public static EditorApplicationView viewInstance = new EditorApplicationView();

    public static synchronized EditorApplicationView instance()
    {
        return viewInstance;
    }

    public static synchronized void reset()
    {
        viewInstance = new EditorApplicationView();
    }
}
