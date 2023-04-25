/**
 *  Copyright 2023 University Of Southern California
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.paint.gui.menu;

import com.sri.panther.paintCommon.Constant;
import com.sri.panther.paintCommon.User;
import com.sri.panther.paintCommon.familyLibrary.FileNameGenerator;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.paint.config.Preferences;
import org.paint.dataadapter.FileAdapter;
import org.paint.dataadapter.PantherServer;
import org.paint.dialog.ActiveFamily;
import org.paint.dialog.LoginDlg;
import org.paint.dialog.ManageBooksDlg;
import org.paint.dialog.NewFamily;
import org.paint.gui.DirtyIndicator;
import org.paint.gui.event.AnnotationChangeEvent;
import org.paint.gui.event.AnnotationChangeListener;
import org.paint.gui.event.CommentChangeEvent;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.FamilyChangeEvent;
import org.paint.gui.event.FamilyChangeListener;
import org.paint.main.PaintManager;

public class FileMenu extends JMenu implements AnnotationChangeListener, FamilyChangeListener { 

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    protected static Logger log = Logger.getLogger(FileMenu.class);

    protected JMenuItem loginItem;
    protected JMenuItem logoffItem;
//    protected JMenuItem openDBItem;
    protected JMenuItem manageBooksItem;
    protected JMenuItem updateCommentItem;
    protected JMenuItem updateFamilyNameItem;
    protected JMenuItem saveDBItem;
    protected JMenuItem viewOmittedAnnotInfoItem;
    protected JMenuItem viewTaxonomyViolatoinInfoItem;
    protected JMenuItem viewUpdateHistoryItem;

    private static final String MENU_ITEM_LOGIN = "Login";
    private static final String MENU_ITEM_LOGOFF = "Logoff";
    private static final String MENU_ITEM_OPEN_FROM_DB = "Open from database ... ";
    private static final String MENU_ITEM_MANAGE_BOOKS = "Manage and View Books...";
    private static final String MENU_ITEM_UPDATE_COMMENT = "Update comment...";
    private static final String MENU_ITEM_UPDATE_NAME_FAMILY = "Name Family...";
    private static final String MENU_ITEM_SAVE_TO_DB = "Save to database...";
    private static final String MENU_ITEM_VIEW_ANNOT_INFO = "View annotation information";
    private static final String MENU_ITEM_VIEW_ANNOT_HISTORY_INFO = "View annotation history";
    private static final String MENU_ITEM_VIEW_TAXONOMY_VIOLATION_INFO = "View taxonomy violation information";

    private static List<FileMenu> instances = new ArrayList<FileMenu>();

    public FileMenu() {
        super("File");
        this.setMnemonic('f');
        Toolkit toolkit = Toolkit.getDefaultToolkit();

        loginItem = new JMenuItem(MENU_ITEM_LOGIN);
        loginItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, toolkit.getMenuShortcutKeyMask()));
        loginItem.addActionListener(new LoginActionListener(this, loginItem));
        this.add(loginItem);

        logoffItem = new JMenuItem(MENU_ITEM_LOGOFF);
        logoffItem.addActionListener(new LogoffActionListener(this, logoffItem));
        this.add(logoffItem);
//		logoffItem.setVisible(false);
        logoffItem.setEnabled(false);

        this.addSeparator();

//        openDBItem = new JMenuItem(MENU_ITEM_OPEN_FROM_DB);
//        openDBItem.addActionListener(new SearchBooksActionListener());
//        this.add(openDBItem);
//        openDBItem.setEnabled(false); // not until the user logs in

        manageBooksItem = new JMenuItem(MENU_ITEM_MANAGE_BOOKS);
        manageBooksItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, toolkit.getMenuShortcutKeyMask()));
        manageBooksItem.addActionListener(new ManageBooksActionListener());
        add(manageBooksItem);
        manageBooksItem.setEnabled(false);
        
        updateFamilyNameItem = new JMenuItem(MENU_ITEM_UPDATE_NAME_FAMILY);
        updateFamilyNameItem.addActionListener(new UpdateFamilyNameActionListener());
        add(updateFamilyNameItem);
        updateFamilyNameItem.setEnabled(false);           
        
        updateCommentItem = new JMenuItem(MENU_ITEM_UPDATE_COMMENT);
        updateCommentItem.addActionListener(new UpdateCommentsActionListener());
        add(updateCommentItem);
        updateCommentItem.setEnabled(false);        
        
        
        saveDBItem = new JMenuItem(MENU_ITEM_SAVE_TO_DB);
        saveDBItem.addActionListener(new SaveBookActionListener());
        add(saveDBItem);
        saveDBItem.setEnabled(false);

        this.addSeparator();
        
        viewOmittedAnnotInfoItem = new JMenuItem(MENU_ITEM_VIEW_ANNOT_INFO);
        viewOmittedAnnotInfoItem.addActionListener(new ViewOmittedAnnotActionListener());
        add(viewOmittedAnnotInfoItem);
        viewOmittedAnnotInfoItem.setEnabled(false);
        
        viewTaxonomyViolatoinInfoItem = new JMenuItem(MENU_ITEM_VIEW_TAXONOMY_VIOLATION_INFO);
        viewTaxonomyViolatoinInfoItem.addActionListener(new ViewTaxomomyViolationInfoListener());
        add(viewTaxonomyViolatoinInfoItem);
        viewTaxonomyViolatoinInfoItem.setEnabled(false);
        
        viewUpdateHistoryItem = new JMenuItem(MENU_ITEM_VIEW_ANNOT_HISTORY_INFO);
        viewUpdateHistoryItem.addActionListener(new ViewAnnotHistoryActionListener());
        add(viewUpdateHistoryItem);
        viewUpdateHistoryItem.setEnabled(false);

                // Add save functon later on if required
//		saveFileLocalItem = new JMenuItem(save_annots);
//		saveFileLocalItem.addActionListener(new SaveToFileActionListener());
//		this.add(saveFileLocalItem);
        updateMenu();

        EventManager.inst().registerGeneAnnotationChangeListener(this);
        EventManager.inst().registerFamilyListener(this);

//		instances.add(this);
    }

    public void updateMenu() {
        saveDBItem.setEnabled(DirtyIndicator.inst().bookUpdated());
        updateCommentItem.setEnabled(DirtyIndicator.inst().bookUpdated());
        updateFamilyNameItem.setEnabled(DirtyIndicator.inst().bookUpdated());
        viewOmittedAnnotInfoItem.setEnabled(DirtyIndicator.inst().bookUpdated());
        viewTaxonomyViolatoinInfoItem.setEnabled(DirtyIndicator.inst().bookUpdated());
        viewUpdateHistoryItem.setEnabled(DirtyIndicator.inst().bookUpdated());
//		openDBItem.setEnabled(InternetChecker.getInstance().isConnectionPresent(true));
//
//		boolean family_loaded = DirtyIndicator.inst().familyLoaded();
//		saveFileLocalItem.setEnabled(family_loaded);

    }

    public boolean saveCurrent() {
        
        return PaintManager.inst().saveCurrent();

        // Nothing here for now
        //return true;

    }

    @Override
    public void newFamilyData(FamilyChangeEvent e) {
        this.updateCommentItem.setEnabled(true);
        this.updateFamilyNameItem.setEnabled(true);
        this.saveDBItem.setEnabled(true);
        this.viewOmittedAnnotInfoItem.setEnabled(true);
        this.viewTaxonomyViolatoinInfoItem.setEnabled(true);
        this.viewUpdateHistoryItem.setEnabled(true);
    }

    private class LoginActionListener implements ActionListener {

        FileMenu fileMenu;
        JMenuItem menuItem;

        public LoginActionListener(FileMenu fileMenu, JMenuItem menuItem) {
            this.fileMenu = fileMenu;
            this.menuItem = menuItem;
        }

        /**
         * Method declaration
         *
         *
         * @param e
         *
         * @see
         */
        public void actionPerformed(ActionEvent e) {
            // Save current document, if necessary and close.  Once logged in, user may be in a different operation mode
            if (DirtyIndicator.inst().bookUpdated() && null != PaintManager.inst().getFamily()) {
                int dialogResult = JOptionPane.showConfirmDialog(GUIManager.getManager().getFrame(), "Book has been updated, do you want to save?", "Book Updated", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (dialogResult == JOptionPane.YES_OPTION) {
                    saveCurrent();
                }
            }
            DirtyIndicator.inst().setAnnotated(false);

            LoginDlg dlg = new LoginDlg(GUIManager.getManager().getFrame());

            // First get user login information
            Vector results = dlg.display();

            if (null == results) {
                System.out.println("No login information to verify, returning");
                return;
            }
            if (true == results.isEmpty()) {
                System.out.println("No login information to verify, returning");                
                return;
            }
            System.out.println("Sending user login information with " + results.size() + " elements ");
//            for (int i = 0; i <  results.size(); i++) {
//                Object cur = results.get(i);
//                if (null == cur) {
//                    System.out.println("login item " + i + " is null");
//                }
//                else {
//                    System.out.println("login item " + i + " is not null");
//                }
//            }

            PantherServer pServer = PantherServer.inst();
            User user = pServer.getUserInfo(Preferences.inst().getPantherURL(), results);
            if (null == user) {
                JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Unable to verify user information", "User Information", JOptionPane.ERROR_MESSAGE);
                
                System.exit(-1);
            }
            if (user.getprivilegeLevel() < Constant.USER_PRIVILEGE_SAVE_LOCAL) {
                JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "User does not have privilege to lock and save books", "User privilege warning", JOptionPane.WARNING_MESSAGE);
            }
            PaintManager pm = PaintManager.inst();
            pm.setUser(user);
            pm.setUserInfo(results);
            loginItem.setEnabled(false);
            logoffItem.setEnabled(true);
//            openDBItem.setEnabled(true);
            manageBooksItem.setEnabled(true);
            
        }
    }

    /**
     * Class declaration
     *
     *
     * @author
     * @version %I%, %G%
     */
    private class LogoffActionListener implements ActionListener {

        FileMenu fileMenu;
        JMenuItem menuItem;

        public LogoffActionListener(FileMenu fileMenu, JMenuItem menuItem) {
            this.fileMenu = fileMenu;
            this.menuItem = menuItem;
        }

        /**
         * Method declaration
         *
         *
         * @param e
         *
         * @see
         */
        public void actionPerformed(ActionEvent e) {
            if (DirtyIndicator.inst().bookUpdated() && null != PaintManager.inst().getFamily()) {
                int dialogResult = JOptionPane.showConfirmDialog(GUIManager.getManager().getFrame(), "Book has been updated, do you want to save?", "Book Updated", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (dialogResult == JOptionPane.YES_OPTION) {
                    saveCurrent();
                }
            }
            DirtyIndicator.inst().setAnnotated(false);
            
            PaintManager.inst().setUser(null);
            loginItem.setEnabled(true);
            logoffItem.setEnabled(false);
//            openDBItem.setEnabled(false);
            manageBooksItem.setEnabled(false);
            saveDBItem.setEnabled(false);
            updateCommentItem.setEnabled(false);
            updateFamilyNameItem.setEnabled(false);
            viewOmittedAnnotInfoItem.setEnabled(false);
            viewTaxonomyViolatoinInfoItem.setEnabled(false);
            viewUpdateHistoryItem.setEnabled(false);
        }
    }
    
    private class UpdateFamilyNameActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            PaintManager pm = PaintManager.inst();
            String curFamilyName = pm.getFamily().getName();
            JTextArea ta = new JTextArea(1, 40);
            if (null != curFamilyName) {
                ta.setText(curFamilyName);
            }
            
            ta.setWrapStyleWord(true);
            ta.setLineWrap(true);
            ta.setCaretPosition(0);
            ta.setEditable(true);

            JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), new JScrollPane(ta), "Family Name", JOptionPane.INFORMATION_MESSAGE);
            String newFamilyName = ta.getText();
            if (null != newFamilyName) {
                newFamilyName.trim();
                if (0 == newFamilyName.length()) {
                    newFamilyName = null;
                }
            }
            if (null != curFamilyName && false == curFamilyName.equals(newFamilyName)) {
                pm.getFamily().setName(newFamilyName);
            }
            else if (null != newFamilyName && false == newFamilyName.equals(curFamilyName)) {
                pm.getFamily().setName(newFamilyName);
            }
            pm.setTitle();
            DirtyIndicator.inst().setAnnotated(true);
            EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(pm.getTree().getRoot()));
        }
    }
    
    private static final String LINE_BREAK = "\\\\n";
    private static final String LINE_SEPARATOR_SYSTEM_PROPERY = System.getProperty("line.separator");
    private static final String STR_EMPTY = "";
    
    private class ViewOmittedAnnotActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            PaintManager pm = PaintManager.inst();            
            StringBuffer errorBuf = pm.getFamily().getNodeInfoBuf();
            if (null == errorBuf) {
                errorBuf = new StringBuffer(STR_EMPTY);
            }
            String info = errorBuf.toString().replaceAll(LINE_BREAK, LINE_SEPARATOR_SYSTEM_PROPERY);
            JTextArea ta = new JTextArea(20, 100);
            if (null != info) {
               ta.setText(info);
            }
            ta.setEditable(false);
            ta.setWrapStyleWord(true);
            ta.setLineWrap(true);
            ta.setCaretPosition(0);
            JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), new JScrollPane(ta), "Additional information about annotations", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private class ViewTaxomomyViolationInfoListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {        
            PaintManager pm = PaintManager.inst();            
            ArrayList<String> msgList = pm.getTaxonmomyWarnings();
            
            String info = String.join(LINE_SEPARATOR_SYSTEM_PROPERY, msgList);
            JTextArea ta = new JTextArea(20, 100);
            if (null != info) {
               ta.setText(info);
            }
            ta.setEditable(false);
            ta.setWrapStyleWord(true);
            ta.setLineWrap(true);
            ta.setCaretPosition(0);
            JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), new JScrollPane(ta), "Taxomomy violation information", JOptionPane.INFORMATION_MESSAGE);
        }        
    }
    private class ViewAnnotHistoryActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            PaintManager pm = PaintManager.inst();            
            String upateHistory = pm.getUpdateHistory();
            if (null == upateHistory) {
                upateHistory = STR_EMPTY;
            }
            String info = upateHistory.toString().replaceAll(LINE_BREAK, LINE_SEPARATOR_SYSTEM_PROPERY);
            JTextArea ta = new JTextArea(20, 100);
            if (null != info) {
               ta.setText(info);
            }
            ta.setEditable(false);
            ta.setWrapStyleWord(true);
            ta.setLineWrap(true);
            ta.setCaretPosition(0);
            JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), new JScrollPane(ta), "Curation history information", JOptionPane.INFORMATION_MESSAGE);
        }
    }    
    
    
    
    private class UpdateCommentsActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            PaintManager pm = PaintManager.inst();
            String curComment = pm.getCuratorNotes();
            if (null != curComment) {
                curComment = curComment.replaceAll(LINE_BREAK, LINE_SEPARATOR_SYSTEM_PROPERY);
            }
            JTextArea ta = new JTextArea(20, 100);
            if (null != curComment) {
               ta.setText(curComment);
            }
          
            ta.setWrapStyleWord(true);
            ta.setLineWrap(true);
            ta.setCaretPosition(0);
            ta.setEditable(true);

            JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), new JScrollPane(ta), "Curator Notes", JOptionPane.INFORMATION_MESSAGE);
            String newComment = ta.getText();
            if (null != newComment) {
                newComment = newComment.trim();
                if (0 == newComment.length()) {
                    newComment = null;
                }
            }
            if (null != curComment && false == curComment.equals(newComment)) {
                pm.setCuratorNotes(newComment);
            }
            else if (null != newComment && false == newComment.equals(curComment)) {
                pm.setCuratorNotes(newComment);
            }
            DirtyIndicator.inst().setAnnotated(true);
            EventManager.inst().fireCommentChangeEvent(new CommentChangeEvent(this));
        }
    }

    private class ManageBooksActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            if (DirtyIndicator.inst().bookUpdated() && null != PaintManager.inst().getFamily()) {
                int dialogResult = JOptionPane.showConfirmDialog(GUIManager.getManager().getFrame(), "Book has been updated, do you want to save?", "Book Updated", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (dialogResult == JOptionPane.YES_OPTION) {
                    saveCurrent();
                }
            }
            DirtyIndicator.inst().setAnnotated(false);

            ManageBooksDlg dlg = new ManageBooksDlg(GUIManager.getManager().getFrame(), Preferences.inst().getPantherURL(), PaintManager.inst().getUserInfo());
            String bookId = dlg.display();
            if (null == bookId) {
                return;
            }
            User user = PaintManager.inst().getUser();
            
            if (null == user || user.getprivilegeLevel() < Constant.USER_PRIVILEGE_SAVE_LOCAL) {
                int dialogResult = JOptionPane.showConfirmDialog(GUIManager.getManager().getFrame(), "User does not have privilege to lock and save books, continue?", "User privilege warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (dialogResult != JOptionPane.YES_OPTION) {
                    return;
                }                
            }
            User lockedBy = dlg.getLockedBy();
            if (null != user && null != lockedBy && false == user.getloginName().equals(lockedBy.getLoginName())) {
                int dialogResult = JOptionPane.showConfirmDialog(GUIManager.getManager().getFrame(), "Book is locked by another user " + lockedBy.getLoginName() + ", you will not be able to save changes.  Continue?", "Book is already locked by another user", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (dialogResult != JOptionPane.YES_OPTION) {
                    return;
                }                 
            }
            PaintManager.inst().closeCurrent();

            //                      Open book for user
            PaintManager.inst().openNewFamily(bookId);
            updateMenus();

        }
    }
    
    private class SaveBookActionListener implements ActionListener {
         public void actionPerformed(ActionEvent e) {
             
            PaintManager.inst().saveCurrent();
             
             
             
//             // TESTING
//             List<GeneNode> nodes = PaintManager.inst().getTree().getAllNodes();
//             HashMap<String, Node> nodeLookup = new HashMap<String, Node>();
//             for (GeneNode gn: nodes) {
//                 nodeLookup.put(gn.getNode().getStaticInfo().getNodeAcc(), gn.getNode());
//             }
//
//                try {
//                    FileOutputStream fout = new FileOutputStream("C:\\Temp\\new_paint\\" + PaintManager.inst().getFamily().getFamilyID() + "_nodes.ser");
//                    ObjectOutputStream oos = new ObjectOutputStream(fout);
//                    oos.writeObject(nodeLookup);
//                }
//                catch (Exception ex) {
//
//                }
             
             
             
             
             
             
             
         }
    }

    private class SearchBooksActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (DirtyIndicator.inst().bookUpdated() && null != PaintManager.inst().getFamily()) {
                int dialogResult = JOptionPane.showConfirmDialog(GUIManager.getManager().getFrame(), "Book has been updated, do you want to save?", "Book Updated", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (dialogResult == JOptionPane.YES_OPTION) {
                    saveCurrent();
                }
            }
            DirtyIndicator.inst().setAnnotated(false);

            NewFamily dlg = new NewFamily(GUIManager.getManager().getFrame());
            String familyID = dlg.display();
            if (familyID != null) {
                // Open book for user
                //							PaintManager.inst().closeCurrent();
                PaintManager.inst().openNewFamily(familyID);
                updateMenus();
            }

        }
    }

    private static class SaveToFileActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            ActiveFamily dlg = new ActiveFamily(GUIManager.getManager().getFrame());

            File f = dlg.getSelectedFile(true, null);
            if (f != null) {
                try {
                    // returns the full filename, including the path, for the PTHR*****.paint file
                    String paintfile = FileNameGenerator.formatPAINTFileName(f.getCanonicalPath());
                    FileAdapter dt = new FileAdapter(paintfile);
                    dt.saveOutput();
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Unable to save file " + f);
                }
            }
        }
    }



    @Override
    public void handleAnnotationChangeEvent(AnnotationChangeEvent event) {
        updateMenu();
    }

    private static void updateMenus() {
        for (FileMenu f : instances) {
            f.updateMenu();
        }
    }
}
