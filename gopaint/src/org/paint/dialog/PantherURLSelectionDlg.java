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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.paint.config.PantherDbInfo;
import org.paint.config.Preferences;
import org.paint.dataadapter.PantherServer;
import org.paint.util.LoginUtil;
import org.paint.util.StringConstant;

import com.sri.panther.paintCommon.FixedInfo;

public class PantherURLSelectionDlg extends JDialog{
	private FixedInfo fi;

	private JTable table;
	private JTextField panther_url;
	private JButton saveChoices;
	private JLabel status;

	public PantherURLSelectionDlg(Frame frame) {
		super(frame, true);
		setTitle("Panther DB URL Selection");

		JPanel mainPanel = new JPanel();
		BoxLayout layout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
		mainPanel.setLayout(layout);

		JPanel versionPanel = initVersionPanel();
		JPanel decisionPanel = initButtonPanel();

		//		mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		mainPanel.add(Box.createRigidArea(new Dimension(0,6)));
		mainPanel.add(versionPanel);
		mainPanel.add(Box.createRigidArea(new Dimension(0,6)));
		mainPanel.add(decisionPanel);

		setContentPane(mainPanel);
		getRootPane().setDefaultButton(saveChoices);

		pack();
		setLocationRelativeTo(frame);
		//		Rectangle r = frame.getBounds();
		//		setBounds(r.x + r.width / 2, r.y + r.height / 2, 400, 200);
	}

	private JPanel initVersionPanel() {
		JLabel url_label = new JLabel("URL");
		JLabel vers_label = new JLabel("Versions");
		status = new JLabel("");
		status.setForeground(Color.red);

		panther_url = new JTextField(Preferences.inst().getPantherURL());
		panther_url.addActionListener(new SetURLActionListener());

		panther_url.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);

		panther_url.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				if(evt.getKeyCode() == KeyEvent.VK_TAB) {
					updateURL();
				}
			}
		});

		fi = PantherDbInfo.getFixedInfo();
		URLTableModel model = new URLTableModel(PantherDbInfo.getVersions());
		table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setColumnSelectionAllowed(false);
		int row = model.findInitialRow();
		table.setRowSelectionInterval(row, row);
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);

		url_label.setLabelFor(panther_url);
		vers_label.setLabelFor(scrollPane);

		JPanel label_pane = new JPanel();
		label_pane.setLayout(new BoxLayout(label_pane, BoxLayout.Y_AXIS));
		label_pane.add(Box.createVerticalStrut(12));
		label_pane.add(url_label);
		label_pane.add(Box.createVerticalGlue());
		label_pane.add(vers_label);
		label_pane.add(Box.createVerticalStrut(12));

		JPanel select_pane = new JPanel();
		select_pane.setLayout(new BoxLayout(select_pane, BoxLayout.Y_AXIS));
		select_pane.add(Box.createHorizontalStrut(12));
		select_pane.add(status);		select_pane.add(Box.createVerticalStrut(12));
		select_pane.add(panther_url);
		select_pane.add(Box.createVerticalGlue());
		select_pane.add(scrollPane);
		select_pane.add(Box.createVerticalStrut(12));

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(Box.createHorizontalStrut(12));
		panel.add(label_pane);
		panel.add(Box.createHorizontalStrut(12));
		panel.add(select_pane);
		panel.add(Box.createHorizontalStrut(12));

		return panel;
	}

	private JPanel initButtonPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		saveChoices = new JButton("OK");
		saveChoices.addActionListener(new OKButtonActionListener());
		JButton cancelChoices = new JButton("Cancel");
		cancelChoices.addActionListener(new CancelButtonActionListener());
		panel.add(Box.createHorizontalGlue());
		panel.add(saveChoices);
		panel.add(Box.createHorizontalStrut(12));
		panel.add(cancelChoices);
		panel.add(Box.createHorizontalGlue());
		return panel;
	}

	public void display() {
		setVisible(true);
	}

	protected void saveChoice() {
		if (updateURL()) {
			LoginUtil.logout();
			int row = table.getSelectedRow();
			String pantherURL = panther_url.getText();
			Preferences.inst().setPantherURL(pantherURL);
			String selection = table.getValueAt(row, 0) + StringConstant.PIPE + table.getValueAt(row, 1);
//			Preferences.inst().setUploadVersion(selection);
			PantherDbInfo.setFixedInfo(fi);
			LoginUtil.login();
		} 
	}

	private class SetURLActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			updateURL();
		}
	}

	private boolean updateURL() {
		String pantherURL = panther_url.getText();
		fi = PantherServer.inst().getFixedInfoFromServer(pantherURL);
		URLTableModel model = (URLTableModel) table.getModel();
		int row;
		if (fi != null) {
			status.setText("");
			row = model.setData((Hashtable<String, Hashtable<String, Vector<String>>>) fi.getDbToUploadInfo());
		} 
		else {
			status.setText(PantherServer.inst().getServerStatus());
			row = model.setData(null);
		}
		if (row >= 0)
			table.setRowSelectionInterval(row, row);
		return fi != null;
	}

	private class OKButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			PantherURLSelectionDlg.this.saveChoice();
			PantherURLSelectionDlg.this.setVisible(false);
		}
	}

	private class CancelButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			PantherURLSelectionDlg.this.setVisible(false);
		}
	}

	class URLTableModel extends AbstractTableModel {
		private String[] columnNames = {
				"Database",
				"Version",
				"Date"
		};

		String [][] rowData;

		public URLTableModel (Hashtable<String, Hashtable<String, Vector<String>>> dbToUPLTable) {
			super();
			setData (dbToUPLTable);
		}

		public int findInitialRow() {
			int selected_row = -1;

			if (rowData != null) {
				String currentDB = PantherDbInfo.getCurrentDB();
				String currentVersion = PantherDbInfo.getCurrentVersionName();
				for (int row = 0; row < rowData.length && selected_row < 0; row++) {
					if (rowData[row][0].equals(currentDB) && rowData[row][1].equals(currentVersion))
						selected_row = row;
				}
			}
			return selected_row;
		}

		public int setData (Hashtable<String, Hashtable<String, Vector<String>>> dbToUPLTable) {
			// Go through list of dbs and upls associated with each db
			if (dbToUPLTable == null) {
				rowData = null;
				return -1;
			}
			Set<String> db_names = dbToUPLTable.keySet();
			int row_count = 0;
			for (String db_name : db_names) {
				Hashtable<String, Vector<String>> versions = dbToUPLTable.get(db_name);
				Set<String> version_keys = versions.keySet();
				row_count += version_keys.size();
			}
			rowData = new String[row_count][3];
			int row = 0;
			int selected_row = 0;
			String currentDB = PantherDbInfo.getCurrentDB();
			String currentVersion = PantherDbInfo.getCurrentVersionName();
			int db_width = 0;
			int v_width = 0;
			for (String db_name : db_names) {
				if (db_name.length() > db_width)
					db_width = db_name.length();
				Hashtable<String, Vector<String>> versions = dbToUPLTable.get(db_name);
				Set<String> version_keys = versions.keySet();
				for (String v_key : version_keys) {
					rowData[row][0] = db_name;
					String vers_name = versions.get(v_key).firstElement();
					rowData[row][1] = vers_name;
					String vers_date = versions.get(v_key).lastElement();
					rowData[row][2] = vers_date != null ? vers_date : "";
					if (db_name.equals(currentDB) && vers_name.equals(currentVersion))
						selected_row = row;
					if (vers_name.length() > v_width)
						v_width = versions.get(v_key).firstElement().length();
					row++;
				}
			}
			fireTableDataChanged();
			return selected_row;
		}

		public String getColumnName(int col) {
			return columnNames[col].toString();
		}

		public int getRowCount() {
			if (rowData != null)
				return rowData.length; 
			else
				return 0;
		}

		public int getColumnCount() { 
			return columnNames.length; 
		}

		public Object getValueAt(int row, int col) {
			if (rowData != null)
				return rowData[row][col];
			else
				return null;
		}
	}

}