/**
 *  Copyright 2016 University Of Southern California
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

package org.paint.dialog;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class LoginDlg extends JDialog{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Frame frame;
	JPanel mainPanel;
	JTextField userName;
	JPasswordField password;
	Vector userInfo;

	public LoginDlg(Frame frame) {
		super(frame, true);
		setTitle("Login ");
		this.frame = frame;
		userInfo = null;
		initializePanel();
	}

	protected void initializePanel() {
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				setVisible(false);
			}
		};
		// Create label and field panels
		JPanel labelPane = new JPanel();
		labelPane.setLayout(new GridLayout(0, 1));
		labelPane.add(new JLabel("User name:"));
		labelPane.add(new JLabel("Password:"));

		JPanel fieldPane = new JPanel();
		fieldPane.setLayout(new GridLayout(0, 1));
		userName = new JTextField(10);
		//userName.setText("gouser");
		
		password = new JPasswordField(10);
		password.setText("welcome");
		fieldPane.add(userName);
		fieldPane.add(password);

		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(labelPane, BorderLayout.CENTER);
		mainPanel.add(fieldPane, BorderLayout.EAST);

		// Create decision panel
		JPanel decisionPanel = new JPanel();
		decisionPanel.setLayout(new BoxLayout(decisionPanel, BoxLayout.X_AXIS));
		JButton saveChoices = new JButton("OK");
		saveChoices.addActionListener(new OKButtonActionListener());
		JButton cancelChoices = new JButton("Cancel");
		cancelChoices.addActionListener(new CancelButtonActionListener());
		decisionPanel.add(saveChoices);
		decisionPanel.add(cancelChoices);

		mainPanel.add(decisionPanel, BorderLayout.SOUTH);
		getRootPane().setDefaultButton(saveChoices);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		getRootPane().registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		setContentPane(mainPanel);
		Rectangle r = frame.getBounds();
		setBounds(r.x + r.width / 2, r.y + r.height / 2, 300, 100);
	}

	public Vector<String> display() {
		setVisible(true);
		return userInfo;
	}

	protected void saveUserInfo() {
		userInfo = new Vector<String>();
		userInfo.addElement(userName.getText());
		userInfo.addElement(password.getPassword());
	}

	private class OKButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			LoginDlg.this.saveUserInfo();
			LoginDlg.this.setVisible(false);
		}
	}

	private class CancelButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			userInfo = new Vector<String>();
			LoginDlg.this.setVisible(false);
		}
	}
}
