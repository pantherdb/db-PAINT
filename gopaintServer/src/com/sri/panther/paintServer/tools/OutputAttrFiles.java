package com.sri.panther.paintServer.tools;

import com.sri.panther.paintCommon.Book;
import com.sri.panther.paintCommon.util.FileUtils;
import com.sri.panther.paintServer.database.DataServer;
import com.sri.panther.paintServer.database.DataServerManager;

import com.usc.panther.paintServer.webservices.WSConstants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collections;

import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;


public class OutputAttrFiles extends JDialog {
        private JTextField tfUsername;
        private JPasswordField pfPassword;
        private JLabel lbUsername;
        private JLabel lbPassword;

        
        private JLabel lbDirectory;
        private JButton btnChooseDirectory;
        private JLabel start;
        private JLabel end;
        private JButton btnGenerateTrees;
        private JButton btnCancel;
        private boolean succeeded;
    
        public OutputAttrFiles(JFrame parent) {
            super(parent, "Write Tree Files", true);
            //
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints cs = new GridBagConstraints();
            
            cs.fill = GridBagConstraints.HORIZONTAL;
            
            lbUsername = new JLabel("Username: ");
            cs.gridx = 0;
            cs.gridy = 0;
            cs.gridwidth = 1;
            panel.add(lbUsername, cs);
            
            tfUsername = new JTextField(20);
            cs.gridx = 1;
            cs.gridy = 0;
            cs.gridwidth = 2;
            panel.add(tfUsername, cs);
            
            lbPassword = new JLabel("Password: ");
            cs.gridx = 0;
            cs.gridy = 1;
            cs.gridwidth = 1;
            panel.add(lbPassword, cs);
            
            pfPassword = new JPasswordField(20);
            cs.gridx = 1;
            cs.gridy = 1;
            cs.gridwidth = 2;
            panel.add(pfPassword, cs);
            
            
            panel.setBorder(new LineBorder(Color.GRAY));
            
            btnGenerateTrees = new JButton("Generate Trees");
            
            btnGenerateTrees.addActionListener(new ActionListener() {
            
                public void actionPerformed(ActionEvent e) {
    
                }
            });
            btnCancel = new JButton("Cancel");
            btnCancel.addActionListener(new ActionListener() {
            
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
            JPanel bp = new JPanel();
            bp.add(btnGenerateTrees);
            bp.add(btnCancel);
            
            getContentPane().add(panel, BorderLayout.CENTER);
            getContentPane().add(bp, BorderLayout.PAGE_END);
            
            pack();
            setResizable(false);
            setLocationRelativeTo(parent);
        }
            
        public String getUsername() {
            return tfUsername.getText().trim();
        }
            
        public String getPassword() {
            return new String(pfPassword.getPassword());
        }
            
        public boolean isSucceeded() {
            return succeeded;
        }
    

    private static DataServer ds = DataServerManager.getDataServer(WSConstants.PROPERTY_DB_STANDARD);
    private static final String LINE_RETURN = "\n";
    public static  void outputAttrFile(String userId, String userName, String password, String directory, int start, int end) throws Exception{
        // First get list of books from library
        Vector books = ds.getAllBooks(WSConstants.PROPERTY_CLS_VERSION);
        Collections.sort(books);
        for (int i = 0; i < books.size(); i++) {
            Book book = (Book)books.get(i);
            String bookId = book.getId();
            Vector<String[]> attrInfo = ds.getAttrTableAndSfInfo(bookId, WSConstants.PROPERTY_CLS_VERSION, userId, userName, password);
            String s[] = attrInfo.get(ds.INDEX_ATTR_METHOD_ATTR_TBL);
            if (null != s) {
                for (int j = start; j < end; j++) {
                    s[j] = s[j].trim() + LINE_RETURN;
                }
                FileUtils.writeFile(directory + bookId + ".tree", s);
                System.out.println("Finished writing book " + bookId + " " + i + " of " + books.size());
            }

        }

    }
    
    
    public static void main(String args[]) throws Exception {
        String userId = "goUserId";
        String userName = "gouserName";
        String password = "myPassword";
        int start = 24;
        int end = 7180;
        OutputAttrFiles.outputAttrFile(userId, userName, password, "C:\\Lib_9_trees\\", start, end);
    }
}
