/* 
 * 
 * Copyright (c) 2010, Regents of the University of California 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Neither the name of the Lawrence Berkeley National Lab nor the names of its contributors may be used to endorse 
 * or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package org.paint.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.paint.config.PantherDbInfo;
import org.paint.config.Preferences;
import org.paint.dataadapter.PantherServer;
import org.paint.util.LoginUtil;
import org.paint.util.SpringUtilities;

import com.sri.panther.paintCommon.Book;
import java.util.ArrayList;


public class NewFamily extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel openFamilyPanel;
	private JPanel booksPanel;
	private JPanel bookListPanel;

	private JTextField searchTerm;
	private JRadioButton geneSymbolBtn;
	private JRadioButton geneIdentifierBtn;
	private JRadioButton proteinIdentifierBtn;
	private JRadioButton definitionBtn;

	protected JComboBox pickBook;

	protected JTable familyTable;

	protected JButton openBtn;
	protected JButton submitBtn;
	
	private JLabel status_message;

	private static final String MSG_PLEASE_ENTER_SEARCH_TERM = "Please enter a search term";
	private static final String MSG_SERVER_ERROR_CANNOT_SEARCH_BOOKS =  "Server returned error, cannot search for protein family";
	private static final String MSG_NO_BOOKS_FOR_SEARCH_CRITERIA = "No protein families found matching search criteria";

	private static final String LABEL_ENTER = "Search for";
	private static final String LABEL_SEARCH = "Search";
	private static final String LABEL_CANCEL = "Cancel";
	private static final String LABEL_OPEN = "Open";
	private static final String LABEL_PICK_ID = "Or Pick family ID";

	private static final String LABEL_SEARCH_GENE_SYMBOL = "Gene Symbol";
	private static final String LABEL_SEARCH_GENE_IDENTIFIER = "Gene Identifier";
	private static final String LABEL_SEARCH_PROTEIN_IDENTIFIER = "Protein Identifier";
	private static final String LABEL_SEARCH_DEFINITION = "Definition (partial def supported)";
	private static final String LABEL_TITLE = "Open Protein Family";;

	private static final String[] COLUMN_NAMES = {"PANTHER Id", "Protein Family Name"};

	private ArrayList<Book> bookList;

	String openBookId = null;

	private static Logger log = Logger.getLogger(NewFamily.class);

	public NewFamily(Frame frame) {
		super(frame, true);
		setTitle(LABEL_TITLE);
		bookList = new ArrayList<Book> ();

		openFamilyPanel = new JPanel();
		openFamilyPanel.setLayout(new BorderLayout());

		openFamilyPanel.add(initializeChoicePanel(), BorderLayout.NORTH);
		openFamilyPanel.add(initializeBookListPanel(), BorderLayout.CENTER);
		openFamilyPanel.add(initializeControlPanel(), BorderLayout.SOUTH);

		setContentPane(openFamilyPanel);
		
		pack();

		setLocationRelativeTo(frame);
	}

	private JPanel initializeChoicePanel() {
		JPanel choicePanel = new JPanel();
		choicePanel.setLayout(new SpringLayout());
		
		/*
		 *  the user may search for a book
		 */
		JLabel label = new JLabel(LABEL_ENTER);
		choicePanel.add(label);
		searchTerm = new JTextField(20);
		searchTerm.addActionListener(new BooksListActionListener());
		label.setLabelFor(searchTerm);
		choicePanel.add(searchTerm);
		
		/* 
		 * And they have options on what is the key for the search
		 */
		choicePanel.add(getSearchButton());
		choicePanel.add(getSearchTypePanel());
		
		/*
		 * Or, the user may pick a book by name
		 */
		label = new JLabel(LABEL_PICK_ID);
		choicePanel.add(label);
		JComboBox bookBox = getBookBox();
		label.setLabelFor(bookBox);
		choicePanel.add(bookBox);
		
		//Lay out the panel.
		SpringUtilities.makeCompactGrid(choicePanel,
		                                3, 2, //rows, cols
		                                6, 6,        //initX, initY
		                                6, 6);       //xPad, yPad

		return choicePanel;
	}

	private JComboBox getBookBox() {		
		pickBook = new JComboBox(getBooks());
		pickBook.setEditable(true);
		pickBook.setMaximumRowCount(10);
		pickBook.setName(LABEL_PICK_ID);
		pickBook.setAlignmentY(Component.TOP_ALIGNMENT);
		pickBook.addActionListener(this);
		return pickBook;
	}

	private JPanel getSearchButton() {
		submitBtn = new JButton(LABEL_SEARCH);
		submitBtn.addActionListener(new BooksListActionListener());
		JPanel searchBtnPanel = new JPanel();
		searchBtnPanel.setLayout(new BoxLayout(searchBtnPanel, BoxLayout.LINE_AXIS));
		searchBtnPanel.add(submitBtn);
		return searchBtnPanel;
	}
	
	private JPanel getSearchTypePanel() {
		// Search Type panel
		JPanel searchTypePanel = new JPanel(new GridLayout(4, 1));
		geneSymbolBtn = new JRadioButton(LABEL_SEARCH_GENE_SYMBOL);
		geneIdentifierBtn = new JRadioButton(LABEL_SEARCH_GENE_IDENTIFIER);
		proteinIdentifierBtn = new JRadioButton(LABEL_SEARCH_PROTEIN_IDENTIFIER);
		definitionBtn = new JRadioButton(LABEL_SEARCH_DEFINITION);

		geneSymbolBtn.setSelected(true);

		ButtonGroup bg = new ButtonGroup();
		bg.add(geneSymbolBtn);
		bg.add(geneIdentifierBtn);
		bg.add(proteinIdentifierBtn);
		bg.add(definitionBtn);
		
		searchTypePanel.add(geneSymbolBtn);
		searchTypePanel.add(geneIdentifierBtn);
		searchTypePanel.add(proteinIdentifierBtn);
		searchTypePanel.add(definitionBtn);

		return searchTypePanel;
		
	}

	protected void initializeBooksList(ArrayList<Book> books) {
		BookTableModel book_list = new BookTableModel(books, COLUMN_NAMES);
		familyTable.setModel(book_list);
		book_list.fireTableChanged(new TableModelEvent(book_list));
	}

	protected JPanel initializeBookListPanel() {
		bookListPanel = new JPanel();
		bookListPanel.setLayout(new BoxLayout(bookListPanel, BoxLayout.Y_AXIS));

		familyTable = new JTable();
		initializeBooksList(bookList);
		familyTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
		familyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane familiesScrollPane = new JScrollPane(familyTable);
		familyTable.addMouseListener(getMouseListener());

		JLabel lockedBooksLabel = new JLabel("Search Results");
		lockedBooksLabel.setPreferredSize(lockedBooksLabel.getPreferredSize());

		bookListPanel.add(lockedBooksLabel);
		bookListPanel.add(familiesScrollPane);

		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BoxLayout(containerPanel,
				BoxLayout.Y_AXIS));
		containerPanel.add(bookListPanel);

		booksPanel = new JPanel();
		booksPanel.setLayout(new BorderLayout());
		booksPanel.add(containerPanel, BorderLayout.WEST);

		return booksPanel;
	}

	private JPanel initializeControlPanel() {
		// Search Term panel
		JButton cancelBtn = new JButton(LABEL_CANCEL);
		cancelBtn.addActionListener(this);
		openBtn = new JButton(LABEL_OPEN);
		openBtn.addActionListener(this);
		openBtn.setEnabled(bookList.size() > 0);

		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(cancelBtn);
		buttonPanel.add(openBtn);
		
		status_message = new JLabel();
		status_message.setText(null);
		
		controlPanel.add(buttonPanel, BorderLayout.NORTH);
		controlPanel.add(status_message, BorderLayout.SOUTH);
		
		return controlPanel;
	}

	private MouseListener getMouseListener() {
		MouseListener mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int row = familyTable.rowAtPoint(e.getPoint());
					openBook(row);
				} else {
					openBtn.setEnabled(familyTable.getSelectedRow() >= 0);
				}
			}
		};
		return mouseListener;
	}

	private class BookTableModel extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		protected ArrayList<Book> bookList = null;
		protected String[] columnNames = null;

		BookTableModel(ArrayList<Book> bookList, String columnNames[]) {
			this.bookList = bookList;
			this.columnNames = columnNames;
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

		public int getRowCount() {
			return bookList.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Book book = bookList.get(rowIndex);
			if (columnIndex == 0) {
				return book.getId();
			} else {
				return book.getName();
			}
		}

	}

	public class BooksListActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			status_message.setText("");
			String searchStr = NewFamily.this.searchTerm.getText();
			if (null == searchStr || 0 == searchStr.length()) {
				status_message.setText(MSG_PLEASE_ENTER_SEARCH_TERM);
				return;
			}

			Vector<String> sendInfo = new Vector<String> (2);
			sendInfo.add(searchStr);
			sendInfo.add(PantherDbInfo.getDbAndVersionKey());
			String servletUrl = Preferences.inst().getPantherURL();
			bookList.clear();
			ArrayList<Book> infoFromServer = null;
			submitBtn.setSelected(true);
			PantherServer server = PantherServer.inst();
			
			if (NewFamily.this.geneSymbolBtn.isSelected()) {
				infoFromServer = server.searchGeneName(servletUrl, sendInfo, null, null);
			} else if (NewFamily.this.geneIdentifierBtn.isSelected()) {
				infoFromServer = server.searchGeneExtId(servletUrl, sendInfo, null, null);
			} else if (NewFamily.this.proteinIdentifierBtn.isSelected()) {
				infoFromServer = server.searchProteinExtId(servletUrl, sendInfo, null, null);
			}
			else {
				infoFromServer = server.searchDefinition(servletUrl, sendInfo, null, null); 
			}
			if ((null == infoFromServer) || (0 == infoFromServer.size())){
				status_message.setText(MSG_SERVER_ERROR_CANNOT_SEARCH_BOOKS);
				NewFamily.this.initializeBooksList(bookList);
				submitBtn.setSelected(false);
				return;
			}

//			String  errorMsg = (String) infoFromServer.elementAt(0);
//
//			if (0 != errorMsg.length()){
//				status_message.setText(errorMsg);
//				NewFamily.this.initializeBooksList(bookList);
//				submitBtn.setSelected(false);
//				return;
//			}

			bookList = infoFromServer;

			int numBooks = bookList.size();
			if (0 == numBooks) {
				status_message.setText(MSG_NO_BOOKS_FOR_SEARCH_CRITERIA);
			}

			NewFamily.this.initializeBooksList(bookList);
			submitBtn.setSelected(false);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (LABEL_OPEN.equals(e.getActionCommand())) {
			int row = familyTable.getSelectedRow();
			if (row >= 0) {
				openBook(row);
			}
		} else if (e.getActionCommand().equals(LABEL_CANCEL)) {
			openBookId = null;
			this.setVisible(false);       	
		} else if (e.getActionCommand().equals("comboBoxChanged")){
			if (pickBook.getSelectedIndex() >= 0){
				String book_id = (String) pickBook.getSelectedItem();
				Book fake_book = new Book(book_id, "", 0, null);
				bookList.clear();
				bookList.add(fake_book);
				initializeBooksList(bookList);
			}
		}
	}

	protected void openBook(int row) {
		this.setVisible(false);
		Book book = bookList.get(row);
		openBookId = book.getId();
	}

	public String display() {
		setVisible(true);
		return openBookId;
	}

	private String[]  getBooks() {
		
            ArrayList<Book> books = PantherServer.inst().searchAllBooks(Preferences.inst().getPantherURL(), new Vector(), null, null);
            if (null == books) {
                return new String[0];
            }
            String[] bookArray = new String[books.size()];
            for (int i = 0; i < books.size(); i++) {
                bookArray[i] = books.get(i).getId();
            }
            return bookArray;

	}

}